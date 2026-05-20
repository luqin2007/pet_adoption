<script setup>
import { Icon } from '@iconify/vue'

const props = defineProps({
  pet: { type: Object, required: true },
  badgeText: { type: String, default: '' },
})

const emit = defineEmits(['click'])

function formatAge(age) {
  if (!age && age !== 0) return '年龄待补充'
  const months = Number(age)
  if (Number.isNaN(months)) return '年龄待补充'
  if (months > 12) {
    const years = Math.floor(months / 12)
    const remainMonths = months % 12
    return remainMonths > 0 ? `约 ${years} 岁 ${remainMonths} 个月` : `约 ${years} 岁`
  }
  return `约 ${months} 个月`
}

function formatLocation(pet) {
  const location = pet.locations?.[0]
  if (!location) return '待补充位置'
  return [location.city, location.district].filter(Boolean).join(' · ')
}

function formatTags(pet) {
  const tags = Array.isArray(pet.tags) ? pet.tags.map((item) => item?.tag).filter(Boolean) : []
  return tags.join(' · ')
}

function getStatusText(pet) {
  if (props.badgeText) return props.badgeText
  if (pet.status) return pet.status
  return '资料完善中'
}
</script>

<template>
  <article class="directory-card" style="cursor:pointer" @click="$emit('click')">
    <div class="directory-cover">
      <img v-if="pet.cover" :src="pet.cover" :alt="pet.name" loading="lazy" />
      <div v-else class="directory-cover-placeholder">暂无封面</div>
      <span class="directory-badge">{{ getStatusText(pet) }}</span>
    </div>
    <div class="directory-body">
      <div class="directory-head">
        <div>
          <h3>{{ pet.name }}</h3>
          <p>{{ formatAge(pet.age) }} · {{ pet.sex || '性别待补充' }}</p>
          <span v-if="formatTags(pet)" class="directory-tag-line">
            <Icon icon="mdi:tag-heart-outline" />{{ formatTags(pet) }}
          </span>
        </div>
        <span class="directory-type">{{ pet.type || '宠物' }}</span>
      </div>

      <p class="directory-desc">{{ pet.description || '救助站正在完善它的故事与性格描述。' }}</p>

      <div class="directory-meta">
        <span><Icon icon="mdi:map-marker-radius-outline" />{{ formatLocation(pet) }}</span>
        <span><Icon icon="mdi:account-heart-outline" />{{ pet.username || '暖窝救助站' }}</span>
      </div>
    </div>
    <div v-if="$slots.footer" class="directory-footer">
      <slot name="footer" />
    </div>
  </article>
</template>

<style scoped>
.directory-footer {
  border-top: 1px solid var(--el-border-color-lighter);
  padding: 8px 16px;
}
</style>
