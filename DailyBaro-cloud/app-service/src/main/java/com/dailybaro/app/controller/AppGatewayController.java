package com.dailybaro.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;

@RestController
@RequestMapping("/app")
public class AppGatewayController {

    @Autowired
    private RestTemplate restTemplate;

    private static final String GATEWAY = "http://localhost:8000";
    private static final String USER_SERVICE = "http://localhost:8001";

    // 统一转发：日签
    @GetMapping("/quotes/custom")
    public Object getCustom(@RequestHeader(value = "uid", required = false) String uid){
        return forwardGet("/api/quotes/custom", uid);
    }
    @GetMapping("/quotes/random/user")
    public Object getRandomUser(@RequestHeader(value = "uid", required = false) String uid){
        return forwardGet("/api/quotes/random/user", uid);
    }
    @GetMapping("/quotes/random/user/manual")
    public Object getRandomUserManual(@RequestHeader(value = "uid", required = false) String uid){
        return forwardGet("/api/quotes/random/user/manual", uid);
    }
    @GetMapping("/quotes/history")
    public Object getHistory(@RequestHeader(value = "uid", required = false) String uid){
        return forwardGet("/api/quotes/history", uid);
    }
    @PostMapping("/quotes/custom")
    public Object saveCustom(@RequestHeader(value = "uid", required = false) String uid, @RequestBody Map<String,Object> body){
        return forwardPost("/api/quotes/custom", uid, body);
    }
    @PutMapping("/quotes/custom/{id}")
    public Object updateQuote(@PathVariable("id") String id, @RequestHeader(value = "uid", required = false) String uid, @RequestBody Map<String,Object> body){
        return forwardPost("/api/quotes/" + id, uid, body); // 后端使用 PUT /api/quotes/{quoteId}
    }
    @DeleteMapping("/quotes/custom/{id}")
    public Object deleteQuote(@PathVariable("id") String id, @RequestHeader(value = "uid", required = false) String uid){
        return forwardDelete("/api/quotes/" + id, uid); // 后端使用 DELETE /api/quotes/{quoteId}
    }

    // 胶囊
    @GetMapping("/capsules")
    public Object listCapsules(@RequestHeader(value = "uid", required = false) String uid){
        return forwardGet("/api/capsules", uid);
    }
    @PostMapping("/capsules/json")
    public Object createCapsuleJson(@RequestHeader(value = "uid", required = false) String uid, @RequestBody Map<String,Object> body){
        return forwardPost("/api/capsules/json", uid, body);
    }
    @GetMapping("/capsules/{id}")
    public Object getCapsule(@PathVariable("id") String id, @RequestHeader(value = "uid", required = false) String uid){
        return forwardGet("/api/capsules/" + id, uid);
    }
    @PostMapping("/capsules/{id}/open")
    public Object openCapsule(@PathVariable("id") String id, @RequestHeader(value = "uid", required = false) String uid){
        return forwardPost("/api/capsules/" + id + "/open", uid, Map.of());
    }
    @DeleteMapping("/capsules/{id}")
    public Object deleteCapsule(@PathVariable("id") String id, @RequestHeader(value = "uid", required = false) String uid){
        return forwardDelete("/api/capsules/" + id, uid);
    }
    @GetMapping("/capsules/reminders/unread")
    public Object getUnreadReminders(@RequestHeader(value = "uid", required = false) String uid){
        return forwardGet("/api/capsules/reminders/unread", uid);
    }
    @PostMapping("/capsules/reminders/{id}/read")
    public Object markReminderRead(@PathVariable("id") String id, @RequestHeader(value = "uid", required = false) String uid){
        return forwardPost("/api/capsules/reminders/" + id + "/read", uid, Map.of());
    }

    // 盲盒
    @GetMapping("/mystery/energy")
    public Object energy(@RequestHeader(value = "uid", required = false) String uid){ return forwardGet("/api/mystery-box/energy", uid); }
    @GetMapping("/mystery/status")
    public Object status(@RequestHeader(value = "uid", required = false) String uid){ return forwardGet("/api/mystery-box/status", uid); }
    @GetMapping("/mystery/records")
    public Object records(@RequestHeader(value = "uid", required = false) String uid){ return forwardGet("/api/mystery-box/records", uid); }
    @PostMapping("/mystery/draw")
    public Object draw(@RequestHeader(value = "uid", required = false) String uid){ return forwardPost("/api/mystery-box/draw", uid, Map.of()); }

