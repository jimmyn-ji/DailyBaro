package com.project.service;

import com.project.model.dto.InsertCommentDTO;
import com.project.model.dto.QueryPostDTO;
import com.project.model.dto.ReplyDTO;
import com.project.model.dto.UpdateCommentDTO;
import com.project.model.vo.CommentVO;
import com.project.model.vo.PostVO;
import com.project.model.vo.ReplyVO;
import com.project.util.Result;

import java.util.List;

public interface CommentService {
    Result<List<PostVO>> selectAllPosts(QueryPostDTO queryPostDTO);
    Result<List<ReplyVO>> getPostById(Long postId);
    Result<CommentVO> replyComment(ReplyDTO replyDTO);
    Result<CommentVO> insertComment(InsertCommentDTO insertCommentDTO);
    Result<CommentVO> updateComment(UpdateCommentDTO updateCommentDTO);
    Result<Void> deleteComment(Long commentId);
    Result<List<CommentVO>> getCommentsByPostId(Long postId);
    Result<List<CommentVO>> getCommentsByUserId(Long userId);
}