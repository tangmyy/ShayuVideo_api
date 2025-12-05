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


}
