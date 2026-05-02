<script setup>
import { ArrowRight, Calendar, LocationInformation } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  pets: {
    type: Array,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
})

const router = useRouter()

function goToPets() {
  router.push('/pets')
}

function goToPetProfile(pet) {
  if (pet?.id) {
    router.push(`/pets/${pet.id}`)
    return
  }
  goToPets()
}

function isImageCover(cover) {
  return typeof cover === 'string' && cover && !cover.startsWith('linear-gradient')
}

function coverStyle(cover) {
  if (isImageCover(cover)) {
    return {}
  }
  return {
    background: cover || 'linear-gradient(135deg, #ffd6ad 0%, #ffe8cf 100%)',
  }
}
</script>

<template>
  <section id="adoption" class="content-section">
    <div class="section-head">
      <h2>领养推荐</h2>
    </div>
    <div class="pet-grid">
      <article v-for="pet in props.pets" :key="pet.name">
        <el-card class="pet-card" shadow="hover">
          <div class="pet-cover" :style="coverStyle(pet.cover)">
            <img v-if="isImageCover(pet.cover)" :src="pet.cover" :alt="pet.name" />
            <el-tag type="warning" effect="dark" size="small">{{ pet.status }}</el-tag>
          </div>
          <div class="pet-body">
            <h3>{{ pet.name }}</h3>
            <p>{{ pet.summary }}</p>
            <div class="pet-meta">
              <span><el-icon><Calendar /></el-icon>{{ pet.age }}</span>
              <span><el-icon><LocationInformation /></el-icon>{{ pet.city }}</span>
            </div>
            <el-button text type="warning" class="card-link" @click="goToPetProfile(pet)">
              查看宠物档案
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </el-card>
      </article>
      <el-empty
        v-if="!props.loading && props.pets.length === 0"
        class="section-empty"
        description="当前还没有可展示的领养推荐"
      />
    </div>
    <div class="section-action">
      <el-button class="soft-btn" size="large" @click="goToPets">查看全部待领养宠物</el-button>
    </div>
  </section>
</template>
