<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>物资余量</strong>
        <span>库存批次、出入库操作、物资分类和预警</span>
        <div class="profile-actions">
          <el-button class="warm-btn" :icon="RefreshRight" :loading="stockLoading" @click="handleRefresh">刷新</el-button>
        </div>
      </div>
    </template>

    <el-tabs v-model="activeTab" class="item-stock-tabs">
<el-tab-pane label="库存余量" name="stocks">
        <section class="pet-admin-section">
        <el-table :data="filteredStocks" v-loading="stockLoading" class="user-admin-table">
          <el-table-column label="物资" min-width="140">
            <template #header>
              <TableFilterHeader label="物资" :filter="filters.itemName" type="text" :active="isActive('itemName')" />
            </template>
            <template #default="{ row }">{{ row.itemName }}</template>
          </el-table-column>
          <el-table-column label="分类" min-width="120">
            <template #header>
              <TableFilterHeader label="分类" :filter="filters.categoryName" type="text" :active="isActive('categoryName')" />
            </template>
            <template #default="{ row }">{{ row.categoryName }}</template>
          </el-table-column>
          <el-table-column label="余量" width="120">
            <template #header>
              <TableFilterHeader label="余量" :filter="filters.count" type="number" :active="isActive('count')" />
            </template>
            <template #default="{ row }">{{ row.count }}{{ row.unit || '' }}</template>
          </el-table-column>
          <el-table-column label="来源" width="100">
            <template #header>
              <TableFilterHeader label="来源" :filter="filters.sourceType" type="enum" :options="sourceTypeOptions" :active="isActive('sourceType')" />
            </template>
            <template #default="{ row }">{{ sourceText(row.sourceType) }}</template>
          </el-table-column>
          <el-table-column label="有效期" min-width="150">
            <template #header>
              <TableFilterHeader label="有效期" :filter="filters.expireTime" type="time" :active="isActive('expireTime')" />
            </template>
            <template #default="{ row }">{{ formatDate(row.expireTime) }}</template>
          </el-table-column>
          <el-table-column width="40" class-name="action-col">
            <template #header>
              <TableActionColumnHeader title="操作" :collapsed="stockActionCollapsed" @toggle="stockActionCollapsed = !stockActionCollapsed" />
            </template>
            <template #default="{ row }">
              <div class="table-action-cell">
                <div class="table-action-panel" :class="{ 'is-collapsed': stockActionCollapsed }">
                  <el-button text type="success" @click="openStockDialog(row, 'IN')">入库</el-button>
                  <el-button text type="warning" @click="openStockDialog(row, 'OUT')">出库</el-button>
                  <el-button text type="danger" @click="openStockDialog(row, 'DESTROY')">销毁</el-button>
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="user-admin-pagination">
          <el-pagination layout="prev, pager, next, total" :current-page="stockPage.page" :page-size="stockPage.size" :total="stockTotal" @current-change="changeStockPage" />
        </div>
        </section>
      </el-tab-pane>

      <el-tab-pane label="物资类型" name="catalog">
        <section class="pet-admin-section">
          <div class="item-stock-block-head">
            <strong>物资信息</strong>
            <el-button class="soft-btn" :icon="Plus" @click="openItemDialog()">添加物资</el-button>
          </div>
          <el-table :data="filteredItems" v-loading="itemLoading" class="user-admin-table">
            <el-table-column label="名称" min-width="140">
              <template #header>
                <TableFilterHeader label="名称" :filter="itemFilters.name" type="text" :active="isItemActive('name')" />
              </template>
              <template #default="{ row }">{{ row.name }}</template>
            </el-table-column>
            <el-table-column label="分类" min-width="120">
              <template #header>
                <TableFilterHeader label="分类" :filter="itemFilters.categoryName" type="text" :active="isItemActive('categoryName')" />
              </template>
              <template #default="{ row }">{{ row.categoryName }}</template>
            </el-table-column>
            <el-table-column label="单位" width="80">
              <template #default="{ row }">{{ row.unit }}</template>
            </el-table-column>
            <el-table-column width="120">
              <template #default="{ row }">
                <el-button text type="primary" @click="openItemDialog(row)">编辑</el-button>
                <el-button text type="danger" @click="removeItem(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </section>

        <section class="pet-admin-section" style="margin-top: 20px">
          <div class="item-stock-block-head">
            <strong>物资分类</strong>
            <el-button class="soft-btn" :icon="Plus" @click="openCategoryDialog()">添加分类</el-button>
          </div>
          <el-table :data="filteredCategories" v-loading="categoryLoading" class="user-admin-table">
            <el-table-column label="分类" min-width="120">
              <template #header>
                <TableFilterHeader label="分类" :filter="categoryFilters.name" type="text" :active="isCategoryActive('name')" />
              </template>
              <template #default="{ row }">{{ row.name }}</template>
            </el-table-column>
            <el-table-column label="说明" min-width="150" show-overflow-tooltip>
              <template #default="{ row }">{{ row.description || '' }}</template>
            </el-table-column>
            <el-table-column width="120">
              <template #default="{ row }">
                <el-button text type="primary" @click="openCategoryDialog(row)">编辑</el-button>
                <el-button text type="danger" @click="removeCategory(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </section>
      </el-tab-pane>

      <el-tab-pane label="库存预警" name="subscribes">
        <section class="pet-admin-section">
        <div class="item-stock-block-head">
          <strong>预警订阅</strong>
          <el-button class="soft-btn" :icon="Plus" @click="openSubscribeDialog">添加预警</el-button>
        </div>
        <el-table :data="filteredSubscribes" v-loading="subscribeLoading" class="user-admin-table">
          <el-table-column label="预警类型" min-width="150">
            <template #header>
              <TableFilterHeader label="预警类型" :filter="subscribeFilters.action" type="enum" :options="subscribeActionOptions" :active="isSubscribeActive('action')" />
            </template>
            <template #default="{ row }">{{ subscribeActionText(row.action) }}</template>
          </el-table-column>
          <el-table-column label="物资" min-width="160">
            <template #default="{ row }">{{ row.itemName || row.username || '' }}</template>
          </el-table-column>
          <el-table-column label="阈值" width="100">
            <template #default="{ row }">{{ row.count || '' }}</template>
          </el-table-column>
          <el-table-column label="创建时间" min-width="150">
            <template #header>
              <TableFilterHeader label="创建时间" :filter="subscribeFilters.createTime" type="time" :active="isSubscribeActive('createTime')" />
            </template>
            <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
          </el-table-column>
          <el-table-column width="90">
            <template #default="{ row }">
              <el-button text type="danger" @click="removeSubscribe(row)">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
        </section>
      </el-tab-pane>
    </el-tabs>
  </el-card>

  <el-dialog v-model="stockDialogVisible" :title="stockActionText(stockForm.action)" width="620px" :close-on-click-modal="false">
    <el-form label-position="top" class="item-stock-form">
      <el-form-item label="物资">
        <el-select v-model="stockForm.itemId" filterable placeholder="选择物资" :disabled="stockForm.action !== 'IN' && Boolean(stockForm.id)">
          <el-option v-for="item in itemOptions" :key="item.id" :label="`${item.name}（${item.unit}）`" :value="String(item.id)" />
        </el-select>
      </el-form-item>
      <el-form-item label="库存批次">
        <el-input v-model="stockForm.id" :disabled="stockForm.action === 'IN'" placeholder="出库/销毁时使用已有批次" />
      </el-form-item>
      <el-form-item label="数量">
        <el-input-number v-model="stockForm.count" :min="0.01" :controls="false" class="full-width-control" />
      </el-form-item>
      <el-form-item label="来源">
        <el-select v-model="stockForm.sourceType" :disabled="stockForm.action !== 'IN'" placeholder="来源">
          <el-option label="捐赠" value="DONATION" />
          <el-option label="采购" value="PURCHASE" />
        </el-select>
      </el-form-item>
      <el-form-item label="有效期">
        <el-date-picker v-model="stockForm.expireTime" :disabled="stockForm.action !== 'IN'" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择有效期" />
      </el-form-item>
      <el-form-item label="用途/说明">
        <el-input v-model="stockForm.purpose" placeholder="例如：捐赠入库、犬舍消耗、过期销毁" />
      </el-form-item>
      <el-form-item v-if="stockForm.sourceType === 'PURCHASE' && stockForm.action === 'IN'" label="单价">
        <el-input v-model="stockForm.price" placeholder="采购单价" />
      </el-form-item>
      <el-form-item v-if="stockForm.sourceType === 'PURCHASE' && stockForm.action === 'IN'" label="总价">
        <el-input v-model="stockForm.totalPrice" placeholder="采购总价" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="stockDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" :loading="savingStock" @click="submitStockRecord">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="itemDialogVisible" :title="itemForm.id ? '编辑物资' : '添加物资'" width="520px">
    <el-form label-position="top">
      <el-form-item label="名称"><el-input v-model="itemForm.name" /></el-form-item>
      <el-form-item label="分类">
        <el-select v-model="itemForm.categoryId" filterable placeholder="选择分类">
          <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="String(item.id)" />
        </el-select>
      </el-form-item>
      <el-form-item label="单位"><el-input v-model="itemForm.unit" /></el-form-item>
      <el-form-item label="说明"><el-input v-model="itemForm.description" type="textarea" :rows="3" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="itemDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" :loading="savingItem" @click="submitItem">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="categoryDialogVisible" :title="categoryForm.id ? '编辑分类' : '添加分类'" width="520px">
    <el-form label-position="top">
      <el-form-item label="分类名称"><el-input v-model="categoryForm.name" /></el-form-item>
      <el-form-item label="说明"><el-input v-model="categoryForm.description" type="textarea" :rows="3" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="categoryDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" :loading="savingCategory" @click="submitCategory">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="subscribeDialogVisible" title="添加库存预警" width="520px">
    <el-form label-position="top">
      <el-form-item label="预警类型">
        <el-select v-model="subscribeForm.action">
          <el-option label="物资变动通知" value="ITEM_CHANGE" />
          <el-option label="低库存预警" value="ITEM_COUNT" />
        </el-select>
      </el-form-item>
      <el-form-item label="物资">
        <el-select v-model="subscribeForm.elementId" filterable placeholder="选择物资">
          <el-option v-for="item in itemOptions" :key="item.id" :label="item.name" :value="String(item.id)" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="subscribeForm.action === 'ITEM_COUNT'" label="阈值">
        <el-input v-model="subscribeForm.count" placeholder="低于该数量时提醒" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="subscribeDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" :loading="savingSubscribe" @click="submitSubscribe">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, RefreshRight } from '@element-plus/icons-vue'
