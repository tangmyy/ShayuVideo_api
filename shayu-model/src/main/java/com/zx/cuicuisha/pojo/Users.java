package com.zx.cuicuisha.pojo;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.Date;

@Getter
public class Users {
   /**
    * -- GETTER --
    *
    * @return id
    */
   @Id
    private String id;

    /**
     * 手机号
     * -- GETTER --
     *  获取手机号
     *
     * @return mobile - 手机号

     */
    private String mobile;

    /**
     * 昵称，媒体号
     * -- GETTER --
     *  获取昵称，媒体号
     *
     * @return nickname - 昵称，媒体号

     */
    private String nickname;

    /**
     * 慕课号，类似头条号，抖音号，公众号，唯一标识，需要限制修改次数，比如终生1次，每年1次，每半年1次等，可以用于付费修改。
     * -- GETTER --
     *  获取慕课号，类似头条号，抖音号，公众号，唯一标识，需要限制修改次数，比如终生1次，每年1次，每半年1次等，可以用于付费修改。
     *
     * @return imooc_num - 慕课号，类似头条号，抖音号，公众号，唯一标识，需要限制修改次数，比如终生1次，每年1次，每半年1次等，可以用于付费修改。

     */
    @Column(name = "imooc_num")
    private String imoocNum;

    /**
     * 头像
     * -- GETTER --
     *  获取头像
     *
     * @return face - 头像

     */
    private String face;

    /**
     * 性别 1:男  0:女  2:保密
     * -- GETTER --
     *  获取性别 1:男  0:女  2:保密
     *
     * @return sex - 性别 1:男  0:女  2:保密

     */
    private Integer sex;

    /**
     * 生日
     * -- GETTER --
     *  获取生日
     *
     * @return birthday - 生日

     */
    private Date birthday;

    /**
     * 国家
     * -- GETTER --
     *  获取国家
     *
     * @return country - 国家

     */
    private String country;

    /**
     * 省份
     * -- GETTER --
     *  获取省份
     *
     * @return province - 省份

     */
    private String province;

    /**
     * 城市
     * -- GETTER --
     *  获取城市
     *
     * @return city - 城市

     */
    private String city;

    /**
     * 区县
     * -- GETTER --
     *  获取区县
     *
     * @return district - 区县

     */
    private String district;

    /**
     * 简介
     * -- GETTER --
     *  获取简介
     *
     * @return description - 简介

     */
    private String description;

    /**
     * 个人介绍的背景图
     * -- GETTER --
     *  获取个人介绍的背景图
     *
     * @return bg_img - 个人介绍的背景图

     */
    @Column(name = "bg_img")
    private String bgImg;

    /**
     * 慕课号能否被修改，1：默认，可以修改；0，无法修改
     * -- GETTER --
     *  获取慕课号能否被修改，1：默认，可以修改；0，无法修改
     *
     * @return can_imooc_num_be_updated - 慕课号能否被修改，1：默认，可以修改；0，无法修改

     */
    @Column(name = "can_imooc_num_be_updated")
    private Integer canImoocNumBeUpdated;

    /**
     * 创建时间 创建时间
     * -- GETTER --
     *  获取创建时间 创建时间
     *
     * @return created_time - 创建时间 创建时间

     */
    @Column(name = "created_time")
    private Date createdTime;

    /**
     * 更新时间 更新时间
     * -- GETTER --
     *  获取更新时间 更新时间
     *
     * @return updated_time - 更新时间 更新时间

     */
    @Column(name = "updated_time")
    private Date updatedTime;

   /**
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

   /**
     * 设置手机号
     *
     * @param mobile 手机号
     */
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

   /**
     * 设置昵称，媒体号
     *
     * @param nickname 昵称，媒体号
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

   /**
     * 设置慕课号，类似头条号，抖音号，公众号，唯一标识，需要限制修改次数，比如终生1次，每年1次，每半年1次等，可以用于付费修改。
     *
     * @param imoocNum 慕课号，类似头条号，抖音号，公众号，唯一标识，需要限制修改次数，比如终生1次，每年1次，每半年1次等，可以用于付费修改。
     */
    public void setImoocNum(String imoocNum) {
        this.imoocNum = imoocNum;
    }

   /**
     * 设置头像
     *
     * @param face 头像
     */
    public void setFace(String face) {
        this.face = face;
    }

   /**
     * 设置性别 1:男  0:女  2:保密
     *
     * @param sex 性别 1:男  0:女  2:保密
     */
    public void setSex(Integer sex) {
        this.sex = sex;
    }

   /**
     * 设置生日
     *
     * @param birthday 生日
     */
    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

   /**
     * 设置国家
     *
     * @param country 国家
     */
    public void setCountry(String country) {
        this.country = country;
    }

   /**
     * 设置省份
     *
     * @param province 省份
     */
    public void setProvince(String province) {
        this.province = province;
    }

   /**
     * 设置城市
     *
     * @param city 城市
     */
    public void setCity(String city) {
        this.city = city;
    }

   /**
     * 设置区县
     *
     * @param district 区县
     */
    public void setDistrict(String district) {
        this.district = district;
    }

   /**
     * 设置简介
     *
     * @param description 简介
     */
    public void setDescription(String description) {
        this.description = description;
    }

   /**
     * 设置个人介绍的背景图
     *
     * @param bgImg 个人介绍的背景图
     */
    public void setBgImg(String bgImg) {
        this.bgImg = bgImg;
    }

   /**
     * 设置慕课号能否被修改，1：默认，可以修改；0，无法修改
     *
     * @param canImoocNumBeUpdated 慕课号能否被修改，1：默认，可以修改；0，无法修改
     */
    public void setCanImoocNumBeUpdated(Integer canImoocNumBeUpdated) {
        this.canImoocNumBeUpdated = canImoocNumBeUpdated;
    }

   /**
     * 设置创建时间 创建时间
     *
     * @param createdTime 创建时间 创建时间
     */
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

   /**
     * 设置更新时间 更新时间
     *
     * @param updatedTime 更新时间 更新时间
     */
    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }
}