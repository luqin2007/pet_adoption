<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, RefreshRight } from '@element-plus/icons-vue'
import { getVolunteerProfile, getVolunteerProfiles, updateVolunteerProfile, updateVolunteerProfileStatus } from '../../api/volunteer'
import { useInformationCatalog } from '../../composables/useInformationCatalog'
import { useTableFilters } from '../../composables/useTableFilters'
import { useUserStore } from '../../stores/user'
import TableFilterHeader from '../TableFilterHeader.vue'
import TableActionColumnHeader from '../TableActionColumnHeader.vue'
import { useRoute, useRouter } from 'vue-router'
import { ROLE, hasRole } from '../../utils/roles'

const props = defineProps({
  section: { type: String, default: 'profiles' },
  hideTabs: { type: Boolean, default: false },
  allowedSections: { type: Array, default: null },
})

const emit = defineEmits(['update:section'])

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const loginRole = computed(() => Number(userStore.profile.role || 0))
const isAdmin = computed(() => hasRole(loginRole.value, ROLE.ADMIN))
const isWorker = computed(() => isAdmin.value || hasRole(loginRole.value, ROLE.WORKER))
const isVolunteer = computed(() => hasRole(loginRole.value, ROLE.VOLUNTEER))
const loginUserId = computed(() => String(userStore.profile.id || ''))

function formatDate(value, withTime = false) {
  if (!value) return '待补充'
  const text = String(value)
  return withTime ? text.slice(0, 16).replace('T', ' ') : text.slice(0, 10)
}

function toApiDateTime(value) {
  if (!value) return undefined
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toISOString()
}

function locationText(row) {
  return [row.province, row.city, row.district, row.serviceAddress || row.address || row.detailAddress].filter(Boolean).join(' · ') || '位置待补充'
}

function tagTypeByStatus(value) {
  if (['APPROVED', 'ACTIVE', 'ISSUED', 'COMPLETED'].includes(value)) return 'success'
  if (['UNDER_REVIEW', 'CONFIRMED', 'IN_PROGRESS'].includes(value)) return 'primary'
  if (['REJECTED', 'CANCELED', 'DISABLED', 'CANCELLED', 'ABSENT'].includes(value)) return 'info'
  return 'warning'
}

const profileStatusOptions = [
  { label: '启用', value: 'ACTIVE' },
  { label: '停用', value: 'DISABLED' },
]

function profileStatusText(value) {
  return profileStatusOptions.find((item) => item.value === value)?.label || value || '待补充'
}

const profileLoading = ref(false)
const profileActionCollapsed = ref(false)
const profileRows = ref([])
const profileTotal = ref(0)
const profileDialogVisible = ref(false)
const profileFormRef = ref()
const savingProfile = ref(false)

const profilePage = reactive({ page: 1, size: 10 })

const profileSearch = reactive({
  keyword: '',
  status: '',
})

const profileForm = reactive({
  id: '',
  realName: '',
  sex: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  skills: '',
  serviceIntention: '',
  availableTimeDesc: '',
  remark: '',
})

const { filters: profileFilters, isActive: isProfileFilterActive, applyFilter: applyProfileFilter } = useTableFilters({
  name: { type: 'text' },
  username: { type: 'text' },
  region: { type: 'text' },
  skills: { type: 'text' },
  status: { type: 'enum' },
})

const filteredProfiles = computed(() => applyProfileFilter(profileRows.value || []))

const profileRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  province: [{ required: true, message: '请选择省份', trigger: 'change' }],
  city: [{ required: true, message: '请选择城市', trigger: 'change' }],
  district: [{ required: true, message: '请选择区县', trigger: 'change' }],
  detailAddress: [{ required: true, message: '请输入详细地点', trigger: 'blur' }],
}

const {
  ensureInformationCatalog,
  ensureCityOptions,
  ensureDistrictOptions,
  provinceOptions,
  getCityOptions,
  getDistrictOptions,
} = useInformationCatalog()

const profileFormCityOptions = computed(() => getCityOptions(profileForm.province))
const profileFormDistrictOptions = computed(() => getDistrictOptions(profileForm.province, profileForm.city))

function handleProfileFormProvinceChange() {
  profileForm.city = ''
  profileForm.district = ''
  if (profileForm.province) {
    ensureCityOptions(profileForm.province)
  }
}

function handleProfileFormCityChange() {
  profileForm.district = ''
  if (profileForm.province && profileForm.city) {
    ensureDistrictOptions(profileForm.province, profileForm.city)
  }
}

function buildProfileQuery() {
  return {
    page: profilePage.page,
    size: profilePage.size,
    keyword: profileSearch.keyword.trim() || undefined,
    status: profileSearch.status ? [profileSearch.status] : undefined,
  }
}

