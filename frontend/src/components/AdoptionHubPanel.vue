<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { Plus, RefreshRight } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ROLE, hasRole } from '../utils/roles'
import AdoptionTabSection from './adoption-sections/AdoptionTabSection.vue'
import BreadingTabSection from './adoption-sections/BreadingTabSection.vue'
import FollowTabSection from './adoption-sections/FollowTabSection.vue'
import AgreementTabSection from './adoption-sections/AgreementTabSection.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeTabName = ref('adopt')
const loadedTabs = reactive({ adopt: false, breading: false, follow: false, agreement: false })
const tabRefs = {
  adopt: ref(),
  breading: ref(),
  follow: ref(),
  agreement: ref(),
}

const loginRole = computed(() => Number(userStore.profile?.role || 0))
const isAdmin = computed(() => hasRole(loginRole.value, ROLE.ADMIN))
const isVolunteerRole = computed(() => hasRole(loginRole.value, ROLE.VOLUNTEER))
const canManageUsers = computed(() => isAdmin.value || hasRole(loginRole.value, ROLE.WORKER))

const activeTabLoading = computed(() => {
  const ref = tabRefs[activeTabName.value]
  return ref?.value?.loading ?? false
})

function handleActiveTabRefresh() {
  tabRefs[activeTabName.value]?.value?.refresh?.()
}

watch(activeTabName, (tab) => {
  if (!loadedTabs[tab]) {
    loadedTabs[tab] = true
    tabRefs[tab]?.value?.loadData?.()
  }
  const tabQuery = tab === 'adopt' ? undefined : tab
  router.replace({ query: { ...route.query, tab: tabQuery } })
})

onMounted(() => {
  const tab = route.query.tab
  if (tab && ['adopt', 'breading', 'follow', 'agreement'].includes(tab)) {
    activeTabName.value = tab
  }
  loadedTabs[activeTabName.value] = true
  tabRefs[activeTabName.value]?.value?.loadData?.()
})
</script>

<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>领养寄养</strong>
        <div class="profile-actions">
          <el-button-group class="console-btn-group">
            <el-button v-if="activeTabName === 'breading'" class="warm-btn" :icon="Plus" @click="router.push('/breading/new')" />
            <el-button class="warm-btn" :icon="RefreshRight" :loading="activeTabLoading" @click="handleActiveTabRefresh" />
          </el-button-group>
        </div>
      </div>
    </template>

    <el-tabs v-model="activeTabName" class="adoption-hub-tabs">
      <el-tab-pane label="领养" name="adopt">
        <AdoptionTabSection :ref="(el) => { tabRefs.adopt.value = el }" />
      </el-tab-pane>
      <el-tab-pane label="寄养" name="breading">
        <BreadingTabSection :ref="(el) => { tabRefs.breading.value = el }" />
      </el-tab-pane>
      <el-tab-pane label="回访" name="follow">
        <FollowTabSection :ref="(el) => { tabRefs.follow.value = el }" />
      </el-tab-pane>
      <el-tab-pane v-if="canManageUsers" label="协议" name="agreement">
        <AgreementTabSection :ref="(el) => { tabRefs.agreement.value = el }" />
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>
