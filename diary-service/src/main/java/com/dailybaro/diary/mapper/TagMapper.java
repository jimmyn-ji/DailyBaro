package com.dailybaro.diary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailybaro.diary.model.Tag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
} 