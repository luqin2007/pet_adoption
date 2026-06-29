<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Calendar, Location, Search, UserFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { useInformationCatalog } from '../composables/useInformationCatalog'
import { getRecruitments } from '../api/volunteer'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const router = useRouter()
const loading = ref(false)
const recruitments = ref([])
const page = reactive({ page: 1, size: 9 })
const searchForm = reactive({
  title: '',
  province: '',
  city: '',
  status: 'PUBLISHED',
  timeRange: [],
})

const {
  ensureInformationCatalog,
  ensureCityOptions,
  provinceOptions,
  getCityOptions,
} = useInformationCatalog()

const cityOptions = computed(() => getCityOptions(searchForm.province))

const filteredRecruitments = computed(() =>
  recruitments.value,
)

function handleProvinceChange() {
  searchForm.city = ''
  if (searchForm.province) {
    ensureCityOptions(searchForm.province)
  }
}

function buildQuery() {
  const [time0, time1] = searchForm.timeRange || []
  return {
    page: page.page,
    size: page.size,
    title: searchForm.title.trim() || undefined,
    province: searchForm.province || undefined,
    city: searchForm.city || undefined,
    status: searchForm.status ? [searchForm.status] : undefined,
    time0: time0 || undefined,
    time1: time1 || undefined,
  }
}

async function loadRecruitments() {
  loading.value = true
  try {
    const result = await getRecruitments(buildQuery())
    recruitments.value = Array.isArray(result?.records) ? result.records : []
  } catch {
    recruitments.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.page = 1
  loadRecruitments()
}

function formatDate(value) {
  if (!value) {
    return '待补充'
  }
  return String(value).slice(0, 10)
}

function progressText(item) {
  const total = Number(item.headcount || 0)
  const current = Number(item.appliedCount || 0)
  if (!total) {
    return '名额待确认'
  }
  return `${current}/${total} 已报名`
}

function openRecruitmentDetail(id) {
  if (!id) {
    return
  }
  router.push(`/volunteers/recruitments/${id}`)
}

onMounted(() => {
  ensureInformationCatalog()
  loadRecruitments()
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main">
      <section class="directory-hero volunteer-directory-hero">
        <div>
          <h1>志愿者中心</h1>
        </div>
        <div class="directory-hero-side">
          <span class="directory-hero-count">{{ filteredRecruitments.length }} 个当前开放招募</span>
        </div>
      </section>

      <section class="filter-panel pet-directory-filter-panel volunteer-directory-filter-panel">
        <div class="pet-filter-row volunteer-filter-row-primary">
          <el-input
            v-model="searchForm.title"
            class="filter-field-md"
            placeholder="招募标题"
            clearable
            @keyup.enter="handleSearch"
          />
          <div class="pet-cascader-group pet-cascader-group-2 filter-field-md">
            <el-select v-model="searchForm.province" placeholder="省份" clearable filterable @change="handleProvinceChange">
              <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <el-select v-model="searchForm.city" placeholder="城市" clearable filterable :disabled="!searchForm.province">
              <el-option v-for="item in cityOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </div>
          <el-select v-model="searchForm.status" class="filter-field-sm" placeholder="状态" clearable>
            <el-option label="招募中" value="PUBLISHED" />
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </div>
        <div class="pet-filter-row volunteer-filter-row-secondary">
          <el-date-picker
            v-model="searchForm.timeRange"
            type="daterange"
            value-format="YYYY-MM-DD HH:mm:ss"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            class="filter-field-lg volunteer-filter-date"
          />
          <div class="pet-filter-action volunteer-filter-action">
            <el-button class="warm-btn" :icon="Search" @click="handleSearch">搜索</el-button>
          </div>
        </div>
      </section>

      <section class="volunteer-grid" v-loading="loading">
        <article
          v-for="item in filteredRecruitments"
          :key="item.id"
          class="hub-card volunteer-card volunteer-card-compact"
          tabindex="0"
          role="button"
          @click="openRecruitmentDetail(item.id)"
          @keyup.enter="openRecruitmentDetail(item.id)"
        >
          <div class="hub-card-body">
            <div class="hub-card-head">
              <el-tag type="success" effect="plain">招募中</el-tag>
              <span>{{ progressText(item) }}</span>
            </div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.description }}</p>
            <div class="volunteer-meta">
              <span><el-icon><Location /></el-icon>{{ item.serviceAddress }}</span>
              <span><el-icon><Calendar /></el-icon>{{ formatDate(item.startTime) }} - {{ formatDate(item.endTime) }}</span>
              <span><el-icon><UserFilled /></el-icon>{{ progressText(item) }}</span>
            </div>
            <div class="volunteer-card-footer">
              <span>查看招募详情并申请</span>
            </div>
          </div>
        </article>
        <el-empty
          v-if="!loading && filteredRecruitments.length === 0"
          class="grid-empty"
          description="暂时没有开放招募"
        />
      </section>
    </main>

    <AppFooter />
  </div>
</template>
