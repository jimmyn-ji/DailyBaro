package com.project.controller;

import com.project.model.Tag;
import com.project.service.TagService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tag")
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping
    public Result<List<Tag>> getAllTags() {
        return Result.success(tagService.getAllTags());
    }
} 