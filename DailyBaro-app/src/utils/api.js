// API基础配置
import { config, checkGatewayHealth } from './config.js'

let BASE_URL = config.BASE_URL // 动态网关地址
const NLP_SERVICE_URL = config.NLP_SERVICE_URL // 从配置文件获取NLP服务地址
const API_PREFIX = '' // 移除/api前缀，避免重复

// 初始化网关地址
const initGateway = async () => {
	try {
		BASE_URL = await checkGatewayHealth()
		console.log('✅ 初始化网关地址:', BASE_URL)
		console.log('✅ 配置的BASE_URL:', config.BASE_URL)
		if (BASE_URL !== config.BASE_URL) {
			console.warn('⚠️ 网关地址与配置不一致，使用:', BASE_URL)
		}
	} catch (error) {
		console.error('❌ 网关初始化失败:', error)
		// 使用默认配置
		BASE_URL = config.BASE_URL
		console.log('✅ 使用默认配置:', BASE_URL)
	}
}

// 请求拦截器
const request = (options) => {
	return new Promise(async (resolve, reject) => {
		// 确保网关已初始化
		if (!BASE_URL || BASE_URL === config.BASE_URL) {
			await initGateway()
		}
		
		const userInfo = uni.getStorageSync('userInfo')
		const uid = userInfo ? (userInfo.uid || userInfo.id || userInfo.userId) : null
		
		// 添加请求日志
		if (config.DEBUG) {
			console.log('API请求:', {
				url: BASE_URL + API_PREFIX + options.url,
				method: options.method || 'GET',
				data: options.data || {},
				gateway: BASE_URL
			})
		}
		
		const method = (options.method || 'GET').toUpperCase()
		
		// 确保POST请求的data正确传递
		// 在微信小程序中，uni.request 会自动序列化 data，但需要确保 data 不是 undefined
		let finalData = options.data
		if (method === 'POST' || method === 'PUT' || method === 'PATCH') {
			if (finalData === undefined || finalData === null) {
				finalData = {}
				console.warn('POST请求data为空，使用空对象')
			} else {
				// 确保 data 是一个普通对象，而不是其他类型
				if (typeof finalData !== 'object' || Array.isArray(finalData)) {
					console.warn('POST请求data类型异常:', typeof finalData)
					// 如果类型异常，尝试转换
					if (typeof finalData === 'string') {
						try {
							finalData = JSON.parse(finalData)
						} catch (e) {
							console.error('无法解析data字符串:', e)
							finalData = {}
						}
					} else {
						finalData = {}
					}
				} else {
					console.log('POST请求data:', JSON.stringify(finalData))
					console.log('POST请求data keys:', Object.keys(finalData))
					console.log('POST请求data.code:', finalData.code)
					console.log('POST请求data.code 类型:', typeof finalData.code)
					console.log('POST请求data.code 值:', finalData.code)
					// 确保 code 字段存在且不为空
					if (finalData.code === undefined || finalData.code === null || finalData.code === '') {
						console.error('警告：finalData.code 为空或未定义！')
					}
				}
			}
		}
		
		console.log('发送请求:', {
			url: BASE_URL + API_PREFIX + options.url,
			method: method,
			data: finalData,
			dataType: typeof finalData,
			dataKeys: finalData ? Object.keys(finalData) : [],
			dataString: JSON.stringify(finalData),
			hasCode: finalData && finalData.code ? 'YES: ' + finalData.code : 'NO'
		})
		
		// 构建请求配置
		// 在微信小程序中，uni.request 会自动序列化 data，但需要确保 data 是对象类型
		const requestConfig = {
			url: BASE_URL + API_PREFIX + options.url,
			method: method,
			header: {
				'Content-Type': 'application/json',
				'uid': uid || '',
				...options.header
			},
			dataType: 'json',
			timeout: 10000
		}
		
		// 对于 POST 请求，确保 Content-Type 正确设置
		if (method === 'POST' || method === 'PUT' || method === 'PATCH') {
			// 确保 Content-Type 是 application/json
			if (!requestConfig.header['Content-Type']) {
				requestConfig.header['Content-Type'] = 'application/json'
			}
		}
		
		// 确保 data 正确设置
		if (method === 'GET') {
			requestConfig.data = finalData || {}
		} else {
			// POST/PUT/PATCH 请求，确保 data 存在且是对象
			if (finalData && typeof finalData === 'object' && !Array.isArray(finalData)) {
				requestConfig.data = finalData
			} else {
				requestConfig.data = finalData || {}
			}
		}
		
		console.log('uni.request 最终配置:', {
			url: requestConfig.url,
			method: requestConfig.method,
			data: requestConfig.data,
			dataType: typeof requestConfig.data,
			dataKeys: requestConfig.data ? Object.keys(requestConfig.data) : [],
			dataString: JSON.stringify(requestConfig.data),
			hasCode: requestConfig.data && requestConfig.data.code ? 'YES: ' + requestConfig.data.code : 'NO',
			header: requestConfig.header
		})
		
		// 在微信小程序中，对于POST请求，uni.request会自动序列化对象为JSON字符串
		// 但需要确保 data 是对象类型，不是字符串
		let requestData = requestConfig.data
		
		// 对于 POST/PUT/PATCH 请求，确保 data 是对象类型
		if ((method === 'POST' || method === 'PUT' || method === 'PATCH') && requestData) {
			// 如果 data 是字符串，尝试解析
			if (typeof requestData === 'string') {
				try {
					requestData = JSON.parse(requestData)
				} catch (e) {
					console.warn('data 是字符串但无法解析为JSON:', e)
				}
			}
		}
		
		console.log('最终调用 uni.request，data:', requestData)
		console.log('data 序列化:', JSON.stringify(requestData))
		console.log('data.code 值:', requestData && requestData.code ? requestData.code : 'NO CODE')
		console.log('data.code 类型:', requestData && requestData.code ? typeof requestData.code : 'NO CODE')
		console.log('Content-Type:', requestConfig.header['Content-Type'])
		console.log('完整 header:', requestConfig.header)
		
		// 在微信小程序中，uni.request 会自动序列化对象为 JSON
		// 但需要确保 Content-Type 正确设置
		const finalHeaders = { ...requestConfig.header }
		if ((method === 'POST' || method === 'PUT' || method === 'PATCH') && requestData) {
			// 确保 Content-Type 是 application/json
			finalHeaders['Content-Type'] = 'application/json'
			// 确保 data 是对象，不是字符串
			if (typeof requestData === 'string') {
				try {
					requestData = JSON.parse(requestData)
				} catch (e) {
					console.warn('data 是字符串但无法解析为JSON:', e)
				}
			}
			// 重要：确保 requestData 不是空对象，并且有实际内容
			if (requestData && typeof requestData === 'object' && Object.keys(requestData).length === 0) {
				console.warn('警告：requestData 是空对象，可能导致后端接收不到数据')
			}
		}
		
		console.log('最终发送的 data:', requestData)
		console.log('最终发送的 data 类型:', typeof requestData)
		console.log('最终发送的 data 键:', requestData ? Object.keys(requestData) : [])
		console.log('最终发送的 data JSON:', JSON.stringify(requestData))
		console.log('最终发送的 header:', finalHeaders)
		
		// 在微信小程序中，uni.request 的 data 参数如果是对象，会自动序列化为 JSON
		// 但需要确保 data 不是 undefined 或 null
		// 重要：对于 POST 请求，确保 data 是对象类型，uni.request 会自动序列化
		let requestDataFinal = requestData !== undefined && requestData !== null ? requestData : {}
		
		// 确保 requestDataFinal 是对象类型，不是字符串
		if ((method === 'POST' || method === 'PUT' || method === 'PATCH') && requestDataFinal) {
			if (typeof requestDataFinal === 'string') {
				try {
					requestDataFinal = JSON.parse(requestDataFinal)
				} catch (e) {
					console.warn('requestDataFinal 是字符串但无法解析为JSON:', e)
				}
			}
			// 确保是普通对象，不是数组或其他类型
			if (typeof requestDataFinal !== 'object' || Array.isArray(requestDataFinal)) {
				console.warn('requestDataFinal 类型异常，转换为对象:', typeof requestDataFinal)
				requestDataFinal = {}
			}
		}
		
		console.log('最终传递给 uni.request 的 data:', requestDataFinal)
		console.log('最终传递给 uni.request 的 data JSON:', JSON.stringify(requestDataFinal))
		console.log('最终传递给 uni.request 的 data 类型:', typeof requestDataFinal)
		console.log('最终传递给 uni.request 的 data 键数量:', requestDataFinal ? Object.keys(requestDataFinal).length : 0)
		console.log('最终传递给 uni.request 的完整配置:', {
			url: requestConfig.url,
			method: requestConfig.method,
			data: requestDataFinal,
			header: finalHeaders
		})
		
		uni.request({
			url: requestConfig.url,
			method: requestConfig.method,
			data: requestDataFinal,
			header: finalHeaders,
			dataType: requestConfig.dataType,
			timeout: requestConfig.timeout,
			success: (res) => {
				if (config.DEBUG) {
					console.log('API响应:', {
						url: options.url,
						status: res.statusCode,
						data: res.data
					})
				}
				
				if (res.statusCode === 200) {
					resolve(res.data)
				} else if (res.statusCode === 401) {
					// 未授权，跳转登录
					uni.removeStorageSync('userInfo')
					uni.showToast({
						title: '登录已过期，请重新登录',
						icon: 'none'
					})
					setTimeout(() => {
						uni.reLaunch({
							url: '/pages/login/login'
						})
					}, 1500)
					reject(new Error('登录已过期'))
				} else if (res.statusCode === 404) {
					// 404错误，可能是网关问题
					console.error('404错误，尝试切换网关')
					reject(new Error(`接口不存在: ${options.url}，请检查网关配置`))
				} else {
					reject(new Error(res.data?.message || `请求失败: ${res.statusCode}`))
				}
			},
			fail: (err) => {
				console.error('API请求失败:', {
					url: options.url,
					error: err,
					gateway: BASE_URL
				})
				
				// 如果是网络错误，尝试切换网关
				if (err.errMsg && err.errMsg.includes('fail')) {
					console.log('🔄 尝试切换网关...')
					initGateway().then(() => {
						// 重新发起请求
						request(options).then(resolve).catch(reject)
					}).catch(() => {
						reject(new Error('网关切换失败，请检查网络连接'))
					})
				} else {
					reject(new Error(err.errMsg || '网络请求失败'))
				}
			}
		})
	})
}

