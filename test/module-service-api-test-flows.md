# Service 单元测试与 API 测试流程

本文档按指定顺序覆盖每个业务模块的 Service 接口、Controller API、单元测试重点和集成测试流程。后续补充自动化测试代码时，优先按本文档顺序推进；接口说明与数据库脚本如与后端实现不一致，以后端 Controller、Service、Mapper 当前行为为准修正 `openapi.yml` 和 `init.sql`。

## 通用测试前置条件

1. 确认数据库已导入 `backend/init.sql`。
2. 根据 `src/main/resources/application.properties` 准备 MySQL、Redis 和对象存储配置。
3. 使用 MySQL Shell 验证数据库：

   ```powershell
   & 'C:\Dev\tools\mysql-shell-9.7.0-windows-x86-64bit\bin\mysqlsh.exe' --sql root@192.168.1.170:3306/pet_adoption
   ```

   连接后输入 `application.properties` 中配置的数据库密码，再执行 `SHOW TABLES;`、`SELECT COUNT(*) FROM user;` 等基础检查。

4. 启动后端：

   ```powershell
   .\mvnw.cmd test
   .\mvnw.cmd -DskipTests package
   java -jar target\backend-0.0.1-SNAPSHOT.jar --server.port=18080
   ```

5. API 集成测试基础地址：`http://localhost:18080`。
6. 认证类接口先获得管理员或测试用户 Token，后续请求统一携带 `Authorization: Bearer <token>`。
7. 所有写接口测试后都需要查询数据库或调用对应 GET 接口确认数据落库、状态变化和关联记录正确。

## 1. 用户管理功能

### 单元测试计划

| Service 方法 | 对应 API | 单元测试重点 |
| --- | --- | --- |
| `register` | `POST /auth/register` | 用户名/邮箱/密码/验证码校验，通过后密码加密、角色初始化、重复用户拦截。 |
| `isUsernameExist` | `GET /auth/check/username/{username}` | 已存在返回 true，不存在返回 false，空值或非法值触发参数校验。 |
| `isEmailExist` | `GET /auth/check/email/{email}` | 邮箱格式校验，已存在和不存在两类结果。 |
| `sendMailCode` | `POST /auth/check/code` | 验证码生成、邮件发送调用、Redis 写入和过期时间。 |
| `login` | `POST /auth/login` | 正确账号签发 Token，错误密码返回认证失败，禁用用户不可登录。 |
| `forgetPassword` | `POST /auth/forget` | 邮箱存在性、验证码校验和重置令牌生成。 |
| `resetPassword` | `POST /auth/reset` | 重置令牌有效时更新密码，令牌过期或不匹配时失败。 |
| `getUser` / `getUserWithToken` | `GET /users/{id}`、`POST /auth/refresh` | 用户脱敏输出，Token 刷新保持用户身份一致。 |
| `update` | `PUT /users/{id}` | 只允许合法字段更新，邮箱/用户名唯一性，当前用户与权限校验。 |
| `uploadAvatar` / `deleteAvatar` | `PATCH /users/{id}/avatar`、`DELETE /users/{id}/avatar` | 文件上传、旧头像清理、删除后用户头像字段为空。 |
| `removeUser` / `getAllUsers` | `DELETE /users/{id}`、`GET /users/` | 删除状态、分页查询、管理员权限。 |
| `logout` | `POST /auth/logout` | Token 失效或缓存清理，重复退出行为稳定。 |

### API 测试流程

