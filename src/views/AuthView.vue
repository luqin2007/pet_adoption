<script setup>
import { Icon } from '@iconify/vue'
import { ElMessage } from 'element-plus'
import { computed, onBeforeUnmount, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Lock, Message, User } from '@element-plus/icons-vue'

const router = useRouter()
const activeTab = ref('login')
const loginRef = ref()
const registerRef = ref()
const codeCountdown = ref(0)
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
  if (codeCountdown.value > 0) {
    return `${codeCountdown.value}s 后重试`
  }
  return '获取验证码'
})

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
  if (!/^\d{6}$/.test(value)) {
    callback(new Error('验证码应为 6 位数字'))
    return
  }
  callback()
}

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const registerRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] },
  ],
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

async function sendEmailCode() {
  if (codeCountdown.value > 0 || !registerRef.value) {
    return
  }
  try {
    await registerRef.value.validateField('email')
    ElMessage.success(`验证码已发送到 ${registerForm.email}`)
    startCodeCountdown(60)
  } catch {
    ElMessage.warning('请先输入有效邮箱地址')
  }
}

async function submitLogin() {
  if (!loginRef.value) {
    return
  }
  try {
    await loginRef.value.validate()
    ElMessage.success('登录成功，欢迎回来')
    router.push('/')
  } catch {
    ElMessage.warning('请完善登录信息')
  }
}

async function submitRegister() {
  if (!registerRef.value) {
    return
  }
  try {
    await registerRef.value.validate()
    ElMessage.success('注册成功，请使用新账号登录')
    activeTab.value = 'login'
    loginForm.username = registerForm.name
    loginForm.password = ''
  } catch {
    ElMessage.warning('请完善注册信息')
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
            <Icon icon="mdi:hand-heart-outline" />
            <span>追踪你参与的救助任务</span>
          </li>
          <li>
            <Icon icon="mdi:home-heart" />
            <span>查看领养流程与回访安排</span>
          </li>
          <li>
            <Icon icon="mdi:shield-check-outline" />
            <span>统一管理账号和隐私设置</span>
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
                  <el-button text type="warning">忘记密码</el-button>
                </div>

                <el-button class="auth-submit" type="warning" @click="submitLogin">立即登录</el-button>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="注册" name="register">
              <el-form ref="registerRef" :model="registerForm" :rules="registerRules" label-position="top">
                <el-form-item label="姓名" prop="name">
                  <el-input v-model="registerForm.name" placeholder="请输入姓名">
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
                      <el-button class="auth-code-btn" :disabled="codeCountdown > 0" @click="sendEmailCode">
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

                <el-button class="auth-submit" type="warning" @click="submitRegister">创建账号</el-button>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </section>
    </div>
  </div>
</template>
