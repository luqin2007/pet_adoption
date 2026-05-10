<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { getMedicalRecords, getMedicalDetails } from '../api/services'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const records = ref([])
const petId = computed(() => route.query.pet || '')
const petName = computed(() => route.query.name || '')

function formatDate(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function goDetail(row) {
  if (row?.id) router.push(`/medical/detail/${row.id}`)
}

function goBack() {
  router.back()
}

async function loadRecords() {
  loading.value = true
  try {
    const recordQuery = { sort: 'create_time', order: 'desc', size: 200 }
    if (petId.value) recordQuery.pet = petId.value
    const recordRes = await getMedicalRecords(recordQuery)
    const recordList = recordRes?.records || []
    const recordIds = recordList.map((r) => r.id).filter(Boolean)

    const detailList = []
    if (recordIds.length) {
      const detailRes = await getMedicalDetails({ record: recordIds, size: 200 })
      const details = detailRes?.records || []
      for (const d of details) {
        const rec = recordList.find((r) => String(r.id) === String(d.recordId))
        detailList.push({ ...d, recordStatus: rec?.status, recordType: rec?.type, doctorName: rec?.username })
      }
    }
    records.value = detailList
  } catch (error) {
    ElMessage.warning(error?.message || '加载病例失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => loadRecords())
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main action-form-page" v-loading="loading">
      <section class="action-form-panel">
        <header class="form-section-header">
          <div>
            <h2>{{ petName || '全部病历' }}</h2>
            <p style="font-size:13px;color:var(--muted);margin-top:4px">{{ petId ? '该宠物的所有病例记录' : '所有就诊病例记录' }}</p>
          </div>
          <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回</el-button>
        </header>

        <div v-if="records.length" class="case-list">
          <div v-for="item in records" :key="item.id" class="case-item" @click="goDetail(item)">
            <div class="case-item-main">
              <strong class="case-summary">{{ item.summary || '未命名病例' }}</strong>
              <p class="case-meta">{{ formatDate(item.createTime) }} · {{ item.doctorName || item.username || '—' }}</p>
            </div>
            <el-tag size="small" :type="item.isCompleted ? 'success' : 'warning'" effect="plain">
              {{ item.isCompleted ? '已完成' : '进行中' }}
            </el-tag>
          </div>
        </div>

        <el-empty v-else-if="!loading" description="暂无病例记录" />
      </section>
    </main>

    <AppFooter />
  </div>
</template>

<style scoped>
.form-section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.form-section-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.case-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.case-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border: 1px solid #efefef;
  border-radius: 10px;
  background: #fafafa;
  cursor: pointer;
  transition: background 180ms ease, border-color 180ms ease;
}

.case-item:hover {
  background: #f5f0eb;
  border-color: #e0d5c5;
}

.case-item-main {
  min-width: 0;
}

.case-summary {
  display: block;
  font-size: 15px;
  color: #333;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.case-meta {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--muted);
}
</style>