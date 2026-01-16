<template>
  <div class="healing-background" :class="currentTheme">
    <!-- 动态云朵 -->
    <div class="clouds-container">
      <div 
        v-for="(cloud, index) in clouds" 
        :key="index"
        class="cloud"
        :class="`cloud-${index + 1}`"
        :style="cloud.style"
      ></div>
    </div>
    
    <!-- 背景渐变 -->
    <div class="background-gradient"></div>
    
    <!-- 装饰元素 -->
    <div class="decorative-elements">
      <div class="floating-particle" v-for="i in 20" :key="i"></div>
    </div>
    
    <!-- 内容插槽 -->
    <div class="content-wrapper">
      <slot></slot>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HealingBackground',
  data() {
    return {
      currentTheme: 'theme-1',
      clouds: [
        { style: { animationDelay: '0s' } },
        { style: { animationDelay: '2s' } },
        { style: { animationDelay: '4s' } },
        { style: { animationDelay: '6s' } },
        { style: { animationDelay: '8s' } }
      ]
    }
  },
  mounted() {
    this.setDailyTheme()
    this.startCloudAnimation()
  },
  methods: {
    setDailyTheme() {
      // 根据日期选择主题（3天一循环）
      const today = new Date()
      const dayOfYear = Math.floor((today - new Date(today.getFullYear(), 0, 0)) / (1000 * 60 * 60 * 24))
      const themeIndex = (dayOfYear % 3) + 1
      this.currentTheme = `theme-${themeIndex}`
    },
    startCloudAnimation() {
      // 云朵动画循环
      setInterval(() => {
        this.clouds.forEach((cloud, index) => {
          cloud.style.animationDelay = `${(index * 2) % 10}s`
        })
      }, 10000)
    }
  }
}
</script>

<style scoped>
.healing-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  z-index: -1;
}

