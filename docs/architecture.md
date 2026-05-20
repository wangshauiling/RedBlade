# RedBlade 项目架构设计文档

## 文档版本记录

| 版本号 | 修改日期 | 修改人 | 修改内容 |
|--------|----------|--------|----------|
| v1.0.0 | 2026-05-20 | 王帅令 | 初始版本，创建项目架构设计文档 |
| v1.1.0 | 2026-05-20 | 王帅令 | 新增：在线用户管理架构、Druid数据库监控、缓存设计、安全最佳实践、监控运维章节；补充：Token刷新机制、登录失败限制、密码安全策略、认证接口完整列表 |

---

## 一、项目概述

**RedBlade** 是一个企业级前后端分离基础架构，基于 Spring Boot 3.2.5 构建，采用 Java 17，旨在提供开箱即用的企业级应用开发框架。

### 核心特性

- **多组织数据隔离**：基于 `org_code` 的多租户架构
- **声明式数据库初始化**：通过注解定义表结构和初始数据
- **Model-Driven 开发模式**：统一的数据模型抽象层
- **国际化支持**：内置多语言文本处理机制
- **细粒度数据权限**：支持组织、部门、个人多级数据权限
- **完善的认证安全**：JWT Token、登录限制、在线用户管理
- **数据库监控**：Druid 连接池监控与 SQL 分析

---

## 二、技术栈

| 类别 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 核心框架 | Spring Boot | 3.2.5 | 基础框架 |
| 安全框架 | Spring Security | 6.x | 认证授权 |
| 持久层 | MyBatis-Plus | 3.5.5 | ORM框架 |
| 数据库 | PostgreSQL | 42.7.3 | 主数据库 |
| 连接池 | Druid | 1.2.23 | 数据库连接池与监控 |
| 缓存 | Redis | - | 缓存/会话存储 |
| 认证 | JWT (jjwt) | 0.12.5 | Token认证 |
| 工具类 | Hutool | 5.8.26 | 工具类库 |
| 对象映射 | MapStruct | 1.5.5 | 对象映射 |
| API文档 | Knife4j (OpenAPI 3) | 4.5.0 | 接口文档 |
| 代码简化 | Lombok | 1.18.30 | 代码简化 |

### 开发环境要求

- JDK 17+
- Maven 3.6+
- PostgreSQL 12+
- Redis 6+

---

## 三、模块架构

### 3.1 模块结构

```
redblade-server/
├── rb-admin          # 启动模块（入口）- 配置文件、启动类
├── rb-framework      # 框架核心 - 全局异常、日志、Swagger
├── rb-auth           # 认证授权模块 - JWT、登录、权限、在线用户
├── rb-system         # 系统管理模块 - 用户、角色、菜单、部门、字典
├── rb-model          # 模型驱动模块 - Model基类、统一控制器
├── rb-database       # 数据库模块 - 多数据源、数据权限、Druid配置
├── rb-init           # 数据库初始化模块 - 建表、初始数据
├── rb-i18n           # 国际化模块 - 多语言支持
└── rb-common         # 公共模块 - 常量、枚举、异常、工具
```

### 3.2 模块依赖关系

```
rb-admin
    └── rb-system
            └── rb-auth
                    └── rb-framework
                            └── rb-model
                                    └── rb-database
                                            └── rb-init
                                                    └── rb-i18n
                                                            └── rb-common
```

### 3.3 模块职责

| 模块 | 职责 | 核心类 |
|------|------|--------|
| rb-admin | 应用启动入口，聚合所有模块 | `RedBladeApplication` |
| rb-framework | 全局异常处理、操作日志切面、API文档配置 | `GlobalExceptionHandler`、`OperationLogAspect` |
| rb-auth | JWT认证、权限注解、登录登出、在线用户管理 | `JwtUtils`、`AuthService`、`OnlineUserService` |
| rb-system | 系统管理业务（用户、角色、菜单、组织、字典） | `SysUser`、`SysRole`、`SysMenu` |
| rb-model | Model基类、统一控制器、查询参数 | `BaseModel`、`@Model` |
| rb-database | MyBatis配置、数据权限拦截器、数据库方言、Druid监控 | `DruidConfig`、`DataScopeInterceptor` |
| rb-init | 表结构定义、初始数据、版本管理 | `TableCreator`、`DataLoader` |
| rb-i18n | 多语言文本对象、国际化工具 | `I18nText`、`I18nUtils` |
| rb-common | 通用常量、枚举、异常、响应对象 | `R`、`CacheConstants`、`BusinessException` |

