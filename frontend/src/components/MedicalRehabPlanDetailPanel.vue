<template>
  <el-card class="profile-card">
    <template #header>
      <div class="profile-card-header">
        <strong>康复计划详情</strong>
      </div>
    </template>

    <section v-loading="loading" class="action-form-panel">
      <div v-if="plan" class="rehab-detail-head">
        <div>
          <h2>{{ plan.title || '未命名计划' }}</h2>
          <p>{{ plan.petName || '未命名宠物' }} · {{ plan.frequency || '频率待补充' }}</p>
        </div>
        <div class="rehab-detail-actions">
          <el-tag :type="statusTagType(plan.status)" effect="plain">{{ statusText(plan.status) }}</el-tag>
          <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回</el-button>
          <el-button v-if="canUpdateStatus" class="soft-btn" :icon="Edit" @click="openStatusDialog">修改状态</el-button>
          <el-button v-if="canAddRecord" class="warm-btn" :icon="Plus" @click="openRecordDialog">添加记录</el-button>
        </div>
      </div>

      <section v-if="plan" class="console-detail-grid rehab-detail-grid">
        <section class="console-detail-section">
          <h3>计划信息</h3>
          <dl class="console-detail-list console-detail-list-inline">
            <div><dt>负责兽医</dt><dd>{{ plan.username || '' }}</dd></div>
            <div><dt>宠物年龄</dt><dd>{{ plan.petAge ?? 0 }} 月</dd></div>
            <div><dt>开始时间</dt><dd>{{ formatDate(plan.startTime) }}</dd></div>
            <div><dt>预计结束</dt><dd>{{ formatDate(plan.endTime) }}</dd></div>
          </dl>
        </section>

        <section class="console-detail-section">
          <h3>宠物信息</h3>
          <dl class="console-detail-list console-detail-list-inline">
            <div><dt>宠物名称</dt><dd>{{ plan.petName || '' }}</dd></div>
            <div><dt>类型</dt><dd>{{ plan.petType || '' }}</dd></div>
            <div><dt>品种</dt><dd>{{ plan.petBreed || '' }}</dd></div>
            <div><dt>性别</dt><dd>{{ plan.petSex || '' }}</dd></div>
          </dl>
        </section>

        <section class="console-detail-section console-detail-section-wide">
          <h3>康复内容</h3>
          <p class="rehab-detail-content">{{ plan.content || '' }}</p>
        </section>

        <section class="console-detail-section console-detail-section-wide">
          <h3>处方列表</h3>
          <div v-if="plan.orders?.length" class="medical-mini-links rehab-order-links">
            <span v-for="order in plan.orders" :key="order.id">{{ order.itemName || '物资' }} {{ order.count }}{{ order.unit }} · {{ orderTypeText(order.type) }}</span>
          </div>
          <el-empty v-else description="暂无处方" />
        </section>

        <section class="console-detail-section console-detail-section-wide">
          <div class="medical-subsection-head">
            <strong>康复记录</strong>
            <el-button v-if="canAddRecord" text type="success" :icon="Plus" @click="openRecordDialog">添加记录</el-button>
          </div>
          <div v-if="records.length" class="medical-mini-list">
            <article v-for="item in records" :key="item.id" class="medical-mini-card">
              <div class="medical-plan-head">
                <strong>{{ item.step || '康复执行' }}</strong>
                <span>{{ item.username || '' }} · {{ formatDate(item.createTime) }}</span>
              </div>
              <p>{{ item.reaction || '' }}</p>
              <p>{{ item.note || '' }}</p>
              <div v-if="item.assets?.length" class="medical-mini-links">
                <a v-for="asset in item.assets" :key="asset" :href="asset" target="_blank" rel="noreferrer" download>康复文件</a>
              </div>
            </article>
          </div>
          <el-empty v-else description="暂无康复记录" />
        </section>
      </section>

      <el-empty v-else-if="!loading" description="康复计划不存在">
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回康复计划</el-button>
      </el-empty>
    </section>
  </el-card>

  <el-dialog v-model="statusDialogVisible" title="修改康复状态" width="560px">
    <section class="rehab-status-history">
      <article v-for="item in statusRecords" :key="item.id || `${item.status}-${item.createTime}`" class="medical-mini-card">
        <div class="medical-plan-head">
          <strong>{{ statusText(item.status) }}</strong>
          <span>{{ item.username || '' }} · {{ formatDate(item.createTime) }}</span>
        </div>
        <p>{{ item.reason === '~~~create~~~' ? '创建康复计划' : item.reason }}</p>
      </article>
      <el-empty v-if="!statusRecords.length" description="暂无状态变更记录" />
    </section>

    <el-form label-position="top" class="rehab-dialog-form">
      <el-form-item label="新状态">
        <el-select v-model="statusForm.status" class="full-width-control" placeholder="选择状态">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="变更原因">
        <el-input v-model="statusForm.reason" type="textarea" :rows="3" placeholder="填写状态变更原因" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="statusDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" :loading="savingStatus" @click="submitStatus">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="recordDialogVisible" title="添加康复记录" width="620px">
    <el-form label-position="top" class="rehab-dialog-form">
      <el-form-item label="康复步骤">
        <el-input v-model="recordForm.step" />
      </el-form-item>
      <el-form-item label="宠物反应">
        <el-input v-model="recordForm.reaction" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="recordForm.note" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="文件">
        <div class="medical-upload-row">
          <input ref="fileInputRef" class="profile-avatar-input" type="file" multiple @change="selectRecordFiles" />
          <el-button class="soft-btn" @click="chooseRecordFiles">选择文件</el-button>
          <span v-for="file in recordFiles" :key="file.name" class="medical-upload-chip">{{ file.name }}</span>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="recordDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" :loading="savingRecord" @click="submitRecord">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Edit, Plus } from '@element-plus/icons-vue'
