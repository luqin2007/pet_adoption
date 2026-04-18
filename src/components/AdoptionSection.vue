<script setup>
import { ArrowRight, Calendar, LocationInformation } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  pets: {
    type: Array,
    required: true,
  },
})

const router = useRouter()

function goToPets() {
  router.push('/pets')
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
          <div class="pet-cover" :style="{ background: pet.cover }">
            <el-tag type="warning" effect="dark" size="small">{{ pet.status }}</el-tag>
          </div>
          <div class="pet-body">
            <h3>{{ pet.name }}</h3>
            <p>{{ pet.summary }}</p>
            <div class="pet-meta">
              <span><el-icon><Calendar /></el-icon>{{ pet.age }}</span>
              <span><el-icon><LocationInformation /></el-icon>{{ pet.city }}</span>
            </div>
            <el-button text type="warning" class="card-link" @click="goToPets">
              查看详情
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </el-card>
      </article>
    </div>
    <div class="section-action">
      <el-button class="soft-btn" size="large" @click="goToPets">查看全部待领养宠物</el-button>
    </div>
  </section>
</template>