1. `GET /auth/check/username/codex_user`，预期返回 `200`，响应表示用户名可用或已存在。
2. `GET /auth/check/email/codex_user@example.com`，预期返回 `200`。
3. `POST /auth/check/code?email=codex_user@example.com`，预期返回 `200` 或邮件服务可识别错误；若邮件服务不可用，记录外部依赖限制。
4. 使用数据库或 Redis 中的验证码执行 `POST /auth/register`，预期创建用户成功。
5. 使用错误密码执行 `POST /auth/login`，预期返回统一错误；使用正确密码登录，预期返回 Token。
6. 携带 Token 调用 `GET /users/{id}`、`PUT /users/{id}`、`PATCH /users/{id}/avatar`、`DELETE /users/{id}/avatar`。
7. 调用 `POST /auth/refresh`，预期获得新 Token；调用 `POST /auth/logout` 后再次访问受保护接口，预期 `401`。
8. 管理员 Token 调用 `GET /users/` 和 `DELETE /users/{id}`，验证分页和删除结果。

## 2. 流浪宠物信息管理功能

### 单元测试计划

| Service 方法 | 对应 API | 单元测试重点 |
| --- | --- | --- |
| `addPet` | `POST /pets/` | 必填字段、物种/性别/年龄范围、初始状态和创建人写入。 |
| `addLocation` | `POST /pets/{id}/location` | 宠物存在性、坐标和地址保存、最新位置关联。 |
| `getPets` / `getPet` | `GET /pets/`、`GET /pets/{id}` | 分页筛选、状态筛选、标签和媒体聚合。 |
| `updatePet` | `PUT /pets/{id}` | 只更新允许字段，关联状态不被误改。 |
| `deletePet` | `DELETE /pets/{id}` | 软删除或删除标记，关联查询不再返回。 |
| `uploadMedia` / `updateMedia` / `deleteMedia` | `POST /pets/{id}/media`、`PUT /pets/{id}/media/{mid}`、`DELETE /pets/{id}/media/{mid}` | 文件归属、排序/描述更新、删除对象存储文件和数据库记录。 |
| `getTags` / `addTags` / `deleteTags` | `POST /pets/{id}/tags`、`DELETE /pets/{id}/tags` | 标签去重、批量查询、删除不存在标签时结果稳定。 |
| `updateStatus` / `getStatusRecords` | `PUT /pets/{id}/status`、`GET /pets/{id}/status` | 状态流转合法性、状态记录追加、最新状态同步。 |

### API 测试流程

1. `POST /pets/` 新增宠物，记录 `petId`。
2. `POST /pets/{petId}/location` 写入发现地点，随后 `GET /pets/{petId}` 验证位置字段。
3. `POST /pets/{petId}/media` 上传图片或视频，`PUT /pets/{petId}/media/{mid}` 修改描述，`DELETE /pets/{petId}/media/{mid}` 删除。
4. `POST /pets/{petId}/tags` 添加多个标签，`DELETE /pets/{petId}/tags` 删除其中一个，验证标签去重。
5. `PUT /pets/{petId}/status` 改为救助中或待领养，`GET /pets/{petId}/status` 验证状态历史。
6. `GET /pets/` 使用状态、类型、标签和分页参数查询。
7. `PUT /pets/{petId}` 修改基础资料，再 `DELETE /pets/{petId}` 验证删除后查询行为。

## 3. 救助任务管理功能

### 单元测试计划

| Service 方法 | 对应 API | 单元测试重点 |
| --- | --- | --- |
| `beginRescueTask` | `PUT /tasks/` | 创建草稿任务或临时上传上下文，返回临时 ID。 |
| `addRescueTask` | `POST /tasks/` | 关联宠物/位置/上报人，任务初始状态正确。 |
| `getRescueTasks` / `getRescueTask` | `GET /tasks/`、`GET /tasks/{id}` | 分页、状态、负责人筛选和详情聚合。 |
| `uploadRescueTaskMedia` / `deleteRescueTaskMedia` | `POST /tasks/{_id}/uploads`、`DELETE /tasks/{_id}/uploads/{file}`、`DELETE /tasks/{id}/media/{mid}` | 草稿文件上传、正式媒体删除、文件名和数据库记录一致。 |
| `updateRescueTask` | `PUT /tasks/{id}` | 描述、紧急程度、地点、负责人修改。 |
| `updateRescueTaskStatus` / `getRescueTaskRecords` | `PATCH /tasks/{id}/status`、`GET /tasks/{id}/status` | 状态流转记录、非法回退拦截。 |
| `assignRescueTask` | `POST /tasks/{id}/assign` | 分配志愿者或工作人员，重复分配和不存在人员处理。 |
| `deleteRescueTask` | `DELETE /tasks/{id}` | 删除后列表不可见，关联媒体和记录处理符合设计。 |

