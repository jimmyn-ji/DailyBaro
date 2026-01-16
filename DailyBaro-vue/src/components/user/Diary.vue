<template>
  <div class="diary-bg blog-layout">
    <div class="blog-container">
      <div v-if="!uid" class="empty-state">请先登录</div>
      <template v-else>

        <!-- 治愈系功能按钮区域 -->
        <div class="action-bar">
          <div class="action-group">
            <button class="action-btn write" @click="goEdit()">
              <div class="btn-icon"></div>
              <span>写日记</span>
            </button>
            <button class="action-btn search" @click="showSearch = !showSearch">
              <div class="btn-icon"></div>
              <span>搜索</span>
            </button>
            <button class="action-btn list" @click="showMyDiaries = !showMyDiaries">
              <div class="btn-icon"></div>
              <span>我的日记</span>
            </button>
          </div>
        </div>

        <!-- 治愈系搜索区域 -->
        <transition name="fade">
          <div v-if="showSearch" class="search-section">
            <div class="search-container">
              <div class="search-header">
                <div class="search-icon">🔎</div>
                <h3 class="search-title">搜索日记</h3>
              </div>
              <div class="search-form">
                <div class="form-row">
                  <div class="input-field">
                    <label>选择日期</label>
                    <el-date-picker
                      v-model="searchDate"
                      type="date"
                      placeholder="选择日期"
                      clearable
                      :editable="false"
                      :close-on-select="true"
                      class="healing-date-picker"
                    />
                  </div>
                  <div class="input-field">
                    <label>关键词</label>
                    <input type="text" placeholder="输入关键词..." class="healing-input" v-model="searchKeyword" />
                  </div>
                  <button class="search-submit-btn" @click="performSearch">
                    <span>搜索</span>
                    <div class="btn-arrow">→</div>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </transition>

        <!-- 治愈系状态筛选 -->
        <div class="filter-section">
          <div class="filter-label">筛选状态：</div>
          <div class="filter-buttons">
            <button 
              class="filter-btn" 
              :class="{ active: currentStatus === 'all' }"
              @click="filterByStatus('all')"
            >
              <span class="filter-icon">🌈</span>
              全部
            </button>
            <button 
              class="filter-btn" 
              :class="{ active: currentStatus === 'published' }"
              @click="filterByStatus('published')"
            >
              <span class="filter-icon">✨</span>
              已发布
            </button>
            <button 
              class="filter-btn" 
              :class="{ active: currentStatus === 'draft' }"
              @click="filterByStatus('draft')"
            >
              <span class="filter-icon">📝</span>
              草稿箱
            </button>
          </div>
        </div>

        <!-- Blog风格日记列表 -->
        <div class="diary-list blog-style">
          <div v-if="Array.isArray(diaries) && diaries.length > 0" class="blog-posts">
            <article v-for="(diary, index) in diaries" :key="diary.diaryId" class="blog-post" @click="viewDiary(diary)">
              <!-- 文章头部 -->
              <header class="post-header">
                <h2 class="post-title">{{ diary.title || '无标题' }}</h2>
                <div class="post-meta">
                  <time class="post-date">
                    <span class="meta-icon">📅</span>
                    {{ formatDate(diary.createTime) }}
                  </time>
                  <span v-if="diary.status === 'draft'" class="post-status draft">草稿</span>
                  <span v-else-if="diary.status === 'published'" class="post-status published">已发布</span>
                  </div>
              </header>
              
              <!-- 文章内容预览 -->
              <div class="post-content">
                <p class="post-excerpt">{{ diary.content ? diary.content.substring(0, 150) + (diary.content.length > 150 ? '...' : '') : '暂无内容' }}</p>
                </div>
              
              <!-- 文章标签和元信息 -->
              <footer class="post-footer">
                <div class="post-tags" v-if="Array.isArray(diary.tags) && diary.tags.length">
                  <span class="tag" v-for="(tag, idx) in diary.tags" :key="idx" @click.stop>
                    #{{ tag }}
                  </span>
                </div>
                <div class="post-emotion" v-if="diary.emotion">
                  <span class="emotion-label">情绪：</span>
                  <span class="emotion-value">{{ diary.emotion }}</span>
              </div>
                <div class="post-actions" @click.stop>
                  <button class="action-btn edit" @click.stop="editDiary(diary)">
                    编辑
                  </button>
                  <button class="action-btn delete" @click.stop="deleteDiary(diary.diaryId)">
                    删除
                  </button>
                </div>
              </footer>
            </article>
          </div>
          <div v-else class="empty-state">
            <div class="empty-illustration">
              <div class="empty-icon">🌸</div>
              <div class="empty-decoration"></div>
            </div>
            <h3 class="empty-title">还没有日记呢</h3>
            <p class="empty-hint">写下第一篇日记，开始你的心灵之旅</p>
            <button class="empty-action-btn" @click="goEdit()">
              <span class="btn-icon"></span>
              开始记录
            </button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const uid = window.sessionStorage.getItem('uid')
