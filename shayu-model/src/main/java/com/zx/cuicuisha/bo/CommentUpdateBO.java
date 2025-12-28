package com.zx.cuicuisha.bo;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

/**
 * 评论更新BO
 * 用于管理员更新评论内容
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdateBO {

   @NotBlank(message = "评论ID不能为空")
   private String id;

   @NotBlank(message = "评论内容不能为空")
   @Length(max = 50, message = "评论内容长度不能超过50")
   private String content;
}

