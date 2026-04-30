import { request } from './http'

export function getArticles(query = {}) {
  return request('/publicity/articles', {
    method: 'GET',
    query,
  })
}

export function getArticleById(id) {
  return request(`/publicity/articles/${id}`, {
    method: 'GET',
  })
}
