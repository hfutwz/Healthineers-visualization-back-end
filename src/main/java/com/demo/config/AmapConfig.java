package com.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 高德地图API配置类
 * 用于管理高德地图API的配置信息
 */
@Configuration
public class AmapConfig {
    
    @Value("${amap.api.key:a45594094ddabde9555f030599338cb9}")
    private String apiKey;
    
    @Value("${amap.city:上海}")
    private String city;
    
    public String getApiKey() {
        return apiKey;
    }
    
    public String getCity() {
        return city;
    }
}

