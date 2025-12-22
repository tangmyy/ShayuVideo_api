package com.zx.cuicuisha.service;

import com.zx.cuicuisha.bo.VlogBO;
import com.zx.cuicuisha.mo.MessageMO;
import com.zx.cuicuisha.utils.PagedGridResult;

import javax.validation.constraints.Max;
import java.util.List;
import java.util.Map;

public interface MsgService {

   // 创建消息
   public void createMsg(String fromUserId, String toUserId, Integer type, Map msgContent);

   // 查询消息列表
   public List<MessageMO> queryList(String toUserId, Integer page, Integer pageSize);

   // 删除消息（根据消息ID）
   public void deleteMsg(String msgId, String toUserId);


   // Admin - 查询所有消息（管理员专用，支持按用户ID筛选）
   public PagedGridResult queryAllMessagesForAdmin(String userId, Integer page, Integer pageSize);

   // Admin - 删除消息（管理员专用，不需要权限校验）
   public void deleteMsgForAdmin(String msgId);

}
