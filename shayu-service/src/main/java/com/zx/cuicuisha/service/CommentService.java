package com.zx.cuicuisha.service;

import com.zx.cuicuisha.bo.CommentBO;
import com.zx.cuicuisha.pojo.Comment;
import com.zx.cuicuisha.utils.PagedGridResult;
import com.zx.cuicuisha.vo.CommentVO;

public interface CommentService {
   // 发表评论
   public CommentVO createComment(CommentBO commentBO);

   // 查询评论的列表
   public PagedGridResult queryVlogComments(String vlogId, String userId, Integer page, Integer pageSize);

   // 删除评论
   public void deleteComment(String commentUserId, String commentId, String vlogId);

   // Admin - 根据主键查询
   public Comment getComment(String id);

   // Admin - 查询所有评论（管理端使用，如果vlogId和userId都为空则查询所有，否则按条件查询）
   public PagedGridResult queryAllComments(String vlogId, String userId, Integer page, Integer pageSize);

}
