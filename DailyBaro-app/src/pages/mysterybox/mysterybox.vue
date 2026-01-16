<template>
	<view class="mysterybox-container">
		<view class="content">
			<!-- 顶部介绍区域 -->
			<view class="intro-section">
				<view class="intro-card">
					<view class="intro-icon">🎁</view>
					<text class="intro-title">情绪盲盒</text>
					<text class="intro-desc">每天抽取一个盲盒，发现生活中的小惊喜</text>
				</view>
			</view>
			
			<!-- 今日盲盒区域 -->
			<view class="today-section">
				<view class="section-header">
					<text class="section-title">今日盲盒</text>
					<text class="section-subtitle">点击抽取今日的惊喜</text>
				</view>
				
				<view v-if="todayBox.isOpened" class="box-card active">
					<view class="box-header">
						<view class="box-type-badge">{{ getTypeText(todayBox.type) }}</view>
						<text class="box-status" :class="todayBox.type">
							{{ getStatusText(todayBox.type) }}
						</text>
					</view>
					<text class="box-title">{{ todayBox.content }}</text>
					<view class="box-footer">
						<view class="box-meta">
							<text class="box-time">{{ formatDate(todayBox.date) }}</text>
							<text class="box-reward">+{{ todayBox.reward || 10 }} 能量</text>
						</view>
						<button 
							v-if="todayBox.type === 'task' && !todayBox.completed" 
							class="complete-btn" 
							@tap="completeTask(todayBox.id)"
						>
							完成任务
						</button>
					</view>
				</view>
				
				<view v-else class="box-card empty" @tap="drawBox">
					<view class="empty-box">
						<text class="empty-icon">🎁</text>
						<text class="empty-text">点击抽取盲盒</text>
						<text class="empty-desc">发现今日的惊喜</text>
					</view>
				</view>
			</view>
			
			<!-- 历史记录区域 -->
			<view class="history-section">
				<view class="section-header">
					<text class="section-title">抽取历史</text>
					<button class="refresh-btn" @tap="loadHistory">
						<text class="refresh-icon">🔄</text>
						<text>刷新</text>
					</button>
				</view>
				
				<view v-if="historyList.length === 0" class="empty-history">
					<text class="empty-history-icon">📝</text>
					<text class="empty-history-text">还没有抽取记录</text>
					<text class="empty-history-desc">抽取第一个盲盒开始记录吧</text>
				</view>
				
				<view v-else class="history-list">
					<view 
						v-for="record in historyList" 
						:key="record.id" 
						class="history-item"
						:class="record.type"
					>
						<view class="history-header">
							<view class="history-type">{{ getTypeText(record.type) }}</view>
							<text class="history-status" :class="record.type">
								{{ getStatusText(record.type) }}
							</text>
						</view>
						<text class="history-title">{{ record.content }}</text>
						<view class="history-footer">
							<view class="history-meta">
								<text class="history-time">{{ formatDate(record.date) }}</text>
								<text class="history-reward">+{{ record.reward || 10 }} 能量</text>
							</view>
							<view v-if="record.type === 'task' && !record.completed" class="history-actions">
								<button 
									class="complete-task-btn" 
									@tap="completeTask(record.id)"
								>
									完成任务
								</button>
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { mysteryBoxApi } from '../../utils/api.js'

