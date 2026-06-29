<script setup>
import { Icon } from '@iconify/vue'
import { ElMessage } from 'element-plus'
import { computed, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Lock, Message, User } from '@element-plus/icons-vue'
import { checkEmailExists } from '../api/user'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const activeTab = ref('login')
const loginRef = ref()
const registerRef = ref()
const codeCountdown = ref(0)
const sendingCode = ref(false)
const loginSubmitting = ref(false)
const registerSubmitting = ref(false)
let codeTimer = null

const loginForm = reactive({
  username: '',
  password: '',
  remember: true,
})

const registerForm = reactive({
  name: '',
  email: '',
  emailCode: '',
  password: '',
  confirmPassword: '',
})

const codeButtonText = computed(() => {
  if (sendingCode.value) {
    return '发送中...'
  }
  if (codeCountdown.value > 0) {
    return `${codeCountdown.value}s 后重试`
  }
  return '获取验证码'
})

const redirectPath = computed(() => {
  const redirect = route.query.redirect
  if (typeof redirect !== 'string') {
    return '/'
  }
  if (!redirect.startsWith('/') || redirect.startsWith('/login')) {
    return '/'
  }
  return redirect
})

async function navigateAfterAuth() {
  if (typeof window !== 'undefined') {
    const search = new URLSearchParams(window.location.search)
    let target = search.get('redirect') || redirectPath.value || '/'
    if (!target.startsWith('/') || target.startsWith('/login')) {
      target = '/'
    }
    window.location.href = target
    return
  }

  await router.replace(redirectPath.value || '/')
}

watch(
  () => userStore.accessToken,
  (token) => {
    if (token) {
      navigateAfterAuth()
    }
  },
  { immediate: true },
)

function validateConfirmPassword(_rule, value, callback) {
  if (!value) {
    callback(new Error('请再次输入密码'))
    return
  }
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
    return
  }
  callback()
}

function validateEmailCode(_rule, value, callback) {
  if (!value) {
    callback(new Error('请输入邮箱验证码'))
    return
  }
  callback()
}

async function validateRegisterEmail(_rule, value, callback) {
  if (!value) {
    callback(new Error('请输入邮箱'))
    return
  }
  const email = String(value).trim()
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailPattern.test(email)) {
    callback(new Error('邮箱格式不正确'))
    return
  }
  try {
    await checkEmailExists(email)
    callback()
  } catch (error) {
    callback(new Error(getErrorMessage(error, '邮箱已存在')))
  }
}

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const registerRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [{ validator: validateRegisterEmail, trigger: 'blur' }],
  emailCode: [{ validator: validateEmailCode, trigger: ['blur', 'change'] }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 位', trigger: 'blur' },
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: ['blur', 'change'] }],
}

function startCodeCountdown(seconds) {
  if (codeTimer) {
    clearInterval(codeTimer)
    codeTimer = null
  }
  codeCountdown.value = seconds
  codeTimer = setInterval(() => {
    codeCountdown.value -= 1
    if (codeCountdown.value <= 0) {
      codeCountdown.value = 0
      clearInterval(codeTimer)
      codeTimer = null
    }
  }, 1000)
}

function getErrorMessage(error, fallback) {
  if (error && typeof error === 'object' && 'message' in error && error.message) {
    return String(error.message)
  }
  return fallback
}

async function sendEmailCode() {
  if (codeCountdown.value > 0 || sendingCode.value || !registerRef.value) {
    return
  }
  try {
    await registerRef.value.validateField('email')
    sendingCode.value = true
    await checkEmailExists(registerForm.email.trim())
    await userStore.sendRegisterCode(registerForm.email.trim())
    ElMessage.success(`验证码已发送到 ${registerForm.email.trim()}`)
    startCodeCountdown(30)
  } catch (error) {
    ElMessage.warning(getErrorMessage(error, '请先输入有效邮箱地址'))
  } finally {
    sendingCode.value = false
  }
}

async function submitLogin() {
  if (!loginRef.value || loginSubmitting.value) {
    return
  }
  try {
    loginSubmitting.value = true
    await loginRef.value.validate()
    const loginData = await userStore.login({
      username: loginForm.username.trim(),
      password: loginForm.password,
    })
    if (!loginData?.accessToken) {
      throw new Error('登录成功但未返回 accessToken')
    }
    ElMessage.success(`登录成功，欢迎回来 ${loginData.username || ''}`.trim())
    await navigateAfterAuth()
  } catch (error) {
    ElMessage.warning(getErrorMessage(error, '请完善登录信息'))
  } finally {
    loginSubmitting.value = false
  }
}

