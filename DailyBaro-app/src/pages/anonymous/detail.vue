<template>
	<view class="detail-container">
		<view class="header">
			<view class="back-btn" @tap="goBack">←</view>
			<text class="title">动态详情</text>
			<view class="more-btn" @tap="showMore">⋯</view>
		</view>
		
		<view v-if="loading" class="loading">
			<text>加载中...</text>
		</view>
		
		<view v-else-if="post" class="post-detail">
			<view class="post-card">
				<view class="post-header">
					<view class="user-info">
						<view class="user-avatar">
							<text class="avatar-text">匿</text>
						</view>
						<text class="post-time">{{ formatDate(post.createTime) }}</text>
					</view>
					<view class="post-visibility">
						<text v-if="post.visibility === 'private'" class="visibility-badge private">仅自己</text>
						<text v-else class="visibility-badge public">公开</text>
					</view>
				</view>
				
				<view class="post-content">
					<text class="post-text">{{ post.content }}</text>
				</view>
				
				<view class="post-footer">
					<view class="post-actions">
						<view class="action-item" @tap="likePost">
							<text class="action-icon">{{ post.isLiked ? '❤️' : '🤍' }}</text>
							<text class="action-text">{{ post.likeCount || 0 }}</text>
						</view>
						<view class="action-item" @tap="focusComment">
							<text class="action-icon">💬</text>
							<text class="action-text">{{ post.commentCount || 0 }}</text>
						</view>
					</view>
				</view>
			</view>
			
			<view class="comments-section">
				<view class="section-title">评论 ({{ comments.length }})</view>
				
				<view v-if="comments.length === 0" class="no-comments">
					<text class="no-comments-text">还没有评论，说点什么吧</text>
				</view>
				
				<view v-else class="comments-list">
					<view 
						v-for="comment in comments" 
						:key="comment.commentId" 
						class="comment-item"
					>
						<view class="comment-header">
							<view class="comment-user">
								<view class="comment-avatar">
									<text class="avatar-text">匿</text>
								</view>
								<text class="comment-time">{{ formatDate(comment.createTime) }}</text>
							</view>
						</view>
						<text class="comment-content">{{ comment.content }}</text>
					</view>
				</view>
			</view>
		</view>
		
		<view v-else class="empty">
			<text class="empty-icon">📝</text>
			<text class="empty-text">动态不存在</text>
		</view>
		
		<!-- 评论输入框 -->
		<view class="comment-input-section">
			<view class="comment-input-wrapper">
				<input 
					class="comment-input" 
					type="text" 
					placeholder="说点什么..." 
					v-model="commentText"
					:focus="commentFocused"
					@focus="commentFocused = true"
					@blur="commentFocused = false"
				/>
				<button 
					class="comment-btn" 
					:disabled="!commentText.trim()"
					@tap="submitComment"
				>
					发送
				</button>
			</view>
		</view>
	</view>
</template>

<script>
import { anonymousApi } from '@/utils/api.js'

export default {
	data() {
		return {
			postId: null,
			post: null,
			comments: [],
			loading: false,
			commentText: '',
			commentFocused: false
		}
	},
	
	onLoad(options) {
		if (options.id) {
			this.postId = options.id
			this.loadPostDetail()
		}
	},
	
	methods: {
		goBack() {
			uni.navigateBack()
		},
		
		showMore() {
			uni.showActionSheet({
				itemList: ['举报', '分享'],
				success: (res) => {
					if (res.tapIndex === 0) {
						uni.showToast({
							title: '举报功能开发中',
							icon: 'none'
						})
					} else if (res.tapIndex === 1) {
						uni.showToast({
							title: '分享功能开发中',
							icon: 'none'
						})
					}
				}
			})
		},
		
		async loadPostDetail() {
			this.loading = true
			
			try {
				const res = await anonymousApi.getPostDetail(this.postId)
				
				if (res.code === 200) {
					this.post = res.data
					this.loadComments()
				} else {
					uni.showToast({
						title: res.message || '加载失败',
						icon: 'none'
					})
				}
			} catch (error) {
				uni.showToast({
					title: error.message || '加载失败',
					icon: 'none'
				})
			} finally {
				this.loading = false
			}
		},
		
		async loadComments() {
			try {
				// 这里应该调用获取评论的API
				// 暂时使用模拟数据
				this.comments = []
			} catch (error) {
				console.log('加载评论失败', error)
			}
		},
		
		async likePost() {
			try {
				const res = await anonymousApi.likePost(this.post.postId)
				
				if (res.code === 200) {
					this.post.isLiked = !this.post.isLiked
					this.post.likeCount = this.post.isLiked ? (this.post.likeCount || 0) + 1 : (this.post.likeCount || 1) - 1
					
					uni.showToast({
						title: this.post.isLiked ? '点赞成功' : '取消点赞',
						icon: 'success'
					})
				}
			} catch (error) {
				uni.showToast({
					title: error.message || '操作失败',
					icon: 'none'
				})
			}
		},
		
		focusComment() {
			this.commentFocused = true
		},
		
		async submitComment() {
			if (!this.commentText.trim()) return
			
			try {
				const res = await anonymousApi.commentPost(this.postId, {
					content: this.commentText
				})
				
				if (res.code === 200) {
					uni.showToast({
						title: '评论成功',
						icon: 'success'
					})
					
					this.commentText = ''
					this.commentFocused = false
					
					// 刷新评论列表
					this.loadComments()
				}
			} catch (error) {
				uni.showToast({
					title: error.message || '评论失败',
					icon: 'none'
				})
			}
		},
		
		formatDate(dateStr) {
			if (!dateStr) return ''
			
			const date = new Date(dateStr)
			const now = new Date()
			const diff = now - date
			const minutes = Math.floor(diff / (1000 * 60))
			const hours = Math.floor(diff / (1000 * 60 * 60))
			const days = Math.floor(diff / (1000 * 60 * 60 * 24))
			
			if (minutes < 1) return '刚刚'
			if (minutes < 60) return `${minutes}分钟前`
			if (hours < 24) return `${hours}小时前`
			if (days < 7) return `${days}天前`
			
			return date.toLocaleDateString()
		}
	}
}
</script>

