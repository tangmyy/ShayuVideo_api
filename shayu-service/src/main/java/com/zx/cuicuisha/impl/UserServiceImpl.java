package com.zx.cuicuisha.impl;

import com.github.pagehelper.PageHelper;
import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.bo.UpdatedUserBO;
import com.zx.cuicuisha.enums.UserInfoModifyType;
import com.zx.cuicuisha.exceptions.GraceException;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.mapper.UsersMapper;
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
import com.zx.cuicuisha.enums.Sex;
import com.zx.cuicuisha.enums.YesOrNo;
import com.zx.cuicuisha.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserServiceImpl extends BaseInfoProperties implements UserService {

   @Autowired
   private UsersMapper usersMapper;
   @Autowired
   private Sid sid;
   private static final String USER_FACE = "http://106.54.6.213:9000/shayu/zzx.jpg";
   private static final String USER_BGIMG = "http://106.54.6.213:9000/shayu/1765019119.jpg";

   @Override
   public Users queryMobileIsExist(String mobile) {
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
      user.setSex(Sex.secret.type);
      user.setCountry("中国");
      user.setProvince("");
      user.setCity("");
      user.setDistrict("");
      user.setBgImg(USER_BGIMG);
      user.setDescription("这家伙很懒，什么都没留下~");
      user.setCanVideoNumBeUpdated(YesOrNo.YES.type);
      user.setCreatedTime(new Date());
      user.setUpdatedTime(new Date());

      usersMapper.insert(user);
      return user;
   }

   @Override
   public Users getUser(String userId) {
      return usersMapper.selectByPrimaryKey(userId);
   }

   @Transactional
   @Override
   public Users updateUserInfo(UpdatedUserBO updatedUserBO) {

      Users pendingUser = new Users();
      BeanUtils.copyProperties(updatedUserBO, pendingUser);

      int result = usersMapper.updateByPrimaryKeySelective(pendingUser);
      if (result != 1) {
         GraceException.display(ResponseStatusEnum.USER_UPDATE_ERROR);
      }
      return getUser(updatedUserBO.getId());
   }

   @Transactional
   @Override
   public Users updateUserInfo(UpdatedUserBO updatedUserBO, Integer type) {
      Example example = new Example(Users.class);
      Example.Criteria criteria = example.createCriteria();

      if (type == UserInfoModifyType.NICKNAME.type){
         criteria.andEqualTo("nickname", updatedUserBO.getNickname());
         Users user = usersMapper.selectOneByExample(example);
         if (user != null){
            GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_NICKNAME_EXIST_ERROR);
         }
      }
      if (type == UserInfoModifyType.VIDEONUM.type){
         criteria.andEqualTo("videoNum", updatedUserBO.getVideoNum());
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

   @Override
   public PagedGridResult queryUserList(String nickname, String mobile, String date, Integer page, Integer pageSize) {
      // 开始分页
      PageHelper.startPage(page, pageSize);
      
      // 构建查询条件
      Example example = new Example(Users.class);
      Example.Criteria criteria = example.createCriteria();
      
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

   @Transactional
   @Override
   public void deleteUser(String userId) {
      // 检查用户是否存在
      Users user = getUser(userId);
      if (user == null) {
         GraceException.display(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
      }
      
      // 删除用户
      int result = usersMapper.deleteByPrimaryKey(userId);
      if (result != 1) {
         GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
      }
      
      // 删除Redis中的相关缓存
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
   }

}
