import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import assert from 'node:assert/strict'

const source = readFileSync(resolve('src/views/VolunteerCenterView.vue'), 'utf8')
const styles = readFileSync(resolve('src/style.css'), 'utf8')

assert.match(source, /<section class="directory-hero volunteer-directory-hero">/, 'volunteer center should use directory hero styling')
assert.match(source, /<span class="hero-chip">志愿者中心<\/span>/, 'volunteer center hero should use the same chip pattern as the adoption hall')
assert.match(source, /class="filter-panel pet-directory-filter-panel volunteer-directory-filter-panel"/, 'volunteer search should reuse the public directory filter panel')
assert.match(source, /class="pet-filter-row volunteer-filter-row-primary"/, 'volunteer search should use the first-row directory filter layout')
assert.match(source, /class="pet-filter-row volunteer-filter-row-secondary"/, 'volunteer search should use the second-row directory filter layout')
assert.doesNotMatch(source, /console-filter-form|console-filter-grid|el-form-item/, 'volunteer public search should not use console management form layout')

assert.match(styles, /\.volunteer-directory-filter-panel\s*{[\s\S]*grid-template-columns:\s*1fr;/, 'volunteer filter panel should be a one-column public filter panel')
assert.match(styles, /\.volunteer-filter-row-primary\s*{[\s\S]*grid-template-columns:\s*minmax\(220px,\s*2fr\)\s*repeat\(3,\s*minmax\(0,\s*1fr\)\);/, 'volunteer primary search row should follow the adoption hall grid style')
assert.match(styles, /\.volunteer-filter-row-secondary\s*{[\s\S]*grid-template-columns:\s*minmax\(280px,\s*1fr\)\s*auto;/, 'volunteer secondary search row should keep date range and action separated')
