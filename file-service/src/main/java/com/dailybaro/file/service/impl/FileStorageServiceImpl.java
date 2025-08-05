package com.dailybaro.file.service.impl;

import com.dailybaro.file.service.FileStorageService;
import com.dailybaro.file.util.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 支持的文件类型
    private static final List<String> SUPPORTED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    );
    
    private static final List<String> SUPPORTED_AUDIO_TYPES = Arrays.asList(
        "audio/mpeg", "audio/mp3", "audio/wav", "audio/ogg", "audio/aac", "audio/m4a"
        // 移除 ncm 格式，因为浏览器无法播放
    );
    
    private static final List<String> SUPPORTED_VIDEO_TYPES = Arrays.asList(
        "video/mp4", "video/avi", "video/mov", "video/wmv", "video/flv"
    );

    @Override
    public Result<String> storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("文件为空，无法上传。");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String lowerFileName = originalFilename != null ? originalFilename.toLowerCase() : "";
        
        boolean isValidType =
            SUPPORTED_IMAGE_TYPES.contains(contentType) ||
            SUPPORTED_AUDIO_TYPES.contains(contentType) ||
            SUPPORTED_VIDEO_TYPES.contains(contentType) ||
            lowerFileName.endsWith(".ncm") ||
            lowerFileName.endsWith(".m4a") ||
            lowerFileName.endsWith(".aac");

        if (!isValidType) {
            return Result.fail("不支持的文件类型: " + (originalFilename != null ? originalFilename : "未知文件"));
        }

        try {
            // 确保上传目录存在
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            // 保存文件
            File dest = new File(uploadDir + File.separator + newFilename);
            file.transferTo(dest);

            // 返回文件访问路径
            String fileUrl = "/uploads/" + newFilename;
            return Result.success(fileUrl);
        } catch (IOException e) {
            return Result.fail("文件上传失败: " + e.getMessage());
        }
    }
} 