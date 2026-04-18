import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import ArticleHubView from '../views/ArticleHubView.vue'
import AuthView from '../views/AuthView.vue'
import HomeView from '../views/HomeView.vue'
import PetDirectoryView from '../views/PetDirectoryView.vue'
import ProfileCenterView from '../views/ProfileCenterView.vue'
import VolunteerCenterView from '../views/VolunteerCenterView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/login',
      name: 'login',
      component: AuthView,
    },
    {
      path: '/pets',
      name: 'pets',
      component: PetDirectoryView,
    },
    {
      path: '/articles',
      name: 'articles',
      component: ArticleHubView,
    },
    {
      path: '/volunteers',
      name: 'volunteers',
      component: VolunteerCenterView,
    },
    {
      path: '/console',
      name: 'profile-center',
      component: ProfileCenterView,
      meta: {
        requiresAuth: true,
      },
    },
  ],
})

router.beforeEach((to) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath || '/console',
      },
    }
  }

  return true
})

export default router
