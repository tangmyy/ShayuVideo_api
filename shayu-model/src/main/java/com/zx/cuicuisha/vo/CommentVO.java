package com.zx.cuicuisha.vo;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;


@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommentVO {
   private String id;
   private String commentId;
   private String vlogerId;
   private String fatherCommentId;
   private String vlogId;
   private String commentUserId;
   private String commentUserNickname;
   private String commentUserFace;
   private String content;
   private Integer likeCounts;
   private String replyedUserNickname;
   private Date createTime;
   private Integer isLike = 0;
}
