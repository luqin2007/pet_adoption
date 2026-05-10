# 服务单元测试文档

## 总体说明

本文件覆盖 `src/main/java/com/example/backend/service` 下所有服务公开方法。预计输出中的“成功响应”表示返回 DTO、实体、分页对象或无异常完成；“业务异常”表示抛出 `ServiceException`，错误码按方法中的 `require`、`requireEqual`、`requireById`、权限校验或状态校验决定。

## BaseService

| 方法 | 测试输入 | 预计输出 | 当前覆盖 |
|---|---|---|---|
| `beginRedisUuid(keyTemplate, content)` | Redis key 模板、业务内容；mock Redis 不存在冲突 UUID | 返回 UUID；写入 `StringRedisTemplate` 包装的 Redis key；设置过期时间 | 已覆盖 |
| `requireRedisUuid(keyTemplate, uuid)` | 未过期 UUID | 返回格式化后的 Redis key；刷新过期时间 | 已覆盖 |
| `requireRedisUuid(keyTemplate, uuid)` | 已过期或不存在 UUID | 抛出 `exception.invalidate.request_timeout` | 已覆盖 |
| `convertDto(result, converter)` | MyBatis `Page<V>` 和转换函数 | 保留 current/size/total；records 转换为目标类型 | 已覆盖 |
| `selectById(id, columns...)` | null id、有效 id | null 或 mapper 查询结果 | 待 mapper 单测 |
| `requireById(id)` | 存在/不存在 id | 返回实体或 not found 业务异常 | 待 mapper 单测 |
| `requireExist(id)` | 存在/不存在 id | 无异常或 not found 业务异常 | 待 mapper 单测 |
| `listById(ids, columns...)` | id 集合 | 返回实体列表 | 待 mapper 单测 |
| `groupById(...)` | id 集合或 stream | 返回以 id 为 key 的 Map | 待 mapper 单测 |

## FileService

| 方法 | 测试输入 | 预计输出 | 当前覆盖 |
|---|---|---|---|
| `uploadTempImage(file, name, uuid, fileTemplate, parentType)` | 图片文件、登录用户、临时 uuid | 文件写入临时目录；Redis hash 写入 `TempFileInfo` | 待补图片样本 |
| `uploadTempImage(...)` | 非支持媒体类型文件 | 抛出 `exception.invalidate.file.unsupported_media`；不写 Redis | 已覆盖 |
| `uploadTempMedia(...)` | 图片/视频文件 | 写临时目录；返回媒体类型；写 Redis | 待补视频样本 |
| `uploadTempFile(...)` | 任意文件、登录用户、临时 uuid | 写临时目录；返回 `TempFileInfo`；写 Redis 并刷新 TTL | 已覆盖 |
| `deleteTempFile(...)` | Redis 中存在临时文件 | 从 Redis hash 删除；创建删除任务；刷新 TTL | 待补事务同步 |
| `deleteFile(filename, parentId, parentType)` | 非空文件名 | 插入 `DeleteJob`，路径指向上传目录 | 已覆盖 |
| `deleteFile(...)` | 空白文件名 | 不创建删除任务 | 已覆盖 |
| `saveTempFiles(...)` | Redis hash 中存在多个临时文件 | 复制到正式目录；清理临时目录；删除 Redis key；返回按时间排序 stream | 待集成测试 |
| `saveTempMedias(...)` | 临时图片/视频集合 | 保存媒体记录；自动设置唯一封面 | 待 mapper 单测 |
| `uploadImage(...)` | 图片文件 | 保存图片；返回生成文件名 | 待补图片样本 |
| `uploadMediaFile(...)` | 媒体文件和 `MediaFile` | 保存文件和数据库记录；封面规则正确 | 待 mapper 单测 |
| `uploadMediaFiles(...)` | 多文件列表 | 批量保存文件和媒体记录 | 待 mapper 单测 |
| `updateMediaFile(...)` | 媒体 id、父对象、更新函数 | 校验父对象；更新元数据；封面切换正确 | 待 mapper 单测 |
| `deleteMediaFile(...)` | 媒体 id、父对象 | 删除记录；创建删除任务；必要时重置封面 | 待 mapper 单测 |
| `deleteAllMediaFiles(...)` | 父对象 id/type | 删除全部媒体记录；创建目录删除任务 | 待 mapper 单测 |
| `getCoverUrl(...)` | 父对象 id/type | 返回封面 URL 或 null | 待 mapper 单测 |
| `getCoverUrls(...)` | 父对象 id 集合 | 返回 parentId 到封面 URL 的 Map | 待 mapper 单测 |

