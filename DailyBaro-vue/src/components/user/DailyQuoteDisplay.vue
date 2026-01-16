<template>
  <div class="daily-quote-container">
    <!-- 日签显示区域 -->
    <div class="quote-display-area">
      <div class="quote-content-wrapper" ref="quoteContainerRef" @scroll="updateScrollState">
        <div
          class="quote-content"
          :class="{ expanded: isExpanded }"
          ref="quoteContent"
          @click="onQuoteClick"
        >
          <template v-if="isEditing">
            <input
              class="quote-input"
              v-model="editText"
              ref="quoteInputRef"
              maxlength="100"
              placeholder="写下你的专属日签..."
              @keyup.enter.stop
            />
          </template>
          <template v-else>
            <span v-if="!isExpanded && isOverflow">{{ shortText }}</span>
            <span v-else>{{ currentQuote.content || '点击换一换获取日签' }}</span>
          </template>
        </div>
        <div v-if="!isExpanded && isOverflow" class="arrow right" @click.stop="expandQuote">▶</div>
        <div v-if="isExpanded && canScrollLeft" class="arrow left" @click.stop="scrollLeft">◀</div>
        <div v-if="isExpanded && canScrollRight" class="arrow right" @click.stop="scrollRight">▶</div>
      </div>
      <div class="quote-author" v-if="currentQuote.author && currentQuote.author !== 'Unknown'">—— {{ currentQuote.author }}</div>
    </div>
    <div class="quote-dropdown-arrow" :class="{ 'arrow-up': showHistoryDropdown }" @click.stop="toggleHistoryDropdown">
        <span>▼</span>
      </div>
    <button v-if="isLongText" class="expand-btn" @click.stop="toggleExpand">{{ isExpanded ? '收起' : '展开' }}</button>
    <!-- 移除旧位置的换一换按钮与提示，统一放到右侧操作按钮区 -->
    <!-- 历史记录下拉面板（向下展开） -->
    <div class="history-dropdown" :class="{ open: showHistoryDropdown }" @click.stop>
      <div class="history-dropdown-inner">
        <div class="history-header-row">
          <span class="history-title">历史日签</span>
          <button class="history-close" @click="closeHistoryDropdown">×</button>
        </div>
        <div class="history-list">
          <div v-if="historyQuotes.length > 0" class="history-items">
            <div
              v-for="quote in historyQuotes"
              :key="quote.id"
              class="history-item"
              @click="selectHistoryQuote(quote)"
            >
              <div class="history-content">{{ quote.content }}</div>
              <div class="history-date">{{ formatDate(quote.updateTime) }}</div>
            </div>
          </div>
          <div v-else class="history-empty">
            <div class="empty-icon"></div>
            <div class="empty-text">暂无历史记录</div>
            <div class="empty-desc">开始创建你的第一篇日签吧</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作按钮区域 -->
    <div class="quote-actions">
      <!-- 换一换按钮（与编辑同尺寸的小圆），次数满3变灰并提示 -->
      <div class="quote-refresh-btn" :class="{ disabled: refreshCount >= 3 }" @click="handleRefreshClick" title="换一换">
        <span class="refresh-icon">🔄</span>
      </div>

      <!-- 自定义按钮 -->
  <div class="quote-custom-btn" @click.stop="openCustomDialog" title="自定义日签">
        <span class="custom-icon">✏️</span>
      </div>

      <!-- 确定按钮 -->
      <div class="quote-confirm-btn" @click.stop="confirmSelection" title="确定选择" :class="{ 'btn-active': hasChanges }">
        <span class="confirm-icon">✓</span>
      </div>
    </div>

    <!-- 换一换选项弹窗 - 单个日签显示 -->
  <div v-if="false && showRefreshDialog" class="refresh-dialog-overlay" @click="closeRefreshDialog">
      <div class="refresh-dialog" @click.stop>
        <div class="refresh-dialog-header">
          <h3>✨ 今日日签</h3>
          <button class="close-btn" @click="closeRefreshDialog">×</button>
        </div>
        <div class="refresh-dialog-content">
          <div class="single-quote-display">
            <div class="quote-card">
              <div class="quote-content">{{ currentRandomQuote.content }}</div>
              <div class="quote-author" v-if="currentRandomQuote.author">—— {{ currentRandomQuote.author }}</div>
            </div>
          </div>
          <div class="refresh-dialog-actions">
            <button class="refresh-more-btn" @click="loadNewRandomQuote">🔄</button>
            <button class="confirm-option-btn" @click="confirmCurrentQuote">✅ 使用这个</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 自定义日签弹窗 -->
    <div v-if="showCustomDialog" class="custom-dialog-overlay" @click="closeCustomDialog">
      <div class="custom-dialog" @click.stop>
        <div class="custom-dialog-header">
          <h3>✏️ 自定义日签</h3>
          <button class="close-btn" @click="closeCustomDialog">×</button>
        </div>
        <div class="custom-dialog-content">
          <div class="custom-input-group">
            <label class="custom-label">日签内容</label>
            <textarea
              v-model="customQuoteContent"
              class="custom-textarea"
              placeholder="写下你的专属日签..."
              rows="4"
              maxlength="100"
            ></textarea>
            <div class="char-count">{{ customQuoteContent.length }}/100</div>
          </div>
          <div class="custom-dialog-actions">
            <button class="confirm-custom-btn" @click="confirmCustomQuote" :disabled="!customQuoteContent.trim()">
              💾 确定保存
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

