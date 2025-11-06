# GitLab CI/CD 完整配置指南

## 🎯 目标流程

```
develop分支 (开发)
    ↓ [合并]
test分支
    ↓ [自动触发]
GitLab CI/CD Pipeline
    ├─ Maven编译
    ├─ 运行测试
    ├─ 构建Docker镜像
    ├─ 推送到Nexus
    └─ 自动部署到测试环境 ✅
```

---

## 📋 步骤1：配置GitLab项目

### 1.1 创建GitLab仓库

如果使用本地GitLab（你的Docker中已有）：

```bash
# 访问GitLab
open http://localhost:8084

# 首次登录获取密码
docker exec gitlab grep 'Password:' /etc/gitlab/initial_root_password

# 登录信息：
# 用户名: root
# 密码: (上面命令的输出)
```

创建新项目：`CollaborativeTasks2`

### 1.2 推送代码到GitLab

```bash
cd /Users/zgy/Documents/workspace-web3/CollaborativeTasks2

# 初始化Git（如果还没有）
git init

# 添加GitLab远程仓库
git remote add gitlab http://localhost:8084/root/CollaborativeTasks2.git

# 创建分支结构
git checkout -b main
git add .
git commit -m "Initial commit"
git push -u gitlab main

# 创建develop分支
git checkout -b develop
git push -u gitlab develop

# 创建test分支
git checkout -b test
git push -u gitlab test
```

---

## 📋 步骤2：配置GitLab Runner（本地版）

### 方式1：使用Docker运行Runner（推荐）

```bash
# 启动GitLab Runner容器
docker run -d \
  --name gitlab-runner \
  --restart always \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v gitlab-runner-config:/etc/gitlab-runner \
  gitlab/gitlab-runner:latest

# 查看Runner状态
docker ps | grep gitlab-runner
```

### 方式2：注册Runner

#### 2.1 获取注册Token

1. 访问 http://localhost:8084/root/CollaborativeTasks2
2. **Settings → CI/CD**
3. 展开 **Runners**
4. 复制 **Registration token**

#### 2.2 注册Runner到项目

```bash
# 在gitlab-runner容器中注册
docker exec -it gitlab-runner gitlab-runner register

# 按提示输入：
# GitLab URL: http://gitlab  # 容器间通信用gitlab
# Token: [你的Registration Token]
# Description: docker-runner
# Tags: docker,build
# Executor: docker
# Default Docker image: docker:24
```

**或者一键注册**：

```bash
docker exec gitlab-runner gitlab-runner register \
  --non-interactive \
  --url "http://gitlab" \
  --registration-token "YOUR_TOKEN" \
  --executor "docker" \
  --docker-image docker:24 \
  --description "docker-runner" \
  --tag-list "docker,build" \
  --docker-volumes "/var/run/docker.sock:/var/run/docker.sock" \
  --docker-network-mode "gitlab-network"
```

---

## 📋 步骤3：配置环境变量

在GitLab项目中：**Settings → CI/CD → Variables**

### 必需变量

| 变量名 | 值 | Protected | Masked |
|--------|-----|-----------|--------|
| `NEXUS_REGISTRY` | `host.docker.internal:5000` | ❌ | ❌ |
| `NEXUS_USERNAME` | `admin` | ❌ | ❌ |
| `NEXUS_PASSWORD` | `123456` | ✅ | ✅ |
| `NEXUS_MAVEN_REPO` | `http://host.docker.internal:8081/repository/maven-public/` | ❌ | ❌ |

### 可选变量（如果有测试服务器）

| 变量名 | 值 | 说明 |
|--------|-----|------|
| `TEST_SERVER_HOST` | `192.168.1.100` | 测试服务器IP |
| `TEST_SERVER_USER` | `deploy` | SSH用户名 |
| `SSH_PRIVATE_KEY` | `-----BEGIN...` | SSH私钥（Protected） |

**注意**：使用 `host.docker.internal` 让GitLab Runner容器能访问宿主机的Nexus。

---

## 📋 步骤4：创建网络（重要）

GitLab容器和Runner需要在同一网络：

```bash
# 创建网络
docker network create gitlab-network

# 将GitLab和Runner加入网络
docker network connect gitlab-network gitlab
docker network connect gitlab-network gitlab-runner
docker network connect gitlab-network collabtask-nexus

# 验证网络
docker network inspect gitlab-network
```

---

## 📋 步骤5：测试CI/CD流程

### 5.1 模拟开发流程

```bash
cd /Users/zgy/Documents/workspace-web3/CollaborativeTasks2

# 切换到develop分支
git checkout develop

# 做一些修改
echo "# Test CI/CD" >> README.md
git add README.md
git commit -m "test: 测试CI/CD流程"
git push gitlab develop
```

