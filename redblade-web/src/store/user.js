import { defineStore } from 'pinia'
import { login, logout, getUserInfo, refreshToken as refreshTokenApi } from '@/api/auth'
import router, { resetRouter } from '@/router'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    refreshToken: localStorage.getItem('refreshToken') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
    permissions: JSON.parse(localStorage.getItem('permissions') || '[]'),
    roles: JSON.parse(localStorage.getItem('roles') || '[]')
  }),

  getters: {
    // 是否已登录
    isLogin: (state) => !!state.token,
    // 用户名
    username: (state) => state.userInfo?.username || '',
    // 昵称
    nickname: (state) => state.userInfo?.nickname || '',
    // 组织编码
    orgCode: (state) => state.userInfo?.orgCode || '',
    // 组织名称
    orgName: (state) => state.userInfo?.orgName || '',
    // 是否有权限
    hasPermission: (state) => (permission) => {
      return state.permissions.includes(permission) || state.permissions.includes('*:*:*')
    },
    // 是否有角色
    hasRole: (state) => (role) => {
      return state.roles.includes(role) || state.roles.includes('admin')
    }
  },

  actions: {
    /**
     * 用户登录
     */
    async loginAction(loginForm) {
      try {
        const res = await login(loginForm)
        const { data } = res

        // 保存 Token
        this.token = data.accessToken
        this.refreshToken = data.refreshToken
        localStorage.setItem('token', data.accessToken)
        localStorage.setItem('refreshToken', data.refreshToken)

        // 保存用户基本信息
        const userInfo = {
          userCode: data.userCode,
          username: data.username,
          nickname: data.nickname,
          orgCode: data.orgCode,
          orgName: data.orgName
        }
        this.userInfo = userInfo
        localStorage.setItem('userInfo', JSON.stringify(userInfo))

        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },

    /**
     * 获取用户信息
     */
    async getUserInfoAction() {
      try {
        const res = await getUserInfo()
        const { data } = res

        this.userInfo = data
        this.permissions = data.permissions || []
        this.roles = data.roles || []

        localStorage.setItem('userInfo', JSON.stringify(data))
        localStorage.setItem('permissions', JSON.stringify(this.permissions))
        localStorage.setItem('roles', JSON.stringify(this.roles))

        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },

    /**
     * 刷新 Token
     */
    async refreshTokenAction() {
      try {
        const res = await refreshTokenApi(this.refreshToken)
        const { data } = res

        this.token = data.accessToken
        this.refreshToken = data.refreshToken
        localStorage.setItem('token', data.accessToken)
        localStorage.setItem('refreshToken', data.refreshToken)

        return res
      } catch (error) {
        // 刷新失败，清除登录状态
        this.clearLoginState()
        return Promise.reject(error)
      }
    },

    /**
     * 用户登出
     */
    async logoutAction() {
      try {
        await logout()
      } catch (error) {
        console.error('登出接口调用失败', error)
      } finally {
        this.clearLoginState()
        router.push('/login')
      }
    },

    /**
     * 清除登录状态
     */
    clearLoginState() {
      this.token = ''
      this.refreshToken = ''
      this.userInfo = {}
      this.permissions = []
      this.roles = []

      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('permissions')
      localStorage.removeItem('roles')

      // 重置路由状态
      resetRouter()
    }
  }
})