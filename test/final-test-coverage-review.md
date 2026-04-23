# 最终测试覆盖复盘

## 当前已完成

本轮继续检查未覆盖区域后，已补充以下自动化测试：

- 数据库脚本静态契约：`DatabaseSchemaContractTest`
- OpenAPI 文档质量契约：`OpenApiQualityContractTest`
- 枚举状态机和请求对象契约：`EnumAndRequestContractTest`
- 原有服务单元测试全量回归

最终执行：

```powershell
.\mvnw.cmd test
```

结果：

- 用例数：55
- 失败：0
- 错误：0
- 跳过：0
- 状态：通过

## 已覆盖模块

- 后端基础契约：统一响应、工具类、JWT、用户权限映射。
- 服务层核心单元规则：BaseService、FileService、UserService、PublicityService、NoticeService、PetService、RescueTaskService、VolunteerService。
- 剩余服务入口：AdoptBreadingService、ItemDonationService、LostPetService、MedicalService 的临时流程开始与权限校验。
- API 契约：Controller 路由与 `openapi.yaml` 路径一致，OpenAPI operation 具备 tag、summary 和 2xx 响应。
- OpenAPI 组件契约：统一响应 `Result` schema、Bearer JWT 鉴权方案。
- 数据库脚本契约：`init.sql` 的数据库名、外键开关、实体表覆盖、DROP/CREATE 一致性、外键目标表存在性。
- 状态机契约：救助任务状态、捐赠状态、志愿者申请状态、文章状态、枚举解析、用户角色隐式权限展开。

## 本轮发现并修复

- `openapi.yaml` 缺少 `components.securitySchemes.bearerAuth`。
- `openapi.yaml` 缺少 `components.schemas.Result`。
- `DonationStatus`、`VolunteerApplicationStatus`、`RescueTaskStatus` 已统一为 `VolunteerShiftStatus` 风格的静态状态转移表。

## 仍需真实环境验证的项目

以下内容需要 MySQL/Redis/文件服务联动，不能只靠当前单元测试完全证明：

- `init.sql` 在真实 MySQL 实例中的完整执行。
- MyBatis mapper SQL 与真实表结构的字段级兼容。
- 领养/寄养协议完整流程：协议起草、文件上传、排序、签署、状态流转。
- 捐赠库存完整流程：捐赠单、物资、库存流水、订阅通知。
- 医疗完整流程：初诊、病历、医疗明细、检查文件、诊断、治疗计划、疫苗驱虫、康复。
- 救助任务完整流程：创建、媒体、状态审批、分配、记录。
- API 端到端测试：按 `openapi.yaml` 逐接口发起 HTTP 请求并校验响应 body。

## 环境阻塞

执行：

```powershell
mysqlsh --version
```

重新加载 Machine/User PATH 后，当前 shell 已可识别 `mysqlsh`，详见 `test/problem-report-20260423-mysqlsh-not-found.md`。

## 建议的下一步集成测试命令

在确认目标测试数据库后：

```powershell
mysqlsh --sql root@192.168.1.170:3306 --file init.sql
.\mvnw.cmd test
```

若要做 API 端到端测试，建议在专用测试数据库和测试 Redis 上启动后端，再使用 OpenAPI 生成请求集合或 Postman/Newman 执行接口回归。
