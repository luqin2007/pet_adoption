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

export function likeArticle(id) {
  return request(`/publicity/articles/${id}/like`, {
    method: 'POST',
  })
}

export function unlikeArticle(id) {
  return request(`/publicity/articles/${id}/like`, {
    method: 'DELETE',
  })
}

export function favoriteArticle(id) {
  return request(`/publicity/articles/${id}/favorite`, {
    method: 'POST',
  })
}

export function unfavoriteArticle(id) {
  return request(`/publicity/articles/${id}/favorite`, {
    method: 'DELETE',
  })
}

export function getFavoriteArticles(query = {}) {
  return request('/publicity/favorites', {
    method: 'GET',
    query,
  })
}

export function shareArticle(id) {
  return request(`/publicity/articles/${id}/share`, {
    method: 'POST',
  })
}
