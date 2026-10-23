package com.zx.cuicuisha.vo;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatsVO {
   private List<String> dates;      // 日期数组，格式为 "MM-DD"
   private List<Integer> users;    // 每日新增用户数数组
   private List<Integer> vlogs;    // 每日新增视频数数组
   private List<Integer> comments; // 每日新增评论数数组（可选）
}

