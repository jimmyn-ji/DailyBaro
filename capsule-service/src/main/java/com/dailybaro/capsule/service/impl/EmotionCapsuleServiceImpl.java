package com.dailybaro.capsule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dailybaro.capsule.mapper.CapsuleMediaMapper;
import com.dailybaro.capsule.mapper.EmotionCapsuleMapper;
import com.dailybaro.capsule.model.CapsuleMedia;
import com.dailybaro.capsule.model.EmotionCapsule;
import com.dailybaro.capsule.model.dto.CreateCapsuleDTO;
import com.dailybaro.capsule.model.vo.EmotionCapsuleVO;
import com.dailybaro.capsule.model.vo.MediaVO;
import com.dailybaro.capsule.service.EmotionCapsuleService;
import com.dailybaro.common.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.io.IOException;

@Slf4j
@Service
public class EmotionCapsuleServiceImpl implements EmotionCapsuleService {

    @Autowired
    private EmotionCapsuleMapper capsuleMapper;

    @Autowired
    private CapsuleMediaMapper mediaMapper;

    // 文件服务地址（通过网关）
    private static final String FILE_SERVICE_UPLOAD_URL = "http://localhost:8000/uploads/media";

    @Override
    @Transactional
    public Result<EmotionCapsuleVO> createCapsule(CreateCapsuleDTO createDTO, Long userId) {
        // 添加详细的调试日志
        log.info("=== 开始创建情绪胶囊 ===");
        log.info("用户ID: {}", userId);
        log.info("当前情绪: {}", createDTO.getCurrentEmotion());
        log.info("想法: {}", createDTO.getThoughts());
        log.info("未来目标: {}", createDTO.getFutureGoal());
        log.info("开启时间: {}", createDTO.getOpenTime());
        log.info("提醒类型: {}", createDTO.getReminderType());
        
        // 详细记录媒体文件信息
        if (createDTO.getMediaFiles() != null) {
            log.info("媒体文件数量: {}", createDTO.getMediaFiles().size());
            for (int i = 0; i < createDTO.getMediaFiles().size(); i++) {
                MultipartFile file = createDTO.getMediaFiles().get(i);
                log.info("文件 {}: 名称={}, 大小={} bytes, 类型={}", 
                        i + 1, file.getOriginalFilename(), file.getSize(), file.getContentType());
            }
        } else {
            log.warn("MediaFiles为null");
        }
        
        EmotionCapsule capsule = new EmotionCapsule();
        BeanUtils.copyProperties(createDTO, capsule);
        capsule.setUserId(userId);
        capsule.setCreateTime(new Date());
        
        // 设置新字段
        capsule.setCurrentEmotion(createDTO.getCurrentEmotion());
        capsule.setThoughts(createDTO.getThoughts());
        capsule.setFutureGoal(createDTO.getFutureGoal());
        
        // 如果没有设置content，则使用thoughts作为content
        if (createDTO.getContent() == null || createDTO.getContent().trim().isEmpty()) {
            capsule.setContent(createDTO.getThoughts());
        }
        
        capsule.setReminderType(createDTO.getReminderType()); // 保存提醒方式
        capsule.setReminderSent(0);
        capsule.setReminderRead(0);

        capsuleMapper.insert(capsule);
        log.info("胶囊创建成功，ID: {}", capsule.getCapsuleId());

        // 处理媒体文件上传 - 改进异常处理
        List<String> failedFiles = new ArrayList<>();
        List<String> successFiles = new ArrayList<>();
        
        if (!CollectionUtils.isEmpty(createDTO.getMediaFiles())) {
            log.info("开始处理 {} 个媒体文件", createDTO.getMediaFiles().size());
            
            for (MultipartFile file : createDTO.getMediaFiles()) {
                try {
                    log.info("正在上传文件: {}, 大小: {} bytes, 类型: {}", 
                            file.getOriginalFilename(), file.getSize(), file.getContentType());
                    
                    String fileUrl = uploadToFileService(file);
                    if (fileUrl == null || fileUrl.isEmpty()) {
                        log.warn("文件服务未返回有效URL，跳过该文件: {}", file.getOriginalFilename());
                        failedFiles.add(file.getOriginalFilename());
                        continue;
                    }
                    
                    log.info("文件上传成功，URL: {}", fileUrl);
                    
                    CapsuleMedia media = new CapsuleMedia();
                    media.setCapsuleId(capsule.getCapsuleId());
                    media.setMediaUrl(fileUrl);
                    media.setMediaType(resolveMediaType(file));
                    
                    int insertResult = mediaMapper.insert(media);
                    if (insertResult > 0) {
                        log.info("媒体记录保存成功，mediaId: {}", media.getMediaId());
                        successFiles.add(file.getOriginalFilename());
                    } else {
                        log.error("媒体记录保存失败: {}", file.getOriginalFilename());
                        failedFiles.add(file.getOriginalFilename());
                    }
                } catch (Exception ex) {
                    log.error("上传媒体到文件服务失败: {}", file.getOriginalFilename(), ex);
                    failedFiles.add(file.getOriginalFilename());
                    // 不抛出异常，继续处理其他文件
                }
            }
            
            // 记录处理结果
            if (!successFiles.isEmpty()) {
                log.info("成功上传 {} 个文件: {}", successFiles.size(), String.join(", ", successFiles));
            }
            if (!failedFiles.isEmpty()) {
                log.warn("上传失败 {} 个文件: {}", failedFiles.size(), String.join(", ", failedFiles));
            }
        } else {
            log.info("没有媒体文件需要处理");
        }
        
        log.info("=== 情绪胶囊创建完成 ===");
        return getCapsuleById(capsule.getCapsuleId(), userId);
    }