import {
  cancelSubscribe,
  createCategory,
  createItem,
  createStockRecord,
  createSubscribe,
  discardCategory,
  discardItem,
  getCategories,
  getDonation,
  getItems,
  getStocks,
  getSubscribes,
  updateCategory,
  updateItem,
} from '../api/services'
import TableActionColumnHeader from './TableActionColumnHeader.vue'
import TableFilterHeader from './TableFilterHeader.vue'
import { useTableFilters } from '../composables/useTableFilters'

const route = useRoute()
const router = useRouter()
const activeTab = ref('stocks')
const stockLoading = ref(false)
const itemLoading = ref(false)
const categoryLoading = ref(false)
const subscribeLoading = ref(false)
const savingStock = ref(false)
const savingItem = ref(false)
const savingCategory = ref(false)
const savingSubscribe = ref(false)
const stockActionCollapsed = ref(false)
const stocks = ref([])
const items = ref([])
const itemOptions = ref([])
const categories = ref([])
const subscribes = ref([])
const stockTotal = ref(0)
const stockDialogVisible = ref(false)
const itemDialogVisible = ref(false)
const categoryDialogVisible = ref(false)
const subscribeDialogVisible = ref(false)
const stockPage = reactive({ page: 1, size: 10 })
const stockForm = reactive(resetStockForm())
const itemForm = reactive({ id: '', name: '', categoryId: '', unit: '', description: '' })
const categoryForm = reactive({ id: '', name: '', description: '' })
const subscribeForm = reactive({ action: 'ITEM_COUNT', elementId: '', count: '' })

