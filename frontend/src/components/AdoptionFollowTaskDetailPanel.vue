<template>
  <el-card class="profile-card pet-admin-card" v-loading="loading">
    <template #header>
      <div class="profile-card-header">
        <strong>回访任务详情</strong>
      </div>
    </template>

    <section v-if="task && application" class="action-form-panel follow-task-detail-panel">
      <div class="console-detail-toolbar">
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回记录</el-button>
        <el-tag :type="taskStatusTagType(task.status)" effect="plain">{{ taskStatusText(task.status) }}</el-tag>
      </div>

      <div class="follow-task-hero-grid">
        <section class="console-detail-section follow-task-pet-card">
          <div class="follow-task-pet-cover">
            <img v-if="application.petCover" :src="application.petCover" :alt="application.petName || '宠物封面'" />
            <el-icon v-else><PictureFilled /></el-icon>
          </div>
          <div class="follow-task-pet-copy">
            <span>宠物</span>
            <button class="table-primary-link follow-task-link" type="button" @click="goPetProfile">
              <strong>{{ application.petName || '未命名宠物' }}</strong>
            </button>
            <p>{{ [application.petType, application.petBreed].filter(Boolean).join(' · ') || '信息待补充' }}</p>
          </div>
        </section>

        <section class="console-detail-section follow-task-person-card">
          <p>申请人</p>
          <div class="follow-task-person-row">
            <el-avatar class="follow-task-avatar" :size="44" :src="application.applicantAvatar">
              {{ avatarInitial(application.applicantName) }}
            </el-avatar>
            <div>
              <strong>{{ application.applicantName || '未命名用户' }}</strong>
              <span>{{ application.applicantPhone || '联系方式待补充' }}</span>
            </div>
          </div>
        </section>

        <section class="console-detail-section follow-task-person-card">
          <p>负责人员</p>
          <div class="follow-task-person-row">
            <el-avatar class="follow-task-avatar" :size="44" :src="task.workerAvatar">
              {{ avatarInitial(task.workerName) }}
            </el-avatar>
            <div>
              <strong>{{ task.workerName || '未命名工作人员' }}</strong>
              <span>工作人员</span>
            </div>
          </div>
          <div class="follow-task-person-row">
            <el-avatar class="follow-task-avatar" :size="44" :src="task.volunteerAvatar">
              {{ avatarInitial(task.volunteerName) }}
            </el-avatar>
            <div>
              <strong>{{ task.volunteerName || '未命名志愿者' }}</strong>
              <span>志愿者</span>
            </div>
          </div>
        </section>
      </div>

      <section class="console-detail-section console-detail-section-wide">
        <dl class="console-detail-list console-detail-list-row follow-task-status-row">
          <div><dt>任务状态</dt><dd>{{ taskStatusText(task.status) }}</dd></div>
          <div><dt>计划时间</dt><dd>{{ formatDate(task.planTime) }}</dd></div>
          <div><dt>创建时间</dt><dd>{{ formatDate(task.createTime) }}</dd></div>
          <div><dt>备注</dt><dd>{{ task.remark || '暂无' }}</dd></div>
        </dl>
      </section>

      <section class="console-detail-section console-detail-section-wide">
        <div class="follow-task-section-head">
          <h3>回访记录</h3>
          <el-tag v-if="existingRecord" type="success" effect="plain">已提交</el-tag>
          <el-tag v-else type="warning" effect="plain">暂无记录</el-tag>
        </div>

        <article v-if="existingRecord" class="follow-task-record-card">
          <dl class="console-detail-list console-detail-list-row follow-task-record-summary">
            <div><dt>回访时间</dt><dd>{{ formatDate(existingRecord.visitTime) }}</dd></div>
            <div><dt>志愿者</dt><dd>{{ existingRecord.volunteerName || '未命名志愿者' }}</dd></div>
            <div><dt>任务</dt><dd>#{{ existingRecord.taskId }}</dd></div>
            <div><dt>宠物</dt><dd>{{ existingRecord.petName || '未命名宠物' }}</dd></div>
          </dl>
          <div class="follow-task-record-body">
            <section>
              <h4>摘要</h4>
              <p>{{ existingRecord.summary || '暂无' }}</p>
            </section>
            <section>
              <h4>生活状态</h4>
              <p>{{ existingRecord.lifeStatus || '暂无' }}</p>
            </section>
            <section>
              <h4>健康状态</h4>
              <p>{{ existingRecord.healthStatus || '暂无' }}</p>
            </section>
            <section>
              <h4>风险观察</h4>
              <p>{{ existingRecord.risk || '暂无' }}</p>
            </section>
            <section>
              <h4>建议与反馈</h4>
              <p>{{ existingRecord.suggestion || '暂无' }}</p>
            </section>
          </div>
        </article>

        <el-form v-if="canAddRecord" ref="formRef" :model="form" :rules="rules" label-position="top" class="follow-task-record-form">
          <div class="console-detail-grid follow-task-form-grid">
            <el-form-item label="回访时间" prop="visitTime">
              <el-date-picker
                v-model="form.visitTime"
                type="datetime"
                value-format="YYYY-MM-DDTHH:mm:ss"
                placeholder="选择回访时间"
                class="full-width-control"
              />
            </el-form-item>
            <el-form-item label="摘要" prop="summary">
              <el-input v-model="form.summary" clearable />
            </el-form-item>
            <el-form-item label="生活状态" prop="lifeStatus" class="follow-task-span-2">
              <el-input v-model="form.lifeStatus" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="健康状态" prop="healthStatus" class="follow-task-span-2">
              <el-input v-model="form.healthStatus" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="风险观察" prop="risk" class="follow-task-span-2">
              <el-input v-model="form.risk" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="建议与反馈" prop="suggestion" class="follow-task-span-2">
              <el-input v-model="form.suggestion" type="textarea" :rows="4" />
            </el-form-item>
          </div>
          <div class="follow-task-footer">
            <el-button class="soft-btn" @click="resetForm">清空</el-button>
            <el-button class="warm-btn" :loading="saving" @click="submitRecord">提交记录</el-button>
          </div>
        </el-form>
      </section>
    </section>

    <section v-else-if="!loading" class="pet-admin-section">
      <el-empty description="未找到回访任务">
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回记录</el-button>
      </el-empty>
    </section>
  </el-card>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, PictureFilled } from '@element-plus/icons-vue'
