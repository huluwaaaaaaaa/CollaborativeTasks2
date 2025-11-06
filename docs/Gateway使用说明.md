# Gateway使用说明

## 项目介绍

CollabTask Gateway是API网关服务，提供统一的路由转发和认证功能。

## 主要功能

### 第一版：基础路由转发

- ✅ 服务发现与路由
- ✅ 负载均衡
- ✅ 跨域处理
- ✅ 请求日志

### 第二版：Token认证

- ✅ Token验证（通过HTTP调用API服务）
- ✅ 白名单配置
- ✅ 用户信息传递

## 路由规则

| 路径前缀 | 目标服务 | 说明 |
|---------|---------|------|
| /api/** | collabtask-api | API服务 |
| /admin/** | collabtask-admin | 管理服务 |

**示例**：
- 请求：`http://localhost:8000/api/user/info`
- 转发到：`http://collabtask-api/collabtask-api/user/info`

## 开启Token认证

### 1. 修改Nacos配置

将 `collabtask-gateway.yaml` 中的配置改为：

```yaml
gateway:
  auth:
    enabled: true  # 开启认证
```

### 2. 重启Gateway

Gateway会自动从Nacos拉取新配置。

### 3. 使用Token

```bash
# 1. 登录获取token
curl -X POST http://localhost:8000/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}'

# 2. 使用token访问
curl http://localhost:8000/api/user/info \
  -H "token: your_token_here"
```

## 白名单配置

以下路径无需token即可访问：

- `/api/login` - 登录
- `/api/register` - 注册
- `/admin/login` - 管理员登录
- `/actuator/**` - 健康检查

## 监控端点

```bash
# 健康检查
curl http://localhost:8000/actuator/health

# 查看路由信息
curl http://localhost:8000/actuator/gateway/routes
```

## 日志查看

```bash
# 查看Gateway日志
tail -f logs/gateway/gateway.log

# 查看错误日志
tail -f logs/gateway/error.log
```

## 注意事项

1. Gateway基于WebFlux，使用响应式编程
2. Token验证依赖API服务，确保API服务正常运行
3. 生产环境建议启用认证功能

