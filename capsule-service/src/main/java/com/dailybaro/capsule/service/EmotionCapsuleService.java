package com.dailybaro.capsule.service;

import com.dailybaro.capsule.model.dto.CreateCapsuleDTO;
import com.dailybaro.capsule.model.vo.EmotionCapsuleVO;
import com.dailybaro.common.util.Result;

import java.util.List;

public interface EmotionCapsuleService {

    Result<EmotionCapsuleVO> createCapsule(CreateCapsuleDTO createDTO, Long userId);

    Result<EmotionCapsuleVO> getCapsuleById(Long capsuleId, Long userId);

    Result<List<EmotionCapsuleVO>> listUserCapsules(Long userId);

    Result<List<EmotionCapsuleVO>> getUnreadReminders(Long userId);
    Result<?> markReminderRead(Long capsuleId, Long userId);
    
    Result<?> deleteCapsule(Long capsuleId, Long userId);
} 