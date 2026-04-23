# 测试环境问题报告 2026-04-23

## 检查命令

```powershell
mysqlsh --version
```

## 结果

当前 shell 返回：

```text
The term 'mysqlsh' is not recognized as a name of a cmdlet, function, script file, or executable program.
```

## 影响

- 真实 MySQL Shell 初始化数据库与数据库集成测试暂时无法执行。
- 本轮先补充 `init.sql` 静态契约测试，验证建表脚本的数据库名、外键开关、实体表覆盖、DROP/CREATE 一致性和外键目标表存在性。

## 后续处理

需要将 MySQL Shell 安装目录加入 PATH，或在测试步骤中使用 `mysqlsh.exe` 的绝对路径，再执行数据库初始化和 API 集成测试。
