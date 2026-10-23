package com.zx.cuicuisha.vo;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ActiveUserVO {
   private String id;
   private String nickname;
   private String videoNum;
   private String face;
   private Integer videoNumCount;  // 视频数量（当sortBy=videos时使用）
   private Integer fansCounts;      // 粉丝数量（当sortBy=fans时使用）
}

