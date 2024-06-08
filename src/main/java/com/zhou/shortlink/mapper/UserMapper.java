package com.zhou.shortlink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhou.shortlink.domain.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 82518
 * @description 针对表【user】的数据库操作Mapper
 * @createDate 2024-06-07 23:30:47
 * @Entity generator.domain.User
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




