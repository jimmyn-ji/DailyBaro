package com.dailybaro.nlp.controller;

import com.dailybaro.nlp.dto.EmotionAnalysisRequest;
import com.dailybaro.nlp.dto.EmotionAnalysisResponse;
import com.dailybaro.nlp.service.NlpService;
import com.dailybaro.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * NLP情绪识别控制器
 * 
 * @author DailyBaro Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/nlp")
@Api(tags = "NLP情绪识别服务")
public class NlpController {

    @Autowired
    private NlpService nlpService;

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @ApiOperation("健康检查")
    public Result<String> health() {
        return Result.success("NLP服务运行正常");
    }

    /**
     * 单文本情绪分析
     */
    @PostMapping("/emotion/analyze")
    @ApiOperation("单文本情绪分析")
    public Result<EmotionAnalysisResponse> analyzeEmotion(@Valid @RequestBody EmotionAnalysisRequest request) {
        try {
            log.info("收到情绪分析请求: {}", request.getText());
            EmotionAnalysisResponse response = nlpService.analyzeEmotion(request.getText());
            return Result.success(response);
        } catch (Exception e) {
            log.error("情绪分析失败", e);
            return Result.error("情绪分析失败: " + e.getMessage());
        }
    }

    /**
     * 批量情绪分析
     */
    @PostMapping("/emotion/batch-analyze")
    @ApiOperation("批量情绪分析")
    public Result<List<EmotionAnalysisResponse>> batchAnalyzeEmotion(@RequestBody List<String> texts) {
        try {
            log.info("收到批量情绪分析请求，文本数量: {}", texts.size());
            List<EmotionAnalysisResponse> responses = nlpService.batchAnalyzeEmotion(texts);
            return Result.success(responses);
        } catch (Exception e) {
            log.error("批量情绪分析失败", e);
            return Result.error("批量情绪分析失败: " + e.getMessage());
        }
    }

    /**
     * 情绪分类
     */
    @PostMapping("/emotion/classify")
    @ApiOperation("情绪分类")
    public Result<EmotionAnalysisResponse> classifyEmotion(@RequestParam String text, 
                                                         @RequestParam(defaultValue = "3") Integer topK) {
        try {
            log.info("收到情绪分类请求: {}, topK: {}", text, topK);
            EmotionAnalysisResponse response = nlpService.classifyEmotion(text, topK);
            return Result.success(response);
        } catch (Exception e) {
            log.error("情绪分类失败", e);
            return Result.error("情绪分类失败: " + e.getMessage());
        }
    }

    /**
     * 获取服务信息
     */
    @GetMapping("/info")
    @ApiOperation("获取服务信息")
    public Result<Object> getServiceInfo() {
        try {
            return Result.success(nlpService.getServiceInfo());
        } catch (Exception e) {
            log.error("获取服务信息失败", e);
            return Result.error("获取服务信息失败: " + e.getMessage());
        }
    }
}
