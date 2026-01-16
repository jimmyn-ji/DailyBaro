<template>
  <div class="advanced-healing-background" :class="currentTheme">
    <!-- 背景渐变层 -->
    <div class="background-layers">
      <div class="gradient-layer-1"></div>
      <div class="gradient-layer-2"></div>
      <div class="gradient-layer-3"></div>
    </div>
    
    <!-- 内容容器 -->
    <div class="content-container">
      <slot></slot>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdvancedHealingBackground',
  data() {
    return {
      currentTheme: 'theme-1'
    }
  },
  mounted() {
    this.setDailyTheme()
  },
  methods: {
    setDailyTheme() {
      // 根据日期选择主题（3天一循环）
      const today = new Date()
      const dayOfYear = Math.floor((today - new Date(today.getFullYear(), 0, 0)) / (1000 * 60 * 60 * 24))
      const themeIndex = (dayOfYear % 3) + 1
      this.currentTheme = `theme-${themeIndex}`
    }
  }
}
</script>

<style scoped>
.advanced-healing-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
}

/* 主题1: 蓝天白云 - 贴近照片的自然色调 */
.theme-1 {
  background: linear-gradient(180deg, #1E3A8A 0%, #2563EB 25%, #3B82F6 50%, #60A5FA 75%, #93C5FD 100%);
}

.theme-1 .gradient-layer-1 {
  background: linear-gradient(180deg, 
    rgba(30, 58, 138, 0.8) 0%,
    rgba(37, 99, 235, 0.6) 30%,
    rgba(59, 130, 246, 0.4) 60%,
    rgba(96, 165, 250, 0.2) 100%);
}

/* 主题2: 日照金山 - 美丽的雪山日出效果 */
.theme-2 {
  background: linear-gradient(180deg, #1E40AF 0%, #3B82F6 25%, #60A5FA 50%, #93C5FD 75%, #DBEAFE 100%);
}

.theme-2 .gradient-layer-1 {
  background: linear-gradient(180deg,
    rgba(30, 64, 175, 0.8) 0%,
    rgba(59, 130, 246, 0.6) 30%,
    rgba(96, 165, 250, 0.4) 60%,
    rgba(147, 197, 253, 0.2) 100%);
}

/* 主题3: 真实夜空 - 贴近照片的深邃色调 */
.theme-3 {
  background: linear-gradient(180deg, #0F172A 0%, #1E293B 30%, #334155 60%, #475569 85%, #64748B 100%);
}

.theme-3 .gradient-layer-1 {
  background: linear-gradient(180deg,
    rgba(15, 23, 42, 0.9) 0%,
    rgba(30, 41, 59, 0.7) 35%,
    rgba(51, 65, 85, 0.5) 70%,
    rgba(71, 85, 105, 0.3) 100%);
}

.background-layers {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.gradient-layer-1,
.gradient-layer-2,
.gradient-layer-3 {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.gradient-layer-2 {
  background: radial-gradient(circle at 30% 20%, rgba(255, 255, 255, 0.15) 0%, transparent 60%);
}

.gradient-layer-3 {
  background: radial-gradient(circle at 70% 80%, rgba(255, 255, 255, 0.08) 0%, transparent 60%);
}

.content-container {
  position: relative;
  z-index: 10;
  width: 100%;
  height: 100%;
}
</style>
