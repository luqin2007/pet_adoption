import { request } from './http'

export function getRescueTasks(query = {}) {
  return request('/tasks', {
    method: 'GET',
    query,
  })
}

export function getRescueTask(id) {
  return request(`/tasks/${id}`, {
    method: 'GET',
  })
}

export function getRescueTaskMedia(id) {
  return request(`/tasks/${id}/media`, {
    method: 'GET',
  })
}

export function beginRescueTask() {
  return request('/tasks', {
    method: 'PUT',
  })
}

export function createRescueTask(payload) {
  return request('/tasks', {
    method: 'POST',
    body: payload,
  })
}

export function updateRescueTask(id, payload) {
  return request(`/tasks/${id}`, {
    method: 'PUT',
    body: payload,
  })
}

export function updateRescueTaskStatus(id, payload) {
  return request(`/tasks/${id}/status`, {
    method: 'PATCH',
    body: payload,
  })
}

export function uploadRescueTaskMedia(id, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request(`/tasks/${id}/uploads`, {
    method: 'POST',
    body: formData,
  })
}

export function deleteRescueTaskUpload(id, filename) {
  return request(`/tasks/${id}/uploads/${encodeURIComponent(filename)}`, {
    method: 'DELETE',
  })
}

export function deleteRescueTask(id) {
  return request(`/tasks/${id}`, {
    method: 'DELETE',
  })
}

export function getAdoptApplications(query = {}) {
  return request('/adopt/adopt', {
    method: 'GET',
    query,
  })
}

export function createAdoptApplication(payload) {
  return request('/adopt/adopt', {
    method: 'POST',
    body: payload,
  })
}

export function getBreadingApplications(query = {}) {
  return request('/adopt/breading', {
    method: 'GET',
    query,
  })
}

export function getMedicalRecords(query = {}) {
  return request('/medical/record', {
    method: 'GET',
    query,
  })
}

export function getFirstVisitRegistrations(query = {}) {
  return request('/medical/first', {
    method: 'GET',
    query,
  })
}

export function getFirstVisitRegistration(id) {
  return request(`/medical/first/${id}`, {
    method: 'GET',
  })
}

export function createFirstVisitRegistration(payload) {
  return request('/medical/first', {
    method: 'POST',
    body: payload,
  })
}

export function getItems(query = {}) {
  return request('/items/items', {
    method: 'GET',
    query,
  })
}

export function getDonations(query = {}) {
  return request('/items/donations', {
    method: 'GET',
    query,
  })
}

export function beginDonation() {
  return request('/items/donations', {
    method: 'PUT',
  })
}

export function createDonation(payload) {
  return request('/items/donations', {
    method: 'POST',
    body: payload,
  })
}

export function getCategories(query = {}) {
  return request('/items/categories', {
    method: 'GET',
    query,
  })
}

export function createCategory(payload) {
  return request('/items/categories', {
    method: 'POST',
    body: payload,
  })
}

export function createBreadingApplication(payload) {
  return request('/adopt/breading', {
    method: 'POST',
    body: payload,
  })
}