import { addRehabRecord, getRehabPlan, getRehabRecords, updateRehabPlanStatus } from '../api/medical'
import { useUserStore } from '../stores/user'
import { ROLE, hasRole } from '../utils/roles'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const plan = ref(null)
const records = ref([])
const loading = ref(false)
const statusDialogVisible = ref(false)
const recordDialogVisible = ref(false)
const savingStatus = ref(false)
const savingRecord = ref(false)
const fileInputRef = ref(null)
const recordFiles = ref([])
const statusForm = reactive({ status: 'ACTIVE', reason: '' })
const recordForm = reactive({ step: '', reaction: '', note: '' })

const statusOptions = [
  { label: '进行中', value: 'ACTIVE' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已废弃', value: 'DISCARD' },
]

const loginRole = computed(() => Number(userStore.profile?.role || 0))
const isDoctor = computed(() => hasRole(loginRole.value, ROLE.DOCTOR))
const isVolunteer = computed(() => hasRole(loginRole.value, ROLE.VOLUNTEER))
const canAddRecord = computed(() => isDoctor.value || isVolunteer.value)
const canUpdateStatus = computed(() => isDoctor.value && String(plan.value?.doctorId || '') === String(userStore.profile?.id || ''))
const statusRecords = computed(() => Array.isArray(plan.value?.statusRecords) ? plan.value.statusRecords : [])

function statusText(value) {
  const item = statusOptions.find((option) => option.value === value)
  return item?.label || value || ''
}

function statusTagType(value) {
  if (value === 'COMPLETED') return 'success'
  if (value === 'DISCARD') return 'info'
  return 'warning'
}

function orderTypeText(value) {
  const map = { MEDICINE: '药品', SURGERY: '手术', EXAMINATION: '检查', OTHER: '其他' }
  return map[value] || value || ''
}

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function goBack() {
  router.push('/console/medical/rehab')
}

function openStatusDialog() {
  if (!canUpdateStatus.value) return
  statusForm.status = plan.value?.status || 'ACTIVE'
  statusForm.reason = ''
  statusDialogVisible.value = true
}

function openRecordDialog() {
  if (!canAddRecord.value) return
  recordForm.step = ''
  recordForm.reaction = ''
  recordForm.note = ''
  recordFiles.value = []
  if (fileInputRef.value) fileInputRef.value.value = ''
  recordDialogVisible.value = true
}

function chooseRecordFiles() {
  fileInputRef.value?.click()
}

function selectRecordFiles(event) {
  recordFiles.value = Array.from(event.target.files || [])
}

async function submitStatus() {
  if (!statusForm.status || !statusForm.reason.trim()) {
    ElMessage.warning('请填写状态和变更原因')
    return
  }
  savingStatus.value = true
  try {
    plan.value = await updateRehabPlanStatus(route.params.id, {
      status: statusForm.status,
      reason: statusForm.reason.trim(),
    })
    statusDialogVisible.value = false
    ElMessage.success('康复状态已更新')
  } catch (error) {
    ElMessage.warning(error?.message || '更新康复状态失败')
  } finally {
    savingStatus.value = false
  }
}

async function submitRecord() {
  if (!recordForm.step.trim() || !recordForm.reaction.trim() || !recordForm.note.trim()) {
    ElMessage.warning('请完整填写康复记录')
    return
  }
  savingRecord.value = true
  try {
    await addRehabRecord(route.params.id, {
      step: recordForm.step.trim(),
      reaction: recordForm.reaction.trim(),
      note: recordForm.note.trim(),
      files: recordFiles.value,
    })
    recordDialogVisible.value = false
    await loadRecords()
    ElMessage.success('康复记录已添加')
  } catch (error) {
    ElMessage.warning(error?.message || '添加康复记录失败')
  } finally {
    savingRecord.value = false
  }
}

async function loadPlan() {
  loading.value = true
  try {
    plan.value = await getRehabPlan(route.params.id)
    await loadRecords()
  } catch (error) {
    ElMessage.warning(error?.message || '加载康复计划失败')
  } finally {
    loading.value = false
  }
}

async function loadRecords() {
  records.value = await getRehabRecords(route.params.id)
}

watch(
  () => route.query.action,
  async (action) => {
    if (!plan.value) return
    await nextTick()
    if (action === 'status') openStatusDialog()
    if (action === 'record') openRecordDialog()
  },
)

onMounted(async () => {
  await loadPlan()
  if (route.query.action === 'status') openStatusDialog()
  if (route.query.action === 'record') openRecordDialog()
})
</script>

<style scoped>
.rehab-detail-head,
.rehab-detail-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rehab-detail-head {
  justify-content: space-between;
  margin-bottom: 18px;
}

.rehab-detail-head h2 {
  margin: 0;
  color: #5d3927;
  font-size: 24px;
  line-height: 1.3;
}

.rehab-detail-head p,
.rehab-detail-content {
  margin: 6px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.rehab-detail-actions {
  flex-wrap: wrap;
  justify-content: flex-end;
}

.rehab-detail-grid {
  margin-top: 0;
}

.rehab-dialog-form,
.rehab-status-history {
  display: grid;
  gap: 12px;
}

.rehab-status-history {
  max-height: 260px;
  overflow: auto;
  margin-bottom: 14px;
}

.medical-mini-links a {
  border: 1px solid rgba(231, 122, 59, 0.2);
  border-radius: 999px;
  background: rgba(255, 253, 249, 0.9);
  color: var(--primary-strong);
  padding: 4px 9px;
  font-size: 12px;
  text-decoration: none;
}

.rehab-order-links {
  margin-top: 0;
}

@media (max-width: 760px) {
  .rehab-detail-head {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
