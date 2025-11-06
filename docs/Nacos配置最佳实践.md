# Nacos配置最佳实践

## 核心要点总结

基于生产环境经验，总结Nacos配置的最佳实践。

## 一、配置结构优化

### 1. 引用配置，避免重复

❌ **不好的做法**：重复配置
```yaml
nacos:
  discovery:
    server-addr: localhost:8848
    namespace: dev
  config:
    server-addr: localhost:8848  # 重复
    namespace: dev               # 重复
```

✅ **好的做法**：引用配置
```yaml
nacos:
  server-addr: localhost:8848    # 公共配置
  discovery:
    namespace: dev
  config:
    server-addr: ${spring.cloud.nacos.server-addr}    # 引用
    namespace: ${spring.cloud.nacos.discovery.namespace}  # 引用
```

**优势**：
- 避免重复
- 统一维护
- 减少出错

### 2. 使用环境变量

✅ **推荐做法**：
```yaml
nacos:
  server-addr: ${NACOS_HOST:localhost}:${NACOS_PORT:8848}
  username: ${NACOS_USERNAME:nacos}
  password: ${NACOS_PASSWORD:nacos}
  discovery:
    namespace: ${NACOS_NAMESPACE:dev}
    group: ${NACOS_GROUP:DEFAULT_GROUP}
```

**优势**：
- 不同环境无需修改配置文件
- 敏感信息（密码）不写在代码里
- 容器化部署更方便

**使用方式**：
```bash
# 启动时指定环境变量
export NACOS_HOST=10.30.21.54
export NACOS_PORT=8848
export NACOS_NAMESPACE=prod
export NACOS_USERNAME=prod-user
export NACOS_PASSWORD=your_password
mvn spring-boot:run
```

### 3. 共享配置管理

使用 `extension-configs` 加载公共配置：

```yaml
config:
  extension-configs:
    # 公共配置
    - data-id: common.yaml
      group: DEFAULT_GROUP
      refresh: true
    
    # 数据库配置（多个服务共享）
    - data-id: datasource-common.yaml
      group: DEFAULT_GROUP
      refresh: true
    
    # Redis配置（多个服务共享）
    - data-id: redis-common.yaml
      group: DEFAULT_GROUP
      refresh: true
```

**配置加载顺序**：
```
1. extension-configs（共享配置）
   ↓
2. ${spring.application.name}.yaml（主配置）
   ↓
3. ${spring.application.name}-${profile}.yaml（环境配置）
```

后面的会覆盖前面的。

## 二、完整配置模板

### 开发环境（bootstrap.yml）

```yaml
spring:
  application:
    name: your-service-name
  profiles:
    active: dev
  cloud:
    nacos:
      # Nacos服务地址
      server-addr: ${NACOS_HOST:localhost}:${NACOS_PORT:8848}
      
      # Nacos认证
      username: ${NACOS_USERNAME:nacos}
      password: ${NACOS_PASSWORD:nacos}
      
      # 注册中心
      discovery:
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        ephemeral: true
        metadata:
          management:
            context-path: ${server.servlet.context-path:}/actuator
      
      # 配置中心
      config:
        server-addr: ${spring.cloud.nacos.server-addr}
        namespace: ${spring.cloud.nacos.discovery.namespace}
        group: ${spring.cloud.nacos.discovery.group}
        file-extension: yaml
        refresh-enabled: true
        name: ${spring.application.name}
        
        # 共享配置
        extension-configs:
          - data-id: common.yaml
            group: ${spring.cloud.nacos.config.group}
            refresh: true

# DEBUG日志
logging:
  level:
    com.alibaba.cloud.nacos.config: DEBUG
    com.alibaba.nacos.client.config: DEBUG
```

### 生产环境（环境变量）

```bash
# 启动脚本 start.sh
export NACOS_HOST=10.30.21.54
export NACOS_PORT=8848
export NACOS_NAMESPACE=prod
export NACOS_GROUP=PROD_GROUP
export NACOS_USERNAME=prod-service
export NACOS_PASSWORD=your_strong_password

java -jar your-service.jar
```

### Docker部署（docker-compose.yml）

