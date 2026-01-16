<template>
	<view class="ai-container">
		<!-- 顶部导航栏 -->
		<view class="nav-bar">
			<view class="nav-left" @tap="goBack">
				<text class="back-icon">←</text>
				<text class="back-text">返回</text>
			</view>
			<view class="nav-title">AI智能助手</view>
			<view class="nav-right"></view>
		</view>
		
		<view class="content">
			<!-- 顶部介绍 -->
			<view class="intro-section">
				<view class="intro-card">
					<view class="intro-icon">🤖</view>
					<text class="intro-title">AI智能助手</text>
					<text class="intro-desc">为你提供情绪管理建议和心理健康指导</text>
				</view>
			</view>
			
			<!-- 快速问题 -->
			<view class="quick-questions">
				<view class="section-title">常见问题</view>
				<view class="question-tags">
					<view 
						v-for="question in quickQuestions" 
						:key="question.id"
						class="question-tag"
						@tap="askQuestion(question.text)"
					>
						{{ question.text }}
					</view>
				</view>
			</view>
			
			<!-- 对话区域 -->
			<view class="chat-section">
				<view class="section-title">智能对话</view>
				<view class="chat-container">
					<scroll-view class="chat-messages" scroll-y scroll-into-view="msg-bottom">
						<view 
							v-for="(message, index) in chatMessages" 
							:key="index"
							class="message-item"
							:class="message.type"
						>
							<view class="message-avatar">
								<text v-if="message.type === 'user'">👤</text>
								<text v-else>🤖</text>
							</view>
							<view class="message-content">
								<text class="message-text">{{ message.content }}</text>
								<text class="message-time">{{ formatTime(message.time) }}</text>
							</view>
						</view>
						<view id="msg-bottom"></view>
					</scroll-view>
					
					<view class="chat-input">
						<input 
							class="input-field" 
							v-model="inputMessage" 
							placeholder="输入你的问题..."
							@confirm="sendMessage"
						/>
						<button class="send-btn" @tap="sendMessage" :disabled="!inputMessage.trim()">
							发送
						</button>
					</view>
				</view>
			</view>
			
			<!-- 日记分析 -->
			<view class="diary-analysis">
				<view class="section-title">日记智能分析</view>
				<view class="analysis-card">
					<text class="analysis-desc">基于你的日记内容，AI会为你提供个性化的情绪建议</text>
					<button class="analysis-btn" @tap="analyzeDiary">分析今日日记</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { aiApi } from '../../utils/api.js'

export default {
	data() {
		return {
			inputMessage: '',
			chatMessages: [],
			quickQuestions: []
		}
	},
	
	onLoad() {
		this.initChat()
		this.loadCommonQuestions()
	},
	
	methods: {
		// 返回上一页
		goBack() {
			uni.navigateBack({
				delta: 1
			})
		},
		
		async loadCommonQuestions() {
			try {
				const response = await aiApi.getCommonQuestions()
				if (response && response.code === 200) {
					this.quickQuestions = response.data || []
				}
			} catch (error) {
				console.error('加载常见问题失败:', error)
			}
		},
		initChat() {
			// 添加欢迎消息
			this.addMessage('assistant', '你好！我是你的AI情绪助手，有什么可以帮助你的吗？')
		},
		
		askQuestion(question) {
			this.inputMessage = question
			this.sendMessage()
		},
		
		async sendMessage() {
			if (!this.inputMessage.trim()) return
			
			const message = this.inputMessage.trim()
			this.addMessage('user', message)
			this.inputMessage = ''
			
			// 显示AI正在输入
			this.addMessage('assistant', '正在思考中...', true)
			
			try {
				const res = await aiApi.chat({
					message: message,
					timestamp: Date.now()
				})
				
				console.log('AI API 响应:', res)
				
				// 移除"正在输入"消息
				this.chatMessages.pop()
				
				if (res && res.code === 200) {
					// 尝试多种可能的响应格式
					const reply = res.data?.reply || res.data?.message || res.data?.content || res.data?.answer || res.message || '抱歉，我现在无法回答这个问题。'
					this.addMessage('assistant', reply)
				} else if (res && res.data) {
					// 如果后端直接返回数据对象
					const reply = res.data.reply || res.data.message || res.data.content || res.data.answer || '抱歉，我现在无法回答这个问题。'
					this.addMessage('assistant', reply)
				} else {
					console.error('AI API 返回格式异常:', res)
					this.addMessage('assistant', `抱歉，我遇到了一些问题。${res?.message || '请稍后再试'}`)
				}
			} catch (error) {
				console.error('AI API 调用失败:', error)
				this.chatMessages.pop()
				const errorMsg = error.message || error.errMsg || '网络连接出现问题，请检查网络后重试。'
				this.addMessage('assistant', `网络连接出现问题：${errorMsg}`)
			}
		},
		
		addMessage(type, content, isTyping = false) {
			this.chatMessages.push({
				type,
				content,
				time: new Date(),
				isTyping
			})
			
			// 滚动到底部
			setTimeout(() => {
				uni.pageScrollTo({
					scrollTop: 9999,
					duration: 300
				})
			}, 100)
		},
		
		async analyzeDiary() {
			try {
				uni.showLoading({ title: '分析中...' })
				
				const res = await aiApi.diaryAnalysis({
					action: 'analyze_today',
					timestamp: Date.now()
				})
				
				uni.hideLoading()
				
				if (res.code === 200) {
					this.addMessage('assistant', res.data.analysis || '基于你的日记内容，我为你提供了一些建议。')
				} else {
					uni.showToast({
						title: res.message || '分析失败',
						icon: 'none'
					})
				}
			} catch (error) {
				uni.hideLoading()
				uni.showToast({
					title: '分析失败，请稍后重试',
					icon: 'none'
				})
			}
		},
		
		formatTime(date) {
			const now = new Date()
			const diff = now - date
			
			if (diff < 60000) return '刚刚'
			if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
			if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
			
			return date.toLocaleDateString()
		}
	}
}
</script>

