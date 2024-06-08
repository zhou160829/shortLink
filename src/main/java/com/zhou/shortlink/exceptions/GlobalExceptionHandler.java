package com.zhou.shortlink.exceptions;

import com.zhou.shortlink.result.R;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLIntegrityConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(com.zhou.shortlink.exceptions.BizException.class)
    @ResponseBody
    public R bizException(BizException e) {
        return R.error().setCode(e.getCode()).setData(e.getMsg());
    }



}