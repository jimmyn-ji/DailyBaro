// 环境配置
const ENV = {
	// 开发环境
	development: {
		// 注意：微信小程序无法访问 localhost，需要使用本机IP地址
		// 你的本机IP是: 172.20.10.4
		// 如果要在本地测试，请确保你的服务运行在 172.20.10.4:8000
		BASE_URL: 'http://172.20.10.4:8000', // 网关地址（使用本机IP）
		NLP_SERVICE_URL: 'http://172.20.10.4:5001', // 本地NLP服务地址
		NLP_PYTHON_URL: 'http://172.20.10.4:5001', // 本地Python NLP服务地址
		DEBUG: true,
		// 备用网关地址（如果本地服务不可用）
		FALLBACK_URL: 'http://172.20.10.4:8000'
	},
	// 生产环境
	production: {
		BASE_URL: 'https://dailybaro.cn', // 生产环境网关地址（HTTPS）
		NLP_SERVICE_URL: 'https://dailybaro.cn/api/emotion', // 通过网关访问NLP服务
		NLP_PYTHON_URL: 'https://dailybaro.cn:5000', // 生产环境Python服务地址（如果使用）
		DEBUG: false,
		FALLBACK_URL: 'https://dailybaro.cn'
	}
}

// 当前环境：生产环境使用云服务器
// 如需本地调试，改为 'development'
// 注意：修改后需要重新编译小程序才能生效！
const currentEnv = 'production'

// 导出配置
export const config = ENV[currentEnv]

// 开发环境特殊配置
export const isDev = currentEnv === 'development'

// 微信小程序开发工具配置
export const wxDevConfig = {
	// 开发工具中忽略域名校验
	ignoreDomainCheck: isDev,
	// 开发工具中忽略TLS版本校验
	ignoreTLSVersion: isDev,
	// 开发工具中忽略HTTPS证书校验
	ignoreHTTPSCertificate: isDev
}

// 网关健康检查
export const checkGatewayHealth = async () => {
	// 开发环境直接返回本地地址，避免复杂的健康检查
	if (isDev) {
		console.log('✅ 开发环境使用本地网关:', config.BASE_URL)
		console.log('✅ 当前环境:', currentEnv)
		return config.BASE_URL
	}
	
	try {
		// 生产环境才进行健康检查
		// 注意：小程序端可能无法直接访问 /actuator/health，改用根路径或简单路径
		const healthUrl = config.BASE_URL + (config.BASE_URL.includes('localhost') ? '/actuator/health' : '')
		const response = await uni.request({
			url: healthUrl || config.BASE_URL,
			method: 'GET',
			timeout: 5000,
			// 小程序端需要忽略 SSL 证书验证（仅开发环境）
			sslVerify: isDev
		})
		
		if (response.statusCode === 200 || response.statusCode === 404) {
			// 200 表示健康检查成功，404 可能是路径不存在但服务可用
			console.log('✅ 云服务器网关正常')
			return config.BASE_URL
		}
	} catch (error) {
		console.log('❌ 云服务器网关不可用:', error.message || error)
		// 不立即返回，继续尝试备用地址
	}
	
	// 如果云服务器不可用，尝试备用地址
	if (config.FALLBACK_URL) {
		try {
			const response = await uni.request({
				url: config.FALLBACK_URL + '/actuator/health',
				method: 'GET',
				timeout: 5000
			})
			
			if (response.statusCode === 200) {
				console.log('✅ 备用网关正常')
				return config.FALLBACK_URL
			}
		} catch (error) {
			console.log('❌ 备用网关也不可用:', error)
		}
	}
	
	console.log('❌ 所有网关都不可用，使用默认配置')
	return config.BASE_URL
}

// NLP服务健康检查
export const checkNlpServiceHealth = async () => {
	try {
		// 首先尝试Java服务
		const response = await uni.request({
			url: config.NLP_SERVICE_URL + '/actuator/health',
			method: 'GET',
			timeout: 5000
		})
		
		if (response.statusCode === 200) {
			console.log('✅ Java NLP服务正常')
			return config.NLP_SERVICE_URL
		}
	} catch (error) {
		console.log('❌ Java NLP服务不可用:', error)
	}
	
	// 如果Java服务不可用，尝试Python服务
	if (isDev && config.NLP_PYTHON_URL) {
		try {
			const response = await uni.request({
				url: config.NLP_PYTHON_URL + '/health',
				method: 'GET',
				timeout: 5000
			})
			
			if (response.statusCode === 200) {
				console.log('✅ Python NLP服务正常，使用备用地址')
				return config.NLP_PYTHON_URL
			}
		} catch (error) {
			console.log('❌ Python NLP服务也不可用:', error)
		}
	}
	
	console.log('❌ 所有NLP服务都不可用，使用默认配置')
	return config.NLP_SERVICE_URL
}

export default config
