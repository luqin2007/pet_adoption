import { request } from './http'

export function loginUser(payload) {
  return request('/user/login', {
    method: 'POST',
    body: payload,
  })
}

export function registerUser(payload) {
  return request('/user/register', {
    method: 'POST',
    body: payload,
  })
}

export function sendRegisterCode(email) {
  return request('/user/check/code', {
    method: 'GET',
    query: { email },
  })
}
