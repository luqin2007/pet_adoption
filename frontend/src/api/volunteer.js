import { request } from './http'

export function getRecruitments(query = {}) {
  return request('/volunteers/recruitments', {
    method: 'GET',
    query,
  })
}

export function getRecruitmentById(id) {
  return request(`/volunteers/recruitments/${id}`, {
    method: 'GET',
  })
}

export function createRecruitment(payload) {
  return request('/volunteers/recruitments', {
    method: 'POST',
    body: payload,
  })
}

export function updateRecruitment(id, payload) {
  return request(`/volunteers/recruitments/${id}`, {
    method: 'PUT',
    body: payload,
  })
}

export function updateRecruitmentStatus(id, payload) {
  return request(`/volunteers/recruitments/${id}`, {
    method: 'PATCH',
    body: payload,
  })
}

export function createVolunteerApplication(payload) {
  return request('/volunteers/applications', {
    method: 'POST',
    body: payload,
  })
}

export function getVolunteerApplications(query = {}) {
  return request('/volunteers/applications', {
    method: 'GET',
    query,
  })
}

export function getVolunteerApplicationById(id) {
  return request(`/volunteers/applications/${id}`, {
    method: 'GET',
  })
}

export function updateVolunteerApplicationStatus(id, payload) {
  return request(`/volunteers/applications/${id}`, {
    method: 'PATCH',
    body: payload,
  })
}

export function getVolunteerProfiles(query = {}) {
  return request('/volunteers/profiles', {
    method: 'GET',
    query,
  })
}

export function getVolunteerProfile(id) {
  return request(`/volunteers/profiles/${id}`, {
    method: 'GET',
  })
}

export function updateVolunteerProfile(id, payload) {
  return request(`/volunteers/profiles/${id}`, {
    method: 'PUT',
    body: payload,
  })
}

export function updateVolunteerProfileStatus(id, status) {
  return request(`/volunteers/profiles/${id}/${status}`, {
    method: 'POST',
  })
}

export function getVolunteerRewards(query = {}) {
  return request('/volunteers/rewards', {
    method: 'GET',
    query,
  })
}

export function getVolunteerReward(id) {
  return request(`/volunteers/rewards/${id}`, {
    method: 'GET',
  })
}

export function createVolunteerReward(payload) {
  return request('/volunteers/rewards', {
    method: 'POST',
    body: payload,
  })
}

export function issueVolunteerReward(id) {
  return request(`/volunteers/rewards/${id}/issue`, {
    method: 'POST',
  })
}

export function getVolunteerShifts(query = {}) {
  return request('/volunteers/shifts', {
    method: 'GET',
    query,
  })
}

export function getVolunteerShift(id) {
  return request(`/volunteers/shifts/${id}`, {
    method: 'GET',
  })
}

export function createVolunteerShift(payload) {
  return request('/volunteers/shifts', {
    method: 'POST',
    body: payload,
  })
}

export function updateVolunteerShift(id, payload) {
  return request(`/volunteers/shifts/${id}`, {
    method: 'PUT',
    body: payload,
  })
}

export function updateVolunteerShiftStatus(id, payload) {
  return request(`/volunteers/shifts/${id}`, {
    method: 'PATCH',
    body: payload,
  })
}

export function getVolunteerServiceRecords(query = {}) {
  return request('/volunteers/records', {
    method: 'GET',
    query,
  })
}

export function getVolunteerServiceRecord(id) {
  return request(`/volunteers/records/${id}`, {
    method: 'GET',
  })
}

export function createVolunteerServiceRecord(shiftId, payload) {
  return request(`/volunteers/shifts/${shiftId}/records`, {
    method: 'POST',
    body: payload,
  })
}

export function updateVolunteerServiceRecordStatus(id, payload) {
  return request(`/volunteers/records/${id}`, {
    method: 'PATCH',
    body: payload,
  })
}
