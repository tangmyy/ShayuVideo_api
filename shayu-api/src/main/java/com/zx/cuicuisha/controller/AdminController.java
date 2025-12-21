package com.zx.cuicuisha.controller;

import com.zx.cuicuisha.MinIOConfig;
import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.bo.UpdatedUserBO;
import com.zx.cuicuisha.bo.VlogBO;
import com.zx.cuicuisha.enums.FileTypeEnum;
import com.zx.cuicuisha.enums.UserInfoModifyType;
import com.zx.cuicuisha.grace.result.ResponseDataStructureEnum;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.mo.MessageMO;
import com.zx.cuicuisha.pojo.Users;
import com.zx.cuicuisha.service.*;
import com.zx.cuicuisha.utils.MinIOUtils;
import com.zx.cuicuisha.utils.PagedGridResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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
   @Autowired
   private VlogService vlogService;
   @Autowired
   private CommentService commentService;
   @Autowired
   private FansService fansService;
   @Autowired
   private MsgService msgService;

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
   public ResponseDataStructureEnum usersList(
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
   public ResponseDataStructureEnum updateUsersInfo(@RequestBody UpdatedUserBO updatedUserBO, @RequestParam Integer type) throws Exception{
      UserInfoModifyType.checkUserInfoTypeIsRight(type);
      Users newUserInfo =  userService.updateUserInfo(updatedUserBO, type);
      return ResponseDataStructureEnum.ok(newUserInfo);
   }

   @ApiOperation(value = "修改用户头像或背景图")
   @PostMapping("users/modifyImage")
   public ResponseDataStructureEnum modifyImage(@RequestParam String userId, @RequestParam Integer type, MultipartFile file) throws Exception {
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
    * 管理端：视频列表
    * /admin/vlog/list
    * @param search 搜索关键字（可选）
    * @param page   页码（可选，默认1）
    * @param pageSize 每页数量（可选，默认10）
    * @return 分页结果
    */
   @GetMapping("vlog/list")
   public ResponseDataStructureEnum vlogList(@RequestParam(required = false) String search,
                                              @RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer pageSize) {
      if (page == null) {page = COMMON_START_PAGE;}
      if (pageSize == null) {pageSize = COMMON_PAGE_SIZE;}
      // 管理端不需要指定 userId，这里传空字符串
      PagedGridResult list = vlogService.getIndexVlogList("", search == null ? "" : search, page, pageSize);
      return ResponseDataStructureEnum.ok(list);
   }

   @ApiOperation(value = "修改视频信息")
   @PostMapping("vlog/updateInfo")
   public ResponseDataStructureEnum updateVlogInfo(@RequestBody VlogBO vlogBO) {
      if (vlogBO == null || StringUtils.isBlank(vlogBO.getId())) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      vlogService.updateVlog(vlogBO);
      return ResponseDataStructureEnum.ok();
   }

   /**
    * 管理端：评论列表
    * /admin/comment/list
    * @param vlogId  视频ID（必填）
    * @param userId  当前登录用户ID（可选，用于标记是否点赞；管理端可传空）
    * @param page    页码（可选，默认1）
    * @param pageSize 每页数量（可选，默认10）
    * @return 分页结果
    */
   @GetMapping("comment/list")
   public ResponseDataStructureEnum commentList(@RequestParam String vlogId,
                                              @RequestParam(required = false, defaultValue = "") String userId,
                                              @RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer pageSize) {
      if (page == null) {
         page = COMMON_START_PAGE;
      }
      if (pageSize == null) {
         pageSize = COMMON_PAGE_SIZE;
      }
      return ResponseDataStructureEnum.ok(commentService.queryVlogComments(vlogId, userId, page, pageSize));
   }

   /**
    * 管理端：粉丝列表
    * /admin/fans/list
    * @param myId 用户id（查看该用户的粉丝列表）
    * @param page 页码（可选，默认1）
    * @param pageSize 每页数量（可选，默认10）
    * @return 分页结果
    */
   @GetMapping("fans/list")
   public ResponseDataStructureEnum fansList(@RequestParam String myId,
                                              @RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer pageSize) {
      if (page == null){ page = COMMON_START_PAGE; }
      if (pageSize == null){ pageSize = COMMON_PAGE_SIZE; }
      PagedGridResult gridResult = fansService.queryMyFans(myId, page, pageSize);
      return  ResponseDataStructureEnum.ok(gridResult);
   }


   /**
    * 管理端：消息列表
    * /admin/msg/list
    * @param userId 接收消息的用户id
    * @param page   页码（从0开始，默认0）
    * @param pageSize 每页数量（默认10）
    * @return 当前用户的消息列表
    */
   @GetMapping("msg/list")
   public ResponseDataStructureEnum msgList(@RequestParam String userId,
                                              @RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer pageSize) {
      // mongodb 从0分页，区别于数据库
      if (page == null) {page = COMMON_START_PAGE_ZERO;}
      if (pageSize == null) {pageSize = COMMON_PAGE_SIZE;}
      List<MessageMO> list = msgService.queryList(userId, page, pageSize);
      return ResponseDataStructureEnum.ok(list);
   }

}