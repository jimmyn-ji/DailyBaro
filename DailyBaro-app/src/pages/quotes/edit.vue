<template>
	<view class="edit-container">
		<!-- 自定义导航栏 -->
		<view class="custom-navbar">
			<view class="navbar-left" @click="goBack">
				<text class="back-icon">←</text>
			</view>
			<view class="navbar-title">编辑日签</view>
			<view class="navbar-right" @click="saveQuote">
				<text class="save-text">保存</text>
			</view>
		</view>

		<!-- 内容区域 -->
		<view class="content">
			<view class="form-group">
				<text class="label">日签内容</text>
				<textarea 
					class="quote-content" 
					v-model="quoteContent" 
					placeholder="写下你的日签内容..."
					maxlength="200"
					show-count
				></textarea>
			</view>

			<view class="form-group">
				<text class="label">标签</text>
				<input 
					class="quote-tags" 
					v-model="quoteTags" 
					placeholder="添加标签，用逗号分隔"
				/>
			</view>

			<view class="form-group">
				<text class="label">心情</text>
				<view class="mood-selector">
					<view 
						v-for="mood in moods" 
						:key="mood.value"
						class="mood-item"
						:class="{ active: selectedMood === mood.value }"
						@click="selectMood(mood.value)"
					>
						<text class="mood-emoji">{{ mood.emoji }}</text>
						<text class="mood-text">{{ mood.label }}</text>
					</view>
				</view>
			</view>

			<view class="form-group">
				<text class="label">是否公开</text>
				<switch 
					:checked="isPublic" 
					@change="togglePublic"
					color="#4a90e2"
				/>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			quoteId: '',
			quoteContent: '',
			quoteTags: '',
			selectedMood: 'happy',
			isPublic: true,
			moods: [
				{ value: 'happy', label: '开心', emoji: '😊' },
				{ value: 'sad', label: '难过', emoji: '😢' },
				{ value: 'angry', label: '愤怒', emoji: '😠' },
				{ value: 'calm', label: '平静', emoji: '😌' },
				{ value: 'excited', label: '兴奋', emoji: '🤩' },
				{ value: 'tired', label: '疲惫', emoji: '😴' }
			]
		}
	},
	onLoad(options) {
		if (options.id && options.id !== 'undefined') {
			this.quoteId = options.id
			this.loadQuote()
		}
	},
	methods: {
		goBack() {
			uni.navigateBack()
		},
		selectMood(mood) {
			this.selectedMood = mood
		},
		togglePublic(e) {
			this.isPublic = e.detail.value
		},
		async loadQuote() {
			try {
				// 这里应该调用API加载日签详情
				uni.showLoading({ title: '加载中...' })
				
				// 模拟加载数据
				setTimeout(() => {
					this.quoteContent = '这是一个示例日签内容'
					this.quoteTags = '生活,感悟'
					this.selectedMood = 'happy'
					this.isPublic = true
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
		async saveQuote() {
			if (!this.quoteContent.trim()) {
				uni.showToast({
					title: '请输入日签内容',
					icon: 'none'
				})
				return
			}

			try {
				uni.showLoading({ title: '保存中...' })
				
				// 这里应该调用API保存日签
				const quoteData = {
					id: this.quoteId,
					content: this.quoteContent,
					tags: this.quoteTags,
					mood: this.selectedMood,
					isPublic: this.isPublic
				}

				// 模拟保存
				setTimeout(() => {
					uni.hideLoading()
					uni.showToast({
						title: '保存成功',
						icon: 'success'
					})
					
					setTimeout(() => {
						uni.navigateBack()
					}, 1500)
				}, 1000)
			} catch (error) {
				uni.hideLoading()
				uni.showToast({
					title: '保存失败',
					icon: 'none'
				})
			}
		}
	}
}
</script>

<style scoped>
.edit-container {
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

.save-text {
	font-size: 28rpx;
	color: #4a90e2;
	font-weight: 500;
}

.content {
	padding: 30rpx;
	padding-bottom: 100rpx;
}

.form-group {
	margin-bottom: 40rpx;
}

.label {
	display: block;
	font-size: 28rpx;
	color: #333;
	margin-bottom: 20rpx;
	font-weight: 500;
}

.quote-content {
	width: 100%;
	min-height: 200rpx;
	padding: 20rpx;
	background-color: #ffffff;
	border: 1rpx solid #e9ecef;
	border-radius: 12rpx;
	font-size: 28rpx;
	line-height: 1.6;
	box-sizing: border-box;
}

.quote-tags {
	width: 100%;
	padding: 20rpx;
	background-color: #ffffff;
	border: 1rpx solid #e9ecef;
	border-radius: 12rpx;
	font-size: 28rpx;
	box-sizing: border-box;
}

.mood-selector {
	display: flex;
	flex-wrap: wrap;
	gap: 20rpx;
}

.mood-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 20rpx;
	background-color: #ffffff;
	border: 2rpx solid #e9ecef;
	border-radius: 12rpx;
	min-width: 120rpx;
	transition: all 0.3s ease;
}

.mood-item.active {
	border-color: #4a90e2;
	background-color: #f0f2ff;
}

.mood-emoji {
	font-size: 40rpx;
	margin-bottom: 10rpx;
}

.mood-text {
	font-size: 24rpx;
	color: #666;
}

.mood-item.active .mood-text {
	color: #4a90e2;
	font-weight: 500;
}
</style>
