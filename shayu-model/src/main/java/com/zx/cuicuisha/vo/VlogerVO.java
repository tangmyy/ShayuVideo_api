package com.zx.cuicuisha.vo;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class VlogerVO {
   private String vlogerId;
   private String nickname;
   private String face;
   private boolean isFollowed = true;
}
