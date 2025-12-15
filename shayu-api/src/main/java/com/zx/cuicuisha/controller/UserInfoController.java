package com.zx.cuicuisha.controller;


import com.zx.cuicuisha.MinIOConfig;
import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.bo.UpdatedUserBO;
import com.zx.cuicuisha.enums.FileTypeEnum;
import com.zx.cuicuisha.enums.UserInfoModifyType;
import com.zx.cuicuisha.grace.result.ResponseDataStructureEnum;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.pojo.Users;
import com.zx.cuicuisha.service.UserService;
import com.zx.cuicuisha.utils.MinIOUtils;
import com.zx.cuicuisha.utils.PagedGridResult;
import com.zx.cuicuisha.vo.UsersVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Slf4j
@Api(tags = "UserInfoController 用户信息接口模块")
@RequestMapping("userInfo")
@RestController
public class UserInfoController extends BaseInfoProperties {

   @Autowired
   private UserService userService;
   @Autowired
   private MinIOConfig minIOConfig;

   @GetMapping("/query")
   public Object query(@RequestParam String userId) throws Exception {
      Users  user = userService.getUser(userId);
      UsersVO usersVO = new UsersVO();
      BeanUtils.copyProperties(user, usersVO);
      // 我的关注总数
      String myFollowsCountsStr = redis.get(REDIS_MY_FOLLOWS_COUNTS + ":" + userId);
      // 我的粉丝总数
      String myFansCountsStr = redis.get(REDIS_MY_FANS_COUNTS + ":" + userId);
      // 我的视频获赞总数
      String likedVlogerCountsStr = redis.get(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + userId);

      Integer myFollowsCounts = 0;
      Integer myFansCounts = 0;
      Integer likedVlogCounts = 0;
      Integer likedVlogerCounts = 0;
      Integer totalLikeMeCounts = 0;

      if (StringUtils.isNotBlank(myFollowsCountsStr)) {
         myFollowsCounts = Integer.valueOf(myFollowsCountsStr);
      }
      if (StringUtils.isNotBlank(myFansCountsStr)) {
         myFansCounts = Integer.valueOf(myFansCountsStr);
      }
      if (StringUtils.isNotBlank(likedVlogerCountsStr)) {
         likedVlogerCounts = Integer.valueOf(likedVlogerCountsStr);
      }
      totalLikeMeCounts = likedVlogCounts + likedVlogerCounts;

      usersVO.setMyFollowsCounts(myFollowsCounts);
      usersVO.setMyFansCounts(myFansCounts);
      usersVO.setTotalLikeMeCounts(totalLikeMeCounts);

      return ResponseDataStructureEnum.ok(usersVO);
   }

   // 用户信息修改
   @PostMapping("/modifyUserInfo")
   public ResponseDataStructureEnum modifyUserInfo(@RequestBody UpdatedUserBO updatedUserBO, @RequestParam Integer type) throws Exception{
      UserInfoModifyType.checkUserInfoTypeIsRight(type);
      Users newUserInfo =  userService.updateUserInfo(updatedUserBO, type);
      return ResponseDataStructureEnum.ok(newUserInfo);
   }

   // 用户 头像 背景图 修改
   @PostMapping("/modifyImage")
   public ResponseDataStructureEnum upload(@RequestParam String userId, @RequestParam Integer type, MultipartFile file) throws Exception {
      if (type != FileTypeEnum.BGIMG.type && type != FileTypeEnum.FACE.type){
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
      }
      String fileName = file.getOriginalFilename();
      MinIOUtils.uploadFile(minIOConfig.getBucketName(), fileName, file.getInputStream());
      String imgUrl = minIOConfig.getFileHost()
            + "/" + minIOConfig.getBucketName()
            + "/" + fileName;
      // 修改图片地址到数据库
      UpdatedUserBO updatedUserBO = new UpdatedUserBO();
      updatedUserBO.setId(userId);
      if (Objects.equals(type, FileTypeEnum.BGIMG.type)){
         updatedUserBO.setBgImg(imgUrl);
      } else {
         updatedUserBO.setFace(imgUrl);
      }
      Users users = userService.updateUserInfo(updatedUserBO);
      return ResponseDataStructureEnum.ok(users);
   }

   /**
    * 查询用户列表（支持分页和搜索）
    * @param name 搜索名称（昵称或手机号，可选）
    * @param date 注册日期（格式：YYYY-MM-DD，可选）
    * @param page 页码（默认1）
    * @param pageSize 每页数量（默认10）
    * @return 分页结果
    */
   @GetMapping("/list")
   public ResponseDataStructureEnum list(
         @RequestParam(required = false) String name,
         @RequestParam(required = false) String date,
         @RequestParam(required = false) Integer page,
         @RequestParam(required = false) Integer pageSize) {
      
      // 设置默认值
      if (page == null) {
         page = COMMON_START_PAGE;
      }
      if (pageSize == null) {
         pageSize = COMMON_PAGE_SIZE;
      }
      
      // 将name参数同时用于昵称和手机号搜索
      String nickname = null;
      String mobile = null;
      if (StringUtils.isNotBlank(name)) {
         // 如果name是纯数字，认为是手机号；否则认为是昵称
         if (name.matches("\\d+")) {
            mobile = name;
         } else {
            nickname = name;
         }
      }
      
      PagedGridResult result = userService.queryUserList(nickname, mobile, date, page, pageSize);
      return ResponseDataStructureEnum.ok(result);
   }

}
