<template>
	<view class="anonymous-container">
		<!-- 移除了顶部导航栏 -->
		
		<view class="content">
			<!-- 发布动态按钮 -->
			<view class="post-section">
				<button class="post-action-btn" @tap="goToPost">
					<text class="post-action-icon">✏️</text>
					<text class="post-action-text">分享你的心情</text>
				</button>
			</view>
			
			<!-- 动态列表 -->
			<view class="posts-list">
				<view 
					v-for="post in posts" 
					:key="post.id"
					class="post-item"
					@tap="viewPost(post.id)"
				>
					<view class="post-header">
						<view class="post-avatar">
							<text class="avatar-text">{{ getRandomAvatar() }}</text>
						</view>
						<view class="post-info">
							<text class="post-nickname">{{ getRandomNickname() }}</text>
							<text class="post-time">{{ formatTime(post.createTime) }}</text>
						</view>
					</view>
					
					<view class="post-content">
						<text class="post-text">{{ post.content }}</text>
					</view>
					
					<view class="post-footer">
						<view class="post-actions">
							<view class="action-item" @tap.stop="likePost(post.id)">
								<text class="action-icon">{{ post.isLiked ? '❤️' : '🤍' }}</text>
								<text class="action-text">{{ post.likeCount || 0 }}</text>
							</view>
							<view class="action-item" @tap.stop="commentPost(post.id)">
								<text class="action-icon">💬</text>
								<text class="action-text">{{ post.commentCount || 0 }}</text>
							</view>
						</view>
					</view>
				</view>
				
				<view v-if="posts.length === 0" class="empty-state">
					<text class="empty-icon">🌍</text>
					<text class="empty-text">还没有动态，快来分享你的心情吧</text>
					<button class="empty-btn" @tap="goToPost">发布第一条动态</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { anonymousApi } from '../../utils/api.js'

