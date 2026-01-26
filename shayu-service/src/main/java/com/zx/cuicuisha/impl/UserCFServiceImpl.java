package com.zx.cuicuisha.impl;

import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.enums.YesOrNo;
import com.zx.cuicuisha.exceptions.GraceException;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.mapper.RecommendationMapperCustom;
import com.zx.cuicuisha.mapper.VlogMapper;
import com.zx.cuicuisha.pojo.Vlog;
import com.zx.cuicuisha.service.UserCFService;
import com.zx.cuicuisha.utils.JsonUtils;
import com.zx.cuicuisha.utils.PagedGridResult;
import com.zx.cuicuisha.utils.SimilarityCalculator;
import com.zx.cuicuisha.vo.UserBehaviorVO;
import com.zx.cuicuisha.vo.UserSimilarityVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于用户的协同过滤推荐服务实现
 */
@Slf4j
@Service
public class UserCFServiceImpl extends BaseInfoProperties implements UserCFService {

    @Autowired
    private RecommendationMapperCustom recommendationMapperCustom;

    @Autowired
    private VlogMapper vlogMapper;

    // 默认参数
    private static final int DEFAULT_K = 50;  // 默认相似用户数量
    private static final int DEFAULT_TOP_N = 10;  // 默认推荐数量
    private static final double MIN_SIMILARITY = 0.0;  // 最小相似度阈值

    @Override
    public List<String> recommendVlogs(String userId, Integer topN) {
        if (StringUtils.isBlank(userId)) {
            GraceException.display(ResponseStatusEnum.UN_LOGIN);
        }

        if (topN == null || topN <= 0) {
            topN = DEFAULT_TOP_N;
        }

        // 1. 尝试从缓存获取推荐结果
        String cacheKey = BaseInfoProperties.REDIS_USER_RECOMMENDATIONS + ":" + userId + ":" + topN;
        String cachedResult = redis.get(cacheKey);
        if (StringUtils.isNotBlank(cachedResult)) {
            try {
                List<String> cachedList = JsonUtils.jsonToList(cachedResult, String.class);
                if (cachedList != null && !cachedList.isEmpty()) {
                    log.info("从缓存获取用户{}的推荐结果", userId);
                    return cachedList;
                }
            } catch (Exception e) {
                log.warn("解析推荐结果缓存失败: {}", e.getMessage());
            }
        }

        // 2. 构建用户-视频评分矩阵
        Map<String, Map<String, Double>> userItemMatrix = buildUserItemMatrix();

        // 3. 检查目标用户是否有行为数据
        Map<String, Double> targetUserRatings = userItemMatrix.get(userId);
        if (targetUserRatings == null || targetUserRatings.isEmpty()) {
            log.warn("用户{}没有行为数据，无法进行推荐", userId);
            return Collections.emptyList();
        }

        // 4. 获取相似用户
        List<UserSimilarityVO> similarUsers = getSimilarUsers(userId, DEFAULT_K);

        if (similarUsers == null || similarUsers.isEmpty()) {
            log.warn("用户{}没有找到相似用户", userId);
            return Collections.emptyList();
        }

        // 5. 预测评分并生成推荐
        Map<String, Double> predictions = predictRatings(userId, similarUsers, userItemMatrix, targetUserRatings);

        // 6. 过滤掉用户已看过的视频，并按评分排序
        List<String> recommendations = predictions.entrySet().stream()
                .filter(entry -> !targetUserRatings.containsKey(entry.getKey()))
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 7. 验证推荐的视频是否存在且未删除
        recommendations = filterValidVlogs(recommendations);

        // 8. 缓存推荐结果（缓存1小时）
        if (!recommendations.isEmpty()) {
            try {
                redis.set(cacheKey, JsonUtils.objectToJson(recommendations), 3600);
            } catch (Exception e) {
                log.warn("缓存推荐结果失败: {}", e.getMessage());
            }
        }

        return recommendations;
    }

    @Override
    public PagedGridResult recommendVlogsPaged(String userId, Integer page, Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        // 获取更多推荐结果用于分页
        int totalNeeded = page * pageSize;
        List<String> allRecommendations = recommendVlogs(userId, totalNeeded);

        // 分页处理
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, allRecommendations.size());
        List<String> pagedList = start < allRecommendations.size() 
                ? allRecommendations.subList(start, end) 
                : Collections.emptyList();

        PagedGridResult gridResult = new PagedGridResult();
        gridResult.setRows(pagedList);
        gridResult.setPage(page);
        gridResult.setRecords((long) allRecommendations.size());
        gridResult.setTotal((int) Math.ceil((double) allRecommendations.size() / pageSize));

