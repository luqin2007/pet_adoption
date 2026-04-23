# 错误 001：ParentType.getFolder 运行时不可用

## 测试数据

- 测试命令：`.\mvnw.cmd test`
- 测试类：`ResultAndUtilityContractTest.generatedFileNameAndAssetUrlAreStable`
- 调用路径：`FileUtils.generateAssetUrl(ParentType.USER, 9L, "avatar.png")`

## 预期结果

- 正常返回资源访问路径：`/assets/user/9/avatar.png`
- Maven 单元测试通过。

## 实际结果

- 测试失败，异常信息为：`The method getFolder() is undefined for the type ParentType`
- 失败位置：`FileUtils.generateAssetUrl(FileUtils.java:162)`

## 错误原因分析

`ParentType` 中存在 `folder` 字段，并通过 Lombok `@Getter` 期望生成 `getFolder()`。当前测试运行时使用的主代码编译产物没有稳定包含该 getter，导致 `FileUtils` 调用 `parentType.getFolder()` 时出现运行时编译问题。

## 解决方案

- 在 `ParentType` 中显式声明 `getFolder()` 方法，避免该核心枚举 API 依赖 Lombok 生成细节。
- 修正测试预期目录名为后端实际枚举值 `user`。

## 解决结果

- 已修复源码和测试断言，等待重新运行单元测试验证。
