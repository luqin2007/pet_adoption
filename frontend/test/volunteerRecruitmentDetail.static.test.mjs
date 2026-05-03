import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import assert from 'node:assert/strict'

const viewPath = resolve('src/views/VolunteerRecruitmentDetailView.vue')
const source = readFileSync(viewPath, 'utf8')

assert.match(source, /function\s+goLogin\s*\(/, 'volunteer recruitment detail should expose a login navigation handler')
assert.match(source, /class="volunteer-apply-shell"/, 'application form should be wrapped by an overlay shell')
assert.match(source, /class="volunteer-login-overlay"/, 'logged-out state should render a blocking login overlay')
assert.match(source, /@click="goLogin"/, 'login overlay should include a login button')
assert.doesNotMatch(source, /<el-alert\s+v-if="!userStore\.isLoggedIn"/, 'logged-out message should not be a normal alert above the form')
assert.match(source, /age:\s*\[\{\s*required:\s*true,\s*message:\s*'请输入年龄'/, 'age should be required in form rules')
assert.match(source, /<el-form-item\s+label="年龄"\s+prop="age">/, 'age form item should bind validation prop')
assert.match(source, /:disabled="!userStore\.isLoggedIn \|\| isExpired"/, 'submit button should be disabled when logged out')
