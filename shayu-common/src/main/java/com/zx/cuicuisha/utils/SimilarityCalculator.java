package com.zx.cuicuisha.utils;

import java.util.Map;
import java.util.Set;

/**
 * 相似度计算工具类
 */
public class SimilarityCalculator {

    /**
     * 计算两个用户的余弦相似度
     * 公式：cos(u1, u2) = (u1 · u2) / (||u1|| × ||u2||)
     * 
     * @param user1Ratings 用户1的评分向量 (vlogId -> score)
     * @param user2Ratings 用户2的评分向量 (vlogId -> score)
     * @return 相似度值，范围[0, 1]，0表示完全不相似，1表示完全相似
     */
    public static double cosineSimilarity(Map<String, Double> user1Ratings, 
                                         Map<String, Double> user2Ratings) {
        if (user1Ratings == null || user1Ratings.isEmpty() || 
            user2Ratings == null || user2Ratings.isEmpty()) {
            return 0.0;
        }

        // 找到两个用户共同评价的视频
        Set<String> commonItems = user1Ratings.keySet();
        commonItems.retainAll(user2Ratings.keySet());

        // 如果没有共同评价的视频，相似度为0
        if (commonItems.isEmpty()) {
            return 0.0;
        }

        // 计算点积
        double dotProduct = 0.0;
        for (String item : commonItems) {
            dotProduct += user1Ratings.get(item) * user2Ratings.get(item);
        }

        // 计算两个向量的模长
        double norm1 = 0.0;
        for (Double rating : user1Ratings.values()) {
            norm1 += rating * rating;
        }
        norm1 = Math.sqrt(norm1);

        double norm2 = 0.0;
        for (Double rating : user2Ratings.values()) {
            norm2 += rating * rating;
        }
        norm2 = Math.sqrt(norm2);

        // 避免除零
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (norm1 * norm2);
    }

    /**
     * 计算两个用户的Jaccard相似度（可选，用于对比）
     * 公式：J(A, B) = |A ∩ B| / |A ∪ B|
     * 
     * @param user1Ratings 用户1的评分向量
     * @param user2Ratings 用户2的评分向量
     * @return Jaccard相似度值
     */
    public static double jaccardSimilarity(Map<String, Double> user1Ratings,
                                          Map<String, Double> user2Ratings) {
        if (user1Ratings == null || user1Ratings.isEmpty() || 
            user2Ratings == null || user2Ratings.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = user1Ratings.keySet();
        intersection.retainAll(user2Ratings.keySet());

        Set<String> union = user1Ratings.keySet();
        union.addAll(user2Ratings.keySet());

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }
}