### API 测试流程

1. `PUT /tasks/` 创建救助任务草稿，记录 `_id`。
2. `POST /tasks/{_id}/uploads` 上传现场图片，验证返回文件名。
3. `POST /tasks/` 提交正式任务，绑定上一步 `_id` 和宠物/地点数据。
4. `GET /tasks/` 查询列表，`GET /tasks/{id}` 查询详情。
5. `POST /tasks/{id}/assign` 分配负责人，预期任务负责人字段更新。
6. `PATCH /tasks/{id}/status` 依次推进状态，`GET /tasks/{id}/status` 验证记录。
7. `PUT /tasks/{id}` 修改任务信息，删除某个媒体后再次查询详情。
8. `DELETE /tasks/{id}` 删除任务，验证列表和详情响应。

## 4. 医疗护理管理功能

### 单元测试计划

| Service 方法 | 对应 API | 单元测试重点 |
| --- | --- | --- |
| `addFirstVisitRegistration` / `getFirstVisitRegistration(s)` | `POST /medical/first`、`GET /medical/first`、`GET /medical/first/{id}` | 初诊登记、宠物存在性、分页查询。 |
| `addMedicalRecord` / `updateMedicalRecord` / `getMedicalRecord(s)` | `POST /medical/record/pet/{id}`、`PUT /medical/record/{id}`、`GET /medical/record/{id}`、`GET /medical/record` | 病历创建、医生/宠物关联、病历状态和筛选。 |
| `addMedicalDetail` / `updateMedicalDetail` / `completeMedicalDetail` | `POST /medical/detail`、`PUT /medical/detail/{id}`、`PATCH /medical/detail/{id}` | 诊疗明细创建、更新、完成后不可重复完成。 |
| `addDiagnosis` / `discardDiagnosis` | `POST /medical/detail/{id}/diagnosis`、`DELETE /medical/diagnosis/{dId}` | 诊断新增、废弃标记、明细聚合。 |
| `addTreatmentPlan` / `discardTreatmentPlan` | `POST /medical/detail/{id}/plan`、`DELETE /medical/plan` | 治疗方案新增、废弃和多方案排序。 |
| `beginExamination` / `uploadExamination` / `deleteExamination` / `addExamination` / `getExamination` | `PUT /medical/details/{id}/exam`、`PUT /medical/exam/{_id}/doc`、`DELETE /medical/exam/{_id}/doc/{name}`、`POST /medical/exam/{_id}`、`GET /medical/exam/{id}` | 检查草稿、附件上传、正式检查创建和附件删除。 |
| `addVaccine` / `getVaccines` / `getLatestVaccines` | `POST /medical/vaccine/pet/{id}`、`GET /medical/vaccine/pet/{id}`、`GET /medical/vaccine/pet/{id}/new` | 免疫记录新增、历史查询、最新疫苗记录。 |
| `addDeworm` / `getDeworms` | `POST /medical/deworm/pet/{id}`、`GET /medical/deworm/pet/{id}` | 驱虫记录创建、按宠物查询。 |
| `addRehabPlan` / `getRehabPlan(s)` / `updateRehabPlanStatus` / `addRehabRecord` / `getRehabRecords` | `POST /medical/rehab/pet/{id}`、`GET /medical/rehab/{id}`、`GET /medical/rehab`、`PUT /medical/rehab/{id}/status`、`POST /medical/rehab/{id}/record`、`GET /medical/rehab/{id}/record` | 康复计划、状态流转、康复记录追加。 |
| `addHealthAssessment` / `getHealthAssessment(s)` | `POST /medical/health/pet/{id}`、`GET /medical/health/{id}`、`GET /medical/health`、`GET /medical/health/pet/{id}` | 健康评估创建、分页、按宠物查询。 |

