<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check, Plus, Delete } from '@element-plus/icons-vue'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { getPetById } from '../api/pets'
import { createFirstVisitRegistration } from '../api/services'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const LS_IMMUNITY_KEY = 'pet_first_reg_immunity_history'
const LS_ALLERGY_KEY = 'pet_first_reg_allergy_history'

function loadLocalHistory(key) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

function saveLocalHistory(key, records) {
  try {
    const existing = loadLocalHistory(key)
    const merged = [...existing]
    for (const r of records) {
      const exists = merged.some((m) => JSON.stringify(m) === JSON.stringify(r))
      if (!exists) merged.push(r)
    }
    localStorage.setItem(key, JSON.stringify(merged))
  } catch {
    // ignore
  }
}

const router = useRouter()
const route = useRoute()
const formRef = ref()
const submitting = ref(false)

const form = reactive({
  petId: '',
  name: '',
  age: null,
  weight: null,
  temperature: null,
  description: '',
  immunities: [],
  allergies: [],
})

const immunityHistory = ref(loadLocalHistory(LS_IMMUNITY_KEY))
const allergyHistory = ref(loadLocalHistory(LS_ALLERGY_KEY))

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
    if (pet && pet.name) form.name = pet.name
    if (pet && pet.age) form.age = pet.age
  } catch (error) {
    console.error('获取宠物信息失败:', error)
  }
})

const rules = {
  name: [{ required: true, message: '请输入登记名称', trigger: 'blur' }],
  age: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
  weight: [{ required: true, message: '请输入体重', trigger: 'blur' }],
  temperature: [{ required: true, message: '请输入体温', trigger: 'blur' }],
}

function selectImmunity(row, val) {
  const match = immunityHistory.value.find((h) => h.medicine === val)
  if (match) {
    row.illness = match.illness || ''
    row.count = match.count || 1
    row.total = match.total || 3
  }
}

function selectAllergy(row, val) {
  const match = allergyHistory.value.find((h) => h.source === val)
  if (match) {
    row.reaction = match.reaction || ''
  }
}

function addImmunity() {
  form.immunities.push({ medicine: '', illness: '', count: 1, total: 3, immunityTime: '' })
}

function removeImmunity(index) {
  form.immunities.splice(index, 1)
}

function addAllergy() {
  form.allergies.push({ source: '', reaction: '', discoveryTime: '' })
}

function removeAllergy(index) {
  form.allergies.splice(index, 1)
}

