package com.zx.cuicuisha.controller;

import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.enums.YesOrNo;
import com.zx.cuicuisha.grace.result.ResponseDataStructureEnum;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.pojo.Users;
import com.zx.cuicuisha.service.FansService;
import com.zx.cuicuisha.service.UserService;
import com.zx.cuicuisha.utils.PagedGridResult;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "FansController 粉丝相关业务功能的接口")
@RequestMapping("fans")
@RestController
public class FansController extends BaseInfoProperties {

   @Autowired
   private UserService userService;
   @Autowired
   private FansService fansService;

   @PostMapping("follow")
   public ResponseDataStructureEnum follow(@RequestParam String myId, @RequestParam String vlogerId) {
      // 判断两个id不能为空
      if (StringUtils.isBlank(myId) || StringUtils.isBlank(vlogerId)) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
      }
      // 判断当前用户，自己不能关注自己
      if (myId.equalsIgnoreCase(vlogerId)) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.FANS_FOLLOW_YOURSELF_ERROR);
      }
      // 判断两个id对应的用户是否存在
      Users vloger = userService.getUser(vlogerId);
      Users myInfo = userService.getUser(myId);
      // fixme: 两个用户id的数据库查询后的判断，是分开好？还是合并判断好？
      if (myInfo == null) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
      }
      if (vloger == null) {
         return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.VLOGER_NOT_EXIST_ERROR);
      }
      // 保存粉丝关系到数据库
      fansService.doFollow(myId, vlogerId);
      fansService.updateFollowCache(myId, vlogerId);
      return ResponseDataStructureEnum.ok();
   }

   @PostMapping("cancel")
   public ResponseDataStructureEnum cancel(@RequestParam String myId, @RequestParam String vlogerId) {
      // 保存粉丝关系到数据库
      fansService.doCancel(myId, vlogerId);
      fansService.updateCancelCache(myId, vlogerId);
      return ResponseDataStructureEnum.ok();
   }

   @GetMapping("queryDoIFollowVloger")
   public ResponseDataStructureEnum queryDoIFollowVloger(@RequestParam String myId, @RequestParam String vlogerId) {
      boolean followYN = fansService.queryDoIFollowVloger(myId, vlogerId);
      return ResponseDataStructureEnum.ok(followYN);
   }

   @GetMapping("queryMyFollows")
   public ResponseDataStructureEnum queryMyFollows(@RequestParam String myId, @RequestParam Integer page, @RequestParam Integer pageSize) {
      if (page == null){ page = COMMON_START_PAGE; }
      if (pageSize == null){ pageSize = COMMON_PAGE_SIZE; }
      PagedGridResult gridResult = fansService.queryMyFollows(myId, page, pageSize);
      return  ResponseDataStructureEnum.ok(gridResult);
   }

   @GetMapping("queryMyFans")
   public ResponseDataStructureEnum queryMyFans(@RequestParam String myId, @RequestParam Integer page, @RequestParam Integer pageSize) {
      if (page == null){ page = COMMON_START_PAGE; }
      if (pageSize == null){ pageSize = COMMON_PAGE_SIZE; }
      PagedGridResult gridResult = fansService.queryMyFans(myId, page, pageSize);
      return  ResponseDataStructureEnum.ok(gridResult);
   }

}
/**
 * fixme: 答: 分开好
 * 更明确的错误信息：可以区分哪个用户不存在
 * 更好的性能：如果第一个用户不存在，可以避免第二个不必要的数据库查询
 * 便于调试：容易定位具体是哪个用户ID有问题
 *
 * 合并判断：
 * if (myInfo == null || vloger == null) {
 *    return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
 * }
 */
