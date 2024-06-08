package com.zhou.shortlink.handler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhou.shortlink.constant.MqConstants;
import com.zhou.shortlink.domain.Link;
import com.zhou.shortlink.domain.LinkLogs;
import com.zhou.shortlink.domain.LinkToday;
import com.zhou.shortlink.domain.vo.CountVo;
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
    public void listenCount(CountVo countVo) {
        LocalDate now = LocalDate.now();
        String fullShortUrl = countVo.getFullShortUrl();


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

        LinkToday linkToday = LinkToday.builder().todayPv(countVo.getTotalPv()).todayUv(countVo.getTotalUv()).todayUip(countVo.getTotalUip()).date(now).createTime(LocalDateTime.now()).delFlag(DeleteFlag.NO_DELETE).fullShortUrl(fullShortUrl).build();
        if (one == null) {
            linkTodayService.save(linkToday);
        } else {
            linkToday.setId(one.getId());
            linkTodayService.updateById(linkToday);
        }

        UpdateWrapper<Link> linkUpdateWrapper = new UpdateWrapper<>();
        linkUpdateWrapper.setSql("total_pv = total_pv + " + countVo.getTotalPv());
        linkUpdateWrapper.setSql("total_uv = total_uv + " + countVo.getTotalUv());
        linkUpdateWrapper.setSql("total_uip = total_uip + " + countVo.getTotalUip());

        linkService.update(linkUpdateWrapper);

    }

}
