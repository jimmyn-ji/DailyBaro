<template>
	<view class="visualization-container">
		<view class="content">
			<!-- 顶部介绍 -->
			<view class="intro-section">
				<view class="intro-card">
					<view class="intro-icon">📊</view>
					<text class="intro-title">情绪可视化</text>
					<text class="intro-desc">通过图表直观了解你的情绪变化趋势</text>
				</view>
			</view>
			
			<!-- 时间范围选择 -->
			<view class="time-range-section">
				<view class="section-title">选择时间范围</view>
				<view class="time-buttons">
					<button 
						v-for="range in timeRanges" 
						:key="range.value"
						class="time-btn"
						:class="{ active: selectedRange === range.value }"
						@tap="selectTimeRange(range.value)"
					>
						{{ range.label }}
					</button>
				</view>
			</view>
			
			<!-- 情绪波动折线图 -->
			<view class="chart-section">
				<view class="section-title">情绪波动趋势</view>
				<view class="chart-container">
					<view class="chart-placeholder">
						<text class="chart-icon">📈</text>
						<text class="chart-text">情绪波动折线图</text>
						<text class="chart-desc">显示选定时间范围内的情绪变化</text>
					</view>
				</view>
			</view>
			
			<!-- 情绪占比饼图 -->
			<view class="chart-section">
				<view class="section-title">情绪类型占比</view>
				<view class="chart-container">
					<view class="chart-placeholder">
						<text class="chart-icon">🥧</text>
						<text class="chart-text">情绪占比饼图</text>
						<text class="chart-desc">展示各种情绪类型的分布情况</text>
					</view>
				</view>
			</view>
			
			<!-- 文字分析 -->
			<view class="analysis-section">
				<view class="section-title">智能分析报告</view>
				<view class="analysis-card">
					<text class="analysis-content">{{ analysisReport || '点击生成按钮获取智能分析报告' }}</text>
					<button class="generate-btn" @tap="generateReport">生成分析报告</button>
				</view>
			</view>
			
			<!-- 导出功能 -->
			<view class="export-section">
				<view class="section-title">导出报告</view>
				<view class="export-buttons">
					<button class="export-btn pdf" @tap="exportPDF">导出PDF</button>
					<button class="export-btn image" @tap="exportImage">导出图片</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { emotionApi } from '@/utils/api.js'

export default {
	data() {
		return {
			selectedRange: 'week',
			timeRanges: [
				{ label: '一周', value: 'week' },
				{ label: '一月', value: 'month' },
				{ label: '三月', value: 'quarter' },
				{ label: '半年', value: 'half_year' }
			],
			analysisReport: '',
			chartData: null
		}
	},
	
	onLoad() {
		this.loadVisualizationData()
	},
	
	methods: {
		selectTimeRange(range) {
			this.selectedRange = range
			this.loadVisualizationData()
		},
		
		async loadVisualizationData() {
			try {
				uni.showLoading({ title: '加载中...' })
				
				// 获取折线图数据
				const lineRes = await emotionApi.getVisualization({
					timeRange: this.selectedRange,
					type: 'line'
				})
				
				// 获取饼图数据
				const pieRes = await emotionApi.getStatistics({
					timeRange: this.selectedRange,
					type: 'pie'
				})
				
				uni.hideLoading()
				
				if (lineRes.code === 200 && pieRes.code === 200) {
					this.chartData = {
						line: lineRes.data,
						pie: pieRes.data
					}
					// 这里可以调用图表库渲染图表
					this.renderCharts()
				}
			} catch (error) {
				uni.hideLoading()
				uni.showToast({
					title: '加载数据失败',
					icon: 'none'
				})
			}
		},
		
		renderCharts() {
			// 这里集成图表库，如ECharts、Chart.js等
			// 由于小程序限制，可能需要使用专门的图表组件
			console.log('渲染图表数据:', this.chartData)
		},
		
		async generateReport() {
			try {
				uni.showLoading({ title: '生成中...' })
				
				const res = await emotionApi.exportReport({
					timeRange: this.selectedRange,
					includeCharts: true,
					includeAnalysis: true
				})
				
				uni.hideLoading()
				
				if (res.code === 200) {
					this.analysisReport = res.data.analysis || '基于你的情绪数据，AI生成了详细的分析报告。'
					uni.showToast({
						title: '报告生成成功',
						icon: 'success'
					})
				} else {
					uni.showToast({
						title: res.message || '生成失败',
						icon: 'none'
					})
				}
			} catch (error) {
				uni.hideLoading()
				uni.showToast({
					title: '生成失败，请稍后重试',
					icon: 'none'
				})
			}
		},
		
		async exportPDF() {
			try {
				uni.showLoading({ title: '导出中...' })
				
				const res = await emotionApi.exportReport({
					timeRange: this.selectedRange,
					format: 'pdf',
					includeCharts: true,
					includeAnalysis: true
				})
				
				uni.hideLoading()
				
				if (res.code === 200) {
					uni.showToast({
						title: 'PDF导出成功',
						icon: 'success'
					})
					// 这里可以处理文件下载
				} else {
					uni.showToast({
						title: res.message || '导出失败',
						icon: 'none'
					})
				}
			} catch (error) {
				uni.hideLoading()
				uni.showToast({
					title: '导出失败，请稍后重试',
					icon: 'none'
				})
			}
		},
		
		async exportImage() {
			try {
				uni.showLoading({ title: '导出中...' })
				
				const res = await emotionApi.exportReport({
					timeRange: this.selectedRange,
					format: 'image',
					includeCharts: true,
					includeAnalysis: true
				})
				
				uni.hideLoading()
				
				if (res.code === 200) {
					uni.showToast({
						title: '图片导出成功',
						icon: 'success'
					})
					// 这里可以处理图片保存
				} else {
					uni.showToast({
						title: res.message || '导出失败',
						icon: 'none'
					})
				}
			} catch (error) {
				uni.hideLoading()
				uni.showToast({
					title: '导出失败，请稍后重试',
					icon: 'none'
				})
			}
		}
	}
}
</script>

