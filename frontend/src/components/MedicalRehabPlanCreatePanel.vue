<template>
  <el-card class="profile-card">
    <template #header>
      <div class="profile-card-header">
        <strong>创建康复计划</strong>
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回</el-button>
      </div>
    </template>

    <section class="action-form-panel">
      <div class="rehab-create-head">
        <div>
          <h2>{{ petName || '康复计划' }}</h2>
        </div>
      </div>

      <el-form label-position="top" class="medical-detail-edit-form rehab-create-form">
        <section class="medical-edit-section">
          <el-form-item label="计划标题">
            <el-input v-model="form.title" />
          </el-form-item>
        </section>
        <section class="medical-edit-section">
          <el-form-item label="执行频率">
            <el-input v-model="form.frequency" />
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
            <el-input v-model="form.content" type="textarea" :rows="6" />
          </el-form-item>
        </section>
        <section class="medical-edit-section medical-edit-section-wide">
          <div class="rehab-order-head">
            <strong>处方列表</strong>
            <el-button text type="success" :icon="Plus" @click="openAddOrderDialog">添加处方</el-button>
          </div>
          <div v-if="form.orders.length" class="rehab-order-list">
            <div v-for="(order, index) in form.orders" :key="order.key" class="rehab-order-card">
              <div class="rehab-order-card-info">
                <strong>{{ order.itemName || '物资' }}</strong>
                <span class="rehab-order-card-meta">{{ orderTypeText(order.type) }} · {{ order.count }}{{ order.unit }} · ¥{{ order.price }}</span>
              </div>
              <el-button text type="danger" :icon="Delete" @click="removeOrder(index)" />
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

  <el-dialog v-model="orderDialogVisible" title="添加处方" width="700px" :close-on-click-modal="false" destroy-on-close>
    <div v-if="orderStep === 1" class="rehab-order-step1">
      <div class="rehab-order-category-col">
        <h4>物资分类</h4>
        <div class="rehab-order-category-list">
          <div
            v-for="cat in categories"
            :key="cat.id"
            class="rehab-order-category-item"
            :class="{ 'is-selected': selectedCategoryId === String(cat.id) }"
            @click="selectCategory(cat)"
          >
            {{ cat.name }}
          </div>
        </div>
      </div>
      <div class="rehab-order-item-col">
        <h4>选择物品</h4>
        <el-input v-model="itemSearchKeyword" placeholder="搜索物品..." clearable class="rehab-order-item-search" />
        <div class="rehab-order-item-list">
          <div
            v-for="item in filteredCategoryItems"
            :key="item.id"
            class="rehab-order-item-row"
            :class="{ 'is-selected': selectedItem?.id === item.id }"
            @click="selectedItem = item"
          >
            <strong>{{ item.name }}</strong>
            <span class="rehab-order-item-unit">{{ item.unit || '' }}</span>
          </div>
          <el-empty v-if="!filteredCategoryItems.length" description="该分类暂无物资" :image-size="60" />
        </div>
      </div>
    </div>
    <div v-if="orderStep === 2" class="rehab-order-step2">
      <div class="rehab-order-selected-item">
        <strong>{{ selectedItem?.name }}</strong>
        <span>{{ selectedItem?.unit || '' }}</span>
      </div>
      <el-form label-position="top" class="rehab-order-step2-form">
        <el-form-item label="处方类型" class="rehab-order-full-row">
          <el-select v-model="orderDraft.type">
            <el-option v-for="opt in orderTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量">
          <el-input v-model="orderDraft.count" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input v-model="orderDraft.price" />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button v-if="orderStep === 2" @click="orderStep = 1">上一步</el-button>
      <el-button v-if="orderStep === 1" :disabled="!selectedItem" class="warm-btn" @click="orderStep = 2">下一步</el-button>
      <el-button v-if="orderStep === 2" class="warm-btn" @click="confirmAddOrder">确认添加</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Delete, Plus } from '@element-plus/icons-vue'
import { createRehabPlan } from '../api/medical'
import { getCategories, getItems } from '../api/inventory'

const route = useRoute()
const router = useRouter()
const saving = ref(false)
const petName = ref('')
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

function orderTypeText(value) {
  return orderTypeOptions.find((o) => o.value === value)?.label || value || ''
}

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
  const invalidOrder = form.orders.some((order) => !order.itemId || !order.count || !order.price)
  if (invalidOrder) return '请完整填写处方信息'
  return ''
}

function removeOrder(index) {
  form.orders.splice(index, 1)
}

