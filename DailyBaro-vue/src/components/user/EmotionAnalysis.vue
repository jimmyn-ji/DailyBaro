<template>
  <div class="analysis-bg">
    <div class="analysis-card" ref="analysisContentRef">
      <h2 class="analysis-title">情绪分析洞察</h2>
      <div class="analysis-desc">通过数据可视化探索你的情绪世界，发现情绪模式，获取个性化洞察</div>
      <div v-if="!uid" class="empty-state">请先登录</div>
      <template v-else>
        <!-- 时间范围选择区 -->
        <div class="time-select-bar">
          <label>时间范围：</label>
          <select v-model="selectedTimeRange" @change="onTimeRangeChange" class="mystic-select">
            <option v-for="range in timeRangeList" :key="range.id" :value="range.id">{{ range.name }}</option>
          </select>
        </div>
        <div v-if="!emotionData.fluctuation.length && !emotionData.distribution.length" class="empty-state">
          <div class="crystal-ball">
            <div class="inner-glow"></div>
          </div>
          <p>暂无情绪数据</p>
        </div>
        <div v-else class="charts-container">
          <!-- 左侧：情绪波动曲线图 -->
          <div class="chart-section card-hoverable mystic-card" :class="{ 'card-active': activeCard==='line' }" @mouseenter="activeCard='line'" @mouseleave="activeCard=''">
            <div class="chart-header">
              <span class="chart-title mystic-title">情绪波动趋势</span>
              <div class="chart-actions">
                <div class="download-dropdown">
                  <button class="chart-btn download-btn mystic-btn" @click="toggleLineDownloadMenu">
                    <i class="icon-download"></i> 下载 ▼
                  </button>
                  <div v-if="showLineDownloadMenu" class="download-menu mystic-menu">
                    <button class="download-option" @click="exportLineImage">
                      <i class="icon-image"></i> 图片
                    </button>
                    <button class="download-option" @click="exportLinePDF">
                      <i class="icon-pdf"></i> PDF
                    </button>
                  </div>
                </div>
                <button class="chart-btn mystic-btn" @click="showLineDialog=true">
                  <i class="icon-expand"></i> 放大
                </button>
              </div>
            </div>
            <div v-if="emotionData.fluctuation.length" id="line-chart" class="chart-box mystic-chart"></div>
            <div v-else class="empty-state">
              <div class="crystal-ball">
                <div class="inner-glow"></div>
              </div>
              <p>暂无情绪波动数据</p>
            </div>
          </div>
          <!-- 右侧：情绪分布饼图 -->
          <div class="chart-section card-hoverable mystic-card" :class="{ 'card-active': activeCard==='pie' }" @mouseenter="activeCard='pie'" @mouseleave="activeCard=''">
            <div class="chart-header">
              <span class="chart-title mystic-title">情绪分布比例</span>
              <div class="chart-actions">
                <div class="download-dropdown">
                  <button class="chart-btn download-btn mystic-btn" @click="togglePieDownloadMenu">
                    <i class="icon-download"></i> 下载 ▼
                  </button>
                  <div v-if="showPieDownloadMenu" class="download-menu mystic-menu">
                    <button class="download-option" @click="exportPieImage">
                      <i class="icon-image"></i> 图片
                    </button>
                    <button class="download-option" @click="exportPiePDF">
                      <i class="icon-pdf"></i> PDF
                    </button>
                  </div>
                </div>
                <button class="chart-btn mystic-btn" @click="showPieDialog=true">
                  <i class="icon-expand"></i> 放大
                </button>
              </div>
            </div>
            <div v-if="emotionData.distribution.length" id="pie-chart" class="chart-box mystic-chart"></div>
            <div v-else class="empty-state">
              <div class="crystal-ball">
                <div class="inner-glow"></div>
              </div>
              <p>暂无情绪分布数据</p>
            </div>
            <div class="dominant-emotion-bar mystic-emotion">
              <span>主导情绪：</span>
              <span v-if="dominantEmotion" :style="{
                color: getEmotionColor(dominantEmotion.emotion),
                fontWeight: 'bold',
                fontSize: '18px',
                textShadow: '0 1px 2px rgba(0,0,0,0.1)',
                padding: '4px 12px',
                borderRadius: '20px',
                backgroundColor: getEmotionColor(dominantEmotion.emotion) + '20',
                border: `1px solid ${getEmotionColor(dominantEmotion.emotion)}`
              }">
                <i :class="getEmotionIcon(dominantEmotion.emotion)"></i> {{ dominantEmotion.emotion }}
              </span>
              <span v-else>暂无数据</span>
              <div v-if="dominantEmotion" class="dominant-emotion-desc">
                <i class="icon-insight"></i> {{ getDominantEmotionInsight(dominantEmotion.emotion) }}
              </div>
            </div>
          </div>
        </div>
        <!-- AI分析四分区 -->
        <div class="ai-analysis-4">
          <div v-if="aiLoading" class="ai-loading mystic-loading">
            <div class="loading-spinner">
              <div class="spinner-orb"></div>
            </div>
            <div class="loading-text">正在获取AI分析...</div>
          </div>
          <div v-else-if="aiPoints.length" v-for="(item, idx) in aiPoints" :key="idx" class="ai-card card-hoverable mystic-ai-card" :class="{ 'card-active': activeCard===('ai'+idx) }" @mouseenter="activeCard='ai'+idx" @mouseleave="activeCard=''">
            <div class="ai-point-title">
              <i class="icon-ai"></i> AI分析{{ idx+1 }}
            </div>
            <div class="ai-point-content">
              <i class="icon-quote"></i> {{ item }}
            </div>
          </div>
          <div v-else class="empty-state">
            <div class="crystal-ball">
              <div class="inner-glow"></div>
            </div>
            <p>暂无AI分析数据</p>
          </div>
        </div>
        <!-- 放大弹窗 -->
        <div v-if="showLineDialog" class="dialog-mask mystic-mask" @click.self="showLineDialog=false">
          <div class="dialog-content mystic-dialog">
            <div id="line-chart-big" class="chart-box-big mystic-chart-big"></div>
          </div>
        </div>
        <div v-if="showPieDialog" class="dialog-mask mystic-mask" @click.self="showPieDialog=false">
          <div class="dialog-content mystic-dialog">
            <div id="pie-chart-big" class="chart-box-big mystic-chart-big"></div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'

