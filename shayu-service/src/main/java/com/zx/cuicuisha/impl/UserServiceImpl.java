package com.zx.cuicuisha.impl;

import com.github.pagehelper.PageHelper;
import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.bo.UpdatedUserBO;
import com.zx.cuicuisha.enums.UserInfoModifyTypeEnum;
import com.zx.cuicuisha.exceptions.GraceException;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.mapper.UsersMapper;
import com.zx.cuicuisha.repository.MessageRepository;
import com.zx.cuicuisha.utils.DesensitizationUtil;
import org.n3r.idworker.Sid;
import com.zx.cuicuisha.pojo.Users;
import com.zx.cuicuisha.service.UserService;
import com.zx.cuicuisha.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.zx.cuicuisha.enums.SexTypeEnum;
import com.zx.cuicuisha.enums.YesOrNo;
import com.zx.cuicuisha.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserServiceImpl extends BaseInfoProperties implements UserService {

   @Autowired
   private UsersMapper usersMapper;
   @Autowired
   private MessageRepository messageRepository;
   @Autowired
   private Sid sid;
   private static final String USER_FACE = "http://106.54.6.213:9000/shayu/zzx.jpg";
   private static final String USER_BGIMG = "http://106.54.6.213:9000/shayu/1765019119.jpg";

   @Override
   public Users queryMobileIsExist(String mobile) {
      Example userExample = new Example(Users.class);
      Example.Criteria criteria = userExample.createCriteria();
      criteria.andEqualTo("mobile", mobile);
      criteria.andEqualTo("isDeleted", 0); // 只查询未逻辑删除的用户
      Users user = usersMapper.selectOneByExample(userExample);
      return user;
   }

   @Override
   @Transactional
   public Users createUser(String mobile) {
      // 获取全局唯一主键
      String userId = sid.nextShort();
      Users user = new Users();

      user.setId(userId);
      user.setMobile(mobile);
      user.setNickname("用户:" + DesensitizationUtil.commonDisplay(mobile));
      user.setVideoNum(DesensitizationUtil.commonDisplay(mobile));
      user.setFace(USER_FACE);
      user.setBirthday(DateUtil.stringToDate("2000-01-01"));
      user.setSex(SexTypeEnum.secret.type);
      user.setCountry("中国");
      user.setProvince("");
      user.setCity("");
      user.setDistrict("");
      user.setBgImg(USER_BGIMG);
      user.setDescription("这家伙很懒，什么都没留下~");
      user.setCanVideoNumBeUpdated(YesOrNo.YES.type);
      user.setStatus(0); // 默认正常状态
      user.setIsDeleted(0); // 默认未逻辑删除
      user.setCreatedTime(new Date());
      user.setUpdatedTime(new Date());

      usersMapper.insert(user);
      return user;
   }

   @Override
   public Users getUser(String userId) {
      Users user = usersMapper.selectByPrimaryKey(userId);
      // 检查用户是否存在且未被逻辑删除
      if (user == null || (user.getIsDeleted() != null && user.getIsDeleted() == 1)) {
         return null;
      }
      return user;
   }

   /**
    * 用户信息修改
    * @param updatedUserBO UpdatedUserBO信息
    * @param type = 修改用户信息类型 枚举
    * @return updatedUserBO
    */
   @Transactional
   @Override
   public Users updateUserInfo(UpdatedUserBO updatedUserBO, Integer type) {
      // 检查用户是否存在且未被逻辑删除
      Users checkUser = getUser(updatedUserBO.getId());
      if (checkUser == null) {
         GraceException.display(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
      }
      // 检查用户是否被封禁
      if (checkUser.getStatus() != null && checkUser.getStatus() == 1) {
         GraceException.display(ResponseStatusEnum.USER_FROZEN);
      }

      Example example = new Example(Users.class);
      Example.Criteria criteria = example.createCriteria();
      if (type == UserInfoModifyTypeEnum.NICKNAME.type){
         criteria.andEqualTo("nickname", updatedUserBO.getNickname());
         criteria.andEqualTo("isDeleted", 0); // 只检查未逻辑删除的用户（昵称唯一性检查） !如果 要检查未被封号的用户,需要处理封号后昵称重构
         Users user = usersMapper.selectOneByExample(example);
         if (user != null){
            GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_NICKNAME_EXIST_ERROR);
         }
      }
      if (type == UserInfoModifyTypeEnum.VIDEONUM.type){
         criteria.andEqualTo("videoNum", updatedUserBO.getVideoNum());
         criteria.andEqualTo("isDeleted", 0); // 只检查未逻辑删除的用户（视频号唯一性检查） !如果 要检查未被封号的用户,需要处理封号后视频号重构
         Users user = usersMapper.selectOneByExample(example);
         if (user != null){
            // 判断是否已经存在这个视频号
            GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_VIDEONUM_EXIST_ERROR);
         }
         Users tempUser =  getUser(updatedUserBO.getId());
         if (tempUser.getCanVideoNumBeUpdated() ==  YesOrNo.NO.type){
            GraceException.display(ResponseStatusEnum.USER_INFO_CANT_UPDATED_VIDEONUM_ERROR);
         }
         // 不再能修改视频号
         updatedUserBO.setCanVideoNumBeUpdated(YesOrNo.NO.type);
      }
      return updateUserInfo(updatedUserBO);
   }

   /**
    * 用户 头像/背景图 修改
    * @param updatedUserBO UpdatedUserBO信息
    * @return updatedUserBO.getId
    */
   @Transactional
   @Override
   public Users updateUserInfo(UpdatedUserBO updatedUserBO) {
      // 检查用户是否存在且未被逻辑删除
      Users checkUser = getUser(updatedUserBO.getId());
      if (checkUser == null) {
         GraceException.display(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
      }
      // 检查用户是否被封禁
      if (checkUser.getStatus() != null && checkUser.getStatus() == 1) {
         GraceException.display(ResponseStatusEnum.USER_FROZEN);
      }

      Users pendingUser = new Users();
      BeanUtils.copyProperties(updatedUserBO, pendingUser);
      int result = usersMapper.updateByPrimaryKeySelective(pendingUser);
      if (result != 1) {
         GraceException.display(ResponseStatusEnum.USER_UPDATE_ERROR);
      }
      return getUser(updatedUserBO.getId());
   }

   /**
    * 管理后台修改用户信息（允许修改已逻辑删除的用户和status、isDeleted字段）
    */
   @Transactional
   @Override
   public Users updateUserInfoForAdmin(UpdatedUserBO updatedUserBO, Integer type) {
      // 管理后台可以直接查询用户，不过滤 is_deleted
      Users existUser = usersMapper.selectByPrimaryKey(updatedUserBO.getId());
      if (existUser == null) {
         GraceException.display(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
      }

      Example example = new Example(Users.class);
      Example.Criteria criteria = example.createCriteria();
      if (type == UserInfoModifyTypeEnum.NICKNAME.type){
         criteria.andEqualTo("nickname", updatedUserBO.getNickname());
         criteria.andEqualTo("isDeleted", 0); // 只检查未逻辑删除的用户（昵称唯一性检查）  !如果 要检查未被封号的用户,需要处理封号后昵称重构
         Users user = usersMapper.selectOneByExample(example);
         if (user != null && !user.getId().equals(updatedUserBO.getId())){
            GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_NICKNAME_EXIST_ERROR);
         }
      }
      if (type == UserInfoModifyTypeEnum.VIDEONUM.type){
         criteria.andEqualTo("videoNum", updatedUserBO.getVideoNum());
         criteria.andEqualTo("isDeleted", 0); // 只检查未逻辑删除的用户（视频号唯一性检查） !如果 要检查未被封号的用户,需要处理封号后视频号重构
         Users user = usersMapper.selectOneByExample(example);
         if (user != null && !user.getId().equals(updatedUserBO.getId())){
            GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_VIDEONUM_EXIST_ERROR);
         }
         if (existUser.getCanVideoNumBeUpdated() == YesOrNo.NO.type){
            GraceException.display(ResponseStatusEnum.USER_INFO_CANT_UPDATED_VIDEONUM_ERROR);
         }
         // 不再能修改视频号
         updatedUserBO.setCanVideoNumBeUpdated(YesOrNo.NO.type);
      }
      Users pendingUser = new Users();
      BeanUtils.copyProperties(updatedUserBO, pendingUser);
      // 管理后台可以更新 status 和 isDeleted 字段
      pendingUser.setUpdatedTime(new Date());
      int result = usersMapper.updateByPrimaryKeySelective(pendingUser);
      if (result != 1) {
         GraceException.display(ResponseStatusEnum.USER_UPDATE_ERROR);
      }
      // 返回更新后的用户信息（不过滤 is_deleted）
      return usersMapper.selectByPrimaryKey(updatedUserBO.getId());
   }

   /**
    * 管理后台修改用户信息（不带type参数，用于修改头像、背景图等）
    */
   @Transactional
   @Override
   public Users updateUserInfoForAdmin(UpdatedUserBO updatedUserBO) {
      // 管理后台可以直接查询用户，不过滤 is_deleted
      Users existUser = usersMapper.selectByPrimaryKey(updatedUserBO.getId());
      if (existUser == null) {
         GraceException.display(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
      }

      Users pendingUser = new Users();
      BeanUtils.copyProperties(updatedUserBO, pendingUser);
      // 管理后台可以更新 status 和 isDeleted 字段
      pendingUser.setUpdatedTime(new Date());
      int result = usersMapper.updateByPrimaryKeySelective(pendingUser);
      if (result != 1) {
         GraceException.display(ResponseStatusEnum.USER_UPDATE_ERROR);
      }
      // 返回更新后的用户信息（不过滤 is_deleted）
      return usersMapper.selectByPrimaryKey(updatedUserBO.getId());
   }

   /**
    * 管理后台查询用户列表（不过滤已逻辑删除的用户）
    */
   @Override
   public PagedGridResult queryUserListForAdmin(String nickname, String mobile, String date, Integer page, Integer pageSize) {
      // 开始分页
      PageHelper.startPage(page, pageSize);
      // 构建查询条件
      Example example = new Example(Users.class);
      Example.Criteria criteria = example.createCriteria();
      // 管理后台不过滤 is_deleted，可以查询所有用户
      // 昵称模糊搜索
      if (StringUtils.isNotBlank(nickname)) {
         criteria.andLike("nickname", "%" + nickname + "%");
      }
      // 手机号模糊搜索
      if (StringUtils.isNotBlank(mobile)) {
         criteria.andLike("mobile", "%" + mobile + "%");
      }
      // 注册日期搜索（按创建日期）
      if (StringUtils.isNotBlank(date)) {
         try {
            Date searchDate = DateUtil.stringToDate(date);
            if (searchDate != null) {
               // 查询当天的数据（从00:00:00到23:59:59）
               Calendar startCal = Calendar.getInstance();
               startCal.setTime(searchDate);
               startCal.set(Calendar.HOUR_OF_DAY, 0);
               startCal.set(Calendar.MINUTE, 0);
               startCal.set(Calendar.SECOND, 0);
               startCal.set(Calendar.MILLISECOND, 0);
               Date startDate = startCal.getTime();
               
               Calendar endCal = Calendar.getInstance();
               endCal.setTime(searchDate);
               endCal.set(Calendar.HOUR_OF_DAY, 23);
               endCal.set(Calendar.MINUTE, 59);
               endCal.set(Calendar.SECOND, 59);
               endCal.set(Calendar.MILLISECOND, 999);
               Date endDate = endCal.getTime();
               
               criteria.andBetween("createdTime", startDate, endDate);
            }
         } catch (Exception e) {
            // 日期格式错误，忽略该条件
         }
      }
      // 按创建时间倒序排列
      example.setOrderByClause("created_time DESC");
      // 执行查询
      List<Users> list = usersMapper.selectByExample(example);
      // 返回分页结果
      return setterPagedGrid(list, page);
   }

   /**
    * 删除/恢复用户（切换逻辑删除状态）
    * 如果用户是正常状态（is_deleted = 0），则逻辑删除（设置为 1）
    * 如果用户是已逻辑删除状态（is_deleted = 1），则恢复（设置为 0）
    */
   @Transactional
   @Override
   public Users deleteOrRestoreUser(String userId) {
      // 管理后台可以直接查询用户，不过滤 is_deleted
      Users user = usersMapper.selectByPrimaryKey(userId);
      if (user == null) {
         GraceException.display(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
      }
      // 切换逻辑删除状态
      Integer currentIsDeleted = user.getIsDeleted();
      Integer newIsDeleted = (currentIsDeleted == null || currentIsDeleted == 0) ? 1 : 0;
      Users updateUser = new Users();
      updateUser.setId(userId);
      updateUser.setIsDeleted(newIsDeleted);
      updateUser.setUpdatedTime(new Date());
      int result = usersMapper.updateByPrimaryKeySelective(updateUser);
      if (result != 1) {
         GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
      }
      // 如果是删除操作，删除Redis中的相关缓存和MongoDB中的消息
      if (newIsDeleted == 1) {
         try {
            redis.delete(REDIS_USER_TOKEN + ":" + userId);
            redis.delete(REDIS_MY_FOLLOWS_COUNTS + ":" + userId);
            redis.delete(REDIS_MY_FANS_COUNTS + ":" + userId);
            redis.delete(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + userId);
            log.info("删除用户缓存成功：userId={}", userId);
         } catch (Exception e) {
            log.error("删除用户缓存失败：userId={}", userId, e);
            // 缓存删除失败不影响主流程，只记录日志
         }
         // 删除MongoDB中该用户收到的所有消息
         try {
            messageRepository.deleteAllByToUserId(userId);
            log.info("删除用户消息成功：userId={}", userId);
         } catch (Exception e) {
            log.error("删除用户消息失败：userId={}", userId, e);
            // 消息删除失败不影响主流程，只记录日志
         }
      }

      return usersMapper.selectByPrimaryKey(userId);
   }

   /**
    * 封禁/恢复用户（切换用户状态）
    * 如果用户是正常状态（status = 0），则封禁（设置为 1）
    * 如果用户是封禁状态（status = 1），则恢复（设置为 0）
    */
   @Transactional
   @Override
   public Users banOrRestoreUser(String userId) {
      // 管理后台可以直接查询用户，不过滤 is_deleted
      Users user = usersMapper.selectByPrimaryKey(userId);
      if (user == null) {
         GraceException.display(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
      }
      // 切换用户状态
      Integer currentStatus = user.getStatus();
      Integer newStatus = (currentStatus == null || currentStatus == 0) ? 1 : 0;
      Users updateUser = new Users();
      updateUser.setId(userId);
      updateUser.setStatus(newStatus);
      updateUser.setUpdatedTime(new Date());
      int result = usersMapper.updateByPrimaryKeySelective(updateUser);
      if (result != 1) {
         GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
      }
      // 返回更新后的用户信息
      return usersMapper.selectByPrimaryKey(userId);
   }

   /**
    * 获取所有用户的数量
    * 从数据库中统计所有用户（不包括已逻辑删除的）
    * @return 所有用户的数量
    */
   @Override
   public Integer getTotalUserCount() {
      try {
         // 使用Example查询，过滤掉已逻辑删除的用户
         Example example = new Example(Users.class);
         Example.Criteria criteria = example.createCriteria();
         // 只统计未逻辑删除的用户（is_deleted = 0）
         criteria.andEqualTo("isDeleted", 0);
         int count = usersMapper.selectCountByExample(example);
         log.info("统计所有用户数量完成，总用户数：{}", count);
         return count;
      } catch (Exception e) {
         log.error("获取所有用户数量时发生错误", e);
         // 发生异常时返回0，避免影响接口调用
         return 0;
      }
   }

}
