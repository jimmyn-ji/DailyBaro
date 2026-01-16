package com.project.controller;

import com.project.model.dto.InsertCommentDTO;
import com.project.model.dto.QueryPostDTO;
import com.project.model.dto.ReplyDTO;
import com.project.model.dto.UpdateCommentDTO;
import com.project.model.vo.CommentVO;
import com.project.model.vo.PostVO;
import com.project.model.vo.ReplyVO;
import com.project.service.CommentService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 查询所有帖子信息
     * @param queryDto 查询条件 DTO
     * @return 帖子列表
     */
    @RequestMapping("/selectAllPosts")
    public Result<List<PostVO>> selectAllPosts(@RequestBody QueryPostDTO queryDto) {
        return commentService.selectAllPosts(queryDto);
    }

    /**
     * 根据帖子 ID 获取帖子详情
     * @param postId 帖子 ID
     * @return 帖子详情
     */
    @RequestMapping("/getPostById/{postId}")
    public Result<List<ReplyVO>> getPostById(@PathVariable Long postId) {
        return commentService.getPostById(postId);
    }
    /**
     * 发表评论
     * @param insertDto 评论插入信息
     * @return 操作结果
     */
    @RequestMapping("/insertPost")
    public Result<CommentVO> insertComment(@RequestBody InsertCommentDTO insertDto) {
        return commentService.insertComment(insertDto);
    }

    /**
     * 更新评论信息
     * @param updateDto 评论更新信息
     * @return 操作结果
     */
    @RequestMapping("/updatePost")
    public Result<CommentVO> updateComment(@RequestBody UpdateCommentDTO updateDto) {
        return commentService.updateComment(updateDto);
    }

    /**
     * 删除评论
     * @param commentId 评论ID
     * @return 操作结果
     */
    @RequestMapping("/deletePost/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        return commentService.deleteComment(commentId);
    }

    /**
     * 根据论坛ID获取评论列表
     * @param postId 论坛ID
     * @return 评论列表
     */
    @RequestMapping("/getCommentsByPostId/{postId}")
    public Result<List<CommentVO>> getCommentsByPostId(@PathVariable Long postId) {
        return commentService.getCommentsByPostId(postId);
    }

    /**
     * 根据用户ID获取评论列表
     * @param userId 用户ID
     * @return 评论列表
     */
    @RequestMapping("/getCommentsByUserId/{userId}")
    public Result<List<CommentVO>> getCommentsByUserId(@PathVariable Long userId) {
        return commentService.getCommentsByUserId(userId);
    }

    /**
     * 回复评论
     * @param replyDto 回复信息
     * @return 操作结果
     */
    @PostMapping("/replyPost")
    public Result<CommentVO> replyComment(@RequestBody ReplyDTO replyDto) {
        return commentService.replyComment(replyDto);
    }
}