<template>
  <div class="image-emotion">
    <h2><i class="fas fa-smile"></i> CV 图像情绪分析</h2>

    <div class="upload-container">
      <i class="fas fa-cloud-upload-alt"></i>
      <p>上传图片进行情绪分析</p>
      <input
          type="file"
          id="fileInput"
          accept="image/*"
          @change="onFileChange"
          class="file-input"
      >
      <label for="fileInput" class="file-label">选择图片</label>

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
      <span v-if="loading"><i class="fas fa-spinner fa-spin"></i> 分析中...</span>
      <span v-else><i class="fas fa-magic"></i> 开始分析</span>
    </button>

    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>正在分析图像情绪，请稍候...</p>
    </div>

    <div v-if="result" class="result">
      <h3><i class="fas fa-chart-line"></i> 分析结果</h3>

      <div class="result-item">
        <span class="result-label">主要情绪：</span>
        <span class="result-value emotion-value">{{ result.emotion }}</span>
      </div>

      <div class="result-item">
        <span class="result-label">置信度：</span>
        <span class="result-value confidence-value">{{ (result.confidence * 100).toFixed(1) }}%</span>
      </div>

      <div class="result-item">
        <span class="result-label">强度：</span>
        <span class="result-value intensity-value">{{ result.intensity }}</span>
      </div>

      <div class="result-item">
        <span class="result-label">来源：</span>
        <span class="result-value">{{ result.source }}</span>
      </div>

      <div class="result-item">
        <span class="result-label">处理时间：</span>
        <span class="result-value">{{ result.processing_time_ms }} ms</span>
      </div>

      <div v-if="result.top_emotions && result.top_emotions.length" class="top-emotions">
        <h4>情绪分布 Top3：</h4>
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
import { ref } from 'vue';
import { analyzeImageEmotion } from '@/api/emotion';

export default {
  name: 'ImageEmotion',
  setup() {
    const file = ref(null);
    const loading = ref(false);
    const result = ref(null);

    const onFileChange = (e) => {
      const f = e.target.files && e.target.files[0];
      file.value = f || null;
      result.value = null;
    };

    const onAnalyze = async () => {
      if (!file.value) return;

      loading.value = true;
      result.value = null;

      try {
        const fd = new FormData();
        fd.append('image', file.value);
        const resp = await analyzeImageEmotion(fd);
        result.value = resp?.data?.data || null;
      } catch (e) {
        console.error('图像情绪分析失败', e);
      } finally {
        loading.value = false;
      }
    };

    return {
      file,
      loading,
      result,
      onFileChange,
      onAnalyze
    };
  }
};
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.image-emotion {
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 600px;
  padding: 30px;
  transition: all 0.3s ease;
  margin: 20px auto;
}

.image-emotion:hover {
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
}

h2 {
  color: #2c3e50;
  text-align: center;
  margin-bottom: 25px;
  font-weight: 600;
  font-size: 28px;
  position: relative;
  padding-bottom: 15px;
}

h2:after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 4px;
  background: linear-gradient(to right, #3498db, #2ecc71);
  border-radius: 2px;
}

.upload-container {
  border: 2px dashed #3498db;
  border-radius: 12px;
  padding: 30px 20px;
  text-align: center;
  margin-bottom: 25px;
  transition: all 0.3s;
  background: #f8fafc;
}

.upload-container:hover {
  border-color: #2ecc71;
  background: #f0f7ff;
}

.upload-container i {
  font-size: 48px;
  color: #3498db;
  margin-bottom: 15px;
}

.upload-container p {
  color: #7f8c8d;
  margin-bottom: 15px;
}

.file-input {
  display: none;
}

.file-label {
  display: inline-block;
  background: #3498db;
  color: white;
  padding: 12px 24px;
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
}

.file-label:hover {
  background: #2980b9;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(52, 152, 219, 0.3);
}

.selected-file {
  margin-top: 15px;
  font-size: 14px;
  color: #2c3e50;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.selected-file i {
  font-size: 16px;
  color: #2ecc71;
}

.analyze-btn {
  width: 100%;
  background: linear-gradient(to right, #3498db, #2ecc71);
  color: white;
  border: none;
  padding: 16px;
  border-radius: 50px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 25px;
  box-shadow: 0 4px 10px rgba(52, 152, 219, 0.3);
}

.analyze-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(52, 152, 219, 0.4);
}

.analyze-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.result {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  border-left: 4px solid #3498db;
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.result h3 {
  color: #2c3e50;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.result-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.result-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.result-label {
  color: #7f8c8d;
  font-weight: 500;
}

.result-value {
  color: #2c3e50;
  font-weight: 600;
}

.emotion-value {
  color: #e74c3c;
}

.confidence-value {
  color: #2ecc71;
}

.intensity-value {
  color: #f39c12;
}

.top-emotions {
  margin-top: 20px;
}

.top-emotions h4 {
  color: #2c3e50;
  margin-bottom: 12px;
  font-size: 16px;
}

.emotion-bar {
  height: 28px;
  background: #ecf0f1;
  border-radius: 14px;
  margin-bottom: 8px;
  overflow: hidden;
  position: relative;
}

.emotion-fill {
  height: 100%;
  border-radius: 14px;
  background: linear-gradient(to right, #3498db, #2ecc71);
  transition: width 1s ease;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 15px;
  color: white;
  font-weight: 600;
  font-size: 12px;
}

.emotion-label {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #2c3e50;
  font-weight: 500;
  z-index: 1;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  padding: 20px;
}

.spinner {
  width: 30px;
  height: 30px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
