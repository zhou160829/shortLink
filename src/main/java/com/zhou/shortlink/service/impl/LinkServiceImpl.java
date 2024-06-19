package com.zhou.shortlink.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.bloomfilter.BitMapBloomFilter;
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
import com.zhou.shortlink.constant.es.ESConstants;
import com.zhou.shortlink.domain.Link;
import com.zhou.shortlink.domain.LinkLogs;
import com.zhou.shortlink.domain.LinkToday;
import com.zhou.shortlink.domain.User;
import com.zhou.shortlink.domain.vo.IpVo;
import com.zhou.shortlink.enums.DeleteFlag;
import com.zhou.shortlink.enums.EnableStatus;
import com.zhou.shortlink.enums.ValiDateStatus;
import com.zhou.shortlink.es.domain.ElasticLinkVo;
import com.zhou.shortlink.exceptions.BizException;
import com.zhou.shortlink.mapper.LinkLogsMapper;
import com.zhou.shortlink.mapper.LinkMapper;
import com.zhou.shortlink.mapper.LinkTodayMapper;
import com.zhou.shortlink.mapper.UserMapper;
import com.zhou.shortlink.service.LinkService;
import com.zhou.shortlink.util.ShortLinkUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.zhou.shortlink.constant.RedisConstants.SHORT_NULL_URL_KEY;
import static com.zhou.shortlink.constant.RedisConstants.SHORT_URL_KEY;

/**
 * @author 82518
 * @description 针对表【link】的数据库操作Service实现
 * @createDate 2024-06-07 23:30:47
 */
