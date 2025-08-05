package com.dailybaro.diary.service;

import com.dailybaro.diary.model.dto.CreateDiaryDTO;
import com.dailybaro.diary.model.dto.QueryDiaryDTO;
import com.dailybaro.diary.model.dto.UpdateDiaryDTO;
import com.dailybaro.diary.model.vo.DiaryVO;
import com.dailybaro.diary.util.Result;
import java.util.List;

public interface DiaryService {

    Result<DiaryVO> createDiary(CreateDiaryDTO createDiaryDTO, Long userId);

    Result<DiaryVO> updateDiary(UpdateDiaryDTO updateDiaryDTO);

    Result<Void> deleteDiary(Long diaryId);

    Result<DiaryVO> getDiaryById(Long diaryId);

    Result<List<DiaryVO>> findDiaries(QueryDiaryDTO queryDiaryDTO, Long userId);

    Result<Void> deleteDiaryMedia(Long mediaId);
} 