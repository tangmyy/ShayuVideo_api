package com.zx.cuicuisha.service;

import com.zx.cuicuisha.bo.UpdatedUserBO;
import com.zx.cuicuisha.pojo.Users;
import com.zx.cuicuisha.utils.PagedGridResult;
import com.zx.cuicuisha.vo.UsersVO;
import org.springframework.transaction.annotation.Transactional;


public interface UserService {

   // 根据用户主键查询用户信息（不包括已逻辑删除的用户）
   Users getUser(String userId);

   /**
    * 查询自己的用户信息（需要token验证）
    * @param userId 用户ID
    * @param token 用户token
    * @return 用户完整信息（包含手机号、token等），如果不存在或token无效则抛出异常
    */
   UsersVO queryMe(String userId, String token);

   /**
    * 查询用户公开信息（用于查看他人主页，不需要token验证）
    * @param userId 用户ID
    * @return 用户公开信息，如果不存在则返回null
    */
   UsersVO queryVloger(String userId);

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
   Users deleteUser(String userId);

   // Admin - 恢复已逻辑删除的用户
   @Transactional
   Users restoreDeletedUser(String userId);

   // Admin - 设置用户状态
   // status: 0-正常，1-禁言（禁止发布评论），2-封禁（禁止发视频和发评论和关注他人和被他人关注）
   // 前端直接传递目标状态值，可以在三个状态之间任意转换
   @Transactional
   Users banOrRestoreUser(String userId, Integer status);

   // Admin - 获取所有用户的数量
   Integer getTotalUserCount();
}
