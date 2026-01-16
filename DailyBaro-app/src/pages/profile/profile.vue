<template>
	<view class="profile-container">
		<view class="content">
			<!-- 用户信息卡片 -->
			<view class="user-card">
				<view class="user-info-row">
					<image class="avatar" :src="userInfo.avatar || '/static/imgs/avatar.png'" mode="aspectFill" @tap="changeAvatar"></image>
					<view class="user-details">
						<text class="username">{{ userInfo.account || '用户' }}</text>
						<text class="user-id">ID: {{ userInfo.uid || '未知' }}</text>
						<text class="energy">能量值: {{ userInfo.energy || 0 }}</text>
					</view>
				</view>
			</view>
			
			<!-- 今日日签 -->
			<view v-if="dailyQuote" class="daily-quote-card" @tap="goToQuotes">
				<view class="quote-icon">📝</view>
				<view class="quote-content-compact">
					<text class="quote-label-compact">今日日签</text>
					<text class="quote-text-compact">{{ dailyQuote.content || '今天也值得被温柔对待' }}</text>
				</view>
				<view class="quote-arrow">›</view>
			</view>
			
			<!-- 功能菜单 -->
			<view class="menu-section-no-box">
				<view class="menu-item" @tap="goToEmotion">
					<text class="menu-icon">📊</text>
					<text class="menu-text">情绪分析</text>
					<text class="menu-arrow">›</text>
				</view>
				
				<view class="menu-item" @tap="goToAI">
					<text class="menu-icon">🤖</text>
					<text class="menu-text">AI智能助手</text>
					<text class="menu-arrow">›</text>
				</view>
			</view>
			
			<!-- 个性化推荐区域 -->
			<view class="recommendations-section">
				<view class="section-header">
					<text class="section-title">个性化推荐</text>
					<button class="refresh-btn" @tap="loadRecommendations" :loading="loadingRecommendations">
						{{ loadingRecommendations ? '刷新中...' : '🔄' }}
					</button>
				</view>
				
				<view v-if="recommendations" class="recommendations-content">
					<!-- 趋势主情绪 -->
					<view class="trend-emotion">
						<text class="trend-label">当前情绪趋势：</text>
						<view class="emotion-badge" :class="getEmotionClass(recommendations.dominantEmotion)">
							<text class="emotion-text">{{ recommendations.dominantEmotion }}</text>
						</view>
					</view>
					
					<!-- 个性化建议 -->
					<view v-if="recommendations.advice" class="advice-card">
						<view class="advice-header">
							<text class="advice-icon">💡</text>
							<text class="advice-title">个性化建议</text>
						</view>
						<text class="advice-content">{{ recommendations.advice.tip }}</text>
					</view>
					
					<!-- 推荐活动列表 -->
					<view class="activities-list">
						<text class="activities-title">推荐活动</text>
						<view class="activity-item" v-for="(item, index) in recommendations.items" :key="index">
							<view class="activity-info">
								<text class="activity-name">{{ item.title }}</text>
								<text class="activity-energy">消耗能量：{{ item.energyRequired }}</text>
							</view>
							<button class="try-activity-btn" @tap="tryActivity(item)">
								尝试
							</button>
						</view>
					</view>
				</view>
				
				<!-- 加载状态 -->
				<view v-else-if="loadingRecommendations" class="loading-state">
					<text class="loading-text">正在分析您的情绪数据...</text>
				</view>
				
				<!-- 空状态 -->
				<view v-else class="empty-state">
					<text class="empty-icon">📊</text>
					<text class="empty-text">暂无推荐</text>
					<text class="empty-hint">多写日记，让AI更好地了解您</text>
				</view>
			</view>
			
			<!-- 退出登录 -->
			<view class="logout-section">
				<button class="logout-btn" @tap="logout">
					<text class="logout-icon">🚪</text>
					<text class="logout-text">退出登录</text>
				</button>
			</view>
		</view>
	</view>
</template>

<script>
import { userApi, fileApi, quoteApi } from '../../utils/api.js'

