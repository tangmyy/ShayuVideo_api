package com.zx.cuicuisha.impl;

import com.github.pagehelper.PageHelper;
import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.base.RabbitMQConfig;
import com.zx.cuicuisha.bo.AppealBO;
import com.zx.cuicuisha.enums.MessageTypeEnum;
import com.zx.cuicuisha.exceptions.GraceException;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.mapper.AppealMapper;
import com.zx.cuicuisha.mapper.AppealMapperCustom;
import com.zx.cuicuisha.mapper.ReportMapper;
import com.zx.cuicuisha.mo.MessageMO;
import com.zx.cuicuisha.repository.MessageRepository;
import com.zx.cuicuisha.pojo.Appeal;
import com.zx.cuicuisha.pojo.Comment;
import com.zx.cuicuisha.pojo.Report;
import com.zx.cuicuisha.pojo.Vlog;
import com.zx.cuicuisha.service.AppealService;
import com.zx.cuicuisha.service.CommentService;
import com.zx.cuicuisha.service.VlogService;
import com.zx.cuicuisha.utils.JsonUtils;
import com.zx.cuicuisha.utils.PagedGridResult;
import com.zx.cuicuisha.vo.AppealAdminVO;
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
public class AppealServiceImpl extends BaseInfoProperties implements AppealService {

    private static final Integer APPEAL_TYPE_VLOG = 1;  // 视频
    private static final Integer APPEAL_TYPE_COMMENT = 2;  // 评论
    private static final Integer APPEAL_STATUS_PENDING = 0;  // 待处理
    private static final Integer APPEAL_STATUS_HANDLED = 1;  // 已处理（通过）
    private static final Integer APPEAL_STATUS_REJECTED = 2;  // 已驳回

    @Autowired
    private AppealMapper appealMapper;
    @Autowired
    private AppealMapperCustom appealMapperCustom;
    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private Sid sid;
    @Autowired
    private VlogService vlogService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private MessageRepository messageRepository;

