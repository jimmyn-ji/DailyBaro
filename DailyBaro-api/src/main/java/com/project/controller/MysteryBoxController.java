package com.project.controller;

import com.project.model.vo.MysteryBoxVO;
import com.project.service.MysteryBoxService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/mystery-box")
public class MysteryBoxController {

    @Autowired
    private MysteryBoxService mysteryBoxService;

    @PostMapping("/draw")
    public Result<MysteryBoxVO> drawBox(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return mysteryBoxService.drawBox(userId);
    }

    @PostMapping("/complete/{id}")
    public Result<MysteryBoxVO> completeTask(@PathVariable("id") Long drawnBoxId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return mysteryBoxService.completeTask(drawnBoxId, userId);
    }

    @GetMapping("/status")
    public Result<MysteryBoxVO> getTodayStatus(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return mysteryBoxService.getTodayStatus(userId);
    }

    @GetMapping("/records")
    public Result<List<MysteryBoxVO>> getTodayRecords(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return mysteryBoxService.getTodayRecords(userId);
    }

    @GetMapping("/energy")
    public Result<Integer> getUserEnergy(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return mysteryBoxService.getUserEnergy(userId);
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