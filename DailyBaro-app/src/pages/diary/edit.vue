<template>
	<view class="edit-container">
		<!-- 顶部导航栏 -->
		<view class="nav-bar">
			<!-- 修改后的返回按钮 -->
			<view class="back-btn" @tap="goBack">
				<text class="back-icon">←</text>
			</view>
			<text class="page-title">{{ isEdit ? '编辑日记' : '写日记' }}</text>
			<view class="placeholder"></view> <!-- 用于保持布局平衡 -->
		</view>
		
		<view class="content">
			<!-- 日记内容输入 -->
			<view class="input-section">
			<textarea 
				class="diary-input" 
				placeholder="写下今天的心情和想法..." 
				v-model="diaryContent"
				@input="onContentInput"
				maxlength="1000"
				auto-height
			></textarea>
				<text class="char-count">{{ diaryContent.length }}/1000</text>
			</view>
			
			<!-- 日记设置 -->
			<view class="settings-section">
				<view class="setting-item">
					<text class="setting-label">日期</text>
					<picker 
						mode="date" 
						:value="diaryDate" 
						@change="onDateChange"
					>
						<view class="picker-field">
							<text class="picker-text">{{ diaryDate }}</text>
							<text class="picker-arrow">›</text>
						</view>
					</picker>
				</view>
				
				<view class="setting-item emotion-setting-item">
					<text class="setting-label">情绪标签</text>
					<view class="emotion-section">
						<!-- 显示当前识别的情绪 -->
						<view class="emotion-display">
							<text class="emotion-tag">{{ selectedEmotion || '等待识别...' }}</text>
							<text v-if="analyzing" class="analyzing-text">识别中...</text>
						</view>
					</view>
				</view>
				
				<!-- 多模态情绪分析 - 单独一行 -->
				<view class="setting-item multimodal-setting-item">
					<text class="setting-label">多模态情绪分析</text>
					<view class="multimodal-section">
						<!-- 只有当没有选择任何附件时才显示按钮 -->
						<view class="multimodal-buttons" v-if="!selectedImage && !selectedAudio">
							<button 
								class="multimodal-btn cv-btn" 
								@tap="chooseImageForAnalysis"
								:disabled="analyzing"
							>
								<text class="btn-icon">📷</text>
								<text class="btn-text">图像分析</text>
							</button>
							<button 
								class="multimodal-btn audio-btn" 
								@tap="chooseAudioForAnalysis"
								:disabled="analyzing"
							>
								<text class="btn-icon">🎵</text>
								<text class="btn-text">音频分析</text>
							</button>
						</view>
						
						<!-- 图像预览 -->
						<view v-if="selectedImage" class="image-preview-section">
							<view class="image-preview-header">
								<text class="preview-title">已选择图片</text>
								<text class="preview-actions">
									<text class="action-link" @tap="chooseImageForAnalysis">重新选择</text>
									<text class="action-link delete" @tap="removeSelectedImage">删除</text>
								</text>
							</view>
							<image :src="selectedImage" class="preview-image" mode="aspectFit"></image>
							<button 
								class="analyze-image-btn" 
								@tap="analyzeImageEmotion"
								:loading="analyzingImage"
								:disabled="analyzingImage"
							>
								{{ analyzingImage ? '分析中...' : '分析图片情绪' }}
							</button>
						</view>
						
						<!-- 音频预览 -->
						<view v-if="selectedAudio" class="audio-preview-section">
							<view class="audio-preview-header">
								<text class="preview-title">已选择音频</text>
								<text class="preview-actions">
									<text class="action-link" @tap="chooseAudioForAnalysis">重新选择</text>
									<text class="action-link delete" @tap="removeSelectedAudio">删除</text>
								</text>
							</view>
							<view class="audio-info">
								<text class="audio-name">{{ selectedAudio.name }}</text>
								<text class="audio-size">{{ formatFileSize(selectedAudio.size) }}</text>
							</view>
							<button 
								class="analyze-audio-btn" 
								@tap="analyzeAudioEmotion"
								:loading="analyzingAudio"
								:disabled="analyzingAudio"
							>
								{{ analyzingAudio ? '分析中...' : '分析音频情绪' }}
							</button>
						</view>
						
						<!-- 多模态分析结果 -->
						<view v-if="multimodalResult" class="multimodal-result">
							<view class="result-header">
								<text class="result-title">多模态情绪分析</text>
								<text class="close-btn" @tap="closeMultimodalResult">×</text>
							</view>
							<view class="result-content">
								<view v-if="multimodalResult.text" class="result-item">
									<text class="result-label">文本情绪：</text>
									<text class="result-value">{{ multimodalResult.text.emotion }}</text>
								</view>
								<view v-if="multimodalResult.image" class="result-item">
									<text class="result-label">图像情绪：</text>
									<text class="result-value">{{ multimodalResult.image.emotion }}</text>
								</view>
								<view v-if="multimodalResult.audio" class="result-item">
									<text class="result-label">音频情绪：</text>
									<text class="result-value">{{ multimodalResult.audio.emotion }}</text>
								</view>
								<view v-if="multimodalResult.fused" class="result-item">
									<text class="result-label">融合情绪：</text>
									<text class="result-value">{{ multimodalResult.fused.emotion }}</text>
								</view>
							</view>
						</view>
					</view>
				</view>
			</view>
			
			<!-- 操作按钮 -->
			<view class="action-section">
				<button class="draft-btn" @tap="saveAsDraft">保存草稿</button>
				<button class="publish-btn" @tap="publishDiary">发布日记</button>
			</view>
		</view>
	</view>
