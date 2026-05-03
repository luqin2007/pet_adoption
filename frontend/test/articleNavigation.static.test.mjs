import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import assert from 'node:assert/strict'

const featured = readFileSync(resolve('src/components/FeaturedArticlesSection.vue'), 'utf8')
const hub = readFileSync(resolve('src/views/ArticleHubView.vue'), 'utf8')
const router = readFileSync(resolve('src/router/index.js'), 'utf8')

assert.match(featured, /function\s+goToArticle\s*\(\s*article\s*\)/, 'featured articles should navigate to a specific article')
assert.match(featured, /@click="goToArticle\(article\)"/, 'featured Read full article button should pass its article')
assert.match(featured, /`\/articles\/\$\{article\.id\}`/, 'featured article navigation should use /articles/:id')

assert.match(hub, /function\s+openArticle\s*\(\s*article\s*\)/, 'article hub should expose article opening handler')
assert.match(hub, /@click="openArticle\(article\)"/, 'article hub topic button should open the clicked article')

assert.match(router, /path:\s*'\/articles\/:id'/, 'router should include an article detail route')
assert.match(router, /ArticleDetailView/, 'article detail route should render ArticleDetailView')
