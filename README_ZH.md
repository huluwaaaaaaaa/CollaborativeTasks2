[English](README.md) | [中文](README_ZH.md)

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
- 🌍 **多语言** - 支持简体中文、英文、繁体中文（v1.3）

### 🎯 技术特性

- 🌐 Gateway + API服务化架构
- 🔐 JWT双Token认证
- 🌍 多语言国际化（简中/英文/繁中）
- 📊 支持高级筛选和排序
- ✅ 95%接口测试覆盖率

---

## 🏗️ 技术架构

### 系统架构图

```
                    ┌─────────────────┐
                    │   测试页面      │
                    │ api-test.html   │
                    └────────┬────────┘
                             │ HTTP
                             ▼
┌────────────────────────────────────────────────────────────┐
│                      Gateway层 (:8001)                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │   路由   │  │ Token验证│  │ 负载均衡 │  │用户信息  │  │
│  │  转发    │  │  认证    │  │  轮询    │  │请求头传递│  │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │
└───────────────────────┬────────────────────────────────────┘
                        │
            ┌───────────┴───────────┐
            │                       │
    ┌───────▼────────┐      ┌──────▼─────────┐
    │   API节点-1    │      │   API节点-2    │
    │   (:8002)      │      │   (:8002)      │
    │ ┌────────────┐ │      │ ┌────────────┐ │
    │ │ Controller │ │      │ │ Controller │ │
    │ └─────┬──────┘ │      │ └─────┬──────┘ │
    │ ┌─────▼──────┐ │      │ ┌─────▼──────┐ │
    │ │  Service   │ │      │ │  Service   │ │
    │ │ +ACL权限   │ │      │ │ +ACL权限   │ │
    │ │ +幂等性    │ │      │ │ +幂等性    │ │
    │ │ +分布式锁  │ │      │ │ +分布式锁  │ │
    │ └─────┬──────┘ │      │ └─────┬──────┘ │
    │ ┌─────▼──────┐ │      │ ┌─────▼──────┐ │
    │ │    DAO     │ │      │ │    DAO     │ │
    │ └─────┬──────┘ │      │ └─────┬──────┘ │
    └───────┼────────┘      └───────┼────────┘
            │                       │
            └───────────┬───────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
   ┌────▼─────┐   ┌────▼────┐    ┌────▼────┐
   │  MySQL   │   │  Redis  │    │  Nacos  │
   │  :3306   │   │  :6379  │    │  :8848  │
   ├──────────┤   ├─────────┤    ├─────────┤
   │ tb_todos │   │ 分布式锁│    │服务注册 │
   │ tb_teams │   │ 幂等性  │    │配置中心 │
   │ tb_acl_* │   │ Token   │    │         │
   └──────────┘   └─────────┘    └─────────┘
```

### 请求流程

```
1. 客户端请求
   ↓
2. Gateway (:8001)
   ├─ Token验证
   ├─ 添加用户信息到请求头 (X-User-Id, X-Username)
   └─ 负载均衡选择API节点
   ↓
3. API节点 (:8002)
   ├─ Controller: 接收请求
   ├─ Service: 
   │   ├─ 从请求头获取userId (UserContext)
   │   ├─ ACL权限检查 (AclPermissionService)
   │   ├─ 幂等性控制 (@Idempotent + Redis)
   │   ├─ 分布式锁 (@DistributedLock + Redis)
   │   └─ 业务逻辑处理
   └─ DAO: MyBatis查询数据库
   ↓
4. 返回响应
```

### 核心组件说明

| 组件 | 职责 | 技术 |
|------|------|------|
| **Gateway** | 路由转发、Token验证、负载均衡 | Spring Cloud Gateway |
| **API节点** | 业务逻辑、ACL权限、并发控制 | Spring Boot + MyBatis Plus |
| **MySQL** | 数据存储（TODO、团队、ACL权限） | MySQL 8.0 |
| **Redis** | 分布式锁、幂等性、Token缓存 | Redis 6.0 + Redisson |
| **Nacos** | 服务注册、配置中心 | Nacos 2.0 |

**技术栈**：
- Spring Boot 3.5.4 + Spring Cloud Gateway
- MySQL 8.0 + MyBatis Plus + Redis
- Nacos（配置中心/注册中心） + Redisson（分布式锁）

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

## 🌍 多语言支持

系统支持多语言国际化（i18n），通过 `Accept-Language` 请求头切换语言：

```bash
# 英文
curl -H "Accept-Language: en" http://localhost:8001/collabtask-api/api/login

# 繁体中文
curl -H "Accept-Language: zh-TW" http://localhost:8001/collabtask-api/api/login

# 简体中文（默认）
curl -H "Accept-Language: zh-CN" http://localhost:8001/collabtask-api/api/login
```

**支持的语言**：
- 🇨🇳 简体中文（zh-CN）- 默认
- 🇺🇸 英文（en）
- 🇹🇼 繁体中文（zh-TW）

**详细文档**：[多语言国际化使用指南](docs/多语言国际化使用指南.md)

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


