package com.zx.cuicuisha.mapper;

import com.zx.cuicuisha.vo.ActiveUserVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface HomeMapperCustom {

   /**
    * 查询活跃用户排行榜
    * @param map 查询参数，包含sortBy（'videos'或'fans'）和limit
    * @return 活跃用户列表
    */
   List<ActiveUserVO> queryActiveUsers(@Param("paramMap") Map<String, Object> map);

   /**
    * 查询指定年份的每月视频发布数量
    * @param year 年份
    * @return 每月视频发布数量列表（按月份1-12顺序）
    */
   List<Integer> queryMonthlyVideos(@Param("year") Integer year);
}

