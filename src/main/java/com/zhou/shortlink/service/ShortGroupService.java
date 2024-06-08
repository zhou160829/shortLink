package com.zhou.shortlink.service;

import com.zhou.shortlink.domain.ShortGroup;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 82518
* @description 针对表【short_group】的数据库操作Service
* @createDate 2024-06-07 23:30:47
*/
public interface ShortGroupService extends IService<ShortGroup> {

    boolean add(ShortGroup shortGroup);

    boolean updateShortGroup(ShortGroup shortGroup);

    boolean deleteShortGroup(Long id);

    List<ShortGroup> findList();
}
