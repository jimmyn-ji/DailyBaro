// 小程序专用配置文件
// 简化版本，避免复杂的网关检测

// 开发环境配置
const DEV_CONFIG = {
	BASE_URL: 'http://localhost:8000',
	NLP_SERVICE_URL: 'http://localhost:5001',
	DEBUG: true
}

// 生产环境配置
const PROD_CONFIG = {
	BASE_URL: 'http://129.211.94.73',
	NLP_SERVICE_URL: 'http://129.211.94.73/api/emotion',
	DEBUG: false
}

// 当前环境
const currentEnv = process.env.NODE_ENV || 'development'
const config = currentEnv === 'development' ? DEV_CONFIG : PROD_CONFIG

// 获取基础URL
export const getBaseUrl = () => {
	return config.BASE_URL
}

// 获取NLP服务URL
export const getNlpServiceUrl = () => {
	return config.NLP_SERVICE_URL
}

// 是否调试模式
export const isDebug = () => {
	return config.DEBUG
}

// 导出配置
export { config }

export default config
