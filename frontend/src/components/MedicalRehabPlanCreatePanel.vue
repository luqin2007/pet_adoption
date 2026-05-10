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
        <section class="medical-edit-section medical-edit-section-wide">
          <div class="rehab-plan-time-row">
            <el-form-item label="宠物年龄（月）">
              <el-input-number v-model="form.age" :min="0" :max="360" class="full-width-control" />
            </el-form-item>
            <el-form-item label="开始时间">
              <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss.SSS" class="full-width-control" />
            </el-form-item>
            <el-form-item label="预计结束时间">
              <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss.SSS" class="full-width-control" />
            </el-form-item>
          </div>
        </section>
        <section class="medical-edit-section medical-edit-section-wide">
          <el-form-item label="康复内容">
            <el-input v-model="form.content" type="textarea" :rows="6" placeholder="填写康复目标、执行方式、注意事项和观察重点" />
          </el-form-item>
        </section>
        <section class="medical-edit-section medical-edit-section-wide">
          <div class="rehab-order-head">
            <strong>处方列表</strong>
            <el-button text type="success" :icon="Plus" @click="addOrder">添加处方</el-button>
          </div>
          <div v-if="form.orders.length" class="rehab-order-list">
            <div v-for="(order, index) in form.orders" :key="order.key" class="rehab-order-row">
              <el-select
                v-model="order.itemId"
                filterable
                remote
                :remote-method="searchItems"
                :loading="loadingItems"
                placeholder="搜索物资或药品"
                @change="syncOrderItem(order)"
              >
                <el-option v-for="item in itemOptions" :key="item.id" :label="item.name" :value="item.id">
                  <span>{{ item.name }}</span>
                  <span class="rehab-order-option">{{ item.unit || '' }}</span>
                </el-option>
              </el-select>
              <el-select v-model="order.type" placeholder="类型">
                <el-option v-for="item in orderTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
              <el-input v-model="order.count" placeholder="数量" />
              <el-input v-model="order.unit" placeholder="单位" />
              <el-input v-model="order.price" placeholder="价格" />
              <el-button text type="danger" :icon="Delete" @click="removeOrder(index)">删除</el-button>
            </div>
          </div>
          <el-empty v-else description="暂无处方" />
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
import { ArrowLeft, Delete, Plus } from '@element-plus/icons-vue'
import { createRehabPlan, getItems } from '../api/services'

const route = useRoute()
const router = useRouter()
const saving = ref(false)
const loadingItems = ref(false)
const petName = ref('')
const itemOptions = ref([])
const form = reactive({
  age: 0,
  title: '',
  content: '',
  frequency: '',
  startTime: '',
  endTime: '',
  orders: [],
})

const orderTypeOptions = [
  { label: '药品', value: 'MEDICINE' },
  { label: '手术', value: 'SURGERY' },
  { label: '检查', value: 'EXAMINATION' },
  { label: '其他', value: 'OTHER' },
]

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
  const invalidOrder = form.orders.some((order) => !order.itemId || !order.count || !order.unit || !order.price)
  if (invalidOrder) return '请完整填写处方信息'
  return ''
}

function addOrder() {
  form.orders.push({
    key: `${Date.now()}-${Math.random()}`,
    itemId: '',
    type: 'MEDICINE',
    count: '1',
    unit: '',
    price: '0',
  })
}

function removeOrder(index) {
  form.orders.splice(index, 1)
}

function syncOrderItem(order) {
  const item = itemOptions.value.find((option) => String(option.id) === String(order.itemId))
  if (item?.unit && !order.unit) {
    order.unit = item.unit
  }
}

async function searchItems(keyword) {
  const text = String(keyword || '').trim()
  if (!text) return
  loadingItems.value = true
  try {
    const result = await getItems({ keyword: text, page: 1, size: 20 })
    itemOptions.value = Array.isArray(result?.records) ? result.records.filter((item) => !item.discard) : []
  } catch (error) {
    ElMessage.warning(error?.message || '搜索物资失败')
  } finally {
    loadingItems.value = false
  }
}

function buildOrders() {
  return form.orders.map((order) => ({
    itemId: order.itemId,
    type: order.type,
    count: String(order.count),
    unit: String(order.unit).trim(),
    price: String(order.price),
  }))
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
      startTime: form.startTime,
      endTime: form.endTime,
      orders: buildOrders(),
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
  searchItems('药')
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

.rehab-plan-time-row {
  display: grid;
  grid-template-columns: minmax(150px, 0.7fr) minmax(220px, 1fr) minmax(220px, 1fr);
  gap: 12px;
}

.rehab-order-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.rehab-order-head strong {
  color: #5d3927;
  font-size: 15px;
}

.rehab-order-list {
  display: grid;
  gap: 10px;
}

.rehab-order-row {
  display: grid;
  grid-template-columns: minmax(180px, 1.5fr) 120px 90px 90px 90px auto;
  gap: 10px;
  align-items: center;
}

.rehab-order-option {
  float: right;
  color: var(--muted);
  font-size: 12px;
}

@media (max-width: 980px) {
  .rehab-plan-time-row,
  .rehab-order-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .rehab-plan-time-row {
    grid-template-columns: 1fr;
  }
}
</style>
