<template>
  <div class="galaxy-bg">
    <!-- 银河系背景元素 -->
    <div class="galaxy-stars"></div>
    <div class="galaxy-spiral"></div>
    <div class="galaxy-nebula"></div>

    <!-- 漂浮的星球元素 -->
    <div class="floating-planet p1"></div>
    <div class="floating-planet p2"></div>
    <div class="floating-planet p3"></div>

    <div class="planet-container">
      <div class="planet-card">
        <div class="planet-header">
          <div class="planet-icon">🌌</div>
          <h2 class="planet-title">匿名星球</h2>
          <div class="planet-subtitle">在浩瀚银河中分享你的思绪</div>
        </div>

        <div class="post-form">
          <div class="input-container">
            <textarea
                v-model="newPost.content"
                placeholder="向宇宙发射你的信号..."
                class="cosmic-input"
                @input="handleInput"
                ref="textareaRef"
            ></textarea>
            <div class="cosmic-emoji-panel" v-if="showEmojiPanel">
              <div class="emoji-grid">
                <span
                    v-for="emoji in emojiList"
                    :key="emoji"
                    class="cosmic-emoji"
                    @click="insertEmoji(emoji)"
                >
                  {{ emoji }}
                </span>
              </div>
            </div>
          </div>

          <div class="cosmic-controls">
            <button class="cosmic-emoji-btn" @click="toggleEmojiPanel">🌠</button>

            <div class="visibility-selector">
              <label>信号范围:</label>
              <select v-model="newPost.visibility" class="cosmic-select">
                <option value="public">全宇宙可见</option>
                <option value="private">仅自己可见</option>
              </select>
            </div>

            <button @click="createPost" class="cosmic-post-btn">发射信号</button>
          </div>
        </div>

        <div class="cosmic-posts">
          <div v-for="post in posts" :key="post.postId" class="cosmic-post">
            <div class="post-meteor"></div>

            <div class="post-header">
              <span class="post-date">{{ formatTime(post.createTime) }}</span>
              <span class="post-visibility" v-if="post.visibility === 'private'">🔒</span>
            </div>

            <div class="post-content">
              <p>{{ post.content }}</p>
            </div>

            <div class="cosmic-actions">
              <button class="cosmic-comment-btn" @click.stop="showComments(post)">
                <span class="action-icon">💬</span>
                <span class="action-text">回应</span>
              </button>

              <button
                  class="cosmic-like-btn"
                  :class="{ liked: post.liked }"
                  @click.stop="likePost(post)"
              >
                <span class="action-icon">{{ post.liked ? '🚀' : '✧' }}</span>
                <span class="action-text">{{ post.liked ? '已助推' : '助推' }}({{ post.likeCount }})</span>
              </button>
            </div>

            <div class="cosmic-comments" v-if="post.showComments">
              <div class="comments-orbit">
                <div v-if="post.comments && post.comments.length > 0" class="comments-container">
                  <div v-for="comment in post.comments" :key="comment.commentId" class="cosmic-comment">
                    <span class="comment-author">🌠 匿名宇航员:</span>
                    <span class="comment-message">{{ comment.content }}</span>
                  </div>
                </div>

                <div v-else class="no-comments">
                  <div class="empty-comet"></div>
                  <span>暂无太空回应</span>
                </div>

                <div class="comment-transmitter">
                  <input
                      v-model="post.newComment"
                      placeholder="发送回应信号..."
                      class="cosmic-comment-input"
                      @keyup.enter="addComment(post)"
                  />
                  <button class="cosmic-comment-submit" @click="addComment(post)">
                    <span class="transmit-icon">📡</span>
                    <span>发射</span>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div v-if="posts.length === 0" class="cosmic-empty">
            <div class="empty-planet"></div>
            <p>太空中还没有信号</p>
            <p class="cosmic-hint">成为第一个向宇宙发声的人</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import dayjs from 'dayjs'

