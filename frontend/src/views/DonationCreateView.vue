<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check, Delete, Plus, Upload } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { beginDonation, createDonation, deleteDonationFile, getCategories, getItems, uploadDonationFile } from '../api/inventory'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const router = useRouter()
const formRef = ref(null)
const fileInputRef = ref(null)
const submitting = ref(false)
const uploading = ref(false)
const categories = ref([])
const donationUuid = ref('')
const uploadedFiles = ref([])

const form = reactive({
  items: [createBlankItem()],
  delivery: 'ADDRESS',
  address: '',
  trackingNumber: '',
  description: '',
})

const itemsByCategory = ref({})

const rules = {
  delivery: [{ required: true, message: '请选择交付方式', trigger: 'change' }],
}

const deliveryOptions = [
  { label: '定点取货', value: 'ADDRESS' },
  { label: '快递寄送', value: 'EXPRESS' },
  { label: '到站面交', value: 'FACE' },
  { label: '其他方式', value: 'OTHER' },
]

const requiresAddress = computed(() => form.delivery === 'ADDRESS')
const requiresTracking = computed(() => form.delivery === 'EXPRESS')

function createBlankItem() {
  return {
    itemId: null,
    itemName: '',
    itemUnit: '件',
    categoryId: '',
    count: 1,
    expireTime: '',
    description: '',
  }
}

function goBack() {
  router.push('/')
}

async function loadCategories() {
  try {
    const result = await getCategories({ size: 50 })
    categories.value = Array.isArray(result?.records) ? result.records : []
  } catch {
    categories.value = []
  }
}

async function loadItemsByCategory(categoryId) {
  if (!categoryId || itemsByCategory.value[categoryId]) return
  try {
    const result = await getItems({ size: 200, category: [String(categoryId)] })
    itemsByCategory.value[categoryId] = Array.isArray(result?.records) ? result.records : []
  } catch {
    itemsByCategory.value[categoryId] = []
  }
}

function onCategoryChange(item) {
  item.itemId = null
  item.itemName = ''
  item.itemUnit = ''
  loadItemsByCategory(item.categoryId)
}

function onItemNameChange(item, val) {
  const list = itemsByCategory.value[item.categoryId] || []
  const matched = list.find((i) => i.name === val)
  if (matched) {
    item.itemId = matched.id
    item.itemUnit = matched.unit
  } else {
    item.itemId = null
  }
}

function onItemNameClear(item) {
  item.itemId = null
  item.itemUnit = ''
}

async function ensureDonationUuid() {
  if (donationUuid.value) return donationUuid.value
  donationUuid.value = await beginDonation()
  return donationUuid.value
}

function chooseFiles() {
  fileInputRef.value?.click()
}

async function uploadFiles(event) {
  const files = Array.from(event.target.files || [])
  event.target.value = ''
  if (!files.length || uploading.value) return
  uploading.value = true
  try {
    const uuid = await ensureDonationUuid()
    for (const file of files) {
      const filename = await uploadDonationFile(uuid, file)
      uploadedFiles.value.push({ name: filename || file.name, originalName: file.name })
    }
    ElMessage.success('捐赠影像已上传')
  } catch (error) {
    ElMessage.warning(error?.message || '上传捐赠影像失败')
  } finally {
    uploading.value = false
  }
}

async function removeFile(file) {
  if (!donationUuid.value || !file?.name) return
  try {
    await deleteDonationFile(donationUuid.value, file.name)
    uploadedFiles.value = uploadedFiles.value.filter((item) => item !== file)
  } catch (error) {
    ElMessage.warning(error?.message || '删除捐赠影像失败')
  }
}

function addItem() {
  form.items.push(createBlankItem())
}

function removeItem(index) {
  if (form.items.length <= 1) {
    ElMessage.warning('至少保留一项捐赠物资')
    return
  }
  form.items.splice(index, 1)
}

function validateItems() {
  for (let i = 0; i < form.items.length; i++) {
    const item = form.items[i]
    if (!item.categoryId) {
      ElMessage.warning(`请选择第 ${i + 1} 项物资的分类`)
      return false
    }
    if (!item.itemName.trim()) {
      ElMessage.warning(`请输入第 ${i + 1} 项物资名称`)
      return false
    }
    if (!item.itemId && !item.itemUnit.trim()) {
      ElMessage.warning(`请输入第 ${i + 1} 项物资的单位`)
      return false
    }
    if (!item.expireTime) {
      ElMessage.warning(`请选择第 ${i + 1} 项物资的有效期`)
      return false
    }
  }
  return true
}

