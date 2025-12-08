package com.zx.cuicuisha.controller;

import com.zx.cuicuisha.base.RabbitMQConfig;
import com.zx.cuicuisha.grace.result.ResponseDataStructureEnum;
import com.zx.cuicuisha.utils.SMSUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags = "测试接口-1")
@RestController
@RefreshScope
public class HelloController {

   @ApiOperation(value = "hello - 测试项目基本运行路由")
   @GetMapping("hello")
   public Object hello() {
      log.debug("debug");
      log.info("info");
      log.warn("warn");
      log.error("error");
      return ResponseDataStructureEnum.ok("hello sb~");
   }

   @Autowired
   private SMSUtils smsUtils;

   @ApiOperation(value = "smsUtils类 - 的测试路由")
   @GetMapping("sms")
   public Object sms() throws Exception{
      String code = "123456";
      smsUtils.sendSMS("16689532246", code);
      return ResponseDataStructureEnum.ok();
   }

   /**
    * 路由规则
    * route-key
    * display.*.*
    *      display.a.b
    *      display.public.msg
    *      display.a.b.c
    *      * 代表一个占位符
    *
    *  display.#
    *      display.a.b
    *      display.a.b.c.d
    *      display.public.msg
    *      display.delete.msg.do
    *      # 代表多个占位符
    */
   @Autowired
   public RabbitTemplate rabbitTemplate;

   @ApiOperation(value = "MQ的测试路由 - 发送消息")
   @GetMapping("produc_send")
   public Object produc_send() throws Exception {
      rabbitTemplate.convertAndSend(
         RabbitMQConfig.EXCHANGE_MSG, "sys.msg.send", "我发送了一个消息~~");
      return ResponseDataStructureEnum.ok();
   }
   @ApiOperation(value = "MQ的测试路由 - 删除消息")
   @GetMapping("produce_delete")
   public Object produce_delete() throws Exception {
      rabbitTemplate.convertAndSend(
         RabbitMQConfig.EXCHANGE_MSG, "sys.msg.delete", "我删除了一个消息~~");
      return ResponseDataStructureEnum.ok();
   }
}
