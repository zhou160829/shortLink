package com.zhou.shortlink.es.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "link", createIndex = true)
public class ElasticLinkVo {

    @Id
    @Field(type = FieldType.Long)
    private Long id;

    @Field(type = FieldType.Text, store = false, analyzer = "ik_smart")
    private String shortUri;

    @Field(type = FieldType.Text, store = false, analyzer = "ik_smart")
    private String describe;

}
