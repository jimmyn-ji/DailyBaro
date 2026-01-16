<template>
	<view class="emotion-container">
		<view class="nav-bar">
			<view class="back-btn" @tap="goBack">
				<text class="back-icon">←</text>
				<text class="back-text">Back</text>
			</view>
			<text class="page-title">Emotion Analysis</text>
			<view class="nav-actions">
				<view class="cv-btn" @tap="goToCvAnalysis">
					<text class="cv-icon">📷</text>
				</view>
				<view class="audio-btn" @tap="goToAudioAnalysis">
					<text class="audio-icon">🎵</text>
				</view>
				<view class="visualization-btn" @tap="goToVisualization">
					<text class="visualization-icon">📊</text>
				</view>
				<view class="visualization-btn" @tap="goToRecommendations">
					<text class="visualization-icon">🧭</text>
				</view>
			</view>
		</view>
		
		<view class="content">
			<!-- 情绪分析表单 -->
			<view class="analysis-form">
				<text class="form-title">Emotion Analysis</text>
				
				<!-- NLP服务状态 -->
				<view class="nlp-status" v-if="nlpServiceStatus">
					<view class="status-indicator" :class="nlpServiceStatus.ready ? 'ready' : 'error'">
						<text class="status-icon"></text>
						<text class="status-text">{{ nlpServiceStatus.ready ? 'NLP服务就绪' : 'NLP服务未连接' }}</text>
					</view>
					<text class="status-desc">{{ nlpServiceStatus.message }}</text>
				</view>
				
				<view class="input-group">
					<text class="input-label">Description</text>
					<textarea 
						class="description-input" 
						placeholder="请描述你的情绪或感受..."
						v-model="emotionDescription"
						maxlength="500"
						auto-height
					></textarea>
					<text class="char-count">{{ emotionDescription.length }}/500</text>
				</view>
				
				<!-- 智能识别按钮 -->
				<view class="ai-analysis-section" v-if="nlpServiceStatus && nlpServiceStatus.ready">
					<button class="ai-analyze-btn" @tap="aiAnalyzeEmotion" :disabled="!emotionDescription.trim() || aiAnalyzing">
						<text class="ai-icon">🤖</text>
						<text class="ai-text">{{ aiAnalyzing ? 'AI分析中...' : 'AI智能识别情绪' }}</text>
					</button>
					<text class="ai-desc">基于BERT模型的智能情绪识别</text>
				</view>
				
				<!-- AI识别结果 -->
				<view class="ai-result" v-if="aiAnalysisResult">
					<view class="result-header">
						<text class="result-title">AI识别结果</text>
						<text class="confidence">置信度: {{ (aiAnalysisResult.confidence * 100).toFixed(1) }}%</text>
					</view>
					
					<view class="result-content">
						<view class="emotion-display">
							<text class="emotion-label">识别情绪:</text>
							<text class="emotion-value" :class="aiAnalysisResult.polarity">{{ aiAnalysisResult.emotion }}</text>
						</view>
						
						<view class="intensity-display">
							<text class="intensity-label">情绪强度:</text>
							<slider 
								:value="aiAnalysisResult.intensity" 
								min="1" 
								max="10" 
								@change="onAiIntensityChange"
								activeColor="#4a90e2"
								backgroundColor="#e5e7eb"
							/>
							<text class="intensity-value">{{ aiAnalysisResult.intensity }}/10</text>
						</view>
						
						<view class="polarity-display">
							<text class="polarity-label">情绪极性:</text>
							<text class="polarity-value" :class="aiAnalysisResult.polarity">
								{{ aiAnalysisResult.polarity === 'positive' ? '正面' : '负面' }}
							</text>
						</view>
						
						<!-- 所有情绪概率 -->
						<view class="all-emotions">
							<text class="emotions-title">情绪分布:</text>
							<view class="emotion-bars">
								<view 
									v-for="(score, emotion) in aiAnalysisResult.all_emotions" 
									:key="emotion"
									class="emotion-bar-item"
								>
									<text class="emotion-name">{{ emotion }}</text>
									<view class="emotion-bar">
										<view 
											class="emotion-fill" 
											:style="{ width: (score * 100) + '%' }"
											:class="getEmotionClass(emotion)"
										></view>
									</view>
									<text class="emotion-score">{{ (score * 100).toFixed(1) }}%</text>
								</view>
							</view>
						</view>
					</view>
				</view>
				
				<!-- 手动设置情绪 -->
				<view class="manual-emotion-section" v-if="!aiAnalysisResult">
					<view class="input-group">
						<text class="input-label">Emotion Type</text>
						<picker 
							mode="selector" 
							:range="emotionOptions" 
							:value="emotionIndex"
							@change="onEmotionChange"
						>
							<view class="picker-field">
								<text class="picker-text">{{ selectedEmotion }}</text>
								<text class="picker-arrow">›</text>
							</view>
						</picker>
					</view>
					
					<view class="input-group">
						<text class="input-label">Intensity</text>
						<slider 
							:value="emotionIntensity" 
							min="1" 
							max="10" 
							@change="onIntensityChange"
							activeColor="#4a90e2"
							backgroundColor="#e5e7eb"
						/>
						<text class="intensity-value">{{ emotionIntensity }}/10</text>
					</view>
				</view>
				
				<button class="analyze-btn" @tap="analyzeEmotion" :disabled="!canAnalyze">
					{{ aiAnalysisResult ? '保存AI分析结果' : '手动分析情绪' }}
				</button>
			</view>
			
			<!-- 分析历史 -->
			<view class="history-section">
				<text class="section-title">Analysis History</text>
				
				<view v-if="analysisHistory.length > 0" class="history-list">
					<view 
						v-for="item in analysisHistory" 
						:key="item.id"
						class="history-item"
						@tap="viewAnalysis(item.id)"
					>
						<view class="history-header">
							<text class="emotion-badge" :class="getEmotionClass(item.emotion)">{{ item.emotion }}</text>
							<text class="history-time">{{ formatTime(item.createTime) }}</text>
						</view>
						
						<view class="history-content">
							<text class="history-description">{{ item.description }}</text>
						</view>
						
						<view class="history-footer">
							<text class="intensity-text">Intensity: {{ item.intensity }}/10</text>
							<view class="analysis-status">
								<text class="status-icon">{{ item.aiAnalyzed ? '🤖' : '✓' }}</text>
								<text class="status-text">{{ item.aiAnalyzed ? 'AI分析' : '手动分析' }}</text>
							</view>
						</view>
					</view>
				</view>
				
				<view v-else class="empty-history">
					<text class="empty-icon">📊</text>
					<text class="empty-text">No analysis records</text>
					<text class="empty-desc">Start your first emotion analysis</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { emotionApi, nlpEmotionApi } from '../../utils/api.js'