const { filters, isActive, applyFilter } = useTableFilters({
  itemName: { type: 'text' },
  categoryName: { type: 'text' },
  expireTime: { type: 'time' },
  sourceType: { type: 'enum' },
  count: { type: 'number' },
})

const sourceTypeOptions = [
  { value: 'DONATION', label: '捐赠' },
  { value: 'PURCHASE', label: '采购' },
]

const filteredStocks = computed(() => applyFilter(stocks.value || []))

const { filters: itemFilters, isActive: isItemActive, applyFilter: applyItemFilter } = useTableFilters({
  name: { type: 'text' },
  categoryName: { type: 'text' },
})

const filteredItems = computed(() => applyItemFilter(items.value || []))

const { filters: categoryFilters, isActive: isCategoryActive, applyFilter: applyCategoryFilter } = useTableFilters({
  name: { type: 'text' },
})

const filteredCategories = computed(() => applyCategoryFilter(categories.value || []))

const { filters: subscribeFilters, isActive: isSubscribeActive, applyFilter: applySubscribeFilter } = useTableFilters({
  action: { type: 'enum' },
  createTime: { type: 'time' },
})

const subscribeActionOptions = [
  { value: 'ITEM_CHANGE', label: '物资变动通知' },
  { value: 'ITEM_COUNT', label: '低库存预警' },
  { value: 'IN_STOCK', label: '入库通知' },
  { value: 'OUT_STOCK', label: '出库通知' },
  { value: 'DONATE', label: '捐赠通知' },
]

