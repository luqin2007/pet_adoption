<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { getArticleById } from '../api/publicity'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const article = ref(null)

const articleId = computed(() => String(route.params.id || ''))
const paragraphs = computed(() =>
  String(article.value?.content || '')
    .split(/\n+/)
    .map((item) => item.trim())
    .filter(Boolean),
)

function goBack() {
  router.push('/articles')
}

function typeText(type) {
  const map = {
    STORY: '救助故事',
    ACTIVITY: '近期活动',
    KNOWLEDGE: '养护知识',
  }
  return map[type] || '公益内容'
}

function formatDate(value) {
  if (!value) {
    return '待发布'
  }
  return String(value).slice(0, 10)
}

async function loadArticle() {
  if (!articleId.value) {
    article.value = null
    return
  }

  loading.value = true
  try {
    article.value = await getArticleById(articleId.value)
  } catch {
    article.value = null
  } finally {
    loading.value = false
  }
}

watch(
  () => articleId.value,
  () => {
    loadArticle()
  },
)

onMounted(() => {
  loadArticle()
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main article-detail-page" v-loading="loading">
      <section v-if="article" class="article-detail-hero" :class="{ 'article-detail-hero-no-cover': !article.cover }">
        <div class="article-detail-hero-copy">
          <el-button class="soft-btn article-detail-back" :icon="ArrowLeft" @click="goBack">返回公益文章</el-button>
          <div class="article-detail-kicker">
            <el-tag type="warning" effect="plain">{{ typeText(article.type) }}</el-tag>
            <span>{{ formatDate(article.publishTime || article.createTime) }}</span>
          </div>
          <h1>{{ article.title || '未命名内容' }}</h1>
          <div class="article-detail-meta">
            <span><Icon icon="mdi:account-edit-outline" />{{ article.authorName || '匿名作者' }}</span>
            <span><Icon icon="mdi:eye-outline" />{{ article.viewCount || 0 }} 次浏览</span>
            <span><Icon icon="mdi:thumb-up-outline" />{{ article.likeCount || 0 }} 次点赞</span>
            <span><Icon icon="mdi:share-variant-outline" />{{ article.shareCount || 0 }} 次分享</span>
          </div>
        </div>

        <div v-if="article.cover" class="article-detail-cover">
          <img :src="article.cover" :alt="article.title || '文章封面'" loading="lazy" />
        </div>
      </section>

      <section v-if="article" class="article-detail-content">
        <p v-if="paragraphs.length === 0">正文内容正在整理。</p>
        <p v-for="(paragraph, index) in paragraphs" :key="index">{{ paragraph }}</p>
      </section>

      <section v-else-if="!loading" class="pet-profile-empty">
        <el-empty description="未找到对应的文章">
          <el-button class="warm-btn" @click="goBack">返回公益文章</el-button>
        </el-empty>
      </section>
    </main>

    <AppFooter />
  </div>
</template>
