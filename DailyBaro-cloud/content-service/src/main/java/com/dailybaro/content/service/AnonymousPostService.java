package com.dailybaro.content.service;

import com.dailybaro.common.dto.CreateAnonymousPostDTO;
import com.dailybaro.common.vo.AnonymousPostVO;
import com.dailybaro.common.vo.CommentVO;
import com.dailybaro.common.util.Result;
import java.util.List;

public interface AnonymousPostService {
    Result<AnonymousPostVO> createPost(CreateAnonymousPostDTO createDTO, Long userId);
    Result<Void> deletePost(Long postId, Long userId);
    Result<List<AnonymousPostVO>> getPublicPosts(Long currentUserId);
    Result<AnonymousPostVO> getPostById(Long postId, Long userId);
    Result<Void> likePost(Long postId, Long userId);
    Result<Void> unlikePost(Long postId, Long userId);
    Result<List<CommentVO>> getComments(Long postId, Long userId);
    Result<CommentVO> addComment(Long postId, String content, Long userId);
} 