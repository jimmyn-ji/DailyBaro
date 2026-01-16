<template>
  <div class="quote-create-bg">
    <div class="quote-create-card">
      <h2 class="quote-title">创建日签</h2>
      <p class="quote-subtitle">记录今日的心情和感悟</p>
      
      <div class="quote-actions">
        <button class="action-btn auto-btn" @click="generateAutoQuote" :disabled="isGenerating">
          <span class="btn-icon">🤖</span>
          {{ isGenerating ? '生成中...' : '自动生成' }}
        </button>
        <button class="action-btn manual-btn" @click="showManualForm = true">
          <span class="btn-icon">✏️</span>
          手动编辑
        </button>
      </div>
      
      <!-- 自动生成结果 -->
      <div v-if="autoQuote" class="quote-result">
        <h3>AI生成的日签：</h3>
        <div class="quote-content">
          <p class="quote-text">{{ autoQuote }}</p>
          <div class="quote-actions-result">
            <button class="save-btn" @click="saveQuote(autoQuote)">保存</button>
            <button class="regenerate-btn" @click="generateAutoQuote">重新生成</button>
          </div>
        </div>
      </div>
      
      <!-- 手动编辑表单 -->
      <div v-if="showManualForm" class="manual-form">
        <h3>手动编辑日签：</h3>
        <div class="form-group">
          <label class="form-label">今日感悟</label>
          <textarea 
            v-model="manualQuote" 
            class="quote-textarea" 
            placeholder="写下你今日的感悟..."
            rows="4"
          ></textarea>
        </div>
        <div class="form-actions">
          <button class="save-btn" @click="saveQuote(manualQuote)" :disabled="!manualQuote.trim()">
            保存
          </button>
          <button class="cancel-btn" @click="showManualForm = false">取消</button>
        </div>
      </div>
      
      <!-- 今日日签预览 -->
      <div v-if="todayQuote" class="today-quote">
        <h3>今日日签：</h3>
        <div class="quote-display">
          <p class="quote-text">{{ todayQuote.content }}</p>
          <div class="quote-meta">
            <span class="quote-date">{{ formatDate(todayQuote.createTime) }}</span>
            <span class="quote-type">{{ todayQuote.type === 'auto' ? 'AI生成' : '手动编辑' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import dayjs from 'dayjs'

const router = useRouter()
const uid = window.sessionStorage.getItem('uid')

const autoQuote = ref('')
const manualQuote = ref('')
const showManualForm = ref(false)
const isGenerating = ref(false)
const todayQuote = ref(null)

// 自动生成日签
async function generateAutoQuote() {
  if (!uid) {
    ElMessage.error('请先登录')
    return
  }
  
  isGenerating.value = true
  try {
    const res = await request.post('/api/ai/query', {
      question: '请为我生成一句今日日签，内容要积极向上，富有哲理，适合作为今日的心情总结，长度在50字以内。'
    })
    const data = res.data
    if (data.code === 200) {
      autoQuote.value = data.data
      ElMessage.success('日签生成成功')
    } else {
      ElMessage.error(data.message || '生成失败')
    }
  } catch (error) {
    console.error('生成日签失败:', error)
    ElMessage.error('生成失败，请重试')
  } finally {
    isGenerating.value = false
  }
}

// 保存日签
async function saveQuote(content) {
  if (!content || !content.trim()) {
    ElMessage.warning('请输入日签内容')
    return
  }
  
  try {
    const quoteData = {
      content: content.trim(),
      type: autoQuote.value === content ? 'auto' : 'manual',
      userId: uid
    }
    
    const res = await request.post('/api/quotes/custom', quoteData)
    
    if (res.data.code === 200) {
      ElMessage.success('日签保存成功')
      loadTodayQuote()
      showManualForm.value = false
      manualQuote.value = ''
      autoQuote.value = ''
    } else {
      ElMessage.error(res.data.message || '保存失败')
    }
  } catch (error) {
    console.error('保存日签失败:', error)
    ElMessage.error('保存失败，请重试')
  }
}

// 加载今日日签
async function loadTodayQuote() {
  if (!uid) return
  
  try {
    const today = dayjs().format('YYYY-MM-DD')
    const res = await request.get(`/api/quote/today/${uid}`)
    
    if (res.data.code === 200 && res.data.data) {
      todayQuote.value = res.data.data
    }
  } catch (error) {
    console.error('加载今日日签失败:', error)
  }
}

// 格式化日期
function formatDate(date) {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadTodayQuote()
})
</script>

<style scoped>
.quote-create-bg {
  min-height: 100vh;
  background: linear-gradient(135deg, #ffeef8 0%, #fff5f5 100%);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 40px 20px;
}

.quote-create-card {
  background: rgba(255,255,255,0.95);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(255, 182, 193, 0.15);
  padding: 40px;
  width: 100%;
  max-width: 800px;
  border: 2px dashed rgba(255, 182, 193, 0.3);
}

.quote-title {
  font-size: 32px;
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

.quote-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-bottom: 30px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px 30px;
  border: none;
  border-radius: 15px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}

.auto-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.auto-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
}

.manual-btn {
  background: linear-gradient(135deg, #f093fb, #f5576c);
  color: white;
}

.manual-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(240, 147, 251, 0.3);
}

.btn-icon {
  font-size: 20px;
}

.quote-result {
  background: rgba(255,255,255,0.8);
  border-radius: 15px;
  padding: 25px;
  margin-bottom: 30px;
  border: 2px solid rgba(102, 126, 234, 0.2);
}

.quote-result h3 {
  color: #667eea;
  margin-bottom: 15px;
  font-size: 18px;
}

.quote-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.quote-text {
  font-size: 16px;
  line-height: 1.6;
  color: #333;
  padding: 15px;
  background: rgba(255,255,255,0.9);
  border-radius: 10px;
  border-left: 4px solid #667eea;
}

.quote-actions-result {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.save-btn, .regenerate-btn {
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

.save-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(74, 222, 128, 0.3);
}

.regenerate-btn {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: white;
}

.regenerate-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
}

.manual-form {
  background: rgba(255,255,255,0.8);
  border-radius: 15px;
  padding: 25px;
  margin-bottom: 30px;
  border: 2px solid rgba(240, 147, 251, 0.2);
}

.manual-form h3 {
  color: #f093fb;
  margin-bottom: 15px;
  font-size: 18px;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.quote-textarea {
  width: 100%;
  padding: 15px;
  border: 2px solid rgba(240, 147, 251, 0.3);
  border-radius: 10px;
  font-size: 16px;
  resize: vertical;
  min-height: 120px;
  transition: border-color 0.3s ease;
}

.quote-textarea:focus {
  outline: none;
  border-color: #f093fb;
  box-shadow: 0 0 0 3px rgba(240, 147, 251, 0.1);
}

.form-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.cancel-btn {
  padding: 10px 20px;
  border: 2px solid #e5e7eb;
  background: white;
  color: #666;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
}

.today-quote {
  background: rgba(255,255,255,0.8);
  border-radius: 15px;
  padding: 25px;
  border: 2px solid rgba(255, 182, 193, 0.2);
}

.today-quote h3 {
  color: #ff69b4;
  margin-bottom: 15px;
  font-size: 18px;
}

.quote-display {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quote-meta {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #666;
  margin-top: 10px;
}

.quote-type {
  background: linear-gradient(135deg, #ff69b4, #ff1493);
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

@media (max-width: 768px) {
  .quote-create-card {
    padding: 20px;
  }
  
  .quote-actions {
    flex-direction: column;
    gap: 15px;
  }
  
  .action-btn {
    width: 100%;
    justify-content: center;
  }
}
</style> 