import { reactive } from 'vue'

export function useTableFilters(config) {
  const filters = reactive({})

  for (const [key, cfg] of Object.entries(config)) {
    if (cfg.type === 'text') {
      filters[key] = { type: 'text', value: '', mode: 'fuzzy' }
    } else if (cfg.type === 'enum') {
      filters[key] = { type: 'enum', values: [] }
    } else if (cfg.type === 'number') {
      filters[key] = { type: 'number', min: null, max: null }
    } else if (cfg.type === 'time') {
      filters[key] = { type: 'time', range: null }
    }
  }

  function isActive(key) {
    const f = filters[key]
    if (!f) return false
    if (f.type === 'text') return f.value.trim() !== ''
    if (f.type === 'enum') return f.values.length > 0
    if (f.type === 'number') return f.min !== null || f.max !== null
    if (f.type === 'time') return f.range !== null && f.range.length === 2
    return false
  }

  function applyFilter(rows, customMatchers = {}) {
    let result = rows

    for (const [key, f] of Object.entries(filters)) {
      const matcher = customMatchers[key]

      if (f.type === 'text') {
        const text = f.value.trim().toLowerCase()
        if (!text) continue
        result = result.filter((r) => {
          const v = String(r[key] ?? '').toLowerCase()
          return f.mode === 'exact' ? v === text : v.includes(text)
        })
      } else if (f.type === 'enum') {
        if (f.values.length === 0) continue
        result = result.filter((r) =>
          matcher
            ? matcher(r[key], f.values)
            : f.values.includes(r[key]),
        )
      } else if (f.type === 'number') {
        if (f.min !== null) {
          result = result.filter((r) => Number(r[key] ?? 0) >= f.min)
        }
        if (f.max !== null) {
          result = result.filter((r) => Number(r[key] ?? 0) <= f.max)
        }
      } else if (f.type === 'time') {
        if (f.range && f.range.length === 2) {
          const [start, end] = f.range
          result = result.filter((r) => {
            const t = new Date(r[key]).getTime()
            return (!start || t >= new Date(start).getTime()) &&
              (!end || t <= new Date(end).getTime())
          })
        }
      }
    }

    return result
  }

  return { filters, isActive, applyFilter }
}