<template>
  <div class="detail-bg">
    <div class="detail-card">
      <h2 class="detail-title">{{ diary.title }}</h2>
      <div class="detail-meta">
        <span>作者：{{ authorName }}</span>
        <span style="margin-left: 16px;">{{ formatTime(diary.createTime) }}</span>
      </div>
      <!-- 分析结果弹窗（卡片样式） -->
      <div v-if="showResult" class="dialog-mask" @click.self="showResult=false">
        <div class="dialog-content mystic-dialog">
          <h3 class="result-title">情绪分析结果</h3>
          <div v-if="analyzing" class="result-loading">正在分析...</div>
          <div v-else class="result-card">
            <div class="result-row">
              <span class="label">主要情绪</span>
              <span class="value emotion" :style="{color: emotionColor(resultView.emotion)}">{{ resultView.emotion || '—' }}</span>
            </div>
            <div class="result-row">
              <span class="label">置信度</span>
              <span class="value">{{ resultView.confidenceText }}</span>
            </div>
            <div class="result-row">
              <span class="label">强度</span>
              <span class="value">{{ resultView.intensityText }}</span>
            </div>
            <div class="result-row" v-if="resultView.sourceType">
              <span class="label">来源类型</span>
              <span class="value">{{ resultView.sourceType }}</span>
            </div>
            <div class="result-row" v-if="resultView.processing">
              <span class="label">处理耗时</span>
              <span class="value">{{ resultView.processing }} ms</span>
            </div>
            <div class="result-top" v-if="resultView.topEmotions && resultView.topEmotions.length">
              <div class="top-title">情绪分布 Top3</div>
              <div class="top-item" v-for="(e, idx) in resultView.topEmotions" :key="idx">
                <span class="top-name">{{ e.emotion }}</span>
                <div class="top-bar"><div class="top-fill" :style="{width: (e.score*100)+'%'}">{{ (e.score*100).toFixed(1) }}%</div></div>
              </div>
            </div>
          </div>
          <div class="dialog-actions">
            <button class="entry-btn" @click="showResult=false">关闭</button>
          </div>
        </div>
      </div>
      <div class="detail-content">{{ diary.content }}</div>
      <!-- 附件列表 -->
      <div class="media-list" v-if="diary.media && diary.media.length > 0">
        <div class="media-section-title">附件</div>
        <div v-for="media in diary.media" :key="media.mediaId" class="media-item">
          <img v-if="media.mediaType==='image'" :src="getFullUrl(media.mediaUrl)" class="media-img" @click="previewMedia(media)" />
          <video v-else-if="media.mediaType==='video'" :src="getFullUrl(media.mediaUrl)" controls class="media-video" />
          <audio v-else-if="media.mediaType==='audio'" :src="getFullUrl(media.mediaUrl)" controls class="media-audio" />
        </div>
      </div>
      <div class="tags">
        <span v-for="(tag, index) in diary.tags" :key="index" class="tag">{{ tag }}</span>
      </div>
      
      <!-- 只有日记作者才能看到操作按钮，已发布的日记不能编辑 -->
      <div v-if="isAuthor" class="action-buttons">
        <button v-if="diary.status !== 'published'" class="edit-btn" @click="goEdit">编辑日记</button>
        <button class="delete-btn" @click="deleteDiary">删除日记</button>
      </div>
      
      <!-- 附件分析入口 -->
      <div class="analysis-entry" v-if="analysisOptionsLoaded && (options.image || options.audio || options.video)">
        <div class="analysis-section-title">附件分析</div>
        <div class="entry-buttons">
          <button v-if="options.image" class="entry-btn" @click="analyze('image')">
            <i class="el-icon-picture"></i> 图像分析
          </button>
          <button v-if="options.audio" class="entry-btn" @click="analyze('audio')">
            <i class="el-icon-headset"></i> 音频分析
          </button>
          <button v-if="options.video" class="entry-btn" @click="analyze('video')">
            <i class="el-icon-video-camera"></i> 视频分析
          </button>
        </div>
      </div>
      
      <!-- 媒体预览弹窗 -->
      <div v-if="showMediaPreview" class="dialog-mask" @click.self="showMediaPreview=false">
        <div class="dialog-content media-preview-dialog">
          <div class="preview-header">
            <h3>附件预览</h3>
            <button class="close-btn" @click="showMediaPreview=false">×</button>
          </div>
          <div class="preview-content" v-if="previewMediaItem">
            <img v-if="previewMediaItem.mediaType==='image'" :src="getFullUrl(previewMediaItem.mediaUrl)" class="preview-img" />
            <video v-else-if="previewMediaItem.mediaType==='video'" :src="getFullUrl(previewMediaItem.mediaUrl)" controls class="preview-video" />
            <audio v-else-if="previewMediaItem.mediaType==='audio'" :src="getFullUrl(previewMediaItem.mediaUrl)" controls class="preview-audio" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getAnalysisOptions, analyzeFromDiary } from '@/api/emotion'

