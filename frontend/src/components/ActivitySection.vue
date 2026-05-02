<script setup>
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

function goToArticles() {
  router.push('/articles')
}
</script>

<template>
  <section id="news" class="content-section">
    <div class="section-head left">
      <h2>近期活动</h2>
    </div>
    <div class="activity-row">
      <article v-for="item in props.activities" :key="item.title">
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
            <el-button text type="warning" class="card-link" @click="goToArticles">
              查看活动详情
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </el-card>
      </article>
      <el-empty
        v-if="!props.loading && props.activities.length === 0"
        class="section-empty"
        description="当前还没有活动内容"
      />
    </div>
    <div class="section-action">
      <el-button class="soft-btn" size="large" @click="goToArticles">查看全部活动与故事</el-button>
    </div>
  </section>
</template>
