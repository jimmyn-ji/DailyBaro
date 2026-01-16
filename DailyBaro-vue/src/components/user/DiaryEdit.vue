<template>
  <div class="edit-bg">
      <h2 class="edit-title">{{ isEdit ? '编辑日记' : '新增日记' }}</h2>
    <div class="edit-content">
      
      <div class="form-group">
        <label class="form-label">
          <span class="label-icon">✏️</span>
          标题
        </label>
        <el-input v-model="title" class="edit-input" placeholder="请输入日记标题" />
      </div>
      
      <div class="form-group">
        <label class="form-label">
          <span class="label-icon">📅</span>
          日期
        </label>
        <el-date-picker v-model="selectedDate" type="date" placeholder="选择日期" class="edit-date" />
      </div>
      
      <div class="form-group">
        <label class="form-label">
          <span class="label-icon">😊</span>
          情绪状态
          <el-button 
            type="primary" 
            size="small" 
            @click="analyzeEmotion" 
            :loading="analyzing"
            style="margin-left: 10px;"
          >
            <span class="label-icon">🤖</span>
            智能识别
          </el-button>
        </label>
        <el-select v-model="selectedEmotion" placeholder="请选择情绪" class="edit-emotion" @change="onEmotionChange">
          <el-option label="开心" value="开心" />
          <el-option label="难过" value="难过" />
          <el-option label="焦虑" value="焦虑" />
          <el-option label="兴奋" value="兴奋" />
          <el-option label="平静" value="平静" />
          <el-option label="愤怒" value="愤怒" />
          <el-option label="悲伤" value="悲伤" />
          <el-option label="放松" value="放松" />
          <el-option label="满足" value="满足" />
          <el-option label="紧张" value="紧张" />
          <el-option label="困惑" value="困惑" />
          <el-option label="期待" value="期待" />
        </el-select>
        
        <!-- 情绪分析结果展示 -->
        <div v-if="emotionAnalysisResult" class="emotion-analysis-result">
          <div class="analysis-header">
            <span class="analysis-title">🤖 AI情绪分析结果</span>
            <el-button size="small" @click="emotionAnalysisResult = null">关闭</el-button>
          </div>
          <div class="analysis-content">
            <div class="analysis-item">
              <span class="analysis-label">主要情绪：</span>
              <span class="analysis-value">{{ emotionAnalysisResult.emotion }}</span>
            </div>
            <div class="analysis-item">
              <span class="analysis-label">情绪强度：</span>
              <span class="analysis-value">{{ emotionAnalysisResult.intensity }}/10</span>
            </div>
            <div class="analysis-item">
              <span class="analysis-label">置信度：</span>
              <span class="analysis-value">{{ (emotionAnalysisResult.confidence * 100).toFixed(1) }}%</span>
            </div>
            <div class="analysis-item">
              <span class="analysis-label">建议标签：</span>
              <div class="suggested-tags">
                <el-tag 
                  v-for="tag in emotionAnalysisResult.suggested_tags" 
                  :key="tag.emotion"
                  size="small"
                  @click="addSuggestedTag(tag.emotion)"
                  style="margin-right: 5px; cursor: pointer;"
                >
                  {{ tag.emotion }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="form-group">
        <label class="form-label">
          <span class="label-icon">📝</span>
          内容
        </label>
        <el-input type="textarea" v-model="content" class="edit-textarea" placeholder="记录你今天的心情和感受吧..." />
      </div>
      
<!--      <div class="form-group">-->
<!--        <label class="form-label">-->
<!--          <span class="label-icon"></span>-->
<!--          标签-->
<!--        </label>-->
<!--        <div class="tags-input-container">-->
<!--          <el-input -->
<!--            v-model="tagInput" -->
<!--            placeholder="输入标签，回车添加" -->
<!--            @keyup.enter="addTag"-->
<!--            class="tag-input"-->
<!--          />-->
<!--          <div class="tags-display">-->
<!--            <el-tag -->
<!--              v-for="(tag, index) in tags" -->
<!--              :key="index" -->
<!--              closable -->
<!--              @close="removeTag(index)"-->
<!--              class="tag-item"-->
<!--            >-->
<!--              {{ tag }}-->
<!--            </el-tag>-->
<!--          </div>-->
<!--        </div>-->
<!--      </div>-->


      <div class="form-group">
        <label class="form-label">
          <span class="label-icon">📎</span>
          媒体文件
          <span class="label-icon">📎</span>
        </label>
        <input type="file" multiple @change="handleFileChange" accept="image/*,video/*,audio/*" class="file-input" />
        <div class="file-hint">支持图片、视频、音频上传，单文件最大50MB，支持mp3、wav、mp4、mov等格式。</div>
      </div>
      <div class="media-preview">
        <div v-for="(file, idx) in allMedia" :key="getFileKey(file, idx)" class="media-item blog-style">
          <!-- 图片：保持原有预览方式 -->
          <div v-if="file.type==='image'" class="media-thumb image-thumb" @click="previewImage(getFullUrl(file.url), file)" title="点击预览">
            <img :src="getFullUrl(file.url)" class="thumb-img" />
            <div class="thumb-overlay">
              <span class="thumb-action">预览</span>
            </div>
          </div>
          
          <!-- 视频：内嵌播放器 -->
          <div v-else-if="file.type==='video'" class="blog-video-player">
            <div class="video-container">
              <video
                :ref="el => setVideoRef(el, getFileKey(file, idx))"
                :src="getFullUrl(file.url)"
                class="blog-video"
                @loadedmetadata="(e) => onMediaLoaded(e, getFileKey(file, idx), 'video')"
                @timeupdate="(e) => onMediaTimeUpdate(e, getFileKey(file, idx))"
                @play="() => onMediaPlay(getFileKey(file, idx))"
                @pause="() => onMediaPause(getFileKey(file, idx))"
                @ended="() => onMediaEnded(getFileKey(file, idx))"
                @error="() => onMediaError(getFileKey(file, idx))"
              />
              <div class="video-controls">
                <button class="play-pause-btn" @click="togglePlayPause(getFileKey(file, idx), 'video')">
                  <span v-if="mediaStates[getFileKey(file, idx)]?.playing">❚❚</span>
                  <span v-else>▶</span>
                </button>
                <div class="progress-container">
                  <input
                    type="range"
                    class="progress-bar"
                    :value="mediaStates[getFileKey(file, idx)]?.currentTime || 0"
                    :max="mediaStates[getFileKey(file, idx)]?.duration || 0"
                    @input="(e) => seekMedia(e, getFileKey(file, idx), 'video')"
                    min="0"
                    step="0.1"
                  />
            </div>
                <div class="time-display">
                  {{ formatTime(mediaStates[getFileKey(file, idx)]?.currentTime || 0) }} / {{ formatTime(mediaStates[getFileKey(file, idx)]?.duration || 0) }}
          </div>
                <div class="volume-control">
                  <span class="volume-icon">🔊</span>
                  <input
                    type="range"
                    class="volume-bar"
                    :value="mediaStates[getFileKey(file, idx)]?.volume !== undefined ? mediaStates[getFileKey(file, idx)].volume * 100 : 100"
                    @input="(e) => setVolume(e, getFileKey(file, idx), 'video')"
                    min="0"
                    max="100"
                  />
            </div>
          </div>
            </div>
            <div class="media-info">
            <span class="media-name" :title="file.name || file.originName">{{ file.name || file.originName }}</span>
            <div class="media-actions">
              <el-button size="small" @click.stop="quickAnalyze(file)">分析</el-button>
              <el-button size="small" type="danger" @click.stop="removeFile(file, idx)">删除</el-button>
              </div>
            </div>
          </div>
          
          <!-- 音频：内嵌播放器 -->
          <div v-else-if="file.type==='audio'" class="blog-audio-player">
            <div class="audio-container">
              <audio
                :ref="el => setAudioRef(el, getFileKey(file, idx))"
                :src="getFullUrl(file.url)"
                class="blog-audio"
                @loadedmetadata="(e) => onMediaLoaded(e, getFileKey(file, idx), 'audio')"
                @timeupdate="(e) => onMediaTimeUpdate(e, getFileKey(file, idx))"
                @play="() => onMediaPlay(getFileKey(file, idx))"
                @pause="() => onMediaPause(getFileKey(file, idx))"
                @ended="() => onMediaEnded(getFileKey(file, idx))"
                @error="() => onMediaError(getFileKey(file, idx))"
              />
              <div class="audio-controls">
                <button class="play-pause-btn audio-btn" @click="togglePlayPause(getFileKey(file, idx), 'audio')">
                  <span v-if="mediaStates[getFileKey(file, idx)]?.playing">❚❚</span>
                  <span v-else>▶</span>
                </button>
                <div class="audio-info">
                  <div class="audio-name">{{ file.name || file.originName }}</div>
                  <div class="progress-container">
                    <input
                      type="range"
                      class="progress-bar"
                      :value="mediaStates[getFileKey(file, idx)]?.currentTime || 0"
                      :max="mediaStates[getFileKey(file, idx)]?.duration || 0"
                      @input="(e) => seekMedia(e, getFileKey(file, idx), 'audio')"
                      min="0"
                      step="0.1"
                    />
                  </div>
                  <div class="time-display">
                    {{ formatTime(mediaStates[getFileKey(file, idx)]?.currentTime || 0) }} / {{ formatTime(mediaStates[getFileKey(file, idx)]?.duration || 0) }}
                  </div>
                </div>
                <div class="volume-control">
                  <span class="volume-icon">🔊</span>
                  <input
                    type="range"
                    class="volume-bar"
                    :value="mediaStates[getFileKey(file, idx)]?.volume !== undefined ? mediaStates[getFileKey(file, idx)].volume * 100 : 100"
                    @input="(e) => setVolume(e, getFileKey(file, idx), 'audio')"
                    min="0"
                    max="100"
                  />
                </div>
                <div class="media-actions">
                  <el-button size="small" @click.stop="quickAnalyze(file)">分析</el-button>
                  <el-button size="small" type="danger" @click.stop="removeFile(file, idx)">删除</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <el-dialog v-model="showImgDialog" width="auto" top="10vh" class="media-dialog image-dialog">
          <div class="dialog-header">
            <span class="dialog-title">{{ dialogTitle }}</span>
            <el-tag size="small">图片</el-tag>
          </div>
          <img :src="previewImgUrl" class="dialog-image" />
          <div class="dialog-footer">
            <el-button size="small" type="primary" @click="analyzeCurrent('image')">分析此图片</el-button>
            <el-button size="small" @click="showImgDialog=false">关闭</el-button>
          </div>
        </el-dialog>

        <el-dialog v-model="showMediaDialog" :width="previewMediaType==='audio' ? '560px' : '720px'" top="10vh" class="media-dialog">
          <div class="dialog-header">
            <span class="dialog-title">{{ dialogTitle }}</span>
            <el-tag size="small">{{ previewMediaType==='audio' ? '音频' : '视频' }}</el-tag>
          </div>
          <div class="dialog-player">
            <video
              v-if="previewMediaType==='video'"
              ref="dialogMediaEl"
              :src="previewMediaUrl"
              class="dialog-media"
              controls
              @loadedmetadata="onDialogLoadedMetadata"
              @timeupdate="onDialogTimeUpdate"
              @play="onDialogPlay"
              @pause="onDialogPause"
              @ended="onDialogEnded"
              @error="onMediaError"
            />
            <audio
              v-else-if="previewMediaType==='audio'"
              ref="dialogMediaEl"
              :src="previewMediaUrl"
              class="dialog-audio"
              controls
              @loadedmetadata="onDialogLoadedMetadata"
              @timeupdate="onDialogTimeUpdate"
              @play="onDialogPlay"
              @pause="onDialogPause"
              @ended="onDialogEnded"
              @error="onMediaError"
            />
            <div class="dialog-overlay" @click="toggleDialogPlayback">
              <div class="overlay-btn" :class="{ pause: dialogPlaying }">{{ dialogPlaying ? '❚❚' : '▶' }}</div>
              <div class="overlay-status">{{ dialogPlaying ? '正在播放' : '已暂停' }}</div>
            </div>
          </div>
          <div class="dialog-meta">
            <span>{{ formatTime(dialogCurrentTime) }} / {{ formatTime(dialogDuration) }}</span>
            <div class="spacer"></div>
            <el-button size="small" type="primary" @click="analyzeCurrent(previewMediaType)">分析此{{ previewMediaType==='audio' ? '音频' : '视频' }}</el-button>
            <el-button size="small" @click="showMediaDialog=false">关闭</el-button>
          </div>
          <div v-if="mediaError" class="dialog-error">文件无法播放，请检查格式或重新上传。</div>
        </el-dialog>

        <el-dialog v-model="showTextDialog" width="500px" top="10vh">
          <div style="white-space:pre-wrap;word-break:break-all;max-height:60vh;overflow:auto;">{{ content }}</div>
        </el-dialog>
      </div>

      <!-- 多模态情绪分析区域 -->
        <!-- 分析结果卡片展示（编辑/新增通用） -->
        <div v-if="multimodalResult" class="multimodal-result">
          <div class="result-header">
            <span class="result-title">分析结果</span>
            <button class="close-btn" @click="multimodalResult=null">×</button>
          </div>
          <div class="result-content">
            <div class="result-item" style="flex-direction:column; align-items:stretch; gap:6px;">
              <div style="display:flex;justify-content:space-between;align-items:center;">
                <span class="result-label">图片情绪</span>
                <span class="result-value">{{ multimodalResult.imageEmotion || '—' }}</span>
              </div>
              <template v-if="analysisDetails.image">
                <div class="result-sub">置信度：{{ percent(analysisDetails.image.confidence) }}，强度：{{ analysisDetails.image.intensity ?? '—' }}</div>
                <div class="result-sub">耗时：{{ analysisDetails.image.processing_time_ms ?? '—' }} ms</div>
                <div v-if="analysisDetails.image.top_emotions && analysisDetails.image.top_emotions.length" class="result-sub">
                  Top3：
                  <span v-for="(e,idx) in analysisDetails.image.top_emotions.slice(0,3)" :key="idx" style="margin-right:8px;">
                    {{ e.emotion }}({{ percent(e.score) }})
                  </span>
                </div>
              </template>
            </div>
            <div class="result-item" style="flex-direction:column; align-items:stretch; gap:6px;">
              <div style="display:flex;justify-content:space-between;align-items:center;">
                <span class="result-label">音频情绪</span>
                <span class="result-value">{{ multimodalResult.audioEmotion || '—' }}</span>
              </div>
              <template v-if="analysisDetails.audio">
                <div class="result-sub">置信度：{{ percent(analysisDetails.audio.confidence) }}，强度：{{ analysisDetails.audio.intensity ?? '—' }}</div>
                <div class="result-sub">耗时：{{ analysisDetails.audio.processing_time_ms ?? '—' }} ms</div>
                <div v-if="analysisDetails.audio.top_emotions && analysisDetails.audio.top_emotions.length" class="result-sub">
                  Top3：
                  <span v-for="(e,idx) in analysisDetails.audio.top_emotions.slice(0,3)" :key="idx" style="margin-right:8px;">
                    {{ e.emotion }}({{ percent(e.score) }})
                  </span>
                </div>
              </template>
            </div>
            <div class="result-item">
              <span class="result-label">综合情绪</span>
              <span class="result-value">{{ multimodalResult.combinedEmotion || '—' }}</span>
          </div>
        </div>
      </div>

      <!-- 隐藏的文件输入 -->
      <input 
        type="file" 
        ref="imageInput" 
        accept="image/*" 
        @change="onImageSelected" 
        style="display: none"
      />
      <input 
        type="file" 
        ref="audioInput" 
        accept="audio/*" 
        @change="onAudioSelected" 
        style="display: none"
      />

      <div class="btn-group">
        <button class="draft-btn" @click="saveDraft">保存草稿</button>
        <button class="publish-btn" @click="publish">发布</button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { analyzeFromDiary } from '@/api/emotion'
const showImgDialog = ref(false)
const previewImgUrl = ref('')
const showTextDialog = ref(false)
const dialogTitle = ref('预览')
const dialogMediaEl = ref(null)
const dialogPlaying = ref(false)
const dialogDuration = ref(0)
const dialogCurrentTime = ref(0)
function previewImage(url, file) {
  previewImgUrl.value = url
  dialogTitle.value = (file && (file.name || file.originName)) || '图片预览'
  showImgDialog.value = true
}
function getFullUrl(url) {
  if (!url) return ''
  // 本地选择文件的 preview URL，直接返回
  if (url.startsWith('blob:') || url.startsWith('data:') || url.startsWith('file:')) return url
  // 如果已经是完整的URL（包含协议），移除localhost硬编码
  if (url.startsWith('http://') || url.startsWith('https://')) {
    // 移除可能存在的localhost硬编码，替换为相对路径
    if (url.includes('localhost:8000') || url.includes('localhost:8081') || url.includes('127.0.0.1')) {
      try {
        const urlObj = new URL(url)
        return urlObj.pathname + (urlObj.search || '')
      } catch (e) {
        const match = url.match(/https?:\/\/[^\/]+(\/.*)/)
        if (match) return match[1]
      }
    }
    return url
  }
  // 使用相对路径，通过当前域名访问（适配开发和生产环境）
  return url
}
const EMOTION_TAGS = ['开心', '难过', '焦虑', '兴奋', '平静']
const tags = ref([]) // 普通标签
const tagInput = ref('') // 标签输入框

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id
const title = ref('')
const content = ref('')
const selectedDate = ref(new Date())
const selectedEmotion = ref('')

const newFiles = ref([]) // 新上传的文件
const existMedia = ref([]) // 已有媒体（编辑时回显）
const showMediaDialog = ref(false)
const previewMediaType = ref('')
const previewMediaUrl = ref('')
const mediaError = ref(false)

// Blog风格播放器状态管理
const mediaStates = ref({}) // { [idx]: { playing, currentTime, duration, volume } }
const videoRefs = ref({}) // { [idx]: videoElement }
const audioRefs = ref({}) // { [idx]: audioElement }

function setVideoRef(el, idx) {
  if (el) videoRefs.value[idx] = el
}

function setAudioRef(el, idx) {
  if (el) audioRefs.value[idx] = el
}

function onMediaLoaded(e, idx, type) {
  const el = e?.target
  if (!el) return
  if (!mediaStates.value[idx]) {
    mediaStates.value[idx] = { playing: false, currentTime: 0, duration: 0, volume: 1 }
  }
  mediaStates.value[idx].duration = el.duration || 0
  mediaStates.value[idx].volume = el.volume || 1
}

function onMediaTimeUpdate(e, idx) {
  const el = e?.target
  if (!el) return
  if (!mediaStates.value[idx]) {
    mediaStates.value[idx] = { playing: false, currentTime: 0, duration: 0, volume: 1 }
  }
  mediaStates.value[idx].currentTime = el.currentTime || 0
}

function onMediaPlay(idx) {
  if (!mediaStates.value[idx]) {
    mediaStates.value[idx] = { playing: true, currentTime: 0, duration: 0, volume: 1 }
  } else {
    mediaStates.value[idx].playing = true
  }
}

function onMediaPause(idx) {
  if (mediaStates.value[idx]) {
    mediaStates.value[idx].playing = false
  }
}

function onMediaEnded(idx) {
  if (mediaStates.value[idx]) {
    mediaStates.value[idx].playing = false
    mediaStates.value[idx].currentTime = 0
  }
}

function togglePlayPause(idx, type) {
  const el = type === 'video' ? videoRefs.value[idx] : audioRefs.value[idx]
  if (!el) return
  if (mediaStates.value[idx]?.playing) {
    el.pause()
  } else {
    el.play()
  }
}

function seekMedia(e, idx, type) {
  const el = type === 'video' ? videoRefs.value[idx] : audioRefs.value[idx]
  if (!el) return
  const time = parseFloat(e.target.value)
  el.currentTime = time
  if (!mediaStates.value[idx]) {
    mediaStates.value[idx] = { playing: false, currentTime: time, duration: 0, volume: 1 }
  } else {
    mediaStates.value[idx].currentTime = time
  }
}

function setVolume(e, idx, type) {
  const el = type === 'video' ? videoRefs.value[idx] : audioRefs.value[idx]
  if (!el) return
  const volume = parseFloat(e.target.value) / 100
  el.volume = volume
  if (!mediaStates.value[idx]) {
    mediaStates.value[idx] = { playing: false, currentTime: 0, duration: 0, volume }
  } else {
    mediaStates.value[idx].volume = volume
  }
}
function previewMedia(type, url, file) {
  previewMediaType.value = type
  previewMediaUrl.value = url
  dialogTitle.value = (file && (file.name || file.originName)) || (type==='audio' ? '音频预览' : '视频预览')
  mediaError.value = false
  dialogPlaying.value = false
  dialogDuration.value = 0
  dialogCurrentTime.value = 0
  showMediaDialog.value = true
}
function onDialogLoadedMetadata(e) {
  const el = e?.target
  if (!el) return
  dialogDuration.value = el.duration || 0
}
function onDialogTimeUpdate(e) {
  const el = e?.target
  if (!el) return
  dialogCurrentTime.value = el.currentTime || 0
}
function onDialogPlay() { dialogPlaying.value = true }
function onDialogPause() { dialogPlaying.value = false }
function onDialogEnded() { dialogPlaying.value = false }
function toggleDialogPlayback() {
  const el = dialogMediaEl.value
  if (!el) return
  if (dialogPlaying.value) el.pause(); else el.play()
}
function formatTime(sec) {
  const s = Math.max(0, Math.floor(sec || 0))
  const m = Math.floor(s / 60)
  const r = s % 60
  return `${m.toString().padStart(2,'0')}:${r.toString().padStart(2,'0')}`
}
async function quickAnalyze(file) {
  if (!file) return
  const type = file.type
  if (type === 'image') await analyzeByType('image')
  else if (type === 'audio') await analyzeByType('audio')
  else if (type === 'video') await analyzeByType('video')
}
async function analyzeCurrent(type) {
  await analyzeByType(type)
}
const analyzing = ref(false)
const emotionAnalysisResult = ref(null)

// 多模态情绪分析相关
const analyzingImage = ref(false)
const analyzingAudio = ref(false)
const selectedImage = ref(null)
const selectedAudio = ref(null)
const selectedImageUrl = ref('')
const multimodalResult = ref(null)
const analysisDetails = ref({ image: null, audio: null })
const imageInput = ref(null)
const audioInput = ref(null)
const analyzingGeneric = ref(false)
const currentType = ref('')
function onMediaError() {
  mediaError.value = true
}

const allMedia = computed(() => {
  // 统一新上传和已存在的媒体
  return [
    ...existMedia.value.map(m => ({
      ...m,
      url: m.mediaUrl,
      type: m.mediaType,
      name: m.mediaUrl ? m.mediaUrl.split('/').pop() : ''
    })),
    ...newFiles.value
  ]
})

// 动态判断是否存在某类附件（已选或已存在）
const hasImage = computed(() => allMedia.value.some(m => m.type === 'image'))
const hasAudio = computed(() => allMedia.value.some(m => m.type === 'audio'))
const hasVideo = computed(() => allMedia.value.some(m => m.type === 'video'))

function getFileType(file) {
  if (file.type) {
    if (file.type.startsWith('image')) return 'image'
    if (file.type.startsWith('video')) return 'video'
    if (file.type.startsWith('audio')) return 'audio'
  }
  // 已有媒体
  if (file.mediaType) return file.mediaType
  return 'other'
}

function getFileKey(file, idx) {
  // 使用唯一标识符作为key
  if (file.mediaId) return `media_${file.mediaId}`
  if (file.uid) return `file_${file.uid}`
  if (file.file) return `new_${idx}_${file.name}_${file.file.size}`
  return `idx_${idx}`
}

function handleFileChange(e) {
  const files = Array.from(e.target.files)
  files.forEach(f => {
    const type = getFileType(f)
    const url = URL.createObjectURL(f)
    newFiles.value.push({ file: f, url, type, name: f.name })
  })
}

// 通用：基于附件类型分析（未保存走前端已选文件；已保存走后端from-diary）
async function analyzeByType(type) {
  try {
    analyzingGeneric.value = true
    currentType.value = type

    // 优先：编辑已有日记（isEdit）直接调用后端 from-diary
    if (isEdit) {
      // 先检查是否有对应类型的附件
      const hasType = type === 'image' ? hasImage.value : (type === 'audio' ? hasAudio.value : hasVideo.value)
      if (!hasType) {
        ElMessage.warning(`该日记不包含${type === 'image' ? '图片' : (type === 'audio' ? '音频' : '视频')}附件，无法进行分析`)
        return
      }
      
      const resp = await analyzeFromDiary(route.params.id, type)
      if (resp?.data?.code === 200) {
        multimodalResult.value = multimodalResult.value || {}
        const data = resp.data.data || {}
        if (type === 'image') { multimodalResult.value.imageEmotion = data.emotion; analysisDetails.value = { ...(analysisDetails.value||{}), image: data } }
        if (type === 'audio') { multimodalResult.value.audioEmotion = data.emotion; analysisDetails.value = { ...(analysisDetails.value||{}), audio: data } }
        if (type === 'video') { multimodalResult.value.combinedEmotion = data.emotion }
        updateCombinedEmotion()
        ElMessage.success('分析完成')
        return
      }
      // 处理后端返回的错误信息
      const errorMsg = resp?.data?.message || '分析失败'
      if (errorMsg.includes('不包含指定类型的附件')) {
        ElMessage.warning(`该日记不包含${type === 'image' ? '图片' : (type === 'audio' ? '音频' : '视频')}附件`)
        return
      } else {
        throw new Error(errorMsg)
      }
    }

    // 新增未保存：使用已选择的文件直接上传到对应接口
    if (type === 'image') {
      const img = allMedia.value.find(m => m.type === 'image' && m.file)
      if (!img) throw new Error('请先选择图片')
      const fd = new FormData()
      fd.append('image', img.file)
      const r = await request.post('/api/emotion/analyze/image', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
      if (r.data.code === 200) {
        const data = r.data.data
        multimodalResult.value = { ...(multimodalResult.value || {}), imageEmotion: data.emotion }
        updateCombinedEmotion()
        ElMessage.success('图片情绪分析完成')
      } else throw new Error(r.data.message || '图片分析失败')
    } else if (type === 'audio') {
      const au = allMedia.value.find(m => m.type === 'audio' && m.file)
      if (!au) throw new Error('请先选择音频')
      const fd = new FormData()
      fd.append('audio', au.file)
      const r = await request.post('/api/nlp/emotion/analyze/audio', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
      if (r.data.code === 200) {
        const data = r.data.data
        multimodalResult.value = { ...(multimodalResult.value || {}), audioEmotion: data.emotion }
        updateCombinedEmotion()
        ElMessage.success('音频情绪分析完成')
      } else throw new Error(r.data.message || '音频分析失败')
    } else if (type === 'video') {
      if (!diaryId.value) {
        const vid = allMedia.value.find(m => m.type === 'video' && m.file)
        if (!vid) throw new Error('请先选择视频')
        const fd = new FormData()
        fd.append('video', vid.file)
        const r = await request.post('/api/emotion/analyze/video', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
        if (r.data.code === 200) {
          const data = r.data.data
          multimodalResult.value = { ...(multimodalResult.value || {}), videoEmotion: data.emotion }
          updateCombinedEmotion()
          ElMessage.success('视频情绪分析完成')
        } else throw new Error(r.data.message || '视频分析失败')
      } else {
        const r = await request.post('/api/emotion/analysis/from-diary', {
          diaryId: diaryId.value,
          type: 'video'
        })
        if (r.data.code === 200) {
          const data = r.data.data
          multimodalResult.value = { ...(multimodalResult.value || {}), videoEmotion: data.emotion }
          updateCombinedEmotion()
          ElMessage.success('视频情绪分析完成')
        } else throw new Error(r.data.message || '视频分析失败')
      }
    }
  } catch (e) {
    console.error('分析失败:', e)
    ElMessage.error(e.message || '分析失败，请稍后重试')
  } finally {
    analyzingGeneric.value = false
    currentType.value = ''
  }
}
async function deleteMedia(media, diary) {
  try {
    await request.delete(`/api/diary/media/${media.mediaId}`)
    // 从前端移除
    diary.media = diary.media.filter(m => m.mediaId !== media.mediaId)
    ElMessage.success('删除成功')
  } catch (e) {
    ElMessage.error('删除失败')
  }
}


function removeFile(file, idx) {
  // 清理播放器状态和引用
  const key = getFileKey(file, idx)
  if (mediaStates.value[key]) {
    delete mediaStates.value[key]
  }
  if (videoRefs.value[key]) {
    delete videoRefs.value[key]
  }
  if (audioRefs.value[key]) {
    delete audioRefs.value[key]
  }
  
  // 新上传的文件
  if (file.file) {
    newFiles.value = newFiles.value.filter(f => f !== file)
    ElMessage.success('已删除')
  } else if (file.mediaId) {
    // 已有媒体，调用后端删除
    request.delete(`/api/diary/media/${file.mediaId}`).then(() => {
      existMedia.value = existMedia.value.filter(f => f !== file)
      ElMessage.success('已删除')
      // 删除成功后刷新页面数据
      if (isEdit) {
        loadDiary()
      }
    }).catch(error => {
      console.error('删除媒体文件失败:', error)
      ElMessage.error('删除失败')
    })
  } else if (file.mediaId) {
    // 兼容不同的字段名
    request.delete(`/api/diary/media/${file.mediaId}`).then(() => {
      existMedia.value = existMedia.value.filter(f => f !== file)
      ElMessage.success('已删除')
      // 删除成功后刷新页面数据
      if (isEdit) {
        loadDiary()
      }
    }).catch(error => {
      console.error('删除媒体文件失败:', error)
      ElMessage.error('删除失败')
    })
  } else {
    console.warn('无法识别的文件类型:', file)
    ElMessage.warning('无法删除此文件')
  }
}

async function loadDiary() {
  if (!isEdit) return
  try {
    const res = await request.get(`/api/diary/${route.params.id}`)
    const d = res.data.data
    
    // 权限检查：如果不是日记作者，跳转到详情页面
    const currentUserId = window.sessionStorage.getItem('uid')
    if (d.user_id && currentUserId !== d.user_id.toString()) {
      ElMessage.error('您没有权限编辑此日记')
      router.push(`/user/diary/detail/${route.params.id}`)
      return
    }

    // 识别情绪标签
    const emotion = (d.tags || []).find(tag => EMOTION_TAGS.includes(tag))
    selectedEmotion.value = emotion || ''
    // 过滤掉情绪标签，剩下的作为普通标签
    tags.value = (d.tags || []).filter(tag => !EMOTION_TAGS.includes(tag))
    
    title.value = d.title
    content.value = d.content
    
    existMedia.value = (d.media || []).map(m => ({
      ...m,
      url: m.mediaUrl,
      type: m.mediaType,
      name: m.mediaUrl ? m.mediaUrl.split('/').pop() : '',
      mediaId: m.mediaId || m.media_id, // 确保有mediaId字段
      media_id: m.mediaId || m.media_id // 兼容两种字段名
    }))
  } catch (error) {
    console.error('加载日记失败:', error)
    ElMessage.error('加载日记失败')
    router.push('/user/diary')
  }
}
async function saveDiary(status, silent = false) {
  const form = new FormData()
  form.append('title', title.value)
  form.append('content', content.value)
  // 合成tags，情绪标签+普通标签
  const allTags = selectedEmotion.value ? [selectedEmotion.value, ...tags.value] : tags.value
  allTags.forEach(tag => form.append('tags', tag))
  if (isEdit) {
    // 编辑时用 newMediaFiles
    newFiles.value.forEach(f => f.file && form.append('newMediaFiles', f.file))
    form.append('status', status)
    await request.put(`/api/diary/${route.params.id}`, form)
  } else {
    // 新增时用 mediaFiles
    newFiles.value.forEach(f => f.file && form.append('mediaFiles', f.file))
    form.append('status', status)
    await request.post('/api/diary', form)
  }
  if (!silent) ElMessage.success(status === 'draft' ? '草稿已保存' : '发布成功')
  if (!silent) router.push('/user/diary')
}
function onEmotionChange(val) {
  // 保证tags里没有情绪标签
  tags.value = (tags.value || []).filter(tag => !EMOTION_TAGS.includes(tag))
}

// 情绪分析功能
async function analyzeEmotion() {
  if (!content.value || content.value.trim().length < 10) {
    ElMessage.warning('请输入至少10个字的日记内容才能进行情绪分析')
    return
  }
  
  analyzing.value = true
  try {
    const response = await request.post('/api/diary/analyze-emotion', {
      content: content.value
    })
    
    if (response.data.code === 200) {
      emotionAnalysisResult.value = response.data.data
      ElMessage.success('情绪分析完成')
    } else {
      ElMessage.error(response.data.message || '情绪分析失败')
    }
  } catch (error) {
    console.error('情绪分析失败:', error)
    ElMessage.error('情绪分析失败，请稍后重试')
  } finally {
    analyzing.value = false
  }
}

// 添加建议的标签
function addSuggestedTag(tagName) {
  if (!tags.value.includes(tagName) && !EMOTION_TAGS.includes(tagName)) {
    tags.value.push(tagName)
    ElMessage.success(`已添加标签: ${tagName}`)
  } else {
    ElMessage.info(`标签 "${tagName}" 已存在`)
  }
}

// 多模态情绪分析方法
function chooseImageForAnalysis() {
  imageInput.value.click()
}

function chooseAudioForAnalysis() {
  audioInput.value.click()
}

function onImageSelected(event) {
  const file = event.target.files[0]
  if (file) {
    if (file.size > 50 * 1024 * 1024) {
      ElMessage.error('图片文件不能超过50MB')
      return
    }
    selectedImage.value = file
    selectedImageUrl.value = URL.createObjectURL(file)
    multimodalResult.value = null
  }
}

function onAudioSelected(event) {
  const file = event.target.files[0]
  if (file) {
    if (file.size > 50 * 1024 * 1024) {
      ElMessage.error('音频文件不能超过50MB')
      return
    }
    selectedAudio.value = file
    multimodalResult.value = null
  }
}

function removeSelectedImage() {
  selectedImage.value = null
  selectedImageUrl.value = ''
  if (imageInput.value) {
    imageInput.value.value = ''
  }
}

function removeSelectedAudio() {
  selectedAudio.value = null
  if (audioInput.value) {
    audioInput.value.value = ''
  }
}

function formatFileSize(size) {
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / (1024 * 1024)).toFixed(1) + ' MB'
}

function percent(v) {
  if (v == null) return '—'
  const num = typeof v === 'number' ? v : Number(v)
  if (Number.isNaN(num)) return '—'
  return (num <= 1 ? (num * 100).toFixed(1) : num.toFixed(1)) + '%'
}

// 组合情绪计算与映射
function updateCombinedEmotion() {
  try {
    const img = multimodalResult.value?.imageEmotion
    const aud = multimodalResult.value?.audioEmotion
    const scores = []
    if (img) scores.push(emotionScore(img))
    if (aud) scores.push(emotionScore(aud))
    if (!scores.length) return
    const avg = scores.reduce((a, b) => a + b, 0) / scores.length
    multimodalResult.value = { ...(multimodalResult.value || {}), combinedEmotion: labelFromScore(avg) }
  } catch {}
}

function emotionScore(emotion) {
  const map = {
    '开心': 1.0,
    '激动': 0.8,
    '平静': 0.5,
    '无聊': 0.0,
    '疲惫': -0.4,
    '难过': -0.8,
    '焦虑': -0.9,
    '愤怒': -1.0
  }
  return map[emotion] ?? 0.0
}

function labelFromScore(score) {
  if (score >= 0.9) return '开心'
  if (score >= 0.7) return '激动'
  if (score >= 0.4) return '平静'
  if (score >= -0.05) return '无聊'
  if (score >= -0.6) return '疲惫'
  if (score >= -0.85) return '难过'
  if (score >= -0.95) return '焦虑'
  return '愤怒'
}

async function analyzeImageEmotion() {
  if (!selectedImage.value) {
    ElMessage.warning('请先选择图片')
    return
  }
  
  analyzingImage.value = true
  try {
    const formData = new FormData()
    formData.append('image', selectedImage.value)
    
    // 使用正确的API端点 - 根据Gateway配置
    const response = await request.post('/api/emotion/analyze/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (response.data.code === 200) {
      const result = response.data.data
      multimodalResult.value = {
        ...multimodalResult.value,
        imageEmotion: result.emotion,
        confidence: result.confidence
      }
      analysisDetails.value = { ...(analysisDetails.value || {}), image: result }
      updateCombinedEmotion()
      ElMessage.success('图片情绪分析完成')
    } else {
      ElMessage.error(response.data.message || '图片情绪分析失败')
    }
  } catch (error) {
    console.error('图片情绪分析失败:', error)
    ElMessage.error('图片情绪分析失败，请稍后重试')
  } finally {
    analyzingImage.value = false
  }
}

async function analyzeAudioEmotion() {
  if (!selectedAudio.value) {
    ElMessage.warning('请先选择音频')
    return
  }
  
  analyzingAudio.value = true
  try {
    const formData = new FormData()
    formData.append('audio', selectedAudio.value)
    
    // 使用正确的API端点 - 根据Gateway配置
    const response = await request.post('/api/nlp/emotion/analyze/audio', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (response.data.code === 200) {
      const result = response.data.data
      multimodalResult.value = {
        ...multimodalResult.value,
        audioEmotion: result.emotion,
        confidence: result.confidence
      }
      analysisDetails.value = { ...(analysisDetails.value || {}), audio: result }
      updateCombinedEmotion()
      ElMessage.success('音频情绪分析完成')
    } else {
      ElMessage.error(response.data.message || '音频情绪分析失败')
    }
  } catch (error) {
    console.error('音频情绪分析失败:', error)
    ElMessage.error('音频情绪分析失败，请稍后重试')
  } finally {
    analyzingAudio.value = false
  }
}

function closeMultimodalResult() {
  multimodalResult.value = null
}

function addTag() {
  const tag = tagInput.value.trim()
  if (tag && !tags.value.includes(tag) && !EMOTION_TAGS.includes(tag)) {
    tags.value.push(tag)
    tagInput.value = ''
  }
}

function removeTag(index) {
  tags.value.splice(index, 1)
}

function saveDraft() { saveDiary('draft') }
function publish() { saveDiary('published') }
onMounted(() => {
  loadDiary()
})
</script>
<style scoped>
.edit-bg {
  min-height: 100vh;
  background: #FEF5E7;
  padding: 20px 40px;
  max-width: 1200px;
  margin: 0 auto;
}
.edit-content {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.edit-title {
  font-size: 28px;
  font-weight: bold;
  color: #E67E73;
  margin-bottom: 20px;
  letter-spacing: 2px;
  text-align: left;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #E67E73;
  margin-bottom: 8px;
}

.label-icon {
  font-size: 18px;
}
.edit-input, .edit-date, .edit-emotion, .edit-tags {
  width: 100%;
  border-radius: 10px;
  border: 2px dashed rgba(255, 182, 193, 0.3);
  padding: 12px;
  font-size: 16px;
  background: rgba(254, 245, 231, 0.9);
  transition: all 0.3s ease;
}

.edit-input:focus, .edit-date:focus, .edit-emotion:focus, .edit-tags:focus {
  outline: none;
  border-color: #ffb6c1;
  box-shadow: 0 0 0 3px rgba(255, 182, 193, 0.1);
}
.edit-textarea {
  width: 100%;
  border-radius: 10px;
  border: 2px dashed rgba(255, 182, 193, 0.3);
  padding: 12px;
  font-size: 16px;
  min-height: 120px;
  background: rgba(254, 245, 231, 0.9);
  transition: all 0.3s ease;
  resize: vertical;
}

.edit-textarea:focus {
  outline: none;
  border-color: #ffb6c1;
  box-shadow: 0 0 0 3px rgba(255, 182, 193, 0.1);
}

.file-input {
  width: 100%;
  border-radius: 10px;
  border: 2px dashed rgba(255, 182, 193, 0.3);
  padding: 12px;
  background: rgba(254, 245, 231, 0.9);
  transition: all 0.3s ease;
}

.file-hint {
  color: #E67E73;
  font-size: 13px;
  margin-top: 8px;
}
.edit-tags {
  width: 100%;
  margin-bottom: 10px;
}
.tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}
.tag {
  background: #7ec6e6;
  color: #fff;
  padding: 4px 14px;
  border-radius: 14px;
  font-size: 14px;
}
.tag-label {
  background: #e0f0ff;
  color: #007aff;
  padding: 4px 14px;
  border-radius: 14px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}
.tag-label input {
  margin-right: 4px;
}
.file-input {
  margin-bottom: 10px;
}
.media-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 10px;
}
.media-item {
  background: #f7fafc;
  border-radius: 8px;
  padding: 6px 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 2px 8px rgba(126,198,230,0.06);
}
.media-thumb {
  width: 120px;
  height: 80px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(126,198,230,0.10);
  overflow: hidden;
  position: relative;
}
.media-name {
  font-size: 13px;
  color: #E67E73;
  margin-left: 4px;
}
.remove-btn {
  background: #ff4d4f;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 2px 10px;
  font-size: 13px;
  cursor: pointer;
}
.btn-group {
  display: flex;
  gap: 18px;
  justify-content: center;
  margin-top: 10px;
}
.draft-btn {
  background: linear-gradient(135deg, #ffa500, #ff8c00);
  color: #fff;
  border: none;
  border-radius: 10px;
  padding: 12px 28px;
  font-size: 16px;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(255, 165, 0, 0.3);
  cursor: pointer;
  transition: all 0.3s ease;
}

.draft-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 165, 0, 0.4);
}

.publish-btn {
  background: linear-gradient(135deg, #32cd32, #228b22);
  color: #fff;
  border: none;
  border-radius: 10px;
  padding: 12px 28px;
  font-size: 16px;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(50, 205, 50, 0.3);
  cursor: pointer;
  transition: all 0.3s ease;
}

.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(50, 205, 50, 0.4);
}
.diary-edit-bg {
  background: rgba(254, 245, 231, 0.9) !important;
}
.diary-edit-card {
  background: rgba(254, 245, 231, 0.95) !important;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.tags-input-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tag-input {
  width: 100%;
}

.tags-display {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  margin: 0;
}

.emotion-analysis-result {
  margin-top: 15px;
  padding: 15px;
  background: linear-gradient(135deg, #f0f8ff 0%, #e6f3ff 100%);
  border-radius: 12px;
  border: 1px solid #d1e7ff;
}

.analysis-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.analysis-title {
  font-weight: 600;
  color: #E67E73;
  font-size: 14px;
}

.analysis-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.analysis-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.analysis-label {
  font-size: 13px;
  color: #E67E73;
  min-width: 70px;
}

.analysis-value {
  font-weight: 500;
  color: #E67E73;
}

.suggested-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

/* 多模态情绪分析样式 */
.multimodal-section {
  margin-top: 20px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.multimodal-title {
  font-size: 20px;
  font-weight: bold;
  color: #E67E73;
  margin-bottom: 15px;
  text-align: center;
}

.multimodal-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-bottom: 20px;
}

.multimodal-btn {
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  color: #fff;
  border: none;
  border-radius: 10px;
  padding: 12px 25px;
  font-size: 16px;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.multimodal-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(79, 70, 229, 0.4);
}

.cv-btn {
  background: linear-gradient(135deg, #4f46e5, #6366f1);
}

.audio-btn {
  background: linear-gradient(135deg, #10b981, #059669);
}

.image-preview-section, .audio-preview-section {
  margin-top: 20px;
  padding: 15px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.image-preview-header, .audio-preview-header {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.preview-title {
  font-size: 18px;
  font-weight: bold;
  color: #E67E73;
}

.preview-actions {
  display: flex;
  gap: 15px;
}

.action-link {
  font-size: 14px;
  color: #007bff;
  cursor: pointer;
  text-decoration: underline;
}

.action-link.delete {
  color: #dc3545;
}

.preview-image {
  max-width: 200px;
  max-height: 200px;
  object-fit: contain;
  border: 1px solid #eee;
  border-radius: 8px;
}

.analyze-image-btn, .analyze-audio-btn {
  background: linear-gradient(135deg, #ff69b4, #ff4d4f);
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 16px;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(255, 105, 180, 0.3);
  cursor: pointer;
  transition: all 0.3s ease;
  width: 100%;
  text-align: center;
}

.analyze-image-btn:hover, .analyze-audio-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 105, 180, 0.4);
}

.audio-info {
  font-size: 14px;
  color: #555;
  margin-top: 5px;
}

.multimodal-result {
  margin-top: 20px;
  padding: 15px;
  background: linear-gradient(135deg, #f0f8ff 0%, #e6f3ff 100%);
  border-radius: 12px;
  border: 1px solid #d1e7ff;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.result-title {
  font-weight: 600;
  color: #E67E73;
  font-size: 14px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: #999;
  cursor: pointer;
  padding: 5px;
  border-radius: 50%;
  transition: background 0.2s, color 0.2s;
}

.close-btn:hover {
  background: #eee;
  color: #333;
}

.result-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.result-sub {
  color: #E67E73;
  font-size: 12px;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.result-label {
  font-size: 13px;
  color: #E67E73;
  min-width: 80px;
}

.result-value {
  font-weight: 500;
  color: #E67E73;
  font-size: 14px;
}

/* 媒体预览增强 */
.media-item { position: relative; margin-bottom: 20px; }
.media-thumb { overflow: hidden; position: relative; }
.media-thumb .thumb-img, .media-thumb video { width: 100%; height: 100%; object-fit: cover; display:block; }
.image-thumb .thumb-img { width:100%; height:100%; object-fit: cover; }
.thumb-overlay { position:absolute; inset:0; background: rgba(0,0,0,0.15); color:#fff; opacity:0; display:flex; flex-direction:column; align-items:center; justify-content:center; transition: opacity .2s; }
.media-thumb:hover .thumb-overlay { opacity:1; }
.thumb-action { font-size: 12px; margin-top: 6px; }
.thumb-play-btn { width: 40px; height: 40px; border-radius: 50%; background: rgba(255,255,255,0.95); color: #333; display:flex; align-items:center; justify-content:center; font-size: 18px; box-shadow: 0 2px 8px rgba(0,0,0,0.15); }
.thumb-play-btn.small { width: 32px; height: 32px; font-size: 16px; }
.audio-thumb { display:flex; align-items:center; justify-content:center; background:#f5f7fa; }
.audio-icon { font-size: 28px; }
.media-meta { display:flex; align-items:center; justify-content:space-between; gap:6px; width:100%; }
.media-actions { display:flex; gap:6px; }

/* Blog风格播放器样式 */
.blog-style {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  margin-bottom: 20px;
}

.blog-video-player {
  width: 100%;
}

.video-container {
  position: relative;
  width: 100%;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 12px;
}

.blog-video {
  width: 100%;
  display: block;
  max-height: 500px;
  object-fit: contain;
}

.video-controls {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.8), transparent);
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}

.video-container:hover .video-controls {
  opacity: 1;
}

.play-pause-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255,255,255,0.9);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #333;
  transition: all 0.2s;
  flex-shrink: 0;
}

.play-pause-btn:hover {
  background: rgba(255,255,255,1);
  transform: scale(1.1);
}

.progress-container {
  flex: 1;
  position: relative;
}

.progress-bar {
  width: 100%;
  height: 6px;
  border-radius: 3px;
  background: rgba(255,255,255,0.3);
  outline: none;
  cursor: pointer;
  -webkit-appearance: none;
}

.progress-bar::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #E67E73;
  cursor: pointer;
}

.progress-bar::-moz-range-thumb {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #E67E73;
  cursor: pointer;
  border: none;
}

.time-display {
  color: #fff;
  font-size: 12px;
  white-space: nowrap;
  min-width: 80px;
  text-align: right;
}

.volume-control {
  display: flex;
  align-items: center;
  gap: 8px;
}

.volume-icon {
  font-size: 16px;
  color: #fff;
}

.volume-bar {
  width: 80px;
  height: 4px;
  border-radius: 2px;
  background: rgba(255,255,255,0.3);
  outline: none;
  cursor: pointer;
  -webkit-appearance: none;
}

.volume-bar::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #E67E73;
  cursor: pointer;
}

.volume-bar::-moz-range-thumb {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #E67E73;
  cursor: pointer;
  border: none;
}

.media-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 8px;
}

.media-name {
  flex: 1;
  font-size: 14px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 音频播放器样式 */
.blog-audio-player {
  width: 100%;
}

.audio-container {
  position: relative;
  width: 100%;
}

.blog-audio {
  display: none;
}

.audio-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.play-pause-btn.audio-btn {
  width: 48px;
  height: 48px;
  background: #E67E73;
  color: #fff;
  font-size: 20px;
}

.play-pause-btn.audio-btn:hover {
  background: #d46b60;
}

.audio-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.audio-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.audio-info .progress-container {
  width: 100%;
}

.audio-info .progress-bar {
  background: rgba(0,0,0,0.1);
}

.audio-info .time-display {
  color: #666;
  font-size: 12px;
  text-align: left;
}

/* 弹窗播放器样式 */
.media-dialog .dialog-header { display:flex; align-items:center; gap:8px; margin-bottom:8px; }
.media-dialog .dialog-title { font-weight:600; }
.media-dialog .dialog-image { max-width:80vw; max-height:80vh; display:block; margin:auto; border-radius:10px; }
.media-dialog .dialog-player { position:relative; }
.media-dialog .dialog-media { max-width:80vw; max-height:80vh; display:block; margin:auto; border-radius:10px; }
.media-dialog .dialog-audio { width:80vw; display:block; margin:auto; }
.media-dialog .dialog-overlay { position:absolute; inset:0; display:flex; flex-direction:column; align-items:center; justify-content:center; color:#fff; pointer-events:auto; background:rgba(0,0,0,0.0); transition:background .2s; }
.media-dialog .dialog-overlay:hover { background:rgba(0,0,0,0.08); }
.media-dialog .overlay-btn { width:54px; height:54px; border-radius:50%; background:rgba(255,255,255,0.95); color:#333; display:flex; align-items:center; justify-content:center; font-size:24px; box-shadow:0 4px 16px rgba(0,0,0,0.15); }
.media-dialog .overlay-btn.pause { background:#10b981; color:#fff; }
.media-dialog .overlay-status { margin-top:8px; font-size:12px; text-shadow:0 1px 2px rgba(0,0,0,0.2); }
.media-dialog .dialog-meta { margin-top:10px; display:flex; align-items:center; gap:8px; }
.media-dialog .dialog-meta .spacer { flex:1; }
.media-dialog .dialog-error { color:red; text-align:center; margin-top:10px; }
</style> 
