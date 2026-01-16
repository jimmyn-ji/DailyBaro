package com.dailybaro.content.controller;

import com.dailybaro.content.model.Tag;
import com.dailybaro.content.service.DiaryService;
import com.dailybaro.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/tag")
public class TagController {
    @Autowired
    private DiaryService diaryService;

    @GetMapping("")
    public Result<List<Tag>> getAllTags(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return diaryService.getUserTags(userId);
    }

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