export default {
	data() {
		return {
			emotionOptions: ['开心', '兴奋', '满足', '平静', '放松', '焦虑', '紧张', '愤怒', '悲伤', '沮丧', '惊讶', '困惑', '期待', '信任', '其他'],
			emotionIndex: 0,
			emotionIntensity: 5,
			emotionDescription: '',
			analysisHistory: [],
			loading: false,
			// NLP相关
			nlpServiceStatus: null,
			aiAnalyzing: false,
			aiAnalysisResult: null
		}
	},
	
	computed: {
		selectedEmotion() {
			return this.emotionOptions[this.emotionIndex] || '请选择情绪'
		},
		
		canAnalyze() {
			return this.emotionDescription.trim() && (this.aiAnalysisResult || !this.aiAnalysisResult)
		}
	},
	
	onLoad() {
		this.checkNlpService()
		this.loadAnalysisHistory()
	},
	
	methods: {
		goBack() {
			uni.navigateBack()
		},
		
		goToVisualization() {
			uni.showToast({
				title: 'Go to visualization',
				icon: 'success'
			})
		},
		
		goToCvAnalysis() {
			uni.navigateTo({
				url: '/pages/emotion/cv-analysis'
			})
		},
		
		goToAudioAnalysis() {
			uni.navigateTo({
				url: '/pages/emotion/audio-analysis'
			})
		},

		goToRecommendations() {
			uni.navigateTo({
				url: '/pages/recommendations/recommendations'
			})
		},
		
		onEmotionChange(e) {
			this.emotionIndex = e.detail.value
		},
		
		onIntensityChange(e) {
			this.emotionIntensity = e.detail.value
		},
		
		onAiIntensityChange(e) {
			if (this.aiAnalysisResult) {
				this.aiAnalysisResult.intensity = e.detail.value
			}
		},
		
		// 检查NLP服务状态
		async checkNlpService() {
			try {
				const response = await nlpEmotionApi.healthCheck()
				console.log('NLP服务健康检查响应:', response)
				
				// healthCheck 现在返回 { available, status } 对象
				if (response && response.available) {
					this.nlpServiceStatus = {
						ready: true,
						message: 'BERT模型已加载，可以开始智能分析'
					}
				} else {
					console.warn('NLP服务不可用，状态码:', response?.status)
					this.nlpServiceStatus = {
						ready: false,
						message: `NLP服务不可用 (状态码: ${response?.status || '未知'})，将使用后端分析服务`
					}
				}
			} catch (error) {
				console.error('NLP服务检查失败:', error)
				// 不阻塞主功能，只是提示
				this.nlpServiceStatus = {
					ready: false,
					message: 'NLP服务检查失败，将使用后端分析服务'
				}
			}
		},
		
		// AI智能情绪分析
		async aiAnalyzeEmotion() {
			if (!this.emotionDescription.trim()) {
				uni.showToast({
					title: '请先输入文本描述',
					icon: 'none'
				})
				return
			}
			
			this.aiAnalyzing = true
			
			try {
				const response = await nlpEmotionApi.analyzeEmotion(this.emotionDescription)
				
				if (response && response.code === 200) {
					this.aiAnalysisResult = response.data
					
					uni.showToast({
						title: 'AI分析完成',
						icon: 'success'
					})
				} else {
					uni.showToast({
						title: response?.message || 'AI分析失败',
						icon: 'none'
					})
				}
			} catch (error) {
				console.error('AI情绪分析失败:', error)
				uni.showToast({
					title: 'AI分析失败，请检查服务状态',
					icon: 'none'
				})
			} finally {
				this.aiAnalyzing = false
			}
		},
		
		// 获取情绪样式类
		getEmotionClass(emotion) {
			const positiveEmotions = ['开心', '兴奋', '满足', '平静', '放松', '期待', '信任']
			const negativeEmotions = ['焦虑', '紧张', '愤怒', '悲伤', '沮丧', '困惑']
			
			if (positiveEmotions.includes(emotion)) return 'positive'
			if (negativeEmotions.includes(emotion)) return 'negative'
			return 'neutral'
		},
		
		async analyzeEmotion() {
			if (!this.emotionDescription.trim()) {
				uni.showToast({
					title: '请先输入情绪描述',
					icon: 'none'
				})
				return
			}
			
			try {
				const userInfo = uni.getStorageSync('userInfo')
				if (!userInfo || !userInfo.uid) {
					uni.showToast({
						title: '请先登录',
						icon: 'none'
					})
					return
				}
				
				let analysisData = {}
				
				if (this.aiAnalysisResult) {
					// 使用AI分析结果
					analysisData = {
						uid: userInfo.uid,
						emotion: this.aiAnalysisResult.emotion,
						intensity: this.aiAnalysisResult.intensity,
						description: this.emotionDescription,
						aiAnalyzed: true,
						confidence: this.aiAnalysisResult.confidence,
						polarity: this.aiAnalysisResult.polarity,
						allEmotions: this.aiAnalysisResult.all_emotions
					}
				} else {
					// 使用手动设置
					analysisData = {
						uid: userInfo.uid,
						emotion: this.emotionOptions[this.emotionIndex],
						intensity: this.emotionIntensity,
						description: this.emotionDescription,
						aiAnalyzed: false
					}
				}
				
				const response = await emotionApi.analyzeEmotion(analysisData)
				
				if (response && response.code === 200) {
					uni.showToast({
						title: '分析完成',
						icon: 'success'
					})
					
					// 清空表单
					this.emotionDescription = ''
					this.emotionIntensity = 5
					this.aiAnalysisResult = null
					
					// 重新加载历史记录
					this.loadAnalysisHistory()
				} else {
					uni.showToast({
						title: response?.message || '分析失败',
						icon: 'none'
					})
				}
			} catch (error) {
				console.error('情绪分析失败:', error)
				uni.showToast({
					title: '分析失败，请重试',
					icon: 'none'
				})
			}
		},
		
		async loadAnalysisHistory() {
			try {
				const userInfo = uni.getStorageSync('userInfo')
				if (!userInfo || !(userInfo.uid || userInfo.id || userInfo.userId)) {
					this.analysisHistory = []
					return
				}
				const uid = userInfo.uid || userInfo.id || userInfo.userId
				const res = await emotionApi.getAnalysisHistory(uid)
				if (res && res.code === 200) {
					this.analysisHistory = res.data || []
				}
			} catch (e) {
				console.error('加载情绪分析历史失败:', e)
			}
		},
		
		viewAnalysis(id) {
			uni.showToast({
				title: `View analysis ${id}`,
				icon: 'success'
			})
		},
		
		formatTime(dateString) {
			const date = new Date(dateString)
			const month = String(date.getMonth() + 1).padStart(2, '0')
			const day = String(date.getDate()).padStart(2, '0')
			return `${month}-${day}`
		}
	}
}
</script>

