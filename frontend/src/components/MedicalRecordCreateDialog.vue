<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getPetById } from '../api/pets'
import { addMedicalRecord } from '../api/services'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  petId: { type: [String, Number], default: '' },
  petName: { type: String, default: '' },
  petAge: { type: [Number, null], default: null },
})

const emit = defineEmits(['update:modelValue', 'created'])

const visible = ref(false)
const formRef = ref()
const submitting = ref(false)

const form = reactive({
  petId: '',
  petName: '',
  petAge: null,
  type: '',
  price: '',
  ownerPhone: '',
})

const recordTypeOptions = [
  { label: '初诊', value: 'FIRST' },
  { label: '复诊', value: 'REVISIT' },
  { label: '急诊', value: 'EMERGENCY' },
  { label: '体检', value: 'EXAMINATION' },
]

const rules = {
  petAge: [{ required: true, message: '请输入宠物年龄', trigger: 'blur' }],
  type: [{ required: true, message: '请选择就诊类型', trigger: 'change' }],
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    resetForm()
    loadPetInfo()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

function resetForm() {
  form.petId = String(props.petId || '')
  form.petName = props.petName || ''
  form.petAge = props.petAge ?? null
  form.type = ''
  form.price = ''
  form.ownerPhone = ''
  formRef.value?.resetFields()
}

async function loadPetInfo() {
  if (!props.petId) return
  if (props.petName) {
    form.petId = String(props.petId)
    form.petName = props.petName
    form.petAge = props.petAge ?? null
    return
  }
  try {
    const pet = await getPetById(props.petId)
    if (pet?.name) form.petName = pet.name
    if (pet?.age != null) form.petAge = pet.age
  } catch {
    // ignore
  }
}

async function submitForm() {
  if (!formRef.value || submitting.value) return
  try { await formRef.value.validate() } catch { return }

  submitting.value = true
  try {
    const payload = {
      petAge: Number(form.petAge),
      type: form.type,
      price: form.price || undefined,
      cost: 0,
      ownerPhone: form.ownerPhone?.trim() || undefined,
    }
    await addMedicalRecord(form.petId, payload)
    ElMessage.success('就诊记录已创建')
    visible.value = false
    emit('created')
  } catch (err) {
    ElMessage.warning(err?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <el-dialog v-model="visible" title="新增就诊记录" width="480px" :close-on-click-modal="false">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" style="display:grid;gap:4px">
      <el-form-item label="宠物名称">
        <el-input v-model="form.petName" disabled />
      </el-form-item>
      <el-form-item label="领养人电话">
        <el-input v-model="form.ownerPhone" placeholder="可选" />
      </el-form-item>
      <div class="record-create-row">
        <el-form-item label="年龄 (月)" prop="petAge" class="record-create-half">
          <el-input-number v-model="form.petAge" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="就诊类型" prop="type" class="record-create-half">
          <el-select v-model="form.type" placeholder="请选择" style="width: 100%">
            <el-option v-for="item in recordTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </div>
      <el-form-item label="预计费用">
        <el-input v-model="form.price" placeholder="可选" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="soft-btn" @click="visible = false">取消</el-button>
        <el-button class="warm-btn" :loading="submitting" @click="submitForm">创建记录</el-button>
      </div>
</template>

<style scoped>
.record-create-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}
.record-create-half {
  width: 100%;
}
</style>
  </el-dialog>
</template>