        return gridResult;
    }

    @Override
    public List<UserSimilarityVO> getSimilarUsers(String userId, Integer k) {
        if (StringUtils.isBlank(userId)) {
            GraceException.display(ResponseStatusEnum.UN_LOGIN);
        }

        if (k == null || k <= 0) {
            k = DEFAULT_K;
        }

        // 1. 尝试从缓存获取相似用户
        String cacheKey = BaseInfoProperties.REDIS_USER_SIMILARITY + ":" + userId + ":" + k;
        String cachedResult = redis.get(cacheKey);
        if (StringUtils.isNotBlank(cachedResult)) {
            try {
                List<UserSimilarityVO> cachedList = JsonUtils.jsonToList(cachedResult, UserSimilarityVO.class);
                if (cachedList != null && !cachedList.isEmpty()) {
                    log.info("从缓存获取用户{}的相似用户列表", userId);
                    return cachedList;
                }
            } catch (Exception e) {
                log.warn("解析相似用户缓存失败: {}", e.getMessage());
            }
        }

        // 2. 构建用户-视频评分矩阵
        Map<String, Map<String, Double>> userItemMatrix = buildUserItemMatrix();

        // 3. 获取目标用户的评分向量
        Map<String, Double> targetUserRatings = userItemMatrix.get(userId);
        if (targetUserRatings == null || targetUserRatings.isEmpty()) {
            log.warn("用户{}没有行为数据", userId);
            return Collections.emptyList();
        }

        // 4. 计算与所有其他用户的相似度
        List<UserSimilarityVO> similarities = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> entry : userItemMatrix.entrySet()) {
            String otherUserId = entry.getKey();
            if (otherUserId.equals(userId)) {
                continue;
            }

            Map<String, Double> otherUserRatings = entry.getValue();
            if (otherUserRatings == null || otherUserRatings.isEmpty()) {
                continue;
            }

            double similarity = SimilarityCalculator.cosineSimilarity(targetUserRatings, otherUserRatings);
            if (similarity > MIN_SIMILARITY) {
                similarities.add(new UserSimilarityVO(otherUserId, similarity));
            }
        }

        // 5. 按相似度降序排序，取前K个
        similarities.sort((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()));
        List<UserSimilarityVO> topK = similarities.stream()
                .limit(k)
                .collect(Collectors.toList());

        // 6. 缓存相似用户列表（缓存2小时）
        if (!topK.isEmpty()) {
            try {
                redis.set(cacheKey, JsonUtils.objectToJson(topK), 7200);
            } catch (Exception e) {
                log.warn("缓存相似用户列表失败: {}", e.getMessage());
            }
        }

        return topK;
    }

    @Override
    public void calculateAndCacheUserSimilarities() {
        log.info("开始计算用户相似度矩阵...");
        Map<String, Map<String, Double>> userItemMatrix = buildUserItemMatrix();
        int totalUsers = userItemMatrix.size();
        int processed = 0;

        for (String userId : userItemMatrix.keySet()) {
            try {
                // 计算并缓存每个用户的相似用户列表
                getSimilarUsers(userId, DEFAULT_K);
                processed++;
                if (processed % 100 == 0) {
                    log.info("已处理 {}/{} 用户", processed, totalUsers);
                }
            } catch (Exception e) {
                log.error("计算用户{}的相似度失败: {}", userId, e.getMessage());
            }
        }

        log.info("用户相似度矩阵计算完成，共处理 {} 个用户", processed);
    }

    @Override
    public void clearSimilarityCache(String userId) {
        if (StringUtils.isNotBlank(userId)) {
            // 清除指定用户的缓存
            String pattern = BaseInfoProperties.REDIS_USER_SIMILARITY + ":" + userId + ":*";
            Set<String> keys = redis.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                keys.forEach(redis::delete);
            }

            pattern = BaseInfoProperties.REDIS_USER_RECOMMENDATIONS + ":" + userId + ":*";
            keys = redis.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                keys.forEach(redis::delete);
            }
            log.info("已清除用户{}的相似度和推荐缓存", userId);
        } else {
            // 清除所有缓存
            Set<String> keys = redis.keys(BaseInfoProperties.REDIS_USER_SIMILARITY + ":*");
            if (keys != null) {
                keys.forEach(redis::delete);
            }
            keys = redis.keys(BaseInfoProperties.REDIS_USER_RECOMMENDATIONS + ":*");
            if (keys != null) {
                keys.forEach(redis::delete);
            }
            log.info("已清除所有用户相似度和推荐缓存");
        }
    }

    /**
     * 构建用户-视频评分矩阵
     * 
     * @return 用户-视频评分矩阵 (userId -> (vlogId -> score))
     */
    private Map<String, Map<String, Double>> buildUserItemMatrix() {
        // 尝试从缓存获取
        String cacheKey = BaseInfoProperties.REDIS_USER_BEHAVIOR_MATRIX;
        String cachedResult = redis.get(cacheKey);
        if (StringUtils.isNotBlank(cachedResult)) {
            try {
                // 这里简化处理，实际可以缓存整个矩阵
                // 由于矩阵可能很大，建议只缓存行为数据，实时构建矩阵
            } catch (Exception e) {
                log.warn("解析行为矩阵缓存失败: {}", e.getMessage());
            }
        }

        // 从数据库查询所有用户行为
        List<UserBehaviorVO> behaviors = recommendationMapperCustom.getAllUserBehaviors();
        
        // 构建矩阵
        Map<String, Map<String, Double>> matrix = new HashMap<>();
        for (UserBehaviorVO behavior : behaviors) {
            String userId = behavior.getUserId();
            String vlogId = behavior.getVlogId();
            Double score = behavior.getScore();

            matrix.computeIfAbsent(userId, k -> new HashMap<>()).put(vlogId, score);
        }

        log.debug("构建用户-视频评分矩阵完成，用户数: {}, 行为记录数: {}", matrix.size(), behaviors.size());
        return matrix;
    }

    /**
     * 预测用户对未看视频的评分
     * 公式：pred(u, i) = Σ(sim(u, v) * r(v, i)) / Σ|sim(u, v)|
     * 
     * @param userId 目标用户ID
     * @param similarUsers 相似用户列表
     * @param userItemMatrix 用户-视频评分矩阵
     * @param targetUserRatings 目标用户的评分向量
     * @return 预测评分 (vlogId -> predictedScore)
     */
    private Map<String, Double> predictRatings(String userId,
                                               List<UserSimilarityVO> similarUsers,
                                               Map<String, Map<String, Double>> userItemMatrix,
                                               Map<String, Double> targetUserRatings) {
        Map<String, Double> predictions = new HashMap<>();
        Map<String, Double> similaritySum = new HashMap<>();  // 用于归一化

        for (UserSimilarityVO simUser : similarUsers) {
            String similarUserId = simUser.getUserId();
            double similarity = simUser.getSimilarity();

            Map<String, Double> similarUserRatings = userItemMatrix.get(similarUserId);
            if (similarUserRatings == null || similarUserRatings.isEmpty()) {
                continue;
            }

            // 遍历相似用户喜欢的视频
            for (Map.Entry<String, Double> entry : similarUserRatings.entrySet()) {
                String vlogId = entry.getKey();
                double rating = entry.getValue();

                // 只预测目标用户未看过的视频
                if (!targetUserRatings.containsKey(vlogId)) {
                    predictions.put(vlogId, 
                            predictions.getOrDefault(vlogId, 0.0) + similarity * rating);
                    similaritySum.put(vlogId, 
                            similaritySum.getOrDefault(vlogId, 0.0) + Math.abs(similarity));
                }
            }
        }

        // 归一化预测评分
        for (Map.Entry<String, Double> entry : predictions.entrySet()) {
            String vlogId = entry.getKey();
            double sum = similaritySum.getOrDefault(vlogId, 1.0);
            if (sum > 0) {
                predictions.put(vlogId, entry.getValue() / sum);
            }
        }

        return predictions;
    }

    /**
     * 过滤有效的视频（存在且未删除）
     * 
     * @param vlogIds 视频ID列表
     * @return 有效的视频ID列表
     */
    private List<String> filterValidVlogs(List<String> vlogIds) {
        if (vlogIds == null || vlogIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> validVlogIds = new ArrayList<>();
        for (String vlogId : vlogIds) {
            try {
                Vlog vlog = vlogMapper.selectByPrimaryKey(vlogId);
                if (vlog != null 
                        && (vlog.getIsDeleted() == null || vlog.getIsDeleted() == 0)
                        && (vlog.getIsPrivate() == null || vlog.getIsPrivate() == YesOrNo.NO.type)) {
                    validVlogIds.add(vlogId);
                }
            } catch (Exception e) {
                log.warn("查询视频{}信息失败: {}", vlogId, e.getMessage());
            }
        }

        return validVlogIds;
    }
}

