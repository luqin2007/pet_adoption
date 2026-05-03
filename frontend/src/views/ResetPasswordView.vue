<script setup>
import { Icon } from '@iconify/vue'
import { ArrowLeft, Lock } from '@element-plus/icons-vue'
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { resetPassword } from '../api/user'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const isSubmitting = ref(false)
const isSuccess = ref(false)

const form = reactive({
  id: '',
  password: '',
  confirmPassword: ''
})

onMounted(() => {
  const resetId = route.query.id
  if (!resetId) {
    ElMessage.error('无效的重置链接，参数错误！')
    router.push('/login')
  } else {
    form.id = resetId
  }
})

function validateConfirmPassword(_rule, value, callback) {
  if (!value) {
    callback(new Error('请再次输入密码'))
    return
  }
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
    return
  }
  callback()
}

const rules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: ['blur', 'change'] }
  ]
}

function goHome() {
  router.push('/')
}

function goLogin() {
  router.push('/login')
}

async function submitReset() {
  if (!formRef.value || isSubmitting.value) return
  
  try {
    isSubmitting.value = true
    await formRef.value.validate()
    
    await resetPassword({
      id: form.id,
      password: form.password
    })
    
    isSuccess.value = true
    setTimeout(() => {
      if (isSuccess.value) {
        router.push('/login')
      }
    }, 5000)
    
  } catch (err) {
    if (err && typeof err === 'object' && err.message) {
      ElMessage.warning(err.message)
    } else {
      ElMessage.warning('重置失败，请检查填写内容或重试')
    }
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-shell">
      <section class="auth-aside">
        <el-button class="back-home" text @click="goHome">
          <el-icon><ArrowLeft /></el-icon>
          返回首页
        </el-button>
        <h1>找回密码</h1>
      </section>

      <section class="auth-card-wrap">
        <el-card class="auth-card" shadow="hover">
          <div v-if="!isSuccess" class="reset-wrap">
            <h2 style="font-size: 1.5rem; color: #1f2937; margin-bottom: 0.5rem; text-align: center;">重置密码</h2>
            <p style="color: #6b7280; margin-bottom: 2rem; font-size: 0.875rem; text-align: center;">
              请为您的账号设置一个新的密码
            </p>
            <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
              <el-form-item label="新密码" prop="password">
                <el-input v-model="form.password" placeholder="请输入新密码" show-password>
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input v-model="form.confirmPassword" placeholder="请再次输入新密码" show-password>
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <el-button class="auth-submit" type="warning" :loading="isSubmitting" @click="submitReset" style="margin-top: 1rem; width: 100%;">
                保存并登录
              </el-button>
              
              <div style="text-align: center; margin-top: 1rem;">
                <el-button text type="info" @click="goLogin">返回登录</el-button>
              </div>
            </el-form>
          </div>
          
          <div v-else class="reset-success-wrap" style="text-align: center; padding: 2rem 0;">
             <Icon icon="mdi:check-circle-outline" style="font-size: 4rem; color: #10b981; margin-bottom: 1rem;" />
             <h2 style="font-size: 1.5rem; color: #1f2937; margin-bottom: 1rem;">密码修改成功</h2>
             <p style="color: #6b7280; margin-bottom: 2rem;">
               密码已重置，请使用新密码登录。<br/>(5 秒后跳转到登录页)
             </p>
             <el-button type="warning" @click="goLogin" style="width: 100%;">
                立即登录
             </el-button>
          </div>
        </el-card>
      </section>
    </div>
  </div>
</template>

<style scoped>
.auth-submit {
  width: 100%;
  padding: 12px 20px;
  font-size: 1.1rem;
}
</style>
