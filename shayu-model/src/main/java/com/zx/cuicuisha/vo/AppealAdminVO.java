package com.zx.cuicuisha.vo;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 申诉管理VO，用于展示完整的申诉信息（包括申诉人、关联举报、被申诉对象等详细信息）
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AppealAdminVO {
    private String id;                      // 申诉ID
    private String reportId;                // 关联的举报记录ID
    private String appealerId;              // 申诉人ID
    private String appealerNickname;        // 申诉人昵称
    private String appealerFace;            // 申诉人头像
    private Integer appealType;             // 申诉类型：1-视频，2-评论
    private String targetId;                // 被申诉对象ID
    private String targetTitle;             // 被申诉视频标题（如果是视频）
    private String targetContent;           // 被申诉评论内容（如果是评论）
    private String reason;                  // 申诉原因
    private String description;             // 申诉描述
    private Integer status;                 // 处理状态：0-待处理，1-已处理（通过），2-已驳回
    private String handlerId;               // 处理人ID
    private String handlerNickname;         // 处理人昵称
    private Date handleTime;                // 处理时间
    private String handleResult;            // 处理结果说明
    private Date createTime;                // 申诉时间
    private Date updatedTime;               // 更新时间
    private Integer isDeleted;              // 逻辑删除：0-正常，1-已删除
}

