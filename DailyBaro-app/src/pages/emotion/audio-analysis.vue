<template>
  <view class="audio-analysis-container">
    <!-- 添加顶部导航栏 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <text class="page-title">音频情绪分析</text>
      <view class="placeholder"></view>
    </view>
    
    <view class="content">
      <view class="header">
        <text class="title">🎵 音频情绪分析</text>
        <text class="subtitle">上传音频文件，分析语音情绪</text>
      </view>
      
      <!-- 音频上传区域 -->
      <view class="upload-section">
        <view class="upload-area" @click="chooseAudio" v-if="!audioFile">
          <text class="upload-icon">🎵</text>
          <text class="upload-text">点击选择音频文件</text>
          <text class="upload-hint">支持 wav, mp3, m4a, flac, ogg 格式</text>
        </view>
        
        <view class="audio-preview" v-if="audioFile">
          <view class="audio-info">
            <text class="audio-name">{{ audioFile.name }}</text>
            <text class="audio-size">{{ formatFileSize(audioFile.size) }}</text>
          </view>
          <view class="audio-controls">
            <button class="btn btn-secondary" @click="removeAudio">删除</button>
            <button class="btn btn-primary" @click="analyzeAudio" :disabled="analyzing">
              {{ analyzing ? '分析中...' : '开始分析' }}
            </button>
          </view>
        </view>
      </view>
      
      <!-- 分析结果 -->
      <view class="result-section" v-if="analysisResult">
        <view class="result-header">
          <text class="result-title">分析结果</text>
          <text class="result-time">{{ analysisResult.timestamp }}</text>
        </view>
        
        <view class="emotion-card">
          <view class="main-emotion">
            <text class="emotion-label">主要情绪</text>
            <text class="emotion-value">{{ analysisResult.emotion }}</text>
            <view class="confidence-bar">
              <view class="confidence-fill" :style="{ width: analysisResult.confidence * 100 + '%' }"></view>
            </view>
            <text class="confidence-text">置信度: {{ (analysisResult.confidence * 100).toFixed(1) }}%</text>
          </view>
          
          <view class="intensity-info">
            <text class="intensity-label">情绪强度</text>
            <view class="intensity-stars">
              <text v-for="i in 10" :key="i" class="star" :class="{ active: i <= analysisResult.intensity }">⭐</text>
            </view>
            <text class="intensity-value">{{ analysisResult.intensity.toFixed(1) }}/10</text>
          </view>
        </view>
        
        <view class="emotions-breakdown">
          <text class="breakdown-title">情绪分布</text>
          <view class="emotion-list">
            <view v-for="emotion in analysisResult.top_emotions" :key="emotion.emotion" class="emotion-item">
              <text class="emotion-name">{{ emotion.emotion }}</text>
              <view class="emotion-bar">
                <view class="emotion-fill" :style="{ width: emotion.score * 100 + '%' }"></view>
              </view>
              <text class="emotion-score">{{ (emotion.score * 100).toFixed(1) }}%</text>
            </view>
          </view>
        </view>
        
        <view class="audio-features" v-if="analysisResult.audio_features">
          <text class="features-title">音频特征</text>
          <view class="features-grid">
            <view class="feature-item">
              <text class="feature-label">时长</text>
              <text class="feature-value">{{ analysisResult.audio_features.duration.toFixed(2) }}s</text>
            </view>
            <view class="feature-item">
              <text class="feature-label">采样率</text>
              <text class="feature-value">{{ analysisResult.audio_features.sample_rate }}Hz</text>
            </view>
            <view class="feature-item">
              <text class="feature-label">能量</text>
              <text class="feature-value">{{ analysisResult.audio_features.energy.toFixed(4) }}</text>
            </view>
            <view class="feature-item">
              <text class="feature-label">音调</text>
              <text class="feature-value">{{ analysisResult.audio_features.pitch.toFixed(1) }}Hz</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 错误信息 -->
      <view class="error-section" v-if="errorMessage">
        <text class="error-text">{{ errorMessage }}</text>
        <button class="btn btn-secondary" @click="clearError">重试</button>
      </view>
    </view>
  </view>
</template>

<script>
import { config } from '@/utils/config.js'

