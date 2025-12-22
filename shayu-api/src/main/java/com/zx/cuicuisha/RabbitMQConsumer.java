package com.zx.cuicuisha;

import com.zx.cuicuisha.base.RabbitMQConfig;
import com.zx.cuicuisha.enums.MessageEnum;
import com.zx.cuicuisha.exceptions.GraceException;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.mo.MessageMO;
import com.zx.cuicuisha.service.MsgService;
import com.zx.cuicuisha.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 *
 * 创建消息消费者
 *
 */
@Slf4j
@Component
public class RabbitMQConsumer {

   @Autowired
   private MsgService msgService;

   // 监听队列，异步消费消息
   @RabbitListener(queues = {RabbitMQConfig.QUEUE_SYS_MSG})
   public void watchQueue(String payload, Message message) {
      log.info(payload);
      // 1. 将JSON字符串转换为MessageMO对象
      MessageMO messageMO = JsonUtils.jsonToPojo(payload, MessageMO.class);
      // 2. 获取路由键，用于判断消息类型
      String routingKey = message.getMessageProperties().getReceivedRoutingKey();
      log.info(routingKey);
      // 3. 根据路由键判断消息类型，调用对应的处理逻辑
      // 参数优化 and 枚举的判断优化
      if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.FOLLOW_YOU.enValue)) {
         msgService.createMsg(messageMO.getFromUserId(),
               messageMO.getToUserId(),
               MessageEnum.FOLLOW_YOU.type,
               null);
      } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.LIKE_VLOG.enValue)) {
         msgService.createMsg(messageMO.getFromUserId(),
               messageMO.getToUserId(),
               MessageEnum.LIKE_VLOG.type,
               messageMO.getMsgContent());
      } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.COMMENT_VLOG.enValue)) {
         msgService.createMsg(messageMO.getFromUserId(),
               messageMO.getToUserId(),
               MessageEnum.COMMENT_VLOG.type,
               messageMO.getMsgContent());
      } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.REPLY_YOU.enValue)) {
         msgService.createMsg(messageMO.getFromUserId(),
               messageMO.getToUserId(),
               MessageEnum.REPLY_YOU.type,
               messageMO.getMsgContent());
      } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.LIKE_COMMENT.enValue)) {
         msgService.createMsg(messageMO.getFromUserId(),
               messageMO.getToUserId(),
               MessageEnum.LIKE_COMMENT.type,
               messageMO.getMsgContent());
      } else {
         GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
      }
   }

}

