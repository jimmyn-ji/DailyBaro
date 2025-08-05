package com.dailybaro.diary.controller;

import com.dailybaro.diary.model.Tag;
import com.dailybaro.diary.model.dto.CreateDiaryDTO;
import com.dailybaro.diary.model.dto.QueryDiaryDTO;
import com.dailybaro.diary.model.dto.UpdateDiaryDTO;
import com.dailybaro.diary.model.vo.DiaryVO;
import com.dailybaro.diary.service.DiaryService;
import com.dailybaro.diary.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/diary")
public class DiaryController {

    @Autowired
    private DiaryService diaryService;

    @PostMapping
    public Result<DiaryVO> createDiary(@ModelAttribute CreateDiaryDTO createDiaryDTO, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return diaryService.createDiary(createDiaryDTO, userId);
    }

    @PutMapping("/{id}")
    public Result<DiaryVO> updateDiary(@PathVariable("id") Long diaryId, @ModelAttribute UpdateDiaryDTO updateDiaryDTO) {
        updateDiaryDTO.setDiaryId(diaryId);
        return diaryService.updateDiary(updateDiaryDTO);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteDiary(@PathVariable("id") Long diaryId) {
        return diaryService.deleteDiary(diaryId);
    }

    @DeleteMapping("/media/{mediaId}")
    public Result<Void> deleteDiaryMedia(@PathVariable("mediaId") Long mediaId) {
        return diaryService.deleteDiaryMedia(mediaId);
    }

    @GetMapping("/{id}")
    public Result<DiaryVO> getDiaryById(@PathVariable("id") Long diaryId) {
        return diaryService.getDiaryById(diaryId);
    }

    @GetMapping
    public Result<List<DiaryVO>> findDiaries(QueryDiaryDTO queryDiaryDTO, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return diaryService.findDiaries(queryDiaryDTO, userId);
    }
    
    @GetMapping("/search")
    public Result<List<DiaryVO>> searchDiaries(QueryDiaryDTO queryDiaryDTO, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return diaryService.findDiaries(queryDiaryDTO, userId);
    }
    
    @GetMapping("/tags")
    public Result<List<Tag>> getUserTags(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return diaryService.getUserTags(userId);
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