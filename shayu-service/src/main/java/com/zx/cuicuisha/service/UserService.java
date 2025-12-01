package com.zx.cuicuisha.service;

import com.zx.cuicuisha.bo.UpdatedUserBO;
import com.zx.cuicuisha.pojo.Users;
import org.springframework.transaction.annotation.Transactional;


public interface UserService {

   /**
    * 判断用户是否存在, 如果存在则返回用户信息
    */
   public Users queryMobileIsExist(String mobile);

   /**
    * 创建用户信息, 并且返回用户对象
    */
   public Users createUser(String mobile);

   /**
    * 根据用户主键查询用户信息
    */
   public Users getUser(String userId);

   /**
    * 用户信息修改-1
    */
   @Transactional
   public  Users updateUserInfo(UpdatedUserBO updatedUserBO);

   /**
    * 用户信息修改-2
    */
   @Transactional
   public  Users updateUserInfo(UpdatedUserBO updatedUserBO, Integer type);
}
