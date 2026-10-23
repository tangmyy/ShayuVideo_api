package com.zx.cuicuisha.service;

import com.zx.cuicuisha.vo.ActiveUserVO;

import java.util.List;

public interface HomeService {

   /**
    * 查询活跃用户排行榜
    * @param limit 返回数量限制，默认10
    * @param sortBy 排序方式：'videos'（按视频数）或 'fans'（按粉丝数），默认'videos'
    * @return 活跃用户列表
    */
   List<ActiveUserVO> getActiveUsers(Integer limit, String sortBy);

   /**
    * 查询指定年份的每月视频发布数量
    * @param year 年份，如果为null则使用当前年份
    * @return 每月视频发布数量列表（按月份1-12顺序），每个元素表示该月的视频发布总数
    */
   List<Integer> getMonthlyVideos(Integer year);
}

