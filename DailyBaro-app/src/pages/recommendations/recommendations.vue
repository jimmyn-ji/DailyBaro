<template>
	<view class="container">
		<view class="nav">
			<button class="back" @tap="goBack">返回</button>
			<text class="title">个性化推荐</text>
		</view>
		<view class="content">
			<button class="refresh" @tap="load" :disabled="loading">{{ loading ? '加载中...' : '刷新推荐' }}</button>
			<view v-if="data">
				<view class="section">
					<text class="label">趋势主情绪：</text>
					<text class="value">{{ data.dominantEmotion }}</text>
				</view>
				<view v-if="data.advice" class="section">
					<text class="label">建议：</text>
					<text class="value">{{ data.advice.tip }}</text>
				</view>
				<view class="list">
					<view v-for="(item, idx) in data.items" :key="idx" class="card">
						<text class="card-title">{{ item.title }}</text>
						<text class="card-sub">消耗能量：{{ item.energyRequired }}</text>
					</view>
				</view>
			</view>
			<view v-else class="empty">暂无推荐</view>
		</view>
	</view>
</template>

<script>
import { recommendationApi } from '../../utils/api.js'

export default {
	data() {
		return { loading: false, data: null }
	},
	onLoad() {
		this.load()
	},
	methods: {
		goBack() { uni.navigateBack() },
		async load() {
			this.loading = true
			try {
				const res = await recommendationApi.get()
				if (res && res.code === 200) {
					this.data = res.data || null
				} else {
					uni.showToast({ title: res?.message || '加载失败', icon: 'none' })
				}
			} catch (e) {
				console.error('加载推荐失败', e)
				uni.showToast({ title: '加载失败', icon: 'none' })
			} finally {
				this.loading = false
			}
		}
	}
}
</script>

<style scoped>
.container { min-height: 100vh; background: #f7f7fb; }
.nav { display: flex; align-items: center; justify-content: space-between; padding: 24rpx; background: #fff; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04); }
.title { font-size: 32rpx; font-weight: 700; color: #111827; }
.back { background: #e5e7eb; border: none; padding: 12rpx 20rpx; border-radius: 12rpx; }
.content { padding: 24rpx; }
.refresh { width: 100%; background: linear-gradient(135deg, #4a90e2, #6aa8ff); color: #fff; border: none; padding: 24rpx; border-radius: 16rpx; }
.section { margin: 20rpx 0; }
.label { color: #6b7280; font-size: 26rpx; }
.value { color: #111827; font-size: 28rpx; font-weight: 600; }
.list { display: flex; flex-direction: column; gap: 16rpx; margin-top: 16rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); }
.card-title { font-size: 28rpx; font-weight: 700; color: #111827; display: block; margin-bottom: 6rpx; }
.card-sub { font-size: 24rpx; color: #9ca3af; }
.empty { text-align: center; color: #9ca3af; padding: 60rpx 0; }
</style>
