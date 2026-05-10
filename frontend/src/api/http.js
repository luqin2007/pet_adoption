const API_BASE_URL = String(import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')
const API_PREFIX = String(import.meta.env.VITE_API_PREFIX || '/api/v1')
  .replace(/^\/?/, '/')
  .replace(/\/$/, '')
const SHOULD_APPLY_API_PREFIX = Boolean(API_PREFIX && !API_BASE_URL.endsWith(API_PREFIX))
const SESSION_STORAGE_KEY = 'pet_adoption_user_session'
let refreshAccessTokenPromise = null

function isSuccessCode(code) {
  return code === 0 || code === 200 || code === '0' || code === '200'
}

function parseJsonPreservingLongs(text) {
  if (!text) {
    return null
  }
  return JSON.parse(text.replace(/(:|\[|,)\s*(-?\d{16,})(?=\s*[,}\]])/g, '$1"$2"'))
}

export class ApiError extends Error {
  constructor(message, code = 500, status = 200) {
    super(message)
    this.name = 'ApiError'
    this.code = code
    this.status = status
  }
}

function readSession() {
  if (typeof window === 'undefined') {
    return {
      accessToken: '',
      refreshToken: '',
      profile: {
        id: 0,
        username: '',
        email: '',
        avatar: '',
      },
    }
  }

  try {
    const raw = localStorage.getItem(SESSION_STORAGE_KEY)
    if (!raw) {
      return {
        accessToken: '',
        refreshToken: '',
        profile: {
          id: 0,
          username: '',
          email: '',
          avatar: '',
        },
      }
    }

    const parsed = JSON.parse(raw)
    return {
      accessToken: parsed?.accessToken || parsed?.token || '',
      refreshToken: parsed?.refreshToken || '',
      profile: {
        id: String(parsed?.profile?.id || ''),
        username: parsed?.profile?.username || '',
        email: parsed?.profile?.email || '',
        avatar: buildAssetUrl(parsed?.profile?.avatar || ''),
      },
    }
  } catch {
    return {
      accessToken: '',
      refreshToken: '',
      profile: {
        id: 0,
        username: '',
        email: '',
        avatar: '',
      },
    }
  }
}

function writeSession(session) {
  if (typeof window === 'undefined') {
    return
  }
  localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session))
}

function clearSession() {
  if (typeof window === 'undefined') {
    return
  }
  localStorage.removeItem(SESSION_STORAGE_KEY)
}

function syncSessionFromAuthData(userData) {
  if (!userData || typeof userData !== 'object') {
    return
  }

  const current = readSession()
  writeSession({
    accessToken: userData.accessToken || current.accessToken || '',
    refreshToken: userData.refreshToken || current.refreshToken || '',
    profile: {
      id: String(userData.id ?? current.profile.id ?? ''),
      username: userData.username ?? current.profile.username ?? '',
      email: userData.email ?? current.profile.email ?? '',
      avatar: buildAssetUrl(userData.avatar ?? current.profile.avatar ?? ''),
    },
  })
}

function withApiPrefix(path) {
  const normalizedPath = String(path || '').startsWith('/') ? String(path || '') : `/${path || ''}`
  if (
    !SHOULD_APPLY_API_PREFIX ||
    normalizedPath === API_PREFIX ||
    normalizedPath.startsWith(`${API_PREFIX}/`)
  ) {
    return normalizedPath
  }
  return `${API_PREFIX}${normalizedPath}`
}

function buildAssetUrl(path) {
  const normalizedPath = String(path || '').trim()
  if (!normalizedPath) {
    return normalizedPath
  }
  if (/^(https?:)?\/\//.test(normalizedPath) || normalizedPath.startsWith('data:') || normalizedPath.startsWith('blob:')) {
    return normalizedPath
  }
  if (normalizedPath.startsWith('/assets/')) {
    return `${API_BASE_URL}${withApiPrefix(normalizedPath)}`
  }
  if (API_PREFIX && normalizedPath.startsWith(`${API_PREFIX}/assets/`)) {
    return `${API_BASE_URL}${normalizedPath}`
  }
  return normalizedPath
}

