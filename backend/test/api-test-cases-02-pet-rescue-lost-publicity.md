# API 测试用例 02：宠物、救助、走失宠物、宣传文章

## PetController

| 编号 | 接口 | 目的 | 测试数据 | 返回值 | 预期结果 |
|---|---|---|---|---|---|
| PET-01 | `POST /pets` | 新增流浪宠物 | workerUser，name/type/breed/sex/age/status | `Result<PetAddResponse>` | 创建成功，返回 petId |
| PET-02 | `POST /pets/{id}/location` | 新增宠物位置 | petId，province/city/district/detailAddress | `Result<Void>` | 位置写入成功 |
| PET-03 | `GET /pets` | 查询宠物列表 | status/type/breed/page/size | `Result<Page<PetResponse>>` | 返回分页，包含封面 assetUrl |
| PET-04 | `GET /pets/{id}` | 获取宠物详情 | petId | `Result<PetResponse>` | 返回宠物、位置、媒体、标签等详情 |
| PET-05 | `PUT /pets/{id}` | 修改宠物信息 | name/age/health/description | `Result<PetResponse>` | 字段更新成功 |
| PET-06 | `POST /pets/{id}/media` | 上传宠物媒体 | multipart `file=@IMG_PET_COVER`，isCover=true | `Result<PetMediaResponse>` | 返回 mediaId、assetUrl；assetUrl 可访问 |
| PET-07 | `PUT /pets/{id}/media/{mid}` | 修改媒体信息 | name/description/isCover | `Result<PetMediaResponse>` | 媒体元数据更新，封面状态符合预期 |
| PET-08 | `DELETE /pets/{id}/media/{mid}` | 删除媒体 | petId/mediaId | `Result<Void>` | 删除成功，再查详情不含该媒体 |
| PET-09 | `POST /pets/{id}/tags` | 添加宠物标签 | tags=`["亲人","已驱虫"]` | `Result<List<PetTagResponse>>` | 标签保存成功 |
| PET-10 | `DELETE /pets/{id}/tags` | 删除宠物标签 | ids 或 tag names | `Result<Void>` | 标签删除成功 |
| PET-11 | `PUT /pets/{id}/status` | 更新宠物状态 | status=`SHELTERED/HEALTH/HOME`，reason | `Result<PetResponse>` | 状态更新并生成状态记录 |
| PET-12 | `GET /pets/{id}/status` | 查询状态记录 | petId | `Result<List<PetStatusRecordResponse>>` | 返回状态变更历史 |
| PET-13 | `DELETE /pets/{id}` | 废弃宠物记录 | workerUser 删除 petId | `Result<Void>` | `isDiscard=true`；普通用户执行返回无权限 |

## RescueTaskController

| 编号 | 接口 | 目的 | 测试数据 | 返回值 | 预期结果 |
|---|---|---|---|---|---|
| RESCUE-01 | `PUT /tasks` | 准备救助任务上传 | normalUser token | `Result<String>` | 返回 uuid |
| RESCUE-02 | `POST /tasks/{_id}/uploads` | 上传救助任务临时图片 | `_id=uuid`，`file=@IMG_RESCUE` | `Result<String>` | 返回临时文件名 |
| RESCUE-03 | `DELETE /tasks/{_id}/uploads/{file}` | 删除临时救助图片 | uuid + filename | `Result<Void>` | 删除成功；重新上传后继续后续测试 |
| RESCUE-04 | `POST /tasks` | 创建救助任务 | uuid、summary、description、type、location | `Result<RescueTaskResponse>` | 创建成功，状态为 CREATED |
| RESCUE-05 | `GET /tasks` | 查询救助任务 | status/type/page/size | `Result<Page<RescueTaskResponse>>` | 返回任务分页 |
| RESCUE-06 | `GET /tasks/{id}` | 获取救助任务详情 | taskId | `Result<RescueTaskResponse>` | 创建人或 worker 可查看，其他普通用户无权限 |
| RESCUE-07 | `PUT /tasks/{id}` | 修改救助任务 | summary/description/type | `Result<RescueTaskResponse>` | 创建人或 worker 可改，字段更新 |
| RESCUE-08 | `GET /tasks/{id}/status` | 查询状态记录 | taskId | `Result<RescueTaskRecordsResponse>` | 返回状态记录 |
| RESCUE-09 | `PATCH /tasks/{id}/status` | 更新任务状态 | workerUser，status=`APPROVED/PROCESSING/COMPLETED` | `Result<RescueTaskResponse>` | 合法状态流转成功，非法流转返回 422 |
| RESCUE-10 | `POST /tasks/{id}/assign` | 分配救助任务 | volunteerId/workerId | `Result<RescueTaskResponse>` | 分配成功并记录负责人 |
| RESCUE-11 | `DELETE /tasks/{id}/media/{mid}` | 删除救助任务媒体 | taskId/mediaId | `Result<Void>` | 媒体删除成功 |
| RESCUE-12 | `DELETE /tasks/{id}` | 删除救助任务 | workerUser | `Result<Void>` | 删除成功；普通用户无权限 |

## LostPetController

