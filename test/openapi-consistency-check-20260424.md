# OpenAPI 一致性检查报告

检查时间：2026-04-24

## 检查范围

- `openapi.yaml`
- `openapi-apifox.yaml`
- `src/main/java/com/example/backend/controller/*.java`

## 检查方法

1. 解析 `openapi.yaml` 与 `openapi-apifox.yaml` 的 `paths`，按 `method + path` 比对。
2. 对 Apifox 导出的 `"/pets,/pets/"`、`"/users,/users/"` 等复合 path 做拆分，并按后端 Spring 的尾部斜杠等价策略归一化。
3. 扫描后端 Controller 的 `@RequestMapping`、`@GetMapping`、`@PostMapping`、`@PutMapping`、`@PatchMapping`、`@DeleteMapping`，生成实际路由集合。
4. 将后端实际路由集合与 `openapi.yaml` 的操作集合比对。

## 结果

| 项目 | 操作数 | 结论 |
| --- | ---: | --- |
| `openapi.yaml` | 186 | 基准文档 |
| `openapi-apifox.yaml` 归一化后 | 186 | 与 `openapi.yaml` 一致 |
| 后端 Controller 实际路由 | 186 | 与 `openapi.yaml` 一致 |

未发现 `openapi.yaml` 相比 Apifox 文档或后端 Controller 缺失/多余的 path-method 操作。

## 处理结论

`openapi.yaml` 当前与后端项目、Apifox 导出在接口路径和 HTTP 方法层面一致，本次未修改 `openapi.yaml`。

备注：本轮重点核对接口可达性与操作集合。字段级 schema 仍建议后续结合 DTO 自动生成或契约测试持续校验。
