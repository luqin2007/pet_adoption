<template>
  <teleport to="body">
    <transition name="viewer-fade">
      <div
        v-if="visible"
        class="protocol-viewer-overlay"
        :class="{ 'protocol-viewer-thumbs-hidden': !showThumbs || !thumbsVisible || items.length <= 1 }"
        @click.self="close"
        @wheel.prevent="handleWheel"
      >
        <button class="protocol-viewer-close" type="button" @click="close">
          <el-icon><Close /></el-icon>
        </button>

        <div class="protocol-viewer-toolbar">
          <el-tooltip content="上一张" placement="bottom">
            <button class="protocol-viewer-tool" type="button" :disabled="items.length <= 1" @click="prev">
              <el-icon><ArrowLeft /></el-icon>
            </button>
          </el-tooltip>
          <el-tooltip content="下一张" placement="bottom">
            <button class="protocol-viewer-tool" type="button" :disabled="items.length <= 1" @click="next">
              <el-icon><ArrowRight /></el-icon>
            </button>
          </el-tooltip>
          <el-tooltip content="放大" placement="bottom">
            <button class="protocol-viewer-tool" type="button" :disabled="!isImage(currentItem)" @click="zoomIn">
              <el-icon><ZoomIn /></el-icon>
            </button>
          </el-tooltip>
          <el-tooltip content="缩小" placement="bottom">
            <button class="protocol-viewer-tool" type="button" :disabled="!isImage(currentItem)" @click="zoomOut">
              <el-icon><ZoomOut /></el-icon>
            </button>
          </el-tooltip>
          <el-tooltip content="顺时针旋转" placement="bottom">
            <button class="protocol-viewer-tool" type="button" :disabled="!isImage(currentItem)" @click="rotate(90)">
              <el-icon><RefreshRight /></el-icon>
            </button>
          </el-tooltip>
          <el-tooltip content="逆时针旋转" placement="bottom">
            <button class="protocol-viewer-tool" type="button" :disabled="!isImage(currentItem)" @click="rotate(-90)">
              <el-icon><RefreshLeft /></el-icon>
            </button>
          </el-tooltip>
          <el-tooltip content="下载" placement="bottom">
            <button class="protocol-viewer-tool" type="button" :disabled="!currentItem?.assetUrl" @click="downloadCurrent">
              <el-icon><Download /></el-icon>
            </button>
          </el-tooltip>
          <el-tooltip v-if="showThumbs && items.length > 1" :content="thumbsVisible ? '隐藏缩略图' : '显示缩略图'" placement="bottom">
            <button class="protocol-viewer-tool" type="button" :disabled="items.length <= 1" @click="toggleThumbs">
              <el-icon><PictureFilled /></el-icon>
            </button>
          </el-tooltip>
        </div>

        <button v-if="items.length > 1" class="protocol-viewer-arrow protocol-viewer-prev" type="button" @click.stop="prev">
          <el-icon><ArrowLeft /></el-icon>
        </button>
        <button v-if="items.length > 1" class="protocol-viewer-arrow protocol-viewer-next" type="button" @click.stop="next">
          <el-icon><ArrowRight /></el-icon>
        </button>

        <div class="protocol-viewer-stage">
          <div class="protocol-viewer-main">
            <template v-if="isImage(currentItem)">
              <img
                :src="currentItem?.assetUrl"
                :alt="currentItem?.name || '预览图片'"
                class="protocol-viewer-image"
                :style="imageStyle"
                draggable="false"
              />
            </template>
            <template v-else>
              <video
                :src="currentItem?.assetUrl"
                class="protocol-viewer-video"
                controls
                autoplay
                playsinline
              />
            </template>
          </div>

          <div v-if="currentItem?.name" class="protocol-viewer-caption">{{ currentItem.name }}</div>
        </div>

        <transition name="thumb-slide">
          <div v-if="showThumbs && thumbsVisible && items.length > 1" class="protocol-viewer-thumbs">
            <button
              v-for="(item, idx) in items"
              :key="item.id || `${item.assetUrl}-${idx}`"
              type="button"
              class="protocol-viewer-thumb"
              :class="{ 'is-active': idx === currentIndex }"
              @click.stop="setIndex(idx)"
            >
              <img v-if="isImage(item)" :src="item.assetUrl" :alt="item.name || `第 ${idx + 1} 张`" loading="lazy" />
              <video v-else :src="item.assetUrl" preload="metadata" />
              <span>{{ item.name || `第 ${idx + 1} 张` }}</span>
            </button>
          </div>
        </transition>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { ArrowLeft, ArrowRight, Close, Download, PictureFilled, RefreshLeft, RefreshRight, ZoomIn, ZoomOut } from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  items: {
    type: Array,
    default: () => [],
  },
  index: {
    type: Number,
    default: 0,
  },
  showThumbs: {
    type: Boolean,
    default: true,
  },
})

