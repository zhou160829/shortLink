package com.zhou.shortlink.exceptions;

import cn.dev33.satoken.exception.*;
import cn.dev33.satoken.util.SaResult;
import com.zhou.shortlink.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(com.zhou.shortlink.exceptions.BizException.class)
    public R bizException(BizException e) {
        log.error("出现业务异常", e);
        return R.error().setCode(e.getCode()).setData(e.getMsg());
    }


    @ExceptionHandler(com.zhou.shortlink.exceptions.RedisLimitException.class)
    public R redisLimitException(RedisLimitException e) {
        log.error("出现Redis限流异常", e);
        return R.error().setCode(e.getCode()).setData(e.getMsg());
    }

    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R handlerException(NotLoginException e) {
        // 打印堆栈，以供调试
        log.error("出现未登录异常", e);
        // 返回给前端
        return R.error(e.getMessage());
    }

    // 拦截：缺少权限异常
    @ExceptionHandler(NotPermissionException.class)
    public R handlerException(NotPermissionException e) {
        log.error("出现权限异常", e);
        return R.error("缺少权限：" + e.getPermission());
    }

    // 拦截：缺少角色异常
    @ExceptionHandler(NotRoleException.class)
    public R handlerException(NotRoleException e) {
        log.error("出现角色异常", e);
        return R.error("缺少角色：" + e.getRole());
    }

    // 拦截：二级认证校验失败异常
    @ExceptionHandler(NotSafeException.class)
    public R handlerException(NotSafeException e) {
        log.error("出现二级认证校验异常", e);
        return R.error("二级认证校验失败：" + e.getService());
    }

    // 拦截：服务封禁异常
    @ExceptionHandler(DisableServiceException.class)
    public R handlerException(DisableServiceException e) {
        log.error("出现封禁异常", e);
        return R.error("当前账号 " + e.getService() + " 服务已被封禁 (level=" + e.getLevel() + ")：" + e.getDisableTime() + "秒后解封");
    }

    // 拦截：Http Basic 校验失败异常
    @ExceptionHandler(NotBasicAuthException.class)
    public R handlerException(NotBasicAuthException e) {
        log.error("出现Http Basic异常", e);
        return R.error(e.getMessage());
    }

    // 拦截：其它所有异常
    @ExceptionHandler(Exception.class)
    public SaResult handlerException(Exception e) {
        log.error("出现异常", e);
        return SaResult.error("未知错误" + e.getMessage());
    }

}