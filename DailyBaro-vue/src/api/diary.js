import { diaryRequest } from '@/utils/request'

/**
 * 获取日记列表
 * @param {Object} params - 查询参数
 * @param {string} params.status - 日记状态 (published/draft)
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 * @param {string} params.keyword - 搜索关键词
 */
export function getDiaryList(params) {
  return diaryRequest({
    url: '/api/diary',
    method: 'get',
    params
  })
}

/**
 * 搜索日记
 * @param {Object} params - 搜索参数
 */
export function searchDiaries(params) {
  return diaryRequest({
    url: '/api/diary/search',
    method: 'get',
    params
  })
}

/**
 * 获取日记详情
 * @param {number|string} id - 日记ID
 */
export function getDiaryDetail(id) {
  return diaryRequest({
    url: `/api/diary/${id}`,
    method: 'get'
  })
}

/**
 * 创建日记
 * @param {FormData} formData - 日记数据（包含文件和文本）
 */
export function createDiary(formData) {
  return diaryRequest({
    url: '/api/diary',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 更新日记
 * @param {number|string} id - 日记ID
 * @param {FormData} formData - 更新的日记数据
 */
export function updateDiary(id, formData) {
  return diaryRequest({
    url: `/api/diary/${id}`,
    method: 'put',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除日记
 * @param {number|string} id - 日记ID
 */
export function deleteDiary(id) {
  return diaryRequest({
    url: `/api/diary/${id}`,
    method: 'delete'
  })
}

/**
 * 删除日记媒体文件
 * @param {number|string} mediaId - 媒体文件ID
 */
export function deleteDiaryMedia(mediaId) {
  return diaryRequest({
    url: `/api/diary/media/${mediaId}`,
    method: 'delete'
  })
}

/**
 * 获取用户标签列表
 */
export function getUserTags() {
  return diaryRequest({
    url: '/api/diary/tags',
    method: 'get'
  })
} 