async function loadProfiles() {
  if (!isWorker.value) return
  profileLoading.value = true
  try {
    const result = await getVolunteerProfiles(buildProfileQuery())
    profileRows.value = Array.isArray(result?.records) ? result.records : []
    profileTotal.value = Number(result?.total || 0)
  } catch (error) {
    ElMessage.warning(error?.message || '加载志愿者档案失败')
  } finally {
    profileLoading.value = false
  }
}

function searchProfiles() {
  profilePage.page = 1
  loadProfiles()
}

function changeProfilePage(page) {
  profilePage.page = page
  loadProfiles()
}

function resetProfileForm() {
  profileForm.id = ''
  profileForm.realName = ''
  profileForm.sex = ''
  profileForm.phone = ''
  profileForm.province = ''
  profileForm.city = ''
  profileForm.district = ''
  profileForm.detailAddress = ''
  profileForm.skills = ''
  profileForm.serviceIntention = ''
  profileForm.availableTimeDesc = ''
  profileForm.remark = ''
}

async function openProfileDialog(row) {
  resetProfileForm()
  profileDialogVisible.value = true
  profileLoading.value = true
  try {
    const detail = await getVolunteerProfile(row.id)
    Object.assign(profileForm, {
      id: detail.id || row.id,
      realName: detail.realName || '',
      sex: detail.sex || '',
      phone: detail.phone || '',
      province: detail.province || '',
      city: detail.city || '',
      district: detail.district || '',
      detailAddress: detail.address || '',
      skills: detail.skills || '',
      serviceIntention: detail.serviceIntention || '',
      availableTimeDesc: detail.availableTimeDesc || '',
      remark: detail.remark || '',
    })
    if (profileForm.province) ensureCityOptions(profileForm.province)
    if (profileForm.province && profileForm.city) ensureDistrictOptions(profileForm.province, profileForm.city)
  } catch (error) {
    profileDialogVisible.value = false
    ElMessage.warning(error?.message || '加载志愿者档案详情失败')
  } finally {
    profileLoading.value = false
  }
}

async function saveProfile() {
  if (!profileFormRef.value) return
  try {
    await profileFormRef.value.validate()
  } catch {
    return
  }
  savingProfile.value = true
  try {
    await updateVolunteerProfile(profileForm.id, {
      realName: profileForm.realName.trim(),
      sex: profileForm.sex,
      phone: profileForm.phone.trim(),
      province: profileForm.province,
      city: profileForm.city,
      district: profileForm.district,
      detailAddress: profileForm.detailAddress.trim(),
      skills: profileForm.skills.trim() || undefined,
      serviceIntention: profileForm.serviceIntention.trim() || undefined,
      availableTimeDesc: profileForm.availableTimeDesc.trim() || undefined,
      remark: profileForm.remark.trim() || undefined,
    })
    ElMessage.success('志愿者档案已保存')
    profileDialogVisible.value = false
    loadProfiles()
  } catch (error) {
    ElMessage.warning(error?.message || '保存志愿者档案失败')
  } finally {
    savingProfile.value = false
  }
}

async function toggleProfileStatus(row) {
  const nextStatus = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  const actionText = nextStatus === 'ACTIVE' ? '启用' : '停用'
  try {
    await ElMessageBox.confirm(`确认${actionText}「${row.realName || row.username || '该志愿者'}」的志愿者档案？`, `${actionText}档案`, {
      type: 'warning',
      confirmButtonText: actionText,
      cancelButtonText: '取消',
    })
    await updateVolunteerProfileStatus(row.id, nextStatus)
    ElMessage.success(`志愿者档案已${actionText}`)
    loadProfiles()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.warning(error?.message || `${actionText}档案失败`)
    }
  }
}

function goVolunteerActivity(row) {
  const volunteerId = row?.volunteerId || row?.userId
  if (!volunteerId) return
  router.push({ name: 'console-volunteer', query: { volunteer: volunteerId } })
}

function handlePlusClick() {
  router.push('/console/volunteer')
}

function searchCurrentSection() {
  searchProfiles()
}

defineExpose({ handlePlusClick, searchCurrentSection })

onMounted(async () => {
  await ensureInformationCatalog()
  await loadProfiles()
})
</script>

