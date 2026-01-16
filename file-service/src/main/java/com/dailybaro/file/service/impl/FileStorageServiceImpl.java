package com.dailybaro.file.service.impl;

import com.dailybaro.file.service.FileStorageService;
import com.dailybaro.common.util.Result;
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
        "audio/mpeg", "audio/mp3", "audio/wav", "audio/ogg", "audio/aac", "audio/m4a",
        "audio/x-m4a", "audio/mp4", "audio/x-mpeg", "audio/mpeg3"
    );
    
    private static final List<String> SUPPORTED_VIDEO_TYPES = Arrays.asList(
        "video/mp4", "video/avi", "video/mov", "video/wmv", "video/flv",
        "video/quicktime", "video/x-msvideo", "video/x-ms-wmv"
    );
    
    // 支持的扩展名（用于contentType不标准的情况，如微信小程序）
    private static final List<String> AUDIO_EXTENSIONS = Arrays.asList(
        ".mp3", ".wav", ".ogg", ".aac", ".m4a", ".mp4", ".ncm"
    );
    
    private static final List<String> VIDEO_EXTENSIONS = Arrays.asList(
        ".mp4", ".avi", ".mov", ".wmv", ".flv", ".m4v", ".3gp"
    );
    
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(
        ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp"
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
        
        // 添加调试日志
        System.out.println("File upload debug - contentType: " + contentType + ", originalFilename: " + originalFilename);
        
        // 先检查扩展名（微信小程序可能contentType不标准）
        boolean isValidByExtension = false;
        String fileExtension = "";
        if (lowerFileName.contains(".")) {
            fileExtension = lowerFileName.substring(lowerFileName.lastIndexOf("."));
            isValidByExtension = IMAGE_EXTENSIONS.contains(fileExtension) ||
                               AUDIO_EXTENSIONS.contains(fileExtension) ||
                               VIDEO_EXTENSIONS.contains(fileExtension);
        }
        
        // 再检查contentType
        boolean isValidByContentType = false;
        if (contentType != null) {
            String lowerContentType = contentType.toLowerCase();
            isValidByContentType = SUPPORTED_IMAGE_TYPES.contains(contentType) ||
                                 SUPPORTED_AUDIO_TYPES.contains(contentType) ||
                                 SUPPORTED_VIDEO_TYPES.contains(contentType) ||
                                 lowerContentType.startsWith("image/") ||
                                 lowerContentType.startsWith("audio/") ||
                                 lowerContentType.startsWith("video/");
        }
        
        boolean isValidType = isValidByExtension || isValidByContentType;

        if (!isValidType) {
            return Result.fail("不支持的文件类型: " + (originalFilename != null ? originalFilename : "未知文件") + 
                             ", contentType: " + contentType + ", lowerFileName: " + lowerFileName);
        }

        try {
            // 确保上传目录存在
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名（使用之前提取的fileExtension，如果没有则重新提取）
            if (fileExtension.isEmpty() && originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            // 保存文件
            File dest = new File(uploadDir + File.separator + newFilename);
            file.transferTo(dest);

            // 返回文件访问路径（通过网关转发）
            String fileUrl = "/uploads/" + newFilename;
            return Result.success(fileUrl);
        } catch (IOException e) {
            return Result.fail("文件上传失败: " + e.getMessage());
        }
    }
} 