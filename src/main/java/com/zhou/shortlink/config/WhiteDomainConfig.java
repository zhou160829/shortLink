package com.zhou.shortlink.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "short.white.domain")
@Data
public class WhiteDomainConfig {

    /**
     * 可跳转的原始链接域名
     */
    private List<String> details;
}
