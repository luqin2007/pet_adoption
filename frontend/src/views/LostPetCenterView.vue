<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import PetCard from '../components/PetCard.vue'
import { getLostPets } from '../api/lost'
import { useInformationCatalog } from '../composables/useInformationCatalog'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const router = useRouter()
const loading = ref(false)
const lostPets = ref([])
const total = ref(0)
const page = reactive({
  page: 1,
  size: 12,
})

const searchForm = reactive({
  name: '',
  type: '',
  breed: '',
  province: '',
  city: '',
  district: '',
  lostDate: '',
})

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

const cityOptions = computed(() => getCityOptions(searchForm.province))
const districtOptions = computed(() => getDistrictOptions(searchForm.province, searchForm.city))
const breedOptions = computed(() => getBreedOptions(searchForm.type))
const visibleRecords = computed(() => lostPets.value.filter((item) => item.status === 'SEARCHING'))

const petLikeRecords = computed(() =>
  visibleRecords.value.map((item) => ({
    id: item.id,
    cover: item.petCover,
    name: item.name,
    type: item.type,
    breed: item.breed,
    sex: item.sex,
    age: item.age,
    description: item.description,
    tags: item.features ? [{ tag: item.features }] : [],
    locations: item.location ? [item.location] : [],
    username: item.ownerName,
    _lostTime: item.lostTime,
  })),
)

function handleTypeChange() {
  searchForm.breed = ''
}

function handleProvinceChange() {
  searchForm.city = ''
  searchForm.district = ''
  if (searchForm.province) {
    ensureCityOptions(searchForm.province)
  }
}

function handleCityChange() {
  searchForm.district = ''
  if (searchForm.province && searchForm.city) {
    ensureDistrictOptions(searchForm.province, searchForm.city)
  }
}

function buildQuery() {
  return {
    page: page.page,
    size: page.size,
    status: ['SEARCHING'],
    name: searchForm.name.trim() || undefined,
    type: searchForm.type ? [searchForm.type] : undefined,
    bread: searchForm.breed ? [searchForm.breed] : undefined,
    province: searchForm.province || undefined,
    city: searchForm.city || undefined,
    district: searchForm.district || undefined,
    time0: searchForm.lostDate || undefined,
  }
}

async function loadLostPets() {
  loading.value = true
  try {
    const result = await getLostPets(buildQuery())
    lostPets.value = Array.isArray(result?.records) ? result.records : []
    total.value = Number(result?.total || 0)
  } catch {
    lostPets.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.page = 1
  loadLostPets()
}

function changePage(nextPage) {
  page.page = nextPage
  loadLostPets()
}

function openLostPetDetail(id) {
  if (!id) return
  router.push(`/lost/${id}`)
}

function formatDate(value) {
  if (!value) return '时间待补充'
  return String(value).slice(0, 10)
}

onMounted(async () => {
  await ensureInformationCatalog()
  loadLostPets()
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main">
      <section class="directory-hero">
        <div>
          <h1>走失宠物</h1>
        </div>
      </section>

      <section class="filter-panel pet-directory-filter-panel">
        <div class="pet-filter-row lost-filter-grid-top">
          <el-input v-model="searchForm.name" class="filter-field-md" placeholder="宠物名称" clearable />
          <el-select v-model="searchForm.type" class="filter-field-sm" placeholder="宠物类型" clearable filterable @change="handleTypeChange">
            <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="searchForm.breed" class="filter-field-sm" placeholder="品种" clearable filterable :disabled="!searchForm.type">
            <el-option v-for="item in breedOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </div>

        <div class="pet-filter-row lost-filter-grid-bottom">
          <div class="pet-cascader-group pet-cascader-group-3 filter-field-lg">
            <el-select v-model="searchForm.province" placeholder="省份" clearable filterable @change="handleProvinceChange">
              <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <el-select v-model="searchForm.city" placeholder="城市" clearable filterable :disabled="!searchForm.province" @change="handleCityChange">
              <el-option v-for="item in cityOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <el-select v-model="searchForm.district" placeholder="区县" clearable filterable :disabled="!searchForm.city">
              <el-option v-for="item in districtOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </div>
          <el-date-picker
            v-model="searchForm.lostDate"
            class="filter-field-md"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="走失日期"
          />
          <div class="pet-filter-action">
            <el-button class="warm-btn" :icon="Search" @click="handleSearch">搜索</el-button>
          </div>
        </div>
      </section>

      <section class="directory-grid" v-loading="loading">
        <PetCard
          v-for="item in petLikeRecords" :key="item.id"
          :pet="item"
          :badge-text="formatDate(item._lostTime)"
          @click="openLostPetDetail(item.id)"
        />
        <el-empty
          v-if="!loading && visibleRecords.length === 0"
          class="grid-empty"
          description="没有找到相关走失记录"
        />
      </section>

      <div class="user-admin-pagination">
        <el-pagination
          layout="prev, pager, next, total"
          :current-page="page.page"
          :page-size="page.size"
          :total="total"
          @current-change="changePage"
        />
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<style scoped>
</style>
