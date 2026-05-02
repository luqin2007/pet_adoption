<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { getPetById } from '../api/pets'
import { createLostPetClaim, getLostPets } from '../api/lost'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const pet = ref(null)
const lostPetOptions = ref([])

const form = reactive({
  lostPetId: '',
  applicantPhone: '',
  reason: '',
})

const rules = {
  lostPetId: [{ required: true, message: '请选择要认领的走失宠物记录', trigger: 'change' }],
  applicantPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入认领说明', trigger: 'blur' }],
}

const petId = computed(() => String(route.params.id || ''))
const canClaim = computed(() => !['ADOPTED', 'HOME'].includes(pet.value?.status))

function goBack() {
  router.push(`/pets/${petId.value}`)
}

async function loadData() {
  loading.value = true
  try {
    const [petResult, lostResult] = await Promise.all([
      getPetById(petId.value),
      getLostPets({
        size: 20,
        owner: userStore.profile.id ? [Number(userStore.profile.id)] : undefined,
        status: ['SEARCHING'],
      }),
    ])
    pet.value = petResult
    lostPetOptions.value = Array.isArray(lostResult?.records) ? lostResult.records : []
  } catch (error) {
    ElMessage.warning(error?.message || '加载认领信息失败')
  } finally {
    loading.value = false
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
    await createLostPetClaim({
      lostPetId: Number(form.lostPetId),
      petId: Number(petId.value),
      applicantPhone: form.applicantPhone.trim(),
      reason: form.reason.trim(),
    })
    ElMessage.success('认领申请已提交')
    router.push('/services')
  } catch (error) {
    ElMessage.warning(error?.message || '提交认领申请失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main action-form-page" v-loading="loading">
      <section class="action-form-hero">
        <div>
          <span class="hero-chip">认领申请</span>
          <h1>提交宠物认领</h1>
          <p v-if="pet">如果你认为 {{ pet.name || '这只宠物' }} 可能是自己走失的毛孩子，在这里认领。</p>
        </div>
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回宠物档案</el-button>
      </section>

      <section v-if="pet" class="action-form-panel">
        <div class="pet-apply-summary">
          <strong>{{ pet.name || '未命名' }}</strong>
          <span>{{ pet.type || '宠物' }} · {{ pet.breed || '品种待补充' }} · {{ pet.sex || '未知' }}</span>
        </div>

        <el-alert
          v-if="!lostPetOptions.length"
          type="info"
          :closable="false"
          title="你还没有可用于认领的走失记录"
          description="请先登记走失宠物，再返回这里提交认领申请。"
        />

        <el-alert
          v-else-if="!canClaim"
          type="warning"
          :closable="false"
          title="当前档案暂不支持认领"
          description="已完成领养或已回家的档案不能继续发起认领。"
        />

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="action-form-grid">
          <el-form-item label="选择走失记录" prop="lostPetId" class="action-form-span-2">
            <el-select v-model="form.lostPetId" placeholder="选择你登记过的走失宠物" filterable clearable>
              <el-option
                v-for="item in lostPetOptions"
                :key="item.id"
                :label="`${item.name || '未命名'} · ${item.type || '宠物'} · ${item.breed || '品种待补充'}`"
                :value="String(item.id)"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="联系电话" prop="applicantPhone">
            <el-input v-model="form.applicantPhone" placeholder="请输入可联系到你的电话" clearable />
          </el-form-item>
          <el-form-item label="认领说明" prop="reason" class="action-form-span-2">
            <el-input v-model="form.reason" type="textarea" :rows="5" placeholder="说明为什么判断这只宠物可能是你的，比如毛色、项圈、走失时间地点等" />
          </el-form-item>
        </el-form>

        <div class="action-form-actions">
          <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">取消</el-button>
          <el-button class="warm-btn" :icon="Check" :disabled="!canClaim || !lostPetOptions.length" :loading="submitting" @click="submitForm">提交认领</el-button>
        </div>
      </section>
    </main>

    <AppFooter />
  </div>
</template>
