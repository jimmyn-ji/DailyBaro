import request from '@/utils/request'

/**
 * 获取系统数据统计
 * @returns {Promise} 返回系统统计数据
 */
export function getDashboardData() {
  return request({
    url: '/admin/dashboard',
    method: 'get'
  })
}

/**
 * 获取用户列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} [params.keyword] - 搜索关键词
 * @returns {Promise} 返回用户列表数据
 */
export function getUserList(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

/**
 * 更新用户状态
 * @param {Object} data - 更新数据
 * @param {string|number} data.uid - 用户ID
 * @param {number} data.status - 状态(0:禁用 1:启用)
 * @returns {Promise}
 */
export function updateUserStatus(data) {
  return request({
    url: '/admin/user/status',
    method: 'post',
    data
  })
}

/**
 * 获取反馈列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} [params.status] - 状态筛选
 * @returns {Promise} 返回反馈列表数据
 */
export function getFeedbackList(params) {
  return request({
    url: '/admin/feedback',
    method: 'get',
    params
  })
}

/**
 * 处理反馈
 * @param {Object} data - 处理数据
 * @param {string|number} data.id - 反馈ID
 * @param {string} data.reply - 回复内容
 * @param {string} data.status - 处理状态
 * @returns {Promise}
 */
export function handleFeedback(data) {
  return request({
    url: '/admin/feedback/handle',
    method: 'post',
    data
  })
}

/**
 * 获取系统设置
 * @returns {Promise} 返回系统设置数据
 */
export function getSystemSettings() {
  return request({
    url: '/admin/settings',
    method: 'get'
  })
}

/**
 * 更新系统设置
 * @param {Object} data - 设置数据
 * @returns {Promise}
 */
export function updateSystemSettings(data) {
  return request({
    url: '/admin/settings',
    method: 'post',
    data
  })
} 