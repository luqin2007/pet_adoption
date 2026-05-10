import { request } from './http'

export function getRescueTasks(query = {}) {
  return request('/tasks', {
    method: 'GET',
    query,
  })
}

export function getRescueTaskCount() {
  return request('/tasks/count', {
    method: 'GET',
  })
}

export function getRescueTask(id) {
  return request(`/tasks/${id}`, {
    method: 'GET',
  })
}

export function getRescueTaskMedia(id) {
  return request(`/tasks/${id}/media`, {
    method: 'GET',
  })
}

export function beginRescueTask() {
  return request('/tasks', {
    method: 'PUT',
  })
}

export function createRescueTask(payload) {
  return request('/tasks', {
    method: 'POST',
    body: payload,
  })
}

export function updateRescueTask(id, payload) {
  return request(`/tasks/${id}`, {
    method: 'PUT',
    body: payload,
  })
}

export function updateRescueTaskStatus(id, payload) {
  return request(`/tasks/${id}/status`, {
    method: 'PATCH',
    body: payload,
  })
}

export function getRescueTaskRecords(id) {
  return request(`/tasks/${id}/status`, {
    method: 'GET',
  })
}

export function uploadRescueTaskMedia(id, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request(`/tasks/${id}/uploads`, {
    method: 'POST',
    body: formData,
  })
}

export function deleteRescueTaskUpload(id, filename) {
  return request(`/tasks/${id}/uploads/${encodeURIComponent(filename)}`, {
    method: 'DELETE',
  })
}

export function deleteRescueTask(id) {
  return request(`/tasks/${id}`, {
    method: 'DELETE',
  })
}

export function getAdoptApplications(query = {}) {
  return request('/adopt/adopt', {
    method: 'GET',
    query,
  })
}

export function createAdoptApplication(payload) {
  return request('/adopt/adopt', {
    method: 'POST',
    body: payload,
  })
}

export function getBreadingApplications(query = {}) {
  return request('/adopt/breading', {
    method: 'GET',
    query,
  })
}

export function getMedicalRecords(query = {}) {
  return request('/medical/record', {
    method: 'GET',
    query,
  })
}

export function getMedicalRecord(id) {
  return request(`/medical/record/${id}`, {
    method: 'GET',
  })
}

export function medicalRecordOwnerExists(ownerId) {
  return request('/medical/record/owner-exists', {
    method: 'GET',
    query: { ownerId },
  })
}

export function addMedicalRecord(petId, payload) {
  return request(`/medical/record/pet/${petId}`, {
    method: 'POST',
    body: payload,
  })
}

export function updateMedicalRecord(id, payload) {
  return request(`/medical/record/${id}`, {
    method: 'PUT',
    body: payload,
  })
}

export function getMedicalDetails(query = {}) {
  return request('/medical/detail', {
    method: 'GET',
    query,
  })
}

export function getMedicalDetail(id) {
  return request(`/medical/detail/${id}`, {
    method: 'GET',
  })
}

export function addMedicalDetail(payload) {
  return request('/medical/detail', {
    method: 'POST',
    body: payload,
  })
}

export function updateMedicalDetail(id, payload) {
  return request(`/medical/detail/${id}`, {
    method: 'PUT',
    body: payload,
  })
}

export function completeMedicalDetail(id) {
  return request(`/medical/detail/${id}`, {
    method: 'PATCH',
  })
}

export function discardMedicalDetail(id) {
  return request(`/medical/detail/${id}`, {
    method: 'DELETE',
  })
}

export function addDiagnosis(detailId, payload) {
  return request(`/medical/detail/${detailId}/diagnosis`, {
    method: 'POST',
    body: payload,
  })
}

export function addTreatmentPlan(detailId, payload) {
  return request(`/medical/detail/${detailId}/plan`, {
    method: 'POST',
    body: payload,
  })
}

export function discardTreatmentPlan(ids) {
  return request('/medical/plan', {
    method: 'DELETE',
    body: { ids },
  })
}

export function getMedicalExaminations(detailId) {
  return request(`/medical/details/${detailId}/exam`, {
    method: 'GET',
  })
}

export function beginExamination(detailId) {
  return request(`/medical/details/${detailId}/exam`, {
    method: 'PUT',
  })
}

