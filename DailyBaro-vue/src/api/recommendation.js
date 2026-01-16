import request from '@/utils/request'

export function getRecommendations() {
  return request.get('/app/recommendations')
}


