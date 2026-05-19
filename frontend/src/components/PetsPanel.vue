<template>
  <div>
    <el-card class="profile-card pet-admin-card">
      <template #header>
        <div class="profile-card-header">
          <strong>流浪宠物</strong>
          <div class="profile-actions">
            <el-button-group class="console-btn-group">
              <el-button class="warm-btn" :icon="Plus" @click="router.push('/pets/new')" />
              <el-button class="warm-btn" :icon="RefreshRight" :loading="loadingPets" @click="handleRefresh" />
              <el-button class="warm-btn" :icon="MoreFilled" :class="{ 'is-active': showSearchPanel }" @click="showSearchPanel = !showSearchPanel" />
            </el-button-group>
          </div>
        </div>
      </template>
      <div v-if="showSearchPanel" class="console-search-panel" @keyup.enter="handleRefresh">
        <div class="pet-filter-row pet-filter-row-primary">
          <el-input v-model="petSearchForm.name" class="filter-field-sm" placeholder="名称" clearable />
          <el-select v-model="petSearchForm.type" class="filter-field-sm" placeholder="类型" clearable>
            <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="petSearchForm.breed" class="filter-field-sm" placeholder="品种" clearable>
            <el-option v-for="item in breedOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="petSearchForm.sex" class="filter-field-sm" placeholder="性别" clearable>
            <el-option v-for="opt in sexOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
          <el-select v-model="petSearchForm.status" class="filter-field-sm" placeholder="状态" clearable>
            <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </div>
        <div class="pet-filter-row pet-filter-row-primary" style="margin-top: 8px">
          <div class="pet-age-range filter-field-sm">
            <el-input-number v-model="petSearchForm.age0" :min="0" :controls="false" placeholder="最小月龄" />
            <span>至</span>
            <el-input-number v-model="petSearchForm.age1" :min="0" :controls="false" placeholder="最大月龄" />
          </div>
          <el-select v-model="petSearchForm.province" class="filter-field-sm" placeholder="省" clearable filterable @change="petSearchForm.city = ''; petSearchForm.district = ''">
            <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="petSearchForm.city" class="filter-field-sm" placeholder="市" clearable filterable :disabled="!petSearchForm.province" @change="petSearchForm.district = ''">
            <el-option v-for="item in getCityOptions(petSearchForm.province)" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="petSearchForm.district" class="filter-field-sm" placeholder="县" clearable filterable :disabled="!petSearchForm.city">
            <el-option v-for="item in getDistrictOptions(petSearchForm.province, petSearchForm.city)" :key="item" :label="item" :value="item" />
          </el-select>
          <el-input v-model="petSearchForm.address" class="filter-field-lg" placeholder="地址" clearable />
        </div>
      </div>
      <section class="pet-admin-section">
        <el-table :data="filteredPets" v-loading="loadingPets" class="user-admin-table">
          <el-table-column min-width="180">
            <template #header><TableFilterHeader label="宠物" :filter="filters.name" type="text" :active="isActive('name')" /></template>
            <template #default="{ row }">
              <div class="pet-admin-pet">
                <button class="pet-admin-cover-button" type="button" @click="goPetProfile(row)">
                  <img :src="row.cover || 'https://images.pexels.com/photos/1170986/pexels-photo-1170986.jpeg?auto=compress&cs=tinysrgb&w=320'" :alt="row.name" />
                </button>
                <div>
                  <button class="pet-admin-name-button" type="button" @click="goPetProfile(row)">{{ row.name || '未命名' }}</button>
                  <span>{{ row.type || '宠物' }} · {{ row.sex || '未知' }} · {{ row.age ?? 0 }} 月</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column width="120">
            <template #header><TableFilterHeader label="状态" :filter="filters.status" type="enum" :options="statusOptions" :active="isActive('status')" /></template>
            <template #default="{ row }"><el-tag type="warning" effect="plain">{{ petStatusText(row.status) }}</el-tag></template>
          </el-table-column>
          <el-table-column min-width="220">
            <template #header><TableFilterHeader label="位置" :filter="filters.address" type="text" :active="isActive('address')" /></template>
            <template #default="{ row }">{{ petLocationText(row) }}</template>
          </el-table-column>
          <el-table-column min-width="140">
            <template #header><TableFilterHeader label="健康" :filter="filters.health" type="text" :active="isActive('health')" /></template>
            <template #default="{ row }">{{ row.health }}</template>
          </el-table-column>
          <el-table-column width="40" class-name="action-col">
            <template #header><TableActionColumnHeader title="操作" :collapsed="petActionCollapsed" @toggle="petActionCollapsed = !petActionCollapsed" /></template>
            <template #default="{ row }">
              <div class="table-action-cell">
                <div class="table-action-panel" :class="{ 'is-collapsed': petActionCollapsed }">
                  <el-button v-if="canManageUsers" text type="primary" @click="openPetDialog(row)">审核</el-button>
                  <el-button v-if="canEditPet(row)" text type="warning" @click="openPetEditor(row)">编辑</el-button>
                  <el-button v-if="canEditPet(row)" text type="danger" @click="removePet(row)">删除</el-button>
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <div class="user-admin-pagination">
          <el-pagination layout="prev, pager, next, total" :current-page="petPage.page" :page-size="petPage.size" :total="petTotal" @current-change="changePetPage" />
        </div>
      </section>
    </el-card>

    <el-dialog v-model="petCreateDialogVisible" title="记录流浪宠物" width="760px">
      <el-form ref="petFormRef" :model="petForm" :rules="petRules" label-position="top" class="pet-admin-form">
        <el-form-item label="宠物名称"><el-input v-model="petForm.name" placeholder="可留空，后续由救助站命名" /></el-form-item>
        <el-form-item label="年龄（月）"><el-input-number v-model="petForm.age" :min="0" /></el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-select v-model="petForm.sex"><el-option label="未知" value="未知" /><el-option label="公" value="公" /><el-option label="母" value="母" /></el-select>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <div class="catalog-field">
            <el-select v-model="petForm.type" placeholder="选择类型" filterable clearable @change="handlePetFormTypeChange">
              <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <el-input v-model="petForm.typeInput" placeholder="输入新类型" clearable @input="handlePetFormTypeChange" />
          </div>
        </el-form-item>
        <el-form-item label="品种">
          <div class="catalog-field">
            <el-select v-model="petForm.breed" placeholder="选择品种" filterable clearable :disabled="!resolvePetType(petForm)">
              <el-option v-for="item in petFormBreedOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <el-input v-model="petForm.breedInput" placeholder="输入新品种" clearable />
          </div>
        </el-form-item>
        <el-form-item label="健康状况"><el-input v-model="petForm.health" /></el-form-item>
        <el-form-item label="省份" prop="province">
          <el-select v-model="petForm.province" placeholder="选择省份" filterable clearable @change="handlePetFormProvinceChange">
            <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-select v-model="petForm.city" placeholder="选择城市" filterable clearable :disabled="!petForm.province" @change="handlePetFormCityChange">
            <el-option v-for="item in petFormCityOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="区县" prop="district">
          <el-select v-model="petForm.district" placeholder="选择区县 / 县级市" filterable clearable :disabled="!petForm.city">
            <el-option v-for="item in petFormDistrictOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细位置" prop="detailAddress"><el-input v-model="petForm.detailAddress" /></el-form-item>
        <el-form-item label="情况描述" class="pet-admin-span-2"><el-input v-model="petForm.description" type="textarea" :autosize="{ minRows: 3, maxRows: 5 }" /></el-form-item>
        <el-form-item label="图片/视频" class="pet-admin-span-2">
          <input ref="petMediaInputRef" class="profile-avatar-input" type="file" accept="image/*,video/*" multiple @change="uploadPetFiles" />
          <el-button :icon="Upload" :disabled="!petForm.id" :loading="uploadingPetMedia" @click="choosePetMedia">上传图片/视频</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="petCreateDialogVisible = false">完成</el-button>
        <el-button @click="resetPetForm">清空</el-button>
        <el-button type="warning" :loading="savingPet" @click="submitPetInfo">提交记录</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="petDialogVisible" title="审核宠物档案" width="520px">
      <el-form ref="petEditFormRef" :model="petEditForm" :rules="petEditRules" label-position="top" class="pet-review-form">
        <el-form-item label="审核/状态" prop="status">
          <el-select v-model="petEditForm.status" placeholder="请选择审核状态">
            <el-option v-for="item in petStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态原因" prop="reason">
          <el-input v-model="petEditForm.reason" type="textarea" :autosize="{ minRows: 4, maxRows: 7 }" />
        </el-form-item>
      </el-form>
      <div class="dialog-footer">
        <el-button @click="petDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="savingPet" @click="savePetInfo">保存审核</el-button>
      </div>
      <AuditRecordList :records="petAuditRecords" :loading="petAuditLoading" type="pet" :target-id="petEditForm.id" :status-labels="petStatusLabelMap" />
    </el-dialog>

  </div>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { MoreFilled, Plus, Upload, RefreshRight } from '@element-plus/icons-vue'
