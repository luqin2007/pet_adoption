<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check, Delete, Plus, Upload } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { beginDonation, createDonation, deleteDonationFile, getCategories, uploadDonationFile } from '../api/services'
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
  const invalidIndex = form.items.findIndex((item) => !item.itemName.trim() || !item.itemUnit.trim() || !item.categoryId || !item.count || !item.expireTime)
  if (invalidIndex >= 0) {
    ElMessage.warning(`请补全第 ${invalidIndex + 1} 项物资信息`)
    return false
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
      items: form.items.map((item) => ({
        name: item.itemName.trim(),
        itemId: null,
        itemName: item.itemName.trim(),
        itemUnit: item.itemUnit.trim(),
        categoryId: item.categoryId,
        description: item.description.trim() || form.description.trim(),
        count: String(item.count),
        expireTime: item.expireTime,
      })),
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
      <section class="action-form-hero">
        <div>
          <span class="hero-chip">物资捐赠</span>
          <h1>登记可捐赠的救助物资</h1>
          <p>捐赠食品、药品或转运用品时，先留下交付方式。</p>
        </div>
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回首页</el-button>
      </section>

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
                <el-form-item label="物资名称">
                  <el-input v-model="item.itemName" placeholder="例如：幼猫粮、尿垫、航空箱" clearable />
                </el-form-item>
                <el-form-item label="单位">
                  <el-input v-model="item.itemUnit" placeholder="袋 / 箱 / 个" clearable />
                </el-form-item>
                <el-form-item label="物资分类">
                  <el-select v-model="item.categoryId" placeholder="选择已有分类" filterable clearable>
                    <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="String(category.id)" />
                  </el-select>
                </el-form-item>
                <el-form-item label="数量">
                  <el-input-number v-model="item.count" :min="1" :controls="false" />
                </el-form-item>
                <el-form-item label="有效期">
                  <el-date-picker v-model="item.expireTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择有效期" />
                </el-form-item>
                <el-form-item label="物资说明">
                  <el-input v-model="item.description" placeholder="新旧程度、规格、包装状态等" clearable />
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
            <el-input v-model="form.address" :disabled="!requiresAddress" placeholder="定点取货时填写" clearable />
          </el-form-item>
          <el-form-item label="快递单号">
            <el-input v-model="form.trackingNumber" :disabled="!requiresTracking" placeholder="快递寄送时填写" clearable />
          </el-form-item>
          <el-form-item label="备注" class="action-form-span-2">
            <el-input v-model="form.description" type="textarea" :rows="4" placeholder="补充物资状态、交付时间、联系方式等" />
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
</style>
