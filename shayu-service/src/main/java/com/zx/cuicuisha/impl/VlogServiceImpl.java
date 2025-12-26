package com.zx.cuicuisha.impl;

import com.github.pagehelper.PageHelper;
import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.base.RabbitMQConfig;
import com.zx.cuicuisha.bo.VlogBO;
import com.zx.cuicuisha.enums.MessageEnum;
import com.zx.cuicuisha.enums.VlogSortType;
import com.zx.cuicuisha.enums.YesOrNo;
import com.zx.cuicuisha.exceptions.GraceException;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.mapper.MyLikedVlogMapper;
import com.zx.cuicuisha.mapper.UsersMapper;
import com.zx.cuicuisha.mapper.VlogMapper;
import com.zx.cuicuisha.mapper.VlogMapperCustom;
import com.zx.cuicuisha.mo.MessageMO;
import com.zx.cuicuisha.pojo.MyLikedVlog;
import com.zx.cuicuisha.pojo.Users;
import com.zx.cuicuisha.pojo.Vlog;
import com.zx.cuicuisha.service.FansService;
import com.zx.cuicuisha.service.MsgService;
import com.zx.cuicuisha.service.VlogService;
import com.zx.cuicuisha.utils.JsonUtils;
import com.zx.cuicuisha.utils.PagedGridResult;
import com.zx.cuicuisha.vo.IndexVlogVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class VlogServiceImpl extends BaseInfoProperties implements VlogService {

   @Autowired
   private FansService fansService;
   @Autowired
   private VlogMapper vlogMapper;
   @Autowired
   private VlogMapperCustom vlogMapperCustom;
   @Autowired
   private MyLikedVlogMapper myLikedVlogMapper;
   @Autowired
   private UsersMapper usersMapper;
   @Autowired
   private Sid sid;
   @Autowired
   public RabbitTemplate rabbitTemplate;


   @Transactional
   @Override
   public void createVlog(VlogBO vlogBO) {
      // 检查用户是否被封禁
      Users user = usersMapper.selectByPrimaryKey(vlogBO.getVlogerId());
      if (user == null) {
         GraceException.display(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
      }
      if (user.getStatus() != null && user.getStatus() == 1) {
         GraceException.display(ResponseStatusEnum.USER_FROZEN);
      }
      String vid = sid.nextShort();
      Vlog vlog = new Vlog();
      BeanUtils.copyProperties(vlogBO, vlog);
      vlog.setId(vid);
      vlog.setLikeCounts(0);
      vlog.setCommentsCounts(0);
      vlog.setIsPrivate(YesOrNo.NO.type);
      vlog.setIsDeleted(1);               // 默认未审核
      vlog.setCreatedTime(new Date());
      vlog.setUpdatedTime(new Date());
      vlogMapper.insert(vlog);

   }

   @Transactional
   @Override
   public void updateVlog(VlogBO vlogBO) {
      if (StringUtils.isBlank(vlogBO.getId())) {
         GraceException.display(ResponseStatusEnum.SYSTEM_ERROR);
      }
      // 检查视频是否存在（管理员可以查询已逻辑删除的视频）
      Vlog existVlog = vlogMapper.selectByPrimaryKey(vlogBO.getId());
      if (existVlog == null) {
         GraceException.display(ResponseStatusEnum.VLOG_NOT_EXIST_ERROR);
      }
      // 管理员可以修改任何视频，包括已逻辑删除的视频（可以恢复）
      // 更新视频信息
      Vlog vlog = new Vlog();
      BeanUtils.copyProperties(vlogBO, vlog);
      vlog.setUpdatedTime(new Date());
      // 不更新创建时间、点赞数、评论数等字段
      vlog.setCreatedTime(null);
      vlog.setLikeCounts(null);
      vlog.setCommentsCounts(null);
      // 允许更新isDeleted字段（管理员操作）
      vlogMapper.updateByPrimaryKeySelective(vlog);
   }

   private IndexVlogVO setterVO(IndexVlogVO vlogVO, String userId) {
      String vlogerId = vlogVO.getVlogerId();
      String vlogId = vlogVO.getVlogId();
      if (StringUtils.isNotBlank(userId)) {
         // 用户是否关注该博主
         boolean doIFollowVloger = fansService.queryDoIFollowVloger(userId, vlogerId);
         vlogVO.setDoIFollowVloger(doIFollowVloger);
         // 判断当前用户是否点赞过视频
         vlogVO.setDoILikeThisVlog(doILikeVlog(userId, vlogId));
      }
      // 获得当前视频被点赞过的总数
      vlogVO.setLikeCounts(getVlogBeLikedCounts(vlogId));
      return vlogVO;
   }

   @Override
   public PagedGridResult getIndexVlogList(String userId, String search, Integer page, Integer pageSize) {
      // 开始分页
      PageHelper.startPage(page, pageSize);
      Map<String, Object> map = new HashMap<>();
      if (StringUtils.isNoneBlank(search)){
         map.put("search", search);
      }
      // 从Redis读取排序类型（0-创建时间，1-点赞数，2-评论数）
      String sortTypeStr = redis.get(REDIS_VLOG_INDEX_SORT_TYPE);
      Integer sortType = null;
      if (StringUtils.isNotBlank(sortTypeStr)) {
         try {
            sortType = Integer.valueOf(sortTypeStr);
         } catch (Exception e) {
            log.warn("Redis中的排序类型格式错误：{}", sortTypeStr);
         }
      }
      // 根据排序类型设置sortField（使用枚举优化）
      VlogSortType vlogSortType = VlogSortType.getByType(sortType);
      if (vlogSortType != null && vlogSortType != VlogSortType.CREATED_TIME) {
         // 如果不是创建时间排序，则设置sortField
         map.put("sortField", vlogSortType.field);
      }
      // sortType == 0 或 null 时，不设置sortField，使用默认的创建时间排序
      List<IndexVlogVO> list = vlogMapperCustom.getIndexVlogList(map);
      for (IndexVlogVO vlogVO : list) {
         setterVO(vlogVO, userId);
      }
      return setterPagedGrid(list, page);
   }

   /**
    * 管理后台查询视频列表（不过滤已逻辑删除的视频）
    */
   @Override
   public PagedGridResult getIndexVlogListForAdmin(String search, Integer page, Integer pageSize) {
      // 开始分页
      PageHelper.startPage(page, pageSize);
      Map<String, Object> map = new HashMap<>();
      if (StringUtils.isNotBlank(search)){
         map.put("search", search);
      }
      // 从Redis读取排序类型（0-创建时间，1-点赞数，2-评论数）
      String sortTypeStr = redis.get(REDIS_VLOG_INDEX_SORT_TYPE);
      Integer sortType = null;
      if (StringUtils.isNotBlank(sortTypeStr)) {
         try {
            sortType = Integer.valueOf(sortTypeStr);
         } catch (Exception e) {
            log.warn("Redis中的排序类型格式错误：{}", sortTypeStr);
         }
      }
      // 根据排序类型设置sortField（使用枚举优化）
      VlogSortType vlogSortType = VlogSortType.getByType(sortType);
      if (vlogSortType != null && vlogSortType != VlogSortType.CREATED_TIME) {
         // 如果不是创建时间排序，则设置sortField
         map.put("sortField", vlogSortType.field);
      }
      // sortType == 0 或 null 时，不设置sortField，使用默认的创建时间排序
      // 使用管理员专用查询方法，不过滤 is_deleted
      List<IndexVlogVO> list = vlogMapperCustom.getIndexVlogListForAdmin(map);
      // 管理员查询不需要设置关注、点赞等用户相关状态
      return setterPagedGrid(list, page);
   }
   // 获取用户是否点赞过该视频
   private boolean doILikeVlog(String myId, String vlogId){
      String doILike = redis.get(REDIS_USER_LIKE_VLOG + ":" + myId + ":" + vlogId);
      boolean isLike = false;
      if (StringUtils.isNoneBlank(doILike) && doILike.equalsIgnoreCase("1")){
         isLike = true;
      }
      return  isLike;
   }

   // 获取该视频获赞总数
   @Override
   public Integer getVlogBeLikedCounts(String vlogId){
      try {
         String countsStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);
         if (StringUtils.isBlank(countsStr)) {
            countsStr = "0";
         }
         return Integer.valueOf(countsStr);
      } catch (Exception e) {
         log.error("获取视频{}点赞数失败: {}", vlogId, e.getMessage());
         return 0;
      }
   }

   @Override
   public IndexVlogVO getVlogDetailById(String userId, String vlogId) {
      Map<String, Object> map = new HashMap<>();
      map.put("vlogId", vlogId);
      List<IndexVlogVO> list = vlogMapperCustom.getVlogDetailById(map);
      if (list != null && !list.isEmpty()){
         IndexVlogVO vlogVO = list.get(0);
         return setterVO(vlogVO, userId);
      }
      return null;
   }

   @Transactional
   @Override
   public void changeToPrivateOrPublic(String userId, String vlogId, Integer yesOrNo) {
      Example example = new Example(Vlog.class);
      Example.Criteria criteria = example.createCriteria();
      criteria.andEqualTo("id", vlogId);
      criteria.andEqualTo("vlogerId", userId);
      criteria.andEqualTo("isDeleted", 0); // 只能修改未逻辑删除的视频
      Vlog pendingVlog = new Vlog();
      pendingVlog.setIsPrivate(yesOrNo);
      vlogMapper.updateByExampleSelective(pendingVlog, example);
   }

   @Override
   public PagedGridResult queryMyVlogList(String userId, Integer page, Integer pageSize, Integer yesOrNo) {
      Example example = new Example(Vlog.class);
      Example.Criteria criteria = example.createCriteria();
      criteria.andEqualTo("vlogerId", userId);
      criteria.andEqualTo("isPrivate", yesOrNo);
      criteria.andEqualTo("isDeleted", 0); // 只查询未逻辑删除的视频
      PageHelper.startPage(page, pageSize);
      List<Vlog> list = vlogMapper.selectByExample(example);
      return setterPagedGrid(list, page);
   }

   // 点赞视频
   @Transactional
   @Override
   public void userLikeVlog(String userId, String vlogId) {
      // 检查视频是否存在且未被逻辑删除
      Vlog vlog = this.getVlog(vlogId);
      if (vlog == null) {
         GraceException.display(ResponseStatusEnum.VLOG_NOT_EXIST_ERROR);
      }
      String lVid = sid.nextShort();
      MyLikedVlog likedVlog = new MyLikedVlog();
      likedVlog.setId(lVid);
      likedVlog.setVlogId(vlogId);
      likedVlog.setUserId(userId);
      myLikedVlogMapper.insert(likedVlog);
      // 系统消息：点赞短视频
      Map msgContent = new HashMap();
      msgContent.put("vlogId", vlogId);
      msgContent.put("vlogCover", vlog.getCover());
      // msgService.createMsg(userId, vlog.getVlogerId(), MessageEnum.LIKE_VLOG.type, msgContent);
      // MQ异步解耦
      MessageMO messageMO = new MessageMO();
      messageMO.setFromUserId(userId);
      messageMO.setToUserId(vlog.getVlogerId());
      messageMO.setMsgContent(msgContent);
      rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG, "sys.msg." + MessageEnum.LIKE_VLOG.enValue, JsonUtils.objectToJson(messageMO));
   }
   @Override
   public Vlog getVlog(String id) {
      Vlog vlog = vlogMapper.selectByPrimaryKey(id);
      // 检查视频是否存在且未被逻辑删除
      if (vlog == null || (vlog.getIsDeleted() != null && vlog.getIsDeleted() == 1)) {
         return null;
      }
      return vlog;
   }
   // 更新点赞视频时相关的缓存
   public void updateLikeVlog(String userId, String vlogerId, String vlogId) {
      try {
         // 点赞以后，视频点赞数/发布者获赞数 +1
         redis.increment(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId, 1);
         redis.increment(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId, 1);
         //  我点赞的视频，关联关系保存到redis
         redis.set(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId, "1");
         log.info("点赞视频缓存成功：myId={}, vlogId={}", userId, vlogId);
      } catch (Exception e) {
         log.error("点赞视频缓存失败：myId={}, vlogId={}", userId, vlogId, e);
      }
   }

   // 取消点赞视频
   @Transactional
   @Override
   public void userUnLikeVlog(String userId, String vlogId) {
      MyLikedVlog likedVlog = new MyLikedVlog();
      likedVlog.setVlogId(vlogId);
      likedVlog.setUserId(userId);
      myLikedVlogMapper.delete(likedVlog);
   }
   // 更新取消点赞视频时相关的缓存
   public void updateUnLikeVlog(String userId, String vlogerId, String vlogId) {
      try {
         // 取消点赞以后，视频点赞数/发布者获赞数 -1
         redis.decrement(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId, 1);
         redis.decrement(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId, 1);
         //  我取消点赞的视频，关联关系保存到redis
         redis.delete(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId);
         log.info("取消点赞视频缓存成功：myId={}, vlogId={}", userId, vlogId);
      } catch (Exception e) {
         log.error("取消点赞视频缓存失败：myId={}, vlogId={}", userId, vlogId, e);
      }
   }

   @Override
   public PagedGridResult getMyLikedVlogList(String userId, Integer page, Integer pageSize) {
      PageHelper.startPage(page, pageSize);
      Map<String, Object> map = new HashMap<>();
      map.put("userId", userId);
      List<IndexVlogVO> list = vlogMapperCustom.getMyLikedVlogList(map);
      return setterPagedGrid(list, page);
   }

   @Override
   public PagedGridResult getMyFollowVlogList(String myId, Integer page, Integer pageSize) {
      PageHelper.startPage(page, pageSize);
      Map<String, Object> map = new HashMap<>();
      map.put("myId", myId);
      List<IndexVlogVO> list = vlogMapperCustom.getMyFollowVlogList(map);
      for (IndexVlogVO vlog : list) {
         String vlogerId = vlog.getVlogerId();
         String vlogId = vlog.getVlogId();
         if (StringUtils.isNotBlank(myId)) {
            // 用户必定关注了该博主
            vlog.setDoIFollowVloger(true);
            // 判断当前用户是否点赞过视频
            vlog.setDoILikeThisVlog(doILikeVlog(myId, vlogId));
         }
         // 获得当前视频被点赞过的总数
         vlog.setLikeCounts(getVlogBeLikedCounts(vlogId));
      }
      return setterPagedGrid(list, page);
   }

   @Override
   public PagedGridResult getMyFriendVlogList(String myId, Integer page, Integer pageSize) {
      PageHelper.startPage(page, pageSize);
      Map<String, Object> map = new HashMap<>();
      map.put("myId", myId);
      List<IndexVlogVO> list = vlogMapperCustom.getMyFriendVlogList(map);
      for (IndexVlogVO vlog : list) {
         String vlogerId = vlog.getVlogerId();
         String vlogId = vlog.getVlogId();
         if (StringUtils.isNotBlank(myId)) {
            // 用户必定关注了该博主
            vlog.setDoIFollowVloger(true);
            // 判断当前用户是否点赞过视频
            vlog.setDoILikeThisVlog(doILikeVlog(myId, vlogId));
         }
         // 获得当前视频被点赞过的总数
         vlog.setLikeCounts(getVlogBeLikedCounts(vlogId));
      }
      return setterPagedGrid(list, page);
   }

   @Transactional
   @Override
   public void flushCounts(String vlogId, Integer counts) {
      Vlog vlog = new Vlog();
      vlog.setId(vlogId);
      vlog.setLikeCounts(counts);
      vlogMapper.updateByPrimaryKeySelective(vlog);
   }

   /**
    * 删除/恢复视频（切换逻辑删除状态）
    * 如果视频是正常状态（is_deleted = 0），则逻辑删除（设置为 1）
    * 如果视频是已逻辑删除状态（is_deleted = 1），则恢复（设置为 0）
    */
   @Transactional
   @Override
   public Vlog deleteOrRestoreVlog(String vlogId) {
      // 管理员可以直接查询视频，不过滤 is_deleted
      Vlog vlog = vlogMapper.selectByPrimaryKey(vlogId);
      if (vlog == null) {
         GraceException.display(ResponseStatusEnum.VLOG_NOT_EXIST_ERROR);
      }
      // 切换逻辑删除状态
      Integer currentIsDeleted = vlog.getIsDeleted();
      Integer newIsDeleted = (currentIsDeleted == null || currentIsDeleted == 0) ? 1 : 0;
      Vlog updateVlog = new Vlog();
      updateVlog.setId(vlogId);
      updateVlog.setIsDeleted(newIsDeleted);
      updateVlog.setUpdatedTime(new Date());
      int result = vlogMapper.updateByPrimaryKeySelective(updateVlog);
      if (result != 1) {
         GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
      }
      // 返回更新后的视频信息
      return vlogMapper.selectByPrimaryKey(vlogId);
   }

   /**
    * 获取所有视频点赞的总数
    * 通过遍历Redis中所有匹配的key，累加点赞数量
    * @return 所有视频点赞的总数
    */
   @Override
   public Integer getTotalLikeCount() {
      try {
         // 获取所有匹配的Redis key
         String pattern = REDIS_VLOG_BE_LIKED_COUNTS + ":*";
         Set<String> keys = redis.keys(pattern);
         
         if (keys == null || keys.isEmpty()) {
            log.info("未找到任何视频点赞的Redis key");
            return 0;
         }
         // 累加所有key的值
         int totalCount = 0;
         for (String key : keys) {
            String countStr = redis.get(key);
            if (StringUtils.isNotBlank(countStr)) {
               try {
                  int count = Integer.parseInt(countStr);
                  totalCount += count;
               } catch (NumberFormatException e) {
                  log.warn("Redis key {} 的值 {} 不是有效的数字，跳过", key, countStr);
               }
            }
         }
         log.info("统计所有视频点赞总数完成，共找到 {} 个视频，总点赞数：{}", keys.size(), totalCount);
         return totalCount;
      } catch (Exception e) {
         log.error("获取所有视频点赞总数时发生错误", e);
         // 发生异常时返回0，避免影响接口调用
         return 0;
      }
   }

   /**
    * 获取所有视频的数量
    * 从数据库中统计所有视频（不包括已逻辑删除的）
    * @return 所有视频的数量
    */
   @Override
   public Integer getTotalVlogCount() {
      try {
         // 使用Example查询，过滤掉已逻辑删除的视频
         Example example = new Example(Vlog.class);
         Example.Criteria criteria = example.createCriteria();
         // 只统计未逻辑删除的视频（is_deleted = 0）
         criteria.andEqualTo("isDeleted", 0);
         int count = vlogMapper.selectCountByExample(example);
         log.info("统计所有视频数量完成，总视频数：{}", count);
         return count;
      } catch (Exception e) {
         log.error("获取所有视频数量时发生错误", e);
         // 发生异常时返回0，避免影响接口调用
         return 0;
      }
   }

}
