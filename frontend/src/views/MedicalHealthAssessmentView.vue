<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />
    <main class="subpage-main action-form-page">
      <MedicalHealthAssessmentDetailPanel back-path="/pets" />
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import MedicalHealthAssessmentDetailPanel from '../components/MedicalHealthAssessmentDetailPanel.vue'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'
</script>
