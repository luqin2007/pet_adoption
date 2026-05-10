<template>
  <div>
    <el-card class="profile-card pet-admin-card">
      <template #header>
        <div class="profile-card-header"><strong>丢失宠物</strong><span>筛选和审核走失登记</span></div>
      </template>
      <section class="pet-admin-section">
        <section class="filter-panel pet-directory-filter-panel">
          <div class="pet-filter-row lost-filter-grid-top pet-filter-cols-4">
            <el-input v-model="lostPetSearchForm.name" clearable placeholder="名称" />
            <el-select v-model="lostPetSearchForm.type" clearable filterable placeholder="类型" @change="handleLostPetSearchTypeChange">
              <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <el-select v-model="lostPetSearchForm.breed" clearable filterable placeholder="品种" :disabled="!lostPetSearchForm.type">
              <el-option v-for="item in lostPetSearchBreedOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <el-select v-model="lostPetSearchForm.status" clearable placeholder="状态">
              <el-option v-for="item in lostPetStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </div>
          <div class="pet-filter-row lost-filter-grid-bottom lost-filter-cols-row2">
            <el-select v-model="lostPetSearchForm.province" clearable filterable placeholder="省份" @change="handleLostPetSearchProvinceChange">
              <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <el-select v-model="lostPetSearchForm.city" clearable filterable placeholder="城市" :disabled="!lostPetSearchForm.province">
              <el-option v-for="item in lostPetSearchCityOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <el-input v-model="lostPetSearchForm.address" clearable placeholder="详细地点" />
            <el-date-picker v-model="lostPetSearchForm.lostDate" type="date" value-format="YYYY-MM-DD" placeholder="走失日期" />
          </div>
          <div class="pet-filter-row lost-filter-search-row">
            <div class="pet-filter-action">
              <el-button class="warm-btn" :icon="Search" :loading="loadingLostPets" @click="searchLostPets">搜索</el-button>
            </div>
          </div>
        </section>
        <el-table :data="visibleLostPets" v-loading="loadingLostPets" class="user-admin-table">
          <el-table-column label="宠物" min-width="220">
            <template #default="{ row }">
              <div class="pet-admin-pet">
                <button class="pet-admin-cover-button" type="button" @click="goLostPetDetail(row)">
                  <img :src="row.petCover || 'https://images.pexels.com/photos/58997/pexels-photo-58997.jpeg?auto=compress&cs=tinysrgb&w=320'" :alt="row.name" />
                </button>
                <div>
                  <button class="pet-admin-name-button" type="button" @click="goLostPetDetail(row)">{{ row.name || '未命名' }}</button>
                  <span>{{ row.type || '宠物' }} · {{ row.breed || '品种待补充' }} · {{ row.sex || '未知' }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }"><el-tag type="warning" effect="plain">{{ lostPetStatusText(row.status) }}</el-tag></template>
          </el-table-column>
          <el-table-column label="走失时间" min-width="140">
            <template #default="{ row }">{{ row.lostTime ? String(row.lostTime).slice(0, 10) : '时间待补充' }}</template>
          </el-table-column>
          <el-table-column label="位置" min-width="240">
            <template #default="{ row }">{{ lostPetLocationText(row) }}</template>
          </el-table-column>
          <el-table-column label="联系人" min-width="180">
            <template #default="{ row }">{{ row.ownerName || '联系人待补充' }} · {{ row.contactPhone || '电话待补充' }}</template>
          </el-table-column>
          <el-table-column label="匹配结果" min-width="180">
            <template #default="{ row }">{{ row.petName || '暂未匹配到流浪宠物档案' }}</template>
          </el-table-column>
          <el-table-column width="40" class-name="action-col">
            <template #header><TableActionColumnHeader title="操作" :collapsed="lostPetActionCollapsed" @toggle="lostPetActionCollapsed = !lostPetActionCollapsed" /></template>
            <template #default="{ row }">
              <div class="table-action-cell">
                <div class="table-action-panel" :class="{ 'is-collapsed': lostPetActionCollapsed }">
                  <el-button v-if="canManageUsers" text type="primary" @click="openLostPetReviewDialog(row)">审核</el-button>
                  <el-button v-if="canEditLostPet(row)" text type="warning" @click="openLostPetEditDialog(row)">编辑</el-button>
                  <el-button v-if="canEditLostPet(row)" text type="danger" @click="removeLostPet(row)">删除</el-button>
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <div class="user-admin-pagination">
          <el-pagination layout="prev, pager, next, total" :current-page="lostPetPage.page" :page-size="lostPetPage.size" :total="lostPetTotal" @current-change="changeLostPetPage" />
        </div>
      </section>
    </el-card>

    <el-dialog v-model="lostPetEditDialogVisible" title="编辑丢失宠物" width="760px">
      <el-form ref="lostPetEditFormRef" :model="lostPetEditForm" :rules="lostPetEditRules" label-position="top" class="pet-admin-form">
        <el-form-item label="宠物名称" prop="name"><el-input v-model="lostPetEditForm.name" placeholder="请输入宠物名称" /></el-form-item>
        <el-form-item label="年龄（月）" prop="age"><el-input-number v-model="lostPetEditForm.age" :min="0" :controls="false" class="full-width-control" /></el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-select v-model="lostPetEditForm.sex" placeholder="请选择性别"><el-option label="未知" value="未知" /><el-option label="公" value="公" /><el-option label="母" value="母" /></el-select>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="lostPetEditForm.type" placeholder="宠物类型" filterable allow-create default-first-option clearable @change="handleLostPetEditTypeChange">
            <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="品种">
          <el-select v-model="lostPetEditForm.breed" placeholder="宠物品种" filterable allow-create default-first-option clearable :disabled="!lostPetEditForm.type">
            <el-option v-for="item in lostPetEditBreedOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="走失日期" prop="lostTime"><el-date-picker v-model="lostPetEditForm.lostTime" type="date" value-format="YYYY-MM-DD" placeholder="选择走失日期" class="full-width-control" /></el-form-item>
        <el-form-item label="联系电话" prop="phone"><el-input v-model="lostPetEditForm.phone" placeholder="请输入联系电话" /></el-form-item>
        <el-form-item label="省份" prop="province">
          <el-select v-model="lostPetEditForm.province" placeholder="省份" filterable clearable @change="handleLostPetEditProvinceChange">
            <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-select v-model="lostPetEditForm.city" placeholder="城市" filterable clearable :disabled="!lostPetEditForm.province" @change="handleLostPetEditCityChange">
            <el-option v-for="item in lostPetEditCityOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="区县" prop="district">
          <el-select v-model="lostPetEditForm.district" placeholder="区县" filterable clearable :disabled="!lostPetEditForm.city">
            <el-option v-for="item in lostPetEditDistrictOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="明显特征"><el-input v-model="lostPetEditForm.features" placeholder="例如：项圈、毛色、体型特征" /></el-form-item>
        <el-form-item label="详细地点" prop="detailAddress" class="pet-admin-span-2"><el-input v-model="lostPetEditForm.detailAddress" placeholder="请输入详细地点" /></el-form-item>
        <el-form-item label="补充说明" class="pet-admin-span-2"><el-input v-model="lostPetEditForm.description" type="textarea" :autosize="{ minRows: 4, maxRows: 7 }" placeholder="补充走失经过或识别信息" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="lostPetEditDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="savingLostPet" @click="saveLostPetEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="lostPetReviewDialogVisible" title="审核丢失宠物" width="520px">
      <el-form ref="lostPetReviewFormRef" :model="lostPetReviewForm" :rules="lostPetReviewRules" label-position="top" class="pet-review-form">
        <el-form-item label="审核/状态" prop="status">
          <el-select v-model="lostPetReviewForm.status" placeholder="请选择审核状态">
            <el-option v-for="item in lostPetStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核原因" prop="reason">
          <el-input v-model="lostPetReviewForm.reason" type="textarea" :autosize="{ minRows: 4, maxRows: 7 }" placeholder="例如：信息核验通过，保留为寻找中" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="lostPetReviewDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="savingLostPet" @click="saveLostPetReview">保存审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getLostPets, updateLostPet, updateLostPetStatus } from '../api/lost'
