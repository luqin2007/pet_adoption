import { request } from './http'

export function getLostPets(query = {}) {
  return request('/lost/pets', {
    method: 'GET',
    query,
  })
}
