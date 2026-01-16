import request from '@/utils/request'
import { loginRequest } from '@/utils/request'


/**
 * 修改用户状态
 * @param {Object} params - 请求参数
 * @param {string|number} params.id - 用户ID
 * @param {number} params.status - 状态(0:禁用 1:启用)
 */
export function changeStatus(params) {
  return request({
    url: '/user/changeStatus',
    method: 'post',
    data: params
  })
}

/**
 * 用户登录
 * @param {Object} data - 登录参数
 * @param {string} data.account - 用户账号
 * @param {string} data.password - 用户密码
 * @returns {Promise} 返回登录请求的Promise对象
 */
export function login(data) {
  return loginRequest({
    url: '/doLogin',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

/**
 * 用户注册
 * @param {Object} data - 注册参数
 * @param {string} data.account - 用户账号
 * @param {string} data.password - 用户密码
 * @param {string} data.confirmPassword - 确认密码
 * @returns {Promise} 返回注册请求的Promise对象
 */
export function register(data) {
  return loginRequest({
    url: '/doRegister', // 修正路径
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

/**
 * 更新用户信息
 * @param {Object} data - 用户信息
 * @returns {Promise} 返回更新请求的Promise对象
 */
export function updateUserInfo(data) {
  return request({
    url: '/app/users/updateUserInfo', // 通过 app-service
    method: 'post',
    data: data
  })
}

/**
 * 修改密码
 * @param {Object} data - { uid, oldPassword, newPassword }
 */
export function changePassword(data) {
  return request({
    url: `/app/users/changePassword/${data.uid}`, // 通过 app-service
    method: 'post',
    data: data
  })
}

/**
 * 删除账号（软删除）
 * @param {number|string} uid - 用户ID
 */
export function deleteUser(uid) {
  return request({
    url: `/users/delete/${uid}`,
    method: 'delete'
  })
} 