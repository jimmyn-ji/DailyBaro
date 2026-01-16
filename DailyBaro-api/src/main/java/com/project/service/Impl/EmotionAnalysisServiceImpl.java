package com.project.service.Impl;

import com.project.mapper.DiaryMapper;
import com.project.mapper.DiaryTagMapper;
import com.project.mapper.TagMapper;
import com.project.model.Diary;
import com.project.model.DiaryTag;
import com.project.model.Tag;
import com.project.model.vo.EmotionDataPointVO;
import com.project.model.vo.EmotionShareVO;
import com.project.service.AIService;
import com.project.service.EmotionAnalysisService;
import com.project.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmotionAnalysisServiceImpl implements EmotionAnalysisService {

    @Autowired
    private DiaryMapper diaryMapper;
    @Autowired
    private DiaryTagMapper diaryTagMapper;
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    @Qualifier("deepSeekAIService")
    private AIService aiService;
    
    @Autowired
    @Qualifier("mockAIService")
    private AIService mockAIService;

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
        EMOTION_MAP.put("压力", -0.6);
    }

    @Override
    public Result<List<EmotionDataPointVO>> getEmotionFluctuation(Long userId, Date startDate, Date endDate) {
        return getEmotionFluctuation(userId, startDate, endDate, null);
    }
    @Override
    public Result<List<EmotionDataPointVO>> getEmotionFluctuation(Long userId, Date startDate, Date endDate, Long tagId) {
        log.info("获取情绪波动数据 - userId: {}, startDate: {}, endDate: {}", userId, startDate, endDate);
        
        // 只根据userId和时间区间统计所有日记的情绪波动，不再处理tagId
        List<Diary> diaries = diaryMapper.selectList(null).stream()
            .filter(d -> d.getUserId().equals(userId)
                    && !d.getCreateTime().before(startDate)
                    && !d.getCreateTime().after(endDate))
            .collect(Collectors.toList());
        
        log.info("查询到的日记数量: {}", diaries.size());
        
        List<EmotionDataPointVO> result = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        while (!cal.getTime().after(endDate)) {
            Date dayStart = getDayStart(cal.getTime());
            Date dayEnd = getDayEnd(cal.getTime());
            // 筛选当天的日记
            List<Diary> dayDiaries = diaries.stream()
                .filter(d -> !d.getCreateTime().before(dayStart) && !d.getCreateTime().after(dayEnd))
                .collect(Collectors.toList());
            
            if (!dayDiaries.isEmpty()) {
                log.info("日期 {} 有 {} 篇日记", new java.sql.Date(dayStart.getTime()), dayDiaries.size());
            }
            
            List<Double> emotionValues = new ArrayList<>();
            for (Diary diary : dayDiaries) {
                List<String> tags = diaryMapper.findTagsByDiaryId(diary.getDiaryId());
                log.info("日记 {} 的标签: {}", diary.getDiaryId(), tags);
                for (String tag : tags) {
                    if (EMOTION_MAP.containsKey(tag)) {
                        emotionValues.add(EMOTION_MAP.get(tag));
                    }
                }
            }
            Double avg = null;
            if (!emotionValues.isEmpty()) {
                avg = emotionValues.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                log.info("日期 {} 的平均情绪值: {}", new java.sql.Date(dayStart.getTime()), avg);
            }
            EmotionDataPointVO vo = new EmotionDataPointVO();
            vo.setDate(new java.sql.Date(dayStart.getTime()));
            vo.setValue(avg);
            result.add(vo);
            cal.add(Calendar.DATE, 1);
        }
        
        log.info("情绪波动结果: {}", result);
        return Result.success(result);
    }

    // 工具方法：获取一天的起始和结束时间
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
        return getEmotionDistribution(userId, startDate, endDate, (Long) null);
    }
    @Override
    public Result<List<EmotionShareVO>> getEmotionDistribution(Long userId, Date startDate, Date endDate, Long tagId) {
        log.info("获取情绪分布数据 - userId: {}, startDate: {}, endDate: {}", userId, startDate, endDate);
        
        List<String> allTags;
        if (tagId != null) {
            List<Long> diaryIds = diaryTagMapper.findDiaryIdsByTagIds(Collections.singletonList(tagId));
            if (diaryIds.isEmpty()) {
                log.warn("未找到指定标签的日记");
                return Result.success(Collections.emptyList());
            }
            allTags = new ArrayList<>();
            for (Long diaryId : diaryIds) {
                allTags.addAll(diaryMapper.findTagsByDiaryId(diaryId));
            }
        } else {
            allTags = diaryMapper.findTagsByUserIdAndDateRange(userId, startDate, endDate);
        }
        
        log.info("查询到的标签: {}", allTags);
        
        if (allTags.isEmpty()) {
            log.warn("未找到任何情绪标签数据");
            return Result.success(Collections.emptyList());
        }
        
        Map<String, Long> emotionCounts = allTags.stream()
                .filter(EMOTION_MAP::containsKey)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        
        log.info("情绪统计: {}", emotionCounts);
        
        long totalEmotions = emotionCounts.values().stream().mapToLong(Long::longValue).sum();
        if (totalEmotions == 0) {
            log.warn("没有有效的情绪数据");
            return Result.success(Collections.emptyList());
        }
        
        List<EmotionShareVO> result = emotionCounts.entrySet().stream()
                .map(entry -> new EmotionShareVO(entry.getKey(), (double) entry.getValue() / totalEmotions * 100))
                .sorted(Comparator.comparing(EmotionShareVO::getPercentage).reversed())
                .collect(Collectors.toList());
        
        log.info("情绪分布结果: {}", result);
        return Result.success(result);
    }

    @Override
    public Result<List<Map<String, Object>>> getEmotionFluctuationMulti(Long userId, Date startDate, Date endDate) {
        // 获取基础情绪波动数据
        Result<List<EmotionDataPointVO>> fluctuationResult = getEmotionFluctuation(userId, startDate, endDate);
        if (fluctuationResult.getCode() != 200) {
            return Result.fail(fluctuationResult.getMessage());
        }
        
        List<EmotionDataPointVO> baseData = fluctuationResult.getData();
        List<Map<String, Object>> multiData = new ArrayList<>();
        
        // 为每个日期创建多维度情绪数据
        for (EmotionDataPointVO point : baseData) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", point.getDate());
            
            // 根据情绪值生成各情绪的数据
            Double emotionValue = point.getValue();
            if (emotionValue != null) {
                // 根据情绪值映射到各情绪类型
                dayData.put("开心", emotionValue > 0.5 ? emotionValue * 0.8 : 0);
                dayData.put("激动", emotionValue > 0.3 ? emotionValue * 0.6 : 0);
                dayData.put("平静", Math.abs(emotionValue) < 0.2 ? 0.5 : 0);
                dayData.put("无聊", emotionValue < -0.3 ? Math.abs(emotionValue) * 0.4 : 0);
                dayData.put("疲惫", emotionValue < -0.5 ? Math.abs(emotionValue) * 0.6 : 0);
                dayData.put("难过", emotionValue < -0.7 ? Math.abs(emotionValue) * 0.8 : 0);
                dayData.put("焦虑", emotionValue < -0.8 ? Math.abs(emotionValue) * 0.9 : 0);
                dayData.put("愤怒", emotionValue < -0.9 ? Math.abs(emotionValue) : 0);
            } else {
                // 如果没有情绪数据，设置默认值
                dayData.put("开心", 0);
                dayData.put("激动", 0);
                dayData.put("平静", 0);
                dayData.put("无聊", 0);
                dayData.put("疲惫", 0);
                dayData.put("难过", 0);
                dayData.put("焦虑", 0);
                dayData.put("愤怒", 0);
            }
            
            multiData.add(dayData);
        }
        
        return Result.success(multiData);
    }

    @Override
    public Result<List<String>> getAIAnalysisPoints(Map<String, Object> emotionData) {
        List<String> analysisPoints = new ArrayList<>();
        
        try {
            // 分析情绪数据并生成AI分析点
            List<Map<String, Object>> fluctuation = (List<Map<String, Object>>) emotionData.get("fluctuation");
            List<Map<String, Object>> distribution = (List<Map<String, Object>>) emotionData.get("distribution");
            
            // 构建分析数据
            StringBuilder analysisData = new StringBuilder();
            analysisData.append("情绪分析数据：\n");
            
            if (fluctuation != null && !fluctuation.isEmpty()) {
                analysisData.append("情绪波动数据：").append(fluctuation.size()).append("天的数据\n");
                // 计算情绪波动趋势
                double avgFluctuation = fluctuation.stream()
                    .mapToDouble(d -> {
                        double total = 0;
                        int count = 0;
                        for (String emotion : new String[]{"开心", "激动", "平静", "无聊", "疲惫", "难过", "焦虑", "愤怒"}) {
                            Object value = d.get(emotion);
                            if (value instanceof Number) {
                                total += ((Number) value).doubleValue();
                                count++;
                            }
                        }
                        return count > 0 ? total / count : 0;
                    })
                    .average()
                    .orElse(0);
                analysisData.append("平均情绪波动值：").append(String.format("%.2f", avgFluctuation)).append("\n");
            }
            
            if (distribution != null && !distribution.isEmpty()) {
                analysisData.append("情绪分布数据：\n");
                for (Map<String, Object> dist : distribution) {
                    String emotion = (String) dist.get("emotion");
                    Object percentageObj = dist.get("percentage");
                    if (emotion != null && percentageObj != null) {
                        double percentage = 0.0;
                        if (percentageObj instanceof Number) {
                            percentage = ((Number) percentageObj).doubleValue();
                        }
                        analysisData.append(emotion).append(": ").append(String.format("%.1f", percentage)).append("%\n");
                    }
                }
            }
            
            // 调用AI服务进行四维度分析
            String aiPrompt = "请基于以下情绪数据，从四个维度进行分析：\n" +
                "1. 情绪稳定性分析\n" +
                "2. 情绪健康度评估\n" +
                "3. 情绪调节建议\n" +
                "4. 个性化改善方案\n\n" +
                "数据：" + analysisData.toString() + "\n" +
                "请为每个维度提供一条简洁实用的分析建议，每条不超过20字。\n" +
                "请严格按照以下格式返回：\n" +
                "1. [分析内容]\n" +
                "2. [分析内容]\n" +
                "3. [分析内容]\n" +
                "4. [分析内容]";
            
            // 调用AI服务
            log.info("开始调用AI服务进行情绪分析");
            Result<String> aiResponse = callAIService(aiPrompt);
            log.info("AI服务响应: {}", aiResponse);
            
            if (aiResponse.getCode() == 200 && aiResponse.getData() != null) {
                String content = aiResponse.getData();
                log.info("AI返回内容: {}", content);
                
                // 检查是否为Mock响应
                if (isMockResponse(content)) {
                    log.warn("检测到Mock响应，返回正在获取状态");
                    return Result.fail("正在获取AI分析，请稍后重试");
                }
                
                // 解析AI响应，提取分析点
                String[] lines = content.split("\n");
                for (String line : lines) {
                    line = line.trim();
                    if (!line.isEmpty() && (line.contains("1.") || line.contains("2.") || 
                                         line.contains("3.") || line.contains("4.") ||
                                         line.contains("情绪") || line.contains("建议") || 
                                         line.contains("分析") || line.contains("改善"))) {
                        // 提取分析内容
                        String analysisContent = line.replaceAll("^[0-9]+\\.\\s*", "").trim();
                        // 移除markdown格式字符
                        analysisContent = analysisContent.replaceAll("\\*\\*", "").replaceAll("\\*", "")
                                       .replaceAll("`", "").replaceAll("#", "")
                                       .replaceAll("\\[", "").replaceAll("\\]", "")
                                       .replaceAll("\\(", "").replaceAll("\\)", "")
                                       .replaceAll("\\{", "").replaceAll("\\}", "")
                                       .replaceAll("\\|", "").replaceAll("~", "")
                                       .replaceAll(">", "").replaceAll("<", "")
                                       .replaceAll("_", "").replaceAll("-", "")
                                       .trim();
                        if (analysisContent.length() > 0 && analysisContent.length() <= 30) {
                            analysisPoints.add(analysisContent);
                        }
                    }
                }
                
                // 检查是否成功解析到分析点
                if (analysisPoints.size() >= 4) {
                    // 返回解析后的分析点
                    List<String> finalResults = analysisPoints.subList(0, 4);
                    log.info("最终AI分析结果: {}", finalResults);
                    return Result.success(finalResults);
                } else {
                    log.warn("AI分析结果不足4个点，返回正在获取状态");
                    return Result.fail("正在获取AI分析，请稍后重试");
                }
            } else {
                log.warn("AI服务调用失败，返回正在获取状态");
                return Result.fail("正在获取AI分析，请稍后重试");
            }
        } catch (Exception e) {
            log.error("生成AI分析点失败", e);
            return Result.fail("正在获取AI分析，请稍后重试");
        }
    }
    
    /**
     * 调用AI服务
     */
    private Result<String> callAIService(String prompt) {
        try {
            // 直接调用DeepSeek AI服务
            log.info("调用DeepSeek AI服务，prompt: {}", prompt);
            Result<String> aiResponse = aiService.getGeneralResponse(prompt);
            log.info("DeepSeek AI响应: {}", aiResponse);
            
            if (aiResponse.getCode() == 200 && aiResponse.getData() != null) {
                String content = aiResponse.getData();
                log.info("AI返回内容: {}", content);
                
                // 验证返回的内容不是Mock服务的固定文本
                if (isMockResponse(content)) {
                    log.warn("检测到Mock AI响应，返回失败");
                    return Result.fail("AI服务返回Mock响应");
                }
                return aiResponse;
            } else {
                log.warn("DeepSeek AI服务失败，返回错误: {}", aiResponse.getMessage());
                return Result.fail("AI服务返回错误: " + aiResponse.getMessage());
            }
        } catch (Exception e) {
            log.error("调用AI服务失败", e);
            return Result.fail("AI服务调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查是否为Mock响应
     */
    private boolean isMockResponse(String content) {
        String[] mockKeywords = {
            "这是一个很好的问题，我现在还在学习中，暂时无法回答",
            "感谢你的记录，很高兴能成为你的倾听者",
            "根据您的情绪数据，整体情绪状态积极向上",
            "检测到您的情绪存在波动和消极倾向",
            "您的情绪波动较大，建议保持规律作息",
            "基于您的情绪数据，建议保持当前的生活方式"
        };
        
        for (String keyword : mockKeywords) {
            if (content.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
} 