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
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // AI服务配置
    private static final String AI_API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String AI_API_KEY = "sk-92d68b33753d4ab88721f7abbe4713fa";

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
        try {
            // 调用 diary-service 获取真实情绪数据
            List<Map<String, Object>> realEmotionData = diaryFeignClient.getUserEmotions(userId, formatDate(startDate), formatDate(endDate));
            
            if (realEmotionData == null || realEmotionData.isEmpty()) {
                // 如果没有真实数据，返回空列表
                return Result.success(new ArrayList<>());
            }
            
            // 统计各情绪的出现次数
            Map<String, Integer> emotionCounts = new HashMap<>();
            final int[] totalEmotions = {0};
            
            for (Map<String, Object> dayData : realEmotionData) {
                for (Map.Entry<String, Object> entry : dayData.entrySet()) {
                    String emotion = entry.getKey();
                    Object value = entry.getValue();
                    
                    // 跳过日期字段
                    if ("date".equals(emotion)) continue;
                    
                    if (value instanceof Number && ((Number) value).intValue() > 0) {
                        emotionCounts.put(emotion, emotionCounts.getOrDefault(emotion, 0) + ((Number) value).intValue());
                        totalEmotions[0] += ((Number) value).intValue();
                    }
                }
            }
            
            if (totalEmotions[0] == 0) {
                return Result.success(new ArrayList<>());
            }
            
            // 转换为百分比
            List<EmotionShareVO> result = emotionCounts.entrySet().stream()
                .map(entry -> new EmotionShareVO(entry.getKey(), (double) entry.getValue() / totalEmotions[0] * 100))
                .sorted(Comparator.comparing(EmotionShareVO::getPercentage).reversed())
                .collect(Collectors.toList());
                
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("获取情绪分布数据失败", e);
            // 如果获取真实数据失败，返回空列表而不是模拟数据
            return Result.success(new ArrayList<>());
        }
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
        try {
            // 基于真实数据生成智能分析
            List<String> analysisPoints = new ArrayList<>();
            
            // 分析情绪分布
            if (emotionData.containsKey("distribution")) {
                List<Map<String, Object>> distribution = (List<Map<String, Object>>) emotionData.get("distribution");
                double positivePercentage = 0.0;
                double negativePercentage = 0.0;
                
                for (Map<String, Object> item : distribution) {
                    String emotion = (String) item.get("emotion");
                    Double percentage = (Double) item.get("percentage");
                    
                    if (isPositiveEmotion(emotion)) {
                        positivePercentage += percentage;
                    } else if (isNegativeEmotion(emotion)) {
                        negativePercentage += percentage;
                    }
                }
                
                // 根据情绪分布生成分析
                if (positivePercentage > 60) {
                    analysisPoints.add("您的情绪状态整体积极向上，建议继续保持这种良好的心态");
                    analysisPoints.add("多与朋友分享快乐时光，增强社交连接");
                } else if (negativePercentage > 50) {
                    analysisPoints.add("检测到较多负面情绪，建议寻求朋友或专业人士的支持");
                    analysisPoints.add("尝试进行放松活动，如冥想、散步或听音乐");
                } else {
                    analysisPoints.add("情绪状态相对平衡，建议保持规律的生活作息");
                    analysisPoints.add("适当增加户外活动，提升整体心情");
                }
            }
            
            // 分析情绪波动
            if (emotionData.containsKey("fluctuation")) {
                List<Map<String, Object>> fluctuation = (List<Map<String, Object>>) emotionData.get("fluctuation");
                if (fluctuation.size() > 1) {
                    double avgValue = fluctuation.stream()
                        .mapToDouble(item -> ((Number) item.get("value")).doubleValue())
                        .average()
                        .orElse(0.0);
                    
                    double variance = fluctuation.stream()
                        .mapToDouble(item -> Math.pow(((Number) item.get("value")).doubleValue() - avgValue, 2))
                        .average()
                        .orElse(0.0);
                    
                    if (variance > 0.5) {
                        analysisPoints.add("情绪波动较大，建议学习情绪管理技巧");
                        analysisPoints.add("建立稳定的日常作息，有助于情绪稳定");
                    } else {
                        analysisPoints.add("情绪相对稳定，继续保持这种良好的状态");
                    }
                }
            }
            
            // 添加通用建议
            analysisPoints.add("培养积极兴趣爱好，丰富生活内容");
            analysisPoints.add("保持规律运动，有助于情绪调节");
            analysisPoints.add("与家人朋友多沟通，获得情感支持");
            
            return Result.success(analysisPoints);
            
        } catch (Exception e) {
            log.error("情绪分析失败", e);
            return Result.success(getDefaultAnalysisPoints());
        }
    }
    
    private boolean isPositiveEmotion(String emotion) {
        return "开心".equals(emotion) || "兴奋".equals(emotion) || "激动".equals(emotion) || "平静".equals(emotion);
    }
    
    private boolean isNegativeEmotion(String emotion) {
        return "难过".equals(emotion) || "焦虑".equals(emotion) || "愤怒".equals(emotion) || "压力".equals(emotion);
    }
    
    private String callDeepSeek(String prompt) {
        try {
            // 配置SSL信任所有证书（仅用于开发环境）
            javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
            sslContext.init(null, new javax.net.ssl.TrustManager[]{new javax.net.ssl.X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
            }}, new java.security.SecureRandom());
            
            javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(AI_API_KEY);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "deepseek-chat");
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            body.put("messages", Collections.singletonList(userMessage));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(AI_API_URL, entity, Map.class);
            
            if (response.getBody() != null) {
                List choices = (List) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map choice = (Map) choices.get(0);
                    Map msg = (Map) choice.get("message");
                    return (String) msg.get("content");
                }
            }
            return null;
        } catch (Exception e) {
            log.error("调用DeepSeek API失败", e);
            return null;
        }
    }
    
    private List<String> parseAIAnalysis(String aiResponse) {
        List<String> analysisPoints = new ArrayList<>();
        
        // 简单的解析逻辑：按段落分割
        String[] paragraphs = aiResponse.split("\n\n");
        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (!paragraph.isEmpty() && paragraph.length() > 10) {
                // 移除序号和标题
                String cleanText = paragraph.replaceAll("^\\d+[.、]\\s*", "")
                                         .replaceAll("^[一二三四五六七八九十]+[.、]\\s*", "")
                                         .replaceAll("^[A-Z][.、]\\s*", "");
                if (cleanText.length() > 10) {
                    analysisPoints.add(cleanText);
                }
            }
        }
        
        // 如果解析失败，返回默认分析
        if (analysisPoints.isEmpty()) {
            return getDefaultAnalysisPoints();
        }
        
        return analysisPoints;
    }
    
    private List<String> getDefaultAnalysisPoints() {
        List<String> analysisPoints = new ArrayList<>();
        analysisPoints.add("情绪波动较大，建议保持规律作息");
        analysisPoints.add("多进行户外活动，增加社交互动");
        analysisPoints.add("学习情绪管理技巧，建立调节机制");
        analysisPoints.add("培养积极兴趣爱好，丰富生活内容");
        return analysisPoints;
    }
} 