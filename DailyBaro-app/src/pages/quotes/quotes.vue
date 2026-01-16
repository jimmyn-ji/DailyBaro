<template>
	<view class="quotes-container">
		<!-- 顶部导航栏 -->
		<view class="nav-bar">
			<!-- 返回按钮 -->
			<view class="back-btn" @tap="goBack">
				<text class="back-icon">←</text>
			</view>
			<text class="page-title">我的日签</text>
			<!-- 移除创建按钮 -->
		</view>
		
		<view class="content">
			<!-- 今日日签 -->
			<view class="daily-quote-section">
				<view class="section-header">
					<text class="section-title">今日日签</text>
					<!-- 圆形创建按钮 -->
					<view class="create-circle-btn" @tap="goToCreate">
						<text class="create-circle-icon">✏️</text>
					</view>
				</view>
				<view class="daily-quote-card">
					<view class="quote-content">
						<text class="quote-text">{{ dailyQuote?.content || '今天也值得被温柔对待' }}</text>
					</view>
					<view class="quote-footer">
						<text class="quote-author">{{ dailyQuote?.author || 'DailyBaro' }}</text>
						<text class="quote-date">{{ formatDate(new Date()) }}</text>
					</view>
				</view>
			</view>
			
			<!-- 我的日签 -->
			<view class="my-quotes-section">
				<view class="section-header">
					<text class="section-title">我的日签</text>
				</view>
				
				<view class="quotes-list">
					<view 
						v-for="quote in myQuotes" 
						:key="quote.id"
						class="quote-item"
						@tap="viewQuote(quote.id)"
					>
						<view class="quote-header">
							<text class="quote-title">{{ quote.title || '我的日签' }}</text>
							<text class="quote-time">{{ formatTime(quote.createTime) }}</text>
						</view>
						
						<view class="quote-body">
							<text class="quote-content-text">{{ quote.content }}</text>
						</view>
						
						<view class="quote-footer">
							<view class="quote-tags">
								<text v-if="quote.emotion" class="emotion-tag">{{ quote.emotion }}</text>
								<text v-if="quote.category" class="category-tag">{{ quote.category }}</text>
							</view>
							<view class="quote-actions">
								<button class="action-btn edit" @tap.stop="editQuote(quote.id)">编辑</button>
								<button class="action-btn delete" @tap.stop="deleteQuote(quote.id)">删除</button>
							</view>
						</view>
					</view>
					
					<view v-if="myQuotes.length === 0" class="empty-quotes">
						<text class="empty-icon">📝</text>
						<text class="empty-text">还没有创建过日签</text>
						<text class="empty-desc">创建属于你的专属日签，记录美好时光</text>
						<button class="empty-btn" @tap="goToCreate">创建第一个日签</button>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { quoteApi } from '../../utils/api.js'

export default {
	data() {
		return {
			dailyQuote: null,
			myQuotes: [],
			loading: false
		}
	},
	
	onLoad() {
		this.loadDailyQuote()
		this.loadMyQuotes()
	},
	
	methods: {
		// 返回方法
		goBack() {
			uni.navigateBack()
		},
		
		// 跳转到创建日签页面
		goToCreate() {
			uni.navigateTo({
				url: '/pages/quotes/create'
			})
		},
		
		// 查看日签详情
		viewQuote(id) {
			uni.navigateTo({
				url: `/pages/quotes/detail?id=${id}`
			})
		},
		
		// 编辑日签
		editQuote(id) {
			uni.navigateTo({
				url: `/pages/quotes/edit?id=${id}`
			})
		},
		
		// 删除日签
		deleteQuote(id) {
			uni.showModal({
				title: '确认删除',
				content: '确定要删除这个日签吗？删除后无法恢复。',
				success: async (res) => {
					if (res.confirm) {
						try {
							uni.showLoading({ title: '删除中...' })
							
							const result = await quoteApi.deleteQuote(id)
							
							uni.hideLoading()
							
							if (result.code === 200) {
								uni.showToast({
									title: '删除成功',
									icon: 'success'
								})
								this.loadMyQuotes()	}
							 else {
								throw new Error(result.message || '删除失败')
							}
						} catch (error) {
							uni.hideLoading()
							uni.showToast({
								title: error.message || '删除失败',
								icon: 'none'
							})
						}
					}
				}
			})
		},
		
		// 加载今日日签
		async loadDailyQuote() {
			try {
				const res = await quoteApi.getDailyQuote()
				
				if (res.code === 200) {
					this.dailyQuote = res.data
				}
			} catch (error) {
				console.error('加载今日日签失败:', error)
			}
		},
		
		// 加载我的日签
		async loadMyQuotes() {
			try {
				const userInfo = uni.getStorageSync('userInfo')
				if (!userInfo || !userInfo.uid) {
					return
				}
				
				const res = await quoteApi.getUserQuotes(userInfo.uid)
				
				if (res.code === 200) {
					this.myQuotes = res.data || []
				}
			} catch (error) {
				console.error('加载我的日签失败:', error)
			}
		},
		
		// 格式化日期
		formatDate(date) {
			return date.toLocaleDateString('zh-CN', {
				year: 'numeric',
				month: '2-digit',
				day: '2-digit'
			})
		},
		
		// 格式化时间
		formatTime(dateString) {
			if (!dateString) return ''
			
			const date = new Date(dateString)
			const now = new Date()
			const diffTime = Math.abs(now - date)
			const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
			
			if (diffDays === 1) {
				return '昨天'
			} else if (diffDays === 0) {
				return '今天'
			} else if (diffDays < 7) {
				return `${diffDays}天前`
			} else {
				return date.toLocaleDateString('zh-CN', {
					month: '2-digit',
					day: '2-digit'
				})
			}
		}
	}
}
</script>

