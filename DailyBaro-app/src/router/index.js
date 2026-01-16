import { createRouter, createWebHashHistory } from 'vue-router'
import Login from '@/components/Login.vue'
import Register from '@/components/Register.vue'
import Error from '@/components/utils/404.vue'
import Forbidden from '@/components/utils/403.vue'
// user
import UserHome from '@/components/user/UserHome.vue'
import Chat from '@/components/user/menu/Chat.vue'
import Diary from '@/components/user/Diary.vue'
import DiaryEdit from '@/components/user/DiaryEdit.vue'
import DiaryDetail from '@/components/user/DiaryDetail.vue'
import EmotionAnalysis from '@/components/user/EmotionAnalysis.vue'
import MysteryBox from '@/components/user/MysteryBox.vue'
import Capsule from '@/components/user/Capsule.vue'
import DailyQuote from '@/components/user/DailyQuotePage.vue'
import AnonymousPlanet from '@/components/user/AnonymousPlanet.vue'
import AnonymousPostDetail from '@/components/user/AnonymousPostDetail.vue'
import UpdateInfo from '@/components/user/UpdateInfo.vue'
import BackgroundTest from '@/views/BackgroundTest.vue'

const routes = [
  {
    path: '/',
    redirect: 'login',
  },
  {
    path: '/404',
    meta: {title: "页面走丢了"},
    component: Error
  },
  {
    path: '/403',
    meta: {title: "没有权限"},
    component: Forbidden
  },
  {
    path: '/login',
    name: 'Login',
    meta: {title: "登录"},
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/background-test',
    name: 'BackgroundTest',
    meta: {title: "背景测试"},
    component: BackgroundTest
  },
  // 用户页面
  {
    path: '/user',
    component: UserHome,
    redirect: '/user/diary',
    children: [
      { path: 'diary', component: Diary, meta: {title: '情绪日记本'} },
      { path: 'diary/edit/:id?', component: DiaryEdit, meta: {title: '编辑日记'} },
      { path: 'diary/detail/:id', component: DiaryDetail, meta: {title: '日记详情'} },
      { path: 'analysis', component: EmotionAnalysis, meta: {title: '情绪分析'} },
      { path: 'mysterybox', component: MysteryBox, meta: {title: '情绪盲盒'} },
      { path: 'capsule', component: Capsule, meta: {title: '情绪胶囊'} },
      { path: 'quote/create', component: () => import('@/components/user/QuoteCreate.vue'), meta: {title: '创建日签'} },
      { path: 'quote/history', component: () => import('@/components/user/QuoteHistory.vue'), meta: {title: '日签历史'} },
      { path: 'quote/refresh', component: () => import('@/components/user/QuoteCreate.vue'), meta: {title: '换一换日签'} },
      { path: 'planet', component: AnonymousPlanet, meta: {title: '匿名星球'} },
      { path: 'planet/detail/:id', component: AnonymousPostDetail, meta: {title: '动态详情'} },
      { path: 'chat', meta: {title: '对话'}, component: Chat },
      { path: 'update_info', component: UpdateInfo, meta: {title: '个人信息'} }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
