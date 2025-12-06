package com.zx.cuicuisha.impl;

import com.github.pagehelper.PageHelper;
import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.bo.VlogBO;
import com.zx.cuicuisha.enums.YesOrNo;
import com.zx.cuicuisha.mapper.MyLikedVlogMapper;
import com.zx.cuicuisha.mapper.VlogMapper;
import com.zx.cuicuisha.mapper.VlogMapperCustom;
import com.zx.cuicuisha.pojo.MyLikedVlog;
import com.zx.cuicuisha.pojo.Vlog;
import com.zx.cuicuisha.service.FansService;
import com.zx.cuicuisha.service.VlogService;
import com.zx.cuicuisha.utils.PagedGridResult;
import com.zx.cuicuisha.vo.IndexVlogVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
   private Sid sid;

   @Transactional
   @Override
   public void createVlog(VlogBO vlogBO) {
      String vid = sid.nextShort();
      Vlog vlog = new Vlog();
      BeanUtils.copyProperties(vlogBO, vlog);
      vlog.setId(vid);
      vlog.setLikeCounts(0);
      vlog.setCommentsCounts(0);
      vlog.setIsPrivate(YesOrNo.NO.type);
      vlog.setCreatedTime(new Date());
      vlog.setUpdatedTime(new Date());
      vlogMapper.insert(vlog);

   }

   @Override
   public PagedGridResult getIndexVlogList(String userId, String search, Integer page, Integer pageSize) {
      // 开始分页
      PageHelper.startPage(page, pageSize);
      Map<String, Object> map = new HashMap<>();
      if (StringUtils.isNoneBlank(search)){
         map.put("search", search);
      }
      List<IndexVlogVO> list = vlogMapperCustom.getIndexVlogList(map);
      for (IndexVlogVO vlog : list) {
         String vlogerId = vlog.getVlogerId();
         String vlogId = vlog.getVlogId();
         if (StringUtils.isNotBlank(userId)) {
            // 用户是否关注该博主
            boolean doIFollowVloger = fansService.queryDoIFollowVloger(userId, vlogerId);
            vlog.setDoIFollowVloger(doIFollowVloger);
            // 判断当前用户是否点赞过视频
            vlog.setDoILikeThisVlog(doILikeVlog(userId, vlogId));
         }
         // 获得当前视频被点赞过的总数
         vlog.setLikeCounts(getVlogBeLikedCounts(vlogId));
      }
      // return list;
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
   public IndexVlogVO getVlogDetailById(String vlogId) {
      Map<String, Object> map = new HashMap<>();
      map.put("vlogId", vlogId);
      List<IndexVlogVO> list = vlogMapperCustom.getVlogDetailById(map);
      if (list != null && !list.isEmpty()){
         IndexVlogVO vlogVO = list.get(0);
         return vlogVO;
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
      PageHelper.startPage(page, pageSize);
      List<Vlog> list = vlogMapper.selectByExample(example);
      return setterPagedGrid(list, page);
   }

   // 点赞视频
   @Transactional
   @Override
   public void userLikeVlog(String userId, String vlogId) {
      String lVid = sid.nextShort();
      MyLikedVlog likedVlog = new MyLikedVlog();
      likedVlog.setId(lVid);
      likedVlog.setVlogId(vlogId);
      likedVlog.setUserId(userId);
      myLikedVlogMapper.insert(likedVlog);
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
}
