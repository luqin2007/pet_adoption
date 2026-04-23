# API 实际测试总计划

- 时间：2026-04-23
- 接口数量：186 个 operation
- 文档来源：`openapi.yaml`、各 Controller、`application.properties`
- 测试图片目录：`testImages`

## 一、执行前准备

1. 确认 MySQL 测试库可以被清空或重建。
   - `init.sql` 含建库、建表、初始化数据，执行前必须确认目标数据库为测试库。
   - MySQL Shell：`mysqlsh`
2. 启动 Redis 测试实例，并确认 `spring.data.redis.database=1` 可用于测试。
3. 启动后端：

```powershell
.\mvnw.cmd spring-boot:run
```

4. 建议使用 PowerShell 或 Postman 执行接口；上传接口使用 `multipart/form-data`。
5. 所有需要登录的接口统一使用 `Authorization: Bearer <accessToken>`。
6. 每组测试完成后记录：
   - 请求方法与 URL
   - 请求体或表单字段
   - HTTP 状态码
   - `Result.code`、`Result.message`、`Result.data`
   - 数据库关键记录
   - 若返回 `assetUrl`，追加调用 `/assets/{folder}/{parentId}/{filename}` 验证文件可读

## 二、测试账号与角色

实际执行时至少准备以下账号。若 `init.sql` 中已有同类用户，优先使用初始化用户；否则通过注册接口创建后，在数据库中调整角色用于测试。

| 标识 | 角色 | 用途 |
|---|---|---|
| `normalUser` | NORMAL | 注册、登录、领养、走失宠物、捐赠、普通权限拒绝测试 |
| `workerUser` | WORKER + VOLUNTEER + NORMAL | 审核、救助任务、物资、志愿者管理、用户管理 |
| `doctorUser` | DOCTOR + NORMAL | 医疗、检查、疫苗、驱虫、康复计划 |
| `volunteerUser` | VOLUNTEER + NORMAL | 回访、志愿服务记录、医疗初诊辅助 |
| `adminUser` | ADMIN + 全权限 | 全权限兜底测试 |

## 三、测试图片分配

`testImages` 下共有 23 个文件，均可作为上传接口测试素材。建议固定使用如下映射，便于复现：

| 变量 | 文件 |
|---|---|
| `IMG_AVATAR` | `testImages/005d70677bbf3c352f7070c730f8686ad6d9314bb167a16b2f2b2588ad90fb13.jpg` |
| `IMG_PET_COVER` | `testImages/0c526c6fef62bce303082cf2a0ffeaefe596e415cf460674841e24cf8d84f563.jpg` |
| `IMG_PET_MEDIA` | `testImages/1c3f849465c57168.gif` |
| `IMG_RESCUE` | `testImages/0ddd22359d33d90a778e0019a99d352e01b20852c4d2a7c8b4c8c6fa94334d9e.jpg` |
| `IMG_DONATION` | `testImages/0e2a06a469c5d2023583b27dfc30aec26c71516a3c05dfe741999f9b06e85482.jpg` |
| `IMG_LOST_PET` | `testImages/0f29f503202341281c46f6fa8109845a7e5bd80e3859da4f051134cb95301c03.jpg` |
| `IMG_AGREEMENT` | `testImages/0f53b0095b371f8d7ba243cb19ff6368fbe271be0898a712314be689f5e306d2.jpg` |
| `IMG_EXAM` | `testImages/1c3b1c910f4acde31ab74165bd69489556eba9999401e2cc376e01fde56e1fbb.jpg` |
| `IMG_REHAB` | `testImages/1c5061557fb6838db777c12679c80183679dace7b3b72356303d4baaf3b91605.jpg` |

## 四、分组顺序

为了减少前置依赖混乱，按以下顺序执行：

1. 认证、用户、资源文件、通知：`test/api-test-cases-01-auth-user-asset-notice.md`
2. 宠物、救助、走失宠物、宣传文章：`test/api-test-cases-02-pet-rescue-lost-publicity.md`
3. 物资捐赠、医疗：`test/api-test-cases-03-item-medical.md`
4. 领养寄养、志愿者：`test/api-test-cases-04-adopt-volunteer.md`

## 五、通用预期

| 场景 | 预期 |
|---|---|
| 成功 JSON 接口 | HTTP 200，`code=200` 或项目定义的成功码，`data` 类型与 OpenAPI schema 一致 |
| 成功文件接口 | HTTP 200，`Content-Type` 为实际文件类型或 `application/octet-stream`，响应体非空 |
| 未登录访问受保护接口 | HTTP 401 |
| 无权限访问 | HTTP 403 或业务 `Result.code=403` |
| 参数非法 | HTTP 200 且 `Result.code=422`，或全局异常返回对应校验错误 |
| 数据不存在 | HTTP 200 且 `Result.code=404`，或业务 not found 响应 |
| 状态流转非法 | HTTP 200 且 `Result.code=422` |

## 六、执行记录模板

| 编号 | 接口 | 账号 | 请求数据 | 实际返回 | 是否通过 | 问题报告 |
|---|---|---|---|---|---|---|
|  |  |  |  |  |  |  |
