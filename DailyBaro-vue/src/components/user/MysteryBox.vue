<template>
  <div class="mystery-box-container">
    <!-- 背景动画 -->
    <div class="background-animation">
      <!-- 天空 -->
      <div class="sky">
        <div class="sky-gradient"></div>
        
        <!-- 云朵 -->
        <div class="cloud cloud-1"></div>
        <div class="cloud cloud-2"></div>
        <div class="cloud cloud-3"></div>
        <div class="cloud cloud-4"></div>
        <div class="cloud cloud-5"></div>
        <div class="cloud cloud-6"></div>
        
        <!-- 鸟儿 -->
        <div class="bird bird-1"></div>
        <div class="bird bird-2"></div>
        <div class="bird bird-3"></div>
        
        <!-- 太阳/月亮 -->
        <div class="sun-container">
          <div class="sun"></div>
        </div>
      </div>
      
      <!-- 山脉 -->
      <div class="mountains">
        <div class="mountain-back">
          <div class="mountain-back-1"></div>
          <div class="mountain-back-2"></div>
        </div>
        <div class="mountain-middle">
          <div class="mountain-middle-1"></div>
          <div class="mountain-middle-2"></div>
        </div>
        <div class="mountain-top">
          <div class="mountain-top-1"></div>
        </div>
      </div>
      
      <!-- 水面 -->
      <div class="ocean">
        <div class="boat"></div>
        <div class="ocean-layers">
          <div class="ocean-top"></div>
          <div class="ocean-middle"></div>
          <div class="ocean-bottom"></div>
        </div>
      </div>
      
      <!-- 浮动气泡 -->
      <div class="floating-bubbles">
        <div class="bubble" v-for="i in 20" :key="i" :style="{ 
          '--delay': Math.random() * 5 + 's',
          '--duration': 3 + Math.random() * 4 + 's',
          '--size': 20 + Math.random() * 40 + 'px',
          '--x': Math.random() * 100 + '%'
        }"></div>
      </div>
    </div>

    <!-- 能量显示 - 横向进度条 -->
    <div class="energy-display">
      <div class="energy-progress-container">
        <div class="energy-label">
          <span class="energy-icon">⚡</span>
          <span class="energy-text">情绪能量</span>
        </div>
        <div class="energy-progress-bar">
          <div class="progress-fill" :style="{ width: Math.min(userEnergy / 50 * 100, 100) + '%' }"></div>
          <div class="progress-bubbles">
            <div class="bubble" v-for="i in 8" :key="i" :style="{ '--delay': i * 0.3 + 's' }"></div>
          </div>
        </div>
        <div class="energy-value">{{ userEnergy }}/50</div>
      </div>
    </div>

    <!-- 盲盒主体 -->
    <div class="box-container">
      <h2 class="box-title">情绪盲盒</h2>
      
      <!-- 盲盒状态显示 -->
      <div class="box-status">
        <div class="status-item">
          <span class="status-label">今日抽取次数:</span>
          <span class="status-value">{{ todayDrawCount }}</span>
        </div>
        <div class="status-item">
          <span class="status-label">完成任务:</span>
          <span class="status-value">{{ completedCount }}</span>
        </div>
        <div class="status-item">
          <span class="status-label">剩余能量:</span>
          <span class="status-value">{{ userEnergy }} 点</span>
        </div>
      </div>

      <div class="box-animation" :class="{ 'opening': isDrawing, 'opened': showContent }">
        <div class="box-2d">
          <!-- 盲盒正面 -->
          <div class="box-front">
            <img src="/src/static/imgs/box.png" alt="盲盒" class="box-image">
          </div>
          
          <!-- 盲盒背面 - 三张图片翻转 -->
          <div class="box-back">
            <div class="flip-cards" :class="{ 'flipping': isDrawing }">
              <div class="flip-card" v-for="(card, index) in flipCards" :key="index" 
                   :class="{ 'active': card.active, 'final': card.final }"
                   :style="{ '--delay': index * 0.2 + 's' }">
                <img :src="card.image" :alt="`卡片${index + 1}`" class="flip-card-image">
              </div>
            </div>
          </div>
        </div>
        
        <!-- 闪光效果 -->
        <div class="sparkles" v-if="isDrawing">
          <div class="sparkle" v-for="i in 20" :key="i" :style="{ '--delay': i * 0.05 + 's' }">✨</div>
        </div>

        <!-- 粒子爆炸效果 -->
        <div class="particle-explosion" v-if="showContent">
          <div class="particle" v-for="i in 30" :key="i" :style="{ '--angle': i * 12 + 'deg' }">🌟</div>
        </div>

        <!-- 能量波动特效 -->
        <div class="energy-wave" v-if="isDrawing">
          <div class="wave-ring" v-for="i in 3" :key="i" :style="{ '--delay': i * 0.3 + 's' }"></div>
        </div>

        <!-- 光晕效果 -->
        <div class="glow-effect" v-if="isDrawing || showContent"></div>
      </div>
    </div>

    <!-- 抽取按钮 -->
    <div class="draw-section">
      <button 
        class="draw-button" 
        :disabled="userEnergy < 10 || isDrawing"
        @click="drawBox"
        :class="{ 'drawing': isDrawing, 'disabled': userEnergy < 10 }"
      >
        <span v-if="!isDrawing">
          {{ userEnergy < 10 ? '能量不足 (需要10点)' : '抽取盲盒 (消耗10点)' }}
        </span>
        <span v-else class="drawing-text">抽取中...</span>
      </button>
      <div class="energy-cost">消耗 10 点能量</div>
      </div>

    <!-- 内容显示 -->
    <transition name="content-reveal">
      <div v-if="showContent" class="content-display">
        <div class="content-card" :class="'type-' + currentBox.contentType">
          <div class="content-type">{{ getContentTypeText(currentBox.contentType) }}</div>
          <div class="content-text">{{ currentBox.content }}</div>
          
          <!-- 任务完成按钮 -->
          <button 
            v-if="currentBox.contentType === 'task' && !currentBox.isCompleted" 
            class="complete-button"
            @click="markCompleted"
          >
            完成任务
          </button>
          
          <div v-else-if="currentBox.isCompleted" class="completed-badge">
            <span class="completed-icon">✅</span>
            <span>已完成，奖励 +5 能量</span>
          </div>
        </div>
      </div>
      </transition>

    <!-- 能量奖励动画 -->
    <transition name="energy-reward">
      <div v-if="showEnergyReward" class="energy-reward-popup">
        <div class="reward-icon">⚡</div>
        <div class="reward-text">+5 能量</div>
    </div>
    </transition>

    <!-- 今日记录 -->
    <div class="today-records">
      <h3>今日记录</h3>
      <div class="records-list">
        <div v-for="record in todayRecords" :key="record.drawnBoxId" class="record-item" :class="'type-' + record.contentType">
          <span class="record-type">{{ getContentTypeText(record.contentType) }}</span>
          <span class="record-content">{{ record.content }}</span>
          <span v-if="record.isCompleted" class="record-status">✅</span>
        </div>
      </div>
    </div>

    <!-- 音效控制 -->
    <!-- 注意：音效文件需要放在 public/sounds/ 目录下 -->
    <!-- <audio ref="openSound" preload="auto">
      <source src="/sounds/box-open.mp3" type="audio/mpeg">
    </audio>
    <audio ref="rewardSound" preload="auto">
      <source src="/sounds/reward.mp3" type="audio/mpeg">
    </audio> -->
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
// 自动为未登录用户分配测试uid，保证请求一定发出
if (!window.sessionStorage.getItem('uid')) {
  window.sessionStorage.setItem('uid', '1')
}

