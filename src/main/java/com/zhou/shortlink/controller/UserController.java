package com.zhou.shortlink.controller;

import com.zhou.shortlink.result.R;
import com.zhou.shortlink.service.UserService;
import com.zhou.shortlink.vo.UserLoginVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Slf4j
@CrossOrigin
public class UserController {

    @Resource
    UserService userService;

    @PostMapping("/login")
    public R login(@RequestBody UserLoginVo userLoginVo) {
        if (userLoginVo == null || userLoginVo.getUsername() == null || userLoginVo.getPassword() == null) {
            return R.error("账号不能为空");
        }

        String login = userService.login(userLoginVo);
        return R.ok("登录成功").setData(login);
    }

}
