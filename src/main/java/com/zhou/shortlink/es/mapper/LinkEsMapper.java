package com.zhou.shortlink.es.mapper;

import com.zhou.shortlink.es.domain.ElasticLinkVo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface LinkEsMapper extends ElasticsearchRepository<ElasticLinkVo, Long> {
}
