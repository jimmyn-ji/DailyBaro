package com.dailybaro.file.controller;

import com.dailybaro.file.service.FileStorageService;
import com.dailybaro.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/uploads")
public class FileUploadController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        Result<String> r = fileStorageService.storeFile(file);
        // 统一返回 { code, message, data }，data为可直接访问的URL
        return r;
    }

    @PostMapping("/media")
    public Result<String> uploadMedia(@RequestParam("file") MultipartFile file) {
        Result<String> r = fileStorageService.storeFile(file);
        return r;
    }
} 