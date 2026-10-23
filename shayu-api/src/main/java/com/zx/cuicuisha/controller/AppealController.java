package com.zx.cuicuisha.controller;

import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.bo.AppealBO;
import com.zx.cuicuisha.grace.result.ResponseDataStructureEnum;
import com.zx.cuicuisha.pojo.Appeal;
import com.zx.cuicuisha.service.AppealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@Api(tags = "AppealController 申诉模块的接口")
@RequestMapping("appeal")
@RestController
public class AppealController extends BaseInfoProperties {

    @Autowired
    private AppealService appealService;

    @ApiOperation(value = "创建申诉")
    @PostMapping("create")
    public ResponseDataStructureEnum createAppeal(@RequestBody @Valid AppealBO appealBO) {
        Appeal appeal = appealService.createAppeal(appealBO);
        return ResponseDataStructureEnum.ok(appeal);
    }
}

