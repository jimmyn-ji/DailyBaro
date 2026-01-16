package com.dailybaro.aiknowledge.service;

import com.dailybaro.aiknowledge.model.vo.KnowledgeVO;
import java.util.List;

/**
 * 推荐服务接口
 * 基于用户心情推荐知识库文章
 */
public interface RecommendationService {
    
    /**
     * 基于用户近一周心情推荐知识库文章
     * @param userId 用户ID
     * @param days 天数（默认7天）
     * @return 推荐的知识列表
     */
    List<KnowledgeVO> recommendByUserEmotion(Long userId, int days);
    
    /**
     * 获取每日推荐文章（从Redis缓存）
     * @param userId 用户ID
     * @return 推荐文章
     */
    KnowledgeVO getDailyRecommendation(Long userId);
    
    /**
     * 生成并缓存每日推荐文章（定时任务调用）
     * @param userId 用户ID
     */
    void generateDailyRecommendation(Long userId);
}
