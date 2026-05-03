<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { getPetById } from '../api/pets'
import { createAdoptApplication } from '../api/services'
import { useInformationCatalog } from '../composables/useInformationCatalog'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loadingPet = ref(false)
const submitting = ref(false)
const pet = ref(null)

const form = reactive({
  applicantPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
})

const rules = {
  applicantPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  province: [{ required: true, message: '请选择省份', trigger: 'change' }],
  city: [{ required: true, message: '请选择城市', trigger: 'change' }],
  district: [{ required: true, message: '请选择区县', trigger: 'change' }],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
}

const {
  ensureInformationCatalog,
  ensureCityOptions,
  ensureDistrictOptions,
  provinceOptions,
  getCityOptions,
  getDistrictOptions,
} = useInformationCatalog()

const petId = computed(() => String(route.params.id || ''))
const cityOptions = computed(() => getCityOptions(form.province))
const districtOptions = computed(() => getDistrictOptions(form.province, form.city))
const canAdopt = computed(() => ['HEALTH', 'SHELTERED'].includes(pet.value?.status))

function goBack() {
  router.push(`/pets/${petId.value}`)
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

async function loadPet() {
  if (!petId.value) {
    return
  }
  loadingPet.value = true
  try {
    pet.value = await getPetById(petId.value)
  } catch (error) {
    ElMessage.warning(error?.message || '获取宠物信息失败')
  } finally {
    loadingPet.value = false
  }
}

async function submitForm() {
  if (!formRef.value || submitting.value || !pet.value) {
    return
  }
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    await createAdoptApplication({
      petId: Number(petId.value),
      applicantPhone: form.applicantPhone.trim(),
      province: form.province,
      city: form.city,
      district: form.district,
      detailAddress: form.detailAddress.trim(),
    })
    ElMessage.success('领养申请已提交')
    router.push('/console')
  } catch (error) {
    ElMessage.warning(error?.message || '提交领养申请失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await ensureInformationCatalog()
  await loadPet()
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main action-form-page" v-loading="loadingPet">
      <section class="action-form-hero">
        <div>
          <span class="hero-chip">领养申请</span>
          <h1>提交领养意向</h1>
          <p v-if="pet">想把 {{ pet.name || '这只毛孩子' }} 带回家，先留下联系方式和居住位置。</p>
        </div>
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回宠物档案</el-button>
      </section>

      <section v-if="pet" class="action-form-panel">
        <div class="pet-apply-summary">
          <strong>{{ pet.name || '未命名' }}</strong>
          <span>{{ pet.type || '宠物' }} · {{ pet.breed || '品种待补充' }} · {{ pet.sex || '未知' }}</span>
        </div>

        <el-alert
          v-if="!canAdopt"
          type="warning"
          :closable="false"
          title="这份档案暂不开放领养"
          description="只有开放领养的宠物可以提交申请。"
        />

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="action-form-grid">
          <el-form-item label="联系电话" prop="applicantPhone">
            <el-input v-model="form.applicantPhone" placeholder="请输入手机号或其他常用联系方式" clearable />
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
          <el-form-item label="详细地址" prop="detailAddress" class="action-form-span-2">
            <el-input v-model="form.detailAddress" placeholder="填写方便联系和评估的居住地址" clearable />
          </el-form-item>
        </el-form>

        <div class="action-form-actions">
          <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">取消</el-button>
          <el-button class="warm-btn" :icon="Check" :disabled="!canAdopt" :loading="submitting" @click="submitForm">提交申请</el-button>
        </div>
      </section>
    </main>

    <AppFooter />
  </div>
</template>
