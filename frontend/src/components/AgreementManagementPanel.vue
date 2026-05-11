<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>协议管理</strong>
        <span>查看领养/寄养协议并维护协议内容</span>
      </div>
    </template>

    <section class="pet-admin-section">
      <section class="filter-panel pet-directory-filter-panel">
        <div class="pet-filter-row agreement-filter-row">
          <el-select v-model="parentTypeFilter" clearable placeholder="协议来源">
            <el-option label="领养" value="ADOPT" />
            <el-option label="寄养" value="BREADING" />
          </el-select>
          <el-select v-model="signedFilter" clearable placeholder="签署状态">
            <el-option label="已签署" :value="true" />
            <el-option label="未签署" :value="false" />
          </el-select>
          <div class="pet-filter-action">
            <el-button class="warm-btn" :icon="Search" :loading="loading" @click="searchRows">搜索</el-button>
          </div>
        </div>
      </section>

      <el-table :data="rows" v-loading="loading" class="user-admin-table">
        <el-table-column label="宠物" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <button class="table-primary-link" type="button" @click="openAgreementDialog(row)">
              {{ row.petName || parentTypeText(row.parentType) }}
            </button>
          </template>
        </el-table-column>
        <el-table-column label="服务" width="110">
          <template #default="{ row }">{{ parentTypeText(row.parentType) }}</template>
        </el-table-column>
        <el-table-column label="类型" width="110">
          <template #default="{ row }">{{ agreementTypeText(row) }}</template>
        </el-table-column>
        <el-table-column label="签署状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.signTime ? 'success' : 'warning'" effect="plain">{{ row.signTime ? '已签署' : '未签署' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="160">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="更新时间" min-width="160">
          <template #default="{ row }">{{ formatDate(row.updateTime) }}</template>
        </el-table-column>
        <el-table-column width="40" class-name="action-col">
          <template #header>
            <TableActionColumnHeader title="操作" :collapsed="actionCollapsed" @toggle="actionCollapsed = !actionCollapsed" />
          </template>
          <template #default="{ row }">
            <div class="table-action-cell">
              <div class="table-action-panel" :class="{ 'is-collapsed': actionCollapsed }">
                <el-button text type="primary" :icon="Edit" @click="openAgreementDialog(row)">{{ row.signTime ? '查看' : '修改' }}</el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="user-admin-pagination">
        <el-pagination
          layout="prev, pager, next, total"
          :current-page="page.page"
          :page-size="page.size"
          :total="total"
          @current-change="changePage"
        />
      </div>
    </section>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="activeTitle" width="860px" :close-on-click-modal="false">
    <section v-if="activeAgreement" class="agreement-editor" v-loading="detailLoading">
      <div class="agreement-editor-meta">
        <el-tag effect="plain">{{ parentTypeText(activeAgreement.parentType) }}</el-tag>
        <el-tag :type="isActiveSigned ? 'success' : 'warning'" effect="plain">{{ isActiveSigned ? '已签署' : '未签署' }}</el-tag>
        <span>{{ formatDate(activeAgreement.updateTime) }}</span>
      </div>

      <section v-if="activeType === 'ELECTRONIC'" class="agreement-editor-section">
        <el-form label-position="top">
          <el-form-item label="电子协议正文">
            <el-input
              v-model="editContent"
              type="textarea"
              :autosize="{ minRows: 12, maxRows: 20 }"
              placeholder="填写电子协议正文"
              :disabled="isActiveSigned"
            />
          </el-form-item>
        </el-form>
      </section>

      <section v-else class="agreement-editor-section">
        <div class="agreement-editor-toolbar">
          <input ref="fileInputRef" class="profile-avatar-input" type="file" accept="image/*" @change="uploadPaperFile" />
          <strong>纸质扫描件</strong>
          <el-button v-if="!isActiveSigned" class="soft-btn" :icon="Upload" :loading="uploading" @click="chooseFile">添加扫描件</el-button>
        </div>

        <div v-if="paperFiles.length" class="agreement-file-list">
          <article v-for="(file, index) in paperFiles" :key="file.id" class="agreement-file-card">
            <a :href="file.assetUrl" target="_blank" rel="noreferrer">
              <img :src="file.assetUrl" :alt="`协议第 ${index + 1} 页`" />
            </a>
            <div class="agreement-file-card-body">
              <strong>第 {{ index + 1 }} 页</strong>
              <div class="agreement-file-actions">
                <el-button v-if="!isActiveSigned" text :icon="ArrowUp" :disabled="index === 0" @click="moveExistingFile(index, -1)">上移</el-button>
                <el-button v-if="!isActiveSigned" text :icon="ArrowDown" :disabled="index === paperFiles.length - 1" @click="moveExistingFile(index, 1)">下移</el-button>
                <el-button v-if="!isActiveSigned" text type="danger" :icon="Delete" @click="removeExistingFile(file)">删除</el-button>
              </div>
            </div>
          </article>
        </div>
        <el-empty v-else description="暂无协议扫描件" />
      </section>

      <section v-if="activeAgreement.sign" class="agreement-editor-section">
        <div class="agreement-editor-toolbar">
          <strong>签名</strong>
        </div>
        <a :href="activeAgreement.sign" target="_blank" rel="noreferrer" class="agreement-sign-preview">
          <img :src="activeAgreement.sign" alt="协议签名" />
        </a>
      </section>
    </section>

    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
      <el-button v-if="activeType === 'PAPER' && !isActiveSigned" class="soft-btn" :loading="savingOrder" :disabled="!hasPaperOrderChanged" @click="savePaperOrder">保存排序</el-button>
      <el-button v-if="activeType === 'ELECTRONIC' && !isActiveSigned" class="warm-btn" :loading="saving" @click="saveElectronicAgreement">保存协议</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, ArrowUp, Delete, Edit, Search, Upload } from '@element-plus/icons-vue'
import {
  deleteAgreementFile,
  getAgreement,
  getAgreements,
  reorderAgreementFiles,
  updateAgreement,
  uploadAgreement,
} from '../api/services'
import TableActionColumnHeader from './TableActionColumnHeader.vue'

const route = useRoute()

const loading = ref(false)
const detailLoading = ref(false)
const saving = ref(false)
const savingOrder = ref(false)
const uploading = ref(false)
const rows = ref([])
const total = ref(0)
const actionCollapsed = ref(false)
const parentTypeFilter = ref('')
const signedFilter = ref('')
const dialogVisible = ref(false)
const activeAgreement = ref(null)
const editContent = ref('')
const originalPaperOrder = ref([])
const fileInputRef = ref(null)
const page = reactive({ page: 1, size: 10 })

const activeType = computed(() => inferAgreementType(activeAgreement.value))
const activeTitle = computed(() => activeAgreement.value
  ? `${activeAgreement.value.petName || parentTypeText(activeAgreement.value.parentType)} · ${parentTypeText(activeAgreement.value.parentType)}`
  : '协议')
const isActiveSigned = computed(() => Boolean(activeAgreement.value?.signTime))
const paperFiles = computed(() => {
  const files = Array.isArray(activeAgreement.value?.files) ? activeAgreement.value.files : []
  return files
    .filter((file) => Number(file.page || 0) > 0)
    .slice()
    .sort((a, b) => Number(a.page || 0) - Number(b.page || 0))
})
const hasPaperOrderChanged = computed(() => {
  const current = paperFiles.value.map((file) => String(file.id))
  return current.join('|') !== originalPaperOrder.value.join('|')
})

function parentTypeText(value) {
  if (value === 'ADOPT') return '领养'
  if (value === 'BREADING') return '寄养'
  return value || ''
}

function inferAgreementType(value) {
  if (!value) return ''
  return value.type || (value.content ? 'ELECTRONIC' : 'PAPER')
}

function agreementTypeText(row) {
  const type = inferAgreementType(row)
  return type === 'PAPER' ? '纸质协议' : '电子协议'
}

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function buildQuery() {
  return {
    page: page.page,
    size: page.size,
    sort: 'create_time',
    order: 'desc',
    parentType: parentTypeFilter.value || undefined,
    signed: signedFilter.value === '' ? undefined : signedFilter.value,
  }
}

async function loadRows() {
  loading.value = true
  try {
    const result = await getAgreements(buildQuery())
    rows.value = Array.isArray(result?.records) ? result.records : []
    total.value = Number(result?.total || rows.value.length)
    const focusId = String(route.query.focus || '')
    if (focusId) {
      const target = rows.value.find((row) => String(row.id) === focusId)
      if (target) openAgreementDialog(target)
    }
  } catch (error) {
    ElMessage.warning(error?.message || '加载协议失败')
  } finally {
    loading.value = false
  }
}

function searchRows() {
  page.page = 1
  loadRows()
}

function changePage(value) {
  page.page = value
  loadRows()
}

function syncPaperOrder() {
  originalPaperOrder.value = paperFiles.value.map((file) => String(file.id))
}

async function openAgreementDialog(row) {
  dialogVisible.value = true
  activeAgreement.value = row
  editContent.value = row?.content || ''
  detailLoading.value = true
  try {
    const detail = await getAgreement(row.id)
    activeAgreement.value = detail
    editContent.value = detail?.content || ''
    syncPaperOrder()
  } catch (error) {
    ElMessage.warning(error?.message || '加载协议详情失败')
  } finally {
    detailLoading.value = false
  }
}

async function saveElectronicAgreement() {
  if (!activeAgreement.value || saving.value) return
  if (isActiveSigned.value) return
  if (!editContent.value.trim()) {
    ElMessage.warning('请填写协议内容')
    return
  }
  saving.value = true
  try {
    activeAgreement.value = await updateAgreement(activeAgreement.value.id, { content: editContent.value.trim() })
    ElMessage.success('协议已更新')
    await loadRows()
  } catch (error) {
    ElMessage.warning(error?.message || '保存协议失败')
  } finally {
    saving.value = false
  }
}

function chooseFile() {
  fileInputRef.value?.click()
}

async function uploadPaperFile(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file || !activeAgreement.value || uploading.value) return
  if (isActiveSigned.value) return
  uploading.value = true
  try {
    const files = await uploadAgreement(activeAgreement.value.id, {
      file,
      page: paperFiles.value.length + 1,
    })
    activeAgreement.value = { ...activeAgreement.value, type: 'PAPER', content: null, files }
    syncPaperOrder()
    ElMessage.success('扫描件已添加')
    await loadRows()
  } catch (error) {
    ElMessage.warning(error?.message || '添加扫描件失败')
  } finally {
    uploading.value = false
  }
}