const router = useRouter()
const posts = ref([])
const newPost = ref({ content: '', visibility: 'public' })
const showEmojiPanel = ref(false)
const textareaRef = ref(null)
const emojiList = ['😊', '😂', '😍', '🤔', '😭', '😡', '😱', '😴', '🤗', '😎', '🥰', '😋', '🤩', '😇', '🤠', '👻', '💪', '👍', '👎', '❤️', '💔', '💯', '🔥', '💩', '🌈', '⭐', '🎉', '🎊', '🎈', '🎁', '🍕', '🍔', '🍦', '☕', '🍺', '🍷', '🚗', '✈️', '🏠', '🌙', '☀️', '🌧️', '❄️', '🌸', '🌹', '🌻', '🌴', '🐱', '🐶', '🐼', '🦄', '🐸', '🦋', '🐞', '🦁', '🐯', '🐨', '🐷', '🐸', '🐙', '🦑', '🦐', '🦞', '🦀', '🐡', '🐠', '🐟', '🐬', '🐳', '🐋', '🦈', '🐊', '🐅', '🐆', '🦓', '🦍', '🦧', '🐘', '🦛', '🦏', '🐪', '🐫', '🦒', '🦘', '🐃', '🐂', '🐄', '🐎', '🐖', '🐏', '🐑', '🐐', '🦌', '🐕', '🐩', '🐈', '🐓', '🦃', '🦚', '🦜', '🦢', '🦩', '🕊️', '🐇', '🦝', '🦨', '🦡', '🦫', '🦦', '🦥', '🐁', '🐀', '🐿️', '🦔']

async function loadPosts() {
  const res = await request.get('/api/anonymous-posts')
  posts.value = res.data.data || []
  posts.value.forEach(post => {
    post.showComments = false
    post.newComment = ''
  })
}

async function likePost(post) {
  if (post.liked) {
    await request.delete(`/api/anonymous-posts/${post.postId}/like`)
  } else {
    await request.post(`/api/anonymous-posts/${post.postId}/like`)
  }
  loadPosts()
}

async function showComments(post) {
  if (!post.showComments) {
    try {
      const response = await request.get(`/api/anonymous-posts/${post.postId}/comments`)
      if (response.data.code === 200) {
        post.comments = response.data.data || []
      } else {
        post.comments = []
      }
    } catch (error) {
      console.error('获取评论失败:', error)
      post.comments = []
    }
  }
  post.showComments = !post.showComments
}

async function addComment(post) {
  if (!post.newComment || !post.newComment.trim()) return

  try {
    const commentData = { content: post.newComment.trim() }
    const response = await request.post(`/api/anonymous-posts/${post.postId}/comments`, commentData)
    if (response.data.code === 200) {
      post.newComment = ''
      await showComments(post)
    }
  } catch (error) {
    console.error('发表评论失败:', error)
  }
}

async function createPost() {
  if (!newPost.value.content.trim()) return
  await request.post('/api/anonymous-posts', {
    content: newPost.value.content,
    visibility: newPost.value.visibility
  })
  newPost.value = { content: '', visibility: 'public' }
  loadPosts()
}

function formatTime(time) {
  if (!time) return ''
  const t = typeof time === 'number' ? time : Number(time)
  return dayjs(t).format('YYYY.M.D HH:mm')
}

function toggleEmojiPanel() {
  showEmojiPanel.value = !showEmojiPanel.value
}

function insertEmoji(emoji) {
  const textarea = textareaRef.value
  if (textarea) {
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    const text = newPost.value.content
    newPost.value.content = text.substring(0, start) + emoji + text.substring(end)
    nextTick(() => {
      textarea.focus()
      textarea.setSelectionRange(start + emoji.length, start + emoji.length)
    })
  }
  showEmojiPanel.value = false
}

