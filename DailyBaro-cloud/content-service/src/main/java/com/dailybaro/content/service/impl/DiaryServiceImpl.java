package com.dailybaro.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dailybaro.content.mapper.DiaryMapper;
import com.dailybaro.content.mapper.DiaryMediaMapper;
import com.dailybaro.content.mapper.DiaryTagMapper;
import com.dailybaro.content.mapper.TagMapper;
import com.dailybaro.content.model.Diary;
import com.dailybaro.content.model.DiaryMedia;
import com.dailybaro.content.model.DiaryTag;
import com.dailybaro.content.model.Tag;
import com.dailybaro.content.model.dto.CreateDiaryDTO;
import com.dailybaro.content.model.dto.QueryDiaryDTO;
import com.dailybaro.content.model.dto.UpdateDiaryDTO;
import com.dailybaro.content.model.vo.DiaryVO;
import com.dailybaro.content.model.vo.MediaVO;
import com.dailybaro.content.service.DiaryService;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.beans.factory.annotation.Value;
import com.dailybaro.content.service.UserServiceClient;
import com.dailybaro.content.service.NLPServiceClient;
import com.dailybaro.common.util.Result;
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
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
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

    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${file.service.url:http://localhost:8000/uploads/media}")
    private String fileServiceUrl;

    @Autowired
    private UserServiceClient userServiceClient;
    
    @Autowired
    private NLPServiceClient nlpServiceClient;

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

        // 3. Handle media files - 统一使用 file-service
        if (!CollectionUtils.isEmpty(createDiaryDTO.getMediaFiles())) {
            for (MultipartFile file : createDiaryDTO.getMediaFiles()) {
                try {
                    String fileUrl = uploadToFileService(file);
                    if (fileUrl != null && !fileUrl.isEmpty()) {
                        DiaryMedia media = new DiaryMedia();
                        media.setDiaryId(diary.getDiaryId());
                        media.setMediaUrl(fileUrl);
                        media.setMediaType(determineMediaType(file));
                        diaryMediaMapper.insert(media);
                    } else {
                        log.error("Failed to upload file for diary {}: file service returned empty URL", diary.getDiaryId());
                    }
                } catch (Exception e) {
                    log.error("Failed to upload file for diary {}: {}", diary.getDiaryId(), e.getMessage());
                }
            }
        }

        // 4. 写日记奖励 +1 能量（仅在发布时）
        if ("published".equals(createDiaryDTO.getStatus())) {
        try {
            userServiceClient.increaseEnergy(userId, 1);
                log.info("用户发布日记获得能量奖励: userId={}, diaryId={}, energy=1", userId, diary.getDiaryId());
        } catch (Exception e) {
                log.error("增加用户能量值失败: userId={}, diaryId={}, energy=1", userId, diary.getDiaryId(), e);
            }
        } else {
            log.debug("日记保存为草稿，不增加能量: userId={}, diaryId={}, status={}", userId, diary.getDiaryId(), createDiaryDTO.getStatus());
        }

        // 5. Convert final entity to VO and return
        return getDiaryById(diary.getDiaryId());
    }

    /**
     * 优化后的标签处理方法：批量查询标签，减少数据库查询次数
     */
    private void handleTags(List<String> tagNames, Long diaryId, Long userId) {
        if (CollectionUtils.isEmpty(tagNames)) {
            return;
        }
        
        // 批量查询所有标签（一次性查询，避免循环查询）
        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        tagQueryWrapper.in("tag_name", tagNames);
        List<Tag> existingTags = tagMapper.selectList(tagQueryWrapper);
        
        // 构建标签名到Tag的映射
        Map<String, Tag> tagMap = existingTags.stream()
                .collect(Collectors.toMap(Tag::getTagName, tag -> tag, (existing, replacement) -> existing));
        
        // 批量查询已存在的日记-标签关联
        QueryWrapper<DiaryTag> diaryTagQuery = new QueryWrapper<>();
        diaryTagQuery.eq("diary_id", diaryId);
        if (!existingTags.isEmpty()) {
            List<Long> existingTagIds = existingTags.stream().map(Tag::getTagId).collect(Collectors.toList());
            diaryTagQuery.in("tag_id", existingTagIds);
        }
        List<DiaryTag> existingDiaryTags = diaryTagMapper.selectList(diaryTagQuery);
        Set<Long> existingTagIdSet = existingDiaryTags.stream()
                .map(DiaryTag::getTagId)
                .collect(Collectors.toSet());
        
        // 批量插入新标签和关联
        List<Tag> tagsToInsert = new ArrayList<>();
        List<DiaryTag> diaryTagsToInsert = new ArrayList<>();
        
        for (String tagName : tagNames) {
            Tag tag = tagMap.get(tagName);
            
            // 不存在则创建（批量插入）
            if (tag == null) {
                tag = new Tag();
                tag.setTagName(tagName);
                tagsToInsert.add(tag);
                tagMap.put(tagName, tag); // 更新映射
            }
            
            // 检查关联是否存在
            if (!existingTagIdSet.contains(tag.getTagId())) {
                DiaryTag diaryTag = new DiaryTag();
                diaryTag.setDiaryId(diaryId);
                diaryTag.setTagId(tag.getTagId());
                diaryTagsToInsert.add(diaryTag);
            }
        }
        
        // 批量插入新标签
        if (!tagsToInsert.isEmpty()) {
            for (Tag tag : tagsToInsert) {
                tagMapper.insert(tag);
                log.debug("Created new tag: {}", tag.getTagName());
            }
        }
        
        // 批量插入日记-标签关联
        if (!diaryTagsToInsert.isEmpty()) {
            for (DiaryTag diaryTag : diaryTagsToInsert) {
                diaryTagMapper.insert(diaryTag);
            }
            log.info("Added {} tags to diary {}", diaryTagsToInsert.size(), diaryId);
        }
    }

    private String determineMediaType(MultipartFile file) {
        if (file == null) return "unknown";

        // 1) contentType 优先
        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.startsWith("image/")) return "image";
            if (contentType.startsWith("video/")) return "video";
            if (contentType.startsWith("audio/")) return "audio";
        }

        // 2) 文件后缀兜底（小程序/部分环境会给 application/octet-stream）
        String name = file.getOriginalFilename();
        if (name == null) return "unknown";
        String lower = name.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif") || lower.endsWith(".webp") || lower.endsWith(".bmp")) {
            return "image";
        }
        if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mov") || lower.endsWith(".wmv") || lower.endsWith(".flv") || lower.endsWith(".mkv")) {
            return "video";
        }
        if (lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".ogg") || lower.endsWith(".aac") || lower.endsWith(".m4a") || lower.endsWith(".ncm")) {
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
        
        // 检查是否从草稿变为已发布（需要增加能量点）
        boolean isPublishing = false;
        if (updateDiaryDTO.getStatus() != null && 
            "draft".equals(diary.getStatus()) && 
            "published".equals(updateDiaryDTO.getStatus())) {
            isPublishing = true;
        }
        
        // 1. 更新基本字段
        if (updateDiaryDTO.getTitle() != null) diary.setTitle(updateDiaryDTO.getTitle());
        if (updateDiaryDTO.getContent() != null) diary.setContent(updateDiaryDTO.getContent());
        if (updateDiaryDTO.getStatus() != null) diary.setStatus(updateDiaryDTO.getStatus());
        diary.setUpdateTime(new Date());
        diaryMapper.updateById(diary);
        
        // 2. 如果是从草稿发布，增加能量点
        if (isPublishing) {
            try {
                userServiceClient.increaseEnergy(diary.getUserId(), 1);
                log.info("用户发布日记获得能量奖励: userId={}, diaryId={}, energy=1", diary.getUserId(), diary.getDiaryId());
            } catch (Exception e) {
                log.error("增加用户能量值失败: userId={}, diaryId={}, energy=1", diary.getUserId(), diary.getDiaryId(), e);
            }
        }

        // 3. 处理标签
        diaryTagMapper.deleteByDiaryId(diary.getDiaryId());
        handleTags(updateDiaryDTO.getTags(), diary.getDiaryId(), diary.getUserId());

        // 4. 处理媒体（文件删除由 file-service 处理，这里只删除数据库记录）
        if (!CollectionUtils.isEmpty(updateDiaryDTO.getMediaIdsToDelete())) {
            for (Long mediaId : updateDiaryDTO.getMediaIdsToDelete()) {
                DiaryMedia media = diaryMediaMapper.selectById(mediaId);
                if (media != null) {
                    // 注意：实际文件删除应该由 file-service 的删除接口处理
                    diaryMediaMapper.deleteById(mediaId);
                }
            }
        }
        if (!CollectionUtils.isEmpty(updateDiaryDTO.getNewMediaFiles())) {
            for (MultipartFile file : updateDiaryDTO.getNewMediaFiles()) {
                try {
                    String fileUrl = uploadToFileService(file);
                    if (fileUrl != null && !fileUrl.isEmpty()) {
                        DiaryMedia media = new DiaryMedia();
                        media.setDiaryId(diary.getDiaryId());
                        media.setMediaUrl(fileUrl);
                        media.setMediaType(determineMediaType(file));
                        diaryMediaMapper.insert(media);
                    } else {
                        log.error("Failed to upload file for diary {}: file service returned empty URL", diary.getDiaryId());
                    }
                } catch (Exception e) {
                    log.error("Failed to upload file for diary {}: {}", diary.getDiaryId(), e.getMessage());
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
        // 删除媒体文件（文件删除由 file-service 处理，这里只删除数据库记录）
        QueryWrapper<DiaryMedia> mediaQuery = new QueryWrapper<>();
        mediaQuery.eq("diary_id", diaryId);
        List<DiaryMedia> mediaList = diaryMediaMapper.selectList(mediaQuery);
        // 注意：实际文件删除应该由 file-service 的删除接口处理，这里只删除数据库记录
        
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
            // 注意：实际文件删除应该由 file-service 的删除接口处理，这里只删除数据库记录
            diaryMediaMapper.deleteById(mediaId);
        }
        return Result.success();
    }

    /**
     * 上传文件到 file-service（统一使用 file-service 处理文件）
     */
    private String uploadToFileService(MultipartFile file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = 
                new HttpEntity<>(body, headers);
            
            ResponseEntity<Result> response = restTemplate.postForEntity(
                fileServiceUrl + "/uploads/media",
                requestEntity,
                Result.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Result<String> result = response.getBody();
                if (result.getCode() == 200) {
                    return result.getData();
                } else {
                    log.error("File service returned error: {}", result.getMessage());
                    return null;
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to upload file to file-service: {}", e.getMessage(), e);
            return null;
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

    /**
     * 优化后的查询方法：批量查询标签和媒体，避免N+1查询问题
     */
    @Override
    public Result<List<DiaryVO>> findDiaries(QueryDiaryDTO queryDiaryDTO, Long userId) {
        QueryWrapper<Diary> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        } else {
            return Result.fail("用户未登录");
        }
        
        if (queryDiaryDTO != null && queryDiaryDTO.getStatus() != null && !queryDiaryDTO.getStatus().equals("all")) {
            queryWrapper.eq("status", queryDiaryDTO.getStatus());
        }
        
        // 日期过滤
        if (queryDiaryDTO != null && queryDiaryDTO.getDate() != null) {
            queryWrapper.apply("DATE(create_time) = {0}", queryDiaryDTO.getDate());
        }
        
        // 标签过滤（SQL级别优化）
        if (queryDiaryDTO != null && !CollectionUtils.isEmpty(queryDiaryDTO.getTagIds())) {
            QueryWrapper<DiaryTag> tagQuery = new QueryWrapper<>();
            tagQuery.in("tag_id", queryDiaryDTO.getTagIds());
            List<DiaryTag> diaryTags = diaryTagMapper.selectList(tagQuery);
            if (!diaryTags.isEmpty()) {
                List<Long> diaryIds = diaryTags.stream()
                        .map(DiaryTag::getDiaryId)
                        .distinct()
                        .collect(Collectors.toList());
                queryWrapper.in("diary_id", diaryIds);
            } else {
                // 如果没有匹配的标签，返回空列表
                return Result.success(Collections.emptyList());
            }
        }
        
        // 查询日记列表
        List<Diary> diaries = diaryMapper.selectList(queryWrapper);
        if (diaries.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        
        // 批量查询所有日记的标签和媒体（避免N+1查询）
        List<Long> diaryIds = diaries.stream().map(Diary::getDiaryId).collect(Collectors.toList());
        
        // 批量查询标签
        Map<Long, List<String>> tagsMap = new HashMap<>();
        if (!diaryIds.isEmpty()) {
            List<Map<String, Object>> tagResults = diaryMapper.findTagsByDiaryIds(diaryIds);
            for (Map<String, Object> tagResult : tagResults) {
                Long diaryId = ((Number) tagResult.get("diary_id")).longValue();
                String tagName = (String) tagResult.get("tag_name");
                tagsMap.computeIfAbsent(diaryId, k -> new ArrayList<>()).add(tagName);
            }
        }
        
        // 批量查询媒体
        Map<Long, List<MediaVO>> mediaMap = new HashMap<>();
        QueryWrapper<DiaryMedia> mediaQuery = new QueryWrapper<>();
        mediaQuery.in("diary_id", diaryIds);
        List<DiaryMedia> allMedia = diaryMediaMapper.selectList(mediaQuery);
        for (DiaryMedia media : allMedia) {
            MediaVO vo = new MediaVO();
            BeanUtils.copyProperties(media, vo);
            mediaMap.computeIfAbsent(media.getDiaryId(), k -> new ArrayList<>()).add(vo);
        }
        
        // 组装VO（单次循环，高效）
        List<DiaryVO> diaryVOs = diaries.stream().map(diary -> {
            DiaryVO vo = new DiaryVO();
            BeanUtils.copyProperties(diary, vo);
            vo.setTags(tagsMap.getOrDefault(diary.getDiaryId(), Collections.emptyList()));
            vo.setMedia(mediaMap.getOrDefault(diary.getDiaryId(), Collections.emptyList()));
            vo.setUserAccount(null); // 暂时不获取用户账号信息
            return vo;
        }).collect(Collectors.toList());
        
        return Result.success(diaryVOs);
    }
    
    @Override
    public Result<List<Tag>> getUserTags(Long userId) {
        List<Tag> tags = tagMapper.findAll();
        return Result.success(tags);
    }
    
    @Override
    public Result<Map<String, Object>> analyzeDiaryEmotion(Map<String, Object> body) {
        try {
            String content = (String) body.get("content");
            if (content == null || content.trim().isEmpty()) {
                return Result.fail("日记内容不能为空");
            }
            
            // 调用NLP服务进行情绪分析
            Map<String, Object> request = new java.util.HashMap<>();
            request.put("text", content);
            
            Result<Map<String, Object>> nlpResult = nlpServiceClient.analyzeEmotion(request);
            
            if (nlpResult != null && nlpResult.getCode() == 200) {
                return Result.success(nlpResult.getData());
            } else {
                // 如果NLP服务不可用，返回默认结果
                Map<String, Object> defaultResult = new java.util.HashMap<>();
                defaultResult.put("emotion", "中性");
                defaultResult.put("confidence", 0.5);
                defaultResult.put("message", "NLP服务暂时不可用，返回默认分析结果");
                return Result.success(defaultResult);
            }
        } catch (Exception e) {
            log.error("情绪分析失败", e);
            // 返回默认结果
            Map<String, Object> defaultResult = new java.util.HashMap<>();
            defaultResult.put("emotion", "中性");
            defaultResult.put("confidence", 0.5);
            defaultResult.put("message", "情绪分析服务异常: " + e.getMessage());
            return Result.success(defaultResult);
        }
    }
} 