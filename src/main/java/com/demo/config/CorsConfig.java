package com.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:8001",           // 本地开发环境
                        "https://localhost:8001",           // 本地开发环境
                        "http://47.98.159.249",            // 生产环境（80端口，HTTP）
                        "http://47.98.159.249:8001",       // 生产环境（8001端口）
                        "https://47.98.159.249:8001"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)  // 允许携带凭证（cookie/session），用于登录验证
                .maxAge(3600);
    }
}

