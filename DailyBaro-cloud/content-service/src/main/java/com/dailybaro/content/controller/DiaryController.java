package com.dailybaro.content.controller;


import com.dailybaro.content.model.Diary;
import com.dailybaro.content.mapper.DiaryMapper;
import com.dailybaro.content.model.dto.CreateDiaryDTO;
import com.dailybaro.content.model.dto.QueryDiaryDTO;
import com.dailybaro.content.model.dto.UpdateDiaryDTO;
import com.dailybaro.content.model.vo.DiaryVO;
import com.dailybaro.content.service.DiaryService;
import com.dailybaro.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Collections;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

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
    
    @PostMapping("/analyze-emotion")
    public Result<Map<String, Object>> analyzeDiaryEmotion(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return diaryService.analyzeDiaryEmotion(body);
    }
    


    /**
     * 优化后的用户情绪统计方法：批量查询标签，减少数据库查询次数
     */
    @GetMapping("/user-emotions")
    public List<Map<String, Object>> getUserEmotions(@RequestParam("userId") Long userId,
                                                     @RequestParam("startDate") String startDate,
                                                     @RequestParam("endDate") String endDate) {
        List<Diary> diaries = diaryMapper.findByUserIdAndDateRange(userId, java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate));
        if (diaries.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 批量查询所有日记的标签（一次性查询，避免循环查询）
        List<Long> diaryIds = diaries.stream().map(Diary::getDiaryId).collect(Collectors.toList());
        List<Map<String, Object>> tagResults = diaryMapper.findTagsByDiaryIds(diaryIds);
        
        // 构建日记ID到标签列表的映射
        Map<Long, List<String>> diaryTagsMap = new HashMap<>();
        for (Map<String, Object> tagResult : tagResults) {
            Long diaryId = ((Number) tagResult.get("diary_id")).longValue();
            String tagName = (String) tagResult.get("tag_name");
            diaryTagsMap.computeIfAbsent(diaryId, k -> new ArrayList<>()).add(tagName);
        }
        
        // 单次循环统计标签（优化性能）
        Map<String, Map<String, Integer>> dayTagCount = new TreeMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Diary d : diaries) {
            String day = sdf.format(d.getCreateTime());
            List<String> tags = diaryTagsMap.getOrDefault(d.getDiaryId(), Collections.emptyList());
            if (tags.isEmpty()) continue;
            
            Map<String, Integer> tagMap = dayTagCount.computeIfAbsent(day, k -> new HashMap<>());
            for (String tag : tags) {
                tagMap.put(tag, tagMap.getOrDefault(tag, 0) + 1);
            }
        }
        
        // 转换为结果列表
        return dayTagCount.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", entry.getKey());
                    dayData.putAll(entry.getValue());
                    return dayData;
                })
                .collect(Collectors.toList());
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