import request from '@/utils/request'

export function analyzeAudioEmotion(formData) {
  // formData 应包含键名 'audio'
  return request.post('/api/nlp/emotion/analyze/audio', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function analyzeImageEmotion(formData) {
  // formData 应包含键名 'image'
  return request.post('/api/nlp/emotion/analyze/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 获取基于日记附件可用的分析入口
export function getAnalysisOptions(diaryId) {
  return request.get('/api/emotion/analysis/options', {
    params: { diaryId }
  })
}

// 基于日记附件直接触发分析（无需重新上传）
export function analyzeFromDiary(diaryId, type) {
  // app-service 网关使用 @RequestBody，期望 JSON body
  // 网关会转发到 emotion-service，emotion-service 使用 @RequestParam
  // 但网关的 forwardPost 会处理转换
  return request.post('/api/emotion/analysis/from-diary', {
    diaryId: diaryId,
    type: type
  })
}


