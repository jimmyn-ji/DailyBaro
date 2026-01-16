package com.project.service;

import com.project.model.dto.CreateCapsuleDTO;
import com.project.model.vo.EmotionCapsuleVO;
import com.project.util.Result;

import java.util.List;

public interface EmotionCapsuleService {

    Result<EmotionCapsuleVO> createCapsule(CreateCapsuleDTO createDTO, Long userId);

    Result<EmotionCapsuleVO> getCapsuleById(Long capsuleId, Long userId);

    Result<List<EmotionCapsuleVO>> listUserCapsules(Long userId);

    Result<List<EmotionCapsuleVO>> getUnreadReminders(Long userId);
    Result<?> markReminderRead(Long capsuleId, Long userId);
    
    Result<?> deleteCapsule(Long capsuleId, Long userId);
} 