<template>
  <div class="register-container">
    <div class="register-box">
      <el-card class="register-card">
        <div class="title">
          <h2>情绪日记本注册</h2>
          <p class="subtitle">开启你的情绪记录之旅</p>
        </div>
        <el-form 
          :model="registerForm" 
          :rules="registerRules" 
          ref="registerFormRef" 
          class="register-form"
        >
          <div class="form-item">
            <div class="label-box">
              <span class="required">*</span>
              <span class="label">账号</span>
            </div>
            <el-input
              v-model="registerForm.account"
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
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              class="custom-input">
            </el-input>
          </div>
          <div class="form-item">
            <div class="label-box">
              <span class="required">*</span>
              <span class="label">确认密码</span>
            </div>
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              class="custom-input">
            </el-input>
          </div>
          <div class="btn-container">
            <el-button
              type="primary"
              :loading="loading"
              @click="handleRegister"
              class="register-button">
              注册
            </el-button>
          </div>
          <div class="login-link">
            <el-button type="text" @click="router.push('/login')">
              已有账号？立即登录
            </el-button>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/user'

// 路由实例
const router = useRouter()

// 表单引用
const registerFormRef = ref(null)

// 加载状态
const loading = ref(false)

// 注册表单数据
const registerForm = reactive({
  account: '',
  password: '',
  confirmPassword: ''
})

// 密码验证函数
const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

// 表单验证规则
const registerRules = reactive({
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, message: '账号长度不能小于3位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ]
})

/**
 * 处理注册逻辑
 */
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        // 传递 account、password、confirmPassword 给后端
        const payload = {
          account: registerForm.account,
          password: registerForm.password,
          confirmPassword: registerForm.confirmPassword
        }
        const result = await register(payload)
        const { code, message } = result.data
        
        if (code === 1000 || code === 200) {
          ElMessage.success('注册成功')
          router.push('/login')
        } else {
          ElMessage.error(message || '注册失败')
        }
      } catch (error) {
        console.error('注册错误:', error)
        ElMessage.error('注册失败，请重试')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: url('/static/imgs/bg.jpg') no-repeat center center fixed;
  background-size: cover;
  overflow: hidden;
}

.register-box {
  width: 100%;
  max-width: 900px;
  padding: 20px;
  z-index: 10;
}

.register-card {
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

.register-card::before {
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

.register-card:hover {
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

.register-form {
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

.register-button {
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

.register-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(59, 130, 246, 0.6);
  background: linear-gradient(135deg, #2563EB, #3B82F6);
}

.register-button:active {
  transform: translateY(0px);
}

.login-link {
  text-align: center;
  margin-top: 16px;
}

.login-link .el-button {
  font-size: 14px;
  color: #606266;
  transition: all 0.3s ease;
  padding: 10px 15px;
}

.login-link .el-button:hover {
  color: #FF6B9D;
  transform: translateY(-1px);
}

@media screen and (max-width: 480px) {
  .register-box {
    padding: 15px;
  }

  .register-card {
    padding: 30px 25px;
  }

  .title h2 {
    font-size: 24px;
  }
}
</style>
