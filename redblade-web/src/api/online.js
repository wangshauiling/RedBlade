/**
 * 在线用户相关接口
 */
import { get, del } from '@/utils/request'

/**
 * 获取在线用户列表
 * @param {Object} params - 查询参数
 * @param {string} params.orgCode - 组织编码
 * @param {string} params.username - 用户名
 */
export function getOnlineUsers(params) {
  return get('/online/list', params)
}

/**
 * 获取在线用户数量
 */
export function getOnlineCount() {
  return get('/online/count')
}

/**
 * 强制用户下线
 * @param {string} token - 用户Token
 */
export function kickoutUser(token) {
  return del(`/online/${token}`)
}

/**
 * 批量强制下线
 * @param {Array<string>} tokens - Token列表
 */
export function kickoutBatch(tokens) {
  return del('/online/batch', tokens)
}