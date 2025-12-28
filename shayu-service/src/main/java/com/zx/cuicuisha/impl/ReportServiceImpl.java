package com.zx.cuicuisha.impl;

import com.github.pagehelper.PageHelper;
import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.base.RabbitMQConfig;
import com.zx.cuicuisha.bo.ReportBO;
import com.zx.cuicuisha.enums.MessageTypeEnum;
import com.zx.cuicuisha.exceptions.GraceException;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.mapper.CommentMapper;
import com.zx.cuicuisha.mapper.ReportMapper;
import com.zx.cuicuisha.mapper.ReportMapperCustom;
import com.zx.cuicuisha.mapper.UsersMapper;
import com.zx.cuicuisha.mapper.VlogMapper;
import com.zx.cuicuisha.mo.MessageMO;
import com.zx.cuicuisha.repository.MessageRepository;
import com.zx.cuicuisha.pojo.Comment;
import com.zx.cuicuisha.pojo.Report;
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
import tk.mybatis.mapper.entity.Example;

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
    @Autowired
    private MessageRepository messageRepository;

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
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG, "sys.msg." + MessageTypeEnum.REPORT_HANDLED.enValue, JsonUtils.objectToJson(messageMO)
                );
                log.info("举报处理通知已发送到MQ：reportId={}, reporterId={}", reportId, report.getReporterId());
            } catch (Exception e) {
                log.error("发送举报处理通知失败：reportId={}", reportId, e);
                // 通知失败不影响主流程，只记录日志
            }
        }
        return reportMapper.selectByPrimaryKey(reportId);
    }

    // 修改已处理的举报记录（同时更新SQL和MongoDB消息）
    @Override
    @Transactional
    public Report updateReport(String reportId, String handlerId, Integer status, String handleResult) {
        // 1. 验证举报是否存在
        Report report = reportMapper.selectByPrimaryKey(reportId);
        if (report == null) {
            GraceException.display(ResponseStatusEnum.REPORT_NOT_EXIST_ERROR);
        }
        if (report.getIsDeleted() != null && report.getIsDeleted() == 1) {
            GraceException.display(ResponseStatusEnum.REPORT_DELETED_ERROR);
        }
        // 2. 验证状态值（只能修改已处理的记录，状态必须是1或2）
        if (status == null || (status != REPORT_STATUS_HANDLED && status != REPORT_STATUS_REJECTED)) {
            GraceException.display(ResponseStatusEnum.REPORT_STATUS_INVALID_ERROR);
        }
        // 3. 验证记录是否已被处理过（不能修改待处理的记录）
        if (report.getStatus() == null || report.getStatus() == REPORT_STATUS_PENDING) {
            GraceException.display(ResponseStatusEnum.REPORT_NOT_HANDLED_ERROR);
        }
        // 4. 更新SQL中的举报记录
        Report updateReport = new Report();
        updateReport.setId(reportId);
        updateReport.setStatus(status);
        updateReport.setHandlerId(handlerId);
        updateReport.setHandleTime(new Date());
        updateReport.setHandleResult(handleResult);
        updateReport.setUpdatedTime(new Date());
        reportMapper.updateByPrimaryKeySelective(updateReport);
        // 5. 更新MongoDB中的消息记录
        try {
            // 查询所有消息，查找对应的举报处理消息
            // 注意：如果消息量大，可以考虑使用MongoTemplate进行更高效的查询
            List<MessageMO> allMessages = messageRepository.findAll();
            boolean found = false;
            for (MessageMO message : allMessages) {
                // 检查消息类型是否为举报处理，且msgContent中包含对应的reportId
                if (message.getMsgType() != null && message.getMsgType() == MessageTypeEnum.REPORT_HANDLED.type) {
                    Map<String, Object> msgContent = message.getMsgContent();
                    if (msgContent != null && reportId.equals(msgContent.get("reportId"))) {
                        // 找到对应的消息，更新处理结果
                        // 重新构建消息内容
                        Integer reportType = report.getReportType();
                        String reportTypeStr = reportType == REPORT_TYPE_VLOG ? "视频" : "评论";
                        String reportStatusStr = status == REPORT_STATUS_HANDLED ? "已处理" : "已驳回";
                        String finalHandleResult = StringUtils.isBlank(handleResult) ? "已收到反馈！" : handleResult;
                        String msg = reportTypeStr + "举报" + reportStatusStr + "\n处理结果：" + finalHandleResult;
                        // 更新msgContent中的handleResult
                        msgContent.put("handleResult", msg);
                        message.setMsgContent(msgContent);
                        messageRepository.save(message);
                        log.info("更新MongoDB消息成功：msgId={}, reportId={}", message.getId(), reportId);
                        found = true;
                        break; // 找到后退出循环
                    }
                }
            }
            if (!found) {
                log.warn("未找到对应的MongoDB消息记录：reportId={}", reportId);
            }
        } catch (Exception e) {
            log.error("更新MongoDB消息失败：reportId={}", reportId, e);
            // MongoDB更新失败不影响SQL更新，只记录日志
        }
        return reportMapper.selectByPrimaryKey(reportId);
    }

    // 重置举报的处理记录（将状态归零，清空处理信息，并删除MongoDB消息）
    @Override
    @Transactional
    public Report resetReport(String reportId) {
        // 1. 验证举报是否存在
        Report report = reportMapper.selectByPrimaryKey(reportId);
        if (report == null) {
            GraceException.display(ResponseStatusEnum.REPORT_NOT_EXIST_ERROR);
        }
        if (report.getIsDeleted() != null && report.getIsDeleted() == 1) {
            GraceException.display(ResponseStatusEnum.REPORT_DELETED_ERROR);
        }
        // 2. 更新SQL中的举报记录：状态归零，清空处理信息
        // 使用自定义方法显式设置字段为NULL（updateByPrimaryKeySelective会忽略null值）
        int updateCount = reportMapperCustom.resetReportStatus(reportId);
        if (updateCount != 1) {
            log.error("重置举报记录失败：reportId={}, updateCount={}", reportId, updateCount);
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
        // 3. 删除MongoDB中对应的消息文档
        try {
            List<MessageMO> allMessages = messageRepository.findAll();
            boolean found = false;
            for (MessageMO message : allMessages) {
                // 检查消息类型是否为举报处理，且msgContent中包含对应的reportId
                if (message.getMsgType() != null && message.getMsgType() == MessageTypeEnum.REPORT_HANDLED.type) {
                    Map<String, Object> msgContent = message.getMsgContent();
                    if (msgContent != null && reportId.equals(msgContent.get("reportId"))) {
                        // 找到对应的消息，删除
                        messageRepository.deleteById(message.getId());
                        log.info("删除MongoDB消息成功：msgId={}, reportId={}", message.getId(), reportId);
                        found = true;
                        break; // 找到后退出循环
                    }
                }
            }
            if (!found) {
                log.warn("未找到对应的MongoDB消息记录：reportId={}", reportId);
            }
        } catch (Exception e) {
            log.error("删除MongoDB消息失败：reportId={}", reportId, e);
            // MongoDB删除失败不影响SQL更新，只记录日志
        }
        
        return reportMapper.selectByPrimaryKey(reportId);
    }

    // 统计指定对象的举报次数
    @Override
    public Integer countReportsByTarget(Integer reportType, String targetId) {
        return reportMapperCustom.countReportsByTarget(reportType, targetId);
    }

    // 统计所有举报的数量
    @Override
    public Integer getTotalReportCount() {
        try {
            // 使用Example查询，过滤掉已逻辑删除的举报
            Example example = new Example(Report.class);
            Example.Criteria criteria = example.createCriteria();
            // 只统计未逻辑删除的举报（is_deleted = 0）
            criteria.andEqualTo("isDeleted", 0);
            int count = reportMapper.selectCountByExample(example);
            log.info("统计所有举报数量完成，总举报数：{}", count);
            return count;
        } catch (Exception e) {
            log.error("获取所有举报数量时发生错误", e);
            // 发生异常时返回0，避免影响接口调用
            return 0;
        }
    }

    // 统计所有视频举报的数量
    @Override
    public Integer getTotalVlogReportCount() {
        try {
            // 使用Example查询，过滤掉已逻辑删除的举报，且只统计视频举报
            Example example = new Example(Report.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("isDeleted", 0);
            criteria.andEqualTo("reportType", REPORT_TYPE_VLOG);
            int count = reportMapper.selectCountByExample(example);
            log.info("统计所有视频举报数量完成，视频举报数：{}", count);
            return count;
        } catch (Exception e) {
            log.error("获取所有视频举报数量时发生错误", e);
            // 发生异常时返回0，避免影响接口调用
            return 0;
        }
    }

    // 统计所有评论举报的数量
    @Override
    public Integer getTotalCommentReportCount() {
        try {
            // 使用Example查询，过滤掉已逻辑删除的举报，且只统计评论举报
            Example example = new Example(Report.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("isDeleted", 0);
            criteria.andEqualTo("reportType", REPORT_TYPE_COMMENT);
            int count = reportMapper.selectCountByExample(example);
            log.info("统计所有评论举报数量完成，评论举报数：{}", count);
            return count;
        } catch (Exception e) {
            log.error("获取所有评论举报数量时发生错误", e);
            // 发生异常时返回0，避免影响接口调用
            return 0;
        }
    }
}

