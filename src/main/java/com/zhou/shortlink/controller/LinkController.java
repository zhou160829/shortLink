package com.zhou.shortlink.controller;


import cn.hutool.core.util.StrUtil;
import com.zhou.shortlink.annotation.RedisLimitAnnotation;
import com.zhou.shortlink.domain.Link;
import com.zhou.shortlink.enums.ValiDateStatus;
import com.zhou.shortlink.result.R;
import com.zhou.shortlink.service.LinkService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/link")
@Slf4j
@CrossOrigin
public class LinkController {

    @Resource
    LinkService linkService;

    @Resource
    private HttpServletResponse response;

    @Resource
    private HttpServletRequest request;


    @PostMapping("/add")
    @RedisLimitAnnotation(key = "addLink", permitPerSecond = 5, expire = 3)
    public R addLink(@RequestBody Link link) {
        if (link == null || StrUtil.isEmpty(link.getOriginUrl())
                || StrUtil.isEmpty(link.getDescribe()) || link.getGroupId() == null) {
            return R.error("参数错误");
        }

        if (link.getValidDateType().equalsValue(ValiDateStatus.CUSTOM.getValue()) && link.getValidDate() == null) {
            return R.error("有效期为自定义时需选择有效期");
        }


        if (link.getValidDateType().equalsValue(ValiDateStatus.FOREVER.getValue()) && link.getValidDate() != null && LocalDateTime.now().isAfter(link.getValidDate())) {
            return R.error("有效期不能小于等于当前时间");
        }


        if (linkService.add(link)) {
            return R.ok("新增成功");
        }
        return R.error("新增失败");
    }

    @PostMapping("/update")
    public R updateLink(@RequestBody Link link) {
        if (link == null || link.getId() == null || StrUtil.isEmpty(link.getOriginUrl())
                || StrUtil.isEmpty(link.getDescribe()) || link.getGroupId() == null) {
            return R.error("参数错误");
        }

        if (link.getValidDateType().equalsValue(ValiDateStatus.CUSTOM.getValue()) && link.getValidDate() == null) {
            return R.error("有效期为自定义时需选择有效期");
        }

        if (link.getValidDateType().equalsValue(ValiDateStatus.FOREVER.getValue()) && link.getValidDate() != null && LocalDateTime.now().isAfter(link.getValidDate())) {
            return R.error("有效期不能小于等于当前时间");
        }


        if (linkService.updateLink(link)) {
            return R.ok("修改成功");
        }
        return R.error("修改失败");
    }


    @DeleteMapping("{id}")
    public R DeleteLink(@PathVariable("id") Long id) {
        if (id == null) {
            return R.error("参数错误");
        }

        if (linkService.deleteLink(id)) {
            return R.ok("删除成功");
        }
        return R.error("删除失败");
    }


    @GetMapping("findList/{pageNum}/{pageSize}")
    public R findList(@PathVariable("pageNum") Integer pageNum,
                      @PathVariable("pageSize") Integer pageSize,
                      @RequestParam("groupId") Integer groupId, @RequestParam("keyWord") String keyword) throws IOException {
        if (pageNum == null || pageSize == null || groupId == null) {
            return R.error("参数错误");
        }

        return R.ok().set("page", linkService.findList(pageNum, pageSize, groupId, keyword));
    }

    @GetMapping("findById/{id}")
    public R findById(@PathVariable("id") Long id) {
        if (id == null) {
            return R.error("参数错误");
        }

        return R.data(linkService.findById(id));
    }


}