// 响应式数据
const currentQuote = ref({ content: '今天也值得被温柔对待' })
const historyQuotes = ref([])
const currentRandomQuote = ref({})
const customQuoteContent = ref('')
const refreshCount = ref(0)

// 弹窗状态
const showHistoryDropdown = ref(false)
const showRefreshDialog = ref(false)
const showCustomDialog = ref(false)

// 选择状态
const selectedHistoryQuote = ref(null)
const showFullText = ref(false)
const maxLength = 40
const isLongText = computed(() => (currentQuote.value?.content || '').length > maxLength)
const shortText = computed(() => {
  const t = currentQuote.value?.content || ''
  return t.length > maxLength ? t.slice(0, maxLength) + '...' : t
})

// 展开/滚动相关
const isExpanded = ref(false)
const isOverflow = computed(() => {
  const t = currentQuote.value?.content || ''
  return t.length > maxLength
})
const canScrollLeft = ref(false)
const canScrollRight = ref(false)
const quoteContainerRef = ref(null)

function expandQuote() {
  isExpanded.value = true
  nextTick(() => {
    updateScrollState()
  })
}

function scrollLeft() {
  if (quoteContainerRef.value) {
    quoteContainerRef.value.scrollLeft -= 50
    updateScrollState()
  }
}

function scrollRight() {
  if (quoteContainerRef.value) {
    quoteContainerRef.value.scrollLeft += 50
    updateScrollState()
  }
}

function updateScrollState() {
  if (!quoteContainerRef.value) return
  const el = quoteContainerRef.value
  canScrollLeft.value = el.scrollLeft > 0
  canScrollRight.value = el.scrollLeft < el.scrollWidth - el.clientWidth - 1
}

// 计算属性
const hasChanges = computed(() => {
  const editingChanged = isEditing.value && (editText.value || '').trim() && (editText.value.trim() !== (currentQuote.value?.content || ''))
  return editingChanged || selectedHistoryQuote.value !== null || previewQuote.value !== null
})

// 组件挂载时加载数据
onMounted(() => {
  loadCurrentQuote()
})

// 加载当前日签
async function loadCurrentQuote() {
  try {
    console.log('开始加载当前日签...')
    // 优先自定义日签
    const customResponse = await request.get('/api/quotes/custom')
    if (customResponse.data.code === 200 && customResponse.data.data) {
      currentQuote.value = customResponse.data.data
      console.log('加载自定义日签:', currentQuote.value)
      return
    }
    // 否则用专属随机日签
    const randomResponse = await request.get('/api/quotes/random/user')
    if (randomResponse.data.code === 200) {
      currentQuote.value = randomResponse.data.data
      console.log('加载随机日签:', currentQuote.value)
    } else {
      currentQuote.value = { content: '今天也值得被温柔对待' }
      console.log('使用默认日签')
    }
  } catch (error) {
    console.error('加载日签失败:', error)
    currentQuote.value = { content: '今天也值得被温柔对待' }
  }
}

