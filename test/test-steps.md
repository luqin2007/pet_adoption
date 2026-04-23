# 后端测试步骤

## 1. 分支与基线文件

1. 在 `backend` 子项目创建测试分支：`codex/backend-db-api-test`
2. 复制数据库初始化脚本：`../sql/init.sql -> backend/init.sql`
3. 复制 OpenAPI 文档：`../api/openapi.yaml -> backend/openapi.yml`
4. 提交基线文件：`test: add database and api baselines`

## 2. 单元测试代码

1. 新增 `ResultAndUtilityContractTest`
2. 新增 `OpenApiControllerContractTest`
3. `OpenApiControllerContractTest` 通过反射读取 Controller 路由，并解析 `openapi.yml` 的 paths/methods 进行集合比对。
4. 执行命令：`.\mvnw.cmd test`
5. 结果：`7 tests, 0 failures, 0 errors`

## 3. 后端启动测试

1. 先尝试 `.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--server.port=18080`
2. Maven 插件依赖下载曾出现 TLS 握手中断，记录为错误 002。
3. 改用 `.\mvnw.cmd -DskipTests package` 打包。
4. 使用 `java -jar target/backend-0.0.1-SNAPSHOT.jar --server.port=18080` 启动。
5. 逐个修复启动过程中暴露的 Bean 注册和循环依赖问题。
6. 最终服务启动成功，`GET /auth/check/username/codex_probe` 返回 `200`。

## 4. API 集成测试

1. 启动后端服务，端口 `18080`
2. 执行 API 请求矩阵：
   - `GET /auth/check/username/{username}`
   - `GET /auth/check/email/{email}`
   - `POST /auth/check/code?email=invalid-email`
   - `POST /auth/login`
   - `GET /users/1`
   - `GET /pets/`
   - `GET /publicity/articles`
3. 记录 HTTP 状态码和响应体。
4. 结果保存到：
   - `test/integration/api-results.json`
   - `test/integration/api-test-results.md`

## 5. 错误记录与修复提交

每遇到错误均创建独立文档，包含测试数据、预期结果、实际结果、原因分析和解决方案。

错误文档：

- `test/errors/001-parent-type-get-folder.md`
- `test/errors/002-maven-plugin-handshake.md`
- `test/errors/003-startup-circular-dependency-user-auth.md`
- `test/errors/004-redis-template-bean-mismatch.md`
- `test/errors/005-immunity-history-mapper-not-registered.md`
- `test/errors/006-multiple-mappers-missing-annotation.md`
- `test/errors/007-pet-medical-service-cycle.md`
- `test/errors/008-error-endpoint-blocked-by-security.md`
- `test/errors/009-validation-error-handling.md`
