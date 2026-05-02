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

export function createArticle(payload) {
  return request('/publicity/articles', {
    method: 'POST',
    body: payload,
  })
}

export function updateArticle(id, payload) {
  return request(`/publicity/articles/${id}`, {
    method: 'PUT',
    body: payload,
  })
}

export function deleteArticle(id) {
  return request(`/publicity/articles/${id}`, {
    method: 'DELETE',
  })
}

export function updateArticleStatus(id, status) {
  return request(`/publicity/articles/${id}/${encodeURIComponent(status)}`, {
    method: 'PATCH',
  })
}
