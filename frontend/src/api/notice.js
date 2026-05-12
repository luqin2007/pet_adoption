import { request } from './http'

export function getNotices(query = {}) {
  return request('/notices', {
    method: 'GET',
    query,
  })
}

export function getUnreadNoticeCount() {
  return request('/notices/unread', {
    method: 'GET',
  })
}

export function markNoticeRead(ids = []) {
  return request('/notices/read', {
    method: 'PATCH',
    body: { ids },
  })
}

export function markNoticeUnread(ids = []) {
  return request('/notices/unread', {
    method: 'PATCH',
    body: { ids },
  })
}

export function sendNotice(payload) {
  return request('/notices/send', {
    method: 'POST',
    body: payload,
  })
}