<style scoped>
.quotes-container {
	min-height: 100vh;
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
	padding: 20rpx 20rpx 40rpx;
}

/* 顶部导航栏 */
.nav-bar {
	background: rgba(255, 255, 255, 0.9);
	padding: 30rpx 40rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	border-radius: 20rpx;
	margin-bottom: 30rpx;
	backdrop-filter: blur(10px);
	box-shadow: 0 8rpx 32rpx rgba(31, 38, 135, 0.15);
	position: relative;
}

/* 返回按钮样式 */
.back-btn {
	width: 60rpx;
	height: 60rpx;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	background: rgba(102, 126, 234, 0.1);
	z-index: 10;
	transition: all 0.3s ease;
}

.back-btn:active {
	background: rgba(102, 126, 234, 0.2);
	transform: scale(0.95);
}

.back-icon {
	font-size: 36rpx;
	color: #4a90e2;
	font-weight: bold;
}

.page-title {
	font-size: 36rpx;
	font-weight: 700;
	color: #2c3e50;
	letter-spacing: 1rpx;
	position: absolute;
	left: 0;
	right: 0;
	text-align: center;
	z-index: 1;
}

/* 内容区域 */
.content {
	padding: 0 10rpx;
}

/* 今日日签 */
.daily-quote-section {
	margin-bottom: 40rpx;
	position: relative;
}

.section-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 20rpx;
	padding-left: 20rpx;
	padding-right: 20rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: 700;
	color: #ffffff;
	text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
}

/* 圆形创建按钮 */
.create-circle-btn {
	width: 80rpx;
	height: 80rpx;
	background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 8rpx 20rpx rgba(255, 154, 158, 0.3);
	transition: all 0.3s ease;
}

.create-circle-btn:active {
	transform: scale(0.95);
	box-shadow: 0 4rpx 10rpx rgba(255, 154, 158, 0.4);
}

.create-circle-icon {
	font-size: 36rpx;
}

.daily-quote-card {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 24rpx;
	padding: 50rpx 40rpx;
	box-shadow: 0 12rpx 36rpx rgba(0, 0, 0, 0.15);
	backdrop-filter: blur(10px);
	text-align: center;
	position: relative;
	overflow: hidden;
	border: 1rpx solid rgba(255, 255, 255, 0.2);
}

.daily-quote-card::before {
	content: '"';
	position: absolute;
	top: 20rpx;
	left: 30rpx;
	font-size: 80rpx;
	color: rgba(102, 126, 234, 0.1);
	font-family: serif;
}

.quote-content {
	margin-bottom: 40rpx;
	position: relative;
	z-index: 2;
}

.quote-text {
	font-size: 36rpx;
	color: #2c3e50;
	line-height: 1.6;
	font-weight: 500;
	font-style: italic;
	text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.05);
}

.quote-footer {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding-top: 30rpx;
	border-top: 1rpx solid rgba(226, 232, 240, 0.6);
	position: relative;
	z-index: 2;
}

