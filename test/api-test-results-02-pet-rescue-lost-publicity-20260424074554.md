# API Test Results 02: Pet, Rescue, Lost Pet, Publicity

- Time: 2026-04-24 07:46:06
- BaseUrl: http://127.0.0.1:8080
- Summary: passed=69 failed=0 total=69
- Raw JSON: api-test-results-02-pet-rescue-lost-publicity-20260424074554.json

| ID | Method | Path | HTTP | code | Result | Message |
|---|---|---|---:|---:|---|---|
| PRE-01 | GET | /auth/check/username/api_probe_20260424074554 | 200 | 200 | PASS | success |
| AUTH-send-api2_worker_074554@example.com | POST | /auth/check/code?email=api2_worker_074554%40example.com | 200 | 200 | PASS | success |
| AUTH-register-api2_worker_074554 | POST | /auth/register | 200 | 200 | PASS | success |
| AUTH-send-api2_owner_074554@example.com | POST | /auth/check/code?email=api2_owner_074554%40example.com | 200 | 200 | PASS | success |
| AUTH-register-api2_owner_074554 | POST | /auth/register | 200 | 200 | PASS | success |
| AUTH-send-api2_other_074554@example.com | POST | /auth/check/code?email=api2_other_074554%40example.com | 200 | 200 | PASS | success |
| AUTH-register-api2_other_074554 | POST | /auth/register | 200 | 200 | PASS | success |
| AUTH-login-api2_worker_074554 | POST | /auth/login | 200 | 200 | PASS | success |
| AUTH-login-api2_owner_074554 | POST | /auth/login | 200 | 200 | PASS | success |
| AUTH-login-api2_other_074554 | POST | /auth/login | 200 | 200 | PASS | success |
| PET-01 | POST | /pets | 200 | 200 | PASS | success |
| PET-02 | POST | /pets/2047462014883749890/location | 200 | 200 | PASS | success |
| PET-03 | GET | /pets?status=WAITING&page=1&size=10 | 200 | 200 | PASS | success |
| PET-04 | GET | /pets/2047462014883749890 | 200 | 200 | PASS | success |
| PET-05 | PUT | /pets/2047462014883749890 | 200 | 200 | PASS | success |
| PET-06 | POST | /pets/2047462014883749890/media | 200 | 200 | PASS | success |
| PET-06-asset | GET | /assets/pet/2047462014883749890/20260424074559_005d70677bbf3c352f7070c730f8686ad6d9314bb167a16b2f2b2588ad90fb13.jpg | 200 |  | PASS |  |
| PET-07 | PUT | /pets/2047462014883749890/media/2047462016825712641 | 200 | 200 | PASS | success |
| PET-09 | POST | /pets/2047462014883749890/tags | 200 | 200 | PASS | success |
| PET-11 | PUT | /pets/2047462014883749890/status | 200 | 200 | PASS | success |
| PET-12 | GET | /pets/2047462014883749890/status?page=1&size=10 | 200 | 200 | PASS | success |
| PET-08 | DELETE | /pets/2047462014883749890/media/2047462016825712641 | 200 | 200 | PASS | success |
| PET-13-unauthorized | DELETE | /pets/2047462014883749890 | 403 | 403 | PASS | 权限不足 |
| PET-13 | DELETE | /pets/2047462014883749890 | 200 | 200 | PASS | success |
| RESCUE-01 | PUT | /tasks | 200 | 200 | PASS | success |
| RESCUE-02 | POST | /tasks/1482c98c-0cfc-4cd1-a630-2d950dc3d41f/uploads | 200 | 200 | PASS | success |
| RESCUE-03 | DELETE | /tasks/1482c98c-0cfc-4cd1-a630-2d950dc3d41f/uploads/20260424074600_0c526c6fef62bce303082cf2a0ffeaefe596e415cf460674841e24cf8d84f563.jpg | 200 | 200 | PASS | success |
| RESCUE-03-reupload | POST | /tasks/1482c98c-0cfc-4cd1-a630-2d950dc3d41f/uploads | 200 | 200 | PASS | success |
| RESCUE-04 | POST | /tasks | 200 | 200 | PASS | success |
| RESCUE-05 | GET | /tasks?page=1&size=10 | 200 | 200 | PASS | success |
| RESCUE-06-owner | GET | /tasks/2047462026472611842 | 200 | 200 | PASS | success |
| RESCUE-06-other | GET | /tasks/2047462026472611842 | 403 | 403 | PASS | 权限不足 |
| RESCUE-07 | PUT | /tasks/2047462026472611842 | 200 | 200 | PASS | success |
| RESCUE-08 | GET | /tasks/2047462026472611842/status | 200 | 200 | PASS | success |
| RESCUE-09-approved | PATCH | /tasks/2047462026472611842/status | 200 | 200 | PASS | success |
| RESCUE-09-invalid | PATCH | /tasks/2047462026472611842/status | 422 | 422 | PASS | 无效状态 |
| RESCUE-10 | POST | /tasks/2047462026472611842/assign | 200 | 200 | PASS | success |
| RESCUE-09-processing | PATCH | /tasks/2047462026472611842/status | 200 | 200 | PASS | success |
| RESCUE-09-completed | PATCH | /tasks/2047462026472611842/status | 200 | 200 | PASS | success |
| RESCUE-12 | DELETE | /tasks/2047462026472611842 | 422 | 422 | PASS | 无法删除已通过的任务 |
| LOST-01 | PUT | /lost/pets | 200 | 200 | PASS | success |
| LOST-02 | PUT | /lost/pets/46642f54-4339-45a2-ac93-656ab76deeb9/media | 200 | 200 | PASS | success |
| LOST-03 | DELETE | /lost/pets/46642f54-4339-45a2-ac93-656ab76deeb9/media/20260424074602_0ddd22359d33d90a778e0019a99d352e01b20852c4d2a7c8b4c8c6fa94334d9e.webp | 200 | 200 | PASS | success |
| LOST-03-reupload | PUT | /lost/pets/46642f54-4339-45a2-ac93-656ab76deeb9/media | 200 | 200 | PASS | success |
| LOST-04 | POST | /lost/pets | 200 | 200 | PASS | success |
| LOST-05 | PUT | /lost/pets/2047462036014653441 | 200 | 200 | PASS | success |
| LOST-06 | GET | /lost/pets/2047462036014653441/similar | 200 | 200 | PASS | success |
| LOST-07 | POST | /lost/pets/2047462036014653441/mismatch/2047462014883749890 | 200 | 200 | PASS | success |
| LOST-08 | GET | /lost/pets/2047462036014653441 | 200 | 200 | PASS | success |
| LOST-09 | GET | /lost/pets?page=1&size=10 | 200 | 200 | PASS | success |
| LOST-10 | POST | /lost/claim | 200 | 200 | PASS | success |
| LOST-11 | GET | /lost/claim/2047462039009386498 | 200 | 200 | PASS | success |
| LOST-12 | GET | /lost/claim?page=1&size=10 | 200 | 200 | PASS | success |
| LOST-10b | POST | /lost/claim | 200 | 200 | PASS | success |
| LOST-13 | DELETE | /lost/claim/2047462039927939074 | 200 | 200 | PASS | success |
| LOST-14 | PATCH | /lost/claim/2047462039009386498/approve | 200 | 200 | PASS | success |
| PUB-01 | POST | /publicity/articles | 200 | 200 | PASS | success |
| PUB-02 | GET | /publicity/articles?page=1&size=10 | 200 | 200 | PASS | success |
| PUB-04 | PUT | /publicity/articles/2047462040901017602 | 200 | 200 | PASS | success |
| PUB-05-published | PATCH | /publicity/articles/2047462040901017602/PUBLISHED | 200 | 200 | PASS | success |
| PUB-03 | GET | /publicity/articles/2047462040901017602 | 200 | 200 | PASS | success |
| PUB-06 | POST | /publicity/articles/2047462040901017602/like | 200 | 200 | PASS | success |
| PUB-07 | DELETE | /publicity/articles/2047462040901017602/like | 200 | 200 | PASS | success |
| PUB-08 | POST | /publicity/articles/2047462040901017602/favorite | 200 | 200 | PASS | success |
| PUB-10 | GET | /publicity/favorites?page=1&size=10 | 200 | 200 | PASS | success |
| PUB-09 | DELETE | /publicity/articles/2047462040901017602/favorite | 200 | 200 | PASS | success |
| PUB-11 | POST | /publicity/articles/2047462040901017602/share | 200 | 200 | PASS | success |
| PUB-05-draft | PATCH | /publicity/articles/2047462040901017602/DRAFT | 200 | 200 | PASS | success |
| PUB-12 | DELETE | /publicity/articles/2047462040901017602 | 200 | 200 | PASS | success |
