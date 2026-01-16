<template>
  <div class="quote-history-bg">
    <div class="quote-history-card">
      <h2 class="quote-title">日签历史</h2>
      <p class="quote-subtitle">回顾你的心情记录</p>
      
      <!-- 筛选器 -->
      <div class="filter-section">
        <div class="filter-group">
          <label class="filter-label">时间范围：</label>
          <select v-model="selectedTimeRange" @change="onTimeRangeChange" class="filter-select">
            <option value="week">最近一周</option>
            <option value="month">最近一月</option>
            <option value="year">最近一年</option>
            <option value="all">全部</option>
          </select>
        </div>
        <div class="filter-group">
          <label class="filter-label">类型：</label>
          <select v-model="selectedType" @change="loadQuotes" class="filter-select">
            <option value="all">全部</option>
            <option value="auto">AI生成</option>
            <option value="manual">手动编辑</option>
          </select>
        </div>
      </div>
      
      <!-- 日签列表 -->
      <div class="quote-list">
        <div v-if="quotes.length > 0" class="quotes-container">
          <div v-for="quote in quotes" :key="quote.quoteId" class="quote-item">
            <div class="quote-header">
              <div class="quote-date">{{ formatDate(quote.createTime) }}</div>
              <div class="quote-type-badge" :class="quote.type">
                {{ quote.type === 'auto' ? 'AI生成' : '手动编辑' }}
              </div>
            </div>
            <div class="quote-content">
              <p class="quote-text">{{ quote.content }}</p>
            </div>
            <div class="quote-actions">
              <button class="action-btn edit-btn" @click="editQuote(quote)" v-if="quote.type === 'manual'">
                编辑
              </button>
              <button class="action-btn delete-btn" @click="deleteQuote(quote.quoteId)">
                删除
              </button>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <div class="empty-icon"></div>
          <p>暂无日签记录</p>
          <p class="empty-hint">开始创建你的第一篇日签吧</p>
        </div>
      </div>
      
      <!-- 编辑弹窗 -->
      <div v-if="showEditDialog" class="edit-dialog-overlay" @click="closeEditDialog">
        <div class="edit-dialog" @click.stop>
          <div class="edit-dialog-header">
            <h3>编辑日签</h3>
            <button class="close-btn" @click="closeEditDialog">×</button>
          </div>
          <div class="edit-dialog-content">
            <textarea 
              v-model="editingQuote.content" 
              class="edit-textarea" 
              placeholder="编辑日签内容..."
              rows="4"
            ></textarea>
            <div class="edit-dialog-actions">
              <button class="save-btn" @click="saveEditedQuote">保存</button>
              <button class="cancel-btn" @click="closeEditDialog">取消</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import dayjs from 'dayjs'

const uid = window.sessionStorage.getItem('uid')
const quotes = ref([])
const selectedTimeRange = ref('month')
const selectedType = ref('all')
const showEditDialog = ref(false)
const editingQuote = ref(null)

// 加载日签列表
async function loadQuotes() {
  if (!uid) return
  
  try {
    const params = {
      userId: uid,
      type: selectedType.value === 'all' ? undefined : selectedType.value
    }
    
    // 根据时间范围设置日期参数
    const now = dayjs()
    if (selectedTimeRange.value === 'week') {
      params.startDate = now.subtract(7, 'day').format('YYYY-MM-DD')
    } else if (selectedTimeRange.value === 'month') {
      params.startDate = now.subtract(30, 'day').format('YYYY-MM-DD')
    } else if (selectedTimeRange.value === 'year') {
      params.startDate = now.subtract(365, 'day').format('YYYY-MM-DD')
    }
    
    const res = await request.get('/api/quotes/history', { params })
    
    if (res.data.code === 200) {
      quotes.value = Array.isArray(res.data.data) ? res.data.data : []
    } else {
      ElMessage.error(res.data.message || '加载失败')
    }
  } catch (error) {
    console.error('加载日签历史失败:', error)
    ElMessage.error('加载失败，请重试')
  }
}

// 时间范围变化
function onTimeRangeChange() {
  loadQuotes()
}

// 编辑日签
function editQuote(quote) {
  editingQuote.value = { ...quote }
  showEditDialog.value = true
}

// 保存编辑的日签
async function saveEditedQuote() {
  if (!editingQuote.value || !editingQuote.value.content.trim()) {
    ElMessage.warning('请输入日签内容')
    return
  }
  
  try {
    const res = await request.put(`/api/quotes/${editingQuote.value.quoteId}`, {
      content: editingQuote.value.content.trim()
    })
    
    if (res.data.code === 200) {
      ElMessage.success('日签更新成功')
      closeEditDialog()
      loadQuotes()
    } else {
      ElMessage.error(res.data.message || '更新失败')
    }
  } catch (error) {
    console.error('更新日签失败:', error)
    ElMessage.error('更新失败，请重试')
  }
}