export default {
	data() {
		return {
			todayBox: {
				id: 1,
				content: '今天的心情盲盒：保持微笑，世界会因你而美好',
				type: 'quote',
				isOpened: false
			},
			isShaking: false,
			userEnergy: 0,
			historyList: [],
			showHistoryModal: false
		}
	},
	
	onLoad() {
		this.loadUserEnergy()
		this.loadHistory()
	},
	
	methods: {
		async loadUserEnergy() {
			try {
				const userInfo = uni.getStorageSync('userInfo')
				if (!userInfo || !userInfo.uid) {
					uni.showToast({
						title: '请先登录',
						icon: 'none'
					})
					return
				}
				
				const response = await mysteryBoxApi.getUserEnergy(userInfo.uid)
				if (response && response.code === 200) {
					this.userEnergy = response.data.energy || 0
				}
			} catch (error) {
				console.error('获取能量值失败:', error)
			}
		},
		
		async loadHistory() {
			try {
				const userInfo = uni.getStorageSync('userInfo')
				if (!userInfo || !userInfo.uid) {
					return
				}
				
				const response = await mysteryBoxApi.getDrawHistory(userInfo.uid)
				if (response && response.code === 200) {
					console.log('历史记录响应数据:', response.data)
					// 确保每个记录都有日期字段
					this.historyList = (response.data || []).map(item => ({
						...item,
						date: item.date || item.createTime || item.drawTime || new Date().toISOString()
					}))
				}
			} catch (error) {
				console.error('获取历史记录失败:', error)
			}
		},
		
		async drawBox() {
			if (this.isShaking) return
			
			try {
				const userInfo = uni.getStorageSync('userInfo')
				if (!userInfo || !userInfo.uid) {
					uni.showToast({
						title: '请先登录',
						icon: 'none'
					})
					return
				}
				
				this.isShaking = true
				
				const response = await mysteryBoxApi.drawBox(userInfo.uid)
				if (response && response.code === 200) {
					console.log('抽取盲盒响应数据:', response.data)
					this.todayBox = {
						...response.data,
						isOpened: true,
						date: response.data.date || response.data.createTime || response.data.drawTime || new Date().toISOString()
					}
					
					// 更新能量值
					this.loadUserEnergy()
					
					uni.showToast({
						title: '盲盒已开启',
						icon: 'success'
					})
				} else {
					uni.showToast({
						title: response?.message || '抽取失败',
						icon: 'none'
					})
				}
			} catch (error) {
				console.error('抽取盲盒失败:', error)
				uni.showToast({
					title: '抽取失败，请重试',
					icon: 'none'
				})
			} finally {
				this.isShaking = false
			}
		},

		getTypeText(type) {
			switch (type) {
				case 'task': return '任务';
				case 'quote': return '日签';
				case 'reward': return '奖励';
				default: return '盲盒';
			}
		},
		
		getStatusText(type) {
			if (type === 'task') return '待完成'
			return '已开启'
		},
		
		completeTask(itemId) {
			uni.showToast({
				title: '任务完成',
				icon: 'success'
			})
		},
		
		showHistory() {
			this.showHistoryModal = true
		},
		
		hideHistory() {
			this.showHistoryModal = false
		},
		
		formatDate(dateString) {
			if (!dateString) {
				return '--'
			}
			
			try {
				const date = new Date(dateString)
				// 检查日期是否有效
				if (isNaN(date.getTime())) {
					console.warn('无效的日期:', dateString)
					return '--'
				}
				
				const month = String(date.getMonth() + 1).padStart(2, '0')
				const day = String(date.getDate()).padStart(2, '0')
				return `${month}-${day}`
			} catch (error) {
				console.error('日期格式化错误:', error, dateString)
				return '--'
			}
		}
	}
}
</script>

<style scoped>
.mysterybox-container {
	min-height: 100vh;
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
}

.content {
	padding: 40rpx 30rpx;
}

/* 顶部介绍区域 */
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

/* 今日盲盒区域 */
.today-section {
	margin-bottom: 30rpx;
}

.section-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 20rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: 700;
	color: #ffffff;
	text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.3);
}

.section-subtitle {
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.8);
}

.box-card {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 20rpx;
	padding: 30rpx;
	box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
	transition: all 0.3s ease;
	border: 1rpx solid rgba(255, 255, 255, 0.3);
}

.box-card.active {
	transform: scale(1.02);
	box-shadow: 0 12rpx 40rpx rgba(245, 158, 11, 0.4);
	border-color: #f59e0b;
}

.box-card.empty {
	background: rgba(255, 255, 255, 0.1);
	border: 2rpx dashed rgba(255, 255, 255, 0.3);
	cursor: pointer;
	transition: all 0.3s ease;
}

.box-card.empty:active {
	transform: scale(0.98);
	background: rgba(255, 255, 255, 0.2);
}

.box-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 15rpx;
}

.box-type-badge {
	font-size: 22rpx;
	color: #f59e0b;
	background: #fef3c7;
	padding: 6rpx 16rpx;
	border-radius: 12rpx;
	font-weight: 600;
	box-shadow: 0 2rpx 8rpx rgba(245, 158, 11, 0.2);
}

.box-status {
	font-size: 24rpx;
	font-weight: 600;
	padding: 6rpx 16rpx;
	border-radius: 12rpx;
}

.box-status.pending {
	color: #f59e0b;
	background: #fef3c7;
}

.box-status.completed {
	color: #10b981;
	background: #d1fae5;
}

.box-status.failed {
	color: #ef4444;
	background: #fee2e2;
}

.box-title {
	font-size: 28rpx;
	font-weight: 700;
	color: #1f2937;
	margin-bottom: 10rpx;
	display: block;
}

.box-content {
	font-size: 26rpx;
	color: #4b5563;
	line-height: 1.6;
	margin-bottom: 20rpx;
	display: block;
}

