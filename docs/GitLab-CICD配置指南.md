# GitLab CI/CD + Nexus 配置指南

## 🎯 架构方案

```
┌─────────────┐     ┌──────────────┐     ┌───────────────┐
│   GitLab    │────>│  GitLab CI   │────>│    Nexus      │
│   (代码仓库) │     │  (构建+测试) │     │   (镜像仓库)   │
└─────────────┘     └──────┬───────┘     └───────┬───────┘
                           │                     │
                           │                     │
                    ┌──────▼─────────────────────▼───┐
                    │     目标服务器 (Docker Host)     │
                    │  ┌───────────┐  ┌──────────┐   │
                    │  │    API    │  │ Gateway  │   │
                    │  └───────────┘  └──────────┘   │
                    └─────────────────────────────────┘
```

## 📋 前置准备

### 1. Nexus私服配置

#### 安装Nexus（如果还没有）
```bash
# 使用Docker快速启动Nexus
docker run -d -p 8081:8081 -p 8082:8082 \
  --name nexus \
  -v nexus-data:/nexus-data \
  sonatype/nexus3:latest

# 获取初始密码
docker exec nexus cat /nexus-data/admin.password
```

访问：http://localhost:8081

#### 创建Docker Registry

1. 登录Nexus：http://localhost:8081
2. 创建Repository：
   - Type: `docker (hosted)`
   - Name: `docker-hosted`
   - HTTP port: `8082`
   - Enable Docker V1 API: `勾选`
3. 创建用户：Settings → Security → Users
   - ID: `deployer`
   - Password: `your_password`
   - Roles: `nx-admin`

### 2. Docker配置

#### 允许HTTP Registry（开发环境）

**Linux**：
```bash
sudo vim /etc/docker/daemon.json
```

**Mac**：
Docker Desktop → Settings → Docker Engine

添加：
```json
{
  "insecure-registries": ["localhost:8082"]
}
```

重启Docker。

#### 登录Nexus Docker Registry
```bash
docker login localhost:8082 -u deployer -p your_password
```

### 3. GitLab CI/CD配置

#### 在GitLab项目中设置环境变量

Settings → CI/CD → Variables

| 变量名 | 值 | Protected | Masked |
|-------|-----|-----------|--------|
| `NEXUS_REGISTRY` | `localhost:8082` | ❌ | ❌ |
| `NEXUS_USERNAME` | `deployer` | ❌ | ✅ |
| `NEXUS_PASSWORD` | `your_password` | ❌ | ✅ |
| `NEXUS_MAVEN_REPO` | `http://localhost:8081/repository/maven-public/` | ❌ | ❌ |
| `DEV_SERVER_HOST` | `192.168.1.100` | ❌ | ❌ |
| `DEV_SERVER_USER` | `deploy` | ❌ | ❌ |

#### 配置SSH密钥（用于部署）

```bash
# 1. 生成SSH密钥
ssh-keygen -t rsa -b 4096 -C "gitlab-ci" -f ~/.ssh/gitlab_ci_rsa

# 2. 添加公钥到目标服务器
ssh-copy-id -i ~/.ssh/gitlab_ci_rsa.pub deploy@192.168.1.100

# 3. 在GitLab添加私钥
# Settings → CI/CD → Variables
# Variable key: SSH_PRIVATE_KEY
# Value: (粘贴私钥内容)
```

### 4. GitLab Runner配置

#### 安装GitLab Runner（在构建服务器上）

```bash
# CentOS
curl -L https://packages.gitlab.com/install/repositories/runner/gitlab-runner/script.rpm.sh | sudo bash
sudo yum install gitlab-runner

# 注册Runner
sudo gitlab-runner register
```

注册信息：
- GitLab URL: `http://your-gitlab-server`
- Token: 在GitLab项目的 Settings → CI/CD → Runners 获取
- Executor: `docker`
- Default Image: `maven:3.9-eclipse-temurin-17`

## 🚀 使用方式

### 自动触发（推荐）

```bash
# 1. 提交代码
git add .
git commit -m "feat: 添加新功能"
git push origin develop

# 2. GitLab自动执行CI/CD
# - 编译
# - 测试
# - 构建镜像
# - 推送到Nexus

# 3. 在GitLab Pipeline页面手动触发部署
```

### 手动部署

```bash
# 使用部署脚本
chmod +x scripts/deploy.sh

# 设置环境变量
export NEXUS_REGISTRY=localhost:8082
export NEXUS_USERNAME=deployer
export NEXUS_PASSWORD=your_password

# 执行部署
./scripts/deploy.sh
```

## 📊 CI/CD流程

```
┌──────────────┐
│  Git Push    │
└──────┬───────┘
       │
┌──────▼───────┐
│ maven-build  │ - Maven编译
└──────┬───────┘
       │
┌──────▼───────┐
│  unit-test   │ - 单元测试
│code-quality  │ - 代码质量检查
└──────┬───────┘
       │
┌──────▼───────┐
│docker-build  │ - 构建Docker镜像
│              │ - 推送到Nexus
└──────┬───────┘
       │
┌──────▼───────┐
│    deploy    │ - SSH到目标服务器
│   (manual)   │ - 拉取最新镜像
│              │ - 重启服务
└──────────────┘
```

