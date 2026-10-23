package com.zx.cuicuisha.bo;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AppealBO {

    @NotBlank(message = "举报记录ID不能为空")
    private String reportId;

    @NotBlank(message = "申诉原因不能为空")
    @Length(max = 100, message = "申诉原因长度不能超过100")
    private String reason;

    @Length(max = 500, message = "申诉描述长度不能超过500")
    private String description;

    @NotBlank(message = "申诉人ID不能为空")
    private String appealerId;
}

