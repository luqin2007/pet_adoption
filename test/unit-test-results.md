# 单元测试结果

## 执行时间

2026-04-23 20:38:36 Asia/Hong_Kong

## 执行命令

```powershell
.\mvnw.cmd test
```

## 环境

- 工作目录：`C:\Users\lq200\Documents\课程设计\毕业设计\code\backend`
- Java：项目使用 `java.version=17`
- 测试框架：JUnit 5、Mockito、Spring Boot Test
- 单元测试数据库策略：不连接真实 MySQL；使用 mock、临时目录和契约测试隔离外部依赖

## 测试汇总

| 测试类 | 用例数 | 失败 | 错误 | 跳过 | 结果 |
|---|---:|---:|---:|---:|---|
| `OpenApiControllerContractTest` | 1 | 0 | 0 | 0 | 通过 |
| `OpenApiQualityContractTest` | 2 | 0 | 0 | 0 | 通过 |
| `DatabaseSchemaContractTest` | 4 | 0 | 0 | 0 | 通过 |
| `EnumAndRequestContractTest` | 4 | 0 | 0 | 0 | 通过 |
| `ResultAndUtilityContractTest` | 5 | 0 | 0 | 0 | 通过 |
| `PetAdoptionApplicationTests` | 1 | 0 | 0 | 0 | 通过 |
| `BaseServiceUnitTest` | 4 | 0 | 0 | 0 | 通过 |
| `FileServiceUnitTest` | 4 | 0 | 0 | 0 | 通过 |
| `JwtAndSecurityUnitTest` | 4 | 0 | 0 | 0 | 通过 |
| `NoticeServiceUnitTest` | 2 | 0 | 0 | 0 | 通过 |
| `PetServiceUnitTest` | 2 | 0 | 0 | 0 | 通过 |
| `PublicityServiceUnitTest` | 4 | 0 | 0 | 0 | 通过 |
| `RescueTaskServiceUnitTest` | 3 | 0 | 0 | 0 | 通过 |
| `UserServiceUnitTest` | 3 | 0 | 0 | 0 | 通过 |
| `VolunteerServiceUnitTest` | 3 | 0 | 0 | 0 | 通过 |
| `WorkflowBeginServiceUnitTest` | 6 | 0 | 0 | 0 | 通过 |

总计：

- 用例数：52
- 失败：0
- 错误：0
- 跳过：0
- 构建状态：成功

## 本轮新增覆盖

### BaseService

- Redis uuid 开始流程会生成 UUID、写入内容并设置过期时间。
- Redis uuid 校验流程会拒绝过期 uuid。
- Redis uuid 校验成功时会刷新过期时间。
- Page DTO 转换会保留分页元数据并转换 records。

### FileService

- 任意临时文件上传会写入临时目录，并把 `TempFileInfo` 写入 Redis hash。
- 非支持媒体类型上传图片时会被拒绝，且不会写 Redis。
- 删除正式文件会创建 `DeleteJob`。
- 空白文件名不会创建删除任务。

### JWT 和权限

- access token 与 refresh token 只能按各自类型验证。
- access token 拉黑后会写入 Redis 失效 key，并在校验时被拒绝。
- 请求头 token 提取要求符合配置的 `Authorization` + `Bearer` 格式。
- `CustomUserDetails` 会根据角色 bitmask 映射 Spring Security authority。
- 修复后 `WORKER` 不再获得 `ROLE_ADMIN`。

### UserService

- 登录成功时通过 `AuthenticationManager` 返回用户响应并绑定 access/refresh token。
- 密码错误时包装为业务无效参数异常。
- 邮箱验证码会写 Redis，并发布邮件发送事件。

### PublicityService

- 工作人员可创建科普文章草稿。
- 普通用户不能创建科普文章。
- 分享已发布文章会递增分享数并生成详情 URL。
- 草稿文章不能被分享。

### NoticeService

- SSE 连接会使用当前登录用户 id 注册。
- 批量通知会为每个接收人创建未读通知响应。

### PetService

- 工作人员可以废弃宠物记录。
- 普通用户不能废弃宠物记录。

### RescueTaskService

- 救助任务创建人可以查看自己的任务。
- 无关普通用户不能查看他人救助任务。
- 工作人员可以查看任意救助任务。

### VolunteerService

- 工作人员可以发放待发放激励。
- 普通用户不能发放激励。
- 已发放激励不能重复发放。

### 剩余业务流程入口

- `AdoptBreadingService.beginAgreement`：工作人员可开始协议草稿，普通用户被拒绝。
- `ItemDonationService.beginDonation`：登录用户可开始捐赠流程，Redis 内容记录用户 id。
- `LostPetService.beginLostPet`：登录用户可开始走失宠物报备。
- `MedicalService.beginExamination`：医生可为未完成且未废弃的医疗明细开始检查上传流程，非医生被拒绝。

### 数据库和 API 契约

- `init.sql` 声明目标数据库、外键检查开关，并最终恢复外键检查。
- 每个实体类都有对应建表语句。
- `DROP TABLE` 与 `CREATE TABLE` 表集合一致。
- 所有外键引用目标表都存在。
- OpenAPI 每个 operation 具备 tag、summary 和 2xx 响应。
- OpenAPI 声明统一响应 `Result` schema 和 `bearerAuth` 鉴权方案。
- 枚举状态机和请求 id 集合去重逻辑已覆盖。

## 发现并修复的问题

详见：

- `test/problem-report-20260423-200724.md`
- `test/problem-report-20260423-201516.md`
- `test/problem-report-20260423-201611.md`
- `test/problem-report-20260423-201849.md`
- `test/problem-report-20260423-201938.md`
- `test/problem-report-20260423-202031.md`
- `test/problem-report-20260423-203716.md`
- `test/problem-report-20260423-mysqlsh-not-found.md`

核心生产问题：

- `UserRole.ADMIN` 原 `matchMask=0x0` 会使任意角色匹配 `ROLE_ADMIN`。
- 已修复为 `matchMask=0x1F`，只有完整权限掩码才匹配管理员角色。
- `RescueTaskStatus` 原使用 `EnumSet` 在枚举构造期间初始化前置状态，首次访问枚举会失败。
- 已改为普通不可变 `Set`，状态流转语义不变。
- `openapi.yaml` 缺少 Bearer JWT 鉴权方案，已补充 `components.securitySchemes.bearerAuth`。
- `openapi.yaml` 缺少统一响应 schema，已补充 `components.schemas.Result`。

## 未覆盖风险

- 当前单元测试未连接真实 MySQL/Redis；已补 `init.sql` 静态契约，但真实执行仍需 MySQL Shell 环境。
- 当前 OpenAPI 契约测试验证 Controller 路由、operation 基础质量和核心组件声明，不验证所有请求/响应 schema 的字段级兼容。
- 文件服务的图片/视频真实 MIME 样本、事务 afterCommit 行为、媒体封面切换仍建议在后续补充。
- 本轮已覆盖所有服务的至少一个单元入口或核心权限/状态规则；跨 mapper 的完整业务流程仍需要使用 MySQL Shell 初始化数据库后做集成测试。
