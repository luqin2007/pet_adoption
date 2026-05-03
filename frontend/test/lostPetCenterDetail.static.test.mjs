import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import assert from 'node:assert/strict'

const center = readFileSync(resolve('src/views/LostPetCenterView.vue'), 'utf8')
const router = readFileSync(resolve('src/router/index.js'), 'utf8')
const lostApi = readFileSync(resolve('src/api/lost.js'), 'utf8')
const styles = readFileSync(resolve('src/style.css'), 'utf8')
const lostResponse = readFileSync(resolve('../backend/src/main/java/com/example/backend/dto/LostPetResponse.java'), 'utf8')
const lostService = readFileSync(resolve('../backend/src/main/java/com/example/backend/service/LostPetService.java'), 'utf8')
const securityConfig = readFileSync(resolve('../backend/src/main/java/com/example/backend/config/SecurityConfig.java'), 'utf8')
const detailPath = resolve('src/views/LostPetDetailView.vue')

assert.match(center, /import \{ useRouter \} from 'vue-router'/, 'lost center should use router navigation')
assert.match(center, /const router = useRouter\(\)/, 'lost center should create a router instance')
assert.match(
  center,
  /function openLostPetDetail\(id\)[\s\S]*router\.push\(`\/lost\/\$\{id\}`\)/,
  'lost cards should navigate to the lost pet detail page',
)
assert.match(
  center,
  /<article[\s\S]*?class="lost-card"[\s\S]*?tabindex="0"[\s\S]*?role="button"[\s\S]*?@click="openLostPetDetail\(item\.id\)"[\s\S]*?@keyup\.enter="openLostPetDetail\(item\.id\)"/,
  'lost cards should be clickable and keyboard accessible',
)
assert.match(
  center,
  /<img v-if="item\.petCover" :src="item\.petCover" :alt="item\.name" loading="lazy" \/>/,
  'lost cards should show available pet cover images',
)
assert.match(
  center,
  /<div v-else class="lost-cover-placeholder">暂无图片<\/div>/,
  'lost cards should show a no-image placeholder',
)
assert.doesNotMatch(center, /查看线索|lost-actions/, 'lost cards should not keep a separate clue button')

assert.match(
  router,
  /import LostPetDetailView from '..\/views\/LostPetDetailView\.vue'/,
  'router should import the lost pet detail page',
)
assert.match(
  router,
  /path: '\/lost\/:id'[\s\S]*name: 'lost-detail'[\s\S]*component: LostPetDetailView/,
  'router should expose a lost pet detail route',
)
assert.match(lostApi, /export function getLostPet\(id\)/, 'lost API should expose a single-record getter')

assert.ok(existsSync(detailPath), 'lost pet detail view should exist')
const detail = readFileSync(detailPath, 'utf8')
assert.match(detail, /getLostPet\(lostPetId\.value\)/, 'detail page should load the lost pet by route id')
assert.match(detail, /class="[^"]*lost-detail-page/, 'detail page should use a dedicated page class')
assert.match(detail, /record\.petCover/, 'detail page should render the available cover image')
assert.match(detail, /暂无图片/, 'detail page should render a no-image placeholder')
assert.match(detail, /contactPhone/, 'detail page should show contact phone information')

assert.match(styles, /\.lost-card\s*{[\s\S]*cursor:\s*pointer;/, 'clickable lost cards should show pointer cursor')
assert.match(
  styles,
  /\.lost-card:hover,[\s\S]*\.lost-card:focus-visible\s*{[\s\S]*transform:\s*translateY\(-2px\);/,
  'clickable lost cards should have hover and keyboard focus feedback',
)
assert.match(styles, /\.lost-detail-page\s*{/, 'lost detail page should have dedicated styles')

assert.match(
  lostService,
  /fileService\.getCoverUrl\(lostPet\.getId\(\), LOST_PET\)/,
  'lost pet detail responses should fall back to uploaded lost-pet images',
)
assert.match(
  lostService,
  /fileService\.getCoverUrls\(LOST_PET,[\s\S]*lostPetIds/,
  'lost pet list responses should batch-load uploaded lost-pet covers',
)
assert.match(
  lostResponse,
  /String coverUrl[\s\S]*coverUrl,/,
  'lost pet response should accept an already generated cover URL',
)
assert.match(
  securityConfig,
  /"\/api\/v1\/lost\/pets\/\*"/,
  'lost pet detail endpoint should be public for the detail page',
)
