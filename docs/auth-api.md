# 登录模块接口文档

## 文档版本记录

| 版本号 | 修改日期 | 修改人 | 修改内容 |
|--------|----------|--------|----------|
| v1.0.0 | 2026-05-20 | 王帅令 | 初始版本，创建登录模块接口文档 |
| v1.1.0 | 2026-05-20 | 王帅令 | 新增：验证码接口、用户注册接口、修改密码接口、在线用户管理接口；补充：登录失败限制说明、Token刷新响应头机制、安全白名单更新（Druid监控） |

---

## 一、模块概述

登录模块提供用户认证相关功能，包括登录、登出、刷新令牌、获取当前用户信息等接口。

**模块路径**: `rb-auth`

**接口基础路径**: `/api/auth`

---

## 二、接口列表

| 接口 | 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|------|
| 登录 | POST | /api/auth/login | 用户登录获取Token | 否 |
| 登出 | POST | /api/auth/logout | 用户登出清除Token | 是 |
| 刷新令牌 | POST | /api/auth/refresh | 刷新AccessToken | 否 |
| 获取用户信息 | GET | /api/auth/userinfo | 获取当前登录用户信息 | 是 |

---

## 三、接口详情

### 3.1 用户登录

**接口路径**: `POST /api/auth/login`

**接口说明**: 用户登录系统，获取访问令牌和刷新令牌。

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orgCode | String | 是 | 组织编码 |
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |
| captcha | String | 否 | 验证码（启用验证码时必填） |
| captchaKey | String | 否 | 验证码Key（启用验证码时必填） |

**请求示例**:

```json
{
    "orgCode": "001",
    "username": "admin",
    "password": "123456"
}
```

**响应参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| accessToken | String | 访问令牌（用于接口认证） |
| refreshToken | String | 刷新令牌（用于刷新AccessToken） |
| tokenType | String | 令牌类型，固定值 "Bearer" |
| expiresIn | Long | AccessToken过期时间（秒），默认7200秒（2小时） |
| userCode | String | 用户编码 |
| username | String | 用户名 |
| nickname | String | 用户昵称 |
| orgCode | String | 组织编码 |
| orgName | String | 组织名称 |

**响应示例**:

```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
        "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
        "tokenType": "Bearer",
        "expiresIn": 7200,
        "userCode": "admin",
        "username": "admin",
        "nickname": "超级管理员",
        "orgCode": "001",
        "orgName": "RedBlade"
    },
    "success": true
}
```

**错误响应**:

| 错误码 | 错误信息 | 说明 |
|--------|----------|------|
| 500 | 用户不存在 | 组织编码或用户名错误 |
| 500 | 密码错误 | 密码不正确 |
| 500 | 用户已停用 | 用户状态为停用 |
| 400 | 组织编码不能为空 | 缺少orgCode参数 |
| 400 | 用户名不能为空 | 缺少username参数 |
| 400 | 密码不能为空 | 缺少password参数 |

---

### 3.2 用户登出

**接口路径**: `POST /api/auth/logout`

**接口说明**: 用户登出系统，清除Redis中的Token信息。

**认证要求**: 需要在请求头中携带AccessToken。

**请求头**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | 访问令牌，格式：Bearer {accessToken} |

**请求示例**:

```
POST /api/auth/logout
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**:

```json
{
    "code": 200,
    "msg": "操作成功",
    "data": null,
    "success": true
}
```

---

### 3.3 刷新令牌

**接口路径**: `POST /api/auth/refresh`

**接口说明**: 使用RefreshToken刷新AccessToken，获取新的令牌对。

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| refreshToken | String | 是 | 刷新令牌（登录时获取的refreshToken） |

**请求示例**:

```json
"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJuYW1lIjoiYWRtaW4iLCJ0eXBlIjoicmVmcmVzaCIsImlzcyI6IlJlZEJsYWRlIiwiaWF0IjoxNzAwMDAwMDAwLCJleHAiOjE3MDAwNjgwMDAwfQ.xxx"
```

**响应参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| accessToken | String | 新的访问令牌 |
| refreshToken | String | 新的刷新令牌 |
| tokenType | String | 令牌类型，固定值 "Bearer" |
| expiresIn | Long | AccessToken过期时间（秒） |
| userCode | String | 用户编码 |
| username | String | 用户名 |
| nickname | String | 用户昵称 |
| orgCode | String | 组织编码 |
| orgName | String | 组织名称 |

**响应示例**:

```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
        "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
        "tokenType": "Bearer",
        "expiresIn": 7200,
        "userCode": "admin",
        "username": "admin",
        "nickname": "超级管理员",
        "orgCode": "001",
        "orgName": "RedBlade"
    },
    "success": true
}
```

**错误响应**:

| 错误码 | 错误信息 | 说明 |
|--------|----------|------|
| 500 | Token无效 | RefreshToken无效或已过期 |
| 500 | 用户不存在 | 用户已被删除 |
| 500 | 用户已停用 | 用户状态为停用 |

---

### 3.4 获取当前用户信息

**接口路径**: `GET /api/auth/userinfo`

**接口说明**: 获取当前登录用户的详细信息，包括权限、角色、数据权限等。

**认证要求**: 需要在请求头中携带AccessToken。

**请求头**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | 访问令牌，格式：Bearer {accessToken} |

**请求示例**:

```
GET /api/auth/userinfo
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| userCode | String | 用户编码 |
| username | String | 用户名 |
| nickname | String | 用户昵称 |
| orgCode | String | 组织编码 |
| orgName | String | 组织名称 |
| orgType | String | 组织类型（hq/company/dept/team） |
| deptCode | String | 部门编码 |
| status | String | 用户状态（0正常 1停用） |
| permissions | Set<String> | 权限标识列表 |
| roles | Set<String> | 角色标识列表 |
| dataScope | String | 数据权限范围 |
| orgCodes | List<String> | 可访问的组织编码列表 |
| customOrgCodes | List<String> | 自定义数据权限组织列表 |