const route = useRoute()
const router = useRouter()
const diary = ref({ title: '', content: '', createTime: '', media: [], tags: [], userId: null })

// 分析入口可见性
const options = ref({ image: false, audio: false, video: false })
const analysisOptionsLoaded = ref(false)
const analyzing = ref(false)
const analysisResult = ref(null)
const showResult = ref(false)
const previewMediaItem = ref(null)
const showMediaPreview = ref(false)

// 判断是否为日记作者
const isAuthor = computed(() => {
  const currentUserId = window.sessionStorage.getItem('uid')
  return currentUserId && diary.value.userId && currentUserId === diary.value.userId.toString()
})

// 作者名显示
const authorName = computed(() => {
  const currentUserId = window.sessionStorage.getItem('uid')
  if (currentUserId && diary.value.userId && currentUserId === diary.value.userId.toString()) {
    return '我'
  }
  if (diary.value.nickname) return diary.value.nickname
  return `用户ID:${diary.value.userId}`
})

// 时间格式化
function formatTime(time) {
  if (!time) return ''
  const t = typeof time === 'number' ? time : Number(time)
  return dayjs(t).format('YYYY-MM-DD HH:mm:ss')
}

// 处理媒体文件URL
function getFullUrl(url) {
  if (!url) return '';
  // 如果已经是完整的URL（包含协议），移除localhost硬编码
  if (url.startsWith('http://') || url.startsWith('https://')) {
    // 移除可能存在的localhost硬编码，替换为相对路径
    if (url.includes('localhost:8000') || url.includes('localhost:8081') || url.includes('127.0.0.1')) {
      try {
        const urlObj = new URL(url)
        const relativeUrl = urlObj.pathname + (urlObj.search || '')
        console.log('Media URL:', url, '-> Relative URL:', relativeUrl);
        return relativeUrl
      } catch (e) {
        const match = url.match(/https?:\/\/[^\/]+(\/.*)/)
        if (match) {
          console.log('Media URL:', url, '-> Relative URL:', match[1]);
          return match[1]
        }
      }
    }
    return url;
  }
  // 使用相对路径，通过当前域名访问（适配开发和生产环境）
  console.log('Media URL:', url, '-> Relative URL:', url);
  return fullUrl;
}

async function loadDiary() {
  try {
  const res = await request.get(`/api/diary/${route.params.id}`)
    diary.value = res.data.data || { title: '', content: '', createTime: '', media: [], tags: [], userId: null }
    // 添加调试信息
    console.log('Loaded diary:', diary.value)
    console.log('Media files:', diary.value.media)
    console.log('Media count:', diary.value.media ? diary.value.media.length : 0)
    // 加载分析入口
    await loadAnalysisOptions()
  } catch (error) {
    console.error('Error loading diary:', error)
    ElMessage.error('加载日记失败')
  }
}

