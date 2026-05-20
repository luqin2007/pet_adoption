<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllConfig, batchSetConfig, deleteCachedFeatures } from '../api/services'
import { useUserStore } from '../stores/user'
import { ROLE, hasRole } from '../utils/roles'

const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const clearingCache = ref(false)

const canManageUsers = computed(() => hasRole(Number(userStore.profile?.role || 0), ROLE.ADMIN) || hasRole(Number(userStore.profile?.role || 0), ROLE.WORKER))

import { computed } from 'vue'

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
    await ElMessageBox.confirm('确定要清空所有 AI 缓存吗？此操作将清除所有已缓存的特征数据，下次匹配将重新调用 AI 接口。', '确认清空', {
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
    ElMessage.success('AI 缓存已清空')
  } catch (e) {
    ElMessage.warning(e?.message || '清空缓存失败')
  } finally {
    clearingCache.value = false
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
      <el-form-item label="全局开关">
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
        <el-button type="primary" :loading="saving" @click="handleSave">保存配置</el-button>
      </el-form-item>
    </el-form>
  </el-card>

  <el-card v-if="canManageUsers" class="profile-card" style="margin-top:16px">
    <template #header>
      <div class="profile-card-header">
        <strong>系统维护</strong>
      </div>
    </template>
    <div class="profile-actions">
      <el-button :loading="clearingCache" @click="handleClearCache">清空 AI 缓存</el-button>
    </div>
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
.profile-actions {
  display: flex;
  gap: 12px;
}
</style>
