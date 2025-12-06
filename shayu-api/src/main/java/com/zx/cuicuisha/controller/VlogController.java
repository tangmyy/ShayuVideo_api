package com.zx.cuicuisha.controller;

import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.bo.VlogBO;
import com.zx.cuicuisha.enums.YesOrNo;
import com.zx.cuicuisha.grace.result.ResponseDataStructureEnum;
import com.zx.cuicuisha.service.VlogService;
import com.zx.cuicuisha.utils.PagedGridResult;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Api(tags = "VlogController 短视频相关业务功能的接口")
@RequestMapping("vlog")
@RestController
public class VlogController extends BaseInfoProperties {

   @Autowired
   private VlogService vlogService;

   @PostMapping("publish")
   public ResponseDataStructureEnum publish(@RequestBody VlogBO vlogBO) {
      vlogService.createVlog(vlogBO);
      return ResponseDataStructureEnum.ok();
   }

   @GetMapping("indexList")
   public ResponseDataStructureEnum indexList(@RequestParam(defaultValue = "") String search, @RequestParam Integer page, @RequestParam Integer pageSize){
      if (page == null){ page = COMMON_START_PAGE; }
      if (pageSize == null){ pageSize = COMMON_PAGE_SIZE; }
      PagedGridResult list = vlogService.getIndexVlogList(search, page, pageSize);
      return ResponseDataStructureEnum.ok(list);
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

   @GetMapping("myPublicList")
   public ResponseDataStructureEnum myPublicList(@RequestParam String userId, @RequestParam Integer page, @RequestParam Integer pageSize){
      if (page == null){ page = COMMON_START_PAGE; }
      if (pageSize == null){ pageSize = COMMON_PAGE_SIZE; }
      PagedGridResult gridResult = vlogService.queryMyVlogList(userId, page, pageSize, YesOrNo.NO.type);
      return  ResponseDataStructureEnum.ok(gridResult);
   }

   @GetMapping("myPrivateList")
   public ResponseDataStructureEnum myPrivateList(@RequestParam String userId, @RequestParam Integer page, @RequestParam Integer pageSize){
      if (page == null){ page = COMMON_START_PAGE; }
      if (pageSize == null){ pageSize = COMMON_PAGE_SIZE; }
      PagedGridResult gridResult = vlogService.queryMyVlogList(userId, page, pageSize, YesOrNo.YES.type);
      return  ResponseDataStructureEnum.ok(gridResult);
   }

   @PostMapping("like")
   public ResponseDataStructureEnum like(@RequestParam String userId, @RequestParam String vlogerId, @RequestParam String vlogId){
      // 我点赞的视频，关联关系保存到数据库
      vlogService.userLikeVlog(userId, vlogId);
      // 点赞以后，视频点赞数/发布者获赞数 +1
      redis.increment(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId, 1);
      redis.increment(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId, 1);
      //  我点赞的视频，关联关系保存到redis
      redis.set(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId, "1");
      return  ResponseDataStructureEnum.ok();
   }
}
