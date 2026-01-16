# DailyBaro

DailyBaro - 智能情绪健康管理系统（Spring Cloud 微服务架构）

## 项目概述

DailyBaro 是一个基于 SpringCloud 微服务架构的智能情绪管理平台，集成 AI 情绪分析技术与心理干预解决方案，提供情绪记录、分析和改善服务。

## 技术架构

- **后端**: SpringCloud + Nacos 服务治理 + MySQL + Redis 缓存
- **AI模块**: BERT 文本情绪分析 + Librosa 语音情绪识别
- **向量数据库**: Milvus (用于 RAG 知识库检索)

## 服务架构

### 微服务列表

- **gateway-service** (8000): API 网关服务
- **user-service** (8001): 用户服务（用户管理、认证、授权）
- **file-service** (8003): 文件服务（文件上传、存储）
- **content-service** (8011): 内容服务（合并了日记、标签、情绪、匿名帖子、神秘盒子、胶囊、日签等功能）
- **app-service** (8012): 小程序网关服务
- **ai-knowledge-service** (8013): AI 和知识库服务（AI 对话、RAG 检索、知识库管理）
- **nlp-service** (5001): NLP 情绪分析服务（Python）

## 快速开始

#### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Python 3.8+ (用于 NLP 服务)
- Docker & Docker Compose (用于 Milvus)

#### 配置说明

1. 配置数据库：
   ```bash
   # 创建数据库
   mysql -uroot -p < sql/dailybaro_init.sql
   ```

2. 配置各服务的 `application.yml`：
   - 数据库连接信息（使用环境变量 `${MYSQL_PASSWORD:your_mysql_password}`）
   - Redis 连接信息
   - API Key 等（通过环境变量配置，如 `${DEEPSEEK_API_KEY:your_deepseek_api_key}`）

3. 编译项目：
   ```bash
   cd DailyBaro-cloud
   mvn clean package -DskipTests
   ```

4. 启动服务：
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
├── common/                   # 公共模块
├── gateway-service/          # 网关服务 (8000)
├── user-service/             # 用户服务 (8001)
├── content-service/          # 内容服务 (8011) - 合并了日记、情绪、标签、匿名帖子、神秘盒子、胶囊、日签等功能
├── file-service/             # 文件服务 (8003)
├── app-service/              # 小程序网关服务 (8012)
├── ai-knowledge-service/    # AI 和知识库服务 (8013)
└── nlp-service/              # NLP 情绪分析服务 (5001) - Python 服务
```

## 主要功能

- ✅ 情绪记录与分析
- ✅ 日记管理（支持多媒体）
- ✅ AI 智能对话（RAG 增强）
- ✅ 知识库管理
- ✅ 情绪胶囊
- ✅ 神秘盒子
- ✅ 匿名帖子
- ✅ 日签推荐
- ✅ 情绪统计分析

## 性能优化

- ✅ 批量查询优化（避免 N+1 查询问题）
- ✅ Redis 缓存
- ✅ 数据库索引优化
- ✅ 代码复用性提升

## 安全说明

⚠️ **重要**: 所有敏感信息（API Key、密码等）已从代码中移除，请通过环境变量配置。

配置文件使用环境变量占位符，例如：
- `${MYSQL_PASSWORD:your_mysql_password}`
- `${DEEPSEEK_API_KEY:your_deepseek_api_key}`
- `${WECHAT_APP_ID:your_wechat_app_id}`

## License

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！
