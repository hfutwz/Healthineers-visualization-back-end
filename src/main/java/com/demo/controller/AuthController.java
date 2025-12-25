package com.demo.controller;

import com.demo.dto.Result;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // 硬编码的用户名和密码
    private static final String VALID_USERNAME = "admin";
    private static final String VALID_PASSWORD = "hos123";

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result login(@RequestParam String username, 
                       @RequestParam String password,
                       HttpSession session) {
        System.out.println("收到登录请求 - 用户名: " + username + ", 密码长度: " + (password != null ? password.length() : 0));
        
        // 验证用户名和密码
        if (VALID_USERNAME.equals(username) && VALID_PASSWORD.equals(password)) {
            // 登录成功，将用户信息存入session
            session.setAttribute("user", username);
            session.setAttribute("isLoggedIn", true);
            System.out.println("登录成功 - Session ID: " + session.getId());
            return Result.success(session.getId());
        } else {
            System.out.println("登录失败 - 用户名或密码错误");
            return Result.error("用户名或密码错误");
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result logout(HttpSession session) {
        session.invalidate();
        return Result.ok();
    }

    /**
     * 检查登录状态
     */
    @GetMapping("/status")
    public Result checkStatus(HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn != null && isLoggedIn) {
            return Result.success(session.getAttribute("user"));
        } else {
            return Result.error("未登录");
        }
    }
}

