package com.zx.cuicuisha.mapper;

import com.zx.cuicuisha.vo.IndexVlogVO;
import org.springframework.stereotype.Repository;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Repository
public interface VlogMapperCustom {

   // 查询视频列表
   public List<IndexVlogVO> getIndexVlogList(@Param("paramMap")Map<String, Object> map);
   // 根据 ID 查询视频
   public List<IndexVlogVO> getVlogDetailById(@Param("paramMap")Map<String, Object> map);

   public List<IndexVlogVO> getMyLikedVlogList(@Param("paramMap")Map<String, Object> map);

   public List<IndexVlogVO> getMyFollowVlogList(@Param("paramMap")Map<String, Object> map);

   public List<IndexVlogVO> getMyFriendVlogList(@Param("paramMap")Map<String, Object> map);

   // 查询用户的视频列表（公开/私密）
   public List<IndexVlogVO> queryMyVlogList(@Param("paramMap")Map<String, Object> map);

   // Admin - 查询视频列表（管理端使用，不过滤is_deleted）
   public List<IndexVlogVO> getIndexVlogListForAdmin(@Param("paramMap")Map<String, Object> map);
}