const filteredSubscribes = computed(() => applySubscribeFilter(subscribes.value || []))

function resetStockForm() {
  return {
    id: '',
    itemId: '',
    count: 1,
    action: 'IN',
    sourceType: 'DONATION',
    expireTime: '',
    purpose: '',
    price: '',
    totalPrice: '',
  }
}

function assignStockForm(values = {}) {
  Object.assign(stockForm, resetStockForm(), values)
}

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function sourceText(value) {
  return { DONATION: '捐赠', PURCHASE: '采购' }[value] || value || ''
}

function stockActionText(value) {
  return { IN: '入库登记', OUT: '出库登记', DESTROY: '销毁登记' }[value] || value || ''
}

function subscribeActionText(value) {
  return { ITEM_CHANGE: '物资变动通知', ITEM_COUNT: '低库存预警', IN_STOCK: '入库通知', OUT_STOCK: '出库通知', DONATE: '捐赠通知' }[value] || value || ''
}

function stockRecordSummary(row) {
  const record = row?.records?.[0]
  if (!record) return ''
  return `${stockActionText(record.action)} ${record.count || ''} · ${record.purpose || ''}`
}

function buildStockQuery() {
  return {
    page: stockPage.page,
    size: stockPage.size,
  }
}

async function loadStocks() {
  stockLoading.value = true
  try {
    const result = await getStocks(buildStockQuery())
    stocks.value = Array.isArray(result?.records) ? result.records : []
    stockTotal.value = Number(result?.total || stocks.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载库存余量失败')
  } finally {
    stockLoading.value = false
  }
}

function handleRefresh() {
  stockPage.page = 1
  loadStocks()
}

function changeStockPage(value) {
  stockPage.page = value
  loadStocks()
}

async function loadCategories() {
  categoryLoading.value = true
  try {
    const result = await getCategories({ size: 200 })
    categories.value = Array.isArray(result?.records) ? result.records : []
  } catch (error) {
    ElMessage.warning(error?.message || '加载物资分类失败')
  } finally {
    categoryLoading.value = false
  }
}

async function handleRefreshItems() {
  itemLoading.value = true
  try {
    const query = { size: 200 }
    if (categories.value.length) {
      query.category = categories.value.map((item) => String(item.id))
    }
    const result = await getItems(query)
    items.value = Array.isArray(result?.records) ? result.records : []
    itemOptions.value = items.value.filter((item) => !item.discard)
  } catch (error) {
    ElMessage.warning(error?.message || '加载物资失败')
  } finally {
    itemLoading.value = false
  }
}

async function loadSubscribes() {
  subscribeLoading.value = true
  try {
    const result = await getSubscribes({ size: 200 })
    subscribes.value = Array.isArray(result?.records) ? result.records : []
  } catch (error) {
    ElMessage.warning(error?.message || '加载预警失败')
  } finally {
    subscribeLoading.value = false
  }
}

function openStockDialog(row = null, action = 'IN') {
  assignStockForm({
    id: action === 'IN' ? '' : String(row?.id || ''),
    itemId: row?.itemId ? String(row.itemId) : '',
    action,
    sourceType: row?.sourceType || 'DONATION',
    expireTime: action === 'IN' ? (row?.expireTime || '') : '',
    purpose: action === 'IN' ? '物资入库' : action === 'OUT' ? '物资出库' : '物资销毁',
  })
  stockDialogVisible.value = true
}

async function submitStockRecord() {
  if (!stockForm.itemId || !stockForm.count || !stockForm.purpose.trim()) {
    ElMessage.warning('请补全库存登记信息')
    return
  }
  if (stockForm.action !== 'IN' && !stockForm.id) {
    ElMessage.warning('请选择库存批次')
    return
  }
  savingStock.value = true
  try {
    await createStockRecord({
      id: stockForm.id || undefined,
      itemId: stockForm.itemId,
      count: String(stockForm.count),
      action: stockForm.action,
      sourceType: stockForm.sourceType,
      expireTime: stockForm.expireTime || undefined,
      purpose: stockForm.purpose.trim(),
      price: stockForm.price || undefined,
      totalPrice: stockForm.totalPrice || undefined,
    })
    ElMessage.success('库存记录已保存')
    stockDialogVisible.value = false
    await loadStocks()
  } catch (error) {
    ElMessage.warning(error?.message || '保存库存记录失败')
  } finally {
    savingStock.value = false
  }
}

function openItemDialog(row = null) {
  Object.assign(itemForm, {
    id: row?.id ? String(row.id) : '',
    name: row?.name || '',
    categoryId: row?.categoryId ? String(row.categoryId) : '',
    unit: row?.unit || '',
    description: row?.description || '',
  })
  itemDialogVisible.value = true
}

async function submitItem() {
  if (!itemForm.name.trim() || !itemForm.categoryId || !itemForm.unit.trim()) {
    ElMessage.warning('请补全物资信息')
    return
  }
  savingItem.value = true
  try {
    const payload = {
      name: itemForm.name.trim(),
      categoryId: itemForm.categoryId,
      unit: itemForm.unit.trim(),
      description: itemForm.description.trim(),
    }
    if (itemForm.id) await updateItem(itemForm.id, payload)
    else await createItem(payload)
    ElMessage.success('物资信息已保存')
    itemDialogVisible.value = false
    await handleRefreshItems()
  } catch (error) {
    ElMessage.warning(error?.message || '保存物资失败')
  } finally {
    savingItem.value = false
  }
}

async function removeItem(row) {
  try {
    await ElMessageBox.confirm(`确认删除物资「${row.name}」？`, '删除物资', { type: 'warning' })
    await discardItem(row.id)
    ElMessage.success('物资已删除')
    await handleRefreshItems()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.warning(error?.message || '删除物资失败')
  }
}

function openCategoryDialog(row = null) {
  Object.assign(categoryForm, {
    id: row?.id ? String(row.id) : '',
    name: row?.name || '',
    description: row?.description || '',
  })
  categoryDialogVisible.value = true
}

async function submitCategory() {
  if (!categoryForm.name.trim()) {
    ElMessage.warning('请填写分类名称')
    return
  }
  savingCategory.value = true
  try {
    const payload = { name: categoryForm.name.trim(), description: categoryForm.description.trim() }
    if (categoryForm.id) await updateCategory(categoryForm.id, payload)
    else await createCategory(payload)
    ElMessage.success('分类已保存')
    categoryDialogVisible.value = false
    await loadCategories()
    await handleRefreshItems()
  } catch (error) {
    ElMessage.warning(error?.message || '保存分类失败')
  } finally {
    savingCategory.value = false
  }
}

async function removeCategory(row) {
  try {
    await ElMessageBox.confirm(`确认删除分类「${row.name}」？`, '删除分类', { type: 'warning' })
    await discardCategory(row.id)
    ElMessage.success('分类已删除')
    await loadCategories()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.warning(error?.message || '删除分类失败')
  }
}

function openSubscribeDialog() {
  Object.assign(subscribeForm, { action: 'ITEM_COUNT', elementId: '', count: '' })
  subscribeDialogVisible.value = true
}

async function submitSubscribe() {
  if (!subscribeForm.action || !subscribeForm.elementId || (subscribeForm.action === 'ITEM_COUNT' && !subscribeForm.count)) {
    ElMessage.warning('请补全预警信息')
    return
  }
  savingSubscribe.value = true
  try {
    await createSubscribe({
      action: subscribeForm.action,
      elementId: subscribeForm.elementId,
      count: subscribeForm.action === 'ITEM_COUNT' ? subscribeForm.count : '0',
    })
    ElMessage.success('预警已添加')
    subscribeDialogVisible.value = false
    await loadSubscribes()
  } catch (error) {
    ElMessage.warning(error?.message || '添加预警失败')
  } finally {
    savingSubscribe.value = false
  }
}

async function removeSubscribe(row) {
  try {
    await cancelSubscribe(row.id)
    ElMessage.success('预警已取消')
    await loadSubscribes()
  } catch (error) {
    ElMessage.warning(error?.message || '取消预警失败')
  }
}

async function prefillDonationStock() {
  const donationId = route.query.donationId
  if (!donationId) return
  try {
    const donation = await getDonation(donationId)
    const item = donation?.items?.[0]
    assignStockForm({
      action: 'IN',
      itemId: item?.itemId ? String(item.itemId) : '',
      count: item?.count ? Number(item.count) : 1,
      sourceType: 'DONATION',
      purpose: `捐赠 #${donationId} 入库`,
    })
    stockDialogVisible.value = true
  } catch {
    ElMessage.warning('未能读取捐赠入库信息，请手动登记')
  }
}

watch(activeTab, (value) => {
  router.replace({ query: { ...route.query, tab: value === 'stocks' ? undefined : value } })
  if (value === 'catalog') {
    loadCategories()
    handleRefreshItems()
  } else if (value === 'subscribes') {
    loadSubscribes()
  }
})

onMounted(async () => {
  const tab = route.query.tab
  if (tab && ['stocks', 'catalog', 'subscribes'].includes(tab)) {
    activeTab.value = tab
  }
  await loadCategories()
  await handleRefreshItems()
  await loadStocks()
  await prefillDonationStock()
})
</script>

<style scoped>
.item-stock-tabs {
  --el-color-primary: #b56b35;
}

.item-stock-filter-row {
  grid-template-columns: minmax(160px, 1fr) minmax(140px, 0.8fr) auto;
}

.item-stock-name {
  display: block;
  color: #5d3927;
}

.item-stock-subtext {
  display: block;
  margin-top: 3px;
  color: var(--muted);
  font-size: 12px;
}

.item-stock-two-col {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.8fr);
  gap: 18px;
}

.item-stock-block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.item-stock-block-head strong {
  color: #5d3927;
}

.item-stock-inline-filter {
  margin-bottom: 12px;
}

.item-stock-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.item-stock-form .el-form-item:nth-child(6) {
  grid-column: 1 / -1;
}

@media (max-width: 900px) {
  .item-stock-two-col,
  .item-stock-form,
  .item-stock-filter-row,
  .item-stock-inline-filter {
    grid-template-columns: 1fr;
  }
}

.item-stock-filter-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
</style>
