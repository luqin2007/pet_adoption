<script setup>
import { useNoticeSse } from './composables/useNoticeSse'

useNoticeSse()
</script>

<template>
  <router-view />
</template>