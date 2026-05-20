# RedBlade 项目

## 项目概述

RedBlade 是一个基于 Spring Boot 3 的企业级前后端分离基础架构，提供完整的用户认证授权、多租户支持、国际化、数据库初始化等功能。

## 技术栈

### 后端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.5 | 基础框架 |
| Spring Security | 6.x | 安全框架 |
| MyBatis-Plus | 3.5.5 | ORM框架 |
| JWT (jjwt) | 0.12.5 | Token认证 |
| Redis | - | 缓存/会话存储 |
| PostgreSQL | - | 主数据库 |
| Druid | 1.2.23 | 数据库连接池 |
| Knife4j | 4.5.0 | API文档 |
| Hutool | 5.8.26 | 工具类库 |
| MapStruct | 1.5.5 | 对象映射 |
| Lombok | 1.18.30 | 代码简化 |

### 开发环境
- JDK 17+
- Maven 3.6+
- PostgreSQL 12+
- Redis 6+

## 项目结构

```
redblade-server/
├── rb-admin          # 启动与聚合模块 - 配置文件、启动类
├── rb-auth           # 认证授权模块 - JWT、登录、权限
├── rb-common         # 公共模块 - 工具类、常量、异常
├── rb-database       # 数据库模块 - 多数据源、数据权限
├── rb-framework      # 框架模块 - 全局异常、日志、Swagger
├── rb-i18n           # 国际化模块 - 多语言支持
├── rb-init           # 初始化模块 - 建表、初始数据
├── rb-model          # 模型模块 - 业务Model定义
└── rb-system         # 系统模块 - 用户、角色、菜单、部门
```

## 模块说明

### rb-admin
启动与聚合模块，包含：
- `application.yml` 配置文件
- `RedBladeApplication.java` 启动类

### rb-auth
认证授权模块，包含：
- **JWT认证**：`JwtUtils`、`JwtAuthenticationFilter`
- **登录服务**：`AuthService`、`AuthController`
- **验证码**：`CaptchaService`（图形验证码，Base64输出）
- **在线用户**：`OnlineUserService`、`OnlineUserController`
- **权限注解**：`@RequiresPermission`、`@RequiresRole`
- **安全配置**：`SecurityConfig`

### rb-common
公共模块，包含：
- **统一响应**：`R<T>`、`PageResult<T>`
- **常量定义**：`CacheConstants`、`SystemConstants`
- **异常处理**：`BusinessException`
- **工具类**：`MessageHelper`、`MasterDaoHelper`

### rb-database
数据库模块，包含：
- **数据源配置**：`DruidConfig`（Druid连接池监控）
- **多数据库方言**：`PostgresqlDialect`、`OracleDialect`
- **数据权限**：`@DataScope`、`DataScopeInterceptor`
- **租户隔离**：`OrgDataInterceptor`

### rb-framework
框架模块，包含：
- **全局异常处理**：`GlobalExceptionHandler`
- **操作日志**：`OperationLogAspect`
- **API文档**：`Knife4jConfig`
- **JSON配置**：`JacksonConfig`

### rb-i18n
国际化模块，包含：
- **多语言解析**：`HeaderLocaleResolver`
- **国际化工具**：`I18nUtils`
- **类型处理器**：`I18nTextTypeHandler`

### rb-init
初始化模块，包含：
- **表结构初始化**：`TableCreator`、`TableDefinition`
- **数据初始化**：`DataLoader`、`InitRunner`
- **版本管理**：`VersionManager`

### rb-model
模型模块，包含：
- **Model注解**：`@Model`、`@SubModel`、`@ModelMethod`
- **查询参数**：`QueryParams`

### rb-system
系统模块，包含：
- **用户管理**：`SysUser`、`SysUserMapper`
- **角色管理**：`SysRole`、`SysRoleMapper`
- **菜单管理**：`SysMenu`、`SysMenuMapper`
- **部门管理**：`SysDept`、`SysDeptMapper`
- **组织管理**：`SysOrg`、`SysOrgMapper`
- **字典管理**：`SysDictType`、`SysDictData`

## 核心功能

### 1. 用户认证
- 登录/登出接口
- 用户注册接口
- JWT Token 认证
- Token 刷新机制（滑动过期，响应头提醒）
- 图形验证码

### 2. 安全控制
- 登录失败次数限制（5次失败锁定30分钟）
- 密码 Base64 加密存储
- 权限注解 `@RequiresPermission`
- 角色注解 `@RequiresRole`

### 3. 在线用户管理
- 查看在线用户列表
- 强制用户下线
- 批量下线
- 在线用户统计

### 4. 数据权限
- 基于 `@DataScope` 注解的数据权限控制
- 多租户数据隔离（按组织）
- 自定义数据权限范围

### 5. 数据库监控
- Druid 连接池监控
- SQL 监控统计
- 慢SQL日志
- 监控地址：`http://localhost:8080/api/druid/`

## API接口

### 认证接口 `/api/auth`
| 接口 | 方法 | 说明 |
|------|------|------|
| `/login` | POST | 用户登录 |
| `/logout` | POST | 用户登出 |
| `/register` | POST | 用户注册 |
| `/captcha` | GET | 获取验证码 |
| `/refresh` | POST | 刷新Token |

### 在线用户接口 `/api/online`
| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | GET | 在线用户列表 |
| `/{token}` | DELETE | 强制下线 |
| `/batch` | DELETE | 批量下线 |
| `/count` | GET | 在线人数 |

## 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      driver-class-name: org.postgresql.Driver
      url: jdbc:postgresql://localhost:5432/redblade
      username: postgres
      password: xxx
```

### Redis配置
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:
```

### 初始化配置
```yaml
rb:
  init:
    enabled: false          # 是否启用初始化
    mode: create-if-not-exists  # 初始化模式
```

## 构建与运行

### 编译
```bash
cd redblade-server
mvn clean compile
```

### 运行
```bash
cd rb-admin
mvn spring-boot:run
```

### 打包
```bash
mvn clean package -DskipTests
```

## 开发规范

### 代码风格
- 遵循阿里巴巴 Java 开发规范
- 使用 Lombok 简化代码
- 保持代码简洁、可读性强

### 分支管理
- master: 主分支，保持稳定
- feature/*: 功能分支
- fix/*: 修复分支

### 提交规范
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- refactor: 重构
- style: 代码格式

## 缓存Key设计

| Key模式 | 说明 | 过期时间 |
|---------|------|----------|
| `rb:login:token:{token}` | 登录Token | 2小时 |
| `rb:login:fail:{username}` | 登录失败次数 | 30分钟 |
| `rb:user:lock:{username}` | 用户锁定状态 | 30分钟 |
| `rb:captcha:{uuid}` | 验证码 | 5分钟 |
| `rb:user:permission:{userId}` | 用户权限缓存 | 1小时 |
| `rb:user:menu:{userId}` | 用户菜单缓存 | 1小时 |

## 注意事项

1. **密码存储**：使用 Base64 编码存储，生产环境建议使用 BCrypt
2. **Token刷新**：前端需监听 `X-Token-Refresh-Needed` 响应头
3. **数据权限**：查询时需配合 `@DataScope` 注解
4. **多租户**：所有业务表需包含 `org_code` 字段
