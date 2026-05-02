import { computed, ref } from 'vue'
import { getCityCatalog, getDistrictCatalog, getPetTypeCatalog, getProvinceCatalog } from '../api/information'

const provinceCatalog = ref([])
const petTypeCatalog = ref([])
const cityCatalog = ref({})
const districtCatalog = ref({})
const loadingInformation = ref(false)
let loadingPromise = null
const cityLoadingPromises = new Map()
const districtLoadingPromises = new Map()

function normalize(value) {
  return String(value || '').trim()
}

export function resolveCatalogValue(selected, custom) {
  return normalize(custom) || normalize(selected)
}

export function splitCatalogValue(value, options = []) {
  const normalizedValue = normalize(value)
  if (!normalizedValue) {
    return { selected: '', custom: '' }
  }
  return options.includes(normalizedValue)
    ? { selected: normalizedValue, custom: '' }
    : { selected: '', custom: normalizedValue }
}

export function useInformationCatalog() {
  const provinceOptions = computed(() => provinceCatalog.value.filter(Boolean))
  const typeOptions = computed(() => petTypeCatalog.value.map((item) => item.type).filter(Boolean))

  function getCityOptions(province) {
    return cityCatalog.value[normalize(province)] || []
  }

  function getDistrictOptions(province, city) {
    return districtCatalog.value[`${normalize(province)}::${normalize(city)}`] || []
  }

  function getBreedOptions(type) {
    const matchedType = petTypeCatalog.value.find((item) => item.type === normalize(type))
    return matchedType?.breeds?.filter(Boolean) || []
  }

  async function ensureInformationCatalog() {
    if (provinceCatalog.value.length && petTypeCatalog.value.length) {
      return
    }
    if (loadingPromise) {
      await loadingPromise
      return
    }

    loadingInformation.value = true
    loadingPromise = Promise.all([getProvinceCatalog(), getPetTypeCatalog()])
      .then(([provinces, petTypes]) => {
        provinceCatalog.value = Array.isArray(provinces) ? provinces : []
        petTypeCatalog.value = Array.isArray(petTypes) ? petTypes : []
      })
      .finally(() => {
        loadingInformation.value = false
        loadingPromise = null
      })

    await loadingPromise
  }

  async function ensureCityOptions(province) {
    const key = normalize(province)
    if (!key) {
      return []
    }
    if (Array.isArray(cityCatalog.value[key])) {
      return cityCatalog.value[key]
    }
    if (cityLoadingPromises.has(key)) {
      return cityLoadingPromises.get(key)
    }

    const promise = getCityCatalog(key)
      .then((cities) => {
        cityCatalog.value = {
          ...cityCatalog.value,
          [key]: Array.isArray(cities) ? cities : [],
        }
        return cityCatalog.value[key]
      })
      .finally(() => {
        cityLoadingPromises.delete(key)
      })

    cityLoadingPromises.set(key, promise)
    return promise
  }

  async function ensureDistrictOptions(province, city) {
    const provinceKey = normalize(province)
    const cityKey = normalize(city)
    const key = `${provinceKey}::${cityKey}`
    if (!provinceKey || !cityKey) {
      return []
    }
    if (Array.isArray(districtCatalog.value[key])) {
      return districtCatalog.value[key]
    }
    if (districtLoadingPromises.has(key)) {
      return districtLoadingPromises.get(key)
    }

    const promise = getDistrictCatalog(provinceKey, cityKey)
      .then((districts) => {
        districtCatalog.value = {
          ...districtCatalog.value,
          [key]: Array.isArray(districts) ? districts : [],
        }
        return districtCatalog.value[key]
      })
      .finally(() => {
        districtLoadingPromises.delete(key)
      })

    districtLoadingPromises.set(key, promise)
    return promise
  }

  return {
    provinceCatalog,
    cityCatalog,
    districtCatalog,
    petTypeCatalog,
    loadingInformation,
    provinceOptions,
    typeOptions,
    getCityOptions,
    getDistrictOptions,
    getBreedOptions,
    ensureInformationCatalog,
    ensureCityOptions,
    ensureDistrictOptions,
  }
}
