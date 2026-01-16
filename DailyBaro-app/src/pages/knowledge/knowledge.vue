<template>
	<view class="knowledge-container">
		<!-- 顶部导航栏 -->
		<view class="nav-bar">
			<view class="nav-left" @tap="goBack">
				<text class="back-icon">←</text>
				<text class="back-text">返回</text>
			</view>
			<view class="nav-title">心理健康知识库</view>
			<view class="nav-right"></view>
		</view>
		
		<view class="content">
			<!-- 搜索框 -->
			<view class="search-section">
				<view class="search-box">
					<input 
						class="search-input" 
						placeholder="搜索心理健康知识..." 
						v-model="searchKeyword"
						@confirm="performSearch"
						@input="onSearchInput"
					/>
					<view class="search-btn" @tap="performSearch">
						<text class="search-icon">🔍</text>
					</view>
				</view>
			</view>
			
			<!-- 分类标签 -->
			<view class="category-section">
				<scroll-view class="category-scroll" scroll-x>
					<view 
						class="category-tag" 
						:class="{ active: selectedCategory === category.value }"
						v-for="category in categories"
						:key="category.value"
						@tap="selectCategory(category.value)"
					>
						{{ category.label }}
					</view>
				</scroll-view>
			</view>
			
			<!-- 子分类（如果有） -->
			<view v-if="selectedCategory && subcategories[selectedCategory]" class="subcategory-section">
				<scroll-view class="subcategory-scroll" scroll-x>
					<view 
						class="subcategory-tag"
						:class="{ active: selectedSubcategory === sub.value }"
						v-for="sub in subcategories[selectedCategory]"
						:key="sub.value"
						@tap="selectSubcategory(sub.value)"
					>
						{{ sub.label }}
					</view>
				</scroll-view>
			</view>
			
			<!-- 搜索结果 -->
			<view v-if="searching" class="loading-section">
				<text class="loading-text">搜索中...</text>
			</view>
			
			<view v-else-if="searchResults && searchResults.knowledgeList && searchResults.knowledgeList.length > 0" class="results-section">
				<!-- AI回答（RAG） -->
				<view v-if="searchResults.aiAnswer" class="ai-answer-card">
					<view class="ai-answer-header">
						<text class="ai-icon">🤖</text>
						<text class="ai-title">AI智能回答</text>
					</view>
					<view class="ai-answer-content">{{ searchResults.aiAnswer }}</view>
					<view v-if="searchResults.sources && searchResults.sources.length > 0" class="ai-sources">
						<text class="sources-title">参考来源：</text>
						<text v-for="(source, index) in searchResults.sources" :key="index" class="source-tag">
							{{ source }}
						</text>
					</view>
				</view>
				
				<!-- 知识列表 -->
				<view class="knowledge-list">
					<view 
						class="knowledge-item"
						v-for="item in searchResults.knowledgeList"
						:key="item.id"
						@tap="viewKnowledgeDetail(item.id)"
					>
						<view class="item-header">
							<text class="item-title">{{ item.title }}</text>
							<text class="item-category">{{ item.category }}</text>
						</view>
						<view class="item-content">{{ item.summary || item.content }}</view>
						<view class="item-footer">
							<text class="item-tags" v-if="item.tags">{{ item.tags }}</text>
							<text class="item-views">👁 {{ item.viewCount || 0 }}</text>
						</view>
					</view>
				</view>
			</view>
			
			<view v-else-if="!searchKeyword && !selectedCategory" class="empty-section">
				<text class="empty-text">请选择分类或输入关键词搜索</text>
			</view>
			
			<view v-else class="empty-section">
				<text class="empty-text">暂无相关结果</text>
			</view>
		</view>
	</view>
</template>

<script>
import { knowledgeApi } from '@/utils/api.js'

