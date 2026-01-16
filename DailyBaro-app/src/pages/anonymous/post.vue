<template>
	<view class="post-container">
		<view class="header">
			<view class="back-btn" @tap="goBack">取消</view>
			<text class="title">发布动态</text>
			<view class="publish-btn" @tap="publishPost">发布</view>
		</view>
		
		<view class="form-container">
			<view class="input-group">
				<textarea 
					class="content-input" 
					placeholder="说点什么..." 
					v-model="form.content"
					maxlength="500"
					auto-height
				></textarea>
				<text class="char-count">{{ form.content.length }}/500</text>
			</view>
			
			<view class="visibility-section">
				<view class="section-title">可见性设置</view>
				<view class="visibility-options">
					<view 
						class="visibility-item" 
						:class="{ active: form.visibility === 'public' }"
						@tap="form.visibility = 'public'"
					>
						<text class="visibility-icon">🌍</text>
						<view class="visibility-info">
							<text class="visibility-name">公开</text>
							<text class="visibility-desc">所有人都可以看到</text>
						</view>
						<text class="visibility-check">{{ form.visibility === 'public' ? '✓' : '' }}</text>
					</view>
					
					<view 
						class="visibility-item" 
						:class="{ active: form.visibility === 'private' }"
						@tap="form.visibility = 'private'"
					>
						<text class="visibility-icon">🔒</text>
						<view class="visibility-info">
							<text class="visibility-name">仅自己</text>
							<text class="visibility-desc">只有自己可以看到</text>
						</view>
						<text class="visibility-check">{{ form.visibility === 'private' ? '✓' : '' }}</text>
					</view>
				</view>
			</view>
			
			<view class="tips-section">
				<view class="tips-title">💡 小贴士</view>
				<view class="tips-content">
					<text class="tip-item">• 在这里可以自由表达，不用担心被认出</text>
					<text class="tip-item">• 公开的动态会显示在匿名星球中</text>
					<text class="tip-item">• 私密动态只有自己能看到</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { anonymousApi } from '@/utils/api.js'

export default {
	data() {
		return {
			form: {
				content: '',
				visibility: 'public'
			}
		}
	},
	
	methods: {
		goBack() {
			uni.navigateBack()
		},
		
		async publishPost() {
			if (!this.form.content.trim()) {
				uni.showToast({
					title: '请输入动态内容',
					icon: 'none'
				})
				return
			}
			
			if (this.form.content.length < 5) {
				uni.showToast({
					title: '内容至少5个字符',
					icon: 'none'
				})
				return
			}
			
			try {
				const res = await anonymousApi.createPost(this.form)
				
				if (res.code === 200) {
					uni.showToast({
						title: '发布成功',
						icon: 'success'
					})
					
					setTimeout(() => {
						uni.navigateBack()
					}, 1500)
				} else {
					uni.showToast({
						title: res.message || '发布失败',
						icon: 'none'
					})
				}
			} catch (error) {
				uni.showToast({
					title: error.message || '发布失败',
					icon: 'none'
				})
			}
		}
	}
}
</script>

<style scoped>
.post-container {
	min-height: 100vh;
	background: #f8fafc;
}

.header {
	background: #ffffff;
	padding: 30rpx 40rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	border-bottom: 1rpx solid #e2e8f0;
}

.back-btn {
	font-size: 28rpx;
	color: #64748b;
}

.title {
	font-size: 32rpx;
	font-weight: 600;
	color: #1f2937;
}

.publish-btn {
	font-size: 28rpx;
	color: #4a90e2;
	font-weight: 500;
}

.form-container {
	padding: 40rpx;
}

.input-group {
	margin-bottom: 40rpx;
}

.content-input {
	width: 100%;
	min-height: 200rpx;
	background: #ffffff;
	border: 2rpx solid #e2e8f0;
	border-radius: 16rpx;
	padding: 30rpx;
	font-size: 30rpx;
	color: #1f2937;
	line-height: 1.6;
}

.content-input:focus {
	border-color: #4a90e2;
}

.char-count {
	text-align: right;
	font-size: 24rpx;
	color: #94a3b8;
	margin-top: 15rpx;
	display: block;
}

.visibility-section {
	margin-bottom: 40rpx;
}

.section-title {
	font-size: 28rpx;
	font-weight: 600;
	color: #1f2937;
	margin-bottom: 20rpx;
}

.visibility-options {
	display: flex;
	flex-direction: column;
	gap: 15rpx;
}

.visibility-item {
	display: flex;
	align-items: center;
	padding: 25rpx;
	background: #ffffff;
	border: 2rpx solid #e2e8f0;
	border-radius: 16rpx;
	transition: all 0.3s ease;
}

.visibility-item.active {
	border-color: #4a90e2;
	background: #f0f9ff;
}

.visibility-icon {
	font-size: 32rpx;
	margin-right: 20rpx;
}

.visibility-info {
	flex: 1;
}

.visibility-name {
	font-size: 28rpx;
	font-weight: 600;
	color: #1f2937;
	margin-bottom: 5rpx;
	display: block;
}

.visibility-desc {
	font-size: 24rpx;
	color: #64748b;
}

.visibility-check {
	font-size: 32rpx;
	color: #4a90e2;
	font-weight: 600;
}

.tips-section {
	background: #f0f9ff;
	border-radius: 16rpx;
	padding: 30rpx;
	border-left: 4rpx solid #4a90e2;
}

.tips-title {
	font-size: 26rpx;
	font-weight: 600;
	color: #1e40af;
	margin-bottom: 15rpx;
}

.tips-content {
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.tip-item {
	font-size: 24rpx;
	color: #1e40af;
	line-height: 1.5;
}
</style>
