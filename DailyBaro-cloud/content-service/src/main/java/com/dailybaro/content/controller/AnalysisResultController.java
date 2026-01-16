package com.dailybaro.content.controller;

import com.dailybaro.common.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisResultController {

    @GetMapping("/result")
    public Result<Object> getAnalysisResult(@RequestParam(value = "uid", required = false) String uid) {
        // TODO: 可接入数据库查询返回真实历史记录
        return Result.success(Collections.emptyList());
    }
}


