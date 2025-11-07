# CollaborativeTasks2

> 基于Spring Cloud的微服务协作任务管理系统

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.4-blue.svg)](https://spring.io/projects/spring-cloud)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## ✨ 特性

- 🚀 **微服务架构** - 基于Spring Cloud Gateway的API网关
- 🔧 **配置中心** - 集成Nacos实现配置管理和服务发现
- 🔐 **统一认证** - Token based authentication
- 📦 **容器化部署** - 完整的Docker + Docker Compose方案
- 🔄 **CI/CD** - 基于GitLab的自动化构建和部署
- 📊 **多环境支持** - dev / test / prod环境隔离
- 🎯 **一键发版** - Makefile自动化工具

---

## 🏗️ 架构

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ↓
┌─────────────────────────────────┐
│     Gateway (8001)               │
│  - 路由转发                       │
│  - Token认证                     │
│  - CORS处理                      │
└──────┬──────────────────────────┘
       │
       ├──────→ API Service (8002)
       │
       └──────→ Admin Service (8003)
       
┌─────────────────────────────────┐
│    Infrastructure               │
│  - Nacos (配置+注册)             │
│  - MySQL (数据库)                │
│  - Redis (缓存)                  │
│  - Nexus (镜像仓库)              │
└─────────────────────────────────┘
```

---

## 🚀 快速开始

### 前置要求

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- 8GB+ RAM

### 启动步骤

#### 1. 启动基础服务

```bash
# 启动MySQL, Redis, Nacos
make quick-start
```

#### 2. 配置Nacos

访问 http://localhost:8848/nacos (账号：nacos/nacos)

导入配置文件（或手动创建配置）

#### 3. 启动应用

**方式1：本地运行**

```bash
# API服务
make run-api

# Gateway服务
make run-gateway
```

**方式2：Docker运行**

```bash
# 一键发版
make release-test
```

#### 4. 访问系统

- API网关: http://localhost:8001
- Nacos: http://localhost:8848/nacos
- Nexus: http://localhost:8081

---

## 📚 文档

完整文档请查看 **[docs目录](./docs/README.md)**

- [GitLab CI/CD完整指南](./docs/GitLab-CICD完整指南.md) - **⭐ 推荐**
- [架构设计文档](./docs/架构设计文档.md)
- [Docker部署指南](./docs/Docker部署指南.md)
- [Nacos配置指南](./docs/Nacos配置中心使用指南.md)

---

## 🔧 常用命令

### 开发

```bash
make build          # Maven编译
make test           # 运行测试
make clean          # 清理
```

### Docker

```bash
make docker-build   # 构建镜像
make docker-up      # 启动服务
make docker-down    # 停止服务
make docker-logs    # 查看日志
```

### CI/CD

```bash
make release-test   # 发版到测试环境
make release-prod   # 发版到生产环境
```

更多命令请运行: `make help`

---

## 🛠️ 技术栈

### 后端框架

- Spring Boot 3.5.4
- Spring Cloud Gateway
- Spring Cloud Alibaba
- Nacos 2.4.5
- MyBatis Plus
- Druid

### 基础设施

- Docker & Docker Compose
- GitLab CI/CD
- Nexus Repository
- MySQL 8.0
- Redis 7

---

## 📊 项目结构

```
CollaborativeTasks2/
├── collabtask-api/              # API服务模块
├── collabtask-gateway/          # Gateway网关模块
├── collabtask-admin/            # 管理后台模块
├── collabtask-common/           # 公共模块
├── collabtask-dynamic-datasource/  # 动态数据源模块
├── docker/                      # Docker相关
│   ├── base/                   # 基础镜像
│   └── gitlab-runner/          # CI/CD Runner镜像
├── docs/                        # 项目文档
├── scripts/                     # 工具脚本
├── docker-compose.yml           # 基础服务编排
├── docker-compose-nexus.yml     # 应用服务编排
├── Makefile                     # 自动化工具
└── .gitlab-ci.yml              # CI/CD配置
```

---

## 🔄 CI/CD流程

```
开发提交代码 → dev分支
    ↓
创建 Merge Request: dev → test
    ↓
点击 Merge
    ↓
✅ 自动触发 GitLab CI/CD
    ↓
Runner 执行:
  1. Maven 编译
  2. 构建 Docker 镜像
  3. 推送到 Nexus
    ↓
手动部署到测试环境
    ↓
✅ 完成
```

详见: [GitLab CI/CD完整指南](./docs/GitLab-CICD完整指南.md)

---

## 🐛 故障排查

遇到问题？

1. 查看 [GitLab CI/CD完整指南](./docs/GitLab-CICD完整指南.md) 的故障排查章节
2. 查看日志: `docker logs <container-name>`
3. 检查Nacos配置
4. 查看 [常见问题](./docs/GitLab-CICD完整指南.md#故障排查)

---

## 📝 开发规范

### 分支策略

- `dev` - 开发分支
- `test` - 测试分支
- `main` - 生产分支

### 提交规范

```
feat: 新功能
fix: Bug修复
docs: 文档更新
refactor: 代码重构
test: 测试相关
chore: 构建/工具相关
```

---

## 📄 License

[MIT License](LICENSE)

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request!

---

## 📮 联系

有问题或建议？欢迎提Issue！

---

**项目版本: 1.0.0**

**最后更新: 2025-11-07**
