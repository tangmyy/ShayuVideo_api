package com.zx.cuicuisha.impl;

import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.mapper.HomeMapperCustom;
import com.zx.cuicuisha.service.HomeService;
import com.zx.cuicuisha.vo.ActiveUserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class HomeServiceImpl extends BaseInfoProperties implements HomeService {

   @Autowired
   private HomeMapperCustom homeMapperCustom;

   @Override
   public List<ActiveUserVO> getActiveUsers(Integer limit, String sortBy) {
      // 参数默认值处理
      if (limit == null || limit <= 0) {
         limit = 10;
      }
      if (StringUtils.isBlank(sortBy)) {
         sortBy = "videos";
      }
      // 验证sortBy参数
      if (!"videos".equals(sortBy) && !"fans".equals(sortBy)) {
         sortBy = "videos";
      }

      Map<String, Object> paramMap = new HashMap<>();
      paramMap.put("limit", limit);
      paramMap.put("sortBy", sortBy);

      List<ActiveUserVO> activeUsers = homeMapperCustom.queryActiveUsers(paramMap);
      return activeUsers != null ? activeUsers : new ArrayList<>();
   }

   @Override
   public List<Integer> getMonthlyVideos(Integer year) {
      // 硬编码年份为2025
      year = 2025;
      List<Integer> stats = homeMapperCustom.queryMonthlyVideos(year);
      // 确保返回12个月的数据，如果查询结果不足12个，补充0
      List<Integer> result = new ArrayList<>();
      for (int i = 0; i < 12; i++) {
         if (i < stats.size()) {
            result.add(stats.get(i));
         } else {
            result.add(0);
         }
      }
      return result;
   }
}

