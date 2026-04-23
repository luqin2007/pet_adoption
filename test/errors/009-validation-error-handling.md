# 错误 009：校验异常处理链导致 API 返回 `500`

## 测试数据

- `POST /auth/check/code?email=invalid-email`
- `POST /auth/login`，请求体：`{"username":"nobody","password":"badpwd"}`

## 预期结果

- 参数校验错误返回统一 `Result` 包装，状态码为 `422`。
- 登录失败应进入业务认证逻辑并返回可解释错误，而不是框架级 `500`。

## 实际结果

- 无效邮箱触发 `ConstraintViolationException` 后进入通用异常处理，但错误编号使用 `Instant` 格式化 `yyMMdd`，再次抛出 `UnsupportedTemporalTypeException`，最终返回 Spring Boot 默认 `500`。
- 登录接口被全局 `RequestValidator` 错误绑定到普通 DTO，抛出 `Invalid target for Validator`，最终返回 `500`。

## 错误原因分析

- `DateTimeFormatter.ofPattern("yyMMdd")` 不能直接格式化 `Instant`，需要使用 `LocalDate` 或带时区的时间类型。
- `WebMvcConfig` 对所有 Binder 都添加了只支持 `IValidatedRequest` 的自定义 Validator，普通请求对象会被 Spring 判定为非法目标。
- 缺少针对 Bean Validation 异常的统一处理。

## 解决方案

- `GlobalExceptionHandler` 使用 `LocalDate.now()` 生成错误编号日期。
- 增加 `ConstraintViolationException` 和 `MethodArgumentNotValidException` 的统一 `422` 响应。
- `WebMvcConfig` 仅当目标对象实现 `IValidatedRequest` 时添加自定义 Validator。

## 解决结果

- 已完成代码修复，待重新执行单元测试和 API 集成测试验证。
