# Docker镜像构建说明

## 📦 镜像分层架构

```
┌─────────────────────────────────────┐
│  CentOS 7 官方镜像 (Layer 0)         │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│  collabtask-base:1.0 (Layer 1)      │
│  - OpenJDK 17                       │
│  - 常用工具（curl、wget、vim等）     │
│  - 时区设置（Asia/Shanghai）        │
│  - 默认JVM参数                      │
└──────────────┬──────────────────────┘
               │
        ┌──────┴──────┐
        │             │
┌───────▼─────┐  ┌────▼──────────┐
│collabtask-  │  │ collabtask-   │
│  api:1.0    │  │ gateway:1.0   │
│ (Layer 2)   │  │  (Layer 2)    │
│ - JAR包     │  │  - JAR包      │
└─────────────┘  └───────────────┘
```

## 🚀 构建顺序

### 第一步：构建基础镜像（一次性）

```bash
# 进入docker目录
cd docker

# 构建基础镜像
chmod +x build-base.sh
./build-base.sh

# 或手动构建
docker build -t collabtask-base:1.0 -f base/Dockerfile .
```

**这个步骤只需要执行一次**，以后除非需要升级JDK或添加工具，否则不用重新构建。

### 第二步：构建应用镜像

```bash
# 返回项目根目录
cd ..

# 构建API镜像
docker build -f collabtask-api/Dockerfile -t collabtask-api:1.0 .

# 构建Gateway镜像
docker build -f collabtask-gateway/Dockerfile -t collabtask-gateway:1.0 .
```

## 📊 镜像大小对比

### 方案一：每次都安装JDK（旧方案）
```
collabtask-api:    800MB  (CentOS + JDK + JAR)
collabtask-gateway: 750MB  (CentOS + JDK + JAR)
总计: 1.55GB
```

### 方案二：基础镜像分层（新方案）
```
collabtask-base:    650MB  (CentOS + JDK + 工具) ← 只下载一次
collabtask-api:     150MB  (只有JAR + 配置)
collabtask-gateway: 100MB  (只有JAR + 配置)
总计: 900MB (实际占用，因为base层共享)
```

**节省空间 + 构建速度快10倍！**

## 🔧 推送到Nexus

### 1. 推送基础镜像

```bash
# 登录Nexus
docker login localhost:8082 -u deployer -p password

# 打标签
docker tag collabtask-base:1.0 localhost:8082/collabtask-base:1.0
docker tag collabtask-base:1.0 localhost:8082/collabtask-base:latest

# 推送
docker push localhost:8082/collabtask-base:1.0
docker push localhost:8082/collabtask-base:latest
```

### 2. 推送应用镜像

```bash
docker tag collabtask-api:1.0 localhost:8082/collabtask-api:1.0
docker push localhost:8082/collabtask-api:1.0

docker tag collabtask-gateway:1.0 localhost:8082/collabtask-gateway:1.0
docker push localhost:8082/collabtask-gateway:1.0
```

## 📝 使用Nexus基础镜像

### 修改Dockerfile（如果基础镜像在Nexus）

```dockerfile
# 从Nexus拉取基础镜像
FROM localhost:8082/collabtask-base:1.0

# 复制JAR包
COPY --from=builder /build/collabtask-api/target/*.jar /app/app.jar

# 启动
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
```

## 🎯 团队协作

### 基础镜像维护

**谁维护**：运维团队或架构师

**更新时机**：
- JDK版本升级
- 添加新的工具
- 安全补丁更新

**更新流程**：
1. 修改 `docker/base/Dockerfile`
2. 重新构建基础镜像
3. 推送到Nexus
4. 通知所有项目更新基础镜像版本

### 应用镜像维护

**谁维护**：项目开发团队

**构建频率**：每次代码变更

**依赖**：依赖基础镜像（FROM collabtask-base:1.0）

## 💡 最佳实践

### 1. 版本管理

```bash
# 基础镜像版本规则
collabtask-base:1.0     # 主版本号
collabtask-base:1.0.1   # 小版本号（bug修复）
collabtask-base:1.1.0   # 次版本号（新功能）

# 应用镜像版本规则
collabtask-api:v1.0.0           # 发布版本
collabtask-api:20251106-abc123  # 日期+commit
collabtask-api:latest           # 最新版本
```

### 2. 构建缓存

```bash
# 使用BuildKit加速构建
export DOCKER_BUILDKIT=1

# 构建时使用缓存
docker build --cache-from collabtask-base:latest \
  -t collabtask-api:1.0 .
```

### 3. 多阶段优化

- ✅ 构建阶段使用完整Maven镜像
- ✅ 运行阶段使用精简基础镜像
- ✅ 只复制必要的JAR包

## 🔍 镜像验证

### 查看镜像信息

```bash
# 查看镜像
docker images | grep collabtask

# 查看镜像历史
docker history collabtask-base:1.0

# 查看镜像详情
docker inspect collabtask-base:1.0
```

### 测试基础镜像

```bash
# 启动基础镜像
docker run -it --rm collabtask-base:1.0 bash

# 测试Java
java -version

# 测试工具
curl --version
wget --version
```

## 📋 完整构建流程

```bash
# 1. 构建基础镜像（只需一次）
cd docker
./build-base.sh

# 2. 构建应用镜像
cd ..
docker build -f collabtask-api/Dockerfile -t collabtask-api:1.0 .
docker build -f collabtask-gateway/Dockerfile -t collabtask-gateway:1.0 .

# 3. 推送到Nexus
docker tag collabtask-api:1.0 localhost:8082/collabtask-api:1.0
docker push localhost:8082/collabtask-api:1.0

# 4. 从Nexus部署
export NEXUS_REGISTRY=localhost:8082
export IMAGE_TAG=1.0
docker-compose -f docker-compose-nexus.yml up -d
```

## ⏱️ 构建时间对比

| 方案 | 首次构建 | 后续构建 | 说明 |
|------|---------|---------|------|
| 单层镜像 | ~10分钟 | ~10分钟 | 每次都安装JDK |
| 分层镜像 | ~12分钟 | **~2分钟** | 基础镜像缓存 |

**节省80%的构建时间！**

---

**这个方案更符合企业实践，基础镜像可以给多个项目复用！** 🎯

