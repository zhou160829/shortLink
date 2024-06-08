package com.zhou.shortlink.exceptions;

import lombok.Data;

@Data
public class BizException extends RuntimeException{
    private Integer code;
    private String msg;
    public BizException(String msg) {
        super(msg);
        this.code = 500;
        this.msg = msg;
    }

}
