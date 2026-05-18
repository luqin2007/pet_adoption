<template>
  <el-card class="profile-card" v-loading="loading || agreementLoading">
    <template #header>
      <div class="profile-card-header">
        <strong>申请详情</strong>
        <span>{{ application?.petName || '查看申请信息' }}</span>
      </div>
    </template>

    <section v-if="application" class="action-form-panel application-detail-panel">
      <div class="console-detail-toolbar">
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回列表</el-button>
        <el-tag :type="statusTagType(application.status)" effect="plain">{{ statusText(application.status) }}</el-tag>
      </div>

      <div class="application-hero-grid">
        <section class="console-detail-section application-pet-card">
          <div class="application-pet-cover">
            <img v-if="application.petCover" :src="application.petCover" :alt="application.petName || '宠物封面'" />
            <el-icon v-else><PictureFilled /></el-icon>
          </div>
          <div class="application-pet-copy">
            <span>宠物</span>
            <button class="table-primary-link" type="button" @click="goPetProfile">
              <strong>{{ application.petName || '未命名宠物' }}</strong>
            </button>
            <p>{{ [application.petType, application.petBreed, application.petAge != null ? `${application.petAge} 月` : ''].filter(Boolean).join(' · ') || '基础信息待补充' }}</p>
          </div>
        </section>

        <section class="console-detail-section application-person-card">
          <p>申请人</p>
          <div class="application-person-row">
            <el-avatar class="application-avatar" :size="44" :src="application.applicantAvatar">
              {{ avatarInitial(application.applicantName) }}
            </el-avatar>
            <div>
              <strong>{{ application.applicantName || '未命名用户' }}</strong>
              <span>{{ application.applicantPhone || '联系方式待补充' }}</span>
            </div>
          </div>
        </section>

        <section class="console-detail-section application-person-card">
          <p>审核人</p>
          <div class="application-person-row">
            <el-avatar class="application-avatar" :size="44" :src="application.reviewerAvatar">
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
        <dl class="console-detail-list console-detail-list-row application-status-row">
          <div><dt>申请状态</dt><dd>{{ statusText(application.status) }}</dd></div>
          <div v-if="application.reviewTime"><dt>审核时间</dt><dd>{{ formatDate(application.reviewTime) }}</dd></div>
          <div v-if="isBreadingType && application.startTime"><dt>开始时间</dt><dd>{{ formatDate(application.startTime) }}</dd></div>
          <div v-if="isBreadingType && application.endTime"><dt>预计结束时间</dt><dd>{{ formatDate(application.endTime) }}</dd></div>
          <div v-if="!isBreadingType && application.adoptTime"><dt>领养时间</dt><dd>{{ formatDate(application.adoptTime) }}</dd></div>
        </dl>
      </section>

      <section v-if="application.petDescription" class="console-detail-section console-detail-section-wide">
        <div class="application-section-head">
          <h3>说明</h3>
        </div>
        <p class="application-note">{{ application.petDescription }}</p>
      </section>

      <template v-if="hasAgreement">
        <section class="console-detail-section console-detail-section-wide">
          <div class="application-section-head">
            <h3>协议内容</h3>
            <div class="breading-section-tags">
              <el-tag effect="plain">{{ agreementTypeText(currentAgreementType) }}</el-tag>
              <el-tag :type="currentAgreement?.signTime ? 'success' : 'warning'" effect="plain">
                {{ currentAgreement?.signTime ? '已签署' : '未签署' }}
              </el-tag>
            </div>
          </div>
          <div v-if="currentAgreementType === 'ELECTRONIC'" class="adoption-agreement-text-box">
            <p class="application-note">{{ currentAgreement.content || '暂无协议正文' }}</p>
          </div>
          <div v-else-if="currentAgreementType === 'PAPER'" class="adoption-paper-grid">
            <button
              v-for="(file, index) in paperFiles"
              :key="file.id"
              type="button"
              class="adoption-paper-card"
              @click="openPaperPreview(index)"
            >
              <div class="adoption-paper-folder">
                <img :src="file.assetUrl" :alt="`协议第 ${file.page || index + 1} 页`" loading="lazy" />
                <span class="adoption-paper-page-banner">第 {{ file.page || index + 1 }} 页</span>
              </div>
            </button>
          </div>
        </section>
      </template>

      <section v-if="isReject" class="console-detail-section console-detail-section-wide">
        <div class="application-section-head">
          <h3>拒绝原因</h3>
        </div>
        <p class="application-note">{{ application.rejectReason || '暂无拒绝原因' }}</p>
      </section>
    </section>

    <section v-else-if="!loading" class="pet-admin-section">
      <el-empty description="未找到申请">
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回列表</el-button>
      </el-empty>
    </section>
  </el-card>

  <ProtocolMediaViewer
    v-model:visible="paperViewerVisible"
    v-model:index="paperViewerIndex"
    :items="paperViewerItems"
  />
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, PictureFilled } from '@element-plus/icons-vue'
import { getBreadingApplication, getAdoptApplication, getAgreements } from '../api/services'
import ProtocolMediaViewer from './ProtocolMediaViewer.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const agreementLoading = ref(false)
const application = ref(null)
const agreements = ref([])
const paperViewerVisible = ref(false)
const paperViewerIndex = ref(0)