## UserService

| 方法 | 测试输入 | 预计输出 | 当前覆盖 |
|---|---|---|---|
| `register(request)` | 合法用户名/邮箱/密码/角色 | 创建用户；密码加密；返回用户响应 | 待 mapper 单测 |
| `register(request)` | 重复用户名或邮箱 | 抛出重复业务异常 | 待 mapper 单测 |
| `isUsernameExist(username)` | 用户名 | 返回是否存在 | 待 mapper 单测 |
| `isEmailExist(email)` | 邮箱 | 返回是否存在 | 待 mapper 单测 |
| `sendMailCode(email)` | 已注册邮箱 | 生成验证码；写 Redis；发布邮件事件 | 已覆盖 |
| `login(username, password)` | 正确密码 | 返回用户信息和 access/refresh token | 已覆盖 |
| `login(username, password)` | 错误密码 | 抛出认证异常 | 已覆盖 |
| `getUser(userId)` | 有效用户 id | 返回用户响应 | 待 mapper 单测 |
| `getUserWithToken(username)` | 有效用户名 | 返回用户响应和 token | JWT 已部分覆盖 |
| `removeUser(userId)` | 管理员删除用户 | 删除用户或标记删除 | 待 mapper 单测 |
| `getAllUsers(page)` | 分页参数 | 返回用户分页 | 待 mapper 单测 |
| `forgetPassword(email)` | 已注册邮箱 | 写重置 key；发送验证码 | 待 Redis/事件单测 |
| `resetPassword(request)` | 合法 key 和新密码 | 更新密码；删除/失效 key | 待 Redis/mapper 单测 |
| `update(userId, request)` | 本人或管理员更新资料 | 返回更新后的用户响应 | 待权限单测 |
| `uploadAvatar(userId, file)` | 图片文件 | 上传头像；更新用户 avatar | 文件/JWT 已部分覆盖 |
| `deleteAvatar(userId)` | 有头像用户 | 删除头像文件；清空 avatar | 待 mapper 单测 |
| `loadUserByUsername(username)` | 存在/不存在用户名 | 返回 `CustomUserDetails` 或 `UsernameNotFoundException` | 权限映射已覆盖 |

## PetService

| 方法 | 测试输入 | 预计输出 | 当前覆盖 |
|---|---|---|---|
| `addPet(request)` | 宠物基础信息、标签、媒体 uuid | 创建宠物；保存标签/媒体；返回新增响应 | 待集成测试 |
| `addLocation(petId, request)` | 宠物 id、经纬度/地址 | 保存位置；返回宠物响应 | 待 mapper 单测 |
| `getPets(query, page)` | 状态/类型/位置/分页 | 返回分页宠物列表 | 待 mapper 单测 |
| `getPet(petId)` | 有效宠物 id | 返回详情、媒体、标签、健康信息 | 待集成测试 |
| `updatePet(petId, request)` | 更新字段 | 更新宠物并发布事件 | 待 mapper/事件单测 |
| `deletePet(petId)` | 工作人员、普通用户 | 工作人员可废弃；普通用户被拒绝 | 已覆盖 |
| `uploadMedia(petId, request)` | 图片/视频 | 上传媒体；返回媒体响应 | FileService 已部分覆盖 |
| `updateMedia(petId, mediaId, request)` | 媒体元数据 | 更新名称/描述/封面 | 待 mapper 单测 |
| `deleteMedia(petId, mediaId)` | 媒体 id | 删除媒体并重置封面 | 待 mapper 单测 |
| `getTags(petId)` | 宠物 id | 返回标签列表 | 待 mapper 单测 |
| `getTags(petIds)` | 宠物 id 集合 | 返回 id 到标签列表 Map | 待 mapper 单测 |
| `addTags(petId, request)` | 标签名称列表 | 新增标签并返回完整列表 | 待 mapper 单测 |
| `deleteTags(petId, request)` | 标签 id 列表 | 删除并返回剩余标签 | 待 mapper 单测 |
| `updateStatus(petId, request)` | 新状态、原因 | 写状态记录；更新宠物状态 | 待 mapper 单测 |
| `getStatusRecords(petId, page, userFilter)` | 宠物 id、分页、用户过滤 | 返回状态历史分页 | 待 mapper 单测 |

