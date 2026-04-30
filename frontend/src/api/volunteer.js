import { request } from './http'

export function getRecruitments(query = {}) {
  return request('/volunteers/recruitments', {
    method: 'GET',
    query,
  })
}
