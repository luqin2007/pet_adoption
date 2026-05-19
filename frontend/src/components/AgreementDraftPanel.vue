<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>{{ isPaper ? '创建纸质协议' : '起草电子协议' }}</strong>
      </div>
    </template>

    <section class="action-form-panel agreement-draft-panel" v-loading="loading">
      <div class="agreement-draft-head">
        <div>
          <h2>{{ isPaper ? '纸质协议扫描件' : '电子协议正文' }}</h2>
          <p>{{ targetText }}</p>
        </div>
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回</el-button>
      </div>

      <section v-if="isPaper" class="agreement-paper-section">
        <div class="agreement-toolbar">
          <input ref="fileInputRef" class="profile-avatar-input" type="file" accept="image/*" multiple @change="uploadPaperFiles" />
          <el-button class="soft-btn" :icon="Upload" :loading="uploading" @click="chooseFiles">上传扫描件</el-button>
          <span>{{ paperFiles.length }} 张图片</span>
        </div>

        <div v-if="paperFiles.length" class="agreement-paper-grid">
          <article v-for="(item, index) in paperFiles" :key="item.key" class="agreement-paper-card">
            <img :src="item.previewUrl" :alt="item.name" />
            <div class="agreement-paper-meta">
              <strong>第 {{ index + 1 }} 页</strong>
              <span>{{ item.name }}</span>
            </div>
            <div class="agreement-paper-actions">
              <el-button text :icon="ArrowUp" :disabled="index === 0" @click="movePaperFile(index, -1)">上移</el-button>
              <el-button text :icon="ArrowDown" :disabled="index === paperFiles.length - 1" @click="movePaperFile(index, 1)">下移</el-button>
              <el-button text type="danger" :icon="Delete" @click="removePaperFile(item)">删除</el-button>
            </div>
          </article>
        </div>
        <el-empty v-else description="暂无协议扫描件" />
      </section>

      <section v-else class="agreement-electronic-section">
        <el-form label-position="top">
          <el-form-item label="协议内容">
            <el-input
              v-model="content"
              type="textarea"
              :autosize="{ minRows: 14, maxRows: 22 }"
            />
          </el-form-item>
        </el-form>
      </section>

      <div class="agreement-draft-footer">
        <el-button class="soft-btn" @click="goBack">取消</el-button>
        <el-button class="warm-btn" :icon="Check" :loading="saving" @click="submitAgreement">提交协议</el-button>
      </div>
    </section>
  </el-card>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, ArrowLeft, ArrowUp, Check, Delete, Upload } from '@element-plus/icons-vue'
import {
  addAgreement,
  beginAgreement,
  deleteAgreementWhenAdd,
  uploadAgreementWhenAdd,
} from '../api/services'

const props = defineProps({
  type: {
    type: String,
    default: 'ELECTRONIC',
  },
})

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const uploading = ref(false)
const saving = ref(false)
const uuid = ref('')
const content = ref('')
const paperFiles = ref([])
const fileInputRef = ref(null)

const isPaper = computed(() => props.type === 'PAPER')
const parentId = computed(() => String(route.query.parentId || ''))
const parentType = computed(() => String(route.query.parentType || 'ADOPT'))
const draftSubtitle = computed(() => [route.query.pet, route.query.applicant].filter(Boolean).join(' · ') || '领养协议')
const targetText = computed(() => {
  const pet = route.query.pet || '领养宠物'
  const applicant = route.query.applicant || '申请人'
  return `${pet} · ${applicant}`
})

function goBack() {
  router.push('/console/adoption/adopts')
}

function chooseFiles() {
  fileInputRef.value?.click()
}

function revokePreview(item) {
  if (item?.previewUrl) URL.revokeObjectURL(item.previewUrl)
}

async function ensureUuid() {
  if (uuid.value) return uuid.value
  uuid.value = await beginAgreement()
  return uuid.value
}

async function uploadPaperFiles(event) {
  const files = Array.from(event.target.files || [])
  event.target.value = ''
  if (!files.length || uploading.value) return
  uploading.value = true
  try {
    const batchId = await ensureUuid()
    for (const file of files) {
      const previewUrl = URL.createObjectURL(file)
      try {
        const filename = await uploadAgreementWhenAdd(batchId, file)
        paperFiles.value.push({
          key: `${filename}-${Date.now()}-${Math.random()}`,
          filename,
          name: file.name,
          previewUrl,
        })
      } catch (error) {
        URL.revokeObjectURL(previewUrl)
        throw error
      }
    }
    ElMessage.success('扫描件已上传')
  } catch (error) {
    ElMessage.warning(error?.message || '上传扫描件失败')
  } finally {
    uploading.value = false
  }
}

