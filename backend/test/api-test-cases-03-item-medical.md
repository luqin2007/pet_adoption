# API 测试用例 03：物资捐赠、医疗

## ItemDonationController：捐赠

| 编号 | 接口 | 目的 | 测试数据 | 返回值 | 预期结果 |
|---|---|---|---|---|---|
| DON-01 | `PUT /items/donations` | 准备捐赠上传 | normalUser token | `Result<String>` | 返回 uuid，Redis 中绑定当前用户 id |
| DON-02 | `POST /items/donations/upload/{_id}` | 上传捐赠凭证 | `_id=uuid`，`file=@IMG_DONATION` | `Result<String>` | 返回临时文件名 |
| DON-03 | `DELETE /items/donations/upload/{_id}/{name}` | 删除捐赠临时凭证 | uuid + filename | `Result<Void>` | 删除成功；重新上传后继续后续测试 |
| DON-04 | `POST /items/donations` | 确认物资捐赠 | uuid、delivery、description、items | `Result<DonationResponse>` | 创建成功，状态 CREATED，用户获得 DONOR 权限 |
| DON-05 | `PUT /items/donations/{id}` | 修改捐赠内容 | donationId、items/description | `Result<DonationResponse>` | CREATED 状态本人可改，非 CREATED 仅 worker 可改 |
| DON-06 | `PATCH /items/donations/{id}/status` | 修改捐赠状态 | workerUser，CREATED -> PENDING -> TRANSFERRING -> RECEIVED -> STOCKED | `Result<DonationResponse>` | 合法状态流转成功并生成状态记录 |
| DON-07 | `PATCH /items/donations/{id}/status` | 非法状态流转 | STOCKED -> PENDING | `Result` | 返回 422 |
| DON-08 | `PATCH /items/donations/{id}/status` | 用户关闭退回捐赠 | BACKING -> CLOSED，donor 本人 | `Result<DonationResponse>` | 本人可关闭退回流程 |
| DON-09 | `GET /items/donations/{id}` | 获取捐赠详情 | donationId | `Result<DonationResponse>` | 返回捐赠、物资、文件、状态记录 |
| DON-10 | `GET /items/donations` | 查询捐赠列表 | user/date/page | `Result<Page<DonationResponse>>` | 返回分页 |

## ItemDonationController：物资、分类、库存、订阅

| 编号 | 接口 | 目的 | 测试数据 | 返回值 | 预期结果 |
|---|---|---|---|---|---|
| ITEM-01 | `POST /items/categories` | 新增物资分类 | workerUser，name=`猫粮` | `Result<Category>` | 创建成功 |
| ITEM-02 | `PUT /items/categories/{id}` | 修改分类 | categoryId，name/description | `Result<Category>` | 更新成功 |
| ITEM-03 | `GET /items/categories/{id}` | 获取分类 | categoryId | `Result<Category>` | 返回分类详情 |
| ITEM-04 | `GET /items/categories` | 查询分类 | name/page | `Result<Page<Category>>` | 返回分页 |
| ITEM-05 | `POST /items/items` | 新增物资 | workerUser，categoryId/name/unit/spec | `Result<ItemResponse>` | 创建成功 |
| ITEM-06 | `PUT /items/items/{id}` | 修改物资 | itemId，name/category/spec | `Result<ItemResponse>` | 更新成功 |
| ITEM-07 | `GET /items/items/{id}` | 获取物资 | itemId | `Result<ItemResponse>` | 返回物资详情 |
| ITEM-08 | `GET /items/items` | 查询物资 | category/name/page | `Result<Page<ItemResponse>>` | 返回分页 |
| ITEM-09 | `DELETE /items/items/{id}` | 废弃物资 | workerUser | `Result<Void>` | 废弃成功；普通用户无权限 |
| ITEM-10 | `DELETE /items/categories/{id}` | 删除空分类 | workerUser，未被物资引用的分类 | `Result<Void>` | 空分类可删除；非空分类返回 409 |
| ITEM-11 | `POST /items/stocks` | 入库登记 | worker/doctor，action=`IN`，itemId/count/expireTime/source | `Result<StockResponse>` | 新库存创建或库存数量增加 |
| ITEM-12 | `POST /items/stocks` | 出库登记 | action=`OUT`，id/count | `Result<StockResponse>` | 库存数量减少；不足返回 422 |
| ITEM-13 | `POST /items/stocks` | 销毁登记 | action=`DESTROY`，id/count | `Result<StockResponse>` | 库存数量减少，可销毁过期库存 |
| ITEM-14 | `GET /items/stocks/{id}` | 获取库存 | stockId | `Result<StockResponse>` | 返回库存详情 |
| ITEM-15 | `GET /items/stocks` | 查询库存 | source/item/count/page | `Result<Page<StockResponse>>` | worker 看全部，普通用户仅捐赠来源 |
| ITEM-16 | `GET /items/records` | 查询库存流水 | source/item/action/page | `Result<Page<StockRecordResponse>>` | 返回流水分页 |
| ITEM-17 | `POST /items/subscribe` | 添加库存预警 | itemId/threshold/contact | `Result<SubscribeResponse>` | 创建订阅成功 |
| ITEM-18 | `GET /items/subscribe/{id}` | 获取预警 | subscribeId | `Result<SubscribeResponse>` | 返回订阅详情 |
| ITEM-19 | `GET /items/subscribe` | 查询预警 | item/user/page | `Result<Page<SubscribeResponse>>` | 返回分页 |
| ITEM-20 | `DELETE /items/subscribe` | 取消预警 | ids=`[subscribeId]` | `Result<Void>` | worker 可删任意，普通用户仅删自己的 |

