<template>
  <div class="audio-emotion">
    <h2><i class="fas fa-microphone-alt"></i> 音频情绪分析</h2>

    <div class="upload-container">
      <i class="fas fa-file-audio upload-icon"></i>
      <p class="upload-text">上传音频文件进行情绪分析</p>
      <input
          type="file"
          id="fileInput"
          accept="audio/*"
          @change="onFileChange"
          class="file-input"
      >
      <label for="fileInput" class="file-label">
        <i class="fas fa-upload"></i> 选择音频文件
      </label>

      <div v-if="file" class="selected-file">
        <i class="fas fa-check-circle"></i>
        <span>已选择: {{ file.name }}</span>
      </div>
    </div>

    <button
        @click="onAnalyze"
        :disabled="!file || loading"
        class="analyze-btn"
    >
      <span v-if="loading">
        <i class="fas fa-spinner fa-spin"></i> 分析中...
      </span>
      <span v-else>
        <i class="fas fa-brain"></i> 开始分析
      </span>
    </button>

    <div v-if="loading" class="loading">
      <div class="waveform">
        <div class="wave-bar"></div>
        <div class="wave-bar"></div>
        <div class="wave-bar"></div>
        <div class="wave-bar"></div>
        <div class="wave-bar"></div>
        <div class="wave-bar"></div>
      </div>
      <p class="loading-text">正在分析音频情绪，请稍候...</p>
    </div>

    <div v-if="result" class="result">
      <h3><i class="fas fa-chart-pie"></i> 分析结果</h3>

      <div class="result-grid">
        <div class="result-item">
          <div class="result-label">主要情绪</div>
          <div class="result-value emotion-value">{{ result.emotion }}</div>
        </div>

        <div class="result-item">
          <div class="result-label">置信度</div>
          <div class="result-value confidence-value">{{ (result.confidence * 100).toFixed(1) }}%</div>
        </div>

        <div class="result-item">
          <div class="result-label">强度</div>
          <div class="result-value intensity-value">{{ result.intensity }}</div>
        </div>

        <div class="result-item">
          <div class="result-label">处理时间</div>
          <div class="result-value time-value">{{ result.processing_time_ms }} ms</div>
        </div>
      </div>

      <div v-if="result.top_emotions && result.top_emotions.length" class="top-emotions">
        <h4><i class="fas fa-chart-bar"></i> 情绪分布 Top3</h4>
        <div v-for="(e, idx) in result.top_emotions" :key="idx" class="emotion-bar">
          <span class="emotion-label">{{ e.emotion }}</span>
          <div class="emotion-fill" :style="{ width: (e.score * 100) + '%' }">
            {{ (e.score * 100).toFixed(1) }}%
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { analyzeAudioEmotion } from '@/api/emotion'

export default {
  name: 'AudioEmotion',
  setup() {
    const file = ref(null)
    const loading = ref(false)
    const result = ref(null)

    const onFileChange = (e) => {
      const f = e.target.files && e.target.files[0]
      file.value = f || null
      result.value = null
    }

    const onAnalyze = async () => {
      if (!file.value) return

      loading.value = true
      result.value = null

      try {
        const fd = new FormData()
        fd.append('audio', file.value)
        const resp = await analyzeAudioEmotion(fd)
        result.value = resp?.data?.data || null
      } catch (e) {
        console.error('音频情绪分析失败', e)
      } finally {
        loading.value = false
      }
    }

    return {
      file,
      loading,
      result,
      onFileChange,
      onAnalyze
    }
  }
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.audio-emotion {
  background: white;
  border-radius: 20px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  width: 100%;
  max-width: 650px;
  padding: 35px;
  transition: all 0.3s ease;
  overflow: hidden;
  position: relative;
}

.audio-emotion::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  transform: rotate(30deg);
  pointer-events: none;
}

h2 {
  color: #2c3e50;
  text-align: center;
  margin-bottom: 30px;
  font-weight: 700;
  font-size: 32px;
  position: relative;
  padding-bottom: 15px;
}

h2:after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 5px;
  background: linear-gradient(to right, #667eea, #764ba2);
  border-radius: 3px;
}

.upload-container {
  border: 2px dashed #667eea;
  border-radius: 15px;
  padding: 35px 25px;
  text-align: center;
  margin-bottom: 25px;
  transition: all 0.3s;
  background: #f8fafc;
  position: relative;
  overflow: hidden;
}

