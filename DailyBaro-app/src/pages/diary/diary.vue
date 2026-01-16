<template>
	<view class="diary-container">
		<view class="content">
			<!-- 匿名星球入口 -->
			<view class="anonymous-entry" @tap="goToAnonymous">
				<view class="entry-content">
					<view class="entry-icon">🌍</view>
					<view class="entry-info">
						<text class="entry-title">匿名星球</text>
						<text class="entry-desc">分享你的心情，遇见有趣的灵魂</text>
					</view>
					<view class="entry-arrow">›</view>
				</view>
			</view>
			
			<!-- 日记操作栏 -->
			<view class="action-bar">
				<view class="filter-tabs">
					<view 
						class="tab-item" 
						:class="{ active: currentStatus === 'all' }"
						@tap="filterByStatus('all')"
					>
						全部
					</view>
					<view 
						class="tab-item" 
						:class="{ active: currentStatus === 'published' }"
						@tap="filterByStatus('published')"
					>
						已发布
					</view>
					<view 
						class="tab-item" 
						:class="{ active: currentStatus === 'draft' }"
						@tap="filterByStatus('draft')"
					>
						草稿箱
					</view>
				</view>
				
				<button class="add-btn" @tap="createDiary">
					<text class="add-icon">+</text>
					<text class="add-text">写日记</text>
				</button>
			</view>
			
			<!-- 日记列表 -->
			<view class="diary-list">
				<view v-if="loading" class="loading-state">
					<text class="loading-text">加载中...</text>
				</view>
				
				<view 
					v-else-if="filteredDiaries.length > 0"
					v-for="diary in filteredDiaries" 
					:key="diary.diaryId"
					class="diary-item"
					@tap="viewDiary(diary.diaryId)"
				>
					<view class="diary-header">
						<view class="diary-meta">
							<text class="diary-date">{{ formatDate(diary.createTime) }}</text>
							<view class="diary-status" :class="diary.status">
								{{ diary.status === 'published' ? '已发布' : '草稿' }}
							</view>
						</view>
						<view class="diary-actions">
							<button class="action-btn edit" @tap.stop="editDiary(diary.diaryId)">编辑</button>
							<button class="action-btn delete" @tap.stop="deleteDiary(diary.diaryId)">删除</button>
						</view>
					</view>
					
					<view class="diary-content">
						<text class="diary-text">{{ diary.content }}</text>
					</view>
					
					<view class="diary-footer">
						<view class="diary-tags">
							<text v-if="diary.emotion" class="emotion-tag">{{ diary.emotion }}</text>
						</view>
						<view v-if="diary.media && diary.media.length > 0" class="media-count">
							<text class="media-icon">📎</text>
							<text class="media-text">{{ diary.media.length }}个附件</text>
						</view>
					</view>
				</view>
				
				<view v-else class="empty-state">
					<text class="empty-icon">📝</text>
					<text class="empty-text">还没有日记，开始记录你的心情吧</text>
					<button class="empty-btn" @tap="createDiary">写第一篇日记</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { diaryApi } from '../../utils/api.js'
import { formatDateShort } from '../../utils/dateUtils.js'

const diaries = ref([])
const currentStatus = ref('all')
const loading = ref(false)

const filteredDiaries = computed(() => {
	if (currentStatus.value === 'all') {
		return diaries.value
	}
	return diaries.value.filter(diary => diary.status === currentStatus.value)
})

onMounted(() => {
	loadDiaries()
})

