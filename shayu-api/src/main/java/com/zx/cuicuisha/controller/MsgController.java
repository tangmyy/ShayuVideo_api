package com.zx.cuicuisha.controller;

import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.grace.result.ResponseDataStructureEnum;
import com.zx.cuicuisha.mo.MessageMO;
import com.zx.cuicuisha.service.MsgService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

   @GetMapping("list")
   public ResponseDataStructureEnum list(@RequestParam String userId, @RequestParam Integer page, @RequestParam Integer pageSize) {
      // mongodb 从0分页，区别于数据库
      if (page == null) {
         page = COMMON_START_PAGE_ZERO;
      }
      if (pageSize == null) {
         pageSize = COMMON_PAGE_SIZE;
      }
      List<MessageMO> list = msgService.queryList(userId, page, pageSize);
      return ResponseDataStructureEnum.ok(list);
   }
   /**
    * 管理端：消息列表
    * /msg/admin/list
    *
    * @param userId 接收消息的用户id
    * @param page   页码（从0开始，默认0）
    * @param pageSize 每页数量（默认10）
    * @return 当前用户的消息列表
    */
   @GetMapping("admin/list")
   public ResponseDataStructureEnum adminList(@RequestParam String userId,
                                              @RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer pageSize) {
      // mongodb 从0分页，区别于数据库
      if (page == null) {
         page = COMMON_START_PAGE_ZERO;
      }
      if (pageSize == null) {
         pageSize = COMMON_PAGE_SIZE;
      }
      List<MessageMO> list = msgService.queryList(userId, page, pageSize);
      return ResponseDataStructureEnum.ok(list);
   }
}