const diaries = ref([])
const tags = ref([])
const selectedTag = ref(null)
const selectedDate = ref(null)

const showSearch = ref(false)
const showMyDiaries = ref(false)
const searchDate = ref('')
const searchKeyword = ref('')
const currentStatus = ref('all')
const filteredDiaries = ref([])

function resetFiltersExceptTab() {
  selectedTag.value = null;
  selectedDate.value = null;
}

function goEdit() { router.push('/user/diary/edit') }
function editDiary(diary) { router.push(`/user/diary/edit/${diary.diaryId}`) }
function viewDiary(diary) { router.push(`/user/diary/detail/${diary.diaryId}`) }

async function loadTags() {
  const res = await request.get('/api/tag')
  console.log('标签接口返回', res.data)
  tags.value = Array.isArray(res.data.data) ? res.data.data : []
  console.log('标签数据', tags.value)
}

async function loadDiaries() {
  if (!uid) {
    diaries.value = []
    return
  }
  
  try {
    const params = {}
    
    if (currentStatus.value === 'draft') {
      params.status = 'draft'
      params.targetUserId = uid
    } else if (currentStatus.value === 'published') {
      params.status = 'published'
    } else {
      // 全部：获取已发布的日记和当前用户的草稿
      const pubParams = { status: 'published' }
      console.log('[全部] 已发布请求参数:', pubParams)
      const pubRes = await request.get('/api/diary', { params: pubParams })
      console.log('[全部] 已发布返回:', pubRes.data)
      let allList = (pubRes.data.code === 200 && Array.isArray(pubRes.data.data)) ? pubRes.data.data : []
      
      const draftParams = { status: 'draft', targetUserId: uid }
      console.log('[全部] 草稿请求参数:', draftParams)
      const draftRes = await request.get('/api/diary', { params: draftParams })
      console.log('[全部] 草稿返回:', draftRes.data)
      let draftList = (draftRes.data.code === 200 && Array.isArray(draftRes.data.data)) ? draftRes.data.data : []
      
      // 合并并去重
      const allIds = new Set()
      const merged = []
      for (const d of allList.concat(draftList)) {
        if (!allIds.has(d.diaryId)) {
          allIds.add(d.diaryId)
          merged.push(d)
        }
      }
      console.log('[全部] 合并后:', merged)
      diaries.value = merged
      return
    }
    
    console.log('加载日记参数:', params)
    const res = await request.get('/api/diary', { params })
    
    if (res.data.code === 200) {
      diaries.value = Array.isArray(res.data.data) ? res.data.data : []
    } else {
      diaries.value = []
      console.error('加载日记失败:', res.data.message)
    }
  } catch (error) {
    console.error('加载日记失败:', error)
    diaries.value = []
  }
}

function onTabChange() {
  resetFiltersExceptTab();
  loadDiaries();
}

function onDateChange() {
  loadDiaries();
}

function onTagChange() {
  loadDiaries();
}

function formatDate(date) {
  if (!date) return '';
  return dayjs(date).format('YYYY-MM-DD HH:mm');
}

function isAuthor(diary) {
  console.log('检查作者权限:', { 
    diaryUserId: diary.userId, 
    diaryUserId2: diary.user_id, 
    diaryUserId3: diary.userId, 
    currentUid: uid,
    diaryData: diary
  })
  return diary.userId == uid || diary.user_id == uid || diary.userId == uid;
}

