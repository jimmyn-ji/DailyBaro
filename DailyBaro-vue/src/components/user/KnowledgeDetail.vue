<template>
  <div class="knowledge-detail-page">
    <div v-if="loading" class="loading-section">
      <el-skeleton :rows="10" animated />
    </div>

    <div v-else-if="knowledge" class="detail-content">
      <el-card class="knowledge-card">
        <div class="knowledge-header">
          <h1 class="knowledge-title">{{ knowledge.title }}</h1>
          <div class="knowledge-meta">
            <el-tag type="primary">{{ knowledge.category }}</el-tag>
            <el-tag v-if="knowledge.subcategory" type="info">{{ knowledge.subcategory }}</el-tag>
          </div>
        </div>

        <div class="knowledge-body">
          <div class="knowledge-content" v-html="formatContent(knowledge.content)"></div>
        </div>

        <div v-if="knowledge.tags" class="knowledge-tags">
          <span class="tags-label">标签：</span>
          <el-tag
            v-for="tag in tagList"
            :key="tag"
            size="small"
            class="tag-item"
          >
            {{ tag }}
          </el-tag>
        </div>

        <div class="knowledge-footer">
          <div class="footer-info">
            <span class="view-count">
              <el-icon><View /></el-icon>
              {{ knowledge.viewCount || 0 }} 次浏览
            </span>
            <span class="update-time">
              更新时间：{{ formatTime(knowledge.updateTime) }}
            </span>
          </div>
          <el-button @click="goBack">返回</el-button>
        </div>
      </el-card>
    </div>

    <el-empty v-else description="知识不存在或已删除" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View } from '@element-plus/icons-vue'
import request from '@/utils/request'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const knowledge = ref(null)
const loading = ref(true)

const tagList = computed(() => {
  if (!knowledge.value || !knowledge.value.tags) return []
  return knowledge.value.tags.split(',').filter(t => t.trim())
})

const loadKnowledge = async () => {
  const id = route.params.id
  if (!id) {
    ElMessage.error('知识ID不存在')
    return
  }

  loading.value = true
  try {
    const response = await request.get(`/api/knowledge/${id}`)
    
    if (response.data.code === 200) {
      knowledge.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '加载失败')
    }
  } catch (error) {
    console.error('加载知识详情失败:', error)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const formatContent = (content) => {
  if (!content) return ''
  // 简单的格式化：将换行转换为 <br>
  return content.replace(/\n/g, '<br>')
}

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadKnowledge()
})
</script>

<style scoped>
.knowledge-detail-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.loading-section {
  max-width: 900px;
  margin: 0 auto;
}

.detail-content {
  max-width: 900px;
  margin: 0 auto;
}

.knowledge-card {
  padding: 40px;
}

.knowledge-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.knowledge-title {
  font-size: 32px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 16px 0;
  line-height: 1.4;
}

.knowledge-meta {
  display: flex;
  gap: 10px;
  align-items: center;
}

.knowledge-body {
  margin-bottom: 30px;
}

.knowledge-content {
  font-size: 16px;
  color: #374151;
  line-height: 2;
  white-space: pre-wrap;
}

.knowledge-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 30px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.tags-label {
  font-size: 14px;
  color: #9ca3af;
  margin-right: 8px;
}

.tag-item {
  background: #f3f4f6;
  color: #6b7280;
}

.knowledge-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.footer-info {
  display: flex;
  gap: 20px;
  align-items: center;
  font-size: 14px;
  color: #9ca3af;
}

.view-count {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
