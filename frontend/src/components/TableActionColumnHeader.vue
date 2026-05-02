<script setup>
import { ArrowLeftBold, ArrowRightBold } from '@element-plus/icons-vue'

defineProps({
  title: {
    type: String,
    default: '操作',
  },
  collapsed: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['toggle'])
</script>

<template>
  <div class="table-action-header" :class="{ 'is-collapsed': collapsed }">
    <span class="table-action-header-label">{{ title }}</span>
    <el-tooltip :content="collapsed ? '展开处理列' : '隐藏处理列'" placement="top">
      <button type="button" class="table-action-toggle" @click.stop="emit('toggle')">
        <el-icon v-if="collapsed"><ArrowLeftBold /></el-icon>
        <el-icon v-else><ArrowRightBold /></el-icon>
      </button>
    </el-tooltip>
  </div>
</template>
