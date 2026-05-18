export function formatDate(value, fallback = '') {
  if (!value) return fallback
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

export function formatDateShort(value, fallback = '时间待补充') {
  if (!value) return fallback
  const text = String(value)
  return text.slice(0, 10)
}

export function formatDateTime(value, fallback = '') {
  if (!value) return fallback
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export const ADOPTION_STATUS_MAP = {
  CREATE: '已提交',
  PASS: '审核通过',
  REJECT: '审核拒绝',
  AGREEMENT_DRAFT: '协议草拟中',
  AGREEMENT_PENDING_CONFIRM: '待确认',
  AGREEMENT_SIGNED: '协议已签署',
  TRACKING: '回访中',
  FINISH: '流程完成',
  CANCEL: '已取消',
}

export const DONATION_STATUS_MAP = {
  CREATED: '已创建',
  PENDING: '待处理',
  TRANSFERRING: '运输中',
  RECEIVED: '已接收',
  STOCKED: '已入库',
  REFUSED: '已拒绝',
  BACKING: '退回中',
  CANCELED: '已取消',
  CLOSED: '已关闭',
}

export const VOLUNTEER_APPLICATION_STATUS_MAP = {
  UNDER_REVIEW: '审核中',
  APPROVED: '审核通过',
  REJECTED: '审核拒绝',
  CANCELED: '已取消',
  DISABLED: '已禁用',
}

export function adoptionStatusText(status) {
  return ADOPTION_STATUS_MAP[status] || status || ''
}

export function adoptionStatusTagType(status) {
  if (status === 'PASS' || status === 'AGREEMENT_SIGNED' || status === 'FINISH') return 'success'
  if (status === 'REJECT' || status === 'CANCEL') return 'info'
  if (status === 'AGREEMENT_DRAFT' || status === 'TRACKING') return 'primary'
  if (status === 'AGREEMENT_PENDING_CONFIRM') return 'warning'
  return 'warning'
}
