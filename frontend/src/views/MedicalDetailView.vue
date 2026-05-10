<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check, ArrowDown } from '@element-plus/icons-vue'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { getMedicalDetail, getMedicalDetails, updateMedicalDetail } from '../api/services'
import { useConsoleGuards } from '../composables/useConsoleGuards'
import { useUserStore } from '../stores/user'
import { ROLE, hasRole } from '../utils/roles'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { loginRole } = useConsoleGuards()

const detailId = computed(() => route.params.id)
const loading = ref(false)
const editing = ref(false)
const saving = ref(false)
const detail = ref(null)
const diagnosisExpanded = ref(false)
const showAllDiagnoses = ref(false)

const loginUserId = computed(() => Number(userStore.profile?.id || 0))
const isDoctor = computed(() => hasRole(loginRole.value, ROLE.DOCTOR))
const canEdit = computed(() => {
  if (!detail.value) return false
  if (!isDoctor.value) return false
  if (detail.value.isCompleted || detail.value.isDiscard) return false
  return Number(detail.value.doctorId) === loginUserId.value
})

const displayedDiagnoses = computed(() => {
  const list = detail.value?.objectiveDiagnoses || []
  if (showAllDiagnoses.value || list.length <= 3) return list
  return list.slice(0, 3)
})

const hasMoreDiagnoses = computed(() => {
  return (detail.value?.objectiveDiagnoses?.length || 0) > 3
})

const detailSummary = computed(() => detail.value?.summary || '')

const editForm = reactive({
  summary: '', physicalExam: '', diagnosis: '', differential: '', exam: '', treatment: '', advice: '',
})

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleString('zh-CN')
}

function statusText() {
  if (detail.value?.isDiscard) return '已废弃'
  if (detail.value?.isCompleted) return '已完成'
  return '进行中'
}

function statusTagType() {
  if (detail.value?.isDiscard) return 'info'
  if (detail.value?.isCompleted) return 'success'
  return 'warning'
}

async function loadDetail() {
  if (!detailId.value) return
  loading.value = true
  try {
    const res = await getMedicalDetail(detailId.value)
    detail.value = await withDetailSummary(res)
    diagnosisExpanded.value = (detail.value?.objectiveDiagnoses?.length || 0) <= 3
  } catch (error) { ElMessage.warning(error?.message || '加载病历失败') }
  finally { loading.value = false }
}

async function withDetailSummary(value) {
  if (!value || value.summary || !value.recordId) return value || null
  try {
    const res = await getMedicalDetails({ record: [value.recordId], page: 1, size: 20 })
    const item = (res?.records || []).find((record) => String(record.id) === String(value.id))
    return { ...value, summary: item?.summary || '' }
  } catch {
    return value
  }
}

function startEdit() {
  if (!detail.value) return
  Object.assign(editForm, {
    summary: detail.value.summary || '', physicalExam: detail.value.physicalExam || '',
    diagnosis: detail.value.diagnosis || '', differential: detail.value.differential || '',
    exam: detail.value.exam || '', treatment: detail.value.treatment || '', advice: detail.value.advice || '',
  })
  editing.value = true
}

async function saveEdit() {
  if (!detailId.value || saving.value) return
  saving.value = true
  try {
    const payload = {
      doctorId: loginUserId.value, isCompleted: detail.value.isCompleted, summary: editForm.summary,
      physicalExam: editForm.physicalExam || undefined, diagnosis: editForm.diagnosis || undefined,
      differential: editForm.differential || undefined, exam: editForm.exam || undefined,
      treatment: editForm.treatment || undefined, advice: editForm.advice || undefined,
    }
    const res = await updateMedicalDetail(detailId.value, payload)
    detail.value = await withDetailSummary(res || detail.value)
    editing.value = false
    ElMessage.success('病历已更新')
  } catch (error) { ElMessage.warning(error?.message || '保存失败') }
  finally { saving.value = false }
}

function goBack() { router.back() }

