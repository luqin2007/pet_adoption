# 错误 003：应用启动失败（`UserService` 与 `AuthenticationManager` 循环依赖）

## 测试数据

- 启动命令：`java -jar target/backend-0.0.1-SNAPSHOT.jar --server.port=18080`
- 日志文件：`test/integration/app-runtime.log`

## 预期结果

- 应用上下文正常启动，Tomcat 监听 `18080`，可开始 API 集成测试。

## 实际结果

- Spring 容器初始化失败，关键错误：
  - `The dependencies of some of the beans in the application context form a cycle`
  - 依赖链：`jwtAuthenticationFilter -> userService -> authenticationManager -> userService`

## 错误原因分析

`UserService` 同时承担了 `UserDetailsService` 和登录认证逻辑，并在构造注入中直接依赖 `AuthenticationManager`。而 `AuthenticationManager` 初始化时又会回调 `UserDetailsService`（即 `UserService`），导致容器创建阶段形成循环。

## 解决方案

- 将 `UserService` 中的 `AuthenticationManager` 改为 `@Lazy` 延迟注入。
- 保持认证流程不变，仅调整 Bean 初始化时机，打破启动期循环依赖。

## 解决结果

- 已完成代码修复，待重新执行单元测试与启动验证。
