package com.dailybaro.capsule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailybaro.capsule.model.CapsuleMedia;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CapsuleMediaMapper extends BaseMapper<CapsuleMedia> {
    // 使用 MyBatis-Plus 内置的 insert(CapsuleMedia) 方法
} 