---

## 四、核心设计

### 4.1 多组织数据隔离架构

所有业务表采用 **联合主键** 设计：`(org_code, xxx_code)`

```
┌─────────────────────────────────────────────────────┐
│                    sys_user                         │
├─────────────────────────────────────────────────────┤
│ org_code (PK)    │ 组织编码                          │
│ user_code (PK)   │ 用户编码                          │
│ username         │ 用户名                            │
│ password         │ 密码（Base64编码）                │
│ nickname         │ 昵称                              │
│ status           │ 状态（0正常 1停用）               │
│ del_flag         │ 逻辑删除标志                      │
│ create_time      │ 创建时间                          │
│ update_time      │ 更新时间                          │
└─────────────────────────────────────────────────────┘
```

**数据隔离实现**：

- `OrgDataInterceptor`：自动为 SQL 添加 `org_code` 过滤条件
- `DataScopeInterceptor`：基于角色的数据权限范围过滤

### 4.2 Model-Driven 开发模式

通过 `@Model` 注解声明业务模型，自动注册 REST API：

```java
@Model(
    name = "组织管理",
    table = "sys_org",
    api = "org",
    orgIsolation = false,
    logicDelete = true,
    audit = true
)
public class OrgModel extends BaseModel<SysOrg> {
    // 自动获得 CRUD API
    // GET    /api/model/org/list
    // GET    /api/model/org/{id}
    // POST   /api/model/org
    // PUT    /api/model/org/{id}
    // DELETE /api/model/org/{id}
}
```

**BaseModel 提供的能力**：

| 方法 | 说明 |
|------|------|
| `list(params)` | 分页查询 |
| `listAll(params)` | 查询全部 |
| `getOneById(id)` | 根据ID查询 |
| `listByCondition(conditions)` | 条件查询 |
| `insert(entity)` | 新增 |
| `insertBatch(entities)` | 批量新增 |
| `update(id, entity)` | 修改 |
| `updateSelective(id, entity)` | 选择性修改 |
| `delete(id)` | 删除 |
| `deleteBatch(ids)` | 批量删除 |

**生命周期钩子**：

```java
// 查询钩子
beforeList(params), afterList(params, result)
beforeGetById(id), afterGetById(id, entity)

// 新增钩子
beforeInsert(entity), afterInsert(entity)
validateInsert(entity)

// 修改钩子
beforeUpdate(id, entity), afterUpdate(id, entity, existing)
validateUpdate(entity)

// 删除钩子
beforeDelete(id), afterDelete(id, existing)
```

### 4.3 声明式数据库初始化

#### 表结构定义

通过 `@DbTable` 定义表结构：

```java
@DbTable(
    name = "sys_user",
    comment = "用户表",
    version = 1,
    orgCode = true,      // 自动添加 org_code 字段
    logicDelete = true,  // 自动添加 del_flag 字段
    audit = true         // 自动添加审计字段
)
public class SysUserTableDef extends TableDefinitionBuilder {
    public SysUserTableDef() {
        super("sys_user", "用户表");
        orgCode();
        column("user_code", DataType.VARCHAR, 50).primaryKey().comment("用户编码");
        column("username", DataType.VARCHAR, 50).nullable(false).comment("用户名");
        column("password", DataType.VARCHAR, 100).nullable(false).comment("密码");
        column("nickname", DataType.VARCHAR, 50).comment("昵称");
        column("status", DataType.CHAR, 1).defaultValue("'0'").comment("状态");
        // ...
        uniqueIndex("uk_org_username", "org_code", "username");
        index("idx_org_code", "org_code");
    }
}
```

