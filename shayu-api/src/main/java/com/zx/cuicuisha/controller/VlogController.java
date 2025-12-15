package com.zx.cuicuisha.controller;

import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.bo.VlogBO;
import com.zx.cuicuisha.enums.YesOrNo;
import com.zx.cuicuisha.grace.result.ResponseDataStructureEnum;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.pojo.Users;
import com.zx.cuicuisha.service.UserService;
import com.zx.cuicuisha.service.VlogService;
import com.zx.cuicuisha.utils.PagedGridResult;
import com.zx.cuicuisha.vo.IndexVlogVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Api(tags = "VlogController 短视频相关业务功能的接口")
@RequestMapping("vlog")
@RestController
@RefreshScope
public class VlogController extends BaseInfoProperties {

   @Autowired
   private UserService userService;
   @Autowired
   private VlogService vlogService;

   @PostMapping("publish")
   public ResponseDataStructureEnum publish(@RequestBody VlogBO vlogBO) {
      vlogService.createVlog(vlogBO);
      return ResponseDataStructureEnum.ok();
   }
   /**
    * 推荐视频流
    * @return
    */
   @GetMapping("indexList")
   public ResponseDataStructureEnum indexList(@RequestParam(defaultValue = "") String userId,
                                              @RequestParam(defaultValue = "") String search,
                                              @RequestParam Integer page,
                                              @RequestParam Integer pageSize){
      if (page == null){ page = COMMON_START_PAGE; }
      if (pageSize == null){ pageSize = COMMON_PAGE_SIZE; }
      PagedGridResult list = vlogService.getIndexVlogList(userId, search, page, pageSize);
      return ResponseDataStructureEnum.ok(list);
   }
   /**
    * 关注视频流
    * @return
    */
   @GetMapping("followList")
   public ResponseDataStructureEnum followList(@RequestParam String myId, @RequestParam Integer page, @RequestParam Integer pageSize){
      if (page == null){ page = COMMON_START_PAGE; }
      if (pageSize == null){ pageSize = COMMON_PAGE_SIZE; }
      PagedGridResult gridResult = vlogService.getMyFollowVlogList(myId, page, pageSize);
      return  ResponseDataStructureEnum.ok(gridResult);
   }
   /**
    * 朋友视频流
    * @return
    */
   @GetMapping("friendList")
   public ResponseDataStructureEnum friendList(@RequestParam String myId, @RequestParam Integer page, @RequestParam Integer pageSize){
      if (page == null){ page = COMMON_START_PAGE; }
      if (pageSize == null){ pageSize = COMMON_PAGE_SIZE; }
      PagedGridResult gridResult = vlogService.getMyFriendVlogList(myId, page, pageSize);
      return  ResponseDataStructureEnum.ok(gridResult);
   }

   @GetMapping("detail")
   public ResponseDataStructureEnum detail(@RequestParam(defaultValue = "") String userId, @RequestParam String vlogId){
      return ResponseDataStructureEnum.ok(vlogService.getVlogDetailById(userId, vlogId));
   }

   @PostMapping("changeToPrivate")
   public ResponseDataStructureEnum changeToPrivate(@RequestParam String userId, @RequestParam String vlogId){
      vlogService.changeToPrivateOrPublic(userId, vlogId, YesOrNo.YES.type);
      return  ResponseDataStructureEnum.ok();
   }

   @PostMapping("changeToPublic")
   public ResponseDataStructureEnum changeToPublic(@RequestParam String userId, @RequestParam String vlogId){
      vlogService.changeToPrivateOrPublic(userId, vlogId, YesOrNo.NO.type);
      return  ResponseDataStructureEnum.ok();
   }

   /**
    * 我的公开视频
    * @return
    */
   @GetMapping("myPublicList")
   public ResponseDataStructureEnum myPublicList(@RequestParam String userId, @RequestParam Integer page, @RequestParam Integer pageSize){
      if (page == null){ page = COMMON_START_PAGE; }
      if (pageSize == null){ pageSize = COMMON_PAGE_SIZE; }
      PagedGridResult gridResult = vlogService.queryMyVlogList(userId, page, pageSize, YesOrNo.NO.type);
      return  ResponseDataStructureEnum.ok(gridResult);
   }
   /**
    * 我的私密视频
    * @return
    */
   @GetMapping("myPrivateList")
   public ResponseDataStructureEnum myPrivateList(@RequestParam String userId, @RequestParam Integer page, @RequestParam Integer pageSize){
      if (page == null){ page = COMMON_START_PAGE; }
      if (pageSize == null){ pageSize = COMMON_PAGE_SIZE; }
      PagedGridResult gridResult = vlogService.queryMyVlogList(userId, page, pageSize, YesOrNo.YES.type);
      return  ResponseDataStructureEnum.ok(gridResult);
   }
   /**
    * 我的喜欢视频
    * @return
    */
   @GetMapping("myLikedList")
   public ResponseDataStructureEnum myLikedList(@RequestParam String userId, @RequestParam Integer page, @RequestParam Integer pageSize){
      if (page == null){ page = COMMON_START_PAGE; }
      if (pageSize == null){ pageSize = COMMON_PAGE_SIZE; }
      PagedGridResult gridResult = vlogService.getMyLikedVlogList(userId, page, pageSize);
      return  ResponseDataStructureEnum.ok(gridResult);
   }


