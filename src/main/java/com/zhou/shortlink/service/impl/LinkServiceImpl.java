package com.zhou.shortlink.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.bloomfilter.BitMapBloomFilter;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhou.shortlink.component.DistributedLockFactory;
import com.zhou.shortlink.component.RLock;
import com.zhou.shortlink.config.WhiteDomainConfig;
import com.zhou.shortlink.config.mq.RabbitMqHelper;
import com.zhou.shortlink.constant.MqConstants;
import com.zhou.shortlink.constant.RedisConstants;
import com.zhou.shortlink.domain.Link;
import com.zhou.shortlink.domain.LinkLogs;
import com.zhou.shortlink.domain.LinkToday;
import com.zhou.shortlink.domain.User;
import com.zhou.shortlink.domain.vo.CountVo;
import com.zhou.shortlink.enums.DeleteFlag;
import com.zhou.shortlink.enums.EnableStatus;
import com.zhou.shortlink.exceptions.BizException;
import com.zhou.shortlink.mapper.LinkLogsMapper;
import com.zhou.shortlink.mapper.LinkMapper;
import com.zhou.shortlink.mapper.LinkTodayMapper;
import com.zhou.shortlink.mapper.UserMapper;
import com.zhou.shortlink.service.LinkService;
import com.zhou.shortlink.util.ShortLinkUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

import static com.zhou.shortlink.constant.RedisConstants.SHORT_URL_KEY;

/**
 * @author 82518
 * @description 针对表【link】的数据库操作Service实现
 * @createDate 2024-06-07 23:30:47
 */