#### 初始数据定义

通过 `@DbMetaData` 定义初始数据：

```java
@DbMetaData(
    table = "sys_user",
    order = 20,
    idempotent = true,
    uniqueKeys = {"org_code", "username"}
)
public class SysUserInit extends BaseDml<Map<String, Object>> {
    public SysUserInit() {
        Map<String, Object> admin = new HashMap<>();
        admin.put("org_code", "001");
        admin.put("user_code", "admin");
        admin.put("username", "admin");
        admin.put("password", Base64.encode("admin123"));
        admin.put("nickname", "超级管理员");
        add(admin);
    }
}
```

#### 初始化特性

| 特性 | 说明 |
|------|------|
| DDL变更检测 | 自动检测表结构变化并更新 |
| 幂等初始化 | 已存在数据跳过 |
| 版本控制 | 支持增量更新 |
| 注释生成 | 自动添加表和字段注释 |

### 4.4 认证授权架构

#### 认证流程

```
┌─────────────────────────────────────────────────────────────┐
│                     认证流程                                 │
├─────────────────────────────────────────────────────────────┤
│  1. POST /api/auth/login                                    │
│     ↓                                                       │
│  2. AuthServiceImpl.login()                                 │
│     - 检查账户是否被锁定                                     │
│     - 验证用户名密码（Base64解码比对）                       │
│     - 清除登录失败记录                                       │
│     - 查询权限角色                                           │
│     - 生成 JWT Token                                        │
│     - 存储到 Redis（key: rb:login:token:{token}）          │
│     ↓                                                       │
│  3. 返回 AccessToken + RefreshToken                         │
├─────────────────────────────────────────────────────────────┤
│                     请求流程                                 │
├─────────────────────────────────────────────────────────────┤
│  Request → JwtAuthenticationFilter                         │
│           ↓                                                 │
│  验证 Token → 解析用户信息 → 存入 SecurityContext          │
│           ↓                                                 │
│  检查 Token 即将过期 → 添加响应头提醒刷新                   │
│           ↓                                                 │
│  PermissionAspect 检查权限注解                              │
│           ↓                                                 │
│  Controller                                                  │
└─────────────────────────────────────────────────────────────┘
```

#### 密码安全策略

| 策略 | 说明 |
|------|------|
| 存储方式 | Base64 编码存储 |
| 登录验证 | 前端传输明文，后端解码数据库密码比对 |
| 失败限制 | 连续失败 5 次锁定 30 分钟 |

#### 登录失败限制机制

```java
// AuthServiceImpl 核心逻辑
private static final int MAX_LOGIN_FAIL_COUNT = 5;      // 最大失败次数
private static final long LOGIN_LOCK_TIME = 30;         // 锁定时间（分钟）

private void recordLoginFail(String loginKey) {
    // Redis 记录失败次数
    // 达到阈值时设置锁定状态
}

private void clearLoginFailRecord(String loginKey) {
    // 登录成功后清除失败记录
}

private void checkLoginLock(String loginKey) {
    // 检查账户是否被锁定
}
```

**Redis Key 设计**：

| Key | 说明 | 过期时间 |
|-----|------|----------|
| `rb:login:fail:{username}` | 登录失败次数 | 30分钟 |
| `rb:user:lock:{username}` | 用户锁定状态 | 30分钟 |

#### Token 刷新机制

```
┌─────────────────────────────────────────────────────────────┐
│                   Token 刷新机制                             │
├─────────────────────────────────────────────────────────────┤
│  Token 有效期：2小时                                         │
│  刷新阈值：剩余 10 分钟                                      │
├─────────────────────────────────────────────────────────────┤
│  每次请求时：                                                │
│  1. JwtAuthenticationFilter 检查 Token 剩余有效期           │
│  2. 若剩余时间 < 10 分钟，添加响应头：                       │
│     - X-Token-Refresh-Needed: true                         │
│     - X-Token-Expires-In: {剩余秒数}                        │
│  3. 前端监听响应头，调用 /api/auth/refresh 刷新 Token       │
└─────────────────────────────────────────────────────────────┘
```