    @Override
    public Result<EmotionCapsuleVO> getCapsuleById(Long capsuleId, Long userId) {
        EmotionCapsule capsule = capsuleMapper.selectById(capsuleId);
        if (capsule == null || !capsule.getUserId().equals(userId)) {
            return Result.fail("情绪胶囊不存在或无权访问");
        }
        return Result.success(convertToVO(capsule));
    }

    @Override
    public Result<List<EmotionCapsuleVO>> listUserCapsules(Long userId) {
        QueryWrapper<EmotionCapsule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).orderByDesc("create_time");
        List<EmotionCapsule> capsules = capsuleMapper.selectList(queryWrapper);

        List<EmotionCapsuleVO> vos = capsules.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(vos);
    }

    @Override
    public Result<List<EmotionCapsuleVO>> getUnreadReminders(Long userId) {
        Date now = new Date();
        QueryWrapper<EmotionCapsule> query = new QueryWrapper<>();
        query.eq("user_id", userId)
             .le("open_time", now)
             .eq("reminder_sent", 0);
        List<EmotionCapsule> dueCapsules = capsuleMapper.selectList(query);
        // 立即将这些胶囊的reminder_sent设为1
        for (EmotionCapsule capsule : dueCapsules) {
            capsule.setReminderSent(1);
            capsuleMapper.updateById(capsule);
        }
        List<EmotionCapsuleVO> voList = dueCapsules.stream().map(this::convertToVO).collect(Collectors.toList());
        return Result.success(voList);
    }

    @Override
    public Result<?> markReminderRead(Long capsuleId, Long userId) {
        EmotionCapsule capsule = capsuleMapper.selectById(capsuleId);
        if (capsule == null || !capsule.getUserId().equals(userId)) {
            return Result.fail("胶囊不存在或无权限");
        }
        
        capsule.setReminderRead(1);
        capsuleMapper.updateById(capsule);
        return Result.success("标记成功");
    }
    
    @Override
    public Result<?> deleteCapsule(Long capsuleId, Long userId) {
        EmotionCapsule capsule = capsuleMapper.selectById(capsuleId);
        if (capsule == null || !capsule.getUserId().equals(userId)) {
            return Result.fail("胶囊不存在或无权限");
        }
        
        // 删除相关的媒体文件
        QueryWrapper<CapsuleMedia> mediaQuery = new QueryWrapper<>();
        mediaQuery.eq("capsule_id", capsuleId);
        List<CapsuleMedia> mediaList = mediaMapper.selectList(mediaQuery);
        
        for (CapsuleMedia media : mediaList) {
            mediaMapper.deleteById(media.getMediaId());
        }
        
        // 删除胶囊
        capsuleMapper.deleteById(capsuleId);
        return Result.success("删除成功");
    }

    private EmotionCapsuleVO convertToVO(EmotionCapsule capsule) {
        EmotionCapsuleVO vo = new EmotionCapsuleVO();
        BeanUtils.copyProperties(capsule, vo);

        boolean isOpened = new Date().after(capsule.getOpenTime());
        vo.setOpened(isOpened);

        // 无论是否开启，都设置基本信息
        vo.setContent(capsule.getContent());
        vo.setCurrentEmotion(capsule.getCurrentEmotion());
        vo.setThoughts(capsule.getThoughts());
        vo.setFutureGoal(capsule.getFutureGoal());
        
        // 查询媒体文件
        QueryWrapper<CapsuleMedia> mediaQuery = new QueryWrapper<>();
        mediaQuery.eq("capsule_id", capsule.getCapsuleId());
        List<CapsuleMedia> mediaList = mediaMapper.selectList(mediaQuery);
        vo.setMedia(mediaList.stream().map(media -> {
            MediaVO mediaVO = new MediaVO();
            BeanUtils.copyProperties(media, mediaVO);
            return mediaVO;
        }).collect(Collectors.toList()));

        return vo;
    }

    private String resolveMediaType(MultipartFile file) {
        try {
            String contentType = file.getContentType();
            String fileName = file.getOriginalFilename();
            
            // 首先尝试基于Content-Type判断
            if (contentType != null) {
                if (contentType.startsWith("image/")) return "image";
                if (contentType.startsWith("video/")) return "video";
                if (contentType.startsWith("audio/")) return "audio";
            }
            
            // 如果Content-Type判断失败，基于文件扩展名判断
            if (fileName != null) {
                String lowerFileName = fileName.toLowerCase();
                
                // 图片类型
                if (lowerFileName.matches(".*\\.(jpg|jpeg|png|gif|webp|bmp)$")) {
                    return "image";
                }
                
                // 音频类型
                if (lowerFileName.matches(".*\\.(mp3|wav|ogg|aac|m4a|ncm)$")) {
                    return "audio";
                }
                
                // 视频类型
                if (lowerFileName.matches(".*\\.(mp4|avi|mov|wmv|flv|mkv)$")) {
                    return "video";
                }
            }
        } catch (Exception e) {
            log.warn("解析媒体类型失败: {}", e.getMessage());
        }
        return "other";
    }

    private String uploadToFileService(MultipartFile file) {
        log.info("开始调用文件服务上传文件: {}", file.getOriginalFilename());
        
        // 先获取文件字节，处理可能的IOException
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            log.error("读取文件字节失败: {}", file.getOriginalFilename(), e);
            return null;
        }
        
        RestTemplate restTemplate = new RestTemplate();
        
        // 设置超时时间
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10秒连接超时
        factory.setReadTimeout(30000);    // 30秒读取超时
        restTemplate.setRequestFactory(factory);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        Resource fileResource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            log.info("发送请求到文件服务: {}", FILE_SERVICE_UPLOAD_URL);
            
            ResponseEntity<String> response = restTemplate.postForEntity(FILE_SERVICE_UPLOAD_URL, requestEntity, String.class);
            
            log.info("文件服务响应状态: {}, 响应体: {}", response.getStatusCode(), response.getBody());
            
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("文件服务返回失败状态: {}", response.getStatusCode());
                return null;
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            
            if (root.has("code") && root.get("code").asInt() == 200 && root.has("data")) {
                String fileUrl = root.get("data").asText();
                log.info("文件上传成功，返回URL: {}", fileUrl);
                return fileUrl;
            } else {
                log.warn("文件服务返回异常: {}", response.getBody());
                return null;
            }
        } catch (Exception e) {
            log.error("调用文件服务失败: {}", e.getMessage(), e);
            
            // 如果是连接超时，尝试重试一次
            if (e.getMessage().contains("timeout") || e.getMessage().contains("Connection refused")) {
                log.info("检测到超时或连接问题，尝试重试...");
                try {
                    Thread.sleep(2000); // 等待2秒后重试
                    ResponseEntity<String> retryResponse = restTemplate.postForEntity(FILE_SERVICE_UPLOAD_URL, requestEntity, String.class);
                    
                    if (retryResponse.getStatusCode().is2xxSuccessful() && retryResponse.getBody() != null) {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(retryResponse.getBody());
                        
                        if (root.has("code") && root.get("code").asInt() == 200 && root.has("data")) {
                            String fileUrl = root.get("data").asText();
                            log.info("重试成功，文件上传成功，返回URL: {}", fileUrl);
                            return fileUrl;
                        }
                    }
                } catch (Exception retryEx) {
                    log.error("重试失败: {}", retryEx.getMessage(), retryEx);
                }
            }
            
            // 不再抛出异常，返回null表示失败
            return null;
        }
    }
} 