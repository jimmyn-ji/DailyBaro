<template>
	<view></view>
</template>

<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'

onLaunch(() => {
	console.log('App Launch')
	checkLoginStatus()
})

onShow(() => {
	console.log('App Show')
})

onHide(() => {
	console.log('App Hide')
})

const checkLoginStatus = () => {
	const userInfo = uni.getStorageSync('userInfo')
	if (userInfo && userInfo.uid) {
		// 已登录，跳转到主页面
		uni.switchTab({
			url: '/pages/diary/diary'
		})
	} else {
		// 未登录，跳转到登录页面
		uni.redirectTo({
			url: '/pages/login/login'
		})
	}
}
</script>

<style>
	/* 全局样式 */
	page {
		/* 微信小程序不支持 background-attachment: fixed，移除全局背景图，由各页面自己设置 */
		background-color: transparent;
		font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	}
	
	/* 通用容器 */
	.container {
		padding: 20rpx;
	}
	
	/* 卡片样式 */
	.card {
		background: #ffffff;
		border-radius: 16rpx;
		box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
		margin-bottom: 20rpx;
		overflow: hidden;
	}
	
	/* 按钮样式 */
	.btn {
		border-radius: 12rpx;
		font-size: 28rpx;
		font-weight: 500;
		text-align: center;
		padding: 20rpx 32rpx;
		border: none;
		transition: all 0.3s ease;
	}
	
	.btn-primary {
		/* 主按钮统一采用与背景图片同系列的蓝色系 */
		background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
		color: #ffffff;
	}
	
	.btn-secondary {
		background: #f1f5f9;
		color: #64748b;
	}
	
	.btn-outline {
		background: transparent;
		color: #4a90e2;
		border: 2rpx solid #4a90e2;
	}
	
	/* 输入框样式 */
	.input {
		background: #f8fafc;
		border: 2rpx solid #e2e8f0;
		border-radius: 12rpx;
		padding: 24rpx;
		font-size: 28rpx;
		transition: all 0.3s ease;
	}
	
	.input:focus {
		border-color: #4a90e2;
		background: #ffffff;
	}
	
	/* 文本样式 */
	.text-primary {
		color: #4a90e2;
	}
	
	.text-secondary {
		color: #64748b;
	}
	
	.text-muted {
		color: #94a3b8;
	}
	
	/* 间距 */
	.mt-10 { margin-top: 10rpx; }
	.mt-20 { margin-top: 20rpx; }
	.mt-30 { margin-top: 30rpx; }
	.mb-10 { margin-bottom: 10rpx; }
	.mb-20 { margin-bottom: 20rpx; }
	.mb-30 { margin-bottom: 30rpx; }
	.ml-10 { margin-left: 10rpx; }
	.mr-10 { margin-right: 10rpx; }
	
	.p-10 { padding: 10rpx; }
	.p-20 { padding: 20rpx; }
	.p-30 { padding: 30rpx; }
	
	/* 布局 */
	.flex {
		display: flex;
	}
	
	.flex-center {
		display: flex;
		align-items: center;
		justify-content: center;
	}
	
	.flex-between {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}
	
	.flex-column {
		display: flex;
		flex-direction: column;
	}
	
	/* 动画 */
	.fade-in {
		animation: fadeIn 0.3s ease-in-out;
	}
	
	@keyframes fadeIn {
		from { opacity: 0; transform: translateY(20rpx); }
		to { opacity: 1; transform: translateY(0); }
	}
	
	/* 加载状态 */
	.loading {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 60rpx;
		color: #94a3b8;
	}
	
	/* 空状态 */
	.empty {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 120rpx 40rpx;
		color: #94a3b8;
	}
	
	.empty-icon {
		font-size: 80rpx;
		margin-bottom: 20rpx;
	}
	
	.empty-text {
		font-size: 28rpx;
		text-align: center;
	}
</style>
