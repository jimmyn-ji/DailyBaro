package com.dailybaro.tag.service.impl;

import com.dailybaro.tag.mapper.TagMapper;
import com.dailybaro.tag.model.Tag;
import com.dailybaro.tag.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {
    @Autowired
    private TagMapper tagMapper;

    @Override
    public List<Tag> getAllTags() {
        return tagMapper.selectList(null);
    }
} 