@Service
@Slf4j
public class LinkServiceImpl extends ServiceImpl<LinkMapper, Link>
        implements LinkService {

    static final int capacity = 1000;

    private final BitMapBloomFilter filter = new BitMapBloomFilter(capacity);

    @Resource
    RestHighLevelClient restHighLevelClient;

    @Resource
    UserMapper userMapper;

    @Value("${short.domain}")
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

    @Resource
    PlatformTransactionManager platformTransactionManager;

    @Transactional
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

            ElasticLinkVo elasticLinkVo = new ElasticLinkVo();
            BeanUtils.copyProperties(link, elasticLinkVo);
            rabbitMqHelper.send(MqConstants.Exchange.SHORT_ES_EXCHANGE, MqConstants.Key.SHORT_ES_KEY_PREFIX, elasticLinkVo);

        } catch (DuplicateKeyException e) {
            if (!filter.contains(fullShortUrl)) {
                filter.add(fullShortUrl);
            }
            throw new BizException(String.format("短链接：%s 生成重复", fullShortUrl));
        }

        long linkCacheValidTime = ShortLinkUtils.getLinkCacheValidTime(link.getValidDate());
        if (linkCacheValidTime > 0) {
            stringRedisTemplate.opsForValue().set(SHORT_URL_KEY + fullShortUrl, link.getOriginUrl(), ShortLinkUtils.getLinkCacheValidTime(link.getValidDate()), TimeUnit.SECONDS);
        } else {
            stringRedisTemplate.opsForValue().set(SHORT_URL_KEY + fullShortUrl, link.getOriginUrl());
        }

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

        TransactionStatus status = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            link.setUpdateTime(LocalDateTime.now());
            // 如果改变了地址
            if (!byId.getOriginUrl().equals(link.getOriginUrl())) {
                String shortUrlKey = ShortLinkUtils.shortUrl(originUrl);
                String fullShortUrl = String.format("%s/%s", domain, shortUrlKey);
                link.setDomain(domain).setShortUri(shortUrlKey).setFullShortUrl(fullShortUrl);
                try {
                    link.setTotalUip(0).setTotalPv(0).setTotalUv(0);
                    this.updateById(link);
                } catch (DuplicateKeyException e) {
                    if (!filter.contains(fullShortUrl)) {
                        filter.add(fullShortUrl);
                    }
                    // 记录日志或发送警报通知
                    log.error("短链接生成重复：{}", fullShortUrl, e);
                    throw new BizException(String.format("短链接：%s 生成重复", fullShortUrl));
                }
                UpdateWrapper<LinkToday> linkTodayUpdateWrapper = new UpdateWrapper<>();
                linkTodayUpdateWrapper.set("del_flag", DeleteFlag.DELETE);
                linkTodayUpdateWrapper.eq("full_short_url", byId.getFullShortUrl());
                linkTodayUpdateWrapper.eq("del_flag", DeleteFlag.NO_DELETE);
                linkTodayMapper.update(linkTodayUpdateWrapper);

                UpdateWrapper<LinkLogs> logsUpdateWrapper = new UpdateWrapper<>();
                logsUpdateWrapper.set("del_flag", DeleteFlag.DELETE);
                logsUpdateWrapper.eq("full_short_url", byId.getFullShortUrl());
                logsUpdateWrapper.eq("del_flag", DeleteFlag.NO_DELETE);
                linkLogsMapper.update(logsUpdateWrapper);


                rabbitMqHelper.send(MqConstants.Exchange.SHORT_DELETE_EXCHANGE, MqConstants.Key.SHORT_DELETE_COUNT_KEY_PREFIX, byId.getFullShortUrl());

                filter.add(fullShortUrl);
                platformTransactionManager.commit(status);
            } else {
                this.updateById(link);
            }
        } catch (Exception e) {
            platformTransactionManager.rollback(status);
            log.error("出现异常{}", e.getMessage());
        } finally {
            redisLock.unlock();
        }
        return true;
    }


    @Transactional
    @Override
    public boolean deleteLink(Long id) {
        RLock redisLock = distributedLockFactory.getRedisLock(RedisConstants.SHORT_URL_KEY_LOCK + id);
        redisLock.lock();
        try {
            LocalDateTime currentDate = LocalDateTime.now();
            long timestamp = currentDate.toInstant(ZoneOffset.UTC).toEpochMilli();

            Link link = new Link();
            link.setId(id);
            link.setDelTime(timestamp);
            link.setDelFlag(DeleteFlag.DELETE);
            String fullUrl = link.getFullShortUrl();

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

            rabbitMqHelper.send(MqConstants.Exchange.SHORT_DELETE_EXCHANGE, MqConstants.Key.SHORT_DELETE_COUNT_KEY_PREFIX, fullUrl);

            return true;
        } catch (RuntimeException e) {
            log.error("fail", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        } finally {
            redisLock.unlock();
        }

    }

    @Override
    public Page<Link> findList(Integer pageNum, Integer pageSize, Integer groupId, String keyWord) throws IOException {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("未找到用户信息");
        }
        if (StrUtil.isNotBlank(keyWord)) {
            return findListFromDb(pageNum, pageSize, groupId);
        }
        return findListFromEs(pageNum, pageSize, groupId, keyWord);
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

    /*
    long loginIdAsLong = RandomUtil.randomLong(0,100);
            User user = null;
            if (loginIdAsLong > 0L) {
                user = userMapper.selectById(loginIdAsLong);
            }
     */
    @Override
    public String decode(String shortUrlKey, HttpServletResponse response, HttpServletRequest request) throws IOException {
        long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : 0L;


        String fullShortUrl = String.format("%s/%s", domain, shortUrlKey);
        String url = stringRedisTemplate.opsForValue().get(SHORT_URL_KEY + fullShortUrl);

        if (StrUtil.isNotBlank(url)) {
            buildLogs(request, userId, fullShortUrl);
            countToRedis(fullShortUrl, request);
            return url;
        }
        // 防止一直请求一个不存在的
        String isNull = stringRedisTemplate.opsForValue().get("SHORT_NULL_URL_KEY" + fullShortUrl);
        if (StrUtil.isNotBlank(isNull)) {
            return "https://xunmeng.qq.com/";
        }
        if (!filter.contains(fullShortUrl)) {
            return "https://xunmeng.qq.com/";
        }
        // 如果没有从redis中查到，但布隆过滤器中存在且还没查数据库
        RLock redisLock = distributedLockFactory.getRedisLock(RedisConstants.SHORT_URL_WRITE_KEY_LOCK + fullShortUrl);
        redisLock.lock();
        try {
            url = stringRedisTemplate.opsForValue().get(SHORT_URL_KEY + fullShortUrl);
            if (StrUtil.isBlank(url)) {
                QueryWrapper<Link> linkQueryWrapper = new QueryWrapper<>();
                linkQueryWrapper.eq("full_short_url", fullShortUrl);
                linkQueryWrapper.eq("del_time", 0);
                linkQueryWrapper.eq("del_flag", DeleteFlag.NO_DELETE);
                Link one = this.getOne(linkQueryWrapper);
                if (one == null) {
                    stringRedisTemplate.opsForValue().set(SHORT_NULL_URL_KEY + fullShortUrl, "-", 30, TimeUnit.SECONDS);
                    return "https://xunmeng.qq.com/";
                } else if (one.getValidDate() != null && LocalDateTime.now().isAfter(one.getValidDate())) {
                    return "https://xunmeng.qq.com/";
                } else {
                    if (one.getValidDateType().equalsValue(ValiDateStatus.FOREVER.getValue())) {
                        stringRedisTemplate.opsForValue().set(SHORT_URL_KEY + fullShortUrl, one.getOriginUrl());
                    } else {
                        stringRedisTemplate.opsForValue().set(SHORT_URL_KEY + fullShortUrl, one.getOriginUrl(), ShortLinkUtils.getLinkCacheValidTime(one.getValidDate()), TimeUnit.SECONDS);
                    }
                    response.sendRedirect(one.getOriginUrl());
                    return one.getOriginUrl();
                }
            } else {
                buildLogs(request, userId, shortUrlKey);
                countToRedis(fullShortUrl, request);
                response.sendRedirect(url);
                return url;
            }
        } finally {
            redisLock.unlock();
        }


    }

    public Page<Link> findListFromDb(Integer pageNum, Integer pageSize, Integer groupId) {
        Page<Link> linkPage = new Page<>(pageNum, pageSize);

        QueryWrapper<Link> linkQueryWrapper = new QueryWrapper<>();
        linkQueryWrapper.eq("del_flag", DeleteFlag.NO_DELETE);
        linkQueryWrapper.eq("group_id", groupId);

        return this.page(linkPage, linkQueryWrapper);
    }

    public Page<Link> findListFromEs(Integer pageNum, Integer pageSize, Integer groupId, String keyWord) throws IOException {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("未找到用户信息");
        }


        SearchRequest searchRequest = new SearchRequest(ESConstants.LINK_INDEX);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.should(QueryBuilders.matchQuery("shortUri", keyWord));
        boolQuery.should(QueryBuilders.matchQuery("describe", keyWord));
        boolQuery.must(QueryBuilders.termQuery("groupId", groupId));

        sourceBuilder.query(boolQuery);
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);
        sourceBuilder.sort("your_sort_field", SortOrder.ASC); // 可选，按字段排序

        searchRequest.source(sourceBuilder);

        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();

        if (ObjectUtils.isEmpty(hits)) {
            return null;
        }
        Page<Link> linkPage = new Page<>();

        List<Long> ids = Arrays.stream(hits.getHits())
                .map(hit -> {
                    Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                    return Long.valueOf((String) sourceAsMap.get("id"));
                })
                .collect(Collectors.toList());
        List<Link> links = this.listByIds(ids);
        linkPage.setRecords(links);
        return linkPage;
    }

    private void countToRedis(String shortUrlKey, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String device = ShortLinkUtils.getDevice(request);
        IpVo build = IpVo.builder().ip(ip).device(device).fullShortUrl(shortUrlKey).build();
        rabbitMqHelper.send(MqConstants.Exchange.SHORT_EXCHANGE, MqConstants.Key.SHORT_COUNT_KEY_PREFIX, build);
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

    private void buildLogs(HttpServletRequest request, Long userId, String fullShortUrl) {
        String ipAddress = ShortLinkUtils.getActualIp(request);
        String os = ShortLinkUtils.getOs(request);
        String browser = ShortLinkUtils.getBrowser(request);
        String device = ShortLinkUtils.getDevice(request);
        String network = ShortLinkUtils.getNetwork(request);
        LinkLogs build = LinkLogs.builder()
                .ip(ipAddress)
                .os(os)
                .fullShortUrl(fullShortUrl)
                .userId(userId)
                .browser(browser)
                .device(device)
                .network(network)
                .delFlag(DeleteFlag.NO_DELETE)
                .createTime(LocalDateTime.now())
                .build();
        rabbitMqHelper.send(MqConstants.Exchange.SHORT_EXCHANGE, MqConstants.Key.SHORT_KEY_PREFIX, build);
    }


    @PostConstruct
    private void addDataToBloom() {
        log.info("开始加载数据至布隆过滤器");

        try {
            QueryWrapper<Link> linkQueryWrapper = new QueryWrapper<>();
            linkQueryWrapper.select("full_short_url as fullShortUrl");
            linkQueryWrapper.eq("del_flag", DeleteFlag.NO_DELETE);
            linkQueryWrapper.eq("del_time", 0);
            List<String> fullShortUrls = this.listObjs(linkQueryWrapper);

            for (String fullShortUrl : fullShortUrls) {
                filter.add(fullShortUrl);
            }
        } catch (Exception e) {
            log.error("加载数据至布隆过滤器失败");
        }

        log.info("加载数据至布隆过滤器结束");
    }

}




