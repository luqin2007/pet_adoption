<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Clock } from '@element-plus/icons-vue'

const props = defineProps({
  records: { type: Array, default: () => [] },
  type: { type: String, required: true },
  targetId: { type: [String, Number], default: '' },
  loading: { type: Boolean, default: false },
  statusLabels: { type: Object, default: () => ({}) },
})

const router = useRouter()

function labelOf(value) {
  if (!value) return '-'
  return props.statusLabels[value] || value
}

function formatTime(value) {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}/${pad(d.getMonth() + 1)}/${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function viewAll() {
  const routeMap = {
    pet: { name: 'audit-history', query: { type: 'pet', id: props.targetId } },
    task: { name: 'audit-history', query: { type: 'task', id: props.targetId } },
    shift: { name: 'audit-history', query: { type: 'shift', id: props.targetId } },
  }
  const target = routeMap[props.type]
  if (target) router.push(target)
}
</script>

<template>
  <div v-if="records.length || loading" class="audit-record-list">
    <div class="audit-record-title" @click="viewAll">审核记录</div>
    <div v-if="loading" class="audit-record-loading">加载中...</div>
    <div v-else-if="!records.length" class="audit-record-empty">暂无审核记录</div>
    <div v-else class="audit-record-items">
      <div v-for="record in records.slice(0, 5)" :key="record.id" class="audit-record-item">
        <div class="audit-record-left">
          <el-avatar v-if="record.username" :size="24" :src="record.avatar" class="audit-record-avatar">
            {{ record.username?.charAt(0) }}
          </el-avatar>
          <div v-else class="audit-record-avatar-placeholder">
            <el-icon><Clock /></el-icon>
          </div>
        </div>
        <div class="audit-record-content">
          <div class="audit-record-main">
            <span v-if="record.username" class="audit-record-user">{{ record.username }}</span>
            <el-tag size="small" type="info" effect="plain" class="audit-record-tag">{{ labelOf(record.from || record.statusFrom) }}</el-tag>
            <span class="audit-record-arrow">&rarr;</span>
            <el-tag size="small" effect="plain" class="audit-record-tag">{{ labelOf(record.to || record.statusTo) }}</el-tag>
          </div>
          <div v-if="record.reason || record.comment || record.description" class="audit-record-reason">{{ record.reason || record.comment || record.description }}</div>
          <div class="audit-record-time">{{ formatTime(record.createTime) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.audit-record-list {
  margin-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
  padding-top: 12px;
}
.audit-record-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-color-primary);
  margin-bottom: 8px;
  cursor: pointer;
}
.audit-record-title:hover {
  text-decoration: underline;
}
.audit-record-loading,
.audit-record-empty {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  text-align: center;
  padding: 8px 0;
}
.audit-record-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.audit-record-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 6px 8px;
  border-radius: 6px;
  background: var(--el-fill-color-lighter);
  transition: background 0.15s;
}
.audit-record-item:hover {
  background: var(--el-fill-color);
}
.audit-record-left {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  padding-top: 1px;
}
.audit-record-avatar {
  font-size: 11px;
}
.audit-record-avatar-placeholder {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--el-fill-color);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-placeholder);
  font-size: 12px;
}
.audit-record-content {
  flex: 1;
  min-width: 0;
}
.audit-record-main {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}
.audit-record-user {
  font-size: 12px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-right: 2px;
}
.audit-record-tag {
  font-size: 11px;
  height: 18px;
  padding: 0 4px;
  line-height: 16px;
}
.audit-record-arrow {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
}
.audit-record-reason {
  font-size: 12px;
  color: var(--el-text-color-regular);
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.audit-record-time {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  margin-top: 2px;
  text-align: right;
}
</style>
