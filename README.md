# CollaborativeTasks2 - 协作任务管理系统

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com)
[![Code Coverage](https://img.shields.io/badge/coverage-80%25-green)](https://github.com)
[![License](https://img.shields.io/badge/license-MIT-blue)](https://github.com)
[![JDK](https://img.shields.io/badge/JDK-17+-orange)](https://www.oracle.com/java/technologies/downloads/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-green)](https://spring.io/projects/spring-boot)

> 🚀 基于微服务架构的企业级协作任务管理系统

---

## 📋 项目简介

CollaborativeTasks2 是一个功能完整的协作任务管理系统，采用微服务架构，支持团队协作、任务管理、标签分类等功能。

### ✨ 核心特性

- 🔐 **双Token认证** - Access Token + Refresh Token，安全可靠
- 👥 **团队协作** - 团队管理、成员邀请、权限控制
- 📝 **任务管理** - 创建、分配、跟踪、完成TODO
- 🏷️ **标签系统** - 灵活的标签分类和筛选
- 🔒 **ACL权限** - 细粒度的访问控制
- ⚡ **并发控制** - 分布式锁 + 幂等性保证
- 🌐 **微服务架构** - Gateway + API多节点部署

---

## 🏗️ 技术架构

```
┌─────────────────────────────────────────────────────────┐
│                       Gateway                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐            │
│  │  路由    │  │  认证    │  │ 负载均衡 │            │
│  └──────────┘  └──────────┘  └──────────┘            │
└─────────────────────┬───────────────────────────────────┘
                      │
         ┌────────────┴────────────┐
         │                         │
    ┌────▼─────┐             ┌────▼─────┐
    │ API 节点1 │             │ API 节点2 │
    └────┬─────┘             └────┬─────┘
         │                         │
         └────────────┬────────────┘
                      │
         ┌────────────┴────────────┐
         │                         │
    ┌────▼─────┐             ┌────▼─────┐
    │  MySQL   │             │  Redis   │
    └──────────┘             └──────────┘
```

### 技术栈

- **框架**：Spring Boot 3.5.4 + Spring Cloud 2024.0.0
- **数据库**：MySQL 8.0 + MyBatis Plus 3.5.8
- **缓存**：Redis 6.0 + Redisson 3.35.0
- **服务治理**：Nacos 2.0+
- **认证**：JWT + Refresh Token
- **测试**：JUnit 5 + Mockito

---

## 🚀 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Nacos 2.0+
- Maven 3.6+

### 安装步骤

#### 1. 克隆项目

```bash
git clone https://github.com/yourusername/CollaborativeTasks2.git
cd CollaborativeTasks2
```

#### 2. 启动Nacos

```bash
cd nacos/bin
./startup.sh -m standalone
```

#### 3. 初始化数据库

```bash
mysql -u root -p < database/schema_acl.sql
```

#### 4. 配置Nacos

访问 `http://localhost:8848/nacos`（账号：nacos/nacos）

导入配置：
- Data ID: `collabtask-api.yaml`
- Data ID: `collabtask-gateway.yaml`
- Group: `DEFAULT_GROUP`

#### 5. 启动服务

```bash
# 编译打包
mvn clean package -DskipTests

# 启动Gateway
cd collabtask-gateway
java -jar target/collabtask-gateway.jar

# 启动API服务
cd collabtask-api
java -jar target/collabtask-api.jar
```

#### 6. 验证

```bash
# 注册用户
curl -X POST http://localhost:8001/api/register \
  -H "Content-Type: application/json" \
  -d '{"mobile":"13800138000","password":"123456","confirmPassword":"123456"}'

# 登录
curl -X POST http://localhost:8001/api/login \
  -H "Content-Type: application/json" \
  -d '{"mobile":"13800138000","password":"123456"}'
```

---

## 📖 API文档

### 访问地址

- **Swagger UI**：http://localhost:8002/collabtask-api/doc.html
- **Gateway地址**：http://localhost:8001

### 主要接口

#### 认证接口
```bash
POST /api/login          # 登录
POST /api/register       # 注册
POST /api/logout         # 登出
POST /api/auth/refresh   # 刷新Token
```

#### TODO接口
```bash
GET    /api/todos        # 查询TODO列表
POST   /api/todos        # 创建TODO
GET    /api/todos/{id}   # 查询TODO详情
PUT    /api/todos/{id}   # 更新TODO
DELETE /api/todos/{id}   # 删除TODO
PATCH  /api/todos/{id}/complete  # 完成TODO
```

#### 团队接口
```bash
GET    /api/teams                         # 查询我的团队
POST   /api/teams                         # 创建团队
GET    /api/teams/{id}                    # 团队详情
PUT    /api/teams/{id}                    # 更新团队
DELETE /api/teams/{id}                    # 删除团队
POST   /api/teams/{id}/members/{uid}      # 添加成员
DELETE /api/teams/{id}/members/{uid}      # 移除成员
GET    /api/teams/{id}/members            # 成员列表
```

#### 标签接口
```bash
GET    /api/tags                          # 查询标签列表
POST   /api/tags                          # 创建标签
POST   /api/tags/todos/{tid}/tags/{id}    # 添加标签到TODO
GET    /api/tags/todos/{tid}              # 查询TODO的标签
```

详细API文档请查看 `docs/05-API接口设计.md`

---

## 🗂️ 项目结构

```
CollaborativeTasks2/
├── collabtask-gateway/          # 网关服务
├── collabtask-api/              # API服务
│   ├── src/main/java/io/user/
│   │   ├── controller/          # 控制器层
│   │   ├── service/             # 服务层
│   │   ├── dao/                 # 数据访问层
│   │   ├── entity/              # 实体类
│   │   ├── dto/                 # 数据传输对象
│   │   ├── aspect/              # AOP切面
│   │   ├── config/              # 配置类
│   │   └── common/              # 公共组件
│   ├── src/test/java/           # 单元测试
│   └── src/main/resources/
│       └── mapper/              # MyBatis映射文件
├── collabtask-common/           # 公共模块
├── database/                    # 数据库脚本
├── docs/                        # 设计文档
└── pom.xml                      # Maven配置
```

---

## 📊 数据库设计

### 核心表

| 表名 | 说明 | 记录数 |
|------|------|--------|
| tb_user | 用户表 | - |
| tb_tokens | Token表 | - |
| tb_todos | TODO表 | - |
| tb_teams | 团队表 | - |
| tb_team_members | 团队成员表 | - |
| tb_tags | 标签表 | - |
| tb_todo_tags | TODO-标签关联 | - |

### 权限表

| 表名 | 说明 | 记录数 |
|------|------|--------|
| tb_acl_permission_definitions | 权限定义 | 14 |
| tb_acl_access_control | 访问控制列表 | - |
| tb_acl_permission_audit | 权限审计日志 | - |
| tb_acl_role_definitions | 角色定义 | 2 |
| tb_acl_user_roles | 用户角色 | - |

详细设计请查看 `database/schema_acl.sql`

---

## 🧪 测试

### 运行单元测试

```bash
# 修改 collabtask-api/pom.xml 中的配置
<skipTests>false</skipTests>

# 运行测试
mvn test -pl collabtask-api
```

### 运行集成测试

```bash
# 启动服务
java -jar collabtask-api/target/collabtask-api.jar

# 执行测试脚本
bash scripts/integration-test.sh
```

### 测试报告

查看测试报告：
- 功能测试：`功能测试-完整报告.md`
- 集成测试：`集成测试-完整报告.md`
- 单元测试：`单元测试-交付报告.md`

---

## 📚 文档

### 设计文档
- [API接口设计](docs/05-API接口设计.md) - 30个接口详细设计
- [ACL权限系统设计](docs/09-完整ACL权限系统设计.md) - 权限系统详解
- [并发处理方案](docs/20-并发处理方案.md) - 分布式锁和幂等性
- [开发规范](develop-style.mdc) - 代码规范指南

### 测试文档
- [功能测试报告](功能测试-完整报告.md)
- [集成测试报告](集成测试-完整报告.md)
- [单元测试报告](单元测试-交付报告.md)

### 交付文档
- [代码交付报告](代码生成-最终交付报告.md)
- [问题修复报告](问题修复-完成报告.md)
- [项目总结](项目交付-最终总结.md)

---

## 🔧 配置说明

### application.yml

主要配置在Nacos配置中心：

**collabtask-api.yaml**：
- 数据库连接配置
- Redis配置
- MyBatis Plus配置

**collabtask-gateway.yaml**：
- 路由配置
- Token验证配置
- 负载均衡配置

详细配置请查看相关文档。

---

## 🤝 贡献指南

### 开发流程

1. Fork项目
2. 创建特性分支（`git checkout -b feature/AmazingFeature`）
3. 提交改动（`git commit -m 'Add AmazingFeature'`）
4. 推送到分支（`git push origin feature/AmazingFeature`）
5. 提交Pull Request

### 代码规范

请遵循 `develop-style.mdc` 中的开发规范：
- 使用统一的包结构
- 添加完整的注释
- 编写单元测试
- 通过所有测试

---

## 📝 版本历史

### v1.0 (2025-11-10)

**新增功能**：
- ✅ 用户注册、登录、登出
- ✅ TODO完整CRUD
- ✅ 团队管理
- ✅ 标签系统
- ✅ ACL权限控制
- ✅ 并发控制（分布式锁+幂等性）

**技术升级**：
- ✅ Spring Boot 3.5.4
- ✅ Java 17
- ✅ 微服务架构

**测试覆盖**：
- ✅ 55个单元测试
- ✅ 21个集成测试
- ✅ 26个功能测试

---

## 📞 联系方式

- **项目地址**：https://github.com/yourusername/CollaborativeTasks2
- **文档站点**：https://docs.collabtask.io
- **问题反馈**：https://github.com/yourusername/CollaborativeTasks2/issues

---

## 📄 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

---

## 🌟 Star History

如果这个项目对您有帮助，请给我们一个 Star ⭐

---

**© 2025 CollaborativeTasks Team. All Rights Reserved.**
