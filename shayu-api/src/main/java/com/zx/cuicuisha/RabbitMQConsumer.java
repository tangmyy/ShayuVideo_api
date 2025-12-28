package com.zx.cuicuisha;

import com.zx.cuicuisha.base.RabbitMQConfig;
import com.zx.cuicuisha.enums.MessageTypeEnum;
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

import java.util.Map;


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
      // 3. 根据路由键获取对应的消息枚举（优化后的枚举判断）
      MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByRoutingKey(routingKey);
      if (messageTypeEnum == null) {
         log.error("未知的路由键：{}", routingKey);
         GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
         return;
      }
      // 4. 根据消息类型决定是否传递msgContent
      // FOLLOW_YOU 类型的消息不需要msgContent，其他类型需要
      Map<String, Object> msgContent;
      if (messageTypeEnum == MessageTypeEnum.FOLLOW_YOU) {
         msgContent = null;
      } else {
         msgContent = messageMO.getMsgContent();
      }
      // 5. 根据消息类型调用不同的方法创建消息
      // REPORT_HANDLED 类型使用管理员消息方法，其他类型使用普通消息方法
      if (messageTypeEnum == MessageTypeEnum.REPORT_HANDLED) {
         msgService.createAdminMsg(messageMO.getFromUserId(), messageMO.getToUserId(), messageTypeEnum.type, msgContent);
      } else {
         msgService.createMsg(messageMO.getFromUserId(), messageMO.getToUserId(), messageTypeEnum.type, msgContent);
      }
   }

}

