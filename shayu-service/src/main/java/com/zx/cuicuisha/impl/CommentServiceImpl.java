package com.zx.cuicuisha.impl;

import com.github.pagehelper.PageHelper;
import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.bo.CommentBO;
import com.zx.cuicuisha.enums.YesOrNo;
import com.zx.cuicuisha.mapper.CommentMapper;
import com.zx.cuicuisha.mapper.CommentMapperCustom;
import com.zx.cuicuisha.pojo.Comment;
import com.zx.cuicuisha.service.CommentService;
import com.zx.cuicuisha.utils.PagedGridResult;
import com.zx.cuicuisha.vo.CommentVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class CommentServiceImpl extends BaseInfoProperties implements CommentService {

   @Autowired
   private CommentMapper commentMapper;
   @Autowired
   private CommentMapperCustom commentMapperCustom;

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
      // 插入数据
      commentMapper.insert(comment);
      // redis中累加
      redis.increment(REDIS_VLOG_COMMENT_COUNTS + ":" + commentBO.getVlogId(), 1);
      // 留言后的最新评论需要返回给前端进行展示
      CommentVO commentVO = new CommentVO();
      BeanUtils.copyProperties(comment, commentVO);
      return commentVO;
   }

   @Override
   public PagedGridResult queryVlogComments(String vlogId, String userId, Integer page, Integer pageSize) {
      Map<String, Object> map = new HashMap<>();
      map.put("vlogId", vlogId);
      PageHelper.startPage(page, pageSize);
      List<CommentVO> list = commentMapperCustom.getCommentList(map);
      // TODO：优化 抽象封装两个方法
      for (CommentVO commentVO:list) {
         String commentId = commentVO.getCommentId();
         // 当前短视频的某个评论的点赞总数
         String countsStr = redis.getHashValue(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId);
         Integer counts = 0;
         if (StringUtils.isNotBlank(countsStr)) {
            counts = Integer.valueOf(countsStr);
         }
         commentVO.setLikeCounts(counts);
         // 判断当前用户是否点赞过该评论
         String doILike = redis.hget(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId);
         if (StringUtils.isNotBlank(doILike) && doILike.equalsIgnoreCase("1")) {
            commentVO.setIsLike(YesOrNo.YES.type);
         }
      }
      return setterPagedGrid(list, page);
   }

   @Override
   public void deleteComment(String commentUserId, String commentId, String vlogId) {
      Comment pendingDelete = new Comment();
      pendingDelete.setId(commentId);
      pendingDelete.setCommentUserId(commentUserId);
      // 从数据库删除
      commentMapper.delete(pendingDelete);
      // redis中累减
      redis.decrement(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId, 1);
   }

   @Override
   public Comment getComment(String id) {
      return commentMapper.selectByPrimaryKey(id);
   }
}