// 时间范围选择相关
const timeRangeList = ref([
  { id: 'week', name: '最近一周' },
  { id: 'month', name: '最近一月' },
  { id: 'year', name: '最近一年' }
])
const selectedTimeRange = ref('week')
function onTimeRangeChange() {
  updateDateRange()
  loadCharts()
}

const aiPoints = ref([])
const aiLoading = ref(false)
const startDate = ref('')
const endDate = ref('')

// 更新日期范围
function updateDateRange() {
  const now = new Date()
  const end = now.toISOString().slice(0, 10)

  switch (selectedTimeRange.value) {
    case 'week':
      const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
      startDate.value = weekAgo.toISOString().slice(0, 10)
      break
    case 'month':
      const monthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
      startDate.value = monthAgo.toISOString().slice(0, 10)
      break
    case 'year':
      const yearAgo = new Date(now.getTime() - 365 * 24 * 60 * 60 * 1000)
      startDate.value = yearAgo.toISOString().slice(0, 10)
      break
    default:
      const defaultWeekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
      startDate.value = defaultWeekAgo.toISOString().slice(0, 10)
  }

  endDate.value = end
}
const lineChart = ref(null)
const pieChart = ref(null)
const analysisContentRef = ref(null)
const emotionData = ref({ fluctuation: [], distribution: [] })
const aiCache = ref(new Map()) // 缓存AI分析结果
const uid = window.sessionStorage.getItem('uid')
const showLineDialog = ref(false)
const showPieDialog = ref(false)
const showLineDownloadMenu = ref(false)
const showPieDownloadMenu = ref(false)
const activeCard = ref('')

