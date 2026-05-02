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
      <div class="content-hero-copy volunteer-hero-copy-full">
        <h1>志愿者中心</h1>
      </div>

      <section class="filter-panel volunteer-filter-panel">
        <el-form label-position="top" class="console-filter-form">
          <div class="console-filter-grid console-filter-grid-5">
            <el-form-item label="招募标题" class="console-filter-span-2">
              <el-input v-model="searchForm.title" placeholder="输入招募标题关键词" clearable />
            </el-form-item>
            <el-form-item label="省份">
              <el-select v-model="searchForm.province" placeholder="省份" clearable filterable @change="handleProvinceChange">
                <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item label="城市">
              <el-select v-model="searchForm.city" placeholder="城市" clearable filterable :disabled="!searchForm.province">
                <el-option v-for="item in cityOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="状态" clearable>
                <el-option label="招募中" value="PUBLISHED" />
                <el-option label="草稿" value="DRAFT" />
                <el-option label="已关闭" value="CLOSED" />
              </el-select>
            </el-form-item>
          </div>
          <div class="console-filter-grid console-filter-grid-actions">
            <el-form-item label="招募开始时间" class="console-filter-span-2">
              <el-date-picker
                v-model="searchForm.timeRange"
                type="daterange"
                value-format="YYYY-MM-DD HH:mm:ss"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                class="full-width-control"
              />
            </el-form-item>
            <div class="console-filter-actions">
              <el-button class="warm-btn" :icon="Search" @click="handleSearch">搜索</el-button>
            </div>
          </div>
        </el-form>
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
              <el-button class="soft-btn" plain @click.stop="openRecruitmentDetail(item.id)">查看</el-button>
            </div>
          </div>
        </article>
        <el-empty v-if="!loading && filteredRecruitments.length === 0" description="当前还没有开放中的志愿招募" />
      </section>
    </main>

    <AppFooter />
  </div>
</template>
