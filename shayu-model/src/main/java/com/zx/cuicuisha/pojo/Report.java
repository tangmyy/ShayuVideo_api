package com.zx.cuicuisha.pojo;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.Date;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Report {
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
}

