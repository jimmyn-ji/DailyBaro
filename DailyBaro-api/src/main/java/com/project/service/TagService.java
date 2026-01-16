package com.project.service;

import com.project.model.Tag;
import com.project.mapper.TagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagService {
    @Autowired
    private TagMapper tagMapper;

    public List<Tag> getAllTags() {
        return tagMapper.selectList(null);
    }
} 