package com.zhou.shortlink.domain.vo.user;

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
