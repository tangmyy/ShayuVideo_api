package com.zx.cuicuisha.vo;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 评论管理VO，用于展示完整的评论信息（包括评论者、视频、视频作者等详细信息）
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommentAdminVO {
   private String id;                      // 评论ID
   private String vlogId;                  // 视频ID
   private String vlogTitle;               // 视频标题
   private String vlogerId;                // 视频作者ID
   private String vlogerNickname;          // 视频作者昵称
   private String vlogerFace;              // 视频作者头像
   private String commentUserId;           // 评论者ID
   private String commentUserNickname;     // 评论者昵称
   private String commentUserFace;         // 评论者头像
   private String fatherCommentId;        // 父评论ID（如果是回复）
   private String content;                 // 评论内容
   private Integer likeCounts;             // 点赞数
   private String replyedUserNickname;     // 被回复用户昵称（如果是回复评论）
   private Integer isDeleted;              // 逻辑删除 0-正常，1-逻辑删除
   private Date createTime;                // 创建时间
}
