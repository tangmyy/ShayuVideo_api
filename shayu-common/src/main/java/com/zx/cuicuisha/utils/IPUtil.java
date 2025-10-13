package com.zx.cuicuisha.utils;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户获得用户ip的工具类
 */
public class IPUtil {

   /**
    * 获取请求IP:
    * 用户的真实IP不能使用request.getRemoteAddr()
    * 这是因为可能会使用一些代理软件，这样ip获取就不准确了
    * 此外我们如果使用了多级（LVS/Nginx）反向代理的话，ip需要从X-Forwarded-For中获得第一个非unknown的IP才是用户的有效ip。
    * @param request
    * @return
    * x-forwarded-for - 最常用的代理IP头
    * Proxy-Client-IP - 某些代理服务器的专用头
    * WL-Proxy-Client-IP - WebLogic代理的头
    * HTTP_CLIENT_IP - 客户端IP头
    * HTTP_X_FORWARDED_FOR - 另一种转发头
    * request.getRemoteAddr() - 最后备选，直接连接IP
    */
   public static String getRequestIp(HttpServletRequest request) {
      String ip = request.getHeader("x-forwarded-for");
      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
         ip = request.getHeader("Proxy-Client-IP");
      }
      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
         ip = request.getHeader("WL-Proxy-Client-IP");
      }
      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
         ip = request.getHeader("HTTP_CLIENT_IP");
      }
      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
         ip = request.getHeader("HTTP_X_FORWARDED_FOR");
      }
      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
         ip = request.getRemoteAddr();
      }
      return ip;
   }
}