// 加载历史记录
async function loadHistoryQuotes() {
  try {
    console.log('开始加载历史记录...')
    const response = await request.get('/api/quotes/history')
    console.log('历史记录响应:', response.data)
    if (response.data.code === 200) {
      historyQuotes.value = Array.isArray(response.data.data) ? response.data.data : []
      console.log('设置历史记录:', historyQuotes.value)
    } else {
      console.error('历史记录接口返回错误:', response.data.message)
      historyQuotes.value = []
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
    historyQuotes.value = []
  }
}

// 切换历史记录下拉框
function toggleHistoryDropdown() {
  console.log('切换历史记录下拉框，当前状态:', showHistoryDropdown.value)
  showHistoryDropdown.value = !showHistoryDropdown.value
  console.log('切换后状态:', showHistoryDropdown.value)
  if (showHistoryDropdown.value) {
    loadHistoryQuotes()
  }
}
function toggleExpand() {
  showFullText.value = !showFullText.value
}

// 刷新获取随机日签，但不立即生效，等待确认
async function refreshDirect() {
  if (refreshCount.value >= 3) return
  try {
    const response = await request.get('/api/quotes/random/user/manual')
    if (response.data.code === 200) {
      previewQuote.value = response.data.data
      showFullText.value = false
      refreshCount.value++
      ElMessage.info('已选中候选日签，点击右侧✓确认后生效')
    } else if (response.data.code === 429) {
      refreshCount.value = 3
      ElMessage.warning('今日换一换已达上限（3次）')
    }
  } catch (e) {
    ElMessage.error('获取新日签失败')
  }
}

const isEditing = ref(false)
const editText = ref('')
const quoteInputRef = ref(null)
// 新增：待确认的候选日签（来自“换一换”或历史选择）
const previewQuote = ref(null)

function startEdit() {
  editText.value = currentQuote.value.content || ''
  isEditing.value = true
  nextTick(() => {
    editInput.value && editInput.value.focus()
  })
}
async function saveEdit() {
  if (!editText.value.trim()) {
    isEditing.value = false
    return
  }
  try {
    const response = await request.post('/api/quotes/custom', { content: editText.value.trim() })
    if (response.data.code === 200) {
      currentQuote.value.content = editText.value.trim()
      ElMessage.success('自定义日签保存成功')
    } else {
      ElMessage.error(response.data.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  }
  isEditing.value = false
}

// 关闭历史记录下拉框
function closeHistoryDropdown() {
  showHistoryDropdown.value = false
  selectedHistoryQuote.value = null
}

// 历史列表点击：仅设置候选，等待确认
function selectHistoryQuote(quote) {
  selectedHistoryQuote.value = quote
  closeHistoryDropdown()
}

// 显示换一换选项
async function showRefreshOptions() {
  try {
    const response = await request.get('/api/quotes/random/user')
    if (response.data.code === 200) {
      currentRandomQuote.value = response.data.data
      showRefreshDialog.value = true
    } else {
      ElMessage.error('获取日签失败')
    }
  } catch (error) {
    console.error('获取日签失败:', error)
    ElMessage.error('获取日签失败')
  }
}

// 关闭换一换弹窗
function closeRefreshDialog() {
  showRefreshDialog.value = false
}

// 加载新的随机日签
async function loadNewRandomQuote() {
  try {
    const response = await request.get('/api/quotes/random/user')
    if (response.data.code === 200) {
      currentRandomQuote.value = response.data.data
      ElMessage.success('已换新日签')
    } else {
      ElMessage.error('获取新日签失败')
    }
  } catch (error) {
    console.error('获取新日签失败:', error)
    ElMessage.error('获取新日签失败')
  }
}

// 确认使用当前随机日签
function confirmCurrentQuote() {
  if (currentRandomQuote.value && currentRandomQuote.value.content) {
    currentQuote.value = currentRandomQuote.value
    ElMessage.success('日签已更新')
    closeRefreshDialog()
  } else {
    ElMessage.warning('没有可用的日签')
  }
}

// 显示自定义弹窗
function openCustomDialog() {
  isEditing.value = true
  editText.value = (currentQuote.value && currentQuote.value.content) ? currentQuote.value.content : ''
  nextTick(() => {
    if (quoteInputRef.value) {
      quoteInputRef.value.focus()
      try { quoteInputRef.value.select() } catch (e) {}
    }
  })
}

// 关闭自定义弹窗
function closeCustomDialog() {
  showCustomDialog.value = false
  customQuoteContent.value = ''
}

// 确认自定义日签
async function confirmCustomQuote() {
  if (!customQuoteContent.value.trim()) {
    ElMessage.warning('请输入日签内容')
    return
  }

  try {
    const response = await request.post('/api/quotes/custom', {
      content: customQuoteContent.value.trim()
    })

    if (response.data.code === 200) {
      ElMessage.success('自定义日签保存成功')
      closeCustomDialog()
      await loadCurrentQuote()
    } else {
      ElMessage.error(response.data.message || '保存失败')
    }
  } catch (error) {
    console.error('保存自定义日签失败:', error)
    ElMessage.error('保存失败')
  }
}

// 确认按钮：统一生效逻辑
async function confirmSelection() {
  try {
    // 1) 编辑模式优先：保存自定义内容
    if (isEditing.value) {
      const text = (editText.value || '').trim()
      if (!text) {
        isEditing.value = false
      } else {
        const response = await request.post('/api/quotes/custom', { content: text })
        if (response.data.code === 200) {
          currentQuote.value = { ...(currentQuote.value || {}), content: text }
          ElMessage.success('自定义日签保存成功')
        } else {
          ElMessage.error(response.data.message || '保存失败')
          return
        }
      }
      isEditing.value = false
      // 继续判断是否还有候选选择
    }

    // 2) 历史选择
    if (selectedHistoryQuote.value) {
      currentQuote.value = selectedHistoryQuote.value
      ElMessage.success('已选择历史日签')
      selectedHistoryQuote.value = null
    }

    // 3) 换一换候选
    if (previewQuote.value) {
      currentQuote.value = previewQuote.value
      previewQuote.value = null
      ElMessage.success('日签已更新')
    }
  } catch (error) {
    console.error('确认选择失败:', error)
    ElMessage.error('操作失败')
  }
}

// 格式化日期
function formatDate(date) {
  if (!date) return ''
  return dayjs(date).format('MM-DD HH:mm')
}

// 新增：点击换一换的封装，满额时弹窗提示
async function handleRefreshClick() {
  if (refreshCount.value >= 3) {
    try {
      await ElMessageBox.alert('今日已达换一换上限（3次）', '温馨提示', { type: 'warning', confirmButtonText: '知道了' })
    } catch (e) {}
    return
  }
  await refreshDirect()
}

// 点击日签内容，展开/收起历史下拉
function onQuoteClick() {
  if (isEditing.value) return
  showHistoryDropdown.value = !showHistoryDropdown.value
  if (showHistoryDropdown.value) {
    loadHistoryQuotes()
  }
}
</script>
<style scoped>
/* 基础容器样式 */
.daily-quote-container {
  background: linear-gradient(135deg, rgba(255,255,255,0.95), rgba(255,255,255,0.9)) !important;
  display: flex;
  align-items: center;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(126,198,230,0.2);
  padding: 0 16px;
  margin-left: 15px;
  min-width: 252px;
  max-width: 315px;
  min-height: 42px;
  height: auto;
  border: 2px solid rgba(126,198,230,0.3);
  backdrop-filter: blur(15px);
  transition: all 0.3s ease;
  position: relative;
  overflow: visible;
}

.daily-quote-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(126,198,230,0.1), transparent);
  transition: left 0.5s ease;
}

.daily-quote-container:hover::before {
  left: 100%;
}

.daily-quote-container:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(126,198,230,0.3);
  border-color: rgba(126,198,230,0.5);
}

