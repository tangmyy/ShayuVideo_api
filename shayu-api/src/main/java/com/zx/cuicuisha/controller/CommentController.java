package com.zx.cuicuisha.controller;

import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.base.RabbitMQConfig;
import com.zx.cuicuisha.bo.CommentBO;
import com.zx.cuicuisha.enums.MessageTypeEnum;
import com.zx.cuicuisha.grace.result.ResponseDataStructureEnum;
import com.zx.cuicuisha.mo.MessageMO;
import com.zx.cuicuisha.pojo.Comment;
import com.zx.cuicuisha.pojo.Vlog;
import com.zx.cuicuisha.service.CommentService;
import com.zx.cuicuisha.service.MsgService;
import com.zx.cuicuisha.service.VlogService;
import com.zx.cuicuisha.utils.JsonUtils;
import com.zx.cuicuisha.vo.CommentVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Api(tags = "CommentController 评论模块的接口")
@RequestMapping("comment")
@RestController
public class CommentController extends BaseInfoProperties {

   @Autowired
   private CommentService commentService;
   @Autowired
   private VlogService vlogService;
   @Autowired
   private MsgService msgService;
   @Autowired
   public RabbitTemplate rabbitTemplate;

   @ApiOperation(value = "发表评论")
   @PostMapping("create")
   public ResponseDataStructureEnum create(@RequestBody @Valid CommentBO commentBO) throws Exception {
      /**
       * TODO:
       * fatherCommentId不为空且不为“0”的时候，
       * 校验上一条评论是否真实存在，不存在抛出异常
       */

      /**
       * TODO:
       * vlogerId 和 commentUserId 都是对应用户的主键，
       * 需要校验是否真实存在用户，不存在抛出异常
       * 进阶：封装统一方法专门用于校验，[ checkUserExist(userId) ]
       */
      CommentVO commentVO = commentService.createComment(commentBO);
      return ResponseDataStructureEnum.ok(commentVO);
   }

   @GetMapping("counts")
   public ResponseDataStructureEnum counts(@RequestParam String vlogId) {
      String countsStr = redis.get(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId);
      if (StringUtils.isBlank(countsStr)) {
         countsStr = "0";
      }
      return ResponseDataStructureEnum.ok(Integer.valueOf(countsStr));
   }

   @GetMapping("list")
   public ResponseDataStructureEnum list(@RequestParam String vlogId, @RequestParam(defaultValue = "") String userId,
                                         @RequestParam Integer page, @RequestParam Integer pageSize) {
      return ResponseDataStructureEnum.ok(commentService.queryVlogComments(vlogId, userId, page, pageSize));
   }

   @DeleteMapping("delete")
   public ResponseDataStructureEnum delete(@RequestParam String commentUserId, @RequestParam String commentId, @RequestParam String vlogId) {
      commentService.deleteComment(commentUserId, commentId, vlogId);
      return ResponseDataStructureEnum.ok();
   }

   @PostMapping("like")
   public ResponseDataStructureEnum like(@RequestParam String commentId, @RequestParam String userId) {
      // 1. 更新Redis点赞数（核心业务）
      // TODO： 优化bigkey
      redis.incrementHash(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId, 1);
      // redis.hset(REDIS_USER_LIKE_COMMENT, userId, "1");        hset = setHashValue
      // TODO：优化value="1" 为枚举类中的值 再进行传参
      redis.setHashValue(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId, "1");
      // 2. 准备消息内容 系统消息：点赞评论
      Comment comment = commentService.getComment(commentId);
      Vlog vlog = vlogService.getVlog(comment.getVlogId());
      Map msgContent = new HashMap();
      msgContent.put("vlogId", vlog.getId());
      msgContent.put("vlogCover", vlog.getCover());
      msgContent.put("commentId", commentId);
      // MQ异步解耦
      // msgService.createMsg(userId, comment.getCommentUserId(), MessageTypeEnum.LIKE_COMMENT.type, msgContent);
      // 3. 构建消息对象
      MessageMO messageMO = new MessageMO();
      messageMO.setFromUserId(userId);                    // 点赞者ID
      messageMO.setToUserId(comment.getCommentUserId());  // 被点赞评论的作者ID
      messageMO.setMsgContent(msgContent);                // 消息内容
      // 4. 发送到MQ（异步，立即返回，不阻塞）
      rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_MSG,                    // 交换机
            "sys.msg." + MessageTypeEnum.LIKE_COMMENT.enValue,  // 路由键
            JsonUtils.objectToJson(messageMO)               // 消息体（JSON字符串）
      );
      return ResponseDataStructureEnum.ok();
   }

   @PostMapping("unlike")
   public ResponseDataStructureEnum unlike(@RequestParam String commentId, @RequestParam String userId) {
      // 1. 更新Redis点赞数（核心业务）
      redis.decrementHash(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId, 1);
      // redis.hdel(REDIS_USER_LIKE_COMMENT, userId, "1");        hdel = deleteHash
      redis.deleteHash(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId);
      // 2. 删除点赞评论消息
      try {
         Comment comment = commentService.getComment(commentId);
         if (comment != null) {
            // 删除消息（根据 commentId 匹配 msgContent）
            msgService.deleteMsgByCondition(userId, comment.getCommentUserId(), MessageTypeEnum.LIKE_COMMENT.type, "commentId", commentId);
            log.info("删除点赞评论消息成功：userId={}, commentId={}", userId, commentId);
         }
      } catch (Exception e) {
         log.error("删除点赞评论消息失败：userId={}, commentId={}", userId, commentId, e);
         // 删除消息失败不影响主流程，只记录日志
      }
      return ResponseDataStructureEnum.ok();
   }

}

