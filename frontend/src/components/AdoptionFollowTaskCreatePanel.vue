<template>
  <el-card class="profile-card pet-admin-card" v-loading="loading">
    <template #header>
      <div class="profile-card-header">
        <strong>添加回访任务</strong>
        <span>{{ headerSubtitle }}</span>
      </div>
    </template>

    <section v-if="application" class="action-form-panel adoption-follow-task-panel">
      <div class="console-detail-toolbar">
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回列表</el-button>
        <el-tag :type="statusTagType(application.status)" effect="plain">{{ statusText(application.status) }}</el-tag>
      </div>

      <div class="follow-task-hero-grid">
        <section class="console-detail-section follow-task-pet-card">
          <div class="follow-task-pet-cover">
            <img v-if="application.petCover" :src="application.petCover" :alt="summaryPetName" />
            <el-icon v-else><PictureFilled /></el-icon>
          </div>
          <div class="follow-task-pet-copy">
            <span>宠物</span>
            <button class="table-primary-link follow-task-link" type="button" @click="goPetProfile">
              <strong>{{ summaryPetName }}</strong>
            </button>
            <p>#{{ application.petId || '—' }}</p>
          </div>
        </section>

        <div class="follow-task-side-grid">
          <section class="console-detail-section follow-task-person-card">
            <p>领养人</p>
            <div class="follow-task-person-row">
              <span class="user-pill-avatar follow-task-avatar">
                <img v-if="application.applicantAvatar" :src="application.applicantAvatar" :alt="summaryApplicantName" />
                <span v-else>{{ avatarInitial(summaryApplicantName) }}</span>
              </span>
              <div>
                <strong>{{ summaryApplicantName }}</strong>
                <span>{{ application.applicantPhone || '联系方式待补充' }}</span>
              </div>
            </div>
          </section>

          <section class="console-detail-section follow-task-person-card follow-task-volunteer-card">
            <p>负责志愿者</p>
            <el-select
              v-model="form.volunteerId"
              filterable
              clearable
              class="full-width-control"
              placeholder="选择志愿者"
              :loading="volunteerLoading"
              :disabled="volunteerLoading || !volunteerOptions.length"
            >
              <el-option
                v-for="item in volunteerOptions"
                :key="volunteerValue(item)"
                :label="volunteerLabel(item)"
                :value="volunteerValue(item)"
              >
                <div class="follow-volunteer-option">
                  <el-avatar :size="30" :src="item.avatar">
                    {{ avatarInitial(volunteerName(item)) }}
                  </el-avatar>
                  <div class="follow-volunteer-meta">
                    <strong>{{ volunteerName(item) }}</strong>
                    <span>{{ item.phone || '电话待补充' }}</span>
                  </div>
                </div>
              </el-option>
            </el-select>
            <div v-if="selectedVolunteer" class="follow-volunteer-preview">
              <el-avatar :size="36" :src="selectedVolunteer.avatar">
                {{ avatarInitial(volunteerName(selectedVolunteer)) }}
              </el-avatar>
              <div>
                <strong>{{ volunteerName(selectedVolunteer) }}</strong>
                <span>{{ selectedVolunteer.phone || '电话待补充' }}</span>
              </div>
            </div>
            <el-empty v-else-if="!volunteerLoading && !volunteerOptions.length" description="暂无可用志愿者" />
          </section>
        </div>
      </div>

      <section class="console-detail-section console-detail-section-wide">
        <dl class="console-detail-list console-detail-list-row follow-task-status-row">
          <div><dt>申请状态</dt><dd>{{ statusText(application.status) }}</dd></div>
          <div><dt>审核时间</dt><dd>{{ formatDate(application.reviewTime) }}</dd></div>
          <div><dt>领养时间</dt><dd>{{ formatDate(application.adoptTime) }}</dd></div>
          <div class="follow-task-time-item">
            <dt>回访时间</dt>
            <dd>
              <el-date-picker
                v-model="form.planTime"
                type="datetime"
                value-format="YYYY-MM-DDTHH:mm:ss.SSS"
                placeholder="选择回访时间"
                class="full-width-control"
              />
            </dd>
          </div>
        </dl>
      </section>

      <el-alert v-if="blockedReason" :title="blockedReason" type="warning" show-icon :closable="false" />

      <div class="follow-task-footer">
        <el-button class="soft-btn" @click="goBack">取消</el-button>
        <el-button class="warm-btn" :loading="saving" :disabled="!canSubmit" @click="submitFollowTask">创建回访任务</el-button>
      </div>
    </section>

    <section v-else-if="!loading" class="pet-admin-section">
      <el-empty description="未找到领养申请">
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回列表</el-button>
      </el-empty>
    </section>
  </el-card>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, PictureFilled } from '@element-plus/icons-vue'