export default {
	data() {
		return {
			searchKeyword: '',
			selectedCategory: null,
			selectedSubcategory: null,
			searching: false,
			searchResults: null,
			categories: [
				{ label: '全部', value: null },
				{ label: '情绪管理', value: '情绪管理' },
				{ label: '心理排忧', value: '心理排忧' },
				{ label: '成长指南', value: '成长指南' },
				{ label: '案例分享', value: '案例分享' }
			],
			subcategories: {
				'情绪管理': [
					{ label: '全部', value: null },
					{ label: '焦虑', value: '焦虑' },
					{ label: '抑郁', value: '抑郁' },
					{ label: '压力', value: '压力' },
					{ label: '愤怒', value: '愤怒' }
				],
				'心理排忧': [
					{ label: '全部', value: null },
					{ label: '常见问题', value: '常见问题' },
					{ label: '自我调节', value: '自我调节' }
				],
				'成长指南': [
					{ label: '全部', value: null },
					{ label: '自我认知', value: '自我认知' },
					{ label: '人际关系', value: '人际关系' }
				]
			}
		}
	},
	
	onLoad() {
		// 页面加载时可以加载默认分类的知识
	},
	
	methods: {
		goBack() {
			uni.navigateBack()
		},
		
		onSearchInput(e) {
			this.searchKeyword = e.detail.value
		},
		
		selectCategory(category) {
			this.selectedCategory = category
			this.selectedSubcategory = null
			this.loadKnowledgeByCategory()
		},
		
		selectSubcategory(subcategory) {
			this.selectedSubcategory = subcategory
			this.loadKnowledgeBySubcategory()
		},
		
		async performSearch() {
			if (!this.searchKeyword || this.searchKeyword.trim().length === 0) {
				uni.showToast({
					title: '请输入搜索关键词',
					icon: 'none'
				})
				return
			}
			
			this.searching = true
			try {
				const baseUrl = this.getBaseUrl()
				const userInfo = uni.getStorageSync('userInfo')
				const uid = userInfo ? userInfo.uid : ''
				
				const response = await uni.request({
					url: baseUrl + '/app/knowledge/search',
					method: 'POST',
					header: {
						'Content-Type': 'application/json',
						'uid': uid
					},
					data: {
						query: this.searchKeyword,
						useRAG: true,  // 使用RAG检索
						page: 1,
						size: 20
					}
				})
				
				if (response.data && response.data.code === 200) {
					this.searchResults = response.data.data
				} else {
					uni.showToast({
						title: response.data?.message || '搜索失败',
						icon: 'none'
					})
				}
			} catch (error) {
				console.error('搜索失败:', error)
				uni.showToast({
					title: '搜索失败，请稍后重试',
					icon: 'none'
				})
			} finally {
				this.searching = false
			}
		},
		
		async loadKnowledgeByCategory() {
			if (!this.selectedCategory) {
				this.searchResults = null
				return
			}
			
			this.searching = true
			try {
				const baseUrl = this.getBaseUrl()
				const userInfo = uni.getStorageSync('userInfo')
				const uid = userInfo ? userInfo.uid : ''
				
				const response = await uni.request({
					url: baseUrl + '/app/knowledge/category/' + encodeURIComponent(this.selectedCategory),
					method: 'GET',
					header: {
						'uid': uid
					}
				})
				
				if (response.data && response.data.code === 200) {
					this.searchResults = {
						knowledgeList: response.data.data,
						total: response.data.data.length
					}
				}
			} catch (error) {
				console.error('加载分类知识失败:', error)
			} finally {
				this.searching = false
			}
		},
		
		async loadKnowledgeBySubcategory() {
			if (!this.selectedCategory || !this.selectedSubcategory) {
				return
			}
			
			this.searching = true
			try {
				const baseUrl = this.getBaseUrl()
				const userInfo = uni.getStorageSync('userInfo')
				const uid = userInfo ? userInfo.uid : ''
				
				const response = await uni.request({
					url: baseUrl + '/app/knowledge/category/' + encodeURIComponent(this.selectedCategory) + 
						'/subcategory/' + encodeURIComponent(this.selectedSubcategory),
					method: 'GET',
					header: {
						'uid': uid
					}
				})
				
				if (response.data && response.data.code === 200) {
					this.searchResults = {
						knowledgeList: response.data.data,
						total: response.data.data.length
					}
				}
			} catch (error) {
				console.error('加载子分类知识失败:', error)
			} finally {
				this.searching = false
			}
		},
		
		viewKnowledgeDetail(id) {
			uni.navigateTo({
				url: '/pages/knowledge/detail?id=' + id
			})
		},
		
		getBaseUrl() {
			// 从配置中获取基础URL
			const config = require('@/utils/config.js')
			return config.baseUrl || 'https://dailybaro.cn'
		}
	}
}
</script>

