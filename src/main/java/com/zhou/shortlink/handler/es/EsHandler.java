package com.zhou.shortlink.handler.es;

import com.zhou.shortlink.constant.MqConstants;
import com.zhou.shortlink.es.domain.ElasticLinkVo;
import com.zhou.shortlink.es.mapper.LinkEsMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EsHandler {

    @Resource
    ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    LinkEsMapper linkEsMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "short.es.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.SHORT_ES_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.SHORT_ES_KEY_PREFIX
    ))
    public void addLinkToEs(ElasticLinkVo elasticLinkVo) {
        log.info("开始处理ES添加");
        linkEsMapper.save(elasticLinkVo);
        log.info("处理ES添加结束");
    }
}
