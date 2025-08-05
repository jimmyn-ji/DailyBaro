package com.dailybaro.emotion.controller;

import com.dailybaro.emotion.model.vo.EmotionDataPointVO;
import com.dailybaro.emotion.model.vo.EmotionShareVO;
import com.dailybaro.emotion.service.EmotionAnalysisService;
import com.dailybaro.emotion.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
public class EmotionAnalysisController {

    @Autowired
    private EmotionAnalysisService emotionAnalysisService;


    @GetMapping("/fluctuation")
    public Result<List<EmotionDataPointVO>> getFluctuation(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        Date realStart = getDayStart(startDate);
        Date realEnd = getDayEnd(endDate);
        return emotionAnalysisService.getEmotionFluctuation(userId, realStart, realEnd);
    }

    @GetMapping("/distribution")
    public Result<List<EmotionShareVO>> getDistribution(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        Date realStart = getDayStart(startDate);
        Date realEnd = getDayEnd(endDate);
        return emotionAnalysisService.getEmotionDistribution(userId, realStart, realEnd);
    }

    @GetMapping("/fluctuation-multi")
    public Result<List<Map<String, Object>>> getFluctuationMulti(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        Date realStart = getDayStart(startDate);
        Date realEnd = getDayEnd(endDate);
        return emotionAnalysisService.getEmotionFluctuationMulti(userId, realStart, realEnd);
    }

    @PostMapping("/ai/analysis-points")
    public Result<List<String>> getAIAnalysisPoints(@RequestBody Map<String, Object> emotionData) {
        return emotionAnalysisService.getAIAnalysisPoints(emotionData);
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
} 