# API 测试用例 04：领养寄养、志愿者

## AdoptBreadingController：领养、寄养、协议

| 编号 | 接口 | 目的 | 测试数据 | 返回值 | 预期结果 |
|---|---|---|---|---|---|
| ADOPT-01 | `POST /adopt/adopt` | 创建领养申请 | normalUser，petId=可领养宠物，联系方式、地址、要求 | `Result<AdoptResponse>` | 创建成功，状态 CREATE |
| ADOPT-02 | `GET /adopt/adopt/{id}` | 获取领养申请 | adoptId | `Result<AdoptResponse>` | 返回申请详情 |
| ADOPT-03 | `GET /adopt/adopt` | 查询领养申请 | pet/user/status/page | `Result<Page<AdoptResponse>>` | 返回分页 |
| ADOPT-04 | `PATCH /adopt/adopt/{id}/{st}` | 审核领养申请 | workerUser，st=`PASS/REJECT/CANCEL` | `Result<AdoptResponse>` | 合法状态更新；直接 `AGREEMENT_SIGNED` 返回 422 |
| ADOPT-05 | `POST /adopt/breading` | 创建寄养申请 | normalUser，pet info、time range、联系方式 | `Result<BreadingResponse>` | 创建成功，状态 CREATE |
| ADOPT-06 | `GET /adopt/breading/{id}` | 获取寄养申请 | breadingId | `Result<BreadingResponse>` | 返回详情 |
| ADOPT-07 | `GET /adopt/breading` | 查询寄养申请 | user/status/page | `Result<Page<BreadingResponse>>` | 返回分页 |
| ADOPT-08 | `PATCH /adopt/breading/{id}/{st}` | 审核寄养申请 | workerUser，st=`PASS/REJECT` | `Result<BreadingResponse>` | 合法状态更新 |
| ADOPT-09 | `PUT /adopt/agreement` | 准备协议上传 | workerUser | `Result<String>` | 返回 uuid |
| ADOPT-10 | `POST /adopt/agreement/upload/{_id}` | 上传协议扫描件临时文件 | uuid，`file=@IMG_AGREEMENT` | `Result<String>` | 返回临时文件名 |
| ADOPT-11 | `DELETE /adopt/agreement/upload/{_id}/{name}` | 删除协议临时文件 | uuid + filename | `Result<Void>` | 删除成功；重新上传后继续 |
| ADOPT-12 | `POST /adopt/agreement` | 创建协议 | uuid、parentType=`ADOPT/BREADING`、parentId、type=`PAPER/ELECTRONIC`、fileOrder/content | `Result<AgreementResponse>` | parent 需 PASS；创建后 parent 状态 AGREEMENT_DRAFT |
| ADOPT-13 | `PUT /adopt/agreement/{id}` | 修改电子协议 | agreementId，content | `Result<AgreementResponse>` | 记录更新历史，返回新内容 |
| ADOPT-14 | `POST /adopt/agreement/{id}/files` | 为纸质协议追加扫描件 | page、file=@IMG_AGREEMENT | `Result<List<AgreementFileResponse>>` | 文件插入指定页码 |
| ADOPT-15 | `DELETE /adopt/agreement/{id}/files/{fid}` | 删除协议扫描件 | agreementId/fileId | `Result<List<AgreementFileResponse>>` | 删除后页码重新排序 |
| ADOPT-16 | `PUT /adopt/agreement/{id}/files/order` | 调整协议文件顺序 | fileOrder=`[fileId...]` | `Result<List<AgreementFileResponse>>` | 页码按顺序更新 |
| ADOPT-17 | `PATCH /adopt/agreement/{id}/sign` | 签署协议 | sign=@IMG_AGREEMENT | `Result<AgreementResponse>` | 协议签署，parent 状态 AGREEMENT_SIGNED |
| ADOPT-18 | `GET /adopt/agreement/{id}` | 获取协议 | agreementId | `Result<AgreementResponse>` | 返回协议与文件 assetUrl |
| ADOPT-19 | `GET /adopt/agreement` | 查询协议 | parent/type/page | `Result<Page<AgreementResponse>>` | 返回分页 |

## AdoptBreadingController：回访

| 编号 | 接口 | 目的 | 测试数据 | 返回值 | 预期结果 |
|---|---|---|---|---|---|
| FOLLOW-01 | `POST /adopt/follow/adopt/{id}` | 创建领养回访任务 | adoptId 状态 TRACKING，volunteerId、time、content | `Result<FollowTaskResponse>` | worker 可创建，志愿者必须有 VOLUNTEER 权限 |
| FOLLOW-02 | `PUT /adopt/follow/{id}` | 修改回访任务 | taskId，time/status/volunteer/worker | `Result<FollowTaskResponse>` | worker 可全量改；志愿者/领养人仅可改允许字段 |
| FOLLOW-03 | `GET /adopt/follow/{id}` | 获取回访任务 | taskId | `Result<FollowTaskResponse>` | 返回任务详情 |
| FOLLOW-04 | `GET /adopt/follow` | 查询回访任务 | adopt/worker/volunteer/status/page | `Result<Page<FollowTaskResponse>>` | 返回分页 |
| FOLLOW-05 | `POST /adopt/follow/{id}/record` | 创建回访记录 | taskId 状态 IN_PROGRESS，content/result | `Result<FollowRecordResponse>` | worker 或负责志愿者可创建；同任务重复创建返回 409 |
| FOLLOW-06 | `GET /adopt/follow/{id}/record` | 查询某任务回访记录 | taskId/page | `Result<Page<FollowRecordResponse>>` | 返回该任务记录 |
| FOLLOW-07 | `GET /adopt/follow/record` | 查询全部回访记录 | volunteer/adopt/page | `Result<Page<FollowRecordResponse>>` | 返回分页 |

