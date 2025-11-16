package com.zx.cuicuisha.impl;

// import org.springframework.data.domain.Example; 同名 没用
import com.zx.cuicuisha.mapper.UsersMapper;
import com.zx.cuicuisha.utils.DesensitizationUtil;
import org.n3r.idworker.Sid;
import com.zx.cuicuisha.pojo.Users;
import com.zx.cuicuisha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import java.util.Date;
import com.zx.cuicuisha.enums.Sex;
import com.zx.cuicuisha.enums.YesOrNo;
import com.zx.cuicuisha.utils.DateUtil;


// import com.zx.cuicuisha.bo.UpdatedUserBO;
// import com.zx.cuicuisha.vo.UsersVO;
// import com.zx.cuicuisha.enums.UserInfoModifyType;
// import com.zx.cuicuisha.exceptions.GraceException;
// import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
// import org.springframework.beans.BeanUtils;
// import java.lang.reflect.Type;


@Service
public class UserServiceImpl implements UserService {

   @Autowired
   private UsersMapper usersMapper;
   @Autowired
   private Sid sid;
   private static final String USER_FACE1 = "https://c-ssl.duitang.com/uploads/blog/202211/15/20221115121911_a9ab2.png";


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
      user.setImoocNum("用户：" + DesensitizationUtil.commonDisplay(mobile));
      user.setFace(USER_FACE1);
      user.setBirthday(DateUtil.stringToDate("1900-01-01"));
      user.setSex(Sex.secret.type);
      user.setCountry("中国");
      user.setProvince("");
      user.setCity("");
      user.setDistrict("");
      user.setDescription("这家伙很懒，什么都没留下~");
      user.setCanImoocNumBeUpdated(YesOrNo.YES.type);
      user.setCreatedTime(new Date());
      user.setUpdatedTime(new Date());

      usersMapper.insert(user);

      return user;
   }

}
