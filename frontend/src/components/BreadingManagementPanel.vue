<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>寄养管理</strong>
        <span>提交寄养申请并查看当前入口</span>
      </div>
    </template>

    <section class="pet-admin-section">
      <section class="action-form-panel">
        <div class="agreement-draft-head">
          <div>
            <h2>提交寄养申请</h2>
            <p>和首页入口一致，但这里放在后台管理里，方便统一入口。</p>
          </div>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="action-form-grid">
          <el-form-item label="宠物名称" prop="petName">
            <el-input v-model="form.petName" placeholder="请输入宠物名称" clearable />
          </el-form-item>
          <el-form-item label="年龄（月）" prop="petAge">
            <el-input-number v-model="form.petAge" :min="0" :controls="false" />
          </el-form-item>
          <el-form-item label="宠物类型" prop="petType">
            <el-select v-model="form.petType" placeholder="选择类型" filterable clearable @change="handleTypeChange">
              <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="品种" prop="petBreed">
            <el-select v-model="form.petBreed" placeholder="选择品种" filterable clearable :disabled="!form.petType">
              <el-option v-for="item in breedOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="联系方式" prop="applicantPhone">
            <el-input v-model="form.applicantPhone" placeholder="手机号或微信号" clearable />
          </el-form-item>
          <el-form-item label="寄养时间" prop="timeRange">
            <el-date-picker
              v-model="form.timeRange"
              type="datetimerange"
              value-format="YYYY-MM-DDTHH:mm:ss"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
            />
          </el-form-item>
          <el-form-item label="宠物情况说明" class="action-form-span-2">
            <el-input v-model="form.petDescription" type="textarea" :rows="5" placeholder="写下性格、饮食习惯、健康情况和寄养原因" />
          </el-form-item>
        </el-form>

        <div class="agreement-draft-footer">
          <el-button class="soft-btn" @click="goBack">返回后台</el-button>
          <el-button class="warm-btn" :icon="Check" :loading="submitting" @click="submitForm">提交申请</el-button>
        </div>
      </section>
    </section>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { createBreadingApplication } from '../api/services'
import { useInformationCatalog } from '../composables/useInformationCatalog'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  petName: '',
  petAge: 1,
  petType: '',
  petBreed: '',
  petDescription: '',
  applicantPhone: '',
  timeRange: [],
})

const rules = {
  petName: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
  petAge: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
  petType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  petBreed: [{ required: true, message: '请选择品种', trigger: 'change' }],
  applicantPhone: [{ required: true, message: '请输入联系方式', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择寄养时间', trigger: 'change' }],
}

const { ensureInformationCatalog, typeOptions, getBreedOptions } = useInformationCatalog()
const breedOptions = computed(() => getBreedOptions(form.petType))

function goBack() {
  router.push('/console')
}

function handleTypeChange() {
  form.petBreed = ''
}

async function submitForm() {
  if (!formRef.value || submitting.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  const [time0, time1] = form.timeRange || []
  if (!time0 || !time1) {
    ElMessage.warning('请选择完整的寄养时间')
    return
  }

  submitting.value = true
  try {
    await createBreadingApplication({
      petName: form.petName.trim(),
      petAge: Number(form.petAge || 0),
      petType: form.petType,
      petBreed: form.petBreed,
      petDescription: form.petDescription.trim(),
      applicantPhone: form.applicantPhone.trim(),
      time0,
      time1,
    })
    ElMessage.success('宠物寄养申请已提交')
    router.push('/console')
  } catch (error) {
    ElMessage.warning(error?.message || '提交宠物寄养申请失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  ensureInformationCatalog()
})
</script>
