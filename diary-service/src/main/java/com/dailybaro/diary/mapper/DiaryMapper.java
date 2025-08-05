package com.dailybaro.diary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailybaro.diary.model.Diary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface DiaryMapper extends BaseMapper<Diary> {
    List<String> findTagsByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
    List<String> findTagsByDiaryId(@Param("diaryId") Long diaryId);
} 