import { addFollowTask, getAdoptApplication } from '../api/services'
import { getVolunteerProfiles } from '../api/volunteer'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const volunteerLoading = ref(false)
const application = ref(null)
const volunteerOptions = ref([])
const form = reactive({
  volunteerId: '',
  planTime: '',
})

const applicationId = computed(() => String(route.params.id || ''))
const routePetName = computed(() => String(route.query.pet || ''))
const routeApplicantName = computed(() => String(route.query.applicant || ''))

const statusOptions = [
  { label: '已提交', value: 'CREATE' },
  { label: '审核通过', value: 'PASS' },
  { label: '审核拒绝', value: 'REJECT' },
  { label: '协议草拟中', value: 'AGREEMENT_DRAFT' },
  { label: '待确认', value: 'AGREEMENT_PENDING_CONFIRM' },
  { label: '协议已签署', value: 'AGREEMENT_SIGNED' },
  { label: '回访中', value: 'TRACKING' },
  { label: '流程完成', value: 'FINISH' },
  { label: '已取消', value: 'CANCEL' },
]

const statusMap = Object.fromEntries(statusOptions.map((item) => [item.value, item.label]))

const summaryPetName = computed(() => application.value?.petName || routePetName.value || '未命名宠物')
const summaryApplicantName = computed(() => application.value?.applicantName || routeApplicantName.value || '未命名领养人')
const headerSubtitle = computed(() => {
  const parts = [summaryPetName.value, summaryApplicantName.value].filter(Boolean)
  return parts.length ? parts.join(' · ') : '为领养申请创建回访任务'
})
const blockedReason = computed(() => {
  if (!application.value) return ''
  if (!['TRACKING', 'AGREEMENT_SIGNED'].includes(application.value.status)) return '当前申请未进入回访阶段，不能创建回访任务'
  return ''
})
const selectedVolunteer = computed(() => {
  const value = String(form.volunteerId || '')
  return volunteerOptions.value.find((item) => volunteerValue(item) === value) || null
})
const canSubmit = computed(() => Boolean(application.value && !blockedReason.value && form.volunteerId && form.planTime && !saving.value))

function statusText(value) {
  return statusMap[value] || value || ''
}

function statusTagType(value) {
  if (value === 'PASS' || value === 'AGREEMENT_SIGNED' || value === 'FINISH') return 'success'
  if (value === 'REJECT' || value === 'CANCEL') return 'info'
  if (value === 'AGREEMENT_DRAFT' || value === 'TRACKING') return 'primary'
  if (value === 'AGREEMENT_PENDING_CONFIRM') return 'warning'
  return 'warning'
}

function avatarInitial(value) {
  return String(value || '回').slice(0, 1)
}

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function volunteerName(item) {
  return item?.realName || item?.username || '未命名志愿者'
}

function volunteerLabel(item) {
  const phone = item?.phone ? ` · ${item.phone}` : ''
  return `${volunteerName(item)}${phone}`
}

function volunteerValue(item) {
  return String(item?.userId || item?.id || '')
}

function goBack() {
  router.push('/console/adoption/adopts')
}

function goPetProfile() {
  if (application.value?.petId) {
    router.push(`/pets/${application.value.petId}`)
  }
}

async function loadApplication() {
  if (!applicationId.value) return
  try {
    application.value = await getAdoptApplication(applicationId.value)
  } catch (error) {
    application.value = null
    ElMessage.warning(error?.message || '加载领养申请失败')
  }
}

async function loadVolunteerOptions() {
  volunteerLoading.value = true
  try {
    const result = await getVolunteerProfiles({
      size: 100,
      status: ['ACTIVE'],
    })
    volunteerOptions.value = Array.isArray(result?.records) ? result.records : []
  } catch (error) {
    volunteerOptions.value = []
    ElMessage.warning(error?.message || '加载志愿者列表失败')
  } finally {
    volunteerLoading.value = false
  }
}

async function loadPage() {
  if (!applicationId.value) {
    ElMessage.warning('缺少领养申请信息')
    router.push('/console/adoption/adopts')
    return
  }

  loading.value = true
  application.value = null
  form.volunteerId = ''
  try {
    await loadApplication()
    await loadVolunteerOptions()
  } finally {
    loading.value = false
  }
}

