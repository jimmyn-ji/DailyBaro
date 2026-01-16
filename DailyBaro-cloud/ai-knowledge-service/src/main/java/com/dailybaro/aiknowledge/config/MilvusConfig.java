package com.dailybaro.aiknowledge.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;

@Configuration
@Data
public class MilvusConfig {
    
    @Value("${milvus.host:localhost}")
    private String host;
    
    @Value("${milvus.port:19530}")
    private Integer port;
    
    @Value("${milvus.collection-name:knowledge_base}")
    private String collectionName;
    
    @Value("${milvus.dimension:1536}")
    private Integer dimension;
    
    @Bean
    public MilvusServiceClient milvusClient() {
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(host)
                .withPort(port)
                .withConnectTimeout(2L, TimeUnit.SECONDS) // 2秒连接超时，快速失败
                .withIdleTimeout(60L, TimeUnit.SECONDS) // 60秒空闲超时
                .build();
        return new MilvusServiceClient(connectParam);
    }
}
