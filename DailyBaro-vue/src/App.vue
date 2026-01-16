<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import GlobalChat from '@/components/GlobalChat.vue'

const unreadReminders = ref([])
const showCapsuleReminder = ref(false)
const showImagePreview = ref(false)
const previewImageUrl = ref('')
let reminderTimer = null
let reminderCheckTimer = null

// 格式化时间显示
function formatTime(timestamp) {
  if (!timestamp) return '未知时间'
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 启动定时检查提醒
function startReminderCheck() {
  // 每3分钟检查一次提醒
  reminderCheckTimer = setInterval(() => {
    loadUnreadReminders()
  }, 3 * 60 * 1000)
}

// 停止定时检查
function stopReminderCheck() {
  if (reminderCheckTimer) {
    clearInterval(reminderCheckTimer)
    reminderCheckTimer = null
  }
}

// 页面激活时检查提醒
function handlePageVisibilityChange() {
  if (!document.hidden) {
    console.log('页面激活，检查提醒')
    loadUnreadReminders()
  }
}

// 获取完整的媒体文件URL（使用相对路径，适配开发和生产环境）
function getFullUrl(filePath) {
  if (!filePath) return ''
  // 如果已经是完整的URL（包含协议），移除localhost硬编码
  if (filePath.startsWith('http://') || filePath.startsWith('https://')) {
    // 移除可能存在的localhost硬编码，替换为相对路径
    if (filePath.includes('localhost:8000') || filePath.includes('localhost:8081') || filePath.includes('127.0.0.1')) {
      try {
        const urlObj = new URL(filePath)
        const relativeUrl = urlObj.pathname + (urlObj.search || '')
        console.log('媒体文件URL:', { original: filePath, relative: relativeUrl })
        return relativeUrl
      } catch (e) {
        const match = filePath.match(/https?:\/\/[^\/]+(\/.*)/)
        if (match) {
          console.log('媒体文件URL:', { original: filePath, relative: match[1] })
          return match[1]
        }
      }
    }
    return filePath
  }
  // 使用相对路径，通过当前域名访问（适配开发和生产环境）
  console.log('媒体文件URL:', { original: filePath, relative: filePath })
  return filePath
}

// 获取媒体文件类型
function getMediaType(filePath) {
  if (!filePath) return ''
  const lowerPath = filePath.toLowerCase()
  if (lowerPath.match(/\.(jpg|jpeg|png|gif|webp)$/)) return 'image'
  if (lowerPath.match(/\.(mp3|wav|ogg|aac|m4a|nvm|ncm)$/)) return 'audio' // 添加ncm支持在线播放
  if (lowerPath.match(/\.(mp4|avi|mov|wmv|flv)$/)) return 'video'
  return 'other'
}

// 打开图片预览
function openImagePreview(imageUrl) {
  previewImageUrl.value = imageUrl
  showImagePreview.value = true
}

// 关闭图片预览
function closeImagePreview() {
  showImagePreview.value = false
  previewImageUrl.value = ''
}

// 处理图片加载错误
function handleImageError(event) {
  console.error('图片加载失败:', event.target.src)
  event.target.style.display = 'none'
  event.target.nextElementSibling.textContent = '图片加载失败'
}

// 处理音频加载错误
function handleAudioError(event) {
  console.error('音频加载失败:', event.target.src)
  ElMessage.error('音频文件无法播放')
}

// 处理视频加载错误
function handleVideoError(event) {
  console.error('视频加载失败:', event.target.src)
  ElMessage.error('视频文件无法播放')
}

// 播放音频
function playAudio(audioUrl) {
  console.log('播放音频:', audioUrl)
  // 可以在这里添加音频播放逻辑
}

// 播放视频
function playVideo(videoUrl) {
  console.log('播放视频:', videoUrl)
  // 可以在这里添加视频播放逻辑
}

// 获取未读提醒
async function loadUnreadReminders() {
  try {
    const uid = window.sessionStorage.getItem('uid')
    if (!uid) {
      // 未登录不请求，避免后端500
      return
    }
    const res = await request.get('/api/capsules/reminders/unread')
    if (res.data.code === 200) {
      unreadReminders.value = res.data.data || []
      showCapsuleReminder.value = unreadReminders.value.length > 0
      
      // 如果有新提醒，显示通知
      if (unreadReminders.value.length > 0) {
        console.log('发现新的情绪胶囊提醒:', unreadReminders.value.length, '个')
        // 可以在这里添加浏览器通知
        if ('Notification' in window && Notification.permission === 'granted') {
          new Notification('情绪胶囊提醒', {
            body: `您有 ${unreadReminders.value.length} 个情绪胶囊已到开启时间`,
            icon: '/src/static/imgs/back.png'
          })
        }
      }
    }
  } catch (e) {
    console.error('加载提醒失败:', e)
  }
}

async function markRead(capsuleId) {
  const uid = window.sessionStorage.getItem('uid')
  if (!uid) return
  await request.post(`/api/capsules/reminders/read/${capsuleId}`)
  unreadReminders.value = unreadReminders.value.filter(r => r.capsuleId !== capsuleId)
  if (unreadReminders.value.length === 0) showCapsuleReminder.value = false
  console.log('已读后showCapsuleReminder:', showCapsuleReminder.value)
}

onMounted(() => {
  loadUnreadReminders()
  reminderTimer = setInterval(loadUnreadReminders, 60 * 1000) // 每分钟轮询一次
  startReminderCheck()
  document.addEventListener('visibilitychange', handlePageVisibilityChange)
})

onUnmounted(() => {
  // 清理定时器和事件监听器
  if (reminderTimer) {
    clearInterval(reminderTimer)
  }
  stopReminderCheck()
  document.removeEventListener('visibilitychange', handlePageVisibilityChange)
})
</script>

<template>
  <div id="app" class="global-bg">
    <router-view></router-view>
    <GlobalChat />
    
    <!-- 情绪胶囊提醒弹窗 -->
    <div v-if="showCapsuleReminder" class="capsule-reminder-overlay" @click="showCapsuleReminder = false">
      <div class="capsule-reminder-modal" @click.stop>
        <div class="capsule-reminder-header">
          <div class="capsule-reminder-title">
            <i class="el-icon-bell"></i>
            <span>情绪胶囊提醒</span>
          </div>
          <button class="capsule-reminder-close" @click="showCapsuleReminder = false">
            <i class="el-icon-close"></i>
          </button>
        </div>
        
        <div class="capsule-reminder-content">
          <div class="capsule-reminder-intro">
            <p>🎉 您的心情胶囊已到开启时间！</p>
            <p>点击下方胶囊查看您曾经的心情记录</p>
          </div>
          
          <div class="capsule-reminder-list">
            <div v-for="reminder in unreadReminders" :key="reminder.capsuleId" class="capsule-reminder-item">
              <div class="capsule-reminder-header-item">
                <div class="capsule-reminder-emotion">
                  <span class="capsule-reminder-emoji">💌</span>
                  <span class="capsule-reminder-emotion-text">心情胶囊</span>
                </div>
                <div class="capsule-reminder-time">
                  <i class="el-icon-time"></i>
                  <span>{{ formatTime(reminder.openTime) }}</span>
                </div>
              </div>
              
              <div class="capsule-reminder-content-text">
                <p>{{ reminder.content ? reminder.content.slice(0, 100) : '（无内容）' }}</p>
              </div>
              
              <!-- 媒体文件预览 -->
              <div v-if="reminder.media && reminder.media.length > 0" class="capsule-reminder-media">
                <div class="capsule-reminder-media-title">附件：</div>
                <div class="capsule-reminder-media-list">
                  <div v-for="(media, index) in reminder.media" :key="index" class="capsule-reminder-media-item">
                    <!-- 图片预览 -->
                    <div v-if="getMediaType(media.mediaUrl) === 'image'" class="capsule-reminder-image">
                      <img 
                        :src="getFullUrl(media.mediaUrl)" 
                        class="capsule-reminder-image-preview"
                        @click="openImagePreview(getFullUrl(media.mediaUrl))"
                        alt="图片预览"
                        @error="handleImageError"
                      />
                    </div>
                    
                    <!-- 音频预览 -->
                    <div v-else-if="getMediaType(media.mediaUrl) === 'audio'" class="capsule-reminder-audio">
                      <div class="capsule-reminder-audio-header">
                        <i class="el-icon-headset"></i>
                        <span>音频文件</span>
                      </div>
                      <audio 
                        :src="getFullUrl(media.mediaUrl)" 
                        controls 
                        class="capsule-reminder-audio-player" 
                        preload="metadata" 
                        @error="handleAudioError"
                      ></audio>
                    </div>
                    
                    <!-- 视频预览 -->
                    <div v-else-if="getMediaType(media.mediaUrl) === 'video'" class="capsule-reminder-video">
                      <div class="capsule-reminder-video-header">
                        <i class="el-icon-video-camera"></i>
                        <span>视频文件</span>
                      </div>
                      <video 
                        :src="getFullUrl(media.mediaUrl)" 
                        controls 
                        class="capsule-reminder-video-player" 
                        preload="metadata" 
                        @error="handleVideoError"
                      ></video>
                    </div>
                    
                    <!-- 其他文件 -->
                    <div v-else class="capsule-reminder-other">
                      <i class="el-icon-document"></i>
                      <span>{{ media.mediaUrl.split('/').pop() }}</span>
                      <a :href="getFullUrl(media.mediaUrl)" download class="capsule-reminder-download">下载</a>
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="capsule-reminder-actions">
                <el-button size="small" type="primary" @click="markRead(reminder.capsuleId)" class="capsule-reminder-btn">
                  我知道了
                </el-button>
              </div>
            </div>
          </div>
          
          <div v-if="!unreadReminders.length" class="capsule-reminder-empty">
            无未读提醒
          </div>
        </div>
      </div>
    </div>
    
    <!-- 图片预览弹窗 -->
    <el-dialog v-model="showImagePreview" title="图片预览" width="80%" :close-on-click-modal="true" @close="closeImagePreview">
      <div class="image-preview-container">
        <img :src="previewImageUrl" class="preview-image" alt="预览图片" />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.global-bg {
  min-height: 100vh;
  background: url('/static/imgs/bg.jpg') no-repeat center center fixed;
  background-size: cover;
  background-color: transparent !important;
}

/* 情绪胶囊提醒弹窗样式 */
.capsule-reminder-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.3s ease-out;
  backdrop-filter: blur(5px);
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.capsule-reminder-modal {
  background: linear-gradient(135deg, #fff5f7, #fff0f5, #fdf2f8);
  border-radius: 25px;
  box-shadow: 
    0 25px 80px rgba(255, 182, 193, 0.4),
    0 10px 30px rgba(255, 105, 180, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  max-width: 600px;
  width: 90%;
  max-height: 85vh;
  overflow-y: auto;
  animation: slideUp 0.4s ease-out;
  border: 2px solid rgba(255, 182, 193, 0.4);
  position: relative;
}

.capsule-reminder-modal::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 5px;
  background: linear-gradient(90deg, #ff69b4, #ff1493, #ff69b4);
  border-radius: 25px 25px 0 0;
}

@keyframes slideUp {
  from {
    transform: translateY(50px) scale(0.95);
    opacity: 0;
  }
  to {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
}

.capsule-reminder-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25px 30px 20px;
  border-bottom: 1px solid rgba(255, 182, 193, 0.3);
  background: linear-gradient(135deg, rgba(255, 182, 193, 0.15), rgba(255, 192, 203, 0.15));
  position: relative;
}

.capsule-reminder-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, #ff69b4, #ff1493);
  border-radius: 1px;
}

.capsule-reminder-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 700;
  color: #ff69b4;
  text-shadow: 0 1px 2px rgba(255, 105, 180, 0.2);
}

.capsule-reminder-title i {
  color: #ff1493;
  font-size: 24px;
  animation: bellRing 2s ease-in-out infinite;
}

@keyframes bellRing {
  0%, 100% { transform: rotate(0deg); }
  10%, 30% { transform: rotate(10deg); }
  20% { transform: rotate(-10deg); }
  40% { transform: rotate(0deg); }
}

.capsule-reminder-close {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 182, 193, 0.3);
  color: #ff69b4;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  box-shadow: 0 2px 8px rgba(255, 182, 193, 0.2);
}

