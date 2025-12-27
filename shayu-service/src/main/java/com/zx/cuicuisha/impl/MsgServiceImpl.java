package com.zx.cuicuisha.impl;

import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.bo.AdminLoginBO;
import com.zx.cuicuisha.mo.MessageUpdateMO;
import com.zx.cuicuisha.enums.MessageEnum;
import com.zx.cuicuisha.exceptions.GraceException;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.mo.MessageMO;
import com.zx.cuicuisha.pojo.AdminUsers;
import com.zx.cuicuisha.pojo.Users;
import com.zx.cuicuisha.repository.MessageRepository;
import com.zx.cuicuisha.service.AdminService;
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
   @Autowired
   private AdminService adminService;

   private static final String ADMIN_DEFAULT_FACE = "http://106.54.6.213:9000/shayu/zzx.jpg";

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
      // 获取管理员头像
      String adminUsersAvatar = ADMIN_DEFAULT_FACE; // 默认头像
      if (StringUtils.isNotBlank(fromUserId)) {
         try {
            // 将String类型的fromUserId转换为Long类型
            Long adminId = Long.parseLong(fromUserId);
            AdminUsers admin = adminService.getAdminById(adminId);
            if (admin != null && StringUtils.isNotBlank(admin.getAvatar())) {
               adminUsersAvatar = admin.getAvatar();
            } else {
               log.warn("管理员不存在或没有头像，使用默认头像：fromUserId={}", fromUserId);
            }
         } catch (NumberFormatException e) {
            log.warn("fromUserId不是有效的管理员ID，使用默认头像：fromUserId={}", fromUserId);
         } catch (Exception e) {
            log.error("查询管理员信息失败，使用默认头像：fromUserId={}, error={}", fromUserId, e.getMessage());
         }
      }
      MessageMO messageMO = new MessageMO();
      messageMO.setFromUserId(fromUserId);
      messageMO.setFromNickname("系统消息");
      messageMO.setFromFace(adminUsersAvatar);
      messageMO.setToUserId(toUserId);
      messageMO.setMsgType(type);
      if (msgContent != null) {
         messageMO.setMsgContent(msgContent);
      }
      messageMO.setCreateTime(new Date());
      MessageMO savedMsg = messageRepository.save(messageMO);
      log.info("系统消息创建成功：msgId={}, fromUserId={}, toUserId={}, msgType={}, avatar={}", 
               savedMsg.getId(), fromUserId, toUserId, type, adminUsersAvatar);
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

   @Override
   public MessageMO updateMsgForAdmin(MessageUpdateMO messageUpdateMO) {
      // 参数校验
      if (messageUpdateMO == null || StringUtils.isBlank(messageUpdateMO.getId())) {
         GraceException.display(ResponseStatusEnum.SYSTEM_ERROR);
      }
      // 查询原消息
      MessageMO existMessage = messageRepository.findById(messageUpdateMO.getId()).orElse(null);
      if (existMessage == null) {
         log.warn("消息不存在：msgId={}", messageUpdateMO.getId());
         GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
      }
      // 只更新传入的非空字段
      boolean hasUpdate = false;
      // 更新发送者用户ID
      if (StringUtils.isNotBlank(messageUpdateMO.getFromUserId())) {
         existMessage.setFromUserId(messageUpdateMO.getFromUserId());
         hasUpdate = true;
         // 如果更新了fromUserId，可能需要同步更新fromNickname和fromFace
         // 这里可以选择是否自动同步，根据业务需求决定
         // 如果前端传入了fromNickname和fromFace，则使用传入的值
         // 如果未传入，可以从用户表查询（可选）
      }
      // 更新发送者昵称
      if (StringUtils.isNotBlank(messageUpdateMO.getFromNickname())) {
         existMessage.setFromNickname(messageUpdateMO.getFromNickname());
         hasUpdate = true;
      }
      // 更新发送者头像
      if (StringUtils.isNotBlank(messageUpdateMO.getFromFace())) {
         existMessage.setFromFace(messageUpdateMO.getFromFace());
         hasUpdate = true;
      }
      // 更新接收者用户ID
      if (StringUtils.isNotBlank(messageUpdateMO.getToUserId())) {
         existMessage.setToUserId(messageUpdateMO.getToUserId());
         hasUpdate = true;
      }
      // 更新消息类型
      if (messageUpdateMO.getMsgType() != null) {
         existMessage.setMsgType(messageUpdateMO.getMsgType());
         hasUpdate = true;
      }
      // 更新消息内容（支持完全替换或合并更新）
      if (messageUpdateMO.getMsgContent() != null) {
         Map<String, Object> newContent = messageUpdateMO.getMsgContent();
         if (Boolean.TRUE.equals(messageUpdateMO.getMergeContent())) {
            // 合并模式：将新内容合并到原有内容中
            Map<String, Object> existingContent = existMessage.getMsgContent();
            if (existingContent == null) {
               existingContent = new HashMap<>();
            }
            // 将新内容合并到原有内容中（新内容会覆盖相同key的值）
            existingContent.putAll(newContent);
            existMessage.setMsgContent(existingContent);
            log.info("合并更新消息内容：msgId={}, 原有内容={}, 新增内容={}", 
                    messageUpdateMO.getId(), existingContent, newContent);
         } else {
            // 完全替换模式：直接替换原有内容
            existMessage.setMsgContent(newContent);
            log.info("完全替换消息内容：msgId={}, 新内容={}", 
                    messageUpdateMO.getId(), newContent);
         }
         hasUpdate = true;
      }
      // 如果没有需要更新的字段，返回原消息
      if (!hasUpdate) {
         log.warn("没有需要更新的字段：msgId={}", messageUpdateMO.getId());
         return existMessage;
      }
      // 保存更新后的消息（MongoDB的save方法，如果对象有ID会自动更新）
      MessageMO updatedMessage = messageRepository.save(existMessage);
      log.info("管理员更新消息成功：msgId={}", messageUpdateMO.getId());
      return updatedMessage;
   }
}

