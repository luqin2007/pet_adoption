# API Test Results 01: Auth, User, Asset, Notice

- Time: 2026-04-23 22:45:22
- BaseUrl: http://127.0.0.1:8080
- Summary: passed=47 failed=0 total=47
- Raw JSON: api-test-results-01-auth-user-asset-notice-20260423224512.json

| ID | Method | Path | HTTP | code | Result | Message |
|---|---|---|---:|---:|---|---|
| AUTH-02-api_normal_224512-before | GET | /auth/check/username/api_normal_224512 | 200 | 200 | PASS | success |
| AUTH-03-api_normal_224512-before | GET | /auth/check/email/api_normal_224512%40example.com | 200 | 200 | PASS | success |
| AUTH-04-send-api_normal_224512@example.com | POST | /auth/check/code?email=api_normal_224512%40example.com | 200 | 200 | PASS | success |
| AUTH-01-api_normal_224512 | POST | /auth/register | 200 | 200 | PASS | success |
| AUTH-02-api_worker_224512-before | GET | /auth/check/username/api_worker_224512 | 200 | 200 | PASS | success |
| AUTH-03-api_worker_224512-before | GET | /auth/check/email/api_worker_224512%40example.com | 200 | 200 | PASS | success |
| AUTH-04-send-api_worker_224512@example.com | POST | /auth/check/code?email=api_worker_224512%40example.com | 200 | 200 | PASS | success |
| AUTH-01-api_worker_224512 | POST | /auth/register | 200 | 200 | PASS | success |
| AUTH-02-api_doctor_224512-before | GET | /auth/check/username/api_doctor_224512 | 200 | 200 | PASS | success |
| AUTH-03-api_doctor_224512-before | GET | /auth/check/email/api_doctor_224512%40example.com | 200 | 200 | PASS | success |
| AUTH-04-send-api_doctor_224512@example.com | POST | /auth/check/code?email=api_doctor_224512%40example.com | 200 | 200 | PASS | success |
| AUTH-01-api_doctor_224512 | POST | /auth/register | 200 | 200 | PASS | success |
| AUTH-02-api_volunteer_224512-before | GET | /auth/check/username/api_volunteer_224512 | 200 | 200 | PASS | success |
| AUTH-03-api_volunteer_224512-before | GET | /auth/check/email/api_volunteer_224512%40example.com | 200 | 200 | PASS | success |
| AUTH-04-send-api_volunteer_224512@example.com | POST | /auth/check/code?email=api_volunteer_224512%40example.com | 200 | 200 | PASS | success |
| AUTH-01-api_volunteer_224512 | POST | /auth/register | 200 | 200 | PASS | success |
| AUTH-02-api_admin_224512-before | GET | /auth/check/username/api_admin_224512 | 200 | 200 | PASS | success |
| AUTH-03-api_admin_224512-before | GET | /auth/check/email/api_admin_224512%40example.com | 200 | 200 | PASS | success |
| AUTH-04-send-api_admin_224512@example.com | POST | /auth/check/code?email=api_admin_224512%40example.com | 200 | 200 | PASS | success |
| AUTH-01-api_admin_224512 | POST | /auth/register | 200 | 200 | PASS | success |
| AUTH-05-api_normal_224512 | POST | /auth/login | 200 | 200 | PASS | success |
| AUTH-05-api_worker_224512 | POST | /auth/login | 200 | 200 | PASS | success |
| AUTH-05-api_admin_224512 | POST | /auth/login | 200 | 200 | PASS | success |
| AUTH-02-existing | GET | /auth/check/username/api_normal_224512 | 200 | 422 | PASS | 用户名已存在 |
| AUTH-03-existing | GET | /auth/check/email/api_normal_224512%40example.com | 200 | 422 | PASS | 邮箱已存在 |
| AUTH-08-refresh | POST | /auth/refresh | 200 | 200 | PASS | success |
| AUTH-06-forget | POST | /auth/forget?email=api_normal_224512%40example.com | 200 | 200 | PASS | success |
| AUTH-07-reset | POST | /auth/reset | 200 | 200 | PASS | success |
| AUTH-05-login-new-password | POST | /auth/login | 200 | 200 | PASS | success |
| AUTH-09-logout | POST | /auth/logout | 200 | 200 | PASS | success |
| AUTH-05-api_normal_224512 | POST | /auth/login | 200 | 200 | PASS | success |
| USER-01 | GET | /users/2047325928375255041 | 200 | 200 | PASS | success |
| USER-02 | PUT | /users/2047325928375255041 | 200 | 200 | PASS | success |
| USER-02-stale-token | GET | /users/2047325928375255041 | 401 | 401 | PASS | Full authentication is required to access this resource |
| AUTH-05-api_normal_224512_updated | POST | /auth/login | 200 | 200 | PASS | success |
| USER-03 | PUT | /users/2047325930896031745 | 403 | 403 | PASS | 权限不足 |
| USER-04 | PATCH | /users/2047325928375255041/avatar | 200 | 200 | PASS | success |
| USER-05 | GET | /assets/user/2047325928375255041/20260423224521_005d70677bbf3c352f7070c730f8686ad6d9314bb167a16b2f2b2588ad90fb13.jpg | 200 |  | PASS |  |
| ASSET-02 | GET | /assets/unknown/1/a.jpg | 404 | 404 | PASS | exception.not_found.asset |
| ASSET-03 | GET | /assets/pet/1/not-exists.jpg | 404 | 404 | PASS | exception.not_found.asset |
| ASSET-04 | GET | /assets/pet/1/..%2Fsecret.jpg | 400 |  | PASS |  |
| USER-06 | DELETE | /users/2047325928375255041/avatar | 200 | 200 | PASS | success |
| USER-07 | GET | /users?page=1&size=10 | 200 | 200 | PASS | success |
| NOTICE-01 | GET | /notices?page=1&size=10 | 200 | 200 | PASS | success |
| NOTICE-02 | GET | /notices/unread | 200 | 200 | PASS | success |
| NOTICE-03 | PATCH | /notices/read | 200 | 200 | PASS | success |
| NOTICE-04 | PATCH | /notices/unread | 200 | 200 | PASS | success |
