# API Test Results 01: Auth, User, Asset, Notice

- Time: 2026-04-23 22:26:18
- BaseUrl: http://127.0.0.1:8080
- Summary: passed=37 failed=7 total=44
- Raw JSON: api-test-results-01-auth-user-asset-notice-20260423222610.json

| ID | Method | Path | HTTP | code | Result | Message |
|---|---|---|---:|---:|---|---|
| AUTH-02-api_normal_222610-before | GET | /auth/check/username/api_normal_222610 | 200 | 200 | PASS | success |
| AUTH-03-api_normal_222610-before | GET | /auth/check/email/api_normal_222610%40example.com | 200 | 200 | PASS | success |
| AUTH-04-send-api_normal_222610@example.com | POST | /auth/check/code?email=api_normal_222610%40example.com | 200 | 200 | PASS | success |
| AUTH-01-api_normal_222610 | POST | /auth/register | 200 | 200 | PASS | success |
| AUTH-02-api_worker_222610-before | GET | /auth/check/username/api_worker_222610 | 200 | 200 | PASS | success |
| AUTH-03-api_worker_222610-before | GET | /auth/check/email/api_worker_222610%40example.com | 200 | 200 | PASS | success |
| AUTH-04-send-api_worker_222610@example.com | POST | /auth/check/code?email=api_worker_222610%40example.com | 200 | 200 | PASS | success |
| AUTH-01-api_worker_222610 | POST | /auth/register | 200 | 200 | PASS | success |
| AUTH-02-api_doctor_222610-before | GET | /auth/check/username/api_doctor_222610 | 200 | 200 | PASS | success |
| AUTH-03-api_doctor_222610-before | GET | /auth/check/email/api_doctor_222610%40example.com | 200 | 200 | PASS | success |
| AUTH-04-send-api_doctor_222610@example.com | POST | /auth/check/code?email=api_doctor_222610%40example.com | 200 | 200 | PASS | success |
| AUTH-01-api_doctor_222610 | POST | /auth/register | 200 | 200 | PASS | success |
| AUTH-02-api_volunteer_222610-before | GET | /auth/check/username/api_volunteer_222610 | 200 | 200 | PASS | success |
| AUTH-03-api_volunteer_222610-before | GET | /auth/check/email/api_volunteer_222610%40example.com | 200 | 200 | PASS | success |
| AUTH-04-send-api_volunteer_222610@example.com | POST | /auth/check/code?email=api_volunteer_222610%40example.com | 200 | 200 | PASS | success |
| AUTH-01-api_volunteer_222610 | POST | /auth/register | 200 | 200 | PASS | success |
| AUTH-02-api_admin_222610-before | GET | /auth/check/username/api_admin_222610 | 200 | 200 | PASS | success |
| AUTH-03-api_admin_222610-before | GET | /auth/check/email/api_admin_222610%40example.com | 200 | 200 | PASS | success |
| AUTH-04-send-api_admin_222610@example.com | POST | /auth/check/code?email=api_admin_222610%40example.com | 200 | 200 | PASS | success |
| AUTH-01-api_admin_222610 | POST | /auth/register | 200 | 200 | PASS | success |
| AUTH-05-api_normal_222610 | POST | /auth/login | 200 | 200 | PASS | success |
| AUTH-05-api_worker_222610 | POST | /auth/login | 200 | 200 | PASS | success |
| AUTH-05-api_admin_222610 | POST | /auth/login | 200 | 200 | PASS | success |
| AUTH-02-existing | GET | /auth/check/username/api_normal_222610 | 200 | 422 | PASS | 用户名已存在 |
| AUTH-03-existing | GET | /auth/check/email/api_normal_222610%40example.com | 200 | 422 | PASS | 邮箱已存在 |
| AUTH-08-refresh | POST | /auth/refresh | 200 | 200 | PASS | success |
| AUTH-06-forget | POST | /auth/forget?email=api_normal_222610%40example.com | 200 | 200 | PASS | success |
| AUTH-07-reset | POST | /auth/reset | 200 | 200 | PASS | success |
| AUTH-05-login-new-password | POST | /auth/login | 200 | 200 | PASS | success |
| AUTH-09-logout | POST | /auth/logout | 200 | 200 | PASS | success |
| AUTH-05-api_normal_222610 | POST | /auth/login | 200 | 200 | PASS | success |
| USER-01 | GET | /users/2047321137184043009 | 200 | 200 | PASS | success |
| USER-02 | PUT | /users/2047321137184043009 | 200 | 200 | PASS | success |
| USER-03 | PUT | /users/2047321139901952002 | 500 | 0 | FAIL |  |
| USER-04 | PATCH | /users/2047321137184043009/avatar | 500 | 0 | FAIL |  |
| ASSET-02 | GET | /assets/unknown/1/a.jpg | 404 | 404 | PASS | exception.not_found.asset |
| ASSET-03 | GET | /assets/pet/1/not-exists.jpg | 404 | 404 | PASS | exception.not_found.asset |
| ASSET-04 | GET | /assets/pet/1/..%2Fsecret.jpg | 400 |  | PASS |  |
| USER-06 | DELETE | /users/2047321137184043009/avatar | 500 | 0 | FAIL |  |
| USER-07 | GET | /users/?page=1&size=10 | 200 | 200 | PASS | success |
| NOTICE-01 | GET | /notices?page=1&size=10 | 500 | 0 | FAIL |  |
| NOTICE-02 | GET | /notices/unread | 500 | 0 | FAIL |  |
| NOTICE-03 | PATCH | /notices/read | 500 | 0 | FAIL |  |
| NOTICE-04 | PATCH | /notices/unread | 500 | 0 | FAIL |  |
