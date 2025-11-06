# Nacos配置中心使用指南

## 问题诊断

### 之前为什么不生效？

**根本原因**：`bootstrap.yml` 中只配置了 `discovery`（注册中心），**没有配置 `config`（配置中心）**！

#### 错误配置（之前）
```yaml
spring:
  cloud:
    nacos:
      server-addr: localhost:8848
      discovery:          # ✅ 有这个（所以注册中心工作）
        namespace: dev
        group: DEFAULT_GROUP
      # ❌ 缺少config配置（所以配置中心不工作）
```

#### 正确配置（现在）
```yaml
spring:
  cloud:
    nacos:
      server-addr: localhost:8848
      config:             # ✅ 新增配置中心配置
        namespace: dev
        group: DEFAULT_GROUP
        file-extension: yaml
        refresh-enabled: true
      discovery:          # ✅ 保留注册中心配置
        namespace: dev
        group: DEFAULT_GROUP
```

## 完整配置步骤

### 第一步：在Nacos中创建配置

#### 1. 登录Nacos控制台
访问：http://localhost:8848/nacos  
账号密码：`nacos` / `nacos`

#### 2. 创建命名空间
1. 点击左侧菜单"命名空间"
2. 点击"新建命名空间"
3. 填写信息：
   - 命名空间ID：`dev`
   - 命名空间名：`开发环境`
4. 点击"确定"

#### 3. 创建API服务配置

**配置位置**：配置管理 → 配置列表

**配置信息**：
- **命名空间**：`dev`（选择刚创建的）
- **Data ID**：`collabtask-api.yaml`（注意后缀是.yaml）
- **Group**：`DEFAULT_GROUP`
- **配置格式**：`YAML`

**配置内容**：
```yaml
spring:
  # 数据源配置
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
  
  # Redis配置
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

#### 4. 创建Gateway服务配置

**配置信息**：
- **命名空间**：`dev`
- **Data ID**：`collabtask-gateway.yaml`
- **Group**：`DEFAULT_GROUP`
- **配置格式**：`YAML`

**配置内容**：
```yaml
# Gateway网关配置（可以为空，也可以添加环境相关配置）
logging:
  level:
    io.user.gateway: DEBUG
```

### 第二步：删除本地环境配置

**重要**：为了确保从Nacos加载配置，需要清空本地的环境配置文件。

#### API服务
编辑 `collabtask-api/src/main/resources/application-dev.yml`，清空内容：
```yaml
# 开发环境配置已迁移到Nacos配置中心
# 配置位置：Nacos控制台 -> dev命名空间 -> collabtask-api.yaml
```

#### Gateway服务
编辑 `collabtask-gateway/src/main/resources/application-dev.yml`，清空内容：
```yaml
# 开发环境配置已迁移到Nacos配置中心
# 配置位置：Nacos控制台 -> dev命名空间 -> collabtask-gateway.yaml
```

### 第三步：验证配置

#### 1. 重启API服务
```bash
cd collabtask-api
mvn spring-boot:run
```

#### 2. 查看启动日志

**成功标志**：
```
Located property source: [BootstrapPropertySource {name='bootstrapProperties-collabtask-api.yaml,DEFAULT_GROUP'}]
```

如果看到这行日志，说明配置中心已成功加载！

#### 3. 验证配置加载

查看日志中是否有：
```
[main] DEBUG com.alibaba.nacos.client.config.impl.ClientWorker - [fixed-localhost_8848] [polling-resp] config changed. dataId=collabtask-api.yaml, group=DEFAULT_GROUP
```

## 配置加载机制

### Data ID命名规则

Nacos会按以下顺序加载配置（后者覆盖前者）：

1. **共享配置**（可选）
   - Data ID：`common.yaml`

2. **应用配置**（主要）
   - Data ID：`${spring.application.name}.yaml`
   - 示例：`collabtask-api.yaml`

3. **环境配置**（可选）
   - Data ID：`${spring.application.name}-${profile}.yaml`
   - 示例：`collabtask-api-dev.yaml`

### 推荐方案

**方案一：单一配置文件（推荐）**
- Data ID：`collabtask-api.yaml`
- 包含所有环境配置
- 简单直观

**方案二：多环境配置**
- 共享配置：`collabtask-api.yaml`（公共配置）
- 开发环境：`collabtask-api-dev.yaml`（开发特定）
- 测试环境：`collabtask-api-test.yaml`
- 生产环境：`collabtask-api-prod.yaml`

## 配置优先级

```
Nacos远程配置 > 本地application-{profile}.yml > 本地application.yml > bootstrap.yml
```

## 动态刷新配置

### 1. 在Bean上添加注解
```java
@RefreshScope
@Component
public class MyConfig {
    @Value("${my.property}")
    private String myProperty;
}
```

### 2. 在Nacos控制台修改配置
修改配置后点击"发布"

### 3. 验证刷新
配置会自动刷新到应用，无需重启！

## 常见问题排查

### Q1: 配置没有加载？

**排查步骤**：
1. 检查Nacos控制台配置是否存在
2. 检查命名空间是否正确（dev）
3. 检查Data ID是否正确（collabtask-api.yaml）
4. 检查Group是否正确（DEFAULT_GROUP）
5. 查看启动日志是否有 `Located property source`

### Q2: 启动报错"DataSource URL is not set"？

**可能原因**：
1. Nacos配置中没有数据库配置
2. 配置格式不对（需要 `spring:` 前缀）
3. 本地配置也被清空了

**解决方案**：
```yaml
# 在Nacos配置中，必须有spring前缀：
spring:
  datasource:
    druid:
      url: jdbc:mysql://...
