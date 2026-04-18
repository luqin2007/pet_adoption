<script setup>
import { computed, onMounted, ref } from 'vue'
import { Icon } from '@iconify/vue'
import { RefreshRight } from '@element-plus/icons-vue'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { getRecruitments } from '../api/volunteer'

const navItems = [
  { id: 'home', label: '首页', to: '/' },
  { id: 'volunteers', label: '志愿者中心' },
  { id: 'articles', label: '公益文章', to: '/articles' },
  { id: 'console', label: '个人空间', to: '/console' },
]

const loading = ref(false)
const cityFilter = ref('')
const recruitments = ref([])

const fallbackRecruitments = [
  {
    id: 1,
    title: '周末巡护志愿者招募',
    description: '协助完成夜巡、拍照、定位和现场记录。',
    requirement: '有责任心，能接受夜间巡护优先。',
    serviceAddress: '滨江区四个巡护点',
    city: '杭州市',
    district: '滨江区',
    headcount: 20,
    appliedCount: 11,
    publisherName: '暖窝排班组',
    startTime: '2026-04-20',
    endTime: '2026-05-20',
  },
  {
    id: 2,
    title: '开放日志愿接待员',
    description: '在领养开放日协助接待、签到、介绍宠物背景资料。',
    requirement: '具备基础沟通能力，喜欢和人交流。',
    serviceAddress: '西湖区救助中心',
    city: '杭州市',
    district: '西湖区',
    headcount: 12,
    appliedCount: 6,
    publisherName: '领养服务组',
    startTime: '2026-04-25',
    endTime: '2026-05-08',
  },
]

const filteredRecruitments = computed(() =>
  recruitments.value.filter((item) => {
    if (!cityFilter.value) {
      return true
    }
    return [item.city, item.district, item.serviceAddress].join('').includes(cityFilter.value)
  }),
)

async function loadRecruitments() {
  loading.value = true
  try {
    const result = await getRecruitments({
      size: 9,
      city: cityFilter.value || undefined,
      status: ['PUBLISHED'],
    })
    recruitments.value = result?.records?.length ? result.records : fallbackRecruitments
  } catch {
    recruitments.value = fallbackRecruitments
  } finally {
    loading.value = false
  }
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

onMounted(() => {
  loadRecruitments()
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main">
      <section class="volunteer-hero">
        <div class="content-hero-copy">
          <span class="hero-chip">志愿者中心</span>
          <h1>让每一次巡护、接待、记录和回访都有人接力</h1>
          <p>
            这一页对接后端 `volunteers/recruitments` 招募计划接口，用统一风格展示招募信息、报名热度和服务地点，方便后续继续补申请提交流程。
          </p>
        </div>
        <div class="volunteer-sidecard">
          <div>
            <strong>{{ filteredRecruitments.length }}</strong>
            <span>当前开放中的志愿者招募</span>
          </div>
          <ul>
            <li>夜巡与救助陪护</li>
            <li>领养开放日接待</li>
            <li>档案记录与回访协助</li>
          </ul>
        </div>
      </section>

      <section class="filter-panel volunteer-filter-panel">
        <el-input v-model="cityFilter" placeholder="按城市或服务地点筛选" clearable />
        <el-button class="warm-btn" :icon="RefreshRight" @click="loadRecruitments">刷新招募</el-button>
      </section>

      <section class="volunteer-grid" v-loading="loading">
        <article v-for="item in filteredRecruitments" :key="item.id" class="hub-card volunteer-card">
          <div class="hub-card-body">
            <div class="hub-card-head">
              <el-tag type="success" effect="plain">招募中</el-tag>
              <span>{{ progressText(item) }}</span>
            </div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.description }}</p>
            <div class="volunteer-meta">
              <span><Icon icon="mdi:map-marker-radius-outline" />{{ item.serviceAddress }}</span>
              <span><Icon icon="mdi:calendar-range" />{{ formatDate(item.startTime) }} - {{ formatDate(item.endTime) }}</span>
              <span><Icon icon="mdi:account-supervisor-outline" />发布：{{ item.publisherName || '暖窝志愿组' }}</span>
            </div>
            <div class="volunteer-requirement">
              <strong>参与要求</strong>
              <p>{{ item.requirement || '请保持沟通及时、服从排班安排。' }}</p>
            </div>
            <el-button class="soft-btn" plain>后续接申请页</el-button>
          </div>
        </article>
      </section>
    </main>

    <AppFooter />
  </div>
</template>
