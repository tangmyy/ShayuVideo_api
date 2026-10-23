package com.zx.cuicuisha.controller;

import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.grace.result.ResponseDataStructureEnum;
import com.zx.cuicuisha.service.UserCFService;
import com.zx.cuicuisha.utils.PagedGridResult;
import com.zx.cuicuisha.vo.UserSimilarityVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 推荐系统Controller
 */
@Slf4j
@Api(tags = "RecommendationController 推荐系统相关接口")
@RequestMapping("recommendation")
@RestController
public class RecommendationController extends BaseInfoProperties {

    @Autowired
    private UserCFService userCFService;

    @ApiOperation(value = "获取推荐视频列表（基于UserCF，返回格式与vlog/indexList一致）")
    @GetMapping("vlogs")
    public ResponseDataStructureEnum recommendVlogs(
            @RequestParam String userId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult result = userCFService.recommendVlogs(userId, page, pageSize);
        return ResponseDataStructureEnum.ok(result);
    }

    @ApiOperation(value = "获取相似用户列表")
    @GetMapping("similarUsers")
    public ResponseDataStructureEnum getSimilarUsers(
            @RequestParam String userId,
            @RequestParam(required = false) Integer k) {
        if (k == null || k <= 0) {
            k = 50;
        }
        List<UserSimilarityVO> similarUsers = userCFService.getSimilarUsers(userId, k);
        return ResponseDataStructureEnum.ok(similarUsers);
    }

    @ApiOperation(value = "计算并缓存用户相似度矩阵")
    @PostMapping("calculateSimilarities")
    public ResponseDataStructureEnum calculateSimilarities() {
        userCFService.calculateAndCacheUserSimilarities();
        return ResponseDataStructureEnum.ok("相似度矩阵计算完成");
    }

    @ApiOperation(value = "清除推荐缓存")
    @DeleteMapping("cache")
    public ResponseDataStructureEnum clearCache(@RequestParam(required = false) String userId) {
        userCFService.clearSimilarityCache(userId);
        return ResponseDataStructureEnum.ok("缓存已清除");
    }
}