import { createPet, deletePetById, getPets, updatePetStatus, uploadPetMedia, getPetStatusRecords } from '../api/pets'
import { resolveCatalogValue, splitCatalogValue, useInformationCatalog } from '../composables/useInformationCatalog'
import { useConsoleGuards } from '../composables/useConsoleGuards'
import { useTableFilters } from '../composables/useTableFilters'
import { useUserStore } from '../stores/user'
import { ROLE, hasRole, petStatusOptions, petStatusLabelMap, petStatusText, petLocationText } from '../utils/roles'
import TableActionColumnHeader from './TableActionColumnHeader.vue'
import TableFilterHeader from './TableFilterHeader.vue'
import AuditRecordList from './AuditRecordList.vue'

const router = useRouter()
const userStore = useUserStore()
const { canManageUsers, canManageMedical } = useConsoleGuards()
const { ensureInformationCatalog, ensureCityOptions, ensureDistrictOptions, provinceOptions, typeOptions, getCityOptions, getDistrictOptions, getBreedOptions } = useInformationCatalog()
const breedOptions = computed(() => {
  const breeds = []
  typeOptions.value.forEach((type) => {
    getBreedOptions(type).forEach((breed) => {
      if (!breeds.includes(breed)) breeds.push(breed)
    })
  })
  return breeds
})

