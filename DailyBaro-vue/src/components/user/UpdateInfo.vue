<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <h1 class="dashboard-title"><i class="el-icon-user-solid"></i>个人信息中心</h1>
    </div>

    <div class="dashboard-content">
      <div class="info-panel">
        <div class="panel-header">
          <h2 class="panel-title">基本信息</h2>
        </div>

        <div class="info-grid">
          <div class="info-item">
            <div class="info-icon">
              <i class="el-icon-user"></i>
            </div>
            <div class="info-details">
              <div class="info-label">账号</div>
              <div class="info-value">{{ userInfo.account }}</div>
            </div>
          </div>

          <div class="info-item">
            <div class="info-icon">
              <i class="el-icon-mobile-phone"></i>
            </div>
            <div class="info-details">
              <div class="info-label">手机号码</div>
              <div class="info-value">{{ userInfo.phone || '暂未设置' }}</div>
            </div>
          </div>

          <div class="info-item">
            <div class="info-icon">
              <i class="el-icon-message"></i>
            </div>
            <div class="info-details">
              <div class="info-label">邮箱地址</div>
              <div class="info-value">{{ userInfo.email || '暂未设置' }}</div>
            </div>
          </div>
        </div>

        <div class="action-buttons">
          <button class="action-btn edit-btn" @click="update">
            <i class="el-icon-edit"></i>修改信息
          </button>
        </div>
      </div>

      <div class="recommendations-panel">
        <div class="panel-header">
          <h2 class="panel-title">个性化推荐</h2>
          <button class="refresh-btn" @click="loadRecommendations" :disabled="loadingRecommendations">
            <i class="el-icon-refresh"></i>
            {{ loadingRecommendations ? '刷新中...' : '刷新推荐' }}
          </button>
        </div>

        <div v-if="recommendations" class="recommendation-content">
          <div class="recommendation-card">
            <div class="emotion-badge">
              当前情绪趋势：{{ recommendations.dominantEmotion }}
            </div>

            <div v-if="recommendations.advice" class="advice-card">
              <div class="advice-header">
                <span class="advice-icon">💡</span>
                <span class="advice-title">个性化建议</span>
              </div>
              <p>{{ recommendations.advice.tip }}</p>
            </div>
          </div>

          <div class="recommendation-card">
            <h3 class="card-title">推荐活动</h3>
            <div class="activity-list">
              <div class="activity-item" v-for="(item, index) in recommendations.items" :key="index">
                <div class="activity-info">
                  <div class="activity-name">{{ item.title }}</div>
                  <div class="activity-energy">消耗能量：{{ item.energyRequired }}</div>
                </div>
                <button class="try-btn" @click="tryActivity(item)">尝试</button>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="loadingRecommendations" class="loading-state">
          <p class="loading-text">正在分析您的情绪数据...</p>
        </div>

        <div v-else class="empty-state">
          <div class="empty-icon">📊</div>
          <p class="empty-text">暂无推荐</p>
          <p class="empty-hint">多写日记，让AI更好地了解您</p>
        </div>
      </div>
    </div>

    <!-- 修改信息弹窗 -->
    <el-dialog
        title="修改个人信息"
        v-model="updateDialogVisible"
        width="500px"
        :close-on-click-modal="false"
        center>
      <el-form :model="updateUser" label-width="80px" :rules="rules" ref="updateForm">
        <el-form-item label="账号">
          <el-input v-model="updateUser.account" disabled>
            <template #prefix><i class="el-icon-user"></i></template>
          </el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="updateUser.phone" placeholder="请输入手机号码">
            <template #prefix><i class="el-icon-mobile-phone"></i></template>
          </el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="updateUser.email" placeholder="请输入邮箱地址">
            <template #prefix><i class="el-icon-message"></i></template>
          </el-input>
        </el-form-item>
        <el-divider>修改密码</el-divider>
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
              v-model="updateUser.oldPassword"
              type="password"
              show-password
              placeholder="请输入原密码（不修改密码可留空）">
            <template #prefix><i class="el-icon-lock"></i></template>
          </el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
              v-model="updateUser.newPassword"
              type="password"
              show-password
              placeholder="请输入新密码（不修改密码可留空）">
            <template #prefix><i class="el-icon-key"></i></template>
          </el-input>
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
              v-model="updateUser.confirmPassword"
              type="password"
              show-password
              placeholder="请再次输入新密码（不修改密码可留空）">
            <template #prefix><i class="el-icon-key"></i></template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelUpdate">取 消</el-button>
        <el-button type="primary" @click="doUpdate" :loading="updating">确 定</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()

// 表单引用
const updateForm = ref(null)