const userEnergy = ref(0)
const isDrawing = ref(false)
const showContent = ref(false)
const showEnergyReward = ref(false)
const currentBox = ref(null)
const todayRecords = ref([])
const todayDrawCount = ref(0)
const completedCount = ref(0)
const currentImageIndex = ref(0)
const imageCarouselTimer = ref(null)
const autoRefreshTimer = ref(null)

// 翻转卡片数据
const flipCards = ref([
  { image: '/src/static/imgs/pic1.png', active: false, final: false },
  { image: '/src/static/imgs/pic2.png', active: false, final: false },
  { image: '/src/static/imgs/pic3.png', active: false, final: false }
])

// 内容类型映射
const contentTypeMap = {
  'quote': { text: '治愈语录', icon: '💝' },
  'task': { text: '有趣任务', icon: '🎯' },
  'tip': { text: '情绪技巧', icon: '🧘' },
  'challenge': { text: '激励挑战', icon: '🔥' },
  'activity': { text: '治愈活动', icon: '🌸' },
  'social': { text: '社交互动', icon: '🤝' }
}

function getContentTypeText(type) {
  return contentTypeMap[type]?.text || type
}

function getContentIcon(type) {
  return contentTypeMap[type]?.icon || '🎁'
}

// 图片轮播
function startImageCarousel() {
  imageCarouselTimer.value = setInterval(() => {
    currentImageIndex.value = (currentImageIndex.value + 1) % 3
  }, 2000)
}

