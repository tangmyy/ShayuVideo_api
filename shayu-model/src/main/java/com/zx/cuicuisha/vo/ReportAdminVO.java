package com.zx.cuicuisha.vo;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 举报管理VO，用于展示完整的举报信息（包括举报人、被举报对象等详细信息）
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReportAdminVO {
    private String id;                      // 举报ID
    private String reporterId;              // 举报人ID
    private String reporterNickname;        // 举报人昵称
    private String reporterFace;            // 举报人头像
    private Integer reportType;             // 举报类型：1-视频，2-评论
    private String targetId;                // 被举报对象ID
    private String targetTitle;             // 被举报视频标题（如果是视频）
    private String targetContent;           // 被举报评论内容（如果是评论）
    private String targetUserId;            // 被举报对象作者ID
    private String targetUserNickname;      // 被举报对象作者昵称
    private String targetUserFace;          // 被举报对象作者头像
    private String reason;                  // 举报原因
    private String description;             // 举报描述
    private Integer status;                 // 处理状态：0-待处理，1-已处理，2-已驳回
    private String handlerId;               // 处理人ID
    private String handlerNickname;         // 处理人昵称
    private Date handleTime;                // 处理时间
    private String handleResult;            // 处理结果说明
    private Date createTime;                // 举报时间
    private Date updatedTime;               // 更新时间
    private Integer isDeleted;              // 逻辑删除：0-正常，1-已删除
}

