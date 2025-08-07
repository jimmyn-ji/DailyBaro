package com.dailybaro.anonymous.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailybaro.anonymous.model.Comments;
import com.dailybaro.anonymous.model.dto.QueryPostDTO;
import com.dailybaro.anonymous.model.vo.PostVO;
import com.dailybaro.anonymous.model.vo.ReplyVO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comments> {
    List<PostVO> selectAllPosts(QueryPostDTO queryPostDTO);
    PostVO getPostById(Long postId);
    List<ReplyVO> getRepliesByPostId(Long postId);
    Comments getCommentById(Long commentId);
    int insertComment(Comments comments);
    int updateComment(Comments comments);
    int deleteComment(Long commentId);
    List<Comments> getCommentsByPostId(Long postId);
    List<Comments> getCommentsByUserId(Long userId);
    int incrementReplyCount(Long commentId);
} 