.box-footer {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.box-meta {
	display: flex;
	align-items: center;
	gap: 15rpx;
}

.box-time {
	font-size: 24rpx;
	color: #94a3b8;
}

.box-reward {
	font-size: 26rpx;
	font-weight: 700;
	color: #f59e0b;
	background: #fef3c7;
	padding: 4rpx 12rpx;
	border-radius: 8rpx;
}

.complete-btn {
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
	color: #ffffff;
	border: none;
	border-radius: 12rpx;
	padding: 12rpx 24rpx;
	font-size: 24rpx;
	font-weight: 600;
	box-shadow: 0 4rpx 16rpx rgba(99, 102, 241, 0.3);
	transition: all 0.3s ease;
}

.complete-btn:active {
	transform: translateY(2rpx);
	box-shadow: 0 2rpx 8rpx rgba(99, 102, 241, 0.4);
}

.empty-box {
	text-align: center;
	padding: 60rpx 0;
}

.empty-icon {
	font-size: 80rpx;
	margin-bottom: 15rpx;
	display: block;
	filter: drop-shadow(0 4rpx 8rpx rgba(0, 0, 0, 0.1));
}

.empty-text {
	font-size: 28rpx;
	font-weight: 600;
	color: #1f2937;
	margin-bottom: 10rpx;
	display: block;
}

.empty-desc {
	font-size: 24rpx;
	color: #94a3b8;
	display: block;
}

/* 历史记录区域 */
.history-section {
	margin-bottom: 30rpx;
}

.refresh-btn {
	background: rgba(255, 255, 255, 0.2);
	border: 1rpx solid rgba(255, 255, 255, 0.3);
	border-radius: 12rpx;
	padding: 8rpx 16rpx;
	color: #ffffff;
	font-size: 24rpx;
	display: flex;
	align-items: center;
	gap: 8rpx;
	transition: all 0.3s ease;
}

.refresh-btn:active {
	background: rgba(255, 255, 255, 0.3);
	transform: scale(0.95);
}

.refresh-icon {
	font-size: 20rpx;
}

.empty-history {
	text-align: center;
	padding: 60rpx 0;
	background: rgba(255, 255, 255, 0.1);
	border-radius: 20rpx;
	border: 2rpx dashed rgba(255, 255, 255, 0.3);
}

.empty-history-icon {
	font-size: 80rpx;
	margin-bottom: 15rpx;
	display: block;
	opacity: 0.6;
}

.empty-history-text {
	font-size: 28rpx;
	font-weight: 600;
	color: rgba(255, 255, 255, 0.8);
	margin-bottom: 10rpx;
	display: block;
}

.empty-history-desc {
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.6);
	display: block;
}

.history-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.history-item {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 16rpx;
	padding: 25rpx;
	border: 1rpx solid rgba(255, 255, 255, 0.3);
	transition: all 0.3s ease;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
}

.history-item:active {
	transform: scale(0.98);
}

.history-item.pending {
	border-left: 4rpx solid #f59e0b;
	background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(254, 243, 199, 0.1) 100%);
}

.history-item.completed {
	border-left: 4rpx solid #10b981;
	background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(209, 250, 229, 0.1) 100%);
}

.history-item.failed {
	border-left: 4rpx solid #ef4444;
	background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(254, 226, 226, 0.1) 100%);
}

.history-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 15rpx;
}

.history-type {
	font-size: 22rpx;
	color: #f59e0b;
	background: #fef3c7;
	padding: 4rpx 12rpx;
	border-radius: 8rpx;
	font-weight: 600;
}

.history-status {
	font-size: 22rpx;
	font-weight: 600;
	padding: 4rpx 12rpx;
	border-radius: 8rpx;
}

.history-status.pending {
	color: #f59e0b;
	background: #fef3c7;
}

.history-status.completed {
	color: #10b981;
	background: #d1fae5;
}

.history-status.failed {
	color: #ef4444;
	background: #fee2e2;
}

.history-title {
	font-size: 26rpx;
	font-weight: 600;
	color: #1f2937;
	margin-bottom: 10rpx;
	display: block;
}

.history-content {
	font-size: 24rpx;
	color: #4b5563;
	line-height: 1.5;
	margin-bottom: 15rpx;
	display: block;
}

.history-footer {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.history-meta {
	display: flex;
	align-items: center;
	gap: 15rpx;
}

.history-time {
	font-size: 22rpx;
	color: #94a3b8;
}

.history-reward {
	font-size: 24rpx;
	font-weight: 600;
	color: #f59e0b;
	background: #fef3c7;
	padding: 4rpx 12rpx;
	border-radius: 8rpx;
}

.history-actions {
	display: flex;
	gap: 10rpx;
}

.complete-task-btn {
	background: linear-gradient(135deg, #10b981 0%, #059669 100%);
	color: #ffffff;
	border: none;
	border-radius: 8rpx;
	padding: 8rpx 16rpx;
	font-size: 22rpx;
	font-weight: 600;
	box-shadow: 0 2rpx 8rpx rgba(16, 185, 129, 0.3);
	transition: all 0.3s ease;
}

.complete-task-btn:active {
	transform: translateY(1rpx);
	box-shadow: 0 1rpx 4rpx rgba(16, 185, 129, 0.4);
}
</style>