/* 主题1: 蓝天白云 */
.theme-1 {
  background: linear-gradient(135deg, #87CEEB 0%, #98D8E8 50%, #B0E0E6 100%);
}

.theme-1 .background-gradient {
  background: linear-gradient(180deg, 
    rgba(135, 206, 235, 0.8) 0%,
    rgba(152, 216, 232, 0.6) 50%,
    rgba(176, 224, 230, 0.4) 100%);
}

/* 主题2: 冬日暖阳 */
.theme-2 {
  background: linear-gradient(135deg, #E6F3FF 0%, #FFE6E6 50%, #FFF2E6 100%);
}

.theme-2 .background-gradient {
  background: linear-gradient(180deg,
    rgba(230, 243, 255, 0.8) 0%,
    rgba(255, 230, 230, 0.6) 50%,
    rgba(255, 242, 230, 0.4) 100%);
}

/* 主题3: 夜空星月 */
.theme-3 {
  background: linear-gradient(135deg, #1E3A8A 0%, #3730A3 50%, #581C87 100%);
}

.theme-3 .background-gradient {
  background: linear-gradient(180deg,
    rgba(30, 58, 138, 0.8) 0%,
    rgba(55, 48, 163, 0.6) 50%,
    rgba(88, 28, 135, 0.4) 100%);
}

.background-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.clouds-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2;
}

.cloud {
  position: absolute;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50px;
  opacity: 0.8;
  animation: float 20s infinite ease-in-out;
}

.cloud::before,
.cloud::after {
  content: '';
  position: absolute;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50px;
}

.cloud-1 {
  width: 120px;
  height: 40px;
  top: 20%;
  left: -150px;
}

.cloud-1::before {
  width: 60px;
  height: 60px;
  top: -30px;
  left: 20px;
}

.cloud-1::after {
  width: 80px;
  height: 50px;
  top: -20px;
  left: 50px;
}

.cloud-2 {
  width: 100px;
  height: 35px;
  top: 35%;
  left: -120px;
}

.cloud-2::before {
  width: 50px;
  height: 50px;
  top: -25px;
  left: 15px;
}

.cloud-2::after {
  width: 70px;
  height: 40px;
  top: -15px;
  left: 40px;
}

.cloud-3 {
  width: 140px;
  height: 45px;
  top: 50%;
  left: -180px;
}

.cloud-3::before {
  width: 70px;
  height: 70px;
  top: -35px;
  left: 25px;
}

.cloud-3::after {
  width: 90px;
  height: 55px;
  top: -25px;
  left: 55px;
}

.cloud-4 {
  width: 90px;
  height: 30px;
  top: 65%;
  left: -110px;
}

.cloud-4::before {
  width: 45px;
  height: 45px;
  top: -22px;
  left: 12px;
}

.cloud-4::after {
  width: 60px;
  height: 35px;
  top: -12px;
  left: 35px;
}

.cloud-5 {
  width: 110px;
  height: 38px;
  top: 80%;
  left: -140px;
}

.cloud-5::before {
  width: 55px;
  height: 55px;
  top: -27px;
  left: 18px;
}

.cloud-5::after {
  width: 75px;
  height: 45px;
  top: -17px;
  left: 45px;
}

@keyframes float {
  0% {
    transform: translateX(-200px) translateY(0px);
    opacity: 0;
  }
  10% {
    opacity: 0.8;
  }
  90% {
    opacity: 0.8;
  }
  100% {
    transform: translateX(calc(100vw + 200px)) translateY(-20px);
    opacity: 0;
  }
}

.decorative-elements {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 3;
}

.floating-particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 50%;
  animation: particle-float 15s infinite linear;
}

.floating-particle:nth-child(odd) {
  background: rgba(255, 255, 255, 0.4);
}

.floating-particle:nth-child(even) {
  background: rgba(255, 255, 255, 0.8);
}

.floating-particle:nth-child(1) { left: 10%; animation-delay: 0s; }
.floating-particle:nth-child(2) { left: 20%; animation-delay: 1s; }
.floating-particle:nth-child(3) { left: 30%; animation-delay: 2s; }
.floating-particle:nth-child(4) { left: 40%; animation-delay: 3s; }
.floating-particle:nth-child(5) { left: 50%; animation-delay: 4s; }
.floating-particle:nth-child(6) { left: 60%; animation-delay: 5s; }
.floating-particle:nth-child(7) { left: 70%; animation-delay: 6s; }
.floating-particle:nth-child(8) { left: 80%; animation-delay: 7s; }
.floating-particle:nth-child(9) { left: 90%; animation-delay: 8s; }
.floating-particle:nth-child(10) { left: 15%; animation-delay: 9s; }
.floating-particle:nth-child(11) { left: 25%; animation-delay: 10s; }
.floating-particle:nth-child(12) { left: 35%; animation-delay: 11s; }
.floating-particle:nth-child(13) { left: 45%; animation-delay: 12s; }
.floating-particle:nth-child(14) { left: 55%; animation-delay: 13s; }
.floating-particle:nth-child(15) { left: 65%; animation-delay: 14s; }
.floating-particle:nth-child(16) { left: 75%; animation-delay: 15s; }
.floating-particle:nth-child(17) { left: 85%; animation-delay: 16s; }
.floating-particle:nth-child(18) { left: 95%; animation-delay: 17s; }
.floating-particle:nth-child(19) { left: 5%; animation-delay: 18s; }
.floating-particle:nth-child(20) { left: 55%; animation-delay: 19s; }

@keyframes particle-float {
  0% {
    transform: translateY(100vh) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100px) rotate(360deg);
    opacity: 0;
  }
}

.content-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  height: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .cloud {
    transform: scale(0.7);
  }
  
  .floating-particle {
    transform: scale(0.8);
  }
}

@media (max-width: 480px) {
  .cloud {
    transform: scale(0.5);
  }
  
  .floating-particle {
    transform: scale(0.6);
  }
}
</style>
