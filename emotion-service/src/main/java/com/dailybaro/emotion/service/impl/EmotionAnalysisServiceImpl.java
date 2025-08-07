package com.dailybaro.emotion.service.impl;


import com.dailybaro.emotion.model.vo.EmotionDataPointVO;
import com.dailybaro.emotion.model.vo.EmotionShareVO;
import com.dailybaro.emotion.service.EmotionAnalysisService;
import com.dailybaro.common.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@FeignClient(name = "diary-service", url = "http://localhost:8002")
interface DiaryFeignClient {
    @GetMapping("/api/diary/user-emotions")
    List<Map<String, Object>> getUserEmotions(@RequestParam("userId") Long userId, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate);
}

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

    @Autowired
    private DiaryFeignClient diaryFeignClient;

    @Override
    public Result<List<EmotionDataPointVO>> getEmotionFluctuation(Long userId, Date startDate, Date endDate) {
        return getEmotionFluctuation(userId, startDate, endDate, null);
    }
    
    @Override
    public Result<List<EmotionDataPointVO>> getEmotionFluctuation(Long userId, Date startDate, Date endDate, Long tagId) {
        // 简化版本，返回模拟数据
        List<EmotionDataPointVO> result = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        while (!cal.getTime().after(endDate)) {
            Date dayStart = getDayStart(cal.getTime());
            EmotionDataPointVO vo = new EmotionDataPointVO();
            vo.setDate(new java.sql.Date(dayStart.getTime()));
            vo.setValue(Math.random() * 2 - 1); // 随机值在-1到1之间
            result.add(vo);
            cal.add(Calendar.DATE, 1);
        }
        return Result.success(result);
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
        // 简化版本，返回模拟数据
        List<EmotionShareVO> result = new ArrayList<>();
        result.add(new EmotionShareVO("开心", 30.0));
        result.add(new EmotionShareVO("平静", 25.0));
        result.add(new EmotionShareVO("疲惫", 20.0));
        result.add(new EmotionShareVO("难过", 15.0));
        result.add(new EmotionShareVO("焦虑", 10.0));
        return Result.success(result);
    }

    @Override
    public Result<List<Map<String, Object>>> getEmotionFluctuationMulti(Long userId, Date startDate, Date endDate) {
        // 调用 diary-service 获取真实情绪数据
        List<Map<String, Object>> multiData = diaryFeignClient.getUserEmotions(userId, formatDate(startDate), formatDate(endDate));
        return Result.success(multiData);
    }

    private String formatDate(Date date) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    @Override
    public Result<List<String>> getAIAnalysisPoints(Map<String, Object> emotionData) {
        // 简化版本，返回默认分析点
        List<String> analysisPoints = new ArrayList<>();
        analysisPoints.add("情绪波动较大，建议保持规律作息");
        analysisPoints.add("多进行户外活动，增加社交互动");
        analysisPoints.add("学习情绪管理技巧，建立调节机制");
        analysisPoints.add("培养积极兴趣爱好，丰富生活内容");
        return Result.success(analysisPoints);
    }
} 