#### 权限注解

| 注解 | 说明 | 示例 |
|------|------|------|
| `@RequiresPermission` | 功能权限 | `@RequiresPermission("system:user:add")` |
| `@RequiresRole` | 角色权限 | `@RequiresRole("admin")` |

#### LoginUser 结构

```java
public class LoginUser implements UserDetails {
    private String userCode;           // 用户编码
    private String username;           // 用户名
    private String nickname;           // 昵称
    private String orgCode;            // 组织编码
    private String orgName;            // 组织名称
    private String deptCode;           // 部门编码
    private String status;             // 状态
    private Set<String> permissions;   // 权限列表
    private Set<String> roles;         // 角色列表
    private String dataScope;          // 数据权限范围
    private List<String> orgCodes;     // 可访问组织列表
    private List<String> customOrgCodes; // 自定义组织列表
    private String accessToken;        // 访问令牌
    private String refreshToken;       // 刷新令牌
}
```

### 4.5 在线用户管理

#### 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                   在线用户管理架构                           │
├─────────────────────────────────────────────────────────────┤
│  登录时：                                                    │
│  - 生成 Token，存储 LoginUser 到 Redis                     │
│  - Key: rb:login:token:{token}                             │
│  - 过期时间：2小时                                          │
├─────────────────────────────────────────────────────────────┤
│  查询在线用户：                                              │
│  - 扫描 rb:login:token:* 所有 Key                          │
│  - 反序列化 LoginUser 对象                                  │
│  - 支持按组织、用户名过滤                                   │
├─────────────────────────────────────────────────────────────┤
│  强制下线：                                                  │
│  - 删除指定 Token 的 Redis Key                              │
│  - 用户下次请求时 Token 失效                                │
└─────────────────────────────────────────────────────────────┘
```

#### OnlineUser 结构

```java
public class OnlineUser {
    private String userCode;       // 用户编码
    private String username;       // 用户名
    private String nickname;       // 昵称
    private String orgCode;        // 组织编码
    private String orgName;        // 组织名称
    private String deptCode;       // 部门编码
    private String token;          // Token
    private LocalDateTime loginTime;   // 登录时间
    private LocalDateTime expireTime;  // 过期时间
}
```

#### API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/online/list` | GET | 在线用户列表（支持组织、用户名过滤） |
| `/api/online/{token}` | DELETE | 强制用户下线 |
| `/api/online/batch` | DELETE | 批量强制下线 |
| `/api/online/count` | GET | 获取在线用户数量 |

### 4.6 国际化架构

#### 多语言文本对象

```java
public class I18nText {
    private String zhCN;  // 简体中文
    private String zhTW;  // 繁体中文
    private String enUS;  // English
    private String jaJP;  // 日本語
    private String koKR;  // 한국어
    private String viVN;  // Tiếng Việt
    
    public String get(String lang) {
        return switch (lang) {
            case "zh-CN" -> zhCN;
            case "zh-TW" -> zhTW != null ? zhTW : zhCN;
            case "en-US" -> enUS != null ? enUS : zhCN;
            case "ja-JP" -> jaJP != null ? jaJP : zhCN;
            // ...
        };
    }
}
```

#### 使用方式

- 请求头：`Accept-Language: zh-CN`
- 数据库字段存储 JSON 格式多语言文本
- 通过 `I18nUtils` 工具类获取当前语言

### 4.7 数据库连接池监控（Druid）

#### 配置说明

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      # 连接池配置
      initial-size: 5              # 初始连接数
      min-idle: 10                 # 最小空闲连接
      max-active: 20               # 最大活跃连接
      max-wait: 60000              # 获取连接最大等待时间
      
      # 监控配置
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        login-username: admin
        login-password: admin123
        
      # SQL监控
      filter:
        stat:
          enabled: true
          log-slow-sql: true       # 记录慢SQL
          slow-sql-millis: 1000    # 慢SQL阈值