// NLP服务请求（本地服务）
const nlpRequest = (options) => {
	return new Promise((resolve, reject) => {
		uni.request({
			url: NLP_SERVICE_URL + options.url,
			method: options.method || 'POST',
			data: options.data || {},
			header: {
				'Content-Type': 'application/json',
				...options.header
			},
			success: (res) => {
				// 健康检查允许非200状态码，不抛出错误
				if (options.url === '/api/health') {
					if (res.statusCode === 200 || res.statusCode === 426) {
						resolve({ available: res.statusCode === 200, status: res.statusCode })
					} else {
						resolve({ available: false, status: res.statusCode })
					}
				} else if (res.statusCode === 200) {
					resolve(res.data)
				} else {
					reject(new Error(res.data?.message || `NLP服务请求失败: ${res.statusCode}`))
				}
			},
			fail: (err) => {
				// 健康检查失败时返回不可用状态，不抛出错误
				if (options.url === '/api/health') {
					resolve({ available: false, error: err.errMsg })
				} else {
					reject(new Error(err.errMsg || 'NLP服务连接失败'))
				}
			}
		})
	})
}

// 用户相关API
export const userApi = {
	// 用户注册
	register: (data) => {
		return request({
			url: '/api/login/doRegister',
			method: 'POST',
			data
		})
	},
	
	// 用户登录
	login: (data) => {
		return request({
			url: '/api/login/doLogin',
			method: 'POST',
			data
		})
	},
	
	// 微信登录（通过 app-service 转发到后端 /login/wxLogin）
	wechatLogin: (data) => {
		return request({
			url: '/app/login/wxLogin',
			method: 'POST',
			data
		})
	},
	
	// 获取用户信息
	getUserInfo: (uid) => {
		return request({
			url: `/app/users/getMyInfo/${uid}`,
			method: 'GET'
		})
	},
	
	// 更新用户信息
	updateUserInfo: (uid, data) => {
		return request({
			url: `/app/users/updateUserInfo`,
			method: 'POST',
			data: { ...data, uid }
		})
	},
	
	// 删除用户
	deleteUser: (uid) => {
		return request({
			url: `/api/user/delete/${uid}`,
			method: 'DELETE'
		})
	},

	// 获取推荐列表（使用 knowledge-service 的推荐接口）
	getRecommendations: () => {
		return request({
			url: '/api/knowledge/recommendation/by-emotion?days=7',
			method: 'GET'
		})
	},
	
	// 执行活动
	performActivity: (uid, activityId) => {
		return request({
			url: '/app/activities/perform',
			method: 'POST',
			data: {
				uid: uid,
				activityId: activityId
			}
		})
	}
}