### API 测试流程

1. 准备一个已存在宠物 `petId`。
2. `POST /medical/first` 创建初诊登记，分别通过列表和详情接口验证。
3. `POST /medical/record/pet/{petId}` 创建病历，`PUT /medical/record/{id}` 更新病历摘要。
4. `POST /medical/detail` 创建诊疗明细，追加诊断和治疗方案，再分别废弃一条诊断/方案。
5. `PUT /medical/details/{detailId}/exam` 创建检查草稿，上传检查附件，`POST /medical/exam/{_id}` 正式保存，`GET /medical/exam/{id}` 验证。
6. `PATCH /medical/detail/{id}` 完成诊疗明细，验证重复完成失败或保持幂等。
7. `POST /medical/vaccine/pet/{petId}` 和 `POST /medical/deworm/pet/{petId}` 添加免疫/驱虫记录，验证历史和最新查询。
8. `POST /medical/rehab/pet/{petId}` 创建康复计划，推进状态并追加康复记录。
9. `POST /medical/health/pet/{petId}` 创建健康评估，分别按 ID、分页、宠物查询验证。

## 5. 领养与寄养管理功能

### 单元测试计划

| Service 方法 | 对应 API | 单元测试重点 |
| --- | --- | --- |
| `addAdopt` / `getAdopt(s)` / `updateAdoptStatus` | `POST /adopt/adopt`、`GET /adopt/adopt/{id}`、`GET /adopt/adopt`、`PATCH /adopt/adopt/{id}/{st}` | 领养申请创建、宠物状态校验、审批状态流转。 |
| `addBreading` / `getBreading` / `getBreadingPets` / `updateBreadingStatus` | `POST /adopt/breading`、`GET /adopt/breading/{id}`、`GET /adopt/breading`、`PATCH /adopt/breading/{id}/{st}` | 寄养申请、寄养宠物列表、审批状态。 |
| `beginAgreement` / `uploadAgreement` / `uploadAgreementFile` | `PUT /adopt/agreement`、`POST /adopt/agreement/upload/{_id}`、`POST /adopt/agreement/{id}/files` | 协议草稿、文件上传、文件归属。 |
| `addAgreement` / `updateAgreement` / `getAgreement(s)` | `POST /adopt/agreement`、`PUT /adopt/agreement/{id}`、`GET /adopt/agreement/{id}`、`GET /adopt/agreement` | 协议正式创建、修改、分页筛选。 |
| `deleteAgreementFile` / `reorderAgreementFiles` / `signAgreement` | `DELETE /adopt/agreement/upload/{_id}/{name}`、`DELETE /adopt/agreement/{id}/files/{fid}`、`PUT /adopt/agreement/{id}/files/order`、`PATCH /adopt/agreement/{id}/sign` | 文件删除、排序、签署状态和签署时间。 |
| `addFollowTask` / `updateFollowTask` / `getFollowTask(s)` | `POST /adopt/follow/adopt/{id}`、`PUT /adopt/follow/{id}`、`GET /adopt/follow/{id}`、`GET /adopt/follow` | 回访任务创建、更新、按领养申请查询。 |
| `addFollowRecord` / `getFollowRecords` | `POST /adopt/follow/{id}/record`、`GET /adopt/follow/{id}/record`、`GET /adopt/follow/record` | 回访记录追加、分页和条件查询。 |

### API 测试流程