const EMOTION_TAGS = {
  '开心': '😊', 
  '难过': '😢', 
  '焦虑': '😰', 
  '兴奋': '🤩', 
  '平静': '😌',
  '愤怒': '😠',
  '惊喜': '😲',
  '疲惫': '😩',
  '困惑': '😕',
  '满足': '😌'
}

function getEmotionIcon(diary) {
  if (diary.tags && diary.tags.length > 0) {
    for (const tag of diary.tags) {
      if (EMOTION_TAGS[tag]) {
        return EMOTION_TAGS[tag];
      }
    }
  }
  return '😊';
}

function getEmotionText(diary) {
  if (diary.tags && diary.tags.length > 0) {
    for (const tag of diary.tags) {
      if (EMOTION_TAGS[tag]) {
        return tag;
      }
    }
  }
  return '平静';
}

// 搜索功能
async function performSearch() {
  try {
    const params = {
      status: currentStatus.value === 'all' ? undefined : currentStatus.value,
      targetUserId: uid
    }
    
    if (searchDate.value) {
      params.date = dayjs(searchDate.value).format('YYYY-MM-DD')
    }
    
    if (searchKeyword.value && searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }
    
    console.log('搜索参数:', params)
    const res = await request.get('/api/diary/search', { params })
    
    if (res.data.code === 200) {
      diaries.value = Array.isArray(res.data.data) ? res.data.data : []
      ElMessage.success('搜索完成')
    } else {
      ElMessage.error(res.data.message || '搜索失败')
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败')
  }
}

// 状态筛选
function filterByStatus(status) {
  currentStatus.value = status
  loadDiaries()
}

function getMediaList(diary) {
  return Array.isArray(diary.media) ? diary.media : [];
}

async function deleteDiary(diaryId) {
  try {
    await ElMessageBox.confirm('确定要删除这篇日记吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const res = await request.delete(`/api/diary/${diaryId}`);
    if (res.data.code === 200) {
      ElMessage.success('删除成功');
      loadDiaries();
    } else {
      ElMessage.error(res.data.message || '删除失败');
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('Error deleting diary:', e);
      ElMessage.error('删除失败');
    }
  }
}

onMounted(() => {
  loadTags();
  loadDiaries();
})
</script>

<style scoped>
/* 基础样式 */
.diary-bg.blog-layout {
  min-height: 100vh;
  background: #FEF5E7;
  padding: 40px 0;
  position: relative;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

/* 背景装饰元素 */
.bg-decorations {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.cloud {
  position: absolute;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  opacity: 0.6;
  animation: float 15s infinite linear;
}

.cloud.c1 {
  width: 120px;
  height: 60px;
  top: 15%;
  left: 10%;
  animation-delay: 0s;
}

.cloud.c2 {
  width: 180px;
  height: 90px;
  top: 25%;
  right: 15%;
  animation-delay: 3s;
}

.cloud.c3 {
  width: 150px;
  height: 75px;
  bottom: 20%;
  left: 20%;
  animation-delay: 6s;
}

.leaf {
  position: absolute;
  background-size: contain;
  background-repeat: no-repeat;
  opacity: 0.5;
  animation: float 12s infinite ease-in-out;
}

.leaf.l1 {
  width: 40px;
  height: 40px;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="%23a1c4fd"><path d="M17 8C8 10 5.9 16.8 3 20c1.9-1 8-2.1 13-5 5-3 9-8 7-12-2-4-6-3-6-3z"/></svg>');
  top: 10%;
  right: 10%;
  animation-delay: 1s;
}

.leaf.l2 {
  width: 50px;
  height: 50px;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="%23c2e9fb"><path d="M12 3c-4 3-6 8-6 12 0 1 .1 2 .3 3 .2-1.1.7-2.1 1.4-3 2-3 4.3-5 6.3-7 2-2 4-4 5-6-3-1-7 0-7 1z"/></svg>');
  bottom: 15%;
  right: 20%;
  animation-delay: 4s;
}

.leaf.l3 {
  width: 35px;
  height: 35px;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="%23b5ead7"><path d="M12 2c3 2 5 6 5 10 0 1-.1 2-.3 3-.2-1.1-.7-2.1-1.4-3-2-3-4.3-5-6.3-7-2-2-4-4-5-6 3-1 7 0 7 1z"/></svg>');
  top: 30%;
  left: 5%;
  animation-delay: 7s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  25% {
    transform: translateY(-20px) rotate(5deg);
  }
  50% {
    transform: translateY(0) rotate(0deg);
  }
  75% {
    transform: translateY(-10px) rotate(-5deg);
  }
}

.blog-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 治愈系功能按钮区域 */
.action-bar {
  margin-bottom: 30px;
}

.action-group {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
  margin-bottom: 40px;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 30px;
  border: none;
  border-radius: 18px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  min-width: 120px;
  position: relative;
  overflow: hidden;
}

.action-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(100%);
  transition: transform 0.3s ease;
}

.action-btn:hover::before {
  transform: translateY(0);
}

.action-btn.write {
  background: linear-gradient(135deg, #a1c4fd, #c2e9fb);
  color: #4a6fc7;
}

.action-btn.search {
  background: linear-gradient(135deg, #b5ead7, #c7f3d0);
  color: #4a9c6d;
}

.action-btn.list {
  background: linear-gradient(135deg, #ffd3b6, #ffaaa5);
  color: #d45d79;
}

.action-btn:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 25px rgba(0, 0, 0, 0.15);
}

.btn-icon {
  font-size: 28px;
  margin-bottom: 8px;
  transition: transform 0.3s ease;
}

.action-btn:hover .btn-icon {
  transform: scale(1.2);
}

/* 治愈系搜索区域 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.search-section {
  margin-bottom: 30px;
}

.search-container {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 20px;
  padding: 25px;
  box-shadow: 0 10px 30px rgba(161, 196, 253, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.search-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.search-icon {
  font-size: 24px;
  color: #8ba3d9;
}

.search-title {
  font-size: 20px;
  font-weight: 600;
  color: #5a7fdb;
  margin: 0;
}

.search-form {
  background: rgba(255, 255, 255, 0.7);
  border-radius: 15px;
  padding: 20px;
  border: 1px solid rgba(161, 196, 253, 0.3);
}

.form-row {
  display: flex;
  gap: 20px;
  align-items: end;
  flex-wrap: wrap;
}

.input-field {
  flex: 1;
  min-width: 200px;
}

.input-field label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #5a7fdb;
  font-size: 14px;
}

.healing-date-picker {
  width: 100%;
}

.healing-date-picker :deep(.el-input__inner) {
  border: 2px solid rgba(161, 196, 253, 0.5);
  border-radius: 12px;
  height: 44px;
  line-height: 44px;
  font-size: 14px;
  padding: 0 15px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.8);
}

.healing-date-picker :deep(.el-input__inner:hover) {
  border-color: #a1c4fd;
}

.healing-date-picker :deep(.el-input__inner:focus) {
  border-color: #5a7fdb;
  box-shadow: 0 0 0 2px rgba(90, 127, 219, 0.2);
}

.healing-input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid rgba(161, 196, 253, 0.5);
  border-radius: 12px;
  font-size: 14px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.8);
}

.healing-input:focus {
  outline: none;
  border-color: #5a7fdb;
  box-shadow: 0 0 0 2px rgba(90, 127, 219, 0.2);
}

.search-submit-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #a1c4fd, #5a7fdb);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 120px;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(90, 127, 219, 0.3);
}

.search-submit-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(90, 127, 219, 0.4);
}

.btn-arrow {
  font-size: 18px;
  transition: transform 0.3s ease;
}

.search-submit-btn:hover .btn-arrow {
  transform: translateX(5px);
}

/* 治愈系状态筛选 */
.filter-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 10px 30px rgba(161, 196, 253, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  flex-wrap: wrap;
}

.filter-label {
  font-weight: 600;
  color: #5a7fdb;
  font-size: 16px;
  white-space: nowrap;
}

.filter-buttons {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: 2px solid rgba(161, 196, 253, 0.5);
  border-radius: 25px;
  background: rgba(255, 255, 255, 0.7);
  color: #5a7fdb;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.filter-btn:hover {
  border-color: #5a7fdb;
  background: rgba(161, 196, 253, 0.1);
}

.filter-btn.active {
  background: linear-gradient(135deg, #a1c4fd, #5a7fdb);
  color: white;
  border-color: #5a7fdb;
  box-shadow: 0 4px 15px rgba(90, 127, 219, 0.3);
}

.filter-icon {
  font-size: 16px;
}

/* Blog风格日记列表 */
.diary-list.blog-style {
  margin-top: 40px;
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
  padding: 0 20px;
}

.blog-posts {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.blog-post {
  background: #fff;
  border-radius: 8px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid #e5e7eb;
}

.blog-post:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.post-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.post-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 12px 0;
  line-height: 1.4;
  transition: color 0.2s;
}

.blog-post:hover .post-title {
  color: #E67E73;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  color: #6b7280;
}

.post-date {
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-icon {
  font-size: 14px;
}

.post-status {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.post-status.draft {
  background: #fef3c7;
  color: #92400e;
}

.post-status.published {
  background: #d1fae5;
  color: #065f46;
}

.post-content {
  margin-bottom: 20px;
}

.post-excerpt {
  color: #4b5563;
  font-size: 15px;
  line-height: 1.7;
  margin: 0;
}

.post-footer {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.post-tags .tag {
  color: #6b7280;
  font-size: 13px;
  padding: 0;
  background: none;
  border: none;
  cursor: pointer;
  transition: color 0.2s;
}

.post-tags .tag:hover {
  color: #E67E73;
}

.post-emotion {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #6b7280;
}

.emotion-label {
  font-weight: 500;
}

.emotion-value {
  color: #E67E73;
  font-weight: 500;
}

.post-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.post-actions .action-btn {
  padding: 8px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
  color: #6b7280;
}

.post-actions .action-btn.edit {
  color: #E67E73;
  border-color: #E67E73;
}

.post-actions .action-btn.edit:hover {
  background: #E67E73;
  color: #fff;
}

.post-actions .action-btn.delete {
  color: #dc2626;
  border-color: #dc2626;
}

.post-actions .action-btn.delete:hover {
  background: #dc2626;
  color: #fff;
}

.empty-state {
  text-align: center;
  padding: 80px 40px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 25px;
  box-shadow: 0 10px 30px rgba(161, 196, 253, 0.2);
  border: 2px dashed rgba(161, 196, 253, 0.5);
  margin: 40px 0;
  backdrop-filter: blur(10px);
}

.empty-illustration {
  position: relative;
  margin-bottom: 30px;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
  color: #a1c4fd;
  text-shadow: 0 4px 10px rgba(161, 196, 253, 0.3);
}

.empty-decoration {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 120px;
  height: 120px;
  border: 3px dashed rgba(161, 196, 253, 0.3);
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.3;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.1);
    opacity: 0.6;
  }
}

.empty-title {
  font-size: 24px;
  font-weight: 600;
  color: #5a7fdb;
  margin: 0 0 15px 0;
}

.empty-hint {
  font-size: 16px;
  color: #8ba3d9;
  margin: 0 0 30px 0;
  opacity: 0.8;
}

.empty-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 15px 30px;
  background: linear-gradient(135deg, #a1c4fd, #5a7fdb);
  color: white;
  border: none;
  border-radius: 25px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 8px 20px rgba(90, 127, 219, 0.3);
}

.empty-action-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 25px rgba(90, 127, 219, 0.4);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .action-group {
    flex-direction: column;
    align-items: center;
  }
  
  .diary-grid {
    grid-template-columns: 1fr;
  }
  
  .form-row {
    flex-direction: column;
    gap: 15px;
  }
  
  .input-field {
    width: 100%;
  }
  
  .search-submit-btn {
    width: 100%;
  }
  
  .filter-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .filter-buttons {
    width: 100%;
    justify-content: space-between;
  }
  
  .filter-btn {
    flex: 1;
    text-align: center;
  }
}
</style>