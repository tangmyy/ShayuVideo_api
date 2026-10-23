package com.zx.cuicuisha.vo;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 用户行为VO（用于构建用户-视频评分矩阵）
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorVO {
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 视频ID
     */
    private String vlogId;
    
    /**
     * 行为评分（点赞1分，评论2分，如果既有点赞又有评论，取最高分2分）
     */
    private Double score;
}



