package com.zx.cuicuisha.service;

import com.zx.cuicuisha.utils.PagedGridResult;

public interface FansService {

   // 关注
   void doFollow(String myId, String vlogerId);
   // 更新关注相关的缓存
   void updateFollowCache(String myId, String vlogerId);
   // 取关
   void doCancel(String myId, String vlogerId);
   // 更新取关相关的缓存
   void updateCancelCache(String myId, String vlogerId);

   // 查询用户是否关注博主
   boolean queryDoIFollowVloger(String myId, String vlogerId);

   // 查询我关注的博主列表
   PagedGridResult queryMyFollows(String myId, Integer page, Integer pageSize);

   // 查询我的粉丝列表
   PagedGridResult queryMyFans(String myId, Integer page, Integer pageSize);

   // Admin - 查询所有粉丝关系（管理端使用，如果myId为空则查询所有，否则查询指定用户的粉丝）
   PagedGridResult queryAllFansRelations(String myId, Integer page, Integer pageSize);

}
