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
}