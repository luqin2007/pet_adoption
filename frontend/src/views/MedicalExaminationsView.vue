<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check, Upload } from '@element-plus/icons-vue'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import {
  addExamination,
  beginExamination,
  deleteExaminationUpload,
  getMedicalDetail,
  getMedicalExaminations,
  uploadExamination,
} from '../api/medical'
import { useConsoleGuards } from '../composables/useConsoleGuards'
import { ROLE, hasRole } from '../utils/roles'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const route = useRoute()
const router = useRouter()
const { loginRole } = useConsoleGuards()

const detailId = computed(() => String(route.params.id || ''))
const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const dialogVisible = ref(false)
const fileInputRef = ref()
const detail = ref(null)
const examinations = ref([])
const draftUuid = ref('')
const draftFiles = ref([])

const isDoctor = computed(() => hasRole(loginRole.value, ROLE.DOCTOR))
const canAdd = computed(() => {
  if (!detail.value) return false
  return isDoctor.value && !detail.value.isCompleted && !detail.value.isDiscard
})

const form = reactive({
  name: '',
  examType: 'OTHER',
  checkTime: '',
  text: '',
})

const examTypeOptions = [
  { label: '血常规', value: 'BLOOD' },
  { label: '生物化学检查', value: 'BIOCHEMISTRY' },
  { label: 'X 光检查', value: 'XRAY' },
  { label: '超声检查', value: 'ULTRASOUND' },
  { label: '粪便检查', value: 'FECAL_EXAM' },
  { label: '尿检', value: 'URINALYSIS' },
  { label: '传染与免疫检查', value: 'INFECTION' },
  { label: '细胞与病理学检查', value: 'CELL' },
  { label: '内分泌', value: 'SEMINAL' },
  { label: '其他检查', value: 'OTHER' },
]

const examTypeMap = Object.fromEntries(examTypeOptions.map((item) => [item.value, item.label]))

function examTypeText(value) {
  return examTypeMap[value] || value || ''
}

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function goBack() {
  router.push(`/medical/detail/${detailId.value}`)
}

async function loadData() {
  if (!detailId.value) return
  loading.value = true
  try {
    const [detailRes, examRes] = await Promise.all([
      getMedicalDetail(detailId.value),
      getMedicalExaminations(detailId.value),
    ])
    detail.value = detailRes || null
    examinations.value = Array.isArray(examRes) ? examRes : []
  } catch (error) {
    ElMessage.warning(error?.message || '加载检查记录失败')
  } finally {
    loading.value = false
  }
}

async function openCreateDialog() {
  if (!canAdd.value) return
  try {
    draftUuid.value = await beginExamination(detailId.value)
    draftFiles.value = []
    Object.assign(form, {
      name: '',
      examType: 'OTHER',
      checkTime: new Date().toISOString(),
      text: '',
    })
    dialogVisible.value = true
  } catch (error) {
    ElMessage.warning(error?.message || '准备检查记录失败')
  }
}

function chooseFile() {
  fileInputRef.value?.click()
}

