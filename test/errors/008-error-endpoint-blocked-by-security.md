# 错误 008：`/error` 未放行导致参数校验/业务异常被错误返回为 `401`

## 测试数据

- 目标接口：`POST /auth/check/code`
- 请求示例：`/auth/check/code?email=invalid-email`
- 实测返回：`401` + `Full authentication is required to access this resource`

## 预期结果

- 认证白名单下的 `auth` 接口发生参数错误时，应返回校验错误（4xx）而不是认证错误。

## 实际结果

- 发生参数异常后，响应被统一变成 `401`，影响 API 语义与 OpenAPI 契约一致性。

## 错误原因分析

请求处理出错后会进入 `/error` 分发链路；当前安全配置只放行了 `/auth/**`，未放行 `/error`，导致错误分发阶段又被 Security 拦截为未认证访问。

## 解决方案

- 在 `SecurityConfig` 的 `requestMatchers` 中增加 `"/error"` 放行规则。

## 解决结果

- 已完成配置修复，待重新启动并回归 API 错误码行为。
