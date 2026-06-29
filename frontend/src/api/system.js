import { request } from './http'

export function deleteCachedFeatures() {
  return request('/ai/cached-features', { method: 'DELETE' })
}

export function getAllConfig() {
  return request('/config', { method: 'GET' })
}

export function getSysConfig(key) {
  return request(`/config/sys/${key}`, { method: 'GET' })
}

export function setSysConfig(key, value) {
  return request('/config/sys', { method: 'PUT', body: { key, value } })
}

export function batchSetConfig(configs) {
  return request('/config/batch', { method: 'POST', body: configs })
}

export function getUserAiConfig(userId) {
  return request(`/config/user/${userId}`, { method: 'GET' })
}

export function setUserAiConfig(userId, payload) {
  return request(`/config/user/${userId}`, { method: 'PUT', body: payload })
}
