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

        // Handle media files (simplified version)
        if (!CollectionUtils.isEmpty(createDTO.getMediaFiles())) {
            for (MultipartFile file : createDTO.getMediaFiles()) {
                // 简化处理，只保存文件信息
                CapsuleMedia media = new CapsuleMedia();
                media.setCapsuleId(capsule.getCapsuleId());
                media.setMediaUrl("/uploads/" + file.getOriginalFilename());
                media.setMediaType("file");
                mediaMapper.insert(media);
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
} 