package com.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.model.DiaryTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiaryTagMapper extends BaseMapper<DiaryTag> {
    // 通过标签名列表查找所有日记ID
    List<Long> findDiaryIdsByTagNames(@Param("tagNames") List<String> tagNames);
    List<Long> findDiaryIdsByTagIds(@Param("tagIds") List<Long> tagIds);
} 