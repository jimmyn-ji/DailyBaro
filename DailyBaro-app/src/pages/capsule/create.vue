<template>
	<view class="create-container">
		<!-- 添加顶部导航栏 -->
		<view class="nav-bar">
			<view class="back-btn" @tap="goBack">
				<text class="back-icon">←</text>
			</view>
			<text class="page-title">{{ isEdit ? '编辑胶囊' : '创建胶囊' }}</text>
			<view class="placeholder"></view>
		</view>
		
		<view class="content">
			<!-- 胶囊内容输入 -->
			<view class="input-section">
				<textarea 
					class="capsule-input" 
					placeholder="写下你此刻的想法、感受或目标..." 
					v-model="capsuleContent"
					maxlength="500"
					auto-height
				></textarea>
				<text class="char-count">{{ capsuleContent.length }}/500</text>
			</view>
			
			<!-- 开启时间设置 -->
			<view class="time-section">
				<view class="section-title">设置开启时间</view>
				
				<view class="time-picker">
					<view class="date-picker">
						<text class="picker-label">日期</text>
						<picker 
							mode="date" 
							:value="openDate" 
							:start="minDate"
							@change="onDateChange"
						>
							<view class="picker-field">
								<text class="picker-text">{{ openDate }}</text>
								<text class="picker-arrow">›</text>
							</view>
						</picker>
					</view>
					
					<view class="time-picker-row">
						<view class="time-picker-item">
							<text class="picker-label">小时</text>
							<picker 
								mode="selector" 
								:range="hours" 
								:value="selectedHour"
								@change="onHourChange"
							>
								<view class="picker-field">
									<text class="picker-text">{{ selectedHour }}时</text>
									<text class="picker-arrow">›</text>
								</view>
							</picker>
						</view>
						
						<view class="time-picker-item">
							<text class="picker-label">分钟</text>
							<picker 
								mode="selector" 
								:range="minutes" 
								:value="selectedMinute"
								@change="onMinuteChange"
							>
								<view class="picker-field">
									<text class="picker-text">{{ selectedMinute }}分</text>
									<text class="picker-arrow">›</text>
								</view>
							</picker>
						</view>
					</view>
				</view>
			</view>
			
			<!-- 提醒设置 -->
			<view class="reminder-section">
				<view class="section-title">开启提醒</view>
				<view class="reminder-options">
					<view 
						class="reminder-item"
						:class="{ active: enableReminder }"
						@tap="toggleReminder"
					>
						<text class="reminder-icon">{{ enableReminder ? '🔔' : '🔕' }}</text>
						<text class="reminder-text">应用内通知</text>
						<text class="reminder-status">{{ enableReminder ? '已开启' : '已关闭' }}</text>
					</view>
				</view>
			</view>
			
			<!-- 创建按钮 -->
			<view class="action-section">
				<button class="create-btn" @tap="createCapsule" :disabled="!canCreate">
					{{ isEdit ? '保存修改' : '创建胶囊' }}
				</button>
			</view>
		</view>
	</view>
</template>

<script>
import { capsuleApi } from '@/utils/api.js'

export default {
	data() {
		return {
			isEdit: false,
			capsuleId: null,
			capsuleContent: '',
			openDate: '',
			selectedHour: '09',
			selectedMinute: '00',
			enableReminder: true,
			minDate: new Date().toISOString().split('T')[0],
			hours: Array.from({ length: 24 }, (_, i) => i),
			minutes: Array.from({ length: 60 }, (_, i) => i),
		}
	},
	
	onLoad(options) {
		if (options.id) {
			this.isEdit = true
			this.capsuleId = options.id
			this.loadCapsule()
		} else {
			// 默认设置为明天上午9点
			const tomorrow = new Date()
			tomorrow.setDate(tomorrow.getDate() + 1)
			tomorrow.setHours(9, 0, 0, 0)
			
			this.openDate = tomorrow.toISOString().split('T')[0]
			this.selectedHour = '09'
			this.selectedMinute = '00'
		}
	},
	
	computed: {
		canCreate() {
			return this.capsuleContent && this.openDate && this.selectedHour && this.selectedMinute
		}
	},
	
	methods: {
		goBack() {
			uni.navigateBack()
		},
		
		async loadCapsule() {
			try {
				const res = await capsuleApi.getCapsuleDetail(this.capsuleId)
				
				if (res.code === 200) {
					this.capsuleContent = res.data.content || ''
					this.openDate = res.data.openTime ? res.data.openTime.split('T')[0] : ''
					this.selectedHour = res.data.openTime ? res.data.openTime.split('T')[1].substring(0, 2) : '09'
					this.selectedMinute = res.data.openTime ? res.data.openTime.split('T')[1].substring(3, 5) : '00'
					this.enableReminder = res.data.reminderType === 'app_notification'
				}
			} catch (error) {
				uni.showToast({
					title: error.message || '加载失败',
					icon: 'none'
				})
			}
		},
		
		onDateChange(e) {
			this.openDate = e.detail.value
		},
		
		onHourChange(e) {
			this.selectedHour = e.detail.value
		},
		
		onMinuteChange(e) {
			this.selectedMinute = e.detail.value
		},
		
		toggleReminder() {
			this.enableReminder = !this.enableReminder
		},
		
		async createCapsule() {
			if (!this.canCreate) {
				uni.showToast({
					title: '请完善信息',
					icon: 'none'
				})
				return
			}
			
			try {
				const data = {
					content: this.capsuleContent,
					openTime: this.openDate + 'T' + this.selectedHour + ':' + this.selectedMinute + ':00',
					reminderType: this.enableReminder ? 'app_notification' : 'sms'
				}
				
				let res
				if (this.isEdit) {
					res = await capsuleApi.updateCapsule(this.capsuleId, data)
				} else {
					res = await capsuleApi.createCapsule(data)
				}
				
				if (res.code === 200) {
					uni.showToast({
						title: '保存成功',
						icon: 'success'
					})
					
					setTimeout(() => {
						uni.navigateBack()
					}, 1500)
				} else {
					uni.showToast({
						title: res.message || '保存失败',
						icon: 'none'
					})
				}
			} catch (error) {
				uni.showToast({
					title: error.message || '保存失败',
					icon: 'none'
				})
			}
		}
	}
}
</script>

