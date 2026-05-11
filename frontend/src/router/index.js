import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ROLE, hasRole } from '../utils/roles'
import ArticleDetailView from '../views/ArticleDetailView.vue'
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
import LostPetDetailView from '../views/LostPetDetailView.vue'
import PetCreateView from '../views/PetCreateView.vue'
import PetDirectoryView from '../views/PetDirectoryView.vue'
import PetEditView from '../views/PetEditView.vue'
import PetMediaUploadView from '../views/PetMediaUploadView.vue'
import PetAdoptCreateView from '../views/PetAdoptCreateView.vue'
import PetClaimCreateView from '../views/PetClaimCreateView.vue'
import PetProfileView from '../views/PetProfileView.vue'
import RescueTaskCreateView from '../views/RescueTaskCreateView.vue'
import RescueTaskDetailView from '../views/RescueTaskDetailView.vue'
import VolunteerCenterView from '../views/VolunteerCenterView.vue'
import VolunteerRecruitmentDetailView from '../views/VolunteerRecruitmentDetailView.vue'
import FirstRegistrationCreateView from '../views/FirstRegistrationCreateView.vue'
import MedicalDetailView from '../views/MedicalDetailView.vue'
import MedicalDetailCreateView from '../views/MedicalDetailCreateView.vue'
import MedicalExaminationsView from '../views/MedicalExaminationsView.vue'
import MedicalHealthAssessmentView from '../views/MedicalHealthAssessmentView.vue'
import AuditHistoryView from '../views/AuditHistoryView.vue'
import ConsoleLayout from '../components/ConsoleLayout.vue'
import ProfilePanel from '../components/ProfilePanel.vue'
import UsersPanel from '../components/UsersPanel.vue'
import PetsPanel from '../components/PetsPanel.vue'
import LostPetsPanel from '../components/LostPetsPanel.vue'
import TasksPanel from '../components/TasksPanel.vue'
import MedicalFirstPanel from '../components/MedicalFirstPanel.vue'
import MedicalRecordPanel from '../components/MedicalRecordPanel.vue'
import MedicalPreventivePanel from '../components/MedicalPreventivePanel.vue'
import MedicalRehabPlanPanel from '../components/MedicalRehabPlanPanel.vue'
import MedicalRehabPlanDetailPanel from '../components/MedicalRehabPlanDetailPanel.vue'
import MedicalRehabPlanCreatePanel from '../components/MedicalRehabPlanCreatePanel.vue'
import MedicalHealthAssessmentPanel from '../components/MedicalHealthAssessmentPanel.vue'
import MedicalHealthAssessmentDetailPanel from '../components/MedicalHealthAssessmentDetailPanel.vue'
import FirstRegistrationDetailPanel from '../components/FirstRegistrationDetailPanel.vue'
import MedicalRecordDetailPanel from '../components/MedicalRecordDetailPanel.vue'
import FirstRegistrationDetailView from '../views/FirstRegistrationDetailView.vue'
import MedicalDetailListPanel from '../components/MedicalDetailListPanel.vue'
import MyArticleManagementPanel from '../components/MyArticleManagementPanel.vue'
import AdoptionManagementPanel from '../components/AdoptionManagementPanel.vue'
import AdoptionApplicationDetailPanel from '../components/AdoptionApplicationDetailPanel.vue'
import AdoptionFollowTaskCreatePanel from '../components/AdoptionFollowTaskCreatePanel.vue'
import BreadingManagementPanel from '../components/BreadingManagementPanel.vue'
import AgreementDraftPanel from '../components/AgreementDraftPanel.vue'
import AgreementManagementPanel from '../components/AgreementManagementPanel.vue'
import VolunteerManagementPanel from '../components/VolunteerManagementPanel.vue'
import VolunteerApplicationDetailPanel from '../components/VolunteerApplicationDetailPanel.vue'
import { medicalRecordOwnerExists } from '../api/services'

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
      meta: { requiresAuth: true },
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
      meta: { requiresAuth: true },
    },
    {
      path: '/pets/:id/media',
      name: 'pet-media-upload',
      component: PetMediaUploadView,
      meta: { requiresAuth: true },
    },
    {
      path: '/pets/:id/adopt',
      name: 'pet-adopt-create',
      component: PetAdoptCreateView,
      meta: { requiresAuth: true },
    },
    {
      path: '/pets/:id/claim',
      name: 'pet-claim-create',
      component: PetClaimCreateView,
      meta: { requiresAuth: true },
    },
    {
      path: '/articles',
      name: 'articles',
      component: ArticleHubView,
    },
    {
      path: '/articles/:id',
      name: 'article-detail',
      component: ArticleDetailView,
    },
    {
      path: '/console/articles/new',
      name: 'article-create',
      component: ArticleEditorView,
      meta: { requiresAuth: true },
    },
    {
      path: '/console/articles/:id/edit',
      name: 'article-edit',
      component: ArticleEditorView,
      meta: { requiresAuth: true },
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
      meta: { requiresAuth: true },
    },
    {
      path: '/lost/:id',
      name: 'lost-detail',
      component: LostPetDetailView,
    },
    {
      path: '/tasks/new',
      name: 'rescue-task-create',
      component: RescueTaskCreateView,
      meta: { requiresAuth: true },
    },
    {
      path: '/tasks/:id',
      name: 'rescue-task-detail',
      component: RescueTaskDetailView,
      meta: { requiresAuth: true },
    },
    {
      path: '/donations/new',
      name: 'donation-create',
      component: DonationCreateView,
      meta: { requiresAuth: true },
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
      path: '/medical/detail/new',
      name: 'medical-detail-create',
      component: MedicalDetailCreateView,
      meta: { requiresAuth: true },
    },
    {
      path: '/medical/detail/:id',
      name: 'medical-detail',
      component: MedicalDetailView,
      meta: { requiresAuth: true },
    },
    {
      path: '/medical/detail/:id/exams',
      name: 'medical-examinations',
      component: MedicalExaminationsView,
      meta: { requiresAuth: true },
    },
    {
      path: '/medical/first/:id',
      name: 'first-registration-detail',
      component: FirstRegistrationDetailView,
      meta: { requiresAuth: true },
    },
    {
      path: '/medical/health/:id',
      name: 'medical-health-detail',
      component: MedicalHealthAssessmentView,
    },
    // --- Console routes (nested with shared layout) ---
    {
      path: '/console',
      component: ConsoleLayout,
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: '/console/profile' },
        { path: 'profile', name: 'console-profile', component: ProfilePanel },
        { path: 'users', name: 'console-users', component: UsersPanel, meta: { guard: 'canManageUsers' } },
        { path: 'pets', name: 'console-pets', component: PetsPanel },
        { path: 'lost-pets', name: 'console-lost-pets', component: LostPetsPanel },
        { path: 'tasks', name: 'console-tasks', component: TasksPanel },
        { path: 'articles/mine', name: 'console-article-mine', component: MyArticleManagementPanel, props: { mode: 'mine' } },
        { path: 'articles/manage', name: 'console-article-manage', component: MyArticleManagementPanel, props: { mode: 'manage' }, meta: { guard: 'canManageUsers' } },
        { path: 'adoption/adopts', name: 'console-adoption-adopts', component: AdoptionManagementPanel },
        { path: 'adoption/breading', name: 'console-adoption-breading', component: BreadingManagementPanel },
        { path: 'adoption/adopts/:id', name: 'console-adoption-adopt-detail', component: AdoptionApplicationDetailPanel },
        { path: 'adoption/adopts/:id/follow', name: 'console-adoption-follow-create', component: AdoptionFollowTaskCreatePanel, meta: { guard: 'canManageAdoptFollow' } },
        { path: 'adoption/agreements', name: 'console-adoption-agreements', component: AgreementManagementPanel, meta: { guard: 'canManageUsers' } },
        { path: 'adoption/agreements/new-paper', name: 'console-adoption-agreement-paper-create', component: AgreementDraftPanel, props: { type: 'PAPER' }, meta: { guard: 'canManageUsers' } },
        { path: 'adoption/agreements/new-electronic', name: 'console-adoption-agreement-electronic-create', component: AgreementDraftPanel, props: { type: 'ELECTRONIC' }, meta: { guard: 'canManageUsers' } },
        { path: 'volunteer/recruitments', name: 'console-volunteer-recruitments', component: VolunteerManagementPanel, props: { section: 'recruitments', hideTabs: true }, meta: { guard: 'canManageUsers' } },
        { path: 'volunteer/applications', name: 'console-volunteer-applications', component: VolunteerManagementPanel, props: { section: 'applications', hideTabs: true } },
        { path: 'volunteer/applications/:id', name: 'console-volunteer-application-detail', component: VolunteerApplicationDetailPanel },
        { path: 'volunteer/rewards', name: 'console-volunteer-rewards', component: VolunteerManagementPanel, props: { section: 'rewards', hideTabs: true }, meta: { guard: 'canManageUsersOrVolunteer' } },
        { path: 'volunteer/activities', name: 'console-volunteer-activities', component: VolunteerManagementPanel, props: { section: 'activities', hideTabs: true }, meta: { guard: 'canManageUsersOrVolunteer' } },
        { path: 'medical/first', name: 'console-medical-first', component: MedicalFirstPanel, meta: { guard: 'canViewMedical' } },
        { path: 'medical/first/:id', name: 'console-medical-first-detail', component: FirstRegistrationDetailPanel, meta: { guard: 'canViewMedical' } },
        { path: 'medical/records', name: 'console-medical-records', component: MedicalRecordPanel, meta: { guard: 'canViewMedical' } },
        { path: 'medical/records/:id', name: 'console-medical-record-detail', component: MedicalRecordDetailPanel, meta: { guard: 'canViewMedical' } },
        { path: 'medical/detail-list', name: 'console-medical-detail-list', component: MedicalDetailListPanel, meta: { guard: 'canManageMedical' } },
        { path: 'medical/vaccines', name: 'console-medical-vaccines', component: MedicalPreventivePanel, props: { type: 'vaccine' }, meta: { guard: 'canManageMedical' } },
        { path: 'medical/deworms', name: 'console-medical-deworms', component: MedicalPreventivePanel, props: { type: 'deworm' }, meta: { guard: 'canManageMedical' } },
        { path: 'medical/rehab', name: 'console-medical-rehab', component: MedicalRehabPlanPanel, meta: { guard: 'canManageRehab' } },
        { path: 'medical/rehab/new', name: 'console-medical-rehab-create', component: MedicalRehabPlanCreatePanel, meta: { guard: 'canManageRehabDoctor' } },
        { path: 'medical/rehab/:id', name: 'console-medical-rehab-detail', component: MedicalRehabPlanDetailPanel, meta: { guard: 'canManageRehab' } },
        { path: 'medical/health', name: 'console-medical-health', component: MedicalHealthAssessmentPanel, meta: { guard: 'canManageMedical' } },
        { path: 'medical/health/:id', name: 'console-medical-health-detail', component: MedicalHealthAssessmentDetailPanel, meta: { guard: 'canManageMedical' } },
      ],
    },
    {
      path: '/console/audit-history',
      name: 'audit-history',
      component: AuditHistoryView,
      meta: { requiresAuth: true },
    },
    {
      path: '/api-coverage',
      name: 'api-coverage',
      component: ApiCoverageView,
      meta: { requiresAuth: true },
    },
  ],
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath || '/console',
      },
    }
  }

  // Role-based guard for console routes
  if (to.meta.guard) {
    const role = Number(userStore.profile?.role || 0)
    const isAdmin = hasRole(role, ROLE.ADMIN)
    const isWorker = hasRole(role, ROLE.WORKER)
    const isDoctor = hasRole(role, ROLE.DOCTOR)
    const isVolunteer = hasRole(role, ROLE.VOLUNTEER)

    const guard = to.meta.guard
    let allowed = false
    if (guard === 'canManageUsers') allowed = isAdmin || isWorker
    else if (guard === 'canManageMedical') allowed = isAdmin || isWorker || isDoctor
    else if (guard === 'canManageAdoptFollow') allowed = isWorker
    else if (guard === 'canManageRehab') allowed = isDoctor || isVolunteer
    else if (guard === 'canManageRehabDoctor') allowed = isDoctor
    else if (guard === 'canViewMedical') {
      allowed = isAdmin || isWorker || isDoctor
      if (!allowed && userStore.profile?.id) {
        try {
          allowed = Boolean(await medicalRecordOwnerExists(userStore.profile.id))
        } catch {
          allowed = false
        }
      }
    }
    else if (guard === 'canManageArticles') allowed = isWorker || isVolunteer
    else if (guard === 'canManageUsersOrVolunteer') allowed = isAdmin || isWorker || isVolunteer

    if (!allowed) {
      return { path: '/console/profile' }
    }
  }

  return true
})

export default router