async function submitForm() {
  if (!formRef.value || submitting.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const immunities = form.immunities
      .filter((i) => i.medicine)
      .map((i) => ({
        medicine: i.medicine,
        illness: i.illness || i.medicine,
        count: i.count,
        total: i.total,
        immunityTime: i.immunityTime || undefined,
      }))

    const allergies = form.allergies
      .filter((a) => a.source)
      .map((a) => ({
        source: a.source,
        reaction: a.reaction || '未知',
        discoveryTime: a.discoveryTime || undefined,
      }))

    const payload = {
      petId: form.petId,
      name: form.name.trim(),
      age: Number(form.age),
      weight: Number(form.weight),
      temperature: Number(form.temperature),
      description: form.description.trim() || undefined,
      immunities,
      allergies,
    }

    await createFirstVisitRegistration(payload)

    saveLocalHistory(LS_IMMUNITY_KEY, immunities.map(({ medicine, illness, count, total }) => ({ medicine, illness, count, total })))
    saveLocalHistory(LS_ALLERGY_KEY, allergies.map(({ source, reaction }) => ({ source, reaction })))

    ElMessage.success('初诊档案已建立')
    router.push('/console?tab=medical-first')
  } catch (err) {
    ElMessage.warning(err?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.back()
}
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main action-form-page">
      <section class="action-form-panel">
        <header class="form-section-header">
          <h2>初诊登记</h2>
        </header>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="action-form-grid">
          <div class="form-group-title action-form-span-2">基础生命体征</div>

          <el-form-item label="宠物名称" prop="name">
            <el-input v-model="form.name" clearable />
          </el-form-item>

          <el-form-item label="年龄 (月)" prop="age">
            <el-input-number v-model="form.age" :min="0" :max="360" style="width: 100%" />
          </el-form-item>

          <el-form-item label="体重 (kg)" prop="weight">
            <el-input-number v-model="form.weight" :min="0" :precision="2" :step="0.1" style="width: 100%" />
          </el-form-item>

          <el-form-item label="体温 (℃)" prop="temperature">
            <el-input-number v-model="form.temperature" :min="0" :precision="1" :step="0.1" style="width: 100%" />
          </el-form-item>

          <div class="form-group-title action-form-span-2">
            <span>免疫史</span>
            <el-button type="primary" link :icon="Plus" @click="addImmunity">添加记录</el-button>
          </div>

          <div v-if="form.immunities.length" class="dynamic-form-list action-form-span-2">
            <div v-for="(item, index) in form.immunities" :key="index" class="dynamic-form-item">
              <el-form-item label="疫苗名称" class="field-vaccine">
                <el-select
                  v-model="item.medicine"
                  filterable
                  allow-create
                  placeholder="选择或输入"
                  @change="(val) => selectImmunity(item, val)"
                >
                  <el-option v-for="opt in immunityHistory" :key="opt.medicine" :label="opt.medicine" :value="opt.medicine" />
                </el-select>
              </el-form-item>
              <el-form-item label="预防疾病" class="field-illness">
                <el-input v-model="item.illness" />
              </el-form-item>
              <el-form-item label="接种进度" class="field-progress">
                <div style="display: flex; align-items: center; gap: 8px;">
                  <el-input-number v-model="item.count" :min="1" :max="item.total" style="width: 80px;" controls-position="right" />
                  <span>/</span>
                  <el-input-number v-model="item.total" :min="1" style="width: 80px;" controls-position="right" />
                </div>
              </el-form-item>
              <el-form-item label="接种日期" class="field-date">
                <el-date-picker v-model="item.immunityTime" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
              </el-form-item>
              <div class="item-actions">
                <el-button circle type="danger" :icon="Delete" @click="removeImmunity(index)" />
              </div>
            </div>
          </div>
          <div v-else class="empty-list-tip action-form-span-2">还没有免疫记录</div>

          <div class="form-group-title action-form-span-2">
            <span>过敏史</span>
            <el-button type="primary" link :icon="Plus" @click="addAllergy">添加记录</el-button>
          </div>

          <div v-if="form.allergies.length" class="dynamic-form-list action-form-span-2">
            <div v-for="(item, index) in form.allergies" :key="index" class="dynamic-form-item">
              <el-form-item label="过敏原" class="field-allergen">
                <el-select
                  v-model="item.source"
                  filterable
                  allow-create
                  placeholder="选择或输入"
                  @change="(val) => selectAllergy(item, val)"
                >
                  <el-option v-for="opt in allergyHistory" :key="opt.source" :label="opt.source" :value="opt.source" />
                </el-select>
              </el-form-item>
              <el-form-item label="发现时间" class="field-date">
                <el-date-picker v-model="item.discoveryTime" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
              </el-form-item>
              <el-form-item label="反应症状" class="field-reaction">
                <el-input v-model="item.reaction" />
              </el-form-item>
              <div class="item-actions">
                <el-button circle type="danger" :icon="Delete" @click="removeAllergy(index)" />
              </div>
            </div>
          </div>
          <div v-else class="empty-list-tip action-form-span-2">还没有过敏记录</div>
        </el-form>

        <div class="action-form-actions">
          <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">取消</el-button>
          <el-button class="warm-btn" :icon="Check" :loading="submitting" @click="submitForm">保存档案</el-button>
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
  margin: 0 0 8px;
}

.form-section-header p {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.form-group-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 15px;
  font-weight: 600;
  color: #444;
  padding-bottom: 8px;
  border-bottom: 1px dashed #eee;
  margin-top: 16px;
  margin-bottom: 12px;
}

.dynamic-form-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dynamic-form-item {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  background: #f9f9f9;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #efefef;
}

.dynamic-form-item .el-form-item {
  margin-bottom: 0;
}

.field-vaccine { flex: 0 0 160px; min-width: 0; }
.field-allergen { flex: 0 0 160px; min-width: 0; }
.field-illness { flex: 0 0 140px; min-width: 0; }
.field-progress { flex: 0 0 180px; min-width: 0; }
.field-date { flex: 0 0 160px; min-width: 0; }
.field-reaction { flex: 1 1 140px; min-width: 0; }

.item-actions {
  padding-bottom: 2px;
  flex-shrink: 0;
}

.empty-list-tip {
  text-align: center;
  padding: 24px;
  background: #fafafa;
  border: 1px dashed #ddd;
  border-radius: 8px;
  color: #999;
  font-size: 14px;
}

@media (max-width: 768px) {
  .dynamic-form-item {
    flex-direction: column;
    align-items: stretch;
  }
  .field-vaccine,
  .field-allergen,
  .field-illness,
  .field-progress,
  .field-date,
  .field-reaction {
    flex: 1 1 auto;
  }
}
</style>