.capsule-reminder-close:hover {
  background: rgba(255, 182, 193, 0.2);
  color: #ff1493;
  transform: scale(1.1) rotate(90deg);
  box-shadow: 0 4px 12px rgba(255, 182, 193, 0.3);
}

.capsule-reminder-content {
  padding: 30px;
}

.capsule-reminder-intro {
  text-align: center;
  margin-bottom: 30px;
  color: #666;
  line-height: 1.6;
  background: linear-gradient(135deg, rgba(255, 182, 193, 0.1), rgba(255, 192, 203, 0.1));
  padding: 20px;
  border-radius: 15px;
  border: 1px solid rgba(255, 182, 193, 0.2);
}

.capsule-reminder-intro p {
  margin: 8px 0;
  font-size: 15px;
  color: #ff69b4;
  font-weight: 500;
}

.capsule-reminder-intro p:first-child {
  font-size: 18px;
  font-weight: 600;
  color: #ff1493;
}

.capsule-reminder-list {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.capsule-reminder-item {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(255, 248, 250, 0.95));
  border-radius: 20px;
  padding: 25px;
  border: 2px solid rgba(255, 182, 193, 0.3);
  box-shadow: 
    0 8px 25px rgba(255, 182, 193, 0.2),
    0 4px 12px rgba(255, 105, 180, 0.1);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.capsule-reminder-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, #ff69b4, #ff1493, #ff69b4);
}

