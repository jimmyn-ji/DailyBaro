package com.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.model.Comments;
import com.project.model.dto.QueryPostDTO;
import com.project.model.vo.PostVO;
import com.project.model.vo.ReplyVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comments> {


    /**
     * 根据条件查询帖子列表
     * @param queryPostDTO 查询条件 DTO
     * @return 帖子列表
     */
    List<PostVO> selectAllPosts(QueryPostDTO queryPostDTO);

    /**
     * 根据帖子 ID 获取帖子
     * @param postId 帖子 ID
     * @return 帖子对象
     */
    PostVO getPostById(Long postId);

    /**
     * 根据帖子 ID 获取帖子的回复列表
     * @param postId 帖子 ID
     * @return 回复列表
     */
    List<ReplyVO> getRepliesByPostId(Long postId);

    /**
     * 根据评论 ID 获取评论
     * @param commentId 评论 ID
     * @return 评论对象
     */
    Comments getCommentById(Long commentId);

    /**
     * 插入新评论
     * @param comments 评论对象
     * @return 插入成功返回 1，失败返回 0（通常情况）
     */
    int insertComment(Comments comments);

    /**
     * 更新评论
     * @param comments 评论对象
     * @return 更新成功返回 1，失败返回 0（通常情况）
     */
    int updateComment(Comments comments);

    /**
     * 删除评论
     * @param commentId 评论 ID
     * @return 删除成功返回 1，失败返回 0（通常情况）
     */
    int deleteComment(Long commentId);

    /**
     * 根据论坛 ID 获取评论列表
     * @param postId 论坛 ID
     * @return 评论列表
     */
    List<Comments> getCommentsByPostId(Long postId);

    /**
     * 根据用户 ID 获取评论列表
     * @param userId 用户 ID
     * @return 评论列表
     */
    List<Comments> getCommentsByUserId(Long userId);

    int incrementReplyCount(Long commentId);
}