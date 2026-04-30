# API Test Results 04: Adopt Breading and Volunteer

- Time: 2026-04-24 08:57:21
- BaseUrl: http://127.0.0.1:8080
- Summary: passed=80 failed=0 total=80
- Raw JSON: api-test-results-04-adopt-volunteer-20260424085705.json

| ID | Method | Path | HTTP | code | Result | Message |
|---|---|---|---:|---:|---|---|
| PRE-01 | GET | /auth/check/username/api_probe_20260424085705 | 200 | 200 | PASS | success |
| AUTH-send-api4_worker_085705@example.com | POST | /auth/check/code?email=api4_worker_085705%40example.com | 200 | 200 | PASS | success |
| AUTH-register-api4_worker_085705 | POST | /auth/register | 200 | 200 | PASS | success |
| AUTH-send-api4_adopter_085705@example.com | POST | /auth/check/code?email=api4_adopter_085705%40example.com | 200 | 200 | PASS | success |
| AUTH-register-api4_adopter_085705 | POST | /auth/register | 200 | 200 | PASS | success |
| AUTH-send-api4_foster_085705@example.com | POST | /auth/check/code?email=api4_foster_085705%40example.com | 200 | 200 | PASS | success |
| AUTH-register-api4_foster_085705 | POST | /auth/register | 200 | 200 | PASS | success |
| AUTH-send-api4_volunteer_085705@example.com | POST | /auth/check/code?email=api4_volunteer_085705%40example.com | 200 | 200 | PASS | success |
| AUTH-register-api4_volunteer_085705 | POST | /auth/register | 200 | 200 | PASS | success |
| AUTH-login-api4_worker_085705 | POST | /auth/login | 200 | 200 | PASS | success |
| AUTH-login-api4_adopter_085705 | POST | /auth/login | 200 | 200 | PASS | success |
| AUTH-login-api4_foster_085705 | POST | /auth/login | 200 | 200 | PASS | success |
| AUTH-login-api4_volunteer_085705 | POST | /auth/login | 200 | 200 | PASS | success |
| PRE-PET-01 | POST | /pets | 200 | 200 | PASS | success |
| PRE-PET-02 | POST | /pets/2047479930438119425/media | 200 | 200 | PASS | success |
| PRE-PET-03 | PUT | /pets/2047479930438119425/status | 200 | 200 | PASS | success |
| ADOPT-01 | POST | /adopt/adopt | 200 | 200 | PASS | success |
| ADOPT-02 | GET | /adopt/adopt/2047479931402809346 | 200 | 200 | PASS | success |
| ADOPT-03 | GET | /adopt/adopt?pet=2047479930438119425&user=2047479914956943362&page=1&size=10 | 200 | 200 | PASS | success |
| ADOPT-04-invalid | PATCH | /adopt/adopt/2047479931402809346/AGREEMENT_SIGNED | 422 | 422 | PASS | 领养状态异常 |
| ADOPT-04 | PATCH | /adopt/adopt/2047479931402809346/PASS | 200 | 200 | PASS | success |
| ADOPT-05 | POST | /adopt/breading | 200 | 200 | PASS | success |
| ADOPT-06 | GET | /adopt/breading/2047479932732403714 | 200 | 200 | PASS | success |
| ADOPT-07 | GET | /adopt/breading?user=2047479916865351681&page=1&size=10 | 200 | 200 | PASS | success |
| ADOPT-08 | PATCH | /adopt/breading/2047479932732403714/PASS | 200 | 200 | PASS | success |
| VOL-01 | POST | /volunteers/recruitments | 200 | 200 | PASS | success |
| VOL-02 | GET | /volunteers/recruitments/2047479933751619586 | 200 | 200 | PASS | success |
| VOL-03 | GET | /volunteers/recruitments?status=DRAFT&page=1&size=10 | 200 | 200 | PASS | success |
| VOL-04 | PUT | /volunteers/recruitments/2047479933751619586 | 200 | 200 | PASS | success |
| VOL-05 | PATCH | /volunteers/recruitments/2047479933751619586/PUBLISHED | 200 | 200 | PASS | success |
| VOL-06 | POST | /volunteers/applications | 200 | 200 | PASS | success |
| VOL-07 | GET | /volunteers/applications?recruitment=2047479933751619586&page=1&size=10 | 200 | 200 | PASS | success |
| VOL-08 | GET | /volunteers/applications/2047479935345455105 | 200 | 200 | PASS | success |
| VOL-09-1 | PATCH | /volunteers/applications/2047479935345455105 | 200 | 200 | PASS | success |
| VOL-09-2 | PATCH | /volunteers/applications/2047479935345455105 | 200 | 200 | PASS | success |
| VOL-10 | GET | /volunteers/profiles?status=ACTIVE&page=1&size=10 | 200 | 200 | PASS | success |
| VOL-11 | GET | /volunteers/profiles/2047479936557608961 | 200 | 200 | PASS | success |
| VOL-12 | PUT | /volunteers/profiles/2047479936557608961 | 200 | 200 | PASS | success |
| VOL-13-1 | POST | /volunteers/profiles/2047479936557608961/DISABLED | 200 | 200 | PASS | success |
| VOL-13-2 | POST | /volunteers/profiles/2047479936557608961/ACTIVE | 200 | 200 | PASS | success |
| SHIFT-01 | POST | /volunteers/shifts | 200 | 200 | PASS | success |
| SHIFT-02 | GET | /volunteers/shifts?volunteer=2047479918719234049&page=1&size=10 | 200 | 200 | PASS | success |
| SHIFT-03 | GET | /volunteers/shifts/2047479947508936707 | 200 | 200 | PASS | success |
| SHIFT-04 | PUT | /volunteers/shifts/2047479947508936707 | 200 | 200 | PASS | success |
| SHIFT-05-1 | PATCH | /volunteers/shifts/2047479947508936707 | 200 | 200 | PASS | success |
| SHIFT-05-2 | PATCH | /volunteers/shifts/2047479947508936707 | 200 | 200 | PASS | success |
| SHIFT-05-3 | PATCH | /volunteers/shifts/2047479947508936707 | 200 | 200 | PASS | success |
| SHIFT-06 | POST | /volunteers/shifts/2047479947508936707/records | 200 | 200 | PASS | success |
| SHIFT-07 | GET | /volunteers/records?volunteer=2047479918719234049&page=1&size=10 | 200 | 200 | PASS | success |
| SHIFT-08 | PATCH | /volunteers/records/2047479949866135554 | 200 | 200 | PASS | success |
| SHIFT-09 | GET | /volunteers/records/2047479949866135554 | 200 | 200 | PASS | success |
| REWARD-01 | POST | /volunteers/rewards | 200 | 200 | PASS | success |
| REWARD-02 | GET | /volunteers/rewards?volunteer=2047479918719234049&page=1&size=10 | 200 | 200 | PASS | success |
| REWARD-03 | GET | /volunteers/rewards/2047479951120232449 | 200 | 200 | PASS | success |
| REWARD-04 | POST | /volunteers/rewards/2047479951120232449/issue | 200 | 200 | PASS | success |
| REWARD-04-repeat | POST | /volunteers/rewards/2047479951120232449/issue | 422 | 422 | PASS | 当前激励不可发放 |
| ADOPT-09 | PUT | /adopt/agreement | 200 | 200 | PASS | success |
| ADOPT-10 | POST | /adopt/agreement/upload/d9501b6d-7ab1-4117-83b0-ce2637254712 | 200 | 200 | PASS | success |
| ADOPT-11 | DELETE | /adopt/agreement/upload/d9501b6d-7ab1-4117-83b0-ce2637254712/20260424085715_0c526c6fef62bce303082cf2a0ffeaefe596e415cf460674841e24cf8d84f563.jpg | 200 | 200 | PASS | success |
| ADOPT-11-reupload | POST | /adopt/agreement/upload/d9501b6d-7ab1-4117-83b0-ce2637254712 | 200 | 200 | PASS | success |
| ADOPT-12-paper | POST | /adopt/agreement | 200 | 200 | PASS | success |
| ADOPT-12-electronic | POST | /adopt/agreement | 200 | 200 | PASS | success |
| ADOPT-13 | PUT | /adopt/agreement/2047479958720311297 | 200 | 200 | PASS | success |
| ADOPT-14 | POST | /adopt/agreement/2047479958275715074/files | 200 | 200 | PASS | success |
| ADOPT-16 | PUT | /adopt/agreement/2047479958275715074/files/order | 200 | 200 | PASS | success |
| ADOPT-15 | DELETE | /adopt/agreement/2047479958275715074/files/2047479958338629635 | 200 | 200 | PASS | success |
| ADOPT-17 | PATCH | /adopt/agreement/2047479958275715074/sign | 200 | 200 | PASS | success |
| ADOPT-18 | GET | /adopt/agreement/2047479958275715074 | 200 | 200 | PASS | success |
| ADOPT-18-asset | GET | /assets/agreement/2047479958275715074/20260424085718_0ddd22359d33d90a778e0019a99d352e01b20852c4d2a7c8b4c8c6fa94334d9e.webp | 200 |  | PASS |  |
| ADOPT-19 | GET | /adopt/agreement?parent=2047479931402809346&page=1&size=10 | 200 | 200 | PASS | success |
| FOLLOW-00 | PATCH | /adopt/adopt/2047479931402809346/TRACKING | 200 | 200 | PASS | success |
| FOLLOW-01 | POST | /adopt/follow/adopt/2047479931402809346 | 200 | 200 | PASS | success |
| FOLLOW-02-1 | PUT | /adopt/follow/2047479972272107521 | 200 | 200 | PASS | success |
| FOLLOW-02-2 | PUT | /adopt/follow/2047479972272107521 | 200 | 200 | PASS | success |
| FOLLOW-03 | GET | /adopt/follow/2047479972272107521 | 200 | 200 | PASS | success |
| FOLLOW-04 | GET | /adopt/follow?adopt=2047479931402809346&page=1&size=10 | 200 | 200 | PASS | success |
| FOLLOW-05 | POST | /adopt/follow/2047479972272107521/record | 200 | 200 | PASS | success |
| FOLLOW-05-dup | POST | /adopt/follow/2047479972272107521/record | 409 | 409 | PASS | 回访记录已存在 |
| FOLLOW-06 | GET | /adopt/follow/2047479972272107521/record?page=1&size=10 | 200 | 200 | PASS | success |
| FOLLOW-07 | GET | /adopt/follow/record?volunteer=2047479918719234049&adopt=2047479931402809346&page=1&size=10 | 200 | 200 | PASS | success |