.capsule-reminder-item:hover {
  transform: translateY(-3px);
  box-shadow: 
    0 12px 35px rgba(255, 182, 193, 0.3),
    0 6px 18px rgba(255, 105, 180, 0.15);
  border-color: rgba(255, 182, 193, 0.5);
}

.capsule-reminder-header-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.capsule-reminder-emotion {
  display: flex;
  align-items: center;
  gap: 10px;
  background: linear-gradient(135deg, rgba(255, 182, 193, 0.3), rgba(255, 192, 203, 0.3));
  padding: 12px 18px;
  border-radius: 20px;
  border: 1px solid rgba(255, 182, 193, 0.4);
  box-shadow: 0 2px 8px rgba(255, 182, 193, 0.2);
}

.capsule-reminder-emoji {
  font-size: 20px;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.capsule-reminder-emotion-text {
  font-size: 15px;
  font-weight: 600;
  color: #ff69b4;
  text-shadow: 0 1px 2px rgba(255, 105, 180, 0.2);
}

.capsule-reminder-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #999;
  background: rgba(255, 255, 255, 0.8);
  padding: 8px 12px;
  border-radius: 15px;
  border: 1px solid rgba(255, 182, 193, 0.2);
}

.capsule-reminder-time i {
  color: #ff1493;
  font-size: 14px;
}

