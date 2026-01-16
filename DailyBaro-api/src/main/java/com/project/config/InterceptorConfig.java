package com.project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class InterceptorConfig extends WebMvcConfigurationSupport {
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加图片资源映射
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }

    /**
     * 解决跨域问题
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 配置所有路径支持跨域访问
        registry.addMapping("/**")
//                .allowedOrigins("*")
                .allowedOriginPatterns("*") // 允许所有域名进行跨域调用
//                .allowedOrigins("http://localhost:8080", "https://servicewechat.com","http://localhost:8848")  // 允许来自指定源的请求
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的 HTTP 方法
                .allowedHeaders("*")  // 允许所有的请求头
                .allowCredentials(true)  // 是否允许带有凭证（例如 cookies）
                .maxAge(3600);  // 预检请求的有效期，单位秒
    }

}