package com.zx.cuicuisha.controller;

import com.zx.cuicuisha.MinIOConfig;
import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.bo.CommentUpdateBO;
import com.zx.cuicuisha.mo.MessageMO;
import com.zx.cuicuisha.mo.MessageUpdateMO;
import com.zx.cuicuisha.bo.UpdatedUserBO;
import com.zx.cuicuisha.bo.VlogBO;
import com.zx.cuicuisha.enums.FileTypeEnum;
import com.zx.cuicuisha.enums.UserInfoModifyTypeEnum;
import com.zx.cuicuisha.enums.VlogSortTypeEnum;
import com.zx.cuicuisha.grace.result.ResponseDataStructureEnum;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.bo.AdminLoginBO;
import com.zx.cuicuisha.pojo.AdminUsers;
import com.zx.cuicuisha.pojo.Comment;
import com.zx.cuicuisha.pojo.Report;
import com.zx.cuicuisha.pojo.Users;
import com.zx.cuicuisha.service.*;
import com.zx.cuicuisha.vo.AdminUsersVO;
import com.zx.cuicuisha.utils.MinIOUtils;
import com.zx.cuicuisha.utils.PagedGridResult;
import com.zx.cuicuisha.vo.UsersVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
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
   @Autowired
   private ReportService reportService;
   @Autowired
   private AdminService adminService;

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
      if (page == null) {page = COMMON_START_PAGE;}
      if (pageSize == null) {pageSize = COMMON_PAGE_SIZE;}

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

      // 管理后台使用专用方法，可以查询所有用户（包括已逻辑删除的）
      PagedGridResult result = userService.queryUserListForAdmin(nickname, mobile, date, page, pageSize);
      return ResponseDataStructureEnum.ok(result);
   }

   @ApiOperation(value = "修改用户信息（管理后台专用，可修改status和isDeleted字段）")
   @PostMapping("users/updateInfo")
   public ResponseDataStructureEnum updateUsersInfo(@RequestBody UpdatedUserBO updatedUserBO, @RequestParam Integer type) throws Exception{
      UserInfoModifyTypeEnum.checkUserInfoTypeIsRight(type);
      // 管理后台使用专用方法，可以修改已逻辑删除的用户和status、isDeleted字段
      Users newUserInfo = userService.updateUserInfoForAdmin(updatedUserBO, type);
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
      // 管理后台使用专用方法，可以修改已逻辑删除的用户
      Users users = userService.updateUserInfoForAdmin(updatedUserBO);
      return ResponseDataStructureEnum.ok(users);
   }

   @ApiOperation(value = "删除/恢复用户（切换逻辑删除状态）")
   @PostMapping("users/deleteOrRestore")
   public ResponseDataStructureEnum deleteOrRestoreUser(@RequestParam String userId) {
      if (StringUtils.isBlank(userId)) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      // 查询用户当前状态（包括已删除的用户）
      Users currentUser = userService.getUserIncludeDeleted(userId);
      if (currentUser == null) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
      }
      // 根据当前状态决定是删除还是恢复
      Users user;
      if (currentUser.getIsDeleted() != null && currentUser.getIsDeleted() == 1) {
         // 当前是删除状态，执行恢复
         user = userService.restoreDeletedUser(userId);
      } else {
         // 当前是正常状态，执行删除
         user = userService.deleteUser(userId);
      }
      return ResponseDataStructureEnum.ok(user);
   }

   @ApiOperation(value = "封禁/恢复用户（切换用户状态）")
   @PostMapping("users/banOrRestore")
   public ResponseDataStructureEnum banOrRestoreUser(@RequestParam String userId) {
      if (StringUtils.isBlank(userId)) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      Users user = userService.banOrRestoreUser(userId);
      return ResponseDataStructureEnum.ok(user);
   }

   @ApiOperation(value = "新增用户")
   @PostMapping("users/adduser")
   public ResponseDataStructureEnum adduser(@RequestParam String mobile) {
      if (StringUtils.isBlank(mobile)) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      // 查询数据库, 判断用户是否存在（包括已逻辑删除的用户）
      Users user = userService.queryUserByMobileIncludeDeleted(mobile);
      if (user == null) {
         // 如果完全不存在, 则创建新用户
         log.info("手机号 {} 不存在，开始创建新用户", mobile);
         user = userService.createUser(mobile);
         log.info("用户创建成功，用户ID: {}", user.getId());
      } else if (user.getIsDeleted() != null && user.getIsDeleted() == 1) {
         // 如果找到被逻辑删除的用户, 恢复该用户
         log.info("手机号 {} 对应的用户曾被逻辑删除，正在恢复用户，用户ID: {}", mobile, user.getId());
         user = userService.restoreDeletedUser(user.getId());
         log.info("用户恢复成功，用户ID: {}", user.getId());
      }
      // 如果用户存在且未删除，直接使用
      // 返回用户信息
      log.info("手机号 {} 已存在，返回现有用户信息，用户ID: {}", mobile, user.getId());
      UsersVO usersVO = new UsersVO();
      BeanUtils.copyProperties(user, usersVO);
      return ResponseDataStructureEnum.ok(usersVO);
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
      // 使用管理员专用查询方法，可以查询所有视频（包括已逻辑删除的）
      PagedGridResult list = vlogService.getIndexVlogListForAdmin(search == null ? "" : search, page, pageSize);
      return ResponseDataStructureEnum.ok(list);
   }

   @ApiOperation(value = "编辑视频信息")
   @PostMapping("vlog/updateInfo")
   public ResponseDataStructureEnum updateVlogInfo(@RequestBody VlogBO vlogBO) {
      if (vlogBO == null || StringUtils.isBlank(vlogBO.getId())) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      vlogService.updateVlog(vlogBO);
      return ResponseDataStructureEnum.ok();
   }

   @ApiOperation(value = "删除/恢复视频（切换逻辑删除状态）")
   @PostMapping("vlog/deleteOrRestore")
   public ResponseDataStructureEnum deleteOrRestoreVlog(@RequestParam String vlogId) {
      if (StringUtils.isBlank(vlogId)) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      com.zx.cuicuisha.pojo.Vlog vlog = vlogService.deleteOrRestoreVlog(vlogId);
      return ResponseDataStructureEnum.ok(vlog);
   }

   /**
    * 审核接口-封禁/解封视频
    * @param vlogId 视频ID
    * @param auditStatus 审核状态 0-待审核 1-通过 2-拒绝（封禁）
    * @param adminId 管理员ID
    * @param auditRemark 审核备注（可选）
    * @return 操作结果
    */
   @ApiOperation(value = "审核视频（封禁/解封视频）")
   @PostMapping("vlog/audit")
   public ResponseDataStructureEnum auditVlog(@RequestParam String vlogId,
                                               @RequestParam Integer auditStatus,
                                               @RequestParam Long adminId,
                                               @RequestParam(required = false) String auditRemark) {
      if (StringUtils.isBlank(vlogId)) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      if (auditStatus == null) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      if (adminId == null) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      // 验证审核状态值（0-待审核，1-通过，2-拒绝）
      if (auditStatus < 0 || auditStatus > 2) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      vlogService.auditVlog(vlogId, auditStatus, adminId, auditRemark);
      return ResponseDataStructureEnum.ok();
   }

   @ApiOperation(value = "获取推荐视频流排序规则")
   @GetMapping("vlog/getSortType")
   public ResponseDataStructureEnum getVlogSortType() {
      String sortTypeStr = redis.get(REDIS_VLOG_INDEX_SORT_TYPE);
      Integer sortType = null;
      if (StringUtils.isNotBlank(sortTypeStr)) {
         try {
            sortType = Integer.valueOf(sortTypeStr);
         } catch (Exception e) {
            // 使用默认值
         }
      }
      VlogSortTypeEnum vlogSortTypeEnum = VlogSortTypeEnum.getByTypeWithDefault(sortType);
      return ResponseDataStructureEnum.ok(vlogSortTypeEnum);
   }

   @ApiOperation(value = "设置推荐视频流排序规则（0-创建时间，1-点赞数，2-评论数）")
   @PostMapping("vlog/setSortType")
   public ResponseDataStructureEnum setVlogSortType(@RequestParam Integer sortType) {
      if (sortType == null) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      // 验证排序类型是否有效（0、1、2）
      VlogSortTypeEnum vlogSortTypeEnum = VlogSortTypeEnum.getByType(sortType);
      if (vlogSortTypeEnum == null) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      // 保存到Redis
      redis.set(REDIS_VLOG_INDEX_SORT_TYPE, String.valueOf(sortType));
      return ResponseDataStructureEnum.ok(vlogSortTypeEnum);
   }

   /**
    * 管理端：评论列表
    * /admin/comment/list
    * @param vlogId  视频ID（可选，如果为空则查询所有评论，否则查询指定视频的评论）
    * @param userId  评论者ID（可选，如果为空则不按用户筛选，否则查询指定用户发表的评论）
    * @param page    页码（可选，默认1）
    * @param pageSize 每页数量（可选，默认10）
    * @return 分页结果
    */
   @ApiOperation(value = "查询评论列表（支持查询所有、按视频或按用户筛选）")
   @GetMapping("comment/list")
   public ResponseDataStructureEnum commentList(@RequestParam(required = false) String vlogId,
                                              @RequestParam(required = false) String userId,
                                              @RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer pageSize) {
      if (page == null) {page = COMMON_START_PAGE;}
      if (pageSize == null) {pageSize = COMMON_PAGE_SIZE;}
      return ResponseDataStructureEnum.ok(commentService.queryAllComments(vlogId, userId, page, pageSize));
   }

   @ApiOperation(value = "删除/恢复评论（切换逻辑删除状态）")
   @PostMapping("comment/deleteOrRestore")
   public ResponseDataStructureEnum deleteOrRestoreComment(@RequestParam String commentId) {
      if (StringUtils.isBlank(commentId)) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      com.zx.cuicuisha.pojo.Comment comment = commentService.deleteOrRestoreComment(commentId);
      return ResponseDataStructureEnum.ok(comment);
   }

   @ApiOperation(value = "编辑评论（管理员专用，可修改评论内容）")
   @PostMapping("comment/update")
   public ResponseDataStructureEnum updateComment(@RequestBody CommentUpdateBO commentUpdateBO) {
      if (commentUpdateBO == null || StringUtils.isBlank(commentUpdateBO.getId())) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      if (StringUtils.isBlank(commentUpdateBO.getContent())) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      Comment updatedComment = commentService.updateCommentForAdmin(commentUpdateBO);
      return ResponseDataStructureEnum.ok(updatedComment);
   }

   /**
    * 管理端：粉丝列表
    * /admin/fans/list
    * @param myId 用户id（可选，如果为空则查询整个fans表，否则查询指定用户的粉丝列表）
    * @param page 页码（可选，默认1）
    * @param pageSize 每页数量（可选，默认10）
    * @return 分页结果
    */
   @ApiOperation(value = "查询粉丝列表（支持查询所有或指定用户的粉丝）")
   @GetMapping("fans/list")
   public ResponseDataStructureEnum fansList(@RequestParam(required = false) String myId,
                                              @RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer pageSize) {
      if (page == null){ page = COMMON_START_PAGE; }
      if (pageSize == null){ pageSize = COMMON_PAGE_SIZE; }
      // 如果myId为空，查询所有粉丝关系；否则查询指定用户的粉丝
      PagedGridResult gridResult = fansService.queryAllFansRelations(myId, page, pageSize);
      return  ResponseDataStructureEnum.ok(gridResult);
   }

   /**
    * 管理端：消息列表
    * /admin/msg/list
    * @param userId 接收消息的用户id（可选，如果为空则查询所有消息，否则查询指定用户的消息）
    * @param page   页码（可选，默认1）
    * @param pageSize 每页数量（可选，默认10）
    * @return 分页结果
    */
   @ApiOperation(value = "查询消息列表（支持查询所有或按用户ID筛选）")
   @GetMapping("msg/list")
   public ResponseDataStructureEnum msgList(@RequestParam(required = false) String userId, @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer pageSize) {
      if (page == null) {page = COMMON_START_PAGE;}
      if (pageSize == null) {pageSize = COMMON_PAGE_SIZE;}
      // 使用管理员专用方法，支持查询所有消息或按用户ID筛选
      PagedGridResult result = msgService.queryAllMessagesForAdmin(userId, page, pageSize);
      return ResponseDataStructureEnum.ok(result);
   }

   @ApiOperation(value = "删除消息")
   @PostMapping("msg/delete")
   public ResponseDataStructureEnum deleteMsg(@RequestParam String msgId) {
      if (StringUtils.isBlank(msgId)) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      msgService.deleteMsgForAdmin(msgId);
      return ResponseDataStructureEnum.ok();
   }

   /**
    * 管理端：编辑消息
    * /admin/msg/update
    * 支持灵活更新消息的各个字段，所有字段都是可选的（除了id）
    * 更新策略：
    * 1. 只更新传入的非空字段
    * 2. msgContent支持两种模式：
    *    - 完全替换（mergeContent=false或未传）：直接替换原有内容
    *    - 合并更新（mergeContent=true）：将新内容合并到原有内容中
    * @param messageUpdateMO 消息更新对象
    * @return 更新后的消息对象
    */
   @ApiOperation(value = "编辑消息（支持灵活更新，所有字段可选）")
   @PostMapping("msg/update")
   public ResponseDataStructureEnum updateMsg(@RequestBody MessageUpdateMO messageUpdateMO) {
      if (messageUpdateMO == null || StringUtils.isBlank(messageUpdateMO.getId())) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      MessageMO updatedMessage = msgService.updateMsgForAdmin(messageUpdateMO);
      return ResponseDataStructureEnum.ok(updatedMessage);
   }

   /**
    * 管理端：举报列表
    * /admin/report/list
    * @param reportType 举报类型（可选，1-视频，2-评论）
    * @param status 处理状态（可选，0-待处理，1-已处理，2-已驳回）
    * @param targetId 被举报对象ID（可选）
    * @param page 页码（可选，默认1）
    * @param pageSize 每页数量（可选，默认10）
    * @return 分页结果
    */
   @ApiOperation(value = "查询举报列表（支持按类型、状态、对象ID筛选）")
   @GetMapping("report/list")
   public ResponseDataStructureEnum reportList(
         @RequestParam(required = false) Integer reportType, @RequestParam(required = false) Integer status, @RequestParam(required = false) String targetId,
         @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer pageSize) {
      if (page == null) { page = COMMON_START_PAGE; }
      if (pageSize == null) { pageSize = COMMON_PAGE_SIZE; }
      PagedGridResult result = reportService.queryReports(reportType, status, targetId, page, pageSize);
      return ResponseDataStructureEnum.ok(result);
   }

   @ApiOperation(value = "处理举报")
   @PostMapping("report/handle")
   public ResponseDataStructureEnum handleReport(
         @RequestParam String reportId, @RequestParam String handlerId, @RequestParam Integer status,
         @RequestParam(required = false) String handleResult, @RequestParam(required = false, defaultValue = "false") Boolean notifyReporter) {
      if (StringUtils.isBlank(reportId) || StringUtils.isBlank(handlerId) || status == null) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      Report report = reportService.handleReport(reportId, handlerId, status, handleResult, notifyReporter);
      return ResponseDataStructureEnum.ok(report);
   }

   @ApiOperation(value = "修改已处理的举报记录（同时更新SQL和MongoDB消息）")
   @PutMapping("report/update")
   public ResponseDataStructureEnum updateReport(@RequestParam String reportId, @RequestParam String handlerId, @RequestParam Integer status, @RequestParam(required = false) String handleResult) {
      Report report = reportService.updateReport(reportId, handlerId, status, handleResult);
      return ResponseDataStructureEnum.ok(report);
   }

   @ApiOperation(value = "重置举报的处理记录（将状态归零，清空处理信息，并删除MongoDB消息）")
   @PutMapping("report/reset")
   public ResponseDataStructureEnum resetReport(@RequestParam String reportId) {
      Report report = reportService.resetReport(reportId);
      return ResponseDataStructureEnum.ok(report);
   }

   @ApiOperation(value = "获取指定对象的举报次数")
   @GetMapping("report/counts")
   public ResponseDataStructureEnum countReports(@RequestParam Integer reportType, @RequestParam String targetId) {
      if (reportType == null || StringUtils.isBlank(targetId)) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      Integer count = reportService.countReportsByTarget(reportType, targetId);
      return ResponseDataStructureEnum.ok(count);
   }

   /**
    * 管理端：数据统计
    * /admin/counts/
    * @return 数据总数量
    */

   @ApiOperation(value = "统计所有视频评论总量")
   @GetMapping("counts/comment")
   public ResponseDataStructureEnum countsCommentNum() {
      Integer totalCount = commentService.getTotalCommentCount();
      return ResponseDataStructureEnum.ok(totalCount);
   }

   @ApiOperation(value = "统计所有视频点赞总量")
   @GetMapping("counts/likes")
   public ResponseDataStructureEnum countsLikeNum() {
      Integer totalCount = vlogService.getTotalLikeCount();
      return ResponseDataStructureEnum.ok(totalCount);
   }

   @ApiOperation(value = "统计所有视频数量")
   @GetMapping("counts/vlogs")
   public ResponseDataStructureEnum countsVlogNum() {
      Integer totalCount = vlogService.getTotalVlogCount();
      return ResponseDataStructureEnum.ok(totalCount);
   }

   @ApiOperation(value = "统计所有用户数量")
   @GetMapping("counts/users")
   public ResponseDataStructureEnum countsUserNum() {
      Integer totalCount = userService.getTotalUserCount();
      return ResponseDataStructureEnum.ok(totalCount);
   }

   @ApiOperation(value = "统计所有举报数量")
   @GetMapping("counts/report")
   public ResponseDataStructureEnum countsReportNum() {
      Integer totalCount = reportService.getTotalReportCount();
      return ResponseDataStructureEnum.ok(totalCount);
   }

   @ApiOperation(value = "统计所有视频举报数量")
   @GetMapping("counts/report/vlog")
   public ResponseDataStructureEnum countsVlogReportNum() {
      Integer totalCount = reportService.getTotalVlogReportCount();
      return ResponseDataStructureEnum.ok(totalCount);
   }

   @ApiOperation(value = "统计所有评论举报数量")
   @GetMapping("counts/report/comment")
   public ResponseDataStructureEnum countsCommentReportNum() {
      Integer totalCount = reportService.getTotalCommentReportCount();
      return ResponseDataStructureEnum.ok(totalCount);
   }

   /**
    * 管理员登录
    * @param adminLoginBO 登录信息（用户名和密码）
    * @return 管理员信息（包含token）
    */
   @ApiOperation(value = "管理员登录")
   @PostMapping("login")
   public ResponseDataStructureEnum login(@Valid @RequestBody AdminLoginBO adminLoginBO) {
      AdminUsers admin = adminService.adminLogin(adminLoginBO);
      // 转换为VO对象
      AdminUsersVO adminUsersVO = new AdminUsersVO();
      adminUsersVO.setId(admin.getId());
      adminUsersVO.setUsername(admin.getUsername());
      adminUsersVO.setEmail(admin.getEmail());
      adminUsersVO.setRealName(admin.getRealName());
      adminUsersVO.setAvatar(admin.getAvatar());
      // 从Redis获取token
      String adminToken = redis.get(REDIS_ADMIN_TOKEN + ":" + admin.getId());
      adminUsersVO.setAdminToken(adminToken);
      return ResponseDataStructureEnum.ok(adminUsersVO);
   }

   /**
    * 管理员登出
    * @param adminId 管理员ID
    * @return 操作结果
    */
   @ApiOperation(value = "管理员登出")
   @PostMapping("logout")
   public ResponseDataStructureEnum logout(@RequestParam Long adminId) {
      if (adminId == null) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      // 清除管理员会话信息
      redis.delete(REDIS_ADMIN_TOKEN + ":" + adminId);
      log.info("管理员登出：adminId={}", adminId);
      return ResponseDataStructureEnum.ok();
   }


}