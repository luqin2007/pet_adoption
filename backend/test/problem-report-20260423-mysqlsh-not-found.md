# 测试环境问题报告 2026-04-23

## 检查命令

```powershell
mysqlsh --version
```

## 初始结果

当前 shell 返回：

```text
The term 'mysqlsh' is not recognized as a name of a cmdlet, function, script file, or executable program.
```

## 影响

- 真实 MySQL Shell 初始化数据库与数据库集成测试暂时无法执行。
- 本轮先补充 `init.sql` 静态契约测试，验证建表脚本的数据库名、外键开关、实体表覆盖、DROP/CREATE 一致性和外键目标表存在性。

## 复测结果

按用户提供的安装路径重新加载 Machine/User PATH 后执行：

```powershell
$env:Path=[System.Environment]::GetEnvironmentVariable('Path','Machine')+';'+[System.Environment]::GetEnvironmentVariable('Path','User')
mysqlsh --version
```

当前 shell 已可识别 MySQL Shell：

```text
C:\Dev\tools\mysql-shell-9.7.0-windows-x86-64bit\bin\mysqlsh.exe   Ver 9.7.0 for Win64 on x86_64 - for MySQL 9.7.0 (MySQL Community Server (GPL))
```

## 后续处理

后续可以使用已重新加载的 PATH 执行数据库初始化和 API 集成测试。
