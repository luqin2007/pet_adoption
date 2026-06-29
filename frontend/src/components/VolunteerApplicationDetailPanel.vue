<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getVolunteerApplicationById } from '../api/volunteer'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const application = ref(null)

const applicationId = computed(() => String(route.params.id || ''))

const statusOptions = [
  { label: '已提交', value: 'SUBMITTED' },
  { label: '审核中', value: 'UNDER_REVIEW' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已撤回', value: 'CANCELED' },
]

const statusMap = Object.fromEntries(statusOptions.map((item) => [item.value, item.label]))

function statusText(status) {
  return statusMap[status] || status || '待补充'
}

function statusTagType(status) {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED' || status === 'CANCELED') return 'info'
  if (status === 'UNDER_REVIEW') return 'primary'
  return 'warning'
}

function formatDate(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function formatLocation(row) {
  return [row?.province, row?.city, row?.district, row?.address].filter(Boolean).join(' · ') || '—'
}

function goBack() {
  router.push('/console/volunteer/applications')
}

function goRecruitmentDetail() {
  if (application.value?.recruitmentId) {
    router.push(`/volunteers/recruitments/${application.value.recruitmentId}`)
  }
}

async function loadApplication() {
  if (!applicationId.value) return
  loading.value = true
  try {
    application.value = await getVolunteerApplicationById(applicationId.value)
  } catch (error) {
    application.value = null
    ElMessage.warning(error?.message || '加载招募申请详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadApplication()
})
</script>

<template>
  <el-card class="profile-card volunteer-application-detail-card" v-loading="loading">
    <template #header>
      <div class="profile-card-header">
        <strong>招募申请详情</strong>
      </div>
    </template>

    <section v-if="application" class="console-detail-shell">
      <div class="console-detail-toolbar">
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回申请</el-button>
        <el-tag :type="statusTagType(application.status)" effect="plain">{{ statusText(application.status) }}</el-tag>
      </div>

      <div class="console-detail-summary">
        <div>
          <span>申请人</span>
          <strong>{{ application.realName || application.username || '未命名' }}</strong>
          <p>{{ application.phone || '联系方式待补充' }}</p>
        </div>
        <div>
          <span>招募计划</span>
          <button class="table-primary-link" type="button" @click="goRecruitmentDetail">{{ application.recruitmentTitle || '未命名招募' }}</button>
          <p>{{ formatDate(application.createTime) }}</p>
        </div>
      </div>

      <div class="console-detail-grid">
        <section class="console-detail-section">
          <h3>申请人信息</h3>
          <dl class="console-detail-list">
            <div><dt>用户名</dt><dd>{{ application.username || '—' }}</dd></div>
            <div><dt>真实姓名</dt><dd>{{ application.realName || '—' }}</dd></div>
            <div><dt>电话</dt><dd>{{ application.phone || '—' }}</dd></div>
            <div><dt>年龄</dt><dd>{{ application.age ?? '—' }}</dd></div>
            <div><dt>所在地区</dt><dd>{{ formatLocation(application) }}</dd></div>
          </dl>
        </section>

        <section class="console-detail-section">
          <h3>服务意向</h3>
          <dl class="console-detail-list">
            <div><dt>技能标签</dt><dd>{{ application.skills || '—' }}</dd></div>
            <div><dt>可服务时间</dt><dd>{{ application.availableTimeDesc || '—' }}</dd></div>
            <div><dt>过往经历</dt><dd>{{ application.experience || '—' }}</dd></div>
            <div><dt>申请动机</dt><dd>{{ application.motivation || '—' }}</dd></div>
          </dl>
        </section>

        <section class="console-detail-section console-detail-section-wide">
          <h3>审核信息</h3>
          <dl class="console-detail-list console-detail-list-inline">
            <div><dt>当前状态</dt><dd>{{ statusText(application.status) }}</dd></div>
            <div><dt>审核人</dt><dd>{{ application.reviewerName || '—' }}</dd></div>
            <div><dt>审核时间</dt><dd>{{ formatDate(application.reviewTime) }}</dd></div>
            <div><dt>审核意见</dt><dd>{{ application.reviewComment || '—' }}</dd></div>
          </dl>
        </section>
      </div>
    </section>

    <section v-else-if="!loading" class="pet-admin-section">
      <el-empty description="未找到招募申请">
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回申请</el-button>
      </el-empty>
    </section>
  </el-card>
</template>
