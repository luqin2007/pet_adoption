# API 测试用例 01：认证、用户、资源文件、通知

## AuthController

| 编号 | 接口 | 目的 | 测试数据 | 返回值 | 预期结果 |
|---|---|---|---|---|---|
| AUTH-01 | `POST /auth/register` | 注册普通用户 | username=`api_normal_01`，email=`api_normal_01@example.com`，password=`Api@123456`，phone=`13800000001` | `Result<UserResponse>` | 注册成功，返回用户 id、username、email，密码不返回 |
| AUTH-02 | `GET /auth/check/username/{username}` | 检查用户名可用性 | `username=api_normal_01` 与新用户名各测一次 | `Result<Boolean>` | 已存在返回不可用，新用户名返回可用 |
| AUTH-03 | `GET /auth/check/email/{email}` | 检查邮箱可用性 | `api_normal_01@example.com` 与新邮箱各测一次 | `Result<Boolean>` | 已存在返回不可用，新邮箱返回可用 |
| AUTH-04 | `POST /auth/check/code` | 校验邮箱验证码 | 使用 `sendMailCode/forget` 生成的邮箱与验证码 | `Result<Boolean>` | 正确验证码通过，错误验证码失败 |
| AUTH-05 | `POST /auth/login` | 用户登录 | username/email + password | `Result<UserResponse>` | 返回 accessToken、refreshToken、用户基础信息 |
| AUTH-06 | `POST /auth/forget` | 发起忘记密码 | email=`api_normal_01@example.com` | `Result<Void>` | 发送验证码事件成功，Redis 写入验证码 |
| AUTH-07 | `POST /auth/reset` | 重置密码 | email、code、新 password | `Result<Void>` | 密码重置成功，旧密码登录失败，新密码登录成功 |
| AUTH-08 | `POST /auth/refresh` | 刷新 token | refreshToken | `Result<UserResponse>` | 返回新的 accessToken，可访问受保护接口 |
| AUTH-09 | `POST /auth/logout` | 登出 | Header 带 accessToken | `Result<Void>` | token 加入失效列表，再访问受保护接口失败 |

## UserController

| 编号 | 接口 | 目的 | 测试数据 | 返回值 | 预期结果 |
|---|---|---|---|---|---|
| USER-01 | `GET /users/{id}` | 获取用户详情 | normalUser.id | `Result<UserResponse>` | 返回目标用户基础信息与 avatar assetUrl |
| USER-02 | `PUT /users/{id}` | 修改本人资料 | nickname/phone/profile 字段 | `Result<UserResponse>` | 修改成功，返回更新后的字段 |
| USER-03 | `PUT /users/{id}` | 越权修改他人资料 | normalUser 修改 workerUser.id | `Result` | 返回无权限 |
| USER-04 | `PATCH /users/{id}/avatar` | 上传头像 | multipart `avatar=@IMG_AVATAR` | `Result<String>` | 返回 `/assets/user/{id}/{filename}` |
| USER-05 | `GET /assets/user/{id}/{filename}` | 验证头像 assetUrl 可访问 | 使用 USER-04 返回的 URL | 二进制文件 | HTTP 200，响应体非空，Content-Type 为图片 |
| USER-06 | `DELETE /users/{id}/avatar` | 删除头像 | normalUser.id | `Result<Void>` | 删除成功，再查用户 avatar 为空或 assetUrl 不再返回 |
| USER-07 | `GET /users` | 工作人员获取用户列表 | workerUser token，page/size | `Result<Page<UserResponse>>` | 返回分页列表 |
| USER-08 | `DELETE /users/{id}` | 删除测试用户 | workerUser 删除临时用户 | `Result<Void>` | 删除成功；普通用户执行同接口返回无权限 |

## AssetController

| 编号 | 接口 | 目的 | 测试数据 | 返回值 | 预期结果 |
|---|---|---|---|---|---|
| ASSET-01 | `GET /assets/{folder}/{parentId}/{filename}` | 读取真实上传图片 | 使用头像、宠物、捐赠等接口返回的 assetUrl | 文件流 | HTTP 200，`Content-Disposition=inline`，响应体非空 |
| ASSET-02 | `GET /assets/unknown/1/a.jpg` | 非法资源目录 | folder=`unknown` | `Result` | 返回 404 |
| ASSET-03 | `GET /assets/pet/1/not-exists.jpg` | 文件不存在 | 不存在文件名 | `Result` | 返回 404 |
| ASSET-04 | `GET /assets/pet/1/..%2Fsecret.jpg` | 路径穿越防护 | 编码后的上级路径 | `Result` | 返回 422 或 404，不允许读取 upload_files 外文件 |

## NoticeController

| 编号 | 接口 | 目的 | 测试数据 | 返回值 | 预期结果 |
|---|---|---|---|---|---|
| NOTICE-01 | `GET /notices` | 查询通知分页 | normalUser token，page/size | `Result<Page<NoticeResponse>>` | 返回当前用户通知列表 |
| NOTICE-02 | `GET /notices/unread` | 查询未读数 | normalUser token | `Result<Long>` | 返回未读通知数量 |
| NOTICE-03 | `PATCH /notices/read` | 标记已读 | ids=`[noticeId]` | `Result<Void>` | 对应通知变为已读，未读数减少 |
| NOTICE-04 | `PATCH /notices/unread` | 标记未读 | ids=`[noticeId]` | `Result<Void>` | 对应通知变为未读，未读数增加 |
| NOTICE-05 | `GET /notices/connect` | 建立 SSE 连接 | normalUser token | `SseEmitter` | HTTP 200，连接保持；触发业务事件后可收到通知 |