<style scoped>
.knowledge-container {
	min-height: 100vh;
	background: #f5f7fa;
}

.nav-bar {
	background: white;
	padding: 30rpx 40rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	border-bottom: 1rpx solid #e5e7eb;
	position: sticky;
	top: 0;
	z-index: 100;
}

.nav-left {
	display: flex;
	align-items: center;
	gap: 10rpx;
}

.back-icon {
	font-size: 36rpx;
	color: #6366f1;
}

.back-text {
	font-size: 28rpx;
	color: #6366f1;
}

.nav-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #1f2937;
}

.content {
	padding: 30rpx;
}

.search-section {
	margin-bottom: 30rpx;
}

.search-box {
	display: flex;
	align-items: center;
	background: white;
	border-radius: 24rpx;
	padding: 20rpx 30rpx;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.search-input {
	flex: 1;
	font-size: 28rpx;
	color: #1f2937;
}

.search-btn {
	padding: 10rpx;
}

.search-icon {
	font-size: 32rpx;
}

.category-section {
	margin-bottom: 20rpx;
}

.category-scroll {
	white-space: nowrap;
}

.category-tag {
	display: inline-block;
	padding: 16rpx 32rpx;
	margin-right: 20rpx;
	background: white;
	border-radius: 40rpx;
	font-size: 26rpx;
	color: #6b7280;
	border: 2rpx solid #e5e7eb;
}

.category-tag.active {
	background: #6366f1;
	color: white;
	border-color: #6366f1;
}

.subcategory-section {
	margin-bottom: 30rpx;
}

.subcategory-scroll {
	white-space: nowrap;
}

.subcategory-tag {
	display: inline-block;
	padding: 12rpx 24rpx;
	margin-right: 15rpx;
	background: white;
	border-radius: 30rpx;
	font-size: 24rpx;
	color: #6b7280;
	border: 1rpx solid #e5e7eb;
}

.subcategory-tag.active {
	background: #e0e7ff;
	color: #6366f1;
	border-color: #6366f1;
}

.loading-section {
	text-align: center;
	padding: 100rpx 0;
}

.loading-text {
	font-size: 28rpx;
	color: #9ca3af;
}

.ai-answer-card {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
	color: white;
}

.ai-answer-header {
	display: flex;
	align-items: center;
	gap: 15rpx;
	margin-bottom: 20rpx;
}

.ai-icon {
	font-size: 32rpx;
}

.ai-title {
	font-size: 30rpx;
	font-weight: 600;
}

.ai-answer-content {
	font-size: 28rpx;
	line-height: 1.8;
	margin-bottom: 20rpx;
}

.ai-sources {
	display: flex;
	flex-wrap: wrap;
	gap: 10rpx;
	align-items: center;
}

.sources-title {
	font-size: 24rpx;
	opacity: 0.9;
}

.source-tag {
	font-size: 22rpx;
	padding: 6rpx 12rpx;
	background: rgba(255, 255, 255, 0.2);
	border-radius: 12rpx;
}

.knowledge-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.knowledge-item {
	background: white;
	border-radius: 16rpx;
	padding: 30rpx;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.item-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 15rpx;
}

.item-title {
	font-size: 30rpx;
	font-weight: 600;
	color: #1f2937;
	flex: 1;
}

.item-category {
	font-size: 22rpx;
	padding: 6rpx 12rpx;
	background: #e0e7ff;
	color: #6366f1;
	border-radius: 12rpx;
}

.item-content {
	font-size: 26rpx;
	color: #6b7280;
	line-height: 1.6;
	margin-bottom: 15rpx;
}

.item-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.item-tags {
	font-size: 22rpx;
	color: #9ca3af;
}

.item-views {
	font-size: 22rpx;
	color: #9ca3af;
}

.empty-section {
	text-align: center;
	padding: 100rpx 0;
}

.empty-text {
	font-size: 28rpx;
	color: #9ca3af;
}
</style>
