<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllConfig, batchSetConfig, deleteCachedFeatures } from '../api/services'
import { useUserStore } from '../stores/user'
import { ROLE, hasRole } from '../utils/roles'
import { computed } from 'vue'

const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const clearingCache = ref(false)
const testing = ref(false)

const canManageUsers = computed(() => hasRole(Number(userStore.profile?.role || 0), ROLE.ADMIN) || hasRole(Number(userStore.profile?.role || 0), ROLE.WORKER))

const form = reactive({
  'ai.enabled': 'false',
  'ai.endpoint': '',
  'ai.model': 'gpt-4o-mini',
  'ai.key': '',
  'ai.multimodal': 'false',
  'ai.daily_token_limit': '1000000',
})

async function loadConfig() {
  loading.value = true
  try {
    const config = await getAllConfig()
    if (config) {
      Object.keys(form).forEach((key) => {
        if (config[key] !== undefined) form[key] = config[key]
      })
    }
  } catch (e) {
    ElMessage.warning(e?.message || '加载配置失败')
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    await batchSetConfig({ ...form })
    ElMessage.success('配置已保存')
  } catch (e) {
    ElMessage.warning(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function handleClearCache() {
  try {
    await ElMessageBox.confirm('确定要清除所有缓存吗？此操作将清除所有已缓存的特征数据，下次匹配将重新调用 AI 接口。', '确认清除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }

  clearingCache.value = true
  try {
    await deleteCachedFeatures()
    ElMessage.success('缓存已清除')
  } catch (e) {
    ElMessage.warning(e?.message || '清除缓存失败')
  } finally {
    clearingCache.value = false
  }
}

// 1x1 透明 PNG，用于多模态测试
const TEST_IMAGE_B64 = 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=='

async function handleTestConnection() {
  const endpoint = form['ai.endpoint']?.trim()
  const key = form['ai.key']?.trim()
  const model = form['ai.model']?.trim()

  if (!endpoint) return ElMessage.warning('请先填写 API 接入点')
  if (!key) return ElMessage.warning('请先填写 API Key')
  if (!model) return ElMessage.warning('请先填写模型名称')

  testing.value = true
  const baseUrl = endpoint.replace(/\/+$/, '')
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${key}`,
  }

  try {
    // 1) 文本能力测试
    const textRes = await fetch(`${baseUrl}/chat/completions`, {
      method: 'POST',
      headers,
      body: JSON.stringify({
        model,
        messages: [{ role: 'user', content: 'Reply with exactly: OK' }],
        max_tokens: 10,
      }),
    })

    if (!textRes.ok) {
      const errBody = await textRes.text().catch(() => '')
      throw new Error(`文本测试失败 (HTTP ${textRes.status})${errBody ? ': ' + errBody.slice(0, 200) : ''}`)
    }

    const textData = await textRes.json()
    const textReply = textData?.choices?.[0]?.message?.content?.trim() || ''
    ElMessage.success(`文本能力正常 — 模型回复: "${textReply.slice(0, 50)}"`)

    // 2) 多模态能力测试（仅在启用多模态时执行）
    if (form['ai.multimodal'] === 'true') {
      const mmRes = await fetch(`${baseUrl}/chat/completions`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
          model,
          messages: [{
            role: 'user',
            content: [
              { type: 'text', text: 'Describe this image in one word.' },
              { type: 'image_url', image_url: { url: `data:image/png;base64,${TEST_IMAGE_B64}` } },
            ],
          }],
          max_tokens: 20,
        }),
      })

      if (!mmRes.ok) {
        const errBody = await mmRes.text().catch(() => '')
        ElMessage.warning(`多模态测试失败 (HTTP ${mmRes.status})${errBody ? ': ' + errBody.slice(0, 200) : ''}`)
      } else {
        const mmData = await mmRes.json()
        const mmReply = mmData?.choices?.[0]?.message?.content?.trim() || ''
        ElMessage.success(`多模态能力正常 — 模型回复: "${mmReply.slice(0, 50)}"`)
      }
    }
  } catch (e) {
    ElMessage.error(e?.message || '连接测试失败，请检查配置')
  } finally {
    testing.value = false
  }
}

onMounted(loadConfig)
</script>

<template>
  <el-card class="profile-card" v-loading="loading">
    <template #header>
      <div class="profile-card-header">
        <strong>系统设置</strong>
      </div>
    </template>

    <el-form label-width="180px" class="ai-config-form">
      <el-form-item label="AI筛选开关">
        <el-switch v-model="form['ai.enabled']" active-value="true" inactive-value="false" />
      </el-form-item>

      <el-divider />

      <el-form-item label="API 接入点" required>
        <el-input v-model="form['ai.endpoint']" placeholder="https://api.openai.com/v1" />
      </el-form-item>

      <el-form-item label="模型名称" required>
        <el-input v-model="form['ai.model']" placeholder="gpt-4o-mini" />
      </el-form-item>

      <el-form-item label="API Key" required>
        <el-input v-model="form['ai.key']" type="password" show-password placeholder="sk-..." />
      </el-form-item>

      <el-form-item label="多模态分析">
        <el-switch v-model="form['ai.multimodal']" active-value="true" inactive-value="false" />
        <span class="form-help">启用后上传宠物图片参与特征分析</span>
      </el-form-item>

      <el-form-item label="每日 Token 上限">
        <el-input-number v-model="form['ai.daily_token_limit']" :min="10000" :step="100000" :max="999999999" />
      </el-form-item>

      <el-divider />

      <el-form-item>
        <el-button :loading="testing" @click="handleTestConnection">连接测试</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存配置</el-button>
        <el-button v-if="canManageUsers" :loading="clearingCache" @click="handleClearCache">清除缓存</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style scoped>
.ai-config-form {
  max-width: 640px;
}
.form-help {
  margin-left: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
