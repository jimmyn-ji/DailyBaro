<template>
	<view class="register-container">
		<view class="header">
			<view class="back-btn" @tap="goBack">←</view>
			<text class="header-title">创建账号</text>
		</view>
		
		<view class="form-card">
			<view class="input-group">
				<input class="input-field" type="text" placeholder="请输入账号" v-model="form.account" />
			</view>
			
			<view class="input-group">
				<input class="input-field" :type="showPassword ? 'text' : 'password'" placeholder="请输入密码" v-model="form.password" />
			</view>
			
			<view class="input-group">
				<input class="input-field" :type="showConfirmPassword ? 'text' : 'password'" placeholder="请确认密码" v-model="form.confirmPassword" />
			</view>
			
			<view class="input-group">
				<input class="input-field" type="number" placeholder="请输入手机号（可选）" v-model="form.phone" />
			</view>
			
			<view class="input-group">
				<input class="input-field" type="text" placeholder="请输入邮箱（可选）" v-model="form.email" />
			</view>
			
			<button class="register-btn" :disabled="loading" @tap="handleRegister">
				{{ loading ? '注册中...' : '注册' }}
			</button>
			
			<view class="login-link">
				<text @tap="goLogin">已有账号？立即登录</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { userApi } from '../../utils/api.js'

const form = ref({
	account: '',
	password: '',
	confirmPassword: '',
	phone: '',
	email: ''
})
const loading = ref(false)
const showPassword = ref(false)
const goBack = () => {
	uni.navigateBack()
}

const handleRegister = async () => {
	if (!form.value.account || !form.value.password || !form.value.confirmPassword) {
		uni.showToast({
			title: '请填写完整信息',
			icon: 'none'
		})
		return
	}
	
	if (form.value.password !== form.value.confirmPassword) {
		uni.showToast({
			title: '两次密码输入不一致',
			icon: 'none'
		})
		return
	}
	
	loading.value = true
	
			try {
			const response = await userApi.register({
				account: form.value.account,
				password: form.value.password,
				confirmPassword: form.value.confirmPassword,
				phone: form.value.phone || '',
				email: form.value.email || ''
			})
			
			if (response && response.code === 200) {
				// 保存用户信息
				const userInfo = {
					uid: response.data.uid,
					account: response.data.account,
					energyReward: response.data.energyReward || 0
				}
				
				// 保存到本地存储
				uni.setStorageSync('userInfo', userInfo)
				
				uni.showToast({
					title: '注册成功',
					icon: 'success'
				})
				
				// 跳转到日记页面
				setTimeout(() => {
					uni.reLaunch({
						url: '/pages/diary/diary'
					})
				}, 1500)
			} else {
				uni.showToast({
					title: response?.message || '注册失败',
					icon: 'none'
				})
			}
		} catch (error) {
			console.error('注册失败:', error)
			uni.showToast({
				title: error.message || '注册失败，请重试',
				icon: 'none'
			})
		} finally {
			loading.value = false
		}
}

const goLogin = () => {
	uni.navigateBack()
}
</script>

<style scoped>
.register-container {
	min-height: 100vh;
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
	padding: 40rpx;
}

.header {
	display: flex;
	align-items: center;
	margin-bottom: 60rpx;
}

.back-btn {
	width: 60rpx;
	height: 60rpx;
	background: rgba(255, 255, 255, 0.2);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: 20rpx;
	color: #ffffff;
	font-size: 32rpx;
}

.header-title {
	font-size: 36rpx;
	font-weight: 600;
	color: #ffffff;
}

.form-card {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 24rpx;
	padding: 50rpx 40rpx;
	box-shadow: 0 20rpx 40rpx rgba(0, 0, 0, 0.1);
}

.input-group {
	margin-bottom: 25rpx;
}

.input-field {
	width: 100%;
	height: 80rpx;
	background: #f8fafc;
	border: 2rpx solid #e2e8f0;
	border-radius: 16rpx;
	padding: 0 20rpx;
	font-size: 28rpx;
	transition: all 0.3s ease;
}

.input-field:focus {
	border-color: #4a90e2;
	background: #ffffff;
}

.register-btn {
	width: 100%;
	height: 88rpx;
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
	border: none;
	border-radius: 16rpx;
	color: #ffffff;
	font-size: 32rpx;
	font-weight: 600;
	margin-top: 40rpx;
}

.register-btn:disabled {
	background: #94a3b8;
}

.login-link {
	text-align: center;
	margin-top: 40rpx;
	color: #4a90e2;
	font-size: 28rpx;
}
</style>