    // 显式映射小程序现有路径：/app/api/mystery-box/**
    @GetMapping("/api/mystery-box/energy")
    public Object energyAlias(@RequestHeader(value = "uid", required = false) String uid) {
        return forwardGet("/api/mystery-box/energy", uid);
    }
    @GetMapping("/api/mystery-box/status")
    public Object statusAlias(@RequestHeader(value = "uid", required = false) String uid) {
        return forwardGet("/api/mystery-box/status", uid);
    }
    @GetMapping("/api/mystery-box/records")
    public Object recordsAlias(@RequestHeader(value = "uid", required = false) String uid) {
        return forwardGet("/api/mystery-box/records", uid);
    }
    @PostMapping("/api/mystery-box/draw")
    public Object drawAlias(@RequestHeader(value = "uid", required = false) String uid) {
        return forwardPost("/api/mystery-box/draw", uid, Map.of());
    }

    // 日记显式映射：/app/api/diary -> /api/diary（保留查询串）
    @GetMapping("/api/diary")
    public Object diaryList(HttpServletRequest request, @RequestHeader(value = "uid", required = false) String uid) {
        String qs = request.getQueryString();
        String path = "/api/diary" + (qs != null && !qs.isEmpty() ? ("?" + qs) : "");
        return forwardGet(path, uid);
    }

    // AI
    @PostMapping("/ai/query")
    public Object ai(@RequestHeader(value = "uid", required = false) String uid, @RequestBody Map<String,Object> body){ return forwardPost("/api/ai/query", uid, body); }

    // AI对话历史
    @GetMapping("/ai/conversation/{uid}")
    public Object getAiConversation(@PathVariable String uid, @RequestHeader(value = "uid", required = false) String headerUid) {
        if (headerUid != null && !headerUid.equals(uid)) {
            return Map.of("code", 403, "message", "权限不足");
        }
        return forwardGet("/api/ai/conversation/" + uid, uid);
    }
    
    // 清除AI对话历史
    @DeleteMapping("/ai/conversation/{uid}")
    public Object clearAiConversation(@PathVariable String uid, @RequestHeader(value = "uid", required = false) String headerUid) {
        if (headerUid != null && !headerUid.equals(uid)) {
            return Map.of("code", 403, "message", "权限不足");
        }
        return forwardDelete("/api/ai/conversation/" + uid, uid);
    }

    // NLP 健康检查（小程序调用）
    @GetMapping("/nlp/health")
    public Object nlpHealth() {
        return forwardGet("/api/nlp/health", null);
    }

    // 登录注册
    // 用户信息
    @GetMapping("/users/getMyInfo/{uid}")
    public Object getUserInfo(@PathVariable String uid, @RequestHeader(value = "uid", required = false) String headerUid){
        // 验证header中的uid与路径参数是否一致
        if (headerUid != null && !headerUid.equals(uid)) {
            return Map.of("code", 403, "message", "权限不足");
        }
        return forwardGet("/users/getMyInfo/" + uid, uid);
    }
    
    // 情绪分析
    @PostMapping("/emotion/analysis")
    public Object analyzeEmotion(@RequestHeader(value = "uid", required = false) String uid, @RequestBody Map<String,Object> body) {
        return forwardPost("/api/emotion/analysis", uid, body);
    }
    
    // 基于日记附件的情绪分析
    @PostMapping("/emotion/analysis/from-diary")
    public Object analyzeEmotionFromDiary(@RequestHeader(value = "uid", required = false) String uid, @RequestBody Map<String,Object> body) {
        return forwardPost("/api/emotion/analysis/from-diary", uid, body);
    }
    
    // 个性化推荐（转发到 knowledge-service 的推荐接口）
    @GetMapping("/recommendations")
    public Object getRecommendations(@RequestHeader(value = "uid", required = false) String uid, 
                                     @RequestParam(value = "days", defaultValue = "7") String days) {
        return forwardGet("/api/knowledge/recommendation/by-emotion?days=" + days, uid);
    }
    
    // 兼容 /api/recommendations 路径
    @GetMapping("/api/recommendations")
    public Object getRecommendationsApi(@RequestHeader(value = "uid", required = false) String uid,
                                        HttpServletRequest request) {
        String qs = request.getQueryString();
        String days = request.getParameter("days");
        if (days == null || days.isEmpty()) {
            days = "7";
        }
        return forwardGet("/api/knowledge/recommendation/by-emotion?days=" + days, uid);
    }
    
    // 活动执行接口（暂时转发到user-service，如果后续有专门的活动服务可以修改）
    @PostMapping("/activities/perform")
    public Object performActivity(@RequestHeader(value = "uid", required = false) String uid, @RequestBody Map<String,Object> body) {
        // 如果后续有专门的活动服务，可以修改转发路径
        // 目前暂时返回成功，实际业务逻辑需要根据需求实现
        // 可以转发到 user-service 或创建新的 activity-service
        return Map.of("code", 200, "message", "活动执行成功", "data", Map.of("activityId", body.get("activityId")));
    }

