import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import { useUserStore } from '@/store/user'

// 静态路由
export const constantRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', hidden: true }
  },
  {
    path: '/',
    redirect: '/home',
    component: () => import('@/layout/index.vue'),
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled', affix: true }
      }
    ]
  }
]

// 动态路由（根据权限动态加载）
export const asyncRoutes = [
  {
    path: '/system',
    name: 'System',
    redirect: '/system/user',
    component: () => import('@/layout/index.vue'),
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'role',
        name: 'Role',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled' }
      },
      {
        path: 'menu',
        name: 'Menu',
        component: () => import('@/views/system/menu/index.vue'),
        meta: { title: '菜单管理', icon: 'Menu' }
      },
      {
        path: 'dept',
        name: 'Dept',
        component: () => import('@/views/system/dept/index.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding' }
      }
    ]
  },
  {
    path: '/monitor',
    name: 'Monitor',
    redirect: '/monitor/online',
    component: () => import('@/layout/index.vue'),
    meta: { title: '系统监控', icon: 'Monitor' },
    children: [
      {
        path: 'online',
        name: 'Online',
        component: () => import('@/views/monitor/online/index.vue'),
        meta: { title: '在线用户', icon: 'Connection' }
      }
    ]
  },
  // 404 页面必须放在最后
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
    meta: { hidden: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes
})

// 路由白名单
const whiteList = ['/login', '/404']

// 是否已添加动态路由
let dynamicRoutesAdded = false

// 路由守卫
router.beforeEach(async (to, from, next) => {
  NProgress.start()

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - RedBlade` : 'RedBlade'

  const userStore = useUserStore()
  const hasToken = userStore.token

  if (hasToken) {
    if (to.path === '/login') {
      // 已登录，跳转到首页
      next({ path: '/home' })
      NProgress.done()
    } else {
      // 判断是否已添加动态路由
      if (!dynamicRoutesAdded) {
        try {
          // 获取用户信息
          await userStore.getUserInfoAction()

          // 动态添加路由
          asyncRoutes.forEach(route => {
            router.addRoute(route)
          })

          dynamicRoutesAdded = true

          // 重新导航到目标路由
          next({ ...to, replace: true })
        } catch (error) {
          // 获取用户信息失败，清除登录状态
          dynamicRoutesAdded = false
          userStore.clearLoginState()
          next(`/login?redirect=${to.path}`)
          NProgress.done()
        }
      } else {
        next()
      }
    }
  } else {
    // 未登录
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})

// 重置路由状态（用于登出）
export function resetRouter() {
  dynamicRoutesAdded = false
  asyncRoutes.forEach(route => {
    if (route.name) {
      router.removeRoute(route.name)
    }
  })
}

export default router