package com.dailybaro.emotion.service.impl;

import com.dailybaro.emotion.model.vo.EmotionDataPointVO;
import com.dailybaro.emotion.model.vo.EmotionShareVO;
import com.dailybaro.emotion.service.EmotionAnalysisService;
import com.dailybaro.emotion.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class EmotionAnalysisServiceImpl implements EmotionAnalysisService {

    private static final Map<String, Double> EMOTION_MAP = new HashMap<>();

    static {
        EMOTION_MAP.put("开心", 1.0);
        EMOTION_MAP.put("激动", 0.8);
        EMOTION_MAP.put("平静", 0.5);
        EMOTION_MAP.put("无聊", 0.0);
        EMOTION_MAP.put("疲惫", -0.4);
        EMOTION_MAP.put("难过", -0.8);
        EMOTION_MAP.put("焦虑", -0.9);
        EMOTION_MAP.put("愤怒", -1.0);
    }

    @Override
    public Result<List<EmotionDataPointVO>> getEmotionFluctuation(Long userId, Date startDate, Date endDate) {
        return getEmotionFluctuation(userId, startDate, endDate, null);
    }

    @Override
    public Result<List<EmotionDataPointVO>> getEmotionFluctuation(Long userId, Date startDate, Date endDate, Long tagId) {
        // 暂时返回模拟数据
        List<EmotionDataPointVO> result = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        
        while (!cal.getTime().after(endDate)) {
            EmotionDataPointVO vo = new EmotionDataPointVO();
            vo.setDate(new java.sql.Date(cal.getTime().getTime()));
            // 生成随机情绪值
            vo.setValue(Math.random() * 2 - 1); // -1 到 1 之间的随机值
            result.add(vo);
            cal.add(Calendar.DATE, 1);
        }
        
        return Result.success(result);
    }

    @Override
    public Result<List<Map<String, Object>>> getEmotionFluctuationMulti(Long userId, Date startDate, Date endDate) {
        // 暂时返回模拟数据
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("startDate", startDate);
        data.put("endDate", endDate);
        data.put("emotionData", Arrays.asList("开心", "难过", "平静"));
        result.add(data);
        
        return Result.success(result);
    }

    @Override
    public Result<List<EmotionShareVO>> getEmotionDistribution(Long userId, Date startDate, Date endDate) {
        return getEmotionDistribution(userId, startDate, endDate, null);
    }

    @Override
    public Result<List<EmotionShareVO>> getEmotionDistribution(Long userId, Date startDate, Date endDate, Long tagId) {
        // 暂时返回模拟数据
        List<EmotionShareVO> result = new ArrayList<>();
        result.add(new EmotionShareVO("开心", 30.0));
        result.add(new EmotionShareVO("平静", 40.0));
        result.add(new EmotionShareVO("难过", 20.0));
        result.add(new EmotionShareVO("焦虑", 10.0));
        
        return Result.success(result);
    }

    @Override
    public Result<List<String>> getAIAnalysisPoints(Map<String, Object> emotionData) {
        // 暂时返回模拟AI分析结果
        List<String> analysisPoints = new ArrayList<>();
        analysisPoints.add("您的情绪整体趋于稳定");
        analysisPoints.add("建议多进行户外活动");
        analysisPoints.add("保持良好的作息习惯");
        
        return Result.success(analysisPoints);
    }
} 