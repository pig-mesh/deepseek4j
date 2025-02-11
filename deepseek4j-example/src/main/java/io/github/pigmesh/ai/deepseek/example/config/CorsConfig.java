package io.github.pigmesh.ai.deepseek.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 配置所有请求路径允许的来源、方法和头信息
        registry.addMapping("/**")
                .allowedOrigins("*")  // 放开所有来源
                .allowedMethods("GET", "POST", "PUT", "DELETE")  // 允许常用HTTP方法
                .allowedHeaders("*");  // 允许所有自定义头信息
    }
}