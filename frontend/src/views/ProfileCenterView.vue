<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Connection, Delete, Lock, Message, RefreshRight, Search, SwitchButton, Upload, User } from '@element-plus/icons-vue'
import { getLostPets, updateLostPet, updateLostPetStatus } from '../api/lost'
import { createPet, deletePetById, getPets, updatePetStatus, uploadPetMedia } from '../api/pets'
import { resolveCatalogValue, splitCatalogValue, useInformationCatalog } from '../composables/useInformationCatalog'
import { deleteUserAvatar, getUserById, getUsers, removeUserById, updateUserById, uploadUserAvatar } from '../api/user'
import { deleteRescueTask, getRescueTasks, updateRescueTask, updateRescueTaskStatus, getFirstVisitRegistrations } from '../api/services'
import { useUserStore } from '../stores/user'
import VolunteerManagementPanel from '../components/VolunteerManagementPanel.vue'
import MyArticleManagementPanel from '../components/MyArticleManagementPanel.vue'
import TableActionColumnHeader from '../components/TableActionColumnHeader.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const profileFormRef = ref()
const userFormRef = ref()
const petFormRef = ref()
const petEditFormRef = ref()
const lostPetEditFormRef = ref()
const lostPetReviewFormRef = ref()
const taskEditFormRef = ref()
const taskReviewFormRef = ref()
const loadingProfile = ref(false)
const loadingUsers = ref(false)
const loadingPets = ref(false)
const loadingLostPets = ref(false)
const loadingTasks = ref(false)
const loadingFirstReg = ref(false)
const userActionCollapsed = ref(false)
const petActionCollapsed = ref(false)
const lostPetActionCollapsed = ref(false)
const taskActionCollapsed = ref(false)
const submitting = ref(false)
const uploadingAvatar = ref(false)
const uploadingPetMedia = ref(false)
const savingUser = ref(false)
const savingPet = ref(false)
const savingLostPet = ref(false)
const savingTask = ref(false)
const avatarInputRef = ref()
const petMediaInputRef = ref()
const activeMenu = ref('profile')
const userDialogVisible = ref(false)
const petCreateDialogVisible = ref(false)
const petDialogVisible = ref(false)
const lostPetEditDialogVisible = ref(false)
const lostPetReviewDialogVisible = ref(false)
const taskEditDialogVisible = ref(false)
const taskReviewDialogVisible = ref(false)
const userKeyword = ref('')
const taskKeyword = ref('')
const userRows = ref([])
const petRows = ref([])
const lostPetRows = ref([])
const taskRows = ref([])
const userTotal = ref(0)
const petTotal = ref(0)
const lostPetTotal = ref(0)
const taskTotal = ref(0)
const firstRegRows = ref([])
const firstRegTotal = ref(0)
const firstRegKeyword = ref('')
const firstRegPage = reactive({ page: 1, size: 10 })
const userPage = reactive({
  page: 1,
  size: 10,
})
const petPage = reactive({
  page: 1,
  size: 10,
})
const lostPetPage = reactive({
  page: 1,
  size: 10,
})
const taskPage = reactive({
  page: 1,
  size: 10,
})

const petSearchForm = reactive({
  name: '',
  age0: undefined,
  age1: undefined,
  sex: '',
  type: '',
  breed: '',
  status: '',
  province: '',
  city: '',
  district: '',
  address: '',
})

const lostPetSearchForm = reactive({
  name: '',
  type: '',
  breed: '',
  status: '',
  province: '',
  city: '',
  address: '',
  lostDate: '',
})

const profileForm = reactive({
  username: '',
  email: '',
  avatar: '',
  phone: '',
  password: '',
  confirmPassword: '',
})

const userForm = reactive({
  id: '',
  username: '',
  email: '',
  phone: '',
  password: '',
  roles: [],
})

const petForm = reactive({
  id: '',
  name: '',
  age: 0,
  sex: '未知',
  type: '',
  typeInput: '猫',
  breed: '',
  breedInput: '',
  health: '',
  description: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
})

const petEditForm = reactive({
  id: '',
  name: '',
  age: 0,
  sex: '',
  type: '',
  typeInput: '',
  breed: '',
  breedInput: '',
  health: '',
  description: '',
  status: '',
  reason: '',
})

const taskEditForm = reactive({
  id: '',
  summary: '',
  description: '',
  type: 'FIND',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
})

const taskReviewForm = reactive({
  id: '',
  status: '',
  reason: '',
})

const lostPetEditForm = reactive({
  id: '',
  name: '',
  age: 0,
  sex: '',
  type: '',
  breed: '',
  features: '',
  lostTime: '',
  phone: '',
  description: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
})

const lostPetReviewForm = reactive({
  id: '',
  status: '',
  reason: '',
})

const {
  ensureInformationCatalog,
  ensureCityOptions,
  ensureDistrictOptions,
  provinceOptions,
  typeOptions,
  getCityOptions,
  getDistrictOptions,
  getBreedOptions,
} = useInformationCatalog()

const petFormCityOptions = computed(() => getCityOptions(petForm.province))
const petFormDistrictOptions = computed(() => getDistrictOptions(petForm.province, petForm.city))
const petFormBreedOptions = computed(() => getBreedOptions(resolveCatalogValue(petForm.type, petForm.typeInput)))
const petSearchCityOptions = computed(() => getCityOptions(petSearchForm.province))
const petSearchDistrictOptions = computed(() => getDistrictOptions(petSearchForm.province, petSearchForm.city))
const petSearchBreedOptions = computed(() => getBreedOptions(petSearchForm.type))
const lostPetSearchCityOptions = computed(() => getCityOptions(lostPetSearchForm.province))
const lostPetSearchBreedOptions = computed(() => getBreedOptions(lostPetSearchForm.type))
const lostPetEditCityOptions = computed(() => getCityOptions(lostPetEditForm.province))
const lostPetEditDistrictOptions = computed(() => getDistrictOptions(lostPetEditForm.province, lostPetEditForm.city))
const lostPetEditBreedOptions = computed(() => getBreedOptions(lostPetEditForm.type))
const taskEditCityOptions = computed(() => getCityOptions(taskEditForm.province))
const taskEditDistrictOptions = computed(() => getDistrictOptions(taskEditForm.province, taskEditForm.city))

const ROLE = {
  VOLUNTEER: 1,
  WORKER: 2,
  DONOR: 4,
  DOCTOR: 8,
  ADMIN: 16,
}

const MANAGED_ROLE_VALUES = [ROLE.VOLUNTEER, ROLE.WORKER, ROLE.DONOR, ROLE.DOCTOR, ROLE.ADMIN]

const profileId = computed(() => String(userStore.profile.id || ''))
const loginRole = computed(() => Number(userStore.profile.role || 0))
const isLoginAdmin = computed(() => (loginRole.value & ROLE.ADMIN) === ROLE.ADMIN)
const canManageUsers = computed(() => isLoginAdmin.value || hasRole(loginRole.value, ROLE.WORKER))
const canManageMedical = computed(() => canManageUsers.value || hasRole(loginRole.value, ROLE.DOCTOR))
const canManageArticles = computed(() => hasRole(loginRole.value, ROLE.WORKER) || hasRole(loginRole.value, ROLE.VOLUNTEER))

const roleOptions = [
  { label: '志愿者', value: ROLE.VOLUNTEER },
  { label: '工作人员', value: ROLE.WORKER },
  { label: '捐赠者', value: ROLE.DONOR },
  { label: '兽医', value: ROLE.DOCTOR },
  { label: '管理员', value: ROLE.ADMIN },
]

const petStatusOptions = [
  { label: '待审核', value: 'WAITING' },
  { label: '审核未通过', value: 'AGAINST' },
  { label: '查找中', value: 'FINDING' },
  { label: '已死亡/无法救助', value: 'DIED' },
  { label: '超时放弃', value: 'TIMEOUT' },
  { label: '已收容', value: 'SHELTERED' },
  { label: '可领养', value: 'HEALTH' },
  { label: '已领养', value: 'ADOPTED' },
  { label: '已回家', value: 'HOME' },
]

