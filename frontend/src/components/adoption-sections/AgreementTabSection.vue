<template>
  <section class="pet-admin-section">
    <el-table :data="agreementFilteredRows" v-loading="agreementLoading" class="user-admin-table">
      <el-table-column min-width="180" show-overflow-tooltip>
        <template #header>
          <TableFilterHeader label="宠物" :filter="agreementFilters.petName" type="text" :active="agreementIsActive('petName')" />
        </template>
        <template #default="{ row }">
          <button class="table-primary-link" type="button" @click="openAgreementDetailDialog(row)">
            {{ row.petName || agreementParentTypeText(row.parentType) }}
          </button>
        </template>
      </el-table-column>
      <el-table-column label="服务" width="110">
        <template #default="{ row }">{{ agreementParentTypeText(row.parentType) }}</template>
      </el-table-column>
      <el-table-column label="类型" width="110">
        <template #default="{ row }">{{ agreementTypeText(row) }}</template>
      </el-table-column>
      <el-table-column width="110">
        <template #header>
          <TableFilterHeader label="签署" :filter="agreementFilters.signed" type="enum" :active="agreementIsActive('signed')" :options="agreementSignedOptions" />
        </template>
        <template #default="{ row }">
          <el-tag :type="row.signTime ? 'success' : 'warning'" effect="plain">{{ row.signTime ? '已签署' : '未签署' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column min-width="160">
        <template #header>
          <TableFilterHeader label="创建时间" :filter="agreementFilters.createTime" type="time" :active="agreementIsActive('createTime')" />
        </template>
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="更新时间" min-width="160">
        <template #default="{ row }">{{ formatDate(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column width="40" class-name="action-col">
        <template #header>
          <TableActionColumnHeader title="操作" :collapsed="agreementActionCollapsed" @toggle="agreementActionCollapsed = !agreementActionCollapsed" />
        </template>
        <template #default="{ row }">
          <div class="table-action-cell">
            <div class="table-action-panel" :class="{ 'is-collapsed': agreementActionCollapsed }">
              <el-button text type="primary" :icon="Edit" @click="openAgreementDetailDialog(row)">{{ row.signTime ? '查看' : '修改' }}</el-button>
            </div>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <div class="user-admin-pagination">
      <el-pagination
        layout="prev, pager, next, total"
        :current-page="agreementPage.page"
        :page-size="agreementPage.size"
        :total="agreementTotal"
        @current-change="agreementChangePage"
      />
    </div>
  </section>

  <el-dialog v-model="agreementDetailDialogVisible" :title="agreementActiveTitle" width="860px" :close-on-click-modal="false">
    <section v-if="agreementActiveRow" class="agreement-editor" v-loading="agreementDetailLoading">
      <div class="agreement-editor-meta">
        <el-tag effect="plain">{{ agreementParentTypeText(agreementActiveRow.parentType) }}</el-tag>
        <el-tag :type="agreementIsActiveSigned ? 'success' : 'warning'" effect="plain">{{ agreementIsActiveSigned ? '已签署' : '未签署' }}</el-tag>
        <span>{{ formatDate(agreementActiveRow.updateTime) }}</span>
      </div>

      <section v-if="agreementActiveType === 'ELECTRONIC'" class="agreement-editor-section">
        <el-form label-position="top">
          <el-form-item label="电子协议正文">
            <el-input
              v-model="agreementEditContent"
              type="textarea"
              :autosize="{ minRows: 12, maxRows: 20 }"
              placeholder="填写电子协议正文"
              :disabled="agreementIsActiveSigned"
            />
          </el-form-item>
        </el-form>
      </section>

      <section v-else class="agreement-editor-section">
        <div class="agreement-editor-toolbar">
          <input ref="agreementFileInputRef" class="profile-avatar-input" type="file" accept="image/*" @change="uploadAgreementPaperFile" />
          <strong>纸质扫描件</strong>
          <el-button v-if="!agreementIsActiveSigned" class="soft-btn" :icon="Upload" :loading="agreementUploading" @click="chooseAgreementFile">添加扫描件</el-button>
        </div>

        <div v-if="agreementPaperFiles.length" class="agreement-file-list">
          <article v-for="(file, index) in agreementPaperFiles" :key="file.id" class="agreement-file-card">
            <a :href="file.assetUrl" target="_blank" rel="noreferrer">
              <img :src="file.assetUrl" :alt="`协议第 ${index + 1} 页`" />
            </a>
            <div class="agreement-file-card-body">
              <strong>第 {{ index + 1 }} 页</strong>
              <div class="agreement-file-actions">
                <el-button v-if="!agreementIsActiveSigned" text :icon="ArrowUp" :disabled="index === 0" @click="agreementMoveFile(index, -1)">上移</el-button>
                <el-button v-if="!agreementIsActiveSigned" text :icon="ArrowDown" :disabled="index === agreementPaperFiles.length - 1" @click="agreementMoveFile(index, 1)">下移</el-button>
                <el-button v-if="!agreementIsActiveSigned" text type="danger" :icon="Delete" @click="agreementRemoveFile(file)">删除</el-button>
              </div>
            </div>
          </article>
        </div>
        <el-empty v-else description="暂无协议扫描件" />
      </section>

      <section v-if="agreementActiveRow.sign" class="agreement-editor-section">
        <div class="agreement-editor-toolbar">
          <strong>签名</strong>
        </div>
        <a :href="agreementActiveRow.sign" target="_blank" rel="noreferrer" class="agreement-sign-preview">
          <img :src="agreementActiveRow.sign" alt="协议签名" />
        </a>
      </section>
    </section>

    <template #footer>
      <el-button @click="agreementDetailDialogVisible = false">关闭</el-button>
      <el-button v-if="agreementActiveType === 'PAPER' && !agreementIsActiveSigned" class="soft-btn" :loading="agreementSavingOrder" :disabled="!agreementHasOrderChanged" @click="saveAgreementPaperOrder">保存排序</el-button>
      <el-button v-if="agreementActiveType === 'ELECTRONIC' && !agreementIsActiveSigned" class="warm-btn" :loading="agreementSaving" @click="saveAgreementElectronic">保存协议</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, ArrowUp, Delete, Edit, Plus, RefreshRight, Upload } from '@element-plus/icons-vue'
import { deleteAgreementFile, getAgreement, getAgreements, reorderAgreementFiles, updateAgreement, uploadAgreement } from '../../api/adoption'
import { useTableFilters } from '../../composables/useTableFilters'
import { useUserStore } from '../../stores/user'
import TableFilterHeader from '../TableFilterHeader.vue'
import TableActionColumnHeader from '../TableActionColumnHeader.vue'
import { useRoute, useRouter } from 'vue-router'
import { ROLE, hasRole } from '../../utils/roles'
import { formatDate } from '../../utils/format'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginRole = computed(() => Number(userStore.profile?.role || 0))
const loginUserId = computed(() => String(userStore.profile?.id || ''))
const isAdmin = computed(() => hasRole(loginRole.value, ROLE.ADMIN))
const canManageUsers = computed(() => isAdmin.value || hasRole(loginRole.value, ROLE.WORKER))

const agreementLoading = ref(false)
const agreementDetailLoading = ref(false)
const agreementSaving = ref(false)
const agreementSavingOrder = ref(false)
const agreementUploading = ref(false)
const agreementRows = ref([])
const agreementTotal = ref(0)
const agreementActionCollapsed = ref(false)
const agreementDetailDialogVisible = ref(false)
const agreementActiveRow = ref(null)
const agreementEditContent = ref('')
const agreementOriginalPaperOrder = ref([])
const agreementFileInputRef = ref(null)
const agreementPage = reactive({ page: 1, size: 10 })

const { filters: agreementFilters, isActive: agreementIsActive, applyFilter: agreementApplyFilter } = useTableFilters({
  petName: { type: 'text' },
  signed: { type: 'enum' },
  createTime: { type: 'time' },
})

const agreementSignedOptions = [
  { value: true, label: '已签署' },
  { value: false, label: '未签署' },
]

const agreementFilteredRows = computed(() => agreementApplyFilter(agreementRows.value || []))

const agreementActiveType = computed(() => inferAgreementType(agreementActiveRow.value))
const agreementActiveTitle = computed(() => agreementActiveRow.value
  ? `${agreementActiveRow.value.petName || agreementParentTypeText(agreementActiveRow.value.parentType)} · ${agreementParentTypeText(agreementActiveRow.value.parentType)}`
  : '协议')
const agreementIsActiveSigned = computed(() => Boolean(agreementActiveRow.value?.signTime))
const agreementPaperFiles = computed(() => {
  const files = Array.isArray(agreementActiveRow.value?.files) ? agreementActiveRow.value.files : []
  return files.filter((file) => Number(file.page || 0) > 0).slice().sort((a, b) => Number(a.page || 0) - Number(b.page || 0))
})
const agreementHasOrderChanged = computed(() => {
  const current = agreementPaperFiles.value.map((file) => String(file.id))
  return current.join('|') !== agreementOriginalPaperOrder.value.join('|')
})

function agreementParentTypeText(value) {
  if (value === 'ADOPT') return '领养'
  if (value === 'BREADING') return '寄养'
  return value || ''
}

function inferAgreementType(value) {
  if (!value) return ''
  return value.type || (value.content ? 'ELECTRONIC' : 'PAPER')
}

function agreementTypeText(row) {
  return inferAgreementType(row) === 'PAPER' ? '纸质协议' : '电子协议'
}

function agreementBuildQuery() {
  return { page: agreementPage.page, size: agreementPage.size, sort: 'create_time', order: 'desc' }
}

async function loadAgreementRows() {
  agreementLoading.value = true
  try {
    const result = await getAgreements(agreementBuildQuery())
    agreementRows.value = Array.isArray(result?.records) ? result.records : []
    agreementTotal.value = Number(result?.total || agreementRows.value.length)
    const focusId = String(route.query.focus || '')
    if (focusId) {
      const target = agreementRows.value.find((row) => String(row.id) === focusId)
      if (target) openAgreementDetailDialog(target)
    }
  } catch (error) {
    ElMessage.warning(error?.message || '加载协议失败')
  } finally {
    agreementLoading.value = false
  }
}

function handleAgreementRefresh() { agreementPage.page = 1; loadAgreementRows() }
function agreementChangePage(value) { agreementPage.page = value; loadAgreementRows() }

function syncAgreementPaperOrder() {
  agreementOriginalPaperOrder.value = agreementPaperFiles.value.map((file) => String(file.id))
}

async function openAgreementDetailDialog(row) {
  agreementDetailDialogVisible.value = true
  agreementActiveRow.value = row
  agreementEditContent.value = row?.content || ''
  agreementDetailLoading.value = true
  try {
    const detail = await getAgreement(row.id)
    agreementActiveRow.value = detail
    agreementEditContent.value = detail?.content || ''
    syncAgreementPaperOrder()
  } catch (error) {
    ElMessage.warning(error?.message || '加载协议详情失败')
  } finally {
    agreementDetailLoading.value = false
  }
}

async function saveAgreementElectronic() {
  if (!agreementActiveRow.value || agreementSaving.value) return
  if (agreementIsActiveSigned.value) return
  if (!agreementEditContent.value.trim()) { ElMessage.warning('请填写协议内容'); return }
  agreementSaving.value = true
  try {
    agreementActiveRow.value = await updateAgreement(agreementActiveRow.value.id, { content: agreementEditContent.value.trim() })
    ElMessage.success('协议已更新')
    await loadAgreementRows()
  } catch (error) {
    ElMessage.warning(error?.message || '保存协议失败')
  } finally {
    agreementSaving.value = false
  }
}

function chooseAgreementFile() { agreementFileInputRef.value?.click() }

async function uploadAgreementPaperFile(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file || !agreementActiveRow.value || agreementUploading.value) return
  if (agreementIsActiveSigned.value) return
  agreementUploading.value = true
  try {
    const files = await uploadAgreement(agreementActiveRow.value.id, { file, page: agreementPaperFiles.value.length + 1 })
    agreementActiveRow.value = { ...agreementActiveRow.value, type: 'PAPER', content: null, files }
    syncAgreementPaperOrder()
    ElMessage.success('扫描件已添加')
    await loadAgreementRows()
  } catch (error) {
    ElMessage.warning(error?.message || '添加扫描件失败')
  } finally {
    agreementUploading.value = false
  }
}