## AdoptBreadingService

| 方法组 | 方法 | 测试输入 | 预计输出 | 当前覆盖 |
|---|---|---|---|---|
| 领养 | `addAdopt`, `getAdopt`, `getAdopts`, `updateAdoptStatus` | 领养申请、id、查询条件、状态 | 新增/查询/分页/状态变更；非法状态抛异常 | 待 mapper/事件单测 |
| 寄养 | `addBreading`, `getBreading`, `getBreadingPets`, `updateBreadingStatus` | 寄养申请、id、查询条件、状态 | 新增/查询/分页/状态变更 | 待 mapper/事件单测 |
| 协议 | `beginAgreement`, `addAgreement`, `updateAgreement`, `getAgreement`, `getAgreements` | 临时 uuid、协议请求、分页 | Redis uuid、协议新增/修改/查询 | beginAgreement 已覆盖 |
| 协议文件 | `uploadAgreement`, `uploadAgreementFile`, `deleteAgreementFile`, `reorderAgreementFiles`, `signAgreement` | 文件、uuid、文件 id、排序列表、签名图片 | 文件保存/删除/排序/签署 | FileService 已部分覆盖 |
| 跟踪 | `addFollowTask`, `updateFollowTask`, `getFollowTask`, `getFollowTasks`, `addFollowRecord`, `getFollowRecords` | 跟踪任务/记录请求、分页 | 新增、修改、查询和分页 | 待 mapper/事件单测 |

## ItemDonationService

| 方法组 | 方法 | 测试输入 | 预计输出 | 当前覆盖 |
|---|---|---|---|---|
| 捐赠 | `beginDonation`, `uploadDonationFile`, `deleteDonationFile`, `addDonation`, `updateDonation`, `updateDonationStatus`, `getDonation`, `getDonations` | uuid、文件、捐赠请求、状态、分页 | 临时文件、捐赠新增/修改/状态流转/查询 | beginDonation 已覆盖 |
| 物资 | `addItem`, `updateItem`, `getItem`, `getItems`, `discardItem` | 物资请求、id、查询条件 | 新增/修改/查询/弃用 | 待 mapper 单测 |
| 分类 | `addCategory`, `updateCategory`, `discardCategory`, `getCategory`, `getCategories` | 分类请求、名称、分页 | 新增/修改/弃用/查询 | 待 mapper 单测 |
| 库存 | `addStockRecord`, `getStock`, `getStocks`, `getStockRecords` | 入库/出库请求、库存查询 | 写库存流水；返回库存/流水分页 | 待 mapper 单测 |
| 订阅 | `addSubscribe`, `getSubscribe`, `getSubscribes`, `cancelSubscribe` | 订阅请求、id 集合、分页 | 新增/查询/取消订阅 | 待 mapper 单测 |

## LostPetService

| 方法组 | 方法 | 测试输入 | 预计输出 | 当前覆盖 |
|---|---|---|---|---|
| 丢失宠物 | `beginLostPet`, `addLostPet`, `uploadLostPetMedia`, `deleteLostPetMedia`, `updateLostPet`, `getLostPet`, `getLostPets` | uuid、媒体、丢失宠物请求、分页 | 创建/上传/删除/修改/详情/分页 | beginLostPet 已覆盖 |
| 相似匹配 | `getSimilarPets`, `markPetMismatch`, `listMatchedPets`, `listMatchedLostPets` | 丢失宠物、宠物、位置 | 返回匹配列表；不匹配记录被排除 | 待匹配逻辑单测 |
| 认领 | `addClaim`, `getClaim`, `getClaims`, `cancelClaim`, `approveClaim` | 认领请求、审批请求、分页 | 新增/查询/取消/审批 | 待 mapper/事件单测 |