.quote-author {
	font-size: 28rpx;
	color: #4a90e2;
	font-weight: 600;
}

.quote-date {
	font-size: 26rpx;
	color: #a0aec0;
}

/* 我的日签 */
.my-quotes-section {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 24rpx;
	padding: 40rpx 30rpx;
	box-shadow: 0 12rpx 36rpx rgba(0, 0, 0, 0.15);
	backdrop-filter: blur(10px);
	border: 1rpx solid rgba(255, 255, 255, 0.2);
}

.my-quotes-section .section-header {
	margin-bottom: 30rpx;
	padding: 0;
}

/* 其余样式保持不变... */
.quotes-list {
	display: flex;
	flex-direction: column;
	gap: 24rpx;
}

.quote-item {
	background: #f8fafc;
	border-radius: 20rpx;
	padding: 30rpx;
	border: 1rpx solid #e2e8f0;
	transition: all 0.3s ease;
	position: relative;
	overflow: hidden;
}

.quote-item::before {
	content: "";
	position: absolute;
	top: 0;
	left: 0;
	width: 8rpx;
	height: 100%;
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
}

.quote-item:active {
	transform: translateY(4rpx);
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.quote-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 20rpx;
}

.quote-title {
	font-size: 30rpx;
	font-weight: 600;
	color: #2c3e50;
}

.quote-time {
	font-size: 24rpx;
	color: #a0aec0;
}

.quote-body {
	margin-bottom: 25rpx;
}

.quote-content-text {
	font-size: 28rpx;
	color: #4a5568;
	line-height: 1.5;
	display: -webkit-box;
	-webkit-line-clamp: 3;
	-webkit-box-orient: vertical;
	overflow: hidden;
}

.quote-footer {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.quote-tags {
	display: flex;
	gap: 12rpx;
}

.emotion-tag {
	background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%);
	color: #ffffff;
	padding: 8rpx 16rpx;
	border-radius: 20rpx;
	font-size: 22rpx;
	font-weight: 600;
}

.category-tag {
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
	color: #ffffff;
	padding: 8rpx 16rpx;
	border-radius: 20rpx;
	font-size: 22rpx;
	font-weight: 600;
}

.quote-actions {
	display: flex;
	gap: 12rpx;
}

.action-btn {
	padding: 10rpx 20rpx;
	border-radius: 14rpx;
	font-size: 24rpx;
	border: none;
	transition: all 0.3s ease;
}

.action-btn.edit {
	background: rgba(102, 126, 234, 0.1);
	color: #4a90e2;
	border: 1rpx solid rgba(102, 126, 234, 0.2);
}

.action-btn.delete {
	background: rgba(245, 101, 101, 0.1);
	color: #f56565;
	border: 1rpx solid rgba(245, 101, 101, 0.2);
}

.action-btn:active {
	transform: scale(0.95);
}

/* 空状态 */
.empty-quotes {
	text-align: center;
	padding: 80rpx 40rpx;
}

.empty-icon {
	font-size: 80rpx;
	margin-bottom: 30rpx;
	display: block;
	opacity: 0.7;
}

.empty-text {
	font-size: 32rpx;
	color: #4a5568;
	margin-bottom: 15rpx;
	display: block;
	font-weight: 600;
}

.empty-desc {
	font-size: 26rpx;
	color: #718096;
	line-height: 1.5;
	margin-bottom: 40rpx;
}

.empty-btn {
	background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%);
	color: #ffffff;
	border: none;
	border-radius: 18rpx;
	padding: 24rpx 48rpx;
	font-size: 28rpx;
	font-weight: 600;
	box-shadow: 0 8rpx 20rpx rgba(255, 154, 158, 0.3);
	transition: all 0.3s ease;
}

.empty-btn:active {
	transform: translateY(4rpx);
	box-shadow: 0 4rpx 10rpx rgba(255, 154, 158, 0.4);
}

/* 动画效果 */
@keyframes fadeIn {
	from { opacity: 0; transform: translateY(20rpx); }
	to { opacity: 1; transform: translateY(0); }
}

.daily-quote-card, .quote-item {
	animation: fadeIn 0.5s ease-out;
}

.quote-item:nth-child(2) { animation-delay: 0.1s; }
.quote-item:nth-child(3) { animation-delay: 0.2s; }
.quote-item:nth-child(4) { animation-delay: 0.3s; }
</style>