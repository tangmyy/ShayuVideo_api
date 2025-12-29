package com.zx.cuicuisha.impl;

import com.github.pagehelper.PageHelper;
import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.bo.UpdatedUserBO;
import com.zx.cuicuisha.enums.UserInfoModifyTypeEnum;
import com.zx.cuicuisha.exceptions.GraceException;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.mapper.UsersMapper;
import com.zx.cuicuisha.mapper.VlogMapper;
import com.zx.cuicuisha.mapper.CommentMapper;
import com.zx.cuicuisha.mapper.MyLikedVlogMapper;
import com.zx.cuicuisha.mapper.FansMapper;
import com.zx.cuicuisha.mapper.ReportMapper;
import com.zx.cuicuisha.pojo.Vlog;
import com.zx.cuicuisha.pojo.Comment;
import com.zx.cuicuisha.pojo.MyLikedVlog;
import com.zx.cuicuisha.pojo.Fans;
import com.zx.cuicuisha.pojo.Report;
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
   private VlogMapper vlogMapper;
   @Autowired
   private CommentMapper commentMapper;
   @Autowired
   private MyLikedVlogMapper myLikedVlogMapper;
   @Autowired
   private FansMapper fansMapper;
   @Autowired
   private ReportMapper reportMapper;
   @Autowired
   private MessageRepository messageRepository;
   @Autowired
   private Sid sid;
   private static final String USER_FACE = "http://106.54.6.213:9000/shayu/zzx.jpg";
   private static final String USER_BGIMG = "http://106.54.6.213:9000/shayu/1765019119.jpg";

   /**
    * 查询用户（只查询未逻辑删除的用户）
    * @param mobile 手机号
    * @return 用户信息，如果不存在则返回null
    */
   @Override
   public Users queryMobileIsExist(String mobile) {
      Example userExample = new Example(Users.class);
      Example.Criteria criteria = userExample.createCriteria();
      criteria.andEqualTo("mobile", mobile);
      criteria.andEqualTo("isDeleted", 0);
      Users user = usersMapper.selectOneByExample(userExample);
      return user;
   }

   /**
    * 查询用户（包括已逻辑删除的用户）
    * @param mobile 手机号
    * @return 用户信息，如果不存在则返回null
    */
   @Override
   public Users queryUserByMobileIncludeDeleted(String mobile) {
      Example userExample = new Example(Users.class);
      Example.Criteria criteria = userExample.createCriteria();
      criteria.andEqualTo("mobile", mobile);
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
    * 删除用户（逻辑删除并清理相关数据）
    * 如果用户是正常状态（is_deleted = 0），则逻辑删除（设置为 1）并清理所有相关数据
    * 如果用户已经是已逻辑删除状态（is_deleted = 1），则不做任何操作
    */
   @Transactional
   @Override
   public Users deleteOrRestoreUser(String userId) {
      // 管理后台可以直接查询用户，不过滤 is_deleted
      Users user = usersMapper.selectByPrimaryKey(userId);
      if (user == null) {
         GraceException.display(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
      }
      // 如果已经是删除状态，直接返回
      Integer currentIsDeleted = user.getIsDeleted();
      if (currentIsDeleted != null && currentIsDeleted == 1) {
         log.warn("用户已经是删除状态，无需重复删除：userId={}", userId);
         return user;
      }
      // 逻辑删除用户
      Users updateUser = new Users();
      updateUser.setId(userId);
      updateUser.setIsDeleted(1);
      updateUser.setUpdatedTime(new Date());
      int result = usersMapper.updateByPrimaryKeySelective(updateUser);
      if (result != 1) {
         GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
      }
      // 删除/重置用户的所有相关数据
      {
         try {
            // 1. 删除Redis缓存
            redis.delete(REDIS_USER_TOKEN + ":" + userId);
            redis.delete(REDIS_MY_FOLLOWS_COUNTS + ":" + userId);
            redis.delete(REDIS_MY_FANS_COUNTS + ":" + userId);
            redis.delete(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + userId);
            log.info("删除用户Redis缓存成功：userId={}", userId);
         } catch (Exception e) {
            log.error("删除用户Redis缓存失败：userId={}", userId, e);
            // 缓存删除失败不影响主流程，只记录日志
         }
         try {
            // 2. 重置用户个人信息（保留手机号）
            Users resetUser = new Users();
            resetUser.setId(userId);
            resetUser.setNickname("用户:" + DesensitizationUtil.commonDisplay(user.getMobile()));
            resetUser.setVideoNum(DesensitizationUtil.commonDisplay(user.getMobile()));
            resetUser.setFace(USER_FACE);
            resetUser.setBirthday(DateUtil.stringToDate("2000-01-01"));
            resetUser.setSex(SexTypeEnum.secret.type);
            resetUser.setCountry("中国");
            resetUser.setProvince("");
            resetUser.setCity("");
            resetUser.setDistrict("");
            resetUser.setBgImg(USER_BGIMG);
            resetUser.setDescription("这家伙很懒，什么都没留下~");
            resetUser.setCanVideoNumBeUpdated(YesOrNo.YES.type);
            resetUser.setStatus(0);
            resetUser.setUpdatedTime(new Date());
            usersMapper.updateByPrimaryKeySelective(resetUser);
            log.info("重置用户个人信息成功：userId={}", userId);
         } catch (Exception e) {
            log.error("重置用户个人信息失败：userId={}", userId, e);
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
         }
         try {
            // 3. 逻辑删除用户发布的视频
            Example vlogExample = new Example(Vlog.class);
            Example.Criteria vlogCriteria = vlogExample.createCriteria();
            vlogCriteria.andEqualTo("vlogerId", userId);
            Vlog deleteVlog = new Vlog();
            deleteVlog.setIsDeleted(1);
            deleteVlog.setUpdatedTime(new Date());
            vlogMapper.updateByExampleSelective(deleteVlog, vlogExample);
            log.info("逻辑删除用户发布的视频成功：userId={}", userId);
         } catch (Exception e) {
            log.error("逻辑删除用户发布的视频失败：userId={}", userId, e);
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
         }
         try {
            // 4. 逻辑删除用户发布的评论
            Example commentExample = new Example(Comment.class);
            Example.Criteria commentCriteria = commentExample.createCriteria();
            commentCriteria.andEqualTo("commentUserId", userId);
            Comment deleteComment = new Comment();
            deleteComment.setIsDeleted(1);
            commentMapper.updateByExampleSelective(deleteComment, commentExample);
            log.info("逻辑删除用户发布的评论成功：userId={}", userId);
         } catch (Exception e) {
            log.error("逻辑删除用户发布的评论失败：userId={}", userId, e);
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
         }
         try {
            // 5. 删除用户点赞的记录
            Example likedVlogExample = new Example(MyLikedVlog.class);
            Example.Criteria likedVlogCriteria = likedVlogExample.createCriteria();
            likedVlogCriteria.andEqualTo("userId", userId);
            myLikedVlogMapper.deleteByExample(likedVlogExample);
            log.info("删除用户点赞记录成功：userId={}", userId);
         } catch (Exception e) {
            log.error("删除用户点赞记录失败：userId={}", userId, e);
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
         }
         try {
            // 6. 删除用户的关注关系（作为博主和作为粉丝）
            Example fansExample1 = new Example(Fans.class);
            Example.Criteria fansCriteria1 = fansExample1.createCriteria();
            fansCriteria1.andEqualTo("vlogerId", userId);
            fansMapper.deleteByExample(fansExample1);
            Example fansExample2 = new Example(Fans.class);
            Example.Criteria fansCriteria2 = fansExample2.createCriteria();
            fansCriteria2.andEqualTo("fanId", userId);
            fansMapper.deleteByExample(fansExample2);
            log.info("删除用户关注关系成功：userId={}", userId);
         } catch (Exception e) {
            log.error("删除用户关注关系失败：userId={}", userId, e);
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
         }
         try {
            // 7. 逻辑删除用户发起的举报记录
            Example reportExample = new Example(Report.class);
            Example.Criteria reportCriteria = reportExample.createCriteria();
            reportCriteria.andEqualTo("reporterId", userId);
            Report deleteReport = new Report();
            deleteReport.setIsDeleted(1);
            reportMapper.updateByExampleSelective(deleteReport, reportExample);
            log.info("逻辑删除用户发起的举报记录成功：userId={}", userId);
         } catch (Exception e) {
            log.error("逻辑删除用户发起的举报记录失败：userId={}", userId, e);
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
         }
         try {
            // 8. 删除MongoDB中该用户发送和接收的所有消息
            messageRepository.deleteAllByToUserId(userId);
            messageRepository.deleteAllByFromUserId(userId);
            log.info("删除用户MongoDB消息成功：userId={}", userId);
         } catch (Exception e) {
            log.error("删除用户MongoDB消息失败：userId={}", userId, e);
            // 消息删除失败不影响主流程，只记录日志
         }
      }
      return usersMapper.selectByPrimaryKey(userId);
   }

   /**
    * 恢复用户 - 已逻辑删除的
    * @param userId 用户ID
    * @return 恢复后的用户信息
    */
   @Transactional
   @Override
   public Users restoreDeletedUser(String userId) {
      Users existUser = usersMapper.selectByPrimaryKey(userId);
      if (existUser == null) {
         GraceException.display(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
      }
      // 如果用户未删除，直接返回
      if (existUser.getIsDeleted() == null || existUser.getIsDeleted() == 0) {
         log.warn("用户未删除，无需恢复：userId={}", userId);
         return existUser;
      }
      // 恢复用户（设置 isDeleted = 0）
      Users updateUser = new Users();
      updateUser.setId(userId);
      updateUser.setIsDeleted(0);
      updateUser.setUpdatedTime(new Date());
      int result = usersMapper.updateByPrimaryKeySelective(updateUser);
      if (result != 1) {
         GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
      }
      // 返回恢复后的用户信息
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
