package com.dailybaro.aiknowledge.service.impl;

import com.dailybaro.aiknowledge.mapper.KnowledgeMapper;
import com.dailybaro.aiknowledge.model.Knowledge;
import com.dailybaro.aiknowledge.model.vo.KnowledgeVO;
import com.dailybaro.aiknowledge.service.KnowledgeService;
import com.dailybaro.aiknowledge.service.RecommendationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecommendationServiceImpl implements RecommendationService {
    
    @Autowired
    private KnowledgeMapper knowledgeMapper;
    
    @Autowired
    private KnowledgeService knowledgeService;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${gateway.url:http://localhost:8000}")
    private String gatewayUrl;
    
    private static final String DAILY_RECOMMENDATION_KEY = "daily_recommendation:user:%d:date:%s";
    private static final String EMOTION_CACHE_KEY = "user_emotion:user:%d:week:%s";
    
    // 情绪到知识分类的映射
    private static final Map<String, List<String>> EMOTION_TO_CATEGORIES = new HashMap<>();
    private static final Map<String, List<String>> EMOTION_TO_SUBCATEGORIES = new HashMap<>();
    
    static {
        // 负面情绪 -> 排忧解难类内容
        EMOTION_TO_CATEGORIES.put("焦虑", Arrays.asList("心理排忧", "情绪管理"));
        EMOTION_TO_CATEGORIES.put("抑郁", Arrays.asList("心理排忧", "成长指南"));
        EMOTION_TO_CATEGORIES.put("悲伤", Arrays.asList("心理排忧", "案例分享"));
        EMOTION_TO_CATEGORIES.put("愤怒", Arrays.asList("情绪管理", "心理排忧"));
        EMOTION_TO_CATEGORIES.put("压力", Arrays.asList("心理排忧", "成长指南"));
        EMOTION_TO_CATEGORIES.put("担心", Arrays.asList("心理排忧", "情绪管理"));
        EMOTION_TO_CATEGORIES.put("不安", Arrays.asList("心理排忧", "情绪管理"));
        EMOTION_TO_CATEGORIES.put("难过", Arrays.asList("心理排忧", "案例分享"));
        EMOTION_TO_CATEGORIES.put("伤心", Arrays.asList("心理排忧", "案例分享"));
        EMOTION_TO_CATEGORIES.put("生气", Arrays.asList("情绪管理", "心理排忧"));
        EMOTION_TO_CATEGORIES.put("negative", Arrays.asList("心理排忧", "情绪管理"));
        
        // 正面情绪 -> 积极内容
        EMOTION_TO_CATEGORIES.put("开心", Arrays.asList("成长指南", "案例分享"));
        EMOTION_TO_CATEGORIES.put("高兴", Arrays.asList("成长指南", "案例分享"));
        EMOTION_TO_CATEGORIES.put("愉快", Arrays.asList("成长指南", "案例分享"));
        EMOTION_TO_CATEGORIES.put("兴奋", Arrays.asList("成长指南", "案例分享"));
        EMOTION_TO_CATEGORIES.put("满足", Arrays.asList("成长指南", "案例分享"));
        EMOTION_TO_CATEGORIES.put("平静", Arrays.asList("成长指南", "情绪管理"));
        EMOTION_TO_CATEGORIES.put("平和", Arrays.asList("成长指南", "情绪管理"));
        EMOTION_TO_CATEGORIES.put("positive", Arrays.asList("成长指南", "案例分享"));
        
        // 中性情绪
        EMOTION_TO_CATEGORIES.put("中性", Arrays.asList("成长指南", "情绪管理"));
        EMOTION_TO_CATEGORIES.put("neutral", Arrays.asList("成长指南", "情绪管理"));
        EMOTION_TO_CATEGORIES.put("冷静", Arrays.asList("成长指南", "情绪管理"));
        
        // 子分类映射
        EMOTION_TO_SUBCATEGORIES.put("焦虑", Arrays.asList("常见问题", "自我调节"));
        EMOTION_TO_SUBCATEGORIES.put("抑郁", Arrays.asList("常见问题", "自我认知"));
        EMOTION_TO_SUBCATEGORIES.put("悲伤", Arrays.asList("常见问题"));
        EMOTION_TO_SUBCATEGORIES.put("愤怒", Arrays.asList("自我调节"));
        EMOTION_TO_SUBCATEGORIES.put("压力", Arrays.asList("常见问题", "自我调节"));
        EMOTION_TO_SUBCATEGORIES.put("开心", Arrays.asList("自我认知", "人际关系"));
        EMOTION_TO_SUBCATEGORIES.put("高兴", Arrays.asList("自我认知", "人际关系"));
        EMOTION_TO_SUBCATEGORIES.put("愉快", Arrays.asList("自我认知", "人际关系"));
    }
    
    @Override
    public List<KnowledgeVO> recommendByUserEmotion(Long userId, int days) {
        try {
            // 1. 获取用户近一周的情绪数据
            List<Map<String, Object>> emotionHistory = getUserEmotionHistory(userId, days);
            
            if (emotionHistory == null || emotionHistory.isEmpty()) {
                // 没有情绪数据，返回热门文章
                return getPopularKnowledge(5);
            }
            
            // 2. 分析情绪趋势
            EmotionAnalysis analysis = analyzeEmotionTrend(emotionHistory);
            
            // 3. 根据情绪推荐知识
            List<KnowledgeVO> recommendations = recommendKnowledgeByEmotion(analysis);
            
            // 4. 如果推荐不足，补充热门文章
            if (recommendations.size() < 5) {
                List<KnowledgeVO> popular = getPopularKnowledge(5 - recommendations.size());
                recommendations.addAll(popular);
            }
            
            return recommendations.stream()
                    .distinct()
                    .limit(10)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("推荐失败 userId={}, days={}", userId, days, e);
            // 降级：返回热门文章
            return getPopularKnowledge(5);
        }
    }
    
    @Override
    public KnowledgeVO getDailyRecommendation(Long userId) {
        try {
            String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            String key = String.format(DAILY_RECOMMENDATION_KEY, userId, today);
            
            if (redisTemplate != null && redisTemplate.hasKey(key)) {
                Object cached = redisTemplate.opsForValue().get(key);
                if (cached instanceof Map) {
                    return convertToVO((Map<String, Object>) cached);
                }
            }
            
            // 缓存未命中，生成新的推荐
            generateDailyRecommendation(userId);
            
            // 再次尝试获取
            if (redisTemplate != null && redisTemplate.hasKey(key)) {
                Object cached = redisTemplate.opsForValue().get(key);
                if (cached instanceof Map) {
                    return convertToVO((Map<String, Object>) cached);
                }
            }
            
            // 仍然失败，返回随机文章
            return getRandomKnowledge();
            
        } catch (Exception e) {
            log.error("获取每日推荐失败 userId={}", userId, e);
            return getRandomKnowledge();
        }
    }
    
    @Override
    public void generateDailyRecommendation(Long userId) {
        try {
            String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            String key = String.format(DAILY_RECOMMENDATION_KEY, userId, today);
            
            // 基于近一周心情推荐
            List<KnowledgeVO> recommendations = recommendByUserEmotion(userId, 7);
            
            if (!recommendations.isEmpty()) {
                KnowledgeVO daily = recommendations.get(0);
                
                if (redisTemplate != null) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", daily.getId());
                    data.put("title", daily.getTitle());
                    data.put("content", daily.getContent());
                    data.put("category", daily.getCategory());
                    data.put("subcategory", daily.getSubcategory());
                    data.put("summary", daily.getSummary());
                    
                    // 缓存到明天
                    long ttl = LocalDate.now().plusDays(1).atStartOfDay()
                            .toEpochSecond(java.time.ZoneOffset.of("+8")) 
                            - System.currentTimeMillis() / 1000;
                    redisTemplate.opsForValue().set(key, data, ttl, TimeUnit.SECONDS);
                    log.info("生成每日推荐 userId={}, knowledgeId={}, ttl={}秒", userId, daily.getId(), ttl);
                }
            }
        } catch (Exception e) {
            log.error("生成每日推荐失败 userId={}", userId, e);
        }
    }
    
    /**
     * 获取用户情绪历史
     */
    private List<Map<String, Object>> getUserEmotionHistory(Long userId, int days) {
        try {
            // 先尝试从Redis缓存获取
            String weekKey = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-ww"));
            String cacheKey = String.format(EMOTION_CACHE_KEY, userId, weekKey);
            
            if (redisTemplate != null && redisTemplate.hasKey(cacheKey)) {
                Object cached = redisTemplate.opsForValue().get(cacheKey);
                if (cached instanceof List) {
                    return (List<Map<String, Object>>) cached;
                }
            }
            
            // 从emotion-service获取
            HttpHeaders headers = new HttpHeaders();
            headers.set("uid", String.valueOf(userId));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = gatewayUrl + "/api/analysis/result?uid=" + userId;
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (body.get("code") != null && (Integer) body.get("code") == 200) {
                    Object data = body.get("data");
                    if (data instanceof List) {
                        List<Map<String, Object>> history = (List<Map<String, Object>>) data;
                        
                        // 过滤最近N天的数据
                        LocalDate cutoff = LocalDate.now().minusDays(days);
                        List<Map<String, Object>> recent = history.stream()
                                .filter(item -> {
                                    Object timestamp = item.get("timestamp");
                                    if (timestamp == null) timestamp = item.get("createTime");
                                    if (timestamp == null) return false;
                                    
                                    try {
                                        LocalDate date = parseDate(timestamp);
                                        return date != null && !date.isBefore(cutoff);
                                    } catch (Exception e) {
                                        return false;
                                    }
                                })
                                .collect(Collectors.toList());
                        
                        // 缓存到Redis（缓存7天）
                        if (redisTemplate != null && !recent.isEmpty()) {
                            redisTemplate.opsForValue().set(cacheKey, recent, 7, TimeUnit.DAYS);
                        }
                        
                        return recent;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取用户情绪历史失败 userId={}, days={}: {}", userId, days, e.getMessage());
        }
        return Collections.emptyList();
    }
    
    /**
     * 分析情绪趋势
     */
    private EmotionAnalysis analyzeEmotionTrend(List<Map<String, Object>> history) {
        EmotionAnalysis analysis = new EmotionAnalysis();
        
        if (history == null || history.isEmpty()) {
            analysis.dominantEmotion = "平静";
            analysis.positiveRatio = 0.5;
            analysis.emotionScore = 5.0;
            return analysis;
        }
        
        // 统计情绪
        Map<String, Integer> emotionCount = new HashMap<>();
        int positiveCount = 0;
        double totalScore = 0;
        int count = 0;
        
        for (Map<String, Object> item : history) {
            String emotion = extractEmotion(item);
            if (emotion != null) {
                emotionCount.merge(emotion, 1, Integer::sum);
                if (isPositiveEmotion(emotion)) {
                    positiveCount++;
                }
            }
            
            // 计算情绪分数（如果有）
            Object score = item.get("score");
            if (score != null) {
                try {
                    totalScore += Double.parseDouble(String.valueOf(score));
                    count++;
                } catch (Exception ignored) {}
            }
        }
        
        // 主导情绪
        analysis.dominantEmotion = emotionCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("平静");
        
        // 正面情绪比例
        analysis.positiveRatio = history.isEmpty() ? 0.5 : (double) positiveCount / history.size();
        
        // 情绪分数（0-10，5为中性）
        analysis.emotionScore = count > 0 ? totalScore / count : 5.0;
        
        return analysis;
    }
    
    /**
     * 根据情绪推荐知识
     */
    private List<KnowledgeVO> recommendKnowledgeByEmotion(EmotionAnalysis analysis) {
        List<KnowledgeVO> recommendations = new ArrayList<>();
        
        // 根据情绪分数决定推荐策略
        if (analysis.emotionScore < 4.0 || analysis.positiveRatio < 0.3) {
            // 负面情绪较多 -> 推荐排忧解难内容
            recommendations.addAll(getKnowledgeByEmotion(analysis.dominantEmotion, true));
        } else if (analysis.emotionScore > 6.0 || analysis.positiveRatio > 0.7) {
            // 正面情绪较多 -> 推荐积极成长内容
            recommendations.addAll(getKnowledgeByEmotion(analysis.dominantEmotion, false));
        } else {
            // 情绪平衡 -> 推荐多样化内容
            recommendations.addAll(getKnowledgeByEmotion(analysis.dominantEmotion, false));
            recommendations.addAll(getPopularKnowledge(3));
        }
        
        return recommendations;
    }
    
    /**
     * 根据情绪获取知识
     */
    private List<KnowledgeVO> getKnowledgeByEmotion(String emotion, boolean isNegative) {
        List<KnowledgeVO> result = new ArrayList<>();
        
        // 获取情绪对应的分类
        List<String> categories = EMOTION_TO_CATEGORIES.getOrDefault(emotion, 
                isNegative ? Arrays.asList("心理排忧", "情绪管理") : Arrays.asList("成长指南", "案例分享"));
        
        // 获取子分类
        List<String> subcategories = EMOTION_TO_SUBCATEGORIES.getOrDefault(emotion, Collections.emptyList());
        
        // 按分类查询
        for (String category : categories) {
            List<Knowledge> knowledgeList = knowledgeMapper.selectByCategory(category);
            for (Knowledge k : knowledgeList) {
                if (k.getStatus() == 1) { // 只推荐已发布的内容
                    KnowledgeVO vo = convertToVO(k);
                    result.add(vo);
                }
            }
        }
        
        // 按子分类查询
        for (String category : categories) {
            for (String subcategory : subcategories) {
                List<Knowledge> knowledgeList = knowledgeMapper.selectBySubcategory(category, subcategory);
                for (Knowledge k : knowledgeList) {
                    if (k.getStatus() == 1) {
                        KnowledgeVO vo = convertToVO(k);
                        result.add(vo);
                    }
                }
            }
        }
        
        // 去重并随机排序
        return result.stream()
                .distinct()
                .sorted((a, b) -> new Random().nextInt(3) - 1)
                .limit(10)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取热门文章
     */
    private List<KnowledgeVO> getPopularKnowledge(int limit) {
        List<Knowledge> all = knowledgeMapper.selectByStatus(1); // 已发布
        return all.stream()
                .sorted((a, b) -> Integer.compare(
                        b.getViewCount() != null ? b.getViewCount() : 0,
                        a.getViewCount() != null ? a.getViewCount() : 0))
                .limit(limit)
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取随机文章
     */
    private KnowledgeVO getRandomKnowledge() {
        List<Knowledge> all = knowledgeMapper.selectByStatus(1);
        if (all.isEmpty()) {
            return null;
        }
        Knowledge random = all.get(new Random().nextInt(all.size()));
        return convertToVO(random);
    }
    
    /**
     * 判断是否为正面情绪
     */
    private boolean isPositiveEmotion(String emotion) {
        if (emotion == null) return false;
        String lower = emotion.toLowerCase();
        return lower.contains("开心") || lower.contains("高兴") || lower.contains("愉快") 
                || lower.contains("兴奋") || lower.contains("满足") || lower.contains("平静")
                || lower.contains("平和") || lower.contains("positive") || lower.contains("happy")
                || lower.contains("joy") || lower.contains("calm");
    }
    
    /**
     * 提取情绪标签
     */
    private String extractEmotion(Map<String, Object> item) {
        Object emotion = item.get("emotion");
        if (emotion == null) emotion = item.get("primaryEmotion");
        if (emotion == null) emotion = item.get("label");
        if (emotion == null) {
            // 尝试从tags中提取
            Object tags = item.get("tags");
            if (tags instanceof List) {
                List<?> tagList = (List<?>) tags;
                for (Object tag : tagList) {
                    String tagStr = String.valueOf(tag);
                    if (isEmotionTag(tagStr)) {
                        return tagStr;
                    }
                }
            }
        }
        return emotion != null ? String.valueOf(emotion) : null;
    }
    
    /**
     * 判断是否为情绪标签
     */
    private boolean isEmotionTag(String tag) {
        if (tag == null) return false;
        String lower = tag.toLowerCase();
        return lower.contains("焦虑") || lower.contains("抑郁") || lower.contains("悲伤")
                || lower.contains("愤怒") || lower.contains("压力") || lower.contains("开心")
                || lower.contains("高兴") || lower.contains("愉快") || lower.contains("平静")
                || lower.contains("担心") || lower.contains("不安") || lower.contains("难过");
    }
    
    /**
     * 解析日期
     */
    private LocalDate parseDate(Object dateObj) {
        if (dateObj == null) return null;
        try {
            if (dateObj instanceof String) {
                String dateStr = (String) dateObj;
                // 尝试多种格式
                try {
                    return LocalDate.parse(dateStr.substring(0, 10));
                } catch (Exception e) {
                    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
            } else if (dateObj instanceof Long) {
                return new java.util.Date((Long) dateObj).toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
            }
        } catch (Exception e) {
            log.debug("日期解析失败: {}", dateObj);
        }
        return null;
    }
    
    /**
     * 转换为VO
     */
    private KnowledgeVO convertToVO(Knowledge knowledge) {
        KnowledgeVO vo = new KnowledgeVO();
        BeanUtils.copyProperties(knowledge, vo);
        String content = knowledge.getContent();
        if (content != null && content.length() > 200) {
            vo.setSummary(content.substring(0, 200) + "...");
        } else {
            vo.setSummary(content);
        }
        return vo;
    }
    
    /**
     * 从Map转换为VO
     */
    private KnowledgeVO convertToVO(Map<String, Object> map) {
        KnowledgeVO vo = new KnowledgeVO();
        if (map.get("id") != null) {
            vo.setId(Long.valueOf(String.valueOf(map.get("id"))));
        }
        vo.setTitle((String) map.get("title"));
        vo.setContent((String) map.get("content"));
        vo.setCategory((String) map.get("category"));
        vo.setSubcategory((String) map.get("subcategory"));
        vo.setSummary((String) map.get("summary"));
        return vo;
    }
    
    /**
     * 情绪分析结果
     */
    private static class EmotionAnalysis {
        String dominantEmotion;
        double positiveRatio;
        double emotionScore;
    }
}
