<template>
	<view class="login-container">
		<!-- 使用 image 作为全屏背景，微信小程序不支持本地图片的 WXSS background-image -->
		<image class="bg-image" src="/static/appbg.jpg" mode="aspectFill"></image>
		<view class="login-content">
			<view class="logo-section">
				<view class="logo-wrapper">
					<view class="diary-binding"></view>
					<image class="logo-icon" src="/static/logo.jpg" mode="aspectFit"></image>
				</view>
				<text class="app-name">DailyBaro</text>
				<text class="app-slogan">记录心情，遇见更好的自己</text>
			</view>

			<!-- 登录表单 -->
			<view class="form-section">
				<view class="input-group">
					<input
						class="input-field"
						:class="{ 'focused': usernameFocused }"
						type="text"
						placeholder="请输入用户名"
						v-model="form.username"
						maxlength="20"
						@focus="onUsernameFocus"
						@blur="onUsernameBlur"
					/>
				</view>

				<view class="input-group">
					<input
						class="input-field"
						:class="{ 'focused': passwordFocused }"
						type="password"
						placeholder="请输入密码"
						v-model="form.password"
						maxlength="20"
						@focus="onPasswordFocus"
						@blur="onPasswordBlur"
					/>
				</view>

				<button class="login-btn" @tap="login" :disabled="loading">
					{{ loading ? '登录中...' : '登录' }}
				</button>

				<!-- 微信一键登录文字链接 -->
				<view class="wechat-login-section">
					<text class="wechat-login-text" @tap="wechatLogin" :class="{ 'disabled': loading }">微信一键登录</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { userApi } from '../../utils/api.js'

