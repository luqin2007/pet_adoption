<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, EditPen, LocationInformation, Plus } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { getPetHealthAssessments } from '../api/services'
import { addPetLocation, getPetById } from '../api/pets'
import { useInformationCatalog } from '../composables/useInformationCatalog'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'
import { useUserStore } from '../stores/user'
import { userCanEditPet } from '../utils/petPermissions'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const savingLocation = ref(false)
const locationDialogVisible = ref(false)
const locationFormRef = ref(null)
const pet = ref(null)
const locationForm = ref({
  province: '',
  city: '',
  district: '',
  detailAddress: '',
})
const latestHealthAssessment = ref(null)

const locationRules = {
  province: [{ required: true, message: '请选择省份', trigger: 'change' }],
  city: [{ required: true, message: '请选择城市', trigger: 'change' }],
  district: [{ required: true, message: '请选择区县', trigger: 'change' }],
  detailAddress: [{ required: true, message: '请输入详细位置', trigger: 'blur' }],
}

const petId = computed(() => String(route.params.id || ''))
const primaryLocation = computed(() => pet.value?.locations?.[0] || null)
const statusText = computed(() => mapStatusText(pet.value?.status))
const statusTone = computed(() => mapStatusTone(pet.value?.status))
const pageTitle = computed(() => pet.value?.name || '宠物档案')
const canEditCurrentPet = computed(() => userStore.isLoggedIn && userCanEditPet(userStore.profile, pet.value))
const canApplyAdopt = computed(() => ['HEALTH', 'SHELTERED'].includes(pet.value?.status))
const canApplyClaim = computed(() => !['ADOPTED', 'HOME'].includes(pet.value?.status))

const infoItems = computed(() => [
  { label: '宠物类型', value: pet.value?.type || '待补充' },
  { label: '品种', value: pet.value?.breed || '待补充' },
  { label: '性别', value: pet.value?.sex || '待补充' },
  { label: '年龄', value: formatAge(pet.value?.age) },
  { label: '发现人', value: pet.value?.username || '暖窝救助站' },
])

const rescueSummary = computed(() =>
  [
    primaryLocation.value
      ? [primaryLocation.value.province, primaryLocation.value.city, primaryLocation.value.district].filter(Boolean).join(' · ')
      : '',
    primaryLocation.value?.detailAddress || primaryLocation.value?.address || '',
  ]
    .filter(Boolean)
    .join(' · ') || '位置待补充',
)

const vaccineItems = computed(() => (Array.isArray(pet.value?.vaccines) ? pet.value.vaccines : []))
const dewormItems = computed(() => (Array.isArray(pet.value?.deworms) ? pet.value.deworms : []))
const locationItems = computed(() => (Array.isArray(pet.value?.locations) ? pet.value.locations : []))
const {
  ensureInformationCatalog,
  ensureCityOptions,
  ensureDistrictOptions,
  provinceOptions,
  getCityOptions,
  getDistrictOptions,
} = useInformationCatalog()
const locationCityOptions = computed(() => getCityOptions(locationForm.value.province))
const locationDistrictOptions = computed(() => getDistrictOptions(locationForm.value.province, locationForm.value.city))

function mapStatusText(status) {
  return status || '状态待补充'
}

function mapStatusTone(status) {
  const map = {
    HEALTH: 'success',
    SHELTERED: 'warning',
    ADOPTED: 'info',
    TREATING: 'danger',
    RESCUED: '',
  }
  return map[status] || ''
}

function formatAge(age) {
  if (age === undefined || age === null || age === '') {
    return '年龄待补充'
  }
  const months = Number(age)
  if (Number.isNaN(months)) {
    return '年龄待补充'
  }
  if (months > 12) {
    const years = Math.floor(months / 12)
    const remainMonths = months % 12
    return remainMonths > 0 ? `约 ${years} 岁 ${remainMonths} 个月` : `约 ${years} 岁`
  }
  return `约 ${months} 个月`
}

function formatDate(value) {
  if (!value) {
    return '时间待补充'
  }
  return String(value).slice(0, 10)
}

