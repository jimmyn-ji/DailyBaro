package com.dailybaro.diary.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dailybaro.diary.mapper.DiaryMapper;
import com.dailybaro.diary.mapper.DiaryMediaMapper;
import com.dailybaro.diary.mapper.DiaryTagMapper;
import com.dailybaro.diary.mapper.TagMapper;
import com.dailybaro.diary.model.Diary;
import com.dailybaro.diary.model.DiaryMedia;
import com.dailybaro.diary.model.DiaryTag;
import com.dailybaro.diary.model.Tag;
import com.dailybaro.diary.model.dto.CreateDiaryDTO;
import com.dailybaro.diary.model.dto.QueryDiaryDTO;
import com.dailybaro.diary.model.dto.UpdateDiaryDTO;
import com.dailybaro.diary.model.vo.DiaryVO;
import com.dailybaro.diary.model.vo.MediaVO;
import com.dailybaro.diary.service.DiaryService;
import com.dailybaro.diary.service.FileStorageService;
import com.dailybaro.diary.service.UserServiceClient;
import com.dailybaro.diary.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DiaryServiceImpl implements DiaryService {

    @Autowired
    private DiaryMapper diaryMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private DiaryTagMapper diaryTagMapper;

    @Autowired
    private DiaryMediaMapper diaryMediaMapper;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserServiceClient userServiceClient;

    @Override
    @Transactional
    public Result<DiaryVO> createDiary(CreateDiaryDTO createDiaryDTO, Long userId) {
        // 1. Convert DTO to Diary entity and save it
        Diary diary = new Diary();
        diary.setUserId(userId);
        diary.setTitle(createDiaryDTO.getTitle());
        diary.setContent(createDiaryDTO.getContent());
        diary.setStatus(createDiaryDTO.getStatus()); // 用前端传来的status
        diary.setCreateTime(new Date());
        diary.setUpdateTime(new Date());
        diaryMapper.insert(diary);

        // 2. Handle tags
        handleTags(createDiaryDTO.getTags(), diary.getDiaryId(), userId);

        // 3. Handle media files
        if (!CollectionUtils.isEmpty(createDiaryDTO.getMediaFiles())) {
            for (MultipartFile file : createDiaryDTO.getMediaFiles()) {
                Result<String> uploadResult = fileStorageService.storeFile(file);
                if (uploadResult.getCode() == 200) {
                    DiaryMedia media = new DiaryMedia();
                    media.setDiaryId(diary.getDiaryId());
                    media.setMediaUrl(uploadResult.getData());
                    media.setMediaType(determineMediaType(file.getContentType()));
                    diaryMediaMapper.insert(media);
                } else {
                    // In a real app, you might want to roll back the transaction
                    log.error("Failed to upload file for diary {}: {}", diary.getDiaryId(), uploadResult.getMessage());
                }
            }
        }

        // 4. 写日记奖励 +1 能量
        try {
            userServiceClient.increaseEnergy(userId, 1);
            log.info("用户写日记获得能量奖励: userId={}, energy=1", userId);
        } catch (Exception e) {
            log.error("增加用户能量值失败: userId={}, energy=1", userId, e);
        }

        // 5. Convert final entity to VO and return
        return getDiaryById(diary.getDiaryId());
    }

    private void handleTags(List<String> tagNames, Long diaryId, Long userId) {
        if (CollectionUtils.isEmpty(tagNames)) {
            return;
        }
        
        for (String tagName : tagNames) {
            // Find if tag exists for the user
            QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId).eq("tag_name", tagName);
            Tag tag = tagMapper.selectOne(queryWrapper);

            // If not, create it
            if (tag == null) {
                tag = new Tag();
                tag.setUserId(userId);
                tag.setTagName(tagName);
                tagMapper.insert(tag);
            }

            // Check if diary-tag association already exists
            QueryWrapper<DiaryTag> diaryTagQuery = new QueryWrapper<>();
            diaryTagQuery.eq("diary_id", diaryId).eq("tag_id", tag.getTagId());
            DiaryTag existingDiaryTag = diaryTagMapper.selectOne(diaryTagQuery);
            
            // Only insert if not exists
            if (existingDiaryTag == null) {
                DiaryTag diaryTag = new DiaryTag();
                diaryTag.setDiaryId(diaryId);
                diaryTag.setTagId(tag.getTagId());
                diaryTagMapper.insert(diaryTag);
                log.info("Added tag '{}' to diary {}", tagName, diaryId);
            } else {
                log.info("Tag '{}' already exists for diary {}", tagName, diaryId);
            }
        }
    }

    private String determineMediaType(String contentType) {
        if (contentType == null) {
            return "unknown";
        }
        if (contentType.startsWith("image/")) {
            return "image";
        } else if (contentType.startsWith("video/")) {
            return "video";
        } else if (contentType.startsWith("audio/")) {
            return "audio";
        }
        return "unknown";
    }

    @Override
    @Transactional
    public Result<DiaryVO> updateDiary(UpdateDiaryDTO updateDiaryDTO) {
        Diary diary = diaryMapper.selectById(updateDiaryDTO.getDiaryId());
        if (diary == null) {
            return Result.fail("日记不存在");
        }
        // 1. 更新基本字段
        if (updateDiaryDTO.getTitle() != null) diary.setTitle(updateDiaryDTO.getTitle());
        if (updateDiaryDTO.getContent() != null) diary.setContent(updateDiaryDTO.getContent());
        if (updateDiaryDTO.getStatus() != null) diary.setStatus(updateDiaryDTO.getStatus());
        diary.setUpdateTime(new Date());
        diaryMapper.updateById(diary);

        // 2. 处理标签
        diaryTagMapper.deleteByDiaryId(diary.getDiaryId());
        handleTags(updateDiaryDTO.getTags(), diary.getDiaryId(), diary.getUserId());

        // 3. 处理媒体
        if (!CollectionUtils.isEmpty(updateDiaryDTO.getMediaIdsToDelete())) {
            for (Long mediaId : updateDiaryDTO.getMediaIdsToDelete()) {
                DiaryMedia media = diaryMediaMapper.selectById(mediaId);
                if (media != null) {
                    deletePhysicalFile(media.getMediaUrl());
                    diaryMediaMapper.deleteById(mediaId);
                }
            }
        }
        if (!CollectionUtils.isEmpty(updateDiaryDTO.getNewMediaFiles())) {
            for (MultipartFile file : updateDiaryDTO.getNewMediaFiles()) {
                Result<String> uploadResult = fileStorageService.storeFile(file);
                if (uploadResult.getCode() == 200) {
                    DiaryMedia media = new DiaryMedia();
                    media.setDiaryId(diary.getDiaryId());
                    media.setMediaUrl(uploadResult.getData());
                    media.setMediaType(determineMediaType(file.getContentType()));
                    diaryMediaMapper.insert(media);
                } else {
                    log.error("Failed to upload file for diary {}: {}", diary.getDiaryId(), uploadResult.getMessage());
                }
            }
        }
        return getDiaryById(diary.getDiaryId());
    }

    @Override
    @Transactional
    public Result<Void> deleteDiary(Long diaryId) {
        Diary diary = diaryMapper.selectById(diaryId);
        if (diary == null) {
            return Result.fail("日记不存在");
        }
        // 删除媒体文件
        QueryWrapper<DiaryMedia> mediaQuery = new QueryWrapper<>();
        mediaQuery.eq("diary_id", diaryId);
        List<DiaryMedia> mediaList = diaryMediaMapper.selectList(mediaQuery);
        for (DiaryMedia media : mediaList) {
            deletePhysicalFile(media.getMediaUrl());
        }
        
        // 删除标签关联
        diaryTagMapper.deleteByDiaryId(diaryId);
        
        // 删除日记
        diaryMapper.deleteById(diaryId);
        return Result.success();
    }

    @Override
    @Transactional
    public Result<Void> deleteDiaryMedia(Long mediaId) {
        DiaryMedia media = diaryMediaMapper.selectById(mediaId);
        if (media != null) {
            deletePhysicalFile(media.getMediaUrl());
            diaryMediaMapper.deleteById(mediaId);
        }
        return Result.success();
    }

    /**
     * 删除物理文件
     */
    private void deletePhysicalFile(String fileUrl) {
        if (fileUrl == null) return;
        // 假设 fileUrl 形如 /uploads/xxxx.jpg
        String basePath = System.getProperty("user.dir") + "/src/main/resources";
        String filePath = basePath + fileUrl;
        java.io.File file = new java.io.File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public Result<DiaryVO> getDiaryById(Long diaryId) {
        Diary diary = diaryMapper.selectById(diaryId);
        if (diary == null) {
            return Result.fail("Diary not found.");
        }

        // Fetch associated tags
        List<String> tagNames = diaryMapper.findTagsByDiaryId(diaryId);
        if (tagNames == null) {
            tagNames = Collections.emptyList();
        }

        // Fetch associated media
        QueryWrapper<DiaryMedia> diaryMediaQuery = new QueryWrapper<>();
        diaryMediaQuery.eq("diary_id", diaryId);
        List<DiaryMedia> diaryMediaList = diaryMediaMapper.selectList(diaryMediaQuery);
        List<MediaVO> mediaVOs = diaryMediaList.stream().map(media -> {
            MediaVO vo = new MediaVO();
            BeanUtils.copyProperties(media, vo);
            return vo;
        }).collect(Collectors.toList());


        // 暂时不获取用户账号信息，避免 Feign 调用错误
        String userAccount = null;

        // Assemble and return VO
        DiaryVO diaryVO = new DiaryVO();
        BeanUtils.copyProperties(diary, diaryVO);
        diaryVO.setUserAccount(userAccount);
        diaryVO.setTags(tagNames);
        diaryVO.setMedia(mediaVOs);

        return Result.success(diaryVO);
    }

    @Override
    public Result<List<DiaryVO>> findDiaries(QueryDiaryDTO queryDiaryDTO, Long userId) {
        QueryWrapper<Diary> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        // 状态过滤
        if (queryDiaryDTO != null && queryDiaryDTO.getStatus() != null) {
            queryWrapper.eq("status", queryDiaryDTO.getStatus());
        } else {
            // 默认只查已发布
            queryWrapper.eq("status", "published");
        }
        // 用户过滤
        if (queryDiaryDTO != null && queryDiaryDTO.getTargetUserId() != null) {
            queryWrapper.eq("user_id", queryDiaryDTO.getTargetUserId());
        }
        // 日期过滤
        if (queryDiaryDTO != null && queryDiaryDTO.getDate() != null) {
            queryWrapper.apply("DATE(create_time) = {0}", queryDiaryDTO.getDate());
        }
        // 标签过滤（如有）
        // TODO: SQL级别优化
        List<Diary> diaries = diaryMapper.selectList(queryWrapper);
        List<DiaryVO> diaryVOs = diaries.stream()
                .map(diary -> getDiaryById(diary.getDiaryId()).getData())
                .collect(Collectors.toList());
        return Result.success(diaryVOs);
    }
    
    @Override
    public Result<List<Tag>> getUserTags(Long userId) {
        List<Tag> tags = tagMapper.findByUserId(userId);
        return Result.success(tags);
    }
} 