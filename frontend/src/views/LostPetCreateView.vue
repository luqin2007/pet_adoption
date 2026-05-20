<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check, Delete, Upload } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import PetCard from '../components/PetCard.vue'
import { beginLostPet, createLostPet, deleteLostPetUpload, getLostPets, uploadLostPetMedia } from '../api/lost'
import { useInformationCatalog } from '../composables/useInformationCatalog'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const router = useRouter()
const formRef = ref(null)
const fileInputRef = ref(null)
const submitting = ref(false)
const uploadingImages = ref(false)
const draftId = ref('')
const lostImages = ref([])
const loadingSimilarLostPets = ref(false)
const similarLostPets = ref([])
let similarLostPetsTimer = null

const form = reactive({
  name: '',
  age: 1,
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

const rules = {
  name: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
  age: [{ required: true, message: '请输入年龄', trigger: 'change' }],
  sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
  type: [{ required: true, message: '请选择或输入宠物类型', trigger: 'change' }],
  lostTime: [{ required: true, message: '请选择走失时间', trigger: 'change' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  province: [{ required: true, message: '请选择省份', trigger: 'change' }],
  city: [{ required: true, message: '请选择城市', trigger: 'change' }],
  district: [{ required: true, message: '请选择区县', trigger: 'change' }],
  detailAddress: [{ required: true, message: '请输入走失地点', trigger: 'blur' }],
}

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

const breedOptions = computed(() => getBreedOptions(form.type))
const cityOptions = computed(() => getCityOptions(form.province))
const districtOptions = computed(() => getDistrictOptions(form.province, form.city))
const hasSimilarQuery = computed(() => Boolean(String(form.type || '').trim()) && Boolean(String(form.city || '').trim()))
const similarPetLikeRecords = computed(() =>
  similarLostPets.value.map((item) => ({
    id: item.id,
    cover: item.petCover,
    name: item.name,
    type: item.type,
    breed: item.breed,
    sex: item.sex,
    age: item.age,
    description: item.description,
    tags: item.features ? [{ tag: item.features }] : [],
    locations: item.location ? [item.location] : [],
    username: item.ownerName,
    _lostTime: item.lostTime,
  })),
)

function goBack() {
  router.push('/lost')
}

function openLostPetDetail(id) {
  if (!id) return
  router.push(`/lost/${id}`)
}

function handleTypeChange() {
  form.breed = ''
}

function handleProvinceChange() {
  form.city = ''
  form.district = ''
  if (form.province) {
    ensureCityOptions(form.province)
  }
}

function handleCityChange() {
  form.district = ''
  if (form.province && form.city) {
    ensureDistrictOptions(form.province, form.city)
  }
}

function formatDate(value) {
  if (!value) {
    return '时间待补充'
  }
  return String(value).slice(0, 10)
}

function lostPetLocationText(item) {
  return [item.location?.province, item.location?.city, item.location?.district].filter(Boolean).join(' · ') || '位置待补充'
}

async function loadSimilarLostPets() {
  if (!hasSimilarQuery.value) {
    similarLostPets.value = []
    return
  }

  loadingSimilarLostPets.value = true
  try {
    const result = await getLostPets({
      size: 4,
      type: [form.type.trim()],
      province: form.province || undefined,
      city: form.city || undefined,
      district: form.district || undefined,
      status: ['SEARCHING'],
    })
    similarLostPets.value = Array.isArray(result?.records) ? result.records : []
  } catch {
    similarLostPets.value = []
  } finally {
    loadingSimilarLostPets.value = false
  }
}

function scheduleSimilarLostPetsSearch() {
  if (similarLostPetsTimer) {
    clearTimeout(similarLostPetsTimer)
    similarLostPetsTimer = null
  }

  if (!hasSimilarQuery.value) {
    similarLostPets.value = []
    return
  }

  similarLostPetsTimer = setTimeout(() => {
    loadSimilarLostPets()
  }, 350)
}

async function ensureDraftId() {
  if (draftId.value) {
    return draftId.value
  }
  draftId.value = await beginLostPet()
  return draftId.value
}

function chooseImages() {
  fileInputRef.value?.click()
}

function revokeImage(item) {
  if (item?.previewUrl) {
    URL.revokeObjectURL(item.previewUrl)
  }
}

async function handleImageChange(event) {
  const files = Array.from(event.target.files || [])
  event.target.value = ''
  const images = files.filter((file) => file.type.startsWith('image/'))
  if (images.length !== files.length) {
    ElMessage.warning('走失报备目前只能上传图片')
  }
  if (!images.length || uploadingImages.value) {
    return
  }

  uploadingImages.value = true
  try {
    const id = await ensureDraftId()
    for (const file of images) {
      const filename = await uploadLostPetMedia(id, file)
      lostImages.value.push({
        uid: `${filename}-${Math.random().toString(16).slice(2)}`,
        filename,
        name: file.name,
        previewUrl: URL.createObjectURL(file),
      })
    }
    ElMessage.success('图片已上传')
  } catch (error) {
    ElMessage.warning(error?.message || '图片上传失败')
  } finally {
    uploadingImages.value = false
  }
}

async function removeImage(item) {
  if (uploadingImages.value) {
    return
  }
  try {
    if (draftId.value && item.filename) {
      await deleteLostPetUpload(draftId.value, item.filename)
    }
    revokeImage(item)
    lostImages.value = lostImages.value.filter((image) => image.uid !== item.uid)
  } catch (error) {
    ElMessage.warning(error?.message || '删除图片失败')
  }
}

async function submitForm() {
  if (!formRef.value || submitting.value) {
    return
  }

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const id = await ensureDraftId()
    await createLostPet({
      uuid: id,
      name: form.name.trim(),
      age: Number(form.age || 0),
      sex: form.sex,
      type: form.type,
      breed: form.breed.trim(),
      features: form.features.trim(),
      lostTime: `${form.lostTime}T00:00:00`,
      phone: form.phone.trim(),
      description: form.description.trim(),
      province: form.province,
      city: form.city,
      district: form.district,
      detailAddress: form.detailAddress.trim(),
    })
    ElMessage.success('走失宠物已登记')
    router.push('/lost')
  } catch (error) {
    ElMessage.warning(error?.message || '提交走失报备失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  ensureInformationCatalog()
})

onBeforeUnmount(() => {
  lostImages.value.forEach(revokeImage)
  if (similarLostPetsTimer) {
    clearTimeout(similarLostPetsTimer)
    similarLostPetsTimer = null
  }
})

watch(
  () => [form.type, form.province, form.city, form.district],
  () => {
    scheduleSimilarLostPetsSearch()
  },
)
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main action-form-page">
      <section class="action-form-panel">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="action-form-grid">
          <el-form-item label="宠物名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入走失宠物名称" clearable />
          </el-form-item>
          <el-form-item label="年龄（月）" prop="age">
            <el-input-number v-model="form.age" class="pet-create-age-input" :min="0" :controls="false" align="left" />
          </el-form-item>
          <el-form-item label="性别" prop="sex">
            <el-select v-model="form.sex" placeholder="选择性别" clearable>
              <el-option label="未知" value="未知" />
              <el-option label="公" value="公" />
              <el-option label="母" value="母" />
            </el-select>
          </el-form-item>
          <el-form-item label="宠物类型" prop="type">
            <el-select
              v-model="form.type"
              placeholder="选择或输入类型"
              filterable
              allow-create
              default-first-option
              clearable
              @change="handleTypeChange"
            >
              <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="品种">
            <el-select
              v-model="form.breed"
              placeholder="选择或输入品种"
              filterable
              allow-create
              default-first-option
              clearable
            >
              <el-option v-for="item in breedOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="走失时间" prop="lostTime">
            <el-date-picker v-model="form.lostTime" type="date" value-format="YYYY-MM-DD" placeholder="选择走失日期" class="full-width-control" />
          </el-form-item>
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入联系人手机号" clearable />
          </el-form-item>
          <el-form-item label="省份" prop="province">
            <el-select v-model="form.province" placeholder="选择省份" filterable clearable @change="handleProvinceChange">
              <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="城市" prop="city">
            <el-select v-model="form.city" placeholder="选择城市" filterable clearable :disabled="!form.province" @change="handleCityChange">
              <el-option v-for="item in cityOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="区县" prop="district">
            <el-select v-model="form.district" placeholder="选择区县" filterable clearable :disabled="!form.city">
              <el-option v-for="item in districtOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="详细地点" prop="detailAddress" class="action-form-span-2">
            <el-input v-model="form.detailAddress" clearable />
          </el-form-item>
          <el-form-item class="action-form-span-2" label="特征">
            <el-input v-model="form.features" clearable />
          </el-form-item>
          <el-form-item label="补充说明" class="action-form-span-2">
            <el-input v-model="form.description" type="textarea" :rows="5" />
          </el-form-item>
          <el-form-item label="宠物照片" class="action-form-span-2">
            <input ref="fileInputRef" class="profile-avatar-input" type="file" accept="image/*" multiple @change="handleImageChange" />
            <div class="rescue-image-uploader">
              <el-button class="soft-btn" :icon="Upload" :loading="uploadingImages" @click="chooseImages">上传图片</el-button>
              <div v-if="lostImages.length" class="rescue-image-list">
                <article v-for="item in lostImages" :key="item.uid">
                  <img :src="item.previewUrl" :alt="item.name" />
                  <span>{{ item.name }}</span>
                  <el-button text type="danger" :icon="Delete" @click="removeImage(item)">删除</el-button>
                </article>
              </div>
            </div>
          </el-form-item>
        </el-form>

        <div class="action-form-actions">
          <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">取消</el-button>
          <el-button class="warm-btn" :icon="Check" :loading="submitting" @click="submitForm">提交登记</el-button>
        </div>
      </section>

      <section v-if="similarLostPets.length" class="action-form-similar">
        <h3>相近走失记录</h3>
        <div class="lost-grid">
          <PetCard
            v-for="item in similarPetLikeRecords" :key="item.id"
            :pet="item"
            :badge-text="formatDate(item._lostTime)"
            @click="openLostPetDetail(item.id)"
          />
        </div>
      </section>
    </main>

    <AppFooter />
  </div>
</template>

<style scoped>
.action-form-similar {
  margin-top: 24px;
}
.action-form-similar h3 {
  margin: 0 0 12px;
  font-size: 18px;
  color: #4f3324;
}
</style>
