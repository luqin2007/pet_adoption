# 物资与捐赠模块页面规划

## Summary
后台菜单组命名为“物资管理”，核心子页为“捐赠”“物资余量”“库存管理”。首页现有“物资捐赠”快捷入口继续进入 `/donations/new`，用户登记后在后台“物资管理 / 捐赠”查看自己的捐赠记录和状态；工作人员也在该页处理捐赠状态。

## Key Pages
- `/donations/new` 物资捐赠登记页：填写捐赠物资、数量、有效期、交付方式，上传捐赠影像后提交。
- `/console/items/donations` 物资管理 / 捐赠：普通用户查看自己的捐赠记录、明细、影像、当前状态和状态变更记录；工作人员查看全部捐赠并修改状态。
- `/console/items/stocks` 物资管理 / 物资余量：工作人员查看库存批次和余量，进行入库、出库、销毁，并维护物资、分类、库存预警。
- `/console/items/records` 物资管理 / 库存管理：工作人员跟踪和查找物资入库、出库、销毁记录。

## Endpoints
- 捐赠：`PUT /items/donations`、`POST /items/donations/upload/{uuid}`、`DELETE /items/donations/upload/{uuid}/{name}`、`POST /items/donations`、`GET /items/donations`、`GET /items/donations/{id}`、`PUT /items/donations/{id}`、`PATCH /items/donations/{id}/status`。
- 物资与分类：`GET/POST/PUT/DELETE /items/items...`、`GET/POST/PUT/DELETE /items/categories...`。
- 库存与记录：`GET /items/stocks`、`GET /items/stocks/{id}`、`POST /items/stocks`、`GET /items/records`。
- 预警：`GET /items/subscribe`、`GET /items/subscribe/{id}`、`POST /items/subscribe`、`DELETE /items/subscribe?subscribeIds=...`。

## Navigation
- 首页快捷入口：`物资捐赠` -> `/donations/new`。
- 捐赠提交成功：跳转 `/console/items/donations`。
- 捐赠详情的“入库登记”：跳转 `/console/items/stocks?donationId=...` 并打开入库弹窗。

## Notes
- Controller 注释中的 Usage 接口目前没有实际 mapping，v1 使用捐赠状态和状态记录展示“使用状态”，不单独做使用跟踪页。
- 普通用户不进入物资余量和库存管理，只看自己的捐赠记录。
