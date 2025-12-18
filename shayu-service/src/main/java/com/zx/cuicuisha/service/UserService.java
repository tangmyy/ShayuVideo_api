package com.zx.cuicuisha.service;

import com.zx.cuicuisha.bo.UpdatedUserBO;
import com.zx.cuicuisha.pojo.Users;
import com.zx.cuicuisha.utils.PagedGridResult;
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
    * 用户信息修改-1 - 用户 头像 背景图 修改
    */
   @Transactional
   public  Users updateUserInfo(UpdatedUserBO updatedUserBO);

   /**
    * 用户信息修改-2
    */
   @Transactional
   public  Users updateUserInfo(UpdatedUserBO updatedUserBO, Integer type);

   /**
    * 查询用户列表（支持分页和搜索）
    * @param nickname 昵称（模糊搜索）
    * @param mobile 手机号（模糊搜索）
    * @param date 注册日期（格式：YYYY-MM-DD）
    * @param page 页码
    * @param pageSize 每页数量
    * @return 分页结果
    */
   public PagedGridResult queryUserList(String nickname, String mobile, String date, Integer page, Integer pageSize);

   /**
    * 删除用户
    * @param userId 用户ID
    */
   @Transactional
   public void deleteUser(String userId);
}