function buildOrders() {
  return form.orders.map((order) => ({
    itemId: order.itemId,
    type: order.type,
    count: String(order.count),
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

const orderDialogVisible = ref(false)
const orderStep = ref(1)
const categories = ref([])
const selectedCategoryId = ref('')
const selectedItem = ref(null)
const itemSearchKeyword = ref('')
const categoryItems = ref([])
const orderDraft = reactive({
  type: 'MEDICINE',
  count: '1',
  price: '0',
})

const filteredCategoryItems = computed(() => {
  const items = categoryItems.value
  const keyword = itemSearchKeyword.value.trim().toLowerCase()
  if (!keyword) return items
  return items.filter((item) => item.name?.toLowerCase().includes(keyword))
})

async function openAddOrderDialog() {
  orderStep.value = 1
  selectedCategoryId.value = ''
  selectedItem.value = null
  categoryItems.value = []
  itemSearchKeyword.value = ''
  orderDraft.type = 'MEDICINE'
  orderDraft.count = '1'
  orderDraft.price = '0'
  if (!categories.value.length) {
    try {
      const result = await getCategories({ size: 200 })
      categories.value = Array.isArray(result?.records) ? result.records : []
    } catch {
      categories.value = []
    }
  }
  orderDialogVisible.value = true
}

async function selectCategory(cat) {
  selectedCategoryId.value = String(cat.id)
  selectedItem.value = null
  itemSearchKeyword.value = ''
  try {
    const result = await getItems({ size: 200, category: [String(cat.id)] })
    categoryItems.value = Array.isArray(result?.records) ? result.records.filter((item) => !item.discard) : []
  } catch {
    categoryItems.value = []
  }
}

function confirmAddOrder() {
  if (!selectedItem.value) {
    ElMessage.warning('请选择物品')
    return
  }
  const item = selectedItem.value
  if (!orderDraft.count.trim()) {
    ElMessage.warning('请输入数量')
    return
  }
  form.orders.push({
    key: `${Date.now()}-${Math.random()}`,
    itemId: item.id,
    itemName: item.name,
    type: orderDraft.type,
    count: orderDraft.count.trim(),
    unit: item.unit || '',
    price: orderDraft.price.trim() || '0',
  })
  orderDialogVisible.value = false
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

.rehab-create-form {
  border-top: 1px solid var(--line);
  padding-top: 16px;
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
  gap: 8px;
}

.rehab-order-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border: 1px solid rgba(179, 124, 82, 0.2);
  border-radius: 8px;
  background: rgba(255, 252, 247, 0.7);
}

.rehab-order-card-info {
  display: grid;
  gap: 3px;
}

.rehab-order-card-info strong {
  color: #5d3927;
  font-size: 14px;
}

.rehab-order-card-meta {
  color: var(--muted);
  font-size: 12px;
}

.rehab-order-step1 {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 16px;
  min-height: 360px;
}

.rehab-order-category-col h4,
.rehab-order-item-col h4 {
  margin: 0 0 8px;
  color: #5d3927;
  font-size: 14px;
}

.rehab-order-category-list {
  display: grid;
  gap: 4px;
  max-height: 320px;
  overflow-y: auto;
}

.rehab-order-category-item {
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: #3f2a1f;
  transition: all 0.15s;
}

.rehab-order-category-item:hover {
  background: rgba(179, 124, 82, 0.1);
}

.rehab-order-category-item.is-selected {
  background: #e77a3b;
  color: #fff;
  font-weight: 600;
}

.rehab-order-item-search {
  margin-bottom: 8px;
}

.rehab-order-item-list {
  display: grid;
  gap: 4px;
  max-height: 280px;
  overflow-y: auto;
}

.rehab-order-item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: #3f2a1f;
  transition: all 0.15s;
}

.rehab-order-item-row:hover {
  background: rgba(179, 124, 82, 0.1);
}

.rehab-order-item-row.is-selected {
  background: #e77a3b;
  color: #fff;
  font-weight: 600;
}

.rehab-order-item-row.is-selected .rehab-order-item-unit {
  color: rgba(255, 255, 255, 0.8);
}

.rehab-order-item-unit {
  color: var(--muted);
  font-size: 12px;
}

.rehab-order-step2 {
  display: grid;
  gap: 16px;
}

.rehab-order-selected-item {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 12px 16px;
  border: 1px solid rgba(179, 124, 82, 0.22);
  border-radius: 8px;
  background: #fffaf3;
}

.rehab-order-selected-item strong {
  color: #5d3927;
  font-size: 15px;
}

.rehab-order-selected-item span {
  color: var(--muted);
  font-size: 13px;
}

.rehab-order-step2-form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.rehab-order-full-row {
  grid-column: 1 / -1;
}

@media (max-width: 768px) {
  .rehab-order-step1 {
    grid-template-columns: 1fr;
  }

  .rehab-order-step2-form {
    grid-template-columns: 1fr;
  }

  .rehab-plan-time-row {
    grid-template-columns: 1fr;
  }
}
</style>
