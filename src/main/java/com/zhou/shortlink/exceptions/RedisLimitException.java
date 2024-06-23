package com.zhou.shortlink.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RedisLimitException extends RuntimeException {
    private Integer code;
    private String msg;

    public RedisLimitException(String msg) {
        super(msg);
        this.code = 500;
        this.msg = msg;
    }
}
