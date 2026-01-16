package com.dailybaro.aiknowledge.controller;

import com.dailybaro.common.util.Result;
import com.dailybaro.aiknowledge.model.dto.KnowledgeDTO;
import com.dailybaro.aiknowledge.service.KnowledgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 知识库内容导入控制器
 * 用于批量导入知识库内容
 */
@Slf4j
@RestController
@RequestMapping("/api/knowledge/import")
public class KnowledgeImportController {
    
    @Autowired
    private KnowledgeService knowledgeService;
    
    /**
     * 批量导入知识
     * 请求体示例：
     * {
     *   "items": [
     *     {
     *       "title": "标题",
     *       "content": "内容",
     *       "category": "分类",
     *       "subcategory": "子分类"
     *     }
     *   ]
     * }
     */
    @PostMapping("/batch")
    public Result<Map<String, Object>> batchImport(
            @RequestHeader(value = "uid", required = false) Long uid,
            @RequestBody Map<String, Object> request) {
        try {
            Object itemsObj = request.get("items");
            if (!(itemsObj instanceof List)) {
                return Result.error(400, "items必须是数组");
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) itemsObj;
            
            int successCount = 0;
            int failCount = 0;
            
            for (Map<String, Object> item : items) {
                try {
                    KnowledgeDTO dto = new KnowledgeDTO();
                    dto.setTitle((String) item.get("title"));
                    dto.setContent((String) item.get("content"));
                    dto.setCategory((String) item.get("category"));
                    dto.setSubcategory((String) item.get("subcategory"));
                    dto.setStatus(1); // 默认发布状态
                    
                    if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
                        failCount++;
                        continue;
                    }
                    
                    knowledgeService.createKnowledge(dto, uid);
                    successCount++;
                } catch (Exception e) {
                    log.error("导入知识失败: {}", item, e);
                    failCount++;
                }
            }
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("total", items.size());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量导入失败", e);
            return Result.error(500, "批量导入失败: " + e.getMessage());
        }
    }
    
    /**
     * 从文本文件导入（简单格式：每行一个知识，格式：标题|内容|分类|子分类）
     */
    @PostMapping("/from-text")
    public Result<Map<String, Object>> importFromText(
            @RequestHeader(value = "uid", required = false) Long uid,
            @RequestBody Map<String, Object> request) {
        try {
            String text = (String) request.get("text");
            if (text == null || text.trim().isEmpty()) {
                return Result.error(400, "文本内容不能为空");
            }
            
            String[] lines = text.split("\n");
            int successCount = 0;
            int failCount = 0;
            
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                
                try {
                    String[] parts = line.split("\\|");
                    if (parts.length < 2) {
                        failCount++;
                        continue;
                    }
                    
                    KnowledgeDTO dto = new KnowledgeDTO();
                    dto.setTitle(parts[0].trim());
                    dto.setContent(parts[1].trim());
                    if (parts.length > 2) {
                        dto.setCategory(parts[2].trim());
                    }
                    if (parts.length > 3) {
                        dto.setSubcategory(parts[3].trim());
                    }
                    dto.setStatus(1);
                    
                    knowledgeService.createKnowledge(dto, uid);
                    successCount++;
                } catch (Exception e) {
                    log.error("导入知识失败: {}", line, e);
                    failCount++;
                }
            }
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("total", lines.length);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("从文本导入失败", e);
            return Result.error(500, "从文本导入失败: " + e.getMessage());
        }
    }
}