import { resolveCatalogValue, splitCatalogValue, useInformationCatalog } from '../composables/useInformationCatalog'
import { useConsoleGuards } from '../composables/useConsoleGuards'
import { useUserStore } from '../stores/user'
import { lostPetStatusOptions, lostPetStatusText, lostPetLocationText } from '../utils/roles'
import TableActionColumnHeader from './TableActionColumnHeader.vue'

const userStore = useUserStore()
const router = useRouter()
const { canManageUsers } = useConsoleGuards()
const { ensureInformationCatalog, ensureCityOptions, ensureDistrictOptions, provinceOptions, typeOptions, getCityOptions, getBreedOptions } = useInformationCatalog()

// --- State ---
const lostPetEditFormRef = ref()
const lostPetReviewFormRef = ref()
const loadingLostPets = ref(false)
const savingLostPet = ref(false)
const lostPetActionCollapsed = ref(false)
const lostPetEditDialogVisible = ref(false)
const lostPetReviewDialogVisible = ref(false)
const lostPetRows = ref([])
const lostPetTotal = ref(0)
const lostPetPage = reactive({ page: 1, size: 10 })

const lostPetSearchForm = reactive({ name: '', type: '', breed: '', status: '', province: '', city: '', address: '', lostDate: '' })
const lostPetEditForm = reactive({ id: '', name: '', age: 0, sex: '', type: '', breed: '', features: '', lostTime: '', phone: '', description: '', province: '', city: '', district: '', detailAddress: '' })
const lostPetReviewForm = reactive({ id: '', status: '', reason: '' })

