package com.zhou.shortlink.domain.vo.count;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Builder
@Accessors(chain = true)
public class CountVo {
    private String fullShortUrl;

    private Long totalPv;

    private Long totalUv;

    private Long totalUip;

}
