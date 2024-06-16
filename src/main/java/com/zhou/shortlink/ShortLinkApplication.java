package com.zhou.shortlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
@EnableElasticsearchRepositories(basePackages = {"com.zhou.shortlink.es.mapper"})
public class ShortLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortLinkApplication.class, args);
    }

}
