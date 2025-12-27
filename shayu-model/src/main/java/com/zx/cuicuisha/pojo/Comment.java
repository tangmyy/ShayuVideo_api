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
public class Comment {
    @Id
    private String id;

    /**
     * 评论的视频是哪个作者（vloger）的关联id
     */
    @Column(name = "vloger_id")
    private String vlogerId;

    /**
     * 如果是回复留言，则本条为子留言，需要关联查询
     */
    @Column(name = "father_comment_id")
    private String fatherCommentId;

    /**
     * 回复的那个视频id
     */
    @Column(name = "vlog_id")
    private String vlogId;

    /**
     * 发布留言的用户id
     */
    @Column(name = "comment_user_id")
    private String commentUserId;

    /**
     * 留言内容
     */
    private String content;

    /**
     * 留言的点赞总数
     */
    @Column(name = "like_counts")
    private Integer likeCounts;

    /**
     * 逻辑删除 0-正常，1-逻辑删除
     */
    @Column(name = "is_deleted")
    private Integer isDeleted;

    /**
     * 留言时间
     */
    @Column(name = "create_time")
    private Date createTime;

}