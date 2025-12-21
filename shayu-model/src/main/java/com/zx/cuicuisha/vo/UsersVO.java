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
public class UsersVO {
   private String id;
   private String mobile;
   private String nickname;
   private String videoNum;
   private String face;
   private Integer sex;
   private Date birthday;
   private String country;
   private String province;
   private String city;
   private String district;
   private String description;
   private String bgImg;
   private Integer canVideoNumBeUpdated;
   private Integer status;              // 用户状态 0-正常状态，1-封禁状态
   private Date createdTime;
   private Date updatedTime;
   private Integer isDeleted;          // 逻辑删除 0-正常，1-逻辑删除
   private String userToken;           // 用户token，传递给前端
   private Integer myFollowsCounts;    // 我关注的
   private Integer myFansCounts;       // 我的粉丝
   // private Integer myLikedVlogCounts;  // 我收到的赞   // 无需返回给前端
   private Integer totalLikeMeCounts;     // 我的视频收到的赞
}