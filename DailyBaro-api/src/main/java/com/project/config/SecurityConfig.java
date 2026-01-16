package com.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable() // 禁用 CSRF 保护
                .authorizeRequests()
                // 允许登录和注册接口
                .antMatchers("/login/doLogin", "/login/doRegister","/login/wxLogin").permitAll()
                // 允许其他接口无需认证
                .antMatchers("/users/**","/rhinitisType/**","/consultation/**","/uploads/**","/slider/**","/comments/**","/product/**","/shoppingCart/**","/order/**","/post/**").permitAll()
                .antMatchers("/api/**").permitAll() // 新增，放开 /api 下所有接口
                .antMatchers("/user/**").permitAll()
                .anyRequest().authenticated(); // 其他请求需要身份验证
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); // 允许所有域名
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 允许的请求方法
        configuration.setAllowedHeaders(Arrays.asList("*")); // 允许的请求头
        configuration.setAllowCredentials(true); // 允许携带凭证
        configuration.setMaxAge(3600L); // 预检请求的有效期

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 应用到所有路径
        return source;
    }
}