.upload-container:hover {
  border-color: #764ba2;
  background: #f0f5ff;
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.15);
}

.upload-icon {
  font-size: 52px;
  color: #667eea;
  margin-bottom: 20px;
  transition: all 0.3s;
}

.upload-container:hover .upload-icon {
  color: #764ba2;
  transform: scale(1.1);
}

.upload-text {
  color: #7f8c8d;
  margin-bottom: 20px;
  font-size: 18px;
}

.file-input {
  display: none;
}

.file-label {
  display: inline-block;
  background: linear-gradient(to right, #667eea, #764ba2);
  color: white;
  padding: 14px 30px;
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 600;
  font-size: 16px;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.file-label:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.selected-file {
  margin-top: 20px;
  font-size: 16px;
  color: #2c3e50;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: rgba(118, 75, 162, 0.1);
  padding: 12px 20px;
  border-radius: 10px;
  animation: fadeIn 0.5s ease;
}

.selected-file i {
  color: #27ae60;
  font-size: 20px;
}

.analyze-btn {
  width: 100%;
  background: linear-gradient(to right, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 18px;
  border-radius: 50px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  margin: 25px 0;
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.3);
  position: relative;
  overflow: hidden;
}

.analyze-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: all 0.6s ease;
}

.analyze-btn:hover::before {
  left: 100%;
}

.analyze-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.analyze-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.loading {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 20px;
  padding: 30px;
  background: rgba(236, 240, 241, 0.5);
  border-radius: 15px;
  margin: 20px 0;
  animation: fadeIn 0.5s ease;
}

.waveform {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  height: 40px;
}

.wave-bar {
  width: 6px;
  background: linear-gradient(to top, #667eea, #764ba2);
  border-radius: 3px;
  animation: wave 1.2s infinite ease-in-out;
}

.wave-bar:nth-child(2) { animation-delay: 0.1s; height: 20px; }
.wave-bar:nth-child(3) { animation-delay: 0.2s; height: 35px; }
.wave-bar:nth-child(4) { animation-delay: 0.3s; height: 40px; }
.wave-bar:nth-child(5) { animation-delay: 0.4s; height: 35px; }
.wave-bar:nth-child(6) { animation-delay: 0.5s; height: 20px; }

@keyframes wave {
  0%, 100% { transform: scaleY(0.6); }
  50% { transform: scaleY(1); }
}

.loading-text {
  color: #2c3e50;
  font-weight: 500;
  font-size: 18px;
}

.result {
  background: linear-gradient(to bottom right, #f8fafc, #eef2f7);
  border-radius: 15px;
  padding: 25px;
  border-left: 5px solid #667eea;
  animation: fadeIn 0.8s ease;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(15px); }
  to { opacity: 1; transform: translateY(0); }
}

.result h3 {
  color: #2c3e50;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  margin-bottom: 20px;
}

.result-item {
  background: white;
  padding: 15px;
  border-radius: 12px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.result-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.result-label {
  color: #7f8c8d;
  font-weight: 500;
  font-size: 14px;
  margin-bottom: 8px;
}

.result-value {
  color: #2c3e50;
  font-weight: 600;
  font-size: 18px;
}

.emotion-value { color: #e74c3c; }
.confidence-value { color: #27ae60; }
.intensity-value { color: #f39c12; }
.time-value { color: #3498db; }

.top-emotions {
  margin-top: 25px;
}

.top-emotions h4 {
  color: #2c3e50;
  margin-bottom: 15px;
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.emotion-bar {
  height: 32px;
  background: #ecf0f1;
  border-radius: 16px;
  margin-bottom: 10px;
  overflow: hidden;
  position: relative;
  box-shadow: inset 0 2px 5px rgba(0, 0, 0, 0.1);
}

.emotion-fill {
  height: 100%;
  border-radius: 16px;
  background: linear-gradient(to right, #667eea, #764ba2);
  transition: width 1.2s cubic-bezier(0.22, 0.61, 0.36, 1);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 20px;
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.emotion-label {
  position: absolute;
  left: 20px;
  top: 50%;
  transform: translateY(-50%);
  color: #2c3e50;
  font-weight: 500;
  z-index: 1;
}

@media (max-width: 600px) {
  .audio-emotion {
    padding: 25px 20px;
  }

  .result-grid {
    grid-template-columns: 1fr;
  }

  h2 {
    font-size: 28px;
  }
}
</style>
