package com.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    // 定义一个helloworld测试类
    @RequestMapping("/test")
    public String helloWorld() {
        System.out.println("hello world");
        return "hello world";
    }
}
