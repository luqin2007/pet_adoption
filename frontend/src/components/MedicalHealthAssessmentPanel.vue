<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>健康评估</strong>
        <div class="profile-actions">
          <el-button-group class="console-btn-group">
            <el-button class="warm-btn" :icon="Plus" @click="petPickerVisible = true"/>
            <el-button class="warm-btn" :icon="RefreshRight" :loading="loading" @click="loadAssessments"/>
          </el-button-group>
        </div>
      </div>
    </template>

    <section class="pet-admin-section">
      <el-table :data="displayedRows" v-loading="loading" class="user-admin-table" row-key="id">
        <el-table-column min-width="160">
          <template #header><TableFilterHeader label="宠物" :filter="filters.petName" type="text" :active="isActive('petName')" /></template>
          <template #default="{ row }">
            <button class="pet-admin-name-button" type="button" @click="goDetail(row)">{{ row.petName || '未命名' }}</button>
            <span class="medical-health-subtext">{{ row.petType || '宠物' }} · {{ row.petSex || '未知' }} · {{ row.petAge ?? 0 }} 月</span>
          </template>
        </el-table-column>
        <el-table-column label="评分" min-width="180">
          <template #default="{ row }">
            <div class="medical-health-score-line">
              <span>体况 {{ row.scoreBcs ?? 0 }}</span>
              <span>精神 {{ row.scoreMental ?? 0 }}</span>
              <span>食欲 {{ row.scoreAppetite ?? 0 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="体重" width="100">
          <template #default="{ row }">{{ row.weight ?? '' }} kg</template>
        </el-table-column>
        <el-table-column label="评估摘要" min-width="220" show-overflow-tooltip>
          <template #header><TableFilterHeader label="评估摘要" :filter="filters.summary" type="text" :active="isActive('summary')" /></template>
          <template #default="{ row }">{{ row.summary || '' }}</template>
        </el-table-column>
        <el-table-column label="兽医" min-width="120">
          <template #header><TableFilterHeader label="兽医" :filter="filters.username" type="text" :active="isActive('username')" /></template>
          <template #default="{ row }">{{ row.username || '' }}</template>
        </el-table-column>
        <el-table-column label="评估时间" min-width="160">
          <template #header><TableFilterHeader label="评估时间" :filter="filters.createTime" type="time" :active="isActive('createTime')" /></template>
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column width="40" class-name="action-col">
          <template #header><TableActionColumnHeader title="操作" :collapsed="actionCollapsed" @toggle="actionCollapsed = !actionCollapsed" /></template>
          <template #default="{ row }">
            <div class="table-action-cell">
              <div class="table-action-panel" :class="{ 'is-collapsed': actionCollapsed }">
                <el-button text type="primary" @click="goDetail(row)">查看</el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="user-admin-pagination">
        <el-pagination layout="prev, pager, next, total" :current-page="page.page" :page-size="page.size" :total="filteredAssessments.length" @current-change="(p) => page.page = p" />
      </div>
    </section>
  </el-card>

  <PetPickerDialog v-model:visible="petPickerVisible" @select="onPetSelected" />

  <el-dialog v-model="createDialogVisible" :title="'健康评估：' + targetPetName" width="520px">
    <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-position="top" class="pet-admin-form">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="年龄（月）" prop="age">
            <el-input-number v-model="createForm.age" :min="0" controls-position="right" class="full-width-control" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="体重（kg）" prop="weight">
            <el-input-number v-model="createForm.weight" :min="0" :precision="1" controls-position="right" class="full-width-control" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="评估摘要" prop="summary">
        <el-input v-model="createForm.summary" type="textarea" :rows="3" placeholder="请填写评估摘要" />
      </el-form-item>
      <el-form-item label="体况评分" prop="scoreBcs">
        <el-slider v-model="createForm.scoreBcs" :min="0" :max="100" show-input />
      </el-form-item>
      <el-form-item label="精神评分" prop="scoreMental">
        <el-slider v-model="createForm.scoreMental" :min="0" :max="100" show-input />
      </el-form-item>
      <el-form-item label="食欲评分" prop="scoreAppetite">
        <el-slider v-model="createForm.scoreAppetite" :min="0" :max="100" show-input />
      </el-form-item>
    </el-form>
    <div class="dialog-footer">
      <el-button @click="createDialogVisible = false">取消</el-button>
      <el-button type="warning" :loading="saving" @click="submitCreate">保存</el-button>
    </div>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, RefreshRight } from '@element-plus/icons-vue'
import { addHealthAssessment, getHealthAssessments } from '../api/services'
import { formatDate } from '../utils/format'
import { useTableFilters } from '../composables/useTableFilters'
import TableActionColumnHeader from './TableActionColumnHeader.vue'
import TableFilterHeader from './TableFilterHeader.vue'
import PetPickerDialog from './PetPickerDialog.vue'

const router = useRouter()
const loading = ref(false)
const rows = ref([])
const actionCollapsed = ref(false)
const page = reactive({ page: 1, size: 10 })
const petPickerVisible = ref(false)
const createDialogVisible = ref(false)
const saving = ref(false)
const targetPetId = ref('')
const targetPetName = ref('')
const createFormRef = ref()

const createForm = reactive({
  age: 0, weight: 0, scoreBcs: 80, scoreMental: 80, scoreAppetite: 80, summary: '',
})

const createRules = {
  summary: [{ required: true, message: '请填写评估摘要', trigger: 'blur' }],
}

const { filters, isActive, applyFilter } = useTableFilters({
  petName: { type: 'text' },
  summary: { type: 'text' },
  username: { type: 'text' },
  createTime: { type: 'time' },
})

const filteredAssessments = computed(() => applyFilter(rows.value || []))

const displayedRows = computed(() => {
  const start = (page.page - 1) * page.size
  return filteredAssessments.value.slice(start, start + page.size)
})

function goDetail(row) {
  if (row?.id) router.push(`/console/medical/health/${row.id}`)
}

async function loadAssessments() {
  loading.value = true
  try {
    const result = await getHealthAssessments({ page: 1, size: 500, sort: 'create_time', order: 'desc' })
    rows.value = Array.isArray(result?.records) ? result.records : []
  } catch (error) {
    ElMessage.warning(error?.message || '加载健康评估失败')
  } finally {
    loading.value = false
  }
}

function onPetSelected(pet) {
  petPickerVisible.value = false
  targetPetId.value = pet.id
  targetPetName.value = pet.name || ''
  Object.assign(createForm, {
    age: Number(pet?.age || 0),
    weight: 0,
    scoreBcs: 80,
    scoreMental: 80,
    scoreAppetite: 80,
    summary: '',
  })
  createDialogVisible.value = true
}

async function submitCreate() {
  if (!createFormRef.value || saving.value) return
  saving.value = true
  try {
    await createFormRef.value.validate()
    await addHealthAssessment(targetPetId.value, { ...createForm })
    ElMessage.success('健康评估已添加')
    createDialogVisible.value = false
    await loadAssessments()
  } catch (error) {
    if (error?.message) ElMessage.warning(error.message)
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadAssessments()
})
</script>

<style scoped>
.medical-health-cols-search {
  grid-template-columns: 1fr auto;
}

.medical-health-subtext {
  display: block;
  margin-top: 3px;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.5;
}

.medical-health-score-line {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.medical-health-score-line span {
  border: 1px solid rgba(231, 122, 59, 0.2);
  border-radius: 999px;
  background: rgba(255, 253, 249, 0.9);
  color: var(--primary-strong);
  padding: 3px 8px;
  font-size: 12px;
}
</style>
