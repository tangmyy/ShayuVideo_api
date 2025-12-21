package com.zx.cuicuisha.impl;

import com.github.pagehelper.PageHelper;
import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.base.RabbitMQConfig;
import com.zx.cuicuisha.enums.MessageEnum;
import com.zx.cuicuisha.enums.YesOrNo;
import com.zx.cuicuisha.mapper.FansMapper;
import com.zx.cuicuisha.mapper.FansMapperCustom;
import com.zx.cuicuisha.mo.MessageMO;
import com.zx.cuicuisha.pojo.Fans;
import com.zx.cuicuisha.service.FansService;
import com.zx.cuicuisha.service.MsgService;
import com.zx.cuicuisha.utils.JsonUtils;
import com.zx.cuicuisha.utils.PagedGridResult;
import com.zx.cuicuisha.vo.FansAdminVO;
import com.zx.cuicuisha.vo.FansVO;
import com.zx.cuicuisha.vo.VlogerVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FansServiceImpl extends BaseInfoProperties implements FansService {

   @Autowired
   private FansMapper fansMapper;
   @Autowired
   private FansMapperCustom fansMapperCustom;
   @Autowired
   private Sid sid;
   @Autowired
   private MsgService msgService;
   @Autowired
   public RabbitTemplate rabbitTemplate;

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

   // 查询是/否关注该博主
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
         // 对方没有关注我，不是朋友关系
         fans.setIsFanFriendOfMine(YesOrNo.NO.type);
      }
      // 插入新的粉丝关系记录
      fansMapper.insert(fans);
      // 系统消息：关注
      // msgService.createMsg(myId, vlogerId, MessageEnum.FOLLOW_YOU.type, null);
      MessageMO messageMO = new MessageMO();
      messageMO.setFromUserId(myId);
      messageMO.setToUserId(vlogerId);
      // 优化：使用mq异步解耦
      rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG, "sys.msg." + MessageEnum.FOLLOW_YOU.enValue, JsonUtils.objectToJson(messageMO));
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
      Map<String, Object> map = new HashMap<>();
      map.put("myId", myId);
      PageHelper.startPage(page, pageSize);
      List<VlogerVO> list = fansMapperCustom.queryMyFollows(map);
      return setterPagedGrid(list, page);
   }

   // 查询我的粉丝列表
   /**
    * <判断粉丝是否是我的朋友（互粉互关）>
    * 一、普通做法：
    * 多表关联+嵌套关联查询，这样会违反多表关联的规范，不可取，高并发下会出现性能问题
    * 二、常规做法：
    * 1. 避免过多的表关联查询，先查询我的粉丝列表，获得fansList
    * 2. 判断粉丝关注我，并且我也关注粉丝 -> 循环fansList，获得每一个粉丝，再去数据库查询我是否关注他
    * 3. 如果我也关注他（粉丝），说明，我俩互为朋友关系（互关互粉），则标记flag为true，否则false
    * 三、高端做法：
    * 1. 关注/取关的时候，关联关系保存在redis中，不要依赖数据库
    * 2. 数据库查询后，直接循环查询redis，避免第二次循环查询数据库的尴尬局面
    */
   @Override
   public PagedGridResult queryMyFans(String myId, Integer page, Integer pageSize) {
      Map<String, Object> map = new HashMap<>();
      map.put("myId", myId);
      PageHelper.startPage(page, pageSize);
      // 循环查询redis
      List<FansVO> list = fansMapperCustom.queryMyFans(map);
      for (FansVO fanslist : list) {
         String relationship = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + fanslist.getFanId());
         if (StringUtils.isNotBlank(relationship) && relationship.equalsIgnoreCase("1")) {
            fanslist.setFriend(true);
         }
      }
      return setterPagedGrid(list, page);
   }

   /**
    * 查询所有粉丝关系（管理端使用）
    * @param myId 用户ID，如果为空则查询所有粉丝关系，否则查询指定用户的粉丝
    * @param page 页码
    * @param pageSize 每页数量
    * @return 分页结果
    */
   @Override
   public PagedGridResult queryAllFansRelations(String myId, Integer page, Integer pageSize) {
      Map<String, Object> map = new HashMap<>();
      if (StringUtils.isNotBlank(myId)) {
         map.put("myId", myId);
      }
      PageHelper.startPage(page, pageSize);
      List<FansAdminVO> list = fansMapperCustom.queryAllFansRelations(map);
      return setterPagedGrid(list, page);
   }
}
