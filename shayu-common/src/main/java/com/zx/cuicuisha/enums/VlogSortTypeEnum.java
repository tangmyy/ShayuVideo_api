package com.zx.cuicuisha.enums;

/**
 * 视频排序类型枚举
 */
public enum VlogSortTypeEnum {
   CREATED_TIME(0, "created_time", "按创建时间排序"),
   LIKE_COUNTS(1, "like_counts", "按点赞数排序"),
   COMMENTS_COUNTS(2, "comments_counts", "按评论数排序");

   public final Integer type;
   public final String field;
   public final String desc;

   VlogSortTypeEnum(Integer type, String field, String desc) {
      this.type = type;
      this.field = field;
      this.desc = desc;
   }

   /**
    * 根据type获取排序类型
    * @param type 排序类型值
    * @return 对应的排序枚举，如果未找到则返回null
    */
   public static VlogSortTypeEnum getByType(Integer type) {
      if (type == null) {
         return null;
      }
      for (VlogSortTypeEnum sortType : VlogSortTypeEnum.values()) {
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
   public static VlogSortTypeEnum getByTypeWithDefault(Integer type) {
      VlogSortTypeEnum sortType = getByType(type);
      return sortType != null ? sortType : CREATED_TIME;
   }
}

