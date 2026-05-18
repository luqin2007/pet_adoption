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
          <div v-if="currentAgreementType === 'ELECTRONIC' && !isEditing" class="adoption-agreement-text-box">
            <p class="application-note">{{ currentAgreement.content || '暂无协议正文' }}</p>
          </div>
          <div v-if="currentAgreementType === 'ELECTRONIC' && isEditing">
            <el-input
              v-model="agreementEditContent"
              type="textarea"
              :autosize="{ minRows: 12, maxRows: 20 }"
              placeholder="填写电子协议正文"
            />
          </div>
          <div v-if="currentAgreementType === 'PAPER' && !isEditing" class="adoption-paper-grid">
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
          <div v-if="currentAgreementType === 'PAPER' && isEditing" class="agreement-editor-section">
            <div class="agreement-editor-actions">
              <input ref="agreementFileInputRef" class="profile-avatar-input" type="file" accept="image/*" @change="uploadAgreementPaperFile" />
              <el-button class="soft-btn" :icon="Upload" :loading="agreementUploading" @click="chooseAgreementFile">添加扫描件</el-button>
            </div>
            <div v-if="agreementEditFiles.length" class="agreement-file-list">
              <article v-for="(file, index) in agreementEditFiles" :key="file.id" class="agreement-file-card">
                <a :href="file.assetUrl" target="_blank" rel="noreferrer">
                  <img :src="file.assetUrl" :alt="`协议第 ${index + 1} 页`" />
                </a>
                <div class="agreement-file-card-body">
                  <strong>第 {{ index + 1 }} 页</strong>
                  <div class="agreement-file-actions">
                    <el-button text :icon="ArrowUp" :disabled="index === 0" @click="agreementMoveFile(index, -1)">上移</el-button>
                    <el-button text :icon="ArrowDown" :disabled="index === agreementEditFiles.length - 1" @click="agreementMoveFile(index, 1)">下移</el-button>
                    <el-button text type="danger" :icon="Delete" @click="agreementRemoveFile(file)">删除</el-button>
                  </div>
                </div>
              </article>
            </div>
          </div>
          <div class="agreement-footer-actions">
            <template v-if="!isEditing">
              <el-button v-if="canEdit" class="warm-btn" :icon="Edit" @click="startEditing">编辑</el-button>
              <el-button v-if="canSign" class="warm-btn" :icon="Upload" @click="signDialogVisible = true">签订</el-button>
            </template>
            <template v-else>
              <el-button class="soft-btn" @click="cancelEditing">取消</el-button>
              <el-button class="warm-btn" :loading="agreementSaving" @click="saveAgreement">保存</el-button>
            </template>
            <el-button v-if="canConfirm" class="warm-btn" @click="submitConfirmAgreement">同意</el-button>
            <el-button v-if="canConfirm" class="soft-btn" style="color: var(--danger)" @click="rejectDialogVisible = true">拒绝</el-button>
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

  <el-dialog v-model="signDialogVisible" title="签署协议" width="520px" @closed="clearSignFile">
    <section class="sign-dialog">
      <input ref="signInputRef" class="profile-avatar-input" type="file" accept="image/*" @change="handleSignFileChange" />
      <button class="sign-uploader" type="button" @click="chooseSignFile">
        <img v-if="signPreviewUrl" :src="signPreviewUrl" alt="签名预览" />
        <template v-else>
          <el-icon><Upload /></el-icon>
          <strong>选择签名图片</strong>
          <span>支持图片格式，用于签署当前协议</span>
        </template>
      </button>
    </section>
    <template #footer>
      <el-button @click="signDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" :icon="Check" :disabled="!signFile" :loading="signing" @click="submitSignAgreement">提交签名</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="460px">
    <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请输入拒绝原因" />
    <template #footer>
      <el-button @click="rejectDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" :loading="confirming" @click="submitRejectAgreement">确认拒绝</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, ArrowLeft, ArrowUp, Check, Delete, Edit, PictureFilled, Upload } from '@element-plus/icons-vue'
