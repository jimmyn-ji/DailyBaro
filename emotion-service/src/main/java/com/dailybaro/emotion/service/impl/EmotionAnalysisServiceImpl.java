package com.dailybaro.emotion.service.impl;

import com.dailybaro.common.util.Result;
import com.dailybaro.emotion.model.vo.EmotionDataPointVO;
import com.dailybaro.emotion.model.vo.EmotionShareVO;
import com.dailybaro.emotion.service.EmotionAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        // 这里应调用数据库获取数据，以下为示例数据
        List<EmotionDataPointVO> result = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        while (!cal.getTime().after(endDate)) {
            Date dayStart = getDayStart(cal.getTime());
            Double value = Math.random() * 2 - 1; // -1~1
            result.add(new EmotionDataPointVO(dayStart, value));
            cal.add(Calendar.DATE, 1);
        }
        return Result.success(result);
    }
    @Override
    public Result<List<EmotionDataPointVO>> getEmotionFluctuation(Long userId, Date startDate, Date endDate, Long tagId) {
        return getEmotionFluctuation(userId, startDate, endDate);
    }
    private Date getDayStart(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    private Date getDayEnd(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
    @Override
    public Result<List<EmotionShareVO>> getEmotionDistribution(Long userId, Date startDate, Date endDate) {
        return getEmotionDistribution(userId, startDate, endDate, null);
    }
    @Override
    public Result<List<EmotionShareVO>> getEmotionDistribution(Long userId, Date startDate, Date endDate, Long tagId) {
        List<EmotionShareVO> result = new ArrayList<>();
        result.add(new EmotionShareVO("开心", 30.0));
        result.add(new EmotionShareVO("平静", 40.0));
        result.add(new EmotionShareVO("难过", 20.0));
        result.add(new EmotionShareVO("焦虑", 10.0));
        return Result.success(result);
    }
    @Override
    public Result<List<Map<String, Object>>> getEmotionFluctuationMulti(Long userId, Date startDate, Date endDate) {
        Result<List<EmotionDataPointVO>> fluctuationResult = getEmotionFluctuation(userId, startDate, endDate);
        List<EmotionDataPointVO> baseData = fluctuationResult.getData();
        List<Map<String, Object>> multiData = new ArrayList<>();
        for (EmotionDataPointVO point : baseData) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", point.getDate());
            dayData.put("开心", point.getValue() > 0.5 ? point.getValue() * 0.8 : 0);
            dayData.put("激动", point.getValue() > 0.3 ? point.getValue() * 0.6 : 0);
            dayData.put("平静", Math.abs(point.getValue()) < 0.2 ? 0.5 : 0);
            dayData.put("无聊", point.getValue() < -0.3 ? Math.abs(point.getValue()) * 0.4 : 0);
            dayData.put("疲惫", point.getValue() < -0.5 ? Math.abs(point.getValue()) * 0.6 : 0);
            dayData.put("难过", point.getValue() < -0.7 ? Math.abs(point.getValue()) * 0.8 : 0);
            dayData.put("焦虑", point.getValue() < -0.8 ? Math.abs(point.getValue()) * 0.9 : 0);
            dayData.put("愤怒", point.getValue() < -0.9 ? Math.abs(point.getValue()) : 0);
            multiData.add(dayData);
        }
        return Result.success(multiData);
    }
    @Override
    public Result<List<String>> getAIAnalysisPoints(Map<String, Object> emotionData) {
        List<String> analysisPoints = new ArrayList<>();
        analysisPoints.add("情绪波动较大，建议保持规律作息");
        analysisPoints.add("多进行户外活动，增加社交互动");
        analysisPoints.add("学习情绪管理技巧，建立调节机制");
        analysisPoints.add("必要时寻求专业心理咨询帮助");
        return Result.success(analysisPoints);
    }
} 