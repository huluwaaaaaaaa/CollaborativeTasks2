# CollabTask - 协作任务管理系统

[![JDK](https://img.shields.io/badge/JDK-17+-orange)](https://www.oracle.com/java/technologies/downloads/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-green)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

> 🚀 支持TODO共享、团队协作、细粒度权限控制的任务管理系统

---

## 📋 项目简介

CollabTask 是一个功能完整的协作任务管理系统，核心特性包括TODO管理、团队协作、TODO共享和ACL权限控制。

### ✨ 核心功能

- 📝 **TODO管理** - 创建、更新、删除、完成、排序、筛选
- 🤝 **TODO共享** - 支持VIEW/EDIT权限，实现协作
- 👥 **团队管理** - 创建团队、添加成员、团队TODO
- 🏷️ **标签系统** - 彩色标签、TODO分类
- 🔒 **ACL权限** - 细粒度权限控制（v1.1）
- ⚡ **并发控制** - 幂等性 + 分布式锁（v1.2）

### 🎯 技术特性

- 🌐 Gateway + API服务化架构
- 🔐 JWT双Token认证
- 📊 支持高级筛选和排序
- ✅ 95%接口测试覆盖率

---

## 🏗️ 技术架构

```
Gateway(8001) → API(8002) → MySQL + Redis
     ↓              ↓
  路由/认证    业务逻辑/ACL
```

**技术栈**：
- Spring Boot 3.5.4 + Spring Cloud Gateway
- MySQL 8.0 + MyBatis Plus + Redis
- Nacos（配置中心） + Redisson（分布式锁）

---

## 🚀 快速开始

### 方式1：Docker Compose（推荐）

```bash
# 一键启动所有服务
docker-compose up -d

# 访问
# Gateway: http://localhost:8001
# 测试页面: api-test.html
```

### 方式2：本地启动

```bash
# 1. 启动基础服务
docker-compose up -d mysql redis nacos

# 2. 初始化数据库
mysql -h localhost -u root -p < database/schema_acl.sql

# 3. 启动应用
make start-gateway  # 或: cd collabtask-gateway && mvn spring-boot:run
make start-api      # 或: cd collabtask-api && mvn spring-boot:run
```

### 方式3：Makefile

```bash
make build          # 编译打包
make docker-up      # 启动Docker服务
make status         # 查看状态
```

---

## 🧪 功能测试

### 测试页面（推荐）⭐

```bash
# 打开测试页面
open api-test.html

# 特性：
# ✅ 覆盖95%接口（38/40）
# ✅ 所有请求通过Gateway
# ✅ 支持TODO共享测试
# ✅ 支持团队/标签管理
# ✅ 自动化测试场景
```

**测试流程**：
1. 点击"登录"
2. 点击"测试所有功能（完整）"
3. 查看日志输出

### 其他测试方式

- **Postman**：导入 `CollabTask-API-v1.1-v1.2.postman_collection.json`
- **Knife4j**：http://localhost:8002/collabtask-api/doc.html

---

## 📖 核心API

### 认证
```bash
POST /api/login      # 登录
POST /api/logout     # 登出
POST /api/register   # 注册
```

### TODO管理
```bash
GET    /api/todos              # 列表（支持筛选+排序）
POST   /api/todos              # 创建（v1.2幂等性）
PUT    /api/todos/{id}         # 更新（v1.1 ACL检查）
DELETE /api/todos/{id}         # 删除
PATCH  /api/todos/{id}/complete # 完成
```

### TODO共享（v1.1核心功能）
```bash
POST   /api/todos/{id}/share           # 共享TODO（VIEW/EDIT权限）
DELETE /api/todos/{id}/share/{userId}  # 取消共享
```

### 团队管理
```bash
POST   /api/teams                      # 创建团队
POST   /api/teams/{id}/members/{uid}   # 添加成员
DELETE /api/teams/{id}/members/{uid}   # 移除成员
```

### 标签管理
```bash
POST   /api/tags                       # 创建标签
POST   /api/tags/todos/{tid}/tags/{id} # 添加标签到TODO
DELETE /api/tags/todos/{tid}/tags/{id} # 移除标签
GET    /api/tags/todos/{tid}/tags      # 查看TODO的标签
```

**完整接口**：40个，详见 `docs/开发文档/06-API接口统计.md`

---

## 🗂️ 项目结构

```
CollaborativeTasks2/
├── collabtask-gateway/         # Gateway服务（8001端口）
├── collabtask-api/             # API服务（8002端口）
│   ├── controller/             # 控制器（TODO、Team、Tag）
│   ├── service/                # 业务层（ACL权限检查）
│   ├── dao/                    # 数据访问层
│   ├── entity/                 # 实体类（Entity）
│   ├── dto/                    # 数据传输对象（DTO/VO）
│   ├── enums/                  # 枚举类（PermissionCode、ResourceType）
│   ├── aspect/                 # AOP切面（幂等性、分布式锁）
│   └── resources/mapper/       # MyBatis XML
├── database/                   # 数据库脚本
│   └── schema_acl.sql          # ACL权限系统表结构
├── docs/                       # 📚 文档（22个文档）
│   ├── 设计文档/               # 系统设计、方案评估
│   ├── 测试文档/               # 测试报告、验证结果
│   └── 开发文档/               # 开发规范、接口统计
├── api-test.html               # 🎨 测试页面（v2.0）
└── .cursor/rules/              # AI开发规范
```

---

## 📊 数据库设计

### 核心表（8张）

| 表名 | 说明 |
|------|------|
| tb_user | 用户表 |
| tb_tokens | Token表（Access + Refresh） |
| tb_todos | TODO表 |
| tb_teams | 团队表 |
| tb_team_members | 团队成员表 |
| tb_tags | 标签表 |
| tb_todo_tags | TODO-标签关联表 |
| tb_scheduled_tasks | 定时任务表 |

### ACL权限表（4张）

| 表名 | 说明 | 特点 |
|------|------|------|
| tb_acl_permission_definitions | 权限定义 | 14种权限 |
| tb_acl_access_control | 访问控制列表 | 支持过期、撤销 |
| tb_acl_permission_audit | 权限审计日志 | 完整追溯 |
| tb_acl_role_definitions | 角色定义 | 预留扩展 |

**脚本位置**：`database/schema_acl.sql`

---

## 🔧 配置说明

### Nacos配置中心

**访问地址**：http://localhost:8848/nacos（nacos/nacos）

**配置文件**：
- `collabtask-api.yaml` - API服务配置（MySQL、Redis）
- `collabtask-gateway.yaml` - Gateway配置（路由、Token验证）

**详细配置**：`docs/Nacos配置中心使用指南.md`

---

## 🚢 部署

### Docker部署（推荐）

```bash
# 启动
docker-compose up -d

# 停止
docker-compose down

# 查看日志
docker-compose logs -f
```

### CI/CD（GitLab）

```bash
# 自动部署：dev → test分支
git checkout dev && git push origin dev
# 创建MR → 合并 → 自动部署测试环境

# 手动部署：test → main分支
# 合并后在GitLab Pipeline中手动点击"deploy-prod"
```

**详细说明**：`docs/CI-CD实战指南.md`

---

## 📝 开发规范

**AI自动遵守**：`.cursor/rules/develop-style.mdc`

**核心规范**：
- ✅ DTO/VO/Entity 分离
- ✅ 分层架构（Controller → Service → DAO）
- ✅ ACL权限检查
- ✅ 并发控制（@Idempotent、@DistributedLock）

---

## 📚 文档

### 核心文档

| 文档 | 说明 |
|------|------|
| [测试页面核心功能完善报告](docs/设计文档/02-测试页面核心功能完善报告.md) | ⭐ 功能详解 |
| [v1.1-v1.2最终验证报告](docs/测试文档/01-v1.1-v1.2最终验证报告.md) | ⭐ 测试结果 |
| [API接口统计](docs/开发文档/06-API接口统计.md) | 40个接口清单 |
| [Postman使用说明](docs/测试文档/04-Postman使用说明.md) | API测试 |

**完整索引**：`docs/README.md`

---

## 🎯 版本历史

### v2.0（2025-11-10）
- ✅ TODO标签关联、团队成员管理
- ✅ 高级筛选+排序
- ✅ 接口覆盖率95%

### v1.3（2025-11-10）
- ✅ TODO列表显示共享的TODO

### v1.2（2025-11-10）
- ✅ 幂等性控制、分布式锁

### v1.1（2025-11-10）
- ✅ ACL权限系统、TODO共享

### v1.0（2025-11-09）
- ✅ 基础TODO、团队、标签管理

---

## 🛠️ 常用命令

```bash
# 快速启动
docker-compose up -d              # 启动所有服务
make status                       # 查看状态

# 开发调试
make start-api                    # 启动API
make start-gateway                # 启动Gateway

# 测试
mvn test                          # 单元测试
open api-test.html                # 功能测试

# 构建部署
make build                        # 编译打包
make release-test                 # 发版到测试环境
```

---

## 🔍 服务访问

| 服务 | 地址 | 说明 |
|------|------|------|
| Gateway | http://localhost:8001 | 统一入口 |
| API服务 | http://localhost:8002 | 直接访问 |
| 测试页面 | `api-test.html` | 功能测试 ⭐ |
| Knife4j | http://localhost:8002/collabtask-api/doc.html | API文档 |
| Nacos | http://localhost:8848/nacos | 配置中心 |

---

## 💡 快速上手

### 1. 启动服务

```bash
docker-compose up -d
```

### 2. 测试功能

```bash
# 打开测试页面
open api-test.html

# 操作步骤：
# 1. 点击"登录"
# 2. 点击"测试所有功能（完整）"
# 3. 查看日志输出
```

### 3. 查看文档

```bash
# 查看文档索引
open docs/README.md

# 查看核心功能说明
open docs/设计文档/02-测试页面核心功能完善报告.md
```

---

## 📄 许可证

MIT License - 详见 [LICENSE](LICENSE)

---

**© 2025 CollabTask Team**
