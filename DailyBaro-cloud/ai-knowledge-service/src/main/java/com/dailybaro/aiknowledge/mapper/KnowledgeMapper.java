package com.dailybaro.aiknowledge.mapper;

import com.dailybaro.aiknowledge.model.Knowledge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeMapper {
    
    int insert(Knowledge knowledge);
    
    int update(Knowledge knowledge);
    
    int delete(Long id);
    
    Knowledge selectById(Long id);
    
    List<Knowledge> selectByCategory(@Param("category") String category);
    
    List<Knowledge> selectBySubcategory(@Param("category") String category, 
                                         @Param("subcategory") String subcategory);
    
    List<Knowledge> selectByStatus(@Param("status") Integer status);
    
    List<Knowledge> selectAll();  // 获取所有知识（用于管理后台）
    
    List<Knowledge> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * 批量查询知识（优化：避免N+1查询）
     */
    List<Knowledge> selectByIds(@Param("ids") List<Long> ids);
    
    int incrementViewCount(Long id);
}