### 5.2 合并到test分支（触发自动发版）

```bash
# 方式1：命令行合并
git checkout test
git merge develop
git push gitlab test

# 方式2：在GitLab Web界面创建Merge Request
# http://localhost:8084/root/CollaborativeTasks2/-/merge_requests/new
# Source: develop
# Target: test
# 点击 Create Merge Request → Merge
```

### 5.3 查看Pipeline

访问：http://localhost:8084/root/CollaborativeTasks2/-/pipelines

**Pipeline阶段**：
1. ✅ **build** - Maven编译
2. ✅ **test** - 单元测试
3. ✅ **docker-build** - 构建镜像
4. ✅ **deploy** - 自动部署到测试环境

---

## 📊 分支策略

```
┌──────────────────────────────────────────┐
│  main (生产)                              │
│  - 稳定版本                                │
│  - 手动部署到生产                          │
└──────────────────────────────────────────┘
           ↑ [Merge Request]
┌──────────────────────────────────────────┐
│  test (测试)                              │
│  - 测试版本                                │
│  - 自动部署到测试环境 ✅                    │
└──────────────────────────────────────────┘
           ↑ [Merge Request]
┌──────────────────────────────────────────┐
│  develop (开发)                           │
│  - 开发中的功能                            │
│  - 自动构建，手动部署                      │
└──────────────────────────────────────────┘
           ↑ [Merge]
┌──────────────────────────────────────────┐
│  feature/* (功能分支)                     │
│  - 新功能开发                              │
│  - 仅编译和测试                            │
└──────────────────────────────────────────┘
```

---

## 🔍 Pipeline配置说明

### test分支特点

```yaml
deploy-test:
  stage: deploy
  only:
    - test
  when: on_success  # 自动触发（前面阶段成功后）
```

**触发条件**：
- ✅ 只在 `test` 分支运行
- ✅ 前面阶段（编译、测试、构建）都成功后自动执行
- ✅ 无需手动点击

### develop/main分支特点

```yaml
deploy-dev:
  only:
    - develop
  when: manual  # 手动触发
```

**触发条件**：
- ⏸️ 需要在Pipeline页面手动点击"Deploy"按钮

---

## 🚀 完整工作流示例

### 场景：开发新功能并发布到测试

```bash
# 1. 创建功能分支
git checkout develop
git checkout -b feature/new-api
# 开发代码...
git add .
git commit -m "feat: 新增API接口"
git push gitlab feature/new-api

# 2. 合并到develop（触发CI，不自动部署）
git checkout develop
git merge feature/new-api
git push gitlab develop
# → Pipeline运行：编译 → 测试 → 构建镜像 → 推送Nexus

# 3. 合并到test（触发CI + 自动部署）
git checkout test
git merge develop
git push gitlab test
# → Pipeline运行：编译 → 测试 → 构建镜像 → 推送Nexus → 自动部署 ✅

# 4. 查看部署结果
# 访问: http://localhost:8001 或 http://test-server:8001
```

---

## 🔧 本地测试（无GitLab）

如果没有GitLab环境，可以用本地脚本模拟：

```bash
# 模拟test分支的CI/CD流程
git checkout test
git merge develop

# 运行本地CI/CD
make ci-cd

# 或
./scripts/ci-cd-local.sh
```

---

## 📋 故障排查

### 问题1：Runner无法访问Nexus

**症状**：`Connection refused: localhost:5000`

**解决**：
```bash
# 使用host.docker.internal替代localhost
NEXUS_REGISTRY: "host.docker.internal:5000"

# 或将Nexus加入gitlab-network
docker network connect gitlab-network collabtask-nexus
```

### 问题2：Runner无法拉取代码

**症状**：`fatal: unable to access 'http://gitlab/...'`

**解决**：
```bash
# 确保Runner和GitLab在同一网络
docker network connect gitlab-network gitlab
docker network connect gitlab-network gitlab-runner
```

### 问题3：Pipeline卡在pending

**症状**：Pipeline显示"pending"不执行

**解决**：
```bash
# 检查Runner状态
docker exec gitlab-runner gitlab-runner verify

# 重启Runner
docker restart gitlab-runner

# 查看Runner日志
docker logs -f gitlab-runner
```

---

## ✅ 验证清单

- [ ] GitLab服务运行中
- [ ] GitLab Runner注册成功
- [ ] 环境变量配置完成
- [ ] 网络配置正确
- [ ] develop分支推送触发编译
- [ ] test分支推送触发自动部署
- [ ] Pipeline所有阶段成功
- [ ] 测试环境服务正常访问

---

## 📚 相关文档

- [CI/CD实战指南](./CI-CD实战指南.md)
- [GitLab CI/CD配置指南](./GitLab-CICD配置指南.md)
- [Docker部署指南](./Docker部署指南.md)

