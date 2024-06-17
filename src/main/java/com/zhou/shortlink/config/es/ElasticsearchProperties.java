package com.zhou.shortlink.config.es;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
@Data
public class ElasticsearchProperties {

    private String host;

    private Integer port;

    private String scheme;

    private String username;
    private String password;
}
