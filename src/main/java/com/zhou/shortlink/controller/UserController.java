package com.zhou.shortlink.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.zhou.shortlink.domain.User;
import com.zhou.shortlink.result.R;
import com.zhou.shortlink.service.UserService;
import com.zhou.shortlink.domain.vo.user.UserLoginVo;
import com.zhou.shortlink.domain.vo.user.UserVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    UserService userService;

    @PostMapping("/login")
    public R login(@RequestBody UserLoginVo userLoginVo) {
        if (userLoginVo == null || userLoginVo.getUsername() == null || userLoginVo.getPassword() == null) {
            return R.error("账号不能为空");
        }

        UserVo userVo = userService.login(userLoginVo);
        return R.ok("登录成功").setData(userVo);
    }

    @GetMapping("/getLoginUser")
    public R getLoginUser() {
        boolean login = StpUtil.isLogin();
        if (login) {
            long loginIdAsLong = StpUtil.getLoginIdAsLong();
            User byId = userService.getById(loginIdAsLong);
            return R.data(byId);
        }
        return R.error("未找到登录信息");
    }


    @GetMapping("/loginOut")
    public R loginOut() {
        boolean login = StpUtil.isLogin();
        if (login) {
            StpUtil.logout();
            return R.data("退出登录成功");
        }
        return R.error("退出失败");
    }
}