async function loadAnalysisOptions() {
  try {
    const resp = await getAnalysisOptions(route.params.id)
    if (resp?.data?.code === 200) {
      options.value = resp.data.data || { image: false, audio: false, video: false }
    }
  } catch (e) {
    console.warn('获取分析入口失败', e)
  } finally {
    analysisOptionsLoaded.value = true
  }
}

// 已删除情绪分析总览功能

async function analyze(type) {
  try {
    analyzing.value = true
    console.log('开始分析，类型:', type, '日记ID:', route.params.id)
    const resp = await analyzeFromDiary(route.params.id, type)
    console.log('分析响应:', resp)
    // request.js 的响应拦截器已经处理了错误，如果到这里说明 code === 200
    // resp 是 axios response 对象，resp.data 是后端返回的数据 {code: 200, data: {...}}
    if (resp?.data) {
      analysisResult.value = resp.data.data
      showResult.value = true
      ElMessage.success('分析完成')
    } else {
      console.error('响应数据为空:', resp)
      ElMessage.error('分析失败：响应数据为空')
    }
  } catch (e) {
    console.error('分析失败', e)
    console.error('错误详情:', {
      message: e.message,
      code: e.code,
      response: e.response,
      data: e.data
    })
    // 错误信息已经在响应拦截器中显示，这里不需要再次显示
    // 但如果需要更详细的错误信息，可以检查 e.response 或 e.data
    if (e.data?.message) {
      ElMessage.error(e.data.message)
    } else if (e.response?.data?.message) {
      ElMessage.error(e.response.data.message)
    } else if (e.message) {
      ElMessage.error(e.message)
    } else {
      ElMessage.error('分析失败，请稍后重试')
    }
  } finally {
    analyzing.value = false
  }
}

function pretty(obj) {
  try { return JSON.stringify(obj || {}, null, 2) } catch { return String(obj) }
}

// 结果视图映射
const resultView = computed(() => {
  const d = analysisResult.value || {}
  const emotion = d.emotion || d.primaryEmotion || ''
  const confidence = typeof d.confidence === 'number' ? d.confidence : (typeof d.confidence === 'string' ? Number(d.confidence) : null)
  const intensity = d.intensity
  const top = Array.isArray(d.top_emotions) ? d.top_emotions : null
  const processing = d.processing_time_ms
  const sourceType = d.sourceType
  return {
    emotion,
    confidenceText: confidence!=null ? (confidence<=1? (confidence*100).toFixed(1)+'%' : confidence.toFixed(1)+'%') : '—',
    intensityText: intensity!=null ? String(intensity) : '—',
    topEmotions: top,
    processing: processing,
    sourceType
  }
})

function emotionColor(e) {
  const map = { '开心':'#4ade80','兴奋':'#f59e0b','平静':'#60a5fa','无聊':'#9ca3af','疲惫':'#f59e0b','难过':'#8b5cf6','焦虑':'#ef4444','愤怒':'#dc2626' }
  return map[e] || '#3b82f6'
}

function previewMedia(media) {
  previewMediaItem.value = media
  showMediaPreview.value = true
}

function goEdit() {
  router.push(`/user/diary/edit/${route.params.id}`)
}

