package com.dailybaro.diary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailybaro.diary.model.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    List<Tag> findByUserId(@Param("userId") Long userId);
} 