<script setup>
import { onMounted, reactive, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check, Plus, Delete } from '@element-plus/icons-vue'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { getPetById } from '../api/pets'
import { createFirstVisitRegistration } from '../api/services'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

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

onMounted(async () => {
  const petId = route.query.petId
  if (!petId) {
    ElMessage.warning('缺少宠物 ID，请从工作台进入')
    router.replace('/console')
    return
  }
  form.petId = petId

  // 自动获取宠物名并填充
  try {
    const pet = await getPetById(petId)
    if (pet && pet.name) {
      form.name = pet.name
    }
    if (pet && pet.age) {
      form.age = pet.age
    }
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

const vaccineOptions = ['狂犬病', '猫三联', '犬五联', '猫五联', '猫八联', '犬八联', '猫瘟', '猫杯状病毒', '猫疱疹病毒', '其它']
const allergyOptions = ['青霉素', '磺胺类', '庆大霉素', '阿司匹林', '布洛芬', '花粉', '猫毛', '犬毛', '其它']

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
    const payload = {
      petId: form.petId,
      name: form.name.trim(),
      age: Number(form.age),
      weight: Number(form.weight),
      temperature: Number(form.temperature),
      description: form.description.trim() || undefined,
      immunities: form.immunities
        .filter(i => i.medicine)
        .map(i => ({ 
          medicine: i.medicine, 
          illness: i.illness || i.medicine, 
          count: i.count, 
          total: i.total, 
          immunityTime: i.immunityTime || undefined 
        })),
      allergies: form.allergies
        .filter(a => a.source)
        .map(a => ({ 
          source: a.source, 
          reaction: a.reaction || '未知', 
          discoveryTime: a.discoveryTime || undefined 
        })),
    }

    await createFirstVisitRegistration(payload)
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
          <!-- 核心体征 -->
          <div class="form-group-title action-form-span-2">基础生命体征</div>
          
          <el-form-item label="宠物名称" prop="name">
            <el-input v-model="form.name" placeholder="例如：西湖小橘" clearable />
          </el-form-item>
          
          <el-form-item label="年龄 (月)" prop="age">
            <el-input-number v-model="form.age" :min="0" :max="360" style="width: 100%" />
          </el-form-item>
          
          <el-form-item label="体重 (kg)" prop="weight">
            <el-input-number v-model="form.weight" :min="0" :precision="2" :step="0.1" style="width: 100%" />
          </el-form-item>
          
          <el-form-item label="体温 (℃)" prop="temperature">
            <el-input-number v-model="form.temperature" :min="30" :max="45" :precision="1" :step="0.1" style="width: 100%" />
          </el-form-item>

          <el-form-item label="情况描述" prop="description" class="action-form-span-2">
            <el-input 
              v-model="form.description" 
              type="textarea" 
              :rows="3" 
              placeholder="描述宠物的精神状态、外伤或其它异常情况..." 
            />
          </el-form-item>

          <!-- 免疫史 -->
          <div class="form-group-title action-form-span-2">
            <span>免疫史</span>
            <el-button type="primary" link :icon="Plus" @click="addImmunity">添加记录</el-button>
          </div>

          <div v-if="form.immunities.length" class="dynamic-form-list action-form-span-2">
            <div v-for="(item, index) in form.immunities" :key="index" class="dynamic-form-item">
              <el-form-item label="疫苗名称">
                <el-select v-model="item.medicine" filterable allow-create placeholder="选择或输入">
                  <el-option v-for="opt in vaccineOptions" :key="opt" :label="opt" :value="opt" />
                </el-select>
              </el-form-item>
              <el-form-item label="预防疾病">
                <el-input v-model="item.illness" placeholder="例如：猫瘟" />
              </el-form-item>
              <el-form-item label="接种进度">
                <div style="display: flex; align-items: center; gap: 8px;">
                  <el-input-number v-model="item.count" :min="1" :max="item.total" style="width: 100px;" controls-position="right" />
                  <span>/</span>
                  <el-input-number v-model="item.total" :min="1" style="width: 100px;" controls-position="right" />
                </div>
              </el-form-item>
              <el-form-item label="接种日期">
                <el-date-picker v-model="item.immunityTime" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
              </el-form-item>
              <div class="item-actions">
                <el-button border circle type="danger" :icon="Delete" @click="removeImmunity(index)" />
              </div>
            </div>
          </div>
          <div v-else class="empty-list-tip action-form-span-2">暂无免疫记录数据</div>

          <!-- 过敏史 -->
          <div class="form-group-title action-form-span-2">
            <span>过敏史</span>
            <el-button type="primary" link :icon="Plus" @click="addAllergy">添加记录</el-button>
          </div>

          <div v-if="form.allergies.length" class="dynamic-form-list action-form-span-2">
            <div v-for="(item, index) in form.allergies" :key="index" class="dynamic-form-item">
              <el-form-item label="过敏原">
                <el-select v-model="item.source" filterable allow-create placeholder="选择或输入">
                  <el-option v-for="opt in allergyOptions" :key="opt" :label="opt" :value="opt" />
                </el-select>
              </el-form-item>
              <el-form-item label="发现时间">
                <el-date-picker v-model="item.discoveryTime" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
              </el-form-item>
              <el-form-item label="反应症状" class="item-grow">
                <el-input v-model="item.reaction" placeholder="描述过敏现象" />
              </el-form-item>
              <div class="item-actions">
                <el-button border circle type="danger" :icon="Delete" @click="removeAllergy(index)" />
              </div>
            </div>
          </div>
          <div v-else class="empty-list-tip action-form-span-2">暂无过敏记录数据</div>
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

.item-grow {
  flex: 1;
}

.item-actions {
  padding-bottom: 2px;
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
}
</style>
