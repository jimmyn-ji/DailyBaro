# DailyBaro

DailyBaro - 智能情绪健康管理系统

## 项目概述

DailyBaro 是一个基于 SpringCloud 微服务架构的智能情绪管理平台，集成 AI 情绪分析技术与心理干预解决方案，提供情绪记录、分析和改善服务。

## 技术架构

- **后端**: SpringCloud + Nacos 服务治理 + MySQL + Redis 缓存
- **前端**: Vue3 + ECharts 可视化 + WebSocket 实时通信
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

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Python 3.8+ (用于 NLP 服务)
- Docker & Docker Compose (用于 Milvus)

### 配置说明

1. 复制环境变量模板：
   ```bash
   cp env.example .env
   ```

2. 编辑 `.env` 文件，填入实际配置值：
   - 服务器信息
   - MySQL 密码
   - DeepSeek API Key
   - 微信小程序 AppID 和 AppSecret
   - 邮箱配置

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
   # ... 其他服务
   ```

### 一键部署到服务器

```bash
cd /Users/minchi/Desktop/DailyBaro
./deploy-to-server.sh
```

部署脚本会自动：
- 安装 JDK、MySQL、Redis、Docker、Nginx 等依赖
- 配置防火墙
- 部署 Milvus 向量数据库
- 编译并上传所有服务
- 配置 Nginx 反向代理
- 安装 SSL 证书
- 启动所有服务

## 项目结构

```
DailyBaro/
├── DailyBaro-cloud/          # 后端微服务
│   ├── common/               # 公共模块
│   ├── gateway-service/      # 网关服务
│   ├── user-service/         # 用户服务
│   ├── content-service/      # 内容服务（日记、情绪、标签等）
│   ├── file-service/         # 文件服务
│   ├── app-service/          # 小程序网关
│   ├── ai-knowledge-service/ # AI 和知识库服务
│   └── nlp-service/          # NLP 情绪分析服务
├── DailyBaro-vue/            # Vue3 前端
├── DailyBaro-app/            # 微信小程序
├── sql/                      # 数据库脚本
├── deploy-to-server.sh       # 一键部署脚本
└── env.example              # 环境变量模板
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

⚠️ **重要**: 所有敏感信息（API Key、密码等）已从代码中移除，请通过环境变量或 `.env` 文件配置。

`.env` 文件已加入 `.gitignore`，不会被提交到 Git 仓库。

## License

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！

## 联系方式

如有问题，请提交 Issue。
