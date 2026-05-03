<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check, Delete, Upload } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { beginRescueTask, createRescueTask, deleteRescueTaskUpload, uploadRescueTaskMedia } from '../api/services'
import { useInformationCatalog } from '../composables/useInformationCatalog'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const router = useRouter()
const formRef = ref(null)
const fileInputRef = ref(null)
const submitting = ref(false)
const uploadingImages = ref(false)
const draftId = ref('')
const rescueImages = ref([])

const form = reactive({
  summary: '',
  description: '',
  type: 'FIND',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
})

const rules = {
  summary: [{ required: true, message: '请输入任务简介', trigger: 'blur' }],
  description: [{ required: true, message: '请输入救助描述', trigger: 'blur' }],
  type: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  province: [{ required: true, message: '请选择省份', trigger: 'change' }],
  city: [{ required: true, message: '请选择城市', trigger: 'change' }],
  district: [{ required: true, message: '请选择区县', trigger: 'change' }],
  detailAddress: [{ required: true, message: '请输入救助位置', trigger: 'blur' }],
}

const rescueTypeOptions = [
  { label: '发现流浪宠物', value: 'FIND' },
  { label: '医疗救助', value: 'MEDICAL' },
  { label: '其他协助', value: 'OTHER' },
]

const {
  ensureInformationCatalog,
  ensureCityOptions,
  ensureDistrictOptions,
  provinceOptions,
  getCityOptions,
  getDistrictOptions,
} = useInformationCatalog()

const cityOptions = computed(() => getCityOptions(form.province))
const districtOptions = computed(() => getDistrictOptions(form.province, form.city))

function goBack() {
  router.push('/')
}

function isSupportedImage(file) {
  return file.type.startsWith('image/')
}

async function ensureDraftId() {
  if (draftId.value) {
    return draftId.value
  }
  draftId.value = await beginRescueTask()
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
  const images = files.filter(isSupportedImage)
  if (images.length !== files.length) {
    ElMessage.warning('救助任务只能上传图片')
  }
  if (!images.length || uploadingImages.value) {
    return
  }

  uploadingImages.value = true
  try {
    const id = await ensureDraftId()
    for (const file of images) {
      const filename = await uploadRescueTaskMedia(id, file)
      rescueImages.value.push({
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
      await deleteRescueTaskUpload(draftId.value, item.filename)
    }
    revokeImage(item)
    rescueImages.value = rescueImages.value.filter((image) => image.uid !== item.uid)
  } catch (error) {
    ElMessage.warning(error?.message || '删除图片失败')
  }
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
    const result = await createRescueTask({
      id,
      summary: form.summary.trim(),
      description: form.description.trim(),
      type: form.type,
      province: form.province,
      city: form.city,
      district: form.district,
      detailAddress: form.detailAddress.trim(),
    })
    ElMessage.success('救助任务已提交')
    router.push(result?.id ? `/tasks/${result.id}` : '/console')
  } catch (error) {
    ElMessage.warning(error?.message || '提交救助任务失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  ensureInformationCatalog()
})

onBeforeUnmount(() => {
  rescueImages.value.forEach(revokeImage)
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main action-form-page">
      <section class="action-form-panel">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="action-form-grid">
          <el-form-item label="任务简介" prop="summary">
            <el-input v-model="form.summary" maxlength="20" show-word-limit placeholder="例如：滨江幼猫救助" clearable />
          </el-form-item>
          <el-form-item label="任务类型" prop="type">
            <el-select v-model="form.type" placeholder="选择任务类型">
              <el-option v-for="item in rescueTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
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
          <el-form-item label="详细位置" prop="detailAddress">
            <el-input v-model="form.detailAddress" placeholder="例如：公园西门、桥下通道" clearable />
          </el-form-item>
          <el-form-item label="情况描述" prop="description" class="action-form-span-2">
            <el-input v-model="form.description" type="textarea" :rows="5" placeholder="写下动物数量、伤情、能否接近和现场风险" />
          </el-form-item>
          <el-form-item label="现场图片" class="action-form-span-2">
            <input ref="fileInputRef" class="profile-avatar-input" type="file" accept="image/*" multiple @change="handleImageChange" />
            <div class="rescue-image-uploader">
              <el-button class="soft-btn" :icon="Upload" :loading="uploadingImages" @click="chooseImages">上传图片</el-button>
              <div v-if="rescueImages.length" class="rescue-image-list">
                <article v-for="item in rescueImages" :key="item.uid">
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
          <el-button class="warm-btn" :icon="Check" :loading="submitting" @click="submitForm">提交任务</el-button>
        </div>
      </section>
    </main>

    <AppFooter />
  </div>
</template>
