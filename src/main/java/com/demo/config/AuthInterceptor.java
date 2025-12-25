package com.demo.config;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 排除登录、登出和状态检查接口
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        System.out.println("拦截器 - 请求URI: " + requestURI + ", 方法: " + method);
        
        if (requestURI.startsWith("/api/auth/")) {
            System.out.println("拦截器 - 允许访问认证接口: " + requestURI);
            return true;
        }

        // 检查session中是否有登录信息
        Object isLoggedIn = request.getSession().getAttribute("isLoggedIn");
        if (isLoggedIn != null && (Boolean) isLoggedIn) {
            return true;
        }

        // 未登录，返回401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"未登录，请先登录\"}");
        return false;
    }
}

