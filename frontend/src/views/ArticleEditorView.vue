<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { createArticle, getArticleById, updateArticle } from '../api/article'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'
import { useUserStore } from '../stores/user'
import { ROLE, hasRole } from '../utils/roles'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const saving = ref(false)

const ARTICLE_TYPE_OPTIONS = [
  { label: '救助故事', value: 'STORY' },
  { label: '活动推广', value: 'ACTIVITY' },
  { label: '科普知识', value: 'KNOWLEDGE' },
]

const form = reactive({
  id: '',
  type: 'STORY',
  title: '',
  content: '',
})

const loginRole = computed(() => Number(userStore.profile.role || 0))
const isWorker = computed(() => hasRole(loginRole.value, ROLE.ADMIN) || hasRole(loginRole.value, ROLE.WORKER))
const isVolunteer = computed(() => hasRole(loginRole.value, ROLE.VOLUNTEER))
const canEditArticles = computed(() => isWorker.value || isVolunteer.value)
const articleTypeOptions = computed(() => (isWorker.value ? ARTICLE_TYPE_OPTIONS : ARTICLE_TYPE_OPTIONS.filter((item) => item.value === 'STORY')))
const isEditMode = computed(() => Boolean(route.params.id))
const pageTitle = computed(() => (isEditMode.value ? '编辑文章' : '发表文章'))
const pageHint = computed(() => (isWorker.value ? '写救助故事、活动消息或养护知识。' : '先写下你的救助故事。'))

const rules = {
  type: [{ required: true, message: '请选择文章类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入文章正文', trigger: 'blur' }],
}

async function loadArticle() {
  if (!isEditMode.value) {
    return
  }
  loading.value = true
  try {
    const detail = await getArticleById(route.params.id)
    form.id = detail?.id || route.params.id
    form.type = detail?.type || 'STORY'
    form.title = detail?.title || ''
    form.content = detail?.content || ''
  } catch (error) {
    ElMessage.warning(error?.message || '加载文章失败')
    router.replace('/console/articles')
  } finally {
    loading.value = false
  }
}

async function saveArticle(publish = false) {
  if (!formRef.value || saving.value) {
    return
  }
  try {
    await formRef.value.validate()
    saving.value = true
    if (isEditMode.value) {
      await updateArticle(form.id, {
        title: form.title.trim(),
        content: form.content.trim(),
      })
      ElMessage.success('文章已更新')
      router.push('/console/articles')
    } else {
      const result = await createArticle({
        type: isWorker.value ? form.type : 'STORY',
        title: form.title.trim(),
        content: form.content.trim(),
        publish,
      })
      ElMessage.success(publish ? '文章已发布' : '草稿已保存')
      if (publish) {
        router.push(`/articles/${result.id}`)
      } else {
        router.push('/console/articles')
      }
    }
  } catch (error) {
    if (error) {
      ElMessage.warning(error?.message || '保存文章失败')
    }
  } finally {
    saving.value = false
  }
}

function goBack() {
  router.push('/console/articles')
}

onMounted(() => {
  if (!canEditArticles.value) {
    ElMessage.warning('当前账号没有文章发布权限')
    router.replace('/console')
    return
  }
  loadArticle()
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main">
      <section class="article-editor-shell">
        <div class="article-editor-header">
          <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回文章列表</el-button>
          <div class="article-editor-copy">
            <span class="hero-chip">公益文章</span>
            <h1>{{ pageTitle }}</h1>
            <p>{{ pageHint }}</p>
          </div>
        </div>

        <el-card class="article-editor-card" v-loading="loading">
          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="article-editor-form-page">
            <el-form-item label="文章类型" prop="type">
              <el-select v-model="form.type" :disabled="isEditMode" placeholder="请选择文章类型">
                <el-option v-for="item in articleTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>

            <el-form-item label="文章标题" prop="title">
              <el-input v-model="form.title" maxlength="60" show-word-limit placeholder="请输入文章标题" />
            </el-form-item>

            <el-form-item label="文章正文" prop="content">
              <el-input
                v-model="form.content"
                type="textarea"
                :autosize="{ minRows: 14, maxRows: 24 }"
                placeholder="在这里写下救助经过、活动信息或养护知识。"
              />
            </el-form-item>
          </el-form>

          <div class="article-editor-actions">
            <el-button class="soft-btn" @click="goBack">取消</el-button>
            <template v-if="isEditMode">
              <el-button class="soft-btn article-search-btn" :loading="saving" @click="saveArticle(false)">保存</el-button>
            </template>
            <template v-else>
              <el-button class="soft-btn" :loading="saving" @click="saveArticle(false)">保存草稿</el-button>
              <el-button class="soft-btn article-search-btn" :loading="saving" @click="saveArticle(true)">直接发布</el-button>
            </template>
          </div>
        </el-card>
      </section>
    </main>

    <AppFooter />
  </div>
</template>
