<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check } from '@element-plus/icons-vue'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { addMedicalDetail } from '../api/medical'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const submitting = ref(false)

const form = reactive({
  recordId: '',
  summary: '',
  description: '',
  history: '',
  pastHistory: '',
  lifeHabit: '',
  weight: null,
  temperature: null,
  heartRate: null,
  respiratoryRate: null,
  physicalExam: '',
})

const rules = {
  summary: [{ required: true, message: '请输入摘要', trigger: 'blur' }],
  description: [{ required: true, message: '请输入主诉', trigger: 'blur' }],
  history: [{ required: true, message: '请输入现病史', trigger: 'blur' }],
  pastHistory: [{ required: true, message: '请输入既往史', trigger: 'blur' }],
  lifeHabit: [{ required: true, message: '请输入生活习惯', trigger: 'blur' }],
  weight: [{ required: true, message: '请输入体重', trigger: 'blur' }],
  temperature: [{ required: true, message: '请输入体温', trigger: 'blur' }],
  heartRate: [{ required: true, message: '请输入心率', trigger: 'blur' }],
  respiratoryRate: [{ required: true, message: '请输入呼吸频率', trigger: 'blur' }],
}

onMounted(() => {
  const recordId = route.query.recordId
  if (!recordId) {
    ElMessage.warning('缺少就诊记录 ID')
    router.replace('/console/medical/records')
    return
  }
  form.recordId = recordId
})

async function submitForm() {
  if (!formRef.value || submitting.value) return
  try { await formRef.value.validate() } catch { return }

  submitting.value = true
  try {
    const payload = {
      recordId: form.recordId,
      summary: form.summary.trim(),
      description: form.description.trim(),
      history: form.history.trim(),
      pastHistory: form.pastHistory.trim(),
      lifeHabit: form.lifeHabit.trim(),
      weight: form.weight != null ? +form.weight.toFixed(2) : 0,
      temperature: form.temperature != null ? +form.temperature.toFixed(1) : 0,
      heartRate: Number(form.heartRate),
      respiratoryRate: Number(form.respiratoryRate),
      physicalExam: form.physicalExam.trim() || undefined,
    }
    const result = await addMedicalDetail(payload)
    ElMessage.success('病历已创建')
    router.push(`/medical/detail/${result?.id || ''}`)
  } catch (err) {
    ElMessage.warning(err?.message || '创建失败')
  } finally {
    submitting.value = false
  }
}

function goBack() { router.back() }
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main action-form-page">
      <section class="action-form-panel">
        <header class="form-section-header">
          <h2>创建病历</h2>
        </header>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="action-form-grid">
          <el-form-item label="摘要" prop="summary" class="action-form-span-2">
            <el-input v-model="form.summary" />
          </el-form-item>
          <el-form-item label="主诉" prop="description" class="action-form-span-2">
            <el-input v-model="form.description" type="textarea" :autosize="{ minRows: 2 }" />
          </el-form-item>
          <el-form-item label="现病史" prop="history" class="action-form-span-2">
            <el-input v-model="form.history" type="textarea" :autosize="{ minRows: 2 }" />
          </el-form-item>
          <el-form-item label="既往史" prop="pastHistory" class="action-form-span-2">
            <el-input v-model="form.pastHistory" type="textarea" :autosize="{ minRows: 2 }" />
          </el-form-item>
          <el-form-item label="生活习惯" prop="lifeHabit" class="action-form-span-2">
            <el-input v-model="form.lifeHabit" type="textarea" :autosize="{ minRows: 2 }" />
          </el-form-item>

          <el-form-item label="体重 (kg)" prop="weight">
            <el-input-number v-model="form.weight" :min="0" :max="9999.99" :precision="2" :step="0.1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="体温 (℃)" prop="temperature">
            <el-input-number v-model="form.temperature" :min="0" :max="99.9" :precision="1" :step="0.1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="心率 (次/分)" prop="heartRate">
            <el-input-number v-model="form.heartRate" :min="0" style="width: 100%" />
          </el-form-item>
          <el-form-item label="呼吸频率 (次/分)" prop="respiratoryRate">
            <el-input-number v-model="form.respiratoryRate" :min="0" style="width: 100%" />
          </el-form-item>
          <el-form-item label="体格检查" class="action-form-span-2">
            <el-input v-model="form.physicalExam" type="textarea" :autosize="{ minRows: 2 }" />
          </el-form-item>
        </el-form>

        <div class="action-form-actions">
          <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">取消</el-button>
          <el-button class="warm-btn" :icon="Check" :loading="submitting" @click="submitForm">创建病历</el-button>
        </div>
      </section>
    </main>

    <AppFooter />
  </div>
</template>

<style scoped>
.form-section-header {
  margin-bottom: 24px;
}
.form-section-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
}
</style>