.capsule-reminder-content-text {
  margin-bottom: 20px;
}

.capsule-reminder-content-text p {
  font-size: 15px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 12px;
  padding: 15px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  border-left: 4px solid #ff69b4;
}

.capsule-reminder-media {
  margin-bottom: 20px;
}

.capsule-reminder-media-title {
  font-weight: bold;
  color: #606266;
  margin-bottom: 12px;
  font-size: 14px;
}

.capsule-reminder-media-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.capsule-reminder-media-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.capsule-reminder-image {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.capsule-reminder-image-preview {
  max-width: 200px;
  max-height: 150px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.capsule-reminder-image-preview:hover {
  transform: scale(1.05);
}

.capsule-reminder-audio, .capsule-reminder-video {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 10px;
  background-color: #fafafa;
}

.capsule-reminder-audio-header, .capsule-reminder-video-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-weight: bold;
  color: #606266;
}

.capsule-reminder-audio-player, .capsule-reminder-video-player {
  width: 100%;
  max-width: 300px;
}

.capsule-reminder-other {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background-color: #fafafa;
}

.capsule-reminder-download {
  color: #409eff;
  text-decoration: none;
  margin-left: 8px;
}

.capsule-reminder-download:hover {
  color: #ff69b4;
}

.capsule-reminder-actions {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.capsule-reminder-btn {
  background: linear-gradient(135deg, #ff69b4, #ff1493);
  border: none;
  color: white;
  padding: 10px 20px;
  border-radius: 20px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 182, 193, 0.4);
}

.capsule-reminder-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 182, 193, 0.5);
}

.capsule-reminder-empty {
  text-align: center;
  color: #999;
  font-style: italic;
  padding: 20px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .capsule-reminder-modal {
    width: 95%;
    margin: 20px;
  }
  
  .capsule-reminder-content {
    padding: 20px;
  }
  
  .capsule-reminder-header-item {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
}

.reminder-item {
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background-color: #fafafa;
}

.reminder-time {
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.reminder-content {
  margin-bottom: 12px;
  color: #606266;
  line-height: 1.5;
}

.media-preview {
  margin-bottom: 12px;
}

.media-title {
  font-weight: bold;
  color: #606266;
  margin-bottom: 8px;
}

.media-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.media-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.media-image-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.media-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
  cursor: pointer;
  border: 2px solid #dcdfe6;
  transition: all 0.3s ease;
}

.media-image:hover {
  transform: scale(1.05);
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.image-hint {
  font-size: 12px;
  color: #909399;
  text-align: center;
}

.media-audio, .media-video {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.media-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  color: #606266;
}

.media-other {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: #f5f5f5;
  border-radius: 4px;
}

.audio-controls, .video-controls {
  margin-top: 8px;
}

.play-btn {
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 12px;
  cursor: pointer;
  margin-bottom: 8px;
}

.play-btn:hover {
  background: #337ecc;
}

.audio-player, .video-player {
  width: 100%;
  max-width: 100%;
}

.audio-player {
  height: 40px;
}

.video-player {
  max-height: 200px;
}

.mark-read-btn {
  margin-top: 15px;
}

.image-preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.preview-image {
  max-width: 100%;
  max-height: 80vh;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.el-input {
  width: 180px;
}

:deep(.el-input__wrapper) {
  border: none;
}

/* 确保音频和视频播放器在弹窗中正常显示 */
:deep(.el-dialog__body) {
  max-height: 70vh;
  overflow-y: auto;
}
</style>