<style scoped>
.detail-container {
	min-height: 100vh;
	background: #f8fafc;
	padding-bottom: 120rpx;
}

.header {
	background: #ffffff;
	padding: 30rpx 40rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	border-bottom: 1rpx solid #e2e8f0;
}

.back-btn, .more-btn {
	font-size: 28rpx;
	color: #64748b;
	font-weight: 500;
}

.title {
	font-size: 32rpx;
	font-weight: 600;
	color: #1f2937;
}

.loading, .empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 100rpx 40rpx;
	color: #64748b;
}

.empty-icon {
	font-size: 80rpx;
	margin-bottom: 20rpx;
}

.empty-text {
	font-size: 28rpx;
}

.post-detail {
	padding: 20rpx;
}

.post-card {
	background: #ffffff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.post-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 20rpx;
}

.user-info {
	display: flex;
	align-items: center;
}

.user-avatar {
	width: 60rpx;
	height: 60rpx;
	border-radius: 50%;
	background: #f1f5f9;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: 15rpx;
}

.avatar-text {
	font-size: 24rpx;
	color: #64748b;
	font-weight: 500;
}

.post-time {
	font-size: 24rpx;
	color: #94a3b8;
}

.visibility-badge {
	padding: 6rpx 12rpx;
	border-radius: 8rpx;
	font-size: 20rpx;
	font-weight: 500;
}

.visibility-badge.private {
	background: #fef3c7;
	color: #d97706;
}

.visibility-badge.public {
	background: #dbeafe;
	color: #2563eb;
}

.post-content {
	margin-bottom: 20rpx;
}

.post-text {
	font-size: 30rpx;
	color: #1f2937;
	line-height: 1.6;
}

.post-footer {
	border-top: 1rpx solid #f1f5f9;
	padding-top: 20rpx;
}

.post-actions {
	display: flex;
	gap: 40rpx;
}

.action-item {
	display: flex;
	align-items: center;
	gap: 8rpx;
}

.action-icon {
	font-size: 28rpx;
}

.action-text {
	font-size: 24rpx;
	color: #64748b;
}

.comments-section {
	padding: 0 20rpx;
}

.section-title {
	font-size: 28rpx;
	font-weight: 600;
	color: #1f2937;
	margin-bottom: 20rpx;
}

.no-comments {
	text-align: center;
	padding: 40rpx;
	color: #94a3b8;
}

.no-comments-text {
	font-size: 26rpx;
}

.comments-list {
	background: #ffffff;
	border-radius: 16rpx;
	overflow: hidden;
}

.comment-item {
	padding: 25rpx;
	border-bottom: 1rpx solid #f1f5f9;
}

.comment-item:last-child {
	border-bottom: none;
}

.comment-header {
	margin-bottom: 15rpx;
}

.comment-user {
	display: flex;
	align-items: center;
}

.comment-avatar {
	width: 40rpx;
	height: 40rpx;
	border-radius: 50%;
	background: #f1f5f9;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: 15rpx;
}

.comment-time {
	font-size: 22rpx;
	color: #94a3b8;
}

.comment-content {
	font-size: 26rpx;
	color: #1f2937;
	line-height: 1.5;
}

.comment-input-section {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background: #ffffff;
	border-top: 1rpx solid #e2e8f0;
	padding: 20rpx;
}

.comment-input-wrapper {
	display: flex;
	align-items: center;
	gap: 20rpx;
}

.comment-input {
	flex: 1;
	height: 70rpx;
	background: #f8fafc;
	border: 2rpx solid #e2e8f0;
	border-radius: 35rpx;
	padding: 0 25rpx;
	font-size: 28rpx;
}

.comment-input:focus {
	border-color: #4a90e2;
}

.comment-btn {
	height: 70rpx;
	background: #4a90e2;
	color: #ffffff;
	border: none;
	border-radius: 35rpx;
	padding: 0 30rpx;
	font-size: 28rpx;
	font-weight: 500;
}

.comment-btn:disabled {
	background: #94a3b8;
}
</style>
