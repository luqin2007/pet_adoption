<template>
  <el-card class="profile-card">
    <template #header>
      <div class="profile-card-header">
        <strong>健康评估详情</strong>
      </div>
    </template>

    <section v-loading="loading" class="action-form-panel">
      <div v-if="assessment" class="health-detail-head">
        <div>
          <h2>{{ assessment.petName || '未命名宠物' }}</h2>
          <p>{{ assessment.petType || '宠物' }} · {{ assessment.petSex || '未知' }} · {{ assessment.petAge ?? 0 }} 月</p>
        </div>
        <div class="health-detail-actions">
          <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回</el-button>
        </div>
      </div>

      <section v-if="assessment" class="console-detail-grid">
        <section class="console-detail-section">
          <h3>评分</h3>
          <div class="health-score-grid">
            <article>
              <span>体况评分</span>
              <strong>{{ assessment.scoreBcs ?? 0 }}</strong>
            </article>
            <article>
              <span>精神状态</span>
              <strong>{{ assessment.scoreMental ?? 0 }}</strong>
            </article>
            <article>
              <span>食欲评分</span>
              <strong>{{ assessment.scoreAppetite ?? 0 }}</strong>
            </article>
          </div>
        </section>

        <section class="console-detail-section">
          <h3>基础信息</h3>
          <dl class="console-detail-list console-detail-list-inline">
            <div><dt>体重</dt><dd>{{ assessment.weight ?? '' }} kg</dd></div>
            <div><dt>评估人</dt><dd>{{ assessment.username || '' }}</dd></div>
            <div><dt>评估时间</dt><dd>{{ formatDate(assessment.createTime) }}</dd></div>
            <div><dt>品种</dt><dd>{{ assessment.petBreed || '' }}</dd></div>
          </dl>
        </section>

        <section class="console-detail-section console-detail-section-wide">
          <h3>评估摘要</h3>
          <p class="health-detail-summary">{{ assessment.summary || '' }}</p>
        </section>
      </section>

      <el-empty v-else-if="!loading" description="健康评估不存在">
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回健康评估</el-button>
      </el-empty>
    </section>
  </el-card>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getHealthAssessment } from '../api/services'

const route = useRoute()
const router = useRouter()
const props = defineProps({
  backPath: {
    type: String,
    default: '/console/medical/health',
  },
})
const loading = ref(false)
const assessment = ref(null)

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function goBack() {
  router.push(props.backPath)
}

async function loadAssessment() {
  loading.value = true
  try {
    assessment.value = await getHealthAssessment(route.params.id)
  } catch (error) {
    assessment.value = null
    ElMessage.warning(error?.message || '加载健康评估失败')
  } finally {
    loading.value = false
  }
}

watch(() => route.params.id, loadAssessment)

onMounted(() => {
  loadAssessment()
})
</script>

<style scoped>
.health-detail-head,
.health-detail-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.health-detail-head {
  justify-content: space-between;
  margin-bottom: 18px;
}

.health-detail-head h2 {
  margin: 0;
  color: #5d3927;
  font-size: 24px;
  line-height: 1.3;
}

.health-detail-head p,
.health-detail-summary {
  margin: 6px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.health-score-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.health-score-grid article {
  border: 1px solid rgba(243, 223, 204, 0.9);
  border-radius: 12px;
  background: rgba(255, 248, 240, 0.72);
  padding: 12px;
}

.health-score-grid span {
  display: block;
  color: var(--muted);
  font-size: 12px;
}

.health-score-grid strong {
  display: block;
  margin-top: 6px;
  color: var(--primary-strong);
  font-size: 24px;
}
</style>