function stopImageCarousel() {
  if (imageCarouselTimer.value) {
    clearInterval(imageCarouselTimer.value)
    imageCarouselTimer.value = null
  }
}

// 自动刷新页面数据
function startAutoRefresh() {
  autoRefreshTimer.value = setInterval(async () => {
    await loadUserEnergy()
    await loadTodayRecords()
  }, 30000) // 每30秒自动刷新一次
}

function stopAutoRefresh() {
  if (autoRefreshTimer.value) {
    clearInterval(autoRefreshTimer.value)
    autoRefreshTimer.value = null
  }
}

async function loadUserEnergy() {
  console.log('请求能量值...')
  try {
    const res = await request.get('/api/mystery-box/energy')
    if (res.data.code === 200) {
      userEnergy.value = res.data.data || 0
    }
  } catch (error) {
    console.error('获取能量值失败:', error)
  }
}

async function loadTodayRecords() {
  console.log('请求今日记录...')
  try {
    const res = await request.get('/api/mystery-box/records')
    if (res.data.code === 200 && Array.isArray(res.data.data)) {
      todayRecords.value = res.data.data
      todayDrawCount.value = res.data.data.length
      completedCount.value = res.data.data.filter(record => record.isCompleted).length
    } else {
      todayRecords.value = []
      todayDrawCount.value = 0
      completedCount.value = 0
    }
  } catch (error) {
    console.error('获取今日记录失败:', error)
    todayRecords.value = []
    todayDrawCount.value = 0
    completedCount.value = 0
  }
}

async function drawBox() {
  if (userEnergy.value < 10 || isDrawing.value) return
  
  isDrawing.value = true
  showContent.value = false
  
  // 重置翻转卡片状态
  flipCards.value.forEach(card => {
    card.active = false
    card.final = false
  })
  
  try {
    // 开始翻转动画
    setTimeout(() => {
      flipCards.value.forEach((card, index) => {
        setTimeout(() => {
          card.active = true
        }, index * 200)
      })
    }, 500)
    
    // 随机选择最终卡片
    const finalIndex = Math.floor(Math.random() * 3)
    
    // 延迟显示内容，配合动画
    setTimeout(async () => {
      const res = await request.post('/api/mystery-box/draw')
      if (res.data.code === 200) {
        currentBox.value = res.data.data
        
        // 设置最终卡片
        flipCards.value.forEach((card, index) => {
          card.final = index === finalIndex
        })
        
        showContent.value = true
        todayDrawCount.value++
        
        // 自动刷新页面数据
        await loadUserEnergy()
        await loadTodayRecords()
        
        // 显示抽取成功消息
        ElMessage.success('抽取成功！')
      } else {
        ElMessage.error(res.data.message || '抽取失败')
      }
      isDrawing.value = false
    }, 3000)
    
  } catch (error) {
    console.error('抽取盲盒失败:', error)
    ElMessage.error('抽取失败，请重试')
    isDrawing.value = false
  }
}

