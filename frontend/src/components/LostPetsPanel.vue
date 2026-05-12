<template>
  <div>
    <el-card class="profile-card pet-admin-card">
      <template #header>
        <div class="profile-card-header">
          <strong>丢失宠物</strong>
          <span>{{ activeSection === 'claims' ? '认领申请审核与记录' : '筛选和审核走失登记' }}</span>
        </div>
      </template>

      <el-tabs v-model="activeSection" class="lost-console-tabs">
        <el-tab-pane label="走失报备" name="reports">
          <section class="pet-admin-section">
            <section class="filter-panel pet-directory-filter-panel">
              <div class="pet-filter-row lost-filter-grid-top pet-filter-cols-4">
                <el-input v-model="lostPetSearchForm.name" class="filter-field-md" clearable placeholder="名称" />
                <el-select v-model="lostPetSearchForm.type" class="filter-field-sm" clearable filterable placeholder="类型" @change="handleLostPetSearchTypeChange">
                  <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
                </el-select>
                <el-select v-model="lostPetSearchForm.breed" class="filter-field-sm" clearable filterable placeholder="品种" :disabled="!lostPetSearchForm.type">
                  <el-option v-for="item in lostPetSearchBreedOptions" :key="item" :label="item" :value="item" />
                </el-select>
                <el-select v-model="lostPetSearchForm.status" class="filter-field-sm" clearable placeholder="状态">
                  <el-option v-for="item in lostPetStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </div>
              <div class="pet-filter-row lost-filter-grid-bottom lost-filter-cols-row2">
                <el-select v-model="lostPetSearchForm.province" class="filter-field-sm" clearable filterable placeholder="省份" @change="handleLostPetSearchProvinceChange">
                  <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
                </el-select>
                <el-select v-model="lostPetSearchForm.city" class="filter-field-sm" clearable filterable placeholder="城市" :disabled="!lostPetSearchForm.province">
                  <el-option v-for="item in lostPetSearchCityOptions" :key="item" :label="item" :value="item" />
                </el-select>
                <el-input v-model="lostPetSearchForm.address" class="filter-field-xl" clearable placeholder="详细地点" />
                <el-date-picker v-model="lostPetSearchForm.lostDate" class="filter-field-md" type="date" value-format="YYYY-MM-DD" placeholder="走失日期" />
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
                      <img :src="row.petCover || fallbackPetCover" :alt="row.name" />
                    </button>
                    <div>
                      <button class="pet-admin-name-button" type="button" @click="goLostPetDetail(row)">{{ row.name || '未命名' }}</button>
                      <span>{{ row.type || '宠物' }} · {{ row.breed || '品种待补充' }} · {{ row.sex || '未知' }}</span>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="120">
                <template #default="{ row }">
                  <el-tag :type="lostStatusTag(row.status)" effect="plain">{{ lostPetStatusText(row.status) }}</el-tag>
                </template>
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
        </el-tab-pane>

        <el-tab-pane label="认领申请" name="claims">
          <section class="pet-admin-section">
            <section class="filter-panel pet-directory-filter-panel">
              <div class="pet-filter-row claim-filter-row">
                <el-select v-model="claimSearchForm.status" class="filter-field-sm" clearable placeholder="申请状态">
                  <el-option v-for="item in claimStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
                <el-date-picker
                  v-model="claimSearchForm.timeRange"
                  type="daterange"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  class="filter-field-lg"
                />
                <div class="pet-filter-action">
                  <el-button class="warm-btn" :icon="Search" :loading="loadingClaims" @click="searchClaims">搜索</el-button>
                </div>
              </div>
            </section>

            <el-table :data="claimRows" v-loading="loadingClaims" class="user-admin-table">
              <el-table-column label="走失宠物" min-width="190">
                <template #default="{ row }">
                  <button class="pet-admin-name-button" type="button" @click="goClaimLostPet(row)">{{ row.lostPetName || '未命名' }}</button>
                  <span class="lost-claim-subtext">{{ row.lostPetType || '宠物' }} · {{ row.lostPetBreed || '品种待补充' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="申请人" min-width="150">
                <template #default="{ row }">
                  <div class="lost-claim-person">
                    <el-avatar :size="30" :src="row.applicantAvatar">{{ (row.applicantName || '用').slice(0, 1) }}</el-avatar>
                    <span>{{ row.applicantName || '未命名用户' }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="联系方式" min-width="130">
                <template #default="{ row }">{{ row.applicantPhone || '' }}</template>
              </el-table-column>
              <el-table-column label="状态" width="110">
                <template #default="{ row }">
                  <el-tag :type="claimStatusTag(row.status)" effect="plain">{{ claimStatusText(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="申请时间" min-width="160">
                <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
              </el-table-column>
              <el-table-column width="40" class-name="action-col">
                <template #header><TableActionColumnHeader title="操作" :collapsed="claimActionCollapsed" @toggle="claimActionCollapsed = !claimActionCollapsed" /></template>
                <template #default="{ row }">
                  <div class="table-action-cell">
                    <div class="table-action-panel" :class="{ 'is-collapsed': claimActionCollapsed }">
                      <el-button text type="primary" @click="openClaimDetail(row)">详情</el-button>
                      <el-button v-if="canReviewClaim(row)" text type="success" @click="openClaimReviewDialog(row, 'PASS')">通过</el-button>
                      <el-button v-if="canReviewClaim(row)" text type="danger" @click="openClaimReviewDialog(row, 'REJECT')">拒绝</el-button>
                      <el-button v-if="canCancelClaim(row)" text type="danger" @click="cancelClaim(row)">取消</el-button>
                    </div>
                  </div>
                </template>
              </el-table-column>
            </el-table>
            <div class="user-admin-pagination">
              <el-pagination layout="prev, pager, next, total" :current-page="claimPage.page" :page-size="claimPage.size" :total="claimTotal" @current-change="changeClaimPage" />
            </div>
          </section>
        </el-tab-pane>
      </el-tabs>
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

    <el-drawer v-model="claimDetailVisible" title="认领申请详情" size="560px">
      <section v-if="claimDetail" class="lost-claim-detail">
        <div class="lost-claim-detail-hero">
          <strong>{{ claimStatusText(claimDetail.status) }}</strong>
          <span>{{ claimDetail.lostPetName || '走失宠物' }}</span>
        </div>
        <dl>
          <div><dt>走失宠物</dt><dd>{{ claimDetail.lostPetName || '' }} · {{ claimDetail.lostPetType || '' }} · {{ claimDetail.lostPetBreed || '' }}</dd></div>
          <div><dt>流浪宠物档案</dt><dd>#{{ claimDetail.petId || '' }}</dd></div>
          <div><dt>申请人</dt><dd>{{ claimDetail.applicantName || '' }} · {{ claimDetail.applicantPhone || '' }}</dd></div>
          <div><dt>申请说明</dt><dd>{{ claimDetail.reason || '' }}</dd></div>
          <div><dt>审核说明</dt><dd>{{ claimDetail.rejectReason || '' }}</dd></div>
          <div><dt>申请时间</dt><dd>{{ formatDate(claimDetail.createTime) }}</dd></div>
          <div><dt>审核时间</dt><dd>{{ formatDate(claimDetail.reviewTime) }}</dd></div>
        </dl>
      </section>
    </el-drawer>

    <el-dialog v-model="claimReviewDialogVisible" title="认领申请审核" width="520px" :close-on-click-modal="false">
      <section v-if="claimReviewTarget" class="adoption-review-summary">
        <strong>{{ claimReviewTarget.lostPetName || '走失宠物' }}</strong>
        <span>{{ claimReviewTarget.applicantName || '申请人' }} · {{ claimStatusText(claimReviewTarget.status) }}</span>
      </section>
      <el-form label-position="top">
        <el-form-item label="审核结果">
          <el-radio-group v-model="claimReviewForm.status">
            <el-radio-button label="PASS">通过</el-radio-button>
            <el-radio-button label="REJECT">拒绝</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核说明">
          <el-input v-model="claimReviewForm.reason" type="textarea" :rows="4" placeholder="填写审核说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="claimReviewDialogVisible = false">取消</el-button>
        <el-button class="warm-btn" :loading="savingClaim" @click="submitClaimReview">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import {
  approveLostPetClaim,
  cancelLostPetClaim,
  getLostPetClaim,
  getLostPetClaims,
  getLostPets,
  updateLostPet,
  updateLostPetStatus,
} from '../api/lost'
import { useInformationCatalog } from '../composables/useInformationCatalog'
import { useConsoleGuards } from '../composables/useConsoleGuards'
import { useUserStore } from '../stores/user'
import { lostPetStatusOptions, lostPetStatusText, lostPetLocationText } from '../utils/roles'
import TableActionColumnHeader from './TableActionColumnHeader.vue'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const { canManageUsers } = useConsoleGuards()
const { ensureInformationCatalog, ensureCityOptions, ensureDistrictOptions, provinceOptions, typeOptions, getCityOptions, getDistrictOptions, getBreedOptions } = useInformationCatalog()

const fallbackPetCover = 'https://images.pexels.com/photos/58997/pexels-photo-58997.jpeg?auto=compress&cs=tinysrgb&w=320'
const activeSection = ref(route.query.section === 'claims' ? 'claims' : 'reports')

const lostPetEditFormRef = ref()
const lostPetReviewFormRef = ref()
const loadingLostPets = ref(false)
const loadingClaims = ref(false)
const savingLostPet = ref(false)
const savingClaim = ref(false)
const lostPetActionCollapsed = ref(false)
const claimActionCollapsed = ref(false)
const lostPetEditDialogVisible = ref(false)
const lostPetReviewDialogVisible = ref(false)
const claimDetailVisible = ref(false)
const claimReviewDialogVisible = ref(false)
const lostPetRows = ref([])
const claimRows = ref([])
const claimDetail = ref(null)
const claimReviewTarget = ref(null)
const lostPetTotal = ref(0)
const claimTotal = ref(0)
const lostPetPage = reactive({ page: 1, size: 10 })
const claimPage = reactive({ page: 1, size: 10 })

const lostPetSearchForm = reactive({ name: '', type: '', breed: '', status: '', province: '', city: '', address: '', lostDate: '' })
const claimSearchForm = reactive({ status: '', timeRange: [] })
const lostPetEditForm = reactive({ id: '', name: '', age: 0, sex: '', type: '', breed: '', features: '', lostTime: '', phone: '', description: '', province: '', city: '', district: '', detailAddress: '' })
const lostPetReviewForm = reactive({ id: '', status: '', reason: '' })
const claimReviewForm = reactive({ status: 'PASS', reason: '' })

const lostPetSearchCityOptions = computed(() => getCityOptions(lostPetSearchForm.province))
const lostPetSearchBreedOptions = computed(() => getBreedOptions(lostPetSearchForm.type))
const lostPetEditCityOptions = computed(() => getCityOptions(lostPetEditForm.province))
const lostPetEditDistrictOptions = computed(() => getDistrictOptions(lostPetEditForm.province, lostPetEditForm.city))
const lostPetEditBreedOptions = computed(() => getBreedOptions(lostPetEditForm.type))
const visibleLostPets = computed(() => lostPetRows.value)
const currentUserId = computed(() => String(userStore.profile?.id || ''))

const claimStatusOptions = [
  { label: '待审核', value: 'PENDING' },
  { label: '审核通过', value: 'PASS' },
  { label: '审核拒绝', value: 'REJECT' },
]
const claimStatusMap = Object.fromEntries(claimStatusOptions.map((item) => [item.value, item.label]))

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

function handleLostPetSearchTypeChange() { lostPetSearchForm.breed = '' }
function handleLostPetSearchProvinceChange() { lostPetSearchForm.city = ''; if (lostPetSearchForm.province) ensureCityOptions(lostPetSearchForm.province) }
function handleLostPetEditTypeChange() { lostPetEditForm.breed = '' }
function handleLostPetEditProvinceChange() { lostPetEditForm.city = ''; lostPetEditForm.district = ''; if (lostPetEditForm.province) ensureCityOptions(lostPetEditForm.province) }
function handleLostPetEditCityChange() { lostPetEditForm.district = ''; if (lostPetEditForm.province && lostPetEditForm.city) ensureDistrictOptions(lostPetEditForm.province, lostPetEditForm.city) }

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function claimStatusText(value) {
  return claimStatusMap[value] || value || ''
}

function claimStatusTag(value) {
  if (value === 'PASS') return 'success'
  if (value === 'REJECT') return 'info'
  return 'warning'
}

function lostStatusTag(value) {
  if (value === 'CLAIMED') return 'success'
  if (value === 'CLOSED') return 'info'
  return 'warning'
}

function isOwnLostPet(row) {
  return currentUserId.value && String(row?.ownerId || '') === currentUserId.value
}

function canEditLostPet(row) {
  return canManageUsers.value || (isOwnLostPet(row) && String(row?.status || '') === 'SEARCHING')
}

function canReviewClaim(row) {
  return canManageUsers.value && row?.status === 'PENDING'
}

function canCancelClaim(row) {
  return row?.status === 'PENDING' && (canManageUsers.value || String(row?.applicantId || '') === currentUserId.value)
}

function buildLostPetSearchQuery() {
  return {
    page: lostPetPage.page,
    size: lostPetPage.size,
    owner: canManageUsers.value ? undefined : [currentUserId.value],
    name: lostPetSearchForm.name.trim() || undefined,
    type: lostPetSearchForm.type ? [lostPetSearchForm.type] : undefined,
    bread: lostPetSearchForm.breed ? [lostPetSearchForm.breed] : undefined,
    status: lostPetSearchForm.status ? [lostPetSearchForm.status] : undefined,
    province: lostPetSearchForm.province || undefined,
    city: lostPetSearchForm.city || undefined,
    address: lostPetSearchForm.address.trim() || undefined,
    time0: lostPetSearchForm.lostDate || undefined,
  }
}

function buildClaimSearchQuery() {
  return {
    page: claimPage.page,
    size: claimPage.size,
    user: canManageUsers.value ? undefined : [currentUserId.value],
    status: claimSearchForm.status ? [claimSearchForm.status] : undefined,
    time0: claimSearchForm.timeRange?.[0],
    time1: claimSearchForm.timeRange?.[1],
  }
}

async function loadLostPetRows() {
  if (!canManageUsers.value && !currentUserId.value) return
  loadingLostPets.value = true
  try {
    const result = await getLostPets(buildLostPetSearchQuery())
    lostPetRows.value = Array.isArray(result?.records) ? result.records : []
    lostPetTotal.value = Number(result?.total || lostPetRows.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载丢失宠物列表失败')
  } finally {
    loadingLostPets.value = false
  }
}

async function loadClaimRows() {
  if (!canManageUsers.value && !currentUserId.value) return
  loadingClaims.value = true
  try {
    const result = await getLostPetClaims(buildClaimSearchQuery())
    claimRows.value = Array.isArray(result?.records) ? result.records : []
    claimTotal.value = Number(result?.total || claimRows.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载认领申请失败')
  } finally {
    loadingClaims.value = false
  }
}

function searchLostPets() { lostPetPage.page = 1; loadLostPetRows() }
function changeLostPetPage(page) { lostPetPage.page = page; loadLostPetRows() }
function searchClaims() { claimPage.page = 1; loadClaimRows() }
function changeClaimPage(page) { claimPage.page = page; loadClaimRows() }

function goLostPetDetail(row) {
  if (row?.id) router.push(`/lost/${row.id}`)
}

function goClaimLostPet(row) {
  if (row?.lostPetId) router.push(`/lost/${row.lostPetId}`)
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
    ElMessage.success('丢失宠物信息已保存')
    lostPetEditDialogVisible.value = false
    await loadLostPetRows()
  } catch (error) {
    ElMessage.warning(error?.message || '保存丢失宠物信息失败')
  } finally {
    savingLostPet.value = false
  }
}

async function saveLostPetReview() {
  if (!lostPetReviewFormRef.value || savingLostPet.value) return
  savingLostPet.value = true
  try {
    await lostPetReviewFormRef.value.validate()
    await updateLostPetStatus(lostPetReviewForm.id, { status: lostPetReviewForm.status, reason: lostPetReviewForm.reason.trim() })
    ElMessage.success('丢失宠物审核状态已保存')
    lostPetReviewDialogVisible.value = false
    await loadLostPetRows()
  } catch (error) {
    ElMessage.warning(error?.message || '保存审核状态失败')
  } finally {
    savingLostPet.value = false
  }
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

async function openClaimDetail(row) {
  claimDetailVisible.value = true
  claimDetail.value = row
  try {
    claimDetail.value = await getLostPetClaim(row.id)
  } catch (error) {
    ElMessage.warning(error?.message || '加载认领申请详情失败')
  }
}

function openClaimReviewDialog(row, status) {
  claimReviewTarget.value = row
  claimReviewForm.status = status
  claimReviewForm.reason = ''
  claimReviewDialogVisible.value = true
}

async function submitClaimReview() {
  if (!claimReviewTarget.value || savingClaim.value) return
  if (claimReviewForm.status === 'REJECT' && !claimReviewForm.reason.trim()) {
    ElMessage.warning('拒绝认领时请填写原因')
    return
  }
  if (claimReviewForm.status === 'PASS' && !claimReviewTarget.value.petId) {
    ElMessage.warning('缺少流浪宠物档案信息，无法通过认领')
    return
  }
  savingClaim.value = true
  try {
    await approveLostPetClaim(claimReviewTarget.value.id, {
      status: claimReviewForm.status,
      petId: claimReviewForm.status === 'PASS' ? claimReviewTarget.value.petId : undefined,
      reason: claimReviewForm.reason.trim() || (claimReviewForm.status === 'PASS' ? '认领信息核验通过' : undefined),
    })
    ElMessage.success(claimReviewForm.status === 'PASS' ? '认领申请已通过' : '认领申请已拒绝')
    claimReviewDialogVisible.value = false
    await loadClaimRows()
    if (activeSection.value === 'reports') await loadLostPetRows()
  } catch (error) {
    ElMessage.warning(error?.message || '审核认领申请失败')
  } finally {
    savingClaim.value = false
  }
}

async function cancelClaim(row) {
  try {
    await ElMessageBox.confirm(`确认取消「${row.lostPetName || '走失宠物'}」的认领申请？`, '取消认领申请', { type: 'warning', confirmButtonText: '取消申请', cancelButtonText: '返回' })
    await cancelLostPetClaim(row.id)
    ElMessage.success('认领申请已取消')
    await loadClaimRows()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.warning(error?.message || '取消认领申请失败')
  }
}

watch(activeSection, (section) => {
  router.replace({ path: route.path, query: section === 'claims' ? { ...route.query, section: 'claims' } : { ...route.query, section: undefined } })
  if (section === 'claims') loadClaimRows()
  else loadLostPetRows()
})

watch(() => route.query.section, (section) => {
  activeSection.value = section === 'claims' ? 'claims' : 'reports'
})

onMounted(async () => {
  await ensureInformationCatalog()
  if (activeSection.value === 'claims') loadClaimRows()
  else loadLostPetRows()
})
</script>

<style scoped>
.lost-console-tabs {
  --el-color-primary: #b56b35;
}

.pet-directory-filter-panel .pet-filter-cols-4 {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.pet-directory-filter-panel .lost-filter-cols-row2 {
  grid-template-columns: 1fr 1fr 1.5fr 1.5fr;
}

.pet-directory-filter-panel .lost-filter-search-row {
  justify-content: flex-end;
}

.claim-filter-row {
  grid-template-columns: minmax(160px, 0.6fr) minmax(240px, 1fr) auto;
}

.lost-claim-subtext {
  display: block;
  margin-top: 3px;
  color: var(--muted);
  font-size: 12px;
}

.lost-claim-person {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.lost-claim-detail {
  display: grid;
  gap: 18px;
}

.lost-claim-detail-hero {
  border: 1px solid rgba(179, 124, 82, 0.22);
  border-radius: 8px;
  padding: 18px;
  background: #fffaf3;
}

.lost-claim-detail-hero strong {
  display: block;
  color: #5d3927;
  font-size: 22px;
}

.lost-claim-detail-hero span {
  display: block;
  margin-top: 5px;
  color: var(--muted);
}

.lost-claim-detail dl {
  display: grid;
  gap: 14px;
}

.lost-claim-detail div {
  border-bottom: 1px solid rgba(179, 124, 82, 0.16);
  padding-bottom: 12px;
}

.lost-claim-detail dt {
  color: var(--muted);
  font-size: 12px;
}

.lost-claim-detail dd {
  margin: 5px 0 0;
  color: #3f2a1f;
  overflow-wrap: anywhere;
}

@media (max-width: 900px) {
  .pet-directory-filter-panel .pet-filter-cols-4,
  .pet-directory-filter-panel .lost-filter-cols-row2,
  .claim-filter-row {
    grid-template-columns: 1fr;
  }
}
</style>
