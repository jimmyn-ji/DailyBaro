package com.dailybaro.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailybaro.content.model.Comments;
import com.dailybaro.common.dto.QueryPostDTO;
import com.dailybaro.common.vo.PostVO;
import com.dailybaro.common.vo.ReplyVO;
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