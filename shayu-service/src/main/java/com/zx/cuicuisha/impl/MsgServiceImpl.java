package com.zx.cuicuisha.impl;

import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.enums.MessageEnum;
import com.zx.cuicuisha.exceptions.GraceException;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.mo.MessageMO;
import com.zx.cuicuisha.pojo.Users;
import com.zx.cuicuisha.repository.MessageRepository;
import com.zx.cuicuisha.service.MsgService;
import com.zx.cuicuisha.service.UserService;
import com.zx.cuicuisha.utils.PagedGridResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MsgServiceImpl extends BaseInfoProperties implements MsgService {

   @Autowired
   private MessageRepository messageRepository;
   @Autowired
   private UserService userService;

   private static final String ADMIN_DEFAULT_FACE = "http://106.54.6.213:9000/shayu/1765019302.jpg";

   @Override
   public void createMsg(String fromUserId, String toUserId, Integer type, Map msgContent) {
      Users fromUser = userService.getUser(fromUserId);
      if (fromUser == null) {
         log.error("创建消息失败：发送者用户不存在，fromUserId={}", fromUserId);
         GraceException.display(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
      }
      MessageMO messageMO = new MessageMO();
      messageMO.setFromUserId(fromUserId);
      messageMO.setFromNickname(fromUser.getNickname());
      messageMO.setFromFace(fromUser.getFace());
      messageMO.setToUserId(toUserId);
      messageMO.setMsgType(type);
      if (msgContent != null) {
         messageMO.setMsgContent(msgContent);
      }
      messageMO.setCreateTime(new Date());
      messageRepository.save(messageMO);
   }

   @Override
   public void createAdminMsg(String fromUserId, String toUserId, Integer type, Map msgContent) {
      MessageMO messageMO = new MessageMO();
      messageMO.setFromUserId(fromUserId);
      messageMO.setFromNickname("系统消息");
      messageMO.setFromFace(ADMIN_DEFAULT_FACE);
      messageMO.setToUserId(toUserId);
      messageMO.setMsgType(type);
      if (msgContent != null) {
         messageMO.setMsgContent(msgContent);
      }
      messageMO.setCreateTime(new Date());
      MessageMO savedMsg = messageRepository.save(messageMO);
      log.info("系统消息创建成功：msgId={}, fromUserId={}, toUserId={}, msgType={}", 
               savedMsg.getId(), fromUserId, toUserId, type);
   }

   @Override
   public List<MessageMO> queryList(String toUserId, Integer page, Integer pageSize) {
      Pageable pageable = PageRequest.of(page, pageSize, Sort.Direction.DESC, "createTime");
      List<MessageMO> list =  messageRepository.findAllByToUserIdEqualsOrderByCreateTimeDesc(toUserId, pageable);
      log.info("查询消息列表：toUserId={}, page={}, pageSize={}, 查询到{}条消息", toUserId, page, pageSize, list.size());
      for (MessageMO msg : list) {
         // 如果类型是关注消息，则需要查询我之前有没有关注过他，用于在前端标记“互粉”“互关”
         if (msg.getMsgType() != null && msg.getMsgType() == MessageEnum.FOLLOW_YOU.type) {
            Map map = msg.getMsgContent();
            if (map == null) {
               map = new HashMap();
            }
            String relationship = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + msg.getToUserId() + ":" + msg.getFromUserId());
            if (StringUtils.isNotBlank(relationship) && relationship.equalsIgnoreCase("1")) {
               map.put("isFriend", true);
            } else {
               map.put("isFriend", false);
            }
            msg.setMsgContent(map);
         }
      }
      return list;
   }

   @Override
   public void deleteMsg(String msgId, String toUserId) {
      // 参数校验
      if (StringUtils.isBlank(msgId)) {
         GraceException.display(ResponseStatusEnum.SYSTEM_ERROR);
      }
      if (StringUtils.isBlank(toUserId)) {
         GraceException.display(ResponseStatusEnum.SYSTEM_ERROR);
      }
      // 查询消息是否存在，并验证消息是否属于该用户
      MessageMO messageMO = messageRepository.findById(msgId).orElse(null);
      if (messageMO == null) {
         log.warn("消息不存在：msgId={}", msgId);
         GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
      }
      // 验证消息是否属于该用户（只能删除自己接收的消息）
      if (!messageMO.getToUserId().equals(toUserId)) {
         log.warn("无权删除该消息：msgId={}, toUserId={}, messageToUserId={}",
               msgId, toUserId, messageMO.getToUserId());
         GraceException.display(ResponseStatusEnum.NO_AUTH);
      }
      // 删除消息
      messageRepository.deleteById(msgId);
      log.info("消息删除成功：msgId={}, toUserId={}", msgId, toUserId);
   }

   // admin 管理员端专用方法
   @Override
   public PagedGridResult queryAllMessagesForAdmin(String userId, Integer page, Integer pageSize) {
      // MongoDB分页从0开始，需要将页码从1转换为0
      int mongoPage = page - 1;
      Pageable pageable = PageRequest.of(mongoPage, pageSize, Sort.Direction.DESC, "createTime");
      List<MessageMO> list;
      long totalRecords;
      // 如果userId为空，查询所有消息；否则查询指定用户的消息
      if (StringUtils.isBlank(userId)) {
         list = messageRepository.findAllByOrderByCreateTimeDesc(pageable);
         totalRecords = messageRepository.count();
      } else {
         list = messageRepository.findAllByToUserIdEqualsOrderByCreateTimeDesc(userId, pageable);
         totalRecords = messageRepository.countByToUserId(userId);
      }
      // 处理关注消息的互粉关系（与queryList相同的逻辑）
      for (MessageMO msg : list) {
         if (msg.getMsgType() != null && msg.getMsgType() == MessageEnum.FOLLOW_YOU.type) {
            Map map = msg.getMsgContent();
            if (map == null) {
               map = new HashMap();
            }
            String relationship = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + msg.getToUserId() + ":" + msg.getFromUserId());
            if (StringUtils.isNotBlank(relationship) && relationship.equalsIgnoreCase("1")) {
               map.put("isFriend", true);
            } else {
               map.put("isFriend", false);
            }
            msg.setMsgContent(map);
         }
      }
      // 手动构建PagedGridResult（MongoDB不支持PageHelper）
      PagedGridResult gridResult = new PagedGridResult();
      gridResult.setRows(list);
      gridResult.setPage(page);
      gridResult.setRecords(totalRecords);
      // 计算总页数
      long totalPages = (totalRecords + pageSize - 1) / pageSize;
      gridResult.setTotal(totalPages);
      return gridResult;
   }

   @Override
   public void deleteMsgForAdmin(String msgId) {
      // 参数校验
      if (StringUtils.isBlank(msgId)) {
         GraceException.display(ResponseStatusEnum.SYSTEM_ERROR);
      }
      // 查询消息是否存在
      MessageMO messageMO = messageRepository.findById(msgId).orElse(null);
      if (messageMO == null) {
         log.warn("消息不存在：msgId={}", msgId);
         GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
      }
      // 管理员删除消息，不需要权限校验
      messageRepository.deleteById(msgId);
      log.info("管理员删除消息成功：msgId={}", msgId);
   }
}