function formatLocation(location) {
  if (!location) {
    return '位置待补充'
  }
  return [location.province, location.city, location.district, location.detailAddress || location.address]
    .filter(Boolean)
    .join(' · ')
}

function goBack() {
  router.push('/pets')
}

function openAlbum() {
  router.push(`/pets/${petId.value}/media`)
}

function openBasicEditor() {
  router.push(`/pets/${petId.value}/edit`)
}

function openHealthAssessment() {
  if (latestHealthAssessment.value?.id) {
    router.push(`/medical/health/${latestHealthAssessment.value.id}`)
  }
}

function openAdoptFlow() {
  router.push(`/pets/${petId.value}/adopt`)
}

function openClaimFlow() {
  router.push(`/pets/${petId.value}/claim`)
}

function resetLocationForm() {
  locationForm.value = {
    province: primaryLocation.value?.province || '',
    city: primaryLocation.value?.city || '',
    district: primaryLocation.value?.district || '',
    detailAddress: '',
  }
}

async function openLocationDialog() {
  await ensureInformationCatalog()
  resetLocationForm()
  if (locationForm.value.province) {
    await ensureCityOptions(locationForm.value.province)
  }
  if (locationForm.value.province && locationForm.value.city) {
    await ensureDistrictOptions(locationForm.value.province, locationForm.value.city)
  }
  locationDialogVisible.value = true
}

function handleLocationProvinceChange() {
  locationForm.value.city = ''
  locationForm.value.district = ''
  if (locationForm.value.province) {
    ensureCityOptions(locationForm.value.province)
  }
}

function handleLocationCityChange() {
  locationForm.value.district = ''
  if (locationForm.value.province && locationForm.value.city) {
    ensureDistrictOptions(locationForm.value.province, locationForm.value.city)
  }
}

async function submitLocation() {
  if (!locationFormRef.value || savingLocation.value) {
    return
  }

  try {
    await locationFormRef.value.validate()
  } catch {
    return
  }

  savingLocation.value = true
  try {
    pet.value = await addPetLocation(petId.value, locationForm.value)
    ElMessage.success('位置记录已添加')
    locationDialogVisible.value = false
  } catch (error) {
    ElMessage.warning(error?.message || '添加位置记录失败')
  } finally {
    savingLocation.value = false
  }
}

async function loadPet() {
  if (!petId.value) {
    pet.value = null
    return
  }

  loading.value = true
  try {
    pet.value = await getPetById(petId.value)
    await loadLatestHealthAssessment()
  } catch {
    pet.value = null
    latestHealthAssessment.value = null
  } finally {
    loading.value = false
  }
}

async function loadLatestHealthAssessment() {
  latestHealthAssessment.value = null
  if (!petId.value) return
  try {
    const result = await getPetHealthAssessments(petId.value, { page: 1, size: 1, sort: 'create_time', order: 'desc' })
    latestHealthAssessment.value = result?.records?.[0] || null
  } catch {
    latestHealthAssessment.value = null
  }
}

watch(
  () => petId.value,
  () => {
    loadPet()
  },
)

