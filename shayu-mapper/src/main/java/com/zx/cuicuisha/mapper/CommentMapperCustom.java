package com.zx.cuicuisha.mapper;

import com.zx.cuicuisha.vo.CommentAdminVO;
import com.zx.cuicuisha.vo.CommentVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CommentMapperCustom {

   public List<CommentVO> getCommentList(@Param("paramMap") Map<String, Object> map);

   /**
    * 查询所有评论（管理端使用）
    * @param map 查询参数，可包含vlogId（视频ID）和userId（评论者ID）
    * @return 评论列表
    */
   public List<CommentAdminVO> getAllComments(@Param("paramMap") Map<String, Object> map);

}