</template>

<script>
import { diaryApi, fileApi } from '../../utils/api.js'
import { formatDateToStandard } from '../../utils/dateUtils.js'
import { config } from '../../utils/config.js'

export default {
		data() {
		return {
			diaryContent: '',
			diaryDate: '',
			emotionTags: ['开心', '平静', '兴奋', '焦虑', '悲伤', '愤怒', '困惑', '满足', '放松', '紧张', '期待', '信任', '消极', '积极', '中性'],
			selectedEmotion: '等待识别...', // 自动识别的情绪标签
			isEdit: false,
			diaryId: null,
			analyzing: false,
			emotionResult: null,
			selectedImage: null,
			selectedAudio: null,
			selectedVideo: null,
			analyzingImage: false,
			analyzingAudio: false,
			multimodalResult: null,
			autoAnalyzeTimer: null // 自动分析定时器（防抖）
		}
	},
	
	onLoad(options) {
		console.log('编辑页面参数:', options)
		this.initPage(options)
	},
	
	methods: {
		// 获取基础URL
		getBaseUrl() {
			// 使用配置文件中的BASE_URL
			return config.BASE_URL || 'https://dailybaro.cn'
		},
		
		// 获取完整URL（处理相对路径）
		getFullUrl(url) {
			if (!url) {
				return ''
			}
			
			// 如果已经是完整URL，直接返回
			if (url.startsWith('http://') || url.startsWith('https://')) {
				return url
			}
			
			// 处理前端静态资源路径
			if (url.startsWith('/src/static/') || url.startsWith('src/static/')) {
				const filePath = url.replace(/^\/?src\/static\//, '/api/uploads/static/')
				const baseUrl = this.getBaseUrl()
				return baseUrl + filePath
			}
			
			// 处理上传文件的路径
			// 根据网关配置，文件访问路径应该是 /api/uploads/ 或 /uploads/
			// 优先使用 /api/uploads/ 路径
			let filePath = url
			if (url.startsWith('/api/uploads/')) {
				filePath = url
			} else if (url.startsWith('/uploads/')) {
				// 如果已经是 /uploads/，尝试使用 /api/uploads/ 路径
				filePath = '/api/uploads/' + url.substring('/uploads/'.length)
			} else if (url.startsWith('uploads/')) {
				filePath = '/api/uploads/' + url.substring('uploads/'.length)
			} else if (!url.startsWith('/api/') && !url.startsWith('http') && !url.startsWith('/')) {
				filePath = '/api/uploads/' + url
			} else if (url.startsWith('/') && !url.startsWith('/api/') && !url.startsWith('/uploads/')) {
				filePath = '/api/uploads' + url
			}
			
			// 使用配置的BASE_URL
			const baseUrl = this.getBaseUrl()
			const fullUrl = baseUrl + filePath
			console.log('edit.vue getFullUrl: 拼接URL:', url, '->', fullUrl)
			return fullUrl
		},
		
		initPage(options) {
			// 设置当前日期
			const today = new Date()
			this.diaryDate = today.toISOString().split('T')[0]
			
			// 检查是否是编辑模式
			if (options && options.id) {
				this.isEdit = true
				this.diaryId = options.id
				console.log('编辑模式，日记ID:', this.diaryId)
				this.loadDiaryData()
			}
		},
		
		async loadDiaryData() {
			try {
				const response = await diaryApi.getDiaryDetail(this.diaryId)
				if (response && response.code === 200) {
					const diary = response.data
					this.diaryContent = diary.content || ''
					
					// 处理日期格式
					if (diary.createTime) {
						const dateStr = formatDateToStandard(diary.createTime)
						if (dateStr) {
							this.diaryDate = dateStr
						}
					}
					
					// 处理情绪标签（直接使用日记中的情绪）
					this.selectedEmotion = diary.emotion || '等待识别...'
					
				}
			} catch (error) {
				console.error('加载日记数据失败:', error)
				uni.showToast({
					title: '加载失败',
					icon: 'none'
				})
			}
		},
		
		goBack() {
			uni.navigateBack()
		},
		
		onDateChange(e) {
			this.diaryDate = e.detail.value
		},
		
		// 文本输入监听（自动情绪识别）
		onContentInput(e) {
			this.diaryContent = e.detail.value
			// 防抖：延迟1秒后自动识别情绪
			if (this.autoAnalyzeTimer) {
				clearTimeout(this.autoAnalyzeTimer)
			}
			
			// 如果内容少于10个字，不识别
			if (!this.diaryContent || this.diaryContent.trim().length < 10) {
				this.selectedEmotion = '等待识别...'
				return
			}
			
			// 延迟1秒后自动识别
			this.autoAnalyzeTimer = setTimeout(() => {
				this.autoAnalyzeEmotion()
			}, 1000)
		},
		
		// 自动情绪识别（文本）
		async autoAnalyzeEmotion() {
			if (!this.diaryContent || this.diaryContent.trim().length < 10) {
				return
			}
			
			this.analyzing = true
			try {
				const baseUrl = this.getBaseUrl()
				const userInfo = uni.getStorageSync('userInfo')
				const uid = userInfo ? userInfo.uid : ''
				
				if (!uid) {
					return
				}
				
				const response = await uni.request({
					url: baseUrl + '/app/emotion/analysis',
					method: 'POST',
					header: {
						'Content-Type': 'application/json',
						'uid': uid
					},
					data: {
						text: this.diaryContent
					}
				})
				
				if (response.data && response.data.code === 200) {
					const data = response.data.data
					// 支持多种可能的字段名
					const emotion = data?.emotion || data?.primaryEmotion || data?.label
					if (emotion) {
						// 映射情绪到标签
						const mappedEmotion = this.mapEmotionToTag(emotion)
						this.selectedEmotion = mappedEmotion
						this.emotionResult = data
						console.log('情绪识别成功:', { 原始: emotion, 映射后: mappedEmotion, 完整数据: data })
					} else {
						console.warn('情绪识别响应中未找到情绪字段:', data)
					}
				} else {
					console.warn('情绪识别失败:', response.data)
				}
			} catch (error) {
				console.error('自动情绪识别失败:', error)
			} finally {
				this.analyzing = false
			}
		},
		
		// 映射后端返回的情绪到前端标签
		mapEmotionToTag(emotion) {
			if (!emotion) return '等待识别...'
			
			// 统一转换为小写进行匹配
			const emotionLower = emotion.toLowerCase().trim()
			
			// 情绪映射表（包含中英文、同义词）
			const emotionMap = {
				// 积极情绪
				'开心': '开心',
				'快乐': '开心',
				'高兴': '开心',
				'愉快': '开心',
				'positive': '开心',
				'积极': '开心',
				'happy': '开心',
				'joy': '开心',
				'joyful': '开心',
				
				// 中性情绪
				'平静': '平静',
				'冷静': '平静',
				'平和': '平静',
				'neutral': '平静',
				'中性': '平静',
				'calm': '平静',
				'peaceful': '平静',
				
				// 兴奋情绪
				'兴奋': '兴奋',
				'激动': '兴奋',
				'excited': '兴奋',
				'excitement': '兴奋',
				
				// 焦虑情绪
				'焦虑': '焦虑',
				'担心': '焦虑',
				'不安': '焦虑',
				'anxious': '焦虑',
				'anxiety': '焦虑',
				'worried': '焦虑',
				
				// 紧张情绪
				'紧张': '紧张',
				'nervous': '紧张',
				'tense': '紧张',
				
				// 消极情绪
				'悲伤': '悲伤',
				'难过': '悲伤',
				'伤心': '悲伤',
				'negative': '悲伤',
				'消极': '悲伤',
				'sad': '悲伤',
				'sadness': '悲伤',
				'depressed': '悲伤',
				
				// 愤怒情绪
				'愤怒': '愤怒',
				'生气': '愤怒',
				'angry': '愤怒',
				'anger': '愤怒',
				'mad': '愤怒',
				
				// 其他情绪
				'困惑': '困惑',
				'confused': '困惑',
				'confusion': '困惑',
				'满足': '满足',
				'satisfied': '满足',
				'satisfaction': '满足',
				'放松': '放松',
				'relaxed': '放松',
				'relaxation': '放松',
				'期待': '期待',
				'expectation': '期待',
				'信任': '信任',
				'trust': '信任',
				'疲惫': '疲惫',
				'tired': '疲惫',
				'exhausted': '疲惫'
			}
			
			// 1. 直接匹配（不区分大小写）
			const directMatch = emotionMap[emotion] || emotionMap[emotionLower]
			if (directMatch) {
				return directMatch
			}
			
			// 2. 包含匹配（检查情绪字符串是否包含关键词）
			for (const [key, value] of Object.entries(emotionMap)) {
				if (emotionLower.includes(key.toLowerCase()) || key.toLowerCase().includes(emotionLower)) {
					return value
				}
			}
			
			// 3. 如果后端返回的是情绪标签列表中的值，直接返回
			if (this.emotionTags.includes(emotion)) {
				return emotion
			}
			
			// 4. 默认返回平静（中性情绪）
			console.warn('未识别的情绪:', emotion, '，使用默认值: 平静')
			return '平静'
		},
		
		// 关闭情绪分析结果
		closeEmotionResult() {
			this.emotionResult = null
		},
		
		// 选择图片进行多模态分析
		chooseImageForAnalysis() {
			// 先检查权限
			uni.getSetting({
				success: (res) => {
					console.log('当前权限设置:', res.authSetting)
					
					// 检查相机和相册权限
					const hasCameraAuth = res.authSetting['scope.camera'] !== false
					const hasAlbumAuth = res.authSetting['scope.writePhotosAlbum'] !== false
					
					if (!hasCameraAuth || !hasAlbumAuth) {
						// 请求权限
						uni.authorize({
							scope: 'scope.camera',
							success: () => {
								console.log('相机权限授权成功')
								this.selectImage()
							},
							fail: (err) => {
								console.log('相机权限授权失败:', err)
								uni.showModal({
									title: '权限提示',
									content: '需要相机和相册权限来选择图片，请在设置中开启',
									confirmText: '去设置',
									success: (modalRes) => {
										if (modalRes.confirm) {
											uni.openSetting({
												success: (settingRes) => {
													console.log('设置页面返回:', settingRes)
													if (settingRes.authSetting['scope.camera'] && settingRes.authSetting['scope.writePhotosAlbum']) {
														this.selectImage()
													}
												}
											})
										}
									}
								})
							}
						})
					} else {
						// 已有权限，直接选择图片
						this.selectImage()
					}
				},
				fail: (err) => {
					console.error('获取权限设置失败:', err)
					// 直接尝试选择图片
					this.selectImage()
				}
			})
		},
		
		// 实际选择图片的方法
		selectImage() {
			uni.chooseImage({
				count: 1,
				sizeType: ['original', 'compressed'],
				sourceType: ['album', 'camera'],
				success: (res) => {
					console.log('选择图片成功:', res)
					this.selectedImage = res.tempFilePaths[0]
					this.analyzeImageEmotion()
				},
				fail: (err) => {
					console.error('选择图片失败:', err)
					
					// 根据错误类型给出不同的提示
					if (err.errMsg && err.errMsg.includes('cancel')) {
						uni.showToast({
							title: '已取消选择图片',
							icon: 'none'
						})
					} else if (err.errMsg && err.errMsg.includes('auth')) {
						uni.showModal({
							title: '权限不足',
							content: '需要相机和相册权限，请在设置中开启',
							confirmText: '去设置',
							success: (modalRes) => {
								if (modalRes.confirm) {
									uni.openSetting()
								}
							}
						})
					} else {
						uni.showToast({
							title: '选择图片失败，请重试',
							icon: 'none'
						})
					}
				}
			})
		},
		
		// 选择音频进行多模态分析
		chooseAudioForAnalysis() {
			// 先检查权限
			uni.getSetting({
				success: (res) => {
					console.log('当前权限设置:', res.authSetting)
					
					// 检查录音权限
					const hasRecordAuth = res.authSetting['scope.record'] !== false
					
					if (!hasRecordAuth) {
						// 请求权限
						uni.authorize({
							scope: 'scope.record',
							success: () => {
								console.log('录音权限授权成功')
								this.selectAudio()
							},
							fail: (err) => {
								console.log('录音权限授权失败:', err)
								uni.showModal({
									title: '权限提示',
									content: '需要录音权限来选择音频，请在设置中开启',
									confirmText: '去设置',
									success: (modalRes) => {
										if (modalRes.confirm) {
											uni.openSetting({
												success: (settingRes) => {
													console.log('设置页面返回:', settingRes)
													if (settingRes.authSetting['scope.record']) {
														this.selectAudio()
													}
												}
											})
										}
									}
								})
							}
						})
					} else {
						// 已有权限，直接选择音频
						this.selectAudio()
					}
				},
				fail: (err) => {
					console.error('获取权限设置失败:', err)
					// 直接尝试选择音频
					this.selectAudio()
				}
			})
		},
		
		// 实际选择音频的方法
		selectAudio() {
			// 优先使用 chooseAudio（微信小程序原生API）
			if (uni.chooseAudio && typeof uni.chooseAudio === 'function') {
				uni.chooseAudio({
					count: 1,
					success: (res) => {
						console.log('选择音频成功:', res)
						this.selectedAudio = {
							tempFilePath: res.tempFilePath,
							name: res.tempFilePath.split('/').pop() || 'audio.mp3',
							size: res.size || 0
						}
						this.analyzeAudioEmotion()
					},
					fail: (err) => {
						console.error('chooseAudio 选择音频失败:', err)
						// chooseAudio 失败时，尝试使用 chooseMedia
						this.selectAudioWithChooseMedia()
					}
				})
			} else {
				// chooseAudio 不可用时，使用 chooseMedia
				this.selectAudioWithChooseMedia()
			}
		},
		
		// 使用 chooseMedia 选择音频（备选方案）
		selectAudioWithChooseMedia() {
			if (uni.chooseMedia && typeof uni.chooseMedia === 'function') {
				uni.chooseMedia({
					count: 1,
					mediaType: ['audio'],
					sourceType: ['album', 'camera'],
					success: (res) => {
						console.log('chooseMedia 选择音频成功:', res)
						if (res.tempFiles && res.tempFiles.length > 0) {
							const file = res.tempFiles[0]
							this.selectedAudio = {
								tempFilePath: file.tempFilePath,
								name: file.name || file.tempFilePath.split('/').pop() || 'audio.mp3',
								size: file.size || 0
							}
							this.analyzeAudioEmotion()
						} else {
							this.handleAudioSelectError({ errMsg: '未选择音频文件' })
						}
					},
					fail: (err) => {
						console.error('chooseMedia 选择音频失败:', err)
						this.handleAudioSelectError(err)
					}
				})
			} else {
				// 两种方法都不可用
				uni.showToast({
					title: '当前环境不支持音频选择',
					icon: 'none',
					duration: 2000
				})
			}
		},
		
		
		// 处理音频选择错误
		handleAudioSelectError(err) {
			if (err.errMsg && err.errMsg.includes('cancel')) {
				uni.showToast({
					title: '已取消选择音频',
					icon: 'none'
				})
			} else if (err.errMsg && err.errMsg.includes('auth')) {
				uni.showModal({
					title: '权限不足',
					content: '需要录音权限，请在设置中开启',
					confirmText: '去设置',
					success: (modalRes) => {
						if (modalRes.confirm) {
							uni.openSetting()
						}
					}
				})
			} else {
				uni.showToast({
					title: '选择音频失败，请重试',
					icon: 'none'
				})
			}
		},

		onUnload() {
			// 页面卸载时的清理工作
		},
		
		// 格式化音频时间
		formatAudioTime(seconds) {
			if (!seconds || isNaN(seconds)) return '00:00'
			const mins = Math.floor(seconds / 60)
			const secs = Math.floor(seconds % 60)
			return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
		},
		
		// 移除已选择的图片
		removeSelectedImage() {
			this.selectedImage = null
			this.multimodalResult = null // 清空多模态结果
		},
		
		// 移除已选择的音频
		removeSelectedAudio() {
			this.selectedAudio = null
			this.multimodalResult = null // 清空多模态结果
		},
		
		// 格式化文件大小
		formatFileSize(size) {
			if (size === 0) return '0 Bytes';
			const k = 1024;
			const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
			const i = Math.floor(Math.log(size) / Math.log(k));
			return parseFloat((size / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
		},
		
		// 分析图片情绪
		async analyzeImageEmotion() {
			if (!this.selectedImage) {
				uni.showToast({
					title: '请先选择一张图片',
					icon: 'none'
				})
				return
			}
			
			this.analyzingImage = true
			try {
				const baseUrl = this.getBaseUrl()
				const userInfo = uni.getStorageSync('userInfo')
				const uid = userInfo ? userInfo.uid : ''
				
				if (!uid) {
					uni.showToast({
						title: '用户未登录，请重新登录',
						icon: 'none'
					})
					return
				}
				
				console.log('开始图片情绪分析，文件:', this.selectedImage)
				
				const uploadRes = await new Promise((resolve, reject) => {
					uni.uploadFile({
						url: baseUrl + '/app/emotion/analyze/image',
						filePath: this.selectedImage,
						name: 'image',
						header: { 'uid': uid },
						success: (res) => {
							console.log('图片上传成功:', res)
							resolve(res)
						},
						fail: (err) => {
							console.error('图片上传失败:', err)
							reject(err)
						}
					})
				})

				console.log('图片分析响应:', uploadRes)
				
				let response
				try { 
					response = JSON.parse(uploadRes.data) 
					console.log('解析后的响应:', response)
				} catch (e) { 
					console.error('响应解析失败:', e, uploadRes.data)
					response = {} 
				}
				
				if (response && (response.code === 200 || response.status === 'ok')) {
					const data = response.data || response.result || response
					this.multimodalResult = { ...this.multimodalResult, image: data.data || data.image || data }
					
					// 自动更新情绪标签
					const emotion = data.emotion || data.data?.emotion || data.image?.emotion || data.label || data.primaryEmotion
					if (emotion) {
						const mappedEmotion = this.mapEmotionToTag(emotion)
						this.selectedEmotion = mappedEmotion
						console.log('图像情绪识别:', { 原始: emotion, 映射后: mappedEmotion })
					}
					
					uni.showToast({
						title: '图像情绪分析完成',
						icon: 'success'
					})
				} else {
					console.error('图片分析失败，响应:', response)
					uni.showToast({ 
						title: response.message || '图像情绪分析失败', 
						icon: 'none' 
					})
				}
			} catch (error) {
				console.error('图像情绪分析失败:', error)
				uni.showToast({
					title: '图像情绪分析失败，请稍后重试',
					icon: 'none'
				})
			} finally {
				this.analyzingImage = false
			}
		},
		
		// 分析音频情绪
		async analyzeAudioEmotion() {
			if (!this.selectedAudio) {
				uni.showToast({
					title: '请先选择一个音频文件',
					icon: 'none'
				})
				return
			}
			
			this.analyzingAudio = true
			try {
				const baseUrl = this.getBaseUrl()
				const userInfo = uni.getStorageSync('userInfo')
				const uid = userInfo ? userInfo.uid : ''
				
				if (!uid) {
					uni.showToast({
						title: '用户未登录，请重新登录',
						icon: 'none'
					})
					return
				}
				
				console.log('开始音频情绪分析，文件:', this.selectedAudio)
				
				const uploadRes = await new Promise((resolve, reject) => {
					uni.uploadFile({
						url: baseUrl + '/api/emotion/analyze/audio',
						filePath: this.selectedAudio.tempFilePath,
						name: 'audio',
						header: { 'uid': uid },
						success: (res) => {
							console.log('音频上传成功:', res)
							resolve(res)
						},
						fail: (err) => {
							console.error('音频上传失败:', err)
							reject(err)
						}
					})
				})

				console.log('音频分析响应:', uploadRes)
				
				let response
				try { 
					response = JSON.parse(uploadRes.data) 
					console.log('解析后的响应:', response)
				} catch (e) { 
					console.error('响应解析失败:', e, uploadRes.data)
					response = {} 
				}
				
				if (response && (response.code === 200 || response.status === 'ok')) {
					const data = response.data || response.result || response
					this.multimodalResult = { ...this.multimodalResult, audio: data.data || data.audio || data }
					
					// 自动更新情绪标签
					const emotion = data.emotion || data.data?.emotion || data.audio?.emotion || data.label || data.primaryEmotion
					if (emotion) {
						const mappedEmotion = this.mapEmotionToTag(emotion)
						this.selectedEmotion = mappedEmotion
						console.log('音频情绪识别:', { 原始: emotion, 映射后: mappedEmotion })
					}
					
					uni.showToast({
						title: '音频情绪分析完成',
						icon: 'success'
					})
				} else {
					console.error('音频分析失败，响应:', response)
					uni.showToast({ 
						title: response.message || '音频情绪分析失败', 
						icon: 'none' 
					})
				}
			} catch (error) {
				console.error('音频情绪分析失败:', error)
				uni.showToast({
					title: '音频情绪分析失败，请稍后重试',
					icon: 'none'
				})
			} finally {
				this.analyzingAudio = false
			}
		},
		
		// 关闭多模态分析结果
		closeMultimodalResult() {
			this.multimodalResult = null
		},
		
		async saveAsDraft() {
			await this.saveDiary('draft')
		},
		
		async publishDiary() {
			await this.saveDiary('published')
		},
		
		async saveDiary(status) {
			if (!this.diaryContent.trim()) {
				uni.showToast({
					title: '请输入日记内容',
					icon: 'none'
				})
				return
			}
			
			try {
				uni.showLoading({ title: '保存中...' })
				
				const diaryData = {
					content: this.diaryContent,
					emotion: this.selectedEmotion,
					createTime: this.diaryDate,
					status: status
				}
				
				let response
				if (this.isEdit) {
					response = await diaryApi.updateDiary(this.diaryId, diaryData)
				} else {
					response = await diaryApi.createDiary(diaryData)
				}
				
				if (response && response.code === 200) {
					uni.showToast({
						title: status === 'draft' ? '草稿保存成功' : '日记发布成功',
						icon: 'success'
					})
					
					setTimeout(() => {
						uni.navigateBack()
					}, 1500)
				} else {
					uni.showToast({
						title: response?.message || '保存失败',
						icon: 'none'
					})
				}
			} catch (error) {
				console.error('保存日记失败:', error)
				uni.showToast({
					title: '保存失败，请重试',
					icon: 'none'
				})
			} finally {
				uni.hideLoading()
			}
		}
	}
}
</script>

<style scoped>
.edit-container {
	min-height: 100vh;
	background: transparent;
	padding: 0;
}

.nav-bar {
	background: transparent;
	padding: 30rpx 40rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	border-bottom: 1rpx solid rgba(0, 0, 0, 0.1);
	position: sticky;
	top: 0;
	z-index: 100;
}

/* 修改返回按钮样式 */
.back-btn {
	width: 60rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 50%;
	background: rgba(99, 102, 241, 0.1);
	transition: all 0.3s ease;
}

.back-btn:active {
	background: rgba(99, 102, 241, 0.2);
	transform: scale(0.95);
}

.back-icon {
	font-size: 36rpx;
	color: #4a90e2;
	font-weight: 600;
}

/* 移除返回文字样式 */
.back-text {
	display: none;
}

.page-title {
	font-size: 32rpx;
	font-weight: 700;
	color: '1f2937';
}

.placeholder {
	width: 60rpx; /* 与返回按钮宽度相同，保持布局平衡 */
}

.content {
	padding: 30rpx;
	padding-bottom: 120rpx;
}

.input-section {
	padding: 30rpx 0;
	margin-bottom: 30rpx;
	position: relative;
	overflow: hidden;
}


.diary-input {
	width: 100%;
	min-height: 300rpx;
	background: transparent;
	border: none;
	font-size: 30rpx;
	color: #2d3748;
	line-height: 1.6;
	resize: none;
}

.char-count {
	display: block;
	text-align: right;
	font-size: 24rpx;
	color: #718096;
	margin-top: 20rpx;
}

.settings-section {
	padding: 30rpx 0;
	margin-bottom: 30rpx;
}

.setting-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 25rpx 0;
	border-bottom: 1rpx solid #f1f5f9;
}

.setting-item:last-child {
	border-bottom: none;
}

.setting-label {
	font-size: 28rpx;
	color: #2d3748;
	font-weight: 500;
}

.picker-field {
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.picker-text {
	font-size: 26rpx;
	color: #4a90e2;
}

.picker-arrow {
	font-size: 24rpx;
	color: #a0aec0;
}

.emotion-setting-item {
	flex-direction: column;
	align-items: flex-start;
}

.emotion-setting-item .setting-label {
	margin-bottom: 15rpx;
}

.multimodal-setting-item {
	flex-direction: column;
	align-items: flex-start;
}

.multimodal-setting-item .setting-label {
	margin-bottom: 15rpx;
}

.emotion-section {
	width: 100%;
	margin-top: 10rpx;
}

.emotion-display {
	display: flex;
	align-items: center;
	gap: 20rpx;
	width: 100%;
}

.emotion-tag {
	flex: 1;
	padding: 20rpx 30rpx;
	background: linear-gradient(135deg, #f0f9ff, #e0f2fe);
	border: 2rpx solid #0ea5e9;
	border-radius: 12rpx;
	font-size: 28rpx;
	color: #0369a1;
	font-weight: 500;
	text-align: center;
	min-height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.analyzing-text {
	font-size: 24rpx;
	color: #64748b;
	font-style: italic;
}

.emotion-result {
	margin-top: 20rpx;
	padding: 20rpx;
	background: linear-gradient(135deg, #f0f8ff, 'e6f3ff');
	border-radius: 16rpx;
	border: 1rpx solid #d1e7ff;
	width: 100%;
}

.result-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16rpx;
}

.result-title {
	font-size: 26rpx;
	font-weight: 600;
	color: #2c5aa0;
}

.close-btn {
	font-size: 32rpx;
	color: #a0aec0;
	cursor: pointer;
}

.result-content {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
}

.result-item {
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.result-label {
	font-size: 24rpx;
	color: #666;
	min-width: 120rpx;
}

.result-value {
	font-size: 24rpx;
	font-weight: 500;
	color: #333;
}

.multimodal-section {
	margin-top: 30rpx;
	padding-top: 20rpx;
	border-top: 1rpx solid #f1f5f9;
	width: 100%;
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.multimodal-title {
	font-size: 26rpx;
	font-weight: 600;
	color: #2d3748;
	margin-bottom: 16rpx;
}

.multimodal-buttons {
	display: flex;
	gap: 15rpx;
	margin-bottom: 20rpx;
}

.multimodal-btn {
	flex: 1;
	padding: 16rpx 20rpx;
	border-radius: 12rpx;
	font-size: 24rpx;
	font-weight: 600;
	border: none;
	transition: all 0.3s ease;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
}

.cv-btn {
	background: linear-gradient(135deg, #4299e1, #3182ce);
	color: white;
}

.audio-btn {
	background: linear-gradient(135deg, #48bb78, #38a169);
	color: white;
}

.multimodal-btn:active {
	transform: translateY(2rpx);
}

.btn-icon {
	font-size: 28rpx;
}

.btn-text {
	font-size: 24rpx;
}

.image-preview-section {
	margin-top: 20rpx;
	padding: 20rpx;
	background: linear-gradient(135deg, #f0f8ff, #e6f3ff);
	border-radius: 16rpx;
	border: 1rpx solid #d1e7ff;
}

.image-preview-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16rpx;
}

.preview-title {
	font-size: 24rpx;
	font-weight: 500;
	color: #2d3748;
}

.preview-actions {
	display: flex;
	gap: 15rpx;
	font-size: 24rpx;
	color: #4a90e2;
}

.action-link {
	text-decoration: underline;
}

.delete {
	color: #e53e3e;
}

.preview-image {
	width: 100%;
	height: 200rpx;
	object-fit: cover;
	border-radius: 12rpx;
	margin-bottom: 15rpx;
}

.analyze-image-btn {
	background: linear-gradient(135deg, #4a90e2, #6aa8ff);
	color: white;
	border: none;
	border-radius: 12rpx;
	padding: 16rpx 24rpx;
	font-size: 24rpx;
	font-weight: 600;
	transition: all 0.3s ease;
}

.analyze-image-btn:active {
	transform: translateY(2rpx);
}

.audio-preview-section {
	margin-top: 20rpx;
	padding: 20rpx;
	background: linear-gradient(135deg, #f0f8ff, #e6f3ff);
	border-radius: 16rpx;
	border: 1rpx solid #d1e7ff;
}

.audio-preview-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16rpx;
}

.audio-info {
	display: flex;
	justify-content: space-between;
	align-items: center;
	font-size: 24rpx;
	color: #4a5568;
	margin-bottom: 15rpx;
}

.audio-name {
	font-weight: 500;
}

.audio-size {
	color: #718096;
}

.analyze-audio-btn {
	background: linear-gradient(135deg, #4a90e2, #6aa8ff);
	color: white;
	border: none;
	border-radius: 12rpx;
	padding: 16rpx 24rpx;
	font-size: 24rpx;
	font-weight: 600;
	transition: all 0.3s ease;
}

.analyze-audio-btn:active {
	transform: translateY(2rpx);
}

.multimodal-result {
	margin-top: 20rpx;
	padding: 20rpx;
	background: linear-gradient(135deg, #f0f8ff, #e6f3ff);
	border-radius: 16rpx;
	border: 1rpx solid #d1e7ff;
}

.attachment-section {
	margin-top: 30rpx;
	padding: 30rpx 0;
	border-top: 1rpx solid #f1f5f9;
}

.section-title {
	font-size: 28rpx;
	font-weight: 600;
	color: #2d3748;
	margin-bottom: 16rpx;
}

.attachment-tips {
	font-size: 24rpx;
	color: #718096;
	margin-bottom: 20rpx;
}

.attachment-buttons {
	display: flex;
	gap: 15rpx;
	margin-bottom: 30rpx;
}

.attach-btn {
	flex: 1;
	padding: 20rpx;
	border-radius: 12rpx;
	border: 2rpx solid #e2e8f0;
	background: #ffffff;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 8rpx;
	transition: all 0.3s ease;
}

.attach-btn:active {
	transform: scale(0.95);
	background: #f7fafc;
}

.image-btn {
	border-color: #4299e1;
}

.video-btn {
	border-color: #48bb78;
}

.audio-btn {
	border-color: #ed8936;
}

.attach-icon {
	font-size: 32rpx;
}

.attach-text {
	font-size: 24rpx;
	color: #2d3748;
	font-weight: 500;
}

.attachment-preview-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.attachment-item {
	background: #f7fafc;
	border-radius: 16rpx;
	padding: 20rpx;
	border: 1rpx solid #e2e8f0;
}

.attachment-preview {
	display: flex;
	flex-direction: column;
	gap: 15rpx;
}

.preview-img {
	width: 100%;
	height: 300rpx;
	border-radius: 12rpx;
	object-fit: cover;
}

.preview-video {
	width: 100%;
	height: 300rpx;
	border-radius: 12rpx;
}

.attachment-info {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.attachment-name {
	font-size: 26rpx;
	color: #2d3748;
	font-weight: 500;
}

.attachment-size {
	font-size: 24rpx;
	color: #718096;
}

.attachment-actions {
	display: flex;
	gap: 20rpx;
	justify-content: flex-end;
}

.action-btn {
	font-size: 24rpx;
	color: #4a90e2;
	padding: 8rpx 16rpx;
	border-radius: 8rpx;
	background: rgba(74, 144, 226, 0.1);
}

.action-btn.delete {
	color: #e53e3e;
	background: rgba(229, 62, 62, 0.1);
}

.audio-preview-box {
	display: flex;
	align-items: center;
	gap: 15rpx;
	padding: 20rpx;
	background: linear-gradient(135deg, #f0f8ff, #e6f3ff);
	border-radius: 12rpx;
}

.audio-icon {
	font-size: 40rpx;
}

.audio-player-wrapper {
	margin-top: 20rpx;
}

.audio-control-bar {
	display: flex;
	align-items: center;
	gap: 20rpx;
	padding: 20rpx;
	background: linear-gradient(135deg, #f0f8ff, #e6f3ff);
	border-radius: 16rpx;
}

.audio-play-icon {
	width: 60rpx;
	height: 60rpx;
	border-radius: 50%;
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 4rpx 12rpx rgba(74, 144, 226, 0.3);
}

.play-icon-text {
	color: white;
	font-size: 24rpx;
	font-weight: bold;
}

.audio-progress-area {
	flex: 1;
}

.audio-info-line {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 12rpx;
}

.audio-title {
	font-size: 24rpx;
	color: #2d3748;
	font-weight: 500;
}

.audio-time {
	font-size: 22rpx;
	color: #718096;
}

.progress-bar-wrapper {
	width: 100%;
	height: 8rpx;
	background: #e2e8f0;
	border-radius: 4rpx;
	position: relative;
	overflow: hidden;
}

.progress-bar-fill {
	height: 100%;
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
	border-radius: 4rpx;
	transition: width 0.1s ease;
}

.action-section {
	display: flex;
	gap: 20rpx;
	position: fixed;
	bottom: 30rpx;
	left: 30rpx;
	right: 30rpx;
	z-index: 10;
}

.draft-btn,
.publish-btn {
	flex: 1;
	padding: 24rpx;
	border-radius: 16rpx;
	font-size: 28rpx;
	font-weight: 600;
	border: none;
	transition: all 0.3s ease;
}

.draft-btn {
	background: rgba(255, 255, 255, 0.9);
	color: #4a90e2;
	border: 2rpx solid rgba(99, 102, 241, 0.3);
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.draft-btn:active {
	background: rgba(99, 102, 241, 0.1);
	transform: translateY(2rpx);
}

.publish-btn {
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
	color: white;
	box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);
}

.publish-btn:active {
	transform: translateY(2rpx);
	box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.4);
}

/* 响应式设计 */
@media (max-width: 768px) {
	.nav-bar {
		padding: 24rpx 30rpx;
	}
	
	.content {
		padding: 20rpx;
		padding-bottom: 120rpx;
	}
	
	.input-section, .settings-section {
		padding: 24rpx;
	}
	
	.diary-input {
		font-size: 28rpx;
	}
	
	.multimodal-buttons {
		flex-direction: column;
	}
	
	.action-section {
		left: 20rpx;
		right: 20rpx;
		bottom: 20rpx;
	}
}
</style>