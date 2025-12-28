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
public class VlogAudit {
    @Id
    private Long id;

    /**
     * 视频ID
     */
    @Column(name = "vlog_id")
    private String vlogId;

    /**
     * 审核状态 0-待审核 1-通过 2-拒绝
     */
    @Column(name = "audit_status")
    private Integer auditStatus;

    /**
     * 审核人ID
     */
    @Column(name = "auditor_id")
    private String auditorId;

    /**
     * 审核人姓名
     */
    @Column(name = "auditor_name")
    private String auditorName;

    /**
     * 审核时间
     */
    @Column(name = "audit_time")
    private Date auditTime;

    /**
     * 审核备注
     */
    @Column(name = "audit_remark")
    private String auditRemark;

    /**
     * 封禁前的原始私密状态（封禁时保存，解封时恢复）
     * 0-公开 1-私密
     */
    @Column(name = "original_is_private")
    private Integer originalIsPrivate;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;
}