import { addFollowRecord, getAdoptApplication, getFollowRecords, getFollowTask } from '../api/adoption'
import { useUserStore } from '../stores/user'
import { ROLE, hasRole } from '../utils/roles'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const task = ref(null)
const application = ref(null)
const records = ref([])
const formRef = ref(null)
const form = reactive({
  visitTime: '',
  summary: '',
  lifeStatus: '',
  healthStatus: '',
  risk: '',
  suggestion: '',
})

const taskId = computed(() => String(route.params.id || ''))
const loginRole = computed(() => Number(userStore.profile?.role || 0))
const loginUserId = computed(() => String(userStore.profile?.id || ''))
const isWorker = computed(() => hasRole(loginRole.value, ROLE.WORKER) || hasRole(loginRole.value, ROLE.ADMIN))
const isVolunteer = computed(() => hasRole(loginRole.value, ROLE.VOLUNTEER))
const isTaskVolunteer = computed(() => String(task.value?.volunteerId || '') === loginUserId.value)
const canAddRecord = computed(() => Boolean(task.value && task.value.status === 'IN_PROGRESS' && !existingRecord.value && (isWorker.value || (isVolunteer.value && isTaskVolunteer.value))))
const existingRecord = computed(() => Array.isArray(records.value) && records.value.length ? records.value[0] : null)

const rules = {
  summary: [{ required: true, message: '请输入摘要', trigger: 'blur' }],
  lifeStatus: [{ required: true, message: '请输入生活状态', trigger: 'blur' }],
  healthStatus: [{ required: true, message: '请输入健康状态', trigger: 'blur' }],
  risk: [{ required: true, message: '请输入风险观察', trigger: 'blur' }],
  suggestion: [{ required: true, message: '请输入建议与反馈', trigger: 'blur' }],
}

function avatarInitial(value) {
  const text = String(value || '').trim()
  return text ? text.slice(0, 1) : '回'
}

function taskStatusText(value) {
  const map = {
    CREATE: '刚创建',
    NOTIFIED: '已通知',
    IN_PROGRESS: '执行中',
    DELAY: '推迟',
    FINISH: '已完成',
  }
  return map[value] || value || ''
}