function formatDate(ts) {
  const d = new Date(ts)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

async function loadCharts() {
  if (!uid) {
    emotionData.value = { fluctuation: [], distribution: [] }
    return
  }
  const userId = uid
  const params = { userId, startDate: startDate.value, endDate: endDate.value }

  try {
    // 多情绪曲线和分布
    const [res1, res2] = await Promise.all([
      request.get('/api/analysis/fluctuation-multi', { params }),
      request.get('/api/analysis/distribution', { params })
    ])
    console.log('fluctuation-multi返回', res1.data)
    console.log('distribution返回', res2.data)
    const data1 = res1.data.data || []
    const data2 = res2.data.data || []
    emotionData.value = { fluctuation: data1, distribution: data2 }

    // 等待DOM更新后再渲染图表
    await nextTick()
    renderCharts(data1, data2)

    setTimeout(() => {
      loadAIPoints()
    }, 500)
  } catch (error) {
    console.error('加载图表数据失败:', error)
  }
}

function renderCharts(fluctuationData, distributionData) {
  console.log('渲染charts', fluctuationData, distributionData)

  // 多情绪曲线
  const emotions = ['开心','激动','平静','无聊','疲惫','难过','焦虑','愤怒']
  const xData = fluctuationData.map(d => formatDate(d.date))
  const series = emotions.map(emotion => ({
    name: emotion,
    type: 'line',
    smooth: true,
    data: fluctuationData.map(d => d[emotion] || 0),
    symbol: 'circle',
    symbolSize: 10,
    lineStyle: { width: 3 },
    itemStyle: { color: getEmotionColor(emotion) },
    emphasis: { focus: 'series' }
  }))

  // 渲染折线图
  const lineChartDom = document.getElementById('line-chart')
  if (lineChartDom) {
    if (lineChart.value) { lineChart.value.dispose(); }
    lineChart.value = echarts.init(lineChartDom)
    lineChart.value.setOption({
      title: { text: '情绪波动趋势', left: 'center', textStyle: { fontSize: 16, fontWeight: 'bold' } },
      tooltip: { trigger: 'axis' },
      legend: { data: emotions, top: 30 },
      xAxis: { type: 'category', data: xData, axisLabel: { rotate: 45, fontWeight: 'bold' } },
      yAxis: { type: 'value', min: 0, axisLabel: { fontWeight: 'bold' } },
      series
    })
  }

  // 渲染饼图
  const pieChartDom = document.getElementById('pie-chart')
  if (pieChartDom) {
    if (pieChart.value) { pieChart.value.dispose(); }
    pieChart.value = echarts.init(pieChartDom)
    pieChart.value.setOption({
      title: { text: '情绪分布统计', left: 'center', textStyle: { fontSize: 16, fontWeight: 'bold' } },
      tooltip: {
        trigger: 'item',
        formatter: function(params) {
          return `${params.seriesName}<br/>${params.name}: ${params.value} (${params.percent}%)`
        },
        backgroundColor: 'rgba(255,255,255,0.9)',
        borderColor: '#ccc',
        borderWidth: 1,
        textStyle: { color: '#333' }
      },
      series: [{
        name: '情绪分布',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        data: (distributionData || []).map(d => ({
          name: d.emotion,
          value: d.percentage,
          itemStyle: { color: getEmotionColor(d.emotion) }
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 15,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.6)',
            borderWidth: 2,
            borderColor: '#fff'
          }
        },
        label: {
          show: false // 不显示标签，只在悬停时显示
        }
      }]
    })
  }
}

function renderLineChart(domId, isBig) {
  const dom = document.getElementById(domId)
  if (!dom) {
    console.warn(`DOM元素 ${domId} 不存在`)
    return
  }
  const emotions = ['开心','激动','平静','无聊','疲惫','难过','焦虑','愤怒']
  const fluctuationData = emotionData.value.fluctuation || []
  const xData = fluctuationData.map(d => formatDate(d.date))
  const series = emotions.map(emotion => ({
    name: emotion,
    type: 'line',
    smooth: true,
    data: fluctuationData.map(d => d[emotion] || 0),
    symbol: 'circle',
    symbolSize: 10,
    lineStyle: { width: 3 },
    itemStyle: { color: getEmotionColor(emotion) },
    emphasis: { focus: 'series' }
  }))
  const chart = echarts.init(dom)
  chart.setOption({
    title: { text: '情绪波动趋势', left: 'center', textStyle: { fontSize: 16, fontWeight: 'bold' } },
    tooltip: { trigger: 'axis' },
    legend: { data: emotions, top: 30 },
    xAxis: { type: 'category', data: xData, axisLabel: { rotate: 45, fontWeight: 'bold' } },
    yAxis: { type: 'value', min: 0, axisLabel: { fontWeight: 'bold' } },
    series
  })
}
function renderPieChart(domId, isBig) {
  const dom = document.getElementById(domId)
  if (!dom) {
    console.warn(`DOM元素 ${domId} 不存在`)
    return
  }
  const distributionData = emotionData.value.distribution || []
  const chart = echarts.init(dom)
  chart.setOption({
    title: { text: '情绪分布统计', left: 'center', textStyle: { fontSize: 16, fontWeight: 'bold' } },
    tooltip: {
      trigger: 'item',
      formatter: function(params) {
        return `${params.seriesName}<br/>${params.name}: ${params.value} (${params.percent}%)`
      },
      backgroundColor: 'rgba(255,255,255,0.9)',
      borderColor: '#ccc',
      borderWidth: 1,
      textStyle: { color: '#333' }
    },
    series: [{
      name: '情绪分布',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['60%', '50%'],
      data: (distributionData || []).map(d => ({
        name: d.emotion,
        value: d.percentage,
        itemStyle: { color: getEmotionColor(d.emotion) }
      })),
      emphasis: {
        itemStyle: {
          shadowBlur: 15,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.6)',
          borderWidth: 2,
          borderColor: '#fff'
        }
      },
      label: {
        show: false // 不显示标签，只在悬停时显示
      }
    }]
  })
}

