<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { beginDonation, createCategory, createDonation, getCategories } from '../api/services'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const categories = ref([])

const form = reactive({
  itemName: '',
  itemUnit: '件',
  categoryId: '',
  categoryName: '',
  count: 1,
  expireTime: '',
  delivery: 'ADDRESS',
  address: '',
  trackingNumber: '',
  description: '',
})

const rules = {
  itemName: [{ required: true, message: '请输入物资名称', trigger: 'blur' }],
  itemUnit: [{ required: true, message: '请输入单位', trigger: 'blur' }],
  count: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  expireTime: [{ required: true, message: '请选择有效期', trigger: 'change' }],
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

async function resolveCategoryId() {
  if (form.categoryId) {
    return Number(form.categoryId)
  }
  const name = form.categoryName.trim() || '其他物资'
  const matched = categories.value.find((item) => item.name === name)
  if (matched?.id) {
    return Number(matched.id)
  }
  const created = await createCategory({
    name,
    description: `${name}捐赠分类`,
  })
  return Number(created?.id)
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

  submitting.value = true
  try {
    const uuid = await beginDonation()
    const categoryId = await resolveCategoryId()
    await createDonation({
      uuid,
      delivery: form.delivery,
      address: form.address.trim(),
      trackingNumber: form.trackingNumber.trim(),
      description: form.description.trim(),
      items: [
        {
          name: form.itemName.trim(),
          itemId: null,
          itemName: form.itemName.trim(),
          itemUnit: form.itemUnit.trim(),
          categoryId,
          description: form.description.trim(),
          count: String(form.count),
          expireTime: form.expireTime,
        },
      ],
    })
    ElMessage.success('物资捐赠已提交')
    router.push('/services')
  } catch (error) {
    ElMessage.warning(error?.message || '提交物资捐赠失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadCategories()
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
          <p>记录食品、药品、转运用品等捐赠信息，方便工作人员确认交付方式并纳入库存。</p>
        </div>
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回首页</el-button>
      </section>

      <section class="action-form-panel">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="action-form-grid">
          <el-form-item label="物资名称" prop="itemName">
            <el-input v-model="form.itemName" placeholder="例如：幼猫粮、尿垫、航空箱" clearable />
          </el-form-item>
          <el-form-item label="单位" prop="itemUnit">
            <el-input v-model="form.itemUnit" placeholder="袋 / 箱 / 个" clearable />
          </el-form-item>
          <el-form-item label="物资分类">
            <el-select v-model="form.categoryId" placeholder="选择已有分类" filterable clearable>
              <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="String(item.id)" />
            </el-select>
          </el-form-item>
          <el-form-item label="新分类名称">
            <el-input v-model="form.categoryName" :disabled="Boolean(form.categoryId)" placeholder="没有合适分类时填写" clearable />
          </el-form-item>
          <el-form-item label="数量" prop="count">
            <el-input-number v-model="form.count" :min="1" :controls="false" />
          </el-form-item>
          <el-form-item label="有效期" prop="expireTime">
            <el-date-picker v-model="form.expireTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择有效期" />
          </el-form-item>
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