<style scoped>
.ai-container {
	min-height: 100vh;
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
}

/* 导航栏样式 */
.nav-bar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 20rpx 30rpx;
	background: rgba(255, 255, 255, 0.1);
	backdrop-filter: blur(10rpx);
	position: sticky;
	top: 0;
	z-index: 100;
}

.nav-left {
	display: flex;
	align-items: center;
	gap: 10rpx;
	cursor: pointer;
}

.back-icon {
	font-size: 32rpx;
	color: #ffffff;
	font-weight: bold;
}

.back-text {
	font-size: 28rpx;
	color: #ffffff;
}

.nav-title {
	font-size: 32rpx;
	color: #ffffff;
	font-weight: 600;
}

.nav-right {
	width: 80rpx;
}

.content {
	padding: 40rpx 30rpx;
}

/* 顶部介绍 */
.intro-section {
	margin-bottom: 30rpx;
}

.intro-card {
	background: rgba(255, 255, 255, 0.15);
	backdrop-filter: blur(20rpx);
	border-radius: 24rpx;
	padding: 40rpx;
	text-align: center;
	border: 1rpx solid rgba(255, 255, 255, 0.2);
	box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.intro-icon {
	font-size: 80rpx;
	margin-bottom: 20rpx;
	display: block;
	filter: drop-shadow(0 4rpx 8rpx rgba(0, 0, 0, 0.2));
}

.intro-title {
	font-size: 36rpx;
	font-weight: 700;
	color: #ffffff;
	margin-bottom: 15rpx;
	display: block;
	text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.3);
}

.intro-desc {
	font-size: 26rpx;
	color: rgba(255, 255, 255, 0.9);
	line-height: 1.5;
	display: block;
}

/* 快速问题 */
.quick-questions {
	margin-bottom: 30rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: 700;
	color: #ffffff;
	margin-bottom: 20rpx;
	text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.3);
}

.question-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 15rpx;
}

.question-tag {
	background: rgba(255, 255, 255, 0.2);
	border: 1rpx solid rgba(255, 255, 255, 0.3);
	border-radius: 20rpx;
	padding: 12rpx 20rpx;
	color: #ffffff;
	font-size: 24rpx;
	transition: all 0.3s ease;
}

.question-tag:active {
	background: rgba(255, 255, 255, 0.3);
	transform: scale(0.95);
}

/* 对话区域 */
.chat-section {
	margin-bottom: 30rpx;
}

.chat-container {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 20rpx;
	overflow: hidden;
	box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.chat-messages {
	height: 600rpx;
	padding: 20rpx;
}

.message-item {
	display: flex;
	margin-bottom: 20rpx;
	align-items: flex-start;
}

.message-item.user {
	flex-direction: row-reverse;
}

.message-avatar {
	width: 60rpx;
	height: 60rpx;
	border-radius: 50%;
	background: #f3f4f6;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 24rpx;
	margin: 0 15rpx;
}

.message-content {
	flex: 1;
	max-width: 70%;
}

.message-text {
	background: #f3f4f6;
	padding: 15rpx 20rpx;
	border-radius: 18rpx;
	font-size: 26rpx;
	color: #1f2937;
	line-height: 1.5;
	display: block;
}

.message-item.user .message-text {
	background: #4a90e2;
	color: #ffffff;
}

.message-time {
	font-size: 20rpx;
	color: #9ca3af;
	margin-top: 8rpx;
	display: block;
}

.chat-input {
	display: flex;
	padding: 20rpx;
	border-top: 1rpx solid #e5e7eb;
	gap: 15rpx;
}

.input-field {
	flex: 1;
	background: #f9fafb;
	border: 1rpx solid #e5e7eb;
	border-radius: 20rpx;
	padding: 15rpx 20rpx;
	font-size: 26rpx;
}

.send-btn {
	background: #4a90e2;
	color: #ffffff;
	border: none;
	border-radius: 20rpx;
	padding: 15rpx 25rpx;
	font-size: 26rpx;
	font-weight: 600;
}

.send-btn:disabled {
	background: #9ca3af;
}

/* 日记分析 */
.diary-analysis {
	margin-bottom: 30rpx;
}

.analysis-card {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 20rpx;
	padding: 30rpx;
	text-align: center;
	box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.analysis-desc {
	font-size: 26rpx;
	color: #4b5563;
	line-height: 1.6;
	margin-bottom: 20rpx;
	display: block;
}

.analysis-btn {
	background: linear-gradient(135deg, #10b981 0%, #059669 100%);
	color: #ffffff;
	border: none;
	border-radius: 16rpx;
	padding: 20rpx 40rpx;
	font-size: 28rpx;
	font-weight: 600;
	box-shadow: 0 4rpx 16rpx rgba(16, 185, 129, 0.3);
}
</style>