function getEmotionColor(emotion) {
  const colorMap = {
    '开心': '#4ade80', '激动': '#fbbf24', '平静': '#60a5fa', '无聊': '#9ca3af',
    '疲惫': '#f59e0b', '难过': '#8b5cf6', '焦虑': '#ef4444', '愤怒': '#dc2626'
  }
  return colorMap[emotion] || '#9ca3af'
}

function getEmotionIcon(emotion) {
  const iconMap = {
    '开心': 'icon-happy',
    '激动': 'icon-excited',
    '平静': 'icon-calm',
    '无聊': 'icon-bored',
    '疲惫': 'icon-tired',
    '难过': 'icon-sad',
    '焦虑': 'icon-anxious',
    '愤怒': 'icon-angry'
  }
  return iconMap[emotion] || 'icon-emotion'
}

function getDominantEmotionInsight(emotion) {
  const insights = {
    '开心': '你在这段时间保持了非常积极的情绪状态，继续保持这种正能量！',
    '激动': '你的情绪充满活力，这段时间有很多让你兴奋的事情发生。',
    '平静': '你的情绪非常稳定和平和，这是很好的心理状态。',
    '无聊': '你的情绪略显平淡，也许需要寻找一些新的兴趣点。',
    '疲惫': '你似乎有些疲惫，记得给自己足够的休息时间。',
    '难过': '你经历了一些低落的情绪，这是正常的，记得寻求支持。',
    '焦虑': '你有些焦虑情绪，尝试一些放松技巧可能会有帮助。',
    '愤怒': '你有一些愤怒情绪，找到健康的发泄方式很重要。'
  }
  return insights[emotion] || '你在这段时间保持了较好的情绪稳定性'
}

async function loadAIPoints() {
  aiLoading.value = true
  try {
    const res = await request.post('/api/analysis/ai/analysis-points', emotionData.value)
    console.log('AI分析返回', res.data)
    if (res.data.code === 200 && Array.isArray(res.data.data)) {
      aiPoints.value = res.data.data
    } else if (res.data.code !== 200 && res.data.message && res.data.message.includes('正在获取')) {
      // 如果是"正在获取"状态，显示加载提示
      aiPoints.value = ['正在获取AI分析，请稍后重试...']
      // 3秒后重试
      setTimeout(() => {
        loadAIPoints()
      }, 3000)
    } else {
      aiPoints.value = ['AI分析暂时不可用，请稍后重试。']
    }
  } catch (e) {
    console.error('AI分析请求失败:', e)
    aiPoints.value = ['AI分析失败，请检查网络连接后重试。']
  } finally {
    aiLoading.value = false
  }
}

async function exportPDF() {
  if (!analysisContentRef.value) return

  try {
    const canvas = await html2canvas(analysisContentRef.value, {
      scale: 2,
      useCORS: true,
      allowTaint: true
    })

    const imgData = canvas.toDataURL('image/png')
    const pdf = new jsPDF('p', 'mm', 'a4')
    const imgWidth = 210
    const pageHeight = 295
    const imgHeight = (canvas.height * imgWidth) / canvas.width
    let heightLeft = imgHeight

    let position = 0

    pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
    heightLeft -= pageHeight

    while (heightLeft >= 0) {
      position = heightLeft - imgHeight
      pdf.addPage()
      pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
      heightLeft -= pageHeight
    }

    pdf.save('情绪分析报告.pdf')
  } catch (error) {
    console.error('导出PDF失败:', error)
    alert('导出失败，请重试')
  }
}

async function exportImage() {
  if (!analysisContentRef.value) return

  try {
    const canvas = await html2canvas(analysisContentRef.value, {
      scale: 2,
      useCORS: true,
      allowTaint: true
    })

    const link = document.createElement('a')
    link.download = '情绪分析报告.png'
    link.href = canvas.toDataURL()
    link.click()
  } catch (error) {
    console.error('导出图片失败:', error)
    alert('导出失败，请重试')
  }
}

