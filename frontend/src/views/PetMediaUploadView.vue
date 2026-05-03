<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Check, Delete, EditPen, PictureFilled, Upload } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { deletePetMedia, getPetById, getPetMedia, updatePetMedia, uploadPetMedia } from '../api/pets'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'
import { useUserStore } from '../stores/user'
import { userCanEditPet } from '../utils/petPermissions'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const fileInputRef = ref(null)
const loading = ref(false)
const uploading = ref(false)
const savingMedia = ref(false)
const pet = ref(null)
const mediaItems = ref([])
const selectedFile = ref(null)
const previewUrl = ref('')
const editDialogVisible = ref(false)

const uploadForm = reactive({
  name: '',
  description: '',
  isCover: false,
})

const editForm = reactive({
  id: '',
  type: '',
  name: '',
  description: '',
  isCover: false,
})

const petId = computed(() => String(route.params.id || ''))
const pageTitle = computed(() => pet.value?.name || '宠物相册')
const canEditCurrentPet = computed(() => userStore.isLoggedIn && userCanEditPet(userStore.profile, pet.value))
const selectedFileIsImage = computed(() => Boolean(selectedFile.value?.type?.startsWith('image/')))
const coverMedia = computed(() => mediaItems.value.find((item) => item.isCover) || null)

function goBack() {
  router.push(petId.value ? `/pets/${petId.value}` : '/pets')
}

function openBasicEditor() {
  router.push(`/pets/${petId.value}/edit`)
}

function chooseFile() {
  if (!canEditCurrentPet.value) {
    ElMessage.warning('当前账号暂无上传权限')
    return
  }
  fileInputRef.value?.click()
}

function isImage(media) {
  return media?.type === 'IMAGE' || media?.assetUrl?.match(/\.(png|jpe?g|webp|gif|bmp|avif)(\?|$)/i)
}

function isSupportedFile(file) {
  return file.type.startsWith('image/') || file.type.startsWith('video/')
}

function getFileBaseName(file) {
  return String(file?.name || '').replace(/\.[^.]+$/, '')
}

function revokePreview() {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
  previewUrl.value = ''
}

function clearSelectedFile() {
  revokePreview()
  selectedFile.value = null
  uploadForm.name = ''
  uploadForm.description = ''
  uploadForm.isCover = false
}

function handleFileChange(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) {
    return
  }
  if (!isSupportedFile(file)) {
    ElMessage.warning('只能上传图片或视频文件')
    return
  }

  clearSelectedFile()
  selectedFile.value = file
  previewUrl.value = URL.createObjectURL(file)
  uploadForm.name = getFileBaseName(file)
  uploadForm.isCover = file.type.startsWith('image/') && !coverMedia.value
}

function formatDate(value) {
  if (!value) {
    return '时间待补充'
  }
  return String(value).slice(0, 10)
}

function validateMediaForm(form, hasFile = true) {
  if (hasFile && !selectedFile.value) {
    ElMessage.warning('请选择要上传的图片或视频')
    return false
  }
  if (!String(form.name || '').trim()) {
    ElMessage.warning('请输入媒体名称')
    return false
  }
  return true
}

