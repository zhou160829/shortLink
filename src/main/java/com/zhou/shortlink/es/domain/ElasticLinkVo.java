package com.zhou.shortlink.es.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@Document(indexName = "link", createIndex = true)
public class ElasticLinkVo {

    @Id
    @Field(type = FieldType.Long)
    private Long id;

    @Field(type = FieldType.Keyword, store = true)
    private String fullShortUrl;

    @Field(type = FieldType.Text, store = false, analyzer = "ik_smart")
    private String originUrl;

    @Field(type = FieldType.Long)
    private Long groupId;

    @Field(type = FieldType.Text, store = false, analyzer = "ik_smart")
    private String groupName;

    @Field(type = FieldType.Text)
    private String validDateType;

    @Field(type = FieldType.Date, format = DateFormat.basic_date)
    private LocalDateTime validDate;

    @Field(type = FieldType.Boolean)
    private Boolean delFlag;

    @Field(type = FieldType.Text)
    private String describe;

    @Field(type = FieldType.Date, format = DateFormat.basic_date)
    private LocalDateTime createTime;

}
