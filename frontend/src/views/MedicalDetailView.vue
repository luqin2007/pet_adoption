<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check } from '@element-plus/icons-vue'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { getMedicalDetail, updateMedicalDetail } from '../api/services'
import { useConsoleGuards } from '../composables/useConsoleGuards'
import { useUserStore } from '../stores/user'
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

const loginUserId = computed(() => Number(userStore.profile?.id || 0))
const canEdit = computed(() => {
  if (!detail.value) return false
  const isDoctor = (loginRole.value & 4) === 4
  return isDoctor && Number(detail.value.doctorId) === loginUserId.value
})

const editForm = reactive({
  summary: '', physicalExam: '', diagnosis: '', differential: '', exam: '', treatment: '', advice: '',
})

async function loadDetail() {
  if (!detailId.value) return
  loading.value = true
  try {
    const res = await getMedicalDetail(detailId.value)
    detail.value = res || null
  } catch (error) { ElMessage.warning(error?.message || '加载病历失败') }
  finally { loading.value = false }
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
    detail.value = res || detail.value
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
        <header class="form-section-header">
          <h2>病历详情</h2>
          <div style="display:flex;gap:8px;margin-top:8px">
            <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回</el-button>
            <el-button v-if="canEdit && !editing" class="warm-btn" :icon="Check" @click="startEdit">编辑</el-button>
          </div>
        </header>

        <div class="detail-meta-row">
          <div class="detail-meta-item"><span class="detail-label">宠物</span><span class="detail-value">{{ detail.name || '—' }} · {{ detail.type || '' }} · {{ detail.sex || '' }}</span></div>
          <div class="detail-meta-item"><span class="detail-label">兽医</span><span class="detail-value">{{ detail.username || '—' }}</span></div>
          <div class="detail-meta-item"><span class="detail-label">创建时间</span><span class="detail-value">{{ detail.createTime ? new Date(detail.createTime).toLocaleString('zh-CN') : '—' }}</span></div>
          <div class="detail-meta-item"><span class="detail-label">状态</span><span class="detail-value">{{ detail.isCompleted ? '已完成' : '进行中' }}</span></div>
        </div>

        <div class="soap-so-row">
          <!-- S Subjective -->
          <div class="soap-section">
            <div v-if="editing" class="soap-edit">
              <el-form-item label="摘要"><el-input v-model="editForm.summary" /></el-form-item>
            </div>
            <div v-else>
              <div class="detail-info-row"><span class="detail-label">摘要</span><span class="detail-value">{{ detail.summary || '—' }}</span></div>
              <div class="detail-info-row"><span class="detail-label">主诉</span><span class="detail-value">{{ detail.description || '—' }}</span></div>
              <div class="detail-info-row"><span class="detail-label">现病史</span><span class="detail-value">{{ detail.history || '—' }}</span></div>
              <div class="detail-info-row"><span class="detail-label">既往史</span><span class="detail-value">{{ detail.pastHistory || '—' }}</span></div>
              <div class="detail-info-row"><span class="detail-label">生活习惯</span><span class="detail-value">{{ detail.lifeHabit || '—' }}</span></div>
            </div>
          </div>

          <!-- O Objective -->
          <div class="soap-section">
            <div v-if="editing" class="soap-edit">
              <el-form-item label="体格检查"><el-input v-model="editForm.physicalExam" type="textarea" :autosize="{ minRows: 2 }" /></el-form-item>
            </div>
            <div v-else>
              <div class="detail-info-row"><span class="detail-label">体重</span><span class="detail-value">{{ detail.weight ?? '—' }} kg</span></div>
              <div class="detail-info-row"><span class="detail-label">体温</span><span class="detail-value">{{ detail.temperature ?? '—' }} ℃</span></div>
              <div class="detail-info-row"><span class="detail-label">心率</span><span class="detail-value">{{ detail.heartRate ?? '—' }} 次/分</span></div>
              <div class="detail-info-row"><span class="detail-label">呼吸频率</span><span class="detail-value">{{ detail.respiratoryRate ?? '—' }} 次/分</span></div>
              <div class="detail-info-row"><span class="detail-label">体格检查</span><span class="detail-value">{{ detail.physicalExam || '—' }}</span></div>
            </div>
          </div>
        </div>

        <!-- A Assessment -->
        <div class="soap-section">
          <div v-if="editing" class="soap-edit">
            <el-form-item label="诊断"><el-input v-model="editForm.diagnosis" type="textarea" :autosize="{ minRows: 2 }" /></el-form-item>
            <el-form-item label="鉴别诊断"><el-input v-model="editForm.differential" type="textarea" :autosize="{ minRows: 2 }" /></el-form-item>
          </div>
          <div v-else>
            <div class="detail-info-row"><span class="detail-label">诊断</span><span class="detail-value">{{ detail.diagnosis || '—' }}</span></div>
            <div class="detail-info-row"><span class="detail-label">鉴别诊断</span><span class="detail-value">{{ detail.differential || '—' }}</span></div>
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
            <div class="detail-info-row"><span class="detail-label">检查计划</span><span class="detail-value">{{ detail.exam || '—' }}</span></div>
            <div class="detail-info-row"><span class="detail-label">治疗方案</span><span class="detail-value">{{ detail.treatment || '—' }}</span></div>
            <div class="detail-info-row"><span class="detail-label">医嘱</span><span class="detail-value">{{ detail.advice || '—' }}</span></div>
          </div>
        </div>

        <div v-if="editing" class="action-form-actions">
          <el-button class="soft-btn" @click="editing = false">取消编辑</el-button>
          <el-button class="warm-btn" :loading="saving" @click="saveEdit">保存</el-button>
        </div>
      </section>

      <el-empty v-else-if="!loading" description="病历不存在" />
    </main>

    <AppFooter />
  </div>
</template>

<style scoped>
.detail-meta-row {
  display: flex;
  gap: 24px;
  padding: 8px 0;
}
.detail-meta-item {
  display: flex;
  gap: 6px;
  font-size: 14px;
  line-height: 1.6;
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
  gap: 8px;
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
</style>