async function submitFollowTask() {
  if (!application.value) {
    ElMessage.warning('领养申请不存在')
    return
  }
  if (blockedReason.value) {
    ElMessage.warning(blockedReason.value)
    return
  }
  if (!form.volunteerId) {
    ElMessage.warning('请选择志愿者')
    return
  }
  if (!form.planTime) {
    ElMessage.warning('请选择回访时间')
    return
  }
  if (saving.value) return

  saving.value = true
  try {
    const response = await addFollowTask(applicationId.value, {
      volunteerId: form.volunteerId,
      planTime: form.planTime,
    })
    ElMessage.success('回访任务已创建')
    if (response?.id) {
      router.push({
        name: 'console-adoption-follow-task-detail',
        params: { id: String(response.id) },
      })
    } else {
      router.push('/console/adoption/follow-records')
    }
  } catch (error) {
    ElMessage.warning(error?.message || '创建回访任务失败')
  } finally {
    saving.value = false
  }
}

watch(applicationId, () => {
  void loadPage()
}, { immediate: true })
</script>

<style scoped>
.adoption-follow-task-panel {
  display: grid;
  gap: 16px;
}

.follow-task-hero-grid {
  display: grid;
  grid-template-columns: minmax(320px, 1.1fr) minmax(300px, 0.9fr);
  gap: 14px;
}

.follow-task-side-grid {
  display: grid;
  grid-template-rows: repeat(2, minmax(124px, auto));
  gap: 14px;
}

.follow-task-pet-card {
  display: flex;
  align-items: center;
  gap: 16px;
  min-height: 262px;
}

.follow-task-pet-cover {
  display: grid;
  place-items: center;
  flex: none;
  width: 150px;
  height: 206px;
  overflow: hidden;
  border-radius: 18px;
  color: #c47a3a;
  background: linear-gradient(180deg, rgba(255, 244, 232, 0.98), rgba(255, 232, 211, 0.92));
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
  min-width: 0;
}

.follow-task-pet-copy span,
.follow-task-pet-copy p,
.follow-task-person-card p,
.follow-task-person-row span {
  color: var(--muted);
}

.follow-task-pet-copy strong {
  font-size: 20px;
  color: var(--text);
}

.follow-task-person-card {
  display: grid;
  align-content: center;
  gap: 14px;
  min-height: 124px;
  padding-top: 16px;
  padding-bottom: 16px;
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
}

.follow-task-person-row div {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.follow-task-person-row strong {
  color: var(--text);
}

.follow-task-volunteer-card {
  align-content: start;
}

.follow-task-status-row {
  display: grid;
  grid-template-columns: minmax(120px, 0.8fr) minmax(150px, 1fr) minmax(150px, 1fr) minmax(260px, 1.4fr);
  gap: 12px;
}

.follow-task-time-item :deep(.el-date-editor.el-input) {
  width: 100%;
}

.follow-task-link {
  display: inline-flex;
  align-items: center;
  padding: 0;
  border: 0;
  background: transparent;
  font: inherit;
  cursor: pointer;
  text-align: left;
}

.follow-task-link strong {
  display: block;
  margin: 0;
  color: inherit;
  font-size: 20px;
  line-height: 1.25;
}

.follow-task-link:hover,
.follow-task-link:focus-visible {
  color: var(--primary-strong);
}

.follow-volunteer-option {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.follow-volunteer-meta {
  display: grid;
  gap: 2px;
  min-width: 0;
}

.follow-volunteer-meta strong,
.follow-volunteer-preview strong {
  color: #5d3927;
  font-size: 14px;
  line-height: 1.4;
}

.follow-volunteer-meta span,
.follow-volunteer-preview span,
.follow-task-note {
  color: var(--muted);
  font-size: 12px;
  line-height: 1.6;
}

.follow-volunteer-preview {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border: 1px solid rgba(243, 223, 204, 0.9);
  border-radius: 12px;
  background: rgba(255, 253, 249, 0.95);
}

.follow-volunteer-preview div {
  display: grid;
  gap: 2px;
}

.follow-task-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

@media (max-width: 980px) {
  .follow-task-hero-grid,
  .follow-task-status-row {
    grid-template-columns: 1fr;
  }

  .follow-task-pet-card {
    min-height: 0;
  }

  .follow-task-pet-cover {
    width: 96px;
    height: 96px;
  }
}
</style>
