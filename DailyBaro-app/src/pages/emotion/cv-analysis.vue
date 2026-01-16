<template>
	<view class="cv-analysis-container">
		<!-- 自定义导航栏 -->
		<view class="custom-navbar">
			<view class="navbar-left" @click="goBack">
				<text class="back-icon">←</text>
			</view>
			<view class="navbar-title">CV情绪分析</view>
			<view class="navbar-right"></view>
		</view>

		<!-- 内容区域 -->
		<view class="content">
			<!-- 图片上传区域 -->
			<view class="upload-section">
				<view class="upload-area" @click="chooseImage" v-if="!imageUrl">
					<text class="upload-icon">📷</text>
					<text class="upload-text">点击上传图片</text>
					<text class="upload-hint">支持自拍、表情图片</text>
				</view>
				
				<view class="image-preview" v-if="imageUrl">
					<image :src="imageUrl" class="preview-image" mode="aspectFit"></image>
					<view class="image-actions">
						<button class="action-btn" @click="chooseImage">重新选择</button>
						<button class="action-btn primary" @click="analyzeImage">开始分析</button>
					</view>
				</view>
			</view>

			<!-- 分析结果 -->
			<view class="result-section" v-if="analysisResult">
				<view class="result-header">
					<text class="result-title">分析结果</text>
				</view>
				
				<view class="result-content">
					<view class="emotion-item">
						<text class="emotion-label">主要情绪：</text>
						<text class="emotion-value">{{ analysisResult.primaryEmotion }}</text>
					</view>
					
					<view class="emotion-item">
						<text class="emotion-label">置信度：</text>
						<text class="emotion-value">{{ analysisResult.confidence }}%</text>
					</view>
					
					<view class="emotion-item">
						<text class="emotion-label">情绪强度：</text>
						<text class="emotion-value">{{ analysisResult.intensity }}/10</text>
					</view>
					
					<view class="emotion-details" v-if="analysisResult.details">
						<text class="details-title">详细分析：</text>
						<view class="detail-item" v-for="(detail, index) in analysisResult.details" :key="index">
							<text class="detail-label">{{ detail.emotion }}：</text>
							<text class="detail-value">{{ detail.probability }}%</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 使用说明 -->
			<view class="instruction-section">
				<view class="instruction-header">
					<text class="instruction-title">使用说明</text>
				</view>
				<view class="instruction-content">
					<text class="instruction-text">• 上传清晰的人脸照片，确保光线充足</text>
					<text class="instruction-text">• 支持识别：开心、难过、愤怒、平静等情绪</text>
					<text class="instruction-text">• 分析结果仅供参考，请理性对待</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { config } from '@/utils/config.js'

export default {
	data() {
		return {
			imageUrl: '',
			analysisResult: null,
			isAnalyzing: false
		}
	},
	methods: {
		goBack() {
			uni.navigateBack()
		},
		
		// 获取基础URL
		getBaseUrl() {
			return config.BASE_URL
		},
		
		chooseImage() {
			uni.chooseImage({
				count: 1,
				sizeType: ['compressed'],
				sourceType: ['album', 'camera'],
				success: (res) => {
					this.imageUrl = res.tempFilePaths[0]
					this.analysisResult = null
				},
				fail: (err) => {
					console.error('选择图片失败:', err)
					uni.showToast({
						title: '选择图片失败',
						icon: 'none'
					})
				}
			})
		},
		async analyzeImage() {
			if (!this.imageUrl) {
				uni.showToast({
					title: '请先选择图片',
					icon: 'none'
				})
				return
			}
			
			this.isAnalyzing = true
			try {
				const baseUrl = this.getBaseUrl()
				const response = await uni.uploadFile({
					url: baseUrl + '/api/nlp/emotion/analyze/image',
					filePath: this.imageUrl,
					name: 'image',
					success: (res) => {
						try {
							const data = JSON.parse(res.data)
							if (data && data.code === 200) {
								this.analysisResult = data.data
								uni.showToast({
									title: '分析完成',
									icon: 'success'
								})
							} else {
								uni.showToast({
									title: data?.message || '分析失败',
									icon: 'none'
								})
							}
						} catch (e) {
							console.error('响应解析失败:', e)
							uni.showToast({
								title: '响应解析失败',
								icon: 'none'
							})
						}
					},
					fail: (err) => {
						console.error('图片上传失败:', err)
						uni.showToast({
							title: '网络错误，请重试',
							icon: 'none'
						})
					}
				})
			} catch (error) {
				console.error('图片分析失败:', error)
				uni.showToast({
					title: '分析失败，请重试',
					icon: 'none'
				})
			} finally {
				this.isAnalyzing = false
			}
		}
	}
}
</script>

