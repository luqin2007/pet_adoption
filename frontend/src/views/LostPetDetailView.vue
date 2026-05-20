<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { ArrowLeft, ArrowRight, Close, PictureFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { getLostPet, getSimilarPets, markPetMismatch, deleteCachedResult } from '../api/lost'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'
import { useUserStore } from '../stores/user'
import { ROLE, hasRole, petStatusText } from '../utils/roles'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const loadingSimilarPets = ref(false)
const refreshingSimilarPets = ref(false)
const record = ref(null)
const similarPets = ref([])
const previewVisible = ref(false)
const previewIndex = ref(0)
const previewThumbsVisible = ref(true)

const lostPetId = computed(() => String(route.params.id || ''))
const mediaItems = computed(() => Array.isArray(record.value?.files) ? record.value.files : [])
const loginRole = computed(() => Number(userStore.profile?.role || 0))
const loginUserId = computed(() => String(userStore.profile?.id || ''))
const isWorker = computed(() => hasRole(loginRole.value, ROLE.WORKER) || hasRole(loginRole.value, ROLE.ADMIN))
const canViewSimilarPets = computed(() => Boolean(record.value && userStore.isLoggedIn && (isWorker.value || String(record.value.ownerId || '') === loginUserId.value)))
const statusText = computed(() => mapStatusText(record.value?.status))
const statusTone = computed(() => mapStatusTone(record.value?.status))
const basicItems = computed(() => [
  { label: '宠物类型', value: record.value?.type || '待补充' },
  { label: '品种', value: record.value?.breed || '待补充' },
  { label: '性别', value: record.value?.sex || '待补充' },
  { label: '年龄', value: formatAge(record.value?.age) },
])

function isImage(media) {
  return media?.type === 'IMAGE' || media?.assetUrl?.match(/\.(png|jpe?g|webp|gif|bmp|avif)(\?|$)/i)
}

function openPreview(item) {
  const idx = mediaItems.value.findIndex((m) => m.id === item.id)
  previewIndex.value = idx >= 0 ? idx : 0
  previewVisible.value = true
}

function closePreview() {
  previewVisible.value = false
  const video = document.querySelector('.image-viewer-video')
  if (video) video.pause()
}

function previewPrev() {
  if (mediaItems.value.length <= 1) return
  previewIndex.value = (previewIndex.value - 1 + mediaItems.value.length) % mediaItems.value.length
}

function previewNext() {
  if (mediaItems.value.length <= 1) return
  previewIndex.value = (previewIndex.value + 1) % mediaItems.value.length
}

function handlePreviewKeydown(e) {
  if (!previewVisible.value) return
  if (e.key === 'ArrowLeft') previewPrev()
  else if (e.key === 'ArrowRight') previewNext()
  else if (e.key === 'Escape') closePreview()
}

function mapStatusText(status) {
  const map = {
    SEARCHING: '正在寻找',
    CLAIMING: '认领中',
    CLAIMED: '已找回',
    CLOSED: '已关闭',
  }
  return map[status] || status || '状态待补充'
}

function mapStatusTone(status) {
  const map = {
    SEARCHING: 'warning',
    CLAIMING: 'primary',
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

function petLocationText(pet) {
  const location = pet?.locations?.[0]
  if (!location) return '位置待补充'
  return [location.province, location.city, location.district, location.detailAddress].filter(Boolean).join(' · ') || '位置待补充'
}

function goBack() {
  router.push('/lost')
}

function goPet(pet) {
  if (pet?.id) router.push(`/pets/${pet.id}`)
}

async function loadSimilarPets() {
  if (!lostPetId.value || !canViewSimilarPets.value) {
    similarPets.value = []
    return
  }
  loadingSimilarPets.value = true
  try {
    const result = await getSimilarPets(lostPetId.value)
    similarPets.value = Array.isArray(result) ? result : []
  } catch (error) {
    similarPets.value = []
    ElMessage.warning(error?.message || '加载相似流浪宠物失败')
  } finally {
    loadingSimilarPets.value = false
  }
}

async function dismissSimilarPet(pet) {
  if (!pet?.id || !lostPetId.value) return
  try {
    const result = await markPetMismatch(lostPetId.value, pet.id)
    similarPets.value = Array.isArray(result) ? result : similarPets.value.filter((item) => item.id !== pet.id)
    ElMessage.success('已标记为非走失宠物')
  } catch (error) {
    ElMessage.warning(error?.message || '标记失败')
  }
}

async function refreshSimilarPets() {
  if (!lostPetId.value) return
  refreshingSimilarPets.value = true
  try {
    await deleteCachedResult(lostPetId.value)
    await loadSimilarPets()
    ElMessage.success('已刷新匹配结果')
  } catch (e) {
    if (e?.status === 429) {
      ElMessage.warning('操作过于频繁，请稍后再试')
    } else {
      ElMessage.warning(e?.message || '刷新失败')
    }
  } finally {
    refreshingSimilarPets.value = false
  }
}

async function loadLostPet() {
  if (!lostPetId.value) {
    record.value = null
    return
  }

  loading.value = true
  try {
    record.value = await getLostPet(lostPetId.value)
    await loadSimilarPets()
  } catch {
    record.value = null
    similarPets.value = []
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
  document.addEventListener('keydown', handlePreviewKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handlePreviewKeydown)
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

        <div v-if="mediaItems.length" class="lost-detail-section lost-detail-section-wide">
          <h2>相关图片/视频</h2>
          <div class="lost-media-strip">
            <div v-for="item in mediaItems" :key="item.id" class="lost-media-strip-item" @click="openPreview(item)">
              <img v-if="isImage(item)" :src="item.assetUrl" :alt="item.name" loading="lazy" />
              <video v-else :src="item.assetUrl" preload="metadata" />
              <span v-if="item.name" class="lost-media-strip-label">{{ item.name }}</span>
            </div>
          </div>
        </div>

        <div v-if="canViewSimilarPets" class="lost-detail-section lost-detail-section-wide">
          <h2>相似流浪宠物
            <el-button size="small" :loading="refreshingSimilarPets" plain @click="refreshSimilarPets" style="margin-left:12px">刷新</el-button>
          </h2>
          <div v-loading="loadingSimilarPets" class="lost-similar-grid">
            <article v-for="pet in similarPets" :key="pet.id" class="lost-similar-card">
              <div class="lost-similar-cover">
                <img v-if="pet.cover" :src="pet.cover" :alt="pet.name || '流浪宠物'" />
                <span v-else>暂无图片</span>
              </div>
              <div class="lost-similar-body">
                <strong>{{ pet.name || '未命名宠物' }}</strong>
                <span>{{ pet.type || '宠物' }} · {{ pet.breed || '品种待补充' }} · {{ petStatusText(pet.status) }}</span>
                <small>{{ petLocationText(pet) }}</small>
                <div class="lost-similar-actions">
                  <el-button text type="primary" @click="goPet(pet)">查看档案</el-button>
                  <el-button text type="danger" @click="dismissSimilarPet(pet)">不是我的宠物</el-button>
                </div>
              </div>
            </article>
            <el-empty v-if="!loadingSimilarPets && !similarPets.length" description="暂无相似流浪宠物" />
          </div>
        </div>
      </section>

      <section v-else-if="!loading" class="pet-profile-empty">
        <el-empty description="未找到对应的走失宠物">
          <el-button class="warm-btn" @click="goBack">返回丢失宠物</el-button>
        </el-empty>
      </section>
    </main>

    <Transition name="viewer-fade">
      <div v-if="previewVisible" class="image-viewer-overlay" :class="{ 'image-viewer-thumbs-hidden': !previewThumbsVisible || mediaItems.length <= 1 }" @click.self="closePreview">
        <button class="image-viewer-close" @click="closePreview"><el-icon><Close /></el-icon></button>
        <button v-if="mediaItems.length > 1" class="image-viewer-arrow image-viewer-prev" @click.stop="previewPrev"><el-icon><ArrowLeft /></el-icon></button>
        <button v-if="mediaItems.length > 1" class="image-viewer-arrow image-viewer-next" @click.stop="previewNext"><el-icon><ArrowRight /></el-icon></button>

        <div class="image-viewer-main">
          <img v-if="isImage(mediaItems[previewIndex])" :src="mediaItems[previewIndex]?.assetUrl" :alt="mediaItems[previewIndex]?.name" />
          <video v-else :src="mediaItems[previewIndex]?.assetUrl" controls autoplay class="image-viewer-video" />
        </div>

        <Transition name="thumb-slide">
          <div v-if="previewThumbsVisible && mediaItems.length > 1" class="image-viewer-thumbs">
            <div
              v-for="(thumb, idx) in mediaItems"
              :key="thumb.id"
              class="image-viewer-thumb-item"
              :class="{ 'is-active': idx === previewIndex }"
              @click.stop="previewIndex = idx"
            >
              <img v-if="isImage(thumb)" :src="thumb.assetUrl" :alt="thumb.name" />
              <video v-else :src="thumb.assetUrl" />
            </div>
          </div>
        </Transition>

        <button class="image-viewer-thumb-toggle" @click.stop="previewThumbsVisible = !previewThumbsVisible">
          <el-icon><PictureFilled /></el-icon>
        </button>
      </div>
    </Transition>

    <AppFooter />
  </div>
</template>

<style scoped>
</style>
