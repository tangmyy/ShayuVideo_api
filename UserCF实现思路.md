# UserCF (基于用户的协同过滤) 实现思路

## 一、算法原理

UserCF的核心思想：**找到与目标用户兴趣相似的用户，将这些相似用户喜欢的物品推荐给目标用户**。

### 基本步骤：

1. **构建用户-物品矩阵**
   - 根据用户行为（点赞、观看、评论等）构建评分矩阵
   - 矩阵行：用户
   - 矩阵列：视频
   - 矩阵值：用户对视频的评分/行为强度

2. **计算用户相似度**
   - 常用方法：余弦相似度、皮尔逊相关系数、Jaccard相似度
   - 找出与目标用户最相似的K个用户（K近邻）

3. **预测评分并推荐**
   - 根据相似用户的评分，预测目标用户对未看视频的评分
   - 按预测评分排序，取Top-N推荐

## 二、在你的项目中应用

### 数据来源：
- **用户表**：`Users`
- **视频表**：`Vlog`
- **用户点赞表**：`MyLikedVlog` - 这是最核心的交互数据

### 可以扩展的数据源：
- 观看时长（如果有记录）
- 评论行为
- 分享行为
- 关注行为

## 三、实现方案设计

### 方案A：基于点赞的简单UserCF

**评分策略：**
- 用户点赞过视频 = 1分
- 用户未点赞 = 0分（或忽略）

**优点：**
- 实现简单
- 计算快速
- 适合冷启动

**缺点：**
- 评分粒度粗（只有0/1）
- 未考虑点赞强度差异

### 方案B：基于多种行为的加权UserCF

**评分策略：**
- 点赞：1分
- 评论：2分
- 分享：3分
- 完整观看：1.5分

**优点：**
- 更准确反映用户偏好
- 推荐质量更高

**缺点：**
- 需要更多数据源
- 计算复杂度更高

## 四、技术实现架构

```
推荐服务模块结构：
└── recommendation/
    ├── service/
    │   ├── UserCFService.java          # UserCF核心服务
    │   └── RecommendationService.java  # 推荐服务接口
    ├── model/
    │   ├── UserSimilarity.java         # 用户相似度模型
    │   └── RecommendationResult.java   # 推荐结果模型
    ├── mapper/
    │   └── RecommendationMapper.java   # 数据访问（可选）
    └── util/
        ├── SimilarityCalculator.java   # 相似度计算工具
        └── MatrixBuilder.java          # 矩阵构建工具
```

## 五、核心算法实现步骤

### Step 1: 构建用户-视频评分矩阵

```java
// 伪代码
Map<String, Map<String, Double>> userItemMatrix = new HashMap<>();
// userItemMatrix[userId][vlogId] = score

// 从数据库获取所有用户点赞记录
List<MyLikedVlog> likes = getLikedVlogs();
for (MyLikedVlog like : likes) {
    String userId = like.getUserId();
    String vlogId = like.getVlogId();
    userItemMatrix.get(userId).put(vlogId, 1.0);
}
```

### Step 2: 计算用户相似度（余弦相似度）

```java
// 公式：cos(u1, u2) = (u1 · u2) / (||u1|| * ||u2||)

double cosineSimilarity(String userId1, String userId2) {
    Map<String, Double> user1Ratings = userItemMatrix.get(userId1);
    Map<String, Double> user2Ratings = userItemMatrix.get(userId2);
    
    // 计算共同物品
    Set<String> commonItems = intersection(user1Ratings.keySet(), user2Ratings.keySet());
    
    double dotProduct = 0.0;
    double norm1 = 0.0;
    double norm2 = 0.0;
    
    for (String item : commonItems) {
        double r1 = user1Ratings.get(item);
        double r2 = user2Ratings.get(item);
        dotProduct += r1 * r2;
    }
    
    for (Double rating : user1Ratings.values()) {
        norm1 += rating * rating;
    }
    for (Double rating : user2Ratings.values()) {
        norm2 += rating * rating;
    }
    
    return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
}
```