export default {
	data() {
		return {
			form: {
				username: '',
				password: ''
			},
			loading: false,
			usernameFocused: false,
			passwordFocused: false
		}
	},
	
	methods: {
		onUsernameFocus() {
			this.usernameFocused = true
		},
		
		onUsernameBlur() {
			this.usernameFocused = false
		},
		
		onPasswordFocus() {
			this.passwordFocused = true
		},
		
		onPasswordBlur() {
			this.passwordFocused = false
		},
		
		async login() {
			if (!this.form.username || !this.form.password) {
				uni.showToast({
					title: '请输入用户名和密码',
					icon: 'none'
				})
				return
			}
			
			if (this.loading) return
			this.loading = true
			
			try {
				uni.showLoading({ title: '登录中...' })
				
				console.log('登录请求参数:', {
					account: this.form.username,
					password: this.form.password
				})
				
				const response = await userApi.login({
					account: this.form.username,
					password: this.form.password
				})
				
				console.log('登录响应:', response)
				
				if (response && response.code === 200) {
					// 保存用户信息
					uni.setStorageSync('userInfo', response.data)
					
					const reward = (response.data && response.data.energyReward) ? response.data.energyReward : 0
					uni.showToast({
						title: reward > 0 ? `登录成功 · 今日能量+${reward}` : '登录成功',
						icon: 'success'
					})
					
					// 跳转到主页面
					setTimeout(() => {
						uni.switchTab({
							url: '/pages/diary/diary'
						})
					}, 1500)
				} else {
					uni.showToast({
						title: response?.message || '登录失败',
						icon: 'none'
					})
				}
			} catch (error) {
				console.error('登录失败:', error)
				uni.showToast({
					title: '登录失败，请重试',
					icon: 'none'
				})
			} finally {
				uni.hideLoading()
				this.loading = false
			}
		},
		
		async wechatLogin() {
			if (this.loading) return
			this.loading = true
			
			try {
				uni.showLoading({ title: '微信登录中...' })
				
				// 1. 获取微信登录凭证
				// 在微信小程序中，uni.login 会自动使用微信登录，不需要指定 provider
				console.log('开始获取微信登录凭证...')
				const loginResult = await new Promise((resolve, reject) => {
					uni.login({
						success: (res) => {
							console.log('uni.login success:', res)
							resolve(res)
						},
						fail: (err) => {
							console.error('uni.login fail:', err)
							reject(err)
						}
					})
				})
				
				console.log('微信登录凭证:', loginResult)
				console.log('loginResult.code:', loginResult?.code)
				console.log('loginResult.code 类型:', typeof loginResult?.code)
				
				if (!loginResult || !loginResult.code) {
					uni.hideLoading()
					uni.showToast({
						title: '获取微信登录凭证失败，请重试',
						icon: 'none',
						duration: 2000
					})
					return
				}
				
				// 2. 获取用户信息（可选，不影响登录）
				// 注意：getUserProfile 在微信小程序中需要用户主动触发，这里跳过
				// 如果需要用户信息，可以在登录成功后单独获取
				
				// 3. 调用后端微信登录接口（只传递 code，后端 DTO 只需要 code）
				const requestData = {
					code: loginResult.code
				}
				
				console.log('发送微信登录请求，code:', loginResult.code)
				console.log('请求数据:', JSON.stringify(requestData))
				console.log('请求数据类型:', typeof requestData, Object.keys(requestData))
				console.log('requestData.code:', requestData.code)
				console.log('requestData.code 类型:', typeof requestData.code)
				console.log('requestData.code 值:', requestData.code)
				
				// 确保数据正确传递
				if (!requestData.code) {
					uni.hideLoading()
					uni.showToast({
						title: '微信登录code为空，请重试',
						icon: 'none',
						duration: 2000
					})
					return
				}
				
				const response = await userApi.wechatLogin(requestData)
				
				console.log('微信登录响应:', response)
				uni.hideLoading()
				
				if (response && response.code === 200) {
					// 保存用户信息
					uni.setStorageSync('userInfo', response.data)
					
					const reward = (response.data && response.data.energyReward) ? response.data.energyReward : 0
					uni.showToast({
						title: reward > 0 ? `登录成功 · 今日能量+${reward}` : '登录成功',
						icon: 'success'
					})
					
					// 跳转到主页面
					setTimeout(() => {
						uni.switchTab({
							url: '/pages/diary/diary'
						})
					}, 1500)
				} else {
					// 根据后端返回的错误信息显示提示
					let errorMessage = '微信登录失败'
					if (response && response.message) {
						if (response.message === 'WECHAT_LOGIN_NOT_IMPLEMENTED') {
							errorMessage = '微信登录功能暂未开放，请使用账号密码登录'
						} else if (response.message.includes('缺少授权码')) {
							errorMessage = '微信授权码获取失败，请重试'
						} else {
							errorMessage = response.message
						}
					}
					
					uni.showToast({
						title: errorMessage,
						icon: 'none',
						duration: 2000
					})
				}
			} catch (error) {
				console.error('微信登录失败:', error)
				uni.hideLoading()
				
				// 根据错误类型显示不同的提示
				let errorMessage = '微信登录失败，请重试'
				if (error.message) {
					errorMessage = error.message
				} else if (error.errMsg) {
					if (error.errMsg.includes('getUserProfile:fail')) {
						errorMessage = '需要授权获取用户信息才能登录'
					} else if (error.errMsg.includes('login:fail')) {
						errorMessage = '微信登录失败，请检查网络连接'
					}
				}
				
				uni.showToast({
					title: errorMessage,
					icon: 'none',
					duration: 2000
				})
			} finally {
				this.loading = false
			}
		}
	}
}
</script>

<style scoped>
.login-container {
	min-height: 100vh;
	position: relative;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 0 60rpx;
	box-sizing: border-box;
	animation: fadeIn 0.8s ease-out;
	background-color: #ffffff;
}

.bg-image {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	z-index: 0;
	pointer-events: none;
}

