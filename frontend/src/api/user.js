import { request } from './http'

export function loginUser(payload) {
  return request('/auth/login', {
    method: 'POST',
    body: payload,
    skipAuth: true,
  })
}

export function registerUser(payload) {
  const formData = new FormData()
  Object.entries(payload || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      formData.append(key, value)
    }
  })

  return request('/auth/register', {
    method: 'POST',
    body: formData,
    skipAuth: true,
  })
}

export function sendRegisterCode(email) {
  return request('/auth/check/code', {
    method: 'POST',
    query: { email },
    skipAuth: true,
  })
}

export function logoutUser(refreshToken) {
  return request('/auth/logout', {
    method: 'POST',
    body: { refreshToken },
  })
}

export function refreshUserToken(refreshToken) {
  return request('/auth/refresh', {
    method: 'POST',
    body: { refreshToken },
    skipAuth: true,
  })
}

export function checkUsernameExists(username) {
  return request(`/auth/check/username/${encodeURIComponent(username)}`, {
    method: 'GET',
    skipAuth: true,
  })
}

export function checkEmailExists(email) {
  return request(`/auth/check/email/${encodeURIComponent(email)}`, {
    method: 'GET',
    skipAuth: true,
  })
}

export function getUserById(id) {
  return request(`/users/${id}`, {
    method: 'GET',
  })
}

export function getUsers(query = {}) {
  return request('/users', {
    method: 'GET',
    query,
  })
}

export function updateUserById(id, payload) {
  return request(`/users/${id}`, {
    method: 'PUT',
    body: payload,
  })
}

export function uploadUserAvatar(id, file) {
  const formData = new FormData()
  formData.append('avatar', file)
  return request(`/users/${id}/avatar`, {
    method: 'PATCH',
    body: formData,
  })
}

export function deleteUserAvatar(id) {
  return request(`/users/${id}/avatar`, {
    method: 'DELETE',
  })
}

export function removeUserById(id) {
  return request(`/users/${id}`, {
    method: 'DELETE',
  })
}

export function forgetPassword(email) {
  return request('/auth/forget', {
    method: 'POST',
    query: { email },
    skipAuth: true,
  })
}

export function resetPassword(payload) {
  return request('/auth/reset', {
    method: 'POST',
    body: payload,
    skipAuth: true,
  })
}
