export const ROLE = {
  VOLUNTEER: 1,
  WORKER: 2,
  DONOR: 4,
  DOCTOR: 8,
  ADMIN: 16,
}

export const MANAGED_ROLE_VALUES = [ROLE.VOLUNTEER, ROLE.WORKER, ROLE.DONOR, ROLE.DOCTOR, ROLE.ADMIN]

export const roleOptions = [
  { label: '志愿者', value: ROLE.VOLUNTEER },
  { label: '工作人员', value: ROLE.WORKER },
  { label: '捐赠者', value: ROLE.DONOR },
  { label: '兽医', value: ROLE.DOCTOR },
  { label: '管理员', value: ROLE.ADMIN },
]

export function hasRole(role, bit) {
  return (Number(role || 0) & bit) === bit
}

export function expandRoleValues(role) {
  const value = Number(role || 0)
  if (hasRole(value, ROLE.ADMIN)) {
    return [...MANAGED_ROLE_VALUES]
  }
  const values = roleOptions.filter((item) => hasRole(value, item.value)).map((item) => item.value)
  if (values.includes(ROLE.WORKER) && !values.includes(ROLE.VOLUNTEER)) {
    values.unshift(ROLE.VOLUNTEER)
  }
  return values
}

export function visibleRoleLabels(role) {
  const values = expandRoleValues(role)
  if (values.includes(ROLE.ADMIN)) {
    return ['管理员']
  }
  const priority = [ROLE.WORKER, ROLE.DOCTOR, ROLE.VOLUNTEER, ROLE.DONOR]
  return priority
    .filter((value) => values.includes(value) && !(value === ROLE.VOLUNTEER && values.includes(ROLE.WORKER)))
    .map((value) => roleOptions.find((item) => item.value === value)?.label)
    .filter(Boolean)
}

export function roleText(role) {
  const labels = visibleRoleLabels(role)
  return labels.length ? labels.join('、') : '普通用户'
}

export function primaryRoleLabel(role) {
  return visibleRoleLabels(role)[0] || '普通用户'
}

export function hasMoreRoles(role) {
  return visibleRoleLabels(role).length > 1
}

export function normalizeRoleValues(values) {
  const selected = new Set(values.map(Number))
  if (selected.has(ROLE.ADMIN)) {
    return [...MANAGED_ROLE_VALUES]
  }
  if (selected.has(ROLE.WORKER)) {
    selected.add(ROLE.VOLUNTEER)
  }
  return MANAGED_ROLE_VALUES.filter((item) => selected.has(item))
}

export function buildRoleValue(values) {
  if (values.includes(ROLE.ADMIN)) {
    return ROLE.ADMIN
  }
  return normalizeRoleValues(values).reduce((sum, item) => sum | Number(item), 0)
}

export const petStatusOptions = [
  { label: '待审核', value: 'WAITING' },
  { label: '审核未通过', value: 'AGAINST' },
  { label: '查找中', value: 'FINDING' },
  { label: '已死亡/无法救助', value: 'DIED' },
  { label: '超时放弃', value: 'TIMEOUT' },
  { label: '已收容', value: 'SHELTERED' },
  { label: '可领养', value: 'HEALTH' },
  { label: '已领养', value: 'ADOPTED' },
  { label: '已回家', value: 'HOME' },
]

export const petStatusLabelMap = Object.fromEntries(petStatusOptions.map(o => [o.value, o.label]))

export function petStatusText(status) {
  return petStatusOptions.find((item) => item.value === status)?.label || status || '待补充'
}

export function petLocationText(pet) {
  const location = pet.locations?.[0]
  if (!location) return '位置待补充'
  return [location.province, location.city, location.district, location.detailAddress].filter(Boolean).join(' · ')
}

export const lostPetStatusOptions = [
  { label: '寻找中', value: 'SEARCHING' },
  { label: '认领中', value: 'CLAIMING' },
  { label: '已找到', value: 'CLAIMED' },
  { label: '已关闭', value: 'CLOSED' },
]

export function lostPetStatusText(status) {
  return lostPetStatusOptions.find((item) => item.value === status)?.label || status || '待补充'
}

export function lostPetLocationText(row) {
  const location = row.location
  if (!location) return '位置待补充'
  return [location.province, location.city, location.district, location.detailAddress].filter(Boolean).join(' · ')
}

export const rescueTaskTypeOptions = [
  { label: '发现流浪宠物', value: 'FIND' },
  { label: '医疗救助', value: 'MEDICAL' },
  { label: '其他协助', value: 'OTHER' },
]

export function rescueTaskTypeText(type) {
  return rescueTaskTypeOptions.find((item) => item.value === type)?.label || type || '待补充'
}

export const rescueTaskStatusOptions = [
  { label: '审核通过', value: 'APPROVED' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '任务完成', value: 'COMPLETED' },
  { label: '已废弃', value: 'DISCARDED' },
]

export const taskStatusLabelMap = Object.fromEntries([
  ...rescueTaskStatusOptions,
  { label: '待审核', value: 'CREATED' },
])

export function rescueTaskStatusText(status) {
  const map = {
    CREATED: '已创建',
    APPROVED: '审核通过',
    PROCESSING: '处理中',
    COMPLETED: '任务完成',
    DISCARDED: '已废弃',
  }
  return map[status] || status || '待补充'
}

export function rescueTaskLocationText(task) {
  const location = task.location
  if (!location) return '位置待补充'
  return [location.province, location.city, location.district, location.detailAddress].filter(Boolean).join(' · ')
}