    // 创建申诉
    @Override
    @Transactional
    public Appeal createAppeal(AppealBO appealBO) {
        // 验证举报记录是否存在
        Report report = reportMapper.selectByPrimaryKey(appealBO.getReportId());
        if (report == null) {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
        if (report.getIsDeleted() != null && report.getIsDeleted() == 1) {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
        // 验证申诉人是否为被举报人
        String reportedUserId = null;
        if (report.getReportType() == 1) {  // 举报视频
            Vlog vlog = vlogService.getVlog(report.getTargetId());
            if (vlog != null) {
                reportedUserId = vlog.getVlogerId();
            }
        } else if (report.getReportType() == 2) {  // 举报评论
            Comment comment = commentService.getComment(report.getTargetId());
            if (comment != null) {
                reportedUserId = comment.getCommentUserId();
            }
        }
        if (StringUtils.isBlank(reportedUserId) || !reportedUserId.equals(appealBO.getAppealerId())) {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
        // 检查是否已申诉过
        String existingAppealId = appealMapperCustom.checkAppealExists(
                appealBO.getAppealerId(), appealBO.getReportId());
        if (StringUtils.isNotBlank(existingAppealId)) {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
        // 创建申诉记录
        String appealId = sid.nextShort();
        Appeal appeal = new Appeal();
        appeal.setId(appealId);
        appeal.setReportId(appealBO.getReportId());
        appeal.setAppealerId(appealBO.getAppealerId());
        appeal.setAppealType(report.getReportType());  // 申诉类型与举报类型一致
        appeal.setTargetId(report.getTargetId());  // 被申诉对象ID与举报对象ID一致
        appeal.setReason(appealBO.getReason());
        appeal.setDescription(appealBO.getDescription());
        appeal.setStatus(APPEAL_STATUS_PENDING);
        appeal.setIsDeleted(0);
        Date now = new Date();
        appeal.setCreateTime(now);
        appeal.setUpdatedTime(now);

        appealMapper.insert(appeal);
        return appeal;
    }

    // Admin - 查询申诉列表
    @Override
    public PagedGridResult queryAppeals(Integer appealType, Integer status, String targetId, String reportId, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        if (appealType != null) {
            map.put("appealType", appealType);
        }
        if (status != null) {
            map.put("status", status);
        }
        if (StringUtils.isNotBlank(targetId)) {
            map.put("targetId", targetId);
        }
        if (StringUtils.isNotBlank(reportId)) {
            map.put("reportId", reportId);
        }
        PageHelper.startPage(page, pageSize);
        List<AppealAdminVO> list = appealMapperCustom.getAppealList(map);
        return setterPagedGrid(list, page);
    }

    // Admin - 处理申诉
    @Override
    @Transactional
    public Appeal handleAppeal(String appealId, String handlerId, Integer status, String handleResult, Boolean notifyAppealer) {
        // 验证申诉是否存在
        Appeal appeal = appealMapper.selectByPrimaryKey(appealId);
        if (appeal == null) {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
        if (appeal.getIsDeleted() != null && appeal.getIsDeleted() == 1) {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
        // 验证状态值
        if (status == null || (status != APPEAL_STATUS_HANDLED && status != APPEAL_STATUS_REJECTED)) {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
        // 更新申诉记录
        Appeal updateAppeal = new Appeal();
        updateAppeal.setId(appealId);
        updateAppeal.setStatus(status);
        updateAppeal.setHandlerId(handlerId);
        updateAppeal.setHandleTime(new Date());
        updateAppeal.setHandleResult(handleResult);
        updateAppeal.setUpdatedTime(new Date());
        appealMapper.updateByPrimaryKeySelective(updateAppeal);
        // 如果选择通知，发送消息通知申诉人
        if (notifyAppealer != null && notifyAppealer) {
            try {
                Map<String, Object> msgContent = new HashMap<>();
                // 从已查询的 appeal 对象获取申诉类型
                Integer appealType = appeal.getAppealType();  // 1-视频，2-评论
                // 使用方法参数的 status（新的处理状态），而不是 appeal.getStatus()（旧状态）
                Integer appealStatus = status;  // 1-已处理（通过），2-已驳回
                // 转换为可读的字符串
                String appealTypeStr = appealType == APPEAL_TYPE_VLOG ? "视频" : "评论";
                String appealStatusStr = appealStatus == APPEAL_STATUS_HANDLED ? "已通过" : "已驳回";
                // 如果handleResult为空，设置默认值
                String finalHandleResult = StringUtils.isBlank(handleResult) ? "已收到反馈！" : handleResult;
                String msg = "您的" + appealTypeStr + "申诉" + appealStatusStr + "\n处理结果：" + finalHandleResult;

                msgContent.put("appealId", appealId);
                msgContent.put("reportId", appeal.getReportId());
                msgContent.put("handleResult", msg);
                // 如果是申诉视频
                if (appeal.getAppealType() == APPEAL_TYPE_VLOG) {
                    Vlog vlog = vlogService.getVlog(appeal.getTargetId());
                    if (vlog != null) {
                        msgContent.put("vlogId", vlog.getId());
                        msgContent.put("vlogCover", vlog.getCover());
                        msgContent.put("vlogTitle", vlog.getTitle());
                    }
                // 如果是申诉评论
                } else if (appeal.getAppealType() == APPEAL_TYPE_COMMENT) {
                    Comment comment = commentService.getComment(appeal.getTargetId());
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
                // 发送消息到MQ（异步）- 通知申诉人
                MessageMO messageMO = new MessageMO();
                messageMO.setFromUserId(handlerId);
                messageMO.setToUserId(appeal.getAppealerId());
                messageMO.setMsgContent(msgContent);
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG, "sys.msg." + MessageTypeEnum.APPEAL_HANDLED.enValue, JsonUtils.objectToJson(messageMO)
                );
                log.info("申诉处理通知已发送到MQ：appealId={}, appealerId={}", appealId, appeal.getAppealerId());
            } catch (Exception e) {
                log.error("发送申诉处理通知失败：appealId={}", appealId, e);
                // 通知失败不影响主流程，只记录日志
            }
        }
        return appealMapper.selectByPrimaryKey(appealId);
    }

    // Admin - 修改已处理的申诉记录（同时更新SQL和MongoDB消息）
    @Override
    @Transactional
    public Appeal updateAppeal(String appealId, String handlerId, Integer status, String handleResult) {
        // 1. 验证申诉是否存在
        Appeal appeal = appealMapper.selectByPrimaryKey(appealId);
        if (appeal == null) {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
        if (appeal.getIsDeleted() != null && appeal.getIsDeleted() == 1) {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
        // 2. 验证状态值（只能修改已处理的记录，状态必须是1或2）
        if (status == null || (status != APPEAL_STATUS_HANDLED && status != APPEAL_STATUS_REJECTED)) {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
        // 3. 验证记录是否已被处理过（不能修改待处理的记录）
        if (appeal.getStatus() == null || appeal.getStatus() == APPEAL_STATUS_PENDING) {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
        // 4. 更新SQL中的申诉记录
        Appeal updateAppeal = new Appeal();
        updateAppeal.setId(appealId);
        updateAppeal.setStatus(status);
        updateAppeal.setHandlerId(handlerId);
        updateAppeal.setHandleTime(new Date());
        updateAppeal.setHandleResult(handleResult);
        updateAppeal.setUpdatedTime(new Date());
        appealMapper.updateByPrimaryKeySelective(updateAppeal);
        // 5. 更新MongoDB中的消息记录
        try {
            // 查询所有消息，查找对应的申诉处理消息
            List<MessageMO> allMessages = messageRepository.findAll();
            boolean found = false;
            for (MessageMO message : allMessages) {
                // 检查消息类型是否为申诉处理，且msgContent中包含对应的appealId
                if (message.getMsgType() != null && message.getMsgType() == MessageTypeEnum.APPEAL_HANDLED.type) {
                    Map<String, Object> msgContent = message.getMsgContent();
                    if (msgContent != null && appealId.equals(msgContent.get("appealId"))) {
                        // 找到对应的消息，更新处理结果
                        Integer appealType = appeal.getAppealType();
                        String appealTypeStr = appealType == APPEAL_TYPE_VLOG ? "视频" : "评论";
                        String appealStatusStr = status == APPEAL_STATUS_HANDLED ? "已通过" : "已驳回";
                        String finalHandleResult = StringUtils.isBlank(handleResult) ? "已收到反馈！" : handleResult;
                        String msg = "您的" + appealTypeStr + "申诉" + appealStatusStr + "\n处理结果：" + finalHandleResult;
                        // 更新msgContent中的handleResult
                        msgContent.put("handleResult", msg);
                        message.setMsgContent(msgContent);
                        messageRepository.save(message);
                        log.info("更新MongoDB消息成功：msgId={}, appealId={}", message.getId(), appealId);
                        found = true;
                        break; // 找到后退出循环
                    }
                }
            }
            if (!found) {
                log.warn("未找到对应的MongoDB消息记录：appealId={}", appealId);
            }
        } catch (Exception e) {
            log.error("更新MongoDB消息失败：appealId={}", appealId, e);
            // MongoDB更新失败不影响SQL更新，只记录日志
        }
        return appealMapper.selectByPrimaryKey(appealId);
    }

    // Admin - 重置申诉的处理记录（将状态归零，清空处理信息，并删除MongoDB消息）
    @Override
    @Transactional
    public Appeal resetAppeal(String appealId) {
        // 1. 验证申诉是否存在
        Appeal appeal = appealMapper.selectByPrimaryKey(appealId);
        if (appeal == null) {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
        if (appeal.getIsDeleted() != null && appeal.getIsDeleted() == 1) {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
        // 2. 更新SQL中的申诉记录：状态归零，清空处理信息
        int updateCount = appealMapperCustom.resetAppealStatus(appealId);
        if (updateCount != 1) {
            log.error("重置申诉记录失败：appealId={}, updateCount={}", appealId, updateCount);
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
        // 3. 删除MongoDB中对应的消息文档
        try {
            List<MessageMO> allMessages = messageRepository.findAll();
            boolean found = false;
            for (MessageMO message : allMessages) {
                // 检查消息类型是否为申诉处理，且msgContent中包含对应的appealId
                if (message.getMsgType() != null && message.getMsgType() == MessageTypeEnum.APPEAL_HANDLED.type) {
                    Map<String, Object> msgContent = message.getMsgContent();
                    if (msgContent != null && appealId.equals(msgContent.get("appealId"))) {
                        // 找到对应的消息，删除
                        messageRepository.deleteById(message.getId());
                        log.info("删除MongoDB消息成功：msgId={}, appealId={}", message.getId(), appealId);
                        found = true;
                        break; // 找到后退出循环
                    }
                }
            }
            if (!found) {
                log.warn("未找到对应的MongoDB消息记录：appealId={}", appealId);
            }
        } catch (Exception e) {
            log.error("删除MongoDB消息失败：appealId={}", appealId, e);
            // MongoDB删除失败不影响SQL更新，只记录日志
        }
        
        return appealMapper.selectByPrimaryKey(appealId);
    }

    // Admin - 统计所有申诉的数量（不包括已逻辑删除的）
    @Override
    public Integer getTotalAppealCount() {
        try {
            // 使用Example查询，过滤掉已逻辑删除的申诉
            Example example = new Example(Appeal.class);
            Example.Criteria criteria = example.createCriteria();
            // 只统计未逻辑删除的申诉（is_deleted = 0）
            criteria.andEqualTo("isDeleted", 0);
            int count = appealMapper.selectCountByExample(example);
            log.info("统计所有申诉数量完成，总申诉数：{}", count);
            return count;
        } catch (Exception e) {
            log.error("获取所有申诉数量时发生错误", e);
            // 发生异常时返回0，避免影响接口调用
            return 0;
        }
    }

    // Admin - 统计所有视频申诉的数量（不包括已逻辑删除的）
    @Override
    public Integer getTotalVlogAppealCount() {
        try {
            // 使用Example查询，过滤掉已逻辑删除的申诉，且只统计视频申诉
            Example example = new Example(Appeal.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("isDeleted", 0);
            criteria.andEqualTo("appealType", APPEAL_TYPE_VLOG);
            int count = appealMapper.selectCountByExample(example);
            log.info("统计所有视频申诉数量完成，视频申诉数：{}", count);
            return count;
        } catch (Exception e) {
            log.error("获取所有视频申诉数量时发生错误", e);
            // 发生异常时返回0，避免影响接口调用
            return 0;
        }
    }

    // Admin - 统计所有评论申诉的数量（不包括已逻辑删除的）
    @Override
    public Integer getTotalCommentAppealCount() {
        try {
            // 使用Example查询，过滤掉已逻辑删除的申诉，且只统计评论申诉
            Example example = new Example(Appeal.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("isDeleted", 0);
            criteria.andEqualTo("appealType", APPEAL_TYPE_COMMENT);
            int count = appealMapper.selectCountByExample(example);
            log.info("统计所有评论申诉数量完成，评论申诉数：{}", count);
            return count;
        } catch (Exception e) {
            log.error("获取所有评论申诉数量时发生错误", e);
            // 发生异常时返回0，避免影响接口调用
            return 0;
        }
    }
}