// 日记相关API
export const diaryApi = {
	// 获取日记列表
	getDiaries: (params = {}) => {
		// 支持两种调用方式：
		// 1. getDiaries(uid, status) - 向后兼容
		// 2. getDiaries({uid, status}) - 新的对象参数方式
		let queryParams = {}
		if (typeof params === 'string' || typeof params === 'number') {
			// 向后兼容：第一个参数是uid
			const uid = params
			const status = arguments[1] || 'all'
			queryParams = { uid, status }
		} else {
			// 新的对象参数方式
			queryParams = params
		}
		
		return request({
			url: `/api/diary`,
			method: 'GET',
			data: queryParams
		})
	},
	
	// 获取日记详情
	getDiaryDetail: (id) => {
		return request({
			url: `/app/diary/detail/${id}`,
			method: 'GET'
		})
	},
	
	// 创建日记
	createDiary: (data) => {
		return request({
			url: '/api/diary',
			method: 'POST',
			data
		})
	},
	
	// 更新日记
	updateDiary: (id, data) => {
		return request({
			url: `/api/diary/${id}`,
			method: 'PUT',
			data
		})
	},
	
	// 删除日记
	deleteDiary: (id) => {
		return request({
			url: `/api/diary/${id}`,
			method: 'DELETE'
		})
	},
	
	// 按标签查询日记
	searchByTag: (uid, tag) => {
		return request({
			url: `/api/diary/search`,
			method: 'GET',
			data: { uid, tag }
		})
	}
}