## MedicalService

| 方法组 | 方法 | 测试输入 | 预计输出 | 当前覆盖 |
|---|---|---|---|---|
| 初诊 | `addFirstVisitRegistration`, `getFirstVisitRegistrations`, `getFirstVisitRegistration` | 初诊登记、查询、分页 | 新增/列表/详情 | 待 mapper 单测 |
| 病历 | `addMedicalRecord`, `updateMedicalRecord`, `getMedicalRecord`, `getMedicalRecords` | 宠物 id、病历请求、分页 | 新增/修改/详情/分页 | 待 mapper 单测 |
| 医疗明细 | `addMedicalDetail`, `getMedicalDetail`, `getMedicalDetails`, `updateMedicalDetail`, `completeMedicalDetail` | 明细请求、状态 | 新增/查询/修改/完成；已完成不可编辑 | 待 mapper 单测 |
| 诊断和方案 | `addDiagnosis`, `discardDiagnosis`, `addTreatmentPlan`, `discardTreatmentPlan` | 诊断/治疗计划请求、id 集合 | 新增/作废 | 待 mapper 单测 |
| 检查 | `beginExamination`, `uploadExamination`, `deleteExamination`, `addExamination`, `getExamination` | 明细 id、uuid、检查文件、请求 | 临时检查文件、新增检查、查询 | beginExamination 已覆盖 |
| 疫苗驱虫 | `addVaccine`, `getVaccines`, `getLatestVaccines`, `addDeworm`, `getDeworms`, `getVaccinesByPetIds`, `getDewormsByPetIds` | 宠物 id、疫苗/驱虫请求、id 集合 | 新增和查询列表/最新记录/分组 Map | 待 mapper 单测 |
| 康复 | `addRehabPlan`, `getRehabPlan`, `getRehabPlans`, `updateRehabPlanStatus`, `addRehabRecord`, `getRehabRecords` | 康复计划/记录请求、状态、分页 | 新增/查询/状态更新/记录列表 | 待 mapper 单测 |
| 健康评估 | `addHealthAssessment`, `getHealthAssessment`, `getHealthAssessments` | 宠物 id、评估请求、分页 | 新增/详情/分页 | 待 mapper 单测 |

## NoticeService

| 方法 | 测试输入 | 预计输出 | 当前覆盖 |
|---|---|---|---|
| `getNotices(query, page)` | 通知查询和分页 | 返回通知分页 | 待 mapper 单测 |
| `countUnread()` | 当前登录用户 | 返回未读数量 | 待 mapper 单测 |
| `readNotice(noticeId, isRead)` | id 集合、已读标记 | 更新已读状态 | 待 mapper 单测 |
| `connect()` | 当前登录用户 | 返回 SSE emitter 并注册连接 | 已覆盖 |
| `addNotices(receivers, source, title, content)` | 接收用户、来源、标题、内容 | 批量保存通知并推送 SSE | 已覆盖 |

## PublicityService

| 方法 | 测试输入 | 预计输出 | 当前覆盖 |
|---|---|---|---|
| `addArticle(request)` | 工作人员/普通用户、文章请求 | 工作人员可创建科普文章；普通用户被拒绝 | 已覆盖 |
| `getArticles(query, page)` | 查询条件、分页 | 返回文章分页，包含点赞/收藏状态 | 待 mapper 单测 |
| `getArticle(articleId)` | 文章 id | 返回详情并增加阅读/展示所需字段 | 待 mapper 单测 |
| `updateArticle(articleId, request)` | 更新字段 | 返回更新后文章 | 待 mapper 单测 |
| `deleteArticle(articleId)` | 文章 id | 删除或标记删除 | 待 mapper 单测 |
| `updateArticleStatus(articleId, statusName)` | 状态名 | 更新文章状态；非法状态抛异常 | 待状态单测 |
| `likeArticle(articleId, liked)` | 点赞/取消点赞 | 返回点赞数量和状态 | 待 mapper 单测 |
| `favoriteArticle(articleId, favorited)` | 收藏/取消收藏 | 状态更新，无异常 | 待 mapper 单测 |
| `getFavorites(page)` | 当前用户、分页 | 返回收藏文章分页 | 待 mapper 单测 |
| `shareArticle(articleId)` | 已发布/草稿文章 id | 已发布文章返回分享信息并更新分享计数；草稿被拒绝 | 已覆盖 |

