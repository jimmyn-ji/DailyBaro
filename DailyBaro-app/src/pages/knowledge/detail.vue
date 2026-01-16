<template>
	<view class="detail-container">
		<view class="nav-bar">
			<view class="nav-left" @tap="goBack">
				<text class="back-icon">←</text>
			</view>
			<view class="nav-title">知识详情</view>
			<view class="nav-right"></view>
		</view>
		
		<view v-if="loading" class="loading-section">
			<text>加载中...</text>
		</view>
		
		<view v-else-if="knowledge" class="content">
			<view class="knowledge-card">
				<view class="knowledge-header">
					<text class="knowledge-title">{{ knowledge.title }}</text>
					<view class="knowledge-meta">
						<text class="knowledge-category">{{ knowledge.category }}</text>
						<text v-if="knowledge.subcategory" class="knowledge-subcategory">{{ knowledge.subcategory }}</text>
					</view>
				</view>
				
				<view class="knowledge-content">
					<text>{{ knowledge.content }}</text>
				</view>
				
				<view v-if="knowledge.tags" class="knowledge-tags">
					<text class="tag-label">标签：</text>
					<text class="tag-item" v-for="tag in tagList" :key="tag">{{ tag }}</text>
				</view>
				
				<view class="knowledge-footer">
					<text class="view-count">👁 {{ knowledge.viewCount || 0 }} 次浏览</text>
					<text class="update-time">{{ formatTime(knowledge.updateTime) }}</text>
				</view>
			</view>
		</view>
		
		<view v-else class="error-section">
			<text>加载失败</text>
		</view>
	</view>
</template>

<script>
import { knowledgeApi } from '@/utils/api.js'

export default {
	data() {
		return {
			knowledgeId: null,
			knowledge: null,
			loading: true
		}
	},
	
	computed: {
		tagList() {
			if (!this.knowledge || !this.knowledge.tags) return []
			return this.knowledge.tags.split(',').filter(t => t.trim())
		}
	},
	
	onLoad(options) {
		if (options.id) {
			this.knowledgeId = options.id
			this.loadKnowledge()
		}
	},
	
	methods: {
		goBack() {
			uni.navigateBack()
		},
		
		async loadKnowledge() {
			this.loading = true
			try {
				const baseUrl = this.getBaseUrl()
				const userInfo = uni.getStorageSync('userInfo')
				const uid = userInfo ? userInfo.uid : ''
				
				const response = await uni.request({
					url: baseUrl + '/app/knowledge/' + this.knowledgeId,
					method: 'GET',
					header: {
						'uid': uid
					}
				})
				
				if (response.data && response.data.code === 200) {
					this.knowledge = response.data.data
				} else {
					uni.showToast({
						title: '加载失败',
						icon: 'none'
					})
				}
			} catch (error) {
				console.error('加载知识详情失败:', error)
				uni.showToast({
					title: '加载失败',
					icon: 'none'
				})
			} finally {
				this.loading = false
			}
		},
		
		formatTime(time) {
			if (!time) return ''
			const date = new Date(time)
			return date.toLocaleDateString('zh-CN')
		},
		
		getBaseUrl() {
			const config = require('@/utils/config.js')
			return config.baseUrl || 'https://dailybaro.cn'
		}
	}
}
</script>

<style scoped>
.detail-container {
	min-height: 100vh;
	background: #f5f7fa;
}

.nav-bar {
	background: white;
	padding: 30rpx 40rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	border-bottom: 1rpx solid #e5e7eb;
}

.nav-left {
	display: flex;
	align-items: center;
}

.back-icon {
	font-size: 36rpx;
	color: #6366f1;
}

.nav-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #1f2937;
}

.content {
	padding: 30rpx;
}

.knowledge-card {
	background: white;
	border-radius: 20rpx;
	padding: 40rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.knowledge-header {
	margin-bottom: 30rpx;
	padding-bottom: 20rpx;
	border-bottom: 1rpx solid #e5e7eb;
}

.knowledge-title {
	font-size: 36rpx;
	font-weight: 600;
	color: #1f2937;
	line-height: 1.5;
	margin-bottom: 15rpx;
	display: block;
}

.knowledge-meta {
	display: flex;
	gap: 15rpx;
	align-items: center;
}

.knowledge-category {
	font-size: 24rpx;
	padding: 8rpx 16rpx;
	background: #e0e7ff;
	color: #6366f1;
	border-radius: 12rpx;
}

.knowledge-subcategory {
	font-size: 24rpx;
	padding: 8rpx 16rpx;
	background: #f3f4f6;
	color: #6b7280;
	border-radius: 12rpx;
}

.knowledge-content {
	font-size: 30rpx;
	color: #374151;
	line-height: 2;
	margin-bottom: 30rpx;
}

.knowledge-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 10rpx;
	align-items: center;
	margin-bottom: 30rpx;
	padding-top: 20rpx;
	border-top: 1rpx solid #e5e7eb;
}

.tag-label {
	font-size: 24rpx;
	color: #9ca3af;
}

.tag-item {
	font-size: 24rpx;
	padding: 6rpx 12rpx;
	background: #f3f4f6;
	color: #6b7280;
	border-radius: 8rpx;
}

.knowledge-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding-top: 20rpx;
	border-top: 1rpx solid #e5e7eb;
}

.view-count,
.update-time {
	font-size: 24rpx;
	color: #9ca3af;
}

.loading-section,
.error-section {
	text-align: center;
	padding: 100rpx 0;
	font-size: 28rpx;
	color: #9ca3af;
}
</style>
