package com.zx.cuicuisha.bo;

import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommentBO {

   @NotBlank(message = "留言信息不完整")
   private String vlogerId;

   @NotBlank(message = "留言信息不完整")
   private String fatherCommentId;

   @NotBlank(message = "留言信息不完整")
   private String vlogId;

   @NotBlank(message = "当前用户信息不正确，请尝试重新登录")
   private String commentUserId;

   @NotBlank(message = "评论内容不能为空")
   @Length(max = 50, message = "评论内容长度不能超过50")
   private String content;

}