1. 选择待领养宠物，`POST /adopt/adopt` 创建领养申请。
2. `GET /adopt/adopt/{id}` 和 `GET /adopt/adopt` 验证申请详情和列表。
3. `PATCH /adopt/adopt/{id}/{st}` 审批通过或拒绝，验证状态和宠物状态联动。
4. `POST /adopt/breading` 创建寄养申请，查询并更新状态。
5. `PUT /adopt/agreement` 创建协议草稿并上传文件。
6. `POST /adopt/agreement` 创建正式协议，继续上传正式文件、调整排序、删除文件。
7. `PATCH /adopt/agreement/{id}/sign` 签署协议，验证签署时间和状态。
8. `POST /adopt/follow/adopt/{adoptId}` 创建回访任务，更新任务后追加回访记录。
9. 分别通过任务维度和全局维度查询回访记录。

## 6. 物资与捐赠管理功能

### 单元测试计划

| Service 方法 | 对应 API | 单元测试重点 |
| --- | --- | --- |
| `beginDonation` / `uploadDonationFile` / `deleteDonationFile` | `PUT /items/donations`、`POST /items/donations/upload/{_id}`、`DELETE /items/donations/upload/{_id}/{name}` | 捐赠草稿、凭证上传和删除。 |
| `addDonation` / `updateDonation` / `updateDonationStatus` / `getDonation(s)` | `POST /items/donations`、`PUT /items/donations/{id}`、`PATCH /items/donations/{id}/status`、`GET /items/donations/{id}`、`GET /items/donations` | 捐赠创建、审核、状态流转、分页筛选。 |
| `addCategory` / `updateCategory` / `discardCategory` / `getCategory(ies)` | `POST /items/categories`、`PUT /items/categories/{id}`、`DELETE /items/categories/{id}`、`GET /items/categories/{id}`、`GET /items/categories` | 分类唯一性、启停用、被物资引用时删除限制。 |
| `addItem` / `updateItem` / `discardItem` / `getItem(s)` | `POST /items/items`、`PUT /items/items/{id}`、`DELETE /items/items/{id}`、`GET /items/items/{id}`、`GET /items/items` | 物资档案、分类关联、库存基础信息。 |
| `addStockRecord` / `getStock` / `getStocks` / `getStockRecords` | `POST /items/stocks`、`GET /items/stocks/{id}`、`GET /items/stocks`、`GET /items/records` | 入库/出库数量、库存余额、库存记录审计。 |
| `addSubscribe` / `getSubscribe(s)` / `cancelSubscribe` | `POST /items/subscribe`、`GET /items/subscribe/{id}`、`GET /items/subscribe`、`DELETE /items/subscribe` | 订阅创建、重复订阅、取消订阅。 |

### API 测试流程

1. `POST /items/categories` 创建物资分类，查询列表和详情。
2. `POST /items/items` 创建物资，绑定分类。
3. `POST /items/stocks` 添加入库记录，验证 `GET /items/stocks/{id}` 库存余额。
4. 添加出库记录，验证库存不能扣成负数。
5. `PUT /items/donations` 创建捐赠草稿，上传并删除凭证。
6. `POST /items/donations` 创建正式捐赠，更新信息并推进状态。
7. `POST /items/subscribe` 创建库存订阅，查询并取消。
8. 删除物资和分类，验证被引用资源的删除限制或废弃标记。

## 7. 走失宠物认领功能

### 单元测试计划

| Service 方法 | 对应 API | 单元测试重点 |
| --- | --- | --- |
| `beginLostPet` / `uploadLostPetMedia` / `deleteLostPetMedia` | `PUT /lost/pets`、`PUT /lost/pets/{_id}/media`、`DELETE /lost/pets/{_id}/media/{name}` | 走失登记草稿、媒体上传和删除。 |
| `addLostPet` / `updateLostPet` / `getLostPet(s)` | `POST /lost/pets`、`PUT /lost/pets/{id}`、`GET /lost/pets/{id}`、`GET /lost/pets` | 走失信息创建、修改、分页和状态筛选。 |
| `getSimilarPets` / `markPetMismatch` | `GET /lost/pets/{id}/similar`、`POST /lost/pets/{id}/mismatch/{pid}` | 相似宠物匹配、误匹配标记后不再推荐。 |
| `addClaim` / `getClaim(s)` / `cancelClaim` / `approveClaim` | `POST /lost/claim`、`GET /lost/claim/{id}`、`GET /lost/claim`、`DELETE /lost/claim/{id}`、`PATCH /lost/claim/{id}/approve` | 认领申请、取消、审批和宠物状态联动。 |
| `listMatchedPets` / `listMatchedLostPets` | 当前为 Service 内部能力 | 匹配查询应排除已标记不匹配、已关闭或已认领数据。 |

