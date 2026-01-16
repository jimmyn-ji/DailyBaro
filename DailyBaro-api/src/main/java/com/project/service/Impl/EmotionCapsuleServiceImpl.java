package com.project.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.project.mapper.CapsuleMediaMapper;
import com.project.mapper.EmotionCapsuleMapper;
import com.project.model.CapsuleMedia;
import com.project.model.EmotionCapsule;
import com.project.model.dto.CreateCapsuleDTO;
import com.project.model.vo.EmotionCapsuleVO;
import com.project.model.vo.MediaVO;
import com.project.service.EmotionCapsuleService;
import com.project.service.FileStorageService;
import com.project.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmotionCapsuleServiceImpl implements EmotionCapsuleService {

    @Autowired
    private EmotionCapsuleMapper capsuleMapper;

    @Autowired
    private CapsuleMediaMapper mediaMapper;

    @Autowired
    private FileStorageService fileStorageService;
    
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    @Transactional
    public Result<EmotionCapsuleVO> createCapsule(CreateCapsuleDTO createDTO, Long userId) {
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

        // Handle media files
        if (!CollectionUtils.isEmpty(createDTO.getMediaFiles())) {
            for (MultipartFile file : createDTO.getMediaFiles()) {
                Result<String> uploadResult = fileStorageService.storeFile(file);
                if (uploadResult.getCode() == 200) {
                    CapsuleMedia media = new CapsuleMedia();
                    media.setCapsuleId(capsule.getCapsuleId());
                    media.setMediaUrl(uploadResult.getData());
                    media.setMediaType(determineMediaType(file.getContentType(), file.getOriginalFilename()));
                    mediaMapper.insert(media);
                } else {
                    log.error("Failed to upload file for capsule {}: {}", capsule.getCapsuleId(), uploadResult.getMessage());
                }
            }
        }
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
            // 删除物理文件
            try {
                String filePath = uploadDir + media.getMediaUrl().replace("/uploads/", "");
                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                log.warn("删除文件失败: " + media.getMediaUrl(), e);
            }
            // 删除数据库记录
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

    private String determineMediaType(String contentType, String fileName) {
        // 首先尝试基于Content-Type判断
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return "image";
            } else if (contentType.startsWith("audio/")) {
                return "audio";
            } else if (contentType.startsWith("video/")) {
                return "video";
            }
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
        
        return "other";
    }
} 