    @GetMapping("/analysis/result")
    public Object getAnalysisResult(@RequestHeader(value = "uid", required = false) String uid, @RequestParam Map<String, String> params) {
        String qs = params.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .reduce((a, b) -> a + "&" + b)
            .orElse("");
        String path = "/api/analysis/result" + (qs.isEmpty() ? "" : "?" + qs);
        try {
            return forwardGet(path, uid);
        } catch (Exception e) {
            return java.util.Map.of(
                "code", 200,
                "message", "success",
                "data", java.util.Collections.emptyList()
            );
        }
    }
    
    // 图像情绪分析
    @PostMapping("/emotion/analyze/image")
    public Object analyzeImageEmotion(@RequestHeader(value = "uid", required = false) String uid, @RequestParam("image") MultipartFile image) {
        return forwardMultipartWithField("/api/emotion/analyze/image", uid, image, "image");
    }
    
    // CV情绪分析（通过app-service转发）
    @PostMapping("/cv/emotion/analyze")
    public Object analyzeCvEmotion(@RequestHeader(value = "uid", required = false) String uid, @RequestParam("image") MultipartFile image) {
        return forwardMultipartWithField("/api/emotion/analyze/image", uid, image, "image");
    }
    
    // 音频情绪分析（通过app-service转发）
    @PostMapping("/audio/emotion/analyze")
    public Object analyzeAudioEmotion(@RequestHeader(value = "uid", required = false) String uid, @RequestParam("audio") MultipartFile audio) {
        return forwardMultipartWithField("/api/emotion/analyze/audio", uid, audio, "audio");
    }
    
    // 视频情绪分析（通过app-service转发）
    @PostMapping("/video/emotion/analyze")
    public Object analyzeVideoEmotion(@RequestHeader(value = "uid", required = false) String uid, @RequestParam("video") MultipartFile video) {
        return forwardMultipartWithField("/api/emotion/analyze/video", uid, video, "video");
    }
    
    // 日记相关
    @PostMapping("/diary/analyze-emotion")
    public Object analyzeDiaryEmotion(@RequestHeader(value = "uid", required = false) String uid, @RequestBody Map<String,Object> body) {
        return forwardPost("/api/diary/analyze-emotion", uid, body);
    }
    
    @PostMapping("/diary/save")
    public Object saveDiary(@RequestHeader(value = "uid", required = false) String uid, @RequestBody Map<String,Object> body) {
        return forwardPost("/api/diary/save", uid, body);
    }
    
    @GetMapping("/diary/detail/{id}")
    public Object getDiaryDetail(@PathVariable String id, @RequestHeader(value = "uid", required = false) String uid) {
        // content-service 实际路径是 GET /api/diary/{id}
        return forwardGet("/api/diary/" + id, uid);
    }
    
    // 胶囊详情
    @GetMapping("/capsules/detail/{id}")
    public Object getCapsuleDetail(@PathVariable String id, @RequestHeader(value = "uid", required = false) String uid) {
        return forwardGet("/api/capsules/detail/" + id, uid);
    }
    
    // 能量值系统
    @GetMapping("/energy/balance")
    public Object getEnergyBalance(@RequestHeader(value = "uid", required = false) String uid) {
        return forwardGet("/api/energy/balance", uid);
    }
    
