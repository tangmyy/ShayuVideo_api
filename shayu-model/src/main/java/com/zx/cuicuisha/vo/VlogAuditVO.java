package com.zx.cuicuisha.vo;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 视频审核记录VO，用于展示审核信息
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class VlogAuditVO {
    private Long id;                      // 主键ID
    private String vlogId;                // 视频ID
    private Integer auditStatus;          // 审核状态 0-待审核 1-通过 2-拒绝
    private String auditorId;             // 审核人ID
    private String auditorName;           // 审核人姓名
    private Date auditTime;               // 审核时间
    private String auditRemark;           // 审核备注
    private Date createTime;              // 创建时间
}

