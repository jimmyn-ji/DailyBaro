# DailyBaro

DailyBaro - 智能情绪健康管理系统（Spring Cloud 微服务架构）

## 项目概述

DailyBaro 是一个基于 Spring Cloud 微服务架构的智能情绪管理平台，集成 AI 情绪分析技术与心理干预解决方案，提供情绪记录、分析和改善服务。项目采用分布式架构设计，支持高并发访问和水平扩展。

## 技术架构

### 后端技术栈
- **框架**: Spring Cloud 2021.0.4 + Spring Boot 2.7.0
- **服务治理**: Nacos（服务注册与发现、配置管理）
- **API 网关**: Spring Cloud Gateway（路由、负载均衡、限流）
- **数据库**: MySQL 8.0 + MyBatis Plus
- **缓存**: Redis（会话管理、数据缓存）
- **消息队列**: Redis Pub/Sub（异步消息处理）
- **向量数据库**: Milvus（RAG 知识库检索）

### AI 技术栈
- **文本分析**: BERT 模型 + SnowNLP（中文情绪分析）
- **语音分析**: Librosa（音频特征提取）
- **图像分析**: OpenCV + 深度学习模型（情绪识别）
- **RAG 增强**: 向量检索 + DeepSeek API（智能对话）

## 服务架构

### 微服务列表

| 服务名称 | 端口 | 功能描述 |
|---------|------|---------|
| gateway-service | 8000 | API 网关服务，统一入口，路由转发 |
| user-service | 8001 | 用户服务，用户管理、认证授权、第三方登录 |
| file-service | 8003 | 文件服务，文件上传、存储、CDN 分发 |
| content-service | 8011 | 内容服务，日记、情绪、标签、匿名帖子、神秘盒子、胶囊、日签等 |
| app-service | 8012 | 小程序网关服务，适配微信小程序接口 |
| ai-knowledge-service | 8013 | AI 和知识库服务，AI 对话、RAG 检索、知识库管理 |
| nlp-service | 5001 | NLP 情绪分析服务（Python），文本/语音/图像情绪识别 |

### 架构特点

- **微服务拆分**: 按业务领域拆分，服务职责清晰
- **服务合并优化**: 将 diary-service 合并到 content-service，减少服务数量，提高资源利用率
- **性能优化**: 批量查询避免 N+1 问题，减少数据库查询次数
- **高可用设计**: 服务独立部署，故障隔离，支持水平扩展

## 核心功能

### 1. 情绪管理
- 多模态情绪记录（文本、语音、图像）
- 情绪趋势分析（时间序列分析）
- 情绪分布统计（饼图、柱状图）
- AI 情绪分析报告生成

### 2. 日记系统
- 富文本日记编辑
- 多媒体支持（图片、视频、音频）
- 标签分类管理
- 草稿/发布状态管理
- 能量值奖励机制

### 3. AI 智能对话
- RAG 增强对话（基于知识库检索）
- 上下文理解与记忆
- 个性化推荐
- 情绪干预建议

### 4. 知识库管理
- 知识条目 CRUD
- 向量化存储（Milvus）
- 语义搜索
- 知识推荐算法

### 5. 社交功能
- 匿名帖子发布
- 评论与点赞
- 情绪胶囊分享
- 神秘盒子互动

## 技术亮点

### 1. 性能优化
- **批量查询优化**: 实现批量查询标签和媒体，避免 N+1 查询问题
- **缓存策略**: Redis 缓存热点数据，减少数据库压力
- **代码复用**: 提取公共方法，减少重复代码
- **循环优化**: 使用 Stream API 和批量操作，减少循环次数

### 2. 架构设计
- **服务合并**: 将 diary-service 合并到 content-service，简化架构
- **统一网关**: Spring Cloud Gateway 统一入口，统一鉴权
- **服务解耦**: Feign 客户端实现服务间通信
- **配置管理**: Nacos 集中配置管理

### 3. AI 集成
- **RAG 增强**: 结合向量数据库实现检索增强生成
- **多模态分析**: 支持文本、语音、图像三种模态的情绪分析
- **个性化推荐**: 基于用户历史数据的智能推荐

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Python 3.8+ (用于 NLP 服务)
- Docker & Docker Compose (用于 Milvus)

### 编译与运行

1. **编译项目**:
   ```bash
   cd DailyBaro-cloud
   mvn clean package -DskipTests
   ```

2. **配置数据库和 Redis**:
   - 创建 MySQL 数据库
   - 配置各服务的 `application.yml` 中的数据库连接信息
   - 启动 Redis 服务

3. **启动服务**:
   ```bash
   # 使用 Docker Compose 启动所有服务（包括 Milvus）
   cd DailyBaro-cloud
   docker-compose up -d
   
   # 或手动启动各个服务
   java -jar gateway-service/target/gateway-service-1.0-SNAPSHOT.jar
   java -jar user-service/target/user-service-1.0-SNAPSHOT.jar
   java -jar content-service/target/content-service-1.0-SNAPSHOT.jar
   java -jar file-service/target/file-service-1.0-SNAPSHOT.jar
   java -jar app-service/target/app-service-1.0-SNAPSHOT.jar
   java -jar ai-knowledge-service/target/ai-knowledge-service-1.0-SNAPSHOT.jar
   ```

## 项目结构

```
DailyBaro-cloud/              # 后端微服务（Spring Cloud）
├── common/                   # 公共模块（DTO、VO、工具类）
├── gateway-service/          # 网关服务 (8000)
├── user-service/             # 用户服务 (8001)
├── content-service/          # 内容服务 (8011) - 合并了日记、情绪、标签、匿名帖子、神秘盒子、胶囊、日签等功能
├── file-service/             # 文件服务 (8003)
├── app-service/              # 小程序网关服务 (8012)
├── ai-knowledge-service/    # AI 和知识库服务 (8013)
└── nlp-service/             # NLP 情绪分析服务 (5001) - Python 服务
```

## 开发亮点

### 代码质量
- 遵循 SOLID 原则
- 统一异常处理
- 日志规范记录
- 代码注释完善

### 性能优化
- 批量查询优化（避免 N+1 查询问题）
- Redis 缓存策略
- 数据库索引优化
- 代码复用性提升

### 可维护性
- 模块化设计
- 接口抽象清晰
- 配置外部化
- 文档完善

## License

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！
