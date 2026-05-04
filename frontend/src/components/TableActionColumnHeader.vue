<script setup>
import { Setting } from '@element-plus/icons-vue'

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
    <el-tooltip :content="collapsed ? '展开' : '收起'" placement="top">
      <button type="button" class="table-action-toggle" @click.stop="emit('toggle')">
        <el-icon><Setting /></el-icon>
      </button>
    </el-tooltip>
  </div>
</template>
