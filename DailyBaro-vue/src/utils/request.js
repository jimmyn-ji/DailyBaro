import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 使用相对路径，通过Vite代理转发到后端
const GATEWAY_BASE_URL = ''

// 用户服务
const request = axios.create({
  baseURL: GATEWAY_BASE_URL,
  timeout: 30000
})

// AI服务专用请求实例（超时时间更长，因为RAG检索和AI生成需要更多时间）
const aiRequest = axios.create({
  baseURL: GATEWAY_BASE_URL,
  timeout: 120000  // AI请求超时时间设置为120秒（2分钟）
})

// 日记服务
const diaryRequest = axios.create({
  baseURL: GATEWAY_BASE_URL,
  timeout: 30000
})

// 日签服务
const quoteRequest = axios.create({
  baseURL: GATEWAY_BASE_URL,
  timeout: 30000
})

// NLP服务（通过网关）
const nlpRequest = axios.create({
  baseURL: GATEWAY_BASE_URL,
  timeout: 30000
})

// 登录专用请求实例
const isDev = import.meta?.env?.DEV === true
const LOGIN_BASE = isDev ? '/login' : '/api/login'
const loginRequest = axios.create({
  baseURL: LOGIN_BASE, // 开发走 /login，本地Vite代理；生产走 /api/login，云端Nginx+网关
  timeout: 30000
})

// 邮件验证码专用请求实例（超时时间更长，因为邮件发送可能需要更多时间）
const emailRequest = axios.create({
  baseURL: GATEWAY_BASE_URL,
  timeout: 60000  // 邮件发送超时时间设置为60秒
})

// 通用的请求拦截器
const createRequestInterceptor = (instance) => {
  instance.interceptors.request.use(
    config => {
      // 从sessionStorage获取用户ID并添加到请求头
      const uid = window.sessionStorage.getItem('uid')
      if (uid) {
        config.headers.uid = uid
        console.log('Added uid to request headers:', uid)
      } else {
        console.warn('No uid found in sessionStorage')
      }
      console.log('Request config:', config)
      return config
    },
    error => {
      console.error('请求错误:', error)
      return Promise.reject(error)
    }
  )
}

// 通用的响应拦截器
const createResponseInterceptor = (instance) => {
  instance.interceptors.response.use(
    response => {
      const res = response.data
      
      // 调试日志：打印响应数据
      console.log('响应数据:', res)
      console.log('响应 code:', res?.code)
      console.log('响应 message:', res?.message)
      
      // 这里可以根据后端的返回码进行统一处理
      if (res.code !== 200 && res.code !== 1000) {
        const errorMsg = res.message || '请求失败'
        console.error('请求失败，code:', res.code, 'message:', errorMsg)
        ElMessage.error(errorMsg)
        
        // 处理特定错误码
        if (res.code === 401) {
          // 未登录或token过期
          router.push('/login')
        }
        // 创建一个包含响应数据的错误对象，方便调用方获取详细信息
        const error = new Error(errorMsg)
        error.response = response
        error.data = res
        error.code = res.code
        return Promise.reject(error)
      }
      return response
    },
    error => {
      console.error('响应错误:', error)
      // 如果是 HTTP 错误（如 404, 500 等），尝试从响应中获取错误信息
      if (error.response) {
        const res = error.response.data
        if (res && res.message) {
          ElMessage.error(res.message)
        } else {
          ElMessage.error(`请求失败: ${error.response.status} ${error.response.statusText}`)
        }
      } else if (error.request) {
        // 请求已发出但没有收到响应
        ElMessage.error('网络请求失败，请检查网络连接')
      } else {
        // 请求配置出错
        ElMessage.error(error.message || '网络请求失败')
      }
      return Promise.reject(error)
    }
  )
}

// 为所有实例添加拦截器
createRequestInterceptor(request)
createResponseInterceptor(request)
createRequestInterceptor(loginRequest)
createResponseInterceptor(loginRequest)
createRequestInterceptor(diaryRequest)
createResponseInterceptor(diaryRequest)
createRequestInterceptor(quoteRequest)
createResponseInterceptor(quoteRequest)
createRequestInterceptor(nlpRequest)
createResponseInterceptor(nlpRequest)
createRequestInterceptor(aiRequest)
createResponseInterceptor(aiRequest)
createRequestInterceptor(emailRequest)
createResponseInterceptor(emailRequest)

export default request
export { diaryRequest, quoteRequest, loginRequest, nlpRequest, aiRequest, emailRequest } 