// 用户信息
const userInfo = reactive({
  uid: window.sessionStorage.getItem('uid'),
  account: window.sessionStorage.getItem('account'),
  phone: '',
  email: ''
})
const updateUser = reactive({
  uid: '',
  account: '',
  phone: '',
  email: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 控制弹窗
const updateDialogVisible = ref(false)
const updating = ref(false)

// 个性化推荐相关
const recommendations = ref(null)
const loadingRecommendations = ref(false)

// 校验规则
const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号码'))
  } else {
    callback()
  }
}
const validateEmail = (rule, value, callback) => {
  if (value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error('请输入正确的邮箱地址'))
  } else {
    callback()
  }
}
const validateNewPassword = (rule, value, callback) => {
  // 如果新密码为空，说明不修改密码，允许通过
  if (!value || value.trim() === '') {
    callback()
    return
  }
  if (value === updateUser.oldPassword) {
    callback(new Error('新密码不能与原密码相同'))
  } else {
    if (updateUser.confirmPassword !== '') {
      updateForm.value?.validateField('confirmPassword')
    }
    callback()
  }
}
const validateConfirmPassword = (rule, value, callback) => {
  // 如果确认密码为空，说明不修改密码，允许通过
  if (!value || value.trim() === '') {
    callback()
    return
  }
  if (value !== updateUser.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ],
  oldPassword: [
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  newPassword: [
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' },
    { validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 获取用户信息
const fetchUserInfo = async () => {
  if (!userInfo.uid) {
    ElMessage.error('用户未登录')
    return
  }
  try {
    // 使用正确的API端点 - 通过 app-service
    const res = await request.get(`/app/users/getMyInfo/${userInfo.uid}`)
    if (res.data.code === 200) {
      Object.assign(userInfo, res.data.data)
    } else {
      ElMessage.error(res.data.message || '获取用户信息失败')
    }
  } catch (err) {
    console.error('获取用户信息失败:', err)
    ElMessage.error('获取用户信息失败')
  }
}

// 打开修改信息弹窗
const update = () => {
  updateUser.uid = userInfo.uid
  updateUser.account = userInfo.account
  updateUser.phone = userInfo.phone || ''
  updateUser.email = userInfo.email || ''
  updateUser.oldPassword = ''
  updateUser.newPassword = ''
  updateUser.confirmPassword = ''
  updateDialogVisible.value = true
}

// 取消修改信息
const cancelUpdate = () => {
  updateDialogVisible.value = false
  updateForm.value?.resetFields()
}

// 提交修改信息
const doUpdate = async () => {
  const valid = await updateForm.value?.validate().catch(() => false)
  if (!valid) return
  
      updating.value = true
      try {
    // 先更新基本信息
    const updateData = {
          uid: updateUser.uid,
          phone: updateUser.phone,
          email: updateUser.email
    }
    
    const res = await request.post('/app/users/updateUserInfo', updateData)
        if (res.data.code === 200) {
      // 如果填写了密码，则修改密码
      if (updateUser.oldPassword && updateUser.newPassword && updateUser.confirmPassword) {
        if (updateUser.newPassword !== updateUser.confirmPassword) {
          ElMessage.error('两次输入的密码不一致')
          updating.value = false
          return
        }
        try {
          const pwdRes = await request.post(`/app/users/changePassword/${updateUser.uid}`, {
            uid: updateUser.uid,
            oldPassword: updateUser.oldPassword,
            newPassword: updateUser.newPassword
          })
          if (pwdRes.data.code === 200) {
            ElMessage.success('信息和密码修改成功，请重新登录')
            updateDialogVisible.value = false
            setTimeout(() => {
              router.push('/login')
            }, 1500)
            return
          } else {
            ElMessage.error(pwdRes.data.message || '密码修改失败')
            updating.value = false
            return
          }
        } catch (pwdErr) {
          console.error('修改密码失败:', pwdErr)
          ElMessage.error('密码修改失败')
          updating.value = false
          return
        }
      }
      
          ElMessage.success('修改成功')
          updateDialogVisible.value = false
          fetchUserInfo()
        } else {
          ElMessage.error(res.data.message || '更新失败')
        }
      } catch (err) {
        console.error('更新用户信息失败:', err)
        ElMessage.error('更新失败')
      } finally {
        updating.value = false
      }
    }


// 个性化推荐方法
const loadRecommendations = async () => {
  if (!userInfo.uid) return

  loadingRecommendations.value = true
  try {
    // 使用正确的API端点 - 通过 app-service
    // request.js 会自动从 sessionStorage 获取 uid 并添加到请求头
    const response = await request.get('/app/recommendations')
    if (response.data.code === 200) {
      recommendations.value = response.data.data
    } else {
      console.error('加载推荐失败:', response.data.message)
      // 使用模拟数据作为备选
      recommendations.value = {
        dominantEmotion: '平静',
        advice: {
          tip: '尝试进行10分钟的冥想，可以帮助您保持平静的心态。'
        },
        items: [
          { id: 1, title: '晨间冥想', energyRequired: '低' },
          { id: 2, title: '阅读30分钟', energyRequired: '中' },
          { id: 3, title: '散步20分钟', energyRequired: '低' }
        ]
      }
    }
  } catch (error) {
    console.error('加载推荐失败:', error)
    // 使用模拟数据作为备选
    recommendations.value = {
      dominantEmotion: '平静',
      advice: {
        tip: '尝试进行10分钟的冥想，可以帮助您保持平静的心态。'
      },
      items: [
        { id: 1, title: '晨间冥想', energyRequired: '低' },
        { id: 2, title: '阅读30分钟', energyRequired: '中' },
        { id: 3, title: '散步20分钟', energyRequired: '低' }
      ]
    }
  } finally {
    loadingRecommendations.value = false
  }
}

const tryActivity = (activity) => {
  ElMessage.info(`准备执行活动: ${activity.title}`)
}

onMounted(() => {
  fetchUserInfo()
  loadRecommendations()
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.dashboard-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px;
  min-height: 100vh;
  background: transparent;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
}

.dashboard-title {
  font-size: 28px;
  font-weight: 600;
  color: #000000;
  text-shadow: none;
  display: flex;
  align-items: center;
}

.dashboard-title i {
  margin-right: 12px;
  font-size: 32px;
  color: #3498db;
}

.dashboard-content {
  display: grid;
  grid-template-columns: 1fr 1.5fr;
  gap: 30px;
}

@media (max-width: 1024px) {
  .dashboard-content {
    grid-template-columns: 1fr;
  }
}

.info-panel {
  padding: 30px 0;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
}

.panel-title {
  font-size: 20px;
  font-weight: 600;
  color: #000000;
  text-shadow: none;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item:hover {
  transform: translateY(-2px);
}

.info-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #e3f2fd;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  color: #3498db;
  font-size: 20px;
}

.info-details {
  flex: 1;
}

.info-label {
  font-size: 14px;
  color: #000000;
  text-shadow: none;
  margin-bottom: 4px;
}

.info-value {
  font-size: 18px;
  font-weight: 500;
  color: #000000;
  text-shadow: none;
}

.action-buttons {
  display: flex;
  justify-content: center;
  margin-top: 25px;
}

.action-btn {
  padding: 14px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn i {
  margin-right: 8px;
}

.edit-btn {
  background: rgba(230, 126, 115, 0.8);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
}

.edit-btn:hover {
  background: rgba(230, 126, 115, 0.9);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(230, 126, 115, 0.4);
}

.recommendations-panel {
  padding: 30px 0;
}

.recommendation-card {
  padding: 20px 0;
  margin-bottom: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.recommendation-card:last-child {
  border-bottom: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #000000;
  text-shadow: none;
}

.refresh-btn {
  background: #3498db;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  background: #2980b9;
  transform: translateY(-2px);
}

.refresh-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

.emotion-badge {
  display: inline-block;
  background: rgba(255, 255, 255, 0.9);
  color: #3498db;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 600;
  margin-bottom: 15px;
  text-shadow: none;
}

.advice-card {
  border-left: 4px solid #ffc107;
  padding: 15px 0;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(5px);
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.advice-card p {
  color: #2c3e50;
  text-shadow: none;
}

.advice-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.advice-icon {
  font-size: 20px;
  margin-right: 10px;
  color: #ff9800;
}

.advice-title {
  font-weight: 600;
  color: #ff9800;
  text-shadow: none;
}

.activity-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.activity-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-item:hover {
  transform: translateY(-2px);
}

.activity-info {
  flex: 1;
}

.activity-name {
  font-weight: 500;
  margin-bottom: 5px;
  color: #000000;
  text-shadow: none;
}

.activity-energy {
  font-size: 14px;
  color: #000000;
  text-shadow: none;
}

.try-btn {
  background: #2ecc71;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.try-btn:hover {
  background: #27ae60;
  transform: translateY(-2px);
}

.loading-state {
  text-align: center;
  padding: 40px 0;
}

.loading-text {
  font-size: 16px;
  color: #000000;
  text-shadow: none;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 15px;
  color: #bdc3c7;
}

.empty-text {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 10px;
  color: #000000;
  text-shadow: none;
}

.empty-hint {
  font-size: 14px;
  color: #000000;
  text-shadow: none;
}

/* 弹窗样式优化 */
:deep(.el-dialog) {
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

:deep(.el-dialog__header) {
  padding: 20px 20px 10px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

:deep(.el-dialog__title) {
  font-weight: 600;
  color: #2c3e50;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #2c3e50;
}

:deep(.el-input__inner) {
  border-radius: 10px;
  padding-left: 40px;
}

:deep(.el-input__prefix) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  color: #E67E73;
}

:deep(.el-dialog__footer) {
  padding: 15px 20px 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

:deep(.el-divider) {
  margin: 20px 0;
  border-color: rgba(0, 0, 0, 0.1);
}

:deep(.el-divider__text) {
  background-color: rgba(255, 255, 255, 0.95);
  color: #666;
  font-size: 14px;
}

:deep(.el-button--primary) {
  background: rgba(230, 126, 115, 0.8);
  border-color: rgba(230, 126, 115, 0.8);
  color: white;
}

:deep(.el-button--primary:hover) {
  background: rgba(230, 126, 115, 0.9);
  border-color: rgba(230, 126, 115, 0.9);
}
</style>
