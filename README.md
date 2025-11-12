[English](README.md) | [中文](README_ZH.md)

# CollabTask - Collaborative Task Management System

[![JDK](https://img.shields.io/badge/JDK-17+-orange)](https://www.oracle.com/java/technologies/downloads/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-green)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

> 🚀 Task management system with TODO sharing, team collaboration, and fine-grained permission control

---

## 📋 Project Overview

CollabTask is a fully-featured collaborative task management system with core features including TODO management, team collaboration, TODO sharing, and ACL permission control.

### ✨ Core Features

- 📝 **TODO Management** - Create, update, delete, complete, sort, and filter
- 🤝 **TODO Sharing** - Support VIEW/EDIT permissions for collaboration
- 👥 **Team Management** - Create teams, add members, team TODOs
- 🏷️ **Tag System** - Colored tags, TODO categorization
- 🔒 **ACL Permissions** - Fine-grained permission control (v1.1)
- ⚡ **Concurrency Control** - Idempotency + distributed locks (v1.2)
- 🌍 **Multi-language** - Support Simplified Chinese, English, Traditional Chinese (v1.3)

### 🎯 Technical Features

- 🌐 Gateway + API service-oriented architecture
- 🔐 JWT dual-token authentication
- 🌍 Multi-language internationalization (zh-CN/en/zh-TW)
- 📊 Support advanced filtering and sorting
- ✅ 95% API test coverage

---

## 🏗️ Technical Architecture

### System Architecture Diagram

```
                    ┌─────────────────┐
                    │   Test Page     │
                    │ api-test.html   │
                    └────────┬────────┘
                             │ HTTP
                             ▼
┌────────────────────────────────────────────────────────────┐
│                      Gateway Layer (:8001)                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │  Routing │  │  Token   │  │   Load   │  │   User   │  │
│  │ Forward  │  │  Verify  │  │ Balance  │  │  Header  │  │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │
└───────────────────────┬────────────────────────────────────┘
                        │
            ┌───────────┴───────────┐
            │                       │
    ┌───────▼────────┐      ┌──────▼─────────┐
    │   API Node-1   │      │   API Node-2   │
    │   (:8002)      │      │   (:8002)      │
    │ ┌────────────┐ │      │ ┌────────────┐ │
    │ │ Controller │ │      │ │ Controller │ │
    │ └─────┬──────┘ │      │ └─────┬──────┘ │
    │ ┌─────▼──────┐ │      │ ┌─────▼──────┐ │
    │ │  Service   │ │      │ │  Service   │ │
    │ │ +ACL Perms │ │      │ │ +ACL Perms │ │
    │ │ +Idempotent│ │      │ │ +Idempotent│ │
    │ │ +Dist Lock │ │      │ │ +Dist Lock │ │
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
   │ tb_todos │   │ Dist Lock│   │ Service │
   │ tb_teams │   │ Idempote │   │Registry │
   │ tb_acl_* │   │ Token   │    │ Config  │
   └──────────┘   └─────────┘    └─────────┘
```

### Request Flow

```
1. Client Request
   ↓
2. Gateway (:8001)
   ├─ Token Verification
   ├─ Add user info to headers (X-User-Id, X-Username)
   └─ Load balance to select API node
   ↓
3. API Node (:8002)
   ├─ Controller: Receive request
   ├─ AOP Aspects (Before Service):
   │   ├─ @RequirePermission → ACL permission check (AclPermissionAspect)
   │   ├─ @Idempotent → Idempotency control (Redis)
   │   └─ @DistributedLock → Distributed lock (Redisson)
   ├─ Service: 
   │   ├─ Get userId from headers (UserContext)
   │   └─ Business logic processing
   └─ DAO: MyBatis query database
   ↓
4. Return Response
```

### Core Components

| Component | Responsibility | Technology |
|-----------|---------------|------------|
| **Gateway** | Routing, Token verification, Load balancing | Spring Cloud Gateway |
| **API Node** | Business logic, ACL permissions, Concurrency control | Spring Boot + MyBatis Plus |
| **MySQL** | Data storage (TODO, teams, ACL permissions) | MySQL 8.0 |
| **Redis** | Distributed lock, Idempotency, Token cache | Redis 6.0 + Redisson |
| **Nacos** | Service registry, Config center | Nacos 2.0 |

**Tech Stack**:
- Spring Boot 3.5.4 + Spring Cloud Gateway
- MySQL 8.0 + MyBatis Plus + Redis
- Nacos (Config/Registry) + Redisson (Distributed Lock)

---

## 🚀 Quick Start

### Method 1: Docker Compose (Recommended)

```bash
# Start all services with one command
docker-compose up -d

# Access
# Gateway: http://localhost:8001
# Test Page: api-test.html
```

### Method 2: Local Startup

```bash
# 1. Start infrastructure services
docker-compose up -d mysql redis nacos

# 2. Initialize database
mysql -h localhost -u root -p < database/schema_acl.sql

# 3. Start applications
make start-gateway  # or: cd collabtask-gateway && mvn spring-boot:run
make start-api      # or: cd collabtask-api && mvn spring-boot:run
```

### Method 3: Makefile

```bash
make build          # Build and package
make docker-up      # Start Docker services
make status         # Check status
```

---

## 🧪 Feature Testing

### Test Page (Recommended) ⭐

```bash
# Open test page
open api-test.html

# Features:
# ✅ Covers 95% of APIs (38/40)
# ✅ All requests through Gateway
# ✅ TODO sharing test support
# ✅ Team/Tag management support
# ✅ Automated test scenarios
```

**Testing Steps**:
1. Click "Login"
2. Click "Test All Features (Complete)"
3. View log output

### Other Testing Methods

- **Knife4j**: http://localhost:8002/collabtask-api/doc.html

---

## 📖 Core APIs

### Authentication
```bash
POST /api/login      # Login
POST /api/logout     # Logout
POST /api/register   # Register
```

### TODO Management
```bash
GET    /api/todos              # List (with filtering + sorting)
POST   /api/todos              # Create (v1.2 idempotency)
PUT    /api/todos/{id}         # Update (v1.1 ACL check)
DELETE /api/todos/{id}         # Delete
PATCH  /api/todos/{id}/complete # Complete
```

### TODO Sharing (v1.1 Core Feature)
```bash
POST   /api/todos/{id}/share           # Share TODO (VIEW/EDIT permissions)
DELETE /api/todos/{id}/share/{userId}  # Cancel sharing
```

### Team Management
```bash
POST   /api/teams                      # Create team
POST   /api/teams/{id}/members/{uid}   # Add member
DELETE /api/teams/{id}/members/{uid}   # Remove member
```

### Tag Management
```bash
POST   /api/tags                       # Create tag
POST   /api/tags/todos/{tid}/tags/{id} # Add tag to TODO
DELETE /api/tags/todos/{tid}/tags/{id} # Remove tag
GET    /api/tags/todos/{tid}           # View TODO tags
```

**Complete APIs**: 40 APIs, see `docs/开发文档/06-API接口统计.md`

---

## 🗂️ Project Structure

```
CollaborativeTasks2/
├── collabtask-gateway/         # Gateway service (port 8001)
├── collabtask-api/             # API service (port 8002)
│   ├── controller/             # Controllers (TODO, Team, Tag)
│   ├── service/                # Business layer (ACL permission checks)
│   ├── dao/                    # Data access layer
│   ├── entity/                 # Entities
│   ├── dto/                    # Data transfer objects (DTO/VO)
│   ├── enums/                  # Enums (PermissionCode, ResourceType)
│   ├── aspect/                 # AOP aspects (Idempotency, Distributed lock)
│   └── resources/mapper/       # MyBatis XML
├── database/                   # Database scripts
│   └── schema_acl.sql          # ACL permission system schema
├── docs/                       # 📚 Documentation (22 docs)
│   ├── 设计文档/               # System design, solution evaluation
│   ├── 测试文档/               # Test reports, validation results
│   └── 开发文档/               # Development standards, API stats
├── api-test.html               # 🎨 Test page (v2.0)
└── .cursor/rules/              # AI development standards
```

---

## 📊 Database Design

### Core Tables (8)

| Table | Description |
|-------|-------------|
| tb_user | User table |
| tb_tokens | Token table (Access + Refresh) |
| tb_todos | TODO table |
| tb_teams | Team table |
| tb_team_members | Team member table |
| tb_tags | Tag table |
| tb_todo_tags | TODO-Tag association table |
| tb_scheduled_tasks | Scheduled task table |

### ACL Permission Tables (4)

| Table | Description | Feature |
|-------|-------------|---------|
| tb_acl_permission_definitions | Permission definitions | 14 types of permissions |
| tb_acl_access_control | Access control list | Support expiration, revocation |
| tb_acl_permission_audit | Permission audit log | Complete traceability |
| tb_acl_role_definitions | Role definitions | Reserved for expansion |

**Script Location**: `database/schema_acl.sql`

---

## 🔧 Configuration

### Nacos Config Center

**Access URL**: http://localhost:8848/nacos (nacos/nacos)

**Config Files**:
- `collabtask-api.yaml` - API service config (MySQL, Redis)
- `collabtask-gateway.yaml` - Gateway config (Routing, Token verification)

**Detailed Guide**: `docs/Nacos配置中心使用指南.md`

---

## 🚢 Deployment

### Docker Deployment (Recommended)

```bash
# Start
docker-compose up -d

# Stop
docker-compose down

# View logs
docker-compose logs -f
```

### CI/CD (GitLab)

```bash
# Auto deploy: dev → test branch
git checkout dev && git push origin dev
# Create MR → Merge → Auto deploy to test environment

# Manual deploy: test → main branch
# After merge, manually click "deploy-prod" in GitLab Pipeline
```

**Detailed Guide**: `docs/CI-CD实战指南.md`

---

## 📝 Development Standards

**AI Auto-follows**: `.cursor/rules/develop-style.mdc`

**Core Standards**:
- ✅ DTO/VO/Entity separation
- ✅ Layered architecture (Controller → Service → DAO)
- ✅ ACL permission checks
- ✅ Concurrency control (@Idempotent, @DistributedLock)

---

## 🌍 Multi-language Support

The system supports multi-language internationalization (i18n), switch languages via `Accept-Language` header:

```bash
# English
curl -H "Accept-Language: en" http://localhost:8001/collabtask-api/api/login

# Traditional Chinese
curl -H "Accept-Language: zh-TW" http://localhost:8001/collabtask-api/api/login

# Simplified Chinese (Default)
curl -H "Accept-Language: zh-CN" http://localhost:8001/collabtask-api/api/login
```

**Supported Languages**:
- 🇨🇳 Simplified Chinese (zh-CN) - Default
- 🇺🇸 English (en)
- 🇹🇼 Traditional Chinese (zh-TW)

**Detailed Documentation**: [Multi-language Internationalization Guide](docs/多语言国际化使用指南.md)

---


## 🛠️ Common Commands

```bash
# Quick start
docker-compose up -d              # Start all services
make status                       # Check status

# Development
make start-api                    # Start API
make start-gateway                # Start Gateway

# Testing
mvn test                          # Unit tests
open api-test.html                # Feature tests

# Build & Deploy
make build                        # Build and package
make release-test                 # Release to test environment
```

---

## 🔍 Service Access

| Service | URL | Description |
|---------|-----|-------------|
| Gateway | http://localhost:8001 | Unified entry |
| API Service | http://localhost:8002 | Direct access |
| Test Page | `api-test.html` | Feature testing ⭐ |
| Knife4j | http://localhost:8002/collabtask-api/doc.html | API documentation |
| Nacos | http://localhost:8848/nacos | Config center |

---

## 💡 Quick Tutorial

### 1. Start Services

```bash
docker-compose up -d
```

### 2. Test Features

```bash
# Open test page
open api-test.html

# Steps:
# 1. Click "Login"
# 2. Click "Test All Features (Complete)"
# 3. View log output
```