```yaml
version: '3'
services:
  your-service:
    image: your-service:latest
    environment:
      - NACOS_HOST=nacos-server
      - NACOS_PORT=8848
      - NACOS_NAMESPACE=prod
      - NACOS_GROUP=PROD_GROUP
      - NACOS_USERNAME=prod-service
      - NACOS_PASSWORD=${NACOS_PASSWORD}  # 从.env文件读取
    depends_on:
      - nacos-server
```

## 三、Nacos配置文件组织

### 方案一：单一配置文件（简单项目）

```
命名空间：dev
├── collabtask-api.yaml      # API服务所有配置
└── collabtask-gateway.yaml  # Gateway服务所有配置
```

**适用场景**：
- 小型项目
- 配置不多
- 环境差异小

### 方案二：分层配置（推荐）

```
命名空间：dev
├── common.yaml                    # 所有服务共享
├── datasource-common.yaml         # 数据库共享配置
├── redis-common.yaml              # Redis共享配置
├── collabtask-api.yaml            # API服务专属配置
├── collabtask-api-dev.yaml        # API开发环境
├── collabtask-api-prod.yaml       # API生产环境
├── collabtask-gateway.yaml        # Gateway服务专属配置
├── collabtask-gateway-dev.yaml    # Gateway开发环境
└── collabtask-gateway-prod.yaml   # Gateway生产环境
```

**适用场景**：
- 中大型项目
- 多个微服务
- 有很多共享配置
- 环境差异大

### 方案三：多命名空间隔离（企业级）

```
命名空间：dev
├── common.yaml
├── collabtask-api.yaml
└── collabtask-gateway.yaml

命名空间：test
├── common.yaml
├── collabtask-api.yaml
└── collabtask-gateway.yaml

命名空间：prod
├── common.yaml
├── collabtask-api.yaml
└── collabtask-gateway.yaml
```

**适用场景**：
- 企业级项目
- 严格的环境隔离
- 多租户系统

## 四、配置内容规划

### common.yaml（公共配置）

```yaml
# MyBatis Plus配置
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  typeAliasesPackage: io.user.entity
  global-config:
    db-config:
      id-type: ASSIGN_ID
    banner: false

# Jackson配置
spring:
  jackson:
    time-zone: GMT+8
    date-format: yyyy-MM-dd HH:mm:ss

# 日志配置
logging:
  level:
    io.user: INFO
```

### datasource-common.yaml（数据库共享）

```yaml
spring:
  datasource:
    druid:
      initial-size: 10
      max-active: 100
      min-idle: 10
      max-wait: 60000
      pool-prepared-statements: true
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      filter:
        stat:
          log-slow-sql: true
          slow-sql-millis: 1000
```

### collabtask-api.yaml（服务专属）

```yaml
spring:
  datasource:
    druid:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:3306/collabtask
      username: root
      password: your_password

  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0
```

## 五、安全最佳实践

### 1. 敏感信息加密

Nacos支持配置加密：

**在Nacos配置中**：
```yaml
spring:
  datasource:
    password: cipher(AQIDBAUGBwgJAA==)  # 加密后的密码
```

**解密配置**：
```yaml
# bootstrap.yml
spring:
  cloud:
    nacos:
      config:
        # 开启配置加密
        enable-remote-sync-config: true
```

### 2. 权限控制

- **开发环境**：只读权限
- **测试环境**：读写权限（需审批）
- **生产环境**：严格控制，只有运维有权限

### 3. 配置审计

Nacos自带配置历史记录：
- 谁修改了配置
- 什么时间修改
- 修改了什么内容
- 可以回滚到任意版本

## 六、实施步骤

### 第一步：本地配置迁移

1. **识别公共配置**
   - 哪些配置多个服务都在用？
   - 哪些配置不同环境不一样？

2. **规划配置层次**
   ```
   common.yaml         # 所有服务都用
   ↓
   service.yaml        # 服务专属
   ↓
   service-env.yaml    # 环境专属
   ```

3. **逐步迁移**
   - 先迁移非敏感配置
   - 验证正常后迁移敏感配置
   - 最后删除本地配置

### 第二步：配置Nacos