async function submitForm() {
  if (!formRef.value || submitting.value) {
    return
  }

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  if (requiresAddress.value && !form.address.trim()) {
    ElMessage.warning('请填写取货地址')
    return
  }
  if (requiresTracking.value && !form.trackingNumber.trim()) {
    ElMessage.warning('请填写快递单号')
    return
  }
  if (!validateItems()) return

  submitting.value = true
  try {
    const uuid = await ensureDonationUuid()
    await createDonation({
      uuid,
      delivery: form.delivery,
      address: form.address.trim(),
      trackingNumber: form.trackingNumber.trim(),
      description: form.description.trim(),
      items: form.items.map((item) => {
        const base = {
          name: item.itemName.trim(),
          categoryId: item.categoryId,
          description: item.description.trim() || form.description.trim(),
          count: String(item.count),
          expireTime: item.expireTime,
        }
        if (item.itemId) {
          return { ...base, itemId: item.itemId }
        }
        return { ...base, itemId: null, itemName: item.itemName.trim(), itemUnit: item.itemUnit.trim() }
      }),
    })
    ElMessage.success('物资捐赠已提交')
    router.push('/console/items/donations')
  } catch (error) {
    ElMessage.warning(error?.message || '提交物资捐赠失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadCategories()
  ensureDonationUuid().catch(() => {})
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main action-form-page">
      <section class="action-form-panel">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="action-form-grid">
          <div class="donation-item-list action-form-span-2">
            <div class="form-group-title">
              <span>捐赠物资</span>
              <el-button class="soft-btn" :icon="Plus" @click="addItem">添加物资</el-button>
            </div>
            <section v-for="(item, index) in form.items" :key="index" class="donation-item-card">
              <div class="donation-item-card-head">
                <strong>物资 {{ index + 1 }}</strong>
                <el-button text type="danger" :icon="Delete" @click="removeItem(index)">删除</el-button>
              </div>
              <div class="donation-item-grid">
                <el-form-item label="物资分类">
                  <el-select v-model="item.categoryId" placeholder="先选择分类" filterable clearable @change="onCategoryChange(item)">
                    <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="String(category.id)" />
                  </el-select>
                </el-form-item>
                <el-form-item label="物资名称">
                  <el-select v-model="item.itemName" placeholder="选择或输入物品" filterable allow-create default-first-option clearable :disabled="!item.categoryId" @change="(val) => onItemNameChange(item, val)" @clear="onItemNameClear(item)">
                    <el-option v-for="option in (itemsByCategory[item.categoryId] || [])" :key="option.id" :label="option.name" :value="option.name" />
                  </el-select>
                </el-form-item>
                <el-form-item label="单位">
                  <el-input v-model="item.itemUnit" :readonly="!!item.itemId" :class="{ 'is-readonly-unit': !!item.itemId }" clearable />
                  <span v-if="item.itemId" class="donation-unit-hint">来自已有物品</span>
                </el-form-item>
                <el-form-item label="数量">
                  <el-input-number v-model="item.count" :min="1" :controls="false" />
                </el-form-item>
                <el-form-item label="有效期">
                  <el-date-picker v-model="item.expireTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择有效期" />
                </el-form-item>
                <el-form-item label="物资说明">
                  <el-input v-model="item.description" clearable />
                </el-form-item>
              </div>
            </section>
          </div>
          <el-form-item label="交付方式" prop="delivery">
            <el-select v-model="form.delivery" placeholder="选择交付方式">
              <el-option v-for="item in deliveryOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="取货地址">
            <el-input v-model="form.address" :disabled="!requiresAddress" clearable />
          </el-form-item>
          <el-form-item label="快递单号">
            <el-input v-model="form.trackingNumber" :disabled="!requiresTracking" clearable />
          </el-form-item>
          <el-form-item label="备注" class="action-form-span-2">
            <el-input v-model="form.description" type="textarea" :rows="4" />
          </el-form-item>
          <el-form-item label="捐赠影像" class="action-form-span-2">
            <input ref="fileInputRef" class="profile-avatar-input" type="file" accept="image/*,video/*" multiple @change="uploadFiles" />
            <div class="rescue-image-uploader">
              <el-button class="soft-btn" :icon="Upload" :loading="uploading" @click="chooseFiles">上传图片/视频</el-button>
              <span v-for="file in uploadedFiles" :key="file.name" class="medical-upload-chip">
                {{ file.originalName || file.name }}
                <button type="button" @click="removeFile(file)">删除</button>
              </span>
            </div>
          </el-form-item>
        </el-form>

        <div class="action-form-actions">
          <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">取消</el-button>
          <el-button class="warm-btn" :icon="Check" :loading="submitting" @click="submitForm">提交捐赠</el-button>
        </div>
      </section>
    </main>

    <AppFooter />
  </div>
</template>

<style scoped>
.donation-item-list {
  display: grid;
  gap: 14px;
}

.donation-item-list .form-group-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0;
}

.donation-item-card {
  border: 1px solid rgba(179, 124, 82, 0.22);
  border-radius: 8px;
  padding: 16px;
  background: rgba(255, 252, 247, 0.8);
}

.donation-item-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.donation-item-card-head strong {
  color: #5d3927;
}

.donation-item-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px 16px;
}

@media (max-width: 768px) {
  .donation-item-grid {
    grid-template-columns: 1fr;
  }
}

.donation-unit-hint {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: var(--muted);
}

.is-readonly-unit :deep(.el-input__wrapper) {
  background: rgba(179, 124, 82, 0.06);
}
</style>
