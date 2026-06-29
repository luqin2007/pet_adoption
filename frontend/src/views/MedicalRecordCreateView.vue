<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check } from '@element-plus/icons-vue'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { getPetById } from '../api/pets'
import { addMedicalRecord } from '../api/medical'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const submitting = ref(false)

const form = reactive({
  petId: '',
  petName: '',
  petAge: null,
  type: '',
  price: '',
  cost: '',
  ownerPhone: '',
})

onMounted(async () => {
  const petId = route.query.petId
  if (!petId) {
    ElMessage.warning('缺少宠物 ID，请从工作台进入')
    router.replace('/console')
    return
  }
  form.petId = petId
  try {
    const pet = await getPetById(petId)
    if (pet?.name) form.petName = pet.name
    if (pet?.age) form.petAge = pet.age
  } catch (error) {
    console.error('获取宠物信息失败:', error)
  }
})

const recordTypeOptions = [
  { label: '初诊', value: 'FIRST' },
  { label: '复诊', value: 'REVISIT' },
  { label: '急诊', value: 'EMERGENCY' },
  { label: '体检', value: 'EXAMINATION' },
]

const rules = {
  petAge: [{ required: true, message: '请输入宠物年龄', trigger: 'blur' }],
  type: [{ required: true, message: '请选择就诊类型', trigger: 'change' }],
}

async function submitForm() {
  if (!formRef.value || submitting.value) return
  try { await formRef.value.validate() } catch { return }

  submitting.value = true
  try {
    const payload = {
      petAge: Number(form.petAge),
      type: form.type,
      price: form.price || undefined,
      cost: form.cost || undefined,
      ownerPhone: form.ownerPhone?.trim() || undefined,
    }
    await addMedicalRecord(form.petId, payload)
    ElMessage.success('就诊记录已创建')
    router.push(`/console/medical/records?pet=${form.petId}&name=${encodeURIComponent(form.petName)}`)
  } catch (err) {
    ElMessage.warning(err?.message || '提交失败')
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
          <h2>新增就诊记录</h2>
        </header>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="action-form-grid">
          <el-form-item label="宠物名称">
            <el-input v-model="form.petName" disabled />
          </el-form-item>

          <el-form-item label="年龄 (月)" prop="petAge">
            <el-input-number v-model="form.petAge" :min="0" style="width: 100%" />
          </el-form-item>

          <el-form-item label="就诊类型" prop="type">
            <el-select v-model="form.type" placeholder="请选择">
              <el-option v-for="item in recordTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>

          <el-form-item label="预计费用">
            <el-input v-model="form.price" placeholder="可选" />
          </el-form-item>

          <el-form-item label="实际费用">
            <el-input v-model="form.cost" placeholder="可选" />
          </el-form-item>

          <el-form-item label="领养人电话" class="action-form-span-2">
            <el-input v-model="form.ownerPhone" placeholder="可选" />
          </el-form-item>
        </el-form>

        <div class="action-form-actions">
          <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">取消</el-button>
          <el-button class="warm-btn" :icon="Check" :loading="submitting" @click="submitForm">创建记录</el-button>
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
