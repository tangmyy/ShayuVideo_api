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

   /**
    *    fromUserId: 消息发送者ID
    *    toUserId: 消息接收者ID
    *    msgType: 消息类型
    *    msgContentKey: msgContent中的键（可选，如 "commentId", "vlogId"）
    *    msgContentValue: msgContent中的值（可选，与msgContentKey配合使用）
    */
   // 根据业务条件删除消息（用于取消关注、取消点赞等操作）
   void deleteMsgByCondition(String fromUserId, String toUserId, Integer msgType, String msgContentKey, String msgContentValue);

   // Admin - 查询所有消息（管理员专用，支持按用户ID筛选）
   PagedGridResult queryAllMessagesForAdmin(String userId, Integer page, Integer pageSize);

   // Admin - 创建管理员消息（使用系统消息作为发送者）
   void createAdminMsg(String fromUserId, String toUserId, Integer type, Map msgContent);

   // Admin - 删除消息（管理员专用，不需要权限校验）
   void deleteMsgForAdmin(String msgId);

   // Admin - 更新消息（管理员专用，支持灵活更新）
   MessageMO updateMsgForAdmin(MessageUpdateMO messageUpdateMO);

}
