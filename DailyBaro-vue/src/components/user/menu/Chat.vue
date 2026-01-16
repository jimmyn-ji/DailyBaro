<template>
  <!-- 悬浮聊天按钮 -->
  <div class="chat-float-btn" @click="showChat = true" v-if="!showChat">
    <div class="float-icon">💬</div>
    <div class="float-text">AI助手</div>
  </div>

  <!-- 聊天弹窗 -->
  <div v-if="showChat" class="chat-overlay" @click="closeChat">
    <div class="chat-modal" @click.stop>
      <div class="chat-header">
        <h2 class="chat-title">墨语传心</h2>
        <p class="subtitle">与AI对话，获得情绪建议与关怀</p>
        <button class="close-btn" @click="closeChat">×</button>
      </div>
      <div class="chat-content">
        <div class="chat-history" ref="historyRef">
          <div v-for="(message, index) in messages" :key="index" :class="['message', message.role]">
            <img v-if="message.role==='ai'" class="avatar" :src="aiAvatar" />
            <div class="message-content" v-html="formatMessage(message.content)"></div>
            <img v-if="message.role==='user'" class="avatar user" :src="userAvatar" />
          </div>
        </div>
        <div class="chat-input">
          <el-input
            v-model="inputMessage"
            placeholder="请输入你的问题"
            @keyup.enter="sendMessage"
            class="custom-input"
          ></el-input>
          <el-button
            type="primary"
            :loading="loading"
            @click="sendMessage"
            class="send-button"
          >发送</el-button>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import aiAvatar from '@/static/imgs/ai.png'
import userAvatar from '@/static/imgs/avatar.png'

const showChat = ref(false)
const inputMessage = ref('')
const loading = ref(false)
const messages = ref([])
const historyRef = ref(null)

// 关闭聊天窗口
const closeChat = () => {
  showChat.value = false
}

// 格式化消息内容，去除Markdown标记并优化排版
const formatMessage = (content) => {
  if (!content) return ''
  
  return content
    // 去除 ** 标记
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    // 去除 --- 分隔线，替换为更美观的分隔
    .replace(/---/g, '<hr style="border: none; border-top: 1px solid #e0e0e0; margin: 15px 0;">')
    // 处理 ### 标题
    .replace(/###\s*(.*?)(?=\n|$)/g, '<h3 style="color: #333; margin: 15px 0 10px 0; font-size: 16px; font-weight: bold;">$1</h3>')
    // 处理 - 列表项
    .replace(/^\s*-\s*(.*?)(?=\n|$)/gm, '<li style="margin: 5px 0; padding-left: 10px;">$1</li>')
    // 将连续的列表项包装在 ul 中
    .replace(/(<li[^>]*>.*?<\/li>)+/gs, '<ul style="margin: 10px 0; padding-left: 20px;">$&</ul>')
    // 处理换行
    .replace(/\n/g, '<br>')
    // 处理多个连续的空行
    .replace(/(<br>\s*){3,}/g, '<br><br>')
}

const sendMessage = async () => {
  if (!inputMessage.value.trim()) {
    ElMessage.warning('请输入问题')
    return
  }
  loading.value = true
  const userMessage = inputMessage.value
  messages.value.push({ role: 'user', content: userMessage })
  await nextTick()
  if (historyRef.value) historyRef.value.scrollTop = historyRef.value.scrollHeight
  
  try {
    // 获取用户ID
    const uid = window.sessionStorage.getItem('uid')
    const headers = {
      'Content-Type': 'application/json'
    }
    if (uid) {
      headers['uid'] = uid
    }
    
    // 使用RAG增强的对话接口（如果提供了uid）
    const url = uid 
      ? `${window.location.origin}/app/ai/chat`
      : `${window.location.origin}/app/ai/query`
    
    const response = await fetch(url, {
      method: 'POST',
      headers: headers,
      body: JSON.stringify({ question: userMessage })
    })
    if (!response.ok) throw new Error('请求失败')
    const data = await response.json()
    if (data.code === 200) {
      messages.value.push({ role: 'ai', content: data.data })
    } else {
      ElMessage.error(data.message || 'AI 服务返回错误')
    }
    await nextTick()
    if (historyRef.value) historyRef.value.scrollTop = historyRef.value.scrollHeight
  } catch (error) {
    console.error('AI请求失败:', error)
    if (error.name === 'TypeError' && error.message.includes('timeout')) {
      ElMessage.error('AI响应超时，请稍后重试')
    } else {
    ElMessage.error('请求失败，请重试')
    }
  } finally {
    loading.value = false
    inputMessage.value = ''
  }
}
</script>
<style scoped>
/* 悬浮按钮样式 */
.chat-float-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #ff6b9d, #ff8fab);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(255, 107, 157, 0.3);
  transition: all 0.3s ease;
  z-index: 1000;
  border: 3px solid rgba(255, 255, 255, 0.8);
}

