# 最后一轮单元覆盖检查与 API 测试报告

- 时间：2026-04-23 21:36:47
- 范围：后端 service 单元测试覆盖完整性复核、OpenAPI 与 Controller API 合同测试
- 环境：Windows PowerShell，Maven Wrapper，JDK 17

## 一、单元测试覆盖完整性检查

### 检查步骤

1. 扫描 `src/main/java/com/example/backend/service` 下所有服务类。
2. 扫描 `src/test/java/com/example/backend/service` 下现有服务单元测试类。
3. 对比服务类与测试类，确认缺口。
4. 对已有测试只覆盖准备阶段或局部方法的服务，补充高风险业务分支。
5. 执行完整 `.\mvnw.cmd test`。

### 覆盖缺口

本轮检查前，以下服务缺少专门或关键分支覆盖：

| 服务 | 本轮补充内容 |
|---|---|
| `AdoptBreadingService` | 补充领养状态修改权限校验、禁止直接设置 `AGREEMENT_SIGNED` |
| `ItemDonationService` | 补充捐赠状态流转权限、用户关闭退回捐赠、非法状态流转 |
| `LostPetService` | 补充走失宠物匹配过滤、忽略已排除宠物、关闭/废弃状态短路 |
| `MedicalService` | 补充已完成/已废弃病历禁止开始检查上传流程 |

### 单元测试结果

命令：

```powershell
.\mvnw.cmd test
```

结果：

```text
Tests run: 65, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 二、API 测试

### 测试步骤

1. 统计 `openapi.yaml` 中的 path 与 operation 数量。
2. 统计 Controller 注解中的实际 mapping 数量。
3. 执行 OpenAPI 与 Controller 路由一致性测试。
4. 执行 OpenAPI 文档质量测试，检查每个 operation 的 `tags`、`summary`、`2xx responses`，并检查统一响应 schema 与 bearer auth。

### API 覆盖统计

| 项目 | 数量 |
|---|---:|
| OpenAPI paths | 122 |
| OpenAPI operations | 185 |
| Controller classes | 11 |
| Controller mappings | 185 |

HTTP 方法分布：

| 方法 | OpenAPI | Controller |
|---|---:|---:|
| GET | 70 | 70 |
| POST | 52 | 52 |
| PUT | 26 | 26 |
| PATCH | 15 | 15 |
| DELETE | 22 | 22 |

### API 自动化测试结果

命令：

```powershell
.\mvnw.cmd '-Dtest=OpenApiControllerContractTest,OpenApiQualityContractTest' test
```

结果：

```text
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 三、失败与修复记录

本轮补充测试第一次执行时出现测试编译失败，已记录到：

- `test/problem-report-20260423-213521.md`

修复后完整单元测试与 API 合同测试均已通过。

## 四、结论

后端 service 单元测试已完成最后一轮覆盖补充；OpenAPI 与 Controller 路由一致，API 文档基础质量检查通过。当前仍未执行会真实写入数据库的端到端 API 调用，因为 `init.sql` 包含建库/删表语句，执行前需要确认目标 MySQL 实例与可清空测试库。
