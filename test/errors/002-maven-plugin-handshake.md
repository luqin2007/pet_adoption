# 错误 002：`spring-boot:run` 依赖下载 TLS 握手中断

## 测试数据

- 测试命令：`.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--server.port=18080`
- 执行环境：Windows PowerShell，Maven Wrapper

## 预期结果

- Spring Boot 正常启动并监听 `18080` 端口。

## 实际结果

- 启动阶段失败，构建中断。
- 关键报错：
  - `Could not transfer artifact org.springframework.boot:spring-boot-buildpack-platform:jar:4.0.3`
  - `Could not transfer artifact net.java.dev.jna:jna-platform:jar:5.17.0`
  - `Remote host terminated the handshake`

## 错误原因分析

该问题发生在 Maven 从中央仓库下载插件依赖时，属于网络/握手链路瞬时失败，不是业务代码或接口逻辑错误。

## 解决方案

- 重新执行启动命令重试依赖下载。
- 若持续失败，再切换镜像仓库或排查本机 TLS/代理设置。

## 解决结果

- 已执行记录，下一步重试启动验证。
