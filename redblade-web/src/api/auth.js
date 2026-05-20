/**
 * 认证相关接口
 */
import { get, post } from '@/utils/request'

/**
 * 获取验证码
 */
export function getCaptcha() {
  return get('/auth/captcha')
}

/**
 * 用户登录
 * @param {Object} data - 登录参数
 * @param {string} data.orgCode - 组织编码
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {string} data.captcha - 验证码
 * @param {string} data.uuid - 验证码UUID
 */
export function login(data) {
  return post('/auth/login', data)
}

/**
 * 用户登出
 */
export function logout() {
  return post('/auth/logout')
}

/**
 * 刷新Token
 * @param {string} refreshToken - 刷新令牌
 */
export function refreshToken(refreshToken) {
  return post('/auth/refresh', { refreshToken })
}

/**
 * 获取当前用户信息
 */
export function getUserInfo() {
  return get('/auth/userinfo')
}

/**
 * 用户注册
 * @param {Object} data - 注册参数
 */
export function register(data) {
  return post('/auth/register', data)
}

/**
 * 修改密码
 * @param {Object} data - 密码参数
 * @param {string} data.oldPassword - 旧密码
 * @param {string} data.newPassword - 新密码
 */
export function changePassword(data) {
  return post('/auth/changePassword', data)
}