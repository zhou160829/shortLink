package com.zhou.shortlink.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhou.shortlink.domain.Link;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
* @author 82518
* @description 针对表【link】的数据库操作Service
* @createDate 2024-06-07 23:30:47
*/
public interface LinkService extends IService<Link> {

    boolean add(Link link);

    boolean updateLink(Link link);

    boolean deleteLink(Long id);

    Page<Link> findList(Integer pageNum, Integer pageSize, Integer groupId);

    Link findById(Long id);

    void decode(String shortUrlKey ,HttpServletResponse response, HttpServletRequest request) throws IOException;
}