const loadDiaries = async () => {
	try {
		loading.value = true
		const userInfo = uni.getStorageSync('userInfo')
		const uid = userInfo && (userInfo.uid || userInfo.id || userInfo.userId)
		if (!uid) {
			uni.showToast({
				title: '请先登录',
				icon: 'none'
			})
			return
		}
		
		// 根据当前状态决定查询参数
		// 说明：后端通过请求头 uid 识别用户；
		// 为避免误取全站公开，默认查询当前用户全部或按状态筛选。
		let queryParams = {}
		if (currentStatus.value !== 'all') {
			queryParams.status = currentStatus.value
		}
		
		// 调用接口获取日记列表
		const response = await diaryApi.getDiaries(queryParams)
		console.log('日记列表响应:', response)
		if (response && response.code === 200) {
			diaries.value = response.data || []
			console.log('日记数据:', diaries.value)
		} else {
			uni.showToast({
				title: response?.message || '获取日记失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('加载日记失败:', error)
		uni.showToast({
			title: '加载失败，请重试',
			icon: 'none'
		})
	} finally {
		loading.value = false
	}
}

const goToAnonymous = () => {
	uni.navigateTo({
		url: '/pages/anonymous/anonymous'
	})
}

const createDiary = () => {
	uni.navigateTo({
		url: '/pages/diary/edit'
	})
}

const viewDiary = (id) => {
	console.log('查看日记，ID:', id)
	uni.navigateTo({
		url: `/pages/diary/detail?id=${id}`
	})
}

const editDiary = (id) => {
	console.log('编辑日记，ID:', id)
	uni.navigateTo({
		url: `/pages/diary/edit?id=${id}`
	})
}

const deleteDiary = async (id) => {
	uni.showModal({
		title: '确认删除',
		content: '确定要删除这篇日记吗？删除后无法恢复。',
		success: async (res) => {
			if (res.confirm) {
				try {
					const response = await diaryApi.deleteDiary(id)
					if (response && response.code === 200) {
						uni.showToast({
							title: '删除成功',
							icon: 'success'
						})
						// 重新加载日记列表
						loadDiaries()
					} else {
						uni.showToast({
							title: response?.message || '删除失败',
							icon: 'none'
						})
					}
				} catch (error) {
					console.error('删除日记失败:', error)
					uni.showToast({
						title: '删除失败，请重试',
						icon: 'none'
					})
				}
			}
		}
	})
}

const filterByStatus = (status) => {
	currentStatus.value = status
	// 切换状态后重新加载数据
	loadDiaries()
}

const formatDate = (dateString) => {
	return formatDateShort(dateString)
}
</script>

<style scoped>
.diary-container {
	min-height: 100vh;
	background: transparent;
	padding: 20rpx;
}

.content {
	max-width: 750rpx;
	margin: 0 auto;
}

.anonymous-entry {
	margin-bottom: 30rpx;
	padding: 30rpx 0;
}

.entry-content {
	display: flex;
	align-items: center;
}

.entry-icon {
	font-size: 48rpx;
	margin-right: 20rpx;
}

.entry-info {
	flex: 1;
}

.entry-title {
	display: block;
	font-size: 32rpx;
	font-weight: 600;
	color: #2d3748;
	margin-bottom: 8rpx;
}

.entry-desc {
	display: block;
	font-size: 26rpx;
	color: #718096;
}

.entry-arrow {
	font-size: 32rpx;
	color: #a0aec0;
}

.action-bar {
	margin-bottom: 30rpx;
	padding: 30rpx 0;
}

.filter-tabs {
	display: flex;
	margin-bottom: 30rpx;
	background: #f7fafc;
	border-radius: 16rpx;
	padding: 8rpx;
}

.tab-item {
	flex: 1;
	text-align: center;
	padding: 20rpx;
	border-radius: 12rpx;
	font-size: 28rpx;
	color: #718096;
	transition: all 0.3s ease;
}

.tab-item.active {
	/* 选中标签使用统一蓝色渐变 */
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
	color: white;
	box-shadow: 0 4rpx 16rpx rgba(74, 144, 226, 0.4);
}

.add-btn {
	/* 新建日记按钮使用统一蓝色渐变 */
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
	border: none;
	border-radius: 16rpx;
	padding: 24rpx 40rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 8rpx 24rpx rgba(74, 144, 226, 0.3);
	transition: all 0.3s ease;
}

.add-btn:active {
	transform: translateY(2rpx);
	box-shadow: 0 4rpx 12rpx rgba(74, 144, 226, 0.4);
}

.add-icon {
	color: white;
	font-size: 32rpx;
	font-weight: bold;
	margin-right: 12rpx;
}

.add-text {
	color: white;
	font-size: 28rpx;
	font-weight: 500;
}

.diary-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.diary-item {
	padding: 30rpx 0;
	border-bottom: 1rpx solid rgba(0, 0, 0, 0.1);
	transition: all 0.3s ease;
}

.diary-item:active {
	transform: translateY(2rpx);
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.15);
}

.diary-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}

.diary-meta {
	display: flex;
	align-items: center;
	gap: 20rpx;
}

.diary-date {
	font-size: 26rpx;
	color: #718096;
	font-weight: 500;
}

.diary-status {
	padding: 8rpx 16rpx;
	border-radius: 20rpx;
	font-size: 22rpx;
	font-weight: 500;
}

.diary-status.published {
	background: #c6f6d5;
	color: #22543d;
}

.diary-status.draft {
	background: #fed7d7;
	color: #742a2a;
}

.diary-actions {
	display: flex;
	gap: 16rpx;
}

.action-btn {
	border: none;
	border-radius: 12rpx;
	padding: 12rpx 20rpx;
	font-size: 24rpx;
	font-weight: 500;
	transition: all 0.3s ease;
}

.action-btn.edit {
	background: #e6fffa;
	color: #234e52;
}

.action-btn.delete {
	background: #fed7d7;
	color: #742a2a;
}

.diary-content {
	margin-bottom: 20rpx;
}

.diary-text {
	font-size: 28rpx;
	color: #2d3748;
	line-height: 1.6;
}

.diary-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.diary-tags {
	display: flex;
	gap: 12rpx;
}

.emotion-tag {
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
	color: white;
	padding: 8rpx 16rpx;
	border-radius: 20rpx;
	font-size: 22rpx;
	font-weight: 500;
}

.media-count {
	display: flex;
	align-items: center;
	gap: 8rpx;
}

.media-icon {
	font-size: 24rpx;
}

.media-text {
	font-size: 24rpx;
	color: #718096;
}

.loading-state {
	text-align: center;
	padding: 60rpx 0;
}

.loading-text {
	font-size: 28rpx;
	color: rgba(255, 255, 255, 0.8);
}

.empty-state {
	text-align: center;
	padding: 80rpx 0;
}

.empty-icon {
	font-size: 80rpx;
	margin-bottom: 20rpx;
	display: block;
}

.empty-text {
	display: block;
	font-size: 28rpx;
	color: #718096;
	margin-bottom: 30rpx;
}

.empty-btn {
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
	border: none;
	border-radius: 16rpx;
	padding: 20rpx 40rpx;
	color: white;
	font-size: 28rpx;
	font-weight: 500;
	box-shadow: 0 8rpx 24rpx rgba(74, 144, 226, 0.3);
}
</style>
