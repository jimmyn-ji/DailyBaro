<template>
  <div class="recommendations">
    <h2><i class="fas fa-star"></i> 个性化推荐</h2>

    <button @click="load" :disabled="loading" class="refresh-btn">
      <span v-if="loading">
        <i class="fas fa-spinner fa-spin"></i> 加载中...
      </span>
      <span v-else>
        <i class="fas fa-sync-alt"></i> 刷新推荐
      </span>
    </button>

    <div v-if="loading" class="loading">
      <div class="pulse-container">
        <div class="pulse-dot"></div>
        <div class="pulse-dot"></div>
        <div class="pulse-dot"></div>
      </div>
      <p class="loading-text">正在为您生成个性化推荐...</p>
    </div>

    <div v-else-if="data" class="result-container">
      <div class="emotion-card">
        <div class="emotion-icon">
          <i class="fas fa-heart"></i>
        </div>
        <div class="emotion-content">
          <div class="emotion-label">趋势主情绪</div>
          <div class="emotion-value">{{ data.dominantEmotion }}</div>
        </div>
      </div>

      <div v-if="data.advice" class="advice-card">
        <div class="advice-header">
          <i class="fas fa-lightbulb advice-icon"></i>
          <div class="advice-title">专业建议</div>
        </div>
        <div class="advice-content">
          {{ data.advice.tip }}
        </div>
      </div>

      <div class="recommendations-list">
        <div class="recommendations-header">
          <i class="fas fa-list-ul recommendations-icon"></i>
          <div class="recommendations-title">为您推荐的活动</div>
        </div>

        <div v-if="data.items && data.items.length">
          <div v-for="(item, idx) in data.items" :key="idx" class="recommendation-item">
            <div class="item-content">
              <div class="item-title">{{ item.title }}</div>
              <div class="item-energy">
                <i class="fas fa-bolt energy-icon"></i>
                <span>消耗能量：{{ item.energyRequired }}</span>
              </div>
            </div>
            <div class="item-action">
              <button class="action-btn">开始</button>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          <i class="fas fa-inbox empty-icon"></i>
          <p>暂无推荐内容</p>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <i class="fas fa-compass empty-icon"></i>
      <p>点击"刷新推荐"获取个性化内容</p>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { getRecommendations } from '@/api/recommendation'

export default {
  name: 'Recommendations',
  setup() {
    const loading = ref(false)
    const data = ref(null)

    const load = async () => {
      loading.value = true
      try {
        const resp = await getRecommendations()
        data.value = resp?.data?.data || null
      } catch (e) {
        console.error('加载推荐失败', e)
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      load()
    })

    return {
      loading,
      data,
      load
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

.recommendations {
  background: white;
  border-radius: 20px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
  width: 100%;
  max-width: 800px;
  padding: 35px;
  transition: all 0.3s ease;
  overflow: hidden;
  position: relative;
}

.recommendations::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
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
  background: linear-gradient(to right, #a8edea, #fed6e3);
  border-radius: 3px;
}

.refresh-btn {
  width: 100%;
  background: linear-gradient(to right, #a8edea, #fed6e3);
  color: #2c3e50;
  border: none;
  padding: 18px;
  border-radius: 50px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 30px;
  box-shadow: 0 5px 15px rgba(168, 237, 234, 0.3);
  position: relative;
  overflow: hidden;
}

.refresh-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: all 0.6s ease;
}

.refresh-btn:hover::before {
  left: 100%;
}

.refresh-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(168, 237, 234, 0.4);
}

.refresh-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
  color: #7f8c8d;
}

.loading {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 20px;
  padding: 40px;
  background: rgba(236, 240, 241, 0.5);
  border-radius: 15px;
  margin: 20px 0;
  animation: fadeIn 0.5s ease;
}

.pulse-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.pulse-dot {
  width: 12px;
  height: 12px;
  background: linear-gradient(to right, #a8edea, #fed6e3);
  border-radius: 50%;
  animation: pulse 1.5s infinite ease-in-out;
}

.pulse-dot:nth-child(2) { animation-delay: 0.2s; }
.pulse-dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes pulse {
  0%, 100% { transform: scale(0.8); opacity: 0.7; }
  50% { transform: scale(1.2); opacity: 1; }
}

.loading-text {
  color: #2c3e50;
  font-weight: 500;
  font-size: 18px;
  text-align: center;
}

.result-container {
  animation: fadeIn 0.8s ease;
}

.emotion-card {
  background: linear-gradient(to right, #a8edea, #fed6e3);
  border-radius: 15px;
  padding: 25px;
  margin-bottom: 25px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 20px;
}

.emotion-icon {
  font-size: 42px;
  color: #2c3e50;
  background: rgba(255, 255, 255, 0.7);
  width: 70px;
  height: 70px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.emotion-content {
  flex: 1;
}

.emotion-label {
  color: #7f8c8d;
  font-weight: 500;
  font-size: 14px;
  margin-bottom: 5px;
}

.emotion-value {
  color: #2c3e50;
  font-weight: 700;
  font-size: 24px;
}

.advice-card {
  background: white;
  border-radius: 15px;
  padding: 25px;
  margin-bottom: 25px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
  border-left: 5px solid #ff7eb3;
}

.advice-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
}

.advice-icon {
  color: #ff7eb3;
  font-size: 24px;
}

.advice-title {
  color: #2c3e50;
  font-weight: 600;
  font-size: 20px;
}

.advice-content {
  color: #2c3e50;
  font-size: 16px;
  line-height: 1.6;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 10px;
  border-left: 3px solid #ff7eb3;
}

.recommendations-list {
  margin-top: 10px;
}

.recommendations-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.recommendations-icon {
  color: #2c3e50;
  font-size: 24px;
}

.recommendations-title {
  color: #2c3e50;
  font-weight: 600;
  font-size: 20px;
}

.recommendation-item {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 15px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all 0.3s;
  border-left: 4px solid #a8edea;
}

.recommendation-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.item-content {
  flex: 1;
}

.item-title {
  color: #2c3e50;
  font-weight: 600;
  font-size: 18px;
  margin-bottom: 5px;
}

.item-energy {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #7f8c8d;
  font-size: 14px;
}

.energy-icon {
  color: #ff7eb3;
}

.item-action {
  margin-left: 15px;
}

.action-btn {
  background: linear-gradient(to right, #a8edea, #fed6e3);
  color: #2c3e50;
  border: none;
  padding: 10px 20px;
  border-radius: 50px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 3px 8px rgba(168, 237, 234, 0.3);
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 12px rgba(168, 237, 234, 0.4);
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #7f8c8d;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 15px;
  color: #bdc3c7;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(15px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 600px) {
  .recommendations {
    padding: 25px 20px;
  }

  .emotion-card {
    flex-direction: column;
    text-align: center;
    gap: 15px;
  }

  .recommendation-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .item-action {
    margin-left: 0;
    width: 100%;
  }

  .action-btn {
    width: 100%;
  }

  h2 {
    font-size: 28px;
  }
}
</style>
