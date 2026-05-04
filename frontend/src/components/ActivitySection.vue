<script setup>
import { computed } from 'vue'
import { Icon } from '@iconify/vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  activities: {
    type: Array,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
})

const router = useRouter()

const visibleActivities = computed(() => {
  const count = props.activities.length
  if (count >= 8) return props.activities.slice(0, 8)
  if (count >= 4) return props.activities.slice(0, 4)
  return props.activities
})

const isSparse = computed(() => props.activities.length < 4)

function goToArticles() {
  router.push('/articles?type=ACTIVITY')
}

function goToArticle(item) {
  if (item?.id) {
    router.push(`/articles/${item.id}`)
    return
  }
  goToArticles()
}
</script>

<template>
  <section id="news" class="content-section">
    <div class="section-head section-head-row">
      <h2>近期活动</h2>
      <el-button text type="warning" class="section-more" @click="goToArticles">
        查看全部
        <el-icon><ArrowRight /></el-icon>
      </el-button>
    </div>
    <div class="activity-row" :class="{ 'activity-row-sparse': isSparse }">
      <article v-for="item in visibleActivities" :key="item.title" class="clickable-card" @click="goToArticle(item)">
        <el-card class="activity-card" shadow="hover">
          <div class="activity-cover">
            <img v-if="item.cover" :src="item.cover" :alt="item.title" loading="lazy" />
            <div v-else class="activity-cover-placeholder">暂无封面</div>
            <el-tag class="activity-tag" type="warning" effect="dark" size="small">{{ item.type }}</el-tag>
          </div>
          <div class="activity-body">
            <h4>{{ item.title }}</h4>
            <p>{{ item.summary }}</p>
            <div class="activity-meta">
              <span>
                <Icon icon="mdi:calendar-clock-outline" />
                {{ item.date }}
              </span>
              <span>
                <Icon icon="mdi:map-marker-radius-outline" />
                {{ item.location }}
              </span>
            </div>
          </div>
        </el-card>
      </article>
      <el-empty
        v-if="!props.loading && props.activities.length === 0"
        class="section-empty"
        description="暂时没有近期活动"
      />
    </div>
  </section>
</template>
