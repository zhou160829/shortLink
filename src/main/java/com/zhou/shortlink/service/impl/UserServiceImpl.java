package com.zhou.shortlink.service.impl;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhou.shortlink.constant.RedisConstants;
import com.zhou.shortlink.domain.User;
import com.zhou.shortlink.exceptions.BizException;
import com.zhou.shortlink.mapper.UserMapper;
import com.zhou.shortlink.service.UserService;
import com.zhou.shortlink.domain.vo.user.UserLoginVo;
import com.zhou.shortlink.domain.vo.user.UserVo;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author 82518
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2024-06-07 23:30:47
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
    private static final String USER_INFO_KEY_PREFIX = "USER_INFO_KEY_";
    private static final int PASSWORD_INCORRECT_EXPIRE_SECONDS = 180;


    @Resource
    RedisTemplate<String, String> redisTemplate;

    @Override
    public UserVo login(UserLoginVo userLoginVo) {
        Object o = redisTemplate.opsForValue().get(USER_INFO_KEY_PREFIX + userLoginVo.getUsername());
        String count = String.valueOf(o);
        if (RedisConstants.USER_INFO_KEY_LOGIN_COUNT.equals(count)) {
            throw new BizException("错误超过3次请重新再试");
        }

        String username = userLoginVo.getUsername();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        User user = baseMapper.selectOne(wrapper);


        if (user == null) {
            throw new BizException("未找到该用户");
        }
        if (!user.getPassword().equals(userLoginVo.getPassword())) {
            if (o == null) {
                redisTemplate.opsForValue().set(USER_INFO_KEY_PREFIX + userLoginVo.getUsername(), "0", PASSWORD_INCORRECT_EXPIRE_SECONDS, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().increment(USER_INFO_KEY_PREFIX + userLoginVo.getUsername());
            }
            throw new BizException("账号或者密码错误");
        }
        StpUtil.login(user.getId(), SaLoginModel.create().setExtra("username", user.getRealName()));
        return UserVo.builder().token(StpUtil.getTokenValue()).realName(user.getRealName()).build();
    }
}




