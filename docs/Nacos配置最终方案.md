# Nacos配置中心 - 最终解决方案

## 🎯 问题根源

**Spring Boot 3.x 对 Bootstrap 支持有变化**，官方推荐使用 `spring.config.import`，而不是 `bootstrap.yml`。

## ✅ 最终方案

### 配置方式：spring.config.import

在 `application.yml` 中使用新的配置方式：

```yaml
spring:
  config:
    import:
      - optional:nacos:collabtask-api.yaml?group=DEFAULT_GROUP&refreshEnabled=true
      - optional:nacos:common-config.yaml?group=DEFAULT_GROUP&refreshEnabled=true
  
  cloud:
    nacos:
      server-addr: localhost:8848
      username: nacos
      password: nacos
      config:
        namespace: dev
        group: DEFAULT_GROUP
        file-extension: yaml
      discovery:
        namespace: dev
        group: DEFAULT_GROUP
```

### 环境配置文件

所有 `application-{profile}.yml` 已清空，配置全部在Nacos管理。

## 📋 Nacos配置清单

### 命名空间：dev

| Data ID | Group | 说明 | 内容 |
|---------|-------|------|------|
| `collabtask-api.yaml` | DEFAULT_GROUP | API服务配置 | 数据库、Redis配置 |
| `collabtask-gateway.yaml` | DEFAULT_GROUP | Gateway配置 | 路由、认证配置 |
| `common-config.yaml` | DEFAULT_GROUP | 公共配置 | MyBatis、Jackson等 |

### collabtask-api.yaml 示例

```yaml
spring:
  datasource:
    druid:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:3306/collabtask?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&nullCatalogMeansCurrent=true
      username: root
      password: Y9!8nIeRH@163
      initial-size: 10
      max-active: 100
      min-idle: 10
      max-wait: 60000
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
      filter:
        stat:
          log-slow-sql: true
          slow-sql-millis: 1000
          merge-sql: false
        wall:
          config:
            multi-statement-allow: true
  
  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0
      timeout: 6000ms
      lettuce:
        pool:
          max-active: 1000
          max-wait: -1ms
          max-idle: 10
          min-idle: 5
```

### collabtask-gateway.yaml 示例

```yaml
# Gateway环境相关配置（如果需要的话）
logging:
  level:
    io.user.gateway: DEBUG
```

### common-config.yaml 示例

```yaml
# MyBatis Plus公共配置
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  typeAliasesPackage: io.user.entity

# Jackson公共配置
spring:
  jackson:
    time-zone: GMT+8
    date-format: yyyy-MM-dd HH:mm:ss
```

## 🚀 启动验证

### 1. 确认Nacos配置

访问：http://localhost:8848/nacos

检查以下配置是否存在：
- [ ] `collabtask-api.yaml` (dev命名空间)
- [ ] `collabtask-gateway.yaml` (dev命名空间)
- [ ] `common-config.yaml` (dev命名空间)

### 2. 启动API服务

```bash
cd collabtask-api
mvn spring-boot:run
```

### 3. 检查启动日志

**成功标志**：
```log
Located property source: CompositePropertySource {name='nacos'}
Nacos config refreshed successfully
```

**数据库连接成功**：
```log
{dataSource-1} inited
```

### 4. 验证配置来源

如果你在 `application-dev.yml` 中**没有**配置数据库，但服务能正常连接数据库，说明配置是从Nacos加载的！✅

## 🔍 故障排查

### 问题1：启动报错 "DataSource URL is not set"

**原因**：Nacos配置未加载

**排查步骤**：
1. 检查Nacos是否启动：`http://localhost:8848/nacos`
2. 检查配置是否存在：dev命名空间 → collabtask-api.yaml
3. 检查配置格式：必须有 `spring:` 前缀
4. 查看启动日志是否有 `Located property source`

### 问题2：日志中没有 "Located property source"

**原因**：配置加载失败

**解决方案**：
```yaml
# 检查 application.yml 中的配置
spring:
  config:
    import:
      - optional:nacos:collabtask-api.yaml?group=DEFAULT_GROUP&refreshEnabled=true
```

确认：
- Data ID 是否正确（包含.yaml后缀）
- Group 是否正确（DEFAULT_GROUP）
- Namespace 是否正确（在spring.cloud.nacos.config.namespace中配置）

### 问题3：Nacos连接失败

**原因**：Nacos地址或认证错误

**检查**：
```yaml
spring:
  cloud:
    nacos:
      server-addr: localhost:8848  # 检查地址
      username: nacos              # 检查用户名
      password: nacos              # 检查密码
```

## 📊 配置对比

### 旧方式（bootstrap.yml）vs 新方式（spring.config.import）

| 特性 | bootstrap.yml | spring.config.import |
|------|--------------|---------------------|
| Spring Boot 3.x支持 | ⚠️ 需要额外依赖 | ✅ 官方推荐 |
| 配置简单性 | ❌ 两个文件 | ✅ 统一在application.yml |
| 加载优先级 | 最早 | 早期 |
| 动态刷新 | ✅ | ✅ |
| 维护性 | 一般 | 好 |

## 💡 最佳实践

### 1. 配置分层

```
common-config.yaml（公共配置）
    ↓
service.yaml（服务专属配置）
    ↓
application-{profile}.yml（环境特定配置，可选）
```

### 2. 敏感信息

生产环境使用环境变量：
```bash
export NACOS_HOST=10.30.21.54
export NACOS_USERNAME=prod-user
export NACOS_PASSWORD=your_password
java -jar your-service.jar
```

### 3. 配置刷新

Bean上添加 `@RefreshScope`：
```java
@RefreshScope
@Component
public class MyConfig {
    @Value("${my.property}")
    private String myProperty;
}
```

修改Nacos配置后，自动刷新，无需重启！

## 🎉 总结

### 成功标准

- ✅ 服务正常启动
- ✅ 日志显示 "Located property source"
- ✅ 数据库连接成功
- ✅ application-dev.yml 为空或很少配置
- ✅ Nacos控制台能看到配置

### 核心要点

1. **使用 spring.config.import**（Spring Boot 3.x新方式）
2. **Data ID 包含后缀**（如 collabtask-api.yaml）
3. **配置有 spring: 前缀**（Nacos中的配置格式）
4. **本地环境配置清空**（确保从Nacos加载）

---

**配置中心现在应该完全可用了！** 🚀

