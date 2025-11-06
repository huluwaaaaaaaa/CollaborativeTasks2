# Nexus Docker Registry 配置指南

## 📋 当前Nexus配置

- **地址**: `localhost:8081`
- **用户名**: `admin`
- **密码**: `123456`
- **Docker仓库**: `docker-local` (hosted)

## 🔧 配置Docker Registry端口

### 第一步：在Nexus中配置Docker端口

1. **登录Nexus**：http://localhost:8081
   - 用户名：`admin`
   - 密码：`123456`

2. **编辑docker-local仓库**：
   - 进入：Settings → Repository → Repositories
   - 点击 `docker-local`
   - 找到 "HTTP" 部分
   - **勾选** "Create an HTTP connector at specified port"
   - **端口设置为**: `8082` 或其他未占用端口
   - 点击 "Save"

3. **重启Nexus**（如果需要）：
```bash
docker restart nexus
```

### 第二步：配置Docker客户端

#### Mac系统

1. 打开 **Docker Desktop**
2. 点击右上角 **齿轮图标** → **Docker Engine**
3. 添加配置：

```json
{
  "insecure-registries": [
    "localhost:8082",
    "localhost:8081"
  ],
  "registry-mirrors": []
}
```

4. 点击 **Apply & Restart**

#### Linux系统

```bash
# 编辑Docker配置
sudo vim /etc/docker/daemon.json

# 添加内容
{
  "insecure-registries": ["localhost:8082", "localhost:8081"]
}

# 重启Docker
sudo systemctl restart docker
```

### 第三步：测试登录

```bash
# 登录Nexus Docker Registry
docker login localhost:8082 -u admin -p 123456

# 或使用8081端口（如果配置了）
docker login localhost:8081 -u admin -p 123456
```

**预期输出**：
```
Login Succeeded
```

## 📦 推送镜像到Nexus

### 1. 构建并推送基础镜像

```bash
cd docker

# 构建基础镜像
docker build -t collabtask-base:1.0 -f base/Dockerfile .

# 打标签（使用正确的端口）
docker tag collabtask-base:1.0 localhost:8082/collabtask-base:1.0
docker tag collabtask-base:1.0 localhost:8082/collabtask-base:latest

# 推送
docker push localhost:8082/collabtask-base:1.0
docker push localhost:8082/collabtask-base:latest
```

### 2. 验证推送成功

在Nexus界面：
- 点击左侧 **Browse** → `docker-local`
- 应该能看到 `collabtask-base` 镜像

## 🔍 常见问题

### Q1: docker login失败

**错误**：
```
Error response from daemon: Get "https://localhost:8082/v2/": http: server gave HTTP response to HTTPS client
```

**解决**：
确保在 `daemon.json` 中添加了 `insecure-registries`，并重启Docker。

### Q2: 推送失败 - 401 Unauthorized

**原因**：用户名密码错误或没有权限

**解决**：
```bash
# 检查用户权限
# 在Nexus中：Settings → Security → Users → admin
# 确保有 nx-admin 角色
```

### Q3: 端口8082不可用

**原因**：端口未配置或被占用

**解决**：
1. 在Nexus中配置Docker HTTP端口（见上面步骤）
2. 或使用8081端口（需要额外配置）

### Q4: docker-local仓库不接受推送

**检查配置**：
1. 确保仓库类型是 `hosted`（不是proxy）
2. 确保 Deployment Policy 是 `Allow redeploy`

## 📝 Nexus Docker Registry URL

根据你的配置，可能是以下之一：

### 方案一：使用单独端口（推荐）
```
Registry URL: localhost:8082
Repository: docker-local
```

### 方案二：使用Repository Path
```
Registry URL: localhost:8081/repository/docker-local
Repository: docker-local
```

## 🎯 确认你的端口

请在Nexus中检查：
1. 点击 `docker-local` 仓库
2. 查看 **HTTP** 部分
3. 确认端口号（如果没有配置，按上面步骤配置）

**确认端口后告诉我，我帮你更新所有配置！** 🚀

或者，你可以先手动添加执行权限：

```bash
chmod +x build-base.sh
./build-base.sh
```
