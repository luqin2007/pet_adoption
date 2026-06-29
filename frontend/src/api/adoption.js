import { request } from './http'

export function getAdoptApplications(query = {}) {
  return request('/adopt/adopt', { method: 'GET', query })
}

export function getAdoptApplication(id) {
  return request(`/adopt/adopt/${id}`, { method: 'GET' })
}

export function createAdoptApplication(payload) {
  return request('/adopt/adopt', { method: 'POST', body: payload })
}

export function updateAdoptStatus(id, status) {
  return request(`/adopt/adopt/${id}/${encodeURIComponent(status)}`, { method: 'PATCH' })
}

export function addFollowTask(id, payload) {
  return request(`/adopt/follow/adopt/${id}`, { method: 'POST', body: payload })
}

export function getFollowTask(id) {
  return request(`/adopt/follow/${id}`, { method: 'GET' })
}

export function getFollowTasks(query = {}) {
  return request('/adopt/follow', { method: 'GET', query })
}

export function updateFollowTask(id, payload) {
  return request(`/adopt/follow/${id}`, { method: 'PUT', body: payload })
}

export function addFollowRecord(id, payload) {
  return request(`/adopt/follow/${id}/record`, { method: 'POST', body: payload })
}

export function getFollowRecords(idOrQuery, page) {
  if (typeof idOrQuery === 'number' || typeof idOrQuery === 'string') {
    return request(`/adopt/follow/${idOrQuery}/record`, { method: 'GET', query: page || {} })
  }
  return request('/adopt/follow/record', { method: 'GET', query: idOrQuery || {} })
}

export function getBreadingApplications(query = {}) {
  return request('/adopt/breading', { method: 'GET', query })
}

export function getBreadingApplication(id) {
  return request(`/adopt/breading/${id}`, { method: 'GET' })
}

export function updateBreadingStatus(id, status) {
  return request(`/adopt/breading/${id}/${encodeURIComponent(status)}`, { method: 'PATCH' })
}

export function beginAgreement() {
  return request('/adopt/agreement', { method: 'PUT' })
}

export function uploadAgreementWhenAdd(uuid, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request(`/adopt/agreement/upload/${uuid}`, { method: 'POST', body: formData })
}

export function deleteAgreementWhenAdd(uuid, filename) {
  return request(`/adopt/agreement/upload/${uuid}/${encodeURIComponent(filename)}`, { method: 'DELETE' })
}

export function addAgreement(payload) {
  return request('/adopt/agreement', { method: 'POST', body: payload })
}

export function getAgreements(query = {}) {
  return request('/adopt/agreement', { method: 'GET', query })
}

export function getAgreement(id) {
  return request(`/adopt/agreement/${id}`, { method: 'GET' })
}

export function updateAgreement(id, payload) {
  return request(`/adopt/agreement/${id}`, { method: 'PUT', body: payload })
}

export function uploadAgreement(id, { file, page }) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('page', String(page))
  return request(`/adopt/agreement/${id}/files`, { method: 'POST', body: formData })
}

export function deleteAgreementFile(id, fileId) {
  return request(`/adopt/agreement/${id}/files/${fileId}`, { method: 'DELETE' })
}

export function reorderAgreementFiles(id, fileOrder) {
  return request(`/adopt/agreement/${id}/files/order`, { method: 'PUT', body: { fileOrder } })
}

export function signAgreement(id, file) {
  const formData = new FormData()
  formData.append('sign', file)
  return request(`/adopt/agreement/${id}/sign`, { method: 'PATCH', body: formData })
}

export function confirmAgreementSign(id, payload) {
  return request(`/adopt/agreement/${id}/sign/confirm`, { method: 'PATCH', body: payload })
}

export function createBreadingApplication(payload) {
  return request('/adopt/breading', { method: 'POST', body: payload })
}
