<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { ArrowRight, Filter, Plus, RefreshRight } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { getPets } from '../api/pets'
import { useInformationCatalog } from '../composables/useInformationCatalog'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const router = useRouter()
const loading = ref(false)
const pets = ref([])
const filterDialogVisible = ref(false)
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
  filterDialogVisible.value = false
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

function formatAge(age) {
  if (!age && age !== 0) {
    return '年龄待补充'
  }
  const months = Number(age)
  if (Number.isNaN(months)) {
    return '年龄待补充'
  }
  if (months > 12) {
    const years = Math.floor(months / 12)
    const remainMonths = months % 12
    return remainMonths > 0 ? `约 ${years} 岁 ${remainMonths} 个月` : `约 ${years} 岁`
  }
  return `约 ${months} 个月`
}

function formatLocation(pet) {
  const location = pet.locations?.[0]
  if (!location) {
    return '待补充位置'
  }
  return [location.city, location.district].filter(Boolean).join(' · ')
}

function formatTags(pet) {
  const tags = Array.isArray(pet.tags) ? pet.tags.map((item) => item?.tag).filter(Boolean) : []
  return tags.join(' · ')
}

function getStatusText(pet) {
  if (pet.status === 'HEALTH') {
    return '可预约见面'
  }
  if (pet.status === 'SHELTERED') {
    return '等待领养'
  }
  return pet.health || '资料完善中'
}

function openPetProfile(id) {
  if (!id) {
    return
  }
  router.push(`/pets/${id}`)
}

function handleFilterTypeChange() {
  filters.value.breed = ''
}

function handleFilterProvinceChange() {
  filters.value.city = ''
  filters.value.district = ''
  if (filters.value.province) {
    ensureCityOptions(filters.value.province)
  }
}

function handleFilterCityChange() {
  filters.value.district = ''
  if (filters.value.province && filters.value.city) {
    ensureDistrictOptions(filters.value.province, filters.value.city)
  }
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
          <span class="hero-chip">领养大厅</span>
          <h1>把一只流浪生命，接回一个真正的家</h1>
          <p>
            按类型、年龄和位置筛一筛，找到想见面的毛孩子。
          </p>
        </div>
        <div class="directory-hero-side">
          <span class="directory-hero-count">{{ filteredPets.length }} 份领养档案</span>
          <div class="directory-hero-actions">
            <el-badge :value="activeFilterCount" :hidden="activeFilterCount === 0">
              <el-button class="warm-btn" :icon="Filter" @click="filterDialogVisible = true">筛选条件</el-button>
            </el-badge>
            <el-button class="warm-btn directory-hero-action" :icon="Plus" @click="router.push('/pets/new')">发现宠物</el-button>
          </div>
        </div>
      </section>

      <section class="directory-grid" v-loading="loading">
        <article v-for="pet in filteredPets" :key="pet.id" class="directory-card">
          <div class="directory-cover">
            <img v-if="pet.cover" :src="pet.cover" :alt="pet.name" loading="lazy" />
            <div v-else class="directory-cover-placeholder">暂无封面</div>
            <span class="directory-badge">{{ getStatusText(pet) }}</span>
          </div>
          <div class="directory-body">
            <div class="directory-head">
              <div>
                <h3>{{ pet.name }}</h3>
                <p>{{ formatAge(pet.age) }} · {{ pet.sex || '性别待补充' }}</p>
                <span v-if="formatTags(pet)" class="directory-tag-line">
                  <Icon icon="mdi:tag-heart-outline" />{{ formatTags(pet) }}
                </span>
              </div>
              <span class="directory-type">{{ pet.type || '宠物' }}</span>
            </div>

            <p class="directory-desc">{{ pet.description || '救助站正在完善它的故事与性格描述。' }}</p>

            <div class="directory-meta">
              <span><Icon icon="mdi:map-marker-radius-outline" />{{ formatLocation(pet) }}</span>
              <span><Icon icon="mdi:account-heart-outline" />{{ pet.username || '暖窝救助站' }}</span>
            </div>

            <el-button text type="warning" class="card-link" @click="openPetProfile(pet.id)">
              查看宠物档案
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </article>

        <el-empty v-if="!loading && filteredPets.length === 0" description="没有找到合适的领养档案" />
      </section>
    </main>

    <el-dialog v-model="filterDialogVisible" title="筛选条件" width="680px">
      <div class="filter-panel pet-directory-filter-panel">
        <div class="pet-filter-row pet-filter-row-primary">
          <el-select v-model="filters.type" class="filter-field-sm" placeholder="宠物类型" clearable filterable @change="handleFilterTypeChange">
            <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="filters.breed" class="filter-field-sm" placeholder="品种" clearable filterable :disabled="!activeType">
            <el-option v-for="item in breedOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="filters.sex" class="filter-field-sm" placeholder="性别" clearable>
            <el-option label="未知" value="未知" />
            <el-option label="公" value="公" />
            <el-option label="母" value="母" />
          </el-select>
          <div class="pet-age-range filter-field-lg">
            <el-input-number
              v-model="filters.age0"
              :min="0"
              :controls="false"
              placeholder="最小月龄"
            />
            <span>至</span>
            <el-input-number
              v-model="filters.age1"
              :min="hasValue(filters.age0) ? Number(filters.age0) : 0"
              :controls="false"
              placeholder="最大月龄"
            />
          </div>
          <el-input v-model="filters.name" class="filter-field-md" placeholder="名称" clearable />
        </div>

        <div class="pet-filter-row pet-filter-row-secondary">
          <div class="pet-cascader-group pet-cascader-group-3 filter-field-lg">
            <el-select v-model="filters.province" placeholder="省" clearable filterable @change="handleFilterProvinceChange">
              <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <el-select v-model="filters.city" placeholder="市" clearable filterable :disabled="!filters.province" @change="handleFilterCityChange">
              <el-option v-for="item in cityOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <el-select v-model="filters.district" placeholder="县 / 县级市" clearable filterable :disabled="!filters.city">
              <el-option v-for="item in districtOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </div>
          <el-input v-model="filters.address" class="filter-field-xl" placeholder="地址" clearable />
        </div>
      </div>

      <template #footer>
        <el-button @click="resetFilters">重置</el-button>
        <el-button class="warm-btn" :icon="RefreshRight" @click="applyFilters">应用筛选</el-button>
      </template>
    </el-dialog>

    <AppFooter />
  </div>
</template>
