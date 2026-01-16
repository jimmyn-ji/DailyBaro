package com.project.service;

import com.project.model.dto.CreateAnonymousPostDTO;
import com.project.model.dto.CreateCommentDTO;
import com.project.model.vo.AnonymousPostVO;
import com.project.model.vo.CommentVO;
import com.project.util.Result;
import java.util.List;

public interface AnonymousPostService {

    Result<AnonymousPostVO> createPost(CreateAnonymousPostDTO createDTO, Long userId);

    Result<Void> deletePost(Long postId, Long userId);

    Result<List<AnonymousPostVO>> getPublicPosts(Long currentUserId);

    Result<AnonymousPostVO> getPostById(Long postId, Long userId);

    Result<Void> likePost(Long postId, Long userId);

    Result<Void> unlikePost(Long postId, Long userId);

    Result<List<CommentVO>> getComments(Long postId, Long userId);

    Result<CommentVO> addComment(Long postId, CreateCommentDTO commentDTO, Long userId);
} 