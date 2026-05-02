<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { createPet } from '../api/pets'
import { useInformationCatalog } from '../composables/useInformationCatalog'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  name: '',
  age: 1,
  sex: '',
  type: '',
  breed: '',
  health: '',
  description: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
})

const rules = {
  sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
  type: [{ required: true, message: '请选择或输入宠物类型', trigger: 'change' }],
  breed: [{ required: true, message: '请选择或输入品种', trigger: 'change' }],
  province: [{ required: true, message: '请选择省份', trigger: 'change' }],
  city: [{ required: true, message: '请选择城市', trigger: 'change' }],
  district: [{ required: true, message: '请选择区县', trigger: 'change' }],
  detailAddress: [{ required: true, message: '请输入发现位置', trigger: 'blur' }],
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

function goBack() {
  router.push('/')
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
    const result = await createPet({
      name: form.name.trim(),
      age: Number(form.age || 0),
      sex: form.sex,
      type: form.type,
      breed: form.breed,
      health: form.health.trim(),
      description: form.description.trim(),
      province: form.province,
      city: form.city,
      district: form.district,
      detailAddress: form.detailAddress.trim(),
    })
    ElMessage.success('流浪宠物记录已创建，可继续上传图片/视频')
    router.push(result?.id ? `/pets/${result.id}/media` : '/pets')
  } catch (error) {
    ElMessage.warning(error?.message || '创建流浪宠物记录失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  ensureInformationCatalog()
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main action-form-page">
      <section class="action-form-panel">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="action-form-grid">
          <el-form-item label="宠物名称">
            <el-input v-model="form.name" placeholder="可暂留空，后续补充" clearable />
          </el-form-item>
          <el-form-item label="年龄（月）">
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
          <el-form-item label="品种" prop="breed">
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
          <el-form-item label="健康状况">
            <el-input v-model="form.health" placeholder="例如：轻微擦伤、精神状态良好" clearable />
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
          <el-form-item label="详细位置" prop="detailAddress" class="action-form-span-2">
            <el-input v-model="form.detailAddress" placeholder="例如：小区东门、绿道入口、公交站旁" clearable />
          </el-form-item>
          <el-form-item label="情况描述" class="action-form-span-2">
            <el-input v-model="form.description" type="textarea" :rows="4" placeholder="补充宠物状态、发现经过、是否亲人等信息" />
          </el-form-item>
        </el-form>

        <div class="action-form-actions">
          <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">取消</el-button>
          <el-button class="warm-btn" :icon="Check" :loading="submitting" @click="submitForm">提交记录</el-button>
        </div>
      </section>
    </main>

    <AppFooter />
  </div>
</template>
