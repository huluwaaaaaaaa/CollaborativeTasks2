# CollabTask - 协作任务管理系统

[![Build](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com)
[![JDK](https://img.shields.io/badge/JDK-17+-orange)](https://www.oracle.com/java/technologies/downloads/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-green)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

> 🚀 基于服务化架构的企业级协作任务管理系统

---

## 📋 项目简介

CollabTask 是一个功能完整的协作任务管理系统，支持团队协作、任务管理、标签分类和细粒度权限控制。

### ✨ 核心特性

- 🔐 **JWT认证** - 双Token机制（Access Token + Refresh Token）
- 👥 **团队协作** - 团队管理、成员管理、TODO共享
- 📝 **任务管理** - 完整CRUD、排序筛选、标签分类
- 🔒 **ACL权限** - 细粒度访问控制、权限审计
- ⚡ **并发控制** - 分布式锁 + 幂等性保证
- 🌐 **服务化架构** - Gateway + API多节点部署

---

## 🏗️ 技术架构

### 架构图

```
        ┌─────────────────┐
        │   Gateway       │  ← 路由、认证、负载均衡
        │   :8001         │
        └────────┬────────┘
                 │
        ┌────────┴────────┐
        │                 │
   ┌────▼────┐      ┌────▼────┐
   │ API-1   │      │ API-2   │  ← 多节点部署
   │ :8002   │      │ :8002   │
   └────┬────┘      └────┬────┘
        │                 │
        └────────┬────────┘
                 │
        ┌────────┴────────┐
        │                 │
   ┌────▼────┐      ┌────▼────┐
   │  MySQL  │      │  Redis  │  ← 共享数据层
   └─────────┘      └─────────┘
```

### 技术栈

- **框架**：Spring Boot 3.5.4 + Spring Cloud Gateway
- **数据库**：MySQL 8.0 + MyBatis Plus
- **缓存**：Redis 6.0 + Redisson
- **服务治理**：Nacos 2.0
- **认证**：JWT Token
- **测试**：JUnit 5 + Mockito

---

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- Docker & Docker Compose
- MySQL 8.0+
- Redis 6.0+

### 一键启动（推荐）

```bash
# 克隆项目
git clone <your-repo-url>
cd CollaborativeTasks2

# 使用Docker Compose启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 访问服务
# Gateway: http://localhost:8001
# API文档: http://localhost:8002/collabtask-api/doc.html
```

### 本地开发启动

```bash
# 1. 启动基础设施（MySQL + Redis + Nacos）
docker-compose up -d mysql redis nacos

# 2. 初始化数据库
mysql -h localhost -P 3306 -u root -p < database/schema_acl.sql

# 3. 配置Nacos
# 访问 http://localhost:8848/nacos (nacos/nacos)
# 导入配置文件（见 docs/Nacos配置说明.md）

# 4. 启动服务
make start-gateway  # 启动Gateway
make start-api      # 启动API服务
```

### 使用Makefile（推荐）

```bash
# 查看所有可用命令
make help

# 常用命令
make build          # 编译打包
make test           # 运行测试
make docker-build   # 构建Docker镜像
make docker-up      # 启动Docker服务
make status         # 查看服务状态
```

---

## 🧪 测试

### 快速测试

**方法1：使用测试页面（推荐）** ⭐
```bash
# 直接打开测试页面
open api-test.html

# 功能：
# ✅ 覆盖95%接口
# ✅ 所有请求通过Gateway
# ✅ 自动化测试场景
# ✅ 实时日志输出
```

**方法2：使用Postman**
```bash
# 导入Collection
# CollabTask-API-v1.1-v1.2.postman_collection.json
# 详见：docs/测试文档/04-Postman使用说明.md
```

### 运行单元测试

```bash
# 运行所有测试
mvn test

# 运行指定模块测试
mvn test -pl collabtask-api

# 生成测试报告
mvn test jacoco:report
```

---

## 📦 CI/CD流程

### GitLab CI/CD 自动化

本项目已配置完整的CI/CD流水线（`.gitlab-ci.yml`）：

```yaml
工作流程：
  dev分支  → 合并到 → test分支  → 🤖 自动部署到测试环境
  test分支 → 合并到 → main分支  → 👆 手动部署到生产环境
```

### 自动化流程

**测试环境自动部署**：
```bash
# 触发条件：代码合并到test分支
git checkout dev
git add .
git commit -m "feat: 新功能开发"
git push origin dev

# 创建MR：dev → test
# 合并后自动触发CI/CD

# Pipeline步骤：
# 1. Maven编译打包
# 2. 构建Docker镜像
# 3. 推送到Nexus私有仓库
# 4. 自动部署到测试环境
```

**生产环境手动部署**：
```bash
# 触发条件：代码合并到main分支
# 需要手动点击"deploy-prod"按钮

# Pipeline步骤：
# 1. Maven编译打包
# 2. 构建Docker镜像
# 3. 推送到Nexus私有仓库
# 4. 部署到生产环境（需手动触发）
```

### 本地一键发版

使用Makefile快速发版到不同环境：

```bash
# 发版到测试环境
make release-test

# 发版到生产环境
make release-prod

# 发版到开发环境
make release-dev

# 自定义版本号
make release ENV=test VERSION=v1.2.0
```

**一键发版流程**：
```
1. Maven编译   → mvn clean package
2. 构建镜像    → docker build
3. 推送Nexus   → docker push
4. 部署服务    → docker compose up
```

### CI/CD配置

**Nexus配置**：
```yaml
# 位置：.gitlab-ci.yml
NEXUS_REGISTRY: "host.docker.internal:5000"
NEXUS_USERNAME: "admin"
NEXUS_PASSWORD: "123456"
```

**环境变量**：
```bash
ENV=test|prod|dev        # 部署环境
VERSION=v1.x.x           # 版本号（默认latest）
NEXUS_REGISTRY=...       # Nexus地址
```

**详细文档**：
- `docs/CI-CD实战指南.md` - CI/CD完整配置
- `docs/一键发版指南.md` - 发版流程说明
- `docs/Docker部署指南.md` - Docker部署详解

---

## 📖 API文档

### 访问方式

| 方式 | 地址 | 说明 |
|------|------|------|
| **测试页面** | `api-test.html` | ⭐ 推荐，覆盖95%接口 |
| **Knife4j** | http://localhost:8002/collabtask-api/doc.html | Swagger增强版 |
| **Postman** | 导入`*.postman_collection.json` | 自动化脚本 |

### 核心接口

**认证**：
- `POST /api/login` - 登录
- `POST /api/logout` - 登出
- `POST /api/register` - 注册

**TODO管理**：
- `GET /api/todos` - 列表（支持筛选+排序）
- `POST /api/todos` - 创建
- `PUT /api/todos/{id}` - 更新
- `DELETE /api/todos/{id}` - 删除

**TODO共享（v1.1）**：
- `POST /api/todos/{id}/share` - 共享TODO
- `DELETE /api/todos/{id}/share/{userId}` - 取消共享

**团队管理**：
- `POST /api/teams` - 创建团队
- `POST /api/teams/{id}/members/{uid}` - 添加成员

**标签管理**：
- `POST /api/tags` - 创建标签
- `POST /api/tags/todos/{tid}/tags/{id}` - 添加标签到TODO

**完整接口清单**：`docs/开发文档/06-API接口统计.md`（40个接口）

---

## 🗂️ 项目结构

```
CollaborativeTasks2/
├── .cursor/rules/              # AI开发规范
├── .gitlab-ci.yml              # GitLab CI/CD配置
├── Makefile                    # 一键部署脚本
├── docker-compose.yml          # Docker编排
├── collabtask-gateway/         # Gateway服务（8001）
├── collabtask-api/             # API服务（8002）
│   ├── src/main/java/io/user/
│   │   ├── controller/         # 控制器
│   │   ├── service/            # 业务层
│   │   ├── dao/                # 数据访问层
│   │   ├── entity/             # 实体类
│   │   ├── dto/                # DTO/VO
│   │   ├── enums/              # 枚举类
│   │   ├── aspect/             # AOP切面
│   │   └── config/             # 配置类
│   └── src/main/resources/
│       └── mapper/             # MyBatis XML
├── database/                   # 数据库脚本
├── docs/                       # 📚 文档目录
│   ├── 设计文档/               # 系统设计
│   ├── 测试文档/               # 测试报告
│   └── 开发文档/               # 开发规范
├── api-test.html               # 🎨 测试页面
└── *.postman_collection.json   # Postman集合
```

---

## 📊 数据库设计

### 核心表（8张）

| 表名 | 说明 | 特性 |
|------|------|------|
| tb_user | 用户表 | 手机号登录 |
| tb_tokens | Token表 | Access + Refresh |
| tb_todos | TODO表 | 支持排序筛选 |
| tb_teams | 团队表 | 协作基础 |
| tb_team_members | 团队成员 | 多对多关系 |
| tb_tags | 标签表 | 彩色标签 |
| tb_todo_tags | TODO-标签关联 | 多对多关系 |
| tb_scheduled_tasks | 定时任务 | Token清理 |

### ACL权限表（4张）

| 表名 | 说明 | 记录数 |
|------|------|--------|
| tb_acl_permission_definitions | 权限定义 | 14条 |
| tb_acl_access_control | 访问控制列表 | 动态 |
| tb_acl_permission_audit | 权限审计日志 | 动态 |
| tb_acl_role_definitions | 角色定义 | 2条 |

**初始化脚本**：`database/schema_acl.sql`

---

## 🔧 配置管理

### Nacos配置中心

**配置文件**：
- `collabtask-api.yaml` - API服务配置
- `collabtask-gateway.yaml` - Gateway配置

**访问地址**：http://localhost:8848/nacos  
**默认账号**：nacos / nacos

**主要配置**：
```yaml
# MySQL连接
spring.datasource.url: jdbc:mysql://localhost:3306/collabtask

# Redis配置
spring.data.redis.host: localhost

# JWT配置
jwt.secret: your-secret-key
jwt.expire: 7200  # 2小时
```

**详细说明**：`docs/Nacos配置中心使用指南.md`

---

## 🚢 部署方式

### 方式1：Docker Compose（推荐）

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 方式2：Makefile一键部署

```bash
# 查看帮助
make help

# 快速发版到测试环境
make release-test

# 发版到生产环境
make release-prod

# 查看服务状态
make status
```

### 方式3：手动启动（开发调试）

```bash
# 启动Gateway
cd collabtask-gateway
mvn spring-boot:run

# 启动API（另开终端）
cd collabtask-api
mvn spring-boot:run
```

---

## 🔄 CI/CD流程

### GitLab CI/CD配置

**配置文件**：`.gitlab-ci.yml`

**流水线阶段**：
```yaml
Stages:
  - deploy       # 部署阶段

Jobs:
  - deploy-test  # 测试环境（自动）
  - deploy-prod  # 生产环境（手动）
```

### 自动化部署流程

#### 测试环境（自动触发）

```bash
# 1. 开发分支提交代码
git checkout dev
git add .
git commit -m "feat: 新功能"
git push origin dev

# 2. 创建Merge Request：dev → test

# 3. 合并后自动触发Pipeline
# ✅ Maven编译
# ✅ Docker镜像构建
# ✅ 推送到Nexus
# ✅ 自动部署到测试环境

# 4. 部署完成
# 访问：http://localhost:8001
```

#### 生产环境（手动触发）

```bash
# 1. 创建Merge Request：test → main

# 2. 合并后，手动点击"deploy-prod"按钮

# 3. Pipeline执行部署
# ✅ 编译、构建、推送
# ✅ 部署到生产环境
```

### 本地CI/CD模拟

使用Makefile模拟完整CI/CD流程：

```bash
# 模拟完整流程（编译→构建→推送→部署）
make release ENV=test VERSION=v1.2.0

# 只构建和推送（用于CI）
make release-ci ENV=test VERSION=v1.2.0

# 单独部署
make deploy-local
```

### Nexus私有仓库

**配置**：
```bash
# Nexus地址
NEXUS_REGISTRY=host.docker.internal:5000

# 登录Nexus
docker login host.docker.internal:5000 -u admin -p 123456

# 推送镜像
docker push host.docker.internal:5000/collabtask-api:v1.0.0
```

**详细配置**：`docs/Nexus配置指南.md`

---

## 📝 开发规范

### 代码规范

**AI自动遵守的规范**：`.cursor/rules/develop-style.mdc`

**核心要求**：
- ✅ 分层架构（Controller → Service → DAO → Entity）
- ✅ DTO/VO分离（入参Request、出参Response）
- ✅ ACL权限检查
- ✅ 并发控制（@Idempotent、@DistributedLock）
- ✅ 异常统一处理
- ✅ 日志规范

### Git提交规范

```bash
# Commit Message格式
<type>(<scope>): <subject>

# 示例
feat(todo): 添加TODO共享功能
fix(auth): 修复token刷新bug
docs(readme): 更新部署文档
```

**Type类型**：
- `feat` - 新功能
- `fix` - Bug修复
- `docs` - 文档更新
- `refactor` - 代码重构
- `test` - 测试相关
- `chore` - 构建/配置

---

## 📚 文档导航

### 设计文档
- [测试页面核心功能完善报告](docs/设计文档/02-测试页面核心功能完善报告.md) - ⭐ 核心文档
- [v1.1和v1.2版本交付文档](docs/设计文档/04-v1.1和v1.2版本交付文档.md) - ACL权限设计

### 测试文档
- [v1.1-v1.2最终验证报告](docs/测试文档/01-v1.1-v1.2最终验证报告.md) - ⭐ 测试结果
- [Postman使用说明](docs/测试文档/04-Postman使用说明.md) - API测试

### 开发文档
- [API接口统计](docs/开发文档/06-API接口统计.md) - 40个接口清单
- [枚举重构报告](docs/开发文档/02-枚举重构报告.md) - 枚举使用规范

**完整索引**：`docs/README.md`

---

## 🎯 版本历史

### v2.0 - 核心功能完善版（2025-11-10）
- ✅ TODO标签关联（核心）
- ✅ 团队成员管理（核心）
- ✅ 高级筛选+排序
- ✅ 接口覆盖率95%

### v1.3 - TODO列表优化（2025-11-10）
- ✅ 显示共享的TODO
- ✅ 联表查询ACL权限

### v1.2 - 并发控制（2025-11-10）
- ✅ 幂等性控制（@Idempotent）
- ✅ 分布式锁（@DistributedLock）

### v1.1 - ACL权限系统（2025-11-10）
- ✅ TODO共享功能
- ✅ 细粒度权限检查
- ✅ 权限审计日志

### v1.0 - 基础版本（2025-11-09）
- ✅ 用户认证
- ✅ TODO管理
- ✅ 团队管理
- ✅ 标签管理

---

## 🛠️ 常用命令

### Makefile命令

```bash
make build          # 编译打包
make test           # 运行测试
make docker-build   # 构建Docker镜像
make docker-up      # 启动Docker服务
make docker-down    # 停止Docker服务
make status         # 查看服务状态
make clean          # 清理编译产物
make release-test   # 发版到测试环境
make release-prod   # 发版到生产环境
```

### Docker命令

```bash
# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f collabtask-api
docker-compose logs -f collabtask-gateway

# 重启服务
docker-compose restart collabtask-api

# 进入容器
docker exec -it collabtask-api bash
```

### 服务访问

| 服务 | 地址 | 说明 |
|------|------|------|
| Gateway | http://localhost:8001 | 统一入口 |
| API服务 | http://localhost:8002 | API服务 |
| Knife4j | http://localhost:8002/collabtask-api/doc.html | API文档 |
| Nacos | http://localhost:8848/nacos | 配置中心 |
| MySQL | localhost:3306 | 数据库 |
| Redis | localhost:6379 | 缓存 |

---

## 🔍 故障排查

### 常见问题

**服务启动失败**：
```bash
# 检查端口占用
lsof -i:8001
lsof -i:8002

# 查看日志
docker-compose logs collabtask-api
```

**数据库连接失败**：
```bash
# 检查MySQL是否启动
docker-compose ps mysql

# 检查数据库是否初始化
mysql -h localhost -P 3306 -u root -p -e "USE collabtask; SHOW TABLES;"
```

**Redis连接失败**：
```bash
# 检查Redis是否启动
docker-compose ps redis

# 测试连接
redis-cli -h localhost -p 6379 ping
```

**详细排查**：`docs/开发文档/05-Gateway问题修复说明.md`

---

## 📄 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

---

## 🙏 致谢

感谢所有贡献者对本项目的支持！

---

**© 2025 CollabTask Team. All Rights Reserved.**