const dominantEmotion = computed(() => {
  if (!emotionData.value.distribution || emotionData.value.distribution.length === 0) return null
  return emotionData.value.distribution.reduce((max, cur) => (cur.percentage > max.percentage ? cur : max), emotionData.value.distribution[0])
})

// 下载菜单切换函数
function toggleLineDownloadMenu() {
  showLineDownloadMenu.value = !showLineDownloadMenu.value
  showPieDownloadMenu.value = false
}

function togglePieDownloadMenu() {
  showPieDownloadMenu.value = !showPieDownloadMenu.value
  showLineDownloadMenu.value = false
}

// 点击外部关闭下载菜单
function closeDownloadMenus() {
  showLineDownloadMenu.value = false
  showPieDownloadMenu.value = false
}

// 导出图片
async function exportLineImage() {
  const el = document.getElementById('line-chart')
  if (!el) return
  const canvas = await html2canvas(el, { scale: 2, useCORS: true, allowTaint: true })
  const link = document.createElement('a')
  link.download = '情绪波动趋势.png'
  link.href = canvas.toDataURL()
  link.click()
  closeDownloadMenus()
}

async function exportPieImage() {
  const el = document.getElementById('pie-chart')
  if (!el) return
  const canvas = await html2canvas(el, { scale: 2, useCORS: true, allowTaint: true })
  const link = document.createElement('a')
  link.download = '情绪分布比例.png'
  link.href = canvas.toDataURL()
  link.click()
  closeDownloadMenus()
}

// 导出PDF
async function exportLinePDF() {
  const el = document.getElementById('line-chart')
  if (!el) return

  try {
    const canvas = await html2canvas(el, { scale: 2, useCORS: true, allowTaint: true })
    const imgData = canvas.toDataURL('image/png')
    const pdf = new jsPDF('p', 'mm', 'a4')
    const imgWidth = 180
    const imgHeight = (canvas.height * imgWidth) / canvas.width

    // 居中显示
    const x = (210 - imgWidth) / 2
    const y = (297 - imgHeight) / 2

    pdf.addImage(imgData, 'PNG', x, y, imgWidth, imgHeight)
    pdf.save('情绪波动趋势.pdf')
    closeDownloadMenus()
  } catch (error) {
    console.error('导出PDF失败:', error)
    alert('导出失败，请重试')
  }
}

async function exportPiePDF() {
  const el = document.getElementById('pie-chart')
  if (!el) return

  try {
    const canvas = await html2canvas(el, { scale: 2, useCORS: true, allowTaint: true })
    const imgData = canvas.toDataURL('image/png')
    const pdf = new jsPDF('p', 'mm', 'a4')
    const imgWidth = 180
    const imgHeight = (canvas.height * imgWidth) / canvas.width

    // 居中显示
    const x = (210 - imgWidth) / 2
    const y = (297 - imgHeight) / 2

    pdf.addImage(imgData, 'PNG', x, y, imgWidth, imgHeight)
    pdf.save('情绪分布比例.pdf')
    closeDownloadMenus()
  } catch (error) {
    console.error('导出PDF失败:', error)
    alert('导出失败，请重试')
  }
}

// 放大弹窗渲染大图
watch(showLineDialog, async (v) => {
  if (v) {
    await nextTick()
    renderLineChart('line-chart-big', true)
  }
})
watch(showPieDialog, async (v) => {
  if (v) {
    await nextTick()
    renderPieChart('pie-chart-big', true)
  }
})

onMounted(() => {
  // 初始化日期范围
  updateDateRange()
  loadCharts()

  // 监听窗口大小变化，重新调整图表
  window.addEventListener('resize', () => {
    if (lineChart.value) lineChart.value.resize()
    if (pieChart.value) pieChart.value.resize()
  })

  // 监听点击外部关闭下载菜单
  document.addEventListener('click', (e) => {
    if (!e.target.closest('.download-dropdown')) {
      closeDownloadMenus()
    }
  })
})

