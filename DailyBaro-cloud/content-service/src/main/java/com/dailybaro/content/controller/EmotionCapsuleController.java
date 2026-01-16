package com.dailybaro.content.controller;

import com.dailybaro.content.model.dto.CreateCapsuleDTO;
import com.dailybaro.content.model.dto.CreateCapsuleJsonDTO;
import com.dailybaro.content.model.vo.EmotionCapsuleVO;
import com.dailybaro.content.service.EmotionCapsuleService;
import com.dailybaro.common.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/capsules")
public class EmotionCapsuleController {

    @Autowired
    private EmotionCapsuleService capsuleService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<EmotionCapsuleVO> createCapsule(@ModelAttribute CreateCapsuleDTO createDTO,
                                                  @RequestPart(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles,
                                                  HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        // 将文件列表塞回DTO，确保服务层能获取到
        if (mediaFiles != null) {
            createDTO.setMediaFiles(mediaFiles);
        }
        return capsuleService.createCapsule(createDTO, userId);
    }

    // 小程序端简化：直接传已上传的mediaUrls
    @PostMapping(path = "/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<EmotionCapsuleVO> createCapsuleJson(@RequestBody CreateCapsuleJsonDTO createJsonDTO,
                                                      HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        CreateCapsuleDTO dto = new CreateCapsuleDTO();
        dto.setCurrentEmotion(createJsonDTO.getCurrentEmotion());
        dto.setThoughts(createJsonDTO.getThoughts());
        dto.setFutureGoal(createJsonDTO.getFutureGoal());
        dto.setOpenTime(createJsonDTO.getOpenTime());
        dto.setReminderType(createJsonDTO.getReminderType());
        // mediaUrls 将在 service 层持久化
        return capsuleService.createCapsule(dto, userId);
    }

    @GetMapping
    public Result<List<EmotionCapsuleVO>> listMyCapsules(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return capsuleService.listUserCapsules(userId);
    }

    @GetMapping("/{id}")
    public Result<EmotionCapsuleVO> getCapsule(@PathVariable("id") Long capsuleId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return capsuleService.getCapsuleById(capsuleId, userId);
    }
    
    @GetMapping("/reminders/unread")
    public Result<List<EmotionCapsuleVO>> getUnreadReminders(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Result.fail("用户未登录");
            }
            return capsuleService.getUnreadReminders(userId);
        } catch (Exception e) {
            log.error("获取未读提醒失败", e);
            return Result.fail("获取未读提醒失败: " + e.getMessage());
        }
    }

    @PostMapping("/reminders/read/{capsuleId}")
    public Result<?> markReminderRead(@PathVariable("capsuleId") Long capsuleId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return capsuleService.markReminderRead(capsuleId, userId);
    }
    
    @DeleteMapping("/{id}")
    public Result<?> deleteCapsule(@PathVariable("id") Long capsuleId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return capsuleService.deleteCapsule(capsuleId, userId);
    }
    
    /**
     * 从请求头中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String uidHeader = request.getHeader("uid");
        if (uidHeader != null && !uidHeader.trim().isEmpty()) {
            try {
                return Long.parseLong(uidHeader.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
} 