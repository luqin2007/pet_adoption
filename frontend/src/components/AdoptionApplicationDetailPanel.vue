<template>
  <el-card class="profile-card" v-loading="loading">
    <template #header>
      <div class="profile-card-header">
        <strong>领养申请详情</strong>
        <span>{{ application?.petName || '查看申请信息与回访记录' }}</span>
      </div>
    </template>

    <section v-if="application" class="action-form-panel adoption-application-detail-panel">
      <div class="console-detail-toolbar">
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回列表</el-button>
        <el-tag :type="statusTagType(application.status)" effect="plain">{{ statusText(application.status) }}</el-tag>
      </div>

      <div class="console-detail-summary">
        <div>
          <span>宠物</span>
          <button class="table-primary-link adoption-detail-link" type="button" @click="goPetProfile">
            <strong>{{ application.petName || '未命名宠物' }}</strong>
          </button>
          <p>{{ petMetaText }}</p>
        </div>
        <div>
          <span>申请人</span>
          <strong>{{ application.applicantName || '未命名用户' }}</strong>
          <p>{{ application.applicantPhone || '联系方式待补充' }}</p>
        </div>
        <div>
          <span>申请时间</span>
          <strong>{{ formatDate(application.createTime) }}</strong>
          <p>{{ statusTimelineText }}</p>
        </div>
      </div>

      <section class="console-detail-grid">
        <section class="console-detail-section">
          <h3>申请信息</h3>
          <dl class="console-detail-list console-detail-list-inline">
            <div><dt>申请状态</dt><dd>{{ statusText(application.status) }}</dd></div>
            <div><dt>审核人</dt><dd>{{ application.reviewerName || '—' }}</dd></div>
            <div><dt>审核时间</dt><dd>{{ formatDate(application.reviewTime) }}</dd></div>
            <div><dt>领养时间</dt><dd>{{ formatDate(application.adoptTime) }}</dd></div>
          </dl>
          <div v-if="application.rejectReason" class="adoption-detail-note">
            <span>拒绝原因</span>
            <p>{{ application.rejectReason }}</p>
          </div>
        </section>

        <section class="console-detail-section">
          <h3>宠物信息</h3>
          <dl class="console-detail-list console-detail-list-inline">
            <div><dt>宠物编号</dt><dd>{{ application.petId || '—' }}</dd></div>
            <div><dt>宠物头像</dt><dd>{{ application.petCover ? '已设置' : '未设置' }}</dd></div>
            <div><dt>申请人头像</dt><dd>{{ application.applicantAvatar ? '已设置' : '未设置' }}</dd></div>
            <div><dt>审核人头像</dt><dd>{{ application.reviewerAvatar ? '已设置' : '未设置' }}</dd></div>
          </dl>
        </section>

        <section class="console-detail-section console-detail-section-wide">
          <div class="adoption-detail-section-head">
            <h3>回访任务</h3>
            <span>{{ followTasks.length }} 条记录</span>
          </div>
          <div v-if="followTasks.length" class="adoption-follow-list">
            <article v-for="item in followTasks" :key="item.id" class="adoption-follow-card">
              <div class="adoption-follow-head">
                <div>
                  <strong>{{ followTaskTitle(item) }}</strong>
                  <span>{{ followTaskMeta(item) }}</span>
                </div>
                <el-tag :type="followTaskTagType(item.status)" effect="plain">{{ followTaskStatusText(item.status) }}</el-tag>
              </div>
              <p v-if="item.remark">{{ item.remark }}</p>
              <dl class="console-detail-list console-detail-list-inline">
                <div><dt>计划时间</dt><dd>{{ formatDate(item.planTime) }}</dd></div>
                <div><dt>执行记录</dt><dd>{{ item.recordId ? `#${item.recordId}` : '暂无' }}</dd></div>
                <div><dt>记录摘要</dt><dd>{{ item.summary || '暂无' }}</dd></div>
                <div><dt>回访时间</dt><dd>{{ formatDate(item.visitTime) }}</dd></div>
              </dl>
            </article>
          </div>
          <el-empty v-else description="暂无回访任务" />
        </section>
      </section>
    </section>

    <section v-else-if="!loading" class="pet-admin-section">
      <el-empty description="未找到领养申请">
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回列表</el-button>
      </el-empty>
    </section>
  </el-card>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getAdoptApplication } from '../api/services'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const application = ref(null)

