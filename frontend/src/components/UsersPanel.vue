<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, RefreshRight, User } from '@element-plus/icons-vue'
import { getUsers, removeUserById, updateUserById } from '../api/user'
import { useUserStore } from '../stores/user'
import {
  ROLE,
  hasRole,
  roleOptions,
  roleText,
  expandRoleValues,
  normalizeRoleValues,
  buildRoleValue,
  visibleRoleLabels,
  primaryRoleLabel,
  hasMoreRoles,
  MANAGED_ROLE_VALUES,
} from '../utils/roles'
import { useConsoleGuards } from '../composables/useConsoleGuards'
import TableActionColumnHeader from './TableActionColumnHeader.vue'

const emit = defineEmits(['profile-updated'])

const userStore = useUserStore()
const userFormRef = ref()
const loadingUsers = ref(false)
const userActionCollapsed = ref(false)
const userDialogVisible = ref(false)
const savingUser = ref(false)
const userKeyword = ref('')
const userRows = ref([])
const userTotal = ref(0)
const userPage = reactive({
  page: 1,
  size: 10,
})

const userForm = reactive({
  id: '',
  username: '',
  email: '',
  phone: '',
  password: '',
  roles: [],
})

const userRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] },
  ],
  password: [
    {
      validator: (_rule, value, callback) => {
        if (!value || String(value).length >= 6) {
          callback()
          return
        }
        callback(new Error('新密码长度至少 6 位'))
      },
      trigger: 'blur',
    },
  ],
}

const profileId = computed(() => String(userStore.profile.id || ''))
const loginRole = computed(() => Number(userStore.profile.role || 0))
const isLoginAdmin = computed(() => (loginRole.value & ROLE.ADMIN) === ROLE.ADMIN)
const canManageUsers = computed(() => isLoginAdmin.value || hasRole(loginRole.value, ROLE.WORKER))

function canEditUser(row) {
  return isLoginAdmin.value || !hasRole(row?.role, ROLE.ADMIN)
}

const visibleUsers = computed(() => {
  const text = userKeyword.value.trim().toLowerCase()
  if (!text) {
    return userRows.value
  }
  return userRows.value.filter((item) =>
    [item.username, item.email, item.phone, roleText(item.role)]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
      .includes(text),
  )
})

