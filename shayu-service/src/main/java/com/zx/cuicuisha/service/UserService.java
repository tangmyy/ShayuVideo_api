package com.zx.cuicuisha.service;

import com.zx.cuicuisha.pojo.Users;


public interface UserService {

   /**
    * 判断用户是否存在, 如果存在则返回用户信息
    */
   public Users queryMobileIsExist(String mobile);

   /**
    * 创建用户信息, 并且返回用户对象
    */
   public Users createUser(String mobile);

}
