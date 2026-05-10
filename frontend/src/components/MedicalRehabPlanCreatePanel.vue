<template>
  <el-card class="profile-card">
    <template #header>
      <div class="profile-card-header">
        <strong>创建康复计划</strong>
        <span>{{ petName || '为宠物建立康复护理安排' }}</span>
      </div>
    </template>

    <section class="action-form-panel">
      <div class="rehab-create-head">
        <div>
          <h2>{{ petName || '新康复计划' }}</h2>
          <p>填写计划内容、执行频率和计划周期</p>
        </div>
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回</el-button>
      </div>

      <el-form label-position="top" class="medical-detail-edit-form rehab-create-form">
        <section class="medical-edit-section">
          <el-form-item label="计划标题">
            <el-input v-model="form.title" placeholder="例如：术后活动恢复计划" />
          </el-form-item>
        </section>
        <section class="medical-edit-section">
          <el-form-item label="执行频率">
            <el-input v-model="form.frequency" placeholder="例如：每日 2 次，每次 15 分钟" />
          </el-form-item>
        </section>
        <section class="medical-edit-section">
          <el-form-item label="宠物年龄（月）">
            <el-input-number v-model="form.age" :min="0" :max="360" class="full-width-control" />
          </el-form-item>
        </section>
        <section class="medical-edit-section">
          <el-form-item label="计划类型">
            <el-select v-model="form.type" class="full-width-control">
              <el-option label="物理康复" value="PHYSICAL" />
              <el-option label="其他康复" value="OTHER" />
            </el-select>
          </el-form-item>
        </section>
        <section class="medical-edit-section">
          <el-form-item label="开始时间">
            <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss.SSS" class="full-width-control" />
          </el-form-item>
        </section>
        <section class="medical-edit-section">
          <el-form-item label="预计结束时间">
            <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss.SSS" class="full-width-control" />
          </el-form-item>
        </section>
        <section class="medical-edit-section medical-edit-section-wide">
          <el-form-item label="康复内容">
            <el-input v-model="form.content" type="textarea" :rows="6" placeholder="填写康复目标、执行方式、注意事项和观察重点" />
          </el-form-item>
        </section>
      </el-form>

      <div class="rehab-create-footer">
        <el-button class="soft-btn" @click="goBack">取消</el-button>
        <el-button class="warm-btn" :loading="saving" @click="submitPlan">创建计划</el-button>
      </div>
    </section>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { createRehabPlan } from '../api/services'

const route = useRoute()
const router = useRouter()
const saving = ref(false)
const petName = ref('')
const form = reactive({
  age: 0,
  title: '',
  content: '',
  frequency: '',
  type: 'PHYSICAL',
  startTime: '',
  endTime: '',
})

function nowValue() {
  const date = new Date()
  return date.toISOString().slice(0, 23)
}

function goBack() {
  router.push('/console/medical/first')
}

function validateForm() {
  if (!route.query.pet) return '缺少宠物信息'
  if (!form.title.trim()) return '请输入计划标题'
  if (!form.frequency.trim()) return '请输入执行频率'
  if (!form.content.trim()) return '请输入康复内容'
  if (!form.startTime || !form.endTime) return '请选择计划时间'
  return ''
}

async function submitPlan() {
  const message = validateForm()
  if (message) {
    ElMessage.warning(message)
    return
  }
  saving.value = true
  try {
    const result = await createRehabPlan(route.query.pet, {
      age: Number(form.age || 0),
      title: form.title.trim(),
      content: form.content.trim(),
      frequency: form.frequency.trim(),
      type: form.type,
      startTime: form.startTime,
      endTime: form.endTime,
      orders: [],
    })
    ElMessage.success('康复计划已创建')
    router.push(`/console/medical/rehab/${result.id}`)
  } catch (error) {
    ElMessage.warning(error?.message || '创建康复计划失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  petName.value = String(route.query.name || '')
  form.age = Number(route.query.age || 0)
  form.startTime = nowValue()
})
</script>

<style scoped>
.rehab-create-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.rehab-create-head h2 {
  margin: 0;
  color: #5d3927;
  font-size: 24px;
  line-height: 1.3;
}

.rehab-create-head p {
  margin: 6px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.rehab-create-form {
  border-top: 1px solid var(--line);
}

.rehab-create-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
}
</style>
