package com.zx.cuicuisha.impl;

import com.github.pagehelper.PageHelper;
import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.base.RabbitMQConfig;
import com.zx.cuicuisha.bo.ReportBO;
import com.zx.cuicuisha.enums.MessageEnum;
import com.zx.cuicuisha.exceptions.GraceException;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.mapper.CommentMapper;
import com.zx.cuicuisha.mapper.ReportMapper;
import com.zx.cuicuisha.mapper.ReportMapperCustom;
import com.zx.cuicuisha.mapper.UsersMapper;
import com.zx.cuicuisha.mapper.VlogMapper;
import com.zx.cuicuisha.mo.MessageMO;
import com.zx.cuicuisha.pojo.Comment;
import com.zx.cuicuisha.pojo.Report;
import com.zx.cuicuisha.pojo.Users;
import com.zx.cuicuisha.pojo.Vlog;
import com.zx.cuicuisha.service.CommentService;
import com.zx.cuicuisha.service.MsgService;
import com.zx.cuicuisha.service.ReportService;
import com.zx.cuicuisha.service.VlogService;
import com.zx.cuicuisha.utils.JsonUtils;
import com.zx.cuicuisha.utils.PagedGridResult;
import com.zx.cuicuisha.vo.ReportAdminVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReportServiceImpl extends BaseInfoProperties implements ReportService {

    private static final Integer REPORT_TYPE_VLOG = 1;  // 视频
    private static final Integer REPORT_TYPE_COMMENT = 2;  // 评论
    private static final Integer REPORT_STATUS_PENDING = 0;  // 待处理
    private static final Integer REPORT_STATUS_HANDLED = 1;  // 已处理
    private static final Integer REPORT_STATUS_REJECTED = 2;  // 已驳回
    private static final Integer AUTO_DELETE_THRESHOLD = 3;  // 自动下架阈值

    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private ReportMapperCustom reportMapperCustom;
    @Autowired
    private Sid sid;
    @Autowired
    private VlogMapper vlogMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private VlogService vlogService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private MsgService msgService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 举报视频
    @Override
    @Transactional
    public Report reportVlog(ReportBO reportBO) {
        // 验证视频是否存在
        Vlog vlog = vlogService.getVlog(reportBO.getTargetId());
        if (vlog == null) {
            GraceException.display(ResponseStatusEnum.VLOG_NOT_EXIST_ERROR);
        }
        // 检查视频是否已被删除
        if (vlog.getIsDeleted() != null && vlog.getIsDeleted() == 1) {
            GraceException.display(ResponseStatusEnum.VLOG_DELETED_ERROR);
        }
        // 检查是否举报自己的内容
        if (vlog.getVlogerId() != null && vlog.getVlogerId().equals(reportBO.getReporterId())) {
            GraceException.display(ResponseStatusEnum.REPORT_SELF_CONTENT_ERROR);
        }
        // 检查是否已举报过
        String existingReportId = reportMapperCustom.checkReportExists(
                reportBO.getReporterId(), REPORT_TYPE_VLOG, reportBO.getTargetId());
        if (StringUtils.isNotBlank(existingReportId)) {
            GraceException.display(ResponseStatusEnum.REPORT_ALREADY_EXISTS_ERROR);
        }
        // 创建举报记录
        String reportId = sid.nextShort();
        Report report = new Report();
        report.setId(reportId);
        report.setReporterId(reportBO.getReporterId());
        report.setReportType(REPORT_TYPE_VLOG);
        report.setTargetId(reportBO.getTargetId());
        report.setReason(reportBO.getReason());
        report.setDescription(reportBO.getDescription());
        report.setStatus(REPORT_STATUS_PENDING);
        report.setIsDeleted(0);
        Date now = new Date();
        report.setCreateTime(now);
        report.setUpdatedTime(now);

        reportMapper.insert(report);
        // 检查是否达到自动下架阈值（3次）
        // Integer reportCount = reportMapperCustom.countReportsByTarget(REPORT_TYPE_VLOG, reportBO.getTargetId());
        // if (reportCount >= AUTO_DELETE_THRESHOLD) {
        //     log.info("视频 {} 被举报次数达到阈值 {}，自动下架", reportBO.getTargetId(), AUTO_DELETE_THRESHOLD);
        //     // 自动下架视频（逻辑删除）
        //     Vlog updateVlog = new Vlog();
        //     updateVlog.setId(reportBO.getTargetId());
        //     updateVlog.setIsDeleted(1);
        //     vlogMapper.updateByPrimaryKeySelective(updateVlog);
        // }
        return report;
    }

    // 举报评论
    @Override
    @Transactional
    public Report reportComment(ReportBO reportBO) {
        // 验证评论是否存在
        Comment comment = commentService.getComment(reportBO.getTargetId());
        if (comment == null) {
            GraceException.display(ResponseStatusEnum.COMMENT_NOT_EXIST_ERROR);
        }
        // 检查评论是否已被删除
        if (comment.getIsDeleted() != null && comment.getIsDeleted() == 1) {
            GraceException.display(ResponseStatusEnum.COMMENT_DELETED_ERROR);
        }
        // 检查是否举报自己的内容
        if (comment.getCommentUserId() != null && comment.getCommentUserId().equals(reportBO.getReporterId())) {
            GraceException.display(ResponseStatusEnum.REPORT_SELF_CONTENT_ERROR);
        }
        // 检查是否已举报过
        String existingReportId = reportMapperCustom.checkReportExists(
                reportBO.getReporterId(), REPORT_TYPE_COMMENT, reportBO.getTargetId());
        if (StringUtils.isNotBlank(existingReportId)) {
            GraceException.display(ResponseStatusEnum.REPORT_ALREADY_EXISTS_ERROR);
        }
        // 创建举报记录
        String reportId = sid.nextShort();
        Report report = new Report();
        report.setId(reportId);
        report.setReporterId(reportBO.getReporterId());
        report.setReportType(REPORT_TYPE_COMMENT);
        report.setTargetId(reportBO.getTargetId());
        report.setReason(reportBO.getReason());
        report.setDescription(reportBO.getDescription());
        report.setStatus(REPORT_STATUS_PENDING);
        report.setIsDeleted(0);
        Date now = new Date();
        report.setCreateTime(now);
        report.setUpdatedTime(now);

        reportMapper.insert(report);
        // 检查是否达到自动下架阈值（3次）
        // Integer reportCount = reportMapperCustom.countReportsByTarget(REPORT_TYPE_COMMENT, reportBO.getTargetId());
        // if (reportCount >= AUTO_DELETE_THRESHOLD) {
        //     log.info("评论 {} 被举报次数达到阈值 {}，自动删除", reportBO.getTargetId(), AUTO_DELETE_THRESHOLD);
        //     // 自动删除评论（逻辑删除）
        //     Comment updateComment = new Comment();
        //     updateComment.setId(reportBO.getTargetId());
        //     updateComment.setIsDeleted(1);
        //     commentMapper.updateByPrimaryKeySelective(updateComment);
        //     // 更新视频评论数
        //     String vlogId = comment.getVlogId();
        //     if (StringUtils.isNotBlank(vlogId)) {
        //         redis.decrement(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId, 1);
        //     }
        // }
        return report;
    }

    // 查询举报列表
    @Override
    public PagedGridResult queryReports(Integer reportType, Integer status, String targetId, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        if (reportType != null) {
            map.put("reportType", reportType);
        }
        if (status != null) {
            map.put("status", status);
        }
        if (StringUtils.isNotBlank(targetId)) {
            map.put("targetId", targetId);
        }
        PageHelper.startPage(page, pageSize);
        List<ReportAdminVO> list = reportMapperCustom.getReportList(map);
        return setterPagedGrid(list, page);
    }

    // 处理举报
    @Override
    @Transactional
    public Report handleReport(String reportId, String handlerId, Integer status, String handleResult, Boolean notifyReporter) {
        // 验证举报是否存在
        Report report = reportMapper.selectByPrimaryKey(reportId);
        if (report == null) {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
        if (report.getIsDeleted() != null && report.getIsDeleted() == 1) {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
        // 验证状态值
        if (status == null || (status != REPORT_STATUS_HANDLED && status != REPORT_STATUS_REJECTED)) {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
        // 更新举报记录
        Report updateReport = new Report();
        updateReport.setId(reportId);
        updateReport.setStatus(status);
        updateReport.setHandlerId(handlerId);
        updateReport.setHandleTime(new Date());
        updateReport.setHandleResult(handleResult);
        updateReport.setUpdatedTime(new Date());
        reportMapper.updateByPrimaryKeySelective(updateReport);
        // 如果选择通知举报人，发送消息
        if (notifyReporter != null && notifyReporter) {
            try {
                Map<String, Object> msgContent = new HashMap<>();
                // 从已查询的 report 对象获取举报类型
                Integer reportType = report.getReportType();  // 1-视频，2-评论
                // 使用方法参数的 status（新的处理状态），而不是 report.getStatus()（旧状态）
                Integer reportStatus = status;  // 1-已处理，2-已驳回
                // 转换为可读的字符串
                String reportTypeStr = reportType == REPORT_TYPE_VLOG ? "视频" : "评论";
                String reportStatusStr = reportStatus == REPORT_STATUS_HANDLED ? "已处理" : "已驳回";
                // 如果handleResult为空，设置默认值
                String finalHandleResult = StringUtils.isBlank(handleResult) ? "已收到反馈！" : handleResult;
                String msg = reportTypeStr + "举报" + reportStatusStr + "\n处理结果：" + finalHandleResult;

                msgContent.put("reportId", reportId);
                msgContent.put("handleResult", msg);
                // 如果是举报视频
                if (report.getReportType() == REPORT_TYPE_VLOG) {
                    Vlog vlog = vlogService.getVlog(report.getTargetId());
                    if (vlog != null) {
                        msgContent.put("vlogId", vlog.getId());
                        msgContent.put("vlogCover", vlog.getCover());
                        msgContent.put("vlogTitle", vlog.getTitle());
                    }
                // 如果是举报评论
                } else if (report.getReportType() == REPORT_TYPE_COMMENT) {
                    Comment comment = commentService.getComment(report.getTargetId());
                    if (comment != null) {
                        msgContent.put("commentId", comment.getId());
                        msgContent.put("commentContent", comment.getContent());
                        Vlog vlog = vlogService.getVlog(comment.getVlogId());
                        if (vlog != null) {
                            msgContent.put("vlogId", vlog.getId());
                            msgContent.put("vlogCover", vlog.getCover());
                            msgContent.put("vlogTitle", vlog.getTitle());
                        }
                    }
                }
                // 发送消息到MQ（异步）
                MessageMO messageMO = new MessageMO();
                messageMO.setFromUserId(handlerId);
                messageMO.setToUserId(report.getReporterId());
                messageMO.setMsgContent(msgContent);
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG, "sys.msg." + MessageEnum.REPORT_HANDLED.enValue, JsonUtils.objectToJson(messageMO)
                );
                log.info("举报处理通知已发送到MQ：reportId={}, reporterId={}", reportId, report.getReporterId());
            } catch (Exception e) {
                log.error("发送举报处理通知失败：reportId={}", reportId, e);
                // 通知失败不影响主流程，只记录日志
            }
        }
        return reportMapper.selectByPrimaryKey(reportId);
    }

    // 统计指定对象的举报次数
    @Override
    public Integer countReportsByTarget(Integer reportType, String targetId) {
        return reportMapperCustom.countReportsByTarget(reportType, targetId);
    }
}

