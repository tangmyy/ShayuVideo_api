package com.zx.cuicuisha.vo;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 粉丝关系VO，用于展示完整的粉丝关系信息（包括博主和粉丝的详细信息）
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FansAdminVO {
   private String id;                    // 关系ID
   private String vlogerId;              // 博主ID
   private String vlogerNickname;        // 博主昵称
   private String vlogerFace;            // 博主头像
   private String fanId;                 // 粉丝ID
   private String fanNickname;           // 粉丝昵称
   private String fanFace;               // 粉丝头像
   private Integer isFanFriendOfMine;     // 是否是朋友关系（互关）
}