async function uploadFile(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file || !draftUuid.value) return
  uploading.value = true
  try {
    const filename = await uploadExamination(draftUuid.value, file, file.name)
    draftFiles.value.push({ name: filename || file.name })
    ElMessage.success('文件已上传')
  } catch (error) {
    ElMessage.warning(error?.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

async function removeDraftFile(file) {
  if (!draftUuid.value || !file?.name) return
  try {
    await deleteExaminationUpload(draftUuid.value, file.name)
    draftFiles.value = draftFiles.value.filter((item) => item.name !== file.name)
  } catch (error) {
    ElMessage.warning(error?.message || '删除文件失败')
  }
}

async function saveExamination() {
  if (!draftUuid.value || saving.value) return
  if (!form.name.trim()) {
    ElMessage.warning('请输入检查名称')
    return
  }
  saving.value = true
  try {
    await addExamination(draftUuid.value, {
      name: form.name.trim(),
      text: form.text.trim() || undefined,
      examType: form.examType,
      checkTime: form.checkTime,
    })
    ElMessage.success('检查记录已添加')
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.warning(error?.message || '保存检查记录失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main action-form-page" v-loading="loading">
      <section class="action-form-panel">
        <header class="form-section-header detail-header">
          <div>
            <h2 class="detail-pet-name">检查记录</h2>
            <p class="detail-pet-breed">{{ detail?.name || '病例' }} · {{ detail?.summary || '' }}</p>
          </div>
          <el-button v-if="canAdd" class="warm-btn" :icon="Check" @click="openCreateDialog">添加检查</el-button>
        </header>

        <section class="medical-exam-list">
          <article v-for="item in examinations" :key="item.id" class="medical-exam-card">
            <div class="medical-exam-card-head">
              <div>
                <strong>{{ item.name || examTypeText(item.examType) }}</strong>
                <span>{{ examTypeText(item.examType) }} · {{ formatDate(item.checkTime) }}</span>
              </div>
              <el-tag size="small" effect="plain">{{ item.files?.length || 0 }} 个文件</el-tag>
            </div>
            <p v-if="item.text" class="medical-exam-text">{{ item.text }}</p>
            <div v-if="item.files?.length" class="medical-file-list">
              <a v-for="file in item.files" :key="file.id || file.name" :href="file.assetUrl" :download="file.name" target="_blank" rel="noreferrer">
                {{ file.name }}
              </a>
            </div>
          </article>
          <el-empty v-if="!examinations.length && !loading" description="暂无检查记录" />
        </section>

        <div class="detail-bottom-actions">
          <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回病例</el-button>
        </div>
      </section>
    </main>

    <el-dialog v-model="dialogVisible" title="添加检查记录" width="720px" :close-on-click-modal="false">
      <el-form label-position="top" class="medical-exam-form">
        <el-form-item label="检查名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="检查类型">
          <el-select v-model="form.examType" class="full-width-control">
            <el-option v-for="item in examTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="检查时间">
          <el-date-picker v-model="form.checkTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss.SSS" class="full-width-control" />
        </el-form-item>
        <el-form-item label="文本记录" class="medical-exam-form-wide">
          <el-input v-model="form.text" type="textarea" :autosize="{ minRows: 4 }" />
        </el-form-item>
        <el-form-item label="检查文件" class="medical-exam-form-wide">
          <input ref="fileInputRef" class="profile-avatar-input" type="file" @change="uploadFile" />
          <div class="medical-upload-row">
            <el-button class="soft-btn" :icon="Upload" :loading="uploading" @click="chooseFile">上传文件</el-button>
            <span v-for="file in draftFiles" :key="file.name" class="medical-upload-chip">
              {{ file.name }}
              <button type="button" @click="removeDraftFile(file)">删除</button>
            </span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button class="warm-btn" :loading="saving" @click="saveExamination">保存</el-button>
      </template>
    </el-dialog>

    <AppFooter />
  </div>
</template>

<style scoped>
.detail-header {
  align-items: flex-start;
}

.detail-pet-name {
  margin: 0;
  color: #333;
  font-size: 24px;
  font-weight: 700;
}

.detail-pet-breed {
  margin: 4px 0 0;
  color: var(--muted);
  font-size: 13px;
}

.medical-exam-list {
  display: grid;
  gap: 12px;
  margin-top: 18px;
}

.medical-exam-card {
  border: 1px solid rgba(243, 223, 204, 0.9);
  border-radius: 14px;
  background: rgba(255, 248, 240, 0.72);
  padding: 14px;
}

.medical-exam-card-head {
  display: flex;
  justify-content: space-between;
  gap: 14px;
}

.medical-exam-card-head strong {
  display: block;
  color: #5d3927;
  font-size: 16px;
}

.medical-exam-card-head span,
.medical-exam-text {
  color: var(--muted);
  line-height: 1.6;
}

.medical-file-list,
.medical-upload-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.medical-file-list a,
.medical-upload-chip {
  border: 1px solid rgba(231, 122, 59, 0.2);
  border-radius: 999px;
  background: rgba(255, 253, 249, 0.9);
  color: var(--primary-strong);
  padding: 5px 10px;
  font-size: 13px;
  text-decoration: none;
}

.medical-upload-chip button {
  border: none;
  background: transparent;
  color: var(--primary-strong);
  cursor: pointer;
  font-weight: 700;
}

.medical-exam-form {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px 14px;
}

.medical-exam-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.medical-exam-form-wide {
  grid-column: 1 / -1;
}

@media (max-width: 760px) {
  .medical-exam-form {
    grid-template-columns: 1fr;
  }
}
</style>