async function markCompleted() {
  if (!currentBox.value || currentBox.value.isCompleted) return
  
  try {
    const res = await request.post(`/api/mystery-box/complete/${currentBox.value.drawnBoxId}`)
    if (res.data.code === 200) {
      currentBox.value.isCompleted = true
      completedCount.value++
      showEnergyReward.value = true
      
      // 播放奖励音效（暂时注释，需要音效文件）
      // if (rewardSound.value) {
      //   rewardSound.value.currentTime = 0
      //   rewardSound.value.play().catch(e => console.log('音效播放失败:', e))
      // }
      
      setTimeout(async () => {
        showEnergyReward.value = false
        await loadUserEnergy()
        await loadTodayRecords()
        // 触发全局能量更新事件
        window.dispatchEvent(new CustomEvent('energyUpdated'))
      }, 2000)
      
      ElMessage.success('任务完成！获得5点能量奖励')
    }
  } catch (error) {
    console.error('完成任务失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

onMounted(() => {
  loadUserEnergy()
  loadTodayRecords()
  startImageCarousel()
  startAutoRefresh()
})

onUnmounted(() => {
  stopImageCarousel()
  stopAutoRefresh()
})
</script>

<style scoped>
.mystery-box-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #feb8b0 0%, #fef9db 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 背景动画 */
.background-animation {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

/* 天空 */
.sky {
  position: absolute;
  width: 100%;
  height: 55%;
  top: 0;
  background: linear-gradient(135deg, #feb8b0 0%, #fef9db 100%);
  z-index: 1;
  overflow: hidden;
}

.sky-gradient {
  position: absolute;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #feb8b0 0%, #fef9db 100%);
}

/* 云朵 */
.cloud {
  position: absolute;
  background: white;
  border-radius: 50%;
  opacity: 0.8;
  filter: blur(10px);
  animation: floatCloud 20s infinite linear;
}

.cloud-1 {
  width: 80px;
  height: 30px;
  left: 10%;
  top: 20%;
  animation-delay: 0s;
}

.cloud-2 {
  width: 60px;
  height: 25px;
  left: 30%;
  top: 15%;
  animation-delay: 2s;
}

.cloud-3 {
  width: 70px;
  height: 28px;
  left: 60%;
  top: 25%;
  animation-delay: 4s;
}

.cloud-4 {
  width: 50px;
  height: 20px;
  left: 80%;
  top: 10%;
  animation-delay: 6s;
}

.cloud-5 {
  width: 65px;
  height: 26px;
  left: 20%;
  top: 35%;
  animation-delay: 8s;
}

.cloud-6 {
  width: 75px;
  height: 30px;
  left: 70%;
  top: 30%;
  animation-delay: 10s;
}

@keyframes floatCloud {
  0% {
    transform: translateX(-100px);
  }
  100% {
    transform: translateX(calc(100vw + 100px));
  }
}

/* 鸟儿 */
.bird {
  position: absolute;
  z-index: 200;
  animation: bird 1s infinite;
  transition: all 2s ease-in-out;
}

.bird:before,
.bird:after {
  border: 2px solid #bb7074;
  border-bottom: 0;
  width: 18px;
  height: 8px;
}

.bird:before {
  content: '';
  position: absolute;
  left: -19px;
  border-top-right-radius: 18px;
  border-top-left-radius: 18px;
  border-left: 0;
  animation: wing-left 1s infinite;
  transform-origin: bottom right;
}

.bird:after {
  content: '';
  position: absolute;
  border-top-left-radius: 18px;
  border-top-right-radius: 18px;
  border-right: 0;
  animation: wing-right 1s infinite;
  transform-origin: bottom left;
}

.bird-1 {
  left: 240px;
  top: 140px;
  transform: scale(0.9) rotate(10deg);
  animation-delay: 0.4s;
}

.bird-2 {
  left: 280px;
  top: 120px;
  transform: scale(0.8) rotate(-5deg);
  animation-delay: 0.2s;
}

.bird-3 {
  left: 250px;
  top: 100px;
  transform: scale(1) rotate(8deg);
}

@keyframes bird {
  0% {
    transform: translate(0px, 0px);
  }
  50% {
    transform: translate(2px, -3px);
  }
  100% {
    transform: translate(0px, 0px);
  }
}

@keyframes wing-right {
  0% {
    transform: rotate(0deg);
  }
  50% {
    transform: rotate(10deg);
  }
  100% {
    transform: rotate(0deg);
  }
}

@keyframes wing-left {
  0% {
    transform: rotate(0deg);
  }
  50% {
    transform: rotate(-10deg);
  }
  100% {
    transform: rotate(0deg);
  }
}

/* 太阳 */
.sun-container {
  position: absolute;
  left: 50%;
  top: 20%;
  transform: translate(-50%, -50%);
  z-index: 10;
}

.sun {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fefefe, #fffbe8);
  box-shadow: 0 0 20px rgba(255, 255, 255, 0.8);
  animation: sunGlow 3s ease-in-out infinite;
}

@keyframes sunGlow {
  0%, 100% {
    box-shadow: 0 0 20px rgba(255, 255, 255, 0.8);
  }
  50% {
    box-shadow: 0 0 30px rgba(255, 255, 255, 1);
  }
}

/* 山脉 */
.mountains {
  position: absolute;
  width: 100%;
  height: 20%;
  bottom: 35%;
  z-index: 200;
}

.mountain-back,
.mountain-middle,
.mountain-top {
  position: absolute;
  width: 100%;
  height: 100%;
}

.mountain-back-1,
.mountain-back-2,
.mountain-middle-1,
.mountain-middle-2,
.mountain-top-1 {
  position: absolute;
  border-radius: 50%;
  background: #fec6b9;
}

.mountain-back-1 {
  width: 200px;
  height: 100px;
  top: 3px;
  margin-left: 80px;
}

.mountain-back-2 {
  width: 150px;
  height: 80px;
  margin-left: 250px;
}

.mountain-middle-1 {
  width: 200px;
  height: 100px;
  top: 3px;
  margin-left: -110px;
  background: #fee1cb;
}

.mountain-middle-2 {
  width: 220px;
  height: 150px;
  margin-left: -250px;
  background: #fee1cb;
}

.mountain-top-1 {
  width: 250px;
  height: 80px;
  top: 15px;
  right: 0;
  margin-right: 150px;
  background: #fffcdd;
}

/* 水面 */
.ocean {
  position: absolute;
  width: 100%;
  height: 45%;
  bottom: 0;
  background: #ceefe8;
  z-index: 200;
  animation: ocean-top 3s infinite;
  opacity: 1;
  transform-origin: top;
}

.ocean-layers {
  position: relative;
  width: 100%;
  height: 100%;
}

.ocean-top,
.ocean-middle,
.ocean-bottom {
  position: absolute;
  width: 100%;
  bottom: 0;
}

.ocean-top {
  height: 75%;
  background: #baeced;
  animation: ocean-middle 3s infinite;
}

.ocean-middle {
  height: 75%;
  background: #baeced;
  animation: ocean-middle 3s infinite;
}

.ocean-bottom {
  height: 50%;
  background: #ace6f1;
  animation: ocean-bottom 2s infinite;
}

/* 船只 */
.boat {
  position: absolute;
  width: 50px;
  height: 0px;
  border-top: 7px solid #bb7074;
  border-left: 10px solid transparent;
  border-right: 10px solid transparent;
  left: 280px;
  top: -5px;
  transform-origin: center bottom;
  animation: boat 5s infinite;
  animation-delay: 0.25s;
  z-index: 200;
}

.boat:before,
.boat:after {
  content: '';
  position: absolute;
  bottom: 8px;
  width: 0;
  height: 0;
  border: 0 solid transparent;
}

.boat:before {
  right: -6px;
  border-right-width: 15px;
  border-bottom: 30px solid #bb7074;
}

.boat:after {
  right: 11px;
  border-right-width: 0px;
  border-left-width: 20px;
  border-bottom: 48px solid #bb7074;
}

@keyframes boat {
  0% {
    transform: rotate(1deg) translate(2px, 0px);
  }
  25% {
    transform: rotate(-1deg) translate(-2px, -2px);
  }
  50% {
    transform: rotate(1deg) translate(1px, 0px);
  }
  75% {
    transform: rotate(-1deg) translate(-1px, -2px);
  }
  100% {
    transform: rotate(1deg) translate(2px, 0px);
  }
}

@keyframes ocean-top {
  0% {
    transform: rotate(0deg);
  }
  25% {
    transform: rotate(-1deg);
  }
  50% {
    transform: rotate(0deg);
  }
  75% {
    transform: rotate(1deg);
  }
  100% {
    transform: rotate(0deg);
  }
}

@keyframes ocean-middle {
  0% {
    height: 75%;
  }
  50% {
    height: 77%;
    transform: rotate(2deg);
  }
  100% {
    height: 75%;
  }
}

@keyframes ocean-bottom {
  0% {
    height: 50%;
  }
  50% {
    height: 52%;
    transform: rotate(-2deg);
  }
  100% {
    height: 50%;
  }
}

/* 浮动气泡 */
.floating-bubbles {
  position: absolute;
  width: 100%;
  height: 100%;
}

.bubble {
  position: absolute;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  animation: float var(--duration) ease-in-out infinite;
  animation-delay: var(--delay);
  left: var(--x);
  width: var(--size);
  height: var(--size);
}

@keyframes float {
  0%, 100% {
    transform: translateY(100vh) scale(0);
    opacity: 0;
  }
  50% {
    transform: translateY(-20px) scale(1);
    opacity: 0.6;
  }
}

.water-ripples {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
}

.ripple {
  position: absolute;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  width: 20px;
  height: 20px;
  animation: ripple var(--duration) ease-out infinite;
  animation-delay: var(--delay);
}

@keyframes ripple {
  0% {
    transform: scale(0);
    opacity: 1;
  }
  100% {
    transform: scale(20);
    opacity: 0;
  }
}

/* 能量显示 - 横向进度条 */
.energy-display {
  position: relative;
  z-index: 10;
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
}

.energy-progress-container {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(255, 182, 193, 0.2);
  border: 2px solid rgba(255, 182, 193, 0.3);
  min-width: 300px;
  backdrop-filter: blur(10px);
}

.energy-label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #ff69b4;
}

.energy-icon {
  font-size: 18px;
}

.energy-text {
  color: #333;
}

.energy-progress-bar {
  width: 100%;
  height: 20px;
  background: rgba(255, 182, 193, 0.2);
  border-radius: 10px;
  position: relative;
  overflow: hidden;
  margin-bottom: 10px;
  border: 1px solid rgba(255, 182, 193, 0.3);
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #ffb6c1, #ff69b4);
  border-radius: 10px;
  transition: width 0.5s ease;
  position: relative;
  animation: progressGlow 2s ease-in-out infinite;
}

@keyframes progressGlow {
  0%, 100% { box-shadow: 0 0 5px rgba(255, 182, 193, 0.5); }
  50% { box-shadow: 0 0 15px rgba(255, 182, 193, 0.8); }
}

.progress-bubbles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.progress-bubbles .bubble {
  position: absolute;
  width: 6px;
  height: 6px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  animation: bubbleFloat 2s ease-in-out infinite;
  animation-delay: var(--delay);
}

@keyframes bubbleFloat {
  0% {
    opacity: 0;
    transform: translateX(-10px) scale(0);
  }
  50% {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
  100% {
    opacity: 0;
    transform: translateX(10px) scale(0);
  }
}

.energy-value {
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  color: #ff69b4;
}

/* 盲盒容器 */
.box-container {
  position: relative;
  z-index: 10;
  text-align: center;
  margin-bottom: 30px;
}

.box-title {
  color: #bb7074;
  font-size: 28px;
  margin-bottom: 20px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.box-status {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-bottom: 30px;
}

.status-item {
  background: rgba(255, 255, 255, 0.9);
  padding: 10px 20px;
  border-radius: 15px;
  backdrop-filter: blur(10px);
}

.status-label {
  color: #666;
  font-size: 14px;
}

.status-value {
  color: #667eea;
  font-weight: bold;
  font-size: 18px;
  margin-left: 5px;
}

/* 2D盲盒动画 */
.box-animation {
  position: relative;
  width: 200px;
  height: 200px;
  margin: 0 auto 30px;
  perspective: 1000px;
}

.box-2d {
  width: 100%;
  height: 100%;
  position: relative;
  transform-style: preserve-3d;
  transition: transform 2s ease;
}

.box-animation.opening .box-2d {
  animation: flipBox 2s ease forwards;
}

.box-animation.opened .box-2d {
  transform: rotateY(180deg);
}

.box-front, .box-back {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.box-front {
  transform: rotateY(0deg);
}

.box-back {
  transform: rotateY(180deg);
}

.box-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.flip-cards {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.flip-card {
  position: absolute;
  width: 100%;
  height: 100%;
  transform-style: preserve-3d;
  transition: all 0.6s ease;
  opacity: 0;
  transform: scale(0.8) rotateY(0deg);
}

.flip-card.active {
  opacity: 1;
  transform: scale(1) rotateY(360deg);
  animation: cardFlip 0.6s ease forwards;
}

.flip-card.final {
  opacity: 1;
  transform: scale(1) rotateY(0deg);
  z-index: 10;
}

.flip-card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 15px;
}

@keyframes cardFlip {
  0% {
    transform: scale(0.8) rotateY(0deg);
    opacity: 0;
  }
  50% {
    transform: scale(1.1) rotateY(180deg);
    opacity: 1;
  }
  100% {
    transform: scale(1) rotateY(360deg);
    opacity: 1;
  }
}

@keyframes flipBox {
  0% { transform: rotateY(0deg); }
  50% { transform: rotateY(90deg); }
  100% { transform: rotateY(180deg); }
}

/* 闪光效果 */
.sparkles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.sparkle {
  position: absolute;
  font-size: 20px;
  animation: sparkle 1s ease-out infinite;
  animation-delay: var(--delay);
}

@keyframes sparkle {
  0% {
    transform: scale(0) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: scale(1) rotate(360deg);
    opacity: 0;
  }
}

/* 粒子爆炸效果 */
.particle-explosion {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
}

.particle {
  position: absolute;
  font-size: 16px;
  animation: explode 1s ease-out forwards;
  transform: rotate(var(--angle));
}

@keyframes explode {
  0% {
    transform: translate(0, 0) scale(0);
    opacity: 1;
  }
  100% {
    transform: translate(calc(cos(var(--angle)) * 100px), calc(sin(var(--angle)) * 100px)) scale(1);
    opacity: 0;
  }
}

/* 抽取按钮 */
.draw-section {
  text-align: center;
  margin-bottom: 30px;
  position: relative;
  z-index: 10;
}

.draw-button {
  background: linear-gradient(45deg, #ff6b9d, #6a11cb);
  color: white;
  border: none;
  border-radius: 25px;
  padding: 15px 40px;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 8px 32px rgba(255, 107, 157, 0.3);
  position: relative;
  overflow: hidden;
}

.draw-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(255, 107, 157, 0.4);
}

.draw-button:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.draw-button.drawing {
  animation: pulse 1s infinite;
}

.energy-cost {
  color: #bb7074;
  font-size: 14px;
  margin-top: 10px;
  opacity: 0.8;
}

/* 内容显示 */
.content-display {
  position: relative;
  z-index: 10;
  margin-bottom: 30px;
}

.content-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 25px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  border-left: 5px solid;
}

.content-card.type-quote { border-left-color: #ff6b6b; }
.content-card.type-task { border-left-color: #feca57; }
.content-card.type-tip { border-left-color: #48dbfb; }
.content-card.type-challenge { border-left-color: #ff9ff3; }
.content-card.type-activity { border-left-color: #54a0ff; }
.content-card.type-social { border-left-color: #5f27cd; }

.content-type {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.content-text {
  font-size: 18px;
  line-height: 1.6;
  color: #333;
  margin-bottom: 20px;
}

.complete-button {
  background: linear-gradient(45deg, #feca57, #ff9ff3);
  color: white;
  border: none;
  border-radius: 20px;
  padding: 12px 30px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
}

.complete-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(254, 202, 87, 0.4);
}

.completed-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #67c23a;
  font-weight: bold;
}

.completed-icon {
  font-size: 20px;
}

/* 能量奖励动画 */
.energy-reward-popup {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: linear-gradient(45deg, #feca57, #ff9ff3);
  color: white;
  padding: 20px 40px;
  border-radius: 20px;
  text-align: center;
  z-index: 1000;
  animation: rewardPopup 2s ease-out forwards;
}

@keyframes rewardPopup {
  0% {
    transform: translate(-50%, -50%) scale(0);
    opacity: 0;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.2);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0;
  }
}

.reward-icon {
  font-size: 32px;
  margin-bottom: 10px;
}

.reward-text {
  font-size: 20px;
  font-weight: bold;
}

/* 今日记录 */
.today-records {
  position: relative;
  z-index: 10;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  padding: 20px;
  backdrop-filter: blur(10px);
}

.today-records h3 {
  color: #333;
  margin-bottom: 15px;
  text-align: center;
}

.records-list {
  max-height: 300px;
  overflow-y: auto;
}

.record-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  margin-bottom: 10px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 10px;
  border-left: 3px solid;
}

.record-item.type-quote { border-left-color: #ff6b6b; }
.record-item.type-task { border-left-color: #feca57; }
.record-item.type-tip { border-left-color: #48dbfb; }
.record-item.type-challenge { border-left-color: #ff9ff3; }
.record-item.type-activity { border-left-color: #54a0ff; }
.record-item.type-social { border-left-color: #5f27cd; }

.record-type {
  font-size: 12px;
  color: #666;
  background: rgba(0, 0, 0, 0.1);
  padding: 4px 8px;
  border-radius: 10px;
  white-space: nowrap;
}

.record-content {
  flex: 1;
  font-size: 14px;
  color: #333;
  line-height: 1.4;
}

.record-status {
  font-size: 16px;
}

/* 动画过渡 */
.content-reveal-enter-active,
.content-reveal-leave-active {
  transition: all 0.5s ease;
}

.content-reveal-enter-from {
  opacity: 0;
  transform: translateY(30px) scale(0.9);
}

.content-reveal-leave-to {
  opacity: 0;
  transform: translateY(-30px) scale(0.9);
}

.energy-reward-enter-active,
.energy-reward-leave-active {
  transition: all 0.3s ease;
}

.energy-reward-enter-from,
.energy-reward-leave-to {
  opacity: 0;
  transform: translate(-50%, -50%) scale(0);
}

/* 能量波动特效 */
.energy-wave {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
  z-index: 5;
}

.wave-ring {
  position: absolute;
  width: 200px;
  height: 200px;
  border: 3px solid rgba(255, 255, 255, 0.6);
  border-radius: 50%;
  animation: waveExpand 1.5s ease-out infinite;
  animation-delay: var(--delay);
}

@keyframes waveExpand {
  0% {
    transform: translate(-50%, -50%) scale(0);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(2);
    opacity: 0;
  }
}

/* 光晕效果 */
.glow-effect {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 70%);
  border-radius: 50%;
  animation: glowPulse 2s ease-in-out infinite;
  pointer-events: none;
  z-index: 4;
}

@keyframes glowPulse {
  0%, 100% {
    opacity: 0.3;
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    opacity: 0.6;
    transform: translate(-50%, -50%) scale(1.1);
  }
}

/* 增强盲盒打开动画 */
.box-animation.opening .box-2d {
  animation: boxShake 0.5s ease-in-out infinite;
}

@keyframes boxShake {
  0%, 100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-5px) rotate(-2deg);
  }
  75% {
    transform: translateX(5px) rotate(2deg);
  }
}

.box-animation.opened .box-2d {
  animation: boxOpen 1s ease-out forwards;
}

@keyframes boxOpen {
  0% {
    transform: scale(1) rotateY(0deg);
  }
  50% {
    transform: scale(1.1) rotateY(180deg);
  }
  100% {
    transform: scale(1) rotateY(360deg);
  }
}

/* 增强粒子爆炸效果 */
.particle-explosion {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
  z-index: 6;
}

.particle {
  position: absolute;
  font-size: 20px;
  animation: particleExplode 1.5s ease-out forwards;
  animation-delay: calc(var(--angle) * 0.01s);
}

@keyframes particleExplode {
  0% {
    transform: translate(-50%, -50%) scale(0) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(1) rotate(360deg);
    opacity: 0;
  }
}

/* 能量进度条增强动画 */
.energy-progress-bar {
  position: relative;
  overflow: hidden;
}

.progress-fill {
  transition: width 0.5s ease;
  background: linear-gradient(90deg, #feca57, #ff9ff3, #48dbfb);
  background-size: 200% 100%;
  animation: progressGlow 2s ease-in-out infinite;
}

@keyframes progressGlow {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.progress-bubbles .bubble {
  animation: bubbleFloat 3s ease-in-out infinite;
  animation-delay: var(--delay);
}

@keyframes bubbleFloat {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-10px) scale(1.2);
    opacity: 1;
  }
}
</style> 