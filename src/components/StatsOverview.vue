<script setup>
const props = defineProps({
  stats: {
    type: Array,
    required: true,
  },
})
</script>

<template>
  <section class="stats-bar" aria-label="救助数据概览">
    <el-row :gutter="16">
      <el-col v-for="item in props.stats" :key="item.label" :xs="12" :sm="6">
        <el-card class="stat-card" shadow="never">
          <el-statistic :value="item.value">
            <template #title>{{ item.label }}</template>
            <template #suffix>{{ item.suffix }}</template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>
  </section>
</template>
