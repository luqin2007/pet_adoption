<script setup>
import {
  Bell,
  Box,
  DataLine,
  House,
  Location,
  LocationInformation,
  Plus,
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
      <h1>让每一次相遇，都成为被温柔接住的开始</h1>
      <ul class="hero-list">
        <li><el-icon><LocationInformation /></el-icon> 覆盖 12 个城区联动响应</li>
        <li><el-icon><Bell /></el-icon> 重要事件实时通知与回访</li>
        <li><el-icon><DataLine /></el-icon> 救助进展清晰可查</li>
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
    </div>
  </section>
</template>
