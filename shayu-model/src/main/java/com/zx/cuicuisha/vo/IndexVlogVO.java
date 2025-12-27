package com.zx.cuicuisha.vo;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class IndexVlogVO {
   private String vlogId;
   private String vlogerId;
   private String vlogerFace;
   private String vlogerName;
   private String content;
   private String tag;
   private String url;
   private String cover;
   private Integer width;
   private Integer height;
   private Integer likeCounts;
   private Integer commentsCounts;
   private Integer isPrivate;
   private Integer isDeleted; // 逻辑删除 0-正常，1-逻辑删除
   private boolean isPlay = false;
   private boolean doIFollowVloger = false;
   private boolean doILikeThisVlog = false;
}