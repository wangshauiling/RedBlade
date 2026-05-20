# RedBlade Web - 前端项目

## 项目概述

RedBlade Web 是 RedBlade 企业级管理系统的前端项目，基于 Vue 3 + Vite + Element Plus 构建。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.x | 前端框架 |
| Vite | 5.x | 构建工具 |
| Vue Router | 4.x | 路由管理 |
| Pinia | 2.x | 状态管理 |
| Element Plus | 2.6.x | UI组件库 |
| Axios | 1.x | HTTP请求 |
| Sass | 1.x | CSS预处理器 |

## 目录结构

```
src/
├── api/           # 接口请求
│   ├── auth.js    # 认证接口（登录、登出、刷新Token）
│   └── online.js  # 在线用户接口
├── assets/        # 静态资源
│   ├── logo.svg   # Logo图片
│   └── styles/    # 样式文件
│       ├── variables.scss  # SCSS变量
│       └── index.scss      # 全局样式
├── components/    # 公共组件
├── layout/        # 后台布局
│   ├── index.vue           # 布局主组件
│   └── components/
│       ├── Sidebar.vue     # 侧边栏
│       ├── Navbar.vue      # 顶部导航
│       ├── TagsView.vue    # 标签页导航
│       └── AppMain.vue     # 内容区
├── router/        # 路由配置
│   └── index.js   # 路由定义和守卫
├── store/         # Pinia状态管理
│   ├── user.js    # 用户状态（Token、用户信息）
│   ├── app.js     # 应用状态（侧边栏、菜单）
│   ├── tagsView.js # 标签页状态
│   └── index.js   # Store导出
├── utils/         # 工具函数
│   └── request.js # Axios封装
├── views/         # 页面组件
│   ├── login/     # 登录页
│   ├── home/      # 首页
│   ├── system/    # 系统管理
│   │   ├── user/  # 用户管理
│   │   ├── role/  # 角色管理
│   │   ├── menu/  # 菜单管理
│   │   └── dept/  # 部门管理
│   ├── monitor/   # 系统监控
│   │   └── online/ # 在线用户
│   └── error/     # 错误页面
│       └── 404.vue
├── App.vue        # 根组件
└── main.js        # 入口文件
```

## 功能特性

### 1. 用户认证
- 登录/登出
- Token 自动刷新
- 路由权限守卫

### 2. 后台布局
- 侧边栏菜单（可折叠）
- 顶部导航（面包屑、用户信息）
- 标签页导航（多页面切换）

### 3. 状态管理
- 用户信息持久化
- Token 管理
- 菜单状态管理

### 4. 请求封装
- Axios 拦截器
- Token 自动注入
- 错误统一处理
- Token 过期提醒

## 快速开始

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

访问 http://localhost:3000

### 生产构建

```bash
npm run build
```

### 预览构建

```bash
npm run preview
```

## 配置说明

### API代理配置

开发环境下，API请求自动代理到后端服务：

```javascript
// vite.config.js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

### 环境变量

可创建 `.env.development` 和 `.env.production` 文件配置环境变量：

```bash
# .env.development
VITE_API_BASE_URL=/api
VITE_APP_TITLE=RedBlade

# .env.production
VITE_API_BASE_URL=/api
VITE_APP_TITLE=RedBlade
```

## 登录说明

默认登录账号：
- 组织编码：001
- 用户名：admin
- 密码：admin123

（密码需与后端数据库中的密码一致）

## 与后端对接

### 认证接口

| 前端调用 | 后端接口 | 说明 |
|----------|----------|------|
| `login()` | POST /api/auth/login | 用户登录 |
| `logout()` | POST /api/auth/logout | 用户登出 |
| `getUserInfo()` | GET /api/auth/userinfo | 获取用户信息 |
| `refreshToken()` | POST /api/auth/refresh | 刷新Token |

### 在线用户接口

| 前端调用 | 后端接口 | 说明 |
|----------|----------|------|
| `getOnlineUsers()` | GET /api/online/list | 在线用户列表 |
| `kickoutUser()` | DELETE /api/online/{token} | 强制下线 |
| `kickoutBatch()` | DELETE /api/online/batch | 批量下线 |

## 开发规范

### 组件命名
- 页面组件：`index.vue`
- 业务组件：`XxxComponent.vue`
- 公共组件：`Xxx.vue`

### 样式规范
- 使用 SCSS 预处理器
- 变量定义在 `variables.scss`
- 组件样式使用 `scoped`

### API调用
- 统一在 `api/` 目录下定义
- 使用封装的 `request.js`

## 注意事项

1. **Token刷新**：后端会在响应头返回 `X-Token-Refresh-Needed`，前端需监听并刷新
2. **路由守卫**：未登录用户自动跳转到登录页
3. **权限控制**：根据用户权限动态加载路由

## 待开发功能

- [ ] 角色管理页面
- [ ] 菜单管理页面
- [ ] 部门管理页面
- [ ] 个人中心页面
- [ ] 修改密码页面
- [ ] 操作日志页面