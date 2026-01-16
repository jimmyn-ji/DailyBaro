package com.dailybaro.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dailybaro.content.mapper.CapsuleMediaMapper;
import com.dailybaro.content.mapper.EmotionCapsuleMapper;
import com.dailybaro.content.model.CapsuleMedia;
import com.dailybaro.content.model.EmotionCapsule;
import com.dailybaro.content.model.dto.CreateCapsuleDTO;
import com.dailybaro.content.model.vo.EmotionCapsuleVO;
import com.dailybaro.content.model.vo.MediaVO;
import com.dailybaro.content.service.EmotionCapsuleService;
import com.dailybaro.common.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import java.util.Map;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.io.IOException;
import com.dailybaro.content.service.UserClient;
import java.io.File;
import java.nio.file.Files;
import org.springframework.mock.web.MockMultipartFile;
import com.dailybaro.content.util.MggDecoder;

@Slf4j
@Service
public class EmotionCapsuleServiceImpl implements EmotionCapsuleService {

    @Autowired
    private EmotionCapsuleMapper capsuleMapper;

    @Autowired
    private CapsuleMediaMapper mediaMapper;

    @Autowired
    private UserClient userClient;

    // File service address (via gateway)
    @Value("${file.service.url:http://localhost:8000/uploads/media}")
    private String fileServiceUploadUrl;

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
        // 新增：赠送能量值
        try {
            userClient.increaseEnergy(userId, 3);
            log.info("赠送用户{}能量+3成功", userId);
        } catch (Exception e) {
            log.warn("赠送能量失败: {}", e.getMessage());
        }

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
        try {
            log.info("获取未读提醒，用户ID: {}", userId);
            
            if (userId == null) {
                log.warn("用户ID为空");
                return Result.fail("用户ID不能为空");
            }
            
            Date now = new Date();
            QueryWrapper<EmotionCapsule> query = new QueryWrapper<>();
            query.eq("user_id", userId)
                 .le("open_time", now)
                 .eq("reminder_sent", 0);
            
            List<EmotionCapsule> dueCapsules = capsuleMapper.selectList(query);
            log.info("找到 {} 个未读提醒的胶囊", dueCapsules.size());
            
            // 立即将这些胶囊的reminder_sent设为1
            for (EmotionCapsule capsule : dueCapsules) {
                try {
                    capsule.setReminderSent(1);
                    capsuleMapper.updateById(capsule);
                    log.debug("已标记胶囊 {} 为已发送提醒", capsule.getCapsuleId());
                } catch (Exception e) {
                    log.error("更新胶囊提醒状态失败，capsuleId: {}", capsule.getCapsuleId(), e);
                    // 继续处理其他胶囊，不中断流程
                }
            }
            
            // 转换为VO，添加异常处理
            List<EmotionCapsuleVO> voList = new ArrayList<>();
            for (EmotionCapsule capsule : dueCapsules) {
                try {
                    EmotionCapsuleVO vo = convertToVO(capsule);
                    voList.add(vo);
                } catch (Exception e) {
                    log.error("转换胶囊VO失败，capsuleId: {}", capsule.getCapsuleId(), e);
                    // 跳过有问题的胶囊，继续处理其他
                }
            }
            
            log.info("成功返回 {} 个未读提醒", voList.size());
            return Result.success(voList);
        } catch (Exception e) {
            log.error("获取未读提醒失败，userId: {}", userId, e);
            return Result.fail("获取未读提醒失败: " + e.getMessage());
        }
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
        if (capsule == null) {
            log.warn("convertToVO: capsule为null");
            return null;
        }
        