<style scoped>
.create-container {
	min-height: 100vh;
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
}

/* 添加导航栏样式 */
.nav-bar {
	background: rgba(255, 255, 255, 0.95);
	backdrop-filter: blur(20rpx);
	padding: 30rpx 40rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	border-bottom: 1rpx solid rgba(226, 232, 240, 0.8);
	position: sticky;
	top: 0;
	z-index: 100;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.back-btn {
	width: 60rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 50%;
	background: rgba(99, 102, 241, 0.1);
	transition: all 0.3s ease;
}

.back-btn:active {
	background: rgba(99, 102, 241, 0.2);
	transform: scale(0.95);
}

.back-icon {
	font-size: 36rpx;
	color: #4a90e2;
	font-weight: 600;
}

.page-title {
	font-size: 32rpx;
	font-weight: 700;
	color: #1f2937;
}

.placeholder {
	width: 60rpx;
}

.content {
	padding: 30rpx;
	padding-bottom: 120rpx;
}

/* 胶囊内容输入 */
.input-section {
	margin-bottom: 30rpx;
}

.capsule-input {
	width: 100%;
	min-height: 200rpx;
	background: rgba(255, 255, 255, 0.95);
	border: 1rpx solid rgba(255, 255, 255, 0.3);
	border-radius: 20rpx;
	padding: 30rpx;
	font-size: 28rpx;
	line-height: 1.6;
	color: #1f2937;
	box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.char-count {
	text-align: right;
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.8);
	margin-top: 10rpx;
	display: block;
}

/* 时间设置 */
.time-section {
	margin-bottom: 30rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: 700;
	color: #ffffff;
	margin-bottom: 20rpx;
	text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.3);
}

.time-picker {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 20rpx;
	padding: 30rpx;
	box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.date-picker {
	margin-bottom: 25rpx;
}

.picker-label {
	font-size: 26rpx;
	font-weight: 600;
	color: #1f2937;
	margin-bottom: 15rpx;
	display: block;
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

.time-picker-row {
	display: flex;
	gap: 20rpx;
}

.time-picker-item {
	flex: 1;
}

/* 提醒设置 */
.reminder-section {
	margin-bottom: 30rpx;
}

.reminder-options {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 20rpx;
	padding: 30rpx;
	box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.reminder-item {
	display: flex;
	align-items: center;
	padding: 20rpx;
	background: #f9fafb;
	border-radius: 16rpx;
	border: 2rpx solid transparent;
	transition: all 0.3s ease;
}

.reminder-item.active {
	background: #e0f2fe;
	border-color: #0ea5e9;
}

.reminder-icon {
	font-size: 32rpx;
	margin-right: 15rpx;
}

.reminder-text {
	flex: 1;
	font-size: 26rpx;
	color: #1f2937;
	font-weight: 500;
}

.reminder-status {
	font-size: 22rpx;
	color: #64748b;
}

/* 操作按钮 */
.action-section {
	margin-bottom: 30rpx;
}

.create-btn {
	width: 100%;
	background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
	color: #ffffff;
	border: none;
	border-radius: 16rpx;
	padding: 25rpx;
	font-size: 30rpx;
	font-weight: 600;
	box-shadow: 0 4rpx 16rpx rgba(245, 158, 11, 0.3);
	transition: all 0.3s ease;
}

.create-btn:active {
	transform: translateY(2rpx);
	box-shadow: 0 2rpx 8rpx rgba(245, 158, 11, 0.4);
}

.create-btn:disabled {
	background: #9ca3af;
	box-shadow: none;
}
</style>