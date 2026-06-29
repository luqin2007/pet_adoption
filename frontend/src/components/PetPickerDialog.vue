<template>
  <el-dialog v-model="dialogVisible" title="选择流浪宠物" width="640px" :close-on-click-modal="false" destroy-on-close @closed="handleClosed">
    <div class="rehab-pet-picker-search">
      <el-input v-model="petSearchName" placeholder="搜索宠物名称..." clearable @clear="searchPets" @input="debounceSearch" />
    </div>
    <div class="rehab-pet-picker-grid">
      <div
        v-for="pet in pickerPets"
        :key="pet.id"
        class="rehab-pet-picker-card"
        :class="{ 'is-selected': selectedPet?.id === pet.id }"
        @click="selectedPet = pet"
      >
        <img :src="pet.cover || 'https://images.pexels.com/photos/1170986/pexels-photo-1170986.jpeg?auto=compress&cs=tinysrgb&w=320'" :alt="pet.name" class="rehab-pet-picker-cover" />
        <div class="rehab-pet-picker-card-body">
          <strong>{{ pet.name || '未命名' }}</strong>
          <span>{{ pet.type || '宠物' }} · {{ pet.sex || '未知' }} · {{ pet.age ?? 0 }} 月</span>
        </div>
      </div>
      <el-empty v-if="!pickerPets.length && !pickerLoading" description="暂无符合条件的流浪宠物" :image-size="60" />
    </div>
    <div class="user-admin-pagination" style="margin-top:12px">
      <el-pagination
        layout="prev, pager, next, total"
        :current-page="pickerPage.page"
        :page-size="pickerPage.size"
        :total="pickerTotal"
        :disabled="pickerLoading"
        @current-change="changePickerPage"
      />
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button class="warm-btn" :disabled="!selectedPet" @click="confirmPick">选择</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { getPets } from '../api/pets'

const props = defineProps({ visible: Boolean })
const emit = defineEmits(['update:visible', 'select'])

const dialogVisible = ref(false)
const petSearchName = ref('')
const selectedPet = ref(null)
const pickerPets = ref([])
const pickerTotal = ref(0)
const pickerLoading = ref(false)
const pickerPage = reactive({ page: 1, size: 12 })
let searchTimer = null

watch(() => props.visible, (val) => {
  if (val) {
    selectedPet.value = null
    petSearchName.value = ''
    pickerPage.page = 1
    searchPets()
  }
  dialogVisible.value = val
})

watch(dialogVisible, (val) => {
  emit('update:visible', val)
})

function debounceSearch() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    pickerPage.page = 1
    searchPets()
  }, 300)
}

async function searchPets() {
  pickerLoading.value = true
  try {
    const query = { page: pickerPage.page, size: pickerPage.size, status: 'SHELTERED' }
    const keyword = petSearchName.value.trim()
    if (keyword) query.name = keyword
    const result = await getPets(query)
    pickerPets.value = Array.isArray(result?.records) ? result.records : []
    pickerTotal.value = Number(result?.total || 0)
  } catch {
    pickerPets.value = []
    pickerTotal.value = 0
  } finally {
    pickerLoading.value = false
  }
}

function changePickerPage(value) {
  pickerPage.page = value
  searchPets()
}

function confirmPick() {
  if (!selectedPet.value) return
  emit('select', selectedPet.value)
  dialogVisible.value = false
}

function handleClosed() {
  selectedPet.value = null
  pickerPets.value = []
  pickerTotal.value = 0
}
</script>

<style scoped>
.rehab-pet-picker-search {
  margin-bottom: 12px;
}

.rehab-pet-picker-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  max-height: 440px;
  overflow-y: auto;
}

.rehab-pet-picker-card {
  border: 2px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.15s;
  background: #fffaf3;
}

.rehab-pet-picker-card:hover {
  border-color: rgba(231, 122, 59, 0.4);
}

.rehab-pet-picker-card.is-selected {
  border-color: #e77a3b;
  box-shadow: 0 0 0 1px #e77a3b;
}

.rehab-pet-picker-cover {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  display: block;
}

.rehab-pet-picker-card-body {
  padding: 8px 10px;
  display: grid;
  gap: 2px;
}

.rehab-pet-picker-card-body strong {
  color: #5d3927;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rehab-pet-picker-card-body span {
  color: var(--muted);
  font-size: 11px;
}

@media (max-width: 560px) {
  .rehab-pet-picker-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
