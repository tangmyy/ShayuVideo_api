package com.zx.cuicuisha.impl;

import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.bo.CommentBO;
import com.zx.cuicuisha.mapper.CommentMapper;
import com.zx.cuicuisha.pojo.Comment;
import com.zx.cuicuisha.service.CommentService;
import com.zx.cuicuisha.utils.PagedGridResult;
import com.zx.cuicuisha.vo.CommentVO;
import lombok.extern.slf4j.Slf4j;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Slf4j
@Repository
public class CommentServiceImpl extends BaseInfoProperties implements CommentService {

   @Autowired
   private CommentMapper commentMapper;

   @Autowired
   private Sid sid;

   @Override
   public CommentVO createComment(CommentBO commentBO) {
      String commentId = sid.nextShort();

      Comment comment = new Comment();
      comment.setId(commentId);
      comment.setVlogId(commentBO.getVlogId());
      comment.setVlogerId(commentBO.getVlogerId());
      comment.setCommentUserId(commentBO.getCommentUserId());
      comment.setFatherCommentId(commentBO.getFatherCommentId());
      comment.setContent(commentBO.getContent());
      comment.setLikeCounts(0);
      comment.setCreateTime(new Date());
      commentMapper.insert(comment);
      redis.increment(REDIS_VLOG_COMMENT_COUNTS + ":" + commentBO.getVlogId(), 1);
      // 留言后的最新评论需要返回给前端进行展示
      CommentVO commentVO = new CommentVO();
      BeanUtils.copyProperties(comment, commentVO);
      return commentVO;
   }

   @Override
   public PagedGridResult queryVlogComments(String vlogId, String userId, Integer page, Integer pageSize) {
      return null;
   }

   @Override
   public void deleteComment(String commentUserId, String commentId, String vlogId) {

   }

   @Override
   public Comment getComment(String id) {
      return null;
   }
}