const rescueTaskTypeOptions = [
  { label: '发现流浪宠物', value: 'FIND' },
  { label: '医疗救助', value: 'MEDICAL' },
  { label: '其他协助', value: 'OTHER' },
]

const rescueTaskStatusOptions = [
  { label: '审核通过', value: 'APPROVED' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '任务完成', value: 'COMPLETED' },
  { label: '已废弃', value: 'DISCARDED' },
]

const lostPetStatusOptions = [
  { label: '寻找中', value: 'SEARCHING' },
  { label: '认领中', value: 'CLAIMING' },
  { label: '已找到', value: 'CLAIMED' },
  { label: '已关闭', value: 'CLOSED' },
]

const visibleUsers = computed(() => {
  const text = userKeyword.value.trim().toLowerCase()
  if (!text) {
    return userRows.value
  }
  return userRows.value.filter((item) =>
    [item.username, item.email, item.phone, roleText(item.role)]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
      .includes(text),
  )
})

const visiblePets = computed(() => petRows.value)
const visibleLostPets = computed(() => lostPetRows.value)

const visibleTasks = computed(() => {
  const text = taskKeyword.value.trim().toLowerCase()
  if (!text) {
    return taskRows.value
  }
  return taskRows.value.filter((task) =>
    [task.summary, task.description, rescueTaskTypeText(task.type), rescueTaskStatusText(task.status), task.location?.city, task.location?.detailAddress]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
      .includes(text),
  )
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] },
  ],
  password: [
    {
      validator: (_rule, value, callback) => {
        if (!value) {
          callback()
          return
        }
        if (String(value).length < 6) {
          callback(new Error('新密码长度至少 6 位'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    {
      validator: (_rule, value, callback) => {
        if (!profileForm.password) {
          callback()
          return
        }
        if (!value) {
          callback(new Error('请再次输入新密码'))
          return
        }
        if (value !== profileForm.password) {
          callback(new Error('两次输入的新密码不一致'))
          return
        }
        callback()
      },
      trigger: ['blur', 'change'],
    },
  ],
}

const userRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] },
  ],
  password: [
    {
      validator: (_rule, value, callback) => {
        if (!value || String(value).length >= 6) {
          callback()
          return
        }
        callback(new Error('新密码长度至少 6 位'))
      },
      trigger: 'blur',
    },
  ],
}

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

const taskEditRules = {
  summary: [{ required: true, message: '请输入任务简介', trigger: 'blur' }],
  description: [{ required: true, message: '请输入任务描述', trigger: 'blur' }],
  type: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  province: [{ required: true, message: '请选择省份', trigger: 'change' }],
  city: [{ required: true, message: '请选择城市', trigger: 'change' }],
  district: [{ required: true, message: '请选择区县', trigger: 'change' }],
  detailAddress: [{ required: true, message: '请输入详细位置', trigger: 'blur' }],
}

const taskReviewRules = {
  status: [{ required: true, message: '请选择审核状态', trigger: 'change' }],
  reason: [{ required: true, message: '请输入审核原因', trigger: 'blur' }],
}

function validateRequiredValue(value, message, callback) {
  if (String(value || '').trim()) {
    callback()
    return
  }
  callback(new Error(message))
}

function resolvePetType(form) {
  return resolveCatalogValue(form.type, form.typeInput)
}

function resolvePetBreed(form) {
  return resolveCatalogValue(form.breed, form.breedInput)
}

function applyCatalogValue(form, field, customField, value, options = []) {
  const { selected, custom } = splitCatalogValue(value, options)
  form[field] = selected
  form[customField] = custom
}

function fillForm(profileData) {
  profileForm.username = profileData?.username || ''
  profileForm.email = profileData?.email || ''
  profileForm.avatar = profileData?.avatar || ''
  profileForm.phone = profileData?.phone || ''
  profileForm.password = ''
  profileForm.confirmPassword = ''
}

async function loadProfile() {
  loadingProfile.value = true
  try {
    if (!profileId.value) {
      fillForm(userStore.profile)
      return
    }
    const result = await getUserById(profileId.value)
    const mergedProfile = {
      id: profileId.value,
      username: result?.username || userStore.profile.username || '',
      email: result?.email || userStore.profile.email || '',
      avatar: result?.avatar || userStore.profile.avatar || '',
      role: Number(result?.role ?? userStore.profile.role ?? 0),
      phone: result?.phone || userStore.profile.phone || '',
    }
    userStore.setProfile(mergedProfile)
    fillForm(mergedProfile)
  } catch (error) {
    fillForm(userStore.profile)
    const message = error?.message ? String(error.message) : '加载个人信息失败'
    ElMessage.warning(message)
  } finally {
    loadingProfile.value = false
  }
}

function buildUpdatePayload() {
  const payload = {
    username: profileForm.username.trim(),
    email: profileForm.email.trim(),
    phone: profileForm.phone.trim() || undefined,
  }
  if (profileForm.password) {
    payload.password = profileForm.password
  }
  return payload
}

function syncProfileAvatar(avatar) {
  const profile = {
    ...userStore.profile,
    avatar: avatar || '',
  }
  userStore.setProfile(profile)
  profileForm.avatar = profile.avatar
}

function chooseAvatarFile() {
  avatarInputRef.value?.click()
}

async function uploadAvatar(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) {
    return
  }
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  if (!profileId.value) {
    ElMessage.warning('当前账号缺少用户 ID，请重新登录后再试')
    return
  }

  uploadingAvatar.value = true
  try {
    const avatar = await uploadUserAvatar(profileId.value, file)
    syncProfileAvatar(avatar)
    ElMessage.success('头像已上传')
  } catch (error) {
    ElMessage.warning(error?.message || '头像上传失败')
  } finally {
    uploadingAvatar.value = false
  }
}

async function removeAvatar() {
  if (!profileId.value) {
    ElMessage.warning('当前账号缺少用户 ID，请重新登录后再试')
    return
  }
  uploadingAvatar.value = true
  try {
    await deleteUserAvatar(profileId.value)
    syncProfileAvatar('')
    ElMessage.success('头像已删除')
  } catch (error) {
    ElMessage.warning(error?.message || '删除头像失败')
  } finally {
    uploadingAvatar.value = false
  }
}

async function submitProfile() {
  if (!profileFormRef.value || submitting.value) {
    return
  }

  try {
    submitting.value = true
    await profileFormRef.value.validate()

    if (!profileId.value) {
      throw new Error('当前账号缺少用户 ID，请重新登录后再试')
    }

    const payload = buildUpdatePayload()
    const result = await updateUserById(profileId.value, payload)
    const mergedProfile = {
      id: profileId.value,
      username: result?.username || payload.username,
      email: result?.email || payload.email,
      avatar: result?.avatar || payload.avatar || '',
      role: Number(result?.role ?? userStore.profile.role ?? 0),
      phone: result?.phone || payload.phone || '',
    }
    userStore.setProfile(mergedProfile)
    fillForm(mergedProfile)
    ElMessage.success('个人信息已保存')
  } catch (error) {
    const message = error?.message ? String(error.message) : '保存失败，请稍后重试'
    ElMessage.warning(message)
  } finally {
    submitting.value = false
  }
}

function resetForm() {
  fillForm(userStore.profile)
}

async function handleLogout() {
  try {
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.replace('/login')
  } catch (error) {
    const message = error?.message ? String(error.message) : '退出登录失败'
    ElMessage.warning(message)
  }
}

function goHome() {
  router.push('/')
}

function hasRole(role, bit) {
  return (Number(role || 0) & bit) === bit
}

function roleText(role) {
  const labels = visibleRoleLabels(role)
  return labels.length ? labels.join('、') : '普通用户'
}

