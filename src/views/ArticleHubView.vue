<script setup>
import { computed, onMounted, ref } from 'vue'
import { Icon } from '@iconify/vue'
import { ArrowRight, RefreshRight } from '@element-plus/icons-vue'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { getArticles } from '../api/publicity'

const navItems = [
  { id: 'home', label: '首页', to: '/' },
  { id: 'articles', label: '公益文章' },
  { id: 'pets', label: '领养大厅', to: '/pets' },
  { id: 'console', label: '个人空间', to: '/console' },
]

const activeType = ref('ALL')
const loading = ref(false)
const articles = ref([])

const fallbackArticles = [
  {
    id: 1,
    type: 'STORY',
    title: '从街角到新家：姜糖的救助故事',
    content: '记录从发现、治疗到开放领养的全过程。',
    cover:
      'https://images.pexels.com/photos/6568500/pexels-photo-6568500.jpeg?auto=compress&cs=tinysrgb&w=1200',
    publishTime: '2026-03-12',
    likeCount: 36,
    shareCount: 18,
    viewCount: 120,
  },
  {
    id: 2,
    type: 'ACTIVITY',
    title: '春季领养开放日报名启动',
    content: '开放 30+ 待领养宠物档案，现场提供评估与喂养指导。',
    cover:
      'https://images.pexels.com/photos/4587991/pexels-photo-4587991.jpeg?auto=compress&cs=tinysrgb&w=1200',
    publishTime: '2026-03-16',
    likeCount: 20,
    shareCount: 12,
    viewCount: 86,
  },
  {
    id: 3,
    type: 'KNOWLEDGE',
    title: '新手领养前必须准备的 10 件事',
    content: '从疫苗、驱虫到空间隔离，把适应期准备一次讲清楚。',
    cover:
      'https://images.pexels.com/photos/6235233/pexels-photo-6235233.jpeg?auto=compress&cs=tinysrgb&w=1200',
    publishTime: '2026-03-18',
    likeCount: 42,
    shareCount: 25,
    viewCount: 140,
  },
]

const tabOptions = [
  { label: '全部内容', value: 'ALL' },
  { label: '救助故事', value: 'STORY' },
  { label: '近期活动', value: 'ACTIVITY' },
  { label: '养护知识', value: 'KNOWLEDGE' },
]

const visibleArticles = computed(() =>
  articles.value.filter((article) => activeType.value === 'ALL' || article.type === activeType.value),
)

const headlineArticle = computed(() => visibleArticles.value[0] || fallbackArticles[0])

async function loadArticles() {
  loading.value = true
  try {
    const result = await getArticles({
      size: 12,
      type: activeType.value === 'ALL' ? undefined : activeType.value,
    })
    articles.value = result?.records?.length ? result.records : fallbackArticles
  } catch {
    articles.value = fallbackArticles
  } finally {
    loading.value = false
  }
}

function getTypeLabel(type) {
  return tabOptions.find((item) => item.value === type)?.label || '公益内容'
}

function formatDate(value) {
  if (!value) {
    return '待发布'
  }
  return String(value).slice(0, 10)
}

onMounted(() => {
  loadArticles()
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main">
      <section class="content-hero">
        <div class="content-hero-copy">
          <span class="hero-chip">公益中心</span>
          <h1>把一线救助经验，变成看得见也读得懂的内容</h1>
          <p>
            这里聚合了后端 `publicity` 模块的故事、活动和科普内容，方便首页卡片、专题页和后续文章详情页共用同一批接口。
          </p>
        </div>

        <article class="content-highlight">
          <img :src="headlineArticle.cover || fallbackArticles[0].cover" :alt="headlineArticle.title" loading="lazy" />
          <div class="content-highlight-body">
            <el-tag type="warning" effect="dark">{{ getTypeLabel(headlineArticle.type) }}</el-tag>
            <h2>{{ headlineArticle.title }}</h2>
            <p>{{ headlineArticle.content }}</p>
            <div class="content-highlight-meta">
              <span><Icon icon="mdi:calendar-range" />{{ formatDate(headlineArticle.publishTime) }}</span>
              <span><Icon icon="mdi:eye-outline" />{{ headlineArticle.viewCount || 0 }} 次浏览</span>
            </div>
          </div>
        </article>
      </section>

      <section class="filter-panel article-filter-panel">
        <el-segmented v-model="activeType" :options="tabOptions" block />
        <el-button class="warm-btn" :icon="RefreshRight" @click="loadArticles">刷新内容</el-button>
      </section>

      <section class="article-hub-grid" v-loading="loading">
        <article v-for="article in visibleArticles" :key="article.id" class="hub-card">
          <div class="hub-card-cover">
            <img :src="article.cover || fallbackArticles[0].cover" :alt="article.title" loading="lazy" />
          </div>
          <div class="hub-card-body">
            <div class="hub-card-head">
              <el-tag type="warning" effect="plain">{{ getTypeLabel(article.type) }}</el-tag>
              <span>{{ formatDate(article.publishTime) }}</span>
            </div>
            <h3>{{ article.title }}</h3>
            <p>{{ article.content }}</p>
            <div class="hub-card-meta">
              <span><Icon icon="mdi:thumb-up-outline" />{{ article.likeCount || 0 }}</span>
              <span><Icon icon="mdi:share-variant-outline" />{{ article.shareCount || 0 }}</span>
              <span><Icon icon="mdi:eye-outline" />{{ article.viewCount || 0 }}</span>
            </div>
            <el-button text type="warning" class="card-link">
              阅读专题
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </article>
      </section>
    </main>

    <AppFooter />
  </div>
</template>
