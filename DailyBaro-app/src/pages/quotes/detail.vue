<template>
	<view class="detail-container">
		<!-- 自定义导航栏 -->
		<view class="custom-navbar">
			<view class="navbar-left" @click="goBack">
				<text class="back-icon">←</text>
			</view>
			<view class="navbar-title">日签详情</view>
			<view class="navbar-right" @click="editQuote">
				<text class="edit-text">编辑</text>
			</view>
		</view>

		<!-- 内容区域 -->
		<view class="content">
			<!-- 日签卡片 -->
			<view class="quote-card">
				<view class="quote-header">
					<text class="quote-title">{{ quote.title || '我的日签' }}</text>
					<text class="quote-time">{{ formatTime(quote.createTime) }}</text>
				</view>
				
				<view class="quote-body">
					<text class="quote-content">{{ quote.content }}</text>
				</view>
				
				<view class="quote-footer">
					<view class="quote-tags">
						<text v-if="quote.emotion" class="emotion-tag">{{ quote.emotion }}</text>
						<text v-if="quote.category" class="category-tag">{{ quote.category }}</text>
						<text v-if="quote.tags" class="tags-text">{{ quote.tags }}</text>
					</view>
					
					<view class="quote-stats">
						<view class="stat-item">
							<text class="stat-icon"></text>
							<text class="stat-value">{{ quote.views || 0 }}</text>
						</view>
						<view class="stat-item">
							<text class="stat-icon"></text>
							<text class="stat-value">{{ quote.likes || 0 }}</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 操作按钮 -->
			<view class="action-buttons">
				<button class="action-btn share" @click="shareQuote">
					<text class="btn-icon"></text>
					<text class="btn-text">分享</text>
				</button>
				<button class="action-btn like" @click="likeQuote" :class="{ active: quote.isLiked }">
					<text class="btn-icon"></text>
					<text class="btn-text">{{ quote.isLiked ? '已点赞' : '点赞' }}</text>
				</button>
				<button class="action-btn delete" @click="deleteQuote">
					<text class="btn-icon">🗑️</text>
					<text class="btn-text">删除</text>
				</button>
			</view>

			<!-- 相关推荐 -->
			<view class="recommendations" v-if="recommendations.length > 0">
				<view class="section-header">
					<text class="section-title">相关推荐</text>
				</view>
				<view class="recommendation-list">
					<view 
						v-for="item in recommendations" 
						:key="item.id"
						class="recommendation-item"
						@click="viewRecommendation(item.id)"
					>
						<text class="rec-content">{{ item.content }}</text>
						<text class="rec-author">— {{ item.author }}</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			quoteId: '',
			quote: {},
			recommendations: []
		}
	},
	onLoad(options) {
		if (options.id) {
			this.quoteId = options.id
			this.loadQuoteDetail()
		}
	},
	methods: {
		goBack() {
			uni.navigateBack()
		},
		editQuote() {
			uni.navigateTo({
				url: `/pages/quotes/edit?id=${this.quoteId}`
			})
		},
		async loadQuoteDetail() {
			try {
				uni.showLoading({ title: '加载中...' })
				
				// 模拟加载数据
				setTimeout(() => {
					this.quote = {
						id: this.quoteId,
						title: '美好的一天',
						content: '今天又是充满希望的一天，阳光明媚，心情愉悦。感谢生活中的每一个美好瞬间，让我能够感受到幸福的存在。',
						createTime: new Date().toISOString(),
						emotion: '开心',
						category: '生活感悟',
						tags: '生活,感悟,正能量',
						views: 128,
						likes: 15,
						isLiked: false
					}
					
					this.recommendations = [
						{
							id: 1,
							content: '每一个当下都是礼物',
							author: '感恩'
						},
						{
							id: 2,
							content: '保持微笑，保持希望',
							author: '乐观'
						},
						{
							id: 3,
							content: '相信自己，你可以的',
							author: '鼓励'
						}
					]
					
					uni.hideLoading()
				}, 1000)
			} catch (error) {
				uni.hideLoading()
				uni.showToast({
					title: '加载失败',
					icon: 'none'
				})
			}
		},
		shareQuote() {
			uni.showToast({
				title: '分享功能开发中',
				icon: 'none'
			})
		},
		likeQuote() {
			this.quote.isLiked = !this.quote.isLiked
			if (this.quote.isLiked) {
				this.quote.likes++
				uni.showToast({
					title: '点赞成功',
					icon: 'success'
				})
			} else {
				this.quote.likes--
			}
		},
		deleteQuote() {
			uni.showModal({
				title: '确认删除',
				content: '确定要删除这个日签吗？删除后无法恢复。',
				success: (res) => {
					if (res.confirm) {
						uni.showToast({
							title: '删除成功',
							icon: 'success'
						})
						setTimeout(() => {
							uni.navigateBack()
						}, 1500)
					}
				}
			})
		},
		viewRecommendation(id) {
			uni.showToast({
				title: '查看推荐功能开发中',
				icon: 'none'
			})
		},
		formatTime(dateString) {
			const date = new Date(dateString)
			return date.toLocaleDateString('zh-CN', {
				year: 'numeric',
				month: '2-digit',
				day: '2-digit',
				hour: '2-digit',
				minute: '2-digit'
			})
		}
	}
}
</script>

