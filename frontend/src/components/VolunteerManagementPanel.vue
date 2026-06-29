<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { Plus, RefreshRight } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ROLE, hasRole } from '../utils/roles'
import VolunteerRecruitmentsSection from './volunteer-sections/VolunteerRecruitmentsSection.vue'
import VolunteerApplicationsSection from './volunteer-sections/VolunteerApplicationsSection.vue'
import VolunteerProfilesSection from './volunteer-sections/VolunteerProfilesSection.vue'
import VolunteerRewardsSection from './volunteer-sections/VolunteerRewardsSection.vue'
import VolunteerActivitiesSection from './volunteer-sections/VolunteerActivitiesSection.vue'

const props = defineProps({
  section: { type: String, default: 'profiles' },
  hideTabs: { type: Boolean, default: false },
  allowedSections: { type: Array, default: null },
})

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const loginRole = computed(() => Number(userStore.profile.role || 0))
const isAdmin = computed(() => hasRole(loginRole.value, ROLE.ADMIN))
const isWorker = computed(() => isAdmin.value || hasRole(loginRole.value, ROLE.WORKER))
const isVolunteer = computed(() => hasRole(loginRole.value, ROLE.VOLUNTEER))

const activeSection = ref(props.section)

const sections = computed(() => {
  if (!isVolunteer.value) {
    return props.allowedSections
      ? [{ label: '招募申请', value: 'applications' }].filter((item) => props.allowedSections.includes(item.value))
      : [{ label: '招募申请', value: 'applications' }]
  }
  const items = [{ label: '招募申请', value: 'applications' }]
  if (isWorker.value) {
    items.unshift({ label: '招募计划', value: 'recruitments' })
    items.push({ label: '志愿者档案', value: 'profiles' })
  }
  items.push({ label: '志愿者激励', value: 'rewards' })
  items.push({ label: '志愿活动', value: 'activities' })
  if (props.allowedSections) {
    return items.filter((item) => props.allowedSections.includes(item.value))
  }
  return items
})

const sectionRefs = {
  recruitments: ref(),
  applications: ref(),
  profiles: ref(),
  rewards: ref(),
  activities: ref(),
}

function handlePlusClick() {
  sectionRefs[activeSection.value]?.value?.handlePlusClick?.()
}

function searchCurrentSection() {
  sectionRefs[activeSection.value]?.value?.searchCurrentSection?.()
}

watch(sections, (value) => {
  if (!value.some((item) => item.value === activeSection.value)) {
    activeSection.value = value[0]?.value || 'applications'
  }
}, { immediate: true })

watch(() => props.section, (value) => {
  if (value && value !== activeSection.value && sections.value.some((item) => item.value === value)) {
    activeSection.value = value
  }
}, { immediate: true })

watch(activeSection, (value) => {
  const sectionsList = sections.value.map((s) => s.value)
  router.replace({ query: { ...route.query, tab: sectionsList[0] === value ? undefined : value } })
})

watch(() => [route.query.volunteer, route.query.shift], () => {
  if (route.query.volunteer || route.query.shift) {
    activeSection.value = 'activities'
  }
})

onMounted(async () => {
  const tab = route.query.tab
  if (tab && sections.value.some((s) => s.value === tab)) {
    activeSection.value = tab
  } else if (route.query.volunteer || route.query.shift) {
    activeSection.value = 'activities'
  }
})
</script>

<template>
  <el-card class="profile-card volunteer-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>志愿者</strong>
        <div class="profile-actions">
          <el-button-group class="console-btn-group">
            <el-button class="warm-btn" :icon="Plus" @click="handlePlusClick" />
            <el-button class="warm-btn" :icon="RefreshRight" @click="searchCurrentSection" />
          </el-button-group>
        </div>
      </div>
    </template>

    <div class="volunteer-admin-shell">
      <el-tabs v-if="!props.hideTabs" v-model="activeSection" class="volunteer-tabs">
        <el-tab-pane
          v-for="item in sections"
          :key="item.value"
          :label="item.label"
          :name="item.value"
        />
      </el-tabs>

      <VolunteerRecruitmentsSection
        v-if="activeSection === 'recruitments'"
        :ref="(el) => { sectionRefs.recruitments.value = el }"
        v-bind="$attrs"
      />
      <VolunteerApplicationsSection
        v-if="activeSection === 'applications'"
        :ref="(el) => { sectionRefs.applications.value = el }"
        v-bind="$attrs"
      />
      <VolunteerProfilesSection
        v-if="activeSection === 'profiles'"
        :ref="(el) => { sectionRefs.profiles.value = el }"
        v-bind="$attrs"
      />
      <VolunteerRewardsSection
        v-if="activeSection === 'rewards'"
        :ref="(el) => { sectionRefs.rewards.value = el }"
        v-bind="$attrs"
      />
      <VolunteerActivitiesSection
        v-if="activeSection === 'activities'"
        :ref="(el) => { sectionRefs.activities.value = el }"
        v-bind="$attrs"
      />
    </div>
  </el-card>
</template>
