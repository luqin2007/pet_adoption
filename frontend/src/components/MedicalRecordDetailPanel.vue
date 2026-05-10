<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getMedicalDetails, getMedicalRecord } from '../api/services'
import { useConsoleGuards } from '../composables/useConsoleGuards'
import { ROLE, hasRole } from '../utils/roles'

const route = useRoute()
const router = useRouter()
const { loginRole } = useConsoleGuards()

const loading = ref(false)
const record = ref(null)
const detailId = ref('')
const detailSummary = ref('')
const recordId = computed(() => String(route.params.id || ''))
const isDoctor = computed(() => hasRole(loginRole.value, ROLE.DOCTOR))

const recordStatusOptions = [
  { label: '待接诊', value: 'WAITING' },
  { label: '接诊中', value: 'PROCESSING' },
  { label: '待缴费', value: 'PAYING' },
  { label: '完成', value: 'COMPLETED' },
  { label: '取消', value: 'CANCELED' },
]

const recordTypeOptions = [
  { label: '初诊', value: 'FIRST' },
  { label: '复诊', value: 'REVISIT' },
  { label: '急诊', value: 'EMERGENCY' },
  { label: '体检', value: 'EXAMINATION' },
]

const statusMap = Object.fromEntries(recordStatusOptions.map((item) => [item.value, item.label]))
const typeMap = Object.fromEntries(recordTypeOptions.map((item) => [item.value, item.label]))

function statusText(value) {
  return statusMap[value] || value || '—'
}

function typeText(value) {
  return typeMap[value] || value || '—'
}

function statusTagType(value) {
  if (value === 'COMPLETED') return 'success'
  if (value === 'CANCELED') return 'info'
  if (value === 'PROCESSING') return 'primary'
  return 'warning'
}

function formatDate(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function goBack() {
  router.push('/console/medical/records')
}

function goPetProfile() {
  if (record.value?.petId) router.push(`/pets/${record.value.petId}`)
}

function goMedicalDetail() {
  if (detailId.value) {
    router.push(`/medical/detail/${detailId.value}`)
  } else if (record.value?.id && isDoctor.value && record.value.status !== 'WAITING') {
    router.push(`/medical/detail/new?recordId=${record.value.id}`)
  }
}

async function loadDetailId() {
  if (!recordId.value || record.value?.status === 'WAITING') {
    detailId.value = ''
    detailSummary.value = ''
    return
  }
  try {
    const res = await getMedicalDetails({ record: [recordId.value], page: 1, size: 1 })
    const item = res?.records?.[0] || null
    detailId.value = item?.id || ''
    detailSummary.value = item?.summary || ''
  } catch {
    detailId.value = ''
    detailSummary.value = ''
  }
}

async function loadRecord() {
  if (!recordId.value) return
  loading.value = true
  try {
    record.value = await getMedicalRecord(recordId.value)
    await loadDetailId()
  } catch (error) {
    record.value = null
    ElMessage.warning(error?.message || '加载就诊记录详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadRecord()
})
</script>

<template>
  <el-card class="profile-card pet-admin-card" v-loading="loading">
    <template #header>
      <div class="profile-card-header">
        <strong>就诊记录详情</strong>
        <span>{{ record?.petName || '就诊信息' }}</span>
      </div>
    </template>

    <section v-if="record" class="console-detail-shell">
      <div class="console-detail-toolbar">
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回就诊记录</el-button>
        <el-tag :type="statusTagType(record.status)" effect="plain">{{ statusText(record.status) }}</el-tag>
      </div>

      <div class="console-detail-summary">
        <div>
          <span>宠物</span>
          <button class="table-primary-link console-detail-title-link" type="button" @click="goPetProfile">{{ record.petName || '未命名' }}</button>
          <p>{{ record.petType || '宠物' }} · {{ record.petBreed || '品种待补充' }} · {{ record.petSex || '未知' }}</p>
        </div>
        <div>
          <span>接诊医生</span>
          <strong>{{ record.username || '—' }}</strong>
          <p>{{ formatDate(record.createTime) }}</p>
        </div>
      </div>

      <div class="console-detail-grid">
        <section class="console-detail-section">
          <h3>诊疗信息</h3>
          <dl class="console-detail-list">
            <div><dt>类型</dt><dd>{{ typeText(record.type) }}</dd></div>
            <div><dt>状态</dt><dd>{{ statusText(record.status) }}</dd></div>
            <div><dt>开始时间</dt><dd>{{ formatDate(record.startTime) }}</dd></div>
            <div><dt>结束时间</dt><dd>{{ formatDate(record.endTime) }}</dd></div>
          </dl>
        </section>

        <section class="console-detail-section">
          <h3>费用与领养人</h3>
          <dl class="console-detail-list">
            <div><dt>预计费用</dt><dd>{{ record.price || '0' }}</dd></div>
            <div><dt>实际费用</dt><dd>{{ record.cost || '0' }}</dd></div>
            <div><dt>领养人</dt><dd>{{ record.ownerName || '—' }}</dd></div>
            <div><dt>联系电话</dt><dd>{{ record.ownerPhone || '—' }}</dd></div>
          </dl>
        </section>

        <section class="console-detail-section console-detail-section-wide">
          <h3>病历</h3>
          <dl class="console-detail-list">
            <div>
              <dt>关联病历</dt>
              <dd>
                <button v-if="detailId || (isDoctor && record.status !== 'WAITING')" class="table-primary-link" type="button" @click="goMedicalDetail">
                  {{ detailId ? (detailSummary || '查看病历详情') : '创建病历' }}
                </button>
                <span v-else>暂无可查看病历</span>
              </dd>
            </div>
          </dl>
        </section>
      </div>
    </section>

    <section v-else-if="!loading" class="pet-admin-section">
      <el-empty description="未找到就诊记录">
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回就诊记录</el-button>
      </el-empty>
    </section>
  </el-card>
</template>