## RescueTaskService

| 方法 | 测试输入 | 预计输出 | 当前覆盖 |
|---|---|---|---|
| `beginRescueTask()` | 当前登录用户 | 返回临时 uuid，写 Redis | Base 已覆盖 |
| `addRescueTask(request)` | 救助任务请求、临时媒体 | 新增任务、位置、媒体、记录 | 待集成测试 |
| `getRescueTask(taskId)` | 创建人/无关普通用户/工作人员 | 创建人和工作人员可查看；无关普通用户被拒绝 | 已覆盖 |
| `uploadRescueTaskMedia(uuid, file)` | uuid、媒体文件 | 上传临时媒体 | File 已部分覆盖 |
| `updateRescueTask(taskId, request)` | 更新字段 | 更新任务并发布事件 | 待 mapper/事件单测 |
| `deleteRescueTaskMedia(uuid, filename)` | 临时媒体 | 删除临时媒体 | 待 FileService 单测 |
| `deleteRescueTaskMedia(taskId, mediaId)` | 已保存媒体 | 删除正式媒体 | 待 mapper 单测 |
| `deleteRescueTask(taskId)` | 任务 id | 删除任务和媒体 | 待 mapper 单测 |
| `updateRescueTaskStatus(taskId, request)` | 状态记录请求 | 写任务记录并更新任务状态 | 待 mapper 单测 |
| `getRescueTaskRecords(taskId)` | 任务 id | 返回任务记录集合 | 待 mapper 单测 |
| `getRescueTasks(page)` | 分页 | 返回任务分页 | 待 mapper 单测 |
| `assignRescueTask(taskId, request)` | 用户 id 集合 | 分配志愿者/工作人员并返回用户列表 | 待 mapper 单测 |

## VolunteerService

| 方法组 | 方法 | 测试输入 | 预计输出 | 当前覆盖 |
|---|---|---|---|---|
| 招募 | `addRecruitment`, `getRecruitment`, `getRecruitments`, `updateRecruitment`, `updateRecruitmentStatus` | 招募请求、状态、分页 | 新增/查询/修改/状态变更 | 待 mapper 单测 |
| 申请 | `addApplication`, `getApplication`, `getApplications`, `setApplicationStatus` | 申请请求、审批状态、分页 | 新增/查询/审批；通过后建立档案 | 待 mapper/事件单测 |
| 档案 | `getProfile`, `getProfiles`, `updateProfile`, `updateProfileStatus` | 档案 id、更新字段、状态 | 查询/修改/状态变更 | 待 mapper 单测 |
| 排班 | `addShift`, `getShift`, `getShifts`, `updateShift`, `updateShiftStatus` | 排班请求、时间、状态 | 新增/查询/修改/状态变更；时间冲突抛异常 | 待时间冲突单测 |
| 服务记录 | `addServiceRecord`, `getServiceRecord`, `getServiceRecords`, `updateServiceRecordStatus` | 服务记录请求、审核请求 | 新增/查询/审核 | 待 mapper 单测 |
| 奖励 | `addReward`, `getReward`, `getRewards`, `issueReward` | 奖励请求、分页 | 新增/查询/发放 | issueReward 已覆盖 |

## 单元测试优先级建议

- 下一批 P0/P1：`UserService.login/register`、`PublicityService.likeArticle/favoriteArticle`、`VolunteerService.checkTimeConflict`、`LostPetService.listMatchedPets`。
- P2 集成测试：领养/寄养协议、医疗检查、救助任务、捐赠库存，这些方法跨 mapper、Redis、文件和事件，需要 MySQL Shell 初始化数据库后执行。
