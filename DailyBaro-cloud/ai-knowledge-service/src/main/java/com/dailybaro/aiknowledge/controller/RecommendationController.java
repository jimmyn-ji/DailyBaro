package com.dailybaro.aiknowledge.controller;

import com.dailybaro.common.util.Result;
import com.dailybaro.aiknowledge.model.vo.KnowledgeVO;
import com.dailybaro.aiknowledge.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/knowledge/recommendation")
public class RecommendationController {
    
    @Autowired
    private RecommendationService recommendationService;
    
    /**
     * 基于用户心情推荐知识库文章
     * @param uid 用户ID（从header获取）
     * @param days 天数（默认7天）
     */
    @GetMapping("/by-emotion")
    public Result<List<KnowledgeVO>> recommendByEmotion(
            @RequestHeader(value = "uid", required = false) String uid,
            @RequestParam(defaultValue = "7") int days) {
        try {
            if (uid == null) {
                return Result.error(400, "用户ID不能为空");
            }
            
            Long userId = Long.valueOf(uid);
            List<KnowledgeVO> recommendations = recommendationService.recommendByUserEmotion(userId, days);
            
            return Result.success(recommendations);
        } catch (Exception e) {
            log.error("推荐失败 uid={}, days={}", uid, days, e);
            return Result.error(500, "推荐失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取每日推荐文章（从Redis缓存）
     */
    @GetMapping("/daily")
    public Result<KnowledgeVO> getDailyRecommendation(
            @RequestHeader(value = "uid", required = false) String uid) {
        try {
            if (uid == null) {
                return Result.error(400, "用户ID不能为空");
            }
            
            Long userId = Long.valueOf(uid);
            KnowledgeVO recommendation = recommendationService.getDailyRecommendation(userId);
            
            if (recommendation == null) {
                return Result.error(404, "暂无推荐文章");
            }
            
            return Result.success(recommendation);
        } catch (Exception e) {
            log.error("获取每日推荐失败 uid={}", uid, e);
            return Result.error(500, "获取推荐失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动触发生成每日推荐（管理员接口）
     */
    @PostMapping("/generate-daily")
    public Result<String> generateDailyRecommendation(
            @RequestHeader(value = "uid", required = false) String uid) {
        try {
            if (uid == null) {
                return Result.error(400, "用户ID不能为空");
            }
            
            Long userId = Long.valueOf(uid);
            recommendationService.generateDailyRecommendation(userId);
            
            return Result.success("每日推荐生成成功");
        } catch (Exception e) {
            log.error("生成每日推荐失败 uid={}", uid, e);
            return Result.error(500, "生成推荐失败: " + e.getMessage());
        }
    }
}
