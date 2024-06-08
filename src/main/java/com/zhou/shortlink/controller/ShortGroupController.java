package com.zhou.shortlink.controller;

import cn.hutool.core.util.StrUtil;
import com.zhou.shortlink.domain.ShortGroup;
import com.zhou.shortlink.result.R;
import com.zhou.shortlink.service.ShortGroupService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shortGroup")
@Slf4j
@CrossOrigin
public class ShortGroupController {


    @Resource
    ShortGroupService shortGroupService;

    @PostMapping("/add")
    public R addShortGroup(@RequestBody ShortGroup shortGroup) {
        if (shortGroup == null || StrUtil.isEmpty(shortGroup.getName())) {
            return R.error("参数错误");
        }

        if (shortGroupService.add(shortGroup)) {
            return R.ok("新增成功");
        }
        return R.error("新增失败");
    }

    @PostMapping("/update")
    public R updateShortGroup(@RequestBody ShortGroup shortGroup) {
        if (shortGroup == null || StrUtil.isEmpty(shortGroup.getName())) {
            return R.error("参数错误");
        }


        if (shortGroupService.updateShortGroup(shortGroup)) {
            return R.ok("修改成功");
        }
        return R.error("修改失败");
    }


    @DeleteMapping("{id}")
    public R deleteShortGroup(@PathVariable("id") Long id) {
        if (id == null) {
            return R.error("参数错误");
        }

        if (shortGroupService.deleteShortGroup(id)) {
            return R.ok("删除成功");
        }
        return R.error("删除失败");
    }


    @GetMapping("findList")
    public R findList() {
        return R.data(shortGroupService.findList());
    }


}
