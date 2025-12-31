package com.zx.cuicuisha.pojo;

import java.util.Date;
import javax.persistence.*;

public class Report {
    /**
     * 主键id
     */
    @Id
    private String id;

    /**
     * 举报人id
     */
    @Column(name = "reporter_id")
    private String reporterId;

    /**
     * 举报类型：1-视频，2-评论
     */
    @Column(name = "report_type")
    private Integer reportType;

    /**
     * 被举报对象id（vlog_id或comment_id）
     */
    @Column(name = "target_id")
    private String targetId;

    /**
     * 举报原因（如：色情、暴力、垃圾信息等）
     */
    private String reason;

    /**
     * 举报描述（详细说明）
     */
    private String description;

    /**
     * 处理状态：0-待处理，1-已处理，2-已驳回
     */
    private Integer status;

    /**
     * 处理人id（管理员）
     */
    @Column(name = "handler_id")
    private String handlerId;

    /**
     * 处理时间
     */
    @Column(name = "handle_time")
    private Date handleTime;

    /**
     * 处理结果说明
     */
    @Column(name = "handle_result")
    private String handleResult;

    /**
     * 举报时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @Column(name = "updated_time")
    private Date updatedTime;

    /**
     * 逻辑删除：0-正常，1-已删除
     */
    @Column(name = "is_deleted")
    private Integer isDeleted;

    /**
     * 获取主键id
     *
     * @return id - 主键id
     */
    public String getId() {
        return id;
    }

    /**
     * 设置主键id
     *
     * @param id 主键id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取举报人id
     *
     * @return reporter_id - 举报人id
     */
    public String getReporterId() {
        return reporterId;
    }

    /**
     * 设置举报人id
     *
     * @param reporterId 举报人id
     */
    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    /**
     * 获取举报类型：1-视频，2-评论
     *
     * @return report_type - 举报类型：1-视频，2-评论
     */
    public Integer getReportType() {
        return reportType;
    }

    /**
     * 设置举报类型：1-视频，2-评论
     *
     * @param reportType 举报类型：1-视频，2-评论
     */
    public void setReportType(Integer reportType) {
        this.reportType = reportType;
    }

    /**
     * 获取被举报对象id（vlog_id或comment_id）
     *
     * @return target_id - 被举报对象id（vlog_id或comment_id）
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * 设置被举报对象id（vlog_id或comment_id）
     *
     * @param targetId 被举报对象id（vlog_id或comment_id）
     */
    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    /**
     * 获取举报原因（如：色情、暴力、垃圾信息等）
     *
     * @return reason - 举报原因（如：色情、暴力、垃圾信息等）
     */
    public String getReason() {
        return reason;
    }

    /**
     * 设置举报原因（如：色情、暴力、垃圾信息等）
     *
     * @param reason 举报原因（如：色情、暴力、垃圾信息等）
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * 获取举报描述（详细说明）
     *
     * @return description - 举报描述（详细说明）
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置举报描述（详细说明）
     *
     * @param description 举报描述（详细说明）
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取处理状态：0-待处理，1-已处理，2-已驳回
     *
     * @return status - 处理状态：0-待处理，1-已处理，2-已驳回
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置处理状态：0-待处理，1-已处理，2-已驳回
     *
     * @param status 处理状态：0-待处理，1-已处理，2-已驳回
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取处理人id（管理员）
     *
     * @return handler_id - 处理人id（管理员）
     */
    public String getHandlerId() {
        return handlerId;
    }

    /**
     * 设置处理人id（管理员）
     *
     * @param handlerId 处理人id（管理员）
     */
    public void setHandlerId(String handlerId) {
        this.handlerId = handlerId;
    }

    /**
     * 获取处理时间
     *
     * @return handle_time - 处理时间
     */
    public Date getHandleTime() {
        return handleTime;
    }

    /**
     * 设置处理时间
     *
     * @param handleTime 处理时间
     */
    public void setHandleTime(Date handleTime) {
        this.handleTime = handleTime;
    }

    /**
     * 获取处理结果说明
     *
     * @return handle_result - 处理结果说明
     */
    public String getHandleResult() {
        return handleResult;
    }

    /**
     * 设置处理结果说明
     *
     * @param handleResult 处理结果说明
     */
    public void setHandleResult(String handleResult) {
        this.handleResult = handleResult;
    }

    /**
     * 获取举报时间
     *
     * @return create_time - 举报时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 设置举报时间
     *
     * @param createTime 举报时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取更新时间
     *
     * @return updated_time - 更新时间
     */
    public Date getUpdatedTime() {
        return updatedTime;
    }

    /**
     * 设置更新时间
     *
     * @param updatedTime 更新时间
     */
    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    /**
     * 获取逻辑删除：0-正常，1-已删除
     *
     * @return is_deleted - 逻辑删除：0-正常，1-已删除
     */
    public Integer getIsDeleted() {
        return isDeleted;
    }

    /**
     * 设置逻辑删除：0-正常，1-已删除
     *
     * @param isDeleted 逻辑删除：0-正常，1-已删除
     */
    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}