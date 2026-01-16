<template>
  <div class="login-container">
    <div class="login-box">
      <el-card class="login-card">
        <div class="title">
          <h2>情绪日记本登录</h2>
          <p class="subtitle">记录你的心情，遇见更好的自己</p>
        </div>
        <el-form 
          :model="loginForm" 
          :rules="loginRules" 
          ref="loginFormRef" 
          class="login-form"
        >
          <div class="form-item">
            <div class="label-box">
              <span class="required">*</span>
              <span class="label">账号</span>
            </div>
            <el-input
              v-model="loginForm.account"
              placeholder="请输入账号"
              class="custom-input">
            </el-input>
          </div>
          <div class="form-item">
            <div class="label-box">
              <span class="required">*</span>
              <span class="label">密码</span>
            </div>
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              @keyup.enter="handleLogin"
              class="custom-input">
            </el-input>
          </div>
          <div class="forgot-password-link">
            <el-button type="text" @click="showForgotPasswordDialog = true" class="forgot-link">
              忘记密码？
            </el-button>
            <el-button type="text" @click="showDeleteDialog = true" class="delete-link">
              账号注销
            </el-button>
          </div>
          <div class="btn-container">
            <el-button
              type="primary"
              :loading="loading"
              @click="handleLogin"
              class="login-button">
              登录
            </el-button>
          </div>
          <div class="divider">
            <span>或</span>
          </div>
          <div class="social-login">
            <el-button class="social-btn qq-btn" @click="handleQQLogin">
              <i class="icon-qq">QQ</i>
            </el-button>
            <el-button class="social-btn email-btn" @click="showEmailLoginDialog = true">
              <i class="icon-email">📧</i>
            </el-button>
          </div>
          <div class="register-link">
            <el-button type="text" @click="router.push('/register')">
              还没有账号？立即注册
            </el-button>
          </div>
        </el-form>
      </el-card>
    </div>
    
    <!-- 忘记密码对话框 -->
    <el-dialog v-model="showForgotPasswordDialog" title="忘记密码" width="640px" :close-on-click-modal="false">
      <el-form :model="forgotPasswordForm" :rules="forgotPasswordRules" ref="forgotPasswordFormRef">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="forgotPasswordForm.email" placeholder="请输入注册邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForgotPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handleForgotPassword" :loading="forgotPasswordLoading">发送重置邮件</el-button>
      </template>
    </el-dialog>

    <!-- 邮箱登录对话框 -->
    <el-dialog v-model="showEmailLoginDialog" title="邮箱登录" width="640px" :close-on-click-modal="false">
      <el-form :model="emailLoginForm" :rules="emailLoginRules" ref="emailLoginFormRef">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="emailLoginForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div style="display: flex; gap: 10px;">
            <el-input v-model="emailLoginForm.code" placeholder="请输入验证码" />
            <el-button @click="sendEmailCode" :loading="sendingCode" :disabled="codeCountdown > 0">
              {{ codeCountdown > 0 ? `${codeCountdown}秒` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEmailLoginDialog = false">取消</el-button>
        <el-button type="primary" @click="handleEmailLogin" :loading="emailLoginLoading">登录</el-button>
      </template>
    </el-dialog>

    <!-- 账号注销对话框 -->
    <el-dialog v-model="showDeleteDialog" title="账号注销" width="640px" :close-on-click-modal="false">
      <el-form :model="deleteAccountForm" :rules="deleteAccountRules" ref="deleteAccountFormRef">
        <el-form-item label="账号" prop="account">
          <el-input v-model="deleteAccountForm.account" placeholder="请输入要注销的账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="deleteAccountForm.password" type="password" placeholder="请输入账号密码以验证身份" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDeleteDialog = false">取消</el-button>
        <el-button type="danger" @click="doDeleteAccount" :loading="deleting">确定注销</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/user'
import request, { emailRequest } from '@/utils/request'

// 路由实例
const router = useRouter()

// 表单引用
const loginFormRef = ref(null)

// 加载状态
const loading = ref(false)

// 登录表单数据
const loginForm = reactive({
  account: '',
  password: ''
})

// 表单验证规则
const loginRules = reactive({
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ]
})

const showDeleteDialog = ref(false)
const deleteAccountFormRef = ref(null)
const deleteAccountForm = reactive({
  account: '',
  password: ''
})
const deleteAccountRules = reactive({
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码以验证身份', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ]
})
const deleting = ref(false)

// 忘记密码相关
const showForgotPasswordDialog = ref(false)
const forgotPasswordFormRef = ref(null)
const forgotPasswordForm = reactive({
  email: ''
})
const forgotPasswordRules = reactive({
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
})
const forgotPasswordLoading = ref(false)

// 邮箱登录相关
const showEmailLoginDialog = ref(false)
const emailLoginFormRef = ref(null)
const emailLoginForm = reactive({
  email: '',
  code: ''
})
const emailLoginRules = reactive({
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 6, max: 6, message: '验证码长度必须为6位', trigger: 'blur' }
  ]
})
const emailLoginLoading = ref(false)
const sendingCode = ref(false)
const codeCountdown = ref(0)

