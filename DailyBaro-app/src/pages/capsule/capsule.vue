<template>
	<view class="capsule-container">
		<view class="content">
			<!-- 标签页 -->
			<view class="tabs">
				<view 
					class="tab-item" 
					:class="{ active: currentStatus === 'all' }"
					@tap="filterByStatus('all')"
				>
					<text>全部</text>
				</view>
				<view 
					class="tab-item" 
					:class="{ active: currentStatus === 'pending' }"
					@tap="filterByStatus('pending')"
				>
					<text>待开启</text>
				</view>
				<view 
					class="tab-item" 
					:class="{ active: currentStatus === 'opened' }"
					@tap="filterByStatus('opened')"
				>
					<text>已开启</text>
				</view>
			</view>
			
			<!-- 胶囊列表 -->
			<view class="capsules-list">
				<view 
					v-for="capsule in filteredCapsules" 
					:key="capsule.capsuleId" 
					class="capsule-item"
					@tap="viewCapsule(capsule.capsuleId)"
				>
					<view class="capsule-header">
						<text class="capsule-time">{{ formatDate(capsule.openTime) }}</text>
						<text class="capsule-status" :class="capsule.isOpened ? 'opened' : 'pending'">
							{{ getStatusText(capsule.isOpened) }}
						</text>
					</view>
					<text class="capsule-content">{{ capsule.content || '暂无内容' }}</text>
					<view class="capsule-footer">
						<text class="open-time">开启时间: {{ formatDateTime(capsule.openTime) }}</text>
					</view>
				</view>
			</view>
			
			<!-- 创建按钮 -->
			<view class="create-section">
				<button class="create-btn" @tap="createCapsule">创建胶囊</button>
			</view>
		</view>
	</view>
</template>

<script>
import { capsuleApi } from '../../utils/api.js'
import { formatDateShort, formatDateTime as formatDateTimeUtil } from '../../utils/dateUtils.js'

export default {
	data() {
		return {
			capsules: [],
			currentStatus: 'all',
			loading: false
		}
	},
	
	computed: {
		filteredCapsules() {
			if (this.currentStatus === 'all') {
				return this.capsules
			}
			// 根据isOpened字段过滤
			if (this.currentStatus === 'pending') {
				return this.capsules.filter(capsule => !capsule.isOpened)
			} else if (this.currentStatus === 'opened') {
				return this.capsules.filter(capsule => capsule.isOpened)
			}
			return this.capsules
		}
	},
	
	onLoad() {
		this.loadCapsules()
	},
	
	onShow() {
		this.loadCapsules()
	},
	
	methods: {
		async loadCapsules() {
			try {
				const userInfo = uni.getStorageSync('userInfo')
				if (!userInfo || !userInfo.uid) {
					uni.showToast({
						title: '请先登录',
						icon: 'none'
					})
					return
				}
				
				const response = await capsuleApi.getCapsules(userInfo.uid)
				if (response && response.code === 200) {
					this.capsules = response.data || []
				} else {
					uni.showToast({
						title: response?.message || '获取胶囊失败',
						icon: 'none'
					})
				}
			} catch (error) {
				console.error('加载胶囊失败:', error)
				uni.showToast({
					title: '加载失败，请重试',
					icon: 'none'
				})
			}
		},
		
		// 创建胶囊
		createCapsule() {
			uni.navigateTo({
				url: '/pages/capsule/create'
			})
		},
		
		// 查看胶囊详情
		viewCapsule(id) {
			// 跳转到胶囊详情页面
			uni.navigateTo({
				url: `/pages/capsule/detail?id=${id}`
			})
		},
		
		// 删除胶囊
		async deleteCapsule(id) {
			uni.showModal({
				title: '确认删除',
				content: '确定要删除这个胶囊吗？',
				success: async (res) => {
					if (res.confirm) {
						try {
							const response = await capsuleApi.deleteCapsule(id)
							if (response && response.code === 200) {
								uni.showToast({
									title: '删除成功',
									icon: 'success'
								})
								// 重新加载胶囊列表
								this.loadCapsules()
							} else {
								uni.showToast({
									title: response?.message || '删除失败',
									icon: 'none'
								})
							}
						} catch (error) {
							console.error('删除胶囊失败:', error)
							uni.showToast({
								title: '删除失败，请重试',
								icon: 'none'
							})
						}
					}
				}
			})
		},
		
		// 按状态筛选
		filterByStatus(status) {
			this.currentStatus = status
			uni.showToast({
				title: `筛选: ${status}`,
				icon: 'success'
			})
		},
		
		// 格式化日期
		formatDate(dateString) {
			return formatDateShort(dateString)
		},

		formatDateTime(dateStr) {
			return formatDateTimeUtil(dateStr)
		},

		getStatusText(isOpened) {
			return isOpened ? '已开启' : '待开启'
		}
	}
}
</script>

<style scoped>
.capsule-container {
	min-height: 100vh;
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
}

.content {
	padding: 20rpx 40rpx;
}

.tabs {
	display: flex;
	gap: 20rpx;
	margin-bottom: 20rpx;
}

.tab-item {
	flex: 1;
	padding: 20rpx;
	border-radius: 12rpx;
	font-size: 28rpx;
	color: #64748b;
	text-align: center;
	background: #f1f5f9;
	transition: all 0.3s ease;
}

.tab-item.active {
	background: #4a90e2;
	color: #ffffff;
}

.capsules-list {
	background: #ffffff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.capsule-item {
	padding: 20rpx 0;
	border-bottom: 1rpx solid #e2e8f0;
}

.capsule-item:last-child {
	border-bottom: none;
}

.capsule-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 10rpx;
}

.capsule-time {
	font-size: 24rpx;
	color: #94a3b8;
}

.capsule-status {
	padding: 8rpx 16rpx;
	border-radius: 12rpx;
	font-size: 22rpx;
	font-weight: 500;
}

.capsule-status.pending {
	background: #fef3c7;
	color: #d97706;
}

.capsule-status.opened {
	background: #d1fae5;
	color: #065f46;
}

.capsule-content {
	font-size: 28rpx;
	color: #1f2937;
	line-height: 1.6;
	margin-bottom: 10rpx;
}

.capsule-footer {
	display: flex;
	justify-content: flex-end;
}

.open-time {
	font-size: 24rpx;
	color: #64748b;
}

.create-section {
	text-align: center;
	padding: 30rpx 0;
}

.create-btn {
	background: #4a90e2;
	color: #ffffff;
	border: none;
	border-radius: 12rpx;
	padding: 20rpx 40rpx;
	font-size: 28rpx;
}
</style>
