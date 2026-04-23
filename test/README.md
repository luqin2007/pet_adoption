# 后端、数据库、API 测试总览

## 测试范围

- 后端启动与依赖注入验证
- `init.sql` 数据库结构与后端 MyBatis Mapper 启动契约验证
- `openapi.yml` 与 Controller 路由一致性验证
- DTO、工具类、统一响应结构单元测试
- 基于真实后端服务的 API 集成测试

## 测试环境

- 后端目录：`backend`
- 测试分支：`codex/backend-db-api-test`
- 数据库配置来源：`src/main/resources/application.properties`
- 数据库地址：`jdbc:mysql://192.168.1.170:3306/pet_adoption`
- Redis 地址：`192.168.1.170`
- 服务测试端口：`18080`

## 已完成事项

- 已复制 `../sql/init.sql` 到 `backend/init.sql`
- 已复制 `../api/openapi.yaml` 到 `backend/openapi.yml`
- 已新增单元测试：
  - `OpenApiControllerContractTest`
  - `ResultAndUtilityContractTest`
- 已执行 API 集成测试，结果见 `test/integration/api-test-results.md` 和 `test/integration/api-results.json`

## 测试结论

- Maven 单元测试通过：`7 tests, 0 failures, 0 errors`
- 后端服务可正常启动并连接 MySQL
- `openapi.yml` 与当前 Controller 路由集合一致
- 公开认证检查接口返回 `200`
- 参数校验错误返回统一 `422`
- 未认证访问受保护接口返回 `401`
- 测试过程中发现并修复 9 个问题，详见 `test/errors`

## 说明

当前环境没有 `mysql` 命令行客户端，因此未通过外部 SQL 客户端插入临时登录用户。集成测试使用已导入数据库和后端 API 完成可重复验证；如需补充“登录成功 + Token 访问业务接口”的全链路测试，可在数据库预置一个测试用户后继续执行。