function handleInput() {
  // 输入处理逻辑
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
/* 银河系背景 */
.galaxy-bg {
  min-height: 100vh;
  padding: 40px 20px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  background: radial-gradient(ellipse at bottom, #1B2735 0%, #090A0F 100%);
  position: relative;
  overflow: hidden;
  color: #e0f7ff;
}

/* 星空背景 */
.galaxy-stars {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100"><circle cx="10" cy="10" r="0.5" fill="white" opacity="0.8"/><circle cx="30" cy="25" r="0.7" fill="white" opacity="0.6"/><circle cx="70" cy="15" r="1" fill="white" opacity="0.9"/><circle cx="5" cy="50" r="0.8" fill="white" opacity="0.7"/><circle cx="95" cy="60" r="0.6" fill="white" opacity="0.5"/><circle cx="25" cy="80" r="0.9" fill="white" opacity="0.8"/><circle cx="65" cy="90" r="0.5" fill="white" opacity="0.6"/></svg>');
  animation: twinkle 5s infinite alternate;
  z-index: 1;
}

/* 银河螺旋 */
.galaxy-spiral {
  position: fixed;
  top: 50%;
  left: 50%;
  width: 200vw;
  height: 200vh;
  background:
      radial-gradient(circle, transparent 60%, rgba(75, 0, 130, 0.1) 60%, rgba(75, 0, 130, 0.1) 65%, transparent 65%),
      radial-gradient(circle, transparent 50%, rgba(138, 43, 226, 0.1) 50%, rgba(138, 43, 226, 0.1) 55%, transparent 55%),
      radial-gradient(circle, transparent 40%, rgba(123, 104, 238, 0.1) 40%, rgba(123, 104, 238, 0.1) 45%, transparent 45%);
  transform: translate(-50%, -50%) rotate(15deg);
  z-index: 1;
  opacity: 0.3;
  animation: spiralRotate 600s linear infinite;
}

/* 星云效果 */
.galaxy-nebula {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background:
      radial-gradient(circle at 20% 30%, rgba(138, 43, 226, 0.05) 0%, transparent 30%),
      radial-gradient(circle at 80% 70%, rgba(0, 191, 255, 0.05) 0%, transparent 30%),
      radial-gradient(circle at 40% 60%, rgba(147, 112, 219, 0.05) 0%, transparent 30%);
  z-index: 1;
}

/* 漂浮星球 */
.floating-planet {
  position: fixed;
  border-radius: 50%;
  filter: blur(1px);
  opacity: 0.6;
  z-index: 1;
  animation: floatPlanet 60s infinite linear;
}

.floating-planet.p1 {
  width: 150px;
  height: 150px;
  background: radial-gradient(circle at 30% 30%, #483d8b, #2e0854);
  top: 15%;
  left: 10%;
  animation-duration: 80s;
}

.floating-planet.p2 {
  width: 100px;
  height: 100px;
  background: radial-gradient(circle at 40% 40%, #4169e1, #00008b);
  top: 70%;
  left: 80%;
  animation-duration: 100s;
  animation-delay: 20s;
}

.floating-planet.p3 {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle at 50% 50%, #9370db, #4b0082);
  top: 30%;
  left: 70%;
  animation-duration: 120s;
  animation-delay: 40s;
}

@keyframes twinkle {
  0% { opacity: 0.3; }
  100% { opacity: 1; }
}

@keyframes spiralRotate {
  0% { transform: translate(-50%, -50%) rotate(15deg); }
  100% { transform: translate(-50%, -50%) rotate(375deg); }
}

@keyframes floatPlanet {
  0% { transform: translateY(0) rotate(0deg); }
  25% { transform: translateY(-50px) rotate(90deg); }
  50% { transform: translateY(0) rotate(180deg); }
  75% { transform: translateY(50px) rotate(270deg); }
  100% { transform: translateY(0) rotate(360deg); }
}

/* 主容器 */
.planet-container {
  width: 100%;
  max-width: 600px;
  position: relative;
  z-index: 2;
}

.planet-card {
  background: rgba(11, 13, 27, 0.8);
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(138, 43, 226, 0.2);
  display: flex;
  flex-direction: column;
  gap: 25px;
}

/* 星球标题 */
.planet-header {
  text-align: center;
  margin-bottom: 20px;
}

.planet-icon {
  font-size: 50px;
  margin-bottom: 10px;
  animation: pulse 3s infinite ease-in-out;
}

.planet-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(90deg, #9370db, #4169e1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 2px;
  text-shadow: 0 2px 10px rgba(65, 105, 225, 0.3);
}

.planet-subtitle {
  font-size: 14px;
  color: #a5b4fc;
  font-style: italic;
  margin-top: 5px;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.8; }
  50% { transform: scale(1.1); opacity: 1; }
}

/* 发布表单 */
.post-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.input-container {
  position: relative;
}

.cosmic-input {
  width: 100%;
  min-height: 120px;
  padding: 15px;
  border-radius: 12px;
  border: 1px solid rgba(138, 43, 226, 0.3);
  background: rgba(15, 23, 42, 0.7);
  color: #e0f7ff;
  font-size: 15px;
  line-height: 1.5;
  resize: vertical;
  transition: all 0.3s ease;
}

.cosmic-input:focus {
  outline: none;
  border-color: #9370db;
  box-shadow: 0 0 0 2px rgba(147, 112, 219, 0.3);
  background: rgba(15, 23, 42, 0.9);
}

.cosmic-input::placeholder {
  color: #6b7280;
}

.cosmic-controls {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.cosmic-emoji-btn {
  background: rgba(138, 43, 226, 0.2);
  border: 1px solid rgba(138, 43, 226, 0.3);
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 18px;
  color: #e0f7ff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cosmic-emoji-btn:hover {
  background: rgba(138, 43, 226, 0.3);
  transform: scale(1.1);
}

.visibility-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.visibility-selector label {
  font-size: 14px;
  color: #a5b4fc;
  white-space: nowrap;
}

.cosmic-select {
  flex: 1;
  background: rgba(15, 23, 42, 0.7);
  border: 1px solid rgba(138, 43, 226, 0.3);
  border-radius: 8px;
  padding: 8px 12px;
  color: #e0f7ff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cosmic-select:focus {
  outline: none;
  border-color: #9370db;
  box-shadow: 0 0 0 2px rgba(147, 112, 219, 0.3);
}

.cosmic-post-btn {
  background: linear-gradient(135deg, #9370db, #4169e1);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(65, 105, 225, 0.3);
}

.cosmic-post-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(65, 105, 225, 0.4);
}

/* 表情面板 */
.cosmic-emoji-panel {
  position: absolute;
  bottom: 100%;
  left: 0;
  background: rgba(11, 13, 27, 0.95);
  border: 1px solid rgba(138, 43, 226, 0.3);
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
  padding: 10px;
  width: 300px;
  max-height: 200px;
  overflow-y: auto;
  z-index: 10;
  backdrop-filter: blur(10px);
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 5px;
}

.cosmic-emoji {
  font-size: 20px;
  padding: 5px;
  cursor: pointer;
  text-align: center;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.cosmic-emoji:hover {
  background: rgba(138, 43, 226, 0.3);
  transform: scale(1.2);
}

/* 动态列表 */
.cosmic-posts {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.cosmic-post {
  background: rgba(15, 23, 42, 0.7);
  border-radius: 14px;
  padding: 20px;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(138, 43, 226, 0.2);
  transition: all 0.3s ease;
}

.cosmic-post:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(65, 105, 225, 0.2);
  border-color: rgba(138, 43, 226, 0.4);
}

.post-meteor {
  position: absolute;
  top: -10px;
  right: -10px;
  width: 40px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #9370db, transparent);
  transform: rotate(-45deg);
  opacity: 0.6;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.post-date {
  font-size: 13px;
  color: #6b7280;
}

.post-visibility {
  font-size: 14px;
  color: #6b7280;
}

.post-content {
  margin-bottom: 15px;
}

.post-content p {
  margin: 0;
  font-size: 15px;
  line-height: 1.6;
  color: #e0f7ff;
}

/* 动态操作按钮 */
.cosmic-actions {
  display: flex;
  gap: 10px;
}

.cosmic-comment-btn, .cosmic-like-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(138, 43, 226, 0.1);
  border: 1px solid rgba(138, 43, 226, 0.2);
  border-radius: 8px;
  padding: 6px 12px;
  font-size: 14px;
  color: #e0f7ff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cosmic-comment-btn:hover {
  background: rgba(138, 43, 226, 0.2);
}

.cosmic-like-btn {
  background: rgba(65, 105, 225, 0.1);
  border: 1px solid rgba(65, 105, 225, 0.2);
}

.cosmic-like-btn:hover {
  background: rgba(65, 105, 225, 0.2);
}

.cosmic-like-btn.liked {
  background: rgba(255, 69, 0, 0.2);
  border: 1px solid rgba(255, 69, 0, 0.3);
  color: #ff7e67;
}

.action-icon {
  font-size: 16px;
}

.action-text {
  font-size: 13px;
}

/* 评论区域 */
.cosmic-comments {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid rgba(138, 43, 226, 0.1);
}

.comments-orbit {
  background: rgba(11, 13, 27, 0.6);
  border-radius: 10px;
  padding: 15px;
  position: relative;
  overflow: hidden;
}

.comments-orbit::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background:
      radial-gradient(circle at 10% 10%, rgba(138, 43, 226, 0.05) 0%, transparent 20%),
      radial-gradient(circle at 90% 90%, rgba(65, 105, 225, 0.05) 0%, transparent 20%);
  z-index: -1;
}

.comments-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 15px;
}

.cosmic-comment {
  display: flex;
  gap: 8px;
  font-size: 14px;
  line-height: 1.4;
  padding: 8px 0;
  border-bottom: 1px dashed rgba(138, 43, 226, 0.1);
}

.comment-author {
  color: #9370db;
  font-weight: 500;
  flex-shrink: 0;
}

.comment-message {
  color: #e0f7ff;
}

.no-comments {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 0;
  color: #6b7280;
  font-size: 14px;
  gap: 10px;
}

.empty-comet {
  width: 40px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #9370db, transparent);
  transform: rotate(-15deg);
  opacity: 0.6;
}

/* 评论输入 */
.comment-transmitter {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.cosmic-comment-input {
  flex: 1;
  background: rgba(15, 23, 42, 0.7);
  border: 1px solid rgba(138, 43, 226, 0.3);
  border-radius: 8px;
  padding: 8px 12px;
  color: #e0f7ff;
  font-size: 14px;
  transition: all 0.3s ease;
}

.cosmic-comment-input:focus {
  outline: none;
  border-color: #9370db;
  box-shadow: 0 0 0 2px rgba(147, 112, 219, 0.3);
}

.cosmic-comment-submit {
  display: flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, #9370db, #4169e1);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cosmic-comment-submit:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 10px rgba(65, 105, 225, 0.3);
}

.transmit-icon {
  font-size: 16px;
}

/* 空状态 */
.cosmic-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background: rgba(15, 23, 42, 0.5);
  border-radius: 16px;
  border: 2px dashed rgba(138, 43, 226, 0.3);
  text-align: center;
}

.empty-planet {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, #483d8b, #2e0854);
  margin-bottom: 20px;
  position: relative;
}

.empty-planet::after {
  content: '';
  position: absolute;
  top: -10px;
  right: -10px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.8);
  filter: blur(2px);
}

.cosmic-empty p {
  font-size: 16px;
  color: #a5b4fc;
  margin: 5px 0;
}

.cosmic-hint {
  font-size: 14px;
  color: #6b7280;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .planet-card {
    padding: 20px;
  }

  .planet-title {
    font-size: 24px;
  }

  .cosmic-controls {
    flex-direction: column;
    align-items: stretch;
  }

  .visibility-selector {
    order: -1;
  }

  .cosmic-emoji-panel {
    width: 100%;
  }

  .emoji-grid {
    grid-template-columns: repeat(6, 1fr);
  }
}
</style>
