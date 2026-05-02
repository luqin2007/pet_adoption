import { request } from './http'

export function getLostPets(query = {}) {
  return request('/lost/pets', {
    method: 'GET',
    query,
  })
}

export function createLostPetClaim(payload) {
  return request('/lost/claim', {
    method: 'POST',
    body: payload,
  })
}

export function beginLostPet() {
  return request('/lost/pets', {
    method: 'PUT',
  })
}

export function createLostPet(payload) {
  return request('/lost/pets', {
    method: 'POST',
    body: payload,
  })
}

export function updateLostPet(id, payload) {
  return request(`/lost/pets/${id}`, {
    method: 'PUT',
    body: payload,
  })
}

export function updateLostPetStatus(id, payload) {
  return request(`/lost/pets/${id}/status`, {
    method: 'PATCH',
    body: payload,
  })
}

export function deleteLostPet(id) {
  return request(`/lost/pets/${id}`, {
    method: 'DELETE',
  })
}

export function uploadLostPetMedia(id, file, name = '') {
  const formData = new FormData()
  formData.append('file', file)
  if (name) {
    formData.append('name', name)
  }
  return request(`/lost/pets/${id}/media`, {
    method: 'PUT',
    body: formData,
  })
}

export function deleteLostPetUpload(id, filename) {
  return request(`/lost/pets/${id}/media/${encodeURIComponent(filename)}`, {
    method: 'DELETE',
  })
}
