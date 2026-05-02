import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import ArticleHubView from '../views/ArticleHubView.vue'
import ArticleEditorView from '../views/ArticleEditorView.vue'
import AuthView from '../views/AuthView.vue'
import ForgotPasswordView from '../views/ForgotPasswordView.vue'
import ResetPasswordView from '../views/ResetPasswordView.vue'
import ApiCoverageView from '../views/ApiCoverageView.vue'
import BreadingCreateView from '../views/BreadingCreateView.vue'
import DonationCreateView from '../views/DonationCreateView.vue'
import HomeView from '../views/HomeView.vue'
import LostPetCenterView from '../views/LostPetCenterView.vue'
import LostPetCreateView from '../views/LostPetCreateView.vue'
import PetCreateView from '../views/PetCreateView.vue'
import PetDirectoryView from '../views/PetDirectoryView.vue'
import PetEditView from '../views/PetEditView.vue'
import PetMediaUploadView from '../views/PetMediaUploadView.vue'
import PetAdoptCreateView from '../views/PetAdoptCreateView.vue'
import PetClaimCreateView from '../views/PetClaimCreateView.vue'
import PetProfileView from '../views/PetProfileView.vue'
import ProfileCenterView from '../views/ProfileCenterView.vue'
import RescueTaskCreateView from '../views/RescueTaskCreateView.vue'
import RescueTaskDetailView from '../views/RescueTaskDetailView.vue'
import ServiceWorkbenchView from '../views/ServiceWorkbenchView.vue'
import VolunteerCenterView from '../views/VolunteerCenterView.vue'
import VolunteerRecruitmentDetailView from '../views/VolunteerRecruitmentDetailView.vue'
import FirstRegistrationCreateView from '../views/FirstRegistrationCreateView.vue'

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
      path: '/forgot-password',
      alias: '/user/forgot-password',
      name: 'forgot-password',
      component: ForgotPasswordView,
    },
    {
      path: '/user/reset',
      name: 'reset-password',
      component: ResetPasswordView,
    },
    {
      path: '/pets',
      name: 'pets',
      component: PetDirectoryView,
    },
    {
      path: '/pets/new',
      name: 'pet-create',
      component: PetCreateView,
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/pets/:id',
      name: 'pet-profile',
      component: PetProfileView,
    },
    {
      path: '/pets/:id/edit',
      name: 'pet-edit',
      component: PetEditView,
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/pets/:id/media',
      name: 'pet-media-upload',
      component: PetMediaUploadView,
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/pets/:id/adopt',
      name: 'pet-adopt-create',
      component: PetAdoptCreateView,
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/pets/:id/claim',
      name: 'pet-claim-create',
      component: PetClaimCreateView,
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/articles',
      name: 'articles',
      component: ArticleHubView,
    },
    {
      path: '/console/articles/new',
      name: 'article-create',
      component: ArticleEditorView,
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/console/articles/:id/edit',
      name: 'article-edit',
      component: ArticleEditorView,
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/volunteers',
      name: 'volunteers',
      component: VolunteerCenterView,
    },
    {
      path: '/volunteers/recruitments/:id',
      name: 'volunteer-recruitment-detail',
      component: VolunteerRecruitmentDetailView,
    },
    {
      path: '/lost',
      name: 'lost',
      component: LostPetCenterView,
    },
    {
      path: '/lost/new',
      name: 'lost-create',
      component: LostPetCreateView,
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/services',
      name: 'services',
      component: ServiceWorkbenchView,
    },
    {
      path: '/tasks/new',
      name: 'rescue-task-create',
      component: RescueTaskCreateView,
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/tasks/:id',
      name: 'rescue-task-detail',
      component: RescueTaskDetailView,
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/donations/new',
      name: 'donation-create',
      component: DonationCreateView,
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/breading/new',
      alias: '/foster/new',
      name: 'breading-create',
      component: BreadingCreateView,
      meta: { requiresAuth: true },
    },
    {
      path: '/medical/first-registration/new',
      name: 'first-registration-create',
      component: FirstRegistrationCreateView,
      meta: { requiresAuth: true },
    },
    {
      path: '/console',
      name: 'profile-center',
      component: ProfileCenterView,
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/api-coverage',
      name: 'api-coverage',
      component: ApiCoverageView,
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