### API 测试流程

1. `PUT /lost/pets` 创建走失登记草稿，上传宠物照片。
2. `POST /lost/pets` 提交走失信息，记录 `lostPetId`。
3. `GET /lost/pets/{lostPetId}` 和 `GET /lost/pets` 验证详情与列表。
4. `GET /lost/pets/{lostPetId}/similar` 查询相似宠物候选。
5. `POST /lost/pets/{lostPetId}/mismatch/{petId}` 标记误匹配，再次查询候选确认排除。
6. `POST /lost/claim` 创建认领申请。
7. `GET /lost/claim/{id}` 和 `GET /lost/claim` 验证申请。
8. 分别测试 `DELETE /lost/claim/{id}` 取消和 `PATCH /lost/claim/{id}/approve` 审批通过路径。

## 8. 志愿者管理功能

### 单元测试计划

| Service 方法 | 对应 API | 单元测试重点 |
| --- | --- | --- |
| `addRecruitment` / `getRecruitment(s)` / `updateRecruitment` / `updateRecruitmentStatus` | `POST /volunteers/recruitments`、`GET /volunteers/recruitments/{id}`、`GET /volunteers/recruitments`、`PUT /volunteers/recruitments/{id}`、`PATCH /volunteers/recruitments/{id}/{st}` | 招募发布、修改、上下架状态。 |
| `addApplication` / `getApplication(s)` / `setApplicationStatus` | `POST /volunteers/applications`、`GET /volunteers/applications`、`GET /volunteers/applications/{id}`、`PATCH /volunteers/applications/{id}` | 报名申请、重复报名、审批后生成或更新志愿者档案。 |
| `getProfile(s)` / `updateProfile` / `updateProfileStatus` | `GET /volunteers/profiles`、`GET /volunteers/profiles/{id}`、`PUT /volunteers/profiles/{id}`、`POST /volunteers/profiles/{id}/{st}` | 档案查询、修改、启停用。 |
| `addShift` / `getShift(s)` / `updateShift` / `updateShiftStatus` | `POST /volunteers/shifts`、`GET /volunteers/shifts`、`GET /volunteers/shifts/{id}`、`PUT /volunteers/shifts/{id}`、`PATCH /volunteers/shifts/{id}` | 排班创建、时间冲突、状态变更。 |
| `addServiceRecord` / `getServiceRecord(s)` / `updateServiceRecordStatus` | `POST /volunteers/shifts/{id}/records`、`GET /volunteers/records`、`GET /volunteers/records/{id}`、`PATCH /volunteers/records/{id}` | 服务记录、工时统计、审核状态。 |
| `addReward` / `getReward(s)` / `issueReward` | `POST /volunteers/rewards`、`GET /volunteers/rewards`、`GET /volunteers/rewards/{id}`、`POST /volunteers/rewards/{id}/issue` | 奖励创建、发放、重复发放控制。 |

### API 测试流程

