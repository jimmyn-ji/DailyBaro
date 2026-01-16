<template>
	<view class="detail-container">
		<view class="header">
			<view class="back-btn" @tap="goBack">←</view>
			<text class="title">日记详情</text>
			<view class="edit-btn" @tap="editDiary">编辑</view>
		</view>
		
		<view v-if="loading" class="loading">
			<text>加载中...</text>
		</view>
		
		<view v-else-if="diary" class="diary-content">
			<view class="diary-header">
				<text class="diary-title">{{ diary.title || '无标题' }}</text>
				<view class="diary-meta">
					<text class="diary-date">{{ formatDate(diary.createTime) }}</text>
					<text v-if="diary.status === 'draft'" class="status-badge draft">草稿</text>
					<text v-else class="status-badge published">已发布</text>
				</view>
			</view>
			
			<view class="diary-body">
				<text class="diary-text">{{ diary.content || '暂无内容' }}</text>
			</view>
			
			<view v-if="mediaList && mediaList.length > 0" class="media-section">
				<view class="section-title">附件</view>
				<view class="media-list">
					<view 
						v-for="(media, index) in mediaList" 
						:key="index" 
						class="media-item"
					>
						<view v-if="media.mediaType === 'image'" class="image-preview-wrapper">
							<image 
								:src="getFullUrl(media.mediaUrl)" 
								class="media-preview-image"
								mode="aspectFill"
								@tap="previewImage(getFullUrl(media.mediaUrl))"
							/>
							<view class="image-overlay">
								<text class="preview-hint">点击查看大图</text>
							</view>
						</view>
						<view v-else-if="media.mediaType === 'video'" class="video-preview-wrapper">
							<video 
								:src="getFullUrl(media.mediaUrl)" 
								class="media-preview-video"
								controls
								show-center-play-btn
								object-fit="contain"
							></video>
						</view>
						<view v-else-if="media.mediaType === 'audio'" class="audio-preview">
							<view class="audio-player-wrapper">
								<view class="audio-control-bar">
									<view class="audio-play-icon" @tap="playAudio(getFullUrl(media.mediaUrl), index)">
										<text class="play-icon-text">{{ audioPlaying[index] ? '⏸' : '▶' }}</text>
									</view>
									<view class="audio-progress-area">
										<view class="audio-info-line">
											<text class="audio-title">音频文件</text>
											<text class="audio-time">{{ formatAudioTime(audioCurrentTime[index] || 0) }} / {{ formatAudioTime(audioDuration[index] || 0) }}</text>
										</view>
										<view class="progress-bar-wrapper" @tap="seekAudio($event, index)">
											<view class="progress-bar-bg">
												<view class="progress-bar-fill" :style="{ width: audioProgress[index] + '%' }"></view>
											</view>
										</view>
									</view>
								</view>
							</view>
						</view>
						<view v-else class="audio-preview">
							<text class="audio-icon">🎵</text>
							<text class="audio-text">音频文件</text>
						</view>
					</view>
				</view>
				<view class="analysis-section">
					<button class="analysis-btn" @tap="analyzeEmotion" :disabled="analyzing">
						{{ analyzing ? '分析中...' : '情绪分析' }}
					</button>
				</view>
				
				<!-- 情绪分析结果展示 -->
				<view v-if="emotionResult" class="emotion-result-section">
					<view class="result-header">
						<text class="result-title">📊 情绪分析结果</text>
					</view>
					<view class="result-content">
						<view class="emotion-item" v-if="emotionResult.emotion">
							<text class="emotion-label">主要情绪：</text>
							<text class="emotion-value">{{ emotionResult.emotion }}</text>
						</view>
						<view class="emotion-item" v-if="emotionResult.score !== undefined">
							<text class="emotion-label">情绪得分：</text>
							<text class="emotion-value">{{ emotionResult.score }}</text>
						</view>
						<view class="emotion-item" v-if="emotionResult.confidence !== undefined">
							<text class="emotion-label">置信度：</text>
							<text class="emotion-value">{{ (emotionResult.confidence * 100).toFixed(1) }}%</text>
						</view>
						<view class="emotion-item" v-if="emotionResult.description">
							<text class="emotion-label">分析描述：</text>
							<text class="emotion-desc">{{ emotionResult.description }}</text>
						</view>
					</view>
				</view>
			</view>
		</view>
		
		<view v-else class="empty">
			<text class="empty-icon">📝</text>
			<text class="empty-text">日记不存在</text>
		</view>
	</view>
