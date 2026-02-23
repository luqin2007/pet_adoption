<script setup>
import { Icon } from '@iconify/vue'
import { ArrowRight } from '@element-plus/icons-vue'

const props = defineProps({
  activities: {
    type: Array,
    required: true,
  },
})
</script>

<template>
  <section id="news" class="content-section">
    <div class="section-head left">
      <h2>近期活动</h2>
      <p>近期活动横向展示，支持快速了解时间、地点与主题。</p>
    </div>
    <div class="activity-row">
      <article v-for="item in props.activities" :key="item.title">
        <el-card class="activity-card" shadow="hover">
          <div class="activity-cover">
            <img :src="item.cover" :alt="item.title" loading="lazy" />
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
            <el-button text type="warning" class="card-link">
              查看活动详情
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </el-card>
      </article>
    </div>
  </section>
</template>
