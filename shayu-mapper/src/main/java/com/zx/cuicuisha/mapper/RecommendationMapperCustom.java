package com.zx.cuicuisha.mapper;

import com.zx.cuicuisha.vo.UserBehaviorVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 推荐系统自定义Mapper
 */
@Repository
public interface RecommendationMapperCustom {
    
    /**
     * 查询所有用户的视频行为数据（点赞+评论）
     * 评分规则：点赞1分，评论2分，如果同一个视频既有点赞又有评论，取最高分2分
     * 
     * @return 用户行为列表
     */
    List<UserBehaviorVO> getAllUserBehaviors();
    
    /**
     * 查询指定用户的视频行为数据
     * 
     * @param userId 用户ID
     * @return 用户行为列表
     */
    List<UserBehaviorVO> getUserBehaviors(@Param("userId") String userId);
}



