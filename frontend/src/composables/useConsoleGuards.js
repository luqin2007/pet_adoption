import { computed } from 'vue'
import { useUserStore } from '../stores/user'
import { ROLE, hasRole } from '../utils/roles'

export function useConsoleGuards() {
  const userStore = useUserStore()
  const loginRole = computed(() => Number(userStore.profile.role || 0))
  const isLoginAdmin = computed(() => (loginRole.value & ROLE.ADMIN) === ROLE.ADMIN)
  const canManageUsers = computed(() => isLoginAdmin.value || hasRole(loginRole.value, ROLE.WORKER))
  const canManageMedical = computed(() => canManageUsers.value || hasRole(loginRole.value, ROLE.DOCTOR))
  const canManageArticles = computed(() => hasRole(loginRole.value, ROLE.WORKER) || hasRole(loginRole.value, ROLE.VOLUNTEER))
  return { loginRole, isLoginAdmin, canManageUsers, canManageMedical, canManageArticles }
}