export default {
	data() {
		return {
			userInfo: {},
			recommendations: null,
			loadingRecommendations: false,
			dailyQuote: null
		}
	},
	
	onLoad() {
		this.loadUserInfo()
		this.loadRecommendations()
		this.loadDailyQuote()
	},
	
	onShow() {
		this.loadUserInfo()
		// 每次显示页面时刷新推荐
		this.loadRecommendations()
		this.loadDailyQuote()
	},
	
	methods: {
		async changeAvatar() {
			uni.chooseImage({
				count: 1,
				sizeType: ['compressed'],
				sourceType: ['album', 'camera'],
				success: async (res) => {
					const tempFilePath = res.tempFilePaths[0]
					try {
						uni.showLoading({ title: '上传中...' })
						const uploadRes = await fileApi.uploadFile(tempFilePath, 'avatar')
						console.log('头像上传响应:', uploadRes)
						
						if (uploadRes && uploadRes.code === 200) {
							// 处理多种可能的响应格式
							let avatarUrl = null
							if (uploadRes.data) {
								if (typeof uploadRes.data === 'string') {
									avatarUrl = uploadRes.data
								} else if (uploadRes.data.url) {
									avatarUrl = uploadRes.data.url
								} else if (uploadRes.data.fileUrl) {
									avatarUrl = uploadRes.data.fileUrl
								} else if (uploadRes.data.path) {
									avatarUrl = uploadRes.data.path
								} else if (uploadRes.data.filename) {
									// 如果是文件名，需要拼接完整URL
									avatarUrl = `/api/file/uploads/${uploadRes.data.filename}`
								}
							}
							
							if (!avatarUrl) {
								console.error('无法从响应中提取头像URL:', uploadRes)
								uni.showToast({
									title: '上传成功但无法获取头像地址',
									icon: 'none'
								})
								return
							}
							
							// 确保URL是完整路径
							if (avatarUrl.startsWith('/uploads/')) {
								avatarUrl = `/api/file${avatarUrl}`
							} else if (avatarUrl.startsWith('uploads/')) {
								avatarUrl = `/api/file/${avatarUrl}`
							}
							
							console.log('处理后的头像URL:', avatarUrl)
							
							const userInfo = uni.getStorageSync('userInfo')
							if (userInfo && userInfo.uid) {
								const updateRes = await userApi.updateUserInfo(userInfo.uid, { avatar: avatarUrl })
								if (updateRes && updateRes.code === 200) {
									this.userInfo.avatar = avatarUrl
									userInfo.avatar = avatarUrl
									uni.setStorageSync('userInfo', userInfo)
									uni.showToast({
										title: '头像更新成功',
										icon: 'success'
									})
								} else {
									uni.showToast({
										title: updateRes?.message || '更新失败',
										icon: 'none'
									})
								}
							}
						} else {
							const errorMsg = uploadRes?.message || uploadRes?.msg || '上传失败'
							console.error('头像上传失败:', errorMsg, uploadRes)
							uni.showToast({
								title: errorMsg,
								icon: 'none',
								duration: 3000
							})
						}
					} catch (error) {
						console.error('上传头像失败:', error)
						const errorMsg = error.message || error.errMsg || '上传失败，请重试'
						uni.showToast({
							title: errorMsg,
							icon: 'none',
							duration: 3000
						})
					} finally {
						uni.hideLoading()
					}
				}
			})
		},
		
		async loadUserInfo() {
			try {
				const userInfo = uni.getStorageSync('userInfo')
				if (userInfo && userInfo.uid) {
					const response = await userApi.getUserInfo(userInfo.uid)
					if (response && response.code === 200) {
						this.userInfo = response.data
					}
				}
			} catch (error) {
				console.error('加载用户信息失败:', error)
			}
		},
		
		async loadDailyQuote() {
			try {
				const response = await quoteApi.getDailyQuote()
				if (response && response.code === 200) {
					this.dailyQuote = response.data || { content: '今天也值得被温柔对待' }
				} else {
					// 如果没有自定义日签，尝试获取随机日签
					try {
						const userInfo = uni.getStorageSync('userInfo')
						const uid = userInfo && (userInfo.uid || userInfo.id || userInfo.userId)
						if (uid) {
							const randomResponse = await quoteApi.getUserQuotes(uid)
							if (randomResponse && randomResponse.code === 200) {
								this.dailyQuote = randomResponse.data || { content: '今天也值得被温柔对待' }
							} else {
								this.dailyQuote = { content: '今天也值得被温柔对待' }
							}
						} else {
							this.dailyQuote = { content: '今天也值得被温柔对待' }
						}
					} catch (error) {
						console.error('加载随机日签失败:', error)
						this.dailyQuote = { content: '今天也值得被温柔对待' }
					}
				}
			} catch (error) {
				console.error('加载日签失败:', error)
				this.dailyQuote = { content: '今天也值得被温柔对待' }
			}
		},
		
		goToEmotion() {
			uni.navigateTo({
				url: '/pages/emotion/emotion'
			})
		},
		
		goToQuotes() {
			uni.navigateTo({
				url: '/pages/quotes/quotes'
			})
		},
		
		goToAI() {
			uni.navigateTo({
				url: '/pages/ai/ai'
			})
		},
		
		// 获取情绪样式类（用于情绪标签）
		getEmotionClass(emotion) {
			const positiveEmotions = ['开心', '兴奋', '满足', '平静', '放松', '期待', '信任']
			const negativeEmotions = ['焦虑', '紧张', '愤怒', '悲伤', '沮丧', '困惑']
			
			if (positiveEmotions.includes(emotion)) return 'positive'
			if (negativeEmotions.includes(emotion)) return 'negative'
			return 'neutral'
		},
		
		showSettings() {
			uni.showToast({
				title: '设置功能开发中',
				icon: 'none'
			})
		},
		
		showAbout() {
			uni.showToast({
				title: '关于功能开发中',
				icon: 'none'
			})
		},
		
		logout() {
			uni.showModal({
				title: '确认退出',
				content: '确定要退出登录吗？',
				success: (res) => {
					if (res.confirm) {
						uni.clearStorageSync()
						uni.reLaunch({
							url: '/pages/login/login'
						})
					}
				}
			})
		},
		
		async loadRecommendations() {
			this.loadingRecommendations = true
			// 清空旧数据，显示加载状态
			this.recommendations = null
			try {
				const userInfo = uni.getStorageSync('userInfo')
				if (userInfo && userInfo.uid) {
					const response = await userApi.getRecommendations()
					if (response && response.code === 200) {
						// 强制更新数据，触发视图刷新
						this.$set(this, 'recommendations', response.data)
						uni.showToast({
							title: '推荐已更新',
							icon: 'success',
							duration: 1500
						})
					} else {
						this.recommendations = null // Clear recommendations on error
						uni.showToast({
							title: response?.message || '加载失败',
							icon: 'none'
						})
					}
				} else {
					uni.showToast({
						title: '用户未登录',
						icon: 'none'
					})
				}
			} catch (error) {
				console.error('加载推荐失败:', error)
				this.recommendations = null
				uni.showToast({
					title: '加载失败，请重试',
					icon: 'none'
				})
			} finally {
				this.loadingRecommendations = false
			}
		},
		
		tryActivity(activity) {
			uni.showModal({
				title: '提示',
				content: `确定要尝试活动 "${activity.title}" 吗？这将消耗 ${activity.energyRequired} 能量。`,
				success: (res) => {
					if (res.confirm) {
						this.performActivity(activity)
					}
				}
			})
		},
		
		async performActivity(activity) {
			const userInfo = uni.getStorageSync('userInfo')
			if (userInfo && userInfo.uid) {
				try {
					const response = await userApi.performActivity(userInfo.uid, activity.id)
					if (response && response.code === 200) {
						uni.showToast({
							title: '活动成功完成！',
							icon: 'none'
						})
						this.loadUserInfo() // Refresh user info to update energy
						this.loadRecommendations() // Refresh recommendations after activity
					} else {
						uni.showToast({
							title: response.msg || '活动失败',
							icon: 'none'
						})
					}
				} catch (error) {
					console.error('执行活动失败:', error)
					uni.showToast({
						title: '执行活动失败',
						icon: 'none'
					})
				}
			}
		}
	}
}
</script>