<style scoped>
.visualization-container {
	min-height: 100vh;
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
}

.content {
	padding: 40rpx 30rpx;
}

/* 顶部介绍 */
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

/* 时间范围选择 */
.time-range-section {
	margin-bottom: 30rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: 700;
	color: #ffffff;
	margin-bottom: 20rpx;
	text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.3);
}

.time-buttons {
	display: flex;
	gap: 15rpx;
	flex-wrap: wrap;
}

.time-btn {
	background: rgba(255, 255, 255, 0.2);
	border: 1rpx solid rgba(255, 255, 255, 0.3);
	border-radius: 20rpx;
	padding: 12rpx 24rpx;
	color: #ffffff;
	font-size: 24rpx;
	transition: all 0.3s ease;
}

.time-btn.active {
	background: rgba(255, 255, 255, 0.4);
	border-color: #ffffff;
	transform: scale(1.05);
}

/* 图表区域 */
.chart-section {
	margin-bottom: 30rpx;
}

.chart-container {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 20rpx;
	padding: 40rpx;
	box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.chart-placeholder {
	text-align: center;
	padding: 60rpx 0;
}

.chart-icon {
	font-size: 80rpx;
	margin-bottom: 20rpx;
	display: block;
	opacity: 0.6;
}

.chart-text {
	font-size: 28rpx;
	font-weight: 600;
	color: #1f2937;
	margin-bottom: 10rpx;
	display: block;
}

.chart-desc {
	font-size: 24rpx;
	color: #94a3b8;
	display: block;
}

/* 分析报告 */
.analysis-section {
	margin-bottom: 30rpx;
}

.analysis-card {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 20rpx;
	padding: 30rpx;
	box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.analysis-content {
	font-size: 26rpx;
	color: #4b5563;
	line-height: 1.6;
	margin-bottom: 20rpx;
	display: block;
	min-height: 120rpx;
}

.generate-btn {
	background: linear-gradient(135deg, #4a90e2 0%, #6aa8ff 100%);
	color: #ffffff;
	border: none;
	border-radius: 16rpx;
	padding: 20rpx 40rpx;
	font-size: 28rpx;
	font-weight: 600;
	box-shadow: 0 4rpx 16rpx rgba(99, 102, 241, 0.3);
}

/* 导出功能 */
.export-section {
	margin-bottom: 30rpx;
}

.export-buttons {
	display: flex;
	gap: 20rpx;
}

.export-btn {
	flex: 1;
	border: none;
	border-radius: 16rpx;
	padding: 20rpx;
	font-size: 26rpx;
	font-weight: 600;
	color: #ffffff;
	transition: all 0.3s ease;
}

.export-btn.pdf {
	background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
	box-shadow: 0 4rpx 16rpx rgba(239, 68, 68, 0.3);
}

.export-btn.image {
	background: linear-gradient(135deg, #10b981 0%, #059669 100%);
	box-shadow: 0 4rpx 16rpx rgba(16, 185, 129, 0.3);
}

.export-btn:active {
	transform: translateY(2rpx);
}
</style>