<template>
  <section class="pet-admin-section">
    <el-table :data="filteredProfiles" v-loading="profileLoading" class="user-admin-table">
      <el-table-column min-width="180">
        <template #header>
          <TableFilterHeader label="志愿者" :filter="profileFilters.name" type="text" :active="isProfileFilterActive('name')" />
        </template>
        <template #default="{ row }">
          <div class="volunteer-person-cell">
            <strong>{{ row.realName || row.username || '未命名' }}</strong>
            <span>{{ row.phone || '联系方式待补充' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column min-width="130">
        <template #header>
          <TableFilterHeader label="用户名" :filter="profileFilters.username" type="text" :active="isProfileFilterActive('username')" />
        </template>
        <template #default="{ row }">{{ row.username || '' }}</template>
      </el-table-column>
      <el-table-column min-width="180">
        <template #header>
          <TableFilterHeader label="地区" :filter="profileFilters.region" type="text" :active="isProfileFilterActive('region')" />
        </template>
        <template #default="{ row }">{{ locationText(row) }}</template>
      </el-table-column>
      <el-table-column min-width="220" show-overflow-tooltip>
        <template #header>
          <TableFilterHeader label="技能" :filter="profileFilters.skills" type="text" :active="isProfileFilterActive('skills')" />
        </template>
        <template #default="{ row }">{{ row.skills || '' }}</template>
      </el-table-column>
      <el-table-column width="100">
        <template #header>
          <TableFilterHeader label="状态" :filter="profileFilters.status" type="enum" :options="profileStatusOptions" :active="isProfileFilterActive('status')" />
        </template>
        <template #default="{ row }">
          <el-tag :type="tagTypeByStatus(row.status)" effect="plain">{{ profileStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column width="40" class-name="action-col">
        <template #header>
          <TableActionColumnHeader title="操作" :collapsed="profileActionCollapsed" @toggle="profileActionCollapsed = !profileActionCollapsed" />
        </template>
        <template #default="{ row }">
          <div class="table-action-cell">
            <div class="table-action-panel" :class="{ 'is-collapsed': profileActionCollapsed }">
              <el-button text type="warning" @click="openProfileDialog(row)">修改</el-button>
              <el-button text :type="row.status === 'ACTIVE' ? 'danger' : 'success'" @click="toggleProfileStatus(row)">
                {{ row.status === 'ACTIVE' ? '停用' : '启用' }}
              </el-button>
              <el-button text type="primary" @click="goVolunteerActivity(row)">排班</el-button>
            </div>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="user-admin-pagination">
      <el-pagination layout="prev, pager, next, total" :current-page="profilePage.page" :page-size="profilePage.size" :total="profileTotal" @current-change="changeProfilePage" />
    </div>
  </section>

  <el-dialog v-model="profileDialogVisible" title="修改志愿者档案" width="760px">
    <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-position="top" class="pet-admin-form">
      <el-form-item label="真实姓名" prop="realName">
        <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
      </el-form-item>
      <el-form-item label="性别" prop="sex">
        <el-select v-model="profileForm.sex" placeholder="请选择性别">
          <el-option label="未知" value="未知" />
          <el-option label="男" value="男" />
          <el-option label="女" value="女" />
        </el-select>
      </el-form-item>
      <el-form-item label="联系电话" prop="phone">
        <el-input v-model="profileForm.phone" placeholder="请输入联系电话" />
      </el-form-item>
      <el-form-item label="省份" prop="province">
        <el-select v-model="profileForm.province" filterable clearable placeholder="省份" @change="handleProfileFormProvinceChange">
          <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="城市" prop="city">
        <el-select v-model="profileForm.city" filterable clearable placeholder="城市" :disabled="!profileForm.province" @change="handleProfileFormCityChange">
          <el-option v-for="item in profileFormCityOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="区县" prop="district">
        <el-select v-model="profileForm.district" filterable clearable placeholder="区县" :disabled="!profileForm.city">
          <el-option v-for="item in profileFormDistrictOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="详细地点" prop="detailAddress" class="pet-admin-span-2">
        <el-input v-model="profileForm.detailAddress" placeholder="请输入详细地点" />
      </el-form-item>
      <el-form-item label="技能说明" class="pet-admin-span-2">
        <el-input v-model="profileForm.skills" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" />
      </el-form-item>
      <el-form-item label="服务意向" class="pet-admin-span-2">
        <el-input v-model="profileForm.serviceIntention" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" />
      </el-form-item>
      <el-form-item label="可服务时间" class="pet-admin-span-2">
        <el-input v-model="profileForm.availableTimeDesc" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" />
      </el-form-item>
      <el-form-item label="备注" class="pet-admin-span-2">
        <el-input v-model="profileForm.remark" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="profileDialogVisible = false">取消</el-button>
      <el-button type="warning" :loading="savingProfile" @click="saveProfile">保存</el-button>
    </template>
  </el-dialog>
</template>
