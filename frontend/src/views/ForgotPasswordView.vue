<script setup>
import { Icon } from '@iconify/vue'
import { ArrowLeft, Message } from '@element-plus/icons-vue'
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { checkEmailExists, forgetPassword } from '../api/user'

const router = useRouter()
const formRef = ref()
const isSubmitting = ref(false)
const isSuccess = ref(false)

const form = reactive({
  email: ''
})

async function validateEmailExists(_rule, value, callback) {
  if (!value) {
    callback(new Error('请输入邮箱'))
    return
  }
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailPattern.test(value)) {
    callback(new Error('邮箱格式不正确'))
    return
  }
  try {
    // 检查邮箱是否存在，如果存在则正常，如果不存在则抛出错误
    // 后端的 checkEmailExist 是如果存在返回 false，如果是用于验证已有的通常逻辑不同
    // 我们先尝试发验证，这儿直接发就行了
    callback()
  } catch (error) {
    callback(error)
  }
}

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { validator: validateEmailExists, trigger: 'blur' }
  ]
}

function goHome() {
  router.push('/')
}

function goLogin() {
  router.push('/login')
}

async function submitResetRequest() {
  if (!formRef.value || isSubmitting.value) return
  
  try {
    isSubmitting.value = true
    await formRef.value.validate()
    
    // 这里调用 backend 发生重置链接
    await forgetPassword(form.email.trim())
    
    isSuccess.value = true
    ElMessage.success(`重置密码邮件已发送至 ${form.email}，请查收`)
  } catch (err) {
    if (err && typeof err === 'object' && err.message) {
      ElMessage.warning(err.message)
    } else {
      ElMessage.warning('请检查输入的邮箱格式')
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
            <h2 style="font-size: 1.5rem; color: #1f2937; margin-bottom: 0.5rem; text-align: center;">忘记密码</h2>
            <p style="color: #6b7280; margin-bottom: 2rem; font-size: 0.875rem; text-align: center;">
              请输入您注册时使用的电子邮箱
            </p>
            <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
              <el-form-item label="电子邮箱" prop="email">
                <el-input v-model="form.email" placeholder="请输入绑定的邮箱">
                  <template #prefix>
                    <el-icon><Message /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <el-button class="auth-submit" type="warning" :loading="isSubmitting" @click="submitResetRequest" style="margin-top: 1rem; width: 100%;">
                发送重置链接
              </el-button>
              
              <div style="text-align: center; margin-top: 1rem;">
                <el-button text type="info" @click="goLogin">返回登录</el-button>
              </div>
            </el-form>
          </div>
          
          <div v-else class="reset-success-wrap" style="text-align: center; padding: 2rem 0;">
             <Icon icon="mdi:email-check-outline" style="font-size: 4rem; color: #10b981; margin-bottom: 1rem;" />
             <h2 style="font-size: 1.5rem; color: #1f2937; margin-bottom: 1rem;">邮件已发送</h2>
             <p style="color: #6b7280; margin-bottom: 2rem;">
               包含重置操作链接的邮件已成功投递，请前往邮箱查收并按提示操作。<br/>(链接有效时间 10 分钟)
             </p>
             <el-button type="warning" @click="goLogin" style="width: 100%;">
                返回登录页面
             </el-button>
          </div>
        </el-card>
      </section>
    </div>
  </div>
</template>

<style scoped>
/* 样式大部分继承自父级或外部定义的 global style 
 * 为了防止遗漏，此处适当补充针对 auth-submit 的默认样式
 */
.auth-submit {
  width: 100%;
  padding: 12px 20px;
  font-size: 1.1rem;
}
</style>
