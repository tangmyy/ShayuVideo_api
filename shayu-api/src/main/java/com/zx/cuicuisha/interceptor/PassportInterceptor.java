package com.zx.cuicuisha.interceptor;

import com.zx.cuicuisha.controller.BaseInfoProperties;
import com.zx.cuicuisha.exceptions.GraceException;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.utils.IPUtil;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class PassportInterceptor extends BaseInfoProperties implements HandlerInterceptor {

   @Override
   public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
      // 获得用户ip
      String userIp = IPUtil.getRequestIp(request);
      //
      boolean keyIsExist = redis.keyIsExist(MOBILE_SMSCODE + ":" + userIp);

      if (keyIsExist) {
         GraceException.display(ResponseStatusEnum.SMS_NEED_WAIT_ERROR);
         log.info("短信发送频率过快！");
         return false;
      }
      return true;
   }

   @Override
   public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
      // HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
   }

   @Override
   public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
      // HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
   }
}
