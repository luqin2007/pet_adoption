<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Icon } from '@iconify/vue'
import { Search } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
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
  address: '',
  lostDate: '',
})

const {
  ensureInformationCatalog,
  ensureCityOptions,
  provinceOptions,
  typeOptions,
  getCityOptions,
  getBreedOptions,
} = useInformationCatalog()

const cityOptions = computed(() => getCityOptions(searchForm.province))
const breedOptions = computed(() => getBreedOptions(searchForm.type))
const visibleRecords = computed(() => lostPets.value.filter((item) => item.status === 'SEARCHING'))

function handleTypeChange() {
  searchForm.breed = ''
}

function handleProvinceChange() {
  searchForm.city = ''
  if (searchForm.province) {
    ensureCityOptions(searchForm.province)
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
    address: searchForm.address.trim() || undefined,
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
  if (!id) {
    return
  }
  router.push(`/lost/${id}`)
}

function formatDate(value) {
  if (!value) {
    return '时间待补充'
  }
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
      <section class="filter-panel pet-directory-filter-panel">
        <div class="pet-filter-row lost-filter-grid-top">
          <el-input v-model="searchForm.name" placeholder="宠物名称" clearable />
          <el-select v-model="searchForm.type" placeholder="宠物类型" clearable filterable @change="handleTypeChange">
            <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="searchForm.breed" placeholder="品种" clearable filterable :disabled="!searchForm.type">
            <el-option v-for="item in breedOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="searchForm.province" placeholder="省份" clearable filterable @change="handleProvinceChange">
            <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="searchForm.city" placeholder="城市" clearable filterable :disabled="!searchForm.province">
            <el-option v-for="item in cityOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </div>

        <div class="pet-filter-row lost-filter-grid-bottom">
          <el-date-picker
            v-model="searchForm.lostDate"
            class="lost-filter-date"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="走失日期"
          />
          <el-input v-model="searchForm.address" class="lost-filter-address" placeholder="详细地点" clearable />
          <div class="pet-filter-action lost-filter-submit">
            <el-button class="warm-btn" :icon="Search" @click="handleSearch">搜索</el-button>
          </div>
        </div>
      </section>

      <section class="lost-grid" v-loading="loading">
        <article
          v-for="item in visibleRecords"
          :key="item.id"
          class="lost-card"
          tabindex="0"
          role="button"
          @click="openLostPetDetail(item.id)"
          @keyup.enter="openLostPetDetail(item.id)"
          @keyup.space="openLostPetDetail(item.id)"
        >
          <div class="lost-cover">
            <img v-if="item.petCover" :src="item.petCover" :alt="item.name" loading="lazy" />
            <div v-else class="lost-cover-placeholder">暂无图片</div>
          </div>
          <div class="lost-body">
            <div class="directory-head">
              <div>
                <h3>{{ item.name }}</h3>
                <p>{{ item.type }} · {{ item.breed || '品种待补充' }} · {{ item.sex }}</p>
              </div>
              <span class="directory-type">{{ formatDate(item.lostTime) }}</span>
            </div>

            <p class="directory-desc">{{ item.description || '走失经过待补充' }}</p>

            <div class="volunteer-meta">
              <span><Icon icon="mdi:map-marker-radius-outline" />{{ item.location?.city }} {{ item.location?.detailAddress }}</span>
              <span v-if="item.features"><Icon icon="mdi:star-four-points-outline" />{{ item.features }}</span>
              <span><Icon icon="mdi:phone-outline" />{{ item.ownerName }} · {{ item.contactPhone }}</span>
            </div>
          </div>
        </article>
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
