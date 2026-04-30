<script setup>
import { computed, onMounted, ref } from 'vue'
import { Icon } from '@iconify/vue'
import { ArrowRight, RefreshRight } from '@element-plus/icons-vue'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { getPets } from '../api/pets'

const navItems = [
  { id: 'home', label: '首页', to: '/' },
  { id: 'pets', label: '领养大厅' },
  { id: 'console', label: '个人空间', to: '/console' },
]

const loading = ref(false)
const pets = ref([])
const filters = ref({
  city: '',
  type: '',
  status: '',
})

const fallbackPets = [
  {
    id: 1,
    name: '姜糖',
    age: 8,
    sex: '母',
    type: '猫',
    breed: '中华田园猫',
    health: '已体检',
    description: '亲人、爱撒娇，已完成体检与首针疫苗。',
    username: 'Anna',
    locations: [{ city: '杭州市', district: '西湖区' }],
    cover:
      'https://images.pexels.com/photos/1170986/pexels-photo-1170986.jpeg?auto=compress&cs=tinysrgb&w=1200',
  },
  {
    id: 2,
    name: '豆包',
    age: 24,
    sex: '公',
    type: '狗',
    breed: '中华田园犬',
    health: '恢复中',
    description: '稳定安静，适合有陪伴时间的家庭。',
    username: 'Ming',
    locations: [{ city: '杭州市', district: '拱墅区' }],
    cover:
      'https://images.pexels.com/photos/1904105/pexels-photo-1904105.jpeg?auto=compress&cs=tinysrgb&w=1200',
  },
  {
    id: 3,
    name: '奶盖',
    age: 36,
    sex: '母',
    type: '猫',
    breed: '英短',
    health: '适合家庭',
    description: '温柔黏人，适合新手领养人。',
    username: 'Lin',
    locations: [{ city: '杭州市', district: '上城区' }],
    cover:
      'https://images.pexels.com/photos/2558605/pexels-photo-2558605.jpeg?auto=compress&cs=tinysrgb&w=1200',
  },
]

const filteredPets = computed(() =>
  pets.value.filter((pet) => {
    const city = pet.locations?.[0]?.city || ''
    const status = pet.status || ''
    return (
      (!filters.value.city || city.includes(filters.value.city)) &&
      (!filters.value.type || pet.type === filters.value.type) &&
      (!filters.value.status || status === filters.value.status)
    )
  }),
)

async function loadPets() {
  loading.value = true
  try {
    const result = await getPets({
      size: 12,
      city: filters.value.city || undefined,
      type: filters.value.type ? [filters.value.type] : undefined,
      status: filters.value.status ? [filters.value.status] : undefined,
    })
    pets.value = result?.records?.length ? result.records : fallbackPets
  } catch {
    pets.value = fallbackPets
  } finally {
    loading.value = false
  }
}

function formatAge(age) {
  if (!age && age !== 0) {
    return '年龄待补充'
  }
  return `约 ${age} 个月`
}

function formatLocation(pet) {
  const location = pet.locations?.[0]
  if (!location) {
    return '待补充位置'
  }
  return [location.city, location.district].filter(Boolean).join(' · ')
}

function getStatusText(pet) {
  if (pet.status === 'HEALTH') {
    return '可预约见面'
  }
  if (pet.status === 'SHELTERED') {
    return '等待领养'
  }
  return pet.health || '资料完善中'
}

onMounted(() => {
  loadPets()
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main">
      <section class="directory-hero">
        <div>
          <span class="hero-chip">领养大厅</span>
          <h1>把一只流浪生命，接回一个真正的家</h1>
          <p>
            根据后端宠物档案、健康与位置数据展示待领养毛孩子。你可以先按城市、宠物类型和状态快速筛选，再查看合适的见面对象。
          </p>
        </div>
        <div class="directory-hero-card">
          <strong>{{ filteredPets.length }}</strong>
          <span>当前可浏览的领养档案</span>
          <small>若接口暂时不可用，页面会自动回退为演示数据，方便继续联调前端。</small>
        </div>
      </section>

      <section class="filter-panel">
        <el-input v-model="filters.city" placeholder="按城市筛选，例如：杭州" clearable />
        <el-select v-model="filters.type" placeholder="宠物类型" clearable>
          <el-option label="猫咪" value="猫" />
          <el-option label="狗狗" value="狗" />
        </el-select>
        <el-select v-model="filters.status" placeholder="领养状态" clearable>
          <el-option label="可预约见面" value="HEALTH" />
          <el-option label="等待领养" value="SHELTERED" />
        </el-select>
        <el-button class="warm-btn" :icon="RefreshRight" @click="loadPets">刷新列表</el-button>
      </section>

      <section class="directory-grid" v-loading="loading">
        <article v-for="pet in filteredPets" :key="pet.id" class="directory-card">
          <div class="directory-cover">
            <img :src="pet.cover || fallbackPets[0].cover" :alt="pet.name" loading="lazy" />
            <span class="directory-badge">{{ getStatusText(pet) }}</span>
          </div>
          <div class="directory-body">
            <div class="directory-head">
              <div>
                <h3>{{ pet.name }}</h3>
                <p>{{ formatAge(pet.age) }} · {{ pet.sex || '性别待补充' }}</p>
              </div>
              <span class="directory-type">{{ pet.type || '宠物' }}</span>
            </div>

            <p class="directory-desc">{{ pet.description || '救助站正在完善它的故事与性格描述。' }}</p>

            <div class="directory-meta">
              <span><Icon icon="mdi:map-marker-radius-outline" />{{ formatLocation(pet) }}</span>
              <span><Icon icon="mdi:shield-heart-outline" />{{ pet.health || '健康档案整理中' }}</span>
              <span><Icon icon="mdi:account-heart-outline" />{{ pet.username || '暖窝救助站' }}</span>
            </div>

            <el-button text type="warning" class="card-link">
              查看领养档案
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </article>
      </section>
    </main>

    <AppFooter />
  </div>
</template>
