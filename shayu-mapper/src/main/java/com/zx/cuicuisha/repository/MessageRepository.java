package com.zx.cuicuisha.repository;

import com.zx.cuicuisha.mo.MessageMO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<MessageMO, String> {

   // 通过实现Repository，自定义条件查询
   List<MessageMO> findAllByToUserIdEqualsOrderByCreateTimeDesc(String toUserId, Pageable pageable);

   //    void deleteAllByFromUserIdAndToUserIdAndMsgType();

   // Admin - 查询所有消息（按创建时间倒序）
   List<MessageMO> findAllByOrderByCreateTimeDesc(Pageable pageable);

   // Admin - 根据接收者ID查询消息总数
   long countByToUserId(String toUserId);

   // Admin - 根据接收者ID删除所有消息
   void deleteAllByToUserId(String toUserId);

   // Admin - 根据发送者ID删除所有消息
   void deleteAllByFromUserId(String fromUserId);

   // 根据 fromUserId, toUserId, msgType 查询消息（用于删除消息）
   List<MessageMO> findByFromUserIdAndToUserIdAndMsgType(String fromUserId, String toUserId, Integer msgType);

}
