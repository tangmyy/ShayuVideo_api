package com.zx.cuicuisha.controller;

import com.zx.cuicuisha.grace.result.ResponseDataStructureEnum;
import com.zx.cuicuisha.utils.IPUtil;
import com.zx.cuicuisha.utils.SMSUtils;
import com.zx.cuicuisha.utils.SendSms;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Api(tags = "PassportController-验证码通信模块")
@RequestMapping("passport")
@RestController
public class PassportController {

   @Autowired
   private SendSms smsUtils;

   @PostMapping("getSMSCode")
   public Object getSMSCode(@RequestParam String mobile,
                            HttpServletRequest request) throws Exception {
      if (StringUtils.isBlank(mobile)) {
         return ResponseDataStructureEnum.ok();
      }
      // TODO 获得用户ip
      String userIp = IPUtil.getRequestIp(request);
      // TODO 限制同一用户60秒内只能获取一次验证码
      // 等待改进: 投机取巧: 整形 + 字符串 = 字符型
      String code = (int)((Math.random() * 9 + 1) * 1000) + "";
      // smsUtils.sendSMS(mobile, code);
      new SendSms().sendSMS(mobile, code);
      log.info(code);
      log.info("验证码: {}, IP: {}, 手机号: {}", code, userIp, mobile);
      // TODO 将验证码存入Redis, 等待验证
      return ResponseDataStructureEnum.ok();
   }
}