## MedicalCareController：初诊、病历、诊断、治疗、检查

| 编号 | 接口 | 目的 | 测试数据 | 返回值 | 预期结果 |
|---|---|---|---|---|---|
| MED-01 | `POST /medical/first` | 创建初诊登记 | doctor/volunteer，petId、体征、免疫史、过敏史 | `Result<FirstRegistrationResponse>` | 创建成功 |
| MED-02 | `GET /medical/first` | 查询初诊登记 | petId/registrar/page | `Result<Page<FirstRegistrationItemResponse>>` | 返回分页 |
| MED-03 | `GET /medical/first/{id}` | 获取初诊详情 | registrationId | `Result<FirstRegistrationResponse>` | 返回免疫史、过敏史、相关病历 |
| MED-04 | `POST /medical/record/pet/{id}` | 创建就诊记录 | doctorUser，petId、ownerId、petAge | `Result<MedicalRecordResponse>` | 创建成功，要求已有初诊 |
| MED-05 | `PUT /medical/record/{id}` | 修改就诊记录 | recordId，owner/petAge/description | `Result<MedicalRecordResponse>` | worker 或 doctor 可改 |
| MED-06 | `GET /medical/record/{id}` | 获取就诊记录 | recordId | `Result<MedicalRecordResponse>` | 返回宠物、医生、owner |
| MED-07 | `GET /medical/record` | 查询就诊记录 | pet/doctor/date/page | `Result<Page<MedicalRecordResponse>>` | 返回分页 |
| MED-08 | `POST /medical/detail` | 创建病历 | recordId、SOAP 字段 | `Result<MedicalDetailResponse>` | doctor 可创建，同 record 不可重复 |
| MED-09 | `GET /medical/detail/{id}` | 获取病历详情 | detailId | `Result<MedicalDetailResponse>` | 返回诊断、治疗、检查、标签 |
| MED-10 | `GET /medical/detail` | 查询病历 | record/pet/doctor/page | `Result<Page<MedicalDetailResponse>>` | 返回分页 |
| MED-11 | `PUT /medical/detail/{id}` | 修改病历 | SOAP 字段 | `Result<MedicalDetailResponse>` | 未完成且未废弃可改 |
| MED-12 | `PATCH /medical/detail/{id}` | 完成病历 | detailId | `Result<MedicalDetailResponse>` | `isCompleted=true`，之后不可再编辑 |
| MED-13 | `POST /medical/detail/{id}/diagnosis` | 添加诊断 | diagnosis、examinationIds | `Result<DiagnosisResponse>` | 病历开放时可添加 |
| MED-14 | `DELETE /medical/diagnosis/{dId}` | 废弃诊断 | diagnosisId | `Result<Void>` | 病历开放时可废弃 |
| MED-15 | `POST /medical/detail/{id}/plan` | 添加治疗计划 | treatment、orders | `Result<TreatmentPlanResponse>` | 创建治疗计划和医嘱 |
| MED-16 | `DELETE /medical/plan` | 废弃治疗计划 | ids=`[planId]` | `Result<Void>` | 病历开放时可废弃 |
| MED-17 | `PUT /medical/details/{id}/exam` | 准备检查上传 | detailId | `Result<String>` | 返回 uuid；已完成/废弃病历返回 422 |
| MED-18 | `PUT /medical/exam/{_id}/doc` | 上传检查文件 | uuid，`file=@IMG_EXAM` | `Result<String>` | 返回临时文件名 |
| MED-19 | `DELETE /medical/exam/{_id}/doc/{name}` | 删除检查临时文件 | uuid + filename | `Result<Void>` | 删除成功；重新上传后继续 |
| MED-20 | `POST /medical/exam/{_id}` | 创建检查记录 | uuid、examType、description | `Result<ExaminationResponse>` | 创建成功，文件转入正式目录 |
| MED-21 | `GET /medical/exam/{id}` | 获取检查记录 | examId | `Result<ExaminationResponse>` | 返回检查文件 assetUrl，可访问 |

