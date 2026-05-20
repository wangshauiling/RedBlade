import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import NProgress from 'nprogress'
import { useUserStore } from '@/store/user'
import router from '@/router'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    NProgress.start()

    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }

    return config
  },
  error => {
    NProgress.done()
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    NProgress.done()

    const res = response.data

    // 检查 Token 是否需要刷新
    const refreshNeeded = response.headers['x-token-refresh-needed']
    if (refreshNeeded === 'true') {
      const expiresIn = response.headers['x-token-expires-in']
      console.log(`Token 即将过期，剩余 ${expiresIn} 秒，请刷新`)
      // 可以在这里触发 Token 刷新逻辑
    }

    // 业务状态码判断
    if (res.code !== 200) {
      ElMessage({
        message: res.msg || '请求失败',
        type: 'error',
        duration: 5000
      })

      // 401: Token 无效或过期
      if (res.code === 401) {
        ElMessageBox.confirm(
          '登录状态已过期，请重新登录',
          '系统提示',
          {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).then(() => {
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
        })
      }

      return Promise.reject(new Error(res.msg || '请求失败'))
    }

    return res
  },
  error => {
    NProgress.done()

    let message = '请求失败'
    if (error.response) {
      switch (error.response.status) {
        case 400:
          message = '请求参数错误'
          break
        case 401:
          message = '未授权，请登录'
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = error.response.data?.msg || '请求失败'
      }
    } else if (error.message.includes('timeout')) {
      message = '请求超时'
    } else if (error.message.includes('Network')) {
      message = '网络错误'
    }

    ElMessage({
      message,
      type: 'error',
      duration: 5000
    })

    return Promise.reject(error)
  }
)

// 封装请求方法
export function request(config) {
  return service(config)
}

export function get(url, params = {}) {
  return service({
    method: 'get',
    url,
    params
  })
}

export function post(url, data = {}) {
  return service({
    method: 'post',
    url,
    data
  })
}

export function put(url, data = {}) {
  return service({
    method: 'put',
    url,
    data
  })
}

export function del(url, params = {}) {
  return service({
    method: 'delete',
    url,
    params
  })
}

export default service