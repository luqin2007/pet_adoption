<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Check, Delete } from '@element-plus/icons-vue'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import {
  addDiagnosis,
  addTreatmentPlan,
  discardTreatmentPlan,
  getMedicalDetail,
  getMedicalDetails,
  getMedicalExaminations,
  updateMedicalDetail,
} from '../api/services'
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
const examinations = ref([])
const diagnosisDialogVisible = ref(false)
const treatmentDialogVisible = ref(false)
const addingDiagnosis = ref(false)
const addingTreatment = ref(false)
const discardingPlanId = ref('')

const loginUserId = computed(() => Number(userStore.profile?.id || 0))
const isDoctor = computed(() => hasRole(loginRole.value, ROLE.DOCTOR))
const canEdit = computed(() => {
  if (!detail.value) return false
  if (!isDoctor.value) return false
  if (detail.value.isCompleted || detail.value.isDiscard) return false
  return Number(detail.value.doctorId) === loginUserId.value
})
const canManageMedicalEntries = computed(() => {
  if (!detail.value) return false
  return isDoctor.value && !detail.value.isCompleted && !detail.value.isDiscard
})

const detailSummary = computed(() => detail.value?.summary || '')
const objectiveDiagnoses = computed(() => detail.value?.objectiveDiagnoses || [])
const activeTreatments = computed(() => (detail.value?.treatments || []).filter((item) => !item.isDiscard))

const editForm = reactive({
  summary: '', physicalExam: '', diagnosis: '', differential: '', exam: '', treatment: '', advice: '',
})

const diagnosisForm = reactive({
  result: '',
  examinations: [],
})

