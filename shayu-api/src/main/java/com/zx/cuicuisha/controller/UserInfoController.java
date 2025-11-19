package com.zx.cuicuisha.controller;


import com.zx.cuicuisha.pojo.Users;
import com.zx.cuicuisha.service.UserService;
import com.zx.cuicuisha.vo.UsersVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "UserInfoController 用户信息接口模块")
@RequestMapping("userInfo")
@RestController
public class UserInfoController extends BaseInfoProperties {

   @Autowired
   private UserService userService;

   @GetMapping("/query")
   public Object query(@RequestParam String userId) throws Exception {
      Users  user = userService.getUser(userId);
      UsersVO usersVO = new UsersVO();
      BeanUtils.copyProperties(user, usersVO);
      // 我的关注总数
      redis.get(REDIS_MY_FOLLOWS_COUNTS + ":" + userId);
      // 我的粉丝总数
      redis.get(REDIS_MY_FANS_COUNTS + ":" + userId);
      // 我的获赞总数 视频+评论的获赞总和
      redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + REDIS_VLOGER_BE_LIKED_COUNTS + ":" +userId);
      return usersVO;
   }

}
