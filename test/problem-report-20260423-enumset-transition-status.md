# 状态枚举转移检查问题报告 2026-04-23

## 背景

继续检查状态流转枚举后发现，除 `RescueTaskStatus` 外，还有其他枚举在构造器中使用 `EnumSet` 保存前置状态。

## 风险

在 enum 构造器或 enum 静态初始化尚未完成时调用 `EnumSet.noneOf(...)` / `EnumSet.of(...)`，可能触发类似 `RescueTaskStatus` 曾出现的初始化失败。

## 本轮修复

按 `VolunteerShiftStatus` 的实现方式，将以下枚举改为静态 `EnumMap<Status, EnumSet<Status>>` 保存可转移前置状态：

- `DonationStatus`
- `VolunteerApplicationStatus`
- `RescueTaskStatus`

## 验证

扩展 `EnumAndRequestContractTest`，覆盖：

- 救助任务状态转移
- 捐赠状态转移
- 志愿者申请状态转移
- 用户角色隐式权限展开和 `rezip` 归一化

最终执行 `.\mvnw.cmd test` 通过：

- 用例数：55
- 失败：0
- 错误：0
- 跳过：0