<style scoped>
.profile-container {
	min-height: 100vh;
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
}

.content {
	padding: 40rpx 30rpx;
}

.user-card {
	padding: 30rpx 0;
	margin-bottom: 30rpx;
}

.user-info-row {
	display: flex;
	align-items: center;
}

.avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	margin-right: 20rpx;
	border: 3rpx solid rgba(255, 255, 255, 0.3);
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
	cursor: pointer;
}

.user-details {
	flex: 1;
}

.username {
	font-size: 32rpx;
	font-weight: 600;
	color: #ffffff;
	margin-bottom: 5rpx;
	display: block;
}

.user-id {
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.8);
	margin-bottom: 3rpx;
	display: block;
}

.energy {
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.8);
	display: block;
}

.daily-quote-card {
	margin-bottom: 20rpx;
	padding: 20rpx 24rpx;
	background: rgba(255, 255, 255, 0.95);
	border-radius: 16rpx;
	box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
	display: flex;
	align-items: center;
	transition: all 0.3s ease;
}

.daily-quote-card:active {
	background: rgba(255, 255, 255, 0.85);
	transform: scale(0.98);
}

.quote-icon {
	font-size: 36rpx;
	margin-right: 16rpx;
	flex-shrink: 0;
}

.quote-content-compact {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: center;
	min-width: 0;
}