</template>

<script>
import { diaryApi, emotionApi } from '@/utils/api.js'
import { formatDateLocal } from '../../utils/dateUtils.js'
import { config } from '@/utils/config.js'

export default {
	data() {
		return {
			diaryId: null,
			diary: null,
			loading: false,
			analyzing: false,
			audioPlaying: {},
			audioContexts: {},
			audioCurrentTime: {},
			audioDuration: {},
			audioProgress: {},
			emotionResult: null
		}
	},
	
	computed: {
		// 统一处理附件列表
		mediaList() {
			if (!this.diary) {
				console.log('mediaList: diary为空')
				return []
			}
			
			console.log('mediaList computed: 开始处理附件')
			console.log('diary.media:', this.diary.media)
			console.log('diary.mediaUrls:', this.diary.mediaUrls)
			console.log('diary.mediaList:', this.diary.mediaList)
			
			// 后端返回的是 media 字段，可能是对象数组
			if (this.diary.media && Array.isArray(this.diary.media) && this.diary.media.length > 0) {
				console.log('使用 diary.media，数量:', this.diary.media.length)
				// 确保每个media对象都有必要的字段
				return this.diary.media.map((item, index) => {
					// 如果item是字符串，转换为对象
					if (typeof item === 'string') {
						let mediaType = 'image'
						if (item.includes('.mp4') || item.includes('.mov') || item.includes('.avi')) {
							mediaType = 'video'
						} else if (item.includes('.mp3') || item.includes('.wav') || item.includes('.m4a')) {
							mediaType = 'audio'
						}
						return {
							mediaId: index,
							mediaType: mediaType,
							mediaUrl: item
						}
					}
					// 如果已经是对象，确保有必要的字段
					return {
						mediaId: item.mediaId || index,
						mediaType: item.mediaType || item.type || 'image',
						mediaUrl: item.mediaUrl || item.url || item
					}
				})
			}
			
			// 如果是 mediaUrls 数组，转换为 media 格式
			if (this.diary.mediaUrls && Array.isArray(this.diary.mediaUrls) && this.diary.mediaUrls.length > 0) {
				console.log('使用 diary.mediaUrls，数量:', this.diary.mediaUrls.length)
				return this.diary.mediaUrls.map((url, index) => {
					// 根据URL判断类型
					let mediaType = 'image'
					const urlLower = url.toLowerCase()
					if (urlLower.includes('.mp4') || urlLower.includes('.mov') || urlLower.includes('.avi') || urlLower.includes('.m4v')) {
						mediaType = 'video'
					} else if (urlLower.includes('.mp3') || urlLower.includes('.wav') || urlLower.includes('.m4a') || urlLower.includes('.aac')) {
						mediaType = 'audio'
					} else if (urlLower.includes('.jpg') || urlLower.includes('.jpeg') || urlLower.includes('.png') || urlLower.includes('.gif') || urlLower.includes('.webp')) {
						mediaType = 'image'
					}
					return {
						mediaId: index,
						mediaType: mediaType,
						mediaUrl: url
					}
				})
			}
			
			// 尝试其他可能的字段名
			if (this.diary.mediaList && Array.isArray(this.diary.mediaList) && this.diary.mediaList.length > 0) {
				console.log('使用 diary.mediaList，数量:', this.diary.mediaList.length)
				return this.diary.mediaList
			}
			
			console.warn('mediaList: 未找到附件数据')
			return []
		}
	},
	
	onLoad(options) {
		console.log('日记详情页面参数:', options)
		if (options.id) {
			this.diaryId = options.id
			this.loadDiary()
		} else {
			uni.showToast({
				title: '参数错误',
				icon: 'none'
			})
			setTimeout(() => {
				uni.navigateBack()
			}, 1500)
		}
	},
	
	onUnload() {
		// 页面卸载时停止所有音频
		Object.values(this.audioContexts).forEach(audioContext => {
			if (audioContext) {
				audioContext.stop()
				audioContext.destroy()
			}
		})
		this.audioContexts = {}
		this.audioPlaying = {}
	},
	
	methods: {
		goBack() {
			uni.navigateBack()
		},
		
		async loadDiary() {
			this.loading = true
			
			try {
				const res = await diaryApi.getDiaryDetail(this.diaryId)
				
				console.log('========== 日记详情响应 ==========')
				console.log('完整响应:', JSON.stringify(res, null, 2))
				console.log('响应code:', res.code)
				console.log('响应data:', res.data)
				
				if (res.code === 200) {
					this.diary = res.data
					console.log('========== 日记数据 ==========')
					console.log('日记对象:', this.diary)
					console.log('diary.media:', this.diary.media)
					console.log('diary.mediaUrls:', this.diary.mediaUrls)
					console.log('diary.mediaList:', this.diary.mediaList)
					
					// 等待Vue更新computed属性
					this.$nextTick(() => {
						console.log('========== 附件列表 ==========')
						console.log('mediaList computed:', this.mediaList)
						console.log('附件数量:', this.mediaList.length)
						if (this.mediaList.length > 0) {
							this.mediaList.forEach((media, index) => {
								console.log(`附件${index + 1}:`, {
									type: media.mediaType,
									url: media.mediaUrl,
									fullUrl: this.getFullUrl(media.mediaUrl)
								})
							})
						} else {
							console.warn('⚠️ 没有找到附件！')
						}
					})
				} else {
					uni.showToast({
						title: res.message || '加载失败',
						icon: 'none'
					})
				}
			} catch (error) {
				console.error('========== 加载日记失败 ==========')
				console.error('错误详情:', error)
				uni.showToast({
					title: error.message || '加载失败',
					icon: 'none'
				})
			} finally {
				this.loading = false
			}
		},
		
		getFullUrl(url) {
			if (!url) {
				console.warn('getFullUrl: url为空')
				return ''
			}
			
			// 如果已经是完整URL，直接返回
			if (url.startsWith('http://') || url.startsWith('https://')) {
				console.log('getFullUrl: 完整URL，直接返回:', url)
				return url
			}
			
			// 处理前端静态资源路径，转换为文件服务路径
			if (url.startsWith('/src/static/') || url.startsWith('src/static/')) {
				// 将 /src/static/imgs/pic1.png 转换为 /api/uploads/static/imgs/pic1.png
				const filePath = url.replace(/^\/?src\/static\//, '/api/uploads/static/')
				const baseUrl = config.BASE_URL || 'http://172.20.10.4:8000'
				const fullUrl = baseUrl + filePath
				console.log('getFullUrl: 转换静态资源路径:', url, '->', fullUrl)
				return fullUrl
			}
			
		// 处理上传文件的路径
		// 根据网关配置，优先使用 /api/uploads/ 路径（网关会转发到 file-service）
		let filePath = url
		
		// 如果已经是 /api/uploads/ 开头，直接使用
		if (url.startsWith('/api/uploads/')) {
			filePath = url
		}
		// 如果是 /uploads/ 开头，转换为 /api/uploads/（网关配置中有 /api/uploads/** 路由）
		else if (url.startsWith('/uploads/')) {
			filePath = '/api/uploads/' + url.substring('/uploads/'.length)
		}
		// 如果是 uploads/ 开头（没有前导斜杠），转换为 /api/uploads/
		else if (url.startsWith('uploads/')) {
			filePath = '/api/uploads/' + url.substring('uploads/'.length)
		}
		// 如果都不匹配，尝试添加 /api/uploads/ 前缀
		else if (!url.startsWith('/api/') && !url.startsWith('http') && !url.startsWith('/')) {
			filePath = '/api/uploads/' + url
		}
		// 如果以 / 开头但不是 /api/，尝试添加 /api/uploads
		else if (url.startsWith('/') && !url.startsWith('/api/') && !url.startsWith('/uploads/')) {
			filePath = '/api/uploads' + url
		}
		
		// 使用配置的BASE_URL
		const baseUrl = config.BASE_URL || 'http://172.20.10.4:8000'
		const fullUrl = baseUrl + filePath
		console.log('getFullUrl: 拼接URL:', url, '->', fullUrl)
		
		return fullUrl
		},
		
		editDiary() {
			uni.navigateTo({
				url: `/pages/diary/edit?id=${this.diaryId}`
			})
		},
		
		previewImage(url) {
			const urls = this.mediaList
				.filter(media => media.mediaType === 'image')
				.map(media => this.getFullUrl(media.mediaUrl))
			
			uni.previewImage({
				current: url,
				urls: urls
			})
		},
		
		formatDate(dateStr) {
			return formatDateLocal(dateStr)
		},
		
		playAudio(url, index) {
			console.log('播放音频:', url, '索引:', index)
			
			// 如果已经有音频在播放，先停止其他音频
			Object.keys(this.audioContexts).forEach(key => {
				if (key != index && this.audioContexts[key]) {
					this.audioContexts[key].stop()
					this.$set(this.audioPlaying, key, false)
					this.$set(this.audioProgress, key, 0)
				}
			})
			
			// 微信小程序使用 createInnerAudioContext
			if (!this.audioContexts[index]) {
				const audioContext = uni.createInnerAudioContext()
				audioContext.src = url
				audioContext.autoplay = false
				
				audioContext.onPlay(() => {
					console.log('音频开始播放')
					this.$set(this.audioPlaying, index, true)
				})
				
				audioContext.onPause(() => {
					console.log('音频暂停')
					this.$set(this.audioPlaying, index, false)
				})
				
				audioContext.onStop(() => {
					console.log('音频停止')
					this.$set(this.audioPlaying, index, false)
					this.$set(this.audioProgress, index, 0)
					this.$set(this.audioCurrentTime, index, 0)
				})
				
				audioContext.onEnded(() => {
					console.log('音频播放结束')
					this.$set(this.audioPlaying, index, false)
					this.$set(this.audioProgress, index, 0)
					this.$set(this.audioCurrentTime, index, 0)
				})
				
				audioContext.onCanplay(() => {
					// 获取音频总时长
					audioContext.duration && this.$set(this.audioDuration, index, audioContext.duration)
				})
				
				audioContext.onTimeUpdate(() => {
					// 更新播放进度
					if (audioContext.duration) {
						const currentTime = audioContext.currentTime || 0
						const duration = audioContext.duration || 1
						const progress = (currentTime / duration) * 100
						this.$set(this.audioCurrentTime, index, currentTime)
						this.$set(this.audioDuration, index, duration)
						this.$set(this.audioProgress, index, progress)
					}
				})
				
				audioContext.onError((err) => {
					console.error('音频播放错误:', err)
					console.error('错误详情:', JSON.stringify(err))
					console.error('音频URL:', url)
					
					// 检查是否是404错误，尝试备用路径
					if (err.errMsg && err.errMsg.includes('404')) {
						// 尝试不同的路径格式
						let altUrl = null
						if (url.includes('/api/uploads/')) {
							// 尝试去掉 /api 前缀
							altUrl = url.replace('/api/uploads/', '/uploads/')
						} else if (url.includes('/uploads/')) {
							// 尝试添加 /api 前缀
							altUrl = url.replace('/uploads/', '/api/uploads/')
						}
						
						if (altUrl) {
							console.log('尝试备用路径:', altUrl)
							// 销毁当前音频上下文
							audioContext.destroy()
							this.$set(this.audioContexts, index, null)
							
							// 重新创建音频上下文使用备用URL
							setTimeout(() => {
								this.playAudio(altUrl, index)
							}, 100)
							return
						}
						
						uni.showToast({
							title: '音频文件不存在，请检查文件路径',
							icon: 'none',
							duration: 3000
						})
					} else if (err.errMsg && err.errMsg.includes('decode')) {
						uni.showToast({
							title: '音频格式不支持或文件损坏',
							icon: 'none',
							duration: 3000
						})
					} else {
						uni.showToast({
							title: `音频播放失败: ${err.errMsg || '未知错误'}`,
							icon: 'none',
							duration: 3000
						})
					}
					
					this.$set(this.audioPlaying, index, false)
					// 清理错误的音频上下文
					if (this.audioContexts[index]) {
						this.audioContexts[index].destroy()
						this.$set(this.audioContexts, index, null)
					}
				})
				
				this.$set(this.audioContexts, index, audioContext)
			}
			
			const audioContext = this.audioContexts[index]
			if (!audioContext) {
				console.error('音频上下文不存在')
				return
			}
			
			if (this.audioPlaying[index]) {
				console.log('暂停音频')
				audioContext.pause()
				this.$set(this.audioPlaying, index, false)
			} else {
				console.log('播放音频')
				audioContext.play()
				this.$set(this.audioPlaying, index, true)
			}
		},
		
		seekAudio(e, index) {
			const audioContext = this.audioContexts[index]
			if (!audioContext || !audioContext.duration) return
			
			// 获取点击位置
			const query = uni.createSelectorQuery().in(this)
			query.select(`.progress-bar-wrapper`).boundingClientRect((rect) => {
				if (rect) {
					const x = e.detail.x - rect.left
					const percent = x / rect.width
					const seekTime = percent * audioContext.duration
					audioContext.seek(seekTime)
					this.$set(this.audioCurrentTime, index, seekTime)
					this.$set(this.audioProgress, index, percent * 100)
				}
			}).exec()
		},
		
		formatAudioTime(seconds) {
			if (!seconds || isNaN(seconds)) return '00:00'
			const mins = Math.floor(seconds / 60)
			const secs = Math.floor(seconds % 60)
			return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
		},
		
		// 分析情绪
		async analyzeEmotion() {
			if (this.analyzing) return
			
			this.analyzing = true
			try {
				uni.showLoading({ title: '分析中...' })
				
				// 优先分析附件（如果有）
				if (this.mediaList && this.mediaList.length > 0) {
					const audioMedia = this.mediaList.find(m => m.mediaType === 'audio')
					const videoMedia = this.mediaList.find(m => m.mediaType === 'video')
					const imageMedia = this.mediaList.find(m => m.mediaType === 'image')
					
					const mediaToAnalyze = audioMedia || videoMedia || imageMedia
					if (mediaToAnalyze) {
						const type = mediaToAnalyze.mediaType
						const res = await emotionApi.analyzeFromDiary(this.diaryId, type)
						
						uni.hideLoading()
						
					if (res.code === 200) {
						this.emotionResult = res.data
						uni.showToast({
							title: '分析完成',
							icon: 'success'
						})
						console.log('分析结果:', res.data)
						return
					} else {
						uni.showToast({
							title: res.message || '分析失败',
							icon: 'none'
						})
					}
					}
				}
				
				// 如果没有附件或附件分析失败，分析文本内容
				if (this.diary && this.diary.content) {
					const baseUrl = config.BASE_URL || 'https://dailybaro.cn'
					const userInfo = uni.getStorageSync('userInfo')
					const uid = userInfo ? userInfo.uid : ''
					
					const response = await uni.request({
						url: baseUrl + '/app/diary/analyze-emotion',
						method: 'POST',
						header: {
							'Content-Type': 'application/json',
							'uid': uid
						},
						data: {
							content: this.diary.content
						}
					})
					
					uni.hideLoading()
					
					if (response.data && response.data.code === 200) {
						this.emotionResult = response.data.data
						uni.showToast({
							title: '文本分析完成',
							icon: 'success'
						})
						console.log('文本分析结果:', response.data.data)
					} else {
						uni.showToast({
							title: response.data?.message || '分析失败',
							icon: 'none'
						})
					}
				} else {
					uni.hideLoading()
					uni.showToast({
						title: '没有可分析的内容',
						icon: 'none'
					})
				}
			} catch (error) {
				uni.hideLoading()
				console.error('分析失败:', error)
				uni.showToast({
					title: error.message || '分析失败',
					icon: 'none'
				})
			} finally {
				this.analyzing = false
			}
		}
	}
}
</script>

<style scoped>
.detail-container {
	min-height: 100vh;
	background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 100%);
	padding: 0;
}

