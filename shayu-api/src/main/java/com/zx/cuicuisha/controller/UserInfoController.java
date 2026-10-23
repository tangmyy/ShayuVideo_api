package com.zx.cuicuisha.controller;


import com.zx.cuicuisha.MinIOConfig;
import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.bo.UpdatedUserBO;
import com.zx.cuicuisha.enums.FileTypeEnum;
import com.zx.cuicuisha.enums.UserInfoModifyTypeEnum;
import com.zx.cuicuisha.grace.result.ResponseDataStructureEnum;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.pojo.Users;
import com.zx.cuicuisha.service.UserService;
import com.zx.cuicuisha.utils.MinIOUtils;
import com.zx.cuicuisha.vo.UsersVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

   /**
    * 查询自己的用户信息（需要token验证）
    * @param userId 用户ID
    * @param token 用户token（从header获取）
    * @return 用户信息
    */
   @GetMapping("/queryMe")
   public Object queryMe(@RequestParam String userId, @RequestHeader(value = "headerUserToken", required = false) String token) throws Exception {
      // 参数验证
      if (StringUtils.isBlank(userId)) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      // 调用Service层处理业务逻辑（包括token验证、用户查询、统计信息填充）
      UsersVO usersVO = userService.queryMe(userId, token);
      return ResponseDataStructureEnum.ok(usersVO);
   }

   /**
    * 查询用户公开信息（用于查看他人主页，不需要token验证）
    * @param userId 用户ID
    * @return 用户公开信息
    */
   @GetMapping("/queryVloger")
   public Object queryVloger(@RequestParam String userId) throws Exception {
      // 参数验证
      if (StringUtils.isBlank(userId)) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      // 调用Service层处理业务逻辑（用户查询、统计信息填充）
      UsersVO usersVO = userService.queryVloger(userId);
      if (usersVO == null) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
      }
      return ResponseDataStructureEnum.ok(usersVO);
   }

   // 用户信息修改
   @PostMapping("/modifyUserInfo")
   public ResponseDataStructureEnum modifyUserInfo(@RequestBody UpdatedUserBO updatedUserBO, @RequestParam Integer type) throws Exception{
      UserInfoModifyTypeEnum.checkUserInfoTypeIsRight(type);
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

}
