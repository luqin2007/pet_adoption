import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import assert from 'node:assert/strict'

const navigationPath = resolve('src/constants/navigation.js')
const source = readFileSync(navigationPath, 'utf8')

const articleIndex = source.indexOf("id: 'articles'")
const volunteerIndex = source.indexOf("id: 'volunteers'")

assert.ok(articleIndex >= 0, 'navigation should include 公益文章')
assert.ok(volunteerIndex >= 0, 'navigation should include 志愿者中心')
assert.ok(volunteerIndex < articleIndex, '志愿者中心 should appear before 公益文章')
