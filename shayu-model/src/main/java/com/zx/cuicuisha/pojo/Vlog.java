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
public class Vlog {
    @Id
    private String id;

    /**
     * 对应用户表id，vlog视频发布者
     */
    @Column(name = "vloger_id")
    private String vlogerId;

    /**
     * 视频播放地址
     */
    private String url;

    /**
     * 视频封面
     */
    private String cover;

    /**
     * 视频标题，可以为空
     */
    private String title;

    /**
     * 视频标签，多个标签用逗号分隔
     */
    private String tag;

    /**
     * 视频width
     */
    private Integer width;

    /**
     * 视频height
     */
    private Integer height;

    /**
     * 点赞总数
     */
    @Column(name = "like_counts")
    private Integer likeCounts;

    /**
     * 评论总数
     */
    @Column(name = "comments_counts")
    private Integer commentsCounts;

    /**
     * 是否私密，用户可以设置私密，如此可以不公开给比人看
     */
    @Column(name = "is_private")
    private Integer isPrivate;

    /**
     * 逻辑删除 0-正常，1-逻辑删除
     */
    @Column(name = "is_deleted")
    private Integer isDeleted;

    /**
     * 创建时间 创建时间
     */
    @Column(name = "created_time")
    private Date createdTime;

    /**
     * 更新时间 更新时间
     */
    @Column(name = "updated_time")
    private Date updatedTime;
}