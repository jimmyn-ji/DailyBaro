package com.dailybaro.diary.controller;


import com.dailybaro.diary.model.Diary;
import com.dailybaro.diary.mapper.DiaryMapper;
import com.dailybaro.diary.model.dto.CreateDiaryDTO;
import com.dailybaro.diary.model.dto.QueryDiaryDTO;
import com.dailybaro.diary.model.dto.UpdateDiaryDTO;
import com.dailybaro.diary.model.vo.DiaryVO;
import com.dailybaro.diary.service.DiaryService;
import com.dailybaro.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.TreeMap;
import java.text.SimpleDateFormat;

@RestController
@RequestMapping("/api/diary")
public class DiaryController {

    @Autowired
    private DiaryService diaryService;

    @Autowired
    private DiaryMapper diaryMapper;

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
    


    @GetMapping("/user-emotions")
    public List<Map<String, Object>> getUserEmotions(@RequestParam("userId") Long userId,
                                                     @RequestParam("startDate") String startDate,
                                                     @RequestParam("endDate") String endDate) {
        List<Diary> diaries = diaryMapper.findByUserIdAndDateRange(userId, java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate));
        Map<String, Map<String, Integer>> dayTagCount = new TreeMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Diary d : diaries) {
            String day = sdf.format(d.getCreateTime());
            List<String> tags = diaryMapper.findTagsByDiaryId(d.getDiaryId());
            if (tags == null || tags.isEmpty()) continue;
            dayTagCount.putIfAbsent(day, new HashMap<>());
            Map<String, Integer> tagMap = dayTagCount.get(day);
            for (String tag : tags) {
                tagMap.put(tag, tagMap.getOrDefault(tag, 0) + 1);
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> entry : dayTagCount.entrySet()) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", entry.getKey());
            dayData.putAll(entry.getValue());
            result.add(dayData);
        }
        return result;
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