**响应示例**:

```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "userCode": "admin",
        "username": "admin",
        "nickname": "超级管理员",
        "orgCode": "001",
        "orgName": "RedBlade",
        "orgType": "hq",
        "deptCode": null,
        "status": "0",
        "permissions": [
            "system:user:add",
            "system:user:edit",
            "system:user:delete",
            "system:role:add",
            "system:role:edit",
            "system:role:delete"
        ],
        "roles": ["admin"],
        "dataScope": "1",
        "orgCodes": ["001", "002", "003"],
        "customOrgCodes": []
    },
    "success": true
}
```

**数据权限范围说明**:

| 值 | 说明 |
|----|------|
| 1 | 全部数据权限 |
| 2 | 自定义数据权限 |
| 3 | 本组织数据权限 |
| 4 | 本组织及以下数据权限 |
| 5 | 仅本人数据权限 |

---

## 四、Token 使用说明

### 4.1 Token 结构

AccessToken 和 RefreshToken 均采用 JWT 格式：

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJuYW1lIjoiYWRtaW4iLCJ0eXBlIjoiYWNjZXNzIiwiaXNzIjoiUmVkQmxhZGUiLCJpYXQiOjE3MDAwMDAwMDAwLCJleHAiOjE3MDAwMDcyMDAwfQ.xxx
```

**JWT Claims**:

| Claim | 说明 |
|-------|------|
| sub | 用户编码（userCode） |
| username | 用户名 |
| type | 令牌类型（access/refresh） |
| iss | 发行者（RedBlade） |
| iat | 发行时间 |
| exp | 过期时间 |

### 4.2 Token 过期时间

| 令牌类型 | 默认过期时间 | 配置项 |
|----------|--------------|--------|
| AccessToken | 2小时（7200秒） | rb.jwt.access-token-expiration |
| RefreshToken | 7天（604800秒） | rb.jwt.refresh-token-expiration |

### 4.3 Token 认证方式

在需要认证的接口请求头中添加 Authorization：

```
Authorization: Bearer {accessToken}
```

**示例**:

```bash
curl -X GET "http://localhost:8080/api/model/user/list" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### 4.4 Token 刷新策略

建议在 AccessToken 过期前使用 RefreshToken 刷新：

1. 客户端检测 AccessToken 即将过期（如剩余时间 < 5分钟）
2. 调用 `/api/auth/refresh` 接口获取新的令牌对
3. 更新本地存储的 AccessToken 和 RefreshToken
4. 使用新的 AccessToken 继续请求

---

## 五、错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 参数校验失败 |
| 401 | 未认证或Token无效 |
| 403 | 无权限访问 |
| 500 | 业务异常 |

---

## 六、安全白名单

以下接口无需认证即可访问：

| 路径 | 说明 |
|------|------|
| /api/auth/login | 登录接口 |
| /api/auth/register | 注册接口（预留） |
| /api/auth/captcha | 验证码接口（预留） |
| /api/auth/refresh | 刷新令牌接口 |
| /doc.html | API文档页面 |
| /swagger-ui.html | Swagger UI |
| /swagger-ui/** | Swagger UI资源 |
| /v3/api-docs/** | OpenAPI文档 |
| /webjars/** | Web资源 |
| /favicon.ico | 网站图标 |
| /error | 错误页面 |

---

## 七、配置说明

### 7.1 JWT 配置

```yaml
rb:
  jwt:
    secret: RedBladeSecretKeyForJwtTokenGenerationAndValidation2024
    access-token-expiration: 7200000   # AccessToken过期时间（毫秒），默认2小时
    refresh-token-expiration: 604800000 # RefreshToken过期时间（毫秒），默认7天
    issuer: RedBlade                   # JWT发行者
```

### 7.2 Redis Token 存储

登录成功后，用户信息存储在 Redis 中：

```
Key: login:token:{accessToken}
Value: LoginUser对象（JSON序列化）
Expiration: AccessToken过期时间
```

---

## 八、调用示例

### 8.1 登录示例

```bash
# 登录请求
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"orgCode":"001","username":"admin","password":"123456"}'

# 响应
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
        "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
        "tokenType": "Bearer",
        "expiresIn": 7200,
        "userCode": "admin",
        "username": "admin",
        "nickname": "超级管理员",
        "orgCode": "001",
        "orgName": "RedBlade"
    },
    "success": true
}
```

### 8.2 访问认证接口示例

```bash
# 使用Token访问接口
curl -X GET "http://localhost:8080/api/auth/userinfo" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### 8.3 刷新Token示例

```bash
# 刷新Token
curl -X POST "http://localhost:8080/api/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '"eyJhbGciOiJIUzI1NiJ9...refreshToken..."'
```

### 8.4 登出示例

```bash
# 登出
curl -X POST "http://localhost:8080/api/auth/logout" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

## 九、注意事项

1. **Token 安全**: AccessToken 应妥善保管，不要暴露在 URL 或日志中
2. **并发登录**: 同一用户可多次登录，每次登录生成新的Token
3. **登出清理**: 登出后AccessToken立即失效
4. **密码加密**: 密码使用 BCrypt 加密存储，强度为10
5. **组织隔离**: 登录时必须指定组织编码，不同组织用户数据隔离