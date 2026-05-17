<script setup>
import { useNoticePoller } from './composables/useNoticePoller'

useNoticePoller()
</script>

<template>
  <router-view />
</template>