function taskStatusTagType(value) {
  if (value === 'IN_PROGRESS') return 'primary'
  if (value === 'FINISH') return 'success'
  if (value === 'DELAY') return 'warning'
  return 'info'
}

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function goBack() {
  router.push({ name: 'console-adoption', query: { tab: 'follow' } })
}

function goPetProfile() {
  if (application.value?.petId) {
    router.push(`/pets/${application.value.petId}`)
  }
}

function resetForm() {
  form.visitTime = ''
  form.summary = ''
  form.lifeStatus = ''
  form.healthStatus = ''
  form.risk = ''
  form.suggestion = ''
}

async function loadDetail() {
  if (!taskId.value) return
  loading.value = true
  try {
    task.value = await getFollowTask(taskId.value)
    application.value = task.value?.adoptId ? await getAdoptApplication(task.value.adoptId) : null
    const result = await getFollowRecords(taskId.value, { page: 1, size: 20, sort: 'visit_time', order: 'desc' })
    records.value = Array.isArray(result?.records) ? result.records : []
  } catch (error) {
    task.value = null
    application.value = null
    records.value = []
    ElMessage.warning(error?.message || '加载回访任务失败')
  } finally {
    loading.value = false
  }
}

async function submitRecord() {
  if (!task.value || !canAddRecord.value || saving.value) return
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  saving.value = true
  try {
    await addFollowRecord(task.value.id, {
      visitTime: form.visitTime || undefined,
      summary: form.summary.trim(),
      lifeStatus: form.lifeStatus.trim(),
      healthStatus: form.healthStatus.trim(),
      risk: form.risk.trim(),
      suggestion: form.suggestion.trim(),
    })
    ElMessage.success('回访记录已提交')
    await loadDetail()
    resetForm()
  } catch (error) {
    ElMessage.warning(error?.message || '提交回访记录失败')
  } finally {
    saving.value = false
  }
}

watch(taskId, loadDetail, { immediate: true })
</script>

<style scoped>
.follow-task-detail-panel {
  display: grid;
  gap: 16px;
}

.follow-task-hero-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr;
  gap: 16px;
}

.follow-task-pet-card,
.follow-task-person-card {
  min-height: 150px;
}

.follow-task-pet-card {
  display: flex;
  gap: 16px;
  align-items: center;
}

.follow-task-pet-cover {
  display: grid;
  place-items: center;
  width: 96px;
  height: 96px;
  overflow: hidden;
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(255, 244, 232, 0.98), rgba(255, 232, 211, 0.92));
  color: #c47a3a;
}

.follow-task-pet-cover img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.follow-task-pet-cover :deep(.el-icon) {
  font-size: 34px;
}

.follow-task-pet-copy {
  display: grid;
  gap: 6px;
}

.follow-task-pet-copy span,
.follow-task-pet-copy p,
.follow-task-person-card p,
.follow-task-person-row span,
.follow-task-record-body p {
  color: var(--muted);
}

.follow-task-pet-copy strong {
  font-size: 20px;
  color: var(--text);
}

.follow-task-person-card {
  display: grid;
  gap: 12px;
}

.follow-task-person-card p {
  margin: 0;
}

.follow-task-person-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.follow-task-avatar {
  flex: none;
  background: rgba(255, 233, 214, 0.9);
  color: #8a4b1d;
}

.follow-task-person-row div {
  display: grid;
  gap: 4px;
}

.follow-task-person-row strong {
  color: var(--text);
}

.follow-task-status-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.follow-task-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.follow-task-section-head h3 {
  margin: 0;
  font-size: 16px;
  color: var(--text);
}

.follow-task-record-card {
  display: grid;
  gap: 14px;
}

.follow-task-record-body {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.follow-task-record-body section {
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: rgba(255, 253, 249, 0.92);
}

.follow-task-record-body h4 {
  margin: 0 0 8px;
  color: #5d3927;
  font-size: 14px;
}

.follow-task-record-body p {
  margin: 0;
  line-height: 1.7;
  white-space: pre-wrap;
}

.follow-task-record-form {
  display: grid;
  gap: 16px;
}

.follow-task-form-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.follow-task-span-2 {
  grid-column: span 2;
}

.follow-task-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 1024px) {
  .follow-task-hero-grid,
  .follow-task-status-row,
  .follow-task-record-body,
  .follow-task-form-grid {
    grid-template-columns: 1fr;
  }

  .follow-task-span-2 {
    grid-column: span 1;
  }
}
</style>
