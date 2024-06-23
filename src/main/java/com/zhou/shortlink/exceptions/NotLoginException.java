package com.zhou.shortlink.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class NotLoginException extends RuntimeException{
    private Integer code;
    private String msg;
    public NotLoginException(String msg) {
        super(msg);
        this.code = 10000;
        this.msg = msg;
    }
}
