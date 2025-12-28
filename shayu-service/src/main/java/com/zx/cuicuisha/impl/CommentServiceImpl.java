package com.zx.cuicuisha.impl;

import com.github.pagehelper.PageHelper;
import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.base.RabbitMQConfig;
import com.zx.cuicuisha.bo.CommentBO;
import com.zx.cuicuisha.bo.CommentUpdateBO;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
      // 构造 CommentBO 信息
      String commentId = sid.nextShort();
      Comment comment = new Comment();
      comment.setId(commentId);
      comment.setVlogId(commentBO.getVlogId());
      comment.setVlogerId(commentBO.getVlogerId());
      comment.setCommentUserId(commentBO.getCommentUserId());
      comment.setFatherCommentId(commentBO.getFatherCommentId());
      comment.setContent(commentBO.getContent());
      comment.setLikeCounts(0);
      comment.setIsDeleted(0); // 默认未逻辑删除
      comment.setCreateTime(new Date());
      // 插入数据
      commentMapper.insert(comment);
      // redis中累加
      redis.increment(REDIS_VLOG_COMMENT_COUNTS + ":" + commentBO.getVlogId(), 1);
      // 留言后的最新评论需要返回给前端进行展示
      CommentVO commentVO = new CommentVO();
      BeanUtils.copyProperties(comment, commentVO);
      // 系统消息：评论/回复
      // 检查视频是否存在且未被逻辑删除
      Vlog vlog = vlogService.getVlog(commentBO.getVlogId());
      if (vlog == null) {
         GraceException.display(ResponseStatusEnum.VLOG_NOT_EXIST_ERROR);
      }
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
      // 检查评论是否存在且未被逻辑删除
      Comment existComment = commentMapper.selectByPrimaryKey(commentId);
      if (existComment == null) {
         GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
      }
      // 验证评论是否属于该用户
      if (!existComment.getCommentUserId().equals(commentUserId)) {
         GraceException.display(ResponseStatusEnum.NO_AUTH);
      }
      // 检查是否已被逻辑删除
      if (existComment.getIsDeleted() != null && existComment.getIsDeleted() == 1) {
         GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
      }
      // 逻辑删除评论
      Comment pendingDelete = new Comment();
      pendingDelete.setId(commentId);
      pendingDelete.setIsDeleted(1);
      commentMapper.updateByPrimaryKeySelective(pendingDelete);
      // redis中累减
      redis.decrement(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId, 1);
   }

   @Override
   public Comment getComment(String id) {
      Comment comment = commentMapper.selectByPrimaryKey(id);
      // 管理员查询可以返回已逻辑删除的评论，所以这里不过滤
      return comment;
   }

   /**
    * 删除/恢复评论（切换逻辑删除状态）
    * 如果评论是正常状态（is_deleted = 0），则逻辑删除（设置为 1）
    * 如果评论是已逻辑删除状态（is_deleted = 1），则恢复（设置为 0）
    */
   @Transactional
   @Override
   public Comment deleteOrRestoreComment(String commentId) {
      // 管理员可以直接查询评论，不过滤 is_deleted
      Comment comment = commentMapper.selectByPrimaryKey(commentId);
      if (comment == null) {
         GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
      }
      // 切换逻辑删除状态
      Integer currentIsDeleted = comment.getIsDeleted();
      Integer newIsDeleted = (currentIsDeleted == null || currentIsDeleted == 0) ? 1 : 0;
      Comment updateComment = new Comment();
      updateComment.setId(commentId);
      updateComment.setIsDeleted(newIsDeleted);
      int result = commentMapper.updateByPrimaryKeySelective(updateComment);
      if (result != 1) {
         GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
      }
      // 更新视频的评论数（Redis缓存）
      String vlogId = comment.getVlogId();
      if (StringUtils.isNotBlank(vlogId)) {
         try {
            if (newIsDeleted == 1) {
               // 删除操作：评论数 -1
               redis.decrement(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId, 1);
               log.info("删除评论，减少视频评论数：commentId={}, vlogId={}", commentId, vlogId);
            } else {
               // 恢复操作：评论数 +1
               redis.increment(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId, 1);
               log.info("恢复评论，增加视频评论数：commentId={}, vlogId={}", commentId, vlogId);
            }
         } catch (Exception e) {
            log.error("更新视频评论数缓存失败：commentId={}, vlogId={}", commentId, vlogId, e);
            // 缓存更新失败不影响主流程，只记录日志
         }
      }
      // 返回更新后的评论信息
      return commentMapper.selectByPrimaryKey(commentId);
   }

   /**
    * 获取所有视频评论的总数
    * 通过遍历Redis中所有匹配的key，累加评论数量
    * @return 所有视频评论的总数
    */
   @Override
   public Integer getTotalCommentCount() {
      try {
         // 获取所有匹配的Redis key
         String pattern = REDIS_VLOG_COMMENT_COUNTS + ":*";
         Set<String> keys = redis.keys(pattern);
         if (keys == null || keys.isEmpty()) {
            log.info("未找到任何视频评论的Redis key");
            return 0;
         }
         // 累加所有key的值
         int totalCount = 0;
         for (String key : keys) {
            String countStr = redis.get(key);
            if (StringUtils.isNotBlank(countStr)) {
               try {
                  int count = Integer.parseInt(countStr);
                  totalCount += count;
               } catch (NumberFormatException e) {
                  log.warn("Redis key {} 的值 {} 不是有效的数字，跳过", key, countStr);
               }
            }
         }
         log.info("统计所有视频评论总数完成，共找到 {} 个视频，总评论数：{}", keys.size(), totalCount);
         return totalCount;
      } catch (Exception e) {
         log.error("获取所有视频评论总数时发生错误", e);
         // 发生异常时返回0，避免影响接口调用
         return 0;
      }
   }

   /**
    * 管理员更新评论内容
    * @param commentUpdateBO 评论更新对象，包含评论ID和新的评论内容
    * @return 更新后的评论对象
    */
   @Transactional
   @Override
   public Comment updateCommentForAdmin(CommentUpdateBO commentUpdateBO) {
      // 参数校验
      if (commentUpdateBO == null || StringUtils.isBlank(commentUpdateBO.getId())) {
         GraceException.display(ResponseStatusEnum.SYSTEM_ERROR);
      }
      if (StringUtils.isBlank(commentUpdateBO.getContent())) {
         GraceException.display(ResponseStatusEnum.SYSTEM_ERROR);
      }
      // 查询原评论
      Comment existComment = commentMapper.selectByPrimaryKey(commentUpdateBO.getId());
      if (existComment == null) {
         log.warn("评论不存在：commentId={}", commentUpdateBO.getId());
         GraceException.display(ResponseStatusEnum.COMMENT_NOT_EXIST_ERROR);
      }
      // 更新评论内容
      Comment updateComment = new Comment();
      updateComment.setId(commentUpdateBO.getId());
      updateComment.setContent(commentUpdateBO.getContent());
      int result = commentMapper.updateByPrimaryKeySelective(updateComment);
      if (result != 1) {
         log.error("更新评论失败：commentId={}", commentUpdateBO.getId());
         GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
      }
      // 返回更新后的评论信息
      Comment updatedComment = commentMapper.selectByPrimaryKey(commentUpdateBO.getId());
      log.info("管理员更新评论成功：commentId={}, newContent={}", commentUpdateBO.getId(), commentUpdateBO.getContent());
      return updatedComment;
   }

}
