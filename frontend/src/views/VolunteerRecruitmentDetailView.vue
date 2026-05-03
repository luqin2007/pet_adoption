<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Calendar,
  Check,
  Location,
  UserFilled,
} from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { createVolunteerApplication, getRecruitmentById } from '../api/volunteer'
import { useInformationCatalog } from '../composables/useInformationCatalog'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const recruitment = ref(null)

const form = reactive({
  realName: '',
  phone: '',
  sex: '',
  age: undefined,
  profession: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  experience: '',
  skills: '',
  availableTimeDesc: '',
  motivation: '',
})

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
  age: [{ required: true, message: '请输入年龄', trigger: 'change' }],
  province: [{ required: true, message: '请选择省份', trigger: 'change' }],
  city: [{ required: true, message: '请选择城市', trigger: 'change' }],
  district: [{ required: true, message: '请选择区县', trigger: 'change' }],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  experience: [{ required: true, message: '请填写过往经历', trigger: 'blur' }],
  skills: [{ required: true, message: '请填写可提供的技能', trigger: 'blur' }],
  availableTimeDesc: [{ required: true, message: '请填写可服务时间', trigger: 'blur' }],
  motivation: [{ required: true, message: '请填写申请动机', trigger: 'blur' }],
}

const sexOptions = ['男', '女', '保密']

const {
  ensureInformationCatalog,
  ensureCityOptions,
  ensureDistrictOptions,
  provinceOptions,
  getCityOptions,
  getDistrictOptions,
} = useInformationCatalog()

const recruitmentId = computed(() => String(route.params.id || ''))
const cityOptions = computed(() => getCityOptions(form.province))
const districtOptions = computed(() => getDistrictOptions(form.province, form.city))
const isExpired = computed(() => recruitment.value?.status && recruitment.value.status !== 'PUBLISHED')

function formatDate(value) {
  if (!value) {
    return '待补充'
  }
  return String(value).slice(0, 10)
}

function progressText(item) {
  const total = Number(item?.headcount || 0)
  const current = Number(item?.appliedCount || 0)
  if (!total) {
    return '名额待确认'
  }
  return `${current}/${total} 已报名`
}

function locationText(item) {
  return [item?.province, item?.city, item?.district, item?.serviceAddress].filter(Boolean).join(' · ') || '服务地点待补充'
}

function goBack() {
  router.push('/volunteers')
}

