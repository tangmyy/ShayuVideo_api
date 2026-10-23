# UserCF推荐系统使用说明

## 一、功能概述

已实现基于用户的协同过滤（UserCF）推荐系统，支持：
- **评分策略**：点赞1分，评论2分（如果同一视频既有点赞又有评论，取最高分2分）
- **相似度计算**：使用余弦相似度
- **推荐算法**：基于K个最相似用户的喜好进行推荐
- **性能优化**：Redis缓存相似度和推荐结果

## 二、API接口

### 1. 获取推荐视频列表

**接口地址**：`GET /recommendation/vlogs`

**请求参数**：
- `userId` (必填)：用户ID
- `topN` (可选)：推荐数量，默认10

**示例**：
```
GET /recommendation/vlogs?userId=xxx&topN=20
```

**返回结果**：
```json
{
  "status": 200,
  "msg": "成功",
  "success": true,
  "data": ["vlogId1", "vlogId2", "vlogId3", ...]
}
```

### 2. 获取推荐视频列表（分页）

**接口地址**：`GET /recommendation/vlogs/paged`

**请求参数**：
- `userId` (必填)：用户ID
- `page` (可选)：页码，默认1
- `pageSize` (可选)：每页数量，默认10

**示例**：
```
GET /recommendation/vlogs/paged?userId=xxx&page=1&pageSize=20
```

### 3. 获取相似用户列表

**接口地址**：`GET /recommendation/similarUsers`

**请求参数**：
- `userId` (必填)：用户ID
- `k` (可选)：相似用户数量，默认50

**示例**：
```
GET /recommendation/similarUsers?userId=xxx&k=30
```

**返回结果**：
```json
{
  "status": 200,
  "msg": "成功",
  "success": true,
  "data": [
    {
      "userId": "user123",
      "similarity": 0.85
    },
    {
      "userId": "user456",
      "similarity": 0.72
    }
  ]
}
```

### 4. 计算并缓存用户相似度矩阵

**接口地址**：`POST /recommendation/calculateSimilarities`

**说明**：此接口会计算所有用户的相似度并缓存，建议在定时任务中调用，避免在线计算耗时。

**示例**：
```
POST /recommendation/calculateSimilarities
```

### 5. 清除推荐缓存

**接口地址**：`DELETE /recommendation/cache`

**请求参数**：
- `userId` (可选)：用户ID，如果为空则清除所有缓存

**示例**：
```
DELETE /recommendation/cache?userId=xxx
```

## 三、核心实现

### 1. 数据模型

- **UserBehaviorVO**：用户行为数据（用户ID、视频ID、评分）
- **UserSimilarityVO**：用户相似度（用户ID、相似度分数）
- **RecommendationVO**：推荐结果（视频ID、预测评分）

### 2. 核心类

- **UserCFService**：推荐服务接口
- **UserCFServiceImpl**：推荐服务实现
- **SimilarityCalculator**：相似度计算工具类
- **RecommendationMapperCustom**：数据访问层

### 3. 算法流程

```
1. 构建用户-视频评分矩阵
   ↓
2. 计算目标用户与所有其他用户的相似度（余弦相似度）
   ↓
3. 找到K个最相似用户（默认50个）
   ↓
4. 根据相似用户的喜好，预测目标用户对未看视频的评分
   公式：pred(u, i) = Σ(sim(u, v) * r(v, i)) / Σ|sim(u, v)|
   ↓
5. 按预测评分排序，返回Top-N推荐
```

### 4. 缓存策略

- **相似用户列表**：缓存2小时
- **推荐结果**：缓存1小时
- **缓存Key格式**：
  - 相似用户：`redis_user_similarity:{userId}:{k}`
  - 推荐结果：`redis_user_recommendations:{userId}:{topN}`

## 四、使用建议

### 1. 定时任务

建议创建一个定时任务，定期计算并缓存用户相似度：

```java
@Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
public void calculateUserSimilarities() {
    userCFService.calculateAndCacheUserSimilarities();
}
```

### 2. 冷启动处理

对于新用户或没有行为数据的用户，推荐系统会返回空列表。建议：
- 新用户：推荐热门视频
- 行为数据少的用户：混合推荐（UserCF + 热门推荐）

### 3. 性能优化

- **数据量大时**：考虑使用Spark等大数据框架离线计算
- **实时性要求高**：增加缓存时间，减少计算频率
- **内存优化**：对于超大规模数据，可以考虑采样计算

### 4. 集成到现有系统

可以在现有的视频列表接口中集成推荐：

```java
// 在VlogController中添加推荐接口
@GetMapping("recommend")
public ResponseDataStructureEnum recommend(@RequestParam String userId) {
    List<String> vlogIds = userCFService.recommendVlogs(userId, 20);
    // 根据vlogIds查询视频详情并返回
    return ResponseDataStructureEnum.ok(vlogList);
}
```

## 五、注意事项

1. **数据稀疏性**：用户-视频矩阵通常很稀疏，可能影响推荐质量
2. **计算复杂度**：UserCF的复杂度是O(U²)，用户量大时需要优化
3. **实时更新**：用户新行为产生后，建议清除相关缓存
4. **视频有效性**：推荐结果会自动过滤已删除和私密视频

## 六、扩展方向

1. **混合推荐**：结合ItemCF、热门推荐、内容推荐
2. **时间衰减**：考虑时间因素，最近的行为权重更高
3. **多维度评分**：除了点赞和评论，可以加入观看时长、分享等
4. **机器学习优化**：使用深度学习模型优化相似度计算



