# 错误 006：多处 Mapper 缺少 `@Mapper` 导致启动时连续注入失败

## 测试数据

- 启动命令：`java -jar target/backend-0.0.1-SNAPSHOT.jar --server.port=18080`
- 日志文件：`test/integration/app-runtime.log`

## 预期结果

- MyBatis Mapper 全部注册，服务启动完成。

## 实际结果

- 启动过程依次出现 Mapper Bean 缺失异常（先后暴露）：
  - `ImmunityHistoryMapper`（已在错误 005 修复）
  - `MedicalRecordMapper`
  - 进一步扫描发现 `HealthAssessmentMapper`、`MedicalDetailMapper`、`TreatmentPlanMapper` 同样未标注 `@Mapper`

## 错误原因分析

项目中部分 Mapper 接口存在实现代码，但未加 `@Mapper` 注解；当前项目没有统一 `@MapperScan` 覆盖这些接口，导致 Spring 容器无法注册它们。

## 解决方案

- 补齐以下接口的 `@Mapper`：
  - `HealthAssessmentMapper`
  - `MedicalDetailMapper`
  - `MedicalRecordMapper`
  - `TreatmentPlanMapper`
- 保持查询逻辑不变，仅修复组件注册契约。

## 解决结果

- 已完成代码修复，待重新执行测试与启动验证。