| 编号 | 接口 | 目的 | 测试数据 | 返回值 | 预期结果 |
|---|---|---|---|---|---|
| LOST-01 | `PUT /lost/pets` | 准备走失宠物报备 | normalUser token | `Result<String>` | 返回 uuid |
| LOST-02 | `PUT /lost/pets/{_id}/media` | 上传走失宠物临时图片 | `_id=uuid`，`file=@IMG_LOST_PET` | `Result<String>` | 返回临时文件名 |
| LOST-03 | `DELETE /lost/pets/{_id}/media/{name}` | 删除走失宠物临时图片 | uuid + filename | `Result<Void>` | 删除成功；重新上传后继续后续测试 |
| LOST-04 | `POST /lost/pets` | 报备走失宠物 | uuid、name/type/breed/sex/lostTime/location/phone | `Result<LostPetResponse>` | 创建成功，状态 SEARCHING，返回相似宠物列表 |
| LOST-05 | `PUT /lost/pets/{id}` | 修改走失宠物 | lostPetId，描述/联系方式/位置 | `Result<LostPetResponse>` | 本人或 worker 可改 |
| LOST-06 | `GET /lost/pets/{id}/similar` | 查询相似流浪宠物 | lostPetId | `Result<List<PetResponse>>` | 返回匹配宠物；关闭状态返回空 |
| LOST-07 | `POST /lost/pets/{id}/mismatch/{pid}` | 标记不匹配宠物 | lostPetId + petId | `Result<List<PetResponse>>` | 标记后相似列表不再包含该宠物 |
| LOST-08 | `GET /lost/pets/{id}` | 获取走失宠物详情 | lostPetId | `Result<LostPetResponse>` | 本人或 worker 可查看 |
| LOST-09 | `GET /lost/pets` | 查询走失宠物列表 | owner/status/type/breed/location/page | `Result<Page<LostPetResponse>>` | 返回分页 |
| LOST-10 | `POST /lost/claim` | 发起认领申请 | lostPetId、reason、contact | `Result<LostPetClaimResponse>` | SEARCHING 状态可申请 |
| LOST-11 | `GET /lost/claim/{id}` | 获取认领申请 | claimId | `Result<LostPetClaimResponse>` | 返回申请人与走失宠物信息 |
| LOST-12 | `GET /lost/claim` | 查询认领申请 | applicant/status/page | `Result<Page<LostPetClaimResponse>>` | 返回分页 |
| LOST-13 | `DELETE /lost/claim/{id}` | 取消认领申请 | pending claimId | `Result<Void>` | 申请人或 worker 可取消，非 PENDING 返回 422 |
| LOST-14 | `PATCH /lost/claim/{id}/approve` | 审核认领申请 | workerUser，status=`PASS/REJECT`，petId/reason | `Result<LostPetClaimResponse>` | PASS 后走失宠物 CLAIMED，关联宠物 HOME |

## PublicityController

| 编号 | 接口 | 目的 | 测试数据 | 返回值 | 预期结果 |
|---|---|---|---|---|---|
| PUB-01 | `POST /publicity/articles` | 新增宣传文章 | workerUser，title/content/type/status | `Result<ArticleResponse>` | 创建成功，初始 DRAFT 或指定状态 |
| PUB-02 | `GET /publicity/articles` | 查询文章列表 | type/status/page | `Result<Page<ArticleResponse>>` | 返回分页 |
| PUB-03 | `GET /publicity/articles/{id}` | 获取文章详情 | articleId | `Result<ArticleResponse>` | 返回正文、点赞/收藏/分享统计 |
| PUB-04 | `PUT /publicity/articles/{id}` | 修改文章 | title/content/type | `Result<ArticleResponse>` | 字段更新成功 |
| PUB-05 | `PATCH /publicity/articles/{id}/{st}` | 修改文章状态 | st=`PUBLISHED/DRAFT/ARCHIVED` | `Result<ArticleResponse>` | 合法状态更新 |
| PUB-06 | `POST /publicity/articles/{id}/like` | 点赞文章 | normalUser + published article | `Result<ArticleLikeResponse>` | 点赞数加一，重复点赞应受控 |
| PUB-07 | `DELETE /publicity/articles/{id}/like` | 取消点赞 | normalUser | `Result<Void>` | 点赞数减少 |
| PUB-08 | `POST /publicity/articles/{id}/favorite` | 收藏文章 | normalUser | `Result<ArticleLikeResponse>` | 收藏成功 |
| PUB-09 | `DELETE /publicity/articles/{id}/favorite` | 取消收藏 | normalUser | `Result<Void>` | 收藏取消 |
| PUB-10 | `GET /publicity/favorites` | 查询我的收藏 | normalUser | `Result<Page<ArticleResponse>>` | 返回当前用户收藏列表 |
| PUB-11 | `POST /publicity/articles/{id}/share` | 分享文章计数 | published article | `Result<ArticleShareResponse>` | 分享数增加；草稿不可分享 |
| PUB-12 | `DELETE /publicity/articles/{id}` | 删除文章 | workerUser | `Result<Void>` | 删除成功；普通用户无权限 |
