import { request } from './http'

export function getPets(query = {}) {
  return request('/pets/', {
    method: 'GET',
    query,
  })
}

export function getPetById(id) {
  return request(`/pets/${id}`, {
    method: 'GET',
  })
}

export function createPet(payload) {
  return request('/pets/', {
    method: 'POST',
    body: payload,
  })
}

export function updatePetById(id, payload) {
  return request(`/pets/${id}`, {
    method: 'PUT',
    body: payload,
  })
}

export function deletePetById(id) {
  return request(`/pets/${id}`, {
    method: 'DELETE',
  })
}

export function uploadPetMedia(id, { file, name, description, isCover = false }) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('isCover', String(Boolean(isCover)))
  if (name) {
    formData.append('name', name)
  }
  if (description) {
    formData.append('description', description)
  }
  return request(`/pets/${id}/media`, {
    method: 'POST',
    body: formData,
  })
}

export function getPetMedia(id) {
  return request(`/pets/${id}/media`, {
    method: 'GET',
  })
}

export function updatePetMedia(id, mediaId, payload) {
  return request(`/pets/${id}/media/${mediaId}`, {
    method: 'PUT',
    body: payload,
  })
}

export function deletePetMedia(id, mediaId) {
  return request(`/pets/${id}/media/${mediaId}`, {
    method: 'DELETE',
  })
}

export function updatePetStatus(id, payload) {
  return request(`/pets/${id}/status`, {
    method: 'PUT',
    body: {
      petId: id,
      ...payload,
    },
  })
}

export function getPetStatusRecords(id, query = {}) {
  return request(`/pets/${id}/status`, {
    method: 'GET',
    query,
  })
}

export function addPetLocation(id, payload) {
  return request(`/pets/${id}/location`, {
    method: 'POST',
    body: payload,
  })
}
