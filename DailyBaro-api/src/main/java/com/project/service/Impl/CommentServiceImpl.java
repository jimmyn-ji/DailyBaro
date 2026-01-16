package com.project.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.project.mapper.CommentMapper;
import com.project.model.Comments;
import com.project.model.dto.InsertCommentDTO;
import com.project.model.dto.QueryPostDTO;
import com.project.model.dto.ReplyDTO;
import com.project.model.dto.UpdateCommentDTO;
import com.project.model.vo.CommentVO;
import com.project.model.vo.PostVO;
import com.project.model.vo.ReplyVO;
import com.project.service.CommentService;
import com.project.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    // 静态变量，用于记录当前的 postId，初始值为 2
    private static long currentPostId = 2;

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public Result<List<PostVO>> selectAllPosts(QueryPostDTO queryPostDTO) {
        // 1. 查询所有评论，按 create_time 升序排序
        QueryWrapper<Comments> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("create_time");
        List<Comments> commentsList = commentMapper.selectList(queryWrapper);

        // 2. 拆分评论为主评论（Post）和回复（Reply）
        Map<Long, PostVO> postMap = new HashMap<>();
        for (Comments comment : commentsList) {
            Long postId = comment.getPostId();
            PostVO postVO = postMap.computeIfAbsent(postId, k -> {
                PostVO newPost = new PostVO();
                newPost.setPostId(postId);
                newPost.setUserId(comment.getUserId());
                newPost.setContent(comment.getContent());
                newPost.setCreateTime(comment.getCreateTime());
                newPost.setReplies(new ArrayList<>());
                return newPost;
            });

            if (!Objects.equals(postVO.getCreateTime(), comment.getCreateTime())) {
                // 不是主评论，作为回复处理
                ReplyVO replyVO = new ReplyVO();
                replyVO.setPostId(postId);
                replyVO.setUserId(comment.getUserId());
                replyVO.setContent(comment.getContent());
                replyVO.setCreateTime(comment.getCreateTime());
                postVO.getReplies().add(replyVO);
            }
        }

        // 3. 按照创建时间返回主评论列表
        List<PostVO> postList = new ArrayList<>(postMap.values());
        postList.sort(Comparator.comparing(PostVO::getCreateTime));

        return Result.success(postList);
    }

    @Override
    public Result<List<ReplyVO>> getPostById(Long postId) {
        List<ReplyVO> repliesByPostId = commentMapper.getRepliesByPostId(postId);

        if (repliesByPostId == null) {
            return Result.fail("暂无回帖");
        }
        List<ReplyVO> replies = commentMapper.getRepliesByPostId(postId);
//        post.setReplies(replies);
        return Result.success(replies);
    }

    @Override
    public Result<CommentVO> insertComment(InsertCommentDTO insertCommentDTO) {
        log.info("接收到的评论数据: {}", insertCommentDTO);
        
        Comments comments = new Comments();
        BeanUtils.copyProperties(insertCommentDTO, comments);
        
        log.info("转换后的Comments对象: {}", comments);

        // 确保 postId 不为空
        if (comments.getPostId() == null) {
            log.error("postId 为空，原始数据: {}", insertCommentDTO);
            return Result.fail("postId 不能为空");
        }

        // 插入评论
        commentMapper.insertComment(comments);

        return Result.success(convertToVO(comments));
    }

    @Override
    public Result<CommentVO> updateComment(UpdateCommentDTO updateCommentDTO) {
        // 若传入的 commentId 为 null 或 0，视为发表新的回复（插入操作）
        if (updateCommentDTO.getCommentId() == null || updateCommentDTO.getCommentId() == 0) {
            Comments comments = new Comments();
            BeanUtils.copyProperties(updateCommentDTO, comments);
            commentMapper.insertComment(comments);
            return Result.success(convertToVO(comments));
        }
        Comments comments = commentMapper.getCommentById(updateCommentDTO.getCommentId());
        if (comments == null) {
            return Result.fail("评论不存在");
        }
        BeanUtils.copyProperties(updateCommentDTO, comments);
        commentMapper.updateComment(comments);
        return Result.success(convertToVO(comments));
    }


    @Override
    public Result<Void> deleteComment(Long commentId) {
        commentMapper.deleteComment(commentId);
        return Result.success();
    }

    @Override
    public Result<List<CommentVO>> getCommentsByPostId(Long postId) {
        List<Comments> comments = commentMapper.getCommentsByPostId(postId);
        List<CommentVO> vos = comments.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(vos);
    }

    @Override
    public Result<List<CommentVO>> getCommentsByUserId(Long userId) {
        List<Comments> comments = commentMapper.getCommentsByUserId(userId);
        List<CommentVO> vos = comments.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(vos);
    }

    @Override
    public Result<CommentVO> replyComment(ReplyDTO replyDTO) {
        Comments comments = new Comments();
        BeanUtils.copyProperties(replyDTO, comments);
        comments.setIsReply(1); // 标记为回复
        commentMapper.insertComment(comments);

        // 更新被回复评论的回复数
        commentMapper.incrementReplyCount(replyDTO.getCommentId());

        return Result.success(convertToVO(comments));
    }

    private CommentVO convertToVO(Comments comments) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comments, vo);
        return vo;
    }
}