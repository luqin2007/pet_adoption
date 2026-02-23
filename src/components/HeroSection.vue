<script setup>
import { ArrowRight, Bell, DataLine, LocationInformation, StarFilled } from '@element-plus/icons-vue'

const emit = defineEmits(['navigate'])

function handleNavigate(id) {
  emit('navigate', id)
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
          <h3>本周救助进度</h3>
          <el-tag type="success">进行中</el-tag>
        </div>
        <div class="progress-metrics">
          <div>
            <span>新接收</span>
            <strong>42</strong>
          </div>
          <div>
            <span>医疗处理</span>
            <strong>29</strong>
          </div>
          <div>
            <span>完成安置</span>
            <strong>17</strong>
          </div>
        </div>
        <el-progress :stroke-width="12" :percentage="78" color="#e77a3b" />
        <p class="panel-note">距离本周目标还差 13 只，正在持续接力。</p>
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
