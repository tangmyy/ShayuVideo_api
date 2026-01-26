package com.zx.cuicuisha.vo;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 推荐结果VO
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationVO {
    /**
     * 视频ID
     */
    private String vlogId;
    
    /**
     * 预测评分
     */
    private Double predictedScore;
}



