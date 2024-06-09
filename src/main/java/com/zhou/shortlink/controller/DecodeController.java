package com.zhou.shortlink.controller;

import com.zhou.shortlink.result.R;
import com.zhou.shortlink.service.LinkService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping
@Slf4j
public class DecodeController {

    @Resource
    LinkService linkService;

    @Resource
    HttpServletResponse response;

    @Resource
    HttpServletRequest request;

    @GetMapping(value = "{shortUrlKey}")
    public R decode(@PathVariable String shortUrlKey) throws IOException {
        String decode = linkService.decode(shortUrlKey, response, request);
//        try {
//            response.sendRedirect(decode);
//        } catch (IOException e) {
//            log.error("重定向错误{}", decode);
//        }
        return R.data(decode);

    }
}
