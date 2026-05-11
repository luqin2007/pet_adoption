<template>
  <el-card class="profile-card" v-loading="loading || agreementLoading">
    <template #header>
      <div class="profile-card-header">
        <strong>领养申请详情</strong>
        <span>{{ application?.petName || '查看申请信息、协议和回访记录' }}</span>
      </div>
    </template>

    <section v-if="application" class="action-form-panel adoption-application-detail-panel">
      <div class="console-detail-toolbar">
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回列表</el-button>
        <el-tag :type="statusTagType(application.status)" effect="plain">{{ statusText(application.status) }}</el-tag>
      </div>

      <div class="adoption-hero-grid">
        <section class="console-detail-section adoption-pet-card">
          <div class="adoption-pet-cover">
            <img v-if="application.petCover" :src="application.petCover" :alt="application.petName || '宠物封面'" />
            <el-icon v-else><PictureFilled /></el-icon>
          </div>
          <div class="adoption-pet-copy">
            <span>宠物</span>
            <button class="table-primary-link adoption-pet-link" type="button" @click="goPetProfile">
              <strong>{{ application.petName || '未命名宠物' }}</strong>
            </button>
            <p>#{{ application.petId || '—' }}</p>
          </div>
        </section>

        <section class="console-detail-section adoption-person-card">
          <p>申请人</p>
          <div class="adoption-person-row">
            <el-avatar :size="42" :src="application.applicantAvatar">
              {{ avatarInitial(application.applicantName) }}
            </el-avatar>
            <div>
              <strong>{{ application.applicantName || '未命名用户' }}</strong>
              <span>{{ application.applicantPhone || '联系方式待补充' }}</span>
            </div>
          </div>
        </section>

        <section class="console-detail-section adoption-person-card">
          <p>审核人</p>
          <div v-if="application.applicationId" class="adoption-person-row">
            <el-avatar :size="42" :src="application.reviewerAvatar">
              {{ avatarInitial(application.applicantName) }}
            </el-avatar>
            <div>
              <strong>{{ application.reviewerName || '未命名用户' }}</strong>
            </div>
          </div>
        </section>
      </div>

      <section class="console-detail-section console-detail-section-wide">
        <dl class="console-detail-list console-detail-list-row adoption-status-row">
          <div><dt>申请状态</dt><dd>{{ statusText(application.status) }}</dd></div>
          <div><dt>审核时间</dt><dd>{{ formatDate(application.reviewTime) }}</dd></div>
          <div><dt>领养时间</dt><dd>{{ formatDate(application.adoptTime) }}</dd></div>
        </dl>
      </section>

      <section v-if="showStatusPanel" class="console-detail-section console-detail-section-wide adoption-status-panel">
        <template v-if="isReject">
          <h3>拒绝原因</h3>
          <p class="adoption-status-note">{{ application.rejectReason || '暂无拒绝原因' }}</p>
        </template>

        <template v-else-if="hasAgreement">
          <div class="adoption-section-head">
            <div>
              <h3>当前协议内容</h3>
              <p>{{ agreementSummary }}</p>
            </div>
            <div class="adoption-section-tags">
              <el-tag effect="plain">{{ agreementTypeText(currentAgreementType) }}</el-tag>
              <el-tag :type="currentAgreement.signTime ? 'success' : 'warning'" effect="plain">
                {{ currentAgreement.signTime ? '已签署' : '未签署' }}
              </el-tag>
            </div>
          </div>

          <div v-if="currentAgreementType === 'ELECTRONIC'" class="adoption-agreement-text-box">
            <p class="adoption-agreement-text">{{ currentAgreement.content || '暂无协议正文' }}</p>
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
                <div class="adoption-paper-tab" />
                <img :src="file.assetUrl" :alt="`协议第 ${file.page || index + 1} 页`" loading="lazy" />
              </div>
              <div class="adoption-paper-meta">
                <strong>第 {{ file.page || index + 1 }} 页</strong>
                <span>点击全屏查看</span>
              </div>
            </button>
          </div>
        </template>

        <template v-else-if="isTrackingStage">
          <el-collapse v-model="trackingPanels" class="adoption-detail-collapse">
            <el-collapse-item v-if="followTasks.length" title="回访记录" name="records">
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
                    <div><dt>回访时间</dt><dd>{{ formatDate(item.visitTime) }}</dd></div>
                    <div><dt>记录摘要</dt><dd>{{ item.summary || '暂无' }}</dd></div>
                    <div><dt>执行记录</dt><dd>{{ item.recordId ? `#${item.recordId}` : '暂无' }}</dd></div>
                  </dl>
                </article>
              </div>
            </el-collapse-item>
            <el-collapse-item v-if="hasAgreementBody" title="协议内容" name="agreement">
              <div class="adoption-agreement-content-collapsed">
                <div class="adoption-section-tags">
                  <el-tag effect="plain">{{ agreementTypeText(currentAgreementType) }}</el-tag>
                  <el-tag :type="currentAgreement.signTime ? 'success' : 'warning'" effect="plain">
                    {{ currentAgreement.signTime ? '已签署' : '未签署' }}
                  </el-tag>
                </div>
                <p v-if="currentAgreementType === 'ELECTRONIC'" class="adoption-agreement-text">
                  {{ currentAgreement.content || '暂无协议正文' }}
                </p>
                <div v-else-if="currentAgreementType === 'PAPER'" class="adoption-paper-grid">
                  <button
                    v-for="(file, index) in paperFiles"
                    :key="file.id"
                    type="button"
                    class="adoption-paper-card"
                    @click="openPaperPreview(index)"
                  >
                    <div class="adoption-paper-folder">
                      <div class="adoption-paper-tab" />
                      <img :src="file.assetUrl" :alt="`协议第 ${file.page || index + 1} 页`" loading="lazy" />
                    </div>
                    <div class="adoption-paper-meta">
                      <strong>第 {{ file.page || index + 1 }} 页</strong>
                      <span>点击全屏查看</span>
                    </div>
                  </button>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </template>
      </section>
    </section>

    <section v-else-if="!loading" class="pet-admin-section">
      <el-empty description="未找到领养申请">
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回列表</el-button>
      </el-empty>
    </section>
  </el-card>

  <MediaViewerOverlay
    v-model:visible="paperViewerVisible"
    v-model:index="paperViewerIndex"
    :items="paperViewerItems"
    :show-thumbs="false"
  />
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, PictureFilled } from '@element-plus/icons-vue'
import { getAdoptApplication, getAgreements } from '../api/services'
import MediaViewerOverlay from './MediaViewerOverlay.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const agreementLoading = ref(false)
const application = ref(null)
const agreements = ref([])
const paperViewerVisible = ref(false)
const paperViewerIndex = ref(0)
const trackingPanels = ref([])

