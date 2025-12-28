package com.zx.cuicuisha.bo;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class VlogAuditBO {

    @NotBlank(message = "视频ID不能为空")
    private String vlogId;

    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;  // 0-待审核 1-通过 2-拒绝

    private String auditorId;

    private String auditorName;

    @Length(max = 500, message = "审核备注长度不能超过500")
    private String auditRemark;
}