onMounted(() => {
  loadPet()
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main pet-profile-page" v-loading="loading">
      <section v-if="pet" class="pet-profile-hero">
        <div class="pet-profile-media" role="button" tabindex="0" @click="openAlbum" @keydown.enter="openAlbum" @keydown.space.prevent="openAlbum">
          <img v-if="pet.cover" :src="pet.cover" :alt="pet.name" loading="lazy" />
          <div v-else class="pet-profile-cover-placeholder">暂无封面</div>
        </div>

        <div class="pet-profile-main">
          <div class="pet-profile-topbar">
            <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回领养大厅</el-button>
            <div class="pet-profile-actions">
              <el-button class="soft-btn" :disabled="!canApplyAdopt" @click="openAdoptFlow">领养</el-button>
              <el-button class="soft-btn" :disabled="!canApplyClaim" @click="openClaimFlow">认领</el-button>
              <el-button v-if="canEditCurrentPet" class="soft-btn" :icon="EditPen" @click="openBasicEditor">编辑</el-button>
              <el-tag :type="statusTone" effect="dark">{{ statusText }}</el-tag>
            </div>
          </div>

          <h1>{{ pageTitle }}</h1>
          <p class="pet-profile-summary">
            {{ pet.description || '这只毛孩子的故事还在整理，先看看基本信息和近期位置。' }}
          </p>

          <div class="pet-profile-location">
            <el-icon><LocationInformation /></el-icon>
            <span>{{ rescueSummary }}</span>
          </div>

          <div class="pet-profile-facts">
            <article v-for="item in infoItems" :key="item.label">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </article>
          </div>
        </div>
      </section>

      <section v-if="pet" class="pet-profile-layout">
        <section class="pet-profile-panel">
          <div class="pet-profile-panel-head">
            <h2>健康状况</h2>
            <el-button v-if="latestHealthAssessment" class="soft-btn" size="small" @click="openHealthAssessment">健康评估</el-button>
          </div>
          <div class="pet-profile-health-status">
            <Icon icon="mdi:shield-heart-outline" />
            <strong>{{ pet.health || '健康档案整理中' }}</strong>
          </div>
        </section>

        <section class="pet-profile-panel">
          <div class="pet-profile-panel-head">
            <h2>位置记录</h2>
            <el-button class="soft-btn" :icon="Plus" size="small" @click="openLocationDialog">添加</el-button>
          </div>
          <div v-if="locationItems.length" class="pet-profile-list pet-profile-location-list">
            <article
              v-for="(item, index) in locationItems"
              :key="`${item.id || 'location'}-${index}`"
              class="pet-profile-location-item"
            >
              <strong>{{ formatLocation(item) }}</strong>
              <span>{{ item.createTime ? formatDate(item.createTime) : '已记录位置节点' }}</span>
            </article>
          </div>
          <el-empty v-else description="暂未补充位置记录" />
        </section>

        <section class="pet-profile-panel">
          <div class="pet-profile-panel-head">
            <h2>疫苗记录</h2>
          </div>
          <div v-if="vaccineItems.length" class="pet-profile-list">
            <article v-for="item in vaccineItems" :key="item.id">
              <strong>{{ item.vaccineName || '疫苗记录' }}</strong>
              <span>{{ formatDate(item.createTime) }} · 第 {{ item.times || 0 }} 针</span>
            </article>
          </div>
          <el-empty v-else description="暂无疫苗记录" />
        </section>

        <section class="pet-profile-panel">
          <div class="pet-profile-panel-head">
            <h2>驱虫记录</h2>
          </div>
          <div v-if="dewormItems.length" class="pet-profile-list">
            <article v-for="item in dewormItems" :key="item.id">
              <strong>{{ item.dewormerName || '驱虫记录' }}</strong>
              <span>{{ formatDate(item.createTime) }}</span>
            </article>
          </div>
          <el-empty v-else description="暂无驱虫记录" />
        </section>
      </section>

      <section v-else-if="!loading" class="pet-profile-empty">
        <el-empty description="未找到对应的宠物档案">
          <el-button class="warm-btn" @click="goBack">返回领养大厅</el-button>
        </el-empty>
      </section>

      <el-dialog v-model="locationDialogVisible" title="补充出现位置" width="560px">
        <el-form ref="locationFormRef" :model="locationForm" :rules="locationRules" label-position="top" class="location-add-form">
          <el-form-item label="省份" prop="province">
            <el-select v-model="locationForm.province" placeholder="选择省份" filterable clearable @change="handleLocationProvinceChange">
              <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="城市" prop="city">
            <el-select v-model="locationForm.city" placeholder="选择城市" filterable clearable :disabled="!locationForm.province" @change="handleLocationCityChange">
              <el-option v-for="item in locationCityOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="区县" prop="district">
            <el-select v-model="locationForm.district" placeholder="选择区县" filterable clearable :disabled="!locationForm.city">
              <el-option v-for="item in locationDistrictOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="详细位置" prop="detailAddress" class="location-add-span-2">
            <el-input v-model="locationForm.detailAddress" clearable />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="locationDialogVisible = false">取消</el-button>
          <el-button type="warning" :loading="savingLocation" @click="submitLocation">保存位置</el-button>
        </template>
      </el-dialog>
    </main>

    <AppFooter />
  </div>
</template>
