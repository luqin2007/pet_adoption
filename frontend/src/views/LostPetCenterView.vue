<script setup>
import { computed, onMounted, ref } from 'vue'
import { Icon } from '@iconify/vue'
import { RefreshRight } from '@element-plus/icons-vue'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { getLostPets } from '../api/lost'

const navItems = [
  { id: 'home', label: '首页', to: '/' },
  { id: 'lost', label: '走失认领' },
  { id: 'pets', label: '领养大厅', to: '/pets' },
  { id: 'console', label: '个人空间', to: '/console' },
]

const loading = ref(false)
const keyword = ref('')
const lostPets = ref([])

const fallbackRecords = [
  {
    id: 1,
    name: '奶盖',
    age: 36,
    sex: '母',
    type: '猫',
    breed: '英短',
    features: '脖子上有橙色项圈',
    lostTime: '2026-04-10',
    contactPhone: '13900000003',
    description: '于湖滨路附近走失，平时胆子小，听到名字会回头。',
    ownerName: '林女士',
    status: 'CLAIMING',
    location: {
      city: '杭州市',
      detailAddress: '上城区湖滨路',
    },
    petCover:
      'https://images.pexels.com/photos/2558605/pexels-photo-2558605.jpeg?auto=compress&cs=tinysrgb&w=1200',
  },
  {
    id: 2,
    name: '阿布',
    age: 18,
    sex: '公',
    type: '狗',
    breed: '柯基',
    features: '蓝色项圈，右耳有小缺口',
    lostTime: '2026-04-15',
    contactPhone: '13900000005',
    description: '最后一次出现在滨江公园西门。',
    ownerName: '周先生',
    status: 'SEARCHING',
    location: {
      city: '杭州市',
      detailAddress: '滨江公园西门',
    },
    petCover:
      'https://images.pexels.com/photos/58997/pexels-photo-58997.jpeg?auto=compress&cs=tinysrgb&w=1200',
  },
]

const visibleRecords = computed(() =>
  lostPets.value.filter((item) =>
    [item.name, item.type, item.breed, item.ownerName, item.location?.city, item.location?.detailAddress]
      .filter(Boolean)
      .join('')
      .includes(keyword.value),
  ),
)

async function loadLostPets() {
  loading.value = true
  try {
    const result = await getLostPets({
      size: 12,
      name: keyword.value || undefined,
    })
    lostPets.value = result?.records?.length ? result.records : fallbackRecords
  } catch {
    lostPets.value = fallbackRecords
  } finally {
    loading.value = false
  }
}

function formatDate(value) {
  if (!value) {
    return '时间待补充'
  }
  return String(value).slice(0, 10)
}

function statusLabel(status) {
  if (status === 'CLAIMING') {
    return '认领处理中'
  }
  if (status === 'CLAIMED') {
    return '已找回'
  }
  return '寻找中'
}

onMounted(() => {
  loadLostPets()
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main">
      <section class="lost-hero">
        <div class="content-hero-copy">
          <span class="hero-chip">走失认领</span>
          <h1>把线索、位置和相似宠物信息集中到一处</h1>
          <p>
            这一页对接后端 `lost/pets` 查询接口，用于承接首页之外的走失宠物浏览场景，也为后续继续补“相似宠物比对”和“认领申请”页面预留入口。
          </p>
        </div>
        <div class="lost-sidecard">
          <strong>{{ visibleRecords.length }}</strong>
          <span>当前展示的走失档案</span>
          <small>建议与救助中心的流浪宠物记录、地点和时间线联合比对。</small>
        </div>
      </section>

      <section class="filter-panel volunteer-filter-panel">
        <el-input v-model="keyword" placeholder="输入宠物名、品种、地点或联系人关键词" clearable />
        <el-button class="warm-btn" :icon="RefreshRight" @click="loadLostPets">刷新档案</el-button>
      </section>

      <section class="lost-grid" v-loading="loading">
        <article v-for="item in visibleRecords" :key="item.id" class="lost-card">
          <div class="lost-cover">
            <img :src="item.petCover || fallbackRecords[0].petCover" :alt="item.name" loading="lazy" />
            <span class="directory-badge">{{ statusLabel(item.status) }}</span>
          </div>
          <div class="lost-body">
            <div class="directory-head">
              <div>
                <h3>{{ item.name }}</h3>
                <p>{{ item.type }} · {{ item.breed }} · {{ item.sex }}</p>
              </div>
              <span class="directory-type">{{ formatDate(item.lostTime) }}</span>
            </div>

            <p class="directory-desc">{{ item.description }}</p>

            <div class="volunteer-meta">
              <span><Icon icon="mdi:map-marker-radius-outline" />{{ item.location?.city }} {{ item.location?.detailAddress }}</span>
              <span><Icon icon="mdi:star-four-points-outline" />{{ item.features }}</span>
              <span><Icon icon="mdi:phone-outline" />{{ item.ownerName }} · {{ item.contactPhone }}</span>
            </div>

            <div class="lost-actions">
              <el-button class="soft-btn" plain>后续接认领申请页</el-button>
              <el-button text type="warning">查看相似宠物线索</el-button>
            </div>
          </div>
        </article>
      </section>
    </main>

    <AppFooter />
  </div>
</template>
