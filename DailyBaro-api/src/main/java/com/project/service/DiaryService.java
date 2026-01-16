package com.project.service;

import com.project.model.dto.CreateDiaryDTO;
import com.project.model.dto.QueryDiaryDTO;
import com.project.model.dto.UpdateDiaryDTO;
import com.project.model.vo.DiaryVO;
import com.project.util.Result;
import java.util.List;

public interface DiaryService {

    Result<DiaryVO> createDiary(CreateDiaryDTO createDiaryDTO, Long userId);

    Result<DiaryVO> updateDiary(UpdateDiaryDTO updateDiaryDTO);

    Result<Void> deleteDiary(Long diaryId);

    Result<DiaryVO> getDiaryById(Long diaryId);

    Result<List<DiaryVO>> findDiaries(QueryDiaryDTO queryDiaryDTO, Long userId);

    Result<Void> deleteDiaryMedia(Long mediaId);
} 