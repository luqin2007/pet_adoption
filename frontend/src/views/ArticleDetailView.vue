<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { favoriteArticle, getArticleById, likeArticle, shareArticle, unfavoriteArticle, unlikeArticle } from '../api/article'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const liking = ref(false)
const favoriting = ref(false)
const sharing = ref(false)
const article = ref(null)

const articleId = computed(() => String(route.params.id || ''))
const isLoggedIn = computed(() => Boolean(userStore.accessToken))
const canInteractArticle = computed(() => article.value?.status === 'PUBLISHED')
const paragraphs = computed(() =>
  String(article.value?.content || '')
    .split(/\n+/)
    .map((item) => item.trim())
    .filter(Boolean),
)

function goBack() {
  router.push('/articles')
}

function requireLogin(actionText) {
  if (isLoggedIn.value) {
    return true
  }
  ElMessage.info(`请先登录后${actionText}`)
  router.push({ path: '/login', query: { redirect: route.fullPath } })
  return false
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

async function toggleLike() {
  if (!article.value?.id || liking.value || !requireLogin('点赞')) {
    return
  }
  const nextLiked = !article.value.liked
  liking.value = true
  try {
    if (nextLiked) {
      await likeArticle(article.value.id)
      article.value.likeCount = Number(article.value.likeCount || 0) + 1
    } else {
      await unlikeArticle(article.value.id)
      article.value.likeCount = Math.max(0, Number(article.value.likeCount || 0) - 1)
    }
    article.value.liked = nextLiked
    ElMessage.success(nextLiked ? '已点赞' : '已取消点赞')
  } catch (error) {
    ElMessage.warning(error?.message || '点赞操作失败')
    loadArticle()
  } finally {
    liking.value = false
  }
}

async function toggleFavorite() {
  if (!article.value?.id || favoriting.value || !requireLogin('收藏')) {
    return
  }
  const nextFavorited = !article.value.favorited
  favoriting.value = true
  try {
    if (nextFavorited) {
      await favoriteArticle(article.value.id)
    } else {
      await unfavoriteArticle(article.value.id)
    }
    article.value.favorited = nextFavorited
    ElMessage.success(nextFavorited ? '已收藏' : '已取消收藏')
  } catch (error) {
    ElMessage.warning(error?.message || '收藏操作失败')
    loadArticle()
  } finally {
    favoriting.value = false
  }
}

async function copyText(text) {
  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(text)
    return true
  }
  const input = document.createElement('textarea')
  input.value = text
  input.setAttribute('readonly', '')
  input.style.position = 'fixed'
  input.style.left = '-9999px'
  document.body.appendChild(input)
  input.select()
  const copied = document.execCommand('copy')
  document.body.removeChild(input)
  return copied
}

async function handleShare() {
  if (!article.value?.id || sharing.value) {
    return
  }
  sharing.value = true
  try {
    await shareArticle(article.value.id)
    article.value.shareCount = Number(article.value.shareCount || 0) + 1
    const shareUrl = `${window.location.origin}/articles/${article.value.id}`
    if (navigator.share) {
      try {
        await navigator.share({
          title: article.value.title || '公益文章',
          text: article.value.title || '这篇公益文章值得看看',
          url: shareUrl,
        })
        ElMessage.success('分享已打开')
        return
      } catch (error) {
        if (error?.name === 'AbortError') {
          return
        }
      }
    }
    if (await copyText(shareUrl)) {
      ElMessage.success('文章链接已复制')
    } else {
      ElMessage.warning('无法自动复制链接')
    }
  } catch (error) {
    ElMessage.warning(error?.message || '分享失败')
  } finally {
    sharing.value = false
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
          <div v-if="canInteractArticle" class="article-detail-actions">
            <el-button class="soft-btn" :loading="liking" @click="toggleLike">
              <Icon :icon="article.liked ? 'mdi:thumb-up' : 'mdi:thumb-up-outline'" />
              {{ article.liked ? '已点赞' : '点赞' }}
            </el-button>
            <el-button class="soft-btn" :loading="favoriting" @click="toggleFavorite">
              <Icon :icon="article.favorited ? 'mdi:bookmark' : 'mdi:bookmark-outline'" />
              {{ article.favorited ? '已收藏' : '收藏' }}
            </el-button>
            <el-button class="warm-btn" :loading="sharing" @click="handleShare">
              <Icon icon="mdi:share-variant-outline" />
              分享
            </el-button>
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