// 匿名星球API
export const anonymousApi = {
	// 获取动态列表
	getPosts: (page = 1, size = 20) => {
		return request({
			url: `/app/anonymous-posts`,
			method: 'GET',
			data: { page, size }
		})
	},
	
	// 发布动态
	createPost: (data) => {
		return request({
			url: '/app/anonymous-posts',
			method: 'POST',
			data
		})
	},
	
	// 点赞动态
	likePost: (postId) => {
		return request({
			url: `/app/anonymous-posts/${postId}/like`,
			method: 'POST'
		})
	},
	
	// 评论动态
	commentPost: (postId, data) => {
		return request({
			url: `/app/anonymous-posts/${postId}/comment`,
			method: 'POST',
			data
		})
	}
}

// 情绪分析API
export const emotionApi = {
	// 分析情绪
	analyzeEmotion: (data) => {
		return request({
			url: '/app/emotion/analysis',
			method: 'POST',
			data
		})
	},
	
	// 基于日记附件分析情绪
	analyzeFromDiary: (diaryId, type) => {
		return request({
			url: '/app/emotion/analysis/from-diary',
			method: 'POST',
			data: { diaryId, type }
		})
	},
	
	// 获取分析历史
	getAnalysisHistory: (uid) => {
		// 兼容后端接口：改为 /api/analysis/result 或 /app/analysis/result 二选一
		return request({
			url: `/app/analysis/result`,
			method: 'GET',
			data: { uid }
		}).catch(() => {
			return request({
				url: `/api/analysis/result`,
				method: 'GET',
				data: { uid }
			})
		})
	},
	
	// 获取情绪统计
	getEmotionStats: (uid, period = 'week') => {
		return request({
			url: `/api/emotion/visualization`,
			method: 'GET',
			data: { uid, period }
		})
	},
	
	// 导出分析报告
	exportReport: (uid, format = 'pdf') => {
		return request({
			url: `/api/emotion/export`,
			method: 'POST',
			data: { uid, format }
		})
	}
}