function visibleRoleLabels(role) {
  const values = expandRoleValues(role)
  if (values.includes(ROLE.ADMIN)) {
    return ['管理员']
  }
  const priority = [ROLE.WORKER, ROLE.DOCTOR, ROLE.VOLUNTEER, ROLE.DONOR]
  return priority
    .filter((value) => values.includes(value) && !(value === ROLE.VOLUNTEER && values.includes(ROLE.WORKER)))
    .map((value) => roleOptions.find((item) => item.value === value)?.label)
    .filter(Boolean)
}

function primaryRoleLabel(role) {
  return visibleRoleLabels(role)[0] || '普通用户'
}

function hasMoreRoles(role) {
  return visibleRoleLabels(role).length > 1
}

function expandRoleValues(role) {
  const value = Number(role || 0)
  if (hasRole(value, ROLE.ADMIN)) {
    return [...MANAGED_ROLE_VALUES]
  }
  const values = roleOptions.filter((item) => hasRole(value, item.value)).map((item) => item.value)
  if (values.includes(ROLE.WORKER) && !values.includes(ROLE.VOLUNTEER)) {
    values.unshift(ROLE.VOLUNTEER)
  }
  return values
}

function canEditUser(row) {
  return isLoginAdmin.value || !hasRole(row?.role, ROLE.ADMIN)
}

function normalizeRoleValues(values) {
  const selected = new Set(values.map(Number))
  if (selected.has(ROLE.ADMIN)) {
    return [...MANAGED_ROLE_VALUES]
  }
  if (selected.has(ROLE.WORKER)) {
    selected.add(ROLE.VOLUNTEER)
  }
  return MANAGED_ROLE_VALUES.filter((item) => selected.has(item))
}

function buildRoleValue(values) {
  if (values.includes(ROLE.ADMIN)) {
    return ROLE.ADMIN
  }
  return normalizeRoleValues(values).reduce((sum, item) => sum | Number(item), 0)
}