export default {
	data() {
		return {
			posts: [],
			loading: false,
			page: 1,
			hasMore: true
		}
	},
	
	onLoad() {
		this.loadPosts()
	},
	
	methods: {
		goBack() {
			uni.navigateBack()
		},
		
		goToPost() {
			uni.navigateTo({
				url: '/pages/anonymous/post'
			})
		},
		
		async loadPosts() {
			if (this.loading) return
			
			this.loading = true
			
			try {
				const res = await anonymousApi.getPosts()
				
				if (res.code === 200) {
					this.posts = res.data || []
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
		
		loadMore() {
			// 可以在这里实现分页加载
			console.log('加载更多')
		},
		
		viewPost(postId) {
			uni.navigateTo({
				url: `/pages/anonymous/detail?id=${postId}`
			})
		},
		
		async likePost(postId) {
			if (!postId) {
				uni.showToast({ title: '无效的帖子ID', icon: 'none' })
				return
			}
			try {
				const res = await anonymousApi.likePost(postId)
				
				if (res.code === 200) {
					const postIndex = this.posts.findIndex(post => post.id === postId)
					if (postIndex !== -1) {
						this.posts[postIndex].isLiked = !this.posts[postIndex].isLiked
						this.posts[postIndex].likeCount = this.posts[postIndex].isLiked ? (this.posts[postIndex].likeCount || 0) + 1 : (this.posts[postIndex].likeCount || 1) - 1
					}
					
					uni.showToast({
						title: this.posts[postIndex].isLiked ? '点赞成功' : '取消点赞',
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
		
		commentPost(postId) {
			uni.navigateTo({
				url: `/pages/anonymous/detail?id=${postId}`
			})
		},
		
		formatTime(dateStr) {
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
		},
		
		getRandomAvatar() {
			const avatars = ['👤', '👧', '👦', '👩', '👨', '👱', '👵', '👴', '👶', '👷']
			return avatars[Math.floor(Math.random() * avatars.length)]
		},
		
		getRandomNickname() {
			const nicknames = ['匿名小星星', '小星星', '匿名星', '星星', '匿名', '星']
			return nicknames[Math.floor(Math.random() * nicknames.length)]
		}
	}
}
</script>

<style scoped>
.anonymous-container {
	min-height: 100vh;
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
}

/* 移除了导航栏样式 */

/* 内容区域 */
.content {
	padding: 20rpx 40rpx;
}

/* 发布动态按钮 */
.post-section {
	padding: 20rpx 0;
	margin-bottom: 30rpx;
}

.post-action-btn {
	width: 100%;
	background: linear-gradient(135deg, #4a90e2, #6aa8ff);
	color: #ffffff;
	border: none;
	border-radius: 20rpx;
	padding: 25rpx 40rpx;
	font-size: 28rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 15rpx;
	box-shadow: 0 6rpx 24rpx rgba(99, 102, 241, 0.3);
	transition: all 0.3s ease;
}

.post-action-btn:active {
	transform: translateY(2rpx);
	box-shadow: 0 3rpx 12rpx rgba(99, 102, 241, 0.4);
}

.post-action-icon {
	font-size: 28rpx;
}

.post-action-text {
	font-size: 28rpx;
	font-weight: 600;
}

/* 动态列表 */
.posts-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.post-item {
	background: #ffffff;
	border-radius: 20rpx;
	padding: 30rpx;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
	transition: all 0.3s ease;
}

.post-item:active {
	transform: translateY(2rpx);
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.12);
}

.post-header {
	display: flex;
	align-items: center;
	gap: 20rpx;
	margin-bottom: 20rpx;
}

.post-avatar {
	width: 60rpx;
	height: 60rpx;
	background: #f3f4f6;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	border: 2rpx solid rgba(99, 102, 241, 0.2);
}

.avatar-text {
	font-size: 28rpx;
	color: #4a90e2;
}

.post-info {
	display: flex;
	flex-direction: column;
	gap: 5rpx;
}

.post-nickname {
	font-size: 26rpx;
	font-weight: 600;
	color: #1f2937;
}

.post-time {
	font-size: 22rpx;
	color: #9ca3af;
}

.post-content {
	margin-bottom: 20rpx;
}

.post-text {
	font-size: 28rpx;
	color: #374151;
	line-height: 1.5;
}

.post-footer {
	display: flex;
	justify-content: flex-end;
}

.post-actions {
	display: flex;
	gap: 30rpx;
}

.action-item {
	display: flex;
	align-items: center;
	gap: 8rpx;
	padding: 10rpx 16rpx;
	border-radius: 20rpx;
	background: rgba(99, 102, 241, 0.05);
	transition: all 0.3s ease;
}

.action-item:active {
	background: rgba(99, 102, 241, 0.1);
	transform: scale(0.95);
}

.action-icon {
	font-size: 24rpx;
}

.action-text {
	font-size: 24rpx;
	color: #6b7280;
	font-weight: 500;
}

/* 空状态 */
.empty-state {
	text-align: center;
	padding: 80rpx 40rpx;
	background: #ffffff;
	border-radius: 20rpx;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
}

.empty-icon {
	font-size: 80rpx;
	margin-bottom: 20rpx;
	display: block;
}

.empty-text {
	font-size: 28rpx;
	color: #6b7280;
	margin-bottom: 30rpx;
	display: block;
}

.empty-btn {
	background: linear-gradient(135deg, #4a90e2, #6aa8ff);
	color: #ffffff;
	border: none;
	border-radius: 16rpx;
	padding: 20rpx 40rpx;
	font-size: 26rpx;
	font-weight: 600;
	box-shadow: 0 4rpx 16rpx rgba(99, 102, 241, 0.3);
	transition: all 0.3s ease;
}

.empty-btn:active {
	transform: translateY(2rpx);
	box-shadow: 0 2rpx 8rpx rgba(99, 102, 241, 0.4);
}
</style>