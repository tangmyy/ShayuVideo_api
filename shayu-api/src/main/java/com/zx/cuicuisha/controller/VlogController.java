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
import org.springframework.web.bind.annotation.*;


@Slf4j
@Api(tags = "VlogController 短视频相关业务功能的接口")
@RequestMapping("vlog")
@RestController
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
      return ResponseDataStructureEnum.ok(vlogService.getVlogDetailById(vlogId));
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
      IndexVlogVO vlogVO = vlogService.getVlogDetailById(vlogId);
      if (vlogVO == null) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.VLOG_NOT_EXIST_ERROR);
      }
      // 我点赞的视频，关联关系保存到数据库
      vlogService.userLikeVlog(userId, vlogId);
      vlogService.updateLikeVlog(userId, vlogerId, vlogId);
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
      IndexVlogVO vlogVO = vlogService.getVlogDetailById(vlogId);
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
}
