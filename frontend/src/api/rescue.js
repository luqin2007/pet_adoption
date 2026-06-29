import { request } from './http'

export function getRescueTasks(query = {}) {
  return request('/tasks', { method: 'GET', query })
}

export function getRescueTaskCount() {
  return request('/tasks/count', { method: 'GET' })
}

export function getRescueTask(id) {
  return request(`/tasks/${id}`, { method: 'GET' })
}

export function getRescueTaskMedia(id) {
  return request(`/tasks/${id}/media`, { method: 'GET' })
}

export function beginRescueTask() {
  return request('/tasks', { method: 'PUT' })
}

export function createRescueTask(payload) {
  return request('/tasks', { method: 'POST', body: payload })
}

export function updateRescueTask(id, payload) {
  return request(`/tasks/${id}`, { method: 'PUT', body: payload })
}

export function updateRescueTaskStatus(id, payload) {
  return request(`/tasks/${id}/status`, { method: 'PATCH', body: payload })
}

export function getRescueTaskRecords(id) {
  return request(`/tasks/${id}/status`, { method: 'GET' })
}

export function uploadRescueTaskMedia(id, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request(`/tasks/${id}/uploads`, { method: 'POST', body: formData })
}

export function deleteRescueTaskUpload(id, filename) {
  return request(`/tasks/${id}/uploads/${encodeURIComponent(filename)}`, { method: 'DELETE' })
}

export function deleteRescueTask(id) {
  return request(`/tasks/${id}`, { method: 'DELETE' })
}
