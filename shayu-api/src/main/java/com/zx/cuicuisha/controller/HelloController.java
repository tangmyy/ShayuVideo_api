package com.zx.cuicuisha.controller;

import com.zx.cuicuisha.grace.result.ResponseDataStructureEnum;
import com.zx.cuicuisha.utils.SMSUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags = "测试接口-1")
@RestController
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
}