export default {
  data() {
    return {
      audioFile: null,
      analysisResult: null,
      analyzing: false,
      errorMessage: ''
    }
  },
  
  methods: {
    goBack() {
      uni.navigateBack()
    },
    
    // 获取基础URL
    getBaseUrl() {
      return config.BASE_URL
    },
    
    chooseAudio() {
      // 优先使用 chooseAudio（微信小程序原生API）
      if (uni.chooseAudio && typeof uni.chooseAudio === 'function') {
        uni.chooseAudio({
          count: 1,
          success: (res) => {
            this.audioFile = {
              tempFilePath: res.tempFilePath,
              name: res.tempFilePath.split('/').pop() || 'audio.mp3',
              size: res.size || 0
            }
            this.clearError()
          },
          fail: (err) => {
            console.error('chooseAudio 选择音频失败:', err)
            if (err.errMsg && err.errMsg.includes('cancel')) {
              // 用户取消，不显示错误
              return
            }
            // chooseAudio 失败时，尝试使用 chooseMedia
            this.chooseAudioWithChooseMedia()
          }
        })
      } else {
        // chooseAudio 不可用时，使用 chooseMedia
        this.chooseAudioWithChooseMedia()
      }
    },
    
    // 使用 chooseMedia 选择音频（备选方案）
    chooseAudioWithChooseMedia() {
      if (uni.chooseMedia && typeof uni.chooseMedia === 'function') {
        uni.chooseMedia({
          count: 1,
          mediaType: ['audio'],
          sourceType: ['album', 'camera'],
          success: (res) => {
            console.log('chooseMedia 选择音频成功:', res)
            if (res.tempFiles && res.tempFiles.length > 0) {
              const file = res.tempFiles[0]
              this.audioFile = {
                tempFilePath: file.tempFilePath,
                name: file.name || file.tempFilePath.split('/').pop() || 'audio.mp3',
                size: file.size || 0
              }
              this.clearError()
            } else {
              this.errorMessage = '未选择音频文件'
              uni.showToast({
                title: '未选择音频文件',
                icon: 'none'
              })
            }
          },
          fail: (err) => {
            console.error('chooseMedia 选择音频失败:', err)
            if (err.errMsg && err.errMsg.includes('cancel')) {
              return
            }
            this.errorMessage = '选择音频失败，请重试'
            uni.showToast({
              title: '选择音频失败',
              icon: 'none'
            })
          }
        })
      } else {
        // 两种方法都不可用
        uni.showToast({
          title: '当前环境不支持音频选择',
          icon: 'none',
          duration: 2000
        })
      }
    },
    
    removeAudio() {
      this.audioFile = null
      this.analysisResult = null
      this.clearError()
    },
    
    formatFileSize(size) {
      if (size === 0) return '0 Bytes';
      const k = 1024;
      const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
      const i = Math.floor(Math.log(size) / Math.log(k));
      return parseFloat((size / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },
    
    async analyzeAudio() {
      if (!this.audioFile) {
        this.errorMessage = '请先选择音频文件'
        return
      }
      
      this.analyzing = true
      this.clearError()
      
      try {
        const baseUrl = this.getBaseUrl()
        const response = await uni.uploadFile({
          url: baseUrl + '/api/nlp/emotion/analyze/audio',
          filePath: this.audioFile.tempFilePath,
          name: 'audio',
          success: (res) => {
            try {
              const data = JSON.parse(res.data)
              if (data && data.code === 200) {
                this.analysisResult = data.data
                uni.showToast({
                  title: '分析完成',
                  icon: 'success'
                })
              } else {
                this.errorMessage = data?.message || '分析失败，请重试'
              }
            } catch (e) {
              console.error('响应解析失败:', e)
              this.errorMessage = '响应解析失败，请重试'
            }
          },
          fail: (err) => {
            console.error('音频上传失败:', err)
            this.errorMessage = '网络错误，请检查连接后重试'
          }
        })
      } catch (error) {
        console.error('音频分析失败:', error)
        this.errorMessage = '网络错误，请检查连接后重试'
      } finally {
        this.analyzing = false
      }
    },
    
    clearError() {
      this.errorMessage = ''
    }
  }
}
</script>

<style scoped>
.audio-analysis-container {
  min-height: 100vh;
  background: #f7f7fb;
  padding: 0;
}

/* 添加导航栏样式 */
.nav-bar {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20rpx);
  padding: 30rpx 40rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1rpx solid rgba(226, 232, 240, 0.8);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.back-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(99, 102, 241, 0.1);
  transition: all 0.3s ease;
}

.back-btn:active {
  background: rgba(99, 102, 241, 0.2);
  transform: scale(0.95);
}

.back-icon {
  font-size: 36rpx;
  color: #4a90e2;
  font-weight: 600;
}

.page-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2937;
}

.placeholder {
  width: 60rpx;
}

.content {
  padding: 30rpx;
  padding-bottom: 120rpx;
  max-width: 600rpx;
  margin: 0 auto;
}

.header {
  text-align: center;
  margin-bottom: 40rpx;
}

.title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1f2937;
  display: block;
  margin-bottom: 10rpx;
}

