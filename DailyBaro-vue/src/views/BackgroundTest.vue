<template>
  <div class="background-test">
    <!-- 治愈系动态背景 -->
    <AdvancedHealingBackground />
    
    <div class="test-content">
      <h1>🎨 治愈系背景测试页面</h1>
      
      <div class="theme-selector">
        <h3>选择主题：</h3>
        <div class="theme-buttons">
          <button 
            v-for="theme in themes" 
            :key="theme.id"
            @click="switchTheme(theme.id)"
            :class="['theme-btn', { active: currentTheme === theme.id }]"
          >
            {{ theme.name }}
          </button>
        </div>
      </div>
      
      <div class="theme-info">
        <h3>当前主题：{{ getCurrentThemeName() }}</h3>
        <p>{{ getCurrentThemeDescription() }}</p>
      </div>
      
      <div class="test-cards">
        <div class="test-card">
          <h4>登录表单示例</h4>
          <div class="form-preview">
            <div class="input-field">
              <label>用户名</label>
              <input type="text" placeholder="请输入用户名" />
            </div>
            <div class="input-field">
              <label>密码</label>
              <input type="password" placeholder="请输入密码" />
            </div>
            <button class="login-btn">登录</button>
          </div>
        </div>
        
        <div class="test-card">
          <h4>注册表单示例</h4>
          <div class="form-preview">
            <div class="input-field">
              <label>邮箱</label>
              <input type="email" placeholder="请输入邮箱" />
            </div>
            <div class="input-field">
              <label>确认密码</label>
              <input type="password" placeholder="请再次输入密码" />
            </div>
            <button class="register-btn">注册</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import AdvancedHealingBackground from '@/components/AdvancedHealingBackground.vue'

export default {
  name: 'BackgroundTest',
  components: {
    AdvancedHealingBackground
  },
  data() {
    return {
      currentTheme: 'theme-1',
      themes: [
        { id: 'theme-1', name: '蓝天白云', description: '深蓝色天空，白色云朵飘动，适合白天使用' },
        { id: 'theme-2', name: '冬日暖阳', description: '浅蓝到粉橙渐变，雪山背景，温暖治愈' },
        { id: 'theme-3', name: '夜空星月', description: '深蓝夜空，明亮月亮，星光闪烁' }
      ]
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
    },
    switchTheme(themeId) {
      this.currentTheme = themeId
      // 强制重新渲染背景组件
      this.$forceUpdate()
    },
    getCurrentThemeName() {
      const theme = this.themes.find(t => t.id === this.currentTheme)
      return theme ? theme.name : '未知主题'
    },
    getCurrentThemeDescription() {
      const theme = this.themes.find(t => t.id === this.currentTheme)
      return theme ? theme.description : '主题描述'
    }
  }
}
</script>

<style scoped>
.background-test {
  min-height: 100vh;
  position: relative;
}

.test-content {
  position: relative;
  z-index: 10;
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.test-content h1 {
  text-align: center;
  color: #FF6B9D;
  font-size: 2.5rem;
  margin-bottom: 40px;
  text-shadow: 0 2px 4px rgba(255, 107, 157, 0.2);
}

.theme-selector {
  background: rgba(255, 255, 255, 0.9);
  padding: 30px;
  border-radius: 20px;
  margin-bottom: 30px;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.theme-selector h3 {
  color: #FF6B9D;
  margin-bottom: 20px;
  text-align: center;
}

.theme-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}

.theme-btn {
  padding: 12px 24px;
  border: 2px solid #FFB6C1;
  background: white;
  color: #FF6B9D;
  border-radius: 25px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.theme-btn:hover {
  background: #FFB6C1;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 182, 193, 0.4);
}

.theme-btn.active {
  background: #FF6B9D;
  color: white;
  border-color: #FF6B9D;
}

.theme-info {
  background: rgba(255, 255, 255, 0.9);
  padding: 25px;
  border-radius: 20px;
  margin-bottom: 30px;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.theme-info h3 {
  color: #FF6B9D;
  margin-bottom: 15px;
}

.theme-info p {
  color: #666;
  margin: 0;
  line-height: 1.6;
}

.test-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 30px;
}

.test-card {
  background: rgba(255, 255, 255, 0.9);
  padding: 30px;
  border-radius: 20px;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.test-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.test-card h4 {
  color: #FF6B9D;
  margin-bottom: 20px;
  text-align: center;
  font-size: 1.3rem;
}

.form-preview {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-field label {
  color: #666;
  font-weight: 500;
  font-size: 14px;
}

.input-field input {
  padding: 12px 16px;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.8);
}

.input-field input:focus {
  outline: none;
  border-color: #FF6B9D;
  box-shadow: 0 0 0 3px rgba(255, 107, 157, 0.1);
  background: white;
}

.login-btn, .register-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  color: white;
}

.login-btn {
  background: linear-gradient(135deg, #FF6B9D, #FF8EAB);
}

.register-btn {
  background: linear-gradient(135deg, #FF8EAB, #FFB6C1);
}

.login-btn:hover, .register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 107, 157, 0.4);
}

@media (max-width: 768px) {
  .test-content {
    padding: 20px 15px;
  }
  
  .test-content h1 {
    font-size: 2rem;
  }
  
  .theme-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .test-cards {
    grid-template-columns: 1fr;
  }
}
</style>
