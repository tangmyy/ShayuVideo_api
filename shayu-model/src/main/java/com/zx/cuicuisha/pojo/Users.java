package com.zx.cuicuisha.pojo;



import javax.persistence.Column;
import javax.persistence.Id;
import java.util.Date;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Users {
   @Id
   private String id;

   private String mobile;

   private String nickname;

   @Column(name = "video_num")
   private String videoNum;

   private String face;

   private Integer sex;

   private Date birthday;

   private String country;

   private String province;

   private String city;

   private String district;

   private String description;

   @Column(name = "bg_img")
   private String bgImg;

   @Column(name = "can_video_num_be_updated")
   private Integer canVideoNumBeUpdated;

   private Integer status;

   @Column(name = "created_time")
   private Date createdTime;

   @Column(name = "updated_time")
   private Date updatedTime;

   @Column(name = "is_deleted")
   private Integer isDeleted;
}