1. **创建命名空间**
   - dev（开发）
   - test（测试）
   - prod（生产）

2. **创建配置文件**
   - 按规划的层次创建
   - 注意Data ID的命名规则

3. **配置权限**
   - 设置用户权限
   - 设置配置访问权限

### 第三步：应用配置

1. **修改bootstrap.yml**
   - 添加config配置
   - 配置extension-configs

2. **设置环境变量**
   - NACOS_HOST
   - NACOS_USERNAME
   - NACOS_PASSWORD

3. **验证启动**
   - 查看日志确认配置加载
   - 测试所有功能

## 七、常见问题

### Q1: extension-configs和主配置的区别？

**extension-configs**：
- 用于加载额外的配置文件
- 通常是公共配置
- 优先级低，会被主配置覆盖

**主配置**（name指定）：
- 服务专属配置
- 优先级高

### Q2: 如何调试配置加载问题？

开启DEBUG日志：
```yaml
logging:
  level:
    com.alibaba.cloud.nacos.config: DEBUG
    com.alibaba.nacos.client.config: DEBUG
```

查看日志：
```
Located property source: [BootstrapPropertySource {name='bootstrapProperties-xxx.yaml'}]
```

### Q3: 配置更新了但没生效？

**检查清单**：
- [ ] `refresh-enabled: true` 开启了吗？
- [ ] Bean上有 `@RefreshScope` 注解吗？
- [ ] 在Nacos点了"发布"了吗？
- [ ] 查看日志有配置刷新记录吗？

### Q4: 生产环境如何安全管理密码？

**方案一**：环境变量
```bash
export NACOS_PASSWORD=xxx
```

**方案二**：配置加密
```yaml
password: cipher(encrypted_value)
```

**方案三**：密钥管理系统
- Vault
- AWS Secrets Manager
- Azure Key Vault

## 八、性能优化

### 1. 配置缓存

Nacos客户端会缓存配置：
```yaml
config:
  # 缓存目录
  cache-dir: ${user.home}/nacos/config
```

即使Nacos服务不可用，应用也能从缓存启动。

### 2. 长轮询优化

```yaml
config:
  # 长轮询超时时间（毫秒）
  config-long-poll-timeout: 30000
  # 重试时间（毫秒）
  config-retry-time: 2000
```

### 3. 批量获取

使用 `extension-configs` 可以一次加载多个配置，比分开加载更高效。

## 九、监控告警

### 1. 配置变更监控

监控哪些配置被修改：
- 配置Data ID
- 修改时间
- 修改人
- 修改内容

### 2. 配置加载失败告警

监控应用启动时：
- 配置是否成功加载
- 是否有加载失败的配置
- 是否使用了本地缓存

### 3. 配置推送监控

监控配置推送：
- 推送成功率
- 推送延迟
- 客户端响应

## 十、总结

### 核心原则

1. **DRY原则**：不重复配置，使用引用
2. **分层原则**：公共、专属、环境配置分开
3. **安全原则**：敏感信息加密，权限控制
4. **灵活原则**：使用环境变量，适应不同环境

### 配置模板

```yaml
spring:
  cloud:
    nacos:
      server-addr: ${NACOS_HOST:localhost}:${NACOS_PORT:8848}
      username: ${NACOS_USERNAME:nacos}
      password: ${NACOS_PASSWORD:nacos}
      discovery:
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
      config:
        server-addr: ${spring.cloud.nacos.server-addr}
        namespace: ${spring.cloud.nacos.discovery.namespace}
        group: ${spring.cloud.nacos.discovery.group}
        file-extension: yaml
        refresh-enabled: true
        extension-configs:
          - data-id: common.yaml
            group: ${spring.cloud.nacos.config.group}
            refresh: true
```

### 关键要点

- ✅ Config和Discovery都要配置
- ✅ 使用引用避免重复
- ✅ 使用环境变量增加灵活性
- ✅ 使用extension-configs管理公共配置
- ✅ 开启DEBUG日志便于调试
- ✅ 生产环境必须加认证
- ✅ 敏感信息要加密

---

**基于生产环境实践总结，助你少走弯路！** 🚀

