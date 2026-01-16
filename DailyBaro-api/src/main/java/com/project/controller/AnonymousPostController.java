package com.project.controller;

import com.project.model.dto.CreateAnonymousPostDTO;
import com.project.model.dto.CreateCommentDTO;
import com.project.model.vo.AnonymousPostVO;
import com.project.model.vo.CommentVO;
import com.project.service.AnonymousPostService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/anonymous-posts")
public class AnonymousPostController {

    @Autowired
    private AnonymousPostService anonymousPostService;

    @PostMapping
    public Result<AnonymousPostVO> createPost(@RequestBody CreateAnonymousPostDTO createDTO, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return anonymousPostService.createPost(createDTO, userId);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable("id") Long postId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return anonymousPostService.deletePost(postId, userId);
    }

    @GetMapping
    public Result<List<AnonymousPostVO>> getPublicPosts(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return anonymousPostService.getPublicPosts(userId);
    }

    @GetMapping("/{id}")
    public Result<AnonymousPostVO> getPostById(@PathVariable("id") Long postId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return anonymousPostService.getPostById(postId, userId);
    }

    @PostMapping("/{id}/like")
    public Result<Void> likePost(@PathVariable("id") Long postId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return anonymousPostService.likePost(postId, userId);
    }

    @DeleteMapping("/{id}/like")
    public Result<Void> unlikePost(@PathVariable("id") Long postId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return anonymousPostService.unlikePost(postId, userId);
    }

    @GetMapping("/{id}/comments")
    public Result<List<CommentVO>> getComments(@PathVariable("id") Long postId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return anonymousPostService.getComments(postId, userId);
    }

    @PostMapping("/{id}/comments")
    public Result<CommentVO> addComment(@PathVariable("id") Long postId, @RequestBody CreateCommentDTO commentDTO, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return anonymousPostService.addComment(postId, commentDTO, userId);
    }

    /**
     * 从请求头中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String uidHeader = request.getHeader("uid");
        if (uidHeader != null && !uidHeader.trim().isEmpty()) {
            try {
                return Long.parseLong(uidHeader.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
} 