    @PostMapping("/energy/add")
    public Object addEnergy(@RequestHeader(value = "uid", required = false) String uid, @RequestBody Map<String,Object> body) {
        return forwardPost("/api/energy/add", uid, body);
    }
    @PostMapping("/login/doLogin")
    public Object doLogin(@RequestBody Map<String,Object> body){
        return forwardPost("/login/doLogin", null, body);
    }
    @PostMapping("/login/doRegister")
    public Object register(@RequestBody Map<String,Object> body){ return forwardPost("/login/doRegister", null, body); }
    @PostMapping("/login/wxLogin")
    public Object wxLogin(@RequestBody(required = false) Map<String,Object> body, HttpServletRequest request) {
        System.out.println("========== app-service /app/login/wxLogin 接收请求 ==========");
        System.out.println("请求方法: " + request.getMethod());
        System.out.println("Content-Type: " + request.getContentType());
        System.out.println("Content-Length: " + request.getContentLength());
        System.out.println("请求URI: " + request.getRequestURI());
        
        // 如果 @RequestBody 为空，尝试手动读取
        if (body == null || body.isEmpty()) {
            System.out.println("@RequestBody 为空，尝试手动读取请求体");
            try {
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(request.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                String requestBodyStr = sb.toString();
                System.out.println("手动读取的请求体字符串: " + requestBodyStr);
                
                if (requestBodyStr != null && !requestBodyStr.isEmpty() && !requestBodyStr.equals("{}")) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    body = mapper.readValue(requestBodyStr, java.util.Map.class);
                    System.out.println("手动解析后的 body: " + body);
                }
            } catch (Exception e) {
                System.out.println("读取请求体失败: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("@RequestBody 解析成功: " + body);
        }
        
        System.out.println("app-service 最终 body: " + body);
        if (body != null) {
            System.out.println("app-service body 中的 code: " + body.get("code"));
        }
        
        try {
            Object result = forwardPost("/login/wxLogin", null, body);
            System.out.println("app-service 转发成功，返回结果: " + result);
            return result;
        } catch (Exception e) {
            System.out.println("app-service 转发失败: " + e.getMessage());
            e.printStackTrace();
            return Map.of("code", 500, "message", "转发失败: " + e.getMessage());
        }
    }

    // 每日登录能量值奖励
    @PostMapping("/login/daily-reward")
    public Object dailyLoginReward(@RequestHeader(value = "uid", required = false) String uid) {
        if (uid == null) {
            return Map.of("code", 401, "message", "用户未登录");
        }
        return forwardPost("/api/user/daily-login-reward", uid, Map.of("uid", uid));
    }

    // 获取用户能量值
    @GetMapping("/user/energy")
    public Object getUserEnergy(@RequestHeader(value = "uid", required = false) String uid) {
        if (uid == null) {
            return Map.of("code", 401, "message", "用户未登录");
        }
        return forwardGet("/users/getMyInfo/" + uid, uid);
    }


    
    @GetMapping("/users/info/{uid}")
    public Object getUserInfoByUid(@PathVariable String uid, @RequestHeader(value = "uid", required = false) String headerUid){
        if (headerUid != null && !headerUid.equals(uid)) {
            return Map.of("code", 403, "message", "权限不足");
        }
        return forwardGet("/users/info/" + uid, uid);
    }

    // 用户信息（小程序已在用）
    @PostMapping("/users/updateUserInfo")
    public Object updateUserInfo(@RequestHeader(value = "uid", required = false) String uid, @RequestBody Map<String,Object> body){
        return forwardPost("/users/updateUserInfo", uid, body);
    }
    
    @PostMapping("/users/changePassword/{uid}")
    public Object changePassword(@PathVariable("uid") Long uid, @RequestBody Map<String,Object> body){
        return forwardPost("/users/changePassword/" + uid, null, body);
    }
    
    // 账号删除
    @DeleteMapping("/users/deleteAccount/{uid}")
    public Object deleteAccount(@PathVariable("uid") String uid, @RequestHeader(value = "uid", required = false) String headerUid){
        if (headerUid != null && !headerUid.equals(uid)) {
            return Map.of("code", 403, "message", "权限不足");
        }
        return forwardDelete("/users/deleteAccount/" + uid, uid);
    }
    
    // 兼容小程序端的用户删除接口路径
    @DeleteMapping("/api/user/delete/{uid}")
    public Object deleteUser(@PathVariable("uid") String uid, @RequestHeader(value = "uid", required = false) String headerUid){
        if (headerUid != null && !headerUid.equals(uid)) {
            return Map.of("code", 403, "message", "权限不足");
        }
        // 转发到 user-service 的删除接口 /users/delete/{uid}
        return forwardDelete("/users/delete/" + uid, uid);
    }

    // 文件上传转发
    @PostMapping("/uploads/media")
    public Object uploadMedia(@RequestHeader(value = "uid", required = false) String uid,
                              @RequestPart("file") org.springframework.web.multipart.MultipartFile file) throws Exception {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
        if (uid != null) headers.set("uid", uid);
        org.springframework.util.LinkedMultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
        body.add("file", new org.springframework.core.io.ByteArrayResource(file.getBytes()){ @Override public String getFilename(){ return file.getOriginalFilename(); }});
        org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String,Object>> entity = new org.springframework.http.HttpEntity<>(body, headers);
        ResponseEntity<Object> resp = restTemplate.exchange(GATEWAY + "/uploads/media", org.springframework.http.HttpMethod.POST, entity, Object.class);
        return resp.getBody();
    }


    
    @GetMapping("/emotion/result/{id}")
    public Object emotionResult(@RequestHeader(value = "uid", required = false) String uid, @PathVariable String id){ 
        return forwardGet("/api/emotion/result/" + id, uid); 
    }
    
    // 日签相关
    @GetMapping("/quotes/daily")
    public Object getDailyQuote(@RequestHeader(value = "uid", required = false) String uid){
        return forwardGet("/api/quotes/random/user", uid);
    }
    
    @GetMapping("/quotes/my")
    public Object getMyQuotes(@RequestHeader(value = "uid", required = false) String uid){
        return forwardGet("/api/quotes/history", uid);
    }
    
    @PostMapping("/quotes/create")
    public Object createQuote(@RequestHeader(value = "uid", required = false) String uid, @RequestBody Map<String,Object> body){
        return forwardPost("/api/quotes/custom", uid, body);
    }
    

    
    // 情绪分析数据可视化
    @GetMapping("/emotion/visualization")
    public Object getEmotionVisualization(@RequestHeader(value = "uid", required = false) String uid, @RequestParam Map<String, String> params){
        String qs = params.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(java.util.stream.Collectors.joining("&"));
        String path = "/api/emotion/visualization" + (qs.isEmpty() ? "" : "?" + qs);
        return forwardGet(path, uid);
    }
    
    // 情绪统计饼图
    @GetMapping("/emotion/statistics")
    public Object getEmotionStatistics(@RequestHeader(value = "uid", required = false) String uid, @RequestParam Map<String, String> params){
        String qs = params.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(java.util.stream.Collectors.joining("&"));
        String path = "/api/emotion/statistics" + (qs.isEmpty() ? "" : "?" + qs);
        return forwardGet(path, uid);
    }
    
    // 导出情绪分析报告
    @PostMapping("/emotion/export")
    public Object exportEmotionReport(@RequestHeader(value = "uid", required = false) String uid, @RequestBody Map<String,Object> body){
        return forwardPost("/api/emotion/export", uid, body);
    }
    
    // 智能问答
    @PostMapping("/ai/chat")
    public Object aiChat(@RequestHeader(value = "uid", required = false) String uid, @RequestBody Map<String,Object> body){
        return forwardPost("/api/ai/chat", uid, body);
    }
    
    // 知识库相关接口
    @PostMapping("/knowledge/search")
    public Object searchKnowledge(@RequestHeader(value = "uid", required = false) String uid, @RequestBody Map<String,Object> body) {
        return forwardPost("/api/knowledge/search", uid, body);
    }
    
    // 知识库批量导入
    @PostMapping("/knowledge/import/batch")
    public Object batchImportKnowledge(@RequestHeader(value = "uid", required = false) String uid, @RequestBody Map<String,Object> body) {
        return forwardPost("/api/knowledge/import/batch", uid, body);
    }
    
    @PostMapping("/knowledge/import/from-text")
    public Object importKnowledgeFromText(@RequestHeader(value = "uid", required = false) String uid, @RequestBody Map<String,Object> body) {
        return forwardPost("/api/knowledge/import/from-text", uid, body);
    }
    
    @GetMapping("/knowledge/{id}")
    public Object getKnowledgeById(@PathVariable String id, @RequestHeader(value = "uid", required = false) String uid) {
        return forwardGet("/api/knowledge/" + id, uid);
    }
    
    @GetMapping("/knowledge/category/{category}")
    public Object getKnowledgeByCategory(@PathVariable String category, @RequestHeader(value = "uid", required = false) String uid) {
        return forwardGet("/api/knowledge/category/" + category, uid);
    }
    
    @GetMapping("/knowledge/category/{category}/subcategory/{subcategory}")
    public Object getKnowledgeBySubcategory(@PathVariable String category, 
                                            @PathVariable String subcategory,
                                            @RequestHeader(value = "uid", required = false) String uid) {
        return forwardGet("/api/knowledge/category/" + category + "/subcategory/" + subcategory, uid);
    }
    
    @GetMapping("/knowledge/list")
    public Object getKnowledgeList(@RequestHeader(value = "uid", required = false) String uid,
                                   HttpServletRequest request) {
        String qs = request.getQueryString();
        String path = "/api/knowledge/list" + (qs != null && !qs.isEmpty() ? ("?" + qs) : "");
        return forwardGet(path, uid);
    }
    
    // 匿名星球接口转发
    @GetMapping("/anonymous-posts")
    public Object listAnonymousPosts(@RequestHeader(value = "uid", required = false) String uid,
                                     HttpServletRequest request) {
        String qs = request.getQueryString();
        String path = "/api/anonymous-posts" + (qs != null && !qs.isEmpty() ? ("?" + qs) : "");
        return forwardGet(path, uid);
    }

    @PostMapping("/anonymous-posts")
    public Object createAnonymousPost(@RequestHeader(value = "uid", required = false) String uid,
                                      @RequestBody Map<String,Object> body) {
        return forwardPost("/api/anonymous-posts", uid, body);
    }

    @PostMapping("/anonymous-posts/{postId}/like")
    public Object likePost(@PathVariable String postId, @RequestHeader(value = "uid", required = false) String uid) {
        return forwardPost("/api/anonymous-posts/" + postId + "/like", uid, Map.of());
    }
    
    @PostMapping("/anonymous-posts/{postId}/comment")
    public Object commentPost(@PathVariable String postId, @RequestHeader(value = "uid", required = false) String uid, @RequestBody Map<String,Object> body) {
        // 小程序端使用 /comment，后端使用 /comments（复数），需要转换
        return forwardPost("/api/anonymous-posts/" + postId + "/comments", uid, body);
    }
    
    // 基于日记内容的智能回复
    @PostMapping("/ai/diary-analysis")
    public Object diaryAnalysis(@RequestHeader(value = "uid", required = false) String uid, @RequestBody Map<String,Object> body){
        return forwardPost("/api/ai/diary-analysis", uid, body);
    }
    
    // 获取常见问题列表
    @GetMapping("/ai/faq")
    public Object getFAQ(@RequestHeader(value = "uid", required = false) String uid){
        return forwardGet("/api/ai/faq", uid);
    }

    // 专门兜底：/app/api/mystery-box/**
    @RequestMapping(value = "/api/mystery-box/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public Object proxyMystery(@RequestHeader(value = "uid", required = false) String uid,
                               HttpServletRequest request,
                               @RequestBody(required = false) String rawBody) {
        return proxyByStripPrefix(uid, request, rawBody);
    }

    // 专门兜底：/app/api/diary/**
    @RequestMapping(value = "/api/diary/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public Object proxyDiary(@RequestHeader(value = "uid", required = false) String uid,
                             HttpServletRequest request,
                             @RequestBody(required = false) String rawBody) {
        return proxyByStripPrefix(uid, request, rawBody);
    }

    // 专门兜底：/app/api/quotes/**
    @RequestMapping(value = "/api/quotes/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public Object proxyQuotes(@RequestHeader(value = "uid", required = false) String uid,
                              HttpServletRequest request,
                              @RequestBody(required = false) String rawBody) {
        return proxyByStripPrefix(uid, request, rawBody);
    }

    // 创建日记（multipart 直传）
    @PostMapping(value = "/api/diary", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object createDiaryMultipart(@RequestHeader(value = "uid", required = false) String uid,
                                       HttpServletRequest request,
                                       @RequestPart(value = "mediaFiles", required = false) java.util.List<MultipartFile> mediaFiles,
                                       @RequestParam Map<String, String> params) {
        String url = GATEWAY + "/api/diary";
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        if (uid != null) headers.set("uid", uid);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        // 文本字段
        params.forEach(body::add);
        // 文件字段
        if (mediaFiles != null) {
            for (MultipartFile f : mediaFiles) {
                try {
                    byte[] bytes = f.getBytes();
                    String filename = f.getOriginalFilename();
                    body.add("mediaFiles", new ByteArrayResource(bytes){
                        @Override public String getFilename(){ return filename; }
                    });
                } catch (Exception ignored) {}
            }
        }
        org.springframework.http.HttpEntity<MultiValueMap<String, Object>> entity = new org.springframework.http.HttpEntity<>(body, headers);
        ResponseEntity<Object> resp = restTemplate.exchange(url, org.springframework.http.HttpMethod.POST, entity, Object.class);
        return resp.getBody();
    }

    // 通用转发：/app/api/** -> /api/**（保留方法、参数、uid 头）
    @RequestMapping(value = "/api/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public Object proxyApi(@RequestHeader(value = "uid", required = false) String uid,
                           HttpServletRequest request,
                           @RequestBody(required = false) String rawBody) {
        return proxyByStripPrefix(uid, request, rawBody);
    }

    private Object proxyByStripPrefix(String uid, HttpServletRequest request, String rawBody) {
        String originalUri = request.getRequestURI(); // e.g. /app/api/mystery-box/energy
        String targetPath = originalUri.substring("/app".length()); // -> /api/mystery-box/energy
        String url = GATEWAY + targetPath;
        String qs = request.getQueryString();
        if (qs != null && !qs.isEmpty()) {
            url = url + "?" + qs;
        }
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        java.util.Collections.list(request.getHeaderNames()).forEach(h -> {
            if (!"host".equalsIgnoreCase(h)) {
                headers.add(h, request.getHeader(h));
            }
        });
        if (uid != null) headers.set("uid", uid);
        org.springframework.http.HttpMethod method = org.springframework.http.HttpMethod.resolve(request.getMethod());
        org.springframework.http.HttpEntity<String> entity = (rawBody != null)
                ? new org.springframework.http.HttpEntity<>(rawBody, headers)
                : new org.springframework.http.HttpEntity<>(headers);
        ResponseEntity<Object> resp = restTemplate.exchange(url, method, entity, Object.class);
        return resp.getBody();
    }

    private Object forwardGet(String path, String uid) {
        HttpHeaders headers = new HttpHeaders();
        if (uid != null) headers.set("uid", uid);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        // 分离路径和查询参数
        String basePath = path;
        String queryString = null;
        int queryIndex = path.indexOf('?');
        if (queryIndex >= 0) {
            basePath = path.substring(0, queryIndex);
            queryString = path.substring(queryIndex + 1);
        }
        
        // 根据路径决定转发目标，与 forwardPost 逻辑保持一致
        String targetUrl;
        if (basePath.startsWith("/users/")) {
            // 用户服务：直接转发到 user-service
            targetUrl = USER_SERVICE + basePath;
        } else if (basePath.startsWith("/api/emotion/") || basePath.startsWith("/api/analysis/")) {
            // 情绪服务：通过网关转发到 content-service
            targetUrl = GATEWAY + basePath;
        } else if (basePath.startsWith("/api/diary") || basePath.equals("/api/diary")) {
            // 日记服务：通过网关转发到 content-service (8011)
            targetUrl = GATEWAY + basePath;
        } else if (basePath.startsWith("/api/knowledge/") || basePath.startsWith("/api/ai/")) {
            // AI和知识库服务：通过网关转发到 ai-knowledge-service (8013)
            targetUrl = GATEWAY + basePath;
        } else {
            // 其他请求通过网关
            targetUrl = GATEWAY + basePath;
        }
        
        // 添加查询参数
        if (queryString != null && !queryString.isEmpty()) {
            targetUrl = targetUrl + "?" + queryString;
        }
        
        try {
            ResponseEntity<Object> resp = restTemplate.exchange(targetUrl, HttpMethod.GET, entity, Object.class);
            return resp.getBody();
        } catch (org.springframework.web.client.RestClientResponseException ex) {
            // 将下游返回体透传为JSON，避免500
            try {
                String responseBody = ex.getResponseBodyAsString();
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                return mapper.readValue(responseBody, Object.class);
            } catch (Exception e) {
                return java.util.Map.of("code", ex.getRawStatusCode(), "message", ex.getMessage());
            }
        } catch (Exception e) {
            System.out.println("forwardGet 转发失败: " + e.getMessage());
            System.out.println("forwardGet 目标URL: " + targetUrl);
            e.printStackTrace();
            return java.util.Map.of("code", 500, "message", "转发请求失败: " + e.getMessage());
        }
    }
    
    private Object forwardDelete(String path, String uid) {
        HttpHeaders headers = new HttpHeaders();
        if (uid != null) headers.set("uid", uid);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        // 根据路径决定转发目标，与 forwardPost 和 forwardGet 逻辑保持一致
        String targetUrl;
        if (path.startsWith("/users/")) {
            // 用户服务：直接转发到 user-service
            targetUrl = USER_SERVICE + path;
        } else if (path.startsWith("/api/emotion/") || path.startsWith("/api/analysis/")) {
            // 情绪服务：通过网关转发到 content-service
            targetUrl = GATEWAY + path;
        } else if (path.startsWith("/api/diary") || path.equals("/api/diary")) {
            // 日记服务：通过网关转发到 content-service (8011)
            targetUrl = GATEWAY + path;
        } else if (path.startsWith("/api/knowledge/") || path.startsWith("/api/ai/")) {
            // AI和知识库服务：通过网关转发到 ai-knowledge-service (8013)
            targetUrl = GATEWAY + path;
        } else {
            // 其他请求通过网关
            targetUrl = GATEWAY + path;
        }
        
        try {
            ResponseEntity<Object> resp = restTemplate.exchange(targetUrl, HttpMethod.DELETE, entity, Object.class);
            return resp.getBody();
        } catch (org.springframework.web.client.RestClientResponseException ex) {
            // 将下游返回体透传为JSON，避免500
            try {
                String responseBody = ex.getResponseBodyAsString();
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                return mapper.readValue(responseBody, Object.class);
            } catch (Exception e) {
                return java.util.Map.of("code", ex.getRawStatusCode(), "message", ex.getMessage());
            }
        } catch (Exception e) {
            System.out.println("forwardDelete 转发失败: " + e.getMessage());
            System.out.println("forwardDelete 目标URL: " + targetUrl);
            e.printStackTrace();
            return java.util.Map.of("code", 500, "message", "转发请求失败: " + e.getMessage());
        }
    }

    private Object forwardPost(String path, String uid, Object body){
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Content-Type", "application/json");
        if (uid != null) headers.set("uid", uid);
        
        // 根据路径决定转发目标
        String targetUrl;
        if (path.startsWith("/users/")) {
            // 用户服务
            targetUrl = USER_SERVICE + path;
        } else if (path.startsWith("/login/")) {
            // 登录相关接口：直接转发到 user-service，避免网关转发时 body 丢失
            targetUrl = USER_SERVICE + path;
        } else if (path.startsWith("/api/emotion/") || path.startsWith("/api/analysis/")) {
            // 情绪服务：通过网关转发到 content-service
            targetUrl = GATEWAY + path;
        } else if (path.startsWith("/api/diary/") || path.equals("/api/diary")) {
            // 日记服务：通过网关转发到 content-service (8011)
            targetUrl = GATEWAY + path;
        } else if (path.startsWith("/api/knowledge/") || path.startsWith("/api/ai/")) {
            // AI和知识库服务：通过网关转发到 ai-knowledge-service (8013)
            targetUrl = GATEWAY + path;
        } else {
            // 其他请求通过网关
            targetUrl = GATEWAY + path;
        }
        
        // 添加调试日志
        System.out.println("forwardPost 转发路径: " + targetUrl);
        System.out.println("forwardPost 转发 body: " + body);
        if (body instanceof Map) {
            Map<String, Object> bodyMap = (Map<String, Object>) body;
            System.out.println("forwardPost body 中的 code: " + bodyMap.get("code"));
            System.out.println("forwardPost body 中的 diaryId: " + bodyMap.get("diaryId"));
            System.out.println("forwardPost body 中的 type: " + bodyMap.get("type"));
        }
        
        // 确保 body 不为空
        if (body == null) {
            body = new java.util.HashMap<>();
            System.out.println("警告：forwardPost body 为空，使用空 Map");
        }
        
        org.springframework.http.HttpEntity<Object> entity = new org.springframework.http.HttpEntity<>(body, headers);
        try {
            ResponseEntity<Object> resp = restTemplate.exchange(targetUrl, org.springframework.http.HttpMethod.POST, entity, Object.class);
            return resp.getBody();
        } catch (org.springframework.web.client.RestClientResponseException ex) {
            // 将下游返回体透传为JSON，避免500
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                return mapper.readValue(ex.getResponseBodyAsByteArray(), java.util.Map.class);
            } catch (Exception ignore) {
                return Map.of("code", ex.getRawStatusCode(), "message", "上游错误: " + ex.getStatusText());
            }
        } catch (Exception e) {
            System.out.println("forwardPost 转发失败: " + e.getMessage());
            e.printStackTrace();
            return Map.of("code", 500, "message", "转发失败: " + e.getMessage());
        }
    }
    
    private Object forwardMultipart(String path, String uid, MultipartFile file){
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        if (uid != null) headers.set("uid", uid);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        try {
            byte[] bytes = file.getBytes();
            String filename = file.getOriginalFilename();
            body.add("image", new ByteArrayResource(bytes){
                @Override public String getFilename(){ return filename; }
            });
        } catch (Exception e) {
            return Map.of("code", 500, "message", "文件处理失败: " + e.getMessage());
        }
        
        org.springframework.http.HttpEntity<MultiValueMap<String, Object>> entity = new org.springframework.http.HttpEntity<>(body, headers);
        ResponseEntity<Object> resp = restTemplate.exchange(GATEWAY + path, org.springframework.http.HttpMethod.POST, entity, Object.class);
        return resp.getBody();
    }

    private Object forwardMultipartWithField(String path, String uid, MultipartFile file, String fieldName){
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        if (uid != null) headers.set("uid", uid);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        try {
            byte[] bytes = file.getBytes();
            String filename = file.getOriginalFilename();
            body.add(fieldName, new ByteArrayResource(bytes){
                @Override public String getFilename(){ return filename; }
            });
        } catch (Exception e) {
            return Map.of("code", 500, "message", "文件处理失败: " + e.getMessage());
        }
        try {
            org.springframework.http.HttpEntity<MultiValueMap<String, Object>> entity = new org.springframework.http.HttpEntity<>(body, headers);
            // 以字符串接收，手动解析，避免编码/转换异常
            ResponseEntity<String> resp = restTemplate.exchange(GATEWAY + path, org.springframework.http.HttpMethod.POST, entity, String.class);
            String raw = resp.getBody();
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                return mapper.readValue(raw, java.util.Map.class);
            } catch (Exception parseEx) {
                return java.util.Map.of("code", 200, "message", "success", "raw", raw);
            }
        } catch (org.springframework.web.client.RestClientResponseException ex) {
            // 将下游返回体透传为JSON，避免500
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                return mapper.readValue(ex.getResponseBodyAsByteArray(), java.util.Map.class);
            } catch (Exception ignore) {
                return Map.of("code", 500, "message", "上游错误: " + ex.getRawStatusCode());
            }
        } catch (Exception e) {
            return Map.of("code", 500, "message", "转发失败: " + e.getMessage());
        }
    }
}


