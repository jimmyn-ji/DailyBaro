<template>
	<view class="content">
		<view class="welcome-section">
			<view class="logo-container">
				<view class="logo-wrapper">
					<view class="diary-binding"></view>
					<image class="logo" src="/static/logo.jpg" mode="aspectFit"></image>
				</view>
			</view>
			<view class="text-area">
				<text class="title">Welcome to DailyBaro</text>
				<text class="subtitle">记录心情，遇见更好的自己</text>
			</view>
			<view class="action-buttons">
				<button class="action-btn login" @tap="goToLogin">登录</button>
				<button class="action-btn register" @tap="goToRegister">注册</button>
			</view>
		</view>
	</view>
</template>

<script setup>
const goToLogin = () => {
	uni.navigateTo({
		url: '/pages/login/login'
	})
}

const goToRegister = () => {
	uni.navigateTo({
		url: '/pages/register/register'
	})
}
</script>

<style scoped>
.content {
	min-height: 100vh;
	/* 首页背景使用与登录页同系列的蓝紫渐变 */
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 40%, #8fd3f4 100%);
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 40rpx;
}

.welcome-section {
	text-align: center;
	background: rgba(255, 255, 255, 0.1);
	backdrop-filter: blur(20rpx);
	border-radius: 32rpx;
	padding: 60rpx 40rpx;
	border: 1rpx solid rgba(255, 255, 255, 0.2);
	box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.15);
}

.logo-container {
	margin-bottom: 40rpx;
}

.logo-wrapper {
	position: relative;
	display: inline-block;
}

.logo {
	height: 204rpx;
	width: 204rpx;
	border-radius: 50rpx;
	/* 日记本样式边框 */
	border: 4rpx solid rgba(255, 255, 255, 0.6);
	box-shadow: 
		0 0 0 1rpx rgba(139, 69, 19, 0.15),
		0 8rpx 32rpx rgba(0, 0, 0, 0.2),
		inset 0 0 20rpx rgba(255, 255, 255, 0.05);
	display: block;
	position: relative;
	z-index: 1;
}

/* 日记本装订线效果 */
.diary-binding {
	position: absolute;
	left: 0;
	top: 0;
	width: 12rpx;
	height: 100%;
	background: linear-gradient(to right,
		rgba(139, 69, 19, 0.4) 0%,
		rgba(139, 69, 19, 0.2) 50%,
		rgba(139, 69, 19, 0.4) 100%
	);
	border-radius: 50rpx 0 0 50rpx;
	z-index: 2;
	pointer-events: none;
}

.text-area {
	margin-bottom: 50rpx;
}

.title {
	font-size: 48rpx;
	font-weight: 700;
	color: #ffffff;
	display: block;
	margin-bottom: 20rpx;
	text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.3);
}

.subtitle {
	font-size: 28rpx;
	color: rgba(255, 255, 255, 0.9);
	display: block;
	line-height: 1.5;
}

.action-buttons {
	display: flex;
	gap: 20rpx;
	justify-content: center;
}

.action-btn {
	padding: 24rpx 40rpx;
	border-radius: 16rpx;
	font-size: 28rpx;
	font-weight: 600;
	border: none;
	transition: all 0.3s ease;
	min-width: 160rpx;
}

.action-btn.login {
	background: rgba(255, 255, 255, 0.2);
	color: #ffffff;
	border: 2rpx solid rgba(255, 255, 255, 0.3);
}

.action-btn.login:active {
	background: rgba(255, 255, 255, 0.3);
	transform: translateY(2rpx);
}

.action-btn.register {
	background: #ffffff;
	color: #4a90e2;
	box-shadow: 0 8rpx 24rpx rgba(255, 255, 255, 0.3);
}

.action-btn.register:active {
	transform: translateY(2rpx);
	box-shadow: 0 4rpx 12rpx rgba(255, 255, 255, 0.4);
}
</style>