export function uploadExamination(uuid, file, name = '') {
  const formData = new FormData()
  if (name) formData.append('name', name)
  formData.append('file', file)
  return request(`/medical/exam/${uuid}/doc`, {
    method: 'PUT',
    body: formData,
  })
}

export function deleteExaminationUpload(uuid, filename) {
  return request(`/medical/exam/${uuid}/doc/${encodeURIComponent(filename)}`, {
    method: 'DELETE',
  })
}

export function addExamination(uuid, payload) {
  return request(`/medical/exam/${uuid}`, {
    method: 'POST',
    body: payload,
  })
}

export function getVaccineOptions() {
  return request('/medical/vaccine/options', {
    method: 'GET',
  })
}

export function getAllVaccines() {
  return request('/medical/vaccine', {
    method: 'GET',
  })
}

export function getVaccines(petId) {
  return request(`/medical/vaccine/pet/${petId}`, {
    method: 'GET',
  })
}

export function addVaccine(petId, payload) {
  return request(`/medical/vaccine/pet/${petId}`, {
    method: 'POST',
    body: payload,
  })
}

export function getDewormerOptions() {
  return request('/medical/deworm/options', {
    method: 'GET',
  })
}

export function getAllDeworms() {
  return request('/medical/deworm', {
    method: 'GET',
  })
}

export function getDeworms(petId) {
  return request(`/medical/deworm/pet/${petId}`, {
    method: 'GET',
  })
}

export function addDeworm(petId, payload) {
  return request(`/medical/deworm/pet/${petId}`, {
    method: 'POST',
    body: payload,
  })
}

export function createRehabPlan(petId, payload) {
  return request(`/medical/rehab/pet/${petId}`, {
    method: 'POST',
    body: payload,
  })
}

export function getRehabPlan(id) {
  return request(`/medical/rehab/${id}`, {
    method: 'GET',
  })
}

export function getRehabPlans(query = {}) {
  return request('/medical/rehab', {
    method: 'GET',
    query,
  })
}

export function updateRehabPlanStatus(id, payload) {
  return request(`/medical/rehab/${id}/status`, {
    method: 'PUT',
    body: payload,
  })
}

export function getRehabRecords(id) {
  return request(`/medical/rehab/${id}/record`, {
    method: 'GET',
  })
}

export function addRehabRecord(id, payload) {
  const formData = new FormData()
  formData.append('step', payload.step || '')
  formData.append('reaction', payload.reaction || '')
  formData.append('note', payload.note || '')
  ;(payload.files || []).forEach((file) => {
    formData.append('files', file)
  })
  return request(`/medical/rehab/${id}/record`, {
    method: 'POST',
    body: formData,
  })
}

export function addHealthAssessment(petId, payload) {
  return request(`/medical/health/pet/${petId}`, {
    method: 'POST',
    body: payload,
  })
}

export function getHealthAssessment(id) {
  return request(`/medical/health/${id}`, {
    method: 'GET',
  })
}

export function getHealthAssessments(query = {}) {
  return request('/medical/health', {
    method: 'GET',
    query,
  })
}

export function getPetHealthAssessments(petId, query = {}) {
  return request(`/medical/health/pet/${petId}`, {
    method: 'GET',
    query,
  })
}

export function getFirstVisitRegistrations(query = {}) {
  return request('/medical/first', {
    method: 'GET',
    query,
  })
}

export function getFirstVisitRegistration(id) {
  return request(`/medical/first/${id}`, {
    method: 'GET',
  })
}

export function createFirstVisitRegistration(payload) {
  return request('/medical/first', {
    method: 'POST',
    body: payload,
  })
}

export function getItems(query = {}) {
  return request('/items/items', {
    method: 'GET',
    query,
  })
}

export function getDonations(query = {}) {
  return request('/items/donations', {
    method: 'GET',
    query,
  })
}

export function beginDonation() {
  return request('/items/donations', {
    method: 'PUT',
  })
}

export function createDonation(payload) {
  return request('/items/donations', {
    method: 'POST',
    body: payload,
  })
}

export function getCategories(query = {}) {
  return request('/items/categories', {
    method: 'GET',
    query,
  })
}

export function createCategory(payload) {
  return request('/items/categories', {
    method: 'POST',
    body: payload,
  })
}

export function createBreadingApplication(payload) {
  return request('/adopt/breading', {
    method: 'POST',
    body: payload,
  })
}