```

#### 监控功能

| 功能 | 说明 | 访问地址 |
|------|------|----------|
| 监控首页 | 数据源、SQL统计 | `http://localhost:8080/api/druid/` |
| SQL监控 | SQL执行统计、慢SQL分析 | 监控首页 → SQL监控 |
| URI监控 | 接口执行统计 | 监控首页 → URI监控 |
| Spring监控 | Spring Bean监控 | 监控首页 → Spring监控 |

#### 安全配置

Druid 监控页面已加入 Security 白名单：

```java
private static final String[] WHITE_LIST = {
    "/auth/login",
    "/auth/register",
    "/auth/captcha",
    "/auth/refresh",
    "/druid/**",           // Druid 监控
    "/api/druid/**",       // Druid 监控（API前缀）
    // ...
};
```

---

## 五、数据库设计

### 5.1 核心表结构

| 表名 | 说明 | 主键 |
|------|------|------|
| sys_user | 用户表 | (org_code, user_code) |
| sys_role | 角色表 | (org_code, role_code) |
| sys_menu | 菜单权限表 | menu_code |
| sys_org | 组织表 | org_code |
| sys_dept | 部门表 | (org_code, dept_code) |
| sys_user_role | 用户角色关联表 | (org_code, user_code, role_code) |
| sys_role_menu | 角色菜单关联表 | (org_code, role_code, menu_code) |
| sys_dict_type | 字典类型表 | dict_code |
| sys_dict_data | 字典数据表 | dict_code |
| sys_oper_log | 操作日志表 | log_code |
| sys_login_log | 登录日志表 | log_code |

### 5.2 通用字段

所有业务表包含以下通用字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| org_code | VARCHAR(50) | 组织编码（联合主键） |
| del_flag | CHAR(1) | 删除标志（0正常 1删除） |
| create_by | VARCHAR(50) | 创建人 |
| create_time | TIMESTAMP | 创建时间 |
| update_by | VARCHAR(50) | 更新人 |
| update_time | TIMESTAMP | 更新时间 |

### 5.3 数据权限范围

| 值 | 说明 | SQL 过滤条件 |
|----|------|--------------|
| 1 | 全部数据 | 无过滤 |
| 2 | 自定义数据 | org_code IN (customOrgCodes) |
| 3 | 本组织数据 | org_code = currentUser.orgCode |
| 4 | 本组织及以下 | org_code IN (orgCodes) |
| 5 | 仅本人数据 | create_by = currentUser.userCode |

---

## 六、API 设计

### 6.1 RESTful 规范

```
GET    /api/model/{model}/list           # 分页查询
GET    /api/model/{model}/all            # 查询全部
GET    /api/model/{model}/{id}           # 查询详情
GET    /api/model/{model}/query          # 条件查询
POST   /api/model/{model}                # 新增
POST   /api/model/{model}/batch          # 批量新增
PUT    /api/model/{model}/{id}           # 修改
PATCH  /api/model/{model}/{id}           # 选择性修改
DELETE /api/model/{model}/{id}           # 删除
DELETE /api/model/{model}/batch          # 批量删除
POST   /api/model/{model}/action/{method}  # 自定义方法
GET    /api/model/{model}/action/{method}  # 自定义方法
GET    /api/model/{model}/meta           # 获取模型元信息
GET    /api/model/_models                # 获取所有模型列表
```

### 6.2 认证接口

| 接口 | 方法 | 说明 | 请求体 |
|------|------|------|--------|
| `/api/auth/login` | POST | 用户登录 | `{username, password, captcha, uuid}` |
| `/api/auth/logout` | POST | 用户登出 | - |
| `/api/auth/register` | POST | 用户注册 | `{username, password, nickname, ...}` |
| `/api/auth/captcha` | GET | 获取验证码 | - |
| `/api/auth/refresh` | POST | 刷新Token | `{refreshToken}` |
| `/api/auth/userinfo` | GET | 获取当前用户信息 | - |
| `/api/auth/changePassword` | PUT | 修改密码 | `{oldPassword, newPassword}` |

### 6.3 在线用户接口

