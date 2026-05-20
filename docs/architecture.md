# RedBlade 项目架构设计文档

## 一、项目概述

**RedBlade** 是一个企业级前后端分离基础架构，基于 Spring Boot 3.2.5 构建，采用 Java 17，旨在提供开箱即用的企业级应用开发框架。

### 核心特性

- **多组织数据隔离**：基于 `org_code` 的多租户架构
- **声明式数据库初始化**：通过注解定义表结构和初始数据
- **Model-Driven 开发模式**：统一的数据模型抽象层
- **国际化支持**：内置多语言文本处理机制
- **细粒度数据权限**：支持组织、部门、个人多级数据权限

---

## 二、技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 核心框架 | Spring Boot | 3.2.5 |
| 持久层 | MyBatis-Plus | 3.5.5 |
| 数据库 | PostgreSQL | 42.7.3 |
| 缓存 | Redis | - |
| 认证 | Spring Security + JWT | 0.12.5 |
| 工具类 | Hutool | 5.8.26 |
| 对象映射 | MapStruct | 1.5.5 |
| API文档 | Knife4j (OpenAPI 3) | 4.5.0 |

---

## 三、模块架构

### 3.1 模块结构

```
redblade-server/
├── rb-admin          # 启动模块（入口）
├── rb-framework      # 框架核心（全局异常、切面、配置）
├── rb-auth           # 认证授权模块
├── rb-system         # 系统管理模块
├── rb-model          # 模型驱动模块
├── rb-database       # 数据库模块（拦截器、方言）
├── rb-init           # 数据库初始化模块
├── rb-i18n           # 国际化模块
└── rb-common         # 公共模块（常量、枚举、工具）
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

| 模块 | 职责 |
|------|------|
| rb-admin | 应用启动入口，聚合所有模块 |
| rb-framework | 全局异常处理、操作日志切面、API文档配置 |
| rb-auth | JWT认证、权限注解、登录登出服务 |
| rb-system | 系统管理业务（用户、角色、菜单、组织、字典） |
| rb-model | Model基类、统一控制器、查询参数 |
| rb-database | MyBatis配置、数据权限拦截器、数据库方言 |
| rb-init | 表结构定义、初始数据、版本管理 |
| rb-i18n | 多语言文本对象、国际化工具 |
| rb-common | 通用常量、枚举、异常、响应对象 |

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
│ password         │ 密码                              │
│ ...              │ 其他字段                          │
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
validateDelete(id, existing)
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
        admin.put("password", "$2a$10$...");
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
│     - 验证用户名密码                                         │
│     - 查询权限角色                                           │
│     - 生成 JWT Token                                        │
│     - 存储到 Redis                                          │
│     ↓                                                       │
│  3. 返回 AccessToken + RefreshToken                         │
├─────────────────────────────────────────────────────────────┤
│                     请求流程                                 │
├─────────────────────────────────────────────────────────────┤
│  Request → JwtAuthenticationFilter                         │
│           ↓                                                 │
│  验证 Token → 解析用户信息 → 存入 SecurityContext          │
│           ↓                                                 │
│  PermissionAspect 检查权限注解                              │
│           ↓                                                 │
│  Controller                                                  │
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

### 4.5 国际化架构

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
| create_time | TIMESTAMP | 创建时间 |
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

```
POST   /api/auth/login      # 登录
POST   /api/auth/logout     # 登出
POST   /api/auth/refresh    # 刷新Token
GET    /api/auth/userinfo   # 获取当前用户信息
```

### 6.3 统一响应格式

```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {},
    "success": true
}
```

### 6.4 分页响应格式

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

---

## 七、扩展机制

### 7.1 数据库初始化扩展点

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

### 7.2 自定义 Model 方法

在 Model 中定义的任何 public 方法，都可通过以下方式调用：

```
POST /api/model/{model}/action/{methodName}
GET  /api/model/{model}/action/{methodName}
```

---

## 八、配置说明

### 8.1 数据库初始化配置

```yaml
rb:
  init:
    enabled: false                    # 是否启用初始化
    mode: create-if-not-exists        # 初始化模式 (none/create-if-not-exists/always)
    drop-before-create: false         # 是否先删后建（危险）
    version-table: sys_init_version   # 版本控制表名
    database-type: postgresql         # 数据库类型
```

### 8.2 JWT 配置

```yaml
rb:
  jwt:
    secret: RedBladeSecretKeyForJwtTokenGenerationAndValidation2024
    access-token-expiration: 7200000   # 2小时（毫秒）
    refresh-token-expiration: 604800000 # 7天（毫秒）
    issuer: RedBlade
```

### 8.3 数据源配置

```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/redblade
    username: postgres
    password: xxx
    
  data:
    redis:
      host: localhost
      port: 6379
      password:
```

---

## 九、部署架构

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

## 十、开发指南

### 10.1 新增业务模块

1. 在 `rb-system` 下创建 Entity 类
2. 在 `rb-system/init/table` 下创建表定义类
3. 在 `rb-system/init` 下创建初始数据类（可选）
4. 在 `rb-system/model` 下创建 Model 类
5. 启动应用，表结构自动创建

### 10.2 新增自定义接口

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

### 10.3 数据权限控制

在 Service 或 Model 方法上添加注解：

```java
@DataScope(orgCode = "orgCode")
public List<SysUser> listUsers(String orgCode) {
    // 自动添加数据权限过滤
}
```

---

## 十一、总结

RedBlade 项目采用了以下核心设计理念：

| 设计理念 | 说明 |
|----------|------|
| 多租户优先 | 所有业务表设计时即考虑组织隔离 |
| 声明式开发 | 通过注解减少样板代码 |
| Model 抽象层 | 统一的数据操作接口，支持生命周期扩展 |
| 细粒度权限 | 功能权限 + 数据权限双重控制 |
| 国际化内置 | 从底层支持多语言场景 |

这套架构适合需要多组织数据隔离、细粒度权限控制的企业级应用开发。