/* 日签显示区域 */
.quote-display-area {
  flex: 1;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 15px;
  transition: all 0.3s ease;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.quote-display-area:hover {
  background: rgba(126,198,230,0.1);
  transform: scale(1.02);
}

.quote-content {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.quote-text {
  font-size: 11px;
  color: #333;
  font-weight: 600;
  line-height: 1.3;
  display: block;
  transition: color 0.3s ease;
}

.quote-display-area:hover .quote-text {
  color: #7ec6e6;
}

.quote-author {
  font-size: 9px;
  color: #999;
  margin-top: 4px;
  text-align: right;
}

.quote-dropdown-arrow {
  font-size: 10px;
  color: #7ec6e6;
  margin-left: 5px;
  transition: transform 0.3s ease;
  display: flex;
  align-items: center;
}

.quote-dropdown-arrow.arrow-up {
  transform: rotate(180deg);
}

/* 历史记录侧边栏 */
.history-sidebar {
  position: absolute;
  top: 100%;
  right: 0;
  width: 280px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 8px 25px rgba(126,198,230,0.15);
  z-index: 1000;
  max-height: 300px;
  overflow-y: auto;
  border: 2px solid rgba(126,198,230,0.2);
  margin-top: 5px;
  animation: slideInRight 0.3s ease-out;
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  border-bottom: 2px solid rgba(126,198,230,0.2);
  background: linear-gradient(135deg, rgba(126,198,230,0.1), rgba(126,198,230,0.05));
  border-radius: 10px 10px 0 0;
}

.history-title {
  font-size: 14px;
  font-weight: bold;
  color: #7ec6e6;
  display: flex;
  align-items: center;
  gap: 5px;
}

.history-title::before {
  content: '📚';
  font-size: 16px;
}

.history-close {
  background: none;
  border: none;
  font-size: 18px;
  color: #999;
  cursor: pointer;
  padding: 5px;
  border-radius: 50%;
  transition: all 0.3s ease;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.history-close:hover {
  background-color: rgba(126,198,230,0.1);
  color: #7ec6e6;
  transform: scale(1.1);
}

.history-list {
  padding: 10px 15px;
}

.history-items {
  max-height: 200px;
  overflow-y: auto;
  padding-right: 5px;
}

.history-items::-webkit-scrollbar {
  width: 4px;
}

.history-items::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 2px;
}

.history-items::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.history-items::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  cursor: pointer;
  border-bottom: 1px solid rgba(126,198,230,0.1);
  transition: all 0.3s ease;
  min-height: 40px;
  border-radius: 5px;
  margin-bottom: 2px;
}

.history-item:last-child {
  border-bottom: none;
}

.history-item:hover {
  background: linear-gradient(135deg, rgba(126,198,230,0.1), rgba(126,198,230,0.05));
  transform: translateX(2px);
  box-shadow: 0 2px 8px rgba(126,198,230,0.2);
  border-color: rgba(126,198,230,0.3);
}

.history-content {
  font-size: 13px;
  color: #333;
  font-weight: 500;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

.history-date {
  font-size: 11px;
  color: #999;
  margin-left: 10px;
}

.history-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
  color: #999;
}

.empty-icon {
  font-size: 30px;
  margin-bottom: 10px;
}

.empty-text {
  font-size: 14px;
  margin-bottom: 5px;
}

.empty-desc {
  font-size: 12px;
  color: #bbb;
}

/* 操作按钮区域 */
.quote-actions {
  display: flex;
  gap: 6px;
  margin-left: 6px;
}

.quote-refresh-btn, .quote-custom-btn, .quote-confirm-btn {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.quote-refresh-btn {
  background: linear-gradient(135deg, #7ec6e6, #5bb8e6);
}

.quote-custom-btn {
  background: linear-gradient(135deg, #ff6b9d, #ff8fab);
}

.quote-confirm-btn {
  background: linear-gradient(135deg, #67c23a, #85ce61);
  opacity: 0.6;
  transition: all 0.3s ease;
}

.quote-confirm-btn.btn-active {
  opacity: 1;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(103,194,58,0.4);
}

.quote-refresh-btn:hover {
  transform: rotate(180deg) scale(1.1);
  box-shadow: 0 4px 12px rgba(126,198,230,0.4);
}

.quote-custom-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(255,107,157,0.4);
}

.quote-confirm-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(103,194,58,0.4);
}

.refresh-icon, .custom-icon, .confirm-icon {
  font-size: 12px;
  color: white;
  transition: transform 0.3s ease;
}

.quote-refresh-btn:hover .refresh-icon {
  transform: rotate(180deg);
}

.quote-custom-btn:hover .custom-icon {
  transform: scale(1.1);
}

.quote-confirm-btn:hover .confirm-icon {
  transform: scale(1.1);
}

/* 弹窗样式 */
.refresh-dialog-overlay, .custom-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  backdrop-filter: blur(12px);
  animation: fadeIn 0.25s ease-out;
  padding: 20px;
  box-sizing: border-box;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    backdrop-filter: blur(0px);
  }
  to {
    opacity: 1;
    backdrop-filter: blur(12px);
  }
}

.refresh-dialog, .custom-dialog {
  background: linear-gradient(145deg, rgba(255,255,255,0.95), rgba(248,250,252,0.9));
  border-radius: 20px;
  box-shadow:
    0 25px 50px rgba(0,0,0,0.15),
    0 15px 35px rgba(126,198,230,0.1),
    0 5px 15px rgba(0,0,0,0.08),
    inset 0 1px 0 rgba(255,255,255,0.8);
  max-width: 420px;
  width: 90%;
  max-height: 80vh;
  min-height: auto;
  overflow: hidden;
  animation: slideUpBounce 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  border: 2px solid rgba(126,198,230,0.15);
  backdrop-filter: blur(20px);
  position: relative;
}

@keyframes slideUpBounce {
  0% {
    opacity: 0;
    transform: translate3d(0, 60px, 0) scale(0.95);
  }
  50% {
    transform: translate3d(0, -10px, 0) scale(1.02);
  }
  100% {
    opacity: 1;
    transform: translate3d(0, 0, 0) scale(1);
  }
}

.refresh-dialog-header, .custom-dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 16px 24px;
  border-bottom: 1px solid rgba(126,198,230,0.12);
  background: linear-gradient(135deg,
    rgba(126,198,230,0.05),
    rgba(255,255,255,0.8),
    rgba(126,198,230,0.03)
  );
  backdrop-filter: blur(15px);
  position: relative;
}