## 🔐 Nexus Maven仓库配置

### Maven settings.xml

在 `~/.m2/settings.xml` 中配置：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings>
  <mirrors>
    <mirror>
      <id>nexus</id>
      <mirrorOf>*</mirrorOf>
      <url>http://localhost:8081/repository/maven-public/</url>
    </mirror>
  </mirrors>
  
  <servers>
    <server>
      <id>nexus</id>
      <username>deployer</username>
      <password>your_password</password>
    </server>
  </servers>
</settings>
```

### 项目POM配置

```xml
<distributionManagement>
  <repository>
    <id>nexus</id>
    <url>http://localhost:8081/repository/maven-releases/</url>
  </repository>
  <snapshotRepository>
    <id>nexus</id>
    <url>http://localhost:8081/repository/maven-snapshots/</url>
  </snapshotRepository>
</distributionManagement>
```

## 🛠️ 常用命令

### 本地构建并推送

```bash
# 1. 构建镜像
docker build -f collabtask-api/Dockerfile \
  -t localhost:8082/collabtask-api:v1.0 .

# 2. 推送到Nexus
docker push localhost:8082/collabtask-api:v1.0

# 3. 从Nexus拉取
docker pull localhost:8082/collabtask-api:v1.0
```

### 使用特定版本部署

```bash
# 设置版本号
export IMAGE_TAG=abc1234
export NEXUS_REGISTRY=localhost:8082

# 启动
docker-compose -f docker-compose-nexus.yml up -d
```

### 查看Nexus中的镜像

```bash
# 使用Nexus API查询
curl -u deployer:password \
  http://localhost:8081/service/rest/v1/search?repository=docker-hosted
```

## 📝 GitLab Pipeline示例

推送代码后，在GitLab中看到的Pipeline：

```
develop分支:
  ✅ maven-build (2m 30s)
  ✅ unit-test (1m 15s)
  ✅ code-quality (45s)
  ✅ docker-build-api (3m 20s)
  ✅ docker-build-gateway (2m 40s)
  ⏸  deploy-dev (手动触发)

main分支:
  ✅ maven-build
  ✅ unit-test
  ✅ code-quality
  ✅ docker-build-api
  ✅ docker-build-gateway
  ⏸  deploy-prod (手动触发)
```

## 🎯 目录服务器部署配置

### 创建部署目录

```bash
# SSH到目标服务器
ssh deploy@192.168.1.100

# 创建目录
sudo mkdir -p /opt/collabtask
sudo chown deploy:deploy /opt/collabtask
cd /opt/collabtask

# 复制docker-compose配置
scp docker-compose-nexus.yml deploy@192.168.1.100:/opt/collabtask/docker-compose.yml

# 登录Nexus Registry
docker login localhost:8082 -u deployer -p password
```

### 首次部署

```bash
cd /opt/collabtask

# 设置环境变量
export NEXUS_REGISTRY=localhost:8082
export IMAGE_TAG=latest

# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f
```

## 🔍 故障排查

### 无法推送镜像到Nexus

**检查**：
1. Nexus Docker Registry是否启用
2. 端口8082是否开放
3. Docker配置中是否添加了insecure-registry
4. 登录凭证是否正确

### GitLab Runner无法构建

**检查**：
1. Runner是否注册成功
2. Runner是否有Docker权限
3. 网络是否可以访问Nexus

### 部署失败

**检查**：
1. SSH密钥是否配置正确
2. 目标服务器是否可以访问Nexus
3. docker-compose.yml是否存在
4. 环境变量是否设置

## 💡 最佳实践

### 1. 版本管理

使用Git Commit SHA作为镜像版本：
```bash
VERSION=$(git rev-parse --short HEAD)
docker tag app:latest nexus:8082/app:${VERSION}
```

### 2. 镜像清理

定期清理旧镜像：
```bash
# 在Nexus中配置Cleanup Policy
# 保留最近10个版本，删除30天前的镜像
```

### 3. 安全建议

- ✅ Nexus启用HTTPS
- ✅ 使用强密码
- ✅ 定期更新密钥
- ✅ 生产环境使用受保护的变量

## 📊 对比：GitHub Actions vs GitLab CI

| 特性 | GitHub Actions | GitLab CI |
|------|---------------|-----------|
| 本地部署 | ❌ 需要self-hosted runner | ✅ 完全本地 |
| Nexus集成 | ⚠️ 需要配置 | ✅ 简单 |
| Pipeline可视化 | ✅ 好 | ✅ 更好 |
| 配置复杂度 | 中等 | 简单 |
| 适用场景 | 公开项目 | 企业内部 |

**建议**：企业环境使用GitLab CI + Nexus！

---

**你现在有了一套完整的本地GitLab CI/CD + Nexus私服方案！** 🚀