### Step 3: 找到K个最相似用户

```java
List<UserSimilarity> findKSimilarUsers(String targetUserId, int k) {
    List<UserSimilarity> similarities = new ArrayList<>();
    
    for (String otherUserId : allUserIds) {
        if (!otherUserId.equals(targetUserId)) {
            double similarity = cosineSimilarity(targetUserId, otherUserId);
            if (similarity > 0) {  // 只保留有共同兴趣的用户
                similarities.add(new UserSimilarity(otherUserId, similarity));
            }
        }
    }
    
    // 按相似度降序排序，取前K个
    return similarities.stream()
        .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
        .limit(k)
        .collect(Collectors.toList());
}
```

### Step 4: 预测评分并生成推荐

```java
// 预测用户对视频的评分
// 公式：pred(u, i) = Σ(sim(u, v) * r(v, i)) / Σ|sim(u, v)|
// 其中 v 是相似用户，i 是视频

List<String> recommend(String userId, int topN) {
    // 1. 找到K个相似用户
    List<UserSimilarity> similarUsers = findKSimilarUsers(userId, 50);
    
    // 2. 获取目标用户已点赞的视频
    Set<String> likedVlogs = getUserLikedVlogs(userId);
    
    // 3. 预测目标用户对未看视频的评分
    Map<String, Double> predictions = new HashMap<>();
    
    for (UserSimilarity simUser : similarUsers) {
        String similarUserId = simUser.getUserId();
        double similarity = simUser.getSimilarity();
        
        // 获取相似用户喜欢的视频
        Set<String> similarUserLikes = getUserLikedVlogs(similarUserId);
        
        for (String vlogId : similarUserLikes) {
            // 只预测目标用户未看过的视频
            if (!likedVlogs.contains(vlogId)) {
                predictions.put(vlogId, 
                    predictions.getOrDefault(vlogId, 0.0) + similarity);
            }
        }
    }
    
    // 4. 按预测评分排序，返回Top-N
    return predictions.entrySet().stream()
        .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
        .limit(topN)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
}
```

## 六、性能优化策略

### 1. 缓存相似度矩阵
- 用户相似度计算耗时，可以定期计算并缓存
- 使用Redis缓存用户相似度列表
- 设置合适的过期时间（如每天更新一次）

### 2. 增量更新
- 新用户点赞时，只更新相关用户的相似度
- 避免全量重新计算

### 3. 采样优化
- 对于用户量大的情况，可以采样计算
- 只计算活跃用户的相似度

### 4. 并行计算
- 使用多线程并行计算用户相似度
- 使用Spark等大数据框架（如果数据量非常大）

## 七、实现建议

### 阶段1：基础实现
1. 实现基本的UserCF算法
2. 使用MySQL查询数据
3. 内存计算（适合小规模数据）

### 阶段2：优化改进
1. 引入Redis缓存相似度结果
2. 实现定时任务批量计算
3. 添加日志和监控

### 阶段3：扩展增强
1. 混合推荐（UserCF + ItemCF + 热门推荐）
2. 考虑时间衰减（最近行为权重更高）
3. 引入机器学习优化相似度计算

## 八、注意事项

1. **冷启动问题**
   - 新用户：没有行为数据，无法计算相似度
   - 解决方案：推荐热门视频、基于内容推荐

2. **稀疏性问题**
   - 用户-视频矩阵通常很稀疏
   - 解决方案：使用改进的相似度计算方法（如调整余弦相似度）

3. **计算复杂度**
   - UserCF的复杂度是O(U²)，U为用户数
   - 当用户量大时需要优化

4. **实时性**
   - 可以离线计算相似度，在线查询推荐结果
   - 平衡实时性和准确性

## 九、测试建议

1. **离线测试**
   - 使用历史数据
   - 计算准确率、召回率、F1值

2. **A/B测试**
   - 对比UserCF推荐和热门推荐的效果
   - 评估用户点击率、观看时长等指标

3. **性能测试**
   - 测试推荐接口响应时间
   - 测试系统负载能力

