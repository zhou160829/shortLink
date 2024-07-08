package com.zhou.shortlink.exceptions;

import com.zhou.shortlink.result.R;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(com.zhou.shortlink.exceptions.BizException.class)
    @ResponseBody
    public R bizException(BizException e) {
        return R.error().setCode(e.getCode()).setData(e.getMsg());
    }


    @ExceptionHandler(com.zhou.shortlink.exceptions.RedisLimitException.class)
    @ResponseBody
    public R redisLimitException(RedisLimitException e) {
        return R.error().setCode(e.getCode()).setData(e.getMsg());
    }


}