@Service
public class LinkServiceImpl extends ServiceImpl<LinkMapper, Link>
        implements LinkService {

    static final int capacity = 1000;

    private final BitMapBloomFilter filter = new BitMapBloomFilter(capacity);

    @Resource
    UserMapper userMapper;

    @Value("short.domain")
    private String domain;

    @Resource
    DistributedLockFactory distributedLockFactory;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    WhiteDomainConfig whiteDomainConfig;


    @Resource
    RabbitMqHelper rabbitMqHelper;

    @Resource
    LinkLogsMapper linkLogsMapper;

    @Resource
    LinkTodayMapper linkTodayMapper;

    @Override
    public boolean add(Link link) {
        String originUrl = link.getOriginUrl();
        if (!confirmIsWhiteDomain(originUrl)) {
            throw new BizException("奇怪的链接");
        }

        Long loginId = StpUtil.getLoginIdAsLong();

        User user = userMapper.selectById(loginId);
        if (user == null) {
            throw new BizException("未找到用户信息");
        }
        // 存入默认启用
        link.setEnableStatus(EnableStatus.ENABLE);

        // 存入创建人和时间信息
        link.setCreateUserId(loginId).setCreateUserName(user.getRealName()).setCreateTime(LocalDateTime.now());
        link.setTotalPv(0).setTotalUv(0).setTotalUip(0);

        // 计算链接
        String shortUrlKey = ShortLinkUtils.shortUrl(originUrl);

        link.setDomain(domain);
        link.setShortUri(shortUrlKey);
        String fullShortUrl = String.format("%s/%s", domain, shortUrlKey);
        link.setFullShortUrl(fullShortUrl);

        boolean save = false;
        try {
            save = this.save(link);
        } catch (DuplicateKeyException e) {
            if (!filter.contains(fullShortUrl)) {
                filter.add(fullShortUrl);
            }
            throw new BizException(String.format("短链接：%s 生成重复", fullShortUrl));
        }

        Duration between = LocalDateTimeUtil.between(LocalDateTime.now(), link.getValidDate());
        long seconds = between.toSeconds();
        stringRedisTemplate.opsForValue().set(SHORT_URL_KEY + shortUrlKey, link.getOriginUrl(), seconds, TimeUnit.SECONDS);

        // 存入过滤器
        filter.add(fullShortUrl);
        return save;
    }

    @Override
    @Transactional
    public boolean updateLink(Link link) {
        Link byId = this.getById(link.getId());
        if (byId == null) {
            throw new BizException("未找到链接信息");
        }
        String originUrl = link.getOriginUrl();
        if (!confirmIsWhiteDomain(originUrl)) {
            throw new BizException("奇怪的链接");
        }

        if (byId.getDelFlag().equalsValue(DeleteFlag.DELETE.getValue())) {
            throw new BizException("该链接已被删除，不能操作");
        }

        RLock redisLock = distributedLockFactory.getRedisLock(RedisConstants.SHORT_URL_KEY_LOCK + link.getId());
        redisLock.lock();

        try {
            LocalDateTime now = LocalDateTime.now();
            link.setUpdateTime(now);
            // 如果源地址改变
            if (!byId.getOriginUrl().equals(link.getOriginUrl())) {
                Duration between = LocalDateTimeUtil.between(now, link.getValidDate());
                long seconds = between.toSeconds();

                // 计算链接
                String shortUrlKey = ShortLinkUtils.shortUrl(originUrl);
                String fullShortUrl = String.format("%s/%s", domain, shortUrlKey);

                link.setDomain(domain).setShortUri(shortUrlKey).setFullShortUrl(fullShortUrl);

                boolean update = false;
                try {
                    update = this.updateById(link);
                } catch (DuplicateKeyException e) {
                    if (!filter.contains(fullShortUrl)) {
                        filter.add(fullShortUrl);
                    }
                    // 记录日志或发送警报通知
                    log.error("短链接生成重复：" + fullShortUrl, e);
                    throw new BizException(String.format("短链接：%s 生成重复", fullShortUrl));
                }
                UpdateWrapper<LinkToday> linkTodayUpdateWrapper = new UpdateWrapper<>();
                linkTodayUpdateWrapper.set("full_short_url", fullShortUrl);
                linkTodayUpdateWrapper.eq("full_short_url", byId.getFullShortUrl());
                linkTodayUpdateWrapper.eq("del_flag", DeleteFlag.NO_DELETE);
                linkTodayMapper.update(linkTodayUpdateWrapper);

                UpdateWrapper<LinkLogs> logsUpdateWrapper = new UpdateWrapper<>();
                logsUpdateWrapper.set("full_short_url", fullShortUrl);
                logsUpdateWrapper.eq("full_short_url", byId.getFullShortUrl());
                logsUpdateWrapper.eq("del_flag", DeleteFlag.NO_DELETE);
                linkLogsMapper.update(logsUpdateWrapper);

                stringRedisTemplate.delete(SHORT_URL_KEY + byId.getShortUri());

                stringRedisTemplate.opsForValue().set(SHORT_URL_KEY + shortUrlKey, link.getOriginUrl(), seconds, TimeUnit.SECONDS);
                filter.add(fullShortUrl);
                return update;
            } else {
                return this.updateById(link);
            }
        } finally {
            redisLock.unlock();
        }
    }


    @Transactional
    @Override
    public boolean deleteLink(Long id) {
        RLock redisLock = distributedLockFactory.getRedisLock(RedisConstants.SHORT_URL_KEY_LOCK + id);
        redisLock.lock();
        String fullUrl = null;
        try {
            LocalDateTime currentDate = LocalDateTime.now();
            long timestamp = currentDate.toInstant(ZoneOffset.UTC).toEpochMilli();

            Link link = new Link();
            link.setId(id);
            link.setDelTime(timestamp);
            link.setDelFlag(DeleteFlag.DELETE);
            fullUrl = link.getFullShortUrl();


            this.updateById(link);
            UpdateWrapper<LinkLogs> logsUpdateWrapper = new UpdateWrapper<>();
            logsUpdateWrapper.eq("full_short_url", link.getFullShortUrl());
            logsUpdateWrapper.eq("del_flag", DeleteFlag.NO_DELETE);
            logsUpdateWrapper.set("del_flag", DeleteFlag.DELETE);

            linkLogsMapper.update(logsUpdateWrapper);

            UpdateWrapper<LinkToday> linkTodayUpdateWrapper = new UpdateWrapper<>();
            linkTodayUpdateWrapper.eq("full_short_url", link.getFullShortUrl());
            linkTodayUpdateWrapper.eq("del_flag", DeleteFlag.NO_DELETE);
            linkTodayUpdateWrapper.set("del_flag", DeleteFlag.DELETE);
            linkTodayMapper.update(linkTodayUpdateWrapper);

            return true;
        } catch (RuntimeException e) {
            log.error("fail", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        } finally {
            stringRedisTemplate.delete(SHORT_URL_KEY + fullUrl);
            redisLock.unlock();
        }

    }

    @Override
    public Page<Link> findList(Integer pageNum, Integer pageSize, Integer groupId) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("未找到用户信息");
        }

        Page<Link> linkPage = new Page<>(pageNum, pageSize);

        QueryWrapper<Link> linkQueryWrapper = new QueryWrapper<>();
        linkQueryWrapper.eq("del_flag", DeleteFlag.NO_DELETE);
        linkQueryWrapper.eq("group_id", groupId);
        return this.page(linkPage, linkQueryWrapper);
    }

    @Override
    public Link findById(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("未找到用户信息");
        }

        QueryWrapper<Link> linkQueryWrapper = new QueryWrapper<>();
        linkQueryWrapper.eq("del_flag", DeleteFlag.NO_DELETE);
        return this.getOne(linkQueryWrapper);

    }

    @Override
    public void decode(String shortUrlKey, HttpServletResponse response, HttpServletRequest request) throws IOException {
        long loginIdAsLong = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(loginIdAsLong);
        if (user == null) {
            throw new BizException("未找到登录信息");
        }

        String url = stringRedisTemplate.opsForValue().get(SHORT_URL_KEY + shortUrlKey);
        if (StrUtil.isBlank(url)) {
            response.sendRedirect("https://xunmeng.qq.com/");
        } else {
            RLock redisLock = distributedLockFactory.getRedisLock(RedisConstants.SHORT_URL_KEY_LOCK + shortUrlKey);
            redisLock.lock();
            try {
                url = stringRedisTemplate.opsForValue().get(SHORT_URL_KEY + shortUrlKey);
                if (StrUtil.isBlank(url)) {
                    response.sendRedirect("https://xunmeng.qq.com/");
                } else {
                    buildLogs(request, user.getRealName(), shortUrlKey);
                    countToRedis(shortUrlKey, request);
                    response.sendRedirect(url);
                }
            } finally {
                redisLock.unlock();
            }
        }

    }

    private void countToRedis(String shortUrlKey, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        Long uv = stringRedisTemplate.opsForSet().add("uv:" + shortUrlKey, ip);

        // 统计PV
        Long pv = stringRedisTemplate.opsForHyperLogLog().add("pv:" + shortUrlKey, ip);

        // 统计IP数
        Long ipCount = stringRedisTemplate.opsForSet().add("ip:" + shortUrlKey, ip);

        CountVo countVoBuilder = CountVo.builder().totalPv(pv).totalUv(uv).totalUip(ipCount).fullShortUrl(shortUrlKey).build();
        rabbitMqHelper.send(MqConstants.Exchange.SHORT_EXCHANGE, MqConstants.Key.SHORT_COUNT_KEY_PREFIX, countVoBuilder);
    }


    /**
     * 白名单
     *
     * @param url
     * @return
     */
    private boolean confirmIsWhiteDomain(String url) {
        String domain = null;
        try {
            URL url1 = URLUtil.url(url);
            String host = url1.getHost();
            if (StrUtil.isNotBlank(host)) {
                domain = host;
                if (domain.startsWith("www.")) {
                    domain = host.substring(4);
                }
            }
        } catch (Exception ignored) {
        }

        if (StrUtil.isBlank(domain)) {
            return false;
        }

        return whiteDomainConfig.getDetails().contains(domain);
    }

    private void buildLogs(HttpServletRequest request, String username, String fullShortUrl) {
        String ipAddress = ShortLinkUtils.getActualIp(request);
        String os = ShortLinkUtils.getOs(request);
        String browser = ShortLinkUtils.getBrowser(request);
        String device = ShortLinkUtils.getDevice(request);
        String network = ShortLinkUtils.getNetwork(request);
        LinkLogs build = LinkLogs.builder()
                .ip(ipAddress)
                .os(os)
                .fullShortUrl(fullShortUrl)
                .username(username)
                .browser(browser)
                .device(device)
                .network(network)
                .delFlag(DeleteFlag.NO_DELETE)
                .createTime(LocalDateTime.now())
                .build();
        rabbitMqHelper.send(MqConstants.Exchange.SHORT_EXCHANGE, MqConstants.Key.SHORT_KEY_PREFIX, build);
    }

}




