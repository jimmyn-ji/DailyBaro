package com.project.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.project.mapper.AnonymousPostMapper;
import com.project.mapper.PostCommentMapper;
import com.project.mapper.PostLikeMapper;
import com.project.model.AnonymousPost;
import com.project.model.PostLike;
import com.project.model.dto.CreateAnonymousPostDTO;
import com.project.model.dto.CreateCommentDTO;
import com.project.model.vo.AnonymousPostVO;
import com.project.model.vo.CommentVO;
import com.project.service.AnonymousPostService;
import com.project.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AnonymousPostServiceImpl implements AnonymousPostService {

    @Autowired
    private AnonymousPostMapper anonymousPostMapper;

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private PostCommentMapper postCommentMapper;
    @Autowired
    private com.project.mapper.CommentMapper commentMapper;

    @Override
    public Result<AnonymousPostVO> createPost(CreateAnonymousPostDTO createDTO, Long userId) {
        log.info("接收到的创建动态数据: content={}, visibility={}", createDTO.getContent(), createDTO.getVisibility());

        AnonymousPost post = new AnonymousPost();
        BeanUtils.copyProperties(createDTO, post);
        post.setUserId(userId);
        post.setCreateTime(new Date());

        log.info("转换后的AnonymousPost对象: userId={}, content={}, visibility={}", post.getUserId(), post.getContent(), post.getVisibility());

        anonymousPostMapper.insert(post);
        log.info("用户 {} 创建了一个新的匿名帖子，ID: {}", userId, post.getPostId());

        return Result.success(convertToVO(post));
    }

    @Override
    @Transactional
    public Result<Void> deletePost(Long postId, Long userId) {
        AnonymousPost post = anonymousPostMapper.selectById(postId);
        if (post == null) {
            return Result.fail("帖子不存在");
        }
        if (!post.getUserId().equals(userId)) {
            return Result.fail("无权删除此帖子");
        }

        // The database schema uses ON DELETE CASCADE,
        // so associated likes and comments will be deleted automatically.
        anonymousPostMapper.deleteById(postId);
        log.info("用户 {} 删除了帖子 {}", userId, postId);
        return Result.success();
    }

    @Override
    public Result<List<AnonymousPostVO>> getPublicPosts(Long currentUserId) {
        // 查询公开动态 + 当前用户的私有动态
        QueryWrapper<AnonymousPost> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
            .eq("visibility", "public")
            .or()
            .eq("visibility", "private").eq("user_id", currentUserId)
        ).orderByDesc("create_time");
        List<AnonymousPost> posts = anonymousPostMapper.selectList(queryWrapper);

        List<AnonymousPostVO> vos = posts.stream()
                .map(post -> convertToVO(post, currentUserId))
                .collect(Collectors.toList());

        return Result.success(vos);
    }

    @Override
    public Result<AnonymousPostVO> getPostById(Long postId, Long userId) {
        AnonymousPost post = anonymousPostMapper.selectById(postId);
        if (post == null) {
            return Result.fail("帖子不存在");
        }
        AnonymousPostVO vo = convertToVO(post, userId);
        // 查询评论
        java.util.List<com.project.model.Comments> comments = commentMapper.getCommentsByPostId(postId);
        vo.setComments(comments);
        return Result.success(vo);
    }

    @Override
    public Result<Void> likePost(Long postId, Long userId) {
        // 检查帖子是否存在
        if (anonymousPostMapper.selectById(postId) == null) {
            return Result.fail("帖子不存在");
        }
        // 检查是否已点赞
        QueryWrapper<PostLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", postId).eq("user_id", userId);
        if (postLikeMapper.selectCount(queryWrapper) > 0) {
            return Result.success(); // 幂等性：重复点赞直接成功
        }

        PostLike like = new PostLike();
        like.setPostId(postId);
        like.setUserId(userId);
        postLikeMapper.insert(like);
        return Result.success();
    }

    @Override
    public Result<Void> unlikePost(Long postId, Long userId) {
        QueryWrapper<PostLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", postId).eq("user_id", userId);
        postLikeMapper.delete(queryWrapper);
        return Result.success();
    }

    @Override
    public Result<List<CommentVO>> getComments(Long postId, Long userId) {
        // 检查帖子是否存在
        if (anonymousPostMapper.selectById(postId) == null) {
            return Result.fail("帖子不存在");
        }
        
        // 获取评论列表
        List<com.project.model.Comments> comments = commentMapper.getCommentsByPostId(postId);
        List<CommentVO> commentVOs = comments.stream()
            .map(this::convertCommentToVO)
            .collect(Collectors.toList());
        
        return Result.success(commentVOs);
    }

    @Override
    public Result<CommentVO> addComment(Long postId, CreateCommentDTO commentDTO, Long userId) {
        // 检查帖子是否存在
        if (anonymousPostMapper.selectById(postId) == null) {
            return Result.fail("帖子不存在");
        }
        
        // 创建评论
        com.project.model.Comments comment = new com.project.model.Comments();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(commentDTO.getContent());
        comment.setCreateTime(new Date());
        
        commentMapper.insert(comment);
        
        return Result.success(convertCommentToVO(comment));
    }

    private CommentVO convertCommentToVO(com.project.model.Comments comment) {
        CommentVO vo = new CommentVO();
        vo.setCommentId(comment.getCommentId());
        vo.setPostId(comment.getPostId());
        vo.setUserId(comment.getUserId());
        vo.setContent(comment.getContent());
        vo.setCreateTime(comment.getCreateTime());
        return vo;
    }

    private AnonymousPostVO convertToVO(AnonymousPost post) {
        return convertToVO(post, null);
    }

    private AnonymousPostVO convertToVO(AnonymousPost post, Long currentUserId) {
        AnonymousPostVO vo = new AnonymousPostVO();
        BeanUtils.copyProperties(post, vo);

        // 获取点赞数
        QueryWrapper<PostLike> likeQuery = new QueryWrapper<>();
        likeQuery.eq("post_id", post.getPostId());
        vo.setLikeCount(postLikeMapper.selectCount(likeQuery).intValue());

        // 获取评论数
        vo.setCommentCount(0); // Placeholder for now

        // 设置当前用户是否已点赞（使用真实的用户ID）
        if (currentUserId != null) {
        QueryWrapper<PostLike> userLikeQuery = new QueryWrapper<>();
            userLikeQuery.eq("post_id", post.getPostId()).eq("user_id", currentUserId);
        vo.setLiked(postLikeMapper.selectCount(userLikeQuery) > 0);
        } else {
            vo.setLiked(false); // 如果用户未登录，默认未点赞
        }

        return vo;
    }
} 