// --- State ---
const petFormRef = ref()
const petEditFormRef = ref()
const petMediaInputRef = ref()
const loadingPets = ref(false)
const showSearchPanel = ref(false)
const hasValue = (v) => v !== undefined && v !== null && v !== ''

const petSearchForm = reactive({
  name: '',
  type: '',
  breed: '',
  sex: '',
  age0: undefined,
  age1: undefined,
  status: '',
  province: '',
  city: '',
  district: '',
  address: '',
})
const savingPet = ref(false)
const uploadingPetMedia = ref(false)
const petActionCollapsed = ref(false)
const petCreateDialogVisible = ref(false)
const petDialogVisible = ref(false)
const petRows = ref([])
const petTotal = ref(0)
const petPage = reactive({ page: 1, size: 10 })
const petAuditRecords = ref([])
const petAuditLoading = ref(false)

const { filters, isActive, applyFilter } = useTableFilters({
  name: { type: 'text' },
  type: { type: 'enum' },
  breed: { type: 'enum' },
  sex: { type: 'enum' },
  status: { type: 'enum' },
  age: { type: 'number' },
  health: { type: 'text' },
  address: { type: 'text' },
})

const sexOptions = [
  { value: '公', label: '公' },
  { value: '母', label: '母' },
  { value: '未知', label: '未知' },
]

const statusOptions = [
  { value: 'WAITING', label: '待审核' },
  { value: 'APPROVED', label: '已通过' },
  { value: 'SHELTERED', label: '已收容' },
  { value: 'HEALTH', label: '健康' },
  { value: 'ADOPTED', label: '已领养' },
  { value: 'DIED', label: '死亡' },
  { value: 'REJECT', label: '已拒绝' },
]

const petForm = reactive({
  id: '', name: '', age: 0, sex: '未知', type: '', typeInput: '猫', breed: '', breedInput: '',
  health: '', description: '', province: '', city: '', district: '', detailAddress: '',
})

