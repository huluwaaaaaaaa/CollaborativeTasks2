# CollaborativeTasks2 项目文档

## 📚 文档导航

### 🚀 快速开始
- **[快速启动指南](./快速启动指南.md)** - 新人必读，快速上手项目

### ⚙️ 配置相关
- **[配置文件说明](./配置文件说明.md)** - 详细的配置文件说明和使用规则
- **[配置文件整理总结](./配置文件整理总结.md)** - 配置文件整理前后对比

### 🔧 功能说明
- **[Gateway使用说明](./Gateway使用说明.md)** - 网关功能和路由配置
- **[日志配置说明](./日志配置说明.md)** - 统一日志配置说明

### 🐛 问题排查
- **[配置中心问题完整分析](./配置中心问题完整分析.md)** - 🔥 必读！配置中心不生效的根本原因
- **[Nacos配置中心使用指南](./Nacos配置中心使用指南.md)** - 完整的配置中心使用教程
- **[Nacos配置中心问题总结](./Nacos配置中心问题总结.md)** - 历史问题总结

---

## 🏗️ 项目结构

```
CollaborativeTasks2/
├── collabtask-common/              # 公共模块
├── collabtask-dynamic-datasource/  # 动态数据源模块
├── collabtask-admin/               # 管理后台服务
├── collabtask-api/                 # API服务（端口8002）
├── collabtask-gateway/             # Gateway网关服务（端口8001）
└── docs/                           # 项目文档
```

## 🚀 快速开始

### 1. 环境准备
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- Nacos 2.x

### 2. 启动Nacos
```bash
cd nacos/bin
sh startup.sh -m standalone
```

访问：http://localhost:8848/nacos（账号密码：nacos/nacos）

### 3. 配置数据库
编辑 `collabtask-api/src/main/resources/application-dev.yml`
```yaml
spring:
  datasource:
    druid:
      url: jdbc:mysql://localhost:3306/collabtask
      username: root
      password: your_password  # ⚠️ 修改为你的密码
```

### 4. 启动服务
```bash
# 编译项目
mvn clean install

# 启动API服务
cd collabtask-api
mvn spring-boot:run

# 启动Gateway服务（新终端）
cd collabtask-gateway
mvn spring-boot:run
```

### 5. 验证
- Nacos控制台：http://localhost:8848/nacos
- API服务：http://localhost:8002/collabtask-api
- Gateway服务：http://localhost:8001

**详细步骤请查看：[快速启动指南](./快速启动指南.md)**

---

## 📊 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| Nacos | 8848 | 注册中心和控制台 |
| Gateway | 8001 | 网关服务 |
| API | 8002 | API服务 |
| Admin | 8003 | 管理后台（如有） |

## 🔧 技术栈

- **后端框架**：Spring Boot 3.5.4、Spring Cloud 2024.0.0
- **微服务**：Spring Cloud Gateway、Spring Cloud Alibaba 2023.0.3.2
- **数据库**：MySQL 8.0、MyBatis Plus、Druid
- **缓存**：Redis 6.0
- **服务治理**：Nacos（注册中心）
- **文档**：Knife4j
- **日志**：Logback

## 🎯 已实现功能

### Gateway网关
- ✅ 服务路由转发（API服务、Admin服务）
- ✅ Token认证（与API服务交互验证）
- ✅ 跨域配置
- ✅ 负载均衡
- ✅ 认证白名单（登录、注册等接口）

### API服务
- ✅ Token验证接口
- ✅ 服务注册到Nacos
- ✅ 动态数据源
- ✅ Redis缓存
- ✅ 接口文档（Knife4j）

### 整体架构
- ✅ 微服务架构
- ✅ 服务注册与发现
- ✅ 统一网关入口
- ✅ 统一认证鉴权
- ✅ 统一日志管理

## ⚙️ 配置说明

### 配置文件结构
```
src/main/resources/
├── bootstrap.yml           # Nacos注册中心配置
├── application.yml         # 公共配置
├── application-dev.yml     # 开发环境
├── application-test.yml    # 测试环境
└── application-prod.yml    # 生产环境
```

### 配置加载顺序
```
bootstrap.yml → application.yml → application-{profile}.yml
```

### 环境切换
修改 `bootstrap.yml` 中的 `spring.profiles.active`：
- `dev` - 开发环境
- `test` - 测试环境
- `prod` - 生产环境

**详细配置说明请查看：[配置文件说明](./配置文件说明.md)**

## 📝 Nacos使用说明

### 当前使用情况
- ✅ **注册中心** - 服务注册、发现、负载均衡（正常工作）
- ⚠️ **配置中心** - 暂未使用（配置通过本地文件管理）

### 为什么不使用配置中心？
Spring Boot 3.5.4 与 Spring Cloud Alibaba 2023.0.3.2 的配置中心模块存在兼容性问题。

### 影响分析
- ✅ 不影响注册中心功能
- ✅ 不影响任何业务功能
- ⚠️ 配置需要通过本地文件管理（而非远程配置中心）

**详细问题分析请查看：[Nacos配置中心问题总结](./Nacos配置中心问题总结.md)**

## 🔍 常见问题

### Q1: 启动失败怎么办？
**排查步骤**：
1. 检查数据库连接配置
2. 检查Nacos是否启动
3. 检查端口是否被占用
4. 查看日志文件

### Q2: 服务未注册到Nacos？
**解决方案**：
1. 确认Nacos已启动
2. 检查 `bootstrap.yml` 中的Nacos地址
3. 查看启动日志是否有错误

### Q3: Gateway无法转发到API？
**可能原因**：
1. API服务未启动
2. API服务未注册到Nacos
3. Gateway路由配置错误

**详细问题解决请查看：[快速启动指南](./快速启动指南.md)**

## 📂 日志文件位置

```
/Users/zgy/logs/
├── collabtask-api/
│   ├── info.log       # 所有日志
│   └── error.log      # 错误日志
└── collabtask-gateway/
    ├── info.log
    └── error.log
```

### 查看日志
```bash
# 实时查看API日志
tail -f /Users/zgy/logs/collabtask-api/info.log

# 实时查看Gateway日志
tail -f /Users/zgy/logs/collabtask-gateway/info.log
```

## 🛠️ 开发建议

### 推荐启动顺序
1. Nacos
2. API服务
3. Gateway服务

### 调试建议
- 开发调试时建议直接访问API服务（端口8002）
- 测试Gateway功能时通过Gateway访问（端口8001）
- 使用IDE的Debug功能调试代码

### 配置修改
- 修改配置后需要重启服务才能生效
- 建议在开发环境修改和测试配置
- 生产环境配置务必谨慎修改

## 📞 获取帮助

### 文档优先
遇到问题时，请先查阅相关文档：
1. [快速启动指南](./快速启动指南.md)
2. [配置文件说明](./配置文件说明.md)
3. [Gateway使用说明](./Gateway使用说明.md)

### 日志排查
查看日志文件，通常包含详细的错误信息。

### 联系团队
如文档无法解决问题，请联系开发团队。

---

## 📄 文档版本

| 日期 | 版本 | 说明 |
|------|------|------|
| 2025-11-06 | v1.0 | 配置文件整理完成，文档系统建立 |

---

**💡 提示**：
- 新人请从 [快速启动指南](./快速启动指南.md) 开始
- 配置问题请参考 [配置文件说明](./配置文件说明.md)
- 遇到问题先查看日志，再查找相关文档
