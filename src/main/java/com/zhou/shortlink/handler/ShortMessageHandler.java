package com.zhou.shortlink.handler;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhou.shortlink.constant.MqConstants;
import com.zhou.shortlink.domain.Link;
import com.zhou.shortlink.domain.LinkLogs;
import com.zhou.shortlink.domain.LinkToday;
import com.zhou.shortlink.domain.vo.IpVo;
import com.zhou.shortlink.enums.DeleteFlag;
import com.zhou.shortlink.service.LinkLogsService;
import com.zhou.shortlink.service.LinkService;
import com.zhou.shortlink.service.LinkTodayService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.zhou.shortlink.constant.RedisConstants.SHORT_URL_KEY;

@Slf4j
@Component
public class ShortMessageHandler {

    @Resource
    LinkLogsService linkLogsService;


    @Resource
    LinkTodayService linkTodayService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    LinkService linkService;

    @Resource
    ThreadPoolTaskExecutor linkLogThreadExecutor;

    @Resource
    ThreadPoolTaskExecutor countThreadExecutor;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "short.logs.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.SHORT_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.SHORT_KEY_PREFIX
    ))
    public void listenLogs(LinkLogs linkLogs) {
        log.info("进入listenLogs任务");
        linkLogThreadExecutor.execute(() -> {
            try {
                linkLogsService.save(linkLogs);
            } catch (Exception e) {
                log.error("保存linkLogs时出现异常: {}", e.getMessage(), e);
                // 可以选择处理异常，比如发送通知或者采取其他适当的措施
            }
        });
        log.info("结束listenLogs任务");
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "short.count.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.SHORT_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.SHORT_COUNT_KEY_PREFIX
    ))
    @Transactional
    public void listenCount(IpVo ipVo) {
        LocalDate now = LocalDate.now();
        String fullShortUrl = ipVo.getFullShortUrl();
        String ip = ipVo.getIp();
        String device = ipVo.getDevice();

        stringRedisTemplate.opsForHyperLogLog().add("uv:" + now + ":" + fullShortUrl, device);
        // 统计PV
        stringRedisTemplate.opsForValue().increment("pv:" + now + ":" + fullShortUrl, 1);
        // 统计IP数
        stringRedisTemplate.opsForHyperLogLog().add("ip:" + now + ":" + fullShortUrl, ip);

        Long uv = stringRedisTemplate.opsForHyperLogLog().size("uv:" + now + ":" + fullShortUrl);
        String pvStr = stringRedisTemplate.opsForValue().get("pv:" + now + ":" + fullShortUrl);
        Long pv = Long.valueOf(StrUtil.isNotBlank(pvStr) ? pvStr : "0L");
        Long ipv = stringRedisTemplate.opsForHyperLogLog().size("ip:" + now + ":" + fullShortUrl);


        QueryWrapper<LinkToday> linkTodayQueryWrapper = new QueryWrapper<>();
        linkTodayQueryWrapper.eq("full_short_url", fullShortUrl);
        linkTodayQueryWrapper.eq("del_flag", DeleteFlag.NO_DELETE);
        linkTodayQueryWrapper.eq("date", now);
        LinkToday one = linkTodayService.getOne(linkTodayQueryWrapper);

        QueryWrapper<Link> linkQueryWrapper = new QueryWrapper<>();
        linkQueryWrapper.eq("full_short_url", fullShortUrl);
        linkQueryWrapper.eq("del_time", 0);
        linkQueryWrapper.eq("del_flag", DeleteFlag.NO_DELETE);
        Link link = linkService.getOne(linkQueryWrapper);

        LinkToday linkToday = LinkToday.builder().todayPv(pv).todayUv(uv).todayUip(ipv).date(now).createTime(LocalDateTime.now()).delFlag(DeleteFlag.NO_DELETE).fullShortUrl(fullShortUrl).build();
        if (one == null) {
            linkTodayService.save(linkToday);
        } else {
            linkToday.setId(one.getId());
            linkTodayService.updateById(linkToday);
        }

        UpdateWrapper<Link> linkUpdateWrapper = new UpdateWrapper<>();
        linkUpdateWrapper.setSql("total_pv = total_pv + " + pv);
        linkUpdateWrapper.setSql("total_uv = total_uv + " + uv);
        linkUpdateWrapper.setSql("total_uip = total_uip + " + ipv);
        linkUpdateWrapper.eq("id", link.getId());

        linkService.update(linkUpdateWrapper);

    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "short.delete.count.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.SHORT_DELETE_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.SHORT_DELETE_COUNT_KEY_PREFIX
    ))
    @Transactional
    public void delCountRedis(String fullShortUrl) {
        List<String> keysToDelete = Arrays.asList(
                SHORT_URL_KEY + fullShortUrl,
                "uv" + fullShortUrl,
                "pv" + fullShortUrl,
                "ipv" + fullShortUrl
        );
        stringRedisTemplate.delete(keysToDelete);
    }
}