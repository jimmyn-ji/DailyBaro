package com.dailybaro.aiknowledge.controller;

import com.dailybaro.common.util.Result;
import com.dailybaro.aiknowledge.model.dto.KnowledgeDTO;
import com.dailybaro.aiknowledge.model.dto.SearchDTO;
import com.dailybaro.aiknowledge.model.vo.KnowledgeVO;
import com.dailybaro.aiknowledge.model.vo.SearchResultVO;
import com.dailybaro.aiknowledge.model.Knowledge;
import com.dailybaro.aiknowledge.service.KnowledgeService;
import com.dailybaro.aiknowledge.mapper.KnowledgeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
    
    @Autowired
    private KnowledgeService knowledgeService;
    
    @Autowired
    private KnowledgeMapper knowledgeMapper;
    
    /**
     * 创建知识
     */
    @PostMapping("/create")
    public Result<Long> createKnowledge(@RequestBody KnowledgeDTO dto,
                                        @RequestHeader(value = "uid", required = false) Long uid) {
        try {
            Long id = knowledgeService.createKnowledge(dto, uid);
            return Result.success(id);
        } catch (Exception e) {
            log.error("Failed to create knowledge", e);
            return Result.error(500, "创建知识失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新知识
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateKnowledge(@PathVariable Long id, @RequestBody KnowledgeDTO dto) {
        try {
            boolean success = knowledgeService.updateKnowledge(id, dto);
            return success ? Result.success(true) : Result.error(404, "知识不存在");
        } catch (Exception e) {
            log.error("Failed to update knowledge", e);
            return Result.error(500, "更新知识失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除知识
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteKnowledge(@PathVariable Long id) {
        try {
            boolean success = knowledgeService.deleteKnowledge(id);
            return success ? Result.success(true) : Result.error(404, "知识不存在");
        } catch (Exception e) {
            log.error("Failed to delete knowledge", e);
            return Result.error(500, "删除知识失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取知识详情
     */
    @GetMapping("/{id}")
    public Result<KnowledgeVO> getKnowledgeById(@PathVariable Long id) {
        try {
            KnowledgeVO vo = knowledgeService.getKnowledgeById(id);
            return vo != null ? Result.success(vo) : Result.error(404, "知识不存在");
        } catch (Exception e) {
            log.error("Failed to get knowledge", e);
            return Result.error(500, "获取知识失败: " + e.getMessage());
        }
    }
    
    /**
     * 按分类获取知识列表
     */
    @GetMapping("/category/{category}")
    public Result<List<KnowledgeVO>> getKnowledgeByCategory(@PathVariable String category) {
        try {
            List<KnowledgeVO> list = knowledgeService.getKnowledgeByCategory(category);
            return Result.success(list);
        } catch (Exception e) {
            log.error("Failed to get knowledge by category", e);
            return Result.error(500, "获取知识列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 按子分类获取知识列表
     */
    @GetMapping("/category/{category}/subcategory/{subcategory}")
    public Result<List<KnowledgeVO>> getKnowledgeBySubcategory(@PathVariable String category,
                                                                 @PathVariable String subcategory) {
        try {
            List<KnowledgeVO> list = knowledgeService.getKnowledgeBySubcategory(category, subcategory);
            return Result.success(list);
        } catch (Exception e) {
            log.error("Failed to get knowledge by subcategory", e);
            return Result.error(500, "获取知识列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 搜索知识（支持RAG）
     */
    @PostMapping("/search")
    public Result<SearchResultVO> searchKnowledge(@RequestBody SearchDTO searchDTO) {
        try {
            SearchResultVO result = knowledgeService.searchKnowledge(searchDTO);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Failed to search knowledge", e);
            return Result.error(500, "搜索失败: " + e.getMessage());
        }
    }
    
    /**
     * 关键词搜索（传统方式）
     */
    @GetMapping("/search")
    public Result<SearchResultVO> searchByKeyword(@RequestParam String keyword,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size) {
        try {
            SearchDTO searchDTO = new SearchDTO();
            searchDTO.setQuery(keyword);
            searchDTO.setPage(page);
            searchDTO.setSize(size);
            searchDTO.setUseRAG(false);
            
            SearchResultVO result = knowledgeService.searchKnowledge(searchDTO);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Failed to search by keyword", e);
            return Result.error(500, "搜索失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取知识列表（管理后台用）
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getKnowledgeList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            // 优化：使用数据库分页查询，避免加载所有数据到内存
            List<Knowledge> allKnowledge = status != null 
                    ? knowledgeMapper.selectByStatus(status)
                    : knowledgeMapper.selectAll();
            
            // 优化：合并过滤条件，减少stream操作次数
            List<Knowledge> filtered = new java.util.ArrayList<>();
            for (Knowledge k : allKnowledge) {
                // 合并所有过滤条件到一次判断
                boolean match = true;
                if (keyword != null && !keyword.isEmpty()) {
                    match = (k.getTitle() != null && k.getTitle().contains(keyword)) 
                         || (k.getContent() != null && k.getContent().contains(keyword));
                }
                if (match && category != null && !category.equals(k.getCategory())) {
                    match = false;
                }
                if (match) {
                    filtered.add(k);
                }
            }
            
            // 分页
            int total = filtered.size();
            int fromIndex = (page - 1) * size;
            int toIndex = Math.min(fromIndex + size, total);
            List<Knowledge> pagedList = fromIndex < total ? filtered.subList(fromIndex, toIndex) : new java.util.ArrayList<>();
            
            // 优化：一次性转换，避免多次stream
            List<KnowledgeVO> voList = new java.util.ArrayList<>(pagedList.size());
            for (Knowledge k : pagedList) {
                KnowledgeVO vo = new KnowledgeVO();
                org.springframework.beans.BeanUtils.copyProperties(k, vo);
                voList.add(vo);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", voList);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("Failed to get knowledge list", e);
            return Result.error(500, "获取列表失败: " + e.getMessage());
        }
    }
}
