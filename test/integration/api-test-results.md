# API 集成测试结果

## 测试前置

- 后端服务：`http://127.0.0.1:18080`
- 数据库：按 `application.properties` 连接 `pet_adoption`
- Redis：按 `application.properties` 连接 database `1`
- 测试时间：2026-04-23

## 结果汇总

| 用例 | 方法 | 路径 | 预期 | 实际 |
| --- | --- | --- | --- | --- |
| 检查用户名未占用 | GET | `/auth/check/username/codex_probe` | `200` | `200` |
| 检查邮箱未占用 | GET | `/auth/check/email/codex_probe%40163.com` | `200` | `200` |
| 发送验证码邮箱格式错误 | POST | `/auth/check/code?email=invalid-email` | `422` | `422` |
| 错误账号密码登录 | POST | `/auth/login` | `422` | `422` |
| 未登录访问用户详情 | GET | `/users/1` | `401` | `401` |
| 未登录访问宠物列表 | GET | `/pets/` | `401` | `401` |
| 未登录访问文章列表 | GET | `/publicity/articles` | `401` | `401` |

## 详细响应

### 检查用户名未占用

- 请求：`GET /auth/check/username/codex_probe`
- 状态码：`200`
- 响应体：

```json
{"code":200,"data":null,"message":"success"}
```

### 检查邮箱未占用

- 请求：`GET /auth/check/email/codex_probe%40163.com`
- 状态码：`200`
- 响应体：

```json
{"code":200,"data":null,"message":"success"}
```

### 发送验证码邮箱格式错误

- 请求：`POST /auth/check/code?email=invalid-email`
- 状态码：`422`
- 响应体：

```json
{"code":422,"data":null,"message":"邮箱格式错误"}
```

### 错误账号密码登录

- 请求：`POST /auth/login`
- 请求体：

```json
{"username":"nobody","password":"badpwd"}
```

- 状态码：`422`
- 响应体：

```json
{"code":422,"data":null,"message":"用户名或密码错误"}
```

### 未登录访问用户详情

- 请求：`GET /users/1`
- 状态码：`401`
- 响应体：

```json
{"code":401,"data":null,"message":"Full authentication is required to access this resource"}
```

### 未登录访问宠物列表

- 请求：`GET /pets/`
- 状态码：`401`
- 响应体：

```json
{"code":401,"data":null,"message":"Full authentication is required to access this resource"}
```

### 未登录访问文章列表

- 请求：`GET /publicity/articles`
- 状态码：`401`
- 响应体：

```json
{"code":401,"data":null,"message":"Full authentication is required to access this resource"}
```

## 结论

- 后端服务可启动并处理请求。
- 数据库连接可用，公开检查接口成功访问数据库查询。
- API 错误响应已统一到后端 `Result` 格式。
- 认证拦截有效，受保护接口未登录返回 `401`。
