package com.zx.cuicuisha.pojo;

import java.util.Date;
import javax.persistence.*;

public class Appeal {
    /**
     * 主键id
     */
    @Id
    private String id;

    /**
     * 关联的举报记录id
     */
    @Column(name = "report_id")
    private String reportId;

    /**
     * 申诉人id（被举报人）
     */
    @Column(name = "appealer_id")
    private String appealerId;

    /**
     * 申诉类型：1-视频，2-评论（与举报类型一致）
     */
    @Column(name = "appeal_type")
    private Integer appealType;

    /**
     * 被申诉对象id（vlog_id或comment_id）
     */
    @Column(name = "target_id")
    private String targetId;

    /**
     * 申诉原因（如：误判、内容合规、已修改等）
     */
    private String reason;

    /**
     * 申诉描述（详细说明）
     */
    private String description;

    /**
     * 处理状态：0-待处理，1-已处理（通过），2-已驳回
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
     * 申诉时间
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
     * 获取关联的举报记录id
     *
     * @return report_id - 关联的举报记录id
     */
    public String getReportId() {
        return reportId;
    }

    /**
     * 设置关联的举报记录id
     *
     * @param reportId 关联的举报记录id
     */
    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    /**
     * 获取申诉人id（被举报人）
     *
     * @return appealer_id - 申诉人id（被举报人）
     */
    public String getAppealerId() {
        return appealerId;
    }

    /**
     * 设置申诉人id（被举报人）
     *
     * @param appealerId 申诉人id（被举报人）
     */
    public void setAppealerId(String appealerId) {
        this.appealerId = appealerId;
    }

    /**
     * 获取申诉类型：1-视频，2-评论（与举报类型一致）
     *
     * @return appeal_type - 申诉类型：1-视频，2-评论（与举报类型一致）
     */
    public Integer getAppealType() {
        return appealType;
    }

    /**
     * 设置申诉类型：1-视频，2-评论（与举报类型一致）
     *
     * @param appealType 申诉类型：1-视频，2-评论（与举报类型一致）
     */
    public void setAppealType(Integer appealType) {
        this.appealType = appealType;
    }

    /**
     * 获取被申诉对象id（vlog_id或comment_id）
     *
     * @return target_id - 被申诉对象id（vlog_id或comment_id）
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * 设置被申诉对象id（vlog_id或comment_id）
     *
     * @param targetId 被申诉对象id（vlog_id或comment_id）
     */
    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    /**
     * 获取申诉原因（如：误判、内容合规、已修改等）
     *
     * @return reason - 申诉原因（如：误判、内容合规、已修改等）
     */
    public String getReason() {
        return reason;
    }

    /**
     * 设置申诉原因（如：误判、内容合规、已修改等）
     *
     * @param reason 申诉原因（如：误判、内容合规、已修改等）
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * 获取申诉描述（详细说明）
     *
     * @return description - 申诉描述（详细说明）
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置申诉描述（详细说明）
     *
     * @param description 申诉描述（详细说明）
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取处理状态：0-待处理，1-已处理（通过），2-已驳回
     *
     * @return status - 处理状态：0-待处理，1-已处理（通过），2-已驳回
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置处理状态：0-待处理，1-已处理（通过），2-已驳回
     *
     * @param status 处理状态：0-待处理，1-已处理（通过），2-已驳回
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
     * 获取申诉时间
     *
     * @return create_time - 申诉时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 设置申诉时间
     *
     * @param createTime 申诉时间
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