```

### Q3: 配置加载了，但数据不对？

**检查配置优先级**：
- 本地 `application-dev.yml` 会覆盖Nacos配置
- 建议清空本地环境配置文件

### Q4: 如何确认从哪里加载的配置？

**开启DEBUG日志**：
```yaml
# bootstrap.yml
logging:
  level:
    com.alibaba.cloud.nacos.config: DEBUG
```

查看日志中的 `Located property source` 信息。

## 最佳实践

### 1. 配置分类

**敏感配置**（放Nacos）：
- 数据库密码
- Redis密码
- API密钥

**公共配置**（放本地）：
- 服务端口
- MyBatis配置
- Jackson配置

### 2. 环境隔离

- 开发环境：`dev`命名空间
- 测试环境：`test`命名空间
- 生产环境：`prod`命名空间

### 3. 配置备份

定期在Nacos控制台导出配置备份：
1. 进入配置列表
2. 勾选配置
3. 点击"导出"

### 4. 权限管理

生产环境建议：
- 开发人员只读权限
- 运维人员读写权限
- 配置变更需审批

## 配置模板

### API服务完整配置模板

```yaml
spring:
  # 数据源配置
  datasource:
    druid:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:3306/your_database
      username: your_username
      password: your_password
      initial-size: 10
      max-active: 100
      min-idle: 10
      max-wait: 60000
  
  # Redis配置
  data:
    redis:
      host: localhost
      port: 6379
      password: 
      database: 0

# 自定义业务配置
collabtask:
  redis:
    open: true
```

### Gateway服务完整配置模板

```yaml
# Gateway特定配置
spring:
  cloud:
    gateway:
      routes:
        - id: api-dynamic
          uri: lb://collabtask-api
          predicates:
            - Path=/dynamic/**

# 认证配置
gateway:
  auth:
    enabled: true
    skip-urls:
      - /api/public/**
```

## 测试清单

启用配置中心后，需要验证以下内容：

- [ ] Nacos配置已创建
- [ ] 服务能正常启动
- [ ] 数据库连接正常
- [ ] Redis连接正常
- [ ] 日志显示 `Located property source`
- [ ] 修改Nacos配置能动态刷新
- [ ] 所有接口功能正常

## 版本兼容性

### 当前环境
- Spring Boot：3.5.4
- Spring Cloud：2024.0.0
- Spring Cloud Alibaba：2023.0.3.2
- Nacos Server：2.x

### 配置中心功能支持
- ✅ 基本配置加载
- ✅ 动态刷新
- ✅ 多环境配置
- ✅ 配置加密（需Nacos配置）
- ⚠️ 部分高级特性可能不稳定

## 总结

### 配置中心不生效的原因

1. **根本原因**：`bootstrap.yml` 缺少 `spring.cloud.nacos.config` 配置
2. **次要原因**：Nacos中没有创建对应的配置文件
3. **其他原因**：Data ID命名不对、命名空间不对

### 解决方案

1. ✅ 在 `bootstrap.yml` 添加 `config` 配置
2. ✅ 在Nacos创建对应的配置文件
3. ✅ 清空本地环境配置（避免冲突）
4. ✅ 开启DEBUG日志验证

### 配置中心的优势

- 🎯 集中管理配置
- 🔄 动态刷新，无需重启
- 🔒 配置加密，更安全
- 📝 版本管理，可回滚
- 🏢 多环境隔离

---

**现在配置中心应该可以正常工作了！**