.subtitle {
  font-size: 26rpx;
  color: #6b7280;
}

.upload-section {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.upload-area {
  border: 2rpx dashed #d1d5db;
  border-radius: 15rpx;
  padding: 60rpx 40rpx;
  text-align: center;
  transition: all 0.3s ease;
}

.upload-area:active {
  border-color: #4a90e2;
  background: #f8f9ff;
}

.upload-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
  display: block;
}

.upload-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #374151;
  margin-bottom: 10rpx;
  display: block;
}

.upload-hint {
  font-size: 24rpx;
  color: #9ca3af;
}

.audio-preview {
  background: #f9fafb;
  border-radius: 15rpx;
  padding: 30rpx;
  border: 1rpx solid #e5e7eb;
}

.audio-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.audio-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #374151;
}

.audio-size {
  font-size: 24rpx;
  color: #6b7280;
}

.audio-controls {
  display: flex;
  gap: 20rpx;
}

.btn {
  flex: 1;
  padding: 20rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: 600;
  border: none;
  transition: all 0.3s ease;
}

.btn-primary {
  background: #4a90e2;
  color: #ffffff;
}

.btn-secondary {
  background: #f3f4f6;
  color: #374151;
}

.btn:active {
  transform: translateY(2rpx);
}

.result-section {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.result-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2937;
}

.result-time {
  font-size: 24rpx;
  color: #6b7280;
}

.emotion-card {
  background: linear-gradient(135deg, #f0f8ff, #e6f3ff);
  border-radius: 15rpx;
  padding: 25rpx;
  margin-bottom: 25rpx;
  border: 1rpx solid #d1e7ff;
}

.main-emotion {
  text-align: center;
  margin-bottom: 20rpx;
}

.emotion-label {
  font-size: 24rpx;
  color: #4b5563;
  margin-bottom: 10rpx;
  display: block;
}

.emotion-value {
  font-size: 36rpx;
  font-weight: 700;
  color: #1e40af;
  margin-bottom: 15rpx;
  display: block;
}

.confidence-bar {
  background: #e5e7eb;
  border-radius: 10rpx;
  height: 8rpx;
  margin-bottom: 10rpx;
  overflow: hidden;
}

.confidence-fill {
  background: linear-gradient(90deg, #4a90e2, #6aa8ff);
  height: 100%;
  border-radius: 10rpx;
  transition: width 0.5s ease;
}

.confidence-text {
  font-size: 22rpx;
  color: #6b7280;
}

.intensity-info {
  text-align: center;
}

.intensity-label {
  font-size: 24rpx;
  color: #4b5563;
  margin-bottom: 10rpx;
  display: block;
}

.intensity-stars {
  margin-bottom: 10rpx;
}

.star {
  font-size: 24rpx;
  color: #d1d5db;
  margin: 0 2rpx;
}

.star.active {
  color: #fbbf24;
}

.intensity-value {
  font-size: 24rpx;
  color: #6b7280;
}

.emotions-breakdown {
  margin-bottom: 25rpx;
}

.breakdown-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 15rpx;
  display: block;
}

.emotion-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.emotion-item {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.emotion-name {
  font-size: 24rpx;
  color: #4b5563;
  min-width: 80rpx;
}

.emotion-bar {
  flex: 1;
  background: #f3f4f6;
  border-radius: 8rpx;
  height: 6rpx;
  overflow: hidden;
}

.emotion-fill {
  background: linear-gradient(90deg, #10b981, #34d399);
  height: 100%;
  border-radius: 8rpx;
  transition: width 0.5s ease;
}

.emotion-score {
  font-size: 22rpx;
  color: #6b7280;
  min-width: 60rpx;
  text-align: right;
}

.audio-features {
  border-top: 1rpx solid #e5e7eb;
  padding-top: 20rpx;
}

.features-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 15rpx;
  display: block;
}

.features-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15rpx;
}

.feature-item {
  background: #f9fafb;
  border-radius: 10rpx;
  padding: 15rpx;
  text-align: center;
}

.feature-label {
  font-size: 22rpx;
  color: #6b7280;
  margin-bottom: 5rpx;
  display: block;
}

.feature-value {
  font-size: 24rpx;
  font-weight: 600;
  color: #374151;
}

.error-section {
  background: #fef2f2;
  border: 1rpx solid #fecaca;
  border-radius: 15rpx;
  padding: 25rpx;
  text-align: center;
}

.error-text {
  font-size: 26rpx;
  color: #dc2626;
  margin-bottom: 15rpx;
  display: block;
}

.btn-secondary {
  background: #dc2626;
  color: #ffffff;
}
</style>