async function loadUsers() {
  if (!canManageUsers.value) {
    return
  }
  loadingUsers.value = true
  try {
    const result = await getUsers({
      page: userPage.page,
      size: userPage.size,
      sort: 'update_time',
      order: 'desc',
    })
    userRows.value = Array.isArray(result?.records) ? result.records : []
    userTotal.value = Number(result?.total || userRows.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载用户列表失败')
  } finally {
    loadingUsers.value = false
  }
}

function openUserDialog(row) {
  userForm.id = String(row.id || '')
  userForm.username = row.username || ''
  userForm.email = row.email || ''
  userForm.phone = row.phone || ''
  userForm.password = ''
  userForm.roles = expandRoleValues(row.role)
  userDialogVisible.value = true
}

function handleRoleChange(values) {
  userForm.roles = normalizeRoleValues(values)
}

async function saveUser() {
  if (!userFormRef.value || savingUser.value) {
    return
  }
  savingUser.value = true
  try {
    await userFormRef.value.validate()
    const payload = {
      username: userForm.username.trim(),
      email: userForm.email.trim(),
      phone: userForm.phone.trim() || undefined,
      role: buildRoleValue(userForm.roles),
    }
    if (userForm.password) {
      payload.password = userForm.password
    }
    await updateUserById(userForm.id, payload)
    ElMessage.success('用户信息已保存')
    userDialogVisible.value = false
    await loadUsers()
    if (userForm.id === profileId.value) {
      await loadProfile()
    }
  } catch (error) {
    ElMessage.warning(error?.message || '保存用户信息失败')
  } finally {
    savingUser.value = false
  }
}

async function deleteUser(row) {
  try {
    await ElMessageBox.confirm(`确认删除账号「${row.username}」？`, '删除用户', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await removeUserById(row.id)
    ElMessage.success('用户已删除')
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.warning(error?.message || '删除用户失败')
    }
  }
}

function changeUserPage(page) {
  userPage.page = page
  loadUsers()
}

function resetPetForm() {
  Object.assign(petForm, {
    id: '',
    name: '',
    age: 0,
    sex: '未知',
    type: '',
    typeInput: '',
    breed: '',
    breedInput: '',
    health: '',
    description: '',
    province: '',
    city: '',
    district: '',
    detailAddress: '',
  })
}

function openPetCreateDialog() {
  resetPetForm()
  applyCatalogValue(petForm, 'type', 'typeInput', '猫', typeOptions.value)
  petCreateDialogVisible.value = true
}

function handlePetFormTypeChange() {
  petForm.breed = ''
  petForm.breedInput = ''
}

function handlePetFormProvinceChange() {
  petForm.city = ''
  petForm.district = ''
  if (petForm.province) {
    ensureCityOptions(petForm.province)
  }
}

function handlePetFormCityChange() {
  petForm.district = ''
  if (petForm.province && petForm.city) {
    ensureDistrictOptions(petForm.province, petForm.city)
  }
}

function petStatusText(status) {
  return petStatusOptions.find((item) => item.value === status)?.label || status || '待补充'
}

function lostPetStatusText(status) {
  return lostPetStatusOptions.find((item) => item.value === status)?.label || status || '待补充'
}

function rescueTaskStatusText(status) {
  const map = {
    CREATED: '已创建',
    APPROVED: '审核通过',
    PROCESSING: '处理中',
    COMPLETED: '任务完成',
    DISCARDED: '已废弃',
  }
  return map[status] || status || '状态待补充'
}

function rescueTaskTypeText(type) {
  const map = {
    FIND: '发现流浪宠物',
    MEDICAL: '医疗救助',
    OTHER: '其他协助',
  }
  return map[type] || type || '类型待补充'
}

function petLocationText(pet) {
  const location = pet.locations?.[0]
  if (!location) {
    return '位置待补充'
  }
  return [location.province, location.city, location.district, location.detailAddress].filter(Boolean).join(' · ')
}

function lostPetLocationText(row) {
  const location = row.location
  if (!location) {
    return '位置待补充'
  }
  return [location.province, location.city, location.district, location.detailAddress].filter(Boolean).join(' · ')
}

function handleLostPetEditTypeChange() {
  lostPetEditForm.breed = ''
}

function handleLostPetEditProvinceChange() {
  lostPetEditForm.city = ''
  lostPetEditForm.district = ''
  if (lostPetEditForm.province) {
    ensureCityOptions(lostPetEditForm.province)
  }
}

function handleLostPetEditCityChange() {
  lostPetEditForm.district = ''
  if (lostPetEditForm.province && lostPetEditForm.city) {
    ensureDistrictOptions(lostPetEditForm.province, lostPetEditForm.city)
  }
}

function rescueTaskLocationText(task) {
  const location = task.location
  if (!location) {
    return '位置待补充'
  }
  return [location.province, location.city, location.district, location.detailAddress].filter(Boolean).join(' · ')
}

function handleTaskEditProvinceChange() {
  taskEditForm.city = ''
  taskEditForm.district = ''
  if (taskEditForm.province) {
    ensureCityOptions(taskEditForm.province)
  }
}

function handleTaskEditCityChange() {
  taskEditForm.district = ''
  if (taskEditForm.province && taskEditForm.city) {
    ensureDistrictOptions(taskEditForm.province, taskEditForm.city)
  }
}

async function submitPetInfo() {
  if (!petFormRef.value || savingPet.value) {
    return
  }
  savingPet.value = true
  try {
    await petFormRef.value.validate()
    const result = await createPet({
      name: petForm.name.trim(),
      age: Number(petForm.age || 0),
      sex: petForm.sex,
      type: resolvePetType(petForm),
      breed: resolvePetBreed(petForm),
      health: petForm.health.trim(),
      description: petForm.description.trim(),
      province: petForm.province.trim(),
      city: petForm.city.trim(),
      district: petForm.district.trim(),
      detailAddress: petForm.detailAddress.trim(),
    })
    petForm.id = String(result?.id || '')
    ElMessage.success('流浪宠物信息已提交，可继续上传图片/视频')
    await loadPets()
  } catch (error) {
    ElMessage.warning(error?.message || '提交宠物信息失败')
  } finally {
    savingPet.value = false
  }
}

function choosePetMedia() {
  if (!petForm.id) {
    ElMessage.warning('请先提交宠物基础信息')
    return
  }
  petMediaInputRef.value?.click()
}

async function uploadPetFiles(event) {
  const files = Array.from(event.target.files || [])
  event.target.value = ''
  if (!files.length || !petForm.id) {
    return
  }
  uploadingPetMedia.value = true
  try {
    for (let index = 0; index < files.length; index += 1) {
      await uploadPetMedia(petForm.id, {
        file: files[index],
        isCover: index === 0,
      })
    }
    ElMessage.success('宠物图片/视频已上传')
    await loadPets()
  } catch (error) {
    ElMessage.warning(error?.message || '上传媒体失败')
  } finally {
    uploadingPetMedia.value = false
  }
}

function handlePetSearchTypeChange() {
  petSearchForm.breed = ''
}

function handlePetSearchProvinceChange() {
  petSearchForm.city = ''
  petSearchForm.district = ''
  if (petSearchForm.province) {
    ensureCityOptions(petSearchForm.province)
  }
}

function handlePetSearchCityChange() {
  petSearchForm.district = ''
  if (petSearchForm.province && petSearchForm.city) {
    ensureDistrictOptions(petSearchForm.province, petSearchForm.city)
  }
}

function buildPetSearchQuery() {
  return {
    page: petPage.page,
    size: petPage.size,
    sort: 'update_time',
    order: 'desc',
    name: petSearchForm.name.trim() || undefined,
    age0: petSearchForm.age0 ?? undefined,
    age1: petSearchForm.age1 ?? undefined,
    sex: petSearchForm.sex || undefined,
    type: petSearchForm.type ? [petSearchForm.type] : undefined,
    breed: petSearchForm.breed ? [petSearchForm.breed] : undefined,
    status: petSearchForm.status ? [petSearchForm.status] : undefined,
    province: petSearchForm.province || undefined,
    city: petSearchForm.city || undefined,
    district: petSearchForm.district || undefined,
    address: petSearchForm.address.trim() || undefined,
  }
}

async function loadPets() {
  if (!canManageUsers.value) {
    return
  }
  loadingPets.value = true
  try {
    const result = await getPets(buildPetSearchQuery())
    petRows.value = Array.isArray(result?.records) ? result.records : []
    petTotal.value = Number(result?.total || petRows.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载宠物列表失败')
  } finally {
    loadingPets.value = false
  }
}

function openPetEditor(row) {
  router.push(`/pets/${row.id}/edit`)
}

function openPetDialog(row) {
  Object.assign(petEditForm, {
    id: String(row.id || ''),
    name: row.name || '',
    age: Number(row.age || 0),
    sex: row.sex || '',
    type: '',
    typeInput: '',
    breed: '',
    breedInput: '',
    health: row.health || '',
    description: row.description || '',
    status: row.status || 'WAITING',
    reason: '',
  })
  petDialogVisible.value = true
}

async function savePetInfo() {
  if (!petEditFormRef.value || savingPet.value) {
    return
  }
  savingPet.value = true
  try {
    await petEditFormRef.value.validate()
    await updatePetStatus(petEditForm.id, {
      status: petEditForm.status,
      reason: petEditForm.reason.trim(),
    })
    ElMessage.success('宠物审核状态已保存')
    petDialogVisible.value = false
    await loadPets()
  } catch (error) {
    ElMessage.warning(error?.message || '保存审核状态失败')
  } finally {
    savingPet.value = false
  }
}

async function removePet(row) {
  try {
    await ElMessageBox.confirm(`确认删除「${row.name || row.id}」的宠物档案？`, '删除宠物档案', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deletePetById(row.id)
    ElMessage.success('宠物档案已删除')
    await loadPets()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.warning(error?.message || '删除宠物档案失败')
    }
  }
}

function changePetPage(page) {
  petPage.page = page
  loadPets()
}

function searchPets() {
  petPage.page = 1
  loadPets()
}

function handleLostPetSearchTypeChange() {
  lostPetSearchForm.breed = ''
}

function handleLostPetSearchProvinceChange() {
  lostPetSearchForm.city = ''
  if (lostPetSearchForm.province) {
    ensureCityOptions(lostPetSearchForm.province)
  }
}

function buildLostPetSearchQuery() {
  return {
    page: lostPetPage.page,
    size: lostPetPage.size,
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

async function loadLostPetRows() {
  if (!canManageUsers.value) {
    return
  }
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

function searchLostPets() {
  lostPetPage.page = 1
  loadLostPetRows()
}

function changeLostPetPage(page) {
  lostPetPage.page = page
  loadLostPetRows()
}

function openLostPetEditDialog(row) {
  Object.assign(lostPetEditForm, {
    id: String(row.id || ''),
    name: row.name || '',
    age: Number(row.age || 0),
    sex: row.sex || '未知',
    type: row.type || '',
    breed: row.breed || '',
    features: row.features || '',
    lostTime: row.lostTime ? String(row.lostTime).slice(0, 10) : '',
    phone: row.contactPhone || '',
    description: row.description || '',
    province: row.location?.province || '',
    city: row.location?.city || '',
    district: row.location?.district || '',
    detailAddress: row.location?.detailAddress || '',
  })
  if (lostPetEditForm.province) {
    ensureCityOptions(lostPetEditForm.province)
  }
  if (lostPetEditForm.province && lostPetEditForm.city) {
    ensureDistrictOptions(lostPetEditForm.province, lostPetEditForm.city)
  }
  lostPetEditDialogVisible.value = true
}

function openLostPetReviewDialog(row) {
  Object.assign(lostPetReviewForm, {
    id: String(row.id || ''),
    status: row.status || 'SEARCHING',
    reason: '',
  })
  lostPetReviewDialogVisible.value = true
}

async function saveLostPetEdit() {
  if (!lostPetEditFormRef.value || savingLostPet.value) {
    return
  }
  savingLostPet.value = true
  try {
    await lostPetEditFormRef.value.validate()
    await updateLostPet(lostPetEditForm.id, {
      name: lostPetEditForm.name.trim(),
      age: Number(lostPetEditForm.age || 0),
      sex: lostPetEditForm.sex,
      type: lostPetEditForm.type.trim(),
      breed: lostPetEditForm.breed.trim(),
      features: lostPetEditForm.features.trim(),
      lostTime: `${lostPetEditForm.lostTime}T00:00:00`,
      phone: lostPetEditForm.phone.trim(),
      description: lostPetEditForm.description.trim(),
      province: lostPetEditForm.province,
      city: lostPetEditForm.city,
      district: lostPetEditForm.district,
      detailAddress: lostPetEditForm.detailAddress.trim(),
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
  if (!lostPetReviewFormRef.value || savingLostPet.value) {
    return
  }
  savingLostPet.value = true
  try {
    await lostPetReviewFormRef.value.validate()
    await updateLostPetStatus(lostPetReviewForm.id, {
      status: lostPetReviewForm.status,
      reason: lostPetReviewForm.reason.trim(),
    })
    ElMessage.success('丢失宠物审核状态已保存')
    lostPetReviewDialogVisible.value = false
    await loadLostPetRows()
  } catch (error) {
    ElMessage.warning(error?.message || '保存审核状态失败')
  } finally {
    savingLostPet.value = false
  }
}

async function loadTasks() {
  if (!canManageUsers.value) {
    return
  }
  loadingTasks.value = true
  try {
    const result = await getRescueTasks({
      page: taskPage.page,
      size: taskPage.size,
      sort: 'update_time',
      order: 'desc',
    })
    taskRows.value = Array.isArray(result?.records) ? result.records : []
    taskTotal.value = Number(result?.total || taskRows.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载救助任务失败')
  } finally {
    loadingTasks.value = false
  }
}

async function openTaskEditDialog(row) {
  const location = row.location || {}
  Object.assign(taskEditForm, {
    id: String(row.id || ''),
    summary: row.summary || '',
    description: row.description || '',
    type: row.type || 'FIND',
    province: location.province || '',
    city: location.city || '',
    district: location.district || '',
    detailAddress: location.detailAddress || '',
  })
  if (taskEditForm.province) {
    await ensureCityOptions(taskEditForm.province)
  }
  if (taskEditForm.province && taskEditForm.city) {
    await ensureDistrictOptions(taskEditForm.province, taskEditForm.city)
  }
  taskEditDialogVisible.value = true
}

function openTaskReviewDialog(row) {
  Object.assign(taskReviewForm, {
    id: String(row.id || ''),
    status: row.status === 'CREATED' ? 'APPROVED' : row.status || 'APPROVED',
    reason: '',
  })
  taskReviewDialogVisible.value = true
}

async function saveTaskInfo() {
  if (!taskEditFormRef.value || savingTask.value) {
    return
  }
  savingTask.value = true
  try {
    await taskEditFormRef.value.validate()
    await updateRescueTask(taskEditForm.id, {
      summary: taskEditForm.summary.trim(),
      description: taskEditForm.description.trim(),
      type: taskEditForm.type,
      province: taskEditForm.province,
      city: taskEditForm.city,
      district: taskEditForm.district,
      detailAddress: taskEditForm.detailAddress.trim(),
    })
    ElMessage.success('救助任务已保存')
    taskEditDialogVisible.value = false
    await loadTasks()
  } catch (error) {
    ElMessage.warning(error?.message || '保存救助任务失败')
  } finally {
    savingTask.value = false
  }
}

async function saveTaskReview() {
  if (!taskReviewFormRef.value || savingTask.value) {
    return
  }
  savingTask.value = true
  try {
    await taskReviewFormRef.value.validate()
    await updateRescueTaskStatus(taskReviewForm.id, {
      status: taskReviewForm.status,
      reason: taskReviewForm.reason.trim(),
    })
    ElMessage.success('救助任务审核已保存')
    taskReviewDialogVisible.value = false
    await loadTasks()
  } catch (error) {
    ElMessage.warning(error?.message || '保存审核状态失败')
  } finally {
    savingTask.value = false
  }
}

async function removeTask(row) {
  try {
    await ElMessageBox.confirm(`确认删除「${row.summary || row.id}」救助任务？`, '删除救助任务', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteRescueTask(row.id)
    ElMessage.success('救助任务已删除')
    await loadTasks()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.warning(error?.message || '删除救助任务失败')
    }
  }
}

function changeTaskPage(page) {
  taskPage.page = page
  loadTasks()
}

async function loadFirstRegistrations() {
  loadingFirstReg.value = true
  try {
    const query = { page: firstRegPage.page, size: firstRegPage.size }
    if (firstRegKeyword.value.trim()) query.name = firstRegKeyword.value.trim()
    const result = await getFirstVisitRegistrations(query)
    firstRegRows.value = Array.isArray(result?.records) ? result.records : []
    firstRegTotal.value = Number(result?.total || firstRegRows.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载初诊登记失败')
  } finally {
    loadingFirstReg.value = false
  }
}

function changeFirstRegPage(page) {
  firstRegPage.page = page
  loadFirstRegistrations()
}

function goCreateFirstReg(row) {
  router.push(`/medical/first-registration/new?petId=${row.id}`)
}

function handleMenuSelect(index) {
  activeMenu.value = index
  if (index === 'users') {
    loadUsers()
  } else if (index === 'pets') {
    loadPets()
  } else if (index === 'lost-pets') {
    loadLostPetRows()
  } else if (index === 'tasks') {
    loadTasks()
  } else if (index === 'medical-first') {
    loadFirstRegistrations()
  } else if (index === 'article' || index === 'article-mine' || index === 'article-manage') {
    if (index === 'article') {
      activeMenu.value = canManageUsers.value ? 'article-manage' : 'article-mine'
    }
  } else if (index === 'volunteer') {
    activeMenu.value = isLoginAdmin.value || hasRole(loginRole.value, ROLE.WORKER) ? 'volunteer-recruitments' : 'volunteer-applications'
  } else if (index === 'api-coverage') {
    router.push('/api-coverage')
  }
}

onMounted(async () => {
  await ensureInformationCatalog()
  loadProfile()
  if (route.query.tab === 'medical-first') {
    activeMenu.value = 'medical-first'
    loadFirstRegistrations()
  } else if (route.query.tab === 'articles') {
    activeMenu.value = canManageUsers.value ? 'article-manage' : 'article-mine'
  } else if (route.query.tab === 'article-mine') {
    activeMenu.value = 'article-mine'
  } else if (route.query.tab === 'article-manage') {
    activeMenu.value = 'article-manage'
  } else if (route.query.tab === 'lost-pets') {
    activeMenu.value = 'lost-pets'
    loadLostPetRows()
  } else if (route.query.tab === 'volunteer') {
    activeMenu.value = isLoginAdmin.value || hasRole(loginRole.value, ROLE.WORKER) ? 'volunteer-recruitments' : 'volunteer-applications'
  }
})

watch(
  () => petForm.province,
  async (province) => {
    if (province) {
      await ensureCityOptions(province)
    }
  },
)

watch(
  () => [petForm.province, petForm.city],
  async ([province, city]) => {
    if (province && city) {
      await ensureDistrictOptions(province, city)
    }
  },
)
</script>

<template>
  <div class="console-page">
    <header class="console-topbar">
      <div>
        <h1>后台管理</h1>
      </div>
      <div class="console-topbar-actions">
        <el-button text type="warning" @click="goHome">
          <el-icon><ArrowLeft /></el-icon>
          返回首页
        </el-button>
        <el-button text type="danger" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-button>
      </div>
    </header>

    <div class="console-layout">
      <aside class="console-sidebar">
        <el-menu class="console-menu" :default-active="activeMenu" @select="handleMenuSelect">
          <el-menu-item index="profile">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item v-if="canManageUsers" index="users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item v-if="canManageUsers" index="pets">
            <el-icon><Connection /></el-icon>
            <span>流浪宠物</span>
          </el-menu-item>
          <el-menu-item v-if="canManageUsers" index="lost-pets">
            <el-icon><Connection /></el-icon>
            <span>丢失宠物</span>
          </el-menu-item>
            <el-menu-item v-if="canManageUsers" index="tasks">
              <el-icon><Connection /></el-icon>
              <span>救助任务</span>
            </el-menu-item>
            <el-sub-menu v-if="canManageArticles" index="article">
              <template #title>
                <el-icon><Message /></el-icon>
                <span>公益文章</span>
              </template>
              <el-menu-item index="article-mine">我的文章</el-menu-item>
              <el-menu-item v-if="canManageUsers" index="article-manage">文章管理</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="volunteer">
              <template #title>
                <el-icon><Connection /></el-icon>
                <span>志愿者</span>
              </template>
            <el-menu-item v-if="canManageUsers" index="volunteer-recruitments">招募计划</el-menu-item>
            <el-menu-item index="volunteer-applications">招募申请</el-menu-item>
            <el-menu-item v-if="canManageUsers || hasRole(loginRole, ROLE.VOLUNTEER)" index="volunteer-rewards">志愿者激励</el-menu-item>
            <el-menu-item v-if="canManageUsers || hasRole(loginRole, ROLE.VOLUNTEER)" index="volunteer-activities">志愿活动</el-menu-item>
          </el-sub-menu>
          <el-sub-menu v-if="canManageMedical" index="medical">
            <template #title>
              <el-icon><Connection /></el-icon>
              <span>医疗护理</span>
            </template>
            <el-menu-item index="medical-first">初诊登记</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="api-coverage">
            <el-icon><Connection /></el-icon>
            <span>接口覆盖台</span>
          </el-menu-item>
        </el-menu>
      </aside>

      <section class="console-content">
        <el-card v-if="activeMenu === 'profile'" class="profile-card" v-loading="loadingProfile">
          <template #header>
            <div class="profile-card-header">
              <strong>个人信息修改</strong>
              <span>修改后将同步到你的登录资料</span>
            </div>
          </template>

          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="formRules"
            label-position="top"
            class="profile-form"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="profileForm.username" placeholder="请输入用户名">
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱">
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="头像图片" prop="avatar">
              <div class="profile-avatar-field">
                <div class="profile-avatar-preview">
                  <img v-if="profileForm.avatar" :src="profileForm.avatar" alt="用户头像" />
                  <el-icon v-else><User /></el-icon>
                </div>
                <div class="profile-avatar-actions">
                  <input ref="avatarInputRef" class="profile-avatar-input" type="file" accept="image/*" @change="uploadAvatar" />
                  <el-button :icon="Upload" :loading="uploadingAvatar" @click="chooseAvatarFile">上传图片</el-button>
                  <el-button :icon="Delete" :disabled="!profileForm.avatar || uploadingAvatar" plain @click="removeAvatar">删除头像</el-button>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="联系方式" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号或联系电话" />
            </el-form-item>

            <el-form-item label="新密码" prop="password">
              <el-input v-model="profileForm.password" placeholder="留空表示不修改密码" show-password>
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="profileForm.confirmPassword" placeholder="请再次输入新密码" show-password>
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <div class="profile-actions">
              <el-button @click="resetForm">重置</el-button>
              <el-button type="warning" :loading="submitting" @click="submitProfile">保存修改</el-button>
            </div>
          </el-form>
        </el-card>

        <el-card v-else-if="activeMenu === 'users'" class="profile-card user-admin-card">
          <template #header>
            <div class="profile-card-header">
              <strong>用户账号管理</strong>
              <span>工作人员可维护用户资料和角色</span>
            </div>
          </template>

          <div class="user-admin-toolbar">
            <el-input v-model="userKeyword" clearable placeholder="按用户名、邮箱、联系方式或角色搜索" />
            <el-button :loading="loadingUsers" @click="loadUsers">刷新</el-button>
          </div>

          <el-table :data="visibleUsers" v-loading="loadingUsers" class="user-admin-table">
            <el-table-column prop="username" label="用户名" min-width="130" />
            <el-table-column prop="email" label="邮箱" min-width="190" />
            <el-table-column prop="phone" label="联系方式" min-width="130">
              <template #default="{ row }">
                {{ row.phone || '未填写' }}
              </template>
            </el-table-column>
            <el-table-column label="角色" min-width="150">
              <template #default="{ row }">
                <el-tooltip :content="roleText(row.role)" placement="top" :disabled="!hasMoreRoles(row.role)">
                  <span class="user-role-cell">
                    <el-tag class="user-role-tag" type="warning" effect="plain">
                      {{ primaryRoleLabel(row.role) }}
                    </el-tag>
                    <span v-if="hasMoreRoles(row.role)" class="user-role-more">...</span>
                  </span>
                </el-tooltip>
              </template>
            </el-table-column>
              <el-table-column width="170" fixed="right">
                <template #header>
                  <TableActionColumnHeader title="操作" :collapsed="userActionCollapsed" @toggle="userActionCollapsed = !userActionCollapsed" />
                </template>
                <template #default="{ row }">
                  <div class="table-action-cell">
                    <div class="table-action-panel" :class="{ 'is-collapsed': userActionCollapsed }">
                      <el-button text type="warning" :disabled="!canEditUser(row)" @click="openUserDialog(row)">编辑</el-button>
                      <el-button
                        text
                        type="danger"
                        :disabled="String(row.id) === profileId || !canEditUser(row)"
                        @click="deleteUser(row)"
                      >
                        删除
                      </el-button>
                    </div>
                  </div>
                </template>
              </el-table-column>
          </el-table>

          <div class="user-admin-pagination">
            <el-pagination
              layout="prev, pager, next, total"
              :current-page="userPage.page"
              :page-size="userPage.size"
              :total="userTotal"
              @current-change="changeUserPage"
            />
          </div>
        </el-card>

        <el-card v-else-if="activeMenu === 'pets'" class="profile-card pet-admin-card">
          <template #header>
            <div class="profile-card-header">
              <strong>流浪宠物</strong>
              <span>按后端宠物查询接口筛选在库档案</span>
            </div>
          </template>

          <section class="pet-admin-section">
            <el-form label-position="top" class="console-filter-form">
              <div class="console-filter-grid console-filter-grid-5">
                <el-form-item label="名称">
                  <el-input v-model="petSearchForm.name" clearable placeholder="宠物名称关键词" />
                </el-form-item>
                <el-form-item label="类型">
                  <el-select v-model="petSearchForm.type" clearable filterable placeholder="宠物类型" @change="handlePetSearchTypeChange">
                    <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </el-form-item>
                <el-form-item label="品种">
                  <el-select v-model="petSearchForm.breed" clearable filterable placeholder="宠物品种" :disabled="!petSearchForm.type">
                    <el-option v-for="item in petSearchBreedOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </el-form-item>
                <el-form-item label="性别">
                  <el-select v-model="petSearchForm.sex" clearable placeholder="宠物性别">
                    <el-option label="未知" value="未知" />
                    <el-option label="公" value="公" />
                    <el-option label="母" value="母" />
                  </el-select>
                </el-form-item>
                <el-form-item label="状态">
                  <el-select v-model="petSearchForm.status" clearable placeholder="宠物状态">
                    <el-option v-for="item in petStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
              </div>

              <div class="console-filter-grid console-filter-grid-5">
                <el-form-item label="最小年龄（月）">
                  <el-input-number v-model="petSearchForm.age0" :min="0" :controls="false" class="full-width-control" />
                </el-form-item>
                <el-form-item label="最大年龄（月）">
                  <el-input-number v-model="petSearchForm.age1" :min="0" :controls="false" class="full-width-control" />
                </el-form-item>
                <el-form-item label="省份">
                  <el-select v-model="petSearchForm.province" clearable filterable placeholder="省份" @change="handlePetSearchProvinceChange">
                    <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </el-form-item>
                <el-form-item label="城市">
                  <el-select v-model="petSearchForm.city" clearable filterable placeholder="城市" :disabled="!petSearchForm.province" @change="handlePetSearchCityChange">
                    <el-option v-for="item in petSearchCityOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </el-form-item>
                <el-form-item label="区县">
                  <el-select v-model="petSearchForm.district" clearable filterable placeholder="区县" :disabled="!petSearchForm.city">
                    <el-option v-for="item in petSearchDistrictOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </el-form-item>
              </div>

              <div class="console-filter-grid console-filter-grid-actions">
                <el-form-item label="详细地点" class="console-filter-span-2">
                  <el-input v-model="petSearchForm.address" clearable placeholder="例如：地铁口、东门、桥下" />
                </el-form-item>
                <div class="console-filter-actions">
                  <el-button class="warm-btn" :icon="Search" :loading="loadingPets" @click="searchPets">搜索</el-button>
                </div>
              </div>
            </el-form>
            <el-table :data="visiblePets" v-loading="loadingPets" class="user-admin-table">
              <el-table-column label="宠物" min-width="180">
                <template #default="{ row }">
                  <div class="pet-admin-pet">
                    <img :src="row.cover || 'https://images.pexels.com/photos/1170986/pexels-photo-1170986.jpeg?auto=compress&cs=tinysrgb&w=320'" :alt="row.name" />
                    <div>
                      <strong>{{ row.name || '未命名' }}</strong>
                      <span>{{ row.type || '宠物' }} · {{ row.sex || '未知' }} · {{ row.age ?? 0 }} 月</span>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="120">
                <template #default="{ row }">
                  <el-tag type="warning" effect="plain">{{ petStatusText(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="位置" min-width="220">
                <template #default="{ row }">{{ petLocationText(row) }}</template>
              </el-table-column>
              <el-table-column prop="health" label="健康" min-width="140" />
                <el-table-column width="240" fixed="right">
                  <template #header>
                    <TableActionColumnHeader title="操作" :collapsed="petActionCollapsed" @toggle="petActionCollapsed = !petActionCollapsed" />
                  </template>
                  <template #default="{ row }">
                    <div class="table-action-cell">
                      <div class="table-action-panel" :class="{ 'is-collapsed': petActionCollapsed }">
                        <el-button text type="primary" @click="openPetDialog(row)">审核</el-button>
                        <el-button text type="warning" @click="openPetEditor(row)">编辑</el-button>
                        <el-button text type="danger" @click="removePet(row)">删除</el-button>
                        <el-button text type="success" @click="goCreateFirstReg(row)">初诊</el-button>
                      </div>
                    </div>
                  </template>
                </el-table-column>
            </el-table>
            <div class="user-admin-pagination">
              <el-pagination
                layout="prev, pager, next, total"
                :current-page="petPage.page"
                :page-size="petPage.size"
                :total="petTotal"
                @current-change="changePetPage"
              />
            </div>
          </section>
        </el-card>

        <el-card v-else-if="activeMenu === 'lost-pets'" class="profile-card pet-admin-card">
          <template #header>
            <div class="profile-card-header">
              <strong>丢失宠物</strong>
              <span>按走失宠物报备接口筛选登记记录</span>
            </div>
          </template>

          <section class="pet-admin-section">
            <el-form label-position="top" class="console-filter-form">
              <div class="console-filter-grid console-filter-grid-5">
                <el-form-item label="名称">
                  <el-input v-model="lostPetSearchForm.name" clearable placeholder="宠物名称关键词" />
                </el-form-item>
                <el-form-item label="类型">
                  <el-select v-model="lostPetSearchForm.type" clearable filterable placeholder="宠物类型" @change="handleLostPetSearchTypeChange">
                    <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </el-form-item>
                <el-form-item label="品种">
                  <el-select v-model="lostPetSearchForm.breed" clearable filterable placeholder="宠物品种" :disabled="!lostPetSearchForm.type">
                    <el-option v-for="item in lostPetSearchBreedOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </el-form-item>
                <el-form-item label="状态">
                  <el-select v-model="lostPetSearchForm.status" clearable placeholder="报备状态">
                    <el-option v-for="item in lostPetStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
                <el-form-item label="省份">
                  <el-select v-model="lostPetSearchForm.province" clearable filterable placeholder="省份" @change="handleLostPetSearchProvinceChange">
                    <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </el-form-item>
              </div>

              <div class="console-filter-grid console-filter-grid-4">
                <el-form-item label="城市">
                  <el-select v-model="lostPetSearchForm.city" clearable filterable placeholder="城市" :disabled="!lostPetSearchForm.province">
                    <el-option v-for="item in lostPetSearchCityOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </el-form-item>
                <el-form-item label="走失日期">
                  <el-date-picker
                    v-model="lostPetSearchForm.lostDate"
                    type="date"
                    value-format="YYYY-MM-DD"
                    placeholder="选择日期"
                  />
                </el-form-item>
                <el-form-item label="详细地点">
                  <el-input v-model="lostPetSearchForm.address" clearable placeholder="例如：公园西门、商场停车场" />
                </el-form-item>
              </div>

              <div class="console-filter-actions">
                <el-button class="warm-btn" :icon="Search" :loading="loadingLostPets" @click="searchLostPets">搜索</el-button>
              </div>
            </el-form>

            <el-table :data="visibleLostPets" v-loading="loadingLostPets" class="user-admin-table">
              <el-table-column label="宠物" min-width="220">
                <template #default="{ row }">
                  <div class="pet-admin-pet">
                    <img :src="row.petCover || 'https://images.pexels.com/photos/58997/pexels-photo-58997.jpeg?auto=compress&cs=tinysrgb&w=320'" :alt="row.name" />
                    <div>
                      <strong>{{ row.name || '未命名' }}</strong>
                      <span>{{ row.type || '宠物' }} · {{ row.breed || '品种待补充' }} · {{ row.sex || '未知' }}</span>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="120">
                <template #default="{ row }">
                  <el-tag type="warning" effect="plain">{{ lostPetStatusText(row.status) }}</el-tag>
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
                <el-table-column width="200" fixed="right">
                  <template #header>
                    <TableActionColumnHeader title="操作" :collapsed="lostPetActionCollapsed" @toggle="lostPetActionCollapsed = !lostPetActionCollapsed" />
                  </template>
                  <template #default="{ row }">
                    <div class="table-action-cell">
                      <div class="table-action-panel" :class="{ 'is-collapsed': lostPetActionCollapsed }">
                        <el-button text type="primary" @click="openLostPetReviewDialog(row)">审核</el-button>
                        <el-button text type="warning" @click="openLostPetEditDialog(row)">编辑</el-button>
                      </div>
                    </div>
                  </template>
                </el-table-column>
            </el-table>
            <div class="user-admin-pagination">
              <el-pagination
                layout="prev, pager, next, total"
                :current-page="lostPetPage.page"
                :page-size="lostPetPage.size"
                :total="lostPetTotal"
                @current-change="changeLostPetPage"
              />
            </div>
          </section>
        </el-card>

        <el-card v-else-if="activeMenu === 'tasks'" class="profile-card pet-admin-card">
          <template #header>
            <div class="profile-card-header">
              <strong>救助任务</strong>
              <span>查看任务详情、现场图片和处理进度</span>
            </div>
          </template>

          <section class="pet-admin-section">
            <div class="user-admin-toolbar">
              <el-input v-model="taskKeyword" clearable placeholder="按标题、类型、位置或状态搜索" />
              <el-button :icon="RefreshRight" :loading="loadingTasks" @click="loadTasks">刷新</el-button>
            </div>
            <el-table :data="visibleTasks" v-loading="loadingTasks" class="user-admin-table">
              <el-table-column label="任务" min-width="220">
                <template #default="{ row }">
                  <div class="rescue-task-admin-title">
                    <strong>{{ row.summary || '未命名任务' }}</strong>
                    <span>{{ rescueTaskTypeText(row.type) }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="120">
                <template #default="{ row }">
                  <el-tag type="warning" effect="plain">{{ rescueTaskStatusText(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="位置" min-width="240">
                <template #default="{ row }">{{ rescueTaskLocationText(row) }}</template>
              </el-table-column>
              <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
                <el-table-column width="210" fixed="right">
                  <template #header>
                    <TableActionColumnHeader title="操作" :collapsed="taskActionCollapsed" @toggle="taskActionCollapsed = !taskActionCollapsed" />
                  </template>
                  <template #default="{ row }">
                    <div class="table-action-cell">
                      <div class="table-action-panel" :class="{ 'is-collapsed': taskActionCollapsed }">
                        <el-button text type="warning" @click="openTaskEditDialog(row)">编辑</el-button>
                        <el-button text type="primary" @click="openTaskReviewDialog(row)">审核</el-button>
                        <el-button text type="danger" @click="removeTask(row)">删除</el-button>
                      </div>
                    </div>
                  </template>
                </el-table-column>
            </el-table>
            <div class="user-admin-pagination">
              <el-pagination
                layout="prev, pager, next, total"
                :current-page="taskPage.page"
                :page-size="taskPage.size"
                :total="taskTotal"
                @current-change="changeTaskPage"
              />
            </div>
          </section>
        </el-card>

        <!-- 初诊登记列表 -->
        <el-card v-else-if="activeMenu === 'medical-first'" class="profile-card pet-admin-card">
          <template #header>
            <div class="profile-card-header">
              <strong>初诊登记</strong>
              <span>记录流浪宠物收容时的初次检查信息</span>
            </div>
          </template>
          <section class="pet-admin-section">
            <div class="user-admin-toolbar">
              <el-input
                v-model="firstRegKeyword"
                clearable
                placeholder="按宠物名称搜索"
                @keyup.enter="loadFirstRegistrations"
              />
              <el-button :icon="RefreshRight" :loading="loadingFirstReg" @click="loadFirstRegistrations">刷新</el-button>
            </div>
            <el-table :data="firstRegRows" v-loading="loadingFirstReg" class="user-admin-table">
              <el-table-column label="宠物名称" prop="name" min-width="120" />
              <el-table-column label="年龄" width="80">
                <template #default="{ row }">{{ row.age }} 月</template>
              </el-table-column>
              <el-table-column label="类型" width="80">
                <template #default="{ row }">{{ row.type || '—' }}</template>
              </el-table-column>
              <el-table-column label="性别" width="70">
                <template #default="{ row }">{{ row.sex || '—' }}</template>
              </el-table-column>
              <el-table-column label="接诊人" min-width="120">
                <template #default="{ row }">{{ row.username || '—' }}</template>
              </el-table-column>
              <el-table-column label="登记时间" min-width="160">
                <template #default="{ row }">
                  {{ row.createTime ? new Date(row.createTime).toLocaleString('zh-CN') : '—' }}
                </template>
              </el-table-column>
            </el-table>
            <div class="user-admin-pagination">
              <el-pagination
                layout="prev, pager, next, total"
                :current-page="firstRegPage.page"
                :page-size="firstRegPage.size"
                :total="firstRegTotal"
                @current-change="changeFirstRegPage"
              />
            </div>
          </section>
        </el-card>

        <VolunteerManagementPanel
          v-else-if="activeMenu.startsWith('volunteer-')"
          :section="activeMenu.replace('volunteer-', '')"
          hide-tabs
        />

        <MyArticleManagementPanel v-else-if="activeMenu === 'article-mine'" mode="mine" />

        <MyArticleManagementPanel v-else-if="activeMenu === 'article-manage'" mode="manage" />
      </section>
    </div>

    <el-dialog v-model="userDialogVisible" title="编辑用户账号" width="560px">
      <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="联系方式" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入联系方式" />
        </el-form-item>
        <el-form-item label="重置密码" prop="password">
          <el-input v-model="userForm.password" placeholder="留空表示不修改密码" show-password />
        </el-form-item>
        <el-form-item label="角色">
          <el-checkbox-group v-model="userForm.roles" @change="handleRoleChange">
            <el-checkbox
              v-for="item in roleOptions"
              :key="item.value"
              :label="item.value"
              :disabled="(item.value === ROLE.ADMIN && !isLoginAdmin) || (item.value !== ROLE.ADMIN && userForm.roles.includes(ROLE.ADMIN)) || (item.value === ROLE.VOLUNTEER && userForm.roles.includes(ROLE.WORKER))"
            >
              {{ item.label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="savingUser" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="petCreateDialogVisible" title="记录流浪宠物" width="760px">
      <el-form ref="petFormRef" :model="petForm" :rules="petRules" label-position="top" class="pet-admin-form">
        <el-form-item label="宠物名称">
          <el-input v-model="petForm.name" placeholder="可留空，由救助站后续命名" />
        </el-form-item>
        <el-form-item label="年龄（月）">
          <el-input-number v-model="petForm.age" :min="0" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-select v-model="petForm.sex">
            <el-option label="未知" value="未知" />
            <el-option label="公" value="公" />
            <el-option label="母" value="母" />
          </el-select>
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
        <el-form-item label="健康状况">
          <el-input v-model="petForm.health" placeholder="未体检、恢复中、已体检等" />
        </el-form-item>
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
        <el-form-item label="详细位置" prop="detailAddress">
          <el-input v-model="petForm.detailAddress" />
        </el-form-item>
        <el-form-item label="情况描述" class="pet-admin-span-2">
          <el-input v-model="petForm.description" type="textarea" :autosize="{ minRows: 3, maxRows: 5 }" />
        </el-form-item>
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
          <el-input
            v-model="petEditForm.reason"
            type="textarea"
            :autosize="{ minRows: 4, maxRows: 7 }"
            placeholder="例如：资料审核通过，已完成收容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="petDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="savingPet" @click="savePetInfo">保存审核</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="lostPetEditDialogVisible" title="编辑丢失宠物" width="760px">
      <el-form ref="lostPetEditFormRef" :model="lostPetEditForm" :rules="lostPetEditRules" label-position="top" class="pet-admin-form">
        <el-form-item label="宠物名称" prop="name">
          <el-input v-model="lostPetEditForm.name" placeholder="请输入宠物名称" />
        </el-form-item>
        <el-form-item label="年龄（月）" prop="age">
          <el-input-number v-model="lostPetEditForm.age" :min="0" :controls="false" class="full-width-control" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-select v-model="lostPetEditForm.sex" placeholder="请选择性别">
            <el-option label="未知" value="未知" />
            <el-option label="公" value="公" />
            <el-option label="母" value="母" />
          </el-select>
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
        <el-form-item label="走失日期" prop="lostTime">
          <el-date-picker v-model="lostPetEditForm.lostTime" type="date" value-format="YYYY-MM-DD" placeholder="选择走失日期" class="full-width-control" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="lostPetEditForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
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
        <el-form-item label="明显特征">
          <el-input v-model="lostPetEditForm.features" placeholder="例如：项圈、毛色、体型特征" />
        </el-form-item>
        <el-form-item label="详细地点" prop="detailAddress" class="pet-admin-span-2">
          <el-input v-model="lostPetEditForm.detailAddress" placeholder="请输入详细地点" />
        </el-form-item>
        <el-form-item label="补充说明" class="pet-admin-span-2">
          <el-input v-model="lostPetEditForm.description" type="textarea" :autosize="{ minRows: 4, maxRows: 7 }" placeholder="补充走失经过或识别信息" />
        </el-form-item>
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
          <el-input
            v-model="lostPetReviewForm.reason"
            type="textarea"
            :autosize="{ minRows: 4, maxRows: 7 }"
            placeholder="例如：信息核验通过，保留为寻找中"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="lostPetReviewDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="savingLostPet" @click="saveLostPetReview">保存审核</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="taskEditDialogVisible" title="编辑救助任务" width="720px">
      <el-form ref="taskEditFormRef" :model="taskEditForm" :rules="taskEditRules" label-position="top" class="pet-admin-form">
        <el-form-item label="任务简介" prop="summary">
          <el-input v-model="taskEditForm.summary" maxlength="20" show-word-limit placeholder="请输入任务简介" clearable />
        </el-form-item>
        <el-form-item label="任务类型" prop="type">
          <el-select v-model="taskEditForm.type" placeholder="请选择任务类型">
            <el-option v-for="item in rescueTaskTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-select v-model="taskEditForm.province" placeholder="选择省份" filterable clearable @change="handleTaskEditProvinceChange">
            <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-select v-model="taskEditForm.city" placeholder="选择城市" filterable clearable :disabled="!taskEditForm.province" @change="handleTaskEditCityChange">
            <el-option v-for="item in taskEditCityOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="区县" prop="district">
          <el-select v-model="taskEditForm.district" placeholder="选择区县" filterable clearable :disabled="!taskEditForm.city">
            <el-option v-for="item in taskEditDistrictOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细位置" prop="detailAddress">
          <el-input v-model="taskEditForm.detailAddress" placeholder="请输入救助位置" clearable />
        </el-form-item>
        <el-form-item label="情况描述" prop="description" class="pet-admin-span-2">
          <el-input v-model="taskEditForm.description" type="textarea" :autosize="{ minRows: 4, maxRows: 7 }" placeholder="描述现场情况、动物状态和风险" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskEditDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="savingTask" @click="saveTaskInfo">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="taskReviewDialogVisible" title="审核救助任务" width="520px">
      <el-form ref="taskReviewFormRef" :model="taskReviewForm" :rules="taskReviewRules" label-position="top" class="pet-review-form">
        <el-form-item label="审核/状态" prop="status">
          <el-select v-model="taskReviewForm.status" placeholder="请选择审核状态">
            <el-option v-for="item in rescueTaskStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态原因" prop="reason">
          <el-input
            v-model="taskReviewForm.reason"
            type="textarea"
            :autosize="{ minRows: 4, maxRows: 7 }"
            placeholder="例如：资料审核通过，安排志愿者跟进"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskReviewDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="savingTask" @click="saveTaskReview">保存审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>