function moveExistingFile(index, direction) {
  const target = index + direction
  if (isActiveSigned.value) return
  if (!activeAgreement.value || target < 0 || target >= paperFiles.value.length) return
  const pageFiles = [...paperFiles.value]
  const [item] = pageFiles.splice(index, 1)
  pageFiles.splice(target, 0, item)
  const signFiles = (activeAgreement.value.files || []).filter((file) => Number(file.page || 0) === 0)
  activeAgreement.value = {
    ...activeAgreement.value,
    files: [
      ...signFiles,
      ...pageFiles.map((file, fileIndex) => ({ ...file, page: fileIndex + 1 })),
    ],
  }
}

async function savePaperOrder() {
  if (!activeAgreement.value || !hasPaperOrderChanged.value || savingOrder.value) return
  if (isActiveSigned.value) return
  savingOrder.value = true
  try {
    const files = await reorderAgreementFiles(activeAgreement.value.id, paperFiles.value.map((file) => file.id))
    activeAgreement.value = { ...activeAgreement.value, files }
    syncPaperOrder()
    ElMessage.success('扫描件顺序已保存')
    await loadRows()
  } catch (error) {
    ElMessage.warning(error?.message || '保存排序失败')
  } finally {
    savingOrder.value = false
  }
}

async function removeExistingFile(file) {
  if (!activeAgreement.value) return
  if (isActiveSigned.value) return
  try {
    await ElMessageBox.confirm('确认删除这张协议扫描件？', '删除扫描件', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    const files = await deleteAgreementFile(activeAgreement.value.id, file.id)
    activeAgreement.value = { ...activeAgreement.value, files }
    syncPaperOrder()
    ElMessage.success('扫描件已删除')
    await loadRows()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.warning(error?.message || '删除扫描件失败')
    }
  }
}