        try {
            EmotionCapsuleVO vo = new EmotionCapsuleVO();
            BeanUtils.copyProperties(capsule, vo);

            // 安全地检查开启时间
            boolean isOpened = false;
            if (capsule.getOpenTime() != null) {
                isOpened = new Date().after(capsule.getOpenTime());
            }
            vo.setOpened(isOpened);

            // 无论是否开启，都设置基本信息（安全地处理null值）
            vo.setContent(capsule.getContent() != null ? capsule.getContent() : "");
            vo.setCurrentEmotion(capsule.getCurrentEmotion() != null ? capsule.getCurrentEmotion() : "");
            vo.setThoughts(capsule.getThoughts() != null ? capsule.getThoughts() : "");
            vo.setFutureGoal(capsule.getFutureGoal() != null ? capsule.getFutureGoal() : "");
            
            // 查询媒体文件（安全处理）
            List<MediaVO> mediaList = new ArrayList<>();
            if (capsule.getCapsuleId() != null) {
                try {
                    QueryWrapper<CapsuleMedia> mediaQuery = new QueryWrapper<>();
                    mediaQuery.eq("capsule_id", capsule.getCapsuleId());
                    List<CapsuleMedia> mediaEntities = mediaMapper.selectList(mediaQuery);
                    if (mediaEntities != null) {
                        mediaList = mediaEntities.stream()
                            .filter(media -> media != null)
                            .map(media -> {
                                try {
                                    MediaVO mediaVO = new MediaVO();
                                    BeanUtils.copyProperties(media, mediaVO);
                                    return mediaVO;
                                } catch (Exception e) {
                                    log.warn("转换媒体VO失败: {}", e.getMessage());
                                    return null;
                                }
                            })
                            .filter(mediaVO -> mediaVO != null)
                            .collect(Collectors.toList());
                    }
                } catch (Exception e) {
                    log.error("查询媒体文件失败，capsuleId: {}", capsule.getCapsuleId(), e);
                }
            }
            vo.setMedia(mediaList);

            return vo;
        } catch (Exception e) {
            log.error("convertToVO失败，capsuleId: {}", capsule.getCapsuleId(), e);
            throw e; // 重新抛出异常，让调用者处理
        }
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
                if (lowerFileName.matches(".*\\.(mp3|wav|ogg|aac|m4a|ncm|mgg)$")) {
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
        try {
            byte[] fileBytes = file.getBytes();
            String fileName = file.getOriginalFilename();
            String realFileName = fileName;
            if (fileName != null && fileName.toLowerCase().endsWith(".mgg")) {
                try {
                    File tempMgg = File.createTempFile("capsule_", ".mgg");
                    Files.write(tempMgg.toPath(), fileBytes);
                    byte[] mp3Bytes = MggDecoder.decodeToMp3(tempMgg);
                    tempMgg.delete();
                    File tempMp3 = File.createTempFile("capsule_", ".mp3");
                    Files.write(tempMp3.toPath(), mp3Bytes);
                    fileBytes = mp3Bytes;
                    realFileName = fileName.replaceAll("\\.mgg$", ".mp3");
                    file = new MockMultipartFile(realFileName, realFileName, "audio/mp3", mp3Bytes);
                    tempMp3.delete();
                } catch (Exception e) {
                    log.error("mgg转码mp3失败: {}", e.getMessage(), e);
                }
            } else if (fileName != null && fileName.toLowerCase().endsWith(".ogg")) {
                try {
                    File tempOgg = File.createTempFile("capsule_", ".ogg");
                    Files.write(tempOgg.toPath(), fileBytes);
                    byte[] mp3Bytes = MggDecoder.decodeOggToMp3(tempOgg);
                    tempOgg.delete();
                    File tempMp3 = File.createTempFile("capsule_", ".mp3");
                    Files.write(tempMp3.toPath(), mp3Bytes);
                    fileBytes = mp3Bytes;
                    realFileName = fileName.replaceAll("\\.ogg$", ".mp3");
                    file = new MockMultipartFile(realFileName, realFileName, "audio/mp3", mp3Bytes);
                    tempMp3.delete();
                    log.info("OGG已转码为MP3: {} -> {}", fileName, realFileName);
                } catch (Exception e) {
                    log.error("ogg转码mp3失败: {}", e.getMessage(), e);
                }
            }
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            final String finalFileName = realFileName;
            body.add("file", new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return finalFileName;
                }
            });
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            log.info("发送请求到文件服务: {}", fileServiceUploadUrl);
            try {
                ResponseEntity<Object> response = restTemplate.postForEntity(fileServiceUploadUrl, requestEntity, Object.class);
                log.info("文件服务响应状态: {}", response.getStatusCode());
                log.info("文件服务响应内容: {}", response.getBody());
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() instanceof Map) {
                    Map<?, ?> responseBody = (Map<?, ?>) response.getBody();
                    Object dataObj = responseBody.get("data");
                    if (dataObj instanceof String) {
                        String fileUrl = (String) dataObj;
                        log.info("文件上传成功(data)：{}", fileUrl);
                        return fileUrl;
                    }
                    Object urlObj = responseBody.get("url");
                    if (urlObj instanceof String) {
                        String fileUrl = (String) urlObj;
                        log.info("文件上传成功(url)：{}", fileUrl);
                        return fileUrl;
                    }
                }
                log.warn("文件服务返回格式异常或不包含URL字段");
                return null;
            } catch (Exception e) {
                log.error("调用文件服务失败: {}", e.getMessage(), e);
                log.info("尝试重试上传文件...");
                try {
                    Thread.sleep(1000);
                    ResponseEntity<Object> retryResponse = restTemplate.postForEntity(fileServiceUploadUrl, requestEntity, Object.class);
                    if (retryResponse.getStatusCode() == HttpStatus.OK && retryResponse.getBody() instanceof Map) {
                        Map<?, ?> responseBody = (Map<?, ?>) retryResponse.getBody();
                        Object dataObj = responseBody.get("data");
                        if (dataObj instanceof String) {
                            String fileUrl = (String) dataObj;
                            log.info("重试后文件上传成功(data)，URL: {}", fileUrl);
                            return fileUrl;
                        }
                        Object urlObj = responseBody.get("url");
                        if (urlObj instanceof String) {
                            String fileUrl = (String) urlObj;
                            log.info("重试后文件上传成功(url)，URL: {}", fileUrl);
                            return fileUrl;
                        }
                    }
                } catch (Exception retryException) {
                    log.error("重试上传也失败: {}", retryException.getMessage());
                }
                return null;
            }
        } catch (IOException e) {
            log.error("读取文件字节失败: {}", e.getMessage(), e);
            return null;
        }
    }
} 