onMounted(() => { loadDetail() })
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main action-form-page" v-loading="loading">
      <section v-if="detail" class="action-form-panel">
        <header class="form-section-header detail-header">
          <div>
            <h2 class="detail-pet-name">{{ detail.name || '未命名' }}</h2>
            <p class="detail-pet-breed">{{ detail.type || '宠物' }} · {{ detail.breed || detail.sex || '' }}</p>
            <div class="detail-tags">
              <el-tag size="small" effect="plain">{{ detail.username || '' }}</el-tag>
              <el-tag size="small" effect="plain">{{ formatDate(detail.createTime) }}</el-tag>
              <el-tag size="small" effect="plain" :type="statusTagType()">{{ statusText() }}</el-tag>
            </div>
          </div>
        </header>

        <div class="soap-so-row">
          <!-- S Subjective -->
          <div class="soap-section">
            <div v-if="editing" class="soap-edit">
              <el-form-item label="摘要"><el-input v-model="editForm.summary" type="textarea" :autosize="{ minRows: 2 }" /></el-form-item>
            </div>
            <div v-else>
              <div class="detail-info-row"><span class="detail-label">摘要</span><span class="detail-value">{{ detailSummary || '—' }}</span></div>
              <div class="detail-info-row"><span class="detail-label">主诉</span><span class="detail-value">{{ detail.description || '' }}</span></div>
              <div class="detail-info-row"><span class="detail-label">现病史</span><span class="detail-value">{{ detail.history || '' }}</span></div>
              <div class="detail-info-row"><span class="detail-label">既往史</span><span class="detail-value">{{ detail.pastHistory || '' }}</span></div>
              <div class="detail-info-row"><span class="detail-label">生活习惯</span><span class="detail-value">{{ detail.lifeHabit || '' }}</span></div>
            </div>
          </div>

          <!-- O Objective -->
          <div class="soap-section">
            <div v-if="editing" class="soap-edit">
              <el-form-item label="体格检查"><el-input v-model="editForm.physicalExam" type="textarea" :autosize="{ minRows: 2 }" /></el-form-item>
            </div>
            <div v-else>
              <div class="detail-info-row"><span class="detail-label">体重</span><span class="detail-value">{{ detail.weight ?? '' }} kg</span></div>
              <div class="detail-info-row"><span class="detail-label">体温</span><span class="detail-value">{{ detail.temperature ?? '' }} ℃</span></div>
              <div class="detail-info-row"><span class="detail-label">心率</span><span class="detail-value">{{ detail.heartRate ?? '' }} 次/分</span></div>
              <div class="detail-info-row"><span class="detail-label">呼吸频率</span><span class="detail-value">{{ detail.respiratoryRate ?? '' }} 次/分</span></div>
              <div class="detail-info-row"><span class="detail-label">体格检查</span><span class="detail-value">{{ detail.physicalExam || '' }}</span></div>
            </div>
          </div>
        </div>

        <!-- 检查结果 (Objective Diagnoses) -->
        <div v-if="detail.objectiveDiagnoses?.length" class="soap-section">
          <div class="collapse-trigger" @click="diagnosisExpanded = !diagnosisExpanded">
            <span>检查结果</span>
            <el-icon :class="{ 'is-rotated': diagnosisExpanded }"><ArrowDown /></el-icon>
          </div>
          <div v-show="diagnosisExpanded" class="diagnosis-list">
            <div v-for="(item, index) in displayedDiagnoses" :key="index" class="detail-info-row">
              <span class="detail-label">{{ item.name || '项目' }}</span>
              <span class="detail-value">{{ item.result || item.value || '' }}</span>
            </div>
            <el-button v-if="hasMoreDiagnoses && !showAllDiagnoses" link type="primary" size="small" @click="showAllDiagnoses = true">
              展开全部 ({{ detail.objectiveDiagnoses.length - 3 }} 项)
            </el-button>
            <el-button v-if="showAllDiagnoses" link type="primary" size="small" @click="showAllDiagnoses = false">
              收起
            </el-button>
          </div>
        </div>

        <!-- A Assessment -->
        <div class="soap-section">
          <div v-if="editing" class="soap-edit">
            <el-form-item label="诊断"><el-input v-model="editForm.diagnosis" type="textarea" :autosize="{ minRows: 2 }" /></el-form-item>
            <el-form-item label="鉴别诊断"><el-input v-model="editForm.differential" type="textarea" :autosize="{ minRows: 2 }" /></el-form-item>
          </div>
          <div v-else>
            <div class="detail-info-row"><span class="detail-label">诊断</span><span class="detail-value">{{ detail.diagnosis || '' }}</span></div>
            <div class="detail-info-row"><span class="detail-label">鉴别诊断</span><span class="detail-value">{{ detail.differential || '' }}</span></div>
          </div>
        </div>

        <!-- P Plan -->
        <div class="soap-section">
          <div v-if="editing" class="soap-edit">
            <el-form-item label="检查计划"><el-input v-model="editForm.exam" type="textarea" :autosize="{ minRows: 2 }" /></el-form-item>
            <el-form-item label="治疗方案"><el-input v-model="editForm.treatment" type="textarea" :autosize="{ minRows: 2 }" /></el-form-item>
            <el-form-item label="医嘱"><el-input v-model="editForm.advice" type="textarea" :autosize="{ minRows: 2 }" /></el-form-item>
          </div>
          <div v-else>
            <div class="detail-info-row"><span class="detail-label">检查计划</span><span class="detail-value">{{ detail.exam || '' }}</span></div>
            <div class="detail-info-row"><span class="detail-label">治疗方案</span><span class="detail-value">{{ detail.treatment || '' }}</span></div>
            <div class="detail-info-row"><span class="detail-label">医嘱</span><span class="detail-value">{{ detail.advice || '' }}</span></div>
            <div v-if="detail.treatments?.length" class="diagnosis-list" style="margin-top: 12px;">
              <div v-for="(item, index) in detail.treatments" :key="index" class="detail-info-row">
                <span class="detail-label">{{ item.name || '治疗项' }}</span>
                <span class="detail-value">{{ item.description || item.detail || '' }}</span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="editing" class="action-form-actions">
          <el-button class="soft-btn" @click="editing = false">取消编辑</el-button>
          <el-button class="warm-btn" :loading="saving" @click="saveEdit">保存</el-button>
        </div>

        <div v-if="!editing" class="detail-bottom-actions">
          <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回</el-button>
          <el-button v-if="canEdit" class="warm-btn" :icon="Check" @click="startEdit">编辑</el-button>
        </div>
      </section>

      <el-empty v-else-if="!loading" description="病历不存在" />
    </main>

    <AppFooter />
  </div>
