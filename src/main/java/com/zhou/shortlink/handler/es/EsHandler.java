package com.zhou.shortlink.handler.es;

import cn.hutool.core.bean.BeanUtil;
import com.zhou.shortlink.constant.MqConstants;
import com.zhou.shortlink.constant.es.ESConstants;
import com.zhou.shortlink.es.domain.ElasticLinkVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class EsHandler {

    @Resource
    RestHighLevelClient restHighLevelClient;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "short.es.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.SHORT_ES_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.SHORT_ES_KEY_PREFIX
    ))
    public void addLinkToEs(ElasticLinkVo elasticLinkVo) {
        log.info("开始处理ES添加");
        try {
            Map<String, Object> stringObjectMap = BeanUtil.beanToMap(elasticLinkVo);
            IndexRequest indexRequest = new IndexRequest(ESConstants.LINK_INDEX)
                    .id(String.valueOf(elasticLinkVo.getId()))
                    .source(stringObjectMap, XContentType.JSON);

            IndexResponse response = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            log.info("ES添加成功，response: {}", response);
        } catch (IOException e) {

            log.error("出现错误{}", e.getMessage());
        }
        log.info("处理ES添加结束");
    }
}
