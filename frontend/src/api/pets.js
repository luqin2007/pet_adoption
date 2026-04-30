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
