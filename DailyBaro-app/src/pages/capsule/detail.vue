<template>
	<view class="detail-container">
		<!-- 自定义导航栏 -->
		<view class="custom-navbar">
			<view class="navbar-left" @click="goBack">
				<text class="back-icon">←</text>
			</view>
			<view class="navbar-title">胶囊详情</view>
			<view class="navbar-right" @click="editCapsule">
				<text class="edit-text">编辑</text>
			</view>
		</view>

		<!-- 内容区域 -->
		<view class="content">
			<!-- 胶囊信息 -->
			<view class="capsule-info">
				<view class="capsule-header">
					<text class="capsule-title">{{ capsule.title || '未命名胶囊' }}</text>
					<view class="capsule-status" :class="getStatusClass(capsule.status)">
						{{ getStatusText(capsule.status) }}
					</view>
				</view>
				
				<view class="capsule-meta">
					<text class="create-time">创建时间：{{ formatDate(capsule.createTime) }}</text>
					<text class="open-time" v-if="capsule.openTime">开启时间：{{ formatDate(capsule.openTime) }}</text>
				</view>
			</view>

			<!-- 胶囊内容 -->
			<view class="capsule-content">
				<view class="content-title">胶囊内容</view>
				<view class="content-text">{{ capsule.content || '暂无内容' }}</view>
			</view>

			<!-- 情绪标签 -->
			<view class="emotion-tags" v-if="capsule.emotions && capsule.emotions.length > 0">
				<view class="tags-title">情绪标签</view>
				<view class="tags-list">
					<view class="tag" v-for="emotion in capsule.emotions" :key="emotion">
						{{ emotion }}
					</view>
				</view>
			</view>

			<!-- 附件 -->
			<view class="attachments" v-if="capsule.attachments && capsule.attachments.length > 0">
				<view class="attachments-title">附件</view>
				<view class="attachments-list">
					<view class="attachment" v-for="(attachment, index) in capsule.attachments" :key="index">
						<image v-if="isImage(attachment)" :src="attachment" class="attachment-image" mode="aspectFill" @click="previewImage(attachment)"></image>
						<view v-else class="attachment-file" @click="downloadFile(attachment)">
							<text class="file-icon">📎</text>
							<text class="file-name">{{ getFileName(attachment) }}</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 操作按钮 -->
			<view class="action-buttons">
				<button class="action-btn share-btn" @click="shareCapsule">分享</button>
				<button class="action-btn delete-btn" @click="deleteCapsule">删除</button>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			capsule: {},
			capsuleId: null
		}
	},
	
	onLoad(options) {
		if (options.id) {
			this.capsuleId = options.id;
			this.loadCapsuleDetail();
		}
	},
	
	methods: {
		// 加载胶囊详情
		async loadCapsuleDetail() {
			try {
				uni.showLoading({ title: '加载中...' });
				
				// 模拟API调用
				await new Promise(resolve => setTimeout(resolve, 1000));
				
				// 模拟数据
				this.capsule = {
					id: this.capsuleId,
					title: '我的第一个胶囊',
					content: '这是一个充满回忆的胶囊，记录了我当时的心情和想法...',
					status: 'opened', // opened, sealed
					createTime: new Date().getTime() - 7 * 24 * 60 * 60 * 1000,
					openTime: new Date().getTime(),
					emotions: ['开心', '激动', '期待'],
					attachments: [
						'https://via.placeholder.com/300x200/FF6B6B/FFFFFF?text=Photo1',
						'https://via.placeholder.com/300x200/4ECDC4/FFFFFF?text=Photo2'
					]
				};
				
				uni.hideLoading();
			} catch (error) {
				uni.hideLoading();
				uni.showToast({ title: '加载失败', icon: 'none' });
				console.error('加载胶囊详情失败:', error);
			}
		},
		
		// 返回
		goBack() {
			uni.navigateBack();
		},
		
		// 编辑胶囊
		editCapsule() {
			uni.navigateTo({
				url: `/pages/capsule/edit?id=${this.capsuleId}`
			});
		},
		
		// 获取状态样式
		getStatusClass(status) {
			return status === 'opened' ? 'status-opened' : 'status-sealed';
		},
		
		// 获取状态文本
		getStatusText(status) {
			return status === 'opened' ? '已开启' : '未开启';
		},
		
		// 格式化日期
		formatDate(timestamp) {
			if (!timestamp) return '';
			const date = new Date(timestamp);
			return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
		},
		
		// 判断是否为图片
		isImage(url) {
			return url && (url.includes('.jpg') || url.includes('.jpeg') || url.includes('.png') || url.includes('.gif'));
		},
		
		// 预览图片
		previewImage(url) {
			uni.previewImage({
				urls: this.capsule.attachments.filter(att => this.isImage(att)),
				current: url
			});
		},
		
		// 下载文件
		downloadFile(url) {
			uni.downloadFile({
				url: url,
				success: (res) => {
					uni.showToast({ title: '下载成功', icon: 'success' });
				},
				fail: () => {
					uni.showToast({ title: '下载失败', icon: 'none' });
				}
			});
		},
		
		// 获取文件名
		getFileName(url) {
			return url.split('/').pop() || '未知文件';
		},
		
		// 分享胶囊
		shareCapsule() {
			uni.showToast({ title: '分享功能开发中', icon: 'none' });
		},
		
		// 删除胶囊
		deleteCapsule() {
			uni.showModal({
				title: '确认删除',
				content: '确定要删除这个胶囊吗？删除后无法恢复。',
				success: (res) => {
					if (res.confirm) {
						uni.showToast({ title: '删除成功', icon: 'success' });
						setTimeout(() => {
							uni.navigateBack();
						}, 1500);
					}
				}
			});
		}
	}
}
</script>

