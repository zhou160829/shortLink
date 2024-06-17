package com.zhou.shortlink.es.domain;

import lombok.Data;

@Data
public class ElasticLinkVo {

    private Long id;

    private Long groupId;

    private String shortUri;

    private String describe;

}
