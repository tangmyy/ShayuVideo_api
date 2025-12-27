package com.zx.cuicuisha.controller;

import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.grace.result.ResponseDataStructureEnum;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.mo.MessageMO;
import com.zx.cuicuisha.service.MsgService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Api(tags = "MsgController 消息功能模块的接口")
@RequestMapping("msg")
@RestController
public class MsgController extends BaseInfoProperties {

   @Autowired
   private MsgService msgService;

   @ApiOperation(value = "查询所有消息")
   @GetMapping("list")
   public ResponseDataStructureEnum list(@RequestParam String userId, @RequestParam Integer page, @RequestParam Integer pageSize) {
      // mongodb 从0分页，区别于数据库
      if (page == null) {page = COMMON_START_PAGE_ZERO;}
      if (pageSize == null) {pageSize = COMMON_PAGE_SIZE;}
      List<MessageMO> list = msgService.queryList(userId, page, pageSize);
      return ResponseDataStructureEnum.ok(list);
   }

   @ApiOperation(value = "删除消息")
   @DeleteMapping("delete")
   public ResponseDataStructureEnum delete(@RequestParam String msgId, @RequestParam String userId) {
      if (StringUtils.isBlank(msgId) || StringUtils.isBlank(userId)) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      msgService.deleteMsg(msgId, userId);
      return ResponseDataStructureEnum.ok();
   }

}

