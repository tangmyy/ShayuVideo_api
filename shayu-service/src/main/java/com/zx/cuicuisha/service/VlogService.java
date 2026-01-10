package com.zx.cuicuisha.service;

import com.zx.cuicuisha.bo.VlogBO;
import com.zx.cuicuisha.pojo.Vlog;
import com.zx.cuicuisha.utils.PagedGridResult;
import com.zx.cuicuisha.vo.IndexVlogVO;

public interface VlogService {

   // 新增vlog视频
   void createVlog(VlogBO vlogBO);

   // 用户更新自己的vlog视频信息（只能更新标题、标签、封面）
   void updateMyVlog(String userId, VlogBO vlogBO);

   // 查询首页/搜索的vlog列表
   PagedGridResult getIndexVlogList(String userId, String search, Integer page, Integer pageSize);

   // 根据视频主键查询vlog
   IndexVlogVO getVlogDetailById(String userId, String vlogId);

   // 用户把视频改为公开/私密的视频
   void changeToPrivateOrPublic(String userId, String vlogId, Integer yesOrNo);

   // 用户删除自己的视频（逻辑删除）
   void deleteMyVlog(String userId, String vlogId);

   // 查询用的公开/私密的视频列表
   PagedGridResult queryMyVlogList(String userId, Integer page, Integer pageSize, Integer yesOrNo);

   // 用户点赞/喜欢视频
   void userLikeVlog(String userId, String vlogId);
   // 更新点赞视频时相关的缓存
   void updateLikeVlog(String userId, String vlogerId, String vlogId);
   // 根据主键查询vlog信息
   Vlog getVlog(String id);
   // 用户取消点赞/取消喜欢视频
   void userUnLikeVlog(String userId, String vlogId);
   // 更新取消点赞视频时相关的缓存
   void updateUnLikeVlog(String userId, String vlogerId, String vlogId);

   // 获取该视频获赞总数
   Integer getVlogBeLikedCounts(String vlogId);

   // 查询用户点赞过的短视频
   PagedGridResult getMyLikedVlogList(String userId, Integer page, Integer pageSize);

   // 查询用户关注的博主发布的短视频列表
   PagedGridResult getMyFollowVlogList(String myId, Integer page, Integer pageSize);

   // 查询朋友发布的短视频列表
   PagedGridResult getMyFriendVlogList(String myId, Integer page, Integer pageSize);

   // 把counts输入数据库
   void flushCounts(String vlogId, Integer counts);

   // Admin - 更新vlog视频信息
   void updateVlog(VlogBO vlogBO);
   // Admin - 删除/恢复视频（切换逻辑删除状态）
   Vlog deleteOrRestoreVlog(String vlogId);

   // Admin - 审核视频（封禁/解封视频）
   void auditVlog(String vlogId, Integer auditStatus, Long adminId, String auditRemark);

   // Admin - 查询视频列表（管理端使用，不过滤is_deleted，可以查询所有视频）
   PagedGridResult getIndexVlogListForAdmin(String search, String vlogId, Integer page, Integer pageSize);

   // Admin - 获取所有视频点赞的总数
   Integer getTotalLikeCount();

   // Admin - 获取所有视频的数量
   Integer getTotalVlogCount();

}