.header {
	background: rgba(255, 255, 255, 0.92);
	padding: 30rpx 40rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	border-bottom: 1rpx solid rgba(226, 232, 240, 0.8);
	backdrop-filter: blur(10px);
	position: sticky;
	top: 0;
	z-index: 10;
	box-shadow: 0 2rpx 20rpx rgba(0, 0, 0, 0.05);
}

.back-btn {
	font-size: 36rpx;
	color: #4a90e2;
	font-weight: 600;
	width: 60rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 50%;
	background: rgba(99, 102, 241, 0.1);
}

.title {
	font-size: 34rpx;
	font-weight: 700;
	color: #1f2937;
	letter-spacing: 0.5rpx;
}

.edit-btn {
	font-size: 28rpx;
	color: #4a90e2;
	font-weight: 500;
	padding: 12rpx 24rpx;
	background: rgba(99, 102, 241, 0.1);
	border-radius: 24rpx;
}

.loading, .empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 150rpx 40rpx;
	color: #64748b;
	background: rgba(255, 255, 255, 0.8);
	border-radius: 24rpx;
	margin: 30rpx;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.empty-icon {
	font-size: 100rpx;
	margin-bottom: 30rpx;
}

.empty-text {
	font-size: 32rpx;
	margin-bottom: 40rpx;
}

