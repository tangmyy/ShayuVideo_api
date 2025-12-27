package com.zx.cuicuisha.bo;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReportBO {

    @NotBlank(message = "举报类型不能为空")
    private String reportType;  // "1" 表示视频，"2" 表示评论

    @NotBlank(message = "被举报对象ID不能为空")
    private String targetId;

    @NotBlank(message = "举报原因不能为空")
    @Length(max = 100, message = "举报原因长度不能超过100")
    private String reason;

    @Length(max = 500, message = "举报描述长度不能超过500")
    private String description;

    @NotBlank(message = "举报人ID不能为空")
    private String reporterId;
}

