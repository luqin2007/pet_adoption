<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllConfig, batchSetConfig } from '../api/services'

const loading = ref(false)
const saving = ref(false)

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

onMounted(loadConfig)
</script>

<template>
  <el-card class="profile-card" v-loading="loading">
    <template #header>
      <div class="profile-card-header">
        <strong>AI 匹配设置</strong>
      </div>
    </template>

    <el-form label-width="180px" class="ai-config-form">
      <el-form-item label="全局开关">
        <el-switch v-model="form['ai.enabled']" active-value="true" inactive-value="false" />
        <span class="form-help">关闭后所有 AI 匹配接口均不可用</span>
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
