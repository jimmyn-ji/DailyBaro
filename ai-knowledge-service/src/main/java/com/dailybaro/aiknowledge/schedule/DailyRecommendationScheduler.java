package com.dailybaro.aiknowledge.schedule;

import com.dailybaro.aiknowledge.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 每日推荐定时任务
 * 每天凌晨2点生成所有活跃用户的每日推荐
 */
@Slf4j
@Component
public class DailyRecommendationScheduler {
    
    @Autowired
    private RecommendationService recommendationService;
    
    /**
     * 每天凌晨2点执行
     * 为所有活跃用户生成每日推荐
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateDailyRecommendations() {
        log.info("开始生成每日推荐文章...");
        
        try {
            // TODO: 从user-service获取所有活跃用户列表
            // 这里暂时跳过，因为需要调用user-service
            // 实际使用时，可以通过消息队列或定时任务批量处理
            
            log.info("每日推荐生成任务完成（当前为单用户模式，需要扩展为批量处理）");
        } catch (Exception e) {
            log.error("生成每日推荐失败", e);
        }
    }
    
    /**
     * 为单个用户生成推荐（可被外部调用）
     */
    public void generateForUser(Long userId) {
        try {
            recommendationService.generateDailyRecommendation(userId);
            log.info("为用户生成每日推荐成功 userId={}", userId);
        } catch (Exception e) {
            log.error("为用户生成每日推荐失败 userId={}", userId, e);
        }
    }
}
