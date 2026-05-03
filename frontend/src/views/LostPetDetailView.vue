<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { getLostPet } from '../api/lost'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const record = ref(null)

const lostPetId = computed(() => String(route.params.id || ''))
const statusText = computed(() => mapStatusText(record.value?.status))
const statusTone = computed(() => mapStatusTone(record.value?.status))
const basicItems = computed(() => [
  { label: '宠物类型', value: record.value?.type || '待补充' },
  { label: '品种', value: record.value?.breed || '待补充' },
  { label: '性别', value: record.value?.sex || '待补充' },
  { label: '年龄', value: formatAge(record.value?.age) },
])

function mapStatusText(status) {
  const map = {
    SEARCHING: '正在寻找',
    CLAIMED: '已找回',
    CLOSED: '已关闭',
  }
  return map[status] || status || '状态待补充'
}

function mapStatusTone(status) {
  const map = {
    SEARCHING: 'warning',
    CLAIMED: 'success',
    CLOSED: 'info',
  }
  return map[status] || ''
}

function formatAge(age) {
  if (age === undefined || age === null || age === '') {
    return '年龄待补充'
  }
  const months = Number(age)
  if (Number.isNaN(months)) {
    return '年龄待补充'
  }
  if (months >= 12) {
    const years = Math.floor(months / 12)
    const remainMonths = months % 12
    return remainMonths > 0 ? `约 ${years} 岁 ${remainMonths} 个月` : `约 ${years} 岁`
  }
  return `约 ${months} 个月`
}

function formatDate(value) {
  if (!value) {
    return '时间待补充'
  }
  return String(value).slice(0, 10)
}

function formatLocation(location) {
  if (!location) {
    return '地点待补充'
  }
  return [location.province, location.city, location.district, location.detailAddress || location.address]
    .filter(Boolean)
    .join(' · ') || '地点待补充'
}

function goBack() {
  router.push('/lost')
}

async function loadLostPet() {
  if (!lostPetId.value) {
    record.value = null
    return
  }

  loading.value = true
  try {
    record.value = await getLostPet(lostPetId.value)
  } catch {
    record.value = null
  } finally {
    loading.value = false
  }
}

watch(
  () => lostPetId.value,
  () => {
    loadLostPet()
  },
)

onMounted(() => {
  loadLostPet()
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main lost-detail-page" v-loading="loading">
      <section v-if="record" class="lost-detail-hero">
        <div class="lost-detail-cover">
          <img v-if="record.petCover" :src="record.petCover" :alt="record.name || '走失宠物'" loading="lazy" />
          <div v-else class="lost-detail-cover-placeholder">暂无图片</div>
        </div>

        <div class="lost-detail-main">
          <div class="lost-detail-topbar">
            <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回丢失宠物</el-button>
            <el-tag :type="statusTone" effect="dark">{{ statusText }}</el-tag>
          </div>

          <div class="lost-detail-kicker">
            <span><Icon icon="mdi:calendar-alert-outline" />{{ formatDate(record.lostTime) }}</span>
            <span><Icon icon="mdi:map-marker-radius-outline" />{{ formatLocation(record.location) }}</span>
          </div>

          <h1>{{ record.name || '走失宠物' }}</h1>
          <p class="lost-detail-summary">{{ record.description || '报备人暂未补充走失经过。' }}</p>

          <div class="lost-detail-contact">
            <span><Icon icon="mdi:account-heart-outline" />{{ record.ownerName || '报备人' }}</span>
            <span><Icon icon="mdi:phone-outline" />{{ record.contactPhone || '电话待补充' }}</span>
          </div>
        </div>
      </section>

      <section v-if="record" class="lost-detail-grid">
        <div class="lost-detail-section">
          <h2>宠物信息</h2>
          <dl class="lost-detail-list">
            <div v-for="item in basicItems" :key="item.label">
              <dt>{{ item.label }}</dt>
              <dd>{{ item.value }}</dd>
            </div>
          </dl>
        </div>

        <div class="lost-detail-section">
          <h2>明显特征</h2>
          <p>{{ record.features || '暂未补充明显特征。' }}</p>
        </div>

        <div class="lost-detail-section lost-detail-section-wide">
          <h2>丢失位置</h2>
          <p>{{ formatLocation(record.location) }}</p>
        </div>
      </section>

      <section v-else-if="!loading" class="pet-profile-empty">
        <el-empty description="未找到对应的走失宠物">
          <el-button class="warm-btn" @click="goBack">返回丢失宠物</el-button>
        </el-empty>
      </section>
    </main>

    <AppFooter />
  </div>
</template>
