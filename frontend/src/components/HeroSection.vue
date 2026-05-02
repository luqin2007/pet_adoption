<script setup>
import {
  ArrowRight,
  Bell,
  Box,
  DataLine,
  House,
  Location,
  LocationInformation,
  Plus,
  StarFilled,
  Suitcase,
  User,
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const emit = defineEmits(['navigate'])
const router = useRouter()

const quickActions = [
  {
    title: '志愿申请',
    icon: User,
    to: '/volunteers',
  },
  {
    title: '发现宠物',
    icon: Plus,
    to: '/pets/new',
  },
  {
    title: '申请救助',
    icon: Suitcase,
    to: '/tasks/new',
  },
  {
    title: '物资捐赠',
    icon: Box,
    to: '/donations/new',
  },
  {
    title: '宠物寄养',
    icon: House,
    to: '/breading/new',
  },
  {
    title: '走失报备',
    icon: Location,
    to: '/lost/new',
  },
]

function handleNavigate(id) {
  emit('navigate', id)
}

function openAction(path) {
  router.push(path)
}
</script>

<template>
  <section class="hero-section" id="home">
    <div class="hero-copy">
      <el-tag class="hero-tag" type="warning" effect="plain">城市联合救助计划</el-tag>
      <h1>让每一次相遇，都成为被温柔接住的开始</h1>
      <p>
        连接居民、志愿者、医院与公益组织，建立从“发现”到“救助”再到“领养”的完整闭环。
        每一条上报都会被记录、追踪、反馈。
      </p>
      <div class="hero-buttons">
        <el-button class="warm-btn" type="warning" size="large" @click="handleNavigate('adoption')">
          查看领养专区
          <el-icon><ArrowRight /></el-icon>
        </el-button>
        <el-button class="soft-btn" size="large" @click="handleNavigate('news')">
          查看近期活动
        </el-button>
      </div>
      <ul class="hero-list">
        <li><el-icon><LocationInformation /></el-icon> 覆盖 12 个城区联动响应</li>
        <li><el-icon><Bell /></el-icon> 重要事件实时通知与回访</li>
        <li><el-icon><DataLine /></el-icon> 救助数据全流程可追溯</li>
      </ul>
    </div>

    <div class="hero-panel">
      <el-card class="progress-card" shadow="never">
        <div class="panel-head">
          <h3>快捷入口</h3>
          <el-tag type="success">在线办理</el-tag>
        </div>
        <div class="quick-action-grid">
          <button v-for="item in quickActions" :key="item.title" class="quick-action-card" type="button" @click="openAction(item.to)">
            <el-icon class="quick-action-icon">
              <component :is="item.icon" />
            </el-icon>
            <span>
              <strong>{{ item.title }}</strong>
            </span>
          </button>
        </div>
      </el-card>

      <el-card class="story-card" shadow="hover">
        <div class="story-head">
          <el-icon><StarFilled /></el-icon>
          <span>今日暖心故事</span>
        </div>
        <p>
          志愿者在地铁口发现受伤的“奶盖”，3 小时内完成转运、检查与临时安置。
          目前恢复情况稳定，已开放领养申请。
        </p>
      </el-card>
    </div>
  </section>
</template>
