const API_BASE_URL = String(import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')

export class ApiError extends Error {
  constructor(message, code = 500, status = 200) {
    super(message)
    this.name = 'ApiError'
    this.code = code
    this.status = status
  }
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

export async function request(path, options = {}) {
  const {
    method = 'GET',
    query,
    body,
    headers = {},
    signal,
  } = options

  const url = buildUrl(path, query)
  const finalHeaders = {
    Accept: 'application/json',
    ...headers,
  }

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

  if (!response.ok) {
    let message = `请求失败（HTTP ${response.status}）`
    try {
      const text = await response.text()
      if (text) {
        message = text
      }
    } catch {
      // ignore text parsing errors
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

  if (result.code !== 200) {
    throw new ApiError(result.message || '请求失败', result.code || 500, response.status)
  }

  return result.data ?? null
}
