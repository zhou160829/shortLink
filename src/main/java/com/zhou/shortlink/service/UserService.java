package com.zhou.shortlink.service;

import com.zhou.shortlink.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhou.shortlink.vo.UserLoginVo;

/**
* @author 82518
* @description 针对表【user】的数据库操作Service
* @createDate 2024-06-07 23:30:47
*/
public interface UserService extends IService<User> {

    String login(UserLoginVo userLoginVo);
}
