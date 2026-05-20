<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RefreshRight } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import PetCard from '../components/PetCard.vue'
import { getPets } from '../api/pets'
import { useInformationCatalog } from '../composables/useInformationCatalog'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const router = useRouter()
const loading = ref(false)
const pets = ref([])
const showFilterPanel = ref(false)
const filters = ref({
  type: '',
  breed: '',
  sex: '',
  age0: undefined,
  age1: undefined,
  name: '',
  province: '',
  city: '',
  district: '',
  address: '',
})

const visibleStatuses = ['SHELTERED', 'HEALTH']
const hasValue = (value) => value !== undefined && value !== null && value !== ''
const {
  ensureInformationCatalog,
  ensureCityOptions,
  ensureDistrictOptions,
  provinceOptions,
  typeOptions,
  getCityOptions,
  getDistrictOptions,
  getBreedOptions,
} = useInformationCatalog()

const activeType = computed(() => filters.value.type)
const activeBreed = computed(() => filters.value.breed)
const cityOptions = computed(() => getCityOptions(filters.value.province))
const districtOptions = computed(() => getDistrictOptions(filters.value.province, filters.value.city))
const breedOptions = computed(() => getBreedOptions(activeType.value))

const activeFilterCount = computed(() => {
  let count = 0
  if (filters.value.type) count++
  if (filters.value.breed) count++
  if (filters.value.sex) count++
  if (hasValue(filters.value.age0) || hasValue(filters.value.age1)) count++
  if (filters.value.name) count++
  if (filters.value.province) count++
  if (filters.value.city) count++
  if (filters.value.district) count++
  if (filters.value.address) count++
  return count
})

function applyFilters() {
  showFilterPanel.value = false
  loadPets()
}

function resetFilters() {
  filters.value = {
    type: '',
    breed: '',
    sex: '',
    age0: undefined,
    age1: undefined,
    name: '',
    province: '',
    city: '',
    district: '',
    address: '',
  }
}

const filteredPets = computed(() =>
  pets.value.filter((pet) => {
    const location = pet.locations?.[0] || {}
    const age = Number(pet.age)
    const status = pet.status || ''
    const nameText = `${pet.name || ''} ${pet.description || ''}`
    const locationText = `${location.province || ''} ${location.city || ''} ${location.district || ''} ${
      location.detailAddress || location.address || ''
    }`

    return (
      visibleStatuses.includes(status) &&
      (!activeType.value || (pet.type || '').includes(activeType.value)) &&
      (!activeBreed.value || (pet.breed || '').includes(activeBreed.value)) &&
      (!filters.value.sex || (pet.sex || '') === filters.value.sex) &&
      (!hasValue(filters.value.age0) || (!Number.isNaN(age) && age >= Number(filters.value.age0))) &&
      (!hasValue(filters.value.age1) || (!Number.isNaN(age) && age <= Number(filters.value.age1))) &&
      (!filters.value.name || nameText.includes(filters.value.name)) &&
      (!filters.value.province || (location.province || '').includes(filters.value.province)) &&
      (!filters.value.city || (location.city || '').includes(filters.value.city)) &&
      (!filters.value.district || (location.district || '').includes(filters.value.district)) &&
      (!filters.value.address || locationText.includes(filters.value.address))
    )
  }),
)

async function loadPets() {
  loading.value = true
  try {
    const result = await getPets({
      size: 12,
      age0: hasValue(filters.value.age0) ? Number(filters.value.age0) : undefined,
      age1: hasValue(filters.value.age1) ? Number(filters.value.age1) : undefined,
      sex: filters.value.sex || undefined,
      city: filters.value.city || undefined,
      province: filters.value.province || undefined,
      district: filters.value.district || undefined,
      address: filters.value.address || undefined,
      type: activeType.value ? [activeType.value] : undefined,
      breed: activeBreed.value ? [activeBreed.value] : undefined,
      status: visibleStatuses,
      name: filters.value.name || undefined,
    })
    pets.value = Array.isArray(result?.records) ? result.records : []
  } catch {
    pets.value = []
  } finally {
    loading.value = false
  }
}

function openPetProfile(id) {
  if (!id) {
    return
  }
  router.push(`/pets/${id}`)
}

function handleFilterTypeChange() {
  filters.value.breed = ''
  applyFilters()
}

function handleFilterProvinceChange() {
  filters.value.city = ''
  filters.value.district = ''
  if (filters.value.province) {
    ensureCityOptions(filters.value.province)
  }
  applyFilters()
}

function handleFilterCityChange() {
  filters.value.district = ''
  if (filters.value.province && filters.value.city) {
    ensureDistrictOptions(filters.value.province, filters.value.city)
  }
  applyFilters()
}

watch(
  () => filters.value.province,
  async (province) => {
    if (province) {
      await ensureCityOptions(province)
    }
  },
  { immediate: false },
)

watch(
  () => [filters.value.province, filters.value.city],
  async ([province, city]) => {
    if (province && city) {
      await ensureDistrictOptions(province, city)
    }
  },
  { immediate: false },
)

onMounted(async () => {
  await ensureInformationCatalog()
  loadPets()
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main">
      <section class="directory-hero">
        <div>
          <h1>领养大厅</h1>
        </div>
      </section>

      <section class="filter-panel pet-directory-filter-panel" @keyup.enter="applyFilters">
        <div class="pet-filter-row pet-filter-row-primary">
          <el-select v-model="filters.type" class="filter-field-sm" placeholder="宠物类型" clearable filterable @change="handleFilterTypeChange">
            <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="filters.breed" class="filter-field-sm" placeholder="品种" clearable filterable :disabled="!activeType" @change="applyFilters">
            <el-option v-for="item in breedOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="filters.sex" class="filter-field-sm" placeholder="性别" clearable @change="applyFilters">
            <el-option label="未知" value="未知" />
            <el-option label="公" value="公" />
            <el-option label="母" value="母" />
          </el-select>
          <div class="pet-age-range filter-field-lg">
            <el-input-number v-model="filters.age0" :min="0" :controls="false" placeholder="最小月龄" @change="applyFilters" />
            <span>至</span>
            <el-input-number v-model="filters.age1" :min="0" :controls="false" placeholder="最大月龄" @change="applyFilters" />
          </div>
          <el-input v-model="filters.name" class="filter-field-md" placeholder="名称" clearable @change="applyFilters" />
        </div>

        <div class="pet-filter-row pet-filter-row-primary" style="margin-top:8px">
          <el-select v-model="filters.province" class="filter-field-sm" placeholder="省" clearable filterable @change="handleFilterProvinceChange">
            <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="filters.city" class="filter-field-sm" placeholder="市" clearable filterable :disabled="!filters.province" @change="handleFilterCityChange">
            <el-option v-for="item in cityOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="filters.district" class="filter-field-sm" placeholder="县 / 县级市" clearable filterable :disabled="!filters.city" @change="applyFilters">
            <el-option v-for="item in districtOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-input v-model="filters.address" class="filter-field-lg" placeholder="地址" clearable @change="applyFilters" />
          <el-button @click="resetFilters">重置</el-button>
          <el-button class="warm-btn" :icon="RefreshRight" @click="applyFilters">搜索</el-button>
        </div>
      </section>

      <section class="directory-grid" v-loading="loading">
        <PetCard v-for="pet in filteredPets" :key="pet.id" :pet="pet" @click="openPetProfile(pet.id)" />

        <el-empty v-if="!loading && filteredPets.length === 0" description="没有找到合适的领养档案" />
      </section>
    </main>

    <AppFooter />
  </div>
</template>
