import { request } from './http'
import { OPENAPI_OPERATION_COUNT, OPENAPI_OPERATIONS } from './openapiCatalog'

export { OPENAPI_OPERATION_COUNT, OPENAPI_OPERATIONS }

function cleanValues(values = {}) {
  return Object.fromEntries(
    Object.entries(values).filter(([, value]) => value !== undefined && value !== null && value !== ''),
  )
}

export function buildOperationPath(operation, pathValues = {}) {
  return operation.path.replace(/\{([^}]+)\}/g, (_match, key) => {
    const value = pathValues[key]
    if (value === undefined || value === null || value === '') {
      throw new Error(`缺少路径参数：${key}`)
    }
    return encodeURIComponent(String(value))
  })
}

export function parseJsonBody(text) {
  const trimmed = String(text || '').trim()
  if (!trimmed) {
    return undefined
  }
  return JSON.parse(trimmed)
}

export async function callOpenApiOperation(operation, payload = {}) {
  const path = buildOperationPath(operation, payload.pathValues)
  const options = {
    method: operation.method,
    query: cleanValues(payload.queryValues),
  }

  if (operation.contentType === 'application/json') {
    const parsedBody = parseJsonBody(payload.bodyText)
    if (parsedBody !== undefined) {
      options.body = parsedBody
    }
  }

  if (operation.contentType === 'multipart/form-data') {
    const formData = new FormData()
    Object.entries(cleanValues(payload.formValues)).forEach(([key, value]) => {
      formData.append(key, value)
    })
    Object.entries(payload.files || {}).forEach(([key, fileList]) => {
      Array.from(fileList || []).forEach((file) => {
        formData.append(key, file)
      })
    })
    options.body = formData
  }

  return request(path, options)
}