const emit = defineEmits(['update:visible', 'update:index'])

const currentIndex = ref(0)
const scale = ref(1)
const rotation = ref(0)
const thumbsVisible = ref(props.showThumbs)

const currentItem = computed(() => props.items[currentIndex.value] || null)
const imageStyle = computed(() => ({
  transform: `translate(-50%, -50%) scale(${scale.value}) rotate(${rotation.value}deg)`,
}))

function isImage(item) {
  if (!item) return false
  return item.type === 'IMAGE' || /\.(png|jpe?g|webp|gif|bmp|avif)(\?|$)/i.test(String(item.assetUrl || ''))
}

function clampScale(value) {
  return Math.max(0.5, Math.min(4, Number(value.toFixed(2))))
}

function resetTransform() {
  scale.value = 1
  rotation.value = 0
}

function close() {
  emit('update:visible', false)
}

function setIndex(value) {
  if (!props.items.length) return
  const max = props.items.length
  const nextIndex = ((value % max) + max) % max
  currentIndex.value = nextIndex
  emit('update:index', nextIndex)
  resetTransform()
}

function prev() {
  if (props.items.length <= 1) return
  setIndex(currentIndex.value - 1)
}

function next() {
  if (props.items.length <= 1) return
  setIndex(currentIndex.value + 1)
}

function zoomIn() {
  if (!isImage(currentItem.value)) return
  scale.value = clampScale(scale.value + 0.2)
}

function zoomOut() {
  if (!isImage(currentItem.value)) return
  scale.value = clampScale(scale.value - 0.2)
}

function rotate(delta) {
  if (!isImage(currentItem.value)) return
  rotation.value = (rotation.value + delta + 360) % 360
}

