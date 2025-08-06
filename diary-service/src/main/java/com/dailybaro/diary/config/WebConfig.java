package com.dailybaro.diary.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class WebConfig extends WebMvcConfigurationSupport {
    
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加静态资源映射，使上传的文件可以通过 /uploads/** 访问
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
