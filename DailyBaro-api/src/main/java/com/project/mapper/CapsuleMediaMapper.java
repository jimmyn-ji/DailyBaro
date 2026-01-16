package com.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.model.CapsuleMedia;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;

@Mapper
public interface CapsuleMediaMapper extends BaseMapper<CapsuleMedia> {

    @Insert("INSERT INTO capsule_media (capsule_id, media_type, media_url) VALUES (#{capsuleId}, #{mediaType}, #{mediaUrl})")
    int insert(CapsuleMedia media);
} 