function downloadCurrent() {
  const url = currentItem.value?.assetUrl
  if (!url) return
  const link = document.createElement('a')
  link.href = url
  link.download = currentItem.value?.name || 'media'
  link.rel = 'noreferrer'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

function toggleThumbs() {
  if (!props.showThumbs) return
  thumbsVisible.value = !thumbsVisible.value
}

function handleWheel(event) {
  if (!isImage(currentItem.value)) return
  if (event.deltaY < 0) zoomIn()
  else zoomOut()
}

function handleKeydown(event) {
  if (!props.visible) return
  if (event.key === 'Escape') {
    event.preventDefault()
    close()
    return
  }
  if (event.key === 'ArrowLeft' || event.key === 'ArrowUp' || event.key === 'PageUp') {
    event.preventDefault()
    prev()
    return
  }
  if (event.key === 'ArrowRight' || event.key === 'ArrowDown' || event.key === 'PageDown') {
    event.preventDefault()
    next()
    return
  }
  if (event.key === '+' || event.key === '=') {
    event.preventDefault()
    zoomIn()
    return
  }
  if (event.key === '-' || event.key === '_') {
    event.preventDefault()
    zoomOut()
    return
  }
  if (event.key === '0') {
    event.preventDefault()
    resetTransform()
    return
  }
  if (event.key === 'r' || event.key === 'R') {
    event.preventDefault()
    rotate(90)
    return
  }
  if (event.key === 'l' || event.key === 'L') {
    event.preventDefault()
    rotate(-90)
  }
}

watch(
  () => props.visible,
  (value) => {
    if (value) {
      currentIndex.value = Math.min(Math.max(Number(props.index || 0), 0), Math.max(props.items.length - 1, 0))
      resetTransform()
      thumbsVisible.value = Boolean(props.showThumbs)
      document.body.style.overflow = 'hidden'
      document.addEventListener('keydown', handleKeydown)
    } else {
      document.body.style.overflow = ''
      document.removeEventListener('keydown', handleKeydown)
    }
  },
  { immediate: true },
)

watch(
  () => props.index,
  (value) => {
    if (!props.visible) return
    currentIndex.value = Math.min(Math.max(Number(value || 0), 0), Math.max(props.items.length - 1, 0))
    resetTransform()
  },
)

onBeforeUnmount(() => {
  document.body.style.overflow = ''
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.protocol-viewer-overlay {
  position: fixed;
  inset: 0;
  z-index: 4000;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 180px;
  grid-template-rows: 1fr auto;
  gap: 12px;
  padding: 18px;
  background: rgba(14, 14, 14, 0.92);
}

.protocol-viewer-close {
  position: absolute;
  top: 18px;
  left: 18px;
  width: 40px;
  height: 40px;
  border: 0;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  color: #fff;
  cursor: pointer;
}

.protocol-viewer-close:hover {
  background: rgba(255, 255, 255, 0.22);
}

.protocol-viewer-toolbar {
  position: absolute;
  top: 18px;
  left: 70px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.protocol-viewer-tool {
  width: 40px;
  height: 40px;
  border: 0;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.14);
  color: #fff;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.protocol-viewer-tool:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.protocol-viewer-tool:not(:disabled):hover {
  background: rgba(255, 255, 255, 0.22);
}

.protocol-viewer-arrow {
  position: absolute;
  top: 50%;
  margin-top: -24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border: none;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 24px;
  cursor: pointer;
}

.protocol-viewer-prev {
  left: 18px;
}

.protocol-viewer-next {
  right: 196px;
}

.protocol-viewer-thumbs-hidden .protocol-viewer-next {
  right: 18px;
}

.protocol-viewer-arrow:hover {
  background: rgba(255, 255, 255, 0.2);
}

.protocol-viewer-stage {
  position: relative;
  grid-column: 1;
  grid-row: 1;
  display: grid;
  place-items: center;
  min-width: 0;
  min-height: 0;
}

.protocol-viewer-main {
  position: relative;
  width: min(100%, 1200px);
  height: min(100%, calc(100vh - 180px));
  display: grid;
  place-items: center;
  overflow: hidden;
}

.protocol-viewer-image {
  position: absolute;
  left: 50%;
  top: 50%;
  max-width: none;
  max-height: none;
  transform-origin: center center;
  user-select: none;
  object-fit: contain;
  cursor: grab;
  filter: drop-shadow(0 24px 60px rgba(0, 0, 0, 0.45));
}

.protocol-viewer-video {
  width: min(100%, 1200px);
  max-height: calc(100vh - 220px);
  background: #000;
  border-radius: 14px;
  object-fit: contain;
}

.protocol-viewer-caption {
  position: absolute;
  left: 50%;
  bottom: 0;
  transform: translateX(-50%);
  max-width: min(90vw, 960px);
  padding: 10px 14px;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  font-size: 14px;
  line-height: 1.6;
}

.protocol-viewer-thumbs {
  grid-column: 2;
  grid-row: 1 / span 2;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 10px;
  align-self: end;
  width: 180px;
  max-height: 100%;
  overflow: auto;
  padding: 0 0 0 4px;
}

.protocol-viewer-thumb {
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  padding: 8px;
  cursor: pointer;
  display: grid;
  gap: 6px;
  text-align: left;
}

.protocol-viewer-thumb.is-active {
  border-color: rgba(255, 255, 255, 0.62);
  background: rgba(255, 255, 255, 0.18);
}

.protocol-viewer-thumb img,
.protocol-viewer-thumb video {
  width: 100%;
  height: 72px;
  border-radius: 8px;
  object-fit: cover;
  background: rgba(255, 255, 255, 0.08);
}

.protocol-viewer-thumb span {
  font-size: 12px;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.protocol-viewer-thumbs-hidden .protocol-viewer-stage {
  grid-column: 1 / span 2;
}
</style>