import { confirmAgreementSign, deleteAgreementFile, getAdoptApplication, getAgreements, getBreadingApplication, reorderAgreementFiles, signAgreement, updateAgreement, uploadAgreement } from '../api/services'
import { useUserStore } from '../stores/user'
import { ROLE, hasRole } from '../utils/roles'
import ProtocolMediaViewer from './ProtocolMediaViewer.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const agreementLoading = ref(false)
const application = ref(null)
const agreements = ref([])
const paperViewerVisible = ref(false)
const paperViewerIndex = ref(0)
const isEditing = ref(false)
const agreementEditContent = ref('')
const agreementSaving = ref(false)
const agreementUploading = ref(false)
const agreementFileInputRef = ref(null)
const signDialogVisible = ref(false)
const signFile = ref(null)
const signPreviewUrl = ref('')
const signInputRef = ref(null)
const signing = ref(false)
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const confirming = ref(false)

const applicationId = computed(() => String(route.params.id || ''))
const loginUserId = computed(() => String(userStore.profile?.id || ''))
const loginRole = computed(() => Number(userStore.profile?.role || 0))
const isBreadingType = computed(() => route.path.includes('/breading/'))
const isReject = computed(() => application.value?.status === 'REJECT')
const isWorker = computed(() => hasRole(loginRole.value, ROLE.WORKER) || hasRole(loginRole.value, ROLE.ADMIN))
const isApplicant = computed(() => loginUserId.value && String(application.value?.applicantId) === loginUserId.value)
const isReviewer = computed(() => loginUserId.value && String(application.value?.reviewerId) === loginUserId.value)

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

const agreementEditFiles = ref([])

const currentAgreementType = computed(() => {
  const explicit = String(currentAgreement.value?.type || '')
  if (explicit === 'ELECTRONIC' || explicit === 'PAPER') return explicit
  if (String(currentAgreement.value?.content || '').trim()) return 'ELECTRONIC'
  if (paperFiles.value.length) return 'PAPER'
  return ''
})

const hasAgreement = computed(() => {
  const statuses = ['AGREEMENT_DRAFT', 'AGREEMENT_PENDING_CONFIRM', 'AGREEMENT_SIGNED']
  return statuses.includes(application.value?.status) && Boolean(currentAgreement.value)
})

const canEdit = computed(() =>
  application.value?.status === 'AGREEMENT_DRAFT' && (isApplicant.value || isReviewer.value)
)

const canSign = computed(() =>
  application.value?.status === 'AGREEMENT_DRAFT' && isApplicant.value && Boolean(currentAgreement.value?.id) && !currentAgreement.value?.signTime
)

const canConfirm = computed(() =>
  application.value?.status === 'AGREEMENT_PENDING_CONFIRM' && isWorker.value && isReviewer.value && Boolean(currentAgreement.value?.id)
)

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

function startEditing() {
  agreementEditContent.value = currentAgreement.value?.content || ''
  agreementEditFiles.value = paperFiles.value.map((f) => ({ ...f }))
  isEditing.value = true
}

function cancelEditing() {
  isEditing.value = false
  agreementEditContent.value = ''
  agreementEditFiles.value = []
}

async function saveAgreement() {
  if (!currentAgreement.value?.id) return
  agreementSaving.value = true
  try {
    if (currentAgreementType.value === 'ELECTRONIC') {
      await updateAgreement(currentAgreement.value.id, { content: agreementEditContent.value })
    } else {
      const fileOrder = agreementEditFiles.value.map((f) => f.id)
      if (fileOrder.length) {
        await reorderAgreementFiles(currentAgreement.value.id, fileOrder)
      }
    }
    ElMessage.success('协议已保存')
    isEditing.value = false
    await loadAgreementsData()
  } catch (error) {
    ElMessage.warning(error?.message || '保存协议失败')
  } finally {
    agreementSaving.value = false
  }
}

function chooseAgreementFile() {
  agreementFileInputRef.value?.click()
}

async function uploadAgreementPaperFile(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片格式的扫描件')
    return
  }
  agreementUploading.value = true
  try {
    await uploadAgreement(currentAgreement.value.id, { file, page: agreementEditFiles.value.length + 1 })
    ElMessage.success('扫描件已上传')
    await loadAgreementsData()
    agreementEditFiles.value = paperFiles.value.map((f) => ({ ...f }))
  } catch (error) {
    ElMessage.warning(error?.message || '上传扫描件失败')
  } finally {
    agreementUploading.value = false
  }
}

function agreementMoveFile(index, direction) {
  const files = [...agreementEditFiles.value]
  const target = index + direction
  if (target < 0 || target >= files.length) return
  ;[files[index], files[target]] = [files[target], files[index]]
  agreementEditFiles.value = files
}

