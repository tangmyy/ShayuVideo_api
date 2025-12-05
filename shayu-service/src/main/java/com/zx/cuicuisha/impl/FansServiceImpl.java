package com.zx.cuicuisha.impl;

import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.enums.YesOrNo;
import com.zx.cuicuisha.mapper.FansMapper;
import com.zx.cuicuisha.pojo.Fans;
import com.zx.cuicuisha.service.FansService;
import com.zx.cuicuisha.utils.PagedGridResult;
import lombok.extern.slf4j.Slf4j;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Slf4j
@Repository
public class FansServiceImpl extends BaseInfoProperties implements FansService {

   @Autowired
   private FansMapper fansMapper;

   @Autowired
   private Sid sid;

   /**
    * 查询粉丝关系 --> 提供给关注和取关使用
    * 查询是否存在用户A关注用户B的关系
    * @param fanId    粉丝ID（关注者）
    * @param vlogerId 博主ID（被关注者）
    * @return Fans对象，如果存在关系则返回，否则返回null
    */
   public Fans queryFansRelationship(String fanId, String vlogerId) {
      // 创建Example对象，用于构建查询条件
      Example example = new Example(Fans.class);
      // 创建查询条件
      Example.Criteria criteria = example.createCriteria();
      criteria.andEqualTo("fanId", fanId);      // fan_id = fanId
      criteria.andEqualTo("vlogerId", vlogerId); // vloger_id = vlogerId
      // 执行查询
      List<Fans> list = fansMapper.selectByExample(example);
      // 处理查询结果
      Fans fan = null;
      if (list != null && !list.isEmpty()) {
         fan = list.get(0);  // 获取第一条记录（应该只有一条）
      }
      return fan;
   }

   // 查询关注(是/否)
   @Override
   public boolean queryDoIFollowVloger(String myId, String vlogerId) {
      // 1.传统
      // Fans vloger = queryFansRelationship(myId, vlogerId);
      // return vloger != null;
      // 2.从Redis查询
      String relationKey = REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId;
      String relationship = redis.get(relationKey);
      return "1".equals(relationship);
   }

   // 关注
   @Transactional
   @Override
   public void doFollow(String myId, String vlogerId) {
      String fanId = sid.nextShort();
      Fans fans = new Fans();
      fans.setId(fanId);
      fans.setFanId(myId);
      fans.setVlogerId(vlogerId);
      // 判断对方是否关注我, 如果关注我, 那么双方互为朋友关系
      Fans vloger  = queryFansRelationship(vlogerId, myId);
      if (vloger != null) {
         // 说明对方已经关注我, 那么双方互为朋友关系
         fans.setIsFanFriendOfMine(YesOrNo.YES.type);
         vloger.setIsFanFriendOfMine(YesOrNo.YES.type);
         fansMapper.updateByPrimaryKeySelective(vloger);
      } else {
         // 对方没有关注我，单向关注关系
         fans.setIsFanFriendOfMine(YesOrNo.NO.type);     // 不是朋友关系
      }
      // 插入新的粉丝关系记录
      fansMapper.insert(fans);
   }

   // 更新关注相关的缓存
   public void updateFollowCache(String myId, String vlogerId) {
      try {
         // 博主的粉丝+1，我的关注+1
         redis.increment(REDIS_MY_FOLLOWS_COUNTS + ":" + myId, 1);
         redis.increment(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);
         // 我和博主的关联关系
         redis.set(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId, "1");
         log.info("更新关注缓存成功：myId={}, vlogerId={}", myId, vlogerId);
      } catch (Exception e) {
         log.error("更新关注缓存失败：myId={}, vlogerId={}", myId, vlogerId, e);
      }
   }

   // 取关
   @Override
   public void doCancel(String myId, String vlogerId) {
      // 判断我们是否是朋友关系, 取消双方关系
      Fans fan = queryFansRelationship(myId, vlogerId);
      if (fan != null && fan.getIsFanFriendOfMine() == YesOrNo.YES.type) {
         // 取消对方关系
         Fans pendingFan = queryFansRelationship(vlogerId, myId);
         pendingFan.setIsFanFriendOfMine(YesOrNo.NO.type);
         fansMapper.updateByPrimaryKeySelective(pendingFan);
      }
      // 删除我的关注记录
      fansMapper.delete(fan);
   }
   // 更新取关时相关的缓存
   public void updateCancelCache(String myId, String vlogerId) {
      try {
         redis.decrement(REDIS_MY_FOLLOWS_COUNTS + ":" + myId, 1);
         redis.decrement(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);
         redis.delete(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId);
         log.info("更新取关缓存成功：myId={}, vlogerId={}", myId, vlogerId);
      } catch (Exception e) {
         log.error("更新取关缓存失败：myId={}, vlogerId={}", myId, vlogerId, e);
      }
   }

   // 查询我关注的博主列表
   @Override
   public PagedGridResult queryMyFollows(String myId, Integer page, Integer pageSize) {
      return null;
   }
   // 查询我的粉丝列表
   @Override
   public PagedGridResult queryMyFans(String myId, Integer page, Integer pageSize) {
      return null;
   }
}