/**
 * 处理登录逻辑
 */
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        const result = await login(loginForm)
        const { code, data: user, message } = result.data

        if (code === 200) { // 兼容后端code为200
          // 存储用户信息
          const uid = user.uid ? String(user.uid) : null
          if (!uid) {
            throw new Error('服务器未返回有效用户ID')
          }

          // 保存用户信息到 sessionStorage
          window.sessionStorage.setItem('uid', uid)
          window.sessionStorage.setItem('userInfo', JSON.stringify({
            uid: String(user.uid),
            account: user.account,
            role: user.role
          }))

          ElMessage.success('登录成功')

          // 跳转到情绪日记本主页
          router.push('/user/diary')
        } else {
          ElMessage.error(message)
        }
      } catch (error) {
        console.error('登录错误:', error)
        ElMessage.error('登录失败，请重试')
      } finally {
        loading.value = false
      }
    }
  })
}

// 忘记密码
const handleForgotPassword = async () => {
  if (!forgotPasswordFormRef.value) return
  await forgotPasswordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        forgotPasswordLoading.value = true
        const res = await request.post('/api/auth/password/forgot', { email: forgotPasswordForm.email })
        if (res.data.code === 200) {
          ElMessage.success('重置密码邮件已发送，请查收')
          showForgotPasswordDialog.value = false
          forgotPasswordForm.email = ''
        } else {
          ElMessage.error(res.data.message || '发送失败')
        }
      } catch (error) {
        ElMessage.error('发送失败，请重试')
      } finally {
        forgotPasswordLoading.value = false
      }
    }
  })
}

// 发送邮箱验证码
const sendEmailCode = async () => {
  if (!emailLoginForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  try {
    sendingCode.value = true
    const res = await emailRequest.post('/api/auth/email/send-verification', { email: emailLoginForm.email })
    console.log('发送验证码响应:', res)
    if (res.data.code === 200) {
      // 检查是否是开发模式（验证码包含在消息中）
      const message = res.data.data || ''
      if (message.includes('验证码已生成（开发模式）')) {
        // 提取验证码并显示
        const codeMatch = message.match(/验证码已生成（开发模式）: (\d+)/)
        if (codeMatch) {
          const code = codeMatch[1]
          ElMessage({
            message: `开发模式：验证码是 ${code}（已自动填充）`,
            type: 'success',
            duration: 5000,
            showClose: true
          })
          // 自动填充验证码
          emailLoginForm.code = code
        } else {
          ElMessage.success(message)
        }
      } else {
        ElMessage.success('验证码已发送到邮箱')
      }
      codeCountdown.value = 60
      const timer = setInterval(() => {
        codeCountdown.value--
        if (codeCountdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      ElMessage.error(res.data.message || '发送失败')
    }
  } catch (error) {
    console.error('发送验证码错误:', error)
    console.error('错误详情:', error.response?.data || error.message)
    const errorMsg = error.response?.data?.message || error.message || '发送失败，请重试'
    ElMessage.error(errorMsg)
  } finally {
    sendingCode.value = false
  }
}

// 邮箱登录
const handleEmailLogin = async () => {
  if (!emailLoginFormRef.value) return
  await emailLoginFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        emailLoginLoading.value = true
        const res = await request.post('/api/auth/login/email', {
          email: emailLoginForm.email,
          verificationCode: emailLoginForm.code
        })
        if (res.data.code === 200) {
          const user = res.data.data
          const uid = user.uid ? String(user.uid) : null
          if (uid) {
            window.sessionStorage.setItem('uid', uid)
            window.sessionStorage.setItem('userInfo', JSON.stringify({
              uid: String(user.uid),
              account: user.account,
              role: user.role
            }))
            ElMessage.success('登录成功')
            showEmailLoginDialog.value = false
            router.push('/user/diary')
          }
        } else {
          ElMessage.error(res.data.message || '登录失败')
        }
      } catch (error) {
        ElMessage.error('登录失败，请重试')
      } finally {
        emailLoginLoading.value = false
      }
    }
  })
}

// QQ登录
const handleQQLogin = () => {
  // 获取QQ登录授权URL
  request.get('/api/auth/oauth/url?provider=qq').then(res => {
    if (res.data.code === 200 && res.data.data && res.data.data.url) {
      window.location.href = res.data.data.url
    } else {
      const errorMsg = res.data.message || 'QQ登录暂不可用'
      ElMessage.error(errorMsg)
    }
  }).catch((error) => {
    const errorMsg = error.response?.data?.message || error.message || 'QQ登录暂不可用'
    ElMessage.error(errorMsg)
  })
}