const applicationId = computed(() => String(route.params.id || ''))
const followTasks = computed(() => {
  const list = Array.isArray(application.value?.followTasks) ? application.value.followTasks : []
  return list.slice().sort((a, b) => new Date(b?.planTime || b?.createTime || 0) - new Date(a?.planTime || a?.createTime || 0))
})
const currentAgreement = computed(() => {
  if (!agreements.value.length) return null
  return [...agreements.value].sort((a, b) => new Date(b?.updateTime || b?.createTime || 0) - new Date(a?.updateTime || a?.createTime || 0))[0] || null
})
const paperFiles = computed(() => {
  const files = Array.isArray(currentAgreement.value?.files) ? currentAgreement.value.files : []
  return files
    .slice()
    .sort((a, b) => Number(a?.page || 0) - Number(b?.page || 0))
})
const currentAgreementType = computed(() => {
  const explicit = String(currentAgreement.value?.type || '')
  if (explicit === 'ELECTRONIC' || explicit === 'PAPER') return explicit
  if (String(currentAgreement.value?.content || '').trim()) return 'ELECTRONIC'
  if (paperFiles.value.length) return 'PAPER'
  return ''
})
const paperViewerItems = computed(() => paperFiles.value.map((file, index) => ({
  id: file.id,
  assetUrl: file.assetUrl,
  name: `协议第 ${file.page || index + 1} 页`,
  type: 'IMAGE',
})))
const agreementSummary = computed(() => {
  if (!currentAgreement.value) return '暂无协议内容'
  const parts = [agreementTypeText(currentAgreementType.value), currentAgreement.value.signTime ? '已签署' : '未签署']
  return parts.join(' · ')
})
const isReject = computed(() => application.value?.status === 'REJECT')
const hasAgreement = computed(() => ['AGREEMENT_DRAFT', 'AGREEMENT_SIGNED'].includes(application.value?.status) && Boolean(currentAgreement.value))
const isTrackingStage = computed(() => ['TRACKING', 'FINISH'].includes(application.value?.status))
const hasAgreementBody = computed(() => {
  if (!currentAgreement.value) return false
  if (currentAgreementType.value === 'ELECTRONIC') return Boolean(String(currentAgreement.value?.content || '').trim())
  if (currentAgreementType.value === 'PAPER') return paperFiles.value.length > 0
  return Boolean(String(currentAgreement.value?.content || '').trim() || paperFiles.value.length)
})
const showStatusPanel = computed(() => {
  if (isReject.value) return Boolean(String(application.value?.rejectReason || '').trim())
  if (hasAgreement.value) return hasAgreementBody.value
  if (isTrackingStage.value) return Boolean(followTasks.value.length || hasAgreementBody.value)
  return false
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

function avatarInitial(value) {
  return String(value || '申').slice(0, 1)
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

function agreementTypeText(value) {
  const type = typeof value === 'string'
    ? value
    : value?.type || (value?.content ? 'ELECTRONIC' : 'PAPER')
  if (type === 'PAPER') return '纸制协议'
  if (type === 'ELECTRONIC') return '电子协议'
  return ''
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

function openPaperPreview(index) {
  paperViewerIndex.value = index
  paperViewerVisible.value = true
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

async function loadAgreements() {
  if (!applicationId.value) {
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
    ElMessage.warning(error?.message || '加载协议失败')
  } finally {
    agreementLoading.value = false
  }
}

async function loadPage() {
  await Promise.all([loadApplication(), loadAgreements()])
}

watch(() => route.params.id, loadPage, { immediate: true })
</script>

<style scoped>
.adoption-application-detail-panel {
  display: grid;
  gap: 18px;
}

.adoption-hero-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(0, 1fr);
  grid-template-rows: minmax(152px, auto) minmax(152px, auto);
  gap: 14px;
}

.adoption-pet-card {
  grid-row: 1 / span 2;
  display: grid;
  grid-template-columns: 176px minmax(0, 1fr);
  gap: 18px;
  align-items: center;
}

.adoption-pet-cover {
  height: 100%;
  min-height: 304px;
  border-radius: 16px;
  overflow: hidden;
  background: rgba(255, 248, 240, 0.92);
  border: 1px solid rgba(243, 223, 204, 0.95);
  display: grid;
  place-items: center;
}

.adoption-pet-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.adoption-pet-cover :deep(.el-icon) {
  font-size: 42px;
  color: var(--muted);
}

.adoption-pet-copy {
  min-width: 0;
  display: grid;
  gap: 6px;
}

.adoption-pet-copy span {
  color: var(--muted);
  font-size: 13px;
  font-weight: 700;
}

.adoption-pet-link {
  display: inline-flex;
  align-items: center;
  padding: 0;
  border: 0;
  background: transparent;
  font: inherit;
  cursor: pointer;
  text-align: left;
}

.adoption-pet-link strong {
  color: inherit;
  font-size: 26px;
  line-height: 1.25;
}

.adoption-pet-link:hover,
.adoption-pet-link:focus-visible {
  color: var(--primary-strong);
}

.adoption-pet-copy p {
  margin: 0;
  color: var(--muted);
  line-height: 1.7;
}

.adoption-person-card {
  min-width: 0;
  display: grid;
  gap: 14px;
}

.adoption-person-row {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.adoption-person-row strong {
  display: block;
  color: #5d3927;
  font-size: 18px;
  line-height: 1.3;
}

.adoption-person-row span {
  display: block;
  margin-top: 4px;
  color: var(--muted);
  line-height: 1.6;
}

.adoption-status-row {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.adoption-status-panel {
  display: grid;
  gap: 16px;
}

.adoption-status-note {
  margin: 0;
  color: #5d3927;
  line-height: 1.8;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.adoption-section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.adoption-section-head h3 {
  margin: 0;
}

.adoption-section-head p {
  margin: 6px 0 0;
  color: var(--muted);
  line-height: 1.6;
}

.adoption-section-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.adoption-agreement-text-box {
  border: 1px solid rgba(243, 223, 204, 0.9);
  border-radius: 14px;
  background: rgba(255, 253, 249, 0.96);
  padding: 16px;
}

.adoption-agreement-text {
  margin: 0;
  color: #5d3927;
  line-height: 1.9;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  font-size: 14px;
}

.adoption-paper-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 14px;
}

.adoption-paper-card {
  border: 0;
  background: transparent;
  cursor: pointer;
  padding: 0;
  text-align: left;
  display: grid;
  gap: 10px;
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

.adoption-paper-tab {
  position: absolute;
  left: 14px;
  top: 10px;
  width: 58px;
  height: 16px;
  border-radius: 8px 8px 4px 4px;
  background: linear-gradient(180deg, rgba(231, 122, 59, 0.42) 0%, rgba(231, 122, 59, 0.18) 100%);
}

.adoption-paper-folder img {
  position: absolute;
  inset: 34px 12px 12px;
  width: calc(100% - 24px);
  height: calc(100% - 46px);
  object-fit: cover;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.8);
}

.adoption-paper-meta {
  display: grid;
  gap: 2px;
  padding-left: 4px;
}

.adoption-paper-meta strong {
  color: #5d3927;
  font-size: 14px;
  line-height: 1.4;
}

.adoption-paper-meta span {
  color: var(--muted);
  font-size: 12px;
  line-height: 1.4;
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

.adoption-follow-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.adoption-follow-head strong {
  display: block;
  color: #5d3927;
  font-size: 15px;
  line-height: 1.4;
}

.adoption-follow-head span {
  color: var(--muted);
  line-height: 1.6;
}

.adoption-agreement-content-collapsed {
  display: grid;
  gap: 14px;
}

.adoption-detail-collapse :deep(.el-collapse-item__header) {
  font-size: 15px;
  font-weight: 700;
  color: #5d3927;
}

@media (max-width: 1120px) {
  .adoption-hero-grid {
    grid-template-columns: 1fr;
    grid-template-rows: auto;
  }

  .adoption-pet-card {
    grid-row: auto;
  }
}

@media (max-width: 720px) {
  .adoption-pet-card {
    grid-template-columns: 1fr;
  }

  .adoption-pet-cover {
    min-height: 220px;
  }

  .adoption-status-row {
    grid-template-columns: 1fr;
  }
}
</style>