const applicationId = computed(() => String(route.params.id || ''))
const followTasks = computed(() => Array.isArray(application.value?.followTasks) ? application.value.followTasks : [])
const petMetaText = computed(() => {
  const parts = [application.value?.petType, application.value?.petBreed].filter(Boolean)
  return parts.length ? parts.join(' · ') : '宠物信息待补充'
})
const statusTimelineText = computed(() => {
  const parts = [formatDate(application.value?.reviewTime), formatDate(application.value?.adoptTime)].filter(Boolean)
  return parts.length ? parts.join(' · ') : '流程时间待补充'
})

const statusOptions = [
  { label: '已提交', value: 'CREATE' },
  { label: '审核通过', value: 'PASS' },
  { label: '审核拒绝', value: 'REJECT' },
  { label: '协议草拟中', value: 'AGREEMENT_DRAFT' },
  { label: '协议已签署', value: 'AGREEMENT_SIGNED' },
  { label: '回访中', value: 'TRACKING' },
  { label: '流程完成', value: 'FINISH' },
  { label: '已取消', value: 'CANCEL' },
]

const statusMap = Object.fromEntries(statusOptions.map((item) => [item.value, item.label]))
const followTaskStatusMap = {
  CREATE: '待执行',
  NOTIFIED: '已通知',
  IN_PROGRESS: '进行中',
  DELAY: '已推迟',
  FINISH: '已完成',
}

function statusText(value) {
  return statusMap[value] || value || ''
}

function statusTagType(value) {
  if (value === 'PASS' || value === 'AGREEMENT_SIGNED' || value === 'FINISH') return 'success'
  if (value === 'REJECT' || value === 'CANCEL') return 'info'
  if (value === 'AGREEMENT_DRAFT' || value === 'TRACKING') return 'primary'
  return 'warning'
}

function followTaskStatusText(value) {
  return followTaskStatusMap[value] || value || '待补充'
}

function followTaskTagType(value) {
  if (value === 'FINISH') return 'success'
  if (value === 'DELAY') return 'info'
  if (value === 'IN_PROGRESS' || value === 'NOTIFIED') return 'primary'
  return 'warning'
}

function followTaskTitle(item) {
  return item?.volunteerName || item?.workerName || '回访任务'
}

function followTaskMeta(item) {
  const parts = [item?.workerName, item?.volunteerName].filter(Boolean)
  return parts.length ? parts.join(' · ') : '负责人待补充'
}

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function goBack() {
  router.push('/console/adoption/adopts')
}

function goPetProfile() {
  if (application.value?.petId) {
    router.push(`/pets/${application.value.petId}`)
  }
}

async function loadApplication() {
  if (!applicationId.value) return
  loading.value = true
  try {
    application.value = await getAdoptApplication(applicationId.value)
  } catch (error) {
    application.value = null
    ElMessage.warning(error?.message || '加载领养申请详情失败')
  } finally {
    loading.value = false
  }
}

watch(() => route.params.id, loadApplication)

onMounted(() => {
  loadApplication()
})
</script>

<style scoped>
.adoption-application-detail-panel {
  display: grid;
  gap: 18px;
}

.adoption-detail-link {
  display: inline-flex;
  align-items: center;
  padding: 0;
  border: 0;
  background: transparent;
  font: inherit;
  cursor: pointer;
  text-align: left;
}

.adoption-detail-link strong {
  color: inherit;
  font-size: 20px;
  line-height: 1.25;
}

.adoption-detail-link:hover,
.adoption-detail-link:focus-visible {
  color: var(--primary-strong);
}

.adoption-detail-note {
  padding: 12px 14px;
  border: 1px solid rgba(243, 223, 204, 0.9);
  border-radius: 12px;
  background: rgba(255, 253, 249, 0.95);
}

.adoption-detail-note span,
.adoption-detail-section-head span,
.adoption-follow-head span,
.adoption-follow-card p {
  color: var(--muted);
  line-height: 1.6;
}

.adoption-detail-note span {
  display: block;
  font-size: 12px;
  font-weight: 700;
}

.adoption-detail-note p {
  margin: 6px 0 0;
  color: #5d3927;
  line-height: 1.7;
}

.adoption-detail-section-head,
.adoption-follow-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.adoption-detail-section-head h3 {
  margin: 0;
}

.adoption-follow-list {
  display: grid;
  gap: 12px;
}

.adoption-follow-card {
  border: 1px solid rgba(243, 223, 204, 0.9);
  border-radius: 14px;
  background: rgba(255, 253, 249, 0.96);
  padding: 14px;
  display: grid;
  gap: 10px;
}

.adoption-follow-head strong {
  display: block;
  color: #5d3927;
  font-size: 15px;
  line-height: 1.4;
}

.adoption-follow-card p {
  margin: 0;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

@media (max-width: 980px) {
  .adoption-detail-section-head,
  .adoption-follow-head {
    flex-direction: column;
  }
}
</style>
