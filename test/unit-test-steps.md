# 后端单元测试步骤

## 1. 测试范围

本轮先执行单元测试，范围覆盖后端服务基础能力、文件处理、JWT/权限、工具类契约以及 OpenAPI 与 Controller 路由契约。

参考文件：

- 数据库初始化脚本：`init.sql`
- API 接口文档：`openapi.yaml`
- 数据库和运行配置：`src/main/resources/application.properties`
- Maven 配置：`pom.xml`

## 2. 环境准备

1. 进入后端目录。

```powershell
cd C:\Users\lq200\Documents\课程设计\毕业设计\code\backend
```

2. 确认 JDK 与 Maven Wrapper 可用。

```powershell
java -version
.\mvnw.cmd -version
```

3. 确认当前分支与工作区状态。

```powershell
git status --short --branch
```

4. 单元测试阶段不连接真实 MySQL/Redis。数据库配置来自 `application.properties`，数据库结构与初始数据来自 `init.sql`，用于后续集成测试和 API 测试准备。

## 3. 基线测试

执行已有测试，确认新增测试前的基线状态。

```powershell
.\mvnw.cmd test
```

基线结果：

- 用例总数：7
- 失败：0
- 错误：0
- 状态：通过

## 4. 服务测试规划

1. 扫描 `src/main/java/com/example/backend/service` 下所有服务类。
2. 为每个服务整理公开方法、测试输入和预计输出。
3. 按可隔离程度划分单元测试优先级：
   - P0：无数据库依赖、可 mock 外部依赖的基础逻辑。
   - P1：依赖 mapper、Redis、文件系统、登录上下文，但可以用 mock 或临时目录隔离。
   - P2：强依赖事务、多 mapper、事件联动，优先进入集成测试。
4. 生成服务级测试文档：`test/service-unit-test-plan.md`。

## 5. 新增单元测试类

新增测试文件：

- `src/test/java/com/example/backend/service/BaseServiceUnitTest.java`
- `src/test/java/com/example/backend/service/FileServiceUnitTest.java`
- `src/test/java/com/example/backend/util/JwtAndSecurityUnitTest.java`

已有测试文件继续保留：

- `src/test/java/com/example/backend/module/OpenApiControllerContractTest.java`
- `src/test/java/com/example/backend/module/ResultAndUtilityContractTest.java`
- `src/test/java/com/example/backend/PetAdoptionApplicationTests.java`

## 6. 执行新增后的全量单元测试

```powershell
.\mvnw.cmd test
```

第一次新增后结果：

- 用例总数：19
- 失败：3
- 错误：1
- 状态：失败
- 问题报告：`test/problem-report-20260423-200724.md`

修复内容：

- 修正 Mockito varargs matcher 写法。
- 修正非图片文件校验预期。
- 修正 JWT 拉黑测试的 Redis stub 时序。
- 修复 `UserRole.ADMIN` 掩码错误，避免非管理员用户获得 `ROLE_ADMIN`。

## 7. 回归测试

再次执行：

```powershell
.\mvnw.cmd test
```

上一轮结果：

- 用例总数：19
- 失败：0
- 错误：0
- 跳过：0
- 状态：通过

继续补充剩余服务后最终结果：

- 用例总数：42
- 失败：0
- 错误：0
- 跳过：0
- 状态：通过

## 8. 提交步骤

1. 查看变更。

```powershell
git status --short
git diff --stat
```

2. 暂存本轮单元测试、测试文档和权限修复。

```powershell
git add src/main/java/com/example/backend/entity/property/UserRole.java src/test/java/com/example/backend/service/BaseServiceUnitTest.java src/test/java/com/example/backend/service/FileServiceUnitTest.java src/test/java/com/example/backend/util/JwtAndSecurityUnitTest.java test
```

3. 提交。

```powershell
git commit -m "test: add backend unit test plan and coverage"
```

4. 提交后再次查看状态。

```powershell
git status --short --branch
```
