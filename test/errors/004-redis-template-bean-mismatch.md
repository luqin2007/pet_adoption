# 错误 004：`RedisHelper` 注入类型与 Spring 默认 Bean 不匹配

## 测试数据

- 启动命令：`java -jar target/backend-0.0.1-SNAPSHOT.jar --server.port=18080`
- 日志文件：`test/integration/app-runtime.log`

## 预期结果

- 应用上下文正常启动，Redis 相关组件可被注入。

## 实际结果

- 启动失败，关键报错：
  - `No qualifying bean of type 'org.springframework.data.redis.core.RedisTemplate<java.lang.String, java.lang.Object>' available`

## 错误原因分析

`RedisHelper` 构造注入声明为 `RedisTemplate<String, Object>`，但 Spring Boot 自动配置默认提供的是 `RedisTemplate<Object, Object>` 和 `StringRedisTemplate`，泛型签名不一致导致无法匹配。

## 解决方案

- `RedisHelper` 调整为注入：
  - `RedisTemplate<Object, Object>`（对象/hash）
  - `StringRedisTemplate`（字符串键值）
- 业务逻辑保持不变，仅修正 Bean 类型契约。

## 解决结果

- 已完成代码修复，待重新执行测试与启动验证。