async function uploadFile() {
  if (!canEditCurrentPet.value) {
    ElMessage.warning('当前账号暂无上传权限')
    return
  }
  if (uploading.value || !validateMediaForm(uploadForm)) {
    return
  }

  uploading.value = true
  try {
    await uploadPetMedia(petId.value, {
      file: selectedFile.value,
      name: uploadForm.name.trim(),
      description: uploadForm.description.trim(),
      isCover: selectedFileIsImage.value && uploadForm.isCover,
    })
    clearSelectedFile()
    await loadAlbum()
    ElMessage.success('媒体文件已上传')
  } catch (error) {
    ElMessage.warning(error?.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

function openEditMediaDialog(media) {
  editForm.id = String(media.id || '')
  editForm.type = media.type || ''
  editForm.name = media.name || ''
  editForm.description = media.description || ''
  editForm.isCover = Boolean(media.isCover)
  editDialogVisible.value = true
}

async function saveMediaInfo() {
  if (savingMedia.value || !validateMediaForm(editForm, false)) {
    return
  }

  savingMedia.value = true
  try {
    await updatePetMedia(petId.value, editForm.id, {
      name: editForm.name.trim(),
      description: editForm.description.trim(),
      isCover: editForm.type === 'IMAGE' && editForm.isCover,
    })
    editDialogVisible.value = false
    await loadAlbum()
    ElMessage.success('媒体信息已保存')
  } catch (error) {
    ElMessage.warning(error?.message || '保存媒体信息失败')
  } finally {
    savingMedia.value = false
  }
}

async function removeMedia(media) {
  try {
    await ElMessageBox.confirm(`确认删除「${media.name || media.id}」？`, '删除媒体', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deletePetMedia(petId.value, media.id)
    await loadAlbum()
    ElMessage.success('媒体文件已删除')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.warning(error?.message || '删除媒体失败')
    }
  }
}

async function loadAlbum() {
  if (!petId.value) {
    pet.value = null
    mediaItems.value = []
    return
  }

  loading.value = true
  try {
    const [petResult, mediaResult] = await Promise.all([getPetById(petId.value), getPetMedia(petId.value)])
    pet.value = petResult
    mediaItems.value = Array.isArray(mediaResult) ? mediaResult : []
  } catch {
    pet.value = null
    mediaItems.value = []
  } finally {
    loading.value = false
  }
}

watch(
  () => petId.value,
  () => {
    clearSelectedFile()
    loadAlbum()
  },
)

onMounted(() => {
  loadAlbum()
})

onBeforeUnmount(() => {
  clearSelectedFile()
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main action-form-page" v-loading="loading">
      <section v-if="pet" class="action-form-panel pet-media-upload-panel">
        <div class="pet-media-upload-head">
          <div class="pet-media-upload-cover">
            <img v-if="pet.cover" :src="pet.cover" :alt="pet.name" />
            <el-icon v-else><PictureFilled /></el-icon>
          </div>
          <div>
            <span>宠物相册</span>
            <h1>{{ pageTitle }}</h1>
          </div>
          <div class="pet-album-head-actions">
            <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回档案</el-button>
          </div>
        </div>

        <section v-if="canEditCurrentPet" class="pet-album-upload-box">
          <input ref="fileInputRef" class="profile-avatar-input" type="file" accept="image/*,video/*" @change="handleFileChange" />
          <div class="pet-media-dropzone" role="button" tabindex="0" @click="chooseFile" @keydown.enter="chooseFile" @keydown.space.prevent="chooseFile">
            <template v-if="previewUrl">
              <img v-if="selectedFileIsImage" :src="previewUrl" :alt="uploadForm.name" />
              <video v-else :src="previewUrl" muted />
            </template>
            <template v-else>
              <el-icon><Upload /></el-icon>
              <strong>选择图片/视频</strong>
              <span>添加名称、说明，也可设为封面</span>
            </template>
          </div>

          <el-form label-position="top" class="pet-album-upload-form">
            <el-form-item label="媒体名称">
              <el-input v-model="uploadForm.name" placeholder="请输入媒体名称" clearable />
            </el-form-item>
            <el-form-item label="媒体说明">
              <el-input
                v-model="uploadForm.description"
                type="textarea"
                :rows="3"
                placeholder="补充拍摄位置、状态或备注"
                clearable
              />
            </el-form-item>
            <el-form-item v-if="selectedFileIsImage" label="封面设置">
              <el-checkbox v-model="uploadForm.isCover">设为封面</el-checkbox>
            </el-form-item>
          </el-form>

          <div class="action-form-actions">
            <el-button class="soft-btn" :disabled="!selectedFile" @click="clearSelectedFile">清空</el-button>
            <el-button class="warm-btn" :icon="Upload" :disabled="!selectedFile" :loading="uploading" @click="uploadFile">上传</el-button>
          </div>
        </section>

        <div v-if="mediaItems.length" class="pet-album-grid">
          <article v-for="item in mediaItems" :key="item.id" class="pet-album-card">
            <div class="pet-album-card-media">
              <img v-if="isImage(item)" :src="item.assetUrl" :alt="item.name" loading="lazy" />
              <video v-else :src="item.assetUrl" controls preload="metadata" />
              <span v-if="item.isCover" class="pet-album-cover-badge">封面</span>
            </div>
            <div class="pet-album-card-body">
              <div>
                <strong>{{ item.name || '未命名媒体' }}</strong>
                <span>{{ item.type === 'IMAGE' ? '图片' : '视频' }} · {{ formatDate(item.createDate) }}</span>
              </div>
              <p v-if="item.description">{{ item.description }}</p>
              <div v-if="canEditCurrentPet" class="pet-album-card-actions">
                <el-button text type="warning" :icon="EditPen" @click="openEditMediaDialog(item)">编辑</el-button>
                <el-button text type="danger" :icon="Delete" @click="removeMedia(item)">删除</el-button>
              </div>
            </div>
          </article>
        </div>

        <el-empty v-else description="暂未上传图片或视频" />
      </section>

      <section v-else-if="!loading" class="pet-profile-empty">
        <el-empty description="未找到对应的宠物档案">
          <el-button class="warm-btn" :icon="Check" @click="router.push('/pets')">返回领养大厅</el-button>
        </el-empty>
      </section>

      <el-dialog v-model="editDialogVisible" title="编辑媒体信息" width="520px">
        <el-form label-position="top">
          <el-form-item label="媒体名称">
            <el-input v-model="editForm.name" placeholder="请输入媒体名称" clearable />
          </el-form-item>
          <el-form-item label="媒体说明">
            <el-input
              v-model="editForm.description"
              type="textarea"
              :rows="3"
              placeholder="补充拍摄位置、状态或备注"
              clearable
            />
          </el-form-item>
          <el-form-item v-if="editForm.type === 'IMAGE'" label="封面设置">
            <el-checkbox v-model="editForm.isCover">设为封面</el-checkbox>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="warning" :loading="savingMedia" @click="saveMediaInfo">保存</el-button>
        </template>
      </el-dialog>
    </main>

    <AppFooter />
  </div>
</template>
