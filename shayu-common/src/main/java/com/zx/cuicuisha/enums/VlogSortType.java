package com.zx.cuicuisha.enums;

/**
 * 视频排序类型枚举
 */
public enum VlogSortType {
   CREATED_TIME(0, "created_time", "按创建时间排序"),
   LIKE_COUNTS(1, "like_counts", "按点赞数排序"),
   COMMENTS_COUNTS(2, "comments_counts", "按评论数排序");

   public final Integer type;
   public final String field;
   public final String desc;

   VlogSortType(Integer type, String field, String desc) {
      this.type = type;
      this.field = field;
      this.desc = desc;
   }

   /**
    * 根据type获取排序类型
    * @param type 排序类型值
    * @return 对应的排序枚举，如果未找到则返回null
    */
   public static VlogSortType getByType(Integer type) {
      if (type == null) {
         return null;
      }
      for (VlogSortType sortType : VlogSortType.values()) {
         if (sortType.type.equals(type)) {
            return sortType;
         }
      }
      return null;
   }

   /**
    * 根据type获取排序类型（带默认值）
    * @param type 排序类型值
    * @return 对应的排序枚举，如果未找到则返回默认值CREATED_TIME
    */
   public static VlogSortType getByTypeWithDefault(Integer type) {
      VlogSortType sortType = getByType(type);
      return sortType != null ? sortType : CREATED_TIME;
   }
}

