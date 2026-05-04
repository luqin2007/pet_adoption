<script setup>
import { Icon } from '@iconify/vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  articles: {
    type: Array,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
})

const router = useRouter()

function goToArticles() {
  router.push('/articles')
}

function goToArticle(article) {
  if (!article?.id) {
    router.push('/articles')
    return
  }
  router.push(`/articles/${article.id}`)
}
</script>

<template>
  <section id="articles" class="content-section">
    <div class="section-head section-head-row">
      <h2>精选文章</h2>
      <el-button text type="warning" class="section-more" @click="goToArticles">
        查看全部
        <el-icon><ArrowRight /></el-icon>
      </el-button>
    </div>
    <div class="article-row">
      <article v-for="article in props.articles" :key="article.title" class="clickable-card" @click="goToArticle(article)">
        <el-card class="article-card" shadow="hover">
          <div class="article-cover">
            <img v-if="article.cover" :src="article.cover" :alt="article.title" loading="lazy" />
            <div v-else class="article-cover-placeholder">暂无封面</div>
          </div>
          <div class="article-body">
            <div class="article-head">
              <el-tag type="warning" effect="plain">{{ article.category }}</el-tag>
              <span>{{ article.date }}</span>
            </div>
            <h4>{{ article.title }}</h4>
            <p>{{ article.desc }}</p>
            <div class="article-foot">
              <span class="article-read">
                <Icon :icon="article.icon" />
                {{ article.reading }}
              </span>
            </div>
            <div class="article-source">
              <Icon icon="mdi:image-outline" />
              {{ article.source }}
            </div>
          </div>
        </el-card>
      </article>
      <el-empty v-if="!props.loading && props.articles.length === 0" class="section-empty" description="暂时没有精选文章" />
    </div>
  </section>
</template>
