<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Connection, RefreshRight, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  callOpenApiOperation,
  OPENAPI_OPERATION_COUNT,
  OPENAPI_OPERATIONS,
} from '../api/openapi'

const router = useRouter()
const selectedId = ref(OPENAPI_OPERATIONS[0]?.id || '')
const activeTag = ref('全部')
const methodFilter = ref('全部')
const keyword = ref('')
const submitting = ref(false)
const responseText = ref('')
const pathValues = reactive({})
const queryValues = reactive({})
const formValues = reactive({})
const files = reactive({})
const bodyText = ref('')

const tagOptions = computed(() => {
  const tags = new Set()
  OPENAPI_OPERATIONS.forEach((operation) => {
    tags.add(operation.tags?.[0] || '未分组')
  })
  return ['全部', ...Array.from(tags).sort((a, b) => a.localeCompare(b))]
})

const methodOptions = ['全部', 'GET', 'POST', 'PUT', 'PATCH', 'DELETE']

const filteredOperations = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return OPENAPI_OPERATIONS.filter((operation) => {
    const tag = operation.tags?.[0] || '未分组'
    const matchedTag = activeTag.value === '全部' || tag === activeTag.value
    const matchedMethod = methodFilter.value === '全部' || operation.method === methodFilter.value
    const haystack = `${operation.method} ${operation.path} ${operation.summary} ${tag}`.toLowerCase()
    const matchedText = !text || haystack.includes(text)
    return matchedTag && matchedMethod && matchedText
  })
})

const selectedOperation = computed(() => {
  return OPENAPI_OPERATIONS.find((operation) => operation.id === selectedId.value) || filteredOperations.value[0]
})

const operationStats = computed(() => {
  const stats = methodOptions
    .filter((method) => method !== '全部')
    .map((method) => ({
      method,
      count: OPENAPI_OPERATIONS.filter((operation) => operation.method === method).length,
    }))
  return stats
})

function resetObject(target) {
  Object.keys(target).forEach((key) => {
    delete target[key]
  })
}

function resetForm(operation) {
  resetObject(pathValues)
  resetObject(queryValues)
  resetObject(formValues)
  resetObject(files)
  responseText.value = ''

  if (!operation) {
    bodyText.value = ''
    return
  }

  operation.pathParams.forEach((name) => {
    pathValues[name] = ''
  })
  operation.queryParams.forEach((item) => {
    queryValues[item.name] = ''
  })
  operation.formFields.forEach((item) => {
    formValues[item.name] = ''
    files[item.name] = []
  })
  bodyText.value = operation.bodyExample ? JSON.stringify(operation.bodyExample, null, 2) : ''
}

function selectOperation(operation) {
  selectedId.value = operation.id
}

function onFileChange(name, event) {
  files[name] = event.target.files
}

async function submitOperation() {
  if (!selectedOperation.value || submitting.value) {
    return
  }

  submitting.value = true
  try {
    const result = await callOpenApiOperation(selectedOperation.value, {
      pathValues,
      queryValues,
      formValues,
      files,
      bodyText: bodyText.value,
    })
    responseText.value = JSON.stringify(result, null, 2)
    ElMessage.success('接口调用成功')
  } catch (error) {
    responseText.value = JSON.stringify(
      {
        message: error?.message || '接口调用失败',
        code: error?.code,
        status: error?.status,
      },
      null,
      2,
    )
    ElMessage.warning(error?.message || '接口调用失败')
  } finally {
    submitting.value = false
  }
}

function goConsole() {
  router.push('/console')
}

watch(
  selectedOperation,
  (operation) => {
    resetForm(operation)
  },
  { immediate: true },
)

watch(filteredOperations, (operations) => {
  if (!operations.some((operation) => operation.id === selectedId.value) && operations[0]) {
    selectedId.value = operations[0].id
  }
})
</script>

