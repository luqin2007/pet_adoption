<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { getPetById, updatePetById } from '../api/pets'
import { useInformationCatalog } from '../composables/useInformationCatalog'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'
import { useUserStore } from '../stores/user'
import { userCanEditPet } from '../utils/petPermissions'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const pet = ref(null)

const form = reactive({
  name: '',
  age: 0,
  sex: '',
  type: '',
  breed: '',
  health: '',
  description: '',
})

const rules = {
  name: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
  sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
  type: [{ required: true, message: '请选择或输入宠物类型', trigger: 'change' }],
  breed: [{ required: true, message: '请选择或输入品种', trigger: 'change' }],
  health: [{ required: true, message: '请输入健康状况', trigger: 'blur' }],
}

const { ensureInformationCatalog, typeOptions, getBreedOptions } = useInformationCatalog()
const petId = computed(() => String(route.params.id || ''))
const pageTitle = computed(() => pet.value?.name || '编辑宠物信息')
const breedOptions = computed(() => getBreedOptions(form.type))
const canEditCurrentPet = computed(() => userStore.isLoggedIn && userCanEditPet(userStore.profile, pet.value))

function fillForm(data) {
  form.name = data?.name || ''
  form.age = Number(data?.age || 0)
  form.sex = data?.sex || ''
  form.type = data?.type || ''
  form.breed = data?.breed || ''
  form.health = data?.health || ''
  form.description = data?.description || ''
}

function goBack() {
  router.push(petId.value ? `/pets/${petId.value}` : '/pets')
}

function handleTypeChange() {
  form.breed = ''
}

async function submitForm() {
  if (!formRef.value || submitting.value) {
    return
  }
  if (!canEditCurrentPet.value) {
    ElMessage.warning('当前账号暂无编辑权限')
    return
  }

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    await updatePetById(petId.value, {
      name: form.name.trim(),
      age: Number(form.age || 0),
      sex: form.sex,
      type: form.type,
      breed: form.breed,
      health: form.health.trim(),
      description: form.description.trim(),
    })
    ElMessage.success('宠物基本信息已保存')
    router.push(`/pets/${petId.value}`)
  } catch (error) {
    ElMessage.warning(error?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

async function loadPet() {
  if (!petId.value) {
    pet.value = null
    return
  }

  loading.value = true
  try {
    const result = await getPetById(petId.value)
    pet.value = result
    fillForm(result)
  } catch {
    pet.value = null
  } finally {
    loading.value = false
  }
}

watch(
  () => petId.value,
  () => {
    loadPet()
  },
)

onMounted(async () => {
  await ensureInformationCatalog()
  loadPet()
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main action-form-page" v-loading="loading">
      <section v-if="pet" class="action-form-panel">
        <div class="pet-edit-head">
          <div>
            <span>基础信息</span>
            <h1>{{ pageTitle }}</h1>
          </div>
          <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回档案</el-button>
        </div>

        <el-form v-if="canEditCurrentPet" ref="formRef" :model="form" :rules="rules" label-position="top" class="action-form-grid">
          <el-form-item label="宠物名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入宠物名称" clearable />
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
            <el-select v-model="form.breed" placeholder="选择或输入品种" filterable allow-create default-first-option clearable>
              <el-option v-for="item in breedOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="健康状况" prop="health">
            <el-input v-model="form.health" clearable />
          </el-form-item>
          <el-form-item label="情况描述" class="action-form-span-2">
            <el-input v-model="form.description" type="textarea" :rows="4" />
          </el-form-item>
        </el-form>

        <div v-if="canEditCurrentPet" class="action-form-actions">
          <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">取消</el-button>
          <el-button class="warm-btn" :icon="Check" :loading="submitting" @click="submitForm">保存信息</el-button>
        </div>

        <el-empty v-else description="没有编辑这份档案的权限" />
      </section>

      <section v-else-if="!loading" class="pet-profile-empty">
        <el-empty description="未找到对应的宠物档案">
          <el-button class="warm-btn" @click="router.push('/pets')">返回领养大厅</el-button>
        </el-empty>
      </section>
    </main>

    <AppFooter />
  </div>
</template>
