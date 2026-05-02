# 前端 OpenAPI 功能覆盖检查报告

检查时间：2026-04-24

## 检查范围

- 后端契约：`../backend/openapi.yaml`
- 前端 API 封装：`src/api/*.js`
- 前端页面：`src/views/*.vue`
- 路由：`src/router/index.js`

## 检查结果

`openapi.yaml` 共定义 186 个 path-method 操作。原有前端主要覆盖首页展示、登录注册、宠物列表、文章列表、寻宠列表、志愿者招募列表、业务工作台的部分列表接口，未完整覆盖全部后端接口。

## 已补全内容

- 新增 `src/api/openapiCatalog.js`：由 `../backend/openapi.yaml` 生成 186 个操作目录，包含 method、path、summary、tag、path/query 参数、请求体类型和示例结构。
- 新增 `src/api/openapi.js`：提供通用 OpenAPI 调用器，支持：
  - path 参数替换
  - query 参数
  - JSON 请求体
  - `multipart/form-data` 文件/表单上传
- 新增 `src/views/ApiCoverageView.vue`：新增“OpenAPI 接口覆盖台”，可按模块、HTTP 方法和关键字筛选所有接口，并在页面内填写参数后调用。
- 更新 `src/router/index.js`：新增受登录保护的 `/api-coverage` 路由。
- 更新 `src/views/ProfileCenterView.vue`：后台菜单新增“接口覆盖台”入口。
- 更新 `src/style.css`：补充覆盖台响应式布局和操作面板样式。

## 覆盖结论

补全后，前端已具备对 `openapi.yaml` 中 186 个操作的统一页面级访问能力。现有业务页面仍保留原有展示型流程，完整业务表单可基于新增的 OpenAPI 操作目录继续拆分。

## 验证

已执行：

```bash
npm run build
```

结果：构建成功。Vite 输出 chunk 大小超过 500 kB 的提示，属于体积优化警告，不影响本次功能编译通过。
