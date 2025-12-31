package com.zx.cuicuisha.service;

import com.zx.cuicuisha.mo.MessageMO;
import com.zx.cuicuisha.mo.MessageUpdateMO;
import com.zx.cuicuisha.utils.PagedGridResult;

import java.util.List;
import java.util.Map;

public interface MsgService {

   // 创建消息
   void createMsg(String fromUserId, String toUserId, Integer type, Map msgContent);

   // 查询消息列表
   List<MessageMO> queryList(String toUserId, Integer page, Integer pageSize);

   // 删除消息（根据消息ID）
   void deleteMsg(String msgId, String toUserId);

   // Admin - 查询所有消息（管理员专用，支持按用户ID筛选）
   PagedGridResult queryAllMessagesForAdmin(String userId, Integer page, Integer pageSize);

   // Admin - 创建管理员消息（使用系统消息作为发送者）
   void createAdminMsg(String fromUserId, String toUserId, Integer type, Map msgContent);

   // Admin - 删除消息（管理员专用，不需要权限校验）
   void deleteMsgForAdmin(String msgId);

   // Admin - 更新消息（管理员专用，支持灵活更新）
   MessageMO updateMsgForAdmin(MessageUpdateMO messageUpdateMO);

}