function goLogin() {
  router.push({
    path: '/login',
    query: { redirect: route.fullPath },
  })
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

async function loadRecruitment() {
  if (!recruitmentId.value) {
    return
  }
  loading.value = true
  try {
    recruitment.value = await getRecruitmentById(recruitmentId.value)
  } catch (error) {
    ElMessage.warning(error?.message || '获取招募信息失败')
  } finally {
    loading.value = false
  }
}

async function submitApplication() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再提交志愿申请')
    goLogin()
    return
  }
  if (!formRef.value || !recruitment.value || submitting.value || isExpired.value) {
    return
  }
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    await createVolunteerApplication({
      recruitmentId: Number(recruitmentId.value),
      realName: form.realName.trim(),
      phone: form.phone.trim(),
      sex: form.sex,
      age: Number(form.age),
      profession: form.profession.trim() || undefined,
      province: form.province,
      city: form.city,
      district: form.district,
      detailAddress: form.detailAddress.trim(),
      experience: form.experience.trim(),
      skills: form.skills.trim(),
      availableTimeDesc: form.availableTimeDesc.trim(),
      motivation: form.motivation.trim(),
    })
    ElMessage.success('志愿申请已提交')
    router.push('/console')
  } catch (error) {
    ElMessage.warning(error?.message || '提交志愿申请失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await ensureInformationCatalog()
  form.realName = userStore.profile.username || ''
  form.phone = userStore.profile.phone || ''
  await loadRecruitment()
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main recruitment-detail-page" v-loading="loading">
      <section class="recruitment-detail-hero" v-if="recruitment">
        <div class="recruitment-detail-copy">
          <span class="hero-chip">志愿招募详情</span>
          <h1>{{ recruitment.title || '志愿招募' }}</h1>
          <p>{{ recruitment.description || '招募说明待补充' }}</p>
          <div class="recruitment-detail-meta">
            <span><el-icon><Location /></el-icon>{{ locationText(recruitment) }}</span>
            <span><el-icon><Calendar /></el-icon>{{ formatDate(recruitment.startTime) }} - {{ formatDate(recruitment.endTime) }}</span>
            <span><el-icon><UserFilled /></el-icon>{{ progressText(recruitment) }}</span>
          </div>
        </div>
        <div class="recruitment-detail-sidecard">
          <span>发布人</span>
          <strong>{{ recruitment.publisherName || '暖窝志愿组' }}</strong>
          <p>读完要求后，可以直接在下方申请。</p>
          <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回招募列表</el-button>
        </div>
      </section>

      <section v-if="recruitment" class="recruitment-detail-grid">
        <article class="recruitment-detail-panel">
          <div class="detail-section-head">
            <strong>招募信息</strong>
          </div>
          <div class="recruitment-detail-stack">
            <div class="recruitment-detail-item">
              <span>服务地点</span>
              <p>{{ recruitment.serviceAddress || '待补充' }}</p>
            </div>
            <div class="recruitment-detail-item">
              <span>招募人数</span>
              <p>{{ recruitment.headcount || '待补充' }}</p>
            </div>
            <div class="recruitment-detail-item recruitment-detail-item-full">
              <span>参与要求</span>
              <p>{{ recruitment.requirement || '请保持沟通及时，配合排班安排。' }}</p>
            </div>
          </div>
        </article>

        <article class="recruitment-detail-panel">
          <div class="detail-section-head">
            <strong>直接申请</strong>
          </div>

          <el-alert
            v-if="userStore.isLoggedIn && isExpired"
            type="warning"
            :closable="false"
            title="当前招募暂不可提交申请"
            description="这项招募暂未开放，可以看看其他项目。"
          />

          <div class="volunteer-apply-shell" :class="{ 'is-locked': !userStore.isLoggedIn }">
            <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              :disabled="!userStore.isLoggedIn || isExpired"
              label-position="top"
              class="action-form-grid volunteer-apply-grid"
            >
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="form.realName" placeholder="请输入真实姓名" clearable />
              </el-form-item>
              <el-form-item label="联系电话" prop="phone">
                <el-input v-model="form.phone" placeholder="请输入手机号或常用联系方式" clearable />
              </el-form-item>
              <el-form-item label="性别" prop="sex">
                <el-select v-model="form.sex" placeholder="请选择性别" clearable>
                  <el-option v-for="item in sexOptions" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
              <el-form-item label="年龄" prop="age">
                <el-input-number v-model="form.age" :min="1" :max="100" controls-position="right" class="full-width-control" />
              </el-form-item>
              <el-form-item label="职业">
                <el-input v-model="form.profession" placeholder="可填写当前职业或身份" clearable />
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
                <el-input v-model="form.detailAddress" placeholder="填写常驻位置" clearable />
              </el-form-item>
              <el-form-item label="过往经历" prop="experience" class="action-form-span-2">
                <el-input v-model="form.experience" type="textarea" :rows="4" placeholder="参与过巡护、救助、接待或记录整理吗" />
              </el-form-item>
              <el-form-item label="可提供的技能" prop="skills" class="action-form-span-2">
                <el-input v-model="form.skills" type="textarea" :rows="3" placeholder="比如拍照、沟通、驾驶、基础护理" />
              </el-form-item>
              <el-form-item label="可服务时间" prop="availableTimeDesc" class="action-form-span-2">
                <el-input v-model="form.availableTimeDesc" type="textarea" :rows="3" placeholder="比如周末全天、工作日晚上" />
              </el-form-item>
              <el-form-item label="申请动机" prop="motivation" class="action-form-span-2">
                <el-input v-model="form.motivation" type="textarea" :rows="4" placeholder="说说你为什么想参与这项志愿服务" />
              </el-form-item>
            </el-form>

            <div v-if="!userStore.isLoggedIn" class="volunteer-login-overlay">
              <div class="volunteer-login-card">
                <span class="volunteer-login-icon"><el-icon><UserFilled /></el-icon></span>
                <strong>登录后可直接提交志愿申请</strong>
                <p>登录后会回到当前招募，继续填写申请。</p>
                <el-button class="warm-btn" @click="goLogin">去登录</el-button>
              </div>
            </div>
          </div>

          <div class="action-form-actions">
            <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回列表</el-button>
            <el-button class="warm-btn" :icon="Check" :disabled="!userStore.isLoggedIn || isExpired" :loading="submitting" @click="submitApplication">提交申请</el-button>
          </div>
        </article>
      </section>
    </main>

    <AppFooter />
  </div>
</template>
