package com.zx.cuicuisha.pojo;

import java.util.Date;
import javax.persistence.*;

@Table(name = "vlog_audit")
public class VlogAudit {
    /**
     * 主键ID
     */
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
    private Byte auditStatus;

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
     * 封禁前的原始私密状态（封禁时保存，解封时恢复）0-公开 1-私密
     */
    @Column(name = "original_is_private")
    private Integer originalIsPrivate;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 获取主键ID
     *
     * @return id - 主键ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置主键ID
     *
     * @param id 主键ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取视频ID
     *
     * @return vlog_id - 视频ID
     */
    public String getVlogId() {
        return vlogId;
    }

    /**
     * 设置视频ID
     *
     * @param vlogId 视频ID
     */
    public void setVlogId(String vlogId) {
        this.vlogId = vlogId;
    }

    /**
     * 获取审核状态 0-待审核 1-通过 2-拒绝
     *
     * @return audit_status - 审核状态 0-待审核 1-通过 2-拒绝
     */
    public Byte getAuditStatus() {
        return auditStatus;
    }

    /**
     * 设置审核状态 0-待审核 1-通过 2-拒绝
     *
     * @param auditStatus 审核状态 0-待审核 1-通过 2-拒绝
     */
    public void setAuditStatus(Byte auditStatus) {
        this.auditStatus = auditStatus;
    }

    /**
     * 获取审核人ID
     *
     * @return auditor_id - 审核人ID
     */
    public String getAuditorId() {
        return auditorId;
    }

    /**
     * 设置审核人ID
     *
     * @param auditorId 审核人ID
     */
    public void setAuditorId(String auditorId) {
        this.auditorId = auditorId;
    }

    /**
     * 获取审核人姓名
     *
     * @return auditor_name - 审核人姓名
     */
    public String getAuditorName() {
        return auditorName;
    }

    /**
     * 设置审核人姓名
     *
     * @param auditorName 审核人姓名
     */
    public void setAuditorName(String auditorName) {
        this.auditorName = auditorName;
    }

    /**
     * 获取审核时间
     *
     * @return audit_time - 审核时间
     */
    public Date getAuditTime() {
        return auditTime;
    }

    /**
     * 设置审核时间
     *
     * @param auditTime 审核时间
     */
    public void setAuditTime(Date auditTime) {
        this.auditTime = auditTime;
    }

    /**
     * 获取审核备注
     *
     * @return audit_remark - 审核备注
     */
    public String getAuditRemark() {
        return auditRemark;
    }

    /**
     * 设置审核备注
     *
     * @param auditRemark 审核备注
     */
    public void setAuditRemark(String auditRemark) {
        this.auditRemark = auditRemark;
    }

    /**
     * 获取封禁前的原始私密状态（封禁时保存，解封时恢复）0-公开 1-私密
     *
     * @return original_is_private - 封禁前的原始私密状态（封禁时保存，解封时恢复）0-公开 1-私密
     */
    public Integer getOriginalIsPrivate() {
        return originalIsPrivate;
    }

    /**
     * 设置封禁前的原始私密状态（封禁时保存，解封时恢复）0-公开 1-私密
     *
     * @param originalIsPrivate 封禁前的原始私密状态（封禁时保存，解封时恢复）0-公开 1-私密
     */
    public void setOriginalIsPrivate(Integer originalIsPrivate) {
        this.originalIsPrivate = originalIsPrivate;
    }

    /**
     * 获取创建时间
     *
     * @return create_time - 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 设置创建时间
     *
     * @param createTime 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}