package com.zhou.shortlink.vo;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Builder
@Accessors(chain = true)
public class UserVo {

    private String realName;

    private String token;
}