@keyframes fadeIn {
	from {
		opacity: 0;
		transform: translateY(30rpx);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

.login-content {
	width: 100%;
	max-width: 600rpx;
	position: relative;
	z-index: 1;
	animation: slideUp 0.3s ease-out 0.1s both;
}

@keyframes slideUp {
	from {
		opacity: 0;
		transform: translateY(20rpx);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

.logo-section {
	text-align: center;
	margin-bottom: 80rpx;
	animation: fadeIn 0.4s ease-out 0.2s both;
}

@keyframes fadeIn {
	from {
		opacity: 0;
	}
	to {
		opacity: 1;
	}
}

.logo-wrapper {
	position: relative;
	display: inline-block;
	margin-bottom: 24rpx;
	/* 移除浮动动画，弱化效果 */
}

.logo-icon {
	width: 170rpx;
	height: 170rpx;
	border-radius: 50rpx;
	/* 日记本样式边框 */
	border: 4rpx solid rgba(255, 255, 255, 0.6);
	box-shadow: 
		0 0 0 1rpx rgba(139, 69, 19, 0.15),
		0 8rpx 24rpx rgba(0, 0, 0, 0.15),
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

@keyframes float {
	0%, 100% {
		transform: translateY(0);
	}
	50% {
		transform: translateY(-10rpx);
	}
}

.app-name {
	font-size: 52rpx;
	font-weight: 700;
	color: #ffffff;
	display: block;
	margin-bottom: 12rpx;
}

.app-slogan {
	font-size: 30rpx;
	color: rgba(255, 255, 255, 0.8);
	display: block;
}

.form-section {
	background: rgba(255, 255, 255, 0.98);
	border-radius: 24rpx;
	padding: 60rpx 50rpx;
	box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.08);
	backdrop-filter: blur(5rpx);
	animation: slideInUp 0.3s ease-out 0.3s both;
}

@keyframes slideInUp {
	from {
		opacity: 0;
		transform: translateY(20rpx);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

.input-group {
	margin-bottom: 32rpx;
}

.input-field {
	width: 100%;
	background: #f8fafc;
	border: 2rpx solid #e2e8f0;
	border-radius: 14rpx;
	padding: 24rpx 20rpx;
	font-size: 30rpx;
	color: #1f2937;
	transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	box-sizing: border-box;
	line-height: 1.2;
	height: 80rpx;
	display: flex;
	align-items: center;
}

.input-field::placeholder {
	color: #9ca3af;
	font-size: 30rpx;
	line-height: 1.2;
}

.input-field.focused {
	border-color: #4a90e2;
	background: #ffffff;
	box-shadow: 0 0 0 4rpx rgba(99, 102, 241, 0.15);
	transform: translateY(-2rpx);
}

.login-btn {
	width: 100%;
	/* 按钮背景色改为与背景图接近的蓝色 */
	background: #4a90e2;
	color: #ffffff;
	border: none;
	border-radius: 12rpx;
	padding: 18rpx;
	font-size: 26rpx;
	font-weight: 600;
	margin-bottom: 36rpx;
	box-shadow: 0 6rpx 20rpx rgba(99, 102, 241, 0.25);
	transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	position: relative;
	overflow: hidden;
}

.login-btn:disabled {
	opacity: 0.6;
	transform: none;
}

.login-btn::before {
	content: '';
	position: absolute;
	top: 0;
	left: -100%;
	width: 100%;
	height: 100%;
	background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
	transition: left 0.5s;
}

.login-btn:active:not(:disabled) {
	transform: translateY(2rpx);
	box-shadow: 0 3rpx 10rpx rgba(99, 102, 241, 0.35);
}

.login-btn:active:not(:disabled)::before {
	left: 100%;
}

.wechat-login-section {
	text-align: center;
	animation: fadeIn 0.3s ease-out 0.4s both;
}

.wechat-login-text {
	color: #10b981;
	font-size: 28rpx;
	font-weight: 500;
	text-decoration: underline;
	transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	position: relative;
}

.wechat-login-text::after {
	content: '';
	position: absolute;
	bottom: -2rpx;
	left: 0;
	width: 0;
	height: 2rpx;
	background: #10b981;
	transition: width 0.3s ease;
}

.wechat-login-text:active:not(.disabled) {
	color: #059669;
	transform: scale(0.98);
}

.wechat-login-text:active:not(.disabled)::after {
	width: 100%;
}

.wechat-login-text.disabled {
	opacity: 0.6;
	pointer-events: none;
}
</style>