const doDeleteAccount = async () => {
  if (!deleteAccountFormRef.value) return
  
  await deleteAccountFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
  deleting.value = true
        // 先验证账号和密码
        const loginResult = await login({
          account: deleteAccountForm.account,
          password: deleteAccountForm.password
        })
        
        if (loginResult.data.code === 200) {
          // 验证成功，获取用户ID并删除账号
          const uid = loginResult.data.data.uid
          if (uid) {
            const res = await request.delete(`/users/deleteAccount/${uid}`)
            if (res.data.code === 200) {
              ElMessage.success('账号已成功注销')
      showDeleteDialog.value = false
              deleteAccountForm.account = ''
              deleteAccountForm.password = ''
              // 清除本地存储
              window.sessionStorage.clear()
              // 跳转到登录页
              router.push('/login')
            } else {
              ElMessage.error(res.data.message || '注销失败')
            }
          } else {
            ElMessage.error('无法获取用户信息')
          }
    } else {
          ElMessage.error('账号或密码错误，验证失败')
    }
  } catch (err) {
        console.error('注销失败:', err)
        ElMessage.error('注销失败，请检查账号和密码是否正确')
  } finally {
    deleting.value = false
  }
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  position: relative;
  background: url('/static/imgs/bg.jpg') no-repeat center center fixed;
  background-size: cover;
}

.login-box {
  width: 100%;
  max-width: 900px;
  padding: 20px;
}

.login-card {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  box-shadow: 
    0 25px 80px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(25px);
  padding: 20px 40px;
  position: relative;
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
  overflow: visible;
}

.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.1) 0%,
    rgba(255, 255, 255, 0.05) 50%,
    rgba(255, 255, 255, 0.1) 100%);
  border-radius: 25px;
  z-index: -1;
}

.login-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}
.title {
  text-align: center;
  margin-bottom: 18px;
}

.title h2 {
  color: #FFFFFF;
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.subtitle {
  color: rgba(255, 255, 255, 0.8);
  margin-top: 8px;
  font-size: 14px;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
}

.login-form {
  margin: 0 auto;
  width: 100%;
}

.form-item {
  margin-bottom: 16px;
  position: relative;
}

.label-box {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  padding-left: 5px;
}

.required {
  color: #F56C6C;
  margin-right: 4px;
  font-size: 14px;
}

.label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
}

.custom-input {
  width: 100%;
}

.custom-input :deep(.el-input__inner) {
  height: 42px;
  border-radius: 12px;
  font-size: 14px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
  padding: 0 15px;
  background: rgba(255, 255, 255, 0.2);
  color: #000000;
  backdrop-filter: blur(10px);
}

.custom-input :deep(.el-input__inner)::placeholder {
  color: rgba(255, 255, 255, 0.6);
}

.custom-input :deep(.el-input__inner):focus {
  border-color: rgba(255, 182, 193, 0.8);
  box-shadow: 0 0 0 2px rgba(255, 182, 193, 0.2);
  background: rgba(255, 255, 255, 0.9);
}

.custom-input :deep(.el-input__inner):hover {
  border-color: rgba(255, 255, 255, 0.6);
  background: rgba(255, 255, 255, 0.8);
}

.btn-container {
  margin-top: 16px;
  padding: 0 15px;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 18px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #3B82F6, #60A5FA);
  border: none;
  transition: all 0.3s ease;
  letter-spacing: 2px;
  color: white;
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.4);
  backdrop-filter: blur(10px);
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(59, 130, 246, 0.6);
  background: linear-gradient(135deg, #2563EB, #3B82F6);
}

.login-button:active {
  transform: translateY(0px);
}

.register-link {
  text-align: center;
  margin-top: 16px;
}

.register-link .el-button {
  font-size: 14px;
  color: #606266;
  transition: all 0.3s ease;
  padding: 10px 15px;
}

.forgot-password-link {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  margin-bottom: 10px;
}

.forgot-link,
.delete-link {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  padding: 0;
}

.forgot-link:hover {
  color: #FF6B9D;
}

.delete-link:hover {
  color: #F56C6C;
}

.divider {
  text-align: center;
  margin: 16px 0;
  position: relative;
}

.divider::before,
.divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 40%;
  height: 1px;
  background: rgba(255, 255, 255, 0.3);
}

.divider::before {
  left: 0;
}

.divider::after {
  right: 0;
}

.divider span {
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
  background: rgba(255, 255, 255, 0.1);
  padding: 0 10px;
  position: relative;
}

.social-login {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-bottom: 16px;
}

.social-btn {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.social-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
}

.qq-btn:hover {
  background: rgba(18, 183, 245, 0.3);
  border-color: rgba(18, 183, 245, 0.5);
}

.email-btn:hover {
  background: rgba(255, 182, 193, 0.3);
  border-color: rgba(255, 182, 193, 0.5);
}

.icon-qq,
.icon-email {
  font-size: 20px;
  font-style: normal;
}

.register-link .el-button:hover {
  color: #FF6B9D;
  transform: translateY(-1px);
}


@media screen and (max-width: 480px) {
  .login-box {
    padding: 15px;
  }

  .login-card {
    padding: 30px 25px;
  }

  .title h2 {
    font-size: 24px;
  }
}
</style>
