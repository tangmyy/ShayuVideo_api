package com.zx.cuicuisha.impl;

import com.github.pagehelper.PageHelper;
import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.base.RabbitMQConfig;
import com.zx.cuicuisha.bo.CommentBO;
import com.zx.cuicuisha.enums.MessageEnum;
import com.zx.cuicuisha.enums.YesOrNo;
import com.zx.cuicuisha.exceptions.GraceException;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.mapper.CommentMapper;
import com.zx.cuicuisha.mapper.CommentMapperCustom;
import com.zx.cuicuisha.mapper.UsersMapper;
import com.zx.cuicuisha.mo.MessageMO;
import com.zx.cuicuisha.pojo.Comment;
import com.zx.cuicuisha.pojo.Users;
import com.zx.cuicuisha.pojo.Vlog;
import com.zx.cuicuisha.service.CommentService;
import com.zx.cuicuisha.service.MsgService;
import com.zx.cuicuisha.service.VlogService;
import com.zx.cuicuisha.utils.JsonUtils;
import com.zx.cuicuisha.utils.PagedGridResult;
import com.zx.cuicuisha.vo.CommentAdminVO;
import com.zx.cuicuisha.vo.CommentVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CommentServiceImpl extends BaseInfoProperties implements CommentService {

   @Autowired
   private CommentMapper commentMapper;
   @Autowired
   private CommentMapperCustom commentMapperCustom;
   @Autowired
   private UsersMapper usersMapper;
   @Autowired
   private Sid sid;
   @Autowired
   private VlogService vlogService;
   @Autowired
   private MsgService msgService;
   @Autowired
   public RabbitTemplate rabbitTemplate;

   @Override
   public CommentVO createComment(CommentBO commentBO) {
      // 检查用户是否被封禁
      Users user = usersMapper.selectByPrimaryKey(commentBO.getCommentUserId());
      if (user == null) {
         GraceException.display(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
      }
      if (user.getStatus() != null && user.getStatus() == 1) {
         GraceException.display(ResponseStatusEnum.USER_FROZEN);
      }
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

      // 系统消息：评论/回复
      Vlog vlog = vlogService.getVlog(commentBO.getVlogId());
      Map msgContent = new HashMap();
      msgContent.put("vlogId", vlog.getId());
      msgContent.put("vlogCover", vlog.getCover());
      msgContent.put("commentId", commentId);
      msgContent.put("commentContent", commentBO.getContent());
      Integer type = MessageEnum.COMMENT_VLOG.type;
      String routeType = MessageEnum.COMMENT_VLOG.enValue;
      if (StringUtils.isNotBlank(commentBO.getFatherCommentId()) && !commentBO.getFatherCommentId().equalsIgnoreCase("0")) {
         type = MessageEnum.REPLY_YOU.type;
         routeType = MessageEnum.REPLY_YOU.enValue;
      }
      // msgService.createMsg(commentBO.getCommentUserId(), commentBO.getVlogerId(), type, msgContent);
      // MQ异步解耦
      MessageMO messageMO = new MessageMO();
      messageMO.setFromUserId(commentBO.getCommentUserId());
      messageMO.setToUserId(commentBO.getVlogerId());
      messageMO.setMsgContent(msgContent);
      rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG, "sys.msg." + routeType, JsonUtils.objectToJson(messageMO));
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

   /**
    * 查询所有评论（管理端使用）
    * @param vlogId 视频ID（可选，如果为空则不按视频筛选）
    * @param userId 评论者ID（可选，如果为空则不按用户筛选）
    * @param page 页码
    * @param pageSize 每页数量
    * @return 分页结果
    */
   @Override
   public PagedGridResult queryAllComments(String vlogId, String userId, Integer page, Integer pageSize) {
      Map<String, Object> map = new HashMap<>();
      if (StringUtils.isNotBlank(vlogId)) {
         map.put("vlogId", vlogId);
      }
      if (StringUtils.isNotBlank(userId)) {
         map.put("userId", userId);
      }
      PageHelper.startPage(page, pageSize);
      List<CommentAdminVO> list = commentMapperCustom.getAllComments(map);
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