## MedicalCareController：疫苗、驱虫、康复、健康评估

| 编号 | 接口 | 目的 | 测试数据 | 返回值 | 预期结果 |
|---|---|---|---|---|---|
| MED-22 | `POST /medical/vaccine/pet/{id}` | 添加疫苗记录 | petId、vaccineId、time、nextTime | `Result<VaccineResponse>` | doctor 可添加 |
| MED-23 | `GET /medical/vaccine/pet/{id}` | 查询疫苗记录 | petId | `Result<List<VaccineResponse>>` | 返回全部疫苗记录 |
| MED-24 | `GET /medical/vaccine/pet/{id}/new` | 查询每类最新疫苗 | petId | `Result<List<VaccineResponse>>` | 每种疫苗只返回最新一条 |
| MED-25 | `POST /medical/deworm/pet/{id}` | 添加驱虫记录 | petId、dewormerId、time | `Result<DewormResponse>` | doctor/worker 可添加 |
| MED-26 | `GET /medical/deworm/pet/{id}` | 查询驱虫记录 | petId | `Result<List<DewormResponse>>` | 返回驱虫历史 |
| MED-27 | `POST /medical/rehab/pet/{id}` | 添加康复计划 | petId、plan、orders | `Result<RehabPlanResponse>` | doctor 可添加，初始状态记录生成 |
| MED-28 | `GET /medical/rehab/{id}` | 获取康复计划 | planId | `Result<RehabPlanResponse>` | 返回计划、状态历史、宠物 |
| MED-29 | `GET /medical/rehab` | 查询康复计划 | pet/status/page | `Result<Page<RehabPlanResponse>>` | 返回分页 |
| MED-30 | `PUT /medical/rehab/{id}/status` | 更新康复计划状态 | status/reason | `Result<RehabPlanResponse>` | 生成状态记录，计划状态更新 |
| MED-31 | `POST /medical/rehab/{id}/record` | 添加康复记录 | multipart files=`@IMG_REHAB`，description | `Result<RehabRecordResponse>` | doctor/volunteer 可添加，文件 assetUrl 可访问 |
| MED-32 | `GET /medical/rehab/{id}/record` | 获取康复记录 | planId | `Result<List<RehabRecordResponse>>` | 返回记录及文件 |
| MED-33 | `POST /medical/health/pet/{id}` | 添加健康评估 | petId、score、description | `Result<HealthAssessmentResponse>` | doctor 可添加 |
| MED-34 | `GET /medical/health/{id}` | 获取健康评估 | assessmentId | `Result<HealthAssessmentResponse>` | 返回详情 |
| MED-35 | `GET /medical/health` | 查询健康评估 | page/size | `Result<Page<HealthAssessmentResponse>>` | 返回全部健康评估分页 |
| MED-36 | `GET /medical/health/pet/{id}` | 查询宠物健康评估 | petId/page | `Result<Page<HealthAssessmentResponse>>` | 返回该宠物评估分页 |
