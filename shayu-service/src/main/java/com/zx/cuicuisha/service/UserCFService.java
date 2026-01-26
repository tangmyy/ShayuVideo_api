package com.zx.cuicuisha.service;

import com.zx.cuicuisha.utils.PagedGridResult;
import com.zx.cuicuisha.vo.RecommendationVO;
import com.zx.cuicuisha.vo.UserSimilarityVO;

import java.util.List;

/**
 * 基于用户的协同过滤推荐服务接口
 */
public interface UserCFService {

    /**
     * 为用户推荐视频（基于UserCF算法）
     * 
     * @param userId 目标用户ID
     * @param topN 推荐数量，默认10
     * @return 推荐视频ID列表
     */
    List<String> recommendVlogs(String userId, Integer topN);

    /**
     * 为用户推荐视频（分页版本）
     * 
     * @param userId 目标用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 分页推荐结果
     */
    PagedGridResult recommendVlogsPaged(String userId, Integer page, Integer pageSize);

    /**
     * 获取与目标用户最相似的K个用户
     * 
     * @param userId 目标用户ID
     * @param k 相似用户数量，默认50
     * @return 相似用户列表（按相似度降序）
     */
    List<UserSimilarityVO> getSimilarUsers(String userId, Integer k);

    /**
     * 计算并缓存用户相似度矩阵（用于性能优化）
     * 建议在定时任务中调用，定期更新相似度
     */
    void calculateAndCacheUserSimilarities();

    /**
     * 清除用户相似度缓存
     * 
     * @param userId 用户ID，如果为null则清除所有缓存
     */
    void clearSimilarityCache(String userId);
}