// NLP情绪识别API（本地服务）
export const nlpEmotionApi = {
	// 智能情绪分析
	analyzeEmotion: (text) => {
		return nlpRequest({
			url: '/api/nlp/emotion/analyze',
			method: 'POST',
			data: { text }
		})
	},
	
	// 批量情绪分析
	batchAnalyzeEmotion: (texts) => {
		return nlpRequest({
			url: '/api/nlp/emotion/batch-analyze',
			method: 'POST',
			data: { texts }
		})
	},
	
	// 情绪分类
	classifyEmotion: (text, topK = 3) => {
		return nlpRequest({
			url: '/api/nlp/emotion/classify',
			method: 'POST',
			data: { text, top_k: topK }
		})
	},
	
	// 获取服务信息
	getServiceInfo: () => {
		return nlpRequest({
			url: '/api/nlp/emotion/info',
			method: 'GET'
		})
	},
	
	// 健康检查
	healthCheck: () => {
		return nlpRequest({
			url: '/api/health',
			method: 'GET'
		})
	}
}

// 情绪胶囊API
export const capsuleApi = {
	// 获取胶囊列表
	getCapsules: (uid, status = 'all') => {
		return request({
			url: `/app/capsules`,
			method: 'GET',
			data: { uid, status }
		})
	},
	
	// 创建胶囊
	createCapsule: (data) => {
		return request({
			url: '/app/capsules/json',
			method: 'POST',
			data
		})
	},
	
	// 开启胶囊
	openCapsule: (id) => {
		return request({
			url: `/app/capsules/${id}/open`,
			method: 'POST'
		})
	},
	
	// 删除胶囊
	deleteCapsule: (id) => {
		return request({
			url: `/app/capsules/${id}`,
			method: 'DELETE'
		})
	},
	
	// 获取未读提醒
	getUnreadReminders: () => {
		return request({
			url: '/app/capsules/reminders/unread',
			method: 'GET'
		})
	},
	
	// 标记提醒为已读
	markReminderRead: (id) => {
		return request({
			url: `/app/capsules/reminders/${id}/read`,
			method: 'POST'
		})
	}
}

// 神秘盒子API
export const mysteryBoxApi = {
	// 获取用户能量值
	getUserEnergy: (uid) => {
		return request({
			url: `/api/mystery-box/energy`,
			method: 'GET',
			data: { uid }
		})
	},
	
	// 抽取盲盒
	drawBox: (uid) => {
		return request({
			url: '/api/mystery-box/draw',
			method: 'POST',
			data: { uid }
		})
	},
	
	// 完成任务
	completeTask: (taskId) => {
		return request({
			url: `/api/mystery-box/complete/${taskId}`,
			method: 'POST'
		})
	},
	
	// 获取抽取历史
	getDrawHistory: (uid) => {
		return request({
			url: `/api/mystery-box/records`,
			method: 'GET',
			data: { uid }
		})
	}
}

