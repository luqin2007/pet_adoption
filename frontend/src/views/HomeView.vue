<script setup>
import { computed, onMounted, ref } from 'vue'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import AdoptionSection from '../components/AdoptionSection.vue'
import ActivitySection from '../components/ActivitySection.vue'
import FeaturedArticlesSection from '../components/FeaturedArticlesSection.vue'
import HeroSection from '../components/HeroSection.vue'
import StatsOverview from '../components/StatsOverview.vue'
import { getPets } from '../api/pets'
import { getArticles } from '../api/article'
import { getRescueTaskCount } from '../api/rescue'
import { getRecruitments } from '../api/volunteer'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const loading = ref(false)
const pets = ref([])
const activities = ref([])
const articles = ref([])
const overview = ref({
  pets: 0,
  tasks: 0,
  recruitments: 0,
  articles: 0,
})

const stats = computed(() => [
  { label: '流浪宠物档案', value: overview.value.pets, suffix: '只' },
  { label: '救助任务', value: overview.value.tasks, suffix: '项' },
  { label: '开放招募', value: overview.value.recruitments, suffix: '项' },
  { label: '公益内容', value: overview.value.articles, suffix: '篇' },
])

function scrollToSection(id) {
  const target = document.getElementById(id)
  if (target) {
    target.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

function formatPetAge(age) {
  if (!age && age !== 0) {
    return '年龄待补充'
  }
  return `约 ${age} 个月`
}

function petStatusText(status) {
  return status || '待完善'
}

function articleCategory(type) {
  if (type === 'ACTIVITY') return '近期活动'
  if (type === 'KNOWLEDGE') return '养护知识'
  if (type === 'STORY') return '救助故事'
  return '公益内容'
}

function articleIcon(type) {
  if (type === 'ACTIVITY') return 'mdi:calendar-clock-outline'
  if (type === 'KNOWLEDGE') return 'mdi:book-open-page-variant-outline'
  if (type === 'STORY') return 'mdi:heart-circle-outline'
  return 'mdi:bullhorn-variant-outline'
}

async function loadHomeData() {
  loading.value = true
  try {
    const [petResult, taskResult, recruitmentResult, activityResult, articleResult] = await Promise.allSettled([
      getPets({ size: 9, status: ['SHELTERED', 'HEALTH'] }),
      getRescueTaskCount(),
      getRecruitments({ size: 1, status: ['PUBLISHED'] }),
      getArticles({ size: 8, type: 'ACTIVITY' }),
      getArticles({ size: 3 }),
    ])

    const petRecords = petResult.status === 'fulfilled' && Array.isArray(petResult.value?.records) ? petResult.value.records : []
    const activityRecords =
      activityResult.status === 'fulfilled' && Array.isArray(activityResult.value?.records) ? activityResult.value.records : []
    const articleRecords =
      articleResult.status === 'fulfilled' && Array.isArray(articleResult.value?.records) ? articleResult.value.records : []

    pets.value = petRecords.map((pet) => ({
        id: pet.id,
        name: pet.name || '未命名',
        age: formatPetAge(pet.age),
        city: [pet.locations?.[0]?.city, pet.locations?.[0]?.district].filter(Boolean).join(' · ') || '位置待补充',
        summary: pet.description || pet.health || '救助站正在完善它的故事与健康档案。',
        status: petStatusText(pet.status),
        cover: pet.cover,
      }))

    activities.value = activityRecords.map((item) => ({
      id: item.id,
      date: String(item.publishTime || '').slice(0, 10),
      title: item.title || '未命名活动',
      location: '查看详情了解活动安排',
      summary: item.content || '活动说明待补充',
      type: articleCategory(item.type),
      cover: item.cover || '',
    }))

    articles.value = articleRecords
      .filter((item) => item.type !== 'ACTIVITY')
      .slice(0, 3)
      .map((item) => ({
        id: item.id,
        category: articleCategory(item.type),
        title: item.title || '未命名内容',
        desc: item.content || '内容摘要待补充',
        date: String(item.publishTime || '').slice(0, 10),
        reading: `${Math.max(1, Math.ceil(String(item.content || '').length / 120))} 分钟阅读`,
        icon: articleIcon(item.type),
        cover: item.cover || '',
        source: '公益内容库',
      }))

    overview.value = {
      pets: petResult.status === 'fulfilled' ? Number(petResult.value?.total || petRecords.length || 0) : 0,
      tasks: taskResult.status === 'fulfilled' ? Number(taskResult.value || 0) : 0,
      recruitments:
        recruitmentResult.status === 'fulfilled'
          ? Number(recruitmentResult.value?.total || recruitmentResult.value?.records?.length || 0)
          : 0,
      articles: articleResult.status === 'fulfilled' ? Number(articleResult.value?.total || articleRecords.length || 0) : 0,
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadHomeData()
})
</script>

<template>
  <div class="home-page">
    <AppHeader :nav-items="navItems" @navigate="scrollToSection" />

    <main>
      <HeroSection @navigate="scrollToSection" />
      <StatsOverview :stats="stats" />
      <AdoptionSection :pets="pets" :loading="loading" />
      <ActivitySection :activities="activities" :loading="loading" />
      <FeaturedArticlesSection :articles="articles" :loading="loading" />
    </main>

    <AppFooter />
  </div>
</template>
