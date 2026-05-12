# Backend commits since 00186d90

检查范围：`00186d90e710e63b318d999f22c11d14e4173144^..HEAD`，即包含指定提交本身到当前 `HEAD`。

过滤规则：排除提交信息以 `+` 开头的提交；仅保留修改过 `backend/` 路径的提交。指定起点提交 `00186d90 +删除无用字段` 已按规则排除。

整理时间：2026-05-12

## 汇总

共发现 20 个符合条件的后端相关提交，主要集中在医疗护理、领养寄养、回访任务、志愿者、走失宠物认领和站内信模块。

## 提交明细

| 提交 | 日期 | 提交信息 | 后端影响 |
| --- | --- | --- | --- |
| `82f8ac6` | 2026-05-10 | Add medical case actions | 病例响应增加动作展示所需字段。 |
| `f9abe56` | 2026-05-10 | Expand medical detail workflows | 扩展病例详情工作流，涉及检查、检查诊断、治疗计划接口与服务逻辑。 |
| `3ae00f1` | 2026-05-10 | Add preventive medical pages | 增加疫苗、驱虫相关后端选项 DTO 和医疗服务接口支持。 |
| `361178f` | 2026-05-10 | Add rehab care workflow | 增加康复计划请求、查询参数和医疗服务逻辑。 |
| `afd5b71` | 2026-05-10 | Add rehab prescription orders | 康复计划响应补充处方订单，新增订单查询支持。 |
| `ccc2c2d` | 2026-05-11 | Add adoption agreement management | 领养协议响应和服务逻辑增加协议管理字段与处理。 |
| `8eeb0e8` | 2026-05-11 | Add adoption agreement signing | 增加协议签署相关服务逻辑。 |
| `f0890ef` | 2026-05-11 | Implement agreement signature confirmation | 协议签名增加待确认状态、更新类型、Mapper、Controller、服务、测试和消息文案，并调整初始化 SQL。 |
| `6d6ca79` | 2026-05-11 | Add agreement status length migration | 新增协议状态字段长度迁移 SQL。 |
| `4dd5605` | 2026-05-11 | Add breeding management entry and agreement pet names | 协议响应补充宠物名，领养寄养 facade 支持寄养管理入口数据。 |
| `1f28af3` | 2026-05-11 | Add adoption follow record pages | 增加回访记录响应、Controller、facade 与服务支持。 |
| `b0d71fe` | 2026-05-12 | Sync volunteer profile status on role change | 用户角色变更时同步志愿者档案状态。 |
| `89fd6d7` | 2026-05-12 | Show follow tasks in admin console | 后台回访任务增加响应字段、Controller、facade 和服务查询支持。 |
| `2df4676` | 2026-05-12 | Add notified follow task actions | 回访任务通知后增加同意、拒绝、撤销、修改、执行等服务端状态操作。 |
| `9f81cca` | 2026-05-12 | Fix follow task approval validation | 修正回访任务更新请求校验，避免志愿者同意时参数校验失败。 |
| `b94dd4d` | 2026-05-12 | Add volunteer shift rejection action | 志愿活动排班增加拒绝动作服务逻辑。 |
| `e1ec82c` | 2026-05-12 | Complete in-progress follow tasks | 回访任务完成时同步处理进行中的关联流程。 |
| `edcf2a4` | 2026-05-12 | Lock active volunteer shifts from editing | 已完成或执行中的志愿排班禁止编辑。 |
| `64842e3` | 2026-05-12 | Implement lost pet claim workflow | 走失宠物认领增加查询参数、响应 DTO、Mapper 与服务支持。 |
| `2f4e4c8` | 2026-05-12 | 实现站内信通知功能 | 增加站内信发送接口、请求 DTO、来源枚举和通知服务逻辑。 |

## 关联文件

### 医疗护理

- `backend/src/main/java/com/example/backend/controller/MedicalCareController.java`
- `backend/src/main/java/com/example/backend/dto/DewormerOptionResponse.java`
- `backend/src/main/java/com/example/backend/dto/ExaminationResponse.java`
- `backend/src/main/java/com/example/backend/dto/MedicalDetailResponse.java`
- `backend/src/main/java/com/example/backend/dto/RehabPlanAddRequest.java`
- `backend/src/main/java/com/example/backend/dto/RehabPlanQueryParams.java`
- `backend/src/main/java/com/example/backend/dto/RehabPlanResponse.java`
- `backend/src/main/java/com/example/backend/dto/TreatmentPlanAddRequest.java`
- `backend/src/main/java/com/example/backend/dto/VaccineOptionResponse.java`
- `backend/src/main/java/com/example/backend/mapper/OrderMapper.java`
- `backend/src/main/java/com/example/backend/service/MedicalService.java`

### 领养、寄养、协议与回访

- `backend/sql/20260511_agreement_status_length.sql`
- `backend/sql/init.sql`
- `backend/src/main/java/com/example/backend/controller/AdoptBreadingController.java`
- `backend/src/main/java/com/example/backend/dto/AgreementResponse.java`
- `backend/src/main/java/com/example/backend/dto/FollowRecordResponse.java`
- `backend/src/main/java/com/example/backend/dto/FollowTaskResponse.java`
- `backend/src/main/java/com/example/backend/dto/FollowTaskUpdateRequest.java`
- `backend/src/main/java/com/example/backend/entity/property/AdoptBreadingStatus.java`
- `backend/src/main/java/com/example/backend/entity/property/AgreementUpdateType.java`
- `backend/src/main/java/com/example/backend/facade/AdoptBreadingFacade.java`
- `backend/src/main/java/com/example/backend/mapper/AgreementMapper.java`
- `backend/src/main/java/com/example/backend/service/AdoptBreadingService.java`
- `backend/src/main/resources/messages.properties`
- `backend/src/test/java/com/example/backend/service/AdoptBreadingServiceUnitTest.java`

### 志愿者

- `backend/src/main/java/com/example/backend/service/UserService.java`
- `backend/src/main/java/com/example/backend/service/VolunteerService.java`

### 走失宠物认领

- `backend/src/main/java/com/example/backend/dto/ClaimQueryParams.java`
- `backend/src/main/java/com/example/backend/dto/LostPetClaimResponse.java`
- `backend/src/main/java/com/example/backend/mapper/LostPetClaimMapper.java`
- `backend/src/main/java/com/example/backend/service/LostPetService.java`

### 站内信

- `backend/src/main/java/com/example/backend/controller/NoticeController.java`
- `backend/src/main/java/com/example/backend/dto/NoticeSendRequest.java`
- `backend/src/main/java/com/example/backend/entity/property/NoticeSource.java`
- `backend/src/main/java/com/example/backend/service/NoticeService.java`
