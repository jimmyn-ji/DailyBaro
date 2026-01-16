<template>
  <div class="knowledge-manage-page">
    <div class="page-header">
      <h2>知识库管理</h2>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon>
        新增知识
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filter-section">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索标题或内容..."
        class="search-input"
        clearable
        @keyup.enter="loadKnowledgeList"
      >
        <template #append>
          <el-button @click="loadKnowledgeList">搜索</el-button>
        </template>
      </el-input>
      
      <el-select v-model="filterCategory" placeholder="分类筛选" clearable @change="loadKnowledgeList">
        <el-option label="全部" value="" />
        <el-option label="情绪管理" value="情绪管理" />
        <el-option label="心理排忧" value="心理排忧" />
        <el-option label="成长指南" value="成长指南" />
        <el-option label="案例分享" value="案例分享" />
      </el-select>

      <el-select v-model="filterStatus" placeholder="状态筛选" clearable @change="loadKnowledgeList">
        <el-option label="全部" value="" />
        <el-option label="草稿" :value="0" />
        <el-option label="已发布" :value="1" />
        <el-option label="已审核" :value="2" />
      </el-select>
    </div>

    <!-- 知识列表 -->
    <el-table :data="knowledgeList" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="subcategory" label="子分类" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览次数" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="editKnowledge(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteKnowledge(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-section">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadKnowledgeList"
        @current-change="loadKnowledgeList"
      />
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="10"
            placeholder="请输入内容"
          />
        </el-form-item>
        
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类">
            <el-option label="情绪管理" value="情绪管理" />
            <el-option label="心理排忧" value="心理排忧" />
            <el-option label="成长指南" value="成长指南" />
            <el-option label="案例分享" value="案例分享" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="子分类" prop="subcategory">
          <el-input v-model="form.subcategory" placeholder="请输入子分类（如：焦虑、抑郁等）" />
        </el-form-item>
        
        <el-form-item label="标签" prop="tags">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">草稿</el-radio>
            <el-radio :label="1">已发布</el-radio>
            <el-radio :label="2">已审核</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import dayjs from 'dayjs'

const loading = ref(false)
const knowledgeList = ref([])
const searchKeyword = ref('')
const filterCategory = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const dialogVisible = ref(false)
const dialogTitle = ref('新增知识')
const submitting = ref(false)
const formRef = ref(null)
const form = ref({
  id: null,
  title: '',
  content: '',
  category: '',
  subcategory: '',
  tags: '',
  status: 1
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const loadKnowledgeList = async () => {
  loading.value = true
  try {
    // 这里需要根据实际API调整
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterCategory.value) params.category = filterCategory.value
    if (filterStatus.value !== '') params.status = filterStatus.value

    // 注意：这里需要后端提供列表查询接口
    const response = await request.get('/api/knowledge/list', { params })
    
    if (response.data.code === 200) {
      knowledgeList.value = response.data.data.list || []
      total.value = response.data.data.total || 0
    }
  } catch (error) {
    console.error('加载知识列表失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  dialogTitle.value = '新增知识'
  resetForm()
  dialogVisible.value = true
}

const editKnowledge = (row) => {
  dialogTitle.value = '编辑知识'
  form.value = {
    id: row.id,
    title: row.title,
    content: row.content,
    category: row.category,
    subcategory: row.subcategory,
    tags: row.tags,
    status: row.status
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (form.value.id) {
        // 更新
        const response = await request.put(`/api/knowledge/${form.value.id}`, form.value)
        if (response.data.code === 200) {
          ElMessage.success('更新成功')
          dialogVisible.value = false
          loadKnowledgeList()
        }
      } else {
        // 创建
        const response = await request.post('/api/knowledge/create', form.value)
        if (response.data.code === 200) {
          ElMessage.success('创建成功')
          dialogVisible.value = false
          loadKnowledgeList()
        }
      }
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error('操作失败')
    } finally {
      submitting.value = false
    }
  })
}

const deleteKnowledge = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条知识吗？', '提示', {
      type: 'warning'
    })

    const response = await request.delete(`/api/knowledge/${id}`)
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      loadKnowledgeList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const resetForm = () => {
  form.value = {
    id: null,
    title: '',
    content: '',
    category: '',
    subcategory: '',
    tags: '',
    status: 1
  }
  formRef.value?.resetFields()
}

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'warning' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '草稿', 1: '已发布', 2: '已审核' }
  return texts[status] || '未知'
}

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadKnowledgeList()
})
</script>

<style scoped>
.knowledge-manage-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #1f2937;
}

.filter-section {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
  max-width: 400px;
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