.diary-content {
	background: #ffffff;
	border-radius: 24rpx;
	padding: 40rpx;
	margin: 30rpx;
	box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.08);
	position: relative;
	overflow: hidden;
}

.diary-content::before {
	content: "";
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	height: 6rpx;
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
}

.diary-header {
	margin-bottom: 40rpx;
	padding-bottom: 30rpx;
	border-bottom: 1rpx solid #e2e8f0;
}

.diary-title {
	font-size: 44rpx;
	font-weight: 700;
	color: #1f2937;
	margin-bottom: 20rpx;
	display: block;
	line-height: 1.4;
}

.diary-meta {
	display: flex;
	align-items: center;
	gap: 20rpx;
	flex-wrap: wrap;
}

.diary-date {
	font-size: 26rpx;
	color: #64748b;
	display: flex;
	align-items: center;
}

.diary-date::before {
	content: "📅";
	margin-right: 8rpx;
}

.status-badge {
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
	font-size: 22rpx;
	font-weight: 500;
}

.status-badge.draft {
	background: #fef3c7;
	color: #d97706;
}

.status-badge.published {
	background: #c6f6d5;
	color: #2f855a;
}

.diary-body {
	margin-bottom: 40rpx;
}

.diary-text {
	font-size: 32rpx;
	color: #374151;
	line-height: 1.8;
	white-space: pre-wrap;
}

