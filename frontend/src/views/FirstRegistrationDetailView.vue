<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { getFirstVisitRegistration } from '../api/services'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const registration = ref(null)
const registrationId = computed(() => String(route.params.id || ''))

function formatDate(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function goBack() {
  router.push('/console/medical/first')
}

function goPetProfile() {
  if (registration.value?.petId) router.push(`/pets/${registration.value.petId}`)
}

async function loadRegistration() {
  if (!registrationId.value) return
  loading.value = true
  try {
    registration.value = await getFirstVisitRegistration(registrationId.value)
  } catch (error) {
    registration.value = null
    ElMessage.warning(error?.message || '加载初诊登记详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadRegistration()
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main" v-loading="loading">
      <section v-if="registration" class="pet-profile-section console-detail-page">
        <div class="pet-profile-head">
          <div>
            <h1 class="pet-profile-name">{{ registration.name || '未命名' }}</h1>
            <p class="pet-profile-breed">{{ registration.type || '宠物' }} · {{ registration.breed || '品种待补充' }} · {{ registration.sex || '未知' }}</p>
            <div class="pet-profile-meta">
              <div class="pet-profile-meta-item">
                <span class="pet-profile-meta-label">登记人</span>
                <span class="pet-profile-meta-value">{{ registration.username || '—' }}</span>
              </div>
              <div class="pet-profile-meta-item">
                <span class="pet-profile-meta-label">登记时间</span>
                <span class="pet-profile-meta-value">{{ formatDate(registration.createTime) }}</span>
              </div>
            </div>
          </div>
          <div class="pet-profile-head-actions">
            <button class="pet-profile-pet-button" type="button" @click="goPetProfile">查看宠物档案</button>
            <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回初诊登记</el-button>
          </div>
        </div>

        <div class="console-detail-grid">
          <section class="console-detail-section console-detail-section-wide">
            <h3>基础体征</h3>
            <dl class="console-detail-list console-detail-list-row">
              <div><dt>年龄</dt><dd>{{ registration.age ?? '—' }} 月</dd></div>
              <div><dt>体重</dt><dd>{{ registration.weight ?? '—' }} kg</dd></div>
              <div><dt>体温</dt><dd>{{ registration.temperature ?? '—' }} ℃</dd></div>
            </dl>
          </section>

          <section class="console-detail-section">
            <h3>免疫史</h3>
            <dl class="console-detail-list">
              <div v-for="(item, index) in registration.immunity || []" :key="index">
                <dt>{{ item.medicine || item.illness || '免疫记录' }}</dt>
                <dd>{{ item.illness || '疾病待补充' }} · {{ item.count ?? 0 }}/{{ item.total ?? 0 }} · {{ formatDate(item.immunityTime) }}</dd>
              </div>
              <div v-if="!(registration.immunity || []).length"><dt>记录</dt><dd>暂无</dd></div>
            </dl>
          </section>

          <section class="console-detail-section">
            <h3>过敏史</h3>
            <dl class="console-detail-list">
              <div v-for="(item, index) in registration.allergy || []" :key="index">
                <dt>{{ item.source || '过敏源' }}</dt>
                <dd>{{ item.reaction || '反应待补充' }} · {{ formatDate(item.discoveryTime) }}</dd>
              </div>
              <div v-if="!(registration.allergy || []).length"><dt>记录</dt><dd>暂无</dd></div>
            </dl>
          </section>
        </div>

        <section v-if="(registration.results || []).length" class="console-detail-section">
          <h3>病例</h3>
          <dl class="console-detail-list">
            <div v-for="item in registration.results" :key="item.id">
              <dt>{{ item.summary || '病历记录' }}</dt>
              <dd>{{ formatDate(item.createTime) }}</dd>
            </div>
          </dl>
        </section>
      </section>

      <section v-else-if="!loading" class="pet-profile-section">
        <el-empty description="未找到初诊登记">
          <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回初诊登记</el-button>
        </el-empty>
      </section>
    </main>

    <AppFooter />
  </div>
</template>