import { ROLE, hasRole } from './roles'

export { ROLE, hasRole as userHasRole } from './roles'

export function userCanEditPet(profile, pet) {
  if (!profile?.id || !pet?.id) {
    return false
  }

  const role = Number(profile.role || 0)
  if (hasRole(role, ROLE.WORKER)) {
    return true
  }

  const status = String(pet.status || '')
  if (status === 'WAITING' || status === 'AGAINST') {
    return String(profile.id) === String(pet.discoverId || '')
  }
  if (status === 'ADOPTED') {
    return false
  }

  return hasRole(role, ROLE.VOLUNTEER) || hasRole(role, ROLE.DOCTOR)
}