   @Value("${nacos.counts}")
   private Integer nacosCounts;

   @PostMapping("like")
   public ResponseDataStructureEnum like(@RequestParam String userId, @RequestParam String vlogerId, @RequestParam String vlogId){
      // 判断三个id不能为空
      if (StringUtils.isBlank(userId) || StringUtils.isBlank(vlogerId) || StringUtils.isBlank(vlogId)) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      // 判断用户id对应的用户是否存在
      Users user = userService.getUser(userId);
      if (user == null) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
      }
      // 判断博主id对应的用户是否存在
      Users vloger = userService.getUser(vlogerId);
      if (vloger == null) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.VLOGER_NOT_EXIST_ERROR);
      }
      // 判断视频id对应的视频是否存在
      IndexVlogVO vlogVO = vlogService.getVlogDetailById(userId, vlogId);
      if (vlogVO == null) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.VLOG_NOT_EXIST_ERROR);
      }
      // 我点赞的视频，关联关系保存到数据库
      vlogService.userLikeVlog(userId, vlogId);
      vlogService.updateLikeVlog(userId, vlogerId, vlogId);

      // 点赞完毕，获得当前在redis中的总数
      // 比如获得总计数为 1k/1w/10w, 假定阈值（配置）为2000, 此时1k满足2000, 则触发入库
      String countsStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);
      log.info("======" + REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId + "======");
      Integer counts = 0;
      if (StringUtils.isNotBlank(countsStr)) {
         counts = Integer.valueOf(countsStr);
         if (counts >= nacosCounts) {
            vlogService.flushCounts(vlogId, counts);
         }
      }
      return  ResponseDataStructureEnum.ok();
   }

   @PostMapping("unlike")
   public ResponseDataStructureEnum unlike(@RequestParam String userId, @RequestParam String vlogerId, @RequestParam String vlogId){
      // 判断三个id不能为空
      if (StringUtils.isBlank(userId) || StringUtils.isBlank(vlogerId) || StringUtils.isBlank(vlogId)) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      // 判断用户id对应的用户是否存在
      Users user = userService.getUser(userId);
      if (user == null) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
      }
      // 判断博主id对应的用户是否存在
      Users vloger = userService.getUser(vlogerId);
      if (vloger == null) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.VLOGER_NOT_EXIST_ERROR);
      }
      // 判断视频id对应的视频是否存在
      IndexVlogVO vlogVO = vlogService.getVlogDetailById(userId, vlogId);
      if (vlogVO == null) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.VLOG_NOT_EXIST_ERROR);
      }
      // 我点赞的视频，关联关系从数据库删除
      vlogService.userUnLikeVlog(userId, vlogId);
      vlogService.updateUnLikeVlog(userId, vlogerId, vlogId);
      return  ResponseDataStructureEnum.ok();
   }

   @PostMapping("totalLikedCounts")
   public ResponseDataStructureEnum totalLikedCounts(@RequestParam String vlogId) {
      return ResponseDataStructureEnum.ok(vlogService.getVlogBeLikedCounts(vlogId));
   }

   /**
    * 管理端：视频列表
    * /vlog/admin/list
    *
    * @param search 搜索关键字（可选）
    * @param page   页码（可选，默认1）
    * @param pageSize 每页数量（可选，默认10）
    * @return 分页结果
    */
   @GetMapping("admin/list")
   public ResponseDataStructureEnum adminList(@RequestParam(required = false) String search,
                                              @RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer pageSize) {
      if (page == null) {
         page = COMMON_START_PAGE;
      }
      if (pageSize == null) {
         pageSize = COMMON_PAGE_SIZE;
      }
      // 管理端不需要指定 userId，这里传空字符串
      PagedGridResult list = vlogService.getIndexVlogList("", search == null ? "" : search, page, pageSize);
      return ResponseDataStructureEnum.ok(list);
   }
}