.media-section {
	margin-top: 40rpx;
	padding-top: 30rpx;
	border-top: 1rpx solid #e2e8f0;
}

.section-title {
	font-size: 30rpx;
	font-weight: 600;
	color: #1f2937;
	margin-bottom: 24rpx;
	display: flex;
	align-items: center;
}

.section-title::before {
	content: "";
	margin-right: 10rpx;
}

.media-list {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(200rpx, 1fr));
	gap: 20rpx;
}

.media-item {
	border-radius: 16rpx;
	overflow: hidden;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
	transition: all 0.3s ease;
}

.media-item:active {
	transform: scale(0.97);
}

.media-preview-image,
.media-preview-video {
	width: 100%;
	display: block;
}

.image-preview-wrapper {
	position: relative;
	border-radius: 12rpx;
	overflow: hidden;
	background: #f8fafc;
}

.media-preview-image {
	width: 100%;
	min-height: 300rpx;
	max-height: 600rpx;
	object-fit: cover;
}

.image-overlay {
	position: absolute;
	bottom: 0;
	left: 0;
	right: 0;
	background: linear-gradient(to top, rgba(0, 0, 0, 0.5), transparent);
	padding: 20rpx;
	opacity: 0;
	transition: opacity 0.3s ease;
}

.image-preview-wrapper:active .image-overlay {
	opacity: 1;
}

