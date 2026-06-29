<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Icon } from '@iconify/vue'
import { Search } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { getArticles } from '../api/article'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const articles = ref([])
const activeType = ref('ALL')

const draftSearch = reactive({
  title: '',
  timeRange: [],
})

const appliedSearch = reactive({
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

    const titleKeyword = appliedSearch.title.trim().toLowerCase()
    const titleText = String(article.title || '').toLowerCase()
    const publishDate = article.publishTime ? String(article.publishTime).slice(0, 19) : ''
    const [time0, time1] = appliedSearch.timeRange || []

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
      title: appliedSearch.title.trim() || undefined,
      status: ['PUBLISHED'],
      time0: appliedSearch.timeRange?.[0],
      time1: appliedSearch.timeRange?.[1],
      isDiscard: false,
    })
    articles.value = Array.isArray(result?.records) ? result.records : []
  } catch {
    articles.value = []
  } finally {
    loading.value = false
  }
}

function applySearch() {
  appliedSearch.title = draftSearch.title
  appliedSearch.timeRange = Array.isArray(draftSearch.timeRange) ? [...draftSearch.timeRange] : []
  loadArticles()
}

function openArticle(article) {
  if (!article?.id) {
    return
  }
  router.push(`/articles/${article.id}`)
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
  const queryType = route.query.type
  if (queryType && tabOptions.some((t) => t.value === queryType)) {
    activeType.value = queryType
  }
  loadArticles()
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main">
      <section class="directory-hero article-hero-simple">
        <div>
          <h1>公益文章</h1>
        </div>
      </section>

      <section class="filter-panel pet-directory-filter-panel article-directory-filter-panel">
        <div class="pet-filter-row article-filter-row-inline">
          <el-input class="article-filter-title filter-field-md" v-model="draftSearch.title" clearable placeholder="标题" @keyup.enter="applySearch" />
          <el-date-picker
            v-model="draftSearch.timeRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            class="article-filter-date filter-field-lg"
          />
          <div class="pet-filter-action article-filter-action">
            <el-button class="warm-btn" :icon="Search" :loading="loading" @click="applySearch">搜索</el-button>
          </div>
        </div>
      </section>

      <section class="article-category-panel" aria-label="文章分类">
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
      </section>

      <section class="article-hub-grid" v-loading="loading">
        <article
          v-for="article in visibleArticles"
          :key="article.id"
          class="hub-card article-list-card"
          tabindex="0"
          role="button"
          @click="openArticle(article)"
          @keyup.enter="openArticle(article)"
        >
          <div class="hub-card-cover article-list-cover">
            <img v-if="article.cover" :src="article.cover" :alt="article.title" loading="lazy" />
            <div v-else class="hub-card-cover-placeholder article-list-cover-placeholder">暂无封面</div>
          </div>
          <div class="hub-card-body article-list-body">
            <div class="hub-card-head article-list-head">
              <el-tag type="warning" effect="plain">{{ typeText(article.type) }}</el-tag>
              <span>{{ formatDate(article.publishTime) }}</span>
            </div>
            <h3>{{ article.title }}</h3>
            <div class="article-list-meta">
              <span>{{ article.authorName || '匿名作者' }}</span>
              <span><Icon icon="mdi:eye-outline" />{{ article.viewCount || 0 }} 次浏览</span>
              <span><Icon icon="mdi:thumb-up-outline" />{{ article.likeCount || 0 }}</span>
              <span><Icon icon="mdi:share-variant-outline" />{{ article.shareCount || 0 }}</span>
            </div>
          </div>
        </article>
        <el-empty v-if="!loading && visibleArticles.length === 0" class="grid-empty" description="没有找到相关内容" />
      </section>
    </main>

    <AppFooter />
  </div>
</template>