| 接口 | 方法 | 说明 | 参数 |
|------|------|------|------|
| `/api/online/list` | GET | 在线用户列表 | `orgCode`, `username` |
| `/api/online/{token}` | DELETE | 强制下线 | `token` (路径参数) |
| `/api/online/batch` | DELETE | 批量下线 | `["token1", "token2"]` |
| `/api/online/count` | GET | 在线人数 | - |

### 6.4 统一响应格式

```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {},
    "success": true
}
```

### 6.5 分页响应格式

```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "total": 100,
        "rows": []
    },
    "success": true
}
```

### 6.6 验证码响应格式

```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "uuid": "xxx-xxx-xxx",
        "img": "data:image/png;base64,iVBORw0KGgo..."
    },
    "success": true
}
```

---

## 七、缓存设计

### 7.1 缓存Key规范

| Key模式 | 说明 | 过期时间 | 数据结构 |
|---------|------|----------|----------|
| `rb:login:token:{token}` | 登录Token及用户信息 | 2小时 | String (JSON) |
| `rb:login:fail:{username}` | 登录失败次数 | 30分钟 | String (Number) |
| `rb:user:lock:{username}` | 用户锁定状态 | 30分钟 | String |
| `rb:captcha:{uuid}` | 验证码 | 5分钟 | String |
| `rb:user:info:{userId}` | 用户信息缓存 | 1小时 | String (JSON) |
| `rb:user:permission:{userId}` | 用户权限列表 | 1小时 | Set |
| `rb:user:menu:{userId}` | 用户菜单列表 | 1小时 | List |

### 7.2 缓存更新策略

| 场景 | 更新策略 |
|------|----------|
| 用户登录 | 写入 Token、用户信息、权限、菜单缓存 |
| 用户登出 | 删除 Token 缓存 |
| 修改用户信息 | 删除用户信息缓存 |
| 修改角色权限 | 删除相关用户权限、菜单缓存 |
| 强制下线 | 删除 Token 缓存 |

---

## 八、扩展机制

### 8.1 数据库初始化扩展点

```java
public interface DbInitExtension {
    /**
     * 交互式输入
     */
    void onInteractiveInput(InitContext context, UserInput input);
    
    /**
     * 获取初始化数据
     */
    List<InitData> getInitData(InitContext context);
    
    /**
     * 初始化完成后回调
     */
    void afterInit(InitContext context);
}
```

### 8.2 自定义 Model 方法

在 Model 中定义的任何 public 方法，都可通过以下方式调用：

```
POST /api/model/{model}/action/{methodName}
GET  /api/model/{model}/action/{methodName}
```

---

## 九、配置说明