const lostPetSearchCityOptions = computed(() => getCityOptions(lostPetSearchForm.province))
const lostPetSearchBreedOptions = computed(() => getBreedOptions(lostPetSearchForm.type))
const lostPetEditCityOptions = computed(() => getCityOptions(lostPetEditForm.province))
const lostPetEditDistrictOptions = computed(() => getDistrictOptions(lostPetEditForm.province, lostPetEditForm.city))
const lostPetEditBreedOptions = computed(() => getBreedOptions(lostPetEditForm.type))

const visibleLostPets = computed(() => lostPetRows.value)
const currentUserId = computed(() => String(userStore.profile?.id || ''))

// --- Validation rules ---
const lostPetEditRules = {
  name: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
  age: [{ required: true, message: '请输入年龄', trigger: 'change' }],
  sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
  type: [{ required: true, message: '请输入宠物类型', trigger: 'change' }],
  lostTime: [{ required: true, message: '请选择走失日期', trigger: 'change' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  province: [{ required: true, message: '请选择省份', trigger: 'change' }],
  city: [{ required: true, message: '请选择城市', trigger: 'change' }],
  district: [{ required: true, message: '请选择区县', trigger: 'change' }],
  detailAddress: [{ required: true, message: '请输入详细地点', trigger: 'blur' }],
}
const lostPetReviewRules = {
  status: [{ required: true, message: '请选择审核状态', trigger: 'change' }],
  reason: [{ required: true, message: '请输入审核原因', trigger: 'blur' }],
}

// --- Functions ---
function handleLostPetSearchTypeChange() { lostPetSearchForm.breed = '' }
function handleLostPetSearchProvinceChange() { lostPetSearchForm.city = ''; if (lostPetSearchForm.province) ensureCityOptions(lostPetSearchForm.province) }
function handleLostPetEditTypeChange() { lostPetEditForm.breed = '' }
function handleLostPetEditProvinceChange() { lostPetEditForm.city = ''; lostPetEditForm.district = ''; if (lostPetEditForm.province) ensureCityOptions(lostPetEditForm.province) }
function handleLostPetEditCityChange() { lostPetEditForm.district = ''; if (lostPetEditForm.province && lostPetEditForm.city) ensureDistrictOptions(lostPetEditForm.province, lostPetEditForm.city) }

function isOwnLostPet(row) {
  return currentUserId.value && String(row?.ownerId || '') === currentUserId.value
}

function canEditLostPet(row) {
  return canManageUsers.value || (isOwnLostPet(row) && String(row?.status || '') === 'SEARCHING')
}

function buildLostPetSearchQuery() {
  return {
    page: lostPetPage.page, size: lostPetPage.size,
    owner: canManageUsers.value ? undefined : [currentUserId.value],
    name: lostPetSearchForm.name.trim() || undefined, type: lostPetSearchForm.type ? [lostPetSearchForm.type] : undefined,
    bread: lostPetSearchForm.breed ? [lostPetSearchForm.breed] : undefined, status: lostPetSearchForm.status ? [lostPetSearchForm.status] : undefined,
    province: lostPetSearchForm.province || undefined, city: lostPetSearchForm.city || undefined,
    address: lostPetSearchForm.address.trim() || undefined, time0: lostPetSearchForm.lostDate || undefined,
  }
}

async function loadLostPetRows() {
  if (!canManageUsers.value && !currentUserId.value) return
  loadingLostPets.value = true
  try {
    const result = await getLostPets(buildLostPetSearchQuery())
    lostPetRows.value = Array.isArray(result?.records) ? result.records : []
    lostPetTotal.value = Number(result?.total || lostPetRows.value.length)
  } catch (error) { ElMessage.warning(error?.message || '加载丢失宠物列表失败') }
  finally { loadingLostPets.value = false }
}

function searchLostPets() { lostPetPage.page = 1; loadLostPetRows() }
function changeLostPetPage(page) { lostPetPage.page = page; loadLostPetRows() }

function goLostPetDetail(row) {
  if (row?.id) router.push(`/lost/${row.id}`)
}

function openLostPetEditDialog(row) {
  Object.assign(lostPetEditForm, {
    id: String(row.id || ''), name: row.name || '', age: Number(row.age || 0), sex: row.sex || '未知',
    type: row.type || '', breed: row.breed || '', features: row.features || '',
    lostTime: row.lostTime ? String(row.lostTime).slice(0, 10) : '', phone: row.contactPhone || '',
    description: row.description || '', province: row.location?.province || '', city: row.location?.city || '',
    district: row.location?.district || '', detailAddress: row.location?.detailAddress || '',
  })
  if (lostPetEditForm.province) ensureCityOptions(lostPetEditForm.province)
  if (lostPetEditForm.province && lostPetEditForm.city) ensureDistrictOptions(lostPetEditForm.province, lostPetEditForm.city)
  lostPetEditDialogVisible.value = true
}

function openLostPetReviewDialog(row) {
  Object.assign(lostPetReviewForm, { id: String(row.id || ''), status: row.status || 'SEARCHING', reason: '' })
  lostPetReviewDialogVisible.value = true
}

async function saveLostPetEdit() {
  if (!lostPetEditFormRef.value || savingLostPet.value) return
  savingLostPet.value = true
  try {
    await lostPetEditFormRef.value.validate()
    await updateLostPet(lostPetEditForm.id, {
      name: lostPetEditForm.name.trim(), age: Number(lostPetEditForm.age || 0), sex: lostPetEditForm.sex,
      type: lostPetEditForm.type.trim(), breed: lostPetEditForm.breed.trim(), features: lostPetEditForm.features.trim(),
      lostTime: `${lostPetEditForm.lostTime}T00:00:00`, phone: lostPetEditForm.phone.trim(),
      description: lostPetEditForm.description.trim(), province: lostPetEditForm.province, city: lostPetEditForm.city,
      district: lostPetEditForm.district, detailAddress: lostPetEditForm.detailAddress.trim(),
    })
    ElMessage.success('丢失宠物信息已保存'); lostPetEditDialogVisible.value = false; await loadLostPetRows()
  } catch (error) { ElMessage.warning(error?.message || '保存丢失宠物信息失败') }
  finally { savingLostPet.value = false }
}

async function saveLostPetReview() {
  if (!lostPetReviewFormRef.value || savingLostPet.value) return
  savingLostPet.value = true
  try {
    await lostPetReviewFormRef.value.validate()
    await updateLostPetStatus(lostPetReviewForm.id, { status: lostPetReviewForm.status, reason: lostPetReviewForm.reason.trim() })
    ElMessage.success('丢失宠物审核状态已保存'); lostPetReviewDialogVisible.value = false; await loadLostPetRows()
  } catch (error) { ElMessage.warning(error?.message || '保存审核状态失败') }
  finally { savingLostPet.value = false }
}

async function removeLostPet(row) {
  try {
    await ElMessageBox.confirm(`确认删除「${row.name || row.id}」的丢失宠物登记？`, '删除丢失宠物', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
    await updateLostPetStatus(row.id, {
      status: 'CLOSED',
      reason: `${Date.now()} 主动删除`,
    })
    ElMessage.success('丢失宠物登记已删除')
    await loadLostPetRows()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.warning(error?.message || '删除丢失宠物失败')
  }
}

onMounted(async () => { await ensureInformationCatalog(); loadLostPetRows() })
</script>

<style scoped>
.pet-directory-filter-panel .pet-filter-cols-4 {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}
.pet-directory-filter-panel .lost-filter-cols-row2 {
  grid-template-columns: 1fr 1fr 1.5fr 1.5fr;
}
.pet-directory-filter-panel .lost-filter-search-row {
  justify-content: flex-end;
}
</style>
