package com.dailybaro.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailybaro.content.model.Diary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface DiaryMapper extends BaseMapper<Diary> {
    List<String> findTagsByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
    List<String> findTagsByDiaryId(@Param("diaryId") Long diaryId);
    List<Diary> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
    // 批量查询标签（优化性能，避免N+1查询）
    List<Map<String, Object>> findTagsByDiaryIds(@Param("diaryIds") List<Long> diaryIds);
} 