</template>

<style scoped>
.detail-header {
  margin-bottom: 20px;
}

.detail-pet-name {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.detail-pet-breed {
  font-size: 13px;
  color: var(--muted);
  margin: 4px 0 0;
}

.detail-tags {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}

.detail-bottom-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 28px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.soap-so-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.soap-so-row .soap-section {
  margin-top: 0;
  padding-top: 0;
  border-top: none;
}

.soap-section {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.soap-section h3 {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-color-warning);
  margin: 0 0 12px;
}

.soap-edit {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.soap-edit :deep(.el-form-item__label) {
  font-size: 13px;
  color: var(--el-text-color-regular);
  font-weight: 500;
  padding-bottom: 4px;
}

.soap-edit :deep(.el-textarea__inner) {
  font-size: 14px;
  line-height: 1.7;
  border-radius: 8px;
  padding: 10px 12px;
  resize: vertical;
  background: #fcfcfc;
  transition: border-color 200ms ease, background 200ms ease;
}

.soap-edit :deep(.el-textarea__inner):focus {
  background: #fff;
}

.detail-info-row {
  display: flex;
  gap: 12px;
  padding: 6px 0;
  font-size: 14px;
  line-height: 1.6;
}

.detail-label {
  flex-shrink: 0;
  width: 72px;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

.detail-value {
  color: var(--el-text-color-primary);
  white-space: pre-wrap;
}

.collapse-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  color: var(--el-color-warning);
  user-select: none;
}

.collapse-trigger .el-icon {
  transition: transform 200ms ease;
}

.collapse-trigger .el-icon.is-rotated {
  transform: rotate(180deg);
}

.diagnosis-list {
  margin-top: 10px;
  padding-left: 4px;
}
</style>