.quote-label-compact {
	font-size: 22rpx;
	color: #718096;
	margin-bottom: 6rpx;
	display: block;
}

.quote-text-compact {
	font-size: 26rpx;
	color: #2d3748;
	font-weight: 500;
	line-height: 1.4;
	display: block;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.quote-arrow {
	font-size: 28rpx;
	color: #a0aec0;
	margin-left: 12rpx;
	flex-shrink: 0;
}


.menu-section-no-box {
	margin-bottom: 30rpx;
}

.menu-section {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 20rpx;
	margin-bottom: 30rpx;
	overflow: hidden;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
}

.menu-section-no-box .menu-item {
	display: flex;
	align-items: center;
	padding: 30rpx 0;
	border-bottom: 1rpx solid rgba(255, 255, 255, 0.2);
	transition: all 0.3s ease;
}

.menu-section-no-box .menu-item:last-child {
	border-bottom: none;
}

.menu-section-no-box .menu-item:active {
	background: transparent;
	transform: scale(0.98);
}

.menu-section-no-box .menu-text {
	color: #ffffff;
}

.menu-section-no-box .menu-icon {
	color: #ffffff;
}

.menu-section-no-box .menu-arrow {
	color: rgba(255, 255, 255, 0.8);
}

.menu-item {
	display: flex;
	align-items: center;
	padding: 30rpx;
	border-bottom: 1rpx solid #f1f5f9;
	transition: all 0.3s ease;
}

.menu-item:last-child {
	border-bottom: none;
}

.menu-item:active {
	background: #f8fafc;
	transform: scale(0.98);
}

.menu-icon {
	font-size: 36rpx;
	margin-right: 20rpx;
	width: 40rpx;
	text-align: center;
}

.menu-text {
	flex: 1;
	font-size: 30rpx;
	color: #1f2937;
	font-weight: 500;
}

.menu-arrow {
	font-size: 24rpx;
	color: #9ca3af;
	font-weight: 300;
}

/* 推荐区域样式 */
.recommendations-section {
	background: #ffffff;
	border-radius: 20rpx;
	padding: 40rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.section-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}

.section-title {
	font-size: 30rpx;
	font-weight: 700;
	color: #1f2937;
}

.refresh-btn {
	background: linear-gradient(135deg, #4a90e2, #6aa8ff);
	color: #ffffff;
	border-radius: 12rpx;
	padding: 10rpx 20rpx;
	font-size: 24rpx;
	font-weight: 500;
	border: none;
	box-shadow: 0 2rpx 8rpx rgba(74, 144, 226, 0.3);
	transition: all 0.3s ease;
}

.refresh-btn:active {
	transform: scale(0.95);
	box-shadow: 0 1rpx 4rpx rgba(74, 144, 226, 0.4);
}

.recommendations-content {
	padding-top: 10rpx;
}

.trend-emotion {
	display: flex;
	align-items: center;
	margin-bottom: 30rpx;
	gap: 15rpx;
}

.trend-label {
	font-size: 26rpx;
	font-weight: 600;
	color: #374151;
}

.emotion-badge {
	border-radius: 20rpx;
	padding: 8rpx 16rpx;
	display: flex;
	align-items: center;
}

.emotion-badge.positive {
	background: linear-gradient(135deg, #10b981, #059669);
}

.emotion-badge.negative {
	background: linear-gradient(135deg, #ef4444, #dc2626);
}

.emotion-badge.neutral {
	background: linear-gradient(135deg, #6b7280, #4b5563);
}

.emotion-text {
	font-size: 22rpx;
	font-weight: 600;
	color: #ffffff;
}

.advice-card {
	background: #f9fafb;
	border-radius: 16rpx;
	padding: 25rpx;
	margin-bottom: 30rpx;
	border: 1rpx solid #e5e7eb;
}

.advice-header {
	display: flex;
	align-items: center;
	margin-bottom: 15rpx;
	gap: 12rpx;
}

.advice-icon {
	font-size: 28rpx;
}

.advice-title {
	font-size: 26rpx;
	font-weight: 600;
	color: #374151;
}

.advice-content {
	font-size: 24rpx;
	color: #4b5563;
	line-height: 1.6;
}

.activities-list {
	margin-top: 10rpx;
}

.activities-title {
	font-size: 26rpx;
	font-weight: 600;
	color: #374151;
	margin-bottom: 20rpx;
	display: block;
}

.activity-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx 0;
	border-bottom: 1rpx solid #e5e7eb;
	transition: all 0.3s ease;
}

.activity-item:last-child {
	border-bottom: none;
}

.activity-item:active {
	background: transparent;
	transform: translateY(2rpx);
}

.activity-info {
	flex: 1;
	margin-right: 20rpx;
}

.activity-name {
	font-size: 26rpx;
	font-weight: 500;
	color: #1f2937;
	margin-bottom: 8rpx;
	display: block;
}

.activity-energy {
	font-size: 22rpx;
	color: #6b7280;
}

.try-activity-btn {
	background: linear-gradient(135deg, #4a90e2, #6aa8ff);
	color: #ffffff;
	border-radius: 12rpx;
	padding: 12rpx 24rpx;
	font-size: 24rpx;
	font-weight: 500;
	border: none;
	box-shadow: 0 2rpx 8rpx rgba(74, 144, 226, 0.3);
	transition: all 0.3s ease;
}

.try-activity-btn:active {
	transform: translateY(2rpx);
	box-shadow: 0 1rpx 4rpx rgba(74, 144, 226, 0.4);
}

/* 加载状态 */
.loading-state {
	text-align: center;
	padding: 60rpx 40rpx;
}

.loading-text {
	font-size: 26rpx;
	color: #6b7280;
}

/* 空状态 */
.empty-state {
	text-align: center;
	padding: 60rpx 40rpx;
}

.empty-icon {
	font-size: 60rpx;
	margin-bottom: 20rpx;
	display: block;
}

.empty-text {
	font-size: 26rpx;
	color: #6b7280;
	margin-bottom: 10rpx;
	display: block;
}

.empty-hint {
	font-size: 22rpx;
	color: #9ca3af;
	line-height: 1.5;
}

/* 退出登录 */
.logout-section {
	margin-top: 40rpx;
	display: flex;
	justify-content: center;
}

.logout-btn {
	background: rgba(239, 68, 68, 0.1);
	border: 1rpx solid rgba(239, 68, 68, 0.3);
	border-radius: 20rpx;
	padding: 16rpx 32rpx;
	display: flex;
	align-items: center;
	gap: 12rpx;
	transition: all 0.3s ease;
	box-shadow: 0 2rpx 8rpx rgba(239, 68, 68, 0.1);
}

.logout-btn:active {
	background: rgba(239, 68, 68, 0.2);
	transform: scale(0.95);
	box-shadow: 0 1rpx 4rpx rgba(239, 68, 68, 0.2);
}

.logout-icon {
	font-size: 24rpx;
}

.logout-text {
	font-size: 24rpx;
	color: #dc2626;
	font-weight: 500;
}
</style>