const petEditForm = reactive({
  id: '', name: '', age: 0, sex: '', type: '', typeInput: '', breed: '', breedInput: '',
  health: '', description: '', status: '', reason: '',
})

// Computed catalog options
const petFormCityOptions = computed(() => getCityOptions(petForm.province))
const petFormDistrictOptions = computed(() => getDistrictOptions(petForm.province, petForm.city))
const petFormBreedOptions = computed(() => getBreedOptions(resolveCatalogValue(petForm.type, petForm.typeInput)))

const filteredPets = computed(() => applyFilter(petRows.value || []))
const currentUserId = computed(() => String(userStore.profile?.id || ''))

// --- Helpers ---
function resolvePetType(form) { return resolveCatalogValue(form.type, form.typeInput) }
function resolvePetBreed(form) { return resolveCatalogValue(form.breed, form.breedInput) }
function applyCatalogValue(form, field, customField, value, options = []) {
  const { selected, custom } = splitCatalogValue(value, options)
  form[field] = selected
  form[customField] = custom
}

function validateRequiredValue(value, message, callback) {
  if (String(value || '').trim()) { callback(); return }
  callback(new Error(message))
}

function isOwnPet(row) {
  return currentUserId.value && String(row?.discoverId || '') === currentUserId.value
}

function canEditPet(row) {
  return canManageUsers.value || (isOwnPet(row) && ['WAITING', 'AGAINST'].includes(String(row?.status || '')))
}

// --- Validation rules ---
const petRules = {
  sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
  type: [{ validator: (_rule, _value, callback) => validateRequiredValue(resolvePetType(petForm), '请输入宠物类型', callback), trigger: ['blur', 'change'] }],
  province: [{ required: true, message: '请选择省份', trigger: 'change' }],
  city: [{ required: true, message: '请选择城市', trigger: 'change' }],
  district: [{ required: true, message: '请选择区县', trigger: 'change' }],
  detailAddress: [{ required: true, message: '请输入发现位置', trigger: 'blur' }],
}
const petEditRules = {
  status: [{ required: true, message: '请选择审核状态', trigger: 'change' }],
  reason: [{ required: true, message: '更改状态时请输入原因', trigger: 'blur' }],
}

// --- Functions ---
function resetPetForm() {
  Object.assign(petForm, { id: '', name: '', age: 0, sex: '未知', type: '', typeInput: '猫', breed: '', breedInput: '', health: '', description: '', province: '', city: '', district: '', detailAddress: '' })
}

function openPetCreateDialog() {
  resetPetForm()
  applyCatalogValue(petForm, 'type', 'typeInput', '猫', typeOptions.value)
  petCreateDialogVisible.value = true
}

function handlePetFormTypeChange() { petForm.breed = ''; petForm.breedInput = '' }
function handlePetFormProvinceChange() { petForm.city = ''; petForm.district = ''; if (petForm.province) ensureCityOptions(petForm.province) }
function handlePetFormCityChange() { petForm.district = ''; if (petForm.province && petForm.city) ensureDistrictOptions(petForm.province, petForm.city) }

async function loadPets() {
  if (!canManageUsers.value && !currentUserId.value) return
  loadingPets.value = true
  try {
    const result = await getPets({
      page: petPage.page, size: petPage.size, sort: 'update_time', order: 'desc',
      user: (canManageUsers.value || canManageMedical.value) ? undefined : currentUserId.value,
      name: petSearchForm.name || undefined,
      type: petSearchForm.type ? petSearchForm.type : undefined,
      breed: petSearchForm.breed ? petSearchForm.breed : undefined,
      sex: petSearchForm.sex || undefined,
      age0: hasValue(petSearchForm.age0) ? Number(petSearchForm.age0) : undefined,
      age1: hasValue(petSearchForm.age1) ? Number(petSearchForm.age1) : undefined,
      status: petSearchForm.status ? petSearchForm.status : undefined,
      province: petSearchForm.province || undefined,
      city: petSearchForm.city || undefined,
      district: petSearchForm.district || undefined,
      address: petSearchForm.address || undefined,
    })
    petRows.value = Array.isArray(result?.records) ? result.records : []
    petTotal.value = Number(result?.total || petRows.value.length)
  } catch (error) { ElMessage.warning(error?.message || '加载宠物列表失败') }
  finally { loadingPets.value = false }
}