function agreementMoveFile(index, direction) {
  const target = index + direction
  if (agreementIsActiveSigned.value) return
  if (!agreementActiveRow.value || target < 0 || target >= agreementPaperFiles.value.length) return
  const pageFiles = [...agreementPaperFiles.value]
  const [item] = pageFiles.splice(index, 1)
  pageFiles.splice(target, 0, item)
  const signFiles = (agreementActiveRow.value.files || []).filter((file) => Number(file.page || 0) === 0)
  agreementActiveRow.value = {
    ...agreementActiveRow.value,
    files: [...signFiles, ...pageFiles.map((file, fileIndex) => ({ ...file, page: fileIndex + 1 }))],
  }
}

async function saveAgreementPaperOrder() {
  if (!agreementActiveRow.value || !agreementHasOrderChanged.value || agreementSavingOrder.value) return
  if (agreementIsActiveSigned.value) return
  agreementSavingOrder.value = true
  try {
    const files = await reorderAgreementFiles(agreementActiveRow.value.id, agreementPaperFiles.value.map((file) => file.id))
    agreementActiveRow.value = { ...agreementActiveRow.value, files }
    syncAgreementPaperOrder()
    ElMessage.success('扫描件顺序已保存')
    await loadAgreementRows()
  } catch (error) {
    ElMessage.warning(error?.message || '保存排序失败')
  } finally {
    agreementSavingOrder.value = false
  }
}

async function agreementRemoveFile(file) {
  if (!agreementActiveRow.value) return
  if (agreementIsActiveSigned.value) return
  try {
    await ElMessageBox.confirm('确认删除这张协议扫描件？', '删除扫描件', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
    const files = await deleteAgreementFile(agreementActiveRow.value.id, file.id)
    agreementActiveRow.value = { ...agreementActiveRow.value, files }
    syncAgreementPaperOrder()
    ElMessage.success('扫描件已删除')
    await loadAgreementRows()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.warning(error?.message || '删除扫描件失败')
  }
}

onMounted(() => { loadAgreementRows() })

defineExpose({ loadData: loadAgreementRows, refresh: handleAgreementRefresh })
</script>

<style scoped>
.adoption-person-cell {
  display: grid;
  gap: 3px;
}
.adoption-person-cell strong {
  color: var(--text);
}
.adoption-person-cell span {
  color: var(--muted);
  font-size: 12px;
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
  .agreement-editor-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
