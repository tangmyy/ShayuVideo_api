package com.zx.cuicuisha.service;

import com.zx.cuicuisha.bo.UpdatedUserBO;
import com.zx.cuicuisha.pojo.Users;
import com.zx.cuicuisha.utils.PagedGridResult;
import org.springframework.transaction.annotation.Transactional;


public interface UserService {

   // 根据用户主键查询用户信息（不包括已逻辑删除的用户）
   Users getUser(String userId);

   // 根据手机号查询用户（包括已逻辑删除的用户）
   Users queryUserByMobileIncludeDeleted(String mobile);

   // 创建用户信息, 并且返回用户对象
   Users createUser(String mobile);

   // 用户信息修改 -2
   @Transactional
    Users updateUserInfo(UpdatedUserBO updatedUserBO, Integer type);

   // 用户 头像 背景图 修改 -1
   @Transactional
    Users updateUserInfo(UpdatedUserBO updatedUserBO);

   // Admin - 修改用户信息（允许修改已逻辑删除的用户和status字段）
   @Transactional
   Users updateUserInfoForAdmin(UpdatedUserBO updatedUserBO, Integer type);

   // Admin - 修改用户信息（不带type参数，用于修改头像、背景图等，允许修改已逻辑删除的用户和status、isDeleted字段）
   @Transactional
   Users updateUserInfoForAdmin(UpdatedUserBO updatedUserBO);

   /**
    * Admin - 查询用户列表（支持分页和搜索，不过滤已逻辑删除的用户）
    * @param nickname 昵称（模糊搜索）
    * @param mobile 手机号（模糊搜索）
    * @param date 注册日期（格式：YYYY-MM-DD）
    * @param page 页码
    * @param pageSize 每页数量
    * @return 分页结果
    */
   PagedGridResult queryUserListForAdmin(String nickname, String mobile, String date, Integer page, Integer pageSize);

   // Admin - 根据用户主键查询用户信息
   Users getUserIncludeDeleted(String userId);

   // Admin - 删除用户（逻辑删除并清理相关数据）
   @Transactional
   Users deleteOrRestoreUser(String userId);

   // Admin - 恢复已逻辑删除的用户
   @Transactional
   Users restoreDeletedUser(String userId);

   // Admin - 封禁/恢复用户（切换用户状态）
   @Transactional
   Users banOrRestoreUser(String userId);

   // Admin - 获取所有用户的数量
   Integer getTotalUserCount();
}