// 删除日签
async function deleteQuote(quoteId) {
  try {
    await ElMessageBox.confirm('确定要删除这条日签吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await request.delete(`/api/quotes/${quoteId}`)
    
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      loadQuotes()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除日签失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  }
}

// 关闭编辑弹窗
function closeEditDialog() {
  showEditDialog.value = false
  editingQuote.value = null
}

// 格式化日期
function formatDate(date) {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadQuotes()
})
</script>

<style scoped>
.quote-history-bg {
  min-height: 100vh;
  background: linear-gradient(135deg, #ffeef8 0%, #fff5f5 100%);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 40px 20px;
}

.quote-history-card {
  background: rgba(255,255,255,0.95);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(255, 182, 193, 0.15);
  padding: 30px;
  width: 100%;
  max-width: 800px;
  border: 2px dashed rgba(255, 182, 193, 0.3);
}

.quote-title {
  font-size: 26px;
  font-weight: bold;
  color: #ff69b4;
  text-align: center;
  margin-bottom: 10px;
  letter-spacing: 2px;
}

.quote-subtitle {
  text-align: center;
  color: #666;
  font-size: 16px;
  margin-bottom: 30px;
}

.filter-section {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  font-weight: 600;
  color: #333;
}

.filter-select {
  padding: 8px 12px;
  border: 2px solid rgba(255, 182, 193, 0.3);
  border-radius: 8px;
  background: white;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

.filter-select:focus {
  outline: none;
  border-color: #ff69b4;
}

.quote-list {
  margin-top: 20px;
}

.quotes-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.quote-item {
  background: rgba(255,255,255,0.8);
  border-radius: 15px;
  padding: 18px;
  border: 2px solid rgba(255, 182, 193, 0.2);
  box-shadow: 0 4px 15px rgba(255, 182, 193, 0.1);
  transition: all 0.3s ease;
}

.quote-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 182, 193, 0.2);
  border-color: rgba(255, 182, 193, 0.4);
}

.quote-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.quote-date {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.quote-type-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  color: white;
}

.quote-type-badge.auto {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.quote-type-badge.manual {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}

.quote-content {
  margin-bottom: 15px;
}

.quote-text {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  margin: 0;
  padding: 12px;
  background: rgba(255,255,255,0.9);
  border-radius: 10px;
  border-left: 4px solid #ff69b4;
}

.quote-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.action-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.edit-btn {
  background: linear-gradient(135deg, #4ade80, #22c55e);
  color: white;
}

.edit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(74, 222, 128, 0.3);
}

.delete-btn {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: white;
}

.delete-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.empty-hint {
  font-size: 14px;
  color: #bbb;
  margin-top: 10px;
}

/* 编辑弹窗样式 */
.edit-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  backdrop-filter: blur(5px);
}

.edit-dialog {
  background: white;
  border-radius: 15px;
  padding: 30px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
}

.edit-dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.edit-dialog-header h3 {
  color: #ff69b4;
  margin: 0;
  font-size: 20px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(255, 105, 180, 0.1);
  color: #ff69b4;
}

.edit-textarea {
  width: 100%;
  padding: 15px;
  border: 2px solid rgba(255, 182, 193, 0.3);
  border-radius: 10px;
  font-size: 16px;
  resize: vertical;
  min-height: 120px;
  margin-bottom: 20px;
  transition: border-color 0.3s ease;
}

.edit-textarea:focus {
  outline: none;
  border-color: #ff69b4;
  box-shadow: 0 0 0 3px rgba(255, 105, 180, 0.1);
}

.edit-dialog-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
}

.save-btn, .cancel-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.save-btn {
  background: linear-gradient(135deg, #4ade80, #22c55e);
  color: white;
}

.save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(74, 222, 128, 0.3);
}

.cancel-btn {
  background: #f3f4f6;
  color: #666;
  border: 2px solid #e5e7eb;
}

.cancel-btn:hover {
  background: #e5e7eb;
  border-color: #d1d5db;
}

@media (max-width: 768px) {
  .quote-history-card {
    padding: 20px;
  }
  
  .filter-section {
    flex-direction: column;
    align-items: center;
  }
  
  .quote-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  
  .quote-actions {
    justify-content: center;
  }
}
</style> 