<style scoped>
.detail-container {
	min-height: 100vh;
	background-color: #f8f9fa;
	position: relative;
}

.custom-navbar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 20rpx 30rpx;
	background-color: #ffffff;
	border-bottom: 1rpx solid #e9ecef;
	position: sticky;
	top: 0;
	z-index: 100;
}

.navbar-left {
	width: 60rpx;
}

.back-icon {
	font-size: 36rpx;
	color: #333;
}

.navbar-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
}

.navbar-right {
	width: 60rpx;
	text-align: right;
}

.edit-text {
	font-size: 28rpx;
	color: #4a90e2;
	font-weight: 500;
}

.content {
	padding: 30rpx;
	padding-bottom: 100rpx;
}

.quote-card {
	background-color: #ffffff;
	border-radius: 12rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.quote-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}

.quote-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
}

.quote-time {
	font-size: 24rpx;
	color: #666;
}

.quote-body {
	margin-bottom: 30rpx;
}

.quote-content {
	font-size: 28rpx;
	color: #333;
	line-height: 1.8;
}

.quote-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.quote-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 10rpx;
}

.emotion-tag, .category-tag {
	padding: 8rpx 16rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
}

.emotion-tag {
	background-color: #f0f2ff;
	color: #4a90e2;
}

.category-tag {
	background-color: #f0f9ff;
	color: #0ea5e9;
}

.tags-text {
	font-size: 24rpx;
	color: #666;
}

.quote-stats {
	display: flex;
	gap: 20rpx;
}

.stat-item {
	display: flex;
	align-items: center;
	gap: 8rpx;
}

.stat-icon {
	font-size: 24rpx;
}

.stat-value {
	font-size: 24rpx;
	color: #666;
}

.action-buttons {
	display: flex;
	gap: 20rpx;
	margin-bottom: 40rpx;
}

.action-btn {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 20rpx;
	background-color: #ffffff;
	border: 1rpx solid #e9ecef;
	border-radius: 12rpx;
	transition: all 0.3s ease;
}

.action-btn.active {
	background-color: #fef2f2;
	border-color: #fecaca;
}

.btn-icon {
	font-size: 32rpx;
	margin-bottom: 8rpx;
}

.btn-text {
	font-size: 24rpx;
	color: #333;
}

.action-btn.share {
	border-color: #4a90e2;
}

.action-btn.like.active {
	background-color: #fef2f2;
	border-color: #fecaca;
}

.action-btn.delete {
	border-color: #ef4444;
}

.recommendations {
	background-color: #ffffff;
	border-radius: 12rpx;
	padding: 30rpx;
}

.section-header {
	margin-bottom: 20rpx;
}

.section-title {
	font-size: 28rpx;
	font-weight: 600;
	color: #333;
}

.recommendation-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.recommendation-item {
	padding: 20rpx;
	background-color: #f8f9fa;
	border-radius: 8rpx;
	border-left: 4rpx solid #4a90e2;
}

.rec-content {
	display: block;
	font-size: 26rpx;
	color: #333;
	line-height: 1.6;
	margin-bottom: 10rpx;
}

.rec-author {
	font-size: 24rpx;
	color: #666;
	font-style: italic;
}
</style>