watch([startDate, endDate, selectedTimeRange], () => {
  loadCharts()
})
</script>
<style scoped>
/* 基础样式 - 淡蓝色主题 */
.analysis-bg {
  min-height: 100vh;
  background: transparent;
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.analysis-card {
  background: transparent;
  border-radius: 16px;
  padding: 30px;
  width: 90%;
  max-width: 1200px;
}

/* 标题样式 */
.analysis-title {
  font-size: 28px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 12px;
  color: #ffffff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.analysis-desc {
  text-align: center;
  color: rgba(255, 255, 255, 0.9);
  font-size: 16px;
  margin-bottom: 25px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

/* 时间选择器 */
.time-select-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 25px;
}

.time-select-bar label {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.mystic-select {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 8px;
  padding: 8px 15px;
  color: #1e293b;
  font-size: 14px;
  transition: all 0.3s ease;
}

.mystic-select:hover {
  border-color: #93c5fd;
}

/* 图表容器 */
.charts-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 30px;
}

/* 卡片样式 */
.mystic-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 20px;
  transition: all 0.3s ease;
}

.card-hoverable:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  border-color: #93c5fd;
}

/* 图表头部 */
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.mystic-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e3a8a;
  display: flex;
  align-items: center;
  gap: 8px;
  text-shadow: none;
}

/* 按钮样式 */
.mystic-btn {
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 13px;
  color: #334155;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
}

.mystic-btn:hover {
  background: #e2e8f0;
}

/* 图表框 */
.mystic-chart {
  width: 100%;
  height: 280px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.95);
}

/* 主导情绪显示 */
.dominant-emotion {
  margin-top: 20px;
  padding: 12px;
  border-radius: 8px;
  background: #f8fafc;
  text-align: center;
  border: 1px dashed #cbd5e1;
}

/* AI分析卡片 */
.ai-analysis-4 {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.mystic-ai-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.ai-point-title {
  font-weight: 600;
  color: #1d4ed8;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.ai-point-content {
  color: #475569;
  line-height: 1.6;
  font-size: 14px;
}

/* 情绪颜色定义 */
.emotion-positive { color: #3b82f6 !important; } /* 蓝色 */
.emotion-excited { color: #f59e0b !important; } /* 橙色 */
.emotion-calm { color: #10b981 !important; } /* 绿色 */
.emotion-neutral { color: #64748b !important; } /* 灰色 */
.emotion-negative { color: #ef4444 !important; } /* 红色 */

/* 加载状态 */
.mystic-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
  grid-column: 1 / -1;
}

.mystic-loading .loading-text {
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  margin-top: 15px;
  font-size: 16px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #dbeafe;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  grid-column: 1 / -1;
}

.empty-state p {
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

/* 弹窗 */
.mystic-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.mystic-dialog {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 24px;
  width: 80%;
  max-width: 800px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
}

/* 动画 */
@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .ai-analysis-4 {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .charts-container,
  .ai-analysis-4 {
    grid-template-columns: 1fr;
  }

  .mystic-dialog {
    width: 95%;
    padding: 16px;
  }
}

/* 图标样式 - Font Awesome */
.icon-happy::before { content: "\f118"; font-family: "Font Awesome 5 Free"; color: #3b82f6; }
.icon-excited::before { content: "\f59a"; font-family: "Font Awesome 5 Free"; color: #f59e0b; }
.icon-calm::before { content: "\f75f"; font-family: "Font Awesome 5 Free"; color: #10b981; }
.icon-bored::before { content: "\f11a"; font-family: "Font Awesome 5 Free"; color: #64748b; }
.icon-tired::before { content: "\f5c2"; font-family: "Font Awesome 5 Free"; color: #64748b; }
.icon-sad::before { content: "\f119"; font-family: "Font Awesome 5 Free"; color: #ef4444; }
.icon-anxious::before { content: "\f5f4"; font-family: "Font Awesome 5 Free"; color: #ef4444; }
.icon-angry::before { content: "\f556"; font-family: "Font Awesome 5 Free"; color: #ef4444; }
.icon-download::before { content: "\f019"; font-family: "Font Awesome 5 Free"; font-weight: 900; color: #3b82f6; }
.icon-image::before { content: "\f03e"; font-family: "Font Awesome 5 Free"; color: #3b82f6; }
.icon-pdf::before { content: "\f1c1"; font-family: "Font Awesome 5 Free"; color: #3b82f6; }
.icon-expand::before { content: "\f065"; font-family: "Font Awesome 5 Free"; color: #3b82f6; }
.icon-insight::before { content: "\f06e"; font-family: "Font Awesome 5 Free"; color: #3b82f6; }
.icon-ai::before { content: "\f544"; font-family: "Font Awesome 5 Free"; color: #3b82f6; }
.icon-quote::before { content: "\f10e"; font-family: "Font Awesome 5 Free"; color: #3b82f6; }
</style>