<style scoped>
.emotion-container {
	min-height: 100vh;
	background: transparent;
}

.nav-bar {
	background: #ffffff;
	padding: 30rpx 40rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	border-bottom: 1rpx solid #e2e8f0;
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	z-index: 100;
	box-shadow: 0 2rpx 20rpx rgba(0, 0, 0, 0.05);
}

.back-btn {
	display: flex;
	align-items: center;
	gap: 8rpx;
	padding: 12rpx 20rpx;
	border-radius: 20rpx;
	background: rgba(99, 102, 241, 0.1);
	border: 1rpx solid rgba(99, 102, 241, 0.2);
	transition: all 0.3s ease;
}

.back-btn:active {
	background: rgba(99, 102, 241, 0.2);
	transform: scale(0.95);
}

.back-icon {
	font-size: 28rpx;
	color: #4a90e2;
	font-weight: 600;
}

.back-text {
	font-size: 26rpx;
	color: #4a90e2;
	font-weight: 500;
}

.page-title {
	font-size: 32rpx;
	font-weight: 700;
	color: #1f2937;
}

.nav-actions {
	display: flex;
	align-items: center;
	gap: 16rpx;
}

.cv-btn {
	width: 60rpx;
	height: 60rpx;
	background: linear-gradient(135deg, #f59e0b, #d97706);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 4rpx 16rpx rgba(245, 158, 11, 0.3);
	transition: all 0.3s ease;
}

.cv-btn:active {
	transform: scale(0.9);
	box-shadow: 0 2rpx 8rpx rgba(245, 158, 11, 0.4);
}

.cv-icon {
	font-size: 28rpx;
	color: #ffffff;
}

.audio-btn {
	width: 60rpx;
	height: 60rpx;
	background: linear-gradient(135deg, #4a90e2, #6aa8ff);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 4rpx 16rpx rgba(139, 92, 246, 0.3);
	transition: all 0.3s ease;
}

.audio-btn:active {
	transform: scale(0.9);
	box-shadow: 0 2rpx 8rpx rgba(139, 92, 246, 0.4);
}

.audio-icon {
	font-size: 28rpx;
	color: #ffffff;
}

.visualization-btn {
	width: 60rpx;
	height: 60rpx;
	background: linear-gradient(135deg, #10b981, #059669);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 4rpx 16rpx rgba(16, 185, 129, 0.3);
	transition: all 0.3s ease;
}

.visualization-btn:active {
	transform: scale(0.9);
	box-shadow: 0 2rpx 8rpx rgba(16, 185, 129, 0.4);
}

.visualization-icon {
	font-size: 28rpx;
	color: #ffffff;
}

.content {
	padding-top: 120rpx;
	padding: 30rpx;
}

.analysis-form {
	background: #ffffff;
	border-radius: 20rpx;
	padding: 40rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.form-title {
	font-size: 32rpx;
	font-weight: 700;
	color: #1f2937;
	margin-bottom: 30rpx;
	text-align: center;
}

/* NLP服务状态样式 */
.nlp-status {
	background: #f8fafc;
	border-radius: 12rpx;
	padding: 20rpx;
	margin-bottom: 30rpx;
	border: 1rpx solid #e2e8f0;
}

.status-indicator {
	display: flex;
	align-items: center;
	gap: 12rpx;
	margin-bottom: 8rpx;
}

.status-indicator.ready {
	color: #059669;
}

.status-indicator.error {
	color: #dc2626;
}

.status-icon {
	font-size: 24rpx;
}

.status-text {
	font-size: 26rpx;
	font-weight: 600;
}

.status-desc {
	font-size: 22rpx;
	color: #6b7280;
}

.input-group {
	margin-bottom: 30rpx;
}

.input-label {
	font-size: 26rpx;
	font-weight: 600;
	color: #374151;
	margin-bottom: 15rpx;
	display: block;
}

.description-input {
	width: 100%;
	min-height: 120rpx;
	background: #f9fafb;
	border: 1rpx solid #e5e7eb;
	border-radius: 12rpx;
	padding: 20rpx;
	font-size: 26rpx;
	color: #1f2937;
	line-height: 1.6;
}

.char-count {
	text-align: right;
	font-size: 22rpx;
	color: #9ca3af;
	margin-top: 10rpx;
	display: block;
}

/* AI分析区域样式 */
.ai-analysis-section {
	margin-bottom: 30rpx;
	text-align: center;
}

.ai-analyze-btn {
	width: 100%;
	background: linear-gradient(135deg, #4a90e2, #6aa8ff);
	color: #ffffff;
	border: none;
	border-radius: 16rpx;
	padding: 25rpx;
	font-size: 28rpx;
	font-weight: 600;
	box-shadow: 0 6rpx 24rpx rgba(74, 144, 226, 0.3);
	transition: all 0.3s ease;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 12rpx;
}

.ai-analyze-btn:active {
	transform: translateY(2rpx);
	box-shadow: 0 3rpx 12rpx rgba(74, 144, 226, 0.4);
}

.ai-analyze-btn:disabled {
	background: #9ca3af;
	box-shadow: none;
}

.ai-icon {
	font-size: 28rpx;
}

.ai-text {
	font-size: 28rpx;
}

.ai-desc {
	font-size: 22rpx;
	color: #6b7280;
	margin-top: 12rpx;
	display: block;
}

/* AI结果样式 */
.ai-result {
	background: #f0f9ff;
	border: 1rpx solid #0ea5e9;
	border-radius: 16rpx;
	padding: 25rpx;
	margin-bottom: 30rpx;
}

.result-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 20rpx;
}

.result-title {
	font-size: 28rpx;
	font-weight: 700;
	color: #0c4a6e;
}

.confidence {
	font-size: 22rpx;
	color: #0369a1;
	background: rgba(14, 165, 233, 0.1);
	padding: 6rpx 12rpx;
	border-radius: 12rpx;
}

.result-content {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.emotion-display, .intensity-display, .polarity-display {
	display: flex;
	align-items: center;
	gap: 20rpx;
}

.emotion-label, .intensity-label, .polarity-label {
	font-size: 24rpx;
	color: #374151;
	min-width: 120rpx;
}

.emotion-value, .polarity-value {
	font-size: 26rpx;
	font-weight: 600;
	padding: 8rpx 16rpx;
	border-radius: 12rpx;
}

.emotion-value.positive, .polarity-value.positive {
	background: #dcfce7;
	color: #166534;
}

.emotion-value.negative, .polarity-value.negative {
	background: #fef2f2;
	color: #dc2626;
}

.emotion-value.neutral, .polarity-value.neutral {
	background: #f3f4f6;
	color: #374151;
}

.intensity-display {
	flex-direction: column;
	align-items: flex-start;
	gap: 15rpx;
}

.intensity-display .intensity-label {
	min-width: auto;
}

.intensity-value {
	font-size: 22rpx;
	color: #6b7280;
	text-align: center;
	display: block;
	width: 100%;
}

/* 所有情绪概率样式 */
.all-emotions {
	margin-top: 20rpx;
}

.emotions-title {
	font-size: 24rpx;
	color: #374151;
	margin-bottom: 15rpx;
	display: block;
}

.emotion-bars {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
}

.emotion-bar-item {
	display: flex;
	align-items: center;
	gap: 15rpx;
}

.emotion-name {
	font-size: 22rpx;
	color: #374151;
	min-width: 80rpx;
}

.emotion-bar {
	flex: 1;
	height: 20rpx;
	background: #e5e7eb;
	border-radius: 10rpx;
	overflow: hidden;
}

.emotion-fill {
	height: 100%;
	border-radius: 10rpx;
	transition: width 0.3s ease;
}

.emotion-fill.positive {
	background: linear-gradient(90deg, #10b981, #059669);
}

.emotion-fill.negative {
	background: linear-gradient(90deg, #ef4444, #dc2626);
}

.emotion-fill.neutral {
	background: linear-gradient(90deg, #6b7280, #4b5563);
}

.emotion-score {
	font-size: 20rpx;
	color: #6b7280;
	min-width: 60rpx;
	text-align: right;
}

/* 手动情绪设置区域 */
.manual-emotion-section {
	border-top: 1rpx solid #e5e7eb;
	padding-top: 30rpx;
	margin-top: 30rpx;
}

.picker-field {
	display: flex;
	align-items: center;
	justify-content: space-between;
	background: #f9fafb;
	border: 1rpx solid #e5e7eb;
	border-radius: 12rpx;
	padding: 20rpx;
}

.picker-text {
	font-size: 26rpx;
	color: #1f2937;
}

.picker-arrow {
	font-size: 24rpx;
	color: #9ca3af;
}

.analyze-btn {
	width: 100%;
	background: linear-gradient(135deg, #4a90e2, #6aa8ff);
	color: #ffffff;
	border: none;
	border-radius: 16rpx;
	padding: 25rpx;
	font-size: 28rpx;
	font-weight: 600;
	box-shadow: 0 6rpx 24rpx rgba(99, 102, 241, 0.3);
	transition: all 0.3s ease;
}

.analyze-btn:active {
	transform: translateY(2rpx);
	box-shadow: 0 3rpx 12rpx rgba(99, 102, 241, 0.4);
}

.analyze-btn:disabled {
	background: #9ca3af;
	box-shadow: none;
}

.history-section {
	background: #ffffff;
	border-radius: 20rpx;
	padding: 40rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.section-title {
	font-size: 30rpx;
	font-weight: 700;
	color: #1f2937;
	margin-bottom: 30rpx;
}

.history-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.history-item {
	background: #f9fafb;
	border-radius: 16rpx;
	padding: 25rpx;
	border: 1rpx solid #e5e7eb;
	transition: all 0.3s ease;
}

.history-item:active {
	transform: translateY(2rpx);
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.history-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 15rpx;
}

.emotion-badge {
	padding: 8rpx 16rpx;
	border-radius: 20rpx;
	font-size: 22rpx;
	font-weight: 600;
	color: #ffffff;
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

.history-time {
	font-size: 22rpx;
	color: #9ca3af;
}

.history-content {
	margin-bottom: 15rpx;
}

.history-description {
	font-size: 26rpx;
	color: #374151;
	line-height: 1.5;
}

.history-footer {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.intensity-text {
	font-size: 22rpx;
	color: #6b7280;
}

.analysis-status {
	display: flex;
	align-items: center;
	gap: 8rpx;
}

.status-icon {
	font-size: 20rpx;
}

.status-text {
	font-size: 22rpx;
	color: #10b981;
	font-weight: 500;
}

.empty-history {
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

.empty-desc {
	font-size: 22rpx;
	color: #9ca3af;
	line-height: 1.5;
}
</style>
