<template>
  <div class="daily-quote-page">
    <div class="quote-container">
      <h2 class="page-title">日签展示</h2>
      
      <!-- 当前日签展示 -->
      <div class="current-quote-card">
        <div class="quote-header">
          <h3>今日日签</h3>
          <span v-if="isModifiedToday" class="custom-badge">自定义</span>
        </div>
        <div class="quote-content">
          <div class="quote-text">{{ quote ? quote.content : '今天也值得被温柔对待' }}</div>
          <div class="quote-time" v-if="quote && quote.updateTime">
            {{ formatTime(quote.updateTime) }}
          </div>
        </div>
      </div>

      <!-- 操作区域 -->
      <div class="actions-section">
        <!-- 随机日签 -->
        <div class="action-card">
          <h4>随机日签</h4>
          <p>获取一个随机的治愈日签</p>
          <el-button 
            type="primary" 
            @click="refreshQuote" 
            :loading="refreshing"
          >
            🔄 换一个
          </el-button>
          <el-button 
            type="success" 
            @click="saveRandomQuote" 
            :loading="savingRandom"
            :disabled="!quote || isModifiedToday"
            style="margin-left: 10px;"
          >
            确定使用
          </el-button>
          <div class="action-tip" v-if="isModifiedToday">
            今日已设置日签，无法更改
          </div>
          <div class="action-tip" v-else-if="quote && !isModifiedToday">
            点击"确定使用"将此随机日签设为今日日签
          </div>
        </div>

        <!-- 自定义日签 -->
        <div class="action-card">
          <h4>自定义日签</h4>
          <p>创建属于你的专属日签</p>
          <el-input 
            v-model="customQuote" 
            placeholder="输入你的专属日签内容" 
            maxlength="30" 
            show-word-limit
            style="margin-bottom: 15px;"
          />
          <div class="custom-buttons">
            <el-button @click="clearCustomQuote">清空</el-button>
            <el-button 
              type="success" 
              @click="saveCustomQuote" 
              :loading="saving"
              :disabled="!customQuote.trim()"
            >
              确定生成
            </el-button>
          </div>
          <div class="action-tip" v-if="isModifiedToday">
            今日已设置自定义日签，可以重新设置
          </div>
        </div>
      </div>

      <!-- 历史日签 -->
      <div class="history-section">
        <h3>历史日签</h3>
        <div class="history-list">
          <div v-for="(item, index) in historyQuotes" :key="index" class="history-item">
            <div class="history-content">{{ item.content }}</div>
            <div class="history-time">{{ formatTime(item.updateTime) }}</div>
            <div class="history-type" :class="item.isCustom ? 'custom' : 'random'">
              {{ item.isCustom ? '自定义' : '随机' }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const quote = ref(null)
const customQuote = ref('')
const isModifiedToday = ref(false)
const refreshing = ref(false)
const saving = ref(false)
const savingRandom = ref(false)
const historyQuotes = ref([])
let refreshTimer = null

async function loadQuote() {
  try {
    // 首先尝试获取自定义日签
    const customResponse = await request.get('/api/quotes/custom')
    console.log('自定义日签接口返回:', customResponse.data)
    if (customResponse.data.code === 200 && customResponse.data.data) {
      quote.value = customResponse.data.data
      // 检查今天是否已经修改过
      const today = new Date()
      const updateTime = new Date(customResponse.data.data.updateTime)
      if (updateTime.toDateString() === today.toDateString()) {
        isModifiedToday.value = true
      } else {
        // 如果不是今天修改的，重置状态
        isModifiedToday.value = false
      }
      return
    }
    
    // 如果没有自定义日签，获取用户专属的随机日签
    await loadRandomQuote()
  } catch (error) {
    console.error('加载日签失败:', error)
    quote.value = { content: '今天也值得被温柔对待' }
    isModifiedToday.value = false
  }
}

// 加载随机日签
async function loadRandomQuote() {
  try {
    const randomResponse = await request.get('/api/quotes/random/user')
    console.log('用户专属随机日签接口返回:', randomResponse.data)
    if (randomResponse.data.code === 200) {
      quote.value = randomResponse.data.data
      // 重置修改状态，因为这是随机日签
      isModifiedToday.value = false
    } else {
      // 如果都失败了，使用默认日签
      quote.value = { content: '今天也值得被温柔对待' }
      isModifiedToday.value = false
    }
  } catch (error) {
    console.error('加载随机日签失败:', error)
    quote.value = { content: '今天也值得被温柔对待' }
    isModifiedToday.value = false
  }
}

// 手动刷新日签
async function refreshQuote() {
  if (refreshing.value) return
  
  try {
    refreshing.value = true
    console.log('手动刷新日签')
    
    // 调用手动随机日签接口，确保每次都是不同的
    const manualResponse = await request.get('/api/quotes/random/user/manual')
    console.log('手动随机日签接口返回:', manualResponse.data)
    
    if (manualResponse.data.code === 200) {
      quote.value = manualResponse.data.data
      // 重置修改状态，因为这是随机日签
      isModifiedToday.value = false
      ElMessage.success('日签已更新')
      
      // 触发全局日签更新事件
      window.dispatchEvent(new CustomEvent('quoteUpdated', {
        detail: { quote: manualResponse.data.data }
      }))
    } else {
      ElMessage.error('刷新失败，请稍后重试')
    }
  } catch (error) {
    console.error('手动刷新日签失败:', error)
    ElMessage.error('刷新失败，请稍后重试')
  } finally {
    refreshing.value = false
  }
}

async function saveCustomQuote() {
  if (!customQuote.value.trim()) {
    ElMessage.warning('请输入日签内容')
    return
  }
  
  saving.value = true
  try {
    const response = await request.post('/api/quotes/custom', {
      content: customQuote.value.trim()
    })
    
    if (response.data.code === 200) {
      ElMessage.success('自定义日签设置成功')
      quote.value = response.data.data
      customQuote.value = ''
      isModifiedToday.value = true
      localStorage.setItem('lastQuoteRefresh', new Date().toISOString())
      
      // 触发全局日签更新事件，通知其他组件更新
      window.dispatchEvent(new CustomEvent('quoteUpdated', {
        detail: { quote: response.data.data }
      }))
    } else {
      ElMessage.error(response.data.message || '设置失败')
    }
  } catch (error) {
    console.error('保存自定义日签失败:', error)
    ElMessage.error('设置失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

// 清空自定义日签输入
function clearCustomQuote() {
  customQuote.value = ''
}

// 保存随机日签
async function saveRandomQuote() {
  if (!quote.value || isModifiedToday.value) return
  
  savingRandom.value = true
  try {
    const response = await request.post('/api/quotes/custom', {
      content: quote.value.content
    })
    
    if (response.data.code === 200) {
      ElMessage.success('随机日签设置成功')
      isModifiedToday.value = true
      
      // 触发全局日签更新事件
      window.dispatchEvent(new CustomEvent('quoteUpdated', {
        detail: { quote: response.data.data }
      }))
    } else {
      ElMessage.error(response.data.message || '设置失败')
    }
  } catch (error) {
    console.error('保存随机日签失败:', error)
    ElMessage.error('设置失败，请稍后重试')
  } finally {
    savingRandom.value = false
  }
}

// 格式化时间
function formatTime(timeStr) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 加载历史日签（模拟数据）
function loadHistoryQuotes() {
  // 这里可以调用后端接口获取历史日签
  // 暂时使用模拟数据
  historyQuotes.value = [
    {
      content: '今天的你依然闪闪发光',
      updateTime: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
      isCustom: false
    },
    {
      content: '相信自己，你比想象中更强大',
      updateTime: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(),
      isCustom: true
    },
    {
      content: '每一个努力的日子都不会被辜负',
      updateTime: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString(),
      isCustom: false
    }
  ]
}

onMounted(() => {
  loadQuote()
  loadHistoryQuotes()
  
  // 监听全局日签更新事件
  window.addEventListener('quoteUpdated', (event) => {
    if (event.detail && event.detail.quote) {
      quote.value = event.detail.quote
      isModifiedToday.value = true
    }
  })
})
</script>

<style scoped>
.daily-quote-page {
  min-height: 100vh;
  background: rgba(255,255,255,0.85) !important;
  padding: 20px;
}

.quote-container {
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  color: #7ec6e6;
  text-align: center;
  font-size: 32px;
  margin-bottom: 30px;
  text-shadow: 0 2px 4px rgba(126,198,230,0.15);
}

.current-quote-card {
  background: rgba(255,255,255,0.95);
  border-radius: 20px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
  backdrop-filter: blur(6px);
}

.quote-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.quote-header h3 {
  color: #333;
  margin: 0;
  font-size: 24px;
}

.custom-badge {
  background: linear-gradient(45deg, #ffb6c1, #ffc0cb);
  color: white;
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 12px;
  font-weight: bold;
}

.quote-content {
  text-align: center;
}

.quote-text {
  font-size: 24px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 15px;
  font-weight: 500;
}

.quote-time {
  color: #666;
  font-size: 14px;
}

.actions-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 30px;
}

.action-card {
  background: rgba(255,255,255,0.95);
  border-radius: 15px;
  padding: 25px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  backdrop-filter: blur(6px);
}

.action-card h4 {
  color: #333;
  margin: 0 0 10px 0;
  font-size: 18px;
}

.action-card p {
  color: #666;
  margin: 0 0 20px 0;
  font-size: 14px;
}

.custom-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.action-tip {
  color: #ff6b6b;
  font-size: 12px;
  margin-top: 10px;
}

/* 自定义按钮颜色 */
.action-card :deep(.el-button--primary) {
  background: #ffb6c1 !important;
  border-color: #ffb6c1 !important;
  color: white !important;
}

.action-card :deep(.el-button--primary:hover) {
  background: #ffc0cb !important;
  border-color: #ffc0cb !important;
}

.action-card :deep(.el-button--success) {
  background: #87ceeb !important;
  border-color: #87ceeb !important;
  color: white !important;
}

.action-card :deep(.el-button--success:hover) {
  background: #98d8f0 !important;
  border-color: #98d8f0 !important;
}

.custom-buttons :deep(.el-button) {
  background: #ffb6c1 !important;
  border-color: #ffb6c1 !important;
  color: white !important;
}

.custom-buttons :deep(.el-button:hover) {
  background: #ffc0cb !important;
  border-color: #ffc0cb !important;
}

.custom-buttons :deep(.el-button--success) {
  background: #87ceeb !important;
  border-color: #87ceeb !important;
  color: white !important;
}

.custom-buttons :deep(.el-button--success:hover) {
  background: #98d8f0 !important;
  border-color: #98d8f0 !important;
}

.history-section {
  background: rgba(255,255,255,0.95);
  border-radius: 20px;
  padding: 25px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  backdrop-filter: blur(6px);
}

.history-section h3 {
  color: #333;
  margin: 0 0 20px 0;
  text-align: center;
}

.history-list {
  max-height: 400px;
  overflow-y: auto;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  margin-bottom: 10px;
  background: rgba(255,255,255,0.7);
  border-radius: 10px;
  border-left: 3px solid #ffb6c1;
}

.history-content {
  flex: 1;
  color: #333;
  font-size: 16px;
}

.history-time {
  color: #666;
  font-size: 12px;
  white-space: nowrap;
}

.history-type {
  padding: 4px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: bold;
}

.history-type.custom {
  background: #87ceeb;
  color: white;
}

.history-type.random {
  background: #ffb6c1;
  color: white;
}

@media (max-width: 768px) {
  .actions-section {
    grid-template-columns: 1fr;
  }
  .history-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  .history-time {
    white-space: normal;
  }
}
</style> 