<style scoped>
.cv-analysis-container {
	min-height: 100vh;
	background: #f7f7fb;
}

.custom-navbar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 20rpx 30rpx;
	background: #ffffff;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.navbar-left {
	display: flex;
	align-items: center;
}

.back-icon {
	font-size: 36rpx;
	color: #333;
	margin-right: 10rpx;
}

.navbar-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
}

.content {
	padding: 30rpx;
}

.upload-section {
	margin-bottom: 40rpx;
}

.upload-area {
	background: #ffffff;
	border: 2rpx dashed #ddd;
	border-radius: 20rpx;
	padding: 60rpx 40rpx;
	text-align: center;
	transition: all 0.3s ease;
}

.upload-area:active {
	border-color: #4a90e2;
	background: #f8f9ff;
}

.upload-icon {
	font-size: 80rpx;
	margin-bottom: 20rpx;
	display: block;
}

.upload-text {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 10rpx;
	display: block;
}

.upload-hint {
	font-size: 26rpx;
	color: #666;
}

.image-preview {
	background: #ffffff;
	border-radius: 20rpx;
	padding: 30rpx;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.preview-image {
	width: 100%;
	height: 400rpx;
	border-radius: 15rpx;
	margin-bottom: 30rpx;
}

.image-actions {
	display: flex;
	gap: 20rpx;
}

.action-btn {
	flex: 1;
	padding: 20rpx;
	border-radius: 15rpx;
	font-size: 28rpx;
	font-weight: 600;
	border: none;
	transition: all 0.3s ease;
}

.action-btn.primary {
	background: #4a90e2;
	color: #ffffff;
}

.action-btn:not(.primary) {
	background: #f1f5f9;
	color: #64748b;
}

.action-btn:active {
	transform: translateY(2rpx);
}

.result-section {
	background: #ffffff;
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 40rpx;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.result-header {
	margin-bottom: 20rpx;
}

.result-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
}

.emotion-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 15rpx 0;
	border-bottom: 1rpx solid #f1f5f9;
}

.emotion-item:last-child {
	border-bottom: none;
}

.emotion-label {
	font-size: 28rpx;
	color: #64748b;
}

.emotion-value {
	font-size: 28rpx;
	font-weight: 600;
	color: #333;
}

.emotion-details {
	margin-top: 20rpx;
	padding-top: 20rpx;
	border-top: 1rpx solid #f1f5f9;
}

.details-title {
	font-size: 28rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 15rpx;
	display: block;
}

.detail-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 10rpx 0;
}

.detail-label {
	font-size: 26rpx;
	color: #64748b;
}

.detail-value {
	font-size: 26rpx;
	font-weight: 600;
	color: #333;
}

.instruction-section {
	background: #ffffff;
	border-radius: 20rpx;
	padding: 30rpx;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.instruction-header {
	margin-bottom: 20rpx;
}

.instruction-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
}

.instruction-content {
	display: flex;
	flex-direction: column;
	gap: 15rpx;
}

.instruction-text {
	font-size: 26rpx;
	color: #64748b;
	line-height: 1.6;
}
</style>
