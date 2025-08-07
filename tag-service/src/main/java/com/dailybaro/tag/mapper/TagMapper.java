package com.dailybaro.tag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailybaro.tag.model.Tag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
} 