## VolunteerController：招募、申请、档案

| 编号 | 接口 | 目的 | 测试数据 | 返回值 | 预期结果 |
|---|---|---|---|---|---|
| VOL-01 | `POST /volunteers/recruitments` | 创建志愿者招募 | workerUser，title/description/time/location/limit | `Result<VolunteerRecruitmentResponse>` | 创建成功，状态 OPEN/DRAFT |
| VOL-02 | `GET /volunteers/recruitments/{id}` | 获取招募详情 | recruitmentId | `Result<VolunteerRecruitmentResponse>` | 返回详情 |
| VOL-03 | `GET /volunteers/recruitments` | 查询招募 | status/time/page | `Result<Page<VolunteerRecruitmentResponse>>` | 返回分页 |
| VOL-04 | `PUT /volunteers/recruitments/{id}` | 修改招募 | title/time/location/limit | `Result<VolunteerRecruitmentResponse>` | 更新成功 |
| VOL-05 | `PATCH /volunteers/recruitments/{id}/{st}` | 修改招募状态 | st=`OPEN/CLOSED/CANCELLED` | `Result<VolunteerRecruitmentResponse>` | 合法状态更新 |
| VOL-06 | `POST /volunteers/applications` | 提交志愿者申请 | recruitmentId、reason、skills | `Result<VolunteerApplicationResponse>` | 普通用户可申请，状态 SUBMITTED |
| VOL-07 | `GET /volunteers/applications` | 查询申请 | recruitment/user/status/page | `Result<Page<VolunteerApplicationResponse>>` | 返回分页 |
| VOL-08 | `GET /volunteers/applications/{id}` | 获取申请详情 | applicationId | `Result<VolunteerApplicationResponse>` | 返回详情 |
| VOL-09 | `PATCH /volunteers/applications/{id}` | 审核申请 | workerUser，status=`UNDER_REVIEW/APPROVED/REJECTED/CANCELED` | `Result<VolunteerApplicationResponse>` | 合法状态流转成功；非法流转 422 |
| VOL-10 | `GET /volunteers/profiles` | 查询志愿者档案 | status/page | `Result<Page<VolunteerProfileResponse>>` | 返回分页 |
| VOL-11 | `GET /volunteers/profiles/{id}` | 获取档案 | profileId/userId | `Result<VolunteerProfileResponse>` | 返回志愿者信息 |
| VOL-12 | `PUT /volunteers/profiles/{id}` | 修改档案 | skills/availableTime/contact | `Result<VolunteerProfileResponse>` | 本人或 worker 可改 |
| VOL-13 | `POST /volunteers/profiles/{id}/{st}` | 修改档案状态 | st=`ACTIVE/SUSPENDED/...` | `Result<VolunteerProfileResponse>` | worker 可改，状态更新 |

## VolunteerController：排班、服务记录、奖励

| 编号 | 接口 | 目的 | 测试数据 | 返回值 | 预期结果 |
|---|---|---|---|---|---|
| SHIFT-01 | `POST /volunteers/shifts` | 创建志愿排班 | workerUser，volunteerId/task/time/location | `Result<VolunteerShiftResponse>` | 创建成功，状态 ASSIGNED |
| SHIFT-02 | `GET /volunteers/shifts` | 查询排班 | volunteer/status/time/page | `Result<Page<VolunteerShiftResponse>>` | 返回分页 |
| SHIFT-03 | `GET /volunteers/shifts/{id}` | 获取排班详情 | shiftId | `Result<VolunteerShiftResponse>` | 返回详情 |
| SHIFT-04 | `PUT /volunteers/shifts/{id}` | 修改排班 | time/location/task | `Result<VolunteerShiftResponse>` | worker 可改 |
| SHIFT-05 | `PATCH /volunteers/shifts/{id}` | 更新排班状态 | status=`CONFIRMED/IN_PROGRESS/COMPLETED/CANCELLED/ABSENT` | `Result<VolunteerShiftResponse>` | 合法状态流转成功；非法流转 422 |
| SHIFT-06 | `POST /volunteers/shifts/{id}/records` | 添加志愿服务记录 | shiftId、content/hours | `Result<VolunteerServiceRecordResponse>` | 完成服务后可添加记录 |
| SHIFT-07 | `GET /volunteers/records` | 查询服务记录 | volunteer/status/page | `Result<Page<VolunteerServiceRecordResponse>>` | 返回分页 |
| SHIFT-08 | `PATCH /volunteers/records/{id}` | 审核服务记录 | status/reason | `Result<VolunteerServiceRecordResponse>` | worker 审核通过/拒绝 |
| SHIFT-09 | `GET /volunteers/records/{id}` | 获取服务记录 | recordId | `Result<VolunteerServiceRecordResponse>` | 返回详情 |
| REWARD-01 | `POST /volunteers/rewards` | 创建志愿者奖励 | volunteerId、type、score、amount、reason | `Result<VolunteerRewardResponse>` | worker 创建 PENDING 奖励 |
| REWARD-02 | `GET /volunteers/rewards` | 查询奖励 | volunteer/status/type/page | `Result<Page<VolunteerRewardResponse>>` | 返回分页 |
| REWARD-03 | `GET /volunteers/rewards/{id}` | 获取奖励详情 | rewardId | `Result<VolunteerRewardResponse>` | 返回奖励详情 |
| REWARD-04 | `POST /volunteers/rewards/{id}/issue` | 发放奖励 | pending rewardId | `Result<VolunteerRewardResponse>` | 状态变 ISSUED，重复发放返回 422 |
