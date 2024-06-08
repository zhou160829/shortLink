package com.zhou.shortlink.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhou.shortlink.domain.ShortGroup;
import com.zhou.shortlink.domain.User;
import com.zhou.shortlink.enums.DeleteFlag;
import com.zhou.shortlink.exceptions.BizException;
import com.zhou.shortlink.mapper.LinkMapper;
import com.zhou.shortlink.mapper.ShortGroupMapper;
import com.zhou.shortlink.mapper.UserMapper;
import com.zhou.shortlink.service.ShortGroupService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 82518
 * @description 针对表【short_group】的数据库操作Service实现
 * @createDate 2024-06-07 23:30:47
 */
@Service
public class ShortGroupServiceImpl extends ServiceImpl<ShortGroupMapper, ShortGroup>
        implements ShortGroupService {

    @Resource
    UserMapper userMapper;

    @Resource
    LinkMapper linkMapper;

    @Override
    public boolean add(ShortGroup shortGroup) {
        Long loginId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(loginId);
        if (user == null) {
            throw new BizException("未找到用户");
        }

        shortGroup.setCreateUserId(loginId);
        shortGroup.setCreateTime(LocalDateTime.now());
        shortGroup.setCreateUserName(user.getRealName());

        shortGroup.setSortOrder(0);
        return this.save(shortGroup);
    }

    @Override
    public boolean updateShortGroup(ShortGroup shortGroup) {
        shortGroup.setUpdateTime(LocalDateTime.now());
        return this.save(shortGroup);
    }

    @Transactional
    @Override
    public boolean deleteShortGroup(Long id) {
        // todo 删除组内链接
        return this.removeById(id);
    }

    @Override
    public List<ShortGroup> findList() {
        Long loginId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(loginId);
        if (user == null) {
            throw new BizException("未找到用户");
        }

        QueryWrapper<ShortGroup> shortGroupQueryWrapper = new QueryWrapper<>();
        shortGroupQueryWrapper.eq("del_flag", DeleteFlag.NO_DELETE);
        shortGroupQueryWrapper.orderByAsc("sort_order");
        return this.list(shortGroupQueryWrapper);

    }
}




