package com.zx.cuicuisha.vo;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 用户相似度VO
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserSimilarityVO {
    /**
     * 相似用户ID
     */
    private String userId;
    
    /**
     * 相似度分数（0-1之间）
     */
    private Double similarity;
}



