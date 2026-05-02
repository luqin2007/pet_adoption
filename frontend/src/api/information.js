import { request } from './http'

const INFORMATION_CACHE_PREFIX = 'pet_adoption_information_cache_v2'
const INFORMATION_CACHE_TTL = 1000 * 60 * 60 * 24 * 30

function readCache(key) {
  if (typeof window === 'undefined') {
    return null
  }

  try {
    const raw = localStorage.getItem(`${INFORMATION_CACHE_PREFIX}:${key}`)
    if (!raw) {
      return null
    }

    const parsed = JSON.parse(raw)
    if (!parsed || typeof parsed !== 'object') {
      return null
    }

    if (!parsed.expiresAt || Date.now() > Number(parsed.expiresAt)) {
      localStorage.removeItem(`${INFORMATION_CACHE_PREFIX}:${key}`)
      return null
    }

    return {
      hit: true,
      data: parsed.data ?? null,
    }
  } catch {
    return null
  }
}

function writeCache(key, data) {
  if (typeof window === 'undefined') {
    return
  }

  try {
    localStorage.setItem(
      `${INFORMATION_CACHE_PREFIX}:${key}`,
      JSON.stringify({
        data,
        expiresAt: Date.now() + INFORMATION_CACHE_TTL,
      }),
    )
  } catch {
    // ignore storage write failures
  }
}

async function requestWithCache(cacheKey, path, query) {
  const cached = readCache(cacheKey)
  if (cached?.hit) {
    return cached.data
  }

  const data = await request(path, {
    method: 'GET',
    query,
  })

  if (Array.isArray(data)) {
    writeCache(cacheKey, data)
  }

  return data
}

export function getProvinceCatalog() {
  return requestWithCache('provinces', '/info/provinces')
}

export function getCityCatalog(province) {
  return requestWithCache(`cities:${province}`, '/info/cities', {
    province,
  })
}

export function getDistrictCatalog(province, city) {
  return requestWithCache(`districts:${province}:${city}`, '/info/districts', {
    province,
    city,
  })
}

export function getPetTypeCatalog() {
  return request('/info/pet-types', {
    method: 'GET',
  })
}