async function deleteDiary() {
  try {
    await ElMessageBox.confirm('确定要删除这篇日记吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/api/diary/${route.params.id}`)
    ElMessage.success('删除成功')
    router.push('/user/diary')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => { 
  loadDiary() 
})
</script>
<style scoped>
.detail-bg {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 40px 0;
  background: rgba(255,255,255,0.85) !important;
}
.detail-card {
  background: rgba(255,255,255,0.95) !important;
  border-radius: 18px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  padding: 36px 32px 32px 32px;
  width: 600px;
  max-width: 95vw;
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.detail-title {
  font-size: 28px;
  font-weight: bold;
  color: #7ec6e6;
  margin-bottom: 10px;
  letter-spacing: 2px;
  text-align: center;
}
.detail-meta {
  font-size: 14px;
  color: #999;
  margin-bottom: 10px;
  text-align: center;
}
.detail-content {
  font-size: 16px;
  color: #333;
  margin-bottom: 10px;
  white-space: pre-line;
}
.media-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 10px;
}
.media-section-title, .analysis-section-title {
  font-size: 16px;
  font-weight: 600;
  color: #E67E73;
  margin-bottom: 8px;
}
.media-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.media-img {
  max-width: 200px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(126,198,230,0.10);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.media-img:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(126,198,230,0.20);
}
.media-video {
  max-width: 200px;
  border-radius: 8px;
  margin-top: 6px;
}
.media-audio {
  width: 200px;
  margin-top: 6px;
}
.tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 10px;
  justify-content: center;
}
.tag {
  background: #7ec6e6;
  color: #fff;
  padding: 4px 14px;
  border-radius: 14px;
  font-size: 14px;
}
.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-bottom: 10px;
}
.edit-btn, .delete-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}
.edit-btn {
  background: #7ec6e6;
  color: white;
}
.edit-btn:hover {
  background: #6bb5d5;
}
.delete-btn {
  background: #f56c6c;
  color: white;
}
.delete-btn:hover {
  background: #e55a5a;
}
.analysis-entry { 
  display: flex; 
  flex-direction: column; 
  gap: 10px; 
  margin-top: 10px;
}
.entry-buttons { display: flex; gap: 10px; flex-wrap: wrap; justify-content: center; }
.entry-btn {
  background: #fff;
  color: #E67E73;
  border: 1px solid #E67E73;
  border-radius: 8px;
  padding: 10px 16px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.entry-btn:hover {
  background: #E67E73;
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(230,126,115,0.2);
}

/* 结果弹窗样式 */
.result-title { text-align:center; margin: 0 0 12px; color:#1e3a8a; }
.result-card { background:#fff; border:1px solid #e5e7eb; border-radius:12px; padding:14px; display:flex; flex-direction:column; gap:10px; }
.result-row { display:flex; justify-content:space-between; align-items:center; }
.label { color:#64748b; font-size:14px; }
.value { color:#334155; font-weight:600; }
.value.emotion { font-size:18px; }
.top-title { margin-top:8px; font-weight:600; color:#1e40af; }
.top-item { display:flex; align-items:center; gap:10px; margin-top:6px; }
.top-name { width:64px; color:#334155; font-size:14px; }
.top-bar { flex:1; background:#e5e7eb; border-radius:10px; overflow:hidden; height:16px; }
.top-fill { height:100%; background:linear-gradient(90deg,#60a5fa,#3b82f6); color:#fff; font-size:12px; display:flex; align-items:center; justify-content:flex-end; padding-right:6px; }

/* 弹窗遮罩与容器 */
.dialog-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.mystic-dialog {
  background: #fff;
  border-radius: 12px;
  padding: 18px;
  width: 420px;
  max-width: 92vw;
  box-shadow: 0 10px 25px rgba(0,0,0,0.12);
}
.result-loading { text-align:center; padding: 20px; color:#64748b; }

/* 媒体预览弹窗样式 */
.media-preview-dialog {
  max-width: 90vw;
  max-height: 90vh;
  overflow: auto;
}
.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}
.preview-header h3 {
  margin: 0;
  color: #1e3a8a;
}
.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: #64748b;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}
.close-btn:hover {
  background: #f3f4f6;
  color: #1f2937;
}
.preview-content {
  display: flex;
  justify-content: center;
  align-items: center;
}
.preview-img {
  max-width: 100%;
  max-height: 70vh;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.preview-video {
  max-width: 100%;
  max-height: 70vh;
  border-radius: 8px;
}
.preview-audio {
  width: 100%;
  min-width: 300px;
}
</style> 