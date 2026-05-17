<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Delete, Lock, Upload, User } from '@element-plus/icons-vue'
import { getUserById, updateUserById, uploadUserAvatar, deleteUserAvatar } from '../api/user'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const profileFormRef = ref()
const loadingProfile = ref(false)
const submitting = ref(false)
const uploadingAvatar = ref(false)
const avatarInputRef = ref()

const profileForm = reactive({
  username: '',
  email: '',
  avatar: '',
  phone: '',
  password: '',
  confirmPassword: '',
})

const profileId = computed(() => String(userStore.profile.id || ''))

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] },
  ],
  password: [
    {
      validator: (_rule, value, callback) => {
        if (!value) {
          callback()
          return
        }
        if (String(value).length < 6) {
          callback(new Error('新密码长度至少 6 位'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    {
      validator: (_rule, value, callback) => {
        if (!profileForm.password) {
          callback()
          return
        }
        if (!value) {
          callback(new Error('请再次输入新密码'))
          return
        }
        if (value !== profileForm.password) {
          callback(new Error('两次输入的新密码不一致'))
          return
        }
        callback()
      },
      trigger: ['blur', 'change'],
    },
  ],
}

function fillForm(profileData) {
  profileForm.username = profileData?.username || ''
  profileForm.email = profileData?.email || ''
  profileForm.avatar = profileData?.avatar || ''
  profileForm.phone = profileData?.phone || ''
  profileForm.password = ''
  profileForm.confirmPassword = ''
}

async function loadProfile() {
  loadingProfile.value = true
  try {
    if (!profileId.value) {
      fillForm(userStore.profile)
      return
    }
    const result = await getUserById(profileId.value)
    const mergedProfile = {
      id: profileId.value,
      username: result?.username || userStore.profile.username || '',
      email: result?.email || userStore.profile.email || '',
      avatar: result?.avatar || userStore.profile.avatar || '',
      role: Number(result?.role ?? userStore.profile.role ?? 0),
      phone: result?.phone || userStore.profile.phone || '',
    }
    userStore.setProfile(mergedProfile)
    fillForm(mergedProfile)
  } catch (error) {
    fillForm(userStore.profile)
    const message = error?.message ? String(error.message) : '加载个人信息失败'
    ElMessage.warning(message)
  } finally {
    loadingProfile.value = false
  }
}

function buildUpdatePayload() {
  const payload = {
    username: profileForm.username.trim(),
    email: profileForm.email.trim(),
    phone: profileForm.phone.trim() || undefined,
  }
  if (profileForm.password) {
    payload.password = profileForm.password
  }
  return payload
}

function syncProfileAvatar(avatar) {
  const profile = {
    ...userStore.profile,
    avatar: avatar || '',
  }
  userStore.setProfile(profile)
  profileForm.avatar = profile.avatar
}

function chooseAvatarFile() {
  avatarInputRef.value?.click()
}

async function uploadAvatar(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) {
    return
  }
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  if (!profileId.value) {
    ElMessage.warning('当前账号缺少用户 ID，请重新登录后再试')
    return
  }

  uploadingAvatar.value = true
  try {
    const avatar = await uploadUserAvatar(profileId.value, file)
    syncProfileAvatar(avatar)
    ElMessage.success('头像已上传')
  } catch (error) {
    ElMessage.warning(error?.message || '头像上传失败')
  } finally {
    uploadingAvatar.value = false
  }
}

async function removeAvatar() {
  if (!profileId.value) {
    ElMessage.warning('当前账号缺少用户 ID，请重新登录后再试')
    return
  }
  uploadingAvatar.value = true
  try {
    await deleteUserAvatar(profileId.value)
    syncProfileAvatar('')
    ElMessage.success('头像已删除')
  } catch (error) {
    ElMessage.warning(error?.message || '删除头像失败')
  } finally {
    uploadingAvatar.value = false
  }
}

async function submitProfile() {
  if (!profileFormRef.value || submitting.value) {
    return
  }

  try {
    submitting.value = true
    await profileFormRef.value.validate()

    if (!profileId.value) {
      throw new Error('当前账号缺少用户 ID，请重新登录后再试')
    }

    const payload = buildUpdatePayload()
    const result = await updateUserById(profileId.value, payload)
    const mergedProfile = {
      id: profileId.value,
      username: result?.username || payload.username,
      email: result?.email || payload.email,
      avatar: result?.avatar || payload.avatar || '',
      role: Number(result?.role ?? userStore.profile.role ?? 0),
      phone: result?.phone || payload.phone || '',
    }
    userStore.setProfile(mergedProfile)
    fillForm(mergedProfile)
    ElMessage.success('个人信息已保存')
  } catch (error) {
    const message = error?.message ? String(error.message) : '保存失败，请稍后重试'
    ElMessage.warning(message)
  } finally {
    submitting.value = false
  }
}

function resetForm() {
  fillForm(userStore.profile)
}

async function handleLogout() {
  try {
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.replace('/login')
  } catch (error) {
    const message = error?.message ? String(error.message) : '退出登录失败'
    ElMessage.warning(message)
  }
}

function goHome() {
  router.push('/')
}

onMounted(() => {
  loadProfile()
})
</script>

<template>
  <el-card class="profile-card" v-loading="loadingProfile">
    <template #header>
      <div class="profile-card-header">
        <strong>个人信息修改</strong>
        <div class="profile-actions">
          <el-button class="warm-btn" :icon="Check" :loading="submitting" @click="submitProfile" />
        </div>
      </div>
    </template>

    <el-form
      ref="profileFormRef"
      :model="profileForm"
      :rules="formRules"
      label-position="top"
      class="profile-form"
    >
      <el-form-item label="用户名" prop="username">
        <el-input v-model="profileForm.username" placeholder="请输入用户名">
          <template #prefix>
            <el-icon><User /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input v-model="profileForm.email" placeholder="请输入邮箱">
          <template #prefix>
            <el-icon><User /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="头像图片" prop="avatar">
        <div class="profile-avatar-field">
          <div class="profile-avatar-preview">
            <img v-if="profileForm.avatar" :src="profileForm.avatar" alt="用户头像" />
            <el-icon v-else><User /></el-icon>
          </div>
          <div class="profile-avatar-actions">
            <input ref="avatarInputRef" class="profile-avatar-input" type="file" accept="image/*" @change="uploadAvatar" />
            <el-button :icon="Upload" :loading="uploadingAvatar" @click="chooseAvatarFile">上传图片</el-button>
            <el-button :icon="Delete" :disabled="!profileForm.avatar || uploadingAvatar" plain @click="removeAvatar">删除头像</el-button>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="联系方式" prop="phone">
        <el-input v-model="profileForm.phone" placeholder="请输入手机号或联系电话" />
      </el-form-item>

      <el-form-item label="新密码" prop="password">
        <el-input v-model="profileForm.password" placeholder="留空表示不修改密码" show-password>
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input v-model="profileForm.confirmPassword" placeholder="请再次输入新密码" show-password>
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>

    </el-form>
  </el-card>
</template>
