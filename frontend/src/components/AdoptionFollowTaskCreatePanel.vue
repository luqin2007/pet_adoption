<template>
  <el-card class="profile-card pet-admin-card" v-loading="loading">
    <template #header>
      <div class="profile-card-header">
        <strong>添加回访任务</strong>
        <span>{{ headerSubtitle }}</span>
      </div>
    </template>

    <section v-if="application" class="action-form-panel adoption-follow-task-panel">
      <div class="follow-task-head">
        <div>
          <h2>{{ summaryPetName }}</h2>
          <p>{{ summarySubtitle }}</p>
        </div>
        <div class="follow-task-head-actions">
          <el-tag :type="statusTagType(application.status)" effect="plain">{{ statusText(application.status) }}</el-tag>
          <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回</el-button>
        </div>
      </div>

      <div class="console-detail-summary follow-task-summary">
        <div>
          <span>宠物</span>
          <button class="table-primary-link follow-task-link" type="button" @click="goPetProfile">
            <strong>{{ summaryPetName }}</strong>
          </button>
          <p>{{ petMetaText }}</p>
        </div>
        <div>
          <span>申请人</span>
          <strong>{{ summaryApplicantName }}</strong>
          <p>{{ application.applicantPhone || '联系方式待补充' }}</p>
        </div>
        <div>
          <span>当前状态</span>
          <strong>{{ statusText(application.status) }}</strong>
          <p>{{ blockedReason || '可以创建回访任务' }}</p>
        </div>
      </div>

      <el-alert v-if="blockedReason" :title="blockedReason" type="warning" show-icon :closable="false" />

      <div class="console-detail-grid follow-task-grid">
        <section class="console-detail-section">
          <h3>负责志愿者</h3>
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
            <el-avatar :size="42" :src="selectedVolunteer.avatar">
              {{ avatarInitial(volunteerName(selectedVolunteer)) }}
            </el-avatar>
            <div>
              <strong>{{ volunteerName(selectedVolunteer) }}</strong>
              <span>{{ selectedVolunteer.phone || '电话待补充' }}</span>
            </div>
          </div>
          <el-empty v-else-if="!volunteerLoading && !volunteerOptions.length" description="暂无可用志愿者" />
        </section>

        <section class="console-detail-section">
          <h3>回访时间</h3>
          <el-date-picker
            v-model="form.planTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss.SSS"
            placeholder="选择回访时间"
            class="full-width-control"
          />
          <p class="follow-task-note">请填写预计回访时间，方便志愿者安排后续回访。</p>
        </section>

        <section class="console-detail-section console-detail-section-wide">
          <h3>备注</h3>
          <el-input
            v-model="form.remark"
            type="textarea"
            :autosize="{ minRows: 4, maxRows: 7 }"
            class="full-width-control"
            placeholder="填写回访提醒、关注重点或特殊安排"
          />
        </section>
      </div>

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
import { ArrowLeft } from '@element-plus/icons-vue'
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
  remark: '',
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
const summaryApplicantName = computed(() => application.value?.applicantName || routeApplicantName.value || '未命名申请人')
const summarySubtitle = computed(() => {
  const parts = [summaryApplicantName.value, application.value?.applicantPhone].filter(Boolean)
  return parts.length ? parts.join(' · ') : '为领养申请创建回访任务'
})
const headerSubtitle = computed(() => {
  const parts = [summaryPetName.value, summaryApplicantName.value].filter(Boolean)
  return parts.length ? parts.join(' · ') : '为领养申请创建回访任务'
})
const petMetaText = computed(() => {
  const parts = [application.value?.petType, application.value?.petBreed].filter(Boolean)
  return parts.length ? parts.join(' · ') : '信息待补充'
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
      remark: form.remark.trim() || undefined,
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
  gap: 18px;
}

.follow-task-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.follow-task-head h2 {
  margin: 0;
  color: #5d3927;
  font-size: 24px;
  line-height: 1.3;
}

.follow-task-head p {
  margin: 6px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.follow-task-head-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.follow-task-summary {
  align-items: stretch;
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

.follow-task-grid .console-detail-section {
  min-width: 0;
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
  margin-top: 12px;
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
  .follow-task-head {
    flex-direction: column;
  }
}
</style>