.chat-float-btn:hover {
  transform: translateY(-5px) scale(1.05);
  box-shadow: 0 8px 30px rgba(255, 107, 157, 0.4);
}

.float-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.float-text {
  font-size: 12px;
  color: white;
  font-weight: 500;
}

/* 聊天弹窗样式 */
.chat-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  backdrop-filter: blur(5px);
}

.chat-modal {
  background: rgba(255,255,255,0.95);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
  width: 90vw;
  height: 85vh;
  max-width: 800px;
  max-height: 700px;
  display: flex;
  flex-direction: column;
  position: relative;
  border: 2px solid rgba(255,255,255,0.8);
  backdrop-filter: blur(15px);
  overflow: hidden; /* 防止内容溢出容器 */
}

.chat-header {
  background: linear-gradient(135deg, #ff6b9d, #ff8fab);
  color: white;
  padding: 20px 30px;
  border-radius: 18px 18px 0 0;
  position: relative;
  text-align: center;
}

.chat-title {
  font-size: 28px;
  font-weight: bold;
  margin: 0 0 8px 0;
  letter-spacing: 2px;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.close-btn {
  position: absolute;
  top: 15px;
  right: 20px;
  background: none;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.3s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
  gap: 15px;
  overflow: hidden; /* 防止内容溢出 */
  min-height: 0; /* 允许 flex 子元素缩小 */
}

.chat-history {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden; /* 防止横向溢出 */
  padding: 15px;
  background: rgba(255,255,255,0.8);
  border-radius: 12px;
  border: 1px solid rgba(255,255,255,0.6);
  box-shadow: inset 0 2px 8px rgba(0,0,0,0.05);
  min-height: 0; /* 允许 flex 子元素缩小 */
  max-height: 100%; /* 限制最大高度 */
}

.message {
  display: flex;
  align-items: flex-end;
  margin-bottom: 15px;
}

.message.user {
  flex-direction: row-reverse;
}

.message .avatar {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  margin: 0 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.message .avatar.user {
  margin-left: 8px;
  margin-right: 0;
}

.message-content {
  max-width: 70%;
  padding: 10px 15px;
  border-radius: 15px;
  background: rgba(255,255,255,0.95);
  color: #333;
  word-break: break-word; /* 使用 break-word 而不是 break-all，更美观 */
  word-wrap: break-word; /* 兼容性 */
  overflow-wrap: break-word; /* 现代浏览器 */
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  overflow: hidden; /* 防止内容溢出 */
}

.message.ai .message-content {
  background: #ffb6c1;
  color: white;
  border-bottom-left-radius: 5px;
}

.message.user .message-content {
  background: #f7cac9;
  color: #fff;
  border-bottom-right-radius: 5px;
}

.chat-input {
  display: flex;
  gap: 12px;
  padding: 15px;
  background: rgba(255,255,255,0.8);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  border: 1px solid rgba(255,255,255,0.6);
}

.custom-input {
  flex: 1;
}

.custom-input :deep(.el-input__inner) {
  height: 45px;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
  padding: 0 15px;
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid transparent;
}

.custom-input :deep(.el-input__inner):focus {
  border-color: #ffb6c1;
  box-shadow: 0 0 0 3px rgba(255,182,193,0.15);
}

.send-button {
  height: 45px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
  background: linear-gradient(135deg, #ffb6c1, #f7cac9);
  border: none;
  transition: all 0.3s ease;
  color: #fff;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(255,182,193,0.2);
}

.send-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255,182,193,0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-modal {
    width: 95vw;
    height: 90vh;
  }
  
  .chat-float-btn {
    bottom: 20px;
    right: 20px;
    width: 70px;
    height: 70px;
  }
  
  .float-icon {
    font-size: 20px;
  }
  
  .float-text {
    font-size: 10px;
  }
}
</style>
