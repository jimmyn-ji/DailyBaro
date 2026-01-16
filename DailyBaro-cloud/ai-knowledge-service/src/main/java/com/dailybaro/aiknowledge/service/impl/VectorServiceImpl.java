package com.dailybaro.aiknowledge.service.impl;

import com.dailybaro.aiknowledge.config.MilvusConfig;
import com.dailybaro.aiknowledge.service.VectorService;
import com.dailybaro.aiknowledge.service.EmbeddingService;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.collection.*;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.SearchResultsWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VectorServiceImpl implements VectorService {
    
    @Autowired
    private MilvusServiceClient milvusClient;
    
    @Autowired
    private MilvusConfig milvusConfig;
    
    @Autowired
    private com.dailybaro.aiknowledge.service.EmbeddingService embeddingService;
    
    private volatile boolean milvusAvailable = false;
    
    @PostConstruct
    public void init() {
        // 异步初始化 Milvus，不阻塞服务启动
        new Thread(() -> {
            try {
                // 先快速检查端口是否开放，避免 SDK 内部重试
                if (!isPortOpen(milvusConfig.getHost(), milvusConfig.getPort(), 1000)) {
                    log.warn("Milvus port {} is not accessible, skipping initialization", milvusConfig.getPort());
                    milvusAvailable = false;
                    return;
                }
                
                // 端口开放，尝试连接
                createCollectionIfNotExists();
                milvusAvailable = true;
                log.info("Milvus connection established successfully");
            } catch (Exception e) {
                log.warn("Milvus is not available, service will continue without vector search. Error: {}", e.getMessage());
                log.warn("Please start Milvus service (port 19530) or the vector search feature will be disabled.");
                log.warn("To start Milvus, run: docker-compose up -d milvus (in DailyBaro-cloud directory)");
                milvusAvailable = false;
            }
        }, "MilvusInitThread").start();
    }
    
    /**
     * 检查端口是否开放（快速失败，避免 SDK 内部重试）
     */
    private boolean isPortOpen(String host, int port, int timeoutMs) {
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 创建集合（如果不存在）
     */
    private void createCollectionIfNotExists() {
        try {
            // 先快速检查 Milvus 是否可用，避免 SDK 内部重试导致大量错误日志
            HasCollectionParam hasCollectionParam = HasCollectionParam.newBuilder()
                    .withCollectionName(milvusConfig.getCollectionName())
                    .build();
            
            // 使用 try-catch 捕获连接异常，避免 SDK 重试输出大量错误日志
            boolean hasCollection;
            try {
                hasCollection = milvusClient.hasCollection(hasCollectionParam).getData();
            } catch (Exception e) {
                // 连接失败，直接抛出异常，不进行后续操作
                throw new RuntimeException("Milvus connection failed: " + e.getMessage(), e);
            }
            
            if (!hasCollection) {
                // 定义字段
                FieldType idField = FieldType.newBuilder()
                        .withName("id")
                        .withDataType(DataType.Int64)
                        .withPrimaryKey(true)
                        .withAutoID(true)
                        .build();
                
                FieldType vectorField = FieldType.newBuilder()
                        .withName("vector")
                        .withDataType(DataType.FloatVector)
                        .withDimension(milvusConfig.getDimension())
                        .build();
                
                FieldType textField = FieldType.newBuilder()
                        .withName("text")
                        .withDataType(DataType.VarChar)
                        .withMaxLength(65535)
                        .build();
                
                FieldType knowledgeIdField = FieldType.newBuilder()
                        .withName("knowledge_id")
                        .withDataType(DataType.Int64)
                        .build();
                
                // 创建集合
                CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
                        .withCollectionName(milvusConfig.getCollectionName())
                        .withDescription("心理健康知识库向量集合")
                        .withShardsNum(2)
                        .addFieldType(idField)
                        .addFieldType(vectorField)
                        .addFieldType(textField)
                        .addFieldType(knowledgeIdField)
                        .build();
                
                milvusClient.createCollection(createCollectionParam);
                
                // 创建索引
                CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                        .withCollectionName(milvusConfig.getCollectionName())
                        .withFieldName("vector")
                        .withIndexType(IndexType.IVF_FLAT)
                        .withMetricType(MetricType.L2)
                        .withExtraParam("{\"nlist\":1024}")
                        .build();
                
                milvusClient.createIndex(indexParam);
                
                // 加载集合
                LoadCollectionParam loadParam = LoadCollectionParam.newBuilder()
                        .withCollectionName(milvusConfig.getCollectionName())
                        .build();
                milvusClient.loadCollection(loadParam);
                
                log.info("Milvus collection created and loaded: {}", milvusConfig.getCollectionName());
            }
        } catch (Exception e) {
            log.error("Failed to create Milvus collection: {}", e.getMessage());
            throw e; // 重新抛出异常，让调用者知道初始化失败
        }
    }
    
    /**
     * 检查 Milvus 是否可用
     */
    private boolean isMilvusAvailable() {
        if (!milvusAvailable) {
            // 尝试重新连接
            try {
                HasCollectionParam hasCollectionParam = HasCollectionParam.newBuilder()
                        .withCollectionName(milvusConfig.getCollectionName())
                        .build();
                milvusClient.hasCollection(hasCollectionParam);
                milvusAvailable = true;
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public List<Float> textToVector(String text) {
        try {
            // 使用真实的 Embedding 服务
            return embeddingService.embed(text);
        } catch (Exception e) {
            log.error("Failed to convert text to vector", e);
            // 降级方案：生成临时向量
            return generateTemporaryVector(text);
        }
    }
    
    /**
     * 生成临时向量（降级方案，仅在 Embedding 服务失败时使用）
     * 优化：减少stream操作，使用传统循环
     */
    private List<Float> generateTemporaryVector(String text) {
        log.warn("使用临时向量生成（效果不佳），请配置真实的 Embedding API");
        int dimension = milvusConfig.getDimension();
        List<Float> vector = new ArrayList<>(dimension);
        Random random = new Random(text.hashCode());
        
        // 生成向量并计算平方和（一次循环完成）
        double sumSquares = 0.0;
        for (int i = 0; i < dimension; i++) {
            float value = (float) (random.nextGaussian() * 0.1);
            vector.add(value);
            sumSquares += value * value;
        }
        
        // 归一化
        double norm = Math.sqrt(sumSquares);
        if (norm > 0) {
            for (int i = 0; i < dimension; i++) {
                vector.set(i, vector.get(i) / (float) norm);
            }
        }
        return vector;
    }
    
    @Override
    public String storeVector(Long knowledgeId, String text) {
        if (!isMilvusAvailable()) {
            log.warn("Milvus is not available, skipping vector storage for knowledge ID: {}", knowledgeId);
            return null;
        }
        
        try {
            List<Float> vector = textToVector(text);
            
            List<Long> knowledgeIds = Collections.singletonList(knowledgeId);
            List<String> texts = Collections.singletonList(text);
            List<List<Float>> vectors = Collections.singletonList(vector);
            
            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(milvusConfig.getCollectionName())
                    .withFields(Arrays.asList(
                            new InsertParam.Field("knowledge_id", knowledgeIds),
                            new InsertParam.Field("text", texts),
                            new InsertParam.Field("vector", vectors)
                    ))
                    .build();
            
            milvusClient.insert(insertParam);
            
            // 返回向量ID（这里简化处理，实际应该从插入结果中获取）
            return "vec_" + knowledgeId;
        } catch (Exception e) {
            log.error("Failed to store vector", e);
            milvusAvailable = false; // 标记为不可用
            return null;
        }
    }
    
    @Override
    public List<Long> searchSimilar(String queryText, int topK) {
        if (!isMilvusAvailable()) {
            log.warn("Milvus is not available, returning empty search results");
            return Collections.emptyList();
        }
        
        try {
            List<Float> queryVector = textToVector(queryText);
            
            SearchParam searchParam = SearchParam.newBuilder()
                    .withCollectionName(milvusConfig.getCollectionName())
                    .withMetricType(MetricType.L2)
                    .withOutFields(Arrays.asList("knowledge_id", "text"))
                    .withTopK(topK)
                    .withVectors(Collections.singletonList(queryVector))
                    .withVectorFieldName("vector")
                    .build();
            
            SearchResultsWrapper searchResults = new SearchResultsWrapper(
                    milvusClient.search(searchParam).getData().getResults()
            );
            
            List<Long> knowledgeIds = new ArrayList<>();
            for (int i = 0; i < searchResults.getRowRecords().size(); i++) {
                Object knowledgeId = searchResults.getRowRecords().get(i).get("knowledge_id");
                if (knowledgeId instanceof Long) {
                    knowledgeIds.add((Long) knowledgeId);
                } else if (knowledgeId instanceof Number) {
                    knowledgeIds.add(((Number) knowledgeId).longValue());
                }
            }
            
            return knowledgeIds;
        } catch (Exception e) {
            log.error("Failed to search similar vectors", e);
            milvusAvailable = false; // 标记为不可用
            return Collections.emptyList();
        }
    }
    
    @Override
    public boolean deleteVector(String vectorId) {
        if (!isMilvusAvailable()) {
            log.warn("Milvus is not available, skipping vector deletion for ID: {}", vectorId);
            return false;
        }
        
        try {
            // 根据knowledge_id删除向量
            Long knowledgeId = Long.parseLong(vectorId.replace("vec_", ""));
            
            DeleteParam deleteParam = DeleteParam.newBuilder()
                    .withCollectionName(milvusConfig.getCollectionName())
                    .withExpr("knowledge_id == " + knowledgeId)
                    .build();
            
            milvusClient.delete(deleteParam);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete vector", e);
            milvusAvailable = false; // 标记为不可用
            return false;
        }
    }
}
