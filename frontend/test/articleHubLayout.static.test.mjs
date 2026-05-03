import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import assert from 'node:assert/strict'

const hub = readFileSync(resolve('src/views/ArticleHubView.vue'), 'utf8')
const styles = readFileSync(resolve('src/style.css'), 'utf8')

assert.match(hub, /<section class="filter-panel pet-directory-filter-panel article-directory-filter-panel">/, 'article search should reuse the public directory filter panel')
assert.match(hub, /<section class="article-category-panel"[\s\S]*?<div class="article-type-tabs"/, 'article categories should live in a separate panel below search')
assert.match(hub, /class="pet-filter-row article-filter-row-inline"/, 'article search should use one inline directory filter row on desktop')
assert.match(hub, /class="article-filter-author"/, 'article author input should have a directory filter class')
assert.match(hub, /class="article-filter-title"/, 'article title input should have a directory filter class')
assert.match(hub, /class="article-filter-date full-width-control"/, 'article date range should have a directory filter class')
assert.match(hub, /class="pet-filter-action article-filter-action"/, 'article search button should sit in the directory action slot')
assert.match(hub, /<div class="hub-card-cover article-list-cover">[\s\S]*?<img v-if="article\.cover" :src="article\.cover" :alt="article\.title" loading="lazy" \/>[\s\S]*?<div v-else class="hub-card-cover-placeholder article-list-cover-placeholder">暂无封面<\/div>/, 'article cards should show cover images and a no-cover placeholder')
assert.match(hub, /<article[\s\S]*?class="hub-card article-list-card"[\s\S]*?tabindex="0"[\s\S]*?role="button"[\s\S]*?@click="openArticle\(article\)"[\s\S]*?@keyup\.enter="openArticle\(article\)"/, 'article cards should be clickable and keyboard accessible')
assert.doesNotMatch(hub, /阅读专题|class="card-link"[\s\S]*openArticle\(article\)/, 'article list should not keep a separate read-more button')
assert.doesNotMatch(hub, /article-search-panel|article-public-search|article-search-author|article-search-title|article-search-submit/, 'article search should not keep the old custom search layout')
assert.doesNotMatch(hub, /article-filter-row-primary|article-filter-row-secondary/, 'article public search should not split across two rows on desktop')
assert.ok(
  hub.indexOf('class="article-directory-filter-panel"') < hub.indexOf('class="article-category-panel"'),
  'article search panel should appear before category panel',
)

assert.match(styles, /\.article-type-tabs\s*{[\s\S]*justify-content:\s*center;/, 'article category tabs should be centered')
assert.match(styles, /\.article-category-panel\s*{[\s\S]*margin-top:\s*12px;/, 'article categories should stay in a separate panel below search')
assert.match(styles, /\.article-directory-filter-panel\s*{[\s\S]*grid-template-columns:\s*1fr;/, 'article filter panel should be a one-column public filter panel')
assert.match(styles, /\.article-filter-row-inline\s*{[\s\S]*grid-template-columns:\s*minmax\(140px,\s*0\.8fr\)\s*minmax\(200px,\s*1fr\)\s*minmax\(300px,\s*1\.35fr\)\s*auto;/, 'article search should keep author, title, date and action on one desktop row')
assert.match(styles, /\.article-filter-date\s*{[\s\S]*--el-date-editor-width:\s*100%;[\s\S]*max-width:\s*100%;/, 'article date range should override Element Plus fixed width')
assert.match(styles, /\.article-list-cover\s*{[\s\S]*height:\s*190px;/, 'article list cards should have a stable cover area')
assert.match(styles, /\.article-list-cover-placeholder\s*{[\s\S]*min-height:\s*0;/, 'article list no-cover placeholder should fit the cover area')
assert.match(styles, /\.article-list-card\s*{[\s\S]*cursor:\s*pointer;/, 'clickable article cards should show pointer cursor')
assert.match(styles, /\.article-list-card:hover,[\s\S]*\.article-list-card:focus-visible\s*{[\s\S]*transform:\s*translateY\(-2px\);/, 'clickable article cards should have hover and keyboard focus feedback')
assert.doesNotMatch(styles, /\.article-public-search/, 'article search should not keep old custom grid-area styles')
assert.match(styles, /@media \(max-width:\s*900px\)\s*{[\s\S]*\.article-filter-row-inline,[\s\S]*\.volunteer-filter-row-primary,[\s\S]*\.volunteer-filter-row-secondary,[\s\S]*\.lost-filter-grid-top,[\s\S]*\.lost-filter-grid-bottom\s*{[\s\S]*grid-template-columns:\s*1fr;/, 'article search should use a true single-column layout on narrow screens')
