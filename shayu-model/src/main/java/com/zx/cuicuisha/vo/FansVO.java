package com.zx.cuicuisha.vo;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class FansVO {
   private String fanId;
   private String nickname;
   private String face;
   private boolean isFriend = false;
}