<template>
  <div class="api-coverage-page">
    <header class="console-topbar">
      <div>
        <h1>OpenAPI 接口覆盖台</h1>
        <p>{{ OPENAPI_OPERATION_COUNT }} 个后端操作已接入前端调用目录</p>
      </div>
      <el-button text type="warning" @click="goConsole">
        <el-icon><ArrowLeft /></el-icon>
        返回后台
      </el-button>
    </header>

    <section class="api-coverage-stats">
      <article v-for="item in operationStats" :key="item.method">
        <span>{{ item.method }}</span>
        <strong>{{ item.count }}</strong>
      </article>
    </section>

    <section class="api-coverage-layout">
      <aside class="api-operation-list">
        <div class="api-filter-bar">
          <el-select v-model="activeTag" placeholder="模块">
            <el-option v-for="tag in tagOptions" :key="tag" :label="tag" :value="tag" />
          </el-select>
          <el-select v-model="methodFilter" placeholder="方法">
            <el-option v-for="method in methodOptions" :key="method" :label="method" :value="method" />
          </el-select>
          <el-input v-model="keyword" clearable placeholder="搜索接口">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="api-list-scroll">
          <button
            v-for="operation in filteredOperations"
            :key="operation.id"
            class="api-operation-item"
            :class="{ active: selectedOperation?.id === operation.id }"
            type="button"
            @click="selectOperation(operation)"
          >
            <span :data-method="operation.method">{{ operation.method }}</span>
            <strong>{{ operation.path }}</strong>
            <small>{{ operation.summary }}</small>
          </button>
        </div>
      </aside>

      <main v-if="selectedOperation" class="api-operation-panel">
        <div class="api-panel-head">
          <div>
            <el-tag effect="dark" type="warning">{{ selectedOperation.method }}</el-tag>
            <h2>{{ selectedOperation.summary }}</h2>
            <code>{{ selectedOperation.path }}</code>
          </div>
          <el-button :icon="RefreshRight" @click="resetForm(selectedOperation)">重置参数</el-button>
        </div>

        <section class="api-param-grid">
          <div v-if="selectedOperation.pathParams.length" class="api-param-section">
            <h3>路径参数</h3>
            <el-input
              v-for="name in selectedOperation.pathParams"
              :key="name"
              v-model="pathValues[name]"
              :placeholder="name"
            >
              <template #prepend>{{ name }}</template>
            </el-input>
          </div>

          <div v-if="selectedOperation.queryParams.length" class="api-param-section">
            <h3>查询参数</h3>
            <el-input
              v-for="param in selectedOperation.queryParams"
              :key="param.name"
              v-model="queryValues[param.name]"
              :placeholder="param.required ? '必填' : param.type"
            >
              <template #prepend>{{ param.name }}</template>
            </el-input>
          </div>

          <div v-if="selectedOperation.contentType === 'application/json'" class="api-param-section api-body-section">
            <h3>JSON 请求体</h3>
            <el-input v-model="bodyText" type="textarea" :autosize="{ minRows: 10, maxRows: 18 }" />
          </div>

          <div v-if="selectedOperation.contentType === 'multipart/form-data'" class="api-param-section api-body-section">
            <h3>表单请求体</h3>
            <label v-for="field in selectedOperation.formFields" :key="field.name" class="api-file-row">
              <span>{{ field.name }}</span>
              <input
                v-if="field.format === 'binary'"
                type="file"
                :multiple="field.type === 'array'"
                @change="onFileChange(field.name, $event)"
              />
              <el-input v-else v-model="formValues[field.name]" :placeholder="field.required ? '必填' : field.type" />
            </label>
          </div>
        </section>

        <div class="api-submit-row">
          <el-button class="warm-btn" :icon="Connection" :loading="submitting" @click="submitOperation">
            调用接口
          </el-button>
        </div>

        <section class="api-response-panel">
          <h3>响应数据</h3>
          <pre>{{ responseText || '{}' }}</pre>
        </section>
      </main>
    </section>
  </div>
</template>
