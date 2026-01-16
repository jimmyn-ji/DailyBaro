package com.project.controller;

import com.project.model.dto.CreateCapsuleDTO;
import com.project.model.vo.EmotionCapsuleVO;
import com.project.service.EmotionCapsuleService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/capsules")
public class EmotionCapsuleController {

    @Autowired
    private EmotionCapsuleService capsuleService;

    @PostMapping
    public Result<EmotionCapsuleVO> createCapsule(@ModelAttribute CreateCapsuleDTO createDTO, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return capsuleService.createCapsule(createDTO, userId);
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
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return capsuleService.getUnreadReminders(userId);
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