async function removePaperFile(item) {
  try {
    if (uuid.value && item.filename) {
      await deleteAgreementWhenAdd(uuid.value, item.filename)
    }
    paperFiles.value = paperFiles.value.filter((file) => file.key !== item.key)
    revokePreview(item)
    ElMessage.success('扫描件已删除')
  } catch (error) {
    ElMessage.warning(error?.message || '删除扫描件失败')
  }
}

function movePaperFile(index, direction) {
  const target = index + direction
  if (target < 0 || target >= paperFiles.value.length) return
  const list = [...paperFiles.value]
  const [item] = list.splice(index, 1)
  list.splice(target, 0, item)
  paperFiles.value = list
}

function validateDraft() {
  if (!parentId.value) return '缺少领养申请信息'
  if (isPaper.value && !paperFiles.value.length) return '请上传纸质协议扫描件'
  if (!isPaper.value && !content.value.trim()) return '请填写电子协议内容'
  return ''
}

async function submitAgreement() {
  const message = validateDraft()
  if (message) {
    ElMessage.warning(message)
    return
  }
  saving.value = true
  try {
    const payload = {
      uuid: isPaper.value ? uuid.value : undefined,
      parentId: parentId.value,
      parentType: parentType.value,
      type: props.type,
      content: isPaper.value ? undefined : content.value.trim(),
      fileOrder: isPaper.value ? paperFiles.value.map((item) => item.filename) : undefined,
    }
    const result = await addAgreement(payload)
    ElMessage.success('协议已创建')
    router.push(result?.id ? `/console/adoption/agreements?focus=${result.id}` : '/console/adoption/agreements')
  } catch (error) {
    ElMessage.warning(error?.message || '提交协议失败')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  if (!parentId.value) {
    ElMessage.warning('缺少领养申请信息')
    goBack()
    return
  }
  if (!isPaper.value) return
  loading.value = true
  try {
    await ensureUuid()
  } catch (error) {
    ElMessage.warning(error?.message || '初始化纸质协议失败')
  } finally {
    loading.value = false
  }
})

onBeforeUnmount(() => {
  paperFiles.value.forEach(revokePreview)
})
</script>

<style scoped>
.agreement-draft-panel {
  display: grid;
  gap: 18px;
}

.agreement-draft-head,
.agreement-toolbar,
.agreement-draft-footer,
.agreement-paper-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.agreement-draft-head {
  justify-content: space-between;
}

.agreement-draft-head h2 {
  margin: 0;
  color: #5d3927;
  font-size: 24px;
  line-height: 1.3;
}

.agreement-draft-head p,
.agreement-toolbar span,
.agreement-paper-meta span {
  margin: 4px 0 0;
  color: var(--muted);
  line-height: 1.6;
}

.agreement-toolbar {
  justify-content: space-between;
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: rgba(255, 253, 249, 0.92);
}

.agreement-paper-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 14px;
}

.agreement-paper-card {
  overflow: hidden;
  display: grid;
  gap: 10px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: rgba(255, 253, 249, 0.96);
}

.agreement-paper-card img {
  display: block;
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
  background: var(--bg-soft);
}

.agreement-paper-meta {
  display: grid;
  gap: 2px;
  padding: 10px 12px 0;
}

.agreement-paper-meta strong {
  color: #5d3927;
}

.agreement-paper-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px;
  padding: 0 10px 10px;
}

.agreement-paper-actions :deep(.el-button) {
  width: 100%;
  min-width: 0;
  justify-content: center;
}

.agreement-paper-actions :deep(.el-button:last-child) {
  grid-column: 1 / -1;
}

.agreement-electronic-section {
  border-top: 1px solid var(--line);
  padding-top: 16px;
}

.agreement-draft-footer {
  justify-content: flex-end;
}

@media (max-width: 720px) {
  .agreement-draft-head,
  .agreement-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .agreement-paper-grid {
    grid-template-columns: 1fr;
  }
}
</style>