1. `POST /volunteers/recruitments` 发布招募，查询列表和详情。
2. `PUT /volunteers/recruitments/{id}` 修改招募内容，`PATCH /volunteers/recruitments/{id}/{st}` 上下架。
3. 普通用户 `POST /volunteers/applications` 报名，管理员查询并审批。
4. 审批通过后 `GET /volunteers/profiles/{id}` 验证档案生成，修改档案并切换状态。
5. `POST /volunteers/shifts` 创建排班，验证时间冲突和列表筛选。
6. `POST /volunteers/shifts/{id}/records` 添加服务记录，审核记录状态。
7. `POST /volunteers/rewards` 创建奖励，`POST /volunteers/rewards/{id}/issue` 发放并验证不可重复发放。

## 9. 公益宣传功能

### 单元测试计划

| Service 方法 | 对应 API | 单元测试重点 |
| --- | --- | --- |
| `addArticle` / `getArticles` / `getArticle` | `POST /publicity/articles`、`GET /publicity/articles`、`GET /publicity/articles/{id}` | 文章创建、分页、分类/状态筛选、阅读量。 |
| `updateArticle` / `deleteArticle` / `updateArticleStatus` | `PUT /publicity/articles/{id}`、`DELETE /publicity/articles/{id}`、`PATCH /publicity/articles/{id}/{st}` | 编辑权限、删除或废弃、发布/下架状态。 |
| `likeArticle` | `POST /publicity/articles/{id}/like`、`DELETE /publicity/articles/{id}/like` | 点赞幂等、取消点赞、点赞数一致。 |
| `favoriteArticle` / `getFavorites` | `POST /publicity/articles/{id}/favorite`、`DELETE /publicity/articles/{id}/favorite`、`GET /publicity/favorites` | 收藏幂等、取消收藏、收藏列表分页。 |
| `shareArticle` | `POST /publicity/articles/{id}/share` | 分享计数、分享渠道记录。 |

### API 测试流程

1. 管理员 `POST /publicity/articles` 创建文章，默认草稿或待发布状态。
2. `GET /publicity/articles/{id}` 验证详情，`GET /publicity/articles` 验证列表筛选。
3. `PUT /publicity/articles/{id}` 修改标题、正文、封面和标签。
4. `PATCH /publicity/articles/{id}/{st}` 发布文章，普通用户再次查询应可见。
5. 普通用户依次点赞、取消点赞、收藏、取消收藏，验证计数和个人收藏列表。
6. `POST /publicity/articles/{id}/share` 记录分享，验证分享次数。
7. `DELETE /publicity/articles/{id}` 删除或下架，验证列表不可见或状态正确。

## 模块测试执行顺序

1. 用户管理功能：建立测试账号、Token 和权限基线。
2. 流浪宠物信息管理功能：建立后续救助、医疗、领养、走失模块需要的宠物数据。
3. 救助任务管理功能：验证任务状态流转和媒体上传。
4. 医疗护理管理功能：基于宠物数据验证病历、免疫、康复和评估。
5. 领养与寄养管理功能：基于待领养宠物验证申请、协议、回访。
6. 物资与捐赠管理功能：独立验证分类、物资、库存、捐赠和订阅。
7. 走失宠物认领功能：基于走失登记和宠物匹配验证认领闭环。
8. 志愿者管理功能：验证招募、报名、档案、排班、服务记录和奖励。
9. 公益宣传功能：验证文章发布、互动和分享。

## 自动化测试落地建议

1. 单元测试按 Service 拆分为 `UserServiceTest`、`PetServiceTest`、`RescueTaskServiceTest` 等，使用 Mockito Mock Mapper、Redis、文件服务和认证上下文。
2. API 测试按模块拆分为 `*ApiTest`，优先使用 `@SpringBootTest(webEnvironment = RANDOM_PORT)` 或 MockMvc；涉及真实 MySQL 的用例固定使用测试数据前缀 `codex_`，便于清理。
3. 每个模块至少覆盖：成功路径、参数校验失败、资源不存在、权限不足、状态非法流转、分页查询。
4. 每次发现数据库、OpenAPI 或后端实现不一致时，先定位 Controller/Service 当前行为，再修正 `init.sql` 或 `openapi.yml`，最后新增或更新测试用例锁定行为。
