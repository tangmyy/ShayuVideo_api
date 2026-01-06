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
public class MonthlyStatsVO {
   private List<String> months;    // 月份数组，格式为 "YYYY-MM"
   private List<Integer> users;    // 每月新增用户数数组
   private List<Integer> vlogs;    // 每月新增视频数数组
   private List<Integer> comments; // 每月新增评论数数组（可选）
}