const applicationId = computed(() => String(route.params.id || ''))
const isBreadingType = computed(() => route.path.includes('/breading/'))
const isReject = computed(() => application.value?.status === 'REJECT')

const currentAgreement = computed(() => {
  if (!agreements.value.length) return null
  const sorted = [...agreements.value].sort(
    (a, b) => new Date(b?.updateTime || b?.createTime || 0) - new Date(a?.updateTime || a?.createTime || 0)
  )
  return sorted[0] || null
})

const paperFiles = computed(() => {
  const files = Array.isArray(currentAgreement.value?.files) ? currentAgreement.value.files : []
  return files.slice().sort((a, b) => Number(a?.page || 0) - Number(b?.page || 0))
})

const currentAgreementType = computed(() => {
  const explicit = String(currentAgreement.value?.type || '')
  if (explicit === 'ELECTRONIC' || explicit === 'PAPER') return explicit
  if (String(currentAgreement.value?.content || '').trim()) return 'ELECTRONIC'
  if (paperFiles.value.length) return 'PAPER'
  return ''
})

const hasAgreement = computed(() => {
  if (isBreadingType.value) return false
  const statuses = ['AGREEMENT_DRAFT', 'AGREEMENT_PENDING_CONFIRM', 'AGREEMENT_SIGNED']
  return statuses.includes(application.value?.status) && Boolean(currentAgreement.value)
})

const paperViewerItems = computed(() => paperFiles.value.map((file, index) => ({
  id: file.id,
  assetUrl: file.assetUrl,
  name: `协议第 ${file.page || index + 1} 页`,
  type: 'IMAGE',
})))

const statusOptions = [
  { label: '已提交', value: 'CREATE' },
  { label: '审核通过', value: 'PASS' },
  { label: '审核拒绝', value: 'REJECT' },
  { label: '协议草拟中', value: 'AGREEMENT_DRAFT' },
  { label: '待确认', value: 'AGREEMENT_PENDING_CONFIRM' },
  { label: '协议已签署', value: 'AGREEMENT_SIGNED' },
  { label: '回访中', value: 'TRACKING' },
  { label: '流程完成', value: 'FINISH' },
  { label: '已取消', value: 'CANCEL' },
]
const statusMap = Object.fromEntries(statusOptions.map((item) => [item.value, item.label]))

function avatarInitial(value) {
  return String(value || '用户').slice(0, 1)
}

function statusText(value) {
  return statusMap[value] || value || ''
}

function statusTagType(value) {
  if (value === 'PASS' || value === 'AGREEMENT_SIGNED' || value === 'FINISH') return 'success'
  if (value === 'REJECT' || value === 'CANCEL') return 'info'
  if (value === 'AGREEMENT_DRAFT' || value === 'TRACKING') return 'primary'
  if (value === 'AGREEMENT_PENDING_CONFIRM') return 'warning'
  return 'warning'
}

function agreementTypeText(value) {
  if (value === 'PAPER') return '纸质协议'
  if (value === 'ELECTRONIC') return '电子协议'
  return ''
}

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function goBack() {
  router.push({ name: 'console-adoption' })
}

function goPetProfile() {
  if (application.value?.petId) {
    router.push(`/pets/${application.value.petId}`)
  }
}

function openPaperPreview(index) {
  paperViewerIndex.value = index
  paperViewerVisible.value = true
}