async function loadUsers() {
  if (!canManageUsers.value) {
    return
  }
  loadingUsers.value = true
  try {
    const result = await getUsers({
      page: userPage.page,
      size: userPage.size,
      sort: 'update_time',
      order: 'desc',
    })
    userRows.value = Array.isArray(result?.records) ? result.records : []
    userTotal.value = Number(result?.total || userRows.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载用户列表失败')
  } finally {
    loadingUsers.value = false
  }
}

function openUserDialog(row) {
  userForm.id = String(row.id || '')
  userForm.username = row.username || ''
  userForm.email = row.email || ''
  userForm.phone = row.phone || ''
  userForm.password = ''
  userForm.roles = expandRoleValues(row.role)
  userDialogVisible.value = true
}

function handleRoleChange(values) {
  userForm.roles = normalizeRoleValues(values)
}

async function saveUser() {
  if (!userFormRef.value || savingUser.value) {
    return
  }
  savingUser.value = true
  try {
    await userFormRef.value.validate()
    const payload = {
      username: userForm.username.trim(),
      email: userForm.email.trim(),
      phone: userForm.phone.trim() || undefined,
      role: buildRoleValue(userForm.roles),
    }
    if (userForm.password) {
      payload.password = userForm.password
    }
    await updateUserById(userForm.id, payload)
    ElMessage.success('用户信息已保存')
    userDialogVisible.value = false
    await loadUsers()
    if (userForm.id === profileId.value) {
      emit('profile-updated')
    }
  } catch (error) {
    ElMessage.warning(error?.message || '保存用户信息失败')
  } finally {
    savingUser.value = false
  }
}

async function deleteUser(row) {
  try {
    await ElMessageBox.confirm(`确认删除账号「${row.username}」？`, '删除用户', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await removeUserById(row.id)
    ElMessage.success('用户已删除')
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.warning(error?.message || '删除用户失败')
    }
  }
}

function changeUserPage(page) {
  userPage.page = page
  loadUsers()
}

useConsoleGuards()

onMounted(() => {
  loadUsers()
})

defineExpose({ loadUsers })
</script>

<template>
  <el-card class="profile-card user-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>用户账号管理</strong>
        <span>维护用户资料和角色</span>
      </div>
    </template>

    <section class="filter-panel pet-directory-filter-panel">
      <div class="pet-filter-row pet-filter-row-primary">
        <el-input v-model="userKeyword" clearable placeholder="按用户名、邮箱、联系方式或角色搜索" />
        <div class="pet-filter-action">
          <el-button class="warm-btn" :icon="RefreshRight" :loading="loadingUsers" @click="loadUsers">刷新</el-button>
        </div>
      </div>
    </section>

    <el-table :data="visibleUsers" v-loading="loadingUsers" class="user-admin-table">
      <el-table-column prop="username" label="用户名" min-width="130" />
      <el-table-column prop="email" label="邮箱" min-width="190" />
      <el-table-column prop="phone" label="联系方式" min-width="130">
        <template #default="{ row }">
          {{ row.phone || '未填写' }}
        </template>
      </el-table-column>
      <el-table-column label="角色" min-width="150">
        <template #default="{ row }">
          <el-tooltip :content="roleText(row.role)" placement="top" :disabled="!hasMoreRoles(row.role)">
            <span class="user-role-cell">
              <el-tag class="user-role-tag" type="warning" effect="plain">
                {{ primaryRoleLabel(row.role) }}
              </el-tag>
              <span v-if="hasMoreRoles(row.role)" class="user-role-more">...</span>
            </span>
          </el-tooltip>
        </template>
      </el-table-column>
        <el-table-column width="40" class-name="action-col">
          <template #header>
            <TableActionColumnHeader title="操作" :collapsed="userActionCollapsed" @toggle="userActionCollapsed = !userActionCollapsed" />
          </template>
          <template #default="{ row }">
            <div class="table-action-cell">
              <div class="table-action-panel" :class="{ 'is-collapsed': userActionCollapsed }">
                <el-button text type="warning" :disabled="!canEditUser(row)" @click="openUserDialog(row)">编辑</el-button>
                <el-button
                  text
                  type="danger"
                  :disabled="String(row.id) === profileId || !canEditUser(row)"
                  @click="deleteUser(row)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </template>
        </el-table-column>
    </el-table>

    <div class="user-admin-pagination">
      <el-pagination
        layout="prev, pager, next, total"
        :current-page="userPage.page"
        :page-size="userPage.size"
        :total="userTotal"
        @current-change="changeUserPage"
      />
    </div>
  </el-card>

  <el-dialog v-model="userDialogVisible" title="编辑用户账号" width="560px">
    <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-position="top">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="userForm.username" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="userForm.email" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="联系方式" prop="phone">
        <el-input v-model="userForm.phone" placeholder="请输入联系方式" />
      </el-form-item>
      <el-form-item label="重置密码" prop="password">
        <el-input v-model="userForm.password" placeholder="留空表示不修改密码" show-password />
      </el-form-item>
      <el-form-item label="角色">
        <el-checkbox-group v-model="userForm.roles" @change="handleRoleChange">
          <el-checkbox
            v-for="item in roleOptions"
            :key="item.value"
            :label="item.value"
            :disabled="(item.value === ROLE.ADMIN && !isLoginAdmin) || (item.value !== ROLE.ADMIN && userForm.roles.includes(ROLE.ADMIN)) || (item.value === ROLE.VOLUNTEER && userForm.roles.includes(ROLE.WORKER))"
          >
            {{ item.label }}
          </el-checkbox>
        </el-checkbox-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="userDialogVisible = false">取消</el-button>
      <el-button type="warning" :loading="savingUser" @click="saveUser">保存</el-button>
    </template>
  </el-dialog>
</template>
