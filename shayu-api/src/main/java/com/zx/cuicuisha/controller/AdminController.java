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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;


@Slf4j
@Api(tags = "AdminController 管理后台接口模块")
@RequestMapping("admin")
@RestController
public class AdminController extends BaseInfoProperties {

   @Autowired
   private UserService userService;
   @Autowired
   private MinIOConfig minIOConfig;

   /**
    * 查询用户列表（支持分页和搜索）
    * @param name 搜索名称（昵称或手机号，可选）
    * @param date 注册日期（格式：YYYY-MM-DD，可选）
    * @param page 页码（默认1）
    * @param pageSize 每页数量（默认10）
    * @return 分页结果
    */
   @ApiOperation(value = "查询用户列表（支持分页和搜索）")
   @GetMapping("users/list")
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

   @ApiOperation(value = "修改用户信息")
   @PostMapping("users/updateInfo")
   public ResponseDataStructureEnum modifyUserInfo(@RequestBody UpdatedUserBO updatedUserBO, @RequestParam Integer type) throws Exception{
      UserInfoModifyType.checkUserInfoTypeIsRight(type);
      Users newUserInfo =  userService.updateUserInfo(updatedUserBO, type);
      return ResponseDataStructureEnum.ok(newUserInfo);
   }

   @ApiOperation(value = "修改用户头像或背景图")
   @PostMapping("users/modifyImage")
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