async function submitRegister() {
  if (!registerRef.value || registerSubmitting.value) {
    return
  }
  try {
    registerSubmitting.value = true
    await registerRef.value.validate()
    const registerData = await userStore.register({
      username: registerForm.name.trim(),
      password: registerForm.password,
      email: registerForm.email.trim(),
      code: registerForm.emailCode.trim(),
    })
    if (registerData?.accessToken) {
      ElMessage.success('注册并登录成功')
      await navigateAfterAuth()
      return
    }
    ElMessage.success('注册成功，请使用新账号登录')
    activeTab.value = 'login'
    loginForm.username = registerForm.name.trim()
    loginForm.password = ''
  } catch (error) {
    ElMessage.warning(getErrorMessage(error, '请完善注册信息'))
  } finally {
    registerSubmitting.value = false
  }
}

function goHome() {
  router.push('/')
}

onBeforeUnmount(() => {
  if (codeTimer) {
    clearInterval(codeTimer)
    codeTimer = null
  }
})
</script>

<template>
  <div class="auth-page">
    <div class="auth-shell">
      <section class="auth-aside">
        <el-button class="back-home" text @click="goHome">
          <el-icon><ArrowLeft /></el-icon>
          返回首页
        </el-button>
        <h1>欢迎加入暖窝救助</h1>
        <ul>
          <li>
            <Icon icon="mdi:account-heart-outline" />
            <span>志愿同行守护</span>
          </li>
          <li>
            <Icon icon="mdi:home-heart" />
            <span>领养代替购买</span>
          </li>
          <li>
            <Icon icon="mdi:home-plus-outline" />
            <span>爱心寄养接力</span>
          </li>
          <li>
            <Icon icon="mdi:map-marker-alert-outline" />
            <span>走失宠物认领</span>
          </li>
        </ul>
      </section>

      <section class="auth-card-wrap">
        <el-card class="auth-card" shadow="hover">
          <el-tabs v-model="activeTab" stretch>
            <el-tab-pane label="登录" name="login">
              <el-form ref="loginRef" :model="loginForm" :rules="loginRules" label-position="top">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="loginForm.username" placeholder="请输入用户名">
                    <template #prefix>
                      <el-icon><User /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>

                <el-form-item label="密码" prop="password">
                  <el-input v-model="loginForm.password" placeholder="请输入密码" show-password>
                    <template #prefix>
                      <el-icon><Lock /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>

                <div class="auth-form-row">
                  <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
                  <el-button text type="warning" @click="router.push('/forgot-password')">忘记密码</el-button>
                </div>

                <el-button class="auth-submit" type="warning" :loading="loginSubmitting" @click="submitLogin">
                  立即登录
                </el-button>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="注册" name="register">
              <el-form ref="registerRef" :model="registerForm" :rules="registerRules" label-position="top">
                <el-form-item label="用户名" prop="name">
                  <el-input v-model="registerForm.name" placeholder="请输入用户名">
                    <template #prefix>
                      <el-icon><User /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>

                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="registerForm.email" placeholder="请输入邮箱">
                    <template #prefix>
                      <el-icon><Message /></el-icon>
                    </template>
                    <template #append>
                      <el-button
                        class="auth-code-btn"
                        :disabled="sendingCode || codeCountdown > 0"
                        @click="sendEmailCode"
                      >
                        {{ codeButtonText }}
                      </el-button>
                    </template>
                  </el-input>
                </el-form-item>

                <el-form-item label="邮箱验证码" prop="emailCode">
                  <el-input v-model="registerForm.emailCode" placeholder="请输入 6 位邮箱验证码">
                    <template #prefix>
                      <el-icon><Message /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>

                <el-form-item label="密码" prop="password">
                  <el-input v-model="registerForm.password" placeholder="请设置密码" show-password>
                    <template #prefix>
                      <el-icon><Lock /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>

                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input
                    v-model="registerForm.confirmPassword"
                    placeholder="请再次输入密码"
                    show-password
                  >
                    <template #prefix>
                      <el-icon><Lock /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>

                <el-button
                  class="auth-submit"
                  type="warning"
                  :loading="registerSubmitting"
                  @click="submitRegister"
                >
                  创建账号
                </el-button>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </section>
    </div>
  </div>
</template>
