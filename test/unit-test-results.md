# 单元测试结果

## 执行时间

2026-04-23 20:08:26 Asia/Hong_Kong

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
| `ResultAndUtilityContractTest` | 5 | 0 | 0 | 0 | 通过 |
| `PetAdoptionApplicationTests` | 1 | 0 | 0 | 0 | 通过 |
| `BaseServiceUnitTest` | 4 | 0 | 0 | 0 | 通过 |
| `FileServiceUnitTest` | 4 | 0 | 0 | 0 | 通过 |
| `JwtAndSecurityUnitTest` | 4 | 0 | 0 | 0 | 通过 |

总计：

- 用例数：19
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

## 发现并修复的问题

详见：`test/problem-report-20260423-200724.md`

核心生产问题：

- `UserRole.ADMIN` 原 `matchMask=0x0` 会使任意角色匹配 `ROLE_ADMIN`。
- 已修复为 `matchMask=0x1F`，只有完整权限掩码才匹配管理员角色。

## 未覆盖风险

- 当前单元测试未连接真实 MySQL/Redis，因此不验证 `init.sql` 与 mapper SQL 的运行结果。
- 当前 OpenAPI 契约测试验证 Controller 路由是否被 `openapi.yaml` 完整记录，不验证请求/响应 schema 的字段级兼容。
- 文件服务的图片/视频真实 MIME 样本、事务 afterCommit 行为、媒体封面切换仍建议在下一轮补充。
- 跨服务流程如领养协议、救助任务、医疗检查、捐赠库存需要使用 MySQL Shell 初始化数据库后做集成测试。
