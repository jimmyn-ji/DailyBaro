<template>
  <div class="detail-bg">
    <div class="detail-card">
      <h2 class="detail-title">动态详情</h2>
      <div class="detail-content">{{ post.content }}</div>
      <div class="detail-meta">{{ post.create_time }}</div>
      <div class="detail-meta">可见范围: {{ post.visibility }}</div>
      <button
        class="like-btn"
        :class="{ liked: post.liked }"
        @click="likePost"
      >
        {{ post.liked ? '已点赞' : '点赞' }}({{ post.likeCount }})
      </button>
      <div class="comments">
        <div v-for="comment in post.comments" :key="comment.comment_id" class="comment-item">
          <span>{{ comment.content }}</span>
          <span class="comment-meta">{{ comment.create_time }}</span>
        </div>
        <input v-model="newComment" class="comment-input" placeholder="写评论..." />
        <button class="comment-btn" @click="addComment">评论</button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
const route = useRoute()
const post = ref({ content: '', create_time: '', visibility: '', likes: 0, comments: [] })
const newComment = ref('')
async function loadPost() {
  const res = await request.get(`/api/anonymous-posts/${route.params.id}`)
  post.value = res.data.data || { content: '', create_time: '', visibility: '', likes: 0, comments: [] }
}
async function likePost() {
  if (post.value.liked) {
    await request.delete(`/api/anonymous-posts/${route.params.id}/like`)
  } else {
    await request.post(`/api/anonymous-posts/${route.params.id}/like`)
  }
  loadPost()
}
async function addComment() {
  if (!newComment.value) return
  
  const postId = Number(route.params.id)
  console.log('当前postId:', postId, '类型:', typeof postId)
  console.log('route.params:', route.params)
  
  const commentData = { postId: postId, userId: 99999, content: newComment.value }
  console.log('发送的评论数据:', commentData)
  
  await request.post('/post/insertPost', commentData)
  newComment.value = ''
  loadPost()
}
onMounted(() => { loadPost() })
</script>
<style scoped>
.detail-bg {
  min-height: 100vh;
  background: linear-gradient(135deg, #7ec6e6 0%, #f7cac9 100%);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 40px 0;
}
.detail-card {
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 8px 32px rgba(126,198,230,0.10);
  padding: 36px 32px 32px 32px;
  width: 600px;
  max-width: 95vw;
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.detail-title {
  font-size: 28px;
  font-weight: bold;
  color: #7ec6e6;
  margin-bottom: 10px;
  letter-spacing: 2px;
  text-align: center;
}
.detail-content {
  font-size: 16px;
  color: #333;
  margin-bottom: 10px;
  white-space: pre-line;
}
.detail-meta {
  font-size: 14px;
  color: #999;
  margin-bottom: 6px;
  text-align: center;
}
.like-btn {
  background: #f0ad4e;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 6px 18px;
  font-size: 15px;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(240,173,78,0.10);
  cursor: pointer;
  align-self: flex-start;
}
.like-btn.liked {
  background: #ff7e67;
  color: #fff;
  border: 1px solid #ff7e67;
}
.comments {
  margin-top: 20px;
  background: #f7fafc;
  border-radius: 10px;
  padding: 16px 12px;
  box-shadow: 0 2px 8px rgba(126,198,230,0.06);
}
.comment-item {
  display: flex;
  gap: 10px;
  margin-bottom: 8px;
  font-size: 15px;
}
.comment-meta {
  color: #bbb;
  font-size: 13px;
}
.comment-input {
  width: 80%;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  padding: 8px;
  font-size: 15px;
  margin-right: 8px;
}
.comment-btn {
  background: #7ec6e6;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 8px 18px;
  font-size: 15px;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(126,198,230,0.10);
  cursor: pointer;
}
.anonymous-detail-bg {
  background: rgba(255,255,255,0.85) !important;
}
.anonymous-detail-card {
  background: rgba(255,255,255,0.95) !important;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
</style> 