const treatmentForm = reactive({
  plan: '',
  startTime: '',
  endTime: '',
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
    const [res, examRes] = await Promise.all([
      getMedicalDetail(detailId.value),
      getMedicalExaminations(detailId.value).catch(() => []),
    ])
    detail.value = await withDetailSummary(res)
    examinations.value = Array.isArray(examRes) ? examRes : []
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

function goExaminations() {
  router.push(`/medical/detail/${detailId.value}/exams`)
}

function examTitle(item) {
  return item?.name || item?.text || item?.examType || '检查记录'
}

function resetDiagnosisForm() {
  diagnosisForm.result = ''
  diagnosisForm.examinations = []
}

async function openDiagnosisDialog() {
  resetDiagnosisForm()
  try {
    examinations.value = await getMedicalExaminations(detailId.value)
  } catch (error) {
    ElMessage.warning(error?.message || '加载检查记录失败')
  }
  diagnosisDialogVisible.value = true
}

async function submitDiagnosis() {
  if (addingDiagnosis.value) return
  if (!diagnosisForm.examinations.length) {
    ElMessage.warning('请选择检查记录')
    return
  }
  if (!diagnosisForm.result.trim()) {
    ElMessage.warning('请输入诊断结果')
    return
  }
  addingDiagnosis.value = true
  try {
    await addDiagnosis(detailId.value, {
      result: diagnosisForm.result.trim(),
      examinations: diagnosisForm.examinations,
    })
    ElMessage.success('检查诊断已添加')
    diagnosisDialogVisible.value = false
    await loadDetail()
  } catch (error) {
    ElMessage.warning(error?.message || '添加检查诊断失败')
  } finally {
    addingDiagnosis.value = false
  }
}

function openTreatmentDialog() {
  Object.assign(treatmentForm, {
    plan: '',
    startTime: new Date().toISOString(),
    endTime: '',
  })
  treatmentDialogVisible.value = true
}

async function submitTreatmentPlan() {
  if (addingTreatment.value) return
  if (!treatmentForm.plan.trim()) {
    ElMessage.warning('请输入治疗计划')
    return
  }
  addingTreatment.value = true
  try {
    await addTreatmentPlan(detailId.value, {
      plan: treatmentForm.plan.trim(),
      orders: [],
      startTime: treatmentForm.startTime,
      endTime: treatmentForm.endTime || undefined,
    })
    ElMessage.success('治疗计划已添加')
    Object.assign(treatmentForm, {
      plan: '',
      startTime: new Date().toISOString(),
      endTime: '',
    })
    await loadDetail()
  } catch (error) {
    ElMessage.warning(error?.message || '添加治疗计划失败')
  } finally {
    addingTreatment.value = false
  }
}

async function removeTreatmentPlan(item) {
  try {
    await ElMessageBox.confirm('确认删除该治疗计划？', '删除治疗计划', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    discardingPlanId.value = item.id
    await discardTreatmentPlan([item.id])
    ElMessage.success('治疗计划已删除')
    await loadDetail()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.warning(error?.message || '删除治疗计划失败')
  } finally {
    discardingPlanId.value = ''
  }
}

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

        <el-form v-if="editing" class="medical-detail-edit-form" label-position="top">
          <section class="medical-edit-section medical-edit-section-wide">
            <el-form-item label="摘要">
              <el-input v-model="editForm.summary" type="textarea" :autosize="{ minRows: 2 }" placeholder="简要概括本次就诊情况" />
            </el-form-item>
          </section>

          <section class="medical-edit-section">
            <el-form-item label="体格检查">
              <el-input v-model="editForm.physicalExam" type="textarea" :autosize="{ minRows: 3 }" placeholder="记录体格检查观察结果" />
            </el-form-item>
          </section>

          <section class="medical-edit-section">
            <el-form-item label="诊断">
              <el-input v-model="editForm.diagnosis" type="textarea" :autosize="{ minRows: 3 }" placeholder="填写诊断结论" />
            </el-form-item>
            <el-form-item label="鉴别诊断">
              <el-input v-model="editForm.differential" type="textarea" :autosize="{ minRows: 2 }" placeholder="填写需要排除或观察的情况" />
            </el-form-item>
          </section>

          <section class="medical-edit-section medical-edit-section-wide">
            <div class="medical-edit-grid">
              <el-form-item label="检查计划">
                <el-input v-model="editForm.exam" type="textarea" :autosize="{ minRows: 3 }" placeholder="填写后续检查安排" />
              </el-form-item>
              <el-form-item label="治疗方案">
                <el-input v-model="editForm.treatment" type="textarea" :autosize="{ minRows: 3 }" placeholder="填写治疗方案" />
              </el-form-item>
              <el-form-item label="医嘱">
                <el-input v-model="editForm.advice" type="textarea" :autosize="{ minRows: 3 }" placeholder="填写护理、复诊或用药建议" />
              </el-form-item>
            </div>
          </section>
        </el-form>

        <template v-else>
          <div class="soap-so-row">
            <div class="soap-section">
              <div class="detail-info-row"><span class="detail-label">摘要</span><span class="detail-value">{{ detailSummary || '' }}</span></div>
              <div class="detail-info-row"><span class="detail-label">主诉</span><span class="detail-value">{{ detail.description || '' }}</span></div>
              <div class="detail-info-row"><span class="detail-label">现病史</span><span class="detail-value">{{ detail.history || '' }}</span></div>
              <div class="detail-info-row"><span class="detail-label">既往史</span><span class="detail-value">{{ detail.pastHistory || '' }}</span></div>
              <div class="detail-info-row"><span class="detail-label">生活习惯</span><span class="detail-value">{{ detail.lifeHabit || '' }}</span></div>
            </div>

            <div class="soap-section">
              <div class="detail-info-row"><span class="detail-label">体重</span><span class="detail-value">{{ detail.weight ?? '' }} kg</span></div>
              <div class="detail-info-row"><span class="detail-label">体温</span><span class="detail-value">{{ detail.temperature ?? '' }} ℃</span></div>
              <div class="detail-info-row"><span class="detail-label">心率</span><span class="detail-value">{{ detail.heartRate ?? '' }} 次/分</span></div>
              <div class="detail-info-row"><span class="detail-label">呼吸频率</span><span class="detail-value">{{ detail.respiratoryRate ?? '' }} 次/分</span></div>
              <div class="detail-info-row"><span class="detail-label">体格检查</span><span class="detail-value">{{ detail.physicalExam || '' }}</span></div>
            </div>
          </div>

          <div class="soap-section">
            <div class="detail-info-row"><span class="detail-label">诊断</span><span class="detail-value">{{ detail.diagnosis || '' }}</span></div>
            <div class="detail-info-row"><span class="detail-label">鉴别诊断</span><span class="detail-value">{{ detail.differential || '' }}</span></div>
            <div class="medical-subsection-head">
              <strong>检查诊断</strong>
              <el-button v-if="canManageMedicalEntries" link type="primary" @click="openDiagnosisDialog">添加</el-button>
            </div>
            <div v-if="objectiveDiagnoses.length" class="medical-mini-list">
              <article v-for="item in objectiveDiagnoses" :key="item.id" class="medical-mini-card">
                <p>{{ item.result || '' }}</p>
                <div v-if="item.examinations?.length" class="medical-mini-links">
                  <span v-for="exam in item.examinations" :key="exam.id">{{ examTitle(exam) }}</span>
                </div>
              </article>
            </div>
          </div>

          <div class="soap-section">
            <div class="detail-info-row"><span class="detail-label">检查计划</span><span class="detail-value">{{ detail.exam || '' }}</span></div>
            <div class="detail-info-row">
              <span class="detail-label">治疗方案</span>
              <button class="detail-value detail-value-button" type="button" @click="openTreatmentDialog">
                {{ detail.treatment || (activeTreatments.length ? `${activeTreatments.length} 个具体计划` : '') }}
              </button>
            </div>
            <div class="detail-info-row"><span class="detail-label">医嘱</span><span class="detail-value">{{ detail.advice || '' }}</span></div>
          </div>
        </template>

        <div v-if="editing" class="action-form-actions">
          <el-button class="soft-btn" @click="editing = false">取消编辑</el-button>
          <el-button class="warm-btn" :loading="saving" @click="saveEdit">保存</el-button>
        </div>

        <div v-if="!editing" class="detail-bottom-actions">
          <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回</el-button>
          <el-button class="soft-btn" @click="goExaminations">检查</el-button>
          <el-button v-if="canEdit" class="warm-btn" :icon="Check" @click="startEdit">编辑</el-button>
        </div>
      </section>

      <el-empty v-else-if="!loading" description="病历不存在" />
    </main>

    <el-dialog v-model="diagnosisDialogVisible" title="添加检查诊断" width="680px" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="检查记录">
          <el-checkbox-group v-model="diagnosisForm.examinations" class="medical-check-list">
            <el-checkbox v-for="exam in examinations" :key="exam.id" :label="exam.id">
              {{ examTitle(exam) }} · {{ formatDate(exam.checkTime) }}
            </el-checkbox>
          </el-checkbox-group>
          <el-empty v-if="!examinations.length" description="暂无检查记录" />
        </el-form-item>
        <el-form-item label="诊断结果">
          <el-input v-model="diagnosisForm.result" type="textarea" :autosize="{ minRows: 4 }" placeholder="填写检查诊断结果" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="diagnosisDialogVisible = false">取消</el-button>
        <el-button class="warm-btn" :loading="addingDiagnosis" @click="submitDiagnosis">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="treatmentDialogVisible" title="治疗计划" width="760px">
      <section class="medical-plan-dialog">
        <article v-for="item in activeTreatments" :key="item.id" class="medical-mini-card">
          <div class="medical-plan-head">
            <strong>{{ item.plan }}</strong>
            <el-button v-if="canManageMedicalEntries" text type="danger" :icon="Delete" :loading="discardingPlanId === item.id" @click="removeTreatmentPlan(item)">删除</el-button>
          </div>
          <p>{{ formatDate(item.startTime) }} <span v-if="item.endTime">至 {{ formatDate(item.endTime) }}</span></p>
          <div v-if="item.orders?.length" class="medical-mini-links">
            <span v-for="order in item.orders" :key="order.id">{{ order.itemName || '物资' }} {{ order.count }}{{ order.unit }}</span>
          </div>
        </article>
        <el-empty v-if="!activeTreatments.length" description="暂无具体治疗计划" />

        <section v-if="canManageMedicalEntries" class="medical-plan-add">
          <el-input v-model="treatmentForm.plan" type="textarea" :autosize="{ minRows: 3 }" placeholder="新增治疗计划" />
          <div class="medical-plan-time-row">
            <el-date-picker v-model="treatmentForm.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss.SSS" placeholder="开始时间" />
            <el-date-picker v-model="treatmentForm.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss.SSS" placeholder="结束时间" />
            <el-button class="warm-btn" :loading="addingTreatment" @click="submitTreatmentPlan">添加</el-button>
          </div>
        </section>
      </section>
    </el-dialog>

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

.medical-detail-edit-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--line);
}

.medical-edit-section {
  min-width: 0;
  border: 1px solid rgba(243, 223, 204, 0.9);
  border-radius: 16px;
  background: rgba(255, 248, 240, 0.72);
  padding: 14px;
}

.medical-edit-section-wide {
  grid-column: 1 / -1;
}

.medical-edit-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.medical-detail-edit-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.medical-detail-edit-form :deep(.el-form-item__label) {
  color: #6a4a36;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.4;
  padding-bottom: 6px;
}

.medical-detail-edit-form :deep(.el-textarea__inner) {
  border-color: rgba(243, 223, 204, 0.95);
  border-radius: 12px;
  background: rgba(255, 253, 249, 0.95);
  color: var(--text);
  font-size: 14px;
  line-height: 1.7;
  padding: 10px 12px;
  resize: vertical;
  transition: border-color 200ms ease, box-shadow 200ms ease, background-color 200ms ease;
}

.medical-detail-edit-form :deep(.el-textarea__inner):focus {
  border-color: rgba(231, 122, 59, 0.58);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(231, 122, 59, 0.1);
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

.detail-value-button {
  border: none;
  background: transparent;
  padding: 0;
  text-align: left;
  cursor: pointer;
  font: inherit;
}

.detail-value-button:hover {
  color: var(--primary-strong);
}

.medical-subsection-head,
.medical-plan-head,
.medical-plan-time-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.medical-subsection-head {
  justify-content: space-between;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid var(--line);
}

.medical-subsection-head strong {
  color: #5d3927;
  font-size: 15px;
}

.medical-mini-list,
.medical-plan-dialog {
  display: grid;
  gap: 10px;
  margin-top: 10px;
}

.medical-mini-card {
  border: 1px solid rgba(243, 223, 204, 0.9);
  border-radius: 12px;
  background: rgba(255, 248, 240, 0.72);
  padding: 12px;
}

.medical-mini-card p {
  margin: 6px 0 0;
  color: var(--muted);
  line-height: 1.6;
}

.medical-mini-links {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.medical-mini-links span {
  border: 1px solid rgba(231, 122, 59, 0.2);
  border-radius: 999px;
  background: rgba(255, 253, 249, 0.9);
  color: var(--primary-strong);
  padding: 4px 9px;
  font-size: 12px;
}

.medical-check-list {
  display: grid;
  gap: 8px;
  width: 100%;
}

.medical-plan-head {
  justify-content: space-between;
}

.medical-plan-head strong {
  color: #5d3927;
  line-height: 1.5;
}

.medical-plan-add {
  border-top: 1px solid var(--line);
  margin-top: 8px;
  padding-top: 14px;
}

.medical-plan-time-row {
  flex-wrap: wrap;
  margin-top: 10px;
}

@media (max-width: 860px) {
  .medical-detail-edit-form,
  .medical-edit-grid,
  .soap-so-row {
    grid-template-columns: 1fr;
  }

  .medical-plan-time-row {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
