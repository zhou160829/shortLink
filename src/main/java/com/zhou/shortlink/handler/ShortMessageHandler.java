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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "short.logs.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.SHORT_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.SHORT_KEY_PREFIX
    ))
    public void listenLogs(LinkLogs linkLogs) {
        linkLogsService.save(linkLogs);
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

        stringRedisTemplate.opsForHyperLogLog().add("uv:" + fullShortUrl, device);
        // 统计PV
        stringRedisTemplate.opsForValue().increment("pv:" + fullShortUrl, 1);
        // 统计IP数
        stringRedisTemplate.opsForHyperLogLog().add("ip:" + fullShortUrl, ip);

        Long uv = stringRedisTemplate.opsForHyperLogLog().size("uv:" + fullShortUrl);
        String pvStr = stringRedisTemplate.opsForValue().get("pv:" + fullShortUrl);
        Long pv = Long.valueOf(StrUtil.isNotBlank(pvStr) ? pvStr : "0L");
        Long ipv = stringRedisTemplate.opsForHyperLogLog().size("ip:" + fullShortUrl);


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

}
