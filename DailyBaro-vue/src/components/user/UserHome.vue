<template>
  <div class="whole-bg">
    <div class="user-header">
      <div style="display: flex; align-items: center;">
        <img class="avatar-img" src="/src/assets/assets/imgs/bghalf-2.jpg" alt="avatar" @click="goUpdateInfo" title="点击修改个人信息" />
        <div class="energy-box">
          <span class="energy-icon">⚡</span>
          <span class="energy-value">{{ userEnergy }}</span>
        </div>
        <DailyQuoteDisplay />
      </div>
      <div class="header-center">
        <span class="header-title">情绪日记本</span>
      </div>
      <el-button class="logout" type="warning" @click="logout">退出</el-button>
    </div>
    <el-container>
      <el-aside width="280px">
        <el-menu
            :default-active="defaultUrl"
            class="el-menu-vertical-demo"
            @select="handleMenuSelect"
        >
          <el-menu-item index="diary">
            <i class="el-icon-notebook-1"></i>
            <span>情绪日记本</span>
          </el-menu-item>
          <el-menu-item index="analysis">
            <i class="el-icon-data-analysis"></i>
            <span>情绪分析</span>
          </el-menu-item>
          <el-menu-item index="planet">
            <i class="el-icon-star-on"></i>
            <span>匿名星球</span>
          </el-menu-item>
          <el-menu-item index="capsule">
            <i class="el-icon-coin"></i>
            <span>情绪胶囊</span>
            <span v-if="unreadReminders.length > 0" class="red-dot"></span>
          </el-menu-item>
          <el-menu-item index="mysterybox">
            <i class="el-icon-present"></i>
            <span>情绪盲盒</span>
          </el-menu-item>
          <el-menu-item index="knowledge">
            <i class="el-icon-document"></i>
            <span>知识库</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
    <el-dialog v-model="showCapsuleReminder" title="情绪胶囊提醒" width="400px" :close-on-click-modal="false">
      <div v-for="reminder in unreadReminders" :key="reminder.capsuleId" style="margin-bottom: 12px;">
        <div>您的情绪胶囊已到开启时间：{{ reminder.openTime }}</div>
        <div>内容预览：{{ reminder.content ? reminder.content.slice(0, 30) : '（无内容）' }}</div>
        <el-button size="small" type="primary" @click="markRead(reminder.capsuleId)">我知道了</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import DailyQuoteDisplay from './DailyQuoteDisplay.vue'
import request from '@/utils/request'
const router = useRouter()
const route = useRoute()
const defaultUrl = ref('diary')
const userEnergy = ref(0)
const userId = window.sessionStorage.getItem('uid')

watch(
    () => route.path,
    () => {
      defaultUrl.value = route.path.replace('/user/', '')
    }
)
const logout = () => {
  router.push('/login')
}
const handleMenuSelect = (index) => {
  router.push(`/user/${index}`)
}
const goUpdateInfo = () => {
  router.push('/user/update_info')
}

// 胶囊提醒相关
const unreadReminders = ref([])
const showCapsuleReminder = ref(false)
let reminderCheckTimer = null

async function loadUnreadReminders() {
  try {
    const res = await request.get('/api/capsules/reminders/unread')
    if (res.data.code === 200) {
      unreadReminders.value = res.data.data || []
      showCapsuleReminder.value = unreadReminders.value.length > 0
    }
  } catch (e) {
    console.error('加载提醒失败:', e)
  }
}

async function markRead(capsuleId) {
  try {
    await request.post(`/api/capsules/reminders/read/${capsuleId}`)
    unreadReminders.value = unreadReminders.value.filter(r => r.capsuleId !== capsuleId)
    if (unreadReminders.value.length === 0) showCapsuleReminder.value = false
  } catch (e) {
    console.error('标记已读失败:', e)
  }
}

function startReminderCheck() {
  reminderCheckTimer = setInterval(() => {
    loadUnreadReminders()
  }, 2 * 60 * 1000)
}

function stopReminderCheck() {
  if (reminderCheckTimer) {
    clearInterval(reminderCheckTimer)
    reminderCheckTimer = null
  }
}

function handlePageVisibilityChange() {
  if (!document.hidden) {
    loadUnreadReminders()
  }
}

async function loadUserEnergy() {
  if (!userId) return
  try {
    const res = await request.get('/api/mystery-box/energy')
    if (res.data.code === 200 && typeof res.data.data === 'number') {
      userEnergy.value = res.data.data
    }
  } catch (error) {
    console.error('获取用户能量值失败:', error)
    userEnergy.value = 0
  }
}

onMounted(() => {
  loadUnreadReminders()
  loadUserEnergy()
  window.addEventListener('energyUpdated', loadUserEnergy)
  document.addEventListener('visibilitychange', handlePageVisibilityChange)
  startReminderCheck()
})

onUnmounted(() => {
  stopReminderCheck()
  window.removeEventListener('energyUpdated', loadUserEnergy)
  document.removeEventListener('visibilitychange', handlePageVisibilityChange)
})
</script>

<style scoped>
.whole-bg {
  min-height: 100vh;
  background: transparent;
  position: relative;
  overflow: hidden;
}

.user-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  height: 70px;
  background: transparent;
  backdrop-filter: blur(8px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border-bottom: 1px solid rgba(255, 235, 215, 0.8);
  position: relative;
  z-index: 10;
}

.el-aside {
  background: transparent !important;
  height: 100vh;
  position: relative;
}

.el-menu {
  background: transparent;
  backdrop-filter: blur(8px);
  border-radius: 12px;
  margin: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(255, 235, 215, 0.6);
  position: relative;
  height: calc(100vh - 110px);
  min-height: calc(100vh - 110px);
}

.el-main {
  background: transparent;
  backdrop-filter: blur(8px);
  min-height: calc(100vh - 70px);
  border-radius: 12px;
  margin: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(255, 235, 215, 0.6);
  padding: 30px;
  position: relative;
}

.avatar-img {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  margin-right: 12px;
  cursor: pointer;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.energy-box {
  position: absolute;
  top: -5px;
  right: -5px;
  background: linear-gradient(45deg, #E67E73, #F39C82);
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: white;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(230, 126, 115, 0.3);
  animation: pulse 2s infinite;
}

.header-title {
  font-size: 26px;
  font-weight: bold;
  color: #E67E73;
  letter-spacing: 3px;
  text-shadow: 0 2px 4px rgba(230, 126, 115, 0.1);
}

.logout {
  background: rgba(230, 126, 115, 0.7) !important;
  border: none;
  color: white;
  transition: all 0.3s ease;
}

.logout:hover {
  background: rgba(230, 126, 115, 0.9) !important;
}

.el-menu-item {
  color: #ffffff !important;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5) !important;
  font-weight: 500 !important;
}

.el-menu-item.is-active {
  color: #E67E73 !important;
  background: rgba(255, 255, 255, 0.2) !important;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3) !important;
}

.el-menu-item:hover {
  background: rgba(255, 255, 255, 0.15) !important;
  color: #ffffff !important;
}

.red-dot {
  background: #E74C3C;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}
</style>
