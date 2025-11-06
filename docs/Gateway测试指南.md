# Gateway测试指南

## 问题排查：404错误

### 第一步：验证API服务是否正常

**直接访问API服务**（绕过Gateway）：

**Postman配置**：
- Method: `POST`
- URL: `http://localhost:8002/collabtask-api/api/login`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "mobile": "13612345678",
  "password": "admin"
}
```

**预期结果**：
- 如果成功：返回token信息
- 如果失败：检查API服务日志

### 第二步：通过Gateway访问

**只有在第一步成功后，再测试Gateway**：

**Postman配置**：
- Method: `POST`
- URL: `http://localhost:8001/api/login`（注意只有一个/api）
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "mobile": "13612345678",
  "password": "admin"
}
```

## 当前Gateway路径转换

### 配置
```yaml
routes:
  - id: collabtask-api
    uri: lb://collabtask-api
    predicates:
      - Path=/api/**
    filters:
      - RewritePath=/api/(?<segment>.*), /collabtask-api/api/$\{segment}
```

### 转换流程
```
用户请求：/api/login
  ↓ Gateway接收
Gateway匹配：/api/** ✅
  ↓ RewritePath
重写路径：/collabtask-api/api/login
  ↓ LoadBalancer
转发到API：http://10.192.168.112:8002/collabtask-api/api/login
  ↓ API服务
处理请求
```

## 可能的问题

### 问题1：API服务接口路径不对

检查API Controller：
```java
@RestController
@RequestMapping("/api")  // 这个路径
public class ApiLoginController {
    @PostMapping("login")  // 这个路径
```

完整路径：
```
context-path + @RequestMapping + @PostMapping
= /collabtask-api + /api + login
= /collabtask-api/api/login
```

### 问题2：RewritePath语法错误

检查是否是转义符问题：
```yaml
# 可能需要调整为
filters:
  - RewritePath=/api/(.*), /collabtask-api/api/$\{1}
```

### 问题3：服务未注册到Nacos

检查Nacos控制台：
- 访问：http://localhost:8848/nacos
- 服务管理 → 服务列表
- 查看 `collabtask-api` 是否在线
- IP和端口是否正确

## 调试步骤

### 1. 查看Gateway日志

启用TRACE级别日志：
```yaml
logging:
  level:
    org.springframework.cloud.gateway.filter: TRACE
```

### 2. 查看转发的完整URL

在Gateway日志中找：
```
Forwarding request to: http://xxx:8002/xxx
```

### 3. 对比路径

- 期望的路径：`/collabtask-api/api/login`
- 实际转发的路径：？？？

## 可能的解决方案

### 方案一：修改RewritePath

如果当前配置不工作，尝试：
```yaml
filters:
  - RewritePath=/api/(.*), /collabtask-api/api/$\{1}
```

### 方案二：使用SetPath

```yaml
filters:
  - SetPath=/collabtask-api/api/{segment}
```

### 方案三：修改API的context-path

最简单的方案，修改API服务：
```yaml
server:
  servlet:
    context-path: /  # 改为根路径
```

然后Gateway配置：
```yaml
filters:
  - StripPrefix=0  # 不去除前缀，直接转发
```

这样 `/api/login` 直接转发到 `/api/login`。

## 推荐操作

1. 先测试直接访问API服务是否成功
2. 查看Gateway的TRACE日志，找到实际转发的URL
3. 根据日志调整RewritePath配置

---

**请先用Postman直接测试API服务（8002端口），确认接口本身是否正常！**

