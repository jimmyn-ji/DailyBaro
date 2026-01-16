package com.project.controller;

import com.project.service.FileStorageService;
import com.project.util.Result;
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
        return fileStorageService.storeFile(file);
    }

    @PostMapping("/media")
    public Result<String> uploadMedia(@RequestParam("file") MultipartFile file) {
        return fileStorageService.storeFile(file); // 支持所有类型
    }
}