.preview-hint {
	font-size: 24rpx;
	color: #ffffff;
}

.video-preview-wrapper {
	border-radius: 12rpx;
	overflow: hidden;
	background: #000000;
}

.media-preview-video {
	width: 100%;
	min-height: 400rpx;
}

.audio-preview {
	width: 100%;
	margin-top: 20rpx;
}

.audio-player-wrapper {
	background: #ffffff;
	border-radius: 12rpx;
	padding: 20rpx;
	box-shadow: 0 2rpx 8rpx rgba(74, 144, 226, 0.1);
	border: 1rpx solid #e2e8f0;
}

.audio-control-bar {
	display: flex;
	align-items: center;
	gap: 20rpx;
}

.audio-play-icon {
	width: 60rpx;
	height: 60rpx;
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
	box-shadow: 0 2rpx 8rpx rgba(74, 144, 226, 0.3);
	transition: all 0.3s ease;
}

.audio-play-icon:active {
	transform: scale(0.95);
	box-shadow: 0 1rpx 4rpx rgba(74, 144, 226, 0.4);
}

.play-icon-text {
	font-size: 24rpx;
	color: #ffffff;
	margin-left: 2rpx;
}

.audio-progress-area {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 12rpx;
}

.audio-info-line {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.audio-title {
	font-size: 26rpx;
	color: #1f2937;
	font-weight: 500;
}

.audio-time {
	font-size: 22rpx;
	color: #64748b;
}

.progress-bar-wrapper {
	width: 100%;
	height: 6rpx;
	background: #e2e8f0;
	border-radius: 3rpx;
	position: relative;
	cursor: pointer;
}

.progress-bar-bg {
	width: 100%;
	height: 100%;
	background: #e2e8f0;
	border-radius: 3rpx;
	position: relative;
	overflow: hidden;
}

.progress-bar-fill {
	height: 100%;
	background: linear-gradient(90deg, #4a90e2 0%, #6aa8ff 100%);
	border-radius: 3rpx;
	transition: width 0.1s linear;
}

.emotion-result-section {
	margin-top: 30rpx;
	padding: 30rpx;
	background: linear-gradient(135deg, #e0f2fe 0%, #dbeafe 100%);
	border-radius: 16rpx;
	border: 1rpx solid #bfdbfe;
}

.result-header {
	margin-bottom: 20rpx;
}

.result-title {
	font-size: 30rpx;
	font-weight: 600;
	color: #1e40af;
}

.result-content {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.emotion-item {
	display: flex;
	align-items: flex-start;
	gap: 12rpx;
}

.emotion-label {
	font-size: 26rpx;
	color: #64748b;
	font-weight: 500;
	min-width: 140rpx;
}

.emotion-value {
	font-size: 26rpx;
	color: #1e40af;
	font-weight: 600;
}

.emotion-desc {
	font-size: 26rpx;
	color: #334155;
	line-height: 1.6;
	flex: 1;
}

.analysis-section {
	margin-top: 30rpx;
	padding-top: 30rpx;
	border-top: 1rpx solid #e2e8f0;
}

.analysis-btn {
	width: 100%;
	padding: 24rpx;
	background: linear-gradient(135deg, #E67E73 0%, #d46b60 100%);
	color: #fff;
	border: none;
	border-radius: 16rpx;
	font-size: 32rpx;
	font-weight: 600;
	box-shadow: 0 4rpx 16rpx rgba(230, 126, 115, 0.3);
}

.analysis-btn:active {
	opacity: 0.8;
	transform: scale(0.98);
}

/* 响应式设计 */
@media (max-width: 768px) {
	.header {
		padding: 24rpx 30rpx;
	}
	
	.title {
		font-size: 32rpx;
	}
	
	.diary-content {
		margin: 20rpx;
		padding: 30rpx;
	}
	
	.diary-title {
		font-size: 38rpx;
	}
	
	.media-list {
		grid-template-columns: repeat(auto-fill, minmax(180rpx, 1fr));
	}
}
</style>