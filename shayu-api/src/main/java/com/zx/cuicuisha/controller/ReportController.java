package com.zx.cuicuisha.controller;

import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.bo.ReportBO;
import com.zx.cuicuisha.grace.result.ResponseDataStructureEnum;
import com.zx.cuicuisha.pojo.Report;
import com.zx.cuicuisha.service.ReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@Api(tags = "ReportController 举报模块的接口")
@RequestMapping("report")
@RestController
public class ReportController extends BaseInfoProperties {

    @Autowired
    private ReportService reportService;

    @ApiOperation(value = "举报视频")
    @PostMapping("vlog")
    public ResponseDataStructureEnum reportVlog(@RequestBody @Valid ReportBO reportBO) {
        Report report = reportService.reportVlog(reportBO);
        return ResponseDataStructureEnum.ok(report);
    }

    @ApiOperation(value = "举报评论")
    @PostMapping("comment")
    public ResponseDataStructureEnum reportComment(@RequestBody @Valid ReportBO reportBO) {
        Report report = reportService.reportComment(reportBO);
        return ResponseDataStructureEnum.ok(report);
    }

    @ApiOperation(value = "查询指定对象的举报次数")
    @GetMapping("count")
    public ResponseDataStructureEnum countReports(@RequestParam Integer reportType, @RequestParam String targetId) {
        Integer count = reportService.countReportsByTarget(reportType, targetId);
        return ResponseDataStructureEnum.ok(count);
    }
}