onMounted(() => {
  loadRows()
})
</script>

<style scoped>
.agreement-filter-row {
  grid-template-columns: minmax(160px, 220px) minmax(160px, 220px) auto;
}

.agreement-editor {
  display: grid;
  gap: 16px;
}

.agreement-editor-meta,
.agreement-editor-toolbar,
.agreement-file-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.agreement-editor-meta {
  flex-wrap: wrap;
  color: var(--muted);
}

.agreement-editor-section {
  padding-top: 14px;
  border-top: 1px solid var(--line);
}

.agreement-editor-toolbar {
  justify-content: space-between;
  margin-bottom: 12px;
}

.agreement-editor-toolbar strong {
  color: #5d3927;
}

.agreement-file-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 14px;
}

.agreement-file-card {
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: rgba(255, 253, 249, 0.96);
}

.agreement-file-card img {
  display: block;
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
  background: var(--bg-soft);
}

.agreement-file-card-body {
  display: grid;
  gap: 8px;
  padding: 10px;
}

.agreement-file-card-body strong {
  color: #5d3927;
}

.agreement-file-actions {
  justify-content: space-between;
  flex-wrap: wrap;
}

.agreement-sign-preview {
  display: inline-block;
  max-width: 280px;
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: rgba(255, 253, 249, 0.96);
}

.agreement-sign-preview img {
  display: block;
  width: 100%;
  max-height: 180px;
  object-fit: contain;
}

@media (max-width: 720px) {
  .agreement-filter-row {
    grid-template-columns: 1fr;
  }

  .agreement-editor-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
