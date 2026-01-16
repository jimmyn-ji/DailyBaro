<template>
  <div class="knowledge-page">
    <div class="knowledge-header">
      <h2>心理健康知识库</h2>
      <p class="subtitle">专业的心理健康知识，助您更好地管理情绪</p>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索心理健康知识..."
        class="search-input"
        @keyup.enter="performSearch"
      >
        <template #append>
          <el-button @click="performSearch" :loading="searching">搜索</el-button>
        </template>
      </el-input>
      
      <div class="search-options">
        <el-checkbox v-model="useRAG">使用AI智能检索</el-checkbox>
      </div>
    </div>

    <!-- 分类标签 -->
    <div class="category-section">
      <div class="category-tags">
        <el-tag
          v-for="category in categories"
          :key="category.value"
          :type="selectedCategory === category.value ? 'primary' : 'info'"
          class="category-tag"
          @click="selectCategory(category.value)"
          :effect="selectedCategory === category.value ? 'dark' : 'plain'"
        >
          {{ category.label }}
        </el-tag>
      </div>
    </div>

    <!-- 子分类（如果有） -->
    <div v-if="selectedCategory && subcategories[selectedCategory]" class="subcategory-section">
      <div class="subcategory-tags">
        <el-tag
          v-for="sub in subcategories[selectedCategory]"
          :key="sub.value"
          :type="selectedSubcategory === sub.value ? 'primary' : 'info'"
          class="subcategory-tag"
          @click="selectSubcategory(sub.value)"
          :effect="selectedSubcategory === sub.value ? 'dark' : 'plain'"
        >
          {{ sub.label }}
        </el-tag>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="searching" class="loading-section">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- AI回答（RAG） -->
    <div v-if="!searching && searchResults && searchResults.aiAnswer" class="ai-answer-card">
      <div class="ai-answer-header">
        <el-icon><Search /></el-icon>
        <span class="ai-title">AI智能回答</span>
      </div>
      <div class="ai-answer-content">{{ searchResults.aiAnswer }}</div>
      <div v-if="searchResults.sources && searchResults.sources.length > 0" class="ai-sources">
        <span class="sources-title">参考来源：</span>
        <el-tag
          v-for="(source, index) in searchResults.sources"
          :key="index"
          size="small"
          class="source-tag"
        >
          {{ source }}
        </el-tag>
      </div>
    </div>

    <!-- 知识列表 -->
    <div v-if="!searching && searchResults && searchResults.knowledgeList && searchResults.knowledgeList.length > 0" class="knowledge-list">
      <el-card
        v-for="item in searchResults.knowledgeList"
        :key="item.id"
        class="knowledge-card"
        @click="viewKnowledgeDetail(item.id)"
        shadow="hover"
      >
        <div class="card-header">
          <h3 class="card-title">{{ item.title }}</h3>
          <el-tag size="small" type="primary">{{ item.category }}</el-tag>
        </div>
        <div class="card-content">{{ item.summary || item.content }}</div>
        <div class="card-footer">
          <span v-if="item.tags" class="card-tags">{{ item.tags }}</span>
          <span class="card-views">
            <el-icon><View /></el-icon>
            {{ item.viewCount || 0 }}
          </span>
        </div>
      </el-card>
    </div>

    <!-- 空状态 -->
    <el-empty
      v-if="!searching && (!searchResults || !searchResults.knowledgeList || searchResults.knowledgeList.length === 0)"
      description="暂无相关结果"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, View } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

const searchKeyword = ref('')
const searching = ref(false)
const useRAG = ref(true)
const selectedCategory = ref(null)
const selectedSubcategory = ref(null)
const searchResults = ref(null)

const categories = [
  { label: '全部', value: null },
  { label: '情绪管理', value: '情绪管理' },
  { label: '心理排忧', value: '心理排忧' },
  { label: '成长指南', value: '成长指南' },
  { label: '案例分享', value: '案例分享' }
]

const subcategories = {
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

const performSearch = async () => {
  if (!searchKeyword.value || searchKeyword.value.trim().length === 0) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  searching.value = true
  try {
    const response = await request.post('/api/knowledge/search', {
      query: searchKeyword.value,
      useRAG: useRAG.value,
      page: 1,
      size: 20
    })

    if (response.data.code === 200) {
      searchResults.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '搜索失败')
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请稍后重试')
  } finally {
    searching.value = false
  }
}

const selectCategory = async (category) => {
  selectedCategory.value = category
  selectedSubcategory.value = null
  await loadKnowledgeByCategory()
}

const selectSubcategory = async (subcategory) => {
  selectedSubcategory.value = subcategory
  await loadKnowledgeBySubcategory()
}

const loadKnowledgeByCategory = async () => {
  if (!selectedCategory.value) {
    searchResults.value = null
    return
  }

  searching.value = true
  try {
    const response = await request.get(`/api/knowledge/category/${encodeURIComponent(selectedCategory.value)}`)
    
    if (response.data.code === 200) {
      searchResults.value = {
        knowledgeList: response.data.data,
        total: response.data.data.length
      }
    }
  } catch (error) {
    console.error('加载分类知识失败:', error)
  } finally {
    searching.value = false
  }
}

const loadKnowledgeBySubcategory = async () => {
  if (!selectedCategory.value || !selectedSubcategory.value) {
    return
  }

  searching.value = true
  try {
    const response = await request.get(
      `/api/knowledge/category/${encodeURIComponent(selectedCategory.value)}/subcategory/${encodeURIComponent(selectedSubcategory.value)}`
    )
    
    if (response.data.code === 200) {
      searchResults.value = {
        knowledgeList: response.data.data,
        total: response.data.data.length
      }
    }
  } catch (error) {
    console.error('加载子分类知识失败:', error)
  } finally {
    searching.value = false
  }
}

const viewKnowledgeDetail = (id) => {
  router.push(`/user/knowledge/${id}`)
}

onMounted(() => {
  // 页面加载时可以加载默认数据
})
</script>

<style scoped>
.knowledge-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.knowledge-header {
  text-align: center;
  margin-bottom: 30px;
}

.knowledge-header h2 {
  font-size: 32px;
  color: #1f2937;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 16px;
  color: #6b7280;
}

.search-section {
  margin-bottom: 30px;
}

.search-input {
  max-width: 600px;
  margin: 0 auto;
}

.search-options {
  text-align: center;
  margin-top: 15px;
}

.category-section {
  margin-bottom: 20px;
}

.category-tags {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 10px;
}

.category-tag {
  cursor: pointer;
  padding: 8px 16px;
  font-size: 14px;
}

.subcategory-section {
  margin-bottom: 20px;
}

.subcategory-tags {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
}

.subcategory-tag {
  cursor: pointer;
  padding: 6px 12px;
  font-size: 13px;
}

.loading-section {
  margin: 40px 0;
}

.ai-answer-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 30px;
  color: white;
}

.ai-answer-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  font-size: 18px;
  font-weight: 600;
}

.ai-answer-content {
  font-size: 16px;
  line-height: 1.8;
  margin-bottom: 16px;
}

.ai-sources {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  font-size: 14px;
  opacity: 0.9;
}

.sources-title {
  margin-right: 8px;
}

.source-tag {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.knowledge-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-top: 30px;
}

.knowledge-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.knowledge-card:hover {
  transform: translateY(-4px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: start;
  margin-bottom: 12px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  flex: 1;
  margin-right: 12px;
}

.card-content {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #9ca3af;
}

.card-tags {
  flex: 1;
}

.card-views {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
