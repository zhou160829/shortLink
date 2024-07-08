package com.zhou.shortlink.domain.vo.count;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Builder
@Accessors(chain = true)
@Data
public class IpVo {
    private String fullShortUrl;

    private String ip;
    private String device;
}
