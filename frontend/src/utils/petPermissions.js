export const ROLE = {
  VOLUNTEER: 1,
  WORKER: 2,
  DONOR: 4,
  DOCTOR: 8,
  ADMIN: 16,
}

export function userHasRole(role, bit) {
  const value = Number(role || 0)
  if ((value & ROLE.ADMIN) === ROLE.ADMIN) {
    return true
  }
  if (bit === ROLE.VOLUNTEER && (value & ROLE.WORKER) === ROLE.WORKER) {
    return true
  }
  return (value & bit) === bit
}

export function userCanEditPet(profile, pet) {
  if (!profile?.id || !pet?.id) {
    return false
  }

  const role = Number(profile.role || 0)
  if (userHasRole(role, ROLE.WORKER)) {
    return true
  }

  const status = String(pet.status || '')
  if (status === 'WAITING' || status === 'AGAINST') {
    return String(profile.id) === String(pet.discoverId || '')
  }
  if (status === 'ADOPTED') {
    return false
  }

  return userHasRole(role, ROLE.VOLUNTEER) || userHasRole(role, ROLE.DOCTOR)
}
