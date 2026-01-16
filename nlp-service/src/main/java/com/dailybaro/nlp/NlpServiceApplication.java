package com.dailybaro.nlp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * NLP情绪识别服务主应用类
 * 
 * @author DailyBaro Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class NlpServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NlpServiceApplication.class, args);
    }
}
