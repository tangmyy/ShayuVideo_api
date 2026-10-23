package com.zx.cuicuisha.controller;

import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.grace.result.ResponseDataStructureEnum;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.mo.MessageMO;
import com.zx.cuicuisha.repository.MessageRepository;
import com.zx.cuicuisha.service.MsgService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Api(tags = "MsgController 消息功能模块的接口")
@RequestMapping("msg")
@RestController
public class MsgController extends BaseInfoProperties {

   @Autowired
   private MsgService msgService;
   @Autowired
   private MessageRepository messageRepository;

   @ApiOperation(value = "查询所有消息")
   @GetMapping("list")
   public ResponseDataStructureEnum list(@RequestParam String userId, @RequestParam Integer page, @RequestParam Integer pageSize) {
      // mongodb 从0分页，区别于数据库
      if (page == null) {page = COMMON_START_PAGE_ZERO;}
      if (pageSize == null) {pageSize = COMMON_PAGE_SIZE;}
      List<MessageMO> list = msgService.queryList(userId, page, pageSize);
      return ResponseDataStructureEnum.ok(list);
   }

   @ApiOperation(value = "删除消息")
   @DeleteMapping("delete")
   public ResponseDataStructureEnum delete(@RequestParam String msgId, @RequestParam String userId) {
      if (StringUtils.isBlank(msgId) || StringUtils.isBlank(userId)) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      // 查询消息是否存在，并验证消息是否属于该用户
      MessageMO messageMO = messageRepository.findById(msgId).orElse(null);
      if (messageMO == null) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
      }
      // 验证消息是否属于该用户（只能删除自己接收的消息）
      if (!messageMO.getToUserId().equals(userId)) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.NO_AUTH);
      }
      // 使用deleteMsgByCondition删除消息
      // 根据消息类型决定是否需要msgContentKey和msgContentValue
      String msgContentKey = null;
      String msgContentValue = null;
      if (messageMO.getMsgContent() != null) {
         // 如果有commentId，使用commentId匹配
         Object commentId = messageMO.getMsgContent().get("commentId");
         if (commentId != null) {
            msgContentKey = "commentId";
            msgContentValue = commentId.toString();
         } else {
            // 如果有vlogId，使用vlogId匹配
            Object vlogId = messageMO.getMsgContent().get("vlogId");
            if (vlogId != null) {
               msgContentKey = "vlogId";
               msgContentValue = vlogId.toString();
            }
         }
      }
      msgService.deleteMsgByCondition(messageMO.getFromUserId(), messageMO.getToUserId(), messageMO.getMsgType(), msgContentKey, msgContentValue);
      return ResponseDataStructureEnum.ok();
   }

}

