<template>
  <el-card class="profile-card" v-loading="loading">
    <template #header>
      <div class="profile-card-header">
        <strong>寄养申请详情</strong>
        <span>{{ application?.petName || '查看寄养申请信息' }}</span>
      </div>
    </template>

    <section v-if="application" class="action-form-panel breading-application-detail-panel">
      <div class="console-detail-toolbar">
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回列表</el-button>
        <el-tag :type="statusTagType(application.status)" effect="plain">{{ statusText(application.status) }}</el-tag>
      </div>

      <div class="breading-hero-grid">
        <section class="console-detail-section breading-pet-card">
          <div class="breading-pet-cover">
            <el-icon><PictureFilled /></el-icon>
          </div>
          <div class="breading-pet-copy">
            <span>宠物</span>
            <strong>{{ application.petName || '未命名宠物' }}</strong>
            <p>{{ [application.petType, application.petBreed, application.petAge != null ? `${application.petAge} 月` : ''].filter(Boolean).join(' · ') || '基础信息待补充' }}</p>
          </div>
        </section>

        <section class="console-detail-section breading-person-card">
          <p>申请人</p>
          <div class="breading-person-row">
            <el-avatar class="breading-avatar" :size="44" :src="application.applicantAvatar">
              {{ avatarInitial(application.applicantName) }}
            </el-avatar>
            <div>
              <strong>{{ application.applicantName || '未命名用户' }}</strong>
              <span>{{ application.applicantPhone || '联系方式待补充' }}</span>
            </div>
          </div>
        </section>

        <section class="console-detail-section breading-person-card">
          <p>审核人</p>
          <div class="breading-person-row">
            <el-avatar class="breading-avatar" :size="44" :src="application.reviewerAvatar">
              {{ avatarInitial(application.reviewerName || '审核') }}
            </el-avatar>
            <div>
              <strong>{{ application.reviewerName || '待审核' }}</strong>
              <span>{{ application.reviewerId ? '已分配审核人' : '暂无审核人' }}</span>
            </div>
          </div>
        </section>
      </div>

      <section class="console-detail-section console-detail-section-wide">
        <dl class="console-detail-list console-detail-list-row breading-status-row">
          <div><dt>申请状态</dt><dd>{{ statusText(application.status) }}</dd></div>
          <div><dt>审核时间</dt><dd>{{ formatDate(application.reviewTime) }}</dd></div>
          <div><dt>开始时间</dt><dd>{{ formatDate(application.startTime) }}</dd></div>
          <div><dt>预计结束时间</dt><dd>{{ formatDate(application.endTime) }}</dd></div>
        </dl>
      </section>

      <section v-if="application.petDescription" class="console-detail-section console-detail-section-wide">
        <div class="breading-section-head">
          <h3>寄养说明</h3>
        </div>
        <p class="breading-note">{{ application.petDescription }}</p>
      </section>

      <section v-if="isReject" class="console-detail-section console-detail-section-wide">
        <div class="breading-section-head">
          <h3>拒绝原因</h3>
        </div>
        <p class="breading-note">{{ application.rejectReason || '暂无拒绝原因' }}</p>
      </section>
    </section>

    <section v-else-if="!loading" class="pet-admin-section">
      <el-empty description="未找到寄养申请">
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回列表</el-button>
      </el-empty>
    </section>
  </el-card>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, PictureFilled } from '@element-plus/icons-vue'
import { getBreadingApplication } from '../api/services'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const application = ref(null)

const applicationId = computed(() => String(route.params.id || ''))
const isReject = computed(() => application.value?.status === 'REJECT')

function statusText(value) {
  const map = {
    CREATE: '已提交',
    PASS: '审核通过',
    REJECT: '审核拒绝',
    AGREEMENT_DRAFT: '协议草拟中',
    AGREEMENT_PENDING_CONFIRM: '待确认',
    AGREEMENT_SIGNED: '协议已签署',
    TRACKING: '回访中',
    FINISH: '流程完成',
    CANCEL: '已取消',
  }
  return map[value] || value || ''
}

function statusTagType(value) {
  if (value === 'PASS' || value === 'AGREEMENT_SIGNED' || value === 'FINISH') return 'success'
  if (value === 'REJECT' || value === 'CANCEL') return 'info'
  if (value === 'AGREEMENT_DRAFT' || value === 'TRACKING') return 'primary'
  if (value === 'AGREEMENT_PENDING_CONFIRM') return 'warning'
  return 'warning'
}

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function avatarInitial(value) {
  const text = String(value || '').trim()
  return text ? text.slice(0, 1) : '宠'
}

function goBack() {
  router.push({ name: 'console-adoption' })
}

async function loadDetail() {
  if (!applicationId.value) return
  loading.value = true
  try {
    application.value = await getBreadingApplication(applicationId.value)
  } catch (error) {
    application.value = null
    ElMessage.warning(error?.message || '加载寄养申请失败')
  } finally {
    loading.value = false
  }
}

watch(applicationId, loadDetail, { immediate: true })
</script>

<style scoped>
.breading-application-detail-panel {
  display: grid;
  gap: 16px;
}

.breading-hero-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr;
  gap: 16px;
}

.breading-pet-card,
.breading-person-card {
  min-height: 150px;
}

.breading-pet-card {
  display: flex;
  gap: 16px;
  align-items: center;
}

.breading-pet-cover {
  display: grid;
  place-items: center;
  width: 96px;
  height: 96px;
  border-radius: 18px;
  color: #c47a3a;
  background: linear-gradient(180deg, rgba(255, 244, 232, 0.98), rgba(255, 232, 211, 0.92));
}

.breading-pet-cover :deep(.el-icon) {
  font-size: 34px;
}

.breading-pet-copy {
  display: grid;
  gap: 6px;
}

.breading-pet-copy span,
.breading-pet-copy p,
.breading-person-card p,
.breading-person-row span,
.breading-note {
  color: var(--muted);
}

.breading-pet-copy strong {
  font-size: 20px;
  color: var(--text);
}

.breading-person-card {
  display: grid;
  gap: 12px;
}

.breading-person-card p {
  margin: 0;
}

.breading-person-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.breading-avatar {
  flex: none;
  background: rgba(255, 233, 214, 0.9);
  color: #8a4b1d;
}

.breading-person-row div {
  display: grid;
  gap: 4px;
}

.breading-person-row strong {
  color: var(--text);
}

.breading-status-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.breading-section-head {
  margin-bottom: 10px;
}

.breading-section-head h3 {
  margin: 0;
  font-size: 16px;
  color: var(--text);
}

.breading-note {
  margin: 0;
  line-height: 1.8;
  white-space: pre-wrap;
}

@media (max-width: 1024px) {
  .breading-hero-grid {
    grid-template-columns: 1fr;
  }

  .breading-status-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .breading-status-row {
    grid-template-columns: 1fr;
  }
}
</style>