function buildUrl(path, query) {
  const base = `${API_BASE_URL}${withApiPrefix(path)}`
  if (!query || Object.keys(query).length === 0) {
    return base
  }

  const search = new URLSearchParams()
  Object.entries(query).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      if (Array.isArray(value)) {
        value.forEach((item) => {
          if (item !== undefined && item !== null && item !== '') {
            search.append(key, String(item))
          }
        })
      } else {
        search.append(key, String(value))
      }
    }
  })

  const queryString = search.toString()
  if (!queryString) {
    return base
  }
  return `${base}?${queryString}`
}

function normalizeApiAssetUrls(value) {
  if (Array.isArray(value)) {
    return value.map((item) => normalizeApiAssetUrls(item))
  }

  if (value && typeof value === 'object') {
    return Object.fromEntries(Object.entries(value).map(([key, item]) => [key, normalizeApiAssetUrls(item)]))
  }

  if (typeof value === 'string') {
    return buildAssetUrl(value)
  }

  return value
}

function getRequestHeaders(headers, skipAuth) {
  const finalHeaders = {
    Accept: 'application/json',
    ...headers,
  }

  if (!skipAuth && !finalHeaders.Authorization) {
    const { accessToken } = readSession()
    if (accessToken) {
      finalHeaders.Authorization = `Bearer ${accessToken}`
    }
  }

  return finalHeaders
}

async function refreshAccessToken() {
  if (refreshAccessTokenPromise) {
    return refreshAccessTokenPromise
  }

  refreshAccessTokenPromise = (async () => {
    const { refreshToken } = readSession()
    if (!refreshToken) {
      return false
    }

    const response = await fetch(buildUrl('/auth/refresh'), {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ refreshToken }),
    })

    if (!response.ok) {
      clearSession()
      return false
    }

    let result
    try {
      result = parseJsonPreservingLongs(await response.text())
    } catch {
      clearSession()
      return false
    }

    if (!result || typeof result !== 'object' || !isSuccessCode(result.code) || !result.data?.accessToken) {
      clearSession()
      return false
    }

    syncSessionFromAuthData(result.data)
    return true
  })().finally(() => {
    refreshAccessTokenPromise = null
  })

  return refreshAccessTokenPromise
}

export async function request(path, options = {}) {
  const {
    method = 'GET',
    query,
    body,
    headers = {},
    signal,
    skipAuth = false,
    retryOnUnauthorized = true,
  } = options

  const url = buildUrl(path, query)
  const finalHeaders = getRequestHeaders(headers, skipAuth)

  let payload = body
  if (body !== undefined && body !== null && !(body instanceof FormData)) {
    finalHeaders['Content-Type'] = finalHeaders['Content-Type'] || 'application/json'
    payload = JSON.stringify(body)
  }

  const response = await fetch(url, {
    method,
    headers: finalHeaders,
    body: payload,
    signal,
  })

  if (response.status === 401 && !skipAuth && retryOnUnauthorized) {
    const refreshed = await refreshAccessToken()
    if (refreshed) {
      return request(path, {
        ...options,
        retryOnUnauthorized: false,
      })
    }
    clearSession()
  }

  if (!response.ok) {
    let message = `请求失败（HTTP ${response.status}）`
    try {
      const errorPayload = parseJsonPreservingLongs(await response.text())
      if (errorPayload?.message) {
        message = String(errorPayload.message)
      } else if (typeof errorPayload === 'string' && errorPayload) {
        message = errorPayload
      }
    } catch {
      try {
        const text = await response.text()
        if (text) {
          message = text
        }
      } catch {
        // ignore parsing errors
      }
    }
    throw new ApiError(message, response.status, response.status)
  }

  let result
  try {
    result = parseJsonPreservingLongs(await response.text())
  } catch {
    throw new ApiError('服务响应不是有效 JSON', 500, response.status)
  }

  if (!result || typeof result !== 'object') {
    throw new ApiError('服务响应格式错误', 500, response.status)
  }

  if (!isSuccessCode(result.code)) {
    throw new ApiError(result.message || '请求失败', result.code || 500, response.status)
  }

  if (result?.data?.accessToken) {
    syncSessionFromAuthData(result.data)
  }

  return normalizeApiAssetUrls(result.data ?? null)
}