.refresh-dialog-header::before, .custom-dialog-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 4px;
  background: linear-gradient(90deg,
    rgba(126,198,230,0.3),
    rgba(126,198,230,0.6),
    rgba(126,198,230,0.3)
  );
  border-radius: 2px;
}

.refresh-dialog-header h3, .custom-dialog-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 18px;
  font-weight: 700;
  text-shadow: 0 1px 3px rgba(0,0,0,0.1);
  background: linear-gradient(135deg, #2c3e50, #34495e);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.close-btn {
  background: linear-gradient(145deg, rgba(255,255,255,0.9), rgba(248,250,252,0.8));
  border: 1px solid rgba(126,198,230,0.15);
  font-size: 16px;
  color: #7f8c8d;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  box-shadow:
    0 4px 12px rgba(0,0,0,0.1),
    inset 0 1px 0 rgba(255,255,255,0.6);
  position: relative;
  overflow: hidden;
}

.close-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: radial-gradient(circle, rgba(126,198,230,0.2), transparent);
  transition: all 0.3s ease;
  border-radius: 50%;
  transform: translate(-50%, -50%);
}

.close-btn:hover {
  background: linear-gradient(145deg, rgba(126,198,230,0.1), rgba(255,255,255,0.9));
  border-color: rgba(126,198,230,0.3);
  color: #2c3e50;
  transform: scale(1.05) rotate(90deg);
  box-shadow:
    0 6px 20px rgba(126,198,230,0.2),
    inset 0 1px 0 rgba(255,255,255,0.8);
}

