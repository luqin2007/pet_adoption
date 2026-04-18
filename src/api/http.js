const API_BASE_URL = String(import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')
const SESSION_STORAGE_KEY = 'pet_adoption_user_session'
let refreshAccessTokenPromise = null

function isSuccessCode(code) {
  return code === 0 || code === 200 || code === '0' || code === '200'
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
        id: Number(parsed?.profile?.id || 0),
        username: parsed?.profile?.username || '',
        email: parsed?.profile?.email || '',
        avatar: parsed?.profile?.avatar || '',
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
      id: Number(userData.id ?? current.profile.id ?? 0),
      username: userData.username ?? current.profile.username ?? '',
      email: userData.email ?? current.profile.email ?? '',
      avatar: userData.avatar ?? current.profile.avatar ?? '',
    },
  })
}

function buildUrl(path, query) {
  const base = `${API_BASE_URL}${path}`
  if (!query || Object.keys(query).length === 0) {
    return base
  }

  const search = new URLSearchParams()
  Object.entries(query).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.append(key, String(value))
    }
  })

  const queryString = search.toString()
  if (!queryString) {
    return base
  }
  return `${base}?${queryString}`
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
      result = await response.json()
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
      const errorPayload = await response.json()
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
    result = await response.json()
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

  return result.data ?? null
}
