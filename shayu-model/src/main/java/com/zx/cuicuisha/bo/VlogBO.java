package com.zx.cuicuisha.bo;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VlogBO {

   @NotBlank(message = "ID不能为空")
   private String id;

   @NotBlank(message = "博主ID不能为空")
   // @ValidVlogerId(message = "博主ID不存在，请检查用户信息")
   private String vlogerId;

   @NotBlank(message = "视频URL不能为空")
   @URL(message = "视频URL格式不正确")
   private String url;

   @NotBlank(message = "封面图不能为空")
   @URL(message = "封面图URL格式不正确")
   private String cover;

   @NotBlank(message = "标题不能为空")
   @Size(min = 2, message = "标题长度不能小于2个字符")
   private String title;

   @NotNull(message = "视频宽度不能为空")
   @Min(value = 1, message = "视频宽度必须大于0")
   private Integer width;

   @NotNull(message = "视频高度不能为空")
   @Min(value = 1, message = "视频高度必须大于0")
   private Integer height;

   // 这两个字段不校验
   private Integer likeCounts;
   private Integer commentsCounts;
}