.close-btn:hover::before {
  width: 100%;
  height: 100%;
}

.refresh-dialog-content, .custom-dialog-content {
  padding: 20px 24px 24px 24px;
  background: rgba(255,255,255,0.4);
  backdrop-filter: blur(15px);
  max-height: calc(80vh - 120px);
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.refresh-dialog-content::-webkit-scrollbar, .custom-dialog-content::-webkit-scrollbar {
  display: none;
}

/* 单个日签显示样式 */
.single-quote-display {
  margin-bottom: 20px;
}

.quote-card {
  background: linear-gradient(145deg,
    rgba(126,198,230,0.08),
    rgba(255,255,255,0.95),
    rgba(126,198,230,0.05)
  );
  border: 1px solid rgba(126,198,230,0.15);
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  box-shadow:
    0 8px 25px rgba(126,198,230,0.1),
    0 3px 10px rgba(0,0,0,0.05),
    inset 0 1px 0 rgba(255,255,255,0.6);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(15px);
}

.quote-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg,
    rgba(126,198,230,0.3),
    rgba(126,198,230,0.8),
    rgba(126,198,230,0.3)
  );
}

.quote-content {
  font-size: 16px;
  color: #2c3e50;
  line-height: 1.6;
  font-weight: 500;
  text-shadow: 0 1px 2px rgba(0,0,0,0.05);
  margin: 0;
  letter-spacing: 0.3px;
}

