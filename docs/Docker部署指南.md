# Docker部署指南

## 🎯 部署方案

采用**Docker Compose**方式，一键启动所有服务。

## 🏗️ 架构

```
┌─────────────────────────────────────┐
│         Docker Network              │
│  ┌──────────┐  ┌──────────┐        │
│  │  MySQL   │  │  Redis   │        │
│  └────┬─────┘  └────┬─────┘        │
│       │             │               │
│  ┌────▼─────────────▼────┐         │
│  │      Nacos Server      │         │
│  └────────┬────────────────┘        │
│           │                         │
│  ┌────────▼──────┐  ┌──────────┐   │
│  │ collabtask-   │  │collabtask│   │
│  │    api        │◄─┤ gateway  │   │
│  └───────────────┘  └────┬─────┘   │
│                          │          │
└──────────────────────────┼──────────┘
                           │
                      ┌────▼─────┐
                      │  Client  │
                      └──────────┘
```

## 📦 包含的服务

| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| Nacos | 8848, 9848, 9849 | 注册中心+配置中心 |
| Gateway | 8001 | API网关 |
| API | 8002 | 业务服务 |

## 🚀 快速开始

### 前置要求

- Docker 20.10+
- Docker Compose 2.0+
- 8GB+ 可用内存

### 一键启动

```bash
# 方式一：使用Makefile（推荐）
make quick-start

# 方式二：使用Docker Compose
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 访问服务

- **Nacos控制台**: http://localhost:8848/nacos
  - 账号/密码: `nacos`/`nacos`
- **Gateway**: http://localhost:8001
- **API服务**: http://localhost:8002/collabtask-api

## 📋 详细步骤

### 1. 构建镜像

```bash
# 构建所有镜像
docker-compose build

# 或使用Makefile
make docker-build
```

### 2. 启动服务

```bash
# 启动所有服务
docker-compose up -d

# 查看启动日志
docker-compose logs -f
```

### 3. 初始化Nacos配置

**等待Nacos启动**（约1分钟），然后：

1. 访问：http://localhost:8848/nacos
2. 登录：`nacos`/`nacos`
3. 创建命名空间：`dev`
4. 创建配置文件：
   - `collabtask-api.yaml`
   - `collabtask-gateway.yaml`
   - `common-config.yaml`

**配置内容参考**：`docs/Nacos配置中心使用指南.md`

### 4. 验证服务

```bash
# 健康检查
curl http://localhost:8001/actuator/health
curl http://localhost:8002/collabtask-api/actuator/health

# 测试API
curl -X POST http://localhost:8001/api/login \
  -H "Content-Type: application/json" \
  -d '{"mobile":"13612345678","password":"admin"}'
```

## 🛠️ 常用命令

### 查看服务状态
```bash
docker-compose ps
make status
```

### 查看日志
```bash
# 所有服务
docker-compose logs -f

# 单个服务
docker-compose logs -f collabtask-api
docker-compose logs -f collabtask-gateway
```

### 重启服务
```bash
# 重启所有服务
docker-compose restart

# 重启单个服务
docker-compose restart collabtask-api
```

### 停止服务
```bash
# 停止（保留数据）
docker-compose down

# 停止并删除数据卷
docker-compose down -v
```

### 进入容器
```bash
# 进入API容器
docker exec -it collabtask-api sh

# 进入MySQL容器
docker exec -it collabtask-mysql bash
```

## 🔍 故障排查

### 服务启动失败

**检查日志**：
```bash
docker-compose logs collabtask-api
docker-compose logs collabtask-gateway
```

**常见问题**：
1. MySQL未就绪 → 等待更长时间
2. Nacos配置未创建 → 手动创建配置
3. 端口被占用 → 修改docker-compose.yml中的端口映射

### 无法连接Nacos

**检查Nacos状态**：
```bash
docker-compose logs nacos
curl http://localhost:8848/nacos/
```

**解决方案**：
1. 确保MySQL已启动
2. 等待Nacos完全启动（约1-2分钟）
3. 检查nacos日志

### 数据库连接失败

**检查MySQL**：
```bash
docker exec -it collabtask-mysql mysql -uroot -p
# 密码：Y9!8nIeRH@163

# 查看数据库
show databases;
use collabtask;
show tables;
```

## 🎯 环境变量

可以通过环境变量覆盖配置：

```bash
# 设置Nacos地址
export NACOS_HOST=192.168.1.100
export NACOS_PORT=8848

# 设置Nacos命名空间
export NACOS_NAMESPACE=prod

# 启动
docker-compose up -d
```

## 📊 资源使用

| 服务 | CPU | 内存 | 磁盘 |
|------|-----|------|------|
| MySQL | ~5% | ~400MB | ~1GB |
| Redis | ~1% | ~50MB | ~100MB |
| Nacos | ~10% | ~800MB | ~500MB |
| API | ~5% | ~600MB | ~100MB |
| Gateway | ~3% | ~400MB | ~100MB |
| **总计** | **~25%** | **~2.2GB** | **~2GB** |

## 🔐 生产环境建议

### 1. 修改默认密码
```yaml
# docker-compose.yml
mysql:
  environment:
    MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}

nacos:
  environment:
    MYSQL_SERVICE_PASSWORD: ${MYSQL_ROOT_PASSWORD}
```

### 2. 使用外部配置
```bash
# 创建.env文件
cat > .env << EOF
MYSQL_ROOT_PASSWORD=your_strong_password
NACOS_USERNAME=admin
NACOS_PASSWORD=your_nacos_password
EOF

# Docker Compose会自动读取.env
docker-compose up -d
```

### 3. 数据持久化

确保volumes配置正确：
```yaml
volumes:
  mysql-data:
    driver: local
  nacos-data:
    driver: local
```

### 4. 备份数据

```bash
# 备份MySQL
docker exec collabtask-mysql mysqldump -uroot -p collabtask > backup.sql

# 备份Nacos配置
# 在Nacos控制台导出配置
```

## 🚀 CI/CD集成

项目已配置GitHub Actions，推送代码后自动：
1. ✅ 编译和测试
2. ✅ 构建Docker镜像
3. ✅ 代码质量检查
4. ✅ 安全扫描

**查看CI/CD状态**：GitHub仓库 → Actions标签

## 📝 下一步

### 本地开发
1. ✅ Docker Compose本地运行
2. ✅ Nacos配置管理

### 演示准备
1. ✅ 完整的Docker环境
2. ✅ 一键启动脚本
3. ⬜ 准备演示数据
4. ⬜ Postman测试集合

### 生产部署（可选）
1. ⬜ Kubernetes配置
2. ⬜ Helm Charts
3. ⬜ CI/CD自动部署

---

**现在你可以用 `make quick-start` 一键启动整个系统！** 🎉