async function loadApplication() {
  if (!applicationId.value) return
  loading.value = true
  try {
    const api = isBreadingType.value ? getBreadingApplication : getAdoptApplication
    application.value = await api(applicationId.value)
  } catch (error) {
    application.value = null
    ElMessage.warning(error?.message || '加载申请详情失败')
  } finally {
    loading.value = false
  }
}

async function loadAgreementsData() {
  if (!applicationId.value || isBreadingType.value) {
    agreements.value = []
    return
  }
  agreementLoading.value = true
  try {
    const result = await getAgreements({
      parentId: applicationId.value,
      parentType: 'ADOPT',
      size: 20,
      page: 1,
      sort: 'update_time',
      order: 'desc',
    })
    agreements.value = Array.isArray(result?.records) ? result.records : []
  } catch (error) {
    agreements.value = []
  } finally {
    agreementLoading.value = false
  }
}

async function loadPage() {
  await Promise.all([loadApplication(), loadAgreementsData()])
}

watch(() => route.params.id, loadPage, { immediate: true })
</script>

<style scoped>
.application-detail-panel {
  display: grid;
  gap: 16px;
}

.application-hero-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr;
  gap: 16px;
}

.application-pet-card,
.application-person-card {
  min-height: 150px;
}

.application-pet-card {
  display: flex;
  gap: 16px;
  align-items: center;
}

.application-pet-cover {
  width: 96px;
  height: 96px;
  min-height: 96px;
  border-radius: 18px;
  overflow: hidden;
  color: #c47a3a;
  background: linear-gradient(180deg, rgba(255, 244, 232, 0.98), rgba(255, 232, 211, 0.92));
  display: grid;
  place-items: center;
  flex: none;
}

.application-pet-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.application-pet-cover :deep(.el-icon) {
  font-size: 34px;
}

.application-pet-copy {
  display: grid;
  gap: 6px;
  min-width: 0;
}

.application-pet-copy span,
.application-pet-copy p,
.application-person-card p,
.application-person-row span,
.application-note {
  color: var(--muted);
}

.application-pet-copy strong {
  font-size: 20px;
  color: var(--text);
}

.application-person-card {
  display: grid;
  gap: 12px;
}

.application-person-card p {
  margin: 0;
}

.application-person-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.application-avatar {
  flex: none;
  background: rgba(255, 233, 214, 0.9);
  color: #8a4b1d;
}

.application-person-row div {
  display: grid;
  gap: 4px;
}

.application-person-row strong {
  color: var(--text);
}

.application-status-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.application-status-row > div {
  flex: 1;
  min-width: 0;
}

.application-section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.application-section-head h3 {
  margin: 0;
}

.application-note {
  margin: 0;
  line-height: 1.8;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.breading-section-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.adoption-agreement-text-box {
  border: 1px solid rgba(243, 223, 204, 0.9);
  border-radius: 14px;
  background: rgba(255, 253, 249, 0.96);
  padding: 16px;
  margin-top: 10px;
}

.adoption-paper-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 14px;
  margin-top: 10px;
}

.adoption-paper-card {
  border: 0;
  background: transparent;
  cursor: pointer;
  padding: 0;
  text-align: left;
}

.adoption-paper-folder {
  position: relative;
  min-height: 160px;
  border: 1px solid rgba(243, 223, 204, 0.95);
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(255, 249, 241, 0.98) 0%, rgba(255, 241, 224, 0.94) 100%);
  box-shadow: 0 10px 22px rgba(151, 96, 56, 0.08);
  overflow: hidden;
}

.adoption-paper-folder img {
  position: absolute;
  inset: 12px;
  width: calc(100% - 24px);
  height: calc(100% - 24px);
  object-fit: cover;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.8);
}

.adoption-paper-page-banner {
  position: absolute;
  right: 12px;
  bottom: 12px;
  padding: 6px 10px;
  border-radius: 10px 0 10px 0;
  background: rgba(93, 57, 39, 0.84);
  color: #fff;
  font-size: 12px;
  font-weight: 800;
  line-height: 1.2;
  box-shadow: 0 8px 18px rgba(46, 24, 12, 0.18);
}

@media (max-width: 1024px) {
  .application-hero-grid {
    grid-template-columns: 1fr;
  }
  .application-status-row {
    flex-direction: column;
  }
}
</style>