// 日签API
export const quoteApi = {
	// 获取今日日签
	getDailyQuote: () => {
		return request({
			url: '/api/quotes/custom',
			method: 'GET'
		})
	},
	
	// 获取用户日签
	getUserQuotes: (uid) => {
		return request({
			url: `/api/quotes/random/user`,
			method: 'GET',
			data: { uid }
		})
	},
	
	// 创建日签
	createQuote: (data) => {
		return request({
			url: '/api/quotes/custom',
			method: 'POST',
			data
		})
	},
	
	// 更新日签
	updateQuote: (id, data) => {
		return request({
			url: `/api/quotes/custom/${id}`,
			method: 'PUT',
			data
		})
	},
	
	// 删除日签
	deleteQuote: (id) => {
		return request({
			url: `/api/quotes/custom/${id}`,
			method: 'DELETE'
		})
	},
	
	// 获取日签历史
	getQuoteHistory: (uid) => {
		return request({
			url: '/api/quotes/history',
			method: 'GET',
			data: { uid }
		})
	}
}

// AI助手API
export const aiApi = {
	// 智能对话
	chat: (data) => {
		return request({
			url: '/app/ai/query',
			method: 'POST',
			data: {
				message: data.message || data.question || '',
				timestamp: data.timestamp || Date.now()
			}
		})
	},
	
	// 基于日记的智能分析
	diaryAnalysis: (diaryContent) => {
		return request({
			url: '/app/ai/query',
			method: 'POST',
			data: { 
				message: `请分析以下日记内容并给出情绪建议：${diaryContent}`,
				timestamp: Date.now()
			}
		})
	},
	
	// 获取常见问题
	getCommonQuestions: () => {
		return request({
			url: '/app/ai/query',
			method: 'POST',
			data: { 
				message: '请给我一些常见的心理健康问题建议',
				timestamp: Date.now()
			}
		})
	}
}

// 知识库API
export const knowledgeApi = {
	// 搜索知识（支持RAG）
	search: (data) => {
		return request({
			url: '/app/knowledge/search',
			method: 'POST',
			data: {
				query: data.query || '',
				useRAG: data.useRAG !== false,
				page: data.page || 1,
				size: data.size || 20
			}
		})
	},
	
	// 获取知识详情
	getDetail: (id) => {
		return request({
			url: '/app/knowledge/' + id,
			method: 'GET'
		})
	},
	
	// 按分类获取
	getByCategory: (category) => {
		return request({
			url: '/app/knowledge/category/' + encodeURIComponent(category),
			method: 'GET'
		})
	},
	
	// 按子分类获取
	getBySubcategory: (category, subcategory) => {
		return request({
			url: '/app/knowledge/category/' + encodeURIComponent(category) + 
				'/subcategory/' + encodeURIComponent(subcategory),
			method: 'GET'
		})
	}
}

// 个性化推荐API（使用 knowledge-service 的推荐接口）
export const recommendationApi = {
    get: () => {
        return request({
            url: '/api/knowledge/recommendation/by-emotion?days=7',
            method: 'GET'
        })
    }
}

// 文件上传API
export const fileApi = {
	// 上传文件
	uploadFile: (filePath, type = 'image') => {
		return new Promise((resolve, reject) => {
			const userInfo = uni.getStorageSync('userInfo')
			const uid = userInfo ? userInfo.uid : null
			
			uni.uploadFile({
				url: BASE_URL + '/api/uploads/media',
				filePath: filePath,
				name: 'file',
				header: {
					'uid': uid || ''
				},
				formData: {
					type: type
				},
				success: (res) => {
					try {
						const data = JSON.parse(res.data)
						if (data.code === 200) {
							resolve(data)
						} else {
							reject(new Error(data.message || '上传失败'))
						}
					} catch (e) {
						reject(new Error('上传失败'))
					}
				},
				fail: (err) => {
					reject(new Error(err.errMsg || '上传失败'))
				}
			})
		})
	}
}

export default {
	userApi,
	diaryApi,
	anonymousApi,
	emotionApi,
	nlpEmotionApi,
	capsuleApi,
	mysteryBoxApi,
	quoteApi,
	aiApi,
	recommendationApi,
	fileApi
}