<style scoped>
.detail-container {
	min-height: 100vh;
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
	position: relative;
}

.custom-navbar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 20rpx 30rpx;
	background: rgba(255, 255, 255, 0.1);
	backdrop-filter: blur(10px);
}

.navbar-left, .navbar-right {
	width: 80rpx;
	display: flex;
	align-items: center;
}

.back-icon {
	font-size: 36rpx;
	color: #fff;
}

.navbar-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #fff;
}

.edit-text {
	font-size: 28rpx;
	color: #fff;
}

.content {
	padding: 30rpx;
}

.capsule-info {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.capsule-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}

.capsule-title {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
}

.capsule-status {
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
	color: #fff;
}

.status-opened {
	background: #4CAF50;
}

.status-sealed {
	background: #FF9800;
}

.capsule-meta {
	display: flex;
	flex-direction: column;
	gap: 10rpx;
}

.create-time, .open-time {
	font-size: 26rpx;
	color: #666;
}

.capsule-content {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.content-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 20rpx;
}

.content-text {
	font-size: 28rpx;
	color: #666;
	line-height: 1.6;
}

.emotion-tags {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.tags-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 20rpx;
}

.tags-list {
	display: flex;
	flex-wrap: wrap;
	gap: 15rpx;
}

.tag {
	padding: 10rpx 20rpx;
	background: linear-gradient(135deg, #4a90e2, #6aa8ff);
	color: #fff;
	border-radius: 20rpx;
	font-size: 24rpx;
}

.attachments {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.attachments-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 20rpx;
}

.attachments-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.attachment-image {
	width: 100%;
	height: 300rpx;
	border-radius: 15rpx;
}

.attachment-file {
	display: flex;
	align-items: center;
	padding: 20rpx;
	background: #f5f5f5;
	border-radius: 15rpx;
}

.file-icon {
	font-size: 40rpx;
	margin-right: 20rpx;
}

.file-name {
	font-size: 28rpx;
	color: #333;
}

.action-buttons {
	display: flex;
	gap: 30rpx;
	margin-top: 40rpx;
}

.action-btn {
	flex: 1;
	padding: 25rpx;
	border-radius: 15rpx;
	font-size: 28rpx;
	color: #fff;
	border: none;
}

.share-btn {
	background: linear-gradient(135deg, #4a90e2, #6aa8ff);
}

.delete-btn {
	background: linear-gradient(135deg, #ff6b6b, #ee5a52);
}
</style>
