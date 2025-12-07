package com.zx.cuicuisha.controller;

import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.bo.CommentBO;
import com.zx.cuicuisha.grace.result.ResponseDataStructureEnum;
import com.zx.cuicuisha.service.CommentService;
import com.zx.cuicuisha.service.VlogService;
import com.zx.cuicuisha.vo.CommentVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@Api(tags = "CommentController 评论模块的接口")
@RequestMapping("comment")
@RestController
public class CommentController extends BaseInfoProperties {

   @Autowired
   private CommentService commentService;
   // @Autowired
   // private MsgService msgService;
   @Autowired
   private VlogService vlogService;

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
   //
   // @GetMapping("list")
   // public ResponseDataStructureEnum list(@RequestParam String vlogId, @RequestParam(defaultValue = "") String userId,
   //                                       @RequestParam Integer page, @RequestParam Integer pageSize) {
   //
   //    return ResponseDataStructureEnum.ok(
   //          commentService.queryVlogComments(
   //                vlogId,
   //                userId,
   //                page,
   //                pageSize));
   // }
   //
   // @DeleteMapping("delete")
   // public ResponseDataStructureEnum delete(@RequestParam String commentUserId, @RequestParam String commentId, @RequestParam String vlogId) {
   //    commentService.deleteComment(commentUserId,
   //          commentId,
   //          vlogId);
   //    return ResponseDataStructureEnum.ok();
   // }
   //
   // @Autowired
   // public RabbitTemplate rabbitTemplate;
   //
   // @PostMapping("like")
   // public ResponseDataStructureEnum like(@RequestParam String commentId, @RequestParam String userId) {
   //
   //    // 故意犯错，bigkey
   //    redis.incrementHash(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId, 1);
   //    redis.setHashValue(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId, "1");
   //    //        redis.hset(REDIS_USER_LIKE_COMMENT, userId, "1");
   //
   //
   //    // 系统消息：点赞评论
   //    Comment comment = commentService.getComment(commentId);
   //    Vlog vlog = vlogService.getVlog(comment.getVlogId());
   //    Map msgContent = new HashMap();
   //    msgContent.put("vlogId", vlog.getId());
   //    msgContent.put("vlogCover", vlog.getCover());
   //    msgContent.put("commentId", commentId);
   //    //        msgService.createMsg(userId,
   //    //                            comment.getCommentUserId(),
   //    //                            MessageEnum.LIKE_COMMENT.type,
   //    //                            msgContent);
   //
   //    // MQ异步解耦
   //    MessageMO messageMO = new MessageMO();
   //    messageMO.setFromUserId(userId);
   //    messageMO.setToUserId(comment.getCommentUserId());
   //    messageMO.setMsgContent(msgContent);
   //    rabbitTemplate.convertAndSend(
   //          RabbitMQConfig.EXCHANGE_MSG,
   //          "sys.msg." + MessageEnum.LIKE_COMMENT.enValue,
   //          JsonUtils.objectToJson(messageMO));
   //
   //    return ResponseDataStructureEnum.ok();
   // }
   //
   // @PostMapping("unlike")
   // public ResponseDataStructureEnum unlike(@RequestParam String commentId, @RequestParam String userId) {
   //    redis.decrementHash(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId, 1);
   //    redis.hdel(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId);
   //    return ResponseDataStructureEnum.ok();
   // }
}