.quote-card .quote-author {
  font-size: 14px;
  color: #7f8c8d;
  font-style: italic;
  opacity: 0.8;
}

.refresh-dialog-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.dialog-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 20px;
}

.refresh-more-btn, .confirm-option-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(15px);
  box-shadow:
    0 4px 15px rgba(0,0,0,0.1),
    inset 0 1px 0 rgba(255,255,255,0.3);
  position: relative;
  overflow: hidden;
  min-width: 100px;
}

.refresh-more-btn::before, .confirm-option-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
  transition: left 0.6s ease;
}

.refresh-more-btn:hover::before, .confirm-option-btn:hover::before {
  left: 100%;
}

.refresh-more-btn {
  background: linear-gradient(145deg,
    rgba(126,198,230,0.9),
    rgba(91,184,230,0.9),
    rgba(126,198,230,0.8)
  );
  color: white;
  border: 1px solid rgba(126,198,230,0.2);
  text-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.refresh-more-btn:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow:
    0 8px 25px rgba(126,198,230,0.3),
    inset 0 1px 0 rgba(255,255,255,0.4);
  background: linear-gradient(145deg, #7ec6e6, #5bb8e6, #7ec6e6);
}

.confirm-option-btn {
  background: linear-gradient(145deg,
    rgba(103,194,58,0.9),
    rgba(133,206,97,0.9),
    rgba(103,194,58,0.8)
  );
  color: white;
  border: 1px solid rgba(103,194,58,0.2);
  text-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.confirm-option-btn:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow:
    0 8px 25px rgba(103,194,58,0.3),
    inset 0 1px 0 rgba(255,255,255,0.4);
  background: linear-gradient(145deg, #67c23a, #85ce61, #67c23a);
}

/* 自定义弹窗样式 */
.custom-input-group {
  margin-bottom: 16px;
}

.custom-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.custom-textarea {
  width: 100%;
  padding: 10px;
  border: 2px solid #eee;
  border-radius: 6px;
  font-size: 14px;
  line-height: 1.4;
  resize: vertical;
  transition: border-color 0.3s ease;
  min-height: 80px;
}

.custom-textarea:focus {
  outline: none;
  border-color: #7ec6e6;
  box-shadow: 0 0 0 3px rgba(126,198,230,0.1);
}

.char-count {
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.custom-dialog-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.confirm-custom-btn {
  padding: 10px 20px;
  border: 1px solid rgba(103,194,58,0.3);
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: linear-gradient(135deg, rgba(103,194,58,0.9), rgba(133,206,97,0.9));
  color: white;
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.confirm-custom-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(103,194,58,0.4);
}

.confirm-custom-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.quote-content-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  width: 320px;
  overflow: hidden;
}
.quote-content {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: all 0.3s;
  flex: 1;
}
.quote-content.expanded {
  white-space: normal;
  overflow-x: auto;
  text-overflow: unset;
  max-width: 100%;
}
.arrow {
  cursor: pointer;
  user-select: none;
  font-size: 18px;
  margin: 0 4px;
  color: #ff7fae;
  background: #fff0f6;
  border-radius: 50%;
  padding: 2px 6px;
  box-shadow: 0 1px 4px rgba(255,127,174,0.08);
  z-index: 2;
}
.arrow.left { position: absolute; left: 0; top: 50%; transform: translateY(-50%); }
.arrow.right { position: absolute; right: 0; top: 50%; transform: translateY(-50%); }
.quote-edit-input {
  width: 100%;
  font-size: 16px;
  border: 1px solid #7ec6e6;
  border-radius: 6px;
  padding: 6px 10px;
  outline: none;
  color: #333;
  background: #fff;
  box-shadow: 0 2px 8px rgba(126,198,230,0.08);
  transition: border-color 0.3s;
}
.quote-edit-input:focus {
  border-color: #ff7fae;
}
.edit-btn {
  margin-left: 10px;
  padding: 4px 14px;
  background: linear-gradient(135deg, #ffb6c1, #ff8fab);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}
.edit-btn:hover {
  background: linear-gradient(135deg, #ff8fab, #ffb6c1);
  transform: scale(1.05);
}
.refresh-btn {
  margin-left: 6px;
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #7ec6e6, #5bb8e6);
  color: #fff;
  border: none;
  border-radius: 50%;
  font-size: 0;
  cursor: pointer;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(126,198,230,0.12);
  padding: 0;
}
.refresh-btn:disabled {
  background: #eee;
  color: #aaa;
  cursor: not-allowed;
}
.refresh-btn .refresh-icon { font-size: 12px; }
.refresh-btn:hover .refresh-icon { transform: rotate(180deg); }
.refresh-limit-tip {
  margin-left: 10px;
  color: #ff7fae;
  font-size: 13px;
  font-weight: 500;
  background: #fff0f6;
  border-radius: 6px;
  padding: 2px 10px;
  display: inline-block;
}
.quote-input {
  width: 100%;
  height: 28px;
  line-height: 28px;
  border: none;
  outline: none;
  background: transparent;
  color: #333;
  font-size: 14px;
}
/* 提升右侧操作按钮点击层级，避免被其他元素遮挡 */
.quote-actions {
  position: relative;
  z-index: 2;
  pointer-events: auto;
}

/* 下拉历史面板样式 */
.history-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  overflow: hidden;
  max-height: 0;
  transition: max-height 0.35s ease;
  border-radius: 12px;
  border: 1px solid rgba(126,198,230,0.15);
  background: linear-gradient(145deg, rgba(255,255,255,0.98), rgba(248,250,252,0.95));
  box-shadow: 0 12px 30px rgba(0,0,0,0.12);
  z-index: 9999;
}
.history-dropdown.open { max-height: 260px; }
.history-dropdown-inner { padding: 10px 12px; max-height: 260px; overflow-y: auto; }
.history-header-row { display:flex; align-items:center; justify-content:space-between; margin-bottom: 8px; }
.history-close { border:none; background:transparent; font-size:18px; cursor:pointer; color:#666; }

/* 点击区域提示 */
.quote-content { cursor: pointer; }
.quote-content input.quote-input { cursor: text; }

/* 确保日签容器作为定位参考 */
.daily-quote-container { position: relative; }

/* 空状态占位高度，避免展开后看不出区域 */
.history-empty { min-height: 80px; display:flex; flex-direction:column; align-items:center; justify-content:center; }
</style>