async function agreementRemoveFile(file) {
  try {
    await ElMessageBox.confirm('确认删除该扫描件吗？', '删除扫描件', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteAgreementFile(currentAgreement.value.id, file.id)
    ElMessage.success('扫描件已删除')
    await loadAgreementsData()
    agreementEditFiles.value = paperFiles.value.map((f) => ({ ...f }))
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.warning(error?.message || '删除扫描件失败')
    }
  }
}

function chooseSignFile() {
  signInputRef.value?.click()
}

function clearSignFile() {
  if (signPreviewUrl.value) {
    URL.revokeObjectURL(signPreviewUrl.value)
  }
  signPreviewUrl.value = ''
  signFile.value = null
  if (signInputRef.value) signInputRef.value.value = ''
}

function handleSignFileChange(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片格式的签名文件')
    return
  }
  clearSignFile()
  signFile.value = file
  signPreviewUrl.value = URL.createObjectURL(file)
}

async function submitSignAgreement() {
  if (!currentAgreement.value?.id || !signFile.value || signing.value) return
  signing.value = true
  try {
    await signAgreement(currentAgreement.value.id, signFile.value)
    ElMessage.success('签名已上传，等待工作人员确认')
    signDialogVisible.value = false
    clearSignFile()
    await loadPage()
  } catch (error) {
    ElMessage.warning(error?.message || '签署协议失败')
  } finally {
    signing.value = false
  }
}

async function submitConfirmAgreement() {
  if (!currentAgreement.value?.id || confirming.value) return
  confirming.value = true
  try {
    await confirmAgreementSign(currentAgreement.value.id, { agree: true })
    ElMessage.success('协议已确认签署')
    await loadPage()
  } catch (error) {
    ElMessage.warning(error?.message || '确认签署失败')
  } finally {
    confirming.value = false
  }
}

async function submitRejectAgreement() {
  if (!currentAgreement.value?.id || !rejectReason.value.trim() || confirming.value) return
  confirming.value = true
  try {
    await confirmAgreementSign(currentAgreement.value.id, { agree: false, reason: rejectReason.value.trim() })
    ElMessage.success('协议已拒绝')
    rejectDialogVisible.value = false
    rejectReason.value = ''
    await loadPage()
  } catch (error) {
    ElMessage.warning(error?.message || '拒绝协议失败')
  } finally {
    confirming.value = false
  }
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
  if (!applicationId.value) {
    agreements.value = []
    return
  }
  agreementLoading.value = true
  try {
    const result = await getAgreements({
      parentId: applicationId.value,
      parentType: isBreadingType.value ? 'BREADING' : 'ADOPT',
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
  isEditing.value = false
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

.agreement-footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}

.agreement-editor-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.agreement-file-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.agreement-file-card {
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid rgba(243, 223, 204, 0.9);
  border-radius: 14px;
  background: rgba(255, 253, 249, 0.96);
  padding: 10px 14px;
}

.agreement-file-card a {
  flex: none;
}

.agreement-file-card img {
  width: 80px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  display: block;
  background: rgba(255, 248, 240, 0.92);
}

.agreement-file-card-body {
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  min-width: 0;
}

.agreement-file-card-body strong {
  font-size: 13px;
  color: #5d3927;
  white-space: nowrap;
}

.agreement-file-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.sign-dialog {
  display: grid;
  gap: 14px;
}

.sign-uploader {
  width: 100%;
  min-height: 220px;
  border: 1px dashed rgba(231, 122, 59, 0.58);
  border-radius: 16px;
  background: rgba(255, 248, 240, 0.88);
  color: #6c4834;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 8px;
  cursor: pointer;
  transition: border-color 0.2s ease, background-color 0.2s ease;
}

.sign-uploader:hover {
  border-color: rgba(231, 122, 59, 0.9);
  background: rgba(255, 244, 232, 0.98);
}

.sign-uploader img {
  width: 100%;
  max-height: 300px;
  object-fit: contain;
  border-radius: 12px;
  background: #fff;
}

.sign-uploader :deep(.el-icon) {
  color: var(--primary-strong);
  font-size: 30px;
}

.sign-uploader strong {
  font-size: 18px;
}

.sign-uploader span {
  color: var(--muted);
  font-size: 14px;
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