function goPetProfile(row) { router.push(`/pets/${row.id}`) }
function openPetEditor(row) {
  Object.assign(petEditForm, { id: String(row.id || ''), name: row.name || '', age: Number(row.age || 0), sex: row.sex || '', type: '', typeInput: '', breed: '', breedInput: '', health: row.health || '', description: row.description || '', status: row.status || 'WAITING', reason: '' })
  petDialogVisible.value = true
  loadPetAuditRecords(row.id)
}

async function loadPetAuditRecords(petId) {
  petAuditLoading.value = true; petAuditRecords.value = []
  try { const res = await getPetStatusRecords(petId, { page: 1, size: 5 }); petAuditRecords.value = res?.records || res?.data || [] }
  catch { /* ignore */ } finally { petAuditLoading.value = false }
}

async function savePetInfo() {
  if (!petEditFormRef.value || savingPet.value) return
  savingPet.value = true
  try {
    await petEditFormRef.value.validate()
    await updatePetStatus(petEditForm.id, { status: petEditForm.status, reason: petEditForm.reason.trim() })
    ElMessage.success('宠物审核状态已保存')
    petDialogVisible.value = false
    await loadPets()
  } catch (error) { ElMessage.warning(error?.message || '保存审核状态失败') }
  finally { savingPet.value = false }
}

async function removePet(row) {
  try {
    await ElMessageBox.confirm(`确认删除「${row.name || row.id}」的宠物档案？`, '删除宠物档案', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
    await deletePetById(row.id); ElMessage.success('宠物档案已删除'); await loadPets()
  } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.warning(error?.message || '删除宠物档案失败') }
}

function changePetPage(page) { petPage.page = page; loadPets() }
function handleRefresh() { petPage.page = 1; loadPets() }

async function submitPetInfo() {
  if (!petFormRef.value || savingPet.value) return
  savingPet.value = true
  try {
    await petFormRef.value.validate()
    const result = await createPet({ name: petForm.name.trim(), age: Number(petForm.age || 0), sex: petForm.sex, type: resolvePetType(petForm), breed: resolvePetBreed(petForm), health: petForm.health.trim(), description: petForm.description.trim(), province: petForm.province.trim(), city: petForm.city.trim(), district: petForm.district.trim(), detailAddress: petForm.detailAddress.trim() })
    petForm.id = String(result?.id || '')
    ElMessage.success('流浪宠物信息已提交，可继续上传图片/视频')
    await loadPets()
  } catch (error) { ElMessage.warning(error?.message || '提交宠物信息失败') }
  finally { savingPet.value = false }
}

function choosePetMedia() {
  if (!petForm.id) { ElMessage.warning('请先提交宠物基础信息'); return }
  petMediaInputRef.value?.click()
}

async function uploadPetFiles(event) {
  const files = Array.from(event.target.files || [])
  event.target.value = ''
  if (!files.length || !petForm.id) return
  uploadingPetMedia.value = true
  try {
    for (let index = 0; index < files.length; index += 1) { await uploadPetMedia(petForm.id, { file: files[index], isCover: index === 0 }) }
    ElMessage.success('宠物图片/视频已上传'); await loadPets()
  } catch (error) { ElMessage.warning(error?.message || '上传媒体失败') }
  finally { uploadingPetMedia.value = false }
}

onMounted(async () => { await ensureInformationCatalog(); loadPets() })
</script>

<style scoped>
.pet-directory-filter-panel .pet-filter-cols-4 {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}
.pet-directory-filter-panel .pet-filter-cols-status-age {
  grid-template-columns: 1fr 2fr;
}
.pet-directory-filter-panel .pet-filter-cols-loc {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}
.pet-directory-filter-panel .pet-filter-search-row {
  justify-content: flex-end;
}

</style>
