package com.zx.cuicuisha.controller;

import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.bo.RedisLoginBO;
import com.zx.cuicuisha.grace.result.ResponseDataStructureEnum;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.pojo.Users;
import com.zx.cuicuisha.service.UserService;
import com.zx.cuicuisha.utils.IPUtil;
import com.zx.cuicuisha.utils.SendSms;
import com.zx.cuicuisha.vo.UsersVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ThreadLocalRandom;

import javax.validation.Valid;
import java.util.UUID;


@Slf4j
@Api(tags = "PassportController-验证码通信模块")
@RequestMapping("passport")
@RestController
public class PassportController extends BaseInfoProperties {

   @Autowired
   private SendSms Sms;
   @Autowired
   private UserService userService;

   // 等待改进: 投机取巧: 整形 + 字符串 = 字符型
   // String code = (int)((Math.random() * 9 + 1) * 1000) + "";
   // 任意类型转换为字符串类型
   // String code = String.valueOf(randomCode);
   // 专门将int类型转换为字符串
   // String code = Integer.toString(randomCode);
   @PostMapping("getSMSCode")
   public Object getSMSCode(@RequestParam String mobile, HttpServletRequest request) throws Exception {
      if (StringUtils.isBlank(mobile)) {
         return ResponseDataStructureEnum.ok();
      }
      // 获得用户ip
      String userIp = IPUtil.getRequestIp(request);
      // 限制同一用户60秒内只能获取一次验证码
      redis.setnx60s(MOBILE_SMSCODE + ":" + userIp, userIp);
      // 使用ThreadLocalRandom（高性能场景）
      int randomCode = ThreadLocalRandom.current().nextInt(1000, 10000);
      String code = Integer.toString(randomCode);
      Sms.sendSMS(mobile, code);
      log.info("验证码: {}, IP: {}, 手机号: {}", code, userIp, mobile);
      // 将验证码存入Redis, 等待验证
      redis.set(MOBILE_SMSCODE + ":" + mobile, code, 2 * 60);

      return ResponseDataStructureEnum.ok();
   }

   // BindingResult result, // 此参数 对代码有侵入性 不再需要
   // 0. 判断 BindingResult 中是否保存了错误的验证信息, 如果有, 则需要返回到前端
   // if (result.hasErrors()) {
   //    Map<String, String> map = getErrors(result);
   //    return ResponseDataStructureEnum.errorMap(map);
   // }
   @PostMapping("login")
   public Object login(@Valid @RequestBody RedisLoginBO redisLoginBO) throws Exception {
      String mobile = redisLoginBO.getMobile();
      String code = redisLoginBO.getSmsCode();
      // 1. 从Redis中获取验证码进行比对
      String redisCode = redis.get(MOBILE_SMSCODE + ":" + mobile);
      if (StringUtils.isBlank(redisCode) || !redisCode.equalsIgnoreCase(code)) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
      }
      // 2. 查询数据库, 判断用户是否存在（包括已逻辑删除的用户）
      Users user = userService.queryUserByMobileIncludeDeleted(mobile);
      if (user == null) {
         // 如果完全不存在, 则注册一个新用户
         log.info("手机号 {} 不存在，开始创建新用户", mobile);
         user = userService.createUser(mobile);
         log.info("创建新用户成功，用户ID: {}", user.getId());
      } else if (user.getIsDeleted() != null && user.getIsDeleted() == 1) {
         // 如果找到被逻辑删除的用户, 恢复该用户
         log.info("手机号 {} 对应的用户曾被逻辑删除，正在恢复用户，用户ID: {}", mobile, user.getId());
         user = userService.restoreDeletedUser(user.getId());
         log.info("用户恢复成功，用户ID: {}", user.getId());
      }
      // 如果用户存在且未删除，直接使用
      // TODO: 如果需要涉及到账号封禁 可以在 else 处理
      // 3. 如果不为空, 保存用户信息和会话信息到Redis中 (分布式会话)
      String uToken = UUID.randomUUID().toString();
      redis.set(REDIS_USER_TOKEN + ":" + user.getId(), uToken);
      // 4. 用户登录注册成功以后, 删除redis中的短信验证码
      redis.delete(MOBILE_SMSCODE + ":" + mobile);
      // 5. 返回用户会话token令牌给前端
      UsersVO usersVO = new UsersVO();
      BeanUtils.copyProperties(user, usersVO);
      usersVO.setUserToken(uToken);

      return ResponseDataStructureEnum.ok(usersVO);
   }

   @PostMapping("logout")
   public Object logout(@RequestParam String userId) throws Exception {
      // 清除用户会话信息即可, 前端也需要清除本地app中的用户信息和token信息
      redis.delete(REDIS_USER_TOKEN + ":" + userId);
      return ResponseDataStructureEnum.ok();
   }



}


