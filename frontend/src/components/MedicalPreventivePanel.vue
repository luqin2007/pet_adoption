<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>{{ isVaccineMode ? '疫苗接种' : '驱虫管理' }}</strong>
        <div class="profile-actions">
          <el-button-group class="console-btn-group">
            <el-button class="warm-btn" :icon="Plus" @click="petPickerVisible = true"/>
            <el-button class="warm-btn" :icon="Sugar" @click="openAddDialog" />
            <el-button class="warm-btn" :icon="RefreshRight" :loading="loading" @click="loadAll" />
          </el-button-group>
        </div>
      </div>
    </template>

    <section class="pet-admin-section">
      <el-table :data="displayedRows" v-loading="loading" class="user-admin-table">
        <el-table-column min-width="150">
          <template #header><TableFilterHeader label="宠物" :filter="filters.petName" type="text" :active="isActive('petName')" /></template>
          <template #default="{ row }">
            <button class="pet-admin-name-button" type="button" @click="goPet(row)">{{ row.petName || '未命名' }}</button>
            <span class="medical-preventive-subtext">{{ row.petType || '宠物' }} · {{ row.petSex || '未知' }} · {{ row.petAge ?? 0 }} 月</span>
          </template>
        </el-table-column>
        <el-table-column :label="isVaccineMode ? '疫苗' : '驱虫药'" min-width="160">
          <template #header><TableFilterHeader :label="isVaccineMode ? '疫苗' : '驱虫药'" :filter="filters.drugName" type="text" :active="isActive('drugName')" /></template>
          <template #default="{ row }">
            <strong class="medical-preventive-name">{{ isVaccineMode ? row.vaccineName : row.dewormerName }}</strong>
            <span class="medical-preventive-subtext">{{ isVaccineMode ? row.vaccineIll : dewormerTypeText(row.dewormerType) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="次数" width="90">
          <template #default="{ row }">{{ row.times || '' }}</template>
        </el-table-column>
        <el-table-column label="总次数" width="100">
          <template #default="{ row }">{{ isVaccineMode ? row.vaccineTotal : row.dewormerTotal }}</template>
        </el-table-column>
        <el-table-column label="兽医" min-width="120">
          <template #header><TableFilterHeader label="兽医" :filter="filters.username" type="text" :active="isActive('username')" /></template>
          <template #default="{ row }">{{ row.username || '' }}</template>
        </el-table-column>
        <el-table-column label="记录时间" min-width="160">
          <template #header><TableFilterHeader label="记录时间" :filter="filters.recordTime" type="time" :active="isActive('recordTime')" /></template>
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <div class="user-admin-pagination">
        <el-pagination layout="prev, pager, next, total" :current-page="page.page" :page-size="page.size" :total="filteredRecords.length" @current-change="(p) => page.page = p" />
      </div>
    </section>

    <section class="pet-admin-section" style="margin-top: 24px">
      <div class="profile-card-header">
        <strong>{{ isVaccineMode ? '疫苗目录' : '驱虫药目录' }}</strong>
      </div>
      <el-table :data="filteredDrugs" v-loading="drugLoading" class="user-admin-table">
        <el-table-column label="名称" min-width="160">
          <template #header><TableFilterHeader label="名称" :filter="drugFilters.name" type="text" :active="drugIsActive('name')" /></template>
          <template #default="{ row }">{{ row.name }}</template>
        </el-table-column>
        <el-table-column v-if="isVaccineMode" label="疾病" min-width="140">
          <template #header><TableFilterHeader label="疾病" :filter="drugFilters.illness" type="text" :active="drugIsActive('illness')" /></template>
          <template #default="{ row }">{{ row.illness }}</template>
        </el-table-column>
        <el-table-column v-if="!isVaccineMode" label="类型" min-width="100">
          <template #header><TableFilterHeader label="类型" :filter="drugFilters.drugType" type="text" :active="drugIsActive('drugType')" /></template>
          <template #default="{ row }">{{ dewormerTypeText(row.type) }}</template>
        </el-table-column>
        <el-table-column label="最小月龄" width="100">
          <template #header><TableFilterHeader label="最小月龄" :filter="drugFilters.minAge" type="text" :active="drugIsActive('minAge')" /></template>
          <template #default="{ row }">{{ row.minAge ?? 0 }}</template>
        </el-table-column>
        <el-table-column :label="isVaccineMode ? '总针数' : '总次数'" width="90">
          <template #default="{ row }">{{ row.times }}</template>
        </el-table-column>
      </el-table>
    </section>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="isVaccineMode ? '添加疫苗' : '添加驱虫药'" width="600px" :close-on-click-modal="false">
    <el-steps :active="step" finish-status="success" simple style="margin-bottom: 20px">
      <el-step title="选择物品" />
      <el-step title="详细信息" />
    </el-steps>

    <div v-if="step === 0" class="medical-drug-select-grid">
      <div class="medical-drug-select-pane">
        <div class="medical-drug-select-pane-header">分类</div>
        <el-input v-model="categoryKeyword" placeholder="搜索分类" size="small" clearable />
        <el-scrollbar max-height="280px">
          <div v-for="cat in filteredCategories" :key="cat.id" class="medical-drug-select-item" :class="{ 'is-active': selectedCategory?.id === cat.id }" @click="selectedCategory = cat">
            {{ cat.name }}
          </div>
        </el-scrollbar>
      </div>
      <div class="medical-drug-select-pane">
        <div class="medical-drug-select-pane-header">物品</div>
        <el-input v-model="itemKeyword" placeholder="搜索物品" size="small" clearable />
        <el-scrollbar max-height="280px">
          <div v-for="item in filteredItems" :key="item.id" class="medical-drug-select-item" :class="{ 'is-active': selectedItem?.id === item.id }" @click="selectedItem = item">
            {{ item.name }}
          </div>
        </el-scrollbar>
      </div>
    </div>

    <div v-if="step === 1" class="medical-drug-form">
      <el-form label-width="100px">
        <el-form-item label="选择物品">
          <el-tag closable @close="step = 0">{{ selectedItem?.name }}</el-tag>
        </el-form-item>
        <template v-if="!isVaccineMode">
          <el-form-item label="驱虫类型">
            <el-radio-group v-model="drugForm.type">
              <el-radio value="INTERNAL">体内驱虫</el-radio>
              <el-radio value="EXTERNAL">体外驱虫</el-radio>
              <el-radio value="OTHER">其他</el-radio>
            </el-radio-group>
          </el-form-item>
        </template>
        <template v-else>
          <el-form-item label="疾病名称">
            <el-input v-model="drugForm.illness" placeholder="如：狂犬病、猫三联" />
          </el-form-item>
        </template>
        <el-form-item label="最小月龄">
          <el-input-number v-model="drugForm.minAge" :min="0" :max="999" />
        </el-form-item>
        <el-form-item :label="isVaccineMode ? '总针数' : '总次数'">
          <el-input-number v-model="drugForm.times" :min="1" :max="99" />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button class="soft-btn" @click="stepBack">{{ step === 0 ? '取消' : '上一步' }}</el-button>
      <el-button v-if="step === 0" :disabled="!selectedItem" class="warm-btn" @click="step = 1">下一步</el-button>
      <el-button v-else :loading="submitting" class="warm-btn" @click="submitDrug">确认添加</el-button>
    </template>
  </el-dialog>

  <PetPickerDialog v-model:visible="petPickerVisible" @select="onPetSelected" />
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Sugar, RefreshRight } from '@element-plus/icons-vue'
import { createDewormerItem, createVaccineItem, getAllDeworms, getAllVaccines, getDewormerOptions, getVaccineOptions } from '../api/medical'
import { getCategories, getItems } from '../api/inventory'
import { useTableFilters } from '../composables/useTableFilters'
import TableFilterHeader from './TableFilterHeader.vue'
import PetPickerDialog from './PetPickerDialog.vue'

const props = defineProps({
  type: {
    type: String,
    default: 'vaccine',
  },
})

const router = useRouter()
const loading = ref(false)
const rows = ref([])
const page = reactive({ page: 1, size: 10 })
const isVaccineMode = computed(() => props.type === 'vaccine')

const { filters, isActive, applyFilter } = useTableFilters({
  petName: { type: 'text' },
  drugName: { type: 'text' },
  username: { type: 'text' },
  recordTime: { type: 'time' },
})

const filteredRecords = computed(() => applyFilter(rows.value || []))

const displayedRows = computed(() => {
  const start = (page.page - 1) * page.size
  return filteredRecords.value.slice(start, start + page.size)
})

const drugLoading = ref(false)
const drugs = ref([])
const { filters: drugFilters, isActive: drugIsActive, applyFilter: drugApplyFilter } = useTableFilters({
  name: { type: 'text' },
  illness: { type: 'text' },
  drugType: { type: 'text' },
  minAge: { type: 'text' },
})
const filteredDrugs = computed(() => drugApplyFilter(drugs.value || []))

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function dewormerTypeText(value) {
  const map = { INTERNAL: '体内驱虫', EXTERNAL: '体外驱虫', OTHER: '其他驱虫' }
  return map[value] || value || ''
}

function goPet(row) {
  if (row?.petId) router.push(`/pets/${row.petId}`)
}

async function loadRecords() {
  loading.value = true
  try {
    rows.value = isVaccineMode.value ? await getAllVaccines() : await getAllDeworms()
  } catch (error) {
    ElMessage.warning(error?.message || '加载医疗护理记录失败')
  } finally {
    loading.value = false
  }
}

async function loadDrugs() {
  drugLoading.value = true
  try {
    drugs.value = isVaccineMode.value ? await getVaccineOptions() : await getDewormerOptions()
  } catch (error) {
    ElMessage.warning(error?.message || '加载药品目录失败')
  } finally {
    drugLoading.value = false
  }
}

async function loadAll() {
  await Promise.all([loadRecords(), loadDrugs()])
}

const dialogVisible = ref(false)
const step = ref(0)
const categories = ref([])
const items = ref([])
const selectedCategory = ref(null)
const selectedItem = ref(null)
const categoryKeyword = ref('')
const itemKeyword = ref('')
const categoryLoading = ref(false)
const itemLoading = ref(false)
const submitting = ref(false)
const petPickerVisible = ref(false)
const drugForm = reactive({
  type: 'INTERNAL',
  illness: '',
  minAge: 0,
  times: 1,
})

const filteredCategories = computed(() => {
  const kw = categoryKeyword.value?.trim().toLowerCase()
  if (!kw) return categories.value
  return categories.value.filter((c) => c.name?.toLowerCase().includes(kw))
})

const filteredItems = computed(() => {
  const kw = itemKeyword.value?.trim().toLowerCase()
  let list = items.value
  if (kw) list = list.filter((item) => item.name?.toLowerCase().includes(kw))
  return list
})

async function loadCategories() {
  categoryLoading.value = true
  try {
    const result = await getCategories({ size: 200 })
    categories.value = Array.isArray(result?.records) ? result.records : []
  } catch (error) {
    ElMessage.warning(error?.message || '加载分类失败')
  } finally {
    categoryLoading.value = false
  }
}

async function loadItems() {
  if (!selectedCategory.value?.id) {
    items.value = []
    return
  }
  itemLoading.value = true
  try {
    const result = await getItems({ size: 200, category: [String(selectedCategory.value.id)] })
    items.value = Array.isArray(result?.records) ? result.records : []
  } catch (error) {
    ElMessage.warning(error?.message || '加载物品失败')
  } finally {
    itemLoading.value = false
  }
}

watch(selectedCategory, () => {
  selectedItem.value = null
  itemKeyword.value = ''
  loadItems()
})

async function openAddDialog() {
  drugForm.type = 'INTERNAL'
  drugForm.illness = ''
  drugForm.minAge = 0
  drugForm.times = 1
  step.value = 0
  selectedCategory.value = null
  selectedItem.value = null
  categoryKeyword.value = ''
  itemKeyword.value = ''
  dialogVisible.value = true
  await loadCategories()
}

function stepBack() {
  if (step.value === 0) {
    dialogVisible.value = false
  } else {
    step.value = 0
  }
}

async function submitDrug() {
  if (!selectedItem.value) return
  submitting.value = true
  try {
    const payload = { itemId: selectedItem.value.id, minAge: drugForm.minAge, times: drugForm.times }
    if (isVaccineMode.value) {
      payload.illness = drugForm.illness
      if (!payload.illness?.trim()) throw new Error('请输入疾病名称')
      await createVaccineItem(payload)
    } else {
      payload.type = drugForm.type
      await createDewormerItem(payload)
    }
    ElMessage.success(isVaccineMode.value ? '疫苗添加成功' : '驱虫药添加成功')
    dialogVisible.value = false
    await loadDrugs()
  } catch (error) {
    ElMessage.warning(error?.message || '添加失败')
  } finally {
    submitting.value = false
  }
}

function onPetSelected(pet) {
  router.push(`/console/medical/records?pet=${pet.id}&name=${encodeURIComponent(pet.name || '')}`)
}

watch(() => props.type, () => {
  page.page = 1
  loadAll()
})

onMounted(() => {
  loadAll()
})
</script>

<style scoped>
.medical-preventive-name {
  display: block;
  color: #5d3927;
}
.medical-preventive-subtext {
  display: block;
  margin-top: 3px;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.5;
}
.medical-drug-select-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.medical-drug-select-pane {
  border: 1px solid var(--border);
  border-radius: 8px;
  overflow: hidden;
}
.medical-drug-select-pane-header {
  padding: 8px 12px;
  background: var(--bg);
  font-weight: 700;
  font-size: 13px;
  border-bottom: 1px solid var(--border);
}
.medical-drug-select-pane .el-input {
  margin: 8px;
  width: calc(100% - 16px);
}
.medical-drug-select-item {
  padding: 6px 12px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.15s;
}
.medical-drug-select-item:hover {
  background: var(--hover);
}
.medical-drug-select-item.is-active {
  background: var(--primary-weak);
  color: var(--primary-strong);
  font-weight: 700;
}
.medical-drug-form {
  padding: 8px 0;
}
</style>
