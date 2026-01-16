package com.dailybaro.content.service;

import com.dailybaro.content.model.Tag;
import com.dailybaro.content.model.dto.CreateDiaryDTO;
import com.dailybaro.content.model.dto.QueryDiaryDTO;
import com.dailybaro.content.model.dto.UpdateDiaryDTO;
import com.dailybaro.content.model.vo.DiaryVO;
import com.dailybaro.common.util.Result;
import java.util.List;
import java.util.Map;

public interface DiaryService {

    Result<DiaryVO> createDiary(CreateDiaryDTO createDiaryDTO, Long userId);

    Result<DiaryVO> updateDiary(UpdateDiaryDTO updateDiaryDTO);

    Result<Void> deleteDiary(Long diaryId);

    Result<DiaryVO> getDiaryById(Long diaryId);

    Result<List<DiaryVO>> findDiaries(QueryDiaryDTO queryDiaryDTO, Long userId);

    Result<Void> deleteDiaryMedia(Long mediaId);
    
    Result<List<Tag>> getUserTags(Long userId);
    
    Result<Map<String, Object>> analyzeDiaryEmotion(Map<String, Object> body);
} 