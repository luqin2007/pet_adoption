import { request } from './http'

export function getItems(query = {}) {
  return request('/items/items', { method: 'GET', query })
}

export function createItem(payload) {
  return request('/items/items', { method: 'POST', body: payload })
}

export function updateItem(id, payload) {
  return request(`/items/items/${id}`, { method: 'PUT', body: payload })
}

export function getItem(id) {
  return request(`/items/items/${id}`, { method: 'GET' })
}

export function discardItem(id) {
  return request(`/items/items/${id}`, { method: 'DELETE' })
}

export function getDonations(query = {}) {
  return request('/items/donations', { method: 'GET', query })
}

export function beginDonation() {
  return request('/items/donations', { method: 'PUT' })
}

export function createDonation(payload) {
  return request('/items/donations', { method: 'POST', body: payload })
}

export function uploadDonationFile(uuid, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request(`/items/donations/upload/${uuid}`, { method: 'POST', body: formData })
}

export function deleteDonationFile(uuid, filename) {
  return request(`/items/donations/upload/${uuid}/${encodeURIComponent(filename)}`, { method: 'DELETE' })
}

export function getDonation(id) {
  return request(`/items/donations/${id}`, { method: 'GET' })
}

export function updateDonation(id, payload) {
  return request(`/items/donations/${id}`, { method: 'PUT', body: payload })
}

export function updateDonationStatus(id, payload) {
  return request(`/items/donations/${id}/status`, { method: 'PATCH', body: payload })
}

export function getCategories(query = {}) {
  return request('/items/categories', { method: 'GET', query })
}

export function createCategory(payload) {
  return request('/items/categories', { method: 'POST', body: payload })
}

export function updateCategory(id, payload) {
  return request(`/items/categories/${id}`, { method: 'PUT', body: payload })
}

export function getCategory(id) {
  return request(`/items/categories/${id}`, { method: 'GET' })
}

export function discardCategory(id) {
  return request(`/items/categories/${id}`, { method: 'DELETE' })
}

export function createStockRecord(payload) {
  return request('/items/stocks', { method: 'POST', body: payload })
}

export function getStock(id) {
  return request(`/items/stocks/${id}`, { method: 'GET' })
}

export function getStocks(query = {}) {
  return request('/items/stocks', { method: 'GET', query })
}

export function getStockRecords(query = {}) {
  return request('/items/records', { method: 'GET', query })
}

export function createSubscribe(payload) {
  return request('/items/subscribe', { method: 'POST', body: payload })
}

export function getSubscribe(id) {
  return request(`/items/subscribe/${id}`, { method: 'GET' })
}

export function getSubscribes(query = {}) {
  return request('/items/subscribe', { method: 'GET', query })
}

export function cancelSubscribe(subscribeIds) {
  return request('/items/subscribe', {
    method: 'DELETE',
    query: { subscribeIds: Array.isArray(subscribeIds) ? subscribeIds : [subscribeIds] },
  })
}
