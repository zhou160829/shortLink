package com.zhou.shortlink.config.es;

import jakarta.annotation.Resource;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhoujj
 * @date 2023/8/4 17:14
 */
@Configuration
public class ElasticSearchConfig {

    @Resource
    ElasticsearchProperties elasticsearchProperties;

    @Bean
    public RestHighLevelClient esRestClient() {

        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(elasticsearchProperties.getUsername(), elasticsearchProperties.getPassword());
        provider.setCredentials(AuthScope.ANY, credentials);

        RestClientBuilder builder = RestClient.builder(new HttpHost(elasticsearchProperties.getHost(), elasticsearchProperties.getPort(), elasticsearchProperties.getScheme()))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(provider));

        return new RestHighLevelClient(builder);

    }


    public static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        COMMON_OPTIONS = builder.build();
    }

}
