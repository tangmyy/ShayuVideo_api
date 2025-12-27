package com.zx.cuicuisha.pojo;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "my_liked_vlog")
public class MyLikedVlog {
    @Id
    private String id;

    /**
     * 用户id
     */
    @Column(name = "user_id")
    private String userId;

    /**
     * 喜欢的短视频id
     */
    @Column(name = "vlog_id")
    private String vlogId;
}