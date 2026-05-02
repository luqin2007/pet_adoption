---
title: 默认模块
language_tabs:
  - shell: Shell
  - http: HTTP
  - javascript: JavaScript
  - ruby: Ruby
  - python: Python
  - php: PHP
  - java: Java
  - go: Go
toc_footers: []
includes: []
search: true
code_clipboard: true
highlight_theme: darkula
headingLevel: 2
generator: "@tarslib/widdershins v4.0.30"

---

# 默认模块

Base URLs:

# Authentication

- HTTP Authentication, scheme: bearer

# AuthController

## POST refresh

POST /auth/refresh

刷新 AccessToken

> Body 请求参数

```json
{
  "refreshToken": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[RefreshTokenRequest](#schemarefreshtokenrequest)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": {
    "id": 0,
    "username": "string",
    "email": "string",
    "avatar": "string",
    "accessToken": "string",
    "refreshToken": "string"
  },
  "message": "string"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultUserLoginResponse](#schemaresultuserloginresponse)|

## POST logout

POST /auth/logout

登出
服务端仅注销 AccessToken 和 RefreshToken

> Body 请求参数

```json
{
  "refreshToken": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[RefreshTokenRequest](#schemarefreshtokenrequest)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": null,
  "message": "string"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

# UserManagerController

## POST register

POST /user/register

用户注册

> Body 请求参数

```json
{
  "username": "string",
  "password": "string",
  "email": "string",
  "avatar": "string",
  "code": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[UserRegisterRequest](#schemauserregisterrequest)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": {
    "id": 0,
    "username": "string",
    "email": "string",
    "avatar": "string",
    "accessToken": "string",
    "refreshToken": "string"
  },
  "message": "string"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultUserLoginResponse](#schemaresultuserloginresponse)|

## GET checkUsername

GET /user/check/username/{username}

注册时验证用户名是否存在，success 代表用户已存在

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|username|path|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": null,
  "message": "string"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## GET checkMail

GET /user/check/email/{email}

注册时验证邮箱是否存在，邮箱使用 encodeURI 转码，success 代表邮箱已存在

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|email|path|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": null,
  "message": "string"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## GET sendMailCode

GET /user/check/code

请求发送邮箱验证码，邮箱使用 encodeURI 转码

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|email|query|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": null,
  "message": "string"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## POST login

POST /user/login

用户登录

> Body 请求参数

```json
{
  "username": "string",
  "password": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[UserLoginRequest](#schemauserloginrequest)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": {
    "id": 0,
    "username": "string",
    "email": "string",
    "avatar": "string",
    "accessToken": "string",
    "refreshToken": "string"
  },
  "message": "string"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultUserLoginResponse](#schemaresultuserloginresponse)|

## GET getUser

GET /user/users/{id}

根据用户 id 获取用户信息，返回值中 `accessToken` 和 `refreshToken` 均为 `null`

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": {
    "id": 0,
    "username": "string",
    "email": "string",
    "avatar": "string",
    "accessToken": "string",
    "refreshToken": "string"
  },
  "message": "string"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultUserLoginResponse](#schemaresultuserloginresponse)|

## POST updateUser

POST /user/users/{id}

更新用户信息，返回值中 `accessToken` 和 `refreshToken` 均为 `null`

> Body 请求参数

```json
{
  "username": "string",
  "password": "string",
  "email": "string",
  "role": 0,
  "avatar": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |none|
|body|body|[UserUpdateRequest](#schemauserupdaterequest)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": {
    "id": 0,
    "username": "string",
    "email": "string",
    "avatar": "string",
    "accessToken": "string",
    "refreshToken": "string"
  },
  "message": "string"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultUserLoginResponse](#schemaresultuserloginresponse)|

## DELETE removeUser

DELETE /user/users/{id}

删除用户

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": null,
  "message": "string"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## GET forgetPassword

GET /user/forget

忘记密码，向给定邮箱发送重置密码邮件

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|email|query|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": null,
  "message": "string"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## POST resetPassword

POST /user/reset

重置密码页面，从忘记密码的邮件跳转

> Body 请求参数

```json
{
  "id": "string",
  "password": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[PasswordResetRequest](#schemapasswordresetrequest)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": null,
  "message": "string"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

# 数据模型

<h2 id="tocS_ResultUserLoginResponse">ResultUserLoginResponse</h2>

<a id="schemaresultuserloginresponse"></a>
<a id="schema_ResultUserLoginResponse"></a>
<a id="tocSresultuserloginresponse"></a>
<a id="tocsresultuserloginresponse"></a>

```json
{
  "code": 0,
  "data": {
    "id": 0,
    "username": "string",
    "email": "string",
    "avatar": "string",
    "accessToken": "string",
    "refreshToken": "string"
  },
  "message": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|data|[UserLoginResponse](#schemauserloginresponse)|false|none||none|
|message|string|false|none||none|

<h2 id="tocS_UserLoginResponse">UserLoginResponse</h2>

<a id="schemauserloginresponse"></a>
<a id="schema_UserLoginResponse"></a>
<a id="tocSuserloginresponse"></a>
<a id="tocsuserloginresponse"></a>

```json
{
  "id": 0,
  "username": "string",
  "email": "string",
  "avatar": "string",
  "accessToken": "string",
  "refreshToken": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer|false|none||none|
|username|string|false|none||none|
|email|string|false|none||none|
|avatar|string|false|none||none|
|accessToken|string|false|none||none|
|refreshToken|string|false|none||none|

<h2 id="tocS_UserRegisterRequest">UserRegisterRequest</h2>

<a id="schemauserregisterrequest"></a>
<a id="schema_UserRegisterRequest"></a>
<a id="tocSuserregisterrequest"></a>
<a id="tocsuserregisterrequest"></a>

```json
{
  "username": "string",
  "password": "string",
  "email": "string",
  "avatar": "string",
  "code": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|username|string|false|none||none|
|password|string|false|none||none|
|email|string|false|none||none|
|avatar|string¦null|false|none||none|
|code|string|false|none||邮箱验证码|

<h2 id="tocS_ResultVoid">ResultVoid</h2>

<a id="schemaresultvoid"></a>
<a id="schema_ResultVoid"></a>
<a id="tocSresultvoid"></a>
<a id="tocsresultvoid"></a>

```json
{
  "code": 0,
  "data": null,
  "message": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|data|null|false|none||none|
|message|string|false|none||none|

<h2 id="tocS_UserLoginRequest">UserLoginRequest</h2>

<a id="schemauserloginrequest"></a>
<a id="schema_UserLoginRequest"></a>
<a id="tocSuserloginrequest"></a>
<a id="tocsuserloginrequest"></a>

```json
{
  "username": "string",
  "password": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|username|string|false|none||none|
|password|string|false|none||none|

<h2 id="tocS_UserUpdateRequest">UserUpdateRequest</h2>

<a id="schemauserupdaterequest"></a>
<a id="schema_UserUpdateRequest"></a>
<a id="tocSuserupdaterequest"></a>
<a id="tocsuserupdaterequest"></a>

```json
{
  "username": "string",
  "password": "string",
  "email": "string",
  "role": 0,
  "avatar": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|username|string|false|none||none|
|password|string|false|none||none|
|email|string|false|none||none|
|role|integer|false|none||none|
|avatar|string|false|none||none|

<h2 id="tocS_PasswordResetRequest">PasswordResetRequest</h2>

<a id="schemapasswordresetrequest"></a>
<a id="schema_PasswordResetRequest"></a>
<a id="tocSpasswordresetrequest"></a>
<a id="tocspasswordresetrequest"></a>

```json
{
  "id": "string",
  "password": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|string|false|none||忘记密码发送邮件时生成的 id|
|password|string|false|none||新密码|

<h2 id="tocS_RefreshTokenRequest">RefreshTokenRequest</h2>

<a id="schemarefreshtokenrequest"></a>
<a id="schema_RefreshTokenRequest"></a>
<a id="tocSrefreshtokenrequest"></a>
<a id="tocsrefreshtokenrequest"></a>

```json
{
  "refreshToken": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|refreshToken|string|false|none||none|

