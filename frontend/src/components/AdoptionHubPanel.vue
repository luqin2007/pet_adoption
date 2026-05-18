<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>领养寄养</strong>
        <div class="profile-actions">
          <el-button-group class="console-btn-group">
            <el-button v-if="activeTabName === 'breading'" class="warm-btn" :icon="Plus" @click="router.push('/breading/new')" />
            <el-button class="warm-btn" :icon="RefreshRight" :loading="activeTabLoading" @click="handleActiveTabRefresh" />
          </el-button-group>
        </div>
      </div>
    </template>

    <el-tabs v-model="activeTabName" class="adoption-hub-tabs">
      <el-tab-pane label="领养" name="adopt">
        <section class="pet-admin-section">
          <el-table :data="adoptFilteredRows" v-loading="adoptLoading" class="user-admin-table">
            <el-table-column min-width="180" show-overflow-tooltip>
              <template #header>
                <TableFilterHeader label="宠物" :filter="adoptFilters.petName" type="text" :active="adoptIsActive('petName')" />
              </template>
              <template #default="{ row }">
                <button class="table-primary-link adoption-pet-cell" type="button" @click="goAdoptDetail(row)">
                  <el-avatar :size="34" :src="row.petCover">
                    {{ (row.petName || '宠').slice(0, 1) }}
                  </el-avatar>
                  <span>{{ row.petName || '未命名宠物' }}</span>
                </button>
              </template>
            </el-table-column>
            <el-table-column label="申请人" min-width="160">
              <template #header>
                <TableFilterHeader label="申请人" :filter="adoptFilters.applicantName" type="text" :active="adoptIsActive('applicantName')" />
              </template>
              <template #default="{ row }">
                <div class="adoption-person-cell">
                  <strong>{{ row.applicantName || '未命名用户' }}</strong>
                  <span>{{ row.applicantPhone || '' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column width="120">
              <template #header>
                <TableFilterHeader label="状态" :filter="adoptFilters.status" type="enum" :active="adoptIsActive('status')" :options="adoptStatusFilterOptions" />
              </template>
              <template #default="{ row }">
                <el-tag :type="adoptStatusTagType(row.status)" effect="plain">{{ adoptStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="审核人" min-width="120">
              <template #default="{ row }">{{ row.reviewerName || '' }}</template>
            </el-table-column>
            <el-table-column min-width="160">
              <template #header>
                <TableFilterHeader label="申请时间" :filter="adoptFilters.createTime" type="time" :active="adoptIsActive('createTime')" />
              </template>
              <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
            </el-table-column>
            <el-table-column width="40" class-name="action-col">
              <template #header>
                <TableActionColumnHeader title="操作" :collapsed="adoptActionCollapsed" @toggle="adoptActionCollapsed = !adoptActionCollapsed" />
              </template>
              <template #default="{ row }">
                <div class="table-action-cell">
                  <div class="table-action-panel" :class="{ 'is-collapsed': adoptActionCollapsed }">
                    <el-button v-if="adoptCanReview(row)" text type="primary" @click="openAdoptReviewDialog(row)">审核</el-button>
                    <el-button v-if="adoptCanCancel(row)" text type="danger" @click="adoptCancel(row)">取消</el-button>
                    <el-button v-if="adoptCanCreateAgreement(row)" text type="success" @click="openAdoptAgreementChoice(row)">协议</el-button>
                    <el-button v-if="adoptCanCreateFollowTask(row)" text type="warning" :loading="adoptFollowLoadingId === String(row.id)" @click="goAdoptFollowTask(row)">回访</el-button>
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <div class="user-admin-pagination">
            <el-pagination
              layout="prev, pager, next, total"
              :current-page="adoptPage.page"
              :page-size="adoptPage.size"
              :total="adoptTotal"
              @current-change="adoptChangePage"
            />
          </div>
        </section>
      </el-tab-pane>

      <el-tab-pane label="寄养" name="breading">
        <section class="pet-admin-section">
          <el-table :data="breadingFilteredRows" v-loading="breadingLoading" class="user-admin-table">
            <el-table-column label="宠物" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                <button class="table-primary-link adoption-pet-detail-cell" type="button" @click="goBreadingDetail(row)">
                  <div class="adoption-person-cell">
                    <strong>{{ row.petName || '未命名宠物' }}</strong>
                    <span>{{ [row.petType, row.petBreed, row.petAge != null ? `${row.petAge} 月` : ''].filter(Boolean).join(' · ') }}</span>
                  </div>
                </button>
              </template>
            </el-table-column>
            <el-table-column label="申请人" min-width="160">
              <template #header>
                <TableFilterHeader label="申请人" :filter="breadingFilters.applicantName" type="text" :active="breadingIsActive('applicantName')" />
              </template>
              <template #default="{ row }">
                <div class="adoption-person-cell">
                  <strong>{{ row.applicantName || '未命名用户' }}</strong>
                  <span>{{ row.applicantPhone || '' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column width="120">
              <template #header>
                <TableFilterHeader label="状态" :filter="breadingFilters.status" type="enum" :active="breadingIsActive('status')" :options="breadingStatusFilterOptions" />
              </template>
              <template #default="{ row }">
                <el-tag :type="breadingStatusTagType(row.status)" effect="plain">{{ breadingStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="审核人" min-width="120">
              <template #default="{ row }">{{ row.reviewerName || '' }}</template>
            </el-table-column>
            <el-table-column min-width="160">
              <template #header>
                <TableFilterHeader label="申请时间" :filter="breadingFilters.createTime" type="time" :active="breadingIsActive('createTime')" />
              </template>
              <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
            </el-table-column>
            <el-table-column width="40" class-name="action-col">
              <template #header>
                <TableActionColumnHeader title="操作" :collapsed="breadingActionCollapsed" @toggle="breadingActionCollapsed = !breadingActionCollapsed" />
              </template>
              <template #default="{ row }">
                <div class="table-action-cell">
                  <div class="table-action-panel" :class="{ 'is-collapsed': breadingActionCollapsed }">
                    <el-button v-if="breadingCanReview(row)" text type="primary" @click="openBreadingReviewDialog(row)">审核</el-button>
                    <el-button v-if="breadingCanCancel(row)" text type="danger" @click="breadingCancel(row)">取消</el-button>
                    <el-button v-if="breadingCanCreateAgreement(row)" text type="success" @click="openBreadingAgreementChoice(row)">协议</el-button>
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <div class="user-admin-pagination">
            <el-pagination
              layout="prev, pager, next, total"
              :current-page="breadingPage.page"
              :page-size="breadingPage.size"
              :total="breadingTotal"
              @current-change="breadingChangePage"
            />
          </div>
        </section>
      </el-tab-pane>

      <el-tab-pane label="回访" name="follow">
        <section class="pet-admin-section">
          <el-table :data="followFilteredTasks" v-loading="followLoading" class="user-admin-table">
            <el-table-column min-width="180" show-overflow-tooltip>
              <template #header>
                <TableFilterHeader label="宠物" :filter="followFilters.petName" type="text" :active="followIsActive('petName')" />
              </template>
              <template #default="{ row }">
                <button class="table-primary-link adoption-pet-cell" type="button" @click="goFollowTaskDetail(row)">
                  <div class="adoption-person-cell">
                    <strong>{{ row.petName || '未命名宠物' }}</strong>
                    <span>#{{ row.id }} · {{ followStatusText(row.status) }}</span>
                  </div>
                </button>
              </template>
            </el-table-column>
            <el-table-column label="领养人" min-width="160">
              <template #header>
                <TableFilterHeader label="领养人" :filter="followFilters.applicantName" type="text" :active="followIsActive('applicantName')" />
              </template>
              <template #default="{ row }">
                <div class="adoption-person-cell">
                  <strong>{{ row.applicantName || '未命名申请人' }}</strong>
                  <span>#{{ row.applicantId || '—' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column min-width="160">
              <template #header>
                <TableFilterHeader label="计划时间" :filter="followFilters.planTime" type="time" :active="followIsActive('planTime')" />
              </template>
              <template #default="{ row }">{{ formatDate(row.planTime) }}</template>
            </el-table-column>
            <el-table-column width="120">
              <template #header>
                <TableFilterHeader label="状态" :filter="followFilters.status" type="enum" :active="followIsActive('status')" :options="followStatusFilterOptions" />
              </template>
              <template #default="{ row }">
                <el-tag :type="followStatusTagType(row.status)" effect="plain">{{ followStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="志愿者" min-width="160">
              <template #default="{ row }">{{ row.volunteerName || '未命名志愿者' }}</template>
            </el-table-column>
            <el-table-column label="记录摘要" min-width="220" show-overflow-tooltip>
              <template #default="{ row }">{{ row.summary || '' }}</template>
            </el-table-column>
            <el-table-column width="40" class-name="action-col">
              <template #header>
                <TableActionColumnHeader title="操作" :collapsed="followActionCollapsed" @toggle="followActionCollapsed = !followActionCollapsed" />
              </template>
              <template #default="{ row }">
                <div class="table-action-cell">
                  <div class="table-action-panel" :class="{ 'is-collapsed': followActionCollapsed }">
                    <el-button v-if="followCanApprove(row)" text type="success" :loading="followActionLoadingId === followActionKey(row, 'approve')" @click="followSubmitAction(row, 'approve')">同意</el-button>
                    <el-button v-if="followCanReject(row)" text type="danger" :loading="followActionLoadingId === followActionKey(row, 'reject')" @click="followSubmitAction(row, 'reject')">拒绝</el-button>
                    <el-button v-if="followCanRevoke(row)" text type="warning" :loading="followActionLoadingId === followActionKey(row, 'revoke')" @click="followSubmitAction(row, 'revoke')">撤销</el-button>
                    <el-button v-if="followCanModify(row)" text type="primary" @click="openFollowModifyDialog(row)">修改</el-button>
                    <el-button v-if="followCanExecute(row)" text type="success" :loading="followActionLoadingId === followActionKey(row, 'execute')" @click="followSubmitAction(row, 'execute')">执行</el-button>
                    <el-button v-if="followCanFinish(row)" text type="success" :loading="followActionLoadingId === followActionKey(row, 'finish')" @click="followSubmitAction(row, 'finish')">完成</el-button>
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <div class="user-admin-pagination">
            <el-pagination
              layout="prev, pager, next, total"
              :current-page="followPage.page"
              :page-size="followPage.size"
              :total="followTotal"
              @current-change="followChangePage"
            />
          </div>
        </section>
      </el-tab-pane>

      <el-tab-pane label="协议" name="agreement">
        <section class="pet-admin-section">
          <el-table :data="agreementFilteredRows" v-loading="agreementLoading" class="user-admin-table">
            <el-table-column min-width="180" show-overflow-tooltip>
              <template #header>
                <TableFilterHeader label="宠物" :filter="agreementFilters.petName" type="text" :active="agreementIsActive('petName')" />
              </template>
              <template #default="{ row }">
                <button class="table-primary-link" type="button" @click="openAgreementDetailDialog(row)">
                  {{ row.petName || agreementParentTypeText(row.parentType) }}
                </button>
              </template>
            </el-table-column>
            <el-table-column label="服务" width="110">
              <template #default="{ row }">{{ agreementParentTypeText(row.parentType) }}</template>
            </el-table-column>
            <el-table-column label="类型" width="110">
              <template #default="{ row }">{{ agreementTypeText(row) }}</template>
            </el-table-column>
            <el-table-column width="110">
              <template #header>
                <TableFilterHeader label="签署" :filter="agreementFilters.signed" type="enum" :active="agreementIsActive('signed')" :options="agreementSignedOptions" />
              </template>
              <template #default="{ row }">
                <el-tag :type="row.signTime ? 'success' : 'warning'" effect="plain">{{ row.signTime ? '已签署' : '未签署' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column min-width="160">
              <template #header>
                <TableFilterHeader label="创建时间" :filter="agreementFilters.createTime" type="time" :active="agreementIsActive('createTime')" />
              </template>
              <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="更新时间" min-width="160">
              <template #default="{ row }">{{ formatDate(row.updateTime) }}</template>
            </el-table-column>
            <el-table-column width="40" class-name="action-col">
              <template #header>
                <TableActionColumnHeader title="操作" :collapsed="agreementActionCollapsed" @toggle="agreementActionCollapsed = !agreementActionCollapsed" />
              </template>
              <template #default="{ row }">
                <div class="table-action-cell">
                  <div class="table-action-panel" :class="{ 'is-collapsed': agreementActionCollapsed }">
                    <el-button text type="primary" :icon="Edit" @click="openAgreementDetailDialog(row)">{{ row.signTime ? '查看' : '修改' }}</el-button>
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <div class="user-admin-pagination">
            <el-pagination
              layout="prev, pager, next, total"
              :current-page="agreementPage.page"
              :page-size="agreementPage.size"
              :total="agreementTotal"
              @current-change="agreementChangePage"
            />
          </div>
        </section>
      </el-tab-pane>
    </el-tabs>
  </el-card>

  <!-- Adoption review dialog -->
  <el-dialog v-model="adoptReviewDialogVisible" title="审核领养申请" width="520px" :close-on-click-modal="false">
    <section v-if="adoptReviewTarget" class="adoption-review-summary">
      <strong>{{ adoptReviewTarget.petName || '未命名宠物' }}</strong>
      <span>{{ adoptReviewTarget.applicantName || '申请人' }} · {{ adoptReviewTarget.applicantPhone || '' }}</span>
    </section>
    <el-form label-position="top">
      <el-form-item label="审核结果">
        <el-radio-group v-model="adoptReviewStatus">
          <el-radio-button label="PASS">通过</el-radio-button>
          <el-radio-button label="REJECT">拒绝</el-radio-button>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="adoptReviewDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" :loading="adoptActing" @click="submitAdoptReview">保存</el-button>
    </template>
  </el-dialog>

  <!-- Adoption agreement choice dialog -->
  <el-dialog v-model="adoptAgreementDialogVisible" title="选择协议类型" width="520px" :close-on-click-modal="false">
    <section v-if="adoptAgreementTarget" class="adoption-review-summary">
      <strong>{{ adoptAgreementTarget.petName || '未命名宠物' }}</strong>
      <span>{{ adoptAgreementTarget.applicantName || '申请人' }} · 领养协议</span>
    </section>
    <el-radio-group v-model="adoptAgreementType" class="agreement-type-grid">
      <el-radio-button label="ELECTRONIC">
        <span class="agreement-type-card">
          <strong>电子协议</strong>
          <small>在线起草协议正文</small>
        </span>
      </el-radio-button>
      <el-radio-button label="PAPER">
        <span class="agreement-type-card">
          <strong>纸质协议</strong>
          <small>上传扫描图片并排序</small>
        </span>
      </el-radio-button>
    </el-radio-group>
    <template #footer>
      <el-button @click="adoptAgreementDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" @click="goAdoptAgreementDraft">继续</el-button>
    </template>
  </el-dialog>

  <!-- Breading review dialog -->
  <el-dialog v-model="breadingReviewDialogVisible" title="审核寄养申请" width="520px" :close-on-click-modal="false">
    <section v-if="breadingReviewTarget" class="adoption-review-summary">
      <strong>{{ breadingReviewTarget.petName || '未命名宠物' }}</strong>
      <span>{{ breadingReviewTarget.applicantName || '申请人' }} · {{ breadingReviewTarget.applicantPhone || '' }}</span>
    </section>
    <el-form label-position="top">
      <el-form-item label="审核结果">
        <el-radio-group v-model="breadingReviewStatus">
          <el-radio-button label="PASS">通过</el-radio-button>
          <el-radio-button label="REJECT">拒绝</el-radio-button>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="breadingReviewDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" :loading="breadingActing" @click="submitBreadingReview">保存</el-button>
    </template>
  </el-dialog>

  <!-- Breading agreement choice dialog -->
  <el-dialog v-model="breadingAgreementDialogVisible" title="选择协议类型" width="520px" :close-on-click-modal="false">
    <section v-if="breadingAgreementTarget" class="adoption-review-summary">
      <strong>{{ breadingAgreementTarget.petName || '未命名宠物' }}</strong>
      <span>{{ breadingAgreementTarget.applicantName || '申请人' }} · 寄养协议</span>
    </section>
    <el-radio-group v-model="breadingAgreementType" class="agreement-type-grid">
      <el-radio-button label="ELECTRONIC">
        <span class="agreement-type-card">
          <strong>电子协议</strong>
          <small>在线起草协议正文</small>
        </span>
      </el-radio-button>
      <el-radio-button label="PAPER">
        <span class="agreement-type-card">
          <strong>纸质协议</strong>
          <small>上传扫描图片并排序</small>
        </span>
      </el-radio-button>
    </el-radio-group>
    <template #footer>
      <el-button @click="breadingAgreementDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" @click="goBreadingAgreementDraft">继续</el-button>
    </template>
  </el-dialog>

  <!-- Follow modify dialog -->
  <el-dialog v-model="followModifyDialogVisible" title="修改回访任务" width="560px" :close-on-click-modal="false">
    <el-form label-position="top" class="follow-task-modify-form">
      <el-form-item label="志愿者">
        <el-select
          v-model="followModifyForm.volunteerId"
          filterable
          class="full-width-control"
          placeholder="选择志愿者"
          :loading="followVolunteerLoading"
        >
          <el-option
            v-for="item in followVolunteerOptions"
            :key="followVolunteerValue(item)"
            :label="followVolunteerLabel(item)"
            :value="followVolunteerValue(item)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="计划时间">
        <el-date-picker
          v-model="followModifyForm.planTime"
          type="datetime"
          value-format="YYYY-MM-DDTHH:mm:ss.SSS"
          placeholder="选择计划时间"
          class="full-width-control"
        />
      </el-form-item>
      <el-form-item label="备注">
        <el-input
          v-model="followModifyForm.remark"
          type="textarea"
          :autosize="{ minRows: 3, maxRows: 5 }"
          placeholder="填写调整说明"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="followModifyDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" :loading="followActionLoadingId === 'modify'" @click="submitFollowModify">保存</el-button>
    </template>
  </el-dialog>

  <!-- Agreement detail dialog -->
  <el-dialog v-model="agreementDetailDialogVisible" :title="agreementActiveTitle" width="860px" :close-on-click-modal="false">
    <section v-if="agreementActiveRow" class="agreement-editor" v-loading="agreementDetailLoading">
      <div class="agreement-editor-meta">
        <el-tag effect="plain">{{ agreementParentTypeText(agreementActiveRow.parentType) }}</el-tag>
        <el-tag :type="agreementIsActiveSigned ? 'success' : 'warning'" effect="plain">{{ agreementIsActiveSigned ? '已签署' : '未签署' }}</el-tag>
        <span>{{ formatDate(agreementActiveRow.updateTime) }}</span>
      </div>

      <section v-if="agreementActiveType === 'ELECTRONIC'" class="agreement-editor-section">
        <el-form label-position="top">
          <el-form-item label="电子协议正文">
            <el-input
              v-model="agreementEditContent"
              type="textarea"
              :autosize="{ minRows: 12, maxRows: 20 }"
              placeholder="填写电子协议正文"
              :disabled="agreementIsActiveSigned"
            />
          </el-form-item>
        </el-form>
      </section>

      <section v-else class="agreement-editor-section">
        <div class="agreement-editor-toolbar">
          <input ref="agreementFileInputRef" class="profile-avatar-input" type="file" accept="image/*" @change="uploadAgreementPaperFile" />
          <strong>纸质扫描件</strong>
          <el-button v-if="!agreementIsActiveSigned" class="soft-btn" :icon="Upload" :loading="agreementUploading" @click="chooseAgreementFile">添加扫描件</el-button>
        </div>

        <div v-if="agreementPaperFiles.length" class="agreement-file-list">
          <article v-for="(file, index) in agreementPaperFiles" :key="file.id" class="agreement-file-card">
            <a :href="file.assetUrl" target="_blank" rel="noreferrer">
              <img :src="file.assetUrl" :alt="`协议第 ${index + 1} 页`" />
            </a>
            <div class="agreement-file-card-body">
              <strong>第 {{ index + 1 }} 页</strong>
              <div class="agreement-file-actions">
                <el-button v-if="!agreementIsActiveSigned" text :icon="ArrowUp" :disabled="index === 0" @click="agreementMoveFile(index, -1)">上移</el-button>
                <el-button v-if="!agreementIsActiveSigned" text :icon="ArrowDown" :disabled="index === agreementPaperFiles.length - 1" @click="agreementMoveFile(index, 1)">下移</el-button>
                <el-button v-if="!agreementIsActiveSigned" text type="danger" :icon="Delete" @click="agreementRemoveFile(file)">删除</el-button>
              </div>
            </div>
          </article>
        </div>
        <el-empty v-else description="暂无协议扫描件" />
      </section>

      <section v-if="agreementActiveRow.sign" class="agreement-editor-section">
        <div class="agreement-editor-toolbar">
          <strong>签名</strong>
        </div>
        <a :href="agreementActiveRow.sign" target="_blank" rel="noreferrer" class="agreement-sign-preview">
          <img :src="agreementActiveRow.sign" alt="协议签名" />
        </a>
      </section>
    </section>

    <template #footer>
      <el-button @click="agreementDetailDialogVisible = false">关闭</el-button>
      <el-button v-if="agreementActiveType === 'PAPER' && !agreementIsActiveSigned" class="soft-btn" :loading="agreementSavingOrder" :disabled="!agreementHasOrderChanged" @click="saveAgreementPaperOrder">保存排序</el-button>
      <el-button v-if="agreementActiveType === 'ELECTRONIC' && !agreementIsActiveSigned" class="warm-btn" :loading="agreementSaving" @click="saveAgreementElectronic">保存协议</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, ArrowUp, Delete, Edit, Plus, RefreshRight, Upload } from '@element-plus/icons-vue'
import {
  deleteAgreementFile,
  getAdoptApplication,
  getAdoptApplications,
  getAgreement,
  getAgreements,
  getBreadingApplications,
  getFollowTasks,
  reorderAgreementFiles,
  updateAdoptStatus,
  updateAgreement,
  updateBreadingStatus,
  updateFollowTask,
  uploadAgreement,
} from '../api/services'
import { getVolunteerProfiles } from '../api/volunteer'
import { useUserStore } from '../stores/user'
import { ROLE, hasRole } from '../utils/roles'
import TableActionColumnHeader from './TableActionColumnHeader.vue'
import TableFilterHeader from './TableFilterHeader.vue'
import { useTableFilters } from '../composables/useTableFilters'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// ── Common ──────────────────────────────────────────────
const activeTabName = ref('adopt')
const loginRole = computed(() => Number(userStore.profile?.role || 0))
const loginUserId = computed(() => String(userStore.profile?.id || ''))
const isWorker = computed(() => hasRole(loginRole.value, ROLE.WORKER) || hasRole(loginRole.value, ROLE.ADMIN))
const isAdmin = computed(() => hasRole(loginRole.value, ROLE.ADMIN))
const isVolunteerRole = computed(() => hasRole(loginRole.value, ROLE.VOLUNTEER))
const canManageUsers = computed(() => isAdmin.value || hasRole(loginRole.value, ROLE.WORKER))

const statusOptions = [
  { label: '已提交', value: 'CREATE' },
  { label: '审核通过', value: 'PASS' },
  { label: '审核拒绝', value: 'REJECT' },
  { label: '协议草拟中', value: 'AGREEMENT_DRAFT' },
  { label: '待确认', value: 'AGREEMENT_PENDING_CONFIRM' },
  { label: '协议已签署', value: 'AGREEMENT_SIGNED' },
  { label: '回访中', value: 'TRACKING' },
  { label: '流程完成', value: 'FINISH' },
  { label: '已取消', value: 'CANCEL' },
]
const statusMap = Object.fromEntries(statusOptions.map((item) => [item.value, item.label]))

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function statusTagType(value) {
  if (value === 'PASS' || value === 'AGREEMENT_SIGNED' || value === 'FINISH') return 'success'
  if (value === 'REJECT' || value === 'CANCEL') return 'info'
  if (value === 'AGREEMENT_DRAFT' || value === 'TRACKING') return 'primary'
  if (value === 'AGREEMENT_PENDING_CONFIRM') return 'warning'
  return 'warning'
}

const activeTabLoading = computed(() => {
  const map = { adopt: adoptLoading, breading: breadingLoading, follow: followLoading, agreement: agreementLoading }
  return (map[activeTabName.value] || ref(false)).value
})

function handleActiveTabRefresh() {
  const map = {
    adopt: handleAdoptRefresh,
    breading: handleBreadingRefresh,
    follow: handleFollowRefresh,
    agreement: handleAgreementRefresh,
  }
  map[activeTabName.value]?.()
}

// ── Adoption tab ────────────────────────────────────────
const adoptLoading = ref(false)
const adoptActing = ref(false)
const adoptActionCollapsed = ref(false)
const adoptRows = ref([])
const adoptTotal = ref(0)
const adoptReviewDialogVisible = ref(false)
const adoptReviewTarget = ref(null)
const adoptReviewStatus = ref('PASS')
const adoptAgreementDialogVisible = ref(false)
const adoptAgreementTarget = ref(null)
const adoptAgreementType = ref('ELECTRONIC')
const adoptFollowLoadingId = ref('')
const adoptPage = reactive({ page: 1, size: 10 })

const { filters: adoptFilters, isActive: adoptIsActive, applyFilter: adoptApplyFilter } = useTableFilters({
  applicantName: { type: 'text' },
  petName: { type: 'text' },
  status: { type: 'enum' },
  createTime: { type: 'time' },
})

const adoptStatusFilterOptions = [
  { value: 'CREATE', label: '已创建' },
  { value: 'PASS', label: '已通过' },
  { value: 'REJECT', label: '已拒绝' },
  { value: 'FINISH', label: '已完成' },
  { value: 'CANCEL', label: '已取消' },
]

const adoptFilteredRows = computed(() => adoptApplyFilter(adoptRows.value || []))

function adoptStatusText(value) { return statusMap[value] || value || '' }
function adoptStatusTagType(value) { return statusTagType(value) }

function adoptBuildQuery() {
  return {
    page: adoptPage.page,
    size: adoptPage.size,
    sort: 'create_time',
    order: 'desc',
    user: isWorker.value ? undefined : [loginUserId.value],
  }
}

async function loadAdoptRows() {
  if (!isWorker.value && !loginUserId.value) return
  adoptLoading.value = true
  try {
    const result = await getAdoptApplications(adoptBuildQuery())
    adoptRows.value = Array.isArray(result?.records) ? result.records : []
    adoptTotal.value = Number(result?.total || adoptRows.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载领养申请失败')
  } finally {
    adoptLoading.value = false
  }
}

function handleAdoptRefresh() { adoptPage.page = 1; loadAdoptRows() }
function adoptChangePage(value) { adoptPage.page = value; loadAdoptRows() }

function goAdoptDetail(row) {
  if (row?.id) {
    router.push({ name: 'console-adoption-adopt-detail', params: { id: String(row.id) } })
  }
}

function adoptCanReview(row) { return isWorker.value && row?.status === 'CREATE' }
function adoptCanCancel(row) {
  if (!row || row.status === 'FINISH' || row.status === 'CANCEL') return false
  return isWorker.value || String(row.applicantId || '') === loginUserId.value
}
function adoptCanCreateAgreement(row) { return isWorker.value && row?.status === 'PASS' }
function adoptCanCreateFollowTask(row) { return isWorker.value && ['AGREEMENT_SIGNED', 'TRACKING'].includes(row?.status) }

function openAdoptReviewDialog(row) {
  adoptReviewTarget.value = row
  adoptReviewStatus.value = 'PASS'
  adoptReviewDialogVisible.value = true
}

async function submitAdoptReview() {
  if (!adoptReviewTarget.value || adoptActing.value) return
  adoptActing.value = true
  try {
    await updateAdoptStatus(adoptReviewTarget.value.id, adoptReviewStatus.value)
    ElMessage.success(adoptReviewStatus.value === 'PASS' ? '领养申请已通过' : '领养申请已拒绝')
    adoptReviewDialogVisible.value = false
    await loadAdoptRows()
  } catch (error) {
    ElMessage.warning(error?.message || '审核失败')
  } finally {
    adoptActing.value = false
  }
}

async function adoptCancel(row) {
  try {
    await ElMessageBox.confirm(`确认取消「${row.petName || '未命名宠物'}」的领养申请？`, '取消领养申请', {
      type: 'warning', confirmButtonText: '确认取消', cancelButtonText: '返回',
    })
    await updateAdoptStatus(row.id, 'CANCEL')
    ElMessage.success('领养申请已取消')
    await loadAdoptRows()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.warning(error?.message || '取消失败')
  }
}

function openAdoptAgreementChoice(row) {
  adoptAgreementTarget.value = row
  adoptAgreementType.value = 'ELECTRONIC'
  adoptAgreementDialogVisible.value = true
}

async function goAdoptFollowTask(row) {
  if (!row?.id || adoptFollowLoadingId.value) return
  adoptFollowLoadingId.value = String(row.id)
  try {
    const detail = await getAdoptApplication(row.id)
    const ongoing = Array.isArray(detail?.followTasks)
      ? [...detail.followTasks]
          .filter((item) => item.status !== 'FINISH')
          .sort((a, b) => new Date(b?.updateTime || b?.createTime || 0) - new Date(a?.updateTime || a?.createTime || 0))
      : []
    if (ongoing.length) {
      router.push({ name: 'console-adoption-follow-task-detail', params: { id: String(ongoing[0].id) } })
      return
    }
    router.push({
      name: 'console-adoption-follow-create',
      params: { id: String(row.id) },
      query: { pet: detail?.petName || row.petName || '', applicant: detail?.applicantName || row.applicantName || '' },
    })
  } catch (error) {
    ElMessage.warning(error?.message || '获取回访任务失败')
  } finally {
    adoptFollowLoadingId.value = ''
  }
}

function goAdoptAgreementDraft() {
  if (!adoptAgreementTarget.value) return
  const path = adoptAgreementType.value === 'PAPER'
    ? '/console/adoption/agreements/new-paper'
    : '/console/adoption/agreements/new-electronic'
  router.push({
    path,
    query: {
      parentId: adoptAgreementTarget.value.id, parentType: 'ADOPT',
      pet: adoptAgreementTarget.value.petName || '', applicant: adoptAgreementTarget.value.applicantName || '',
    },
  })
}

// ── Breading tab ────────────────────────────────────────
const breadingLoading = ref(false)
const breadingActing = ref(false)
const breadingActionCollapsed = ref(false)
const breadingRows = ref([])
const breadingTotal = ref(0)
const breadingReviewDialogVisible = ref(false)
const breadingReviewTarget = ref(null)
const breadingReviewStatus = ref('PASS')
const breadingAgreementDialogVisible = ref(false)
const breadingAgreementTarget = ref(null)
const breadingAgreementType = ref('ELECTRONIC')
const breadingPage = reactive({ page: 1, size: 10 })

const { filters: breadingFilters, isActive: breadingIsActive, applyFilter: breadingApplyFilter } = useTableFilters({
  applicantName: { type: 'text' },
  status: { type: 'enum' },
  createTime: { type: 'time' },
})

const breadingStatusFilterOptions = [
  { value: 'CREATE', label: '已创建' },
  { value: 'PASS', label: '已通过' },
  { value: 'REJECT', label: '已拒绝' },
  { value: 'FINISH', label: '已完成' },
  { value: 'CANCEL', label: '已取消' },
]

const breadingFilteredRows = computed(() => breadingApplyFilter(breadingRows.value || []))
function breadingStatusText(value) { return statusMap[value] || value || '' }
function breadingStatusTagType(value) { return statusTagType(value) }

function breadingBuildQuery() {
  return {
    page: breadingPage.page, size: breadingPage.size, sort: 'create_time', order: 'desc',
    applicant: isWorker.value ? undefined : [loginUserId.value],
  }
}

async function loadBreadingRows() {
  if (!isWorker.value && !loginUserId.value) return
  breadingLoading.value = true
  try {
    const result = await getBreadingApplications(breadingBuildQuery())
    breadingRows.value = Array.isArray(result?.records) ? result.records : []
    breadingTotal.value = Number(result?.total || breadingRows.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载寄养申请失败')
  } finally {
    breadingLoading.value = false
  }
}

function handleBreadingRefresh() { breadingPage.page = 1; loadBreadingRows() }
function breadingChangePage(value) { breadingPage.page = value; loadBreadingRows() }

function goBreadingDetail(row) {
  if (!row?.id) return
  router.push({ name: 'console-adoption-breading-detail', params: { id: String(row.id) } })
}

function breadingCanReview(row) { return isWorker.value && row?.status === 'CREATE' }
function breadingCanCancel(row) {
  if (!row || row.status === 'FINISH' || row.status === 'CANCEL') return false
  return isWorker.value || String(row.applicantId || '') === loginUserId.value
}
function breadingCanCreateAgreement(row) { return isWorker.value && row?.status === 'PASS' }

function openBreadingReviewDialog(row) {
  breadingReviewTarget.value = row; breadingReviewStatus.value = 'PASS'; breadingReviewDialogVisible.value = true
}

async function submitBreadingReview() {
  if (!breadingReviewTarget.value || breadingActing.value) return
  breadingActing.value = true
  try {
    await updateBreadingStatus(breadingReviewTarget.value.id, breadingReviewStatus.value)
    ElMessage.success(breadingReviewStatus.value === 'PASS' ? '寄养申请已通过' : '寄养申请已拒绝')
    breadingReviewDialogVisible.value = false
    await loadBreadingRows()
  } catch (error) {
    ElMessage.warning(error?.message || '审核失败')
  } finally {
    breadingActing.value = false
  }
}

async function breadingCancel(row) {
  try {
    await ElMessageBox.confirm(`确认取消「${row.petName || '未命名宠物'}」的寄养申请？`, '取消寄养申请', {
      type: 'warning', confirmButtonText: '确认取消', cancelButtonText: '返回',
    })
    await updateBreadingStatus(row.id, 'CANCEL')
    ElMessage.success('寄养申请已取消')
    await loadBreadingRows()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.warning(error?.message || '取消失败')
  }
}

function openBreadingAgreementChoice(row) {
  breadingAgreementTarget.value = row; breadingAgreementType.value = 'ELECTRONIC'; breadingAgreementDialogVisible.value = true
}

function goBreadingAgreementDraft() {
  if (!breadingAgreementTarget.value) return
  const path = breadingAgreementType.value === 'PAPER'
    ? '/console/adoption/agreements/new-paper'
    : '/console/adoption/agreements/new-electronic'
  router.push({
    path,
    query: {
      parentId: breadingAgreementTarget.value.id, parentType: 'BREADING',
      pet: breadingAgreementTarget.value.petName || '', applicant: breadingAgreementTarget.value.applicantName || '',
    },
  })
}

// ── Follow tab ──────────────────────────────────────────
const followLoading = ref(false)
const followVolunteerLoading = ref(false)
const followActionCollapsed = ref(false)
const followActionLoadingId = ref('')
const followModifyDialogVisible = ref(false)
const followModifyTarget = ref(null)
const followVolunteerOptions = ref([])
const followRows = ref([])
const followTotal = ref(0)
const followPage = reactive({ page: 1, size: 10 })
const followModifyForm = reactive({ volunteerId: '', planTime: '', remark: '' })

const { filters: followFilters, isActive: followIsActive, applyFilter: followApplyFilter } = useTableFilters({
  petName: { type: 'text' },
  applicantName: { type: 'text' },
  status: { type: 'enum' },
  planTime: { type: 'time' },
})

const followStatusFilterOptions = [
  { value: 'CREATE', label: '已创建' },
  { value: 'NOTIFIED', label: '已通知' },
  { value: 'IN_PROGRESS', label: '进行中' },
  { value: 'DELAY', label: '已延迟' },
  { value: 'FINISH', label: '已完成' },
]

const followFilteredTasks = computed(() => followApplyFilter(followRows.value || []))

function followStatusText(value) {
  const map = { CREATE: '刚创建', NOTIFIED: '已通知', IN_PROGRESS: '执行中', DELAY: '推迟', FINISH: '已完成' }
  return map[value] || value || ''
}

function followStatusTagType(value) {
  if (value === 'FINISH') return 'success'
  if (value === 'DELAY') return 'info'
  if (value === 'IN_PROGRESS' || value === 'NOTIFIED') return 'primary'
  return 'warning'
}

function followIsNotified(row) { return row?.status === 'NOTIFIED' }
function followIsTaskVolunteer(row) { return String(row?.volunteerId || '') === loginUserId.value }
function followIsTaskWorker(row) { return String(row?.workerId || '') === loginUserId.value }
function followIsApplicant(row) { return String(row?.applicantId || '') === loginUserId.value }

function followCanApprove(row) { return followIsNotified(row) && isVolunteerRole.value && followIsTaskVolunteer(row) }
function followCanReject(row) { return followIsNotified(row) && ((isVolunteerRole.value && followIsTaskVolunteer(row)) || followIsApplicant(row)) }
function followCanRevoke(row) { return followIsNotified(row) && (isAdmin.value || (isWorker.value && followIsTaskVolunteer(row))) }
function followCanModify(row) { return followIsNotified(row) && (isAdmin.value || (isWorker.value && followIsTaskWorker(row))) }
function followCanExecute(row) { return followIsNotified(row) && isVolunteerRole.value && followIsTaskVolunteer(row) }
function followCanFinish(row) { return row?.status === 'IN_PROGRESS' && hasRole(loginRole.value, ROLE.WORKER) && followIsTaskWorker(row) }

function followActionKey(row, action) { return `${row?.id || ''}:${action}` }

function followBuildUpdatePayload(row, status, overrides = {}) {
  return {
    workerId: overrides.workerId ?? row.workerId,
    volunteerId: overrides.volunteerId ?? row.volunteerId,
    planTime: overrides.planTime ?? row.planTime,
    status,
    remark: overrides.remark ?? row.remark ?? undefined,
  }
}

async function followSubmitAction(row, action) {
  if (!row?.id || followActionLoadingId.value) return
  const statusMap = { approve: 'IN_PROGRESS', reject: 'DELAY', revoke: 'CREATE', execute: 'IN_PROGRESS', finish: 'FINISH' }
  const messageMap = {
    approve: '确认同意这项回访任务？', reject: '确认拒绝这项回访任务？', revoke: '确认撤销这项回访任务通知？',
    execute: '确认开始执行这项回访任务？', finish: '确认完成这项回访任务？',
  }
  const successMap = { approve: '已同意回访任务', reject: '已拒绝回访任务', revoke: '已撤销回访任务通知', execute: '回访任务已进入执行中', finish: '回访任务已完成' }
  try {
    await ElMessageBox.confirm(messageMap[action], '回访任务', {
      type: action === 'reject' ? 'warning' : 'info', confirmButtonText: '确认', cancelButtonText: '取消',
    })
    followActionLoadingId.value = followActionKey(row, action)
    await updateFollowTask(row.id, followBuildUpdatePayload(row, statusMap[action]))
    ElMessage.success(successMap[action])
    await loadFollowRows()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.warning(error?.message || '操作回访任务失败')
  } finally {
    followActionLoadingId.value = ''
  }
}

function followVolunteerName(item) { return item?.realName || item?.username || '未命名志愿者' }
function followVolunteerLabel(item) { const phone = item?.phone ? ` · ${item.phone}` : ''; return `${followVolunteerName(item)}${phone}` }
function followVolunteerValue(item) { return String(item?.userId || item?.id || '') }

async function loadFollowVolunteerOptions() {
  followVolunteerLoading.value = true
  try {
    const result = await getVolunteerProfiles({ size: 100, status: ['ACTIVE'] })
    followVolunteerOptions.value = Array.isArray(result?.records) ? result.records : []
  } catch (error) {
    followVolunteerOptions.value = []
    ElMessage.warning(error?.message || '加载志愿者列表失败')
  } finally {
    followVolunteerLoading.value = false
  }
}

async function openFollowModifyDialog(row) {
  followModifyTarget.value = row
  followModifyForm.volunteerId = String(row?.volunteerId || '')
  followModifyForm.planTime = row?.planTime || ''
  followModifyForm.remark = row?.remark || ''
  followModifyDialogVisible.value = true
  if (!followVolunteerOptions.value.length) await loadFollowVolunteerOptions()
}

async function submitFollowModify() {
  if (!followModifyTarget.value || followActionLoadingId.value) return
  if (!followModifyForm.volunteerId) { ElMessage.warning('请选择志愿者'); return }
  if (!followModifyForm.planTime) { ElMessage.warning('请选择计划时间'); return }
  followActionLoadingId.value = 'modify'
  try {
    await updateFollowTask(followModifyTarget.value.id, followBuildUpdatePayload(followModifyTarget.value, 'NOTIFIED', {
      volunteerId: followModifyForm.volunteerId, planTime: followModifyForm.planTime, remark: followModifyForm.remark.trim() || undefined,
    }))
    ElMessage.success('回访任务已修改并重新通知')
    followModifyDialogVisible.value = false
    await loadFollowRows()
  } catch (error) {
    ElMessage.warning(error?.message || '修改回访任务失败')
  } finally {
    followActionLoadingId.value = ''
  }
}

async function loadFollowRows() {
  followLoading.value = true
  try {
    const result = await getFollowTasks({ page: followPage.page, size: followPage.size, sort: 'plan_time', order: 'desc' })
    followRows.value = Array.isArray(result?.records) ? result.records : []
    followTotal.value = Number(result?.total || followRows.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载回访任务失败')
  } finally {
    followLoading.value = false
  }
}

function handleFollowRefresh() { followPage.page = 1; loadFollowRows() }
function followChangePage(value) { followPage.page = value; loadFollowRows() }

function goFollowTaskDetail(row) {
  if (!row?.id) return
  router.push({ name: 'console-adoption-follow-task-detail', params: { id: String(row.id) } })
}

// ── Agreement tab ───────────────────────────────────────
const agreementLoading = ref(false)
const agreementDetailLoading = ref(false)
const agreementSaving = ref(false)
const agreementSavingOrder = ref(false)
const agreementUploading = ref(false)
const agreementRows = ref([])
const agreementTotal = ref(0)
const agreementActionCollapsed = ref(false)
const agreementDetailDialogVisible = ref(false)
const agreementActiveRow = ref(null)
const agreementEditContent = ref('')
const agreementOriginalPaperOrder = ref([])
const agreementFileInputRef = ref(null)
const agreementPage = reactive({ page: 1, size: 10 })

const { filters: agreementFilters, isActive: agreementIsActive, applyFilter: agreementApplyFilter } = useTableFilters({
  petName: { type: 'text' },
  signed: { type: 'enum' },
  createTime: { type: 'time' },
})

const agreementSignedOptions = [
  { value: true, label: '已签署' },
  { value: false, label: '未签署' },
]

const agreementFilteredRows = computed(() => agreementApplyFilter(agreementRows.value || []))

const agreementActiveType = computed(() => inferAgreementType(agreementActiveRow.value))
const agreementActiveTitle = computed(() => agreementActiveRow.value
  ? `${agreementActiveRow.value.petName || agreementParentTypeText(agreementActiveRow.value.parentType)} · ${agreementParentTypeText(agreementActiveRow.value.parentType)}`
  : '协议')
const agreementIsActiveSigned = computed(() => Boolean(agreementActiveRow.value?.signTime))
const agreementPaperFiles = computed(() => {
  const files = Array.isArray(agreementActiveRow.value?.files) ? agreementActiveRow.value.files : []
  return files.filter((file) => Number(file.page || 0) > 0).slice().sort((a, b) => Number(a.page || 0) - Number(b.page || 0))
})
const agreementHasOrderChanged = computed(() => {
  const current = agreementPaperFiles.value.map((file) => String(file.id))
  return current.join('|') !== agreementOriginalPaperOrder.value.join('|')
})

function agreementParentTypeText(value) {
  if (value === 'ADOPT') return '领养'
  if (value === 'BREADING') return '寄养'
  return value || ''
}

function inferAgreementType(value) {
  if (!value) return ''
  return value.type || (value.content ? 'ELECTRONIC' : 'PAPER')
}

function agreementTypeText(row) {
  return inferAgreementType(row) === 'PAPER' ? '纸质协议' : '电子协议'
}

function agreementBuildQuery() {
  return { page: agreementPage.page, size: agreementPage.size, sort: 'create_time', order: 'desc' }
}

async function loadAgreementRows() {
  agreementLoading.value = true
  try {
    const result = await getAgreements(agreementBuildQuery())
    agreementRows.value = Array.isArray(result?.records) ? result.records : []
    agreementTotal.value = Number(result?.total || agreementRows.value.length)
    const focusId = String(route.query.focus || '')
    if (focusId) {
      const target = agreementRows.value.find((row) => String(row.id) === focusId)
      if (target) openAgreementDetailDialog(target)
    }
  } catch (error) {
    ElMessage.warning(error?.message || '加载协议失败')
  } finally {
    agreementLoading.value = false
  }
}

function handleAgreementRefresh() { agreementPage.page = 1; loadAgreementRows() }
function agreementChangePage(value) { agreementPage.page = value; loadAgreementRows() }

function syncAgreementPaperOrder() {
  agreementOriginalPaperOrder.value = agreementPaperFiles.value.map((file) => String(file.id))
}

async function openAgreementDetailDialog(row) {
  agreementDetailDialogVisible.value = true
  agreementActiveRow.value = row
  agreementEditContent.value = row?.content || ''
  agreementDetailLoading.value = true
  try {
    const detail = await getAgreement(row.id)
    agreementActiveRow.value = detail
    agreementEditContent.value = detail?.content || ''
    syncAgreementPaperOrder()
  } catch (error) {
    ElMessage.warning(error?.message || '加载协议详情失败')
  } finally {
    agreementDetailLoading.value = false
  }
}

async function saveAgreementElectronic() {
  if (!agreementActiveRow.value || agreementSaving.value) return
  if (agreementIsActiveSigned.value) return
  if (!agreementEditContent.value.trim()) { ElMessage.warning('请填写协议内容'); return }
  agreementSaving.value = true
  try {
    agreementActiveRow.value = await updateAgreement(agreementActiveRow.value.id, { content: agreementEditContent.value.trim() })
    ElMessage.success('协议已更新')
    await loadAgreementRows()
  } catch (error) {
    ElMessage.warning(error?.message || '保存协议失败')
  } finally {
    agreementSaving.value = false
  }
}

function chooseAgreementFile() { agreementFileInputRef.value?.click() }

async function uploadAgreementPaperFile(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file || !agreementActiveRow.value || agreementUploading.value) return
  if (agreementIsActiveSigned.value) return
  agreementUploading.value = true
  try {
    const files = await uploadAgreement(agreementActiveRow.value.id, { file, page: agreementPaperFiles.value.length + 1 })
    agreementActiveRow.value = { ...agreementActiveRow.value, type: 'PAPER', content: null, files }
    syncAgreementPaperOrder()
    ElMessage.success('扫描件已添加')
    await loadAgreementRows()
  } catch (error) {
    ElMessage.warning(error?.message || '添加扫描件失败')
  } finally {
    agreementUploading.value = false
  }
}

function agreementMoveFile(index, direction) {
  const target = index + direction
  if (agreementIsActiveSigned.value) return
  if (!agreementActiveRow.value || target < 0 || target >= agreementPaperFiles.value.length) return
  const pageFiles = [...agreementPaperFiles.value]
  const [item] = pageFiles.splice(index, 1)
  pageFiles.splice(target, 0, item)
  const signFiles = (agreementActiveRow.value.files || []).filter((file) => Number(file.page || 0) === 0)
  agreementActiveRow.value = {
    ...agreementActiveRow.value,
    files: [...signFiles, ...pageFiles.map((file, fileIndex) => ({ ...file, page: fileIndex + 1 }))],
  }
}

async function saveAgreementPaperOrder() {
  if (!agreementActiveRow.value || !agreementHasOrderChanged.value || agreementSavingOrder.value) return
  if (agreementIsActiveSigned.value) return
  agreementSavingOrder.value = true
  try {
    const files = await reorderAgreementFiles(agreementActiveRow.value.id, agreementPaperFiles.value.map((file) => file.id))
    agreementActiveRow.value = { ...agreementActiveRow.value, files }
    syncAgreementPaperOrder()
    ElMessage.success('扫描件顺序已保存')
    await loadAgreementRows()
  } catch (error) {
    ElMessage.warning(error?.message || '保存排序失败')
  } finally {
    agreementSavingOrder.value = false
  }
}

async function agreementRemoveFile(file) {
  if (!agreementActiveRow.value) return
  if (agreementIsActiveSigned.value) return
  try {
    await ElMessageBox.confirm('确认删除这张协议扫描件？', '删除扫描件', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
    const files = await deleteAgreementFile(agreementActiveRow.value.id, file.id)
    agreementActiveRow.value = { ...agreementActiveRow.value, files }
    syncAgreementPaperOrder()
    ElMessage.success('扫描件已删除')
    await loadAgreementRows()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.warning(error?.message || '删除扫描件失败')
  }
}

// ── Tab change auto-load ───────────────────────────────
const loadedTabs = reactive({ adopt: false, breading: false, follow: false, agreement: false })

watch(activeTabName, (tab) => {
  if (loadedTabs[tab]) return
  loadedTabs[tab] = true
  const loaders = { adopt: loadAdoptRows, breading: loadBreadingRows, follow: loadFollowRows, agreement: loadAgreementRows }
  loaders[tab]?.()
})

onMounted(() => {
  loadAdoptRows()
  loadedTabs.adopt = true
})
</script>

<style scoped>
.adoption-hub-tabs {
  margin-bottom: 4px;
}

.adoption-hub-tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
}

.adoption-filter-row {
  grid-template-columns: minmax(180px, 240px) auto;
}

.adoption-pet-cell {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.adoption-pet-detail-cell {
  display: inline-flex;
  width: 100%;
  padding: 0;
  text-align: left;
}

.adoption-person-cell {
  display: grid;
  gap: 3px;
}

.adoption-person-cell strong {
  color: var(--text);
}

.adoption-person-cell span,
.agreement-type-card small {
  color: var(--muted);
  font-size: 12px;
}

.adoption-review-summary {
  display: grid;
  gap: 4px;
  margin-bottom: 16px;
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: rgba(255, 253, 249, 0.92);
}

.adoption-review-summary span {
  color: var(--muted);
}

.agreement-type-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  width: 100%;
}

.agreement-type-grid :deep(.el-radio-button__inner) {
  width: 100%;
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 12px;
  text-align: left;
  background: rgba(255, 253, 249, 0.92);
  box-shadow: none;
}

.agreement-type-grid :deep(.el-radio-button:first-child .el-radio-button__inner),
.agreement-type-grid :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 12px;
}

.agreement-type-card {
  display: grid;
  gap: 4px;
}

.agreement-filter-row {
  grid-template-columns: minmax(160px, 220px) minmax(160px, 220px) auto;
}

.agreement-editor {
  display: grid;
  gap: 16px;
}

.agreement-editor-meta,
.agreement-editor-toolbar,
.agreement-file-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.agreement-editor-meta {
  flex-wrap: wrap;
  color: var(--muted);
}

.agreement-editor-section {
  padding-top: 14px;
  border-top: 1px solid var(--line);
}

.agreement-editor-toolbar {
  justify-content: space-between;
  margin-bottom: 12px;
}

.agreement-editor-toolbar strong {
  color: #5d3927;
}

.agreement-file-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 14px;
}

.agreement-file-card {
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: rgba(255, 253, 249, 0.96);
}

.agreement-file-card img {
  display: block;
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
  background: var(--bg-soft);
}

.agreement-file-card-body {
  display: grid;
  gap: 8px;
  padding: 10px;
}

.agreement-file-card-body strong {
  color: #5d3927;
}

.agreement-file-actions {
  justify-content: space-between;
  flex-wrap: wrap;
}

.agreement-sign-preview {
  display: inline-block;
  max-width: 280px;
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: rgba(255, 253, 249, 0.96);
}

.agreement-sign-preview img {
  display: block;
  width: 100%;
  max-height: 180px;
  object-fit: contain;
}

.follow-task-modify-form {
  display: grid;
  gap: 2px;
}

@media (max-width: 720px) {
  .adoption-filter-row,
  .agreement-filter-row,
  .agreement-type-grid {
    grid-template-columns: 1fr;
  }

  .agreement-editor-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>