<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Icon } from '@iconify/vue'
import { ArrowRight, Search } from '@element-plus/icons-vue'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { getArticles } from '../api/publicity'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const loading = ref(false)
const articles = ref([])
const activeType = ref('ALL')

const draftSearch = reactive({
  author: '',
  title: '',
  timeRange: [],
})

const appliedSearch = reactive({
  author: '',
  title: '',
  timeRange: [],
})

const tabOptions = [
  { label: '全部内容', value: 'ALL' },
  { label: '救助故事', value: 'STORY' },
  { label: '近期活动', value: 'ACTIVITY' },
  { label: '养护知识', value: 'KNOWLEDGE' },
]

const visibleArticles = computed(() =>
  articles.value.filter((article) => {
    if (activeType.value !== 'ALL' && article.type !== activeType.value) {
      return false
    }

    const authorKeyword = appliedSearch.author.trim().toLowerCase()
    const titleKeyword = appliedSearch.title.trim().toLowerCase()
    const authorText = String(article.authorName || '').toLowerCase()
    const titleText = String(article.title || '').toLowerCase()
    const publishDate = article.publishTime ? String(article.publishTime).slice(0, 19) : ''
    const [time0, time1] = appliedSearch.timeRange || []

    if (authorKeyword && !authorText.includes(authorKeyword)) {
      return false
    }
    if (titleKeyword && !titleText.includes(titleKeyword)) {
      return false
    }
    if (time0 && publishDate && publishDate < time0) {
      return false
    }
    if (time1 && publishDate && publishDate > time1) {
      return false
    }
    return !(time0 || time1) || Boolean(publishDate)
  }),
)

async function loadArticles() {
  loading.value = true
  try {
    const result = await getArticles({
      size: 60,
    })
    articles.value = Array.isArray(result?.records) ? result.records : []
  } catch {
    articles.value = []
  } finally {
    loading.value = false
  }
}

function applySearch() {
  appliedSearch.author = draftSearch.author
  appliedSearch.title = draftSearch.title
  appliedSearch.timeRange = Array.isArray(draftSearch.timeRange) ? [...draftSearch.timeRange] : []
}

function typeText(type) {
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
      <section class="content-hero article-hero-simple">
        <div class="content-hero-copy article-hero-copy-full">
          <span class="hero-chip">公益中心</span>
          <h1>把一线救助经验，变成看得见也读得懂的内容</h1>
          <p>这里收集救助故事、活动推广和养护知识，让每一次现场经验都能继续被看见、被接力。</p>
        </div>
      </section>

      <section class="filter-panel article-filter-panel-v2">
        <div class="article-type-tabs" role="tablist" aria-label="文章分类">
          <button
            v-for="item in tabOptions"
            :key="item.value"
            type="button"
            class="article-type-tab"
            :class="{ 'is-active': activeType === item.value }"
            @click="activeType = item.value"
          >
            {{ item.label }}
          </button>
        </div>

        <div class="article-public-search">
          <el-input v-model="draftSearch.author" clearable placeholder="作者" @keyup.enter="applySearch" />
          <el-input v-model="draftSearch.title" clearable placeholder="标题" @keyup.enter="applySearch" />
          <el-date-picker
            v-model="draftSearch.timeRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            class="full-width-control"
          />
          <el-button class="soft-btn article-search-btn" :icon="Search" :loading="loading" @click="applySearch">搜索</el-button>
        </div>
      </section>

      <section class="article-hub-grid" v-loading="loading">
        <article v-for="article in visibleArticles" :key="article.id" class="hub-card article-list-card">
          <div class="hub-card-body article-list-body">
            <div class="hub-card-head article-list-head">
              <el-tag type="warning" effect="plain">{{ typeText(article.type) }}</el-tag>
              <span>{{ formatDate(article.publishTime) }}</span>
            </div>
            <h3>{{ article.title }}</h3>
            <p>{{ article.content }}</p>
            <div class="article-list-meta">
              <span>{{ article.authorName || '匿名作者' }}</span>
              <span><Icon icon="mdi:eye-outline" />{{ article.viewCount || 0 }} 次浏览</span>
              <span><Icon icon="mdi:thumb-up-outline" />{{ article.likeCount || 0 }}</span>
              <span><Icon icon="mdi:share-variant-outline" />{{ article.shareCount || 0 }}</span>
            </div>
            <el-button text type="warning" class="card-link">
              阅读专题
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </article>
        <el-empty v-if="!loading && visibleArticles.length === 0" description="当前筛选下暂无内容" />
      </section>
    </main>

    <AppFooter />
  </div>
</template>
