# 错误 005：`ImmunityHistoryMapper` 未注册为 MyBatis Bean

## 测试数据

- 启动命令：`java -jar target/backend-0.0.1-SNAPSHOT.jar --server.port=18080`
- 日志文件：`test/integration/app-runtime.log`

## 预期结果

- `MedicalService` 依赖注入完成，系统可正常启动。

## 实际结果

- 启动失败，关键报错：
  - `No qualifying bean of type 'com.example.backend.mapper.ImmunityHistoryMapper' available`

## 错误原因分析

`ImmunityHistoryMapper.java` 虽然存在，但缺少 `@Mapper` 注解。在当前配置下 MyBatis 不会自动注册该接口，导致 `MedicalService` 构造注入失败。

## 解决方案

- 在 `ImmunityHistoryMapper` 上补充 `@Mapper` 注解，纳入 MyBatis Mapper 扫描。

## 解决结果

- 已完成代码修复，待重新执行测试与启动验证。