### 9.1 数据库配置

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      driver-class-name: org.postgresql.Driver
      url: jdbc:postgresql://localhost:5432/redblade
      username: postgres
      password: xxx
      initial-size: 5
      min-idle: 10
      max-active: 20
      max-wait: 60000
      connect-timeout: 30000
      socket-timeout: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      max-evictable-idle-time-millis: 900000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20
      filters: stat,wall,slf4j
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        login-username: admin
        login-password: admin123
        reset-enable: true
      web-stat-filter:
        enabled: true
        url-pattern: /*
        exclusions: "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*"
      filter:
        stat:
          enabled: true
          log-slow-sql: true
          slow-sql-millis: 1000
          merge-sql: true
        wall:
          enabled: true
          config:
            multi-statement-allow: true
```

### 9.2 Redis 配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0
```

### 9.3 初始化配置

```yaml
rb:
  init:
    enabled: false                    # 是否启用初始化
    mode: create-if-not-exists        # 初始化模式 (none/create-if-not-exists/always)
    drop-before-create: false         # 是否先删后建（危险）
    version-table: sys_init_version   # 版本控制表名
    database-type: postgresql         # 数据库类型
```

### 9.4 JWT 配置

```yaml
rb:
  jwt:
    secret: RedBladeSecretKeyForJwtTokenGenerationAndValidation2024
    access-token-expiration: 7200000   # 2小时（毫秒）
    refresh-token-expiration: 604800000 # 7天（毫秒）
    issuer: RedBlade
```

---

## 十、部署架构

```
┌─────────────────────────────────────────────────────────────┐
│                        Nginx                                 │
│                    (反向代理/负载均衡)                        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   Spring Boot 应用                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │  rb-admin   │  │  rb-system  │  │   rb-auth   │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────────────────────────────────────────────┘
          │                    │                    │
          ▼                    ▼                    ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   PostgreSQL    │  │      Redis      │  │   文件存储      │
│   (主数据库)     │  │  (缓存/Session) │  │  (OSS/本地)     │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

---

## 十一、开发指南

### 11.1 新增业务模块

1. 在 `rb-system` 下创建 Entity 类
2. 在 `rb-system/init/table` 下创建表定义类
3. 在 `rb-system/init` 下创建初始数据类（可选）
4. 在 `rb-system/model` 下创建 Model 类
5. 启动应用，表结构自动创建

### 11.2 新增自定义接口

在 Model 中定义方法：

```java
@Model(name = "用户管理", table = "sys_user", api = "user")
public class UserModel extends BaseModel<SysUser> {
    
    @RequiresPermission("system:user:resetPwd")
    public void resetPassword(String userCode, String newPassword) {
        // 业务逻辑
    }
}
```

调用方式：
```
POST /api/model/user/action/resetPassword
Body: { "userCode": "xxx", "newPassword": "xxx" }
```

### 11.3 数据权限控制

在 Service 或 Model 方法上添加注解：

```java
@DataScope(orgCode = "orgCode")
public List<SysUser> listUsers(String orgCode) {
    // 自动添加数据权限过滤
}
```

### 11.4 新增认证相关功能

1. 在 `rb-auth/service` 下定义服务接口
2. 在 `rb-auth/service/impl` 下实现服务
3. 在 `rb-auth/controller` 下创建控制器
4. 更新 `SecurityConfig` 白名单（如需要）

---

## 十二、安全最佳实践

### 12.1 密码安全

| 项目 | 当前实现 | 生产建议 |
|------|----------|----------|
| 存储方式 | Base64 编码 | BCrypt 哈希 |
| 传输方式 | HTTPS | HTTPS |
| 强度要求 | 无限制 | 8位以上，包含大小写数字特殊字符 |

### 12.2 Token 安全

| 项目 | 说明 |
|------|------|
| 存储位置 | Redis（服务端）+ 返回给客户端 |
| 传输方式 | Authorization Header |
| 有效期 | 2小时（可配置） |
| 刷新机制 | 滑动过期，响应头提醒 |

### 12.3 接口安全

| 措施 | 说明 |
|------|------|
| 认证 | JWT Token 验证 |
| 授权 | `@RequiresPermission`、`@RequiresRole` |
| 防刷 | 登录失败限制 |
| 审计 | 操作日志记录 |

---

## 十三、监控与运维

### 13.1 Druid 监控

访问地址：`http://localhost:8080/api/druid/`

| 功能 | 说明 |
|------|------|
| 数据源信息 | 连接池状态、活跃连接数 |
| SQL监控 | SQL执行次数、时间、错误数 |
| 慢SQL | 执行时间超过阈值的SQL |
| URI监控 | 接口访问统计 |
| Spring监控 | Bean方法调用统计 |

### 13.2 健康检查

```bash
# 应用健康状态
GET /actuator/health

# 应用信息
GET /actuator/info
```

---

## 十四、总结

RedBlade 项目采用了以下核心设计理念：

| 设计理念 | 说明 |
|----------|------|
| 多租户优先 | 所有业务表设计时即考虑组织隔离 |
| 声明式开发 | 通过注解减少样板代码 |
| Model 抽象层 | 统一的数据操作接口，支持生命周期扩展 |
| 细粒度权限 | 功能权限 + 数据权限双重控制 |
| 国际化内置 | 从底层支持多语言场景 |
| 安全优先 | Token认证、登录限制、在线用户管理 |
| 可观测性 | Druid监控、操作日志、登录日志 |

这套架构适合需要多组织数据隔离、细粒度权限控制的企业级应用开发。
