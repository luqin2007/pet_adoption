// Generated from backend/openapi.yaml. Do not edit by hand.
export const OPENAPI_OPERATIONS = [
  {
    "id": "getAsset",
    "method": "GET",
    "path": "/assets/{folder}/{parentId}/{filename}",
    "summary": "获取资源文件",
    "tags": [
      "Asset"
    ],
    "pathParams": [
      "folder",
      "parentId",
      "filename"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addAdopt",
    "method": "POST",
    "path": "/adopt/adopt",
    "summary": "申请领养",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "petId": 0,
      "applicantPhone": "",
    },
    "formFields": []
  },
  {
    "id": "getAdopts",
    "method": "GET",
    "path": "/adopt/adopt",
    "summary": "获取领养申请",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "pet",
        "required": false,
        "type": "array",
        "description": "pet"
      },
      {
        "name": "user",
        "required": false,
        "type": "array",
        "description": "user"
      },
      {
        "name": "status",
        "required": false,
        "type": "array",
        "description": "status"
      },
      {
        "name": "time0",
        "required": false,
        "type": "string",
        "description": "time0"
      },
      {
        "name": "time1",
        "required": false,
        "type": "string",
        "description": "time1"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getAdopt",
    "method": "GET",
    "path": "/adopt/adopt/{id}",
    "summary": "获取领养申请",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "updateAdoptStatus",
    "method": "PATCH",
    "path": "/adopt/adopt/{id}/{st}",
    "summary": "申请领养审核",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [
      "id",
      "st"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addBreading",
    "method": "POST",
    "path": "/adopt/breading",
    "summary": "申请寄养",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "petName": "",
      "petAge": 0,
      "petType": "",
      "petBreed": "",
      "petDescription": "",
      "applicantPhone": "",
      "time0": "2026-04-24T00:00:00",
      "time1": "2026-04-24T00:00:00"
    },
    "formFields": []
  },
  {
    "id": "getBreadingPets",
    "method": "GET",
    "path": "/adopt/breading",
    "summary": "获取寄养申请",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "applicant",
        "required": false,
        "type": "array",
        "description": "applicant"
      },
      {
        "name": "reviewer",
        "required": false,
        "type": "array",
        "description": "reviewer"
      },
      {
        "name": "status",
        "required": false,
        "type": "array",
        "description": "status"
      },
      {
        "name": "petType",
        "required": false,
        "type": "array",
        "description": "petType"
      },
      {
        "name": "time0",
        "required": false,
        "type": "string",
        "description": "time0"
      },
      {
        "name": "time1",
        "required": false,
        "type": "string",
        "description": "time1"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getBreading",
    "method": "GET",
    "path": "/adopt/breading/{id}",
    "summary": "获取寄养申请",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "updateBreadingStatus",
    "method": "PATCH",
    "path": "/adopt/breading/{id}/{st}",
    "summary": "更新寄养审核状态",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [
      "id",
      "st"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "beginAgreement",
    "method": "PUT",
    "path": "/adopt/agreement",
    "summary": "准备起草协议",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addAgreement",
    "method": "POST",
    "path": "/adopt/agreement",
    "summary": "起草协议",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "uuid": "",
      "parentId": 0,
      "parentType": "",
      "type": "",
      "content": "",
      "fileOrder": [
        ""
      ]
    },
    "formFields": []
  },
  {
    "id": "getAgreements",
    "method": "GET",
    "path": "/adopt/agreement",
    "summary": "查找协议",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "parentId",
        "required": false,
        "type": "integer",
        "description": "parentId"
      },
      {
        "name": "parentType",
        "required": false,
        "type": "string",
        "description": "parentType"
      },
      {
        "name": "signed",
        "required": false,
        "type": "boolean",
        "description": "signed"
      },
      {
        "name": "time0",
        "required": false,
        "type": "string",
        "description": "time0"
      },
      {
        "name": "time1",
        "required": false,
        "type": "string",
        "description": "time1"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "uploadAgreementWhenAdd",
    "method": "POST",
    "path": "/adopt/agreement/upload/{_id}",
    "summary": "起草阶段上传协议扫描件",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [
      "_id"
    ],
    "queryParams": [
      {
        "name": "file",
        "required": false,
        "type": "string",
        "description": ""
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "deleteAgreementWhenAdd",
    "method": "DELETE",
    "path": "/adopt/agreement/upload/{_id}/{name}",
    "summary": "起草阶段删除协议扫描件",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [
      "_id",
      "name"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "updateAgreement",
    "method": "PUT",
    "path": "/adopt/agreement/{id}",
    "summary": "修改协议",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "content": ""
    },
    "formFields": []
  },
  {
    "id": "getAgreement",
    "method": "GET",
    "path": "/adopt/agreement/{id}",
    "summary": "获取协议",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "uploadAgreement",
    "method": "POST",
    "path": "/adopt/agreement/{id}/files",
    "summary": "上传协议图片",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "multipart/form-data",
    "bodyExample": {
      "file": "",
      "page": 0
    },
    "formFields": [
      {
        "name": "file",
        "type": "string",
        "format": "binary",
        "required": true
      },
      {
        "name": "page",
        "type": "integer",
        "format": "int32",
        "required": true
      }
    ]
  },
  {
    "id": "deleteAgreementFile",
    "method": "DELETE",
    "path": "/adopt/agreement/{id}/files/{fid}",
    "summary": "删除协议图片",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [
      "id",
      "fid"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "reorderAgreementFiles",
    "method": "PUT",
    "path": "/adopt/agreement/{id}/files/order",
    "summary": "调整协议顺序",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "fileOrder": [
        0
      ]
    },
    "formFields": []
  },
  {
    "id": "signAgreement",
    "method": "PATCH",
    "path": "/adopt/agreement/{id}/sign",
    "summary": "签署协议",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [
      {
        "name": "sign",
        "required": false,
        "type": "string",
        "description": ""
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addFollowTask",
    "method": "POST",
    "path": "/adopt/follow/adopt/{id}",
    "summary": "创建回访任务",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "volunteerId": 0,
      "planTime": "2026-04-24T00:00:00",
      "remark": ""
    },
    "formFields": []
  },
  {
    "id": "updateFollowTask",
    "method": "PUT",
    "path": "/adopt/follow/{id}",
    "summary": "更新回访任务",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "workerId": 0,
      "volunteerId": 0,
      "planTime": "2026-04-24T00:00:00",
      "status": "",
      "remark": ""
    },
    "formFields": []
  },
  {
    "id": "getFollowTask",
    "method": "GET",
    "path": "/adopt/follow/{id}",
    "summary": "获取回访任务",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getFollowTasks",
    "method": "GET",
    "path": "/adopt/follow",
    "summary": "查询回访任务",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "adopt",
        "required": false,
        "type": "integer",
        "description": "adopt"
      },
      {
        "name": "volunteer",
        "required": false,
        "type": "integer",
        "description": "volunteer"
      },
      {
        "name": "status",
        "required": false,
        "type": "array",
        "description": "status"
      },
      {
        "name": "time0",
        "required": false,
        "type": "string",
        "description": "time0"
      },
      {
        "name": "time1",
        "required": false,
        "type": "string",
        "description": "time1"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addFollowRecord",
    "method": "POST",
    "path": "/adopt/follow/{id}/record",
    "summary": "提交回访记录",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "visitTime": "2026-04-24T00:00:00",
      "summary": "",
      "lifeStatus": "",
      "healthStatus": "",
      "risk": "",
      "suggestion": ""
    },
    "formFields": []
  },
  {
    "id": "getFollowRecords",
    "method": "GET",
    "path": "/adopt/follow/{id}/record",
    "summary": "获取回访记录",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getFollowRecords_2",
    "method": "GET",
    "path": "/adopt/follow/record",
    "summary": "查询回访记录",
    "tags": [
      "AdoptBreading"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "task",
        "required": false,
        "type": "integer",
        "description": "task"
      },
      {
        "name": "volunteer",
        "required": false,
        "type": "integer",
        "description": "volunteer"
      },
      {
        "name": "time0",
        "required": false,
        "type": "string",
        "description": "time0"
      },
      {
        "name": "time1",
        "required": false,
        "type": "string",
        "description": "time1"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "register",
    "method": "POST",
    "path": "/auth/register",
    "summary": "用户注册",
    "tags": [
      "Auth"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "multipart/form-data",
    "bodyExample": {
      "username": "",
      "password": "",
      "email": "",
      "avatar": "",
      "phone": "",
      "code": ""
    },
    "formFields": [
      {
        "name": "username",
        "type": "string",
        "format": "",
        "required": true
      },
      {
        "name": "password",
        "type": "string",
        "format": "",
        "required": true
      },
      {
        "name": "email",
        "type": "string",
        "format": "",
        "required": true
      },
      {
        "name": "avatar",
        "type": "string",
        "format": "binary",
        "required": false
      },
      {
        "name": "phone",
        "type": "string",
        "format": "",
        "required": false
      },
      {
        "name": "code",
        "type": "string",
        "format": "",
        "required": true
      }
    ]
  },
  {
    "id": "isUsernameExist",
    "method": "GET",
    "path": "/auth/check/username/{username}",
    "summary": "注册页面，检查用户名是否存在",
    "tags": [
      "Auth"
    ],
    "pathParams": [
      "username"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "isMailExist",
    "method": "GET",
    "path": "/auth/check/email/{email}",
    "summary": "注册页面，检查邮箱是否存在",
    "tags": [
      "Auth"
    ],
    "pathParams": [
      "email"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "sendMailCode",
    "method": "POST",
    "path": "/auth/check/code",
    "summary": "发送邮箱验证码",
    "tags": [
      "Auth"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "email",
        "required": false,
        "type": "string",
        "description": ""
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "login",
    "method": "POST",
    "path": "/auth/login",
    "summary": "用户登录",
    "tags": [
      "Auth"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "username": "",
      "password": ""
    },
    "formFields": []
  },
  {
    "id": "forgetPassword",
    "method": "POST",
    "path": "/auth/forget",
    "summary": "忘记密码 - 发送密码重置链接",
    "tags": [
      "Auth"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "email",
        "required": false,
        "type": "string",
        "description": ""
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "resetPassword",
    "method": "POST",
    "path": "/auth/reset",
    "summary": "忘记密码 - 重置密码",
    "tags": [
      "Auth"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "id": "",
      "password": ""
    },
    "formFields": []
  },
  {
    "id": "refresh",
    "method": "POST",
    "path": "/auth/refresh",
    "summary": "刷新访问令牌",
    "tags": [
      "Auth"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "refreshToken": ""
    },
    "formFields": []
  },
  {
    "id": "logout",
    "method": "POST",
    "path": "/auth/logout",
    "summary": "登出",
    "tags": [
      "Auth"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "refreshToken": ""
    },
    "formFields": []
  },
  {
    "id": "addDonation",
    "method": "PUT",
    "path": "/items/donations",
    "summary": "准备物资捐赠",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addDonation_2",
    "method": "POST",
    "path": "/items/donations",
    "summary": "确认物资捐赠",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "uuid": "",
      "items": [
        {
          "name": null,
          "itemId": null,
          "itemName": null,
          "itemUnit": null,
          "categoryId": null,
          "description": null,
          "count": null,
          "expireTime": null
        }
      ],
      "delivery": "",
      "description": "",
      "address": "",
      "trackingNumber": ""
    },
    "formFields": []
  },
  {
    "id": "getDonations",
    "method": "GET",
    "path": "/items/donations",
    "summary": "查询捐赠信息",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "user",
        "required": false,
        "type": "array",
        "description": "user"
      },
      {
        "name": "date0",
        "required": false,
        "type": "string",
        "description": "date0"
      },
      {
        "name": "date1",
        "required": false,
        "type": "string",
        "description": "date1"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "uploadDonation",
    "method": "POST",
    "path": "/items/donations/upload/{_id}",
    "summary": "上传捐赠影像",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [
      "_id"
    ],
    "queryParams": [
      {
        "name": "file",
        "required": false,
        "type": "string",
        "description": ""
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "deleteDonation",
    "method": "DELETE",
    "path": "/items/donations/upload/{_id}/{name}",
    "summary": "删除捐赠影像",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [
      "_id",
      "name"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "updateDonation",
    "method": "PUT",
    "path": "/items/donations/{id}",
    "summary": "修改捐赠信息",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "items": [
        {
          "name": null,
          "itemId": null,
          "itemName": null,
          "itemUnit": null,
          "categoryId": null,
          "description": null,
          "count": null,
          "expireTime": null
        }
      ],
      "delivery": "",
      "description": "",
      "address": "",
      "trackingNumber": ""
    },
    "formFields": []
  },
  {
    "id": "getDonation",
    "method": "GET",
    "path": "/items/donations/{id}",
    "summary": "获取捐赠信息",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "updateDonationStatus",
    "method": "PATCH",
    "path": "/items/donations/{id}/status",
    "summary": "修改捐赠状态",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addItem",
    "method": "POST",
    "path": "/items/items",
    "summary": "创建物资信息",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "name": "",
      "categoryId": 0,
      "description": "",
      "unit": ""
    },
    "formFields": []
  },
  {
    "id": "getItems",
    "method": "GET",
    "path": "/items/items",
    "summary": "查询物资信息",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "category",
        "required": false,
        "type": "array",
        "description": "category"
      },
      {
        "name": "keyword",
        "required": false,
        "type": "string",
        "description": "keyword"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "updateItem",
    "method": "PUT",
    "path": "/items/items/{id}",
    "summary": "修改物资信息",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "name": "",
      "categoryId": 0,
      "description": "",
      "unit": ""
    },
    "formFields": []
  },
  {
    "id": "getItem",
    "method": "GET",
    "path": "/items/items/{id}",
    "summary": "获取物资详情",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "discardItems",
    "method": "DELETE",
    "path": "/items/items/{id}",
    "summary": "删除物资信息",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addCategory",
    "method": "POST",
    "path": "/items/categories",
    "summary": "创建物资分类",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "name": "",
      "description": ""
    },
    "formFields": []
  },
  {
    "id": "getCategories",
    "method": "GET",
    "path": "/items/categories",
    "summary": "查询物资分类",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "updateCategory",
    "method": "PUT",
    "path": "/items/categories/{id}",
    "summary": "修改物资分类",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "name": "",
      "description": ""
    },
    "formFields": []
  },
  {
    "id": "discardCategory",
    "method": "DELETE",
    "path": "/items/categories/{id}",
    "summary": "删除物资分类",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getCategory",
    "method": "GET",
    "path": "/items/categories/{id}",
    "summary": "获取物资分类",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addStockRecord",
    "method": "POST",
    "path": "/items/stocks",
    "summary": "入库出库登记",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "id": 0,
      "itemId": 0,
      "count": "",
      "action": "",
      "sourceType": "",
      "purpose": "",
      "expireTime": "2026-04-24T00:00:00",
      "price": "",
      "totalPrice": ""
    },
    "formFields": []
  },
  {
    "id": "getStocks",
    "method": "GET",
    "path": "/items/stocks",
    "summary": "查询库存物品",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "item",
        "required": false,
        "type": "array",
        "description": "item"
      },
      {
        "name": "user",
        "required": false,
        "type": "array",
        "description": "user"
      },
      {
        "name": "source",
        "required": false,
        "type": "array",
        "description": "source"
      },
      {
        "name": "time0",
        "required": false,
        "type": "string",
        "description": "time0"
      },
      {
        "name": "time1",
        "required": false,
        "type": "string",
        "description": "time1"
      },
      {
        "name": "count",
        "required": false,
        "type": "integer",
        "description": "count"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getStock",
    "method": "GET",
    "path": "/items/stocks/{id}",
    "summary": "获取库存记录",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getStockRecords",
    "method": "GET",
    "path": "/items/records",
    "summary": "查询库存记录",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "stock",
        "required": false,
        "type": "array",
        "description": "stock"
      },
      {
        "name": "user",
        "required": false,
        "type": "array",
        "description": "user"
      },
      {
        "name": "action",
        "required": false,
        "type": "array",
        "description": "action"
      },
      {
        "name": "source",
        "required": false,
        "type": "array",
        "description": "source"
      },
      {
        "name": "purpose",
        "required": false,
        "type": "string",
        "description": "purpose"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addSubscribe",
    "method": "POST",
    "path": "/items/subscribe",
    "summary": "添加预警",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "elementId": 0,
      "action": "",
      "count": ""
    },
    "formFields": []
  },
  {
    "id": "getSubscribes",
    "method": "GET",
    "path": "/items/subscribe",
    "summary": "查询预警",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "user",
        "required": false,
        "type": "array",
        "description": "user"
      },
      {
        "name": "action",
        "required": false,
        "type": "array",
        "description": "action"
      },
      {
        "name": "element",
        "required": false,
        "type": "array",
        "description": "element"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "cancelSubscribe",
    "method": "DELETE",
    "path": "/items/subscribe",
    "summary": "取消预警",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getSubscribe",
    "method": "GET",
    "path": "/items/subscribe/{id}",
    "summary": "获取预警",
    "tags": [
      "ItemDonation"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "beginLostPet",
    "method": "PUT",
    "path": "/lost/pets",
    "summary": "准备报备走失",
    "tags": [
      "LostPet"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addLostPet",
    "method": "POST",
    "path": "/lost/pets",
    "summary": "报备走失宠物",
    "tags": [
      "LostPet"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "uuid": "",
      "name": "",
      "age": 0,
      "sex": "",
      "type": "",
      "breed": "",
      "features": "",
      "lostTime": "2026-04-24T00:00:00",
      "phone": "",
      "description": ""
    },
    "formFields": []
  },
  {
    "id": "getLostPets",
    "method": "GET",
    "path": "/lost/pets",
    "summary": "查询走失宠物",
    "tags": [
      "LostPet"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "owner",
        "required": false,
        "type": "array",
        "description": "owner"
      },
      {
        "name": "status",
        "required": false,
        "type": "array",
        "description": "status"
      },
      {
        "name": "type",
        "required": false,
        "type": "array",
        "description": "type"
      },
      {
        "name": "bread",
        "required": false,
        "type": "array",
        "description": "bread"
      },
      {
        "name": "name",
        "required": false,
        "type": "string",
        "description": "name"
      },
      {
        "name": "time0",
        "required": false,
        "type": "string",
        "description": "time0"
      },
      {
        "name": "time1",
        "required": false,
        "type": "string",
        "description": "time1"
      },
      {
        "name": "province",
        "required": false,
        "type": "string",
        "description": "province"
      },
      {
        "name": "city",
        "required": false,
        "type": "string",
        "description": "city"
      },
      {
        "name": "address",
        "required": false,
        "type": "string",
        "description": "address"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "uploadLostPetMedia",
    "method": "PUT",
    "path": "/lost/pets/{_id}/media",
    "summary": "上传宠物图片",
    "tags": [
      "LostPet"
    ],
    "pathParams": [
      "_id"
    ],
    "queryParams": [],
    "contentType": "multipart/form-data",
    "bodyExample": {
      "name": "",
      "file": ""
    },
    "formFields": [
      {
        "name": "name",
        "type": "string",
        "format": "",
        "required": false
      },
      {
        "name": "file",
        "type": "string",
        "format": "binary",
        "required": false
      }
    ]
  },
  {
    "id": "deleteLostPetMedia",
    "method": "DELETE",
    "path": "/lost/pets/{_id}/media/{name}",
    "summary": "删除宠物图片",
    "tags": [
      "LostPet"
    ],
    "pathParams": [
      "_id",
      "name"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "updateLostPet",
    "method": "PUT",
    "path": "/lost/pets/{id}",
    "summary": "修改走失宠物",
    "tags": [
      "LostPet"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "name": "",
      "age": 0,
      "sex": "",
      "type": "",
      "breed": "",
      "features": "",
      "lostTime": "2026-04-24T00:00:00",
      "phone": "",
      "description": ""
    },
    "formFields": []
  },
  {
    "id": "getLostPet",
    "method": "GET",
    "path": "/lost/pets/{id}",
    "summary": "获取走失宠物",
    "tags": [
      "LostPet"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getSimilarPets",
    "method": "GET",
    "path": "/lost/pets/{id}/similar",
    "summary": "查看相似流浪宠物",
    "tags": [
      "LostPet"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "markPetMismatch",
    "method": "POST",
    "path": "/lost/pets/{id}/mismatch/{pid}",
    "summary": "标记某不是丢失的宠物",
    "tags": [
      "LostPet"
    ],
    "pathParams": [
      "id",
      "pid"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addClaim",
    "method": "POST",
    "path": "/lost/claim",
    "summary": "发起认领申请",
    "tags": [
      "LostPet"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "lostPetId": 0,
      "petId": 0,
      "applicantPhone": "",
      "reason": ""
    },
    "formFields": []
  },
  {
    "id": "getClaims",
    "method": "GET",
    "path": "/lost/claim",
    "summary": "查询认领申请",
    "tags": [
      "LostPet"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "lostPet",
        "required": false,
        "type": "array",
        "description": "lostPet"
      },
      {
        "name": "user",
        "required": false,
        "type": "array",
        "description": "user"
      },
      {
        "name": "status",
        "required": false,
        "type": "array",
        "description": "status"
      },
      {
        "name": "time0",
        "required": false,
        "type": "string",
        "description": "time0"
      },
      {
        "name": "time1",
        "required": false,
        "type": "string",
        "description": "time1"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getClaim",
    "method": "GET",
    "path": "/lost/claim/{id}",
    "summary": "获取认领申请",
    "tags": [
      "LostPet"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "cancelClaim",
    "method": "DELETE",
    "path": "/lost/claim/{id}",
    "summary": "取消认领申请",
    "tags": [
      "LostPet"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "approveClaim",
    "method": "PATCH",
    "path": "/lost/claim/{id}/approve",
    "summary": "认领申请审核",
    "tags": [
      "LostPet"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "petId": 0
    },
    "formFields": []
  },
  {
    "id": "addFirstVisitRegistration",
    "method": "POST",
    "path": "/medical/first",
    "summary": "addFirstVisitRegistration",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "petId": 0,
      "name": "",
      "age": 0,
      "weight": null,
      "temperature": null,
      "immunities": [
        {
          "medicine": null,
          "illness": null,
          "count": null,
          "total": null,
          "immunityTime": null
        }
      ],
      "allergies": [
        {
          "source": null,
          "discoveryTime": null
        }
      ],
      "description": ""
    },
    "formFields": []
  },
  {
    "id": "getFirstVisitRegistrations",
    "method": "GET",
    "path": "/medical/first",
    "summary": "getFirstVisitRegistrations",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "registrar",
        "required": false,
        "type": "integer",
        "description": "registrar"
      },
      {
        "name": "pet",
        "required": false,
        "type": "integer",
        "description": "pet"
      },
      {
        "name": "date0",
        "required": false,
        "type": "string",
        "description": "date0"
      },
      {
        "name": "date1",
        "required": false,
        "type": "string",
        "description": "date1"
      },
      {
        "name": "name",
        "required": false,
        "type": "string",
        "description": "name"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getFirstVisitRegistration",
    "method": "GET",
    "path": "/medical/first/{id}",
    "summary": "getFirstVisitRegistration",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addMedicalRecord",
    "method": "POST",
    "path": "/medical/record/pet/{id}",
    "summary": "addMedicalRecord",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "petAge": 0,
      "ownerId": 0,
      "ownerPhone": "",
      "price": "",
      "cost": "",
      "type": ""
    },
    "formFields": []
  },
  {
    "id": "updateMedicalRecord",
    "method": "PUT",
    "path": "/medical/record/{id}",
    "summary": "updateMedicalRecord",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "ownerId": 0,
      "ownerPhone": "",
      "status": "",
      "type": "",
      "startTime": "2026-04-24T00:00:00",
      "endTime": "2026-04-24T00:00:00",
      "cost": ""
    },
    "formFields": []
  },
  {
    "id": "getMedicalRecord",
    "method": "GET",
    "path": "/medical/record/{id}",
    "summary": "getMedicalRecord",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getMedicalRecords",
    "method": "GET",
    "path": "/medical/record",
    "summary": "getMedicalRecords",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "pet",
        "required": false,
        "type": "integer",
        "description": "pet"
      },
      {
        "name": "doctor",
        "required": false,
        "type": "integer",
        "description": "doctor"
      },
      {
        "name": "status",
        "required": false,
        "type": "string",
        "description": "status"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addMedicalDetail",
    "method": "POST",
    "path": "/medical/detail",
    "summary": "addMedicalDetail",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "recordId": 0,
      "summary": "",
      "description": "",
      "history": "",
      "pastHistory": "",
      "lifeHabit": "",
      "weight": null,
      "temperature": null,
      "heartRate": 0,
      "respiratoryRate": 0,
      "physicalExam": ""
    },
    "formFields": []
  },
  {
    "id": "getMedicalDetails",
    "method": "GET",
    "path": "/medical/detail",
    "summary": "getMedicalDetails",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getMedicalDetail",
    "method": "GET",
    "path": "/medical/detail/{id}",
    "summary": "getMedicalDetail",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "updateMedicalDetail",
    "method": "PUT",
    "path": "/medical/detail/{id}",
    "summary": "updateMedicalDetail",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "doctorId": 0,
      "isCompleted": false,
      "summary": "",
      "physicalExam": "",
      "diagnosis": "",
      "differential": "",
      "exam": "",
      "treatment": "",
      "advice": ""
    },
    "formFields": []
  },
  {
    "id": "completeMedicalDetail",
    "method": "PATCH",
    "path": "/medical/detail/{id}",
    "summary": "completeMedicalDetail",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addDiagnosis",
    "method": "POST",
    "path": "/medical/detail/{id}/diagnosis",
    "summary": "addDiagnosis",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "result": "",
      "examinations": [
        0
      ]
    },
    "formFields": []
  },
  {
    "id": "discardDiagnosis",
    "method": "DELETE",
    "path": "/medical/diagnosis/{dId}",
    "summary": "discardDiagnosis",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "dId"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addTreatmentPlan",
    "method": "POST",
    "path": "/medical/detail/{id}/plan",
    "summary": "addTreatmentPlan",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "plan": "",
      "orders": [
        {
          "itemId": null,
          "type": null,
          "count": null,
          "unit": null,
          "price": null
        }
      ],
      "startTime": "2026-04-24T00:00:00",
      "endTime": "2026-04-24T00:00:00"
    },
    "formFields": []
  },
  {
    "id": "discardTreatmentPlan",
    "method": "DELETE",
    "path": "/medical/plan",
    "summary": "discardTreatmentPlan",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "beginExamination",
    "method": "PUT",
    "path": "/medical/details/{id}/exam",
    "summary": "beginExamination",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "uploadExamination",
    "method": "PUT",
    "path": "/medical/exam/{_id}/doc",
    "summary": "uploadExamination",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "_id"
    ],
    "queryParams": [],
    "contentType": "multipart/form-data",
    "bodyExample": {
      "name": "",
      "file": ""
    },
    "formFields": [
      {
        "name": "name",
        "type": "string",
        "format": "",
        "required": false
      },
      {
        "name": "file",
        "type": "string",
        "format": "binary",
        "required": true
      }
    ]
  },
  {
    "id": "deleteExamination",
    "method": "DELETE",
    "path": "/medical/exam/{_id}/doc/{name}",
    "summary": "deleteExamination",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "_id",
      "name"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addExamination",
    "method": "POST",
    "path": "/medical/exam/{_id}",
    "summary": "addExamination",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "_id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "name": "",
      "text": "",
      "examType": "",
      "checkTime": "2026-04-24T00:00:00"
    },
    "formFields": []
  },
  {
    "id": "getExamination",
    "method": "GET",
    "path": "/medical/exam/{id}",
    "summary": "getExamination",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addVaccine",
    "method": "POST",
    "path": "/medical/vaccine/pet/{id}",
    "summary": "addVaccine",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "vaccineId": 0,
      "petAge": 0,
      "times": 0
    },
    "formFields": []
  },
  {
    "id": "getVaccines",
    "method": "GET",
    "path": "/medical/vaccine/pet/{id}",
    "summary": "getVaccines",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getLatestVaccines",
    "method": "GET",
    "path": "/medical/vaccine/pet/{id}/new",
    "summary": "getLatestVaccines",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addDeworm",
    "method": "POST",
    "path": "/medical/deworm/pet/{id}",
    "summary": "addDeworm",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "dewormerId": 0,
      "times": 0
    },
    "formFields": []
  },
  {
    "id": "getDeworms",
    "method": "GET",
    "path": "/medical/deworm/pet/{id}",
    "summary": "getDeworms",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addRehabPlan",
    "method": "POST",
    "path": "/medical/rehab/pet/{id}",
    "summary": "addRehabPlan",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "age": 0,
      "title": "",
      "content": "",
      "frequency": "",
      "type": "",
      "startTime": "2026-04-24T00:00:00",
      "endTime": "2026-04-24T00:00:00",
      "orders": [
        {
          "itemId": null,
          "type": null,
          "count": null,
          "unit": null,
          "price": null
        }
      ]
    },
    "formFields": []
  },
  {
    "id": "getRehabPlan",
    "method": "GET",
    "path": "/medical/rehab/{id}",
    "summary": "getRehabPlan",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getRehabPlans",
    "method": "GET",
    "path": "/medical/rehab",
    "summary": "getRehabPlans",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "pet",
        "required": false,
        "type": "array",
        "description": "pet"
      },
      {
        "name": "doctor",
        "required": false,
        "type": "array",
        "description": "doctor"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "updateRehabPlanStatus",
    "method": "PUT",
    "path": "/medical/rehab/{id}/status",
    "summary": "updateRehabPlanStatus",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addRehabRecord",
    "method": "POST",
    "path": "/medical/rehab/{id}/record",
    "summary": "addRehabRecord",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "multipart/form-data",
    "bodyExample": {
      "step": "",
      "reaction": "",
      "note": "",
      "files": [
        ""
      ]
    },
    "formFields": [
      {
        "name": "step",
        "type": "string",
        "format": "",
        "required": true
      },
      {
        "name": "reaction",
        "type": "string",
        "format": "",
        "required": true
      },
      {
        "name": "note",
        "type": "string",
        "format": "",
        "required": true
      },
      {
        "name": "files",
        "type": "array",
        "format": "",
        "required": false
      }
    ]
  },
  {
    "id": "getRehabRecords",
    "method": "GET",
    "path": "/medical/rehab/{id}/record",
    "summary": "getRehabRecords",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addHealthAssessment",
    "method": "POST",
    "path": "/medical/health/pet/{id}",
    "summary": "addHealthAssessment",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "age": 0,
      "weight": null,
      "scoreBcs": 0,
      "scoreMental": 0,
      "scoreAppetite": 0,
      "summary": ""
    },
    "formFields": []
  },
  {
    "id": "getHealthAssessments",
    "method": "GET",
    "path": "/medical/health/pet/{id}",
    "summary": "getHealthAssessments",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getHealthAssessment",
    "method": "GET",
    "path": "/medical/health/{id}",
    "summary": "getHealthAssessment",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getHealthAssessments_2",
    "method": "GET",
    "path": "/medical/health",
    "summary": "getHealthAssessments",
    "tags": [
      "MedicalCare"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getNoticeList",
    "method": "GET",
    "path": "/notices",
    "summary": "getNoticeList",
    "tags": [
      "Notice"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "read",
        "required": false,
        "type": "boolean",
        "description": "已读"
      },
      {
        "name": "source",
        "required": false,
        "type": "array",
        "description": "查询分区"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "read",
    "method": "PATCH",
    "path": "/notices/read",
    "summary": "read",
    "tags": [
      "Notice"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "unread",
    "method": "PATCH",
    "path": "/notices/unread",
    "summary": "unread",
    "tags": [
      "Notice"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getUnreadCount",
    "method": "GET",
    "path": "/notices/unread",
    "summary": "getUnreadCount",
    "tags": [
      "Notice"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "subscribe",
    "method": "GET",
    "path": "/notices/connect",
    "summary": "subscribe",
    "tags": [
      "Notice"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addPet",
    "method": "POST",
    "path": "/pets",
    "summary": "添加流浪宠物信息",
    "tags": [
      "Pet"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "age": 0,
      "sex": "",
      "type": "",
      "breed": "",
      "description": "",
      "health": ""
    },
    "formFields": []
  },
  {
    "id": "getPets",
    "method": "GET",
    "path": "/pets",
    "summary": "获取流浪宠物列表",
    "tags": [
      "Pet"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "user",
        "required": false,
        "type": "array",
        "description": "user"
      },
      {
        "name": "sex",
        "required": false,
        "type": "string",
        "description": "sex"
      },
      {
        "name": "type",
        "required": false,
        "type": "array",
        "description": "type"
      },
      {
        "name": "breed",
        "required": false,
        "type": "array",
        "description": "breed"
      },
      {
        "name": "status",
        "required": false,
        "type": "array",
        "description": "status"
      },
      {
        "name": "name",
        "required": false,
        "type": "string",
        "description": "name"
      },
      {
        "name": "isDiscard",
        "required": false,
        "type": "boolean",
        "description": "isDiscard"
      },
      {
        "name": "province",
        "required": false,
        "type": "string",
        "description": "province"
      },
      {
        "name": "city",
        "required": false,
        "type": "string",
        "description": "city"
      },
      {
        "name": "district",
        "required": false,
        "type": "string",
        "description": "district"
      },
      {
        "name": "address",
        "required": false,
        "type": "string",
        "description": "address"
      },
      {
        "name": "time",
        "required": false,
        "type": "string",
        "description": "time"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addLocation",
    "method": "POST",
    "path": "/pets/{id}/location",
    "summary": "添加宠物位置信息",
    "tags": [
      "Pet"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getPet",
    "method": "GET",
    "path": "/pets/{id}",
    "summary": "获取流浪宠物信息",
    "tags": [
      "Pet"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "updatePet",
    "method": "PUT",
    "path": "/pets/{id}",
    "summary": "修改流浪宠物信息",
    "tags": [
      "Pet"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "name": "",
      "age": 0,
      "sex": "",
      "type": "",
      "breed": "",
      "health": "",
      "description": ""
    },
    "formFields": []
  },
  {
    "id": "deletePet",
    "method": "DELETE",
    "path": "/pets/{id}",
    "summary": "删除流浪宠物信息",
    "tags": [
      "Pet"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "uploadMedia",
    "method": "POST",
    "path": "/pets/{id}/media",
    "summary": "上传流浪宠物图片/视频，使用 multipart/form-data",
    "tags": [
      "Pet"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "multipart/form-data",
    "bodyExample": {
      "name": "",
      "description": "",
      "file": ""
    },
    "formFields": [
      {
        "name": "name",
        "type": "string",
        "format": "",
        "required": false
      },
      {
        "name": "description",
        "type": "string",
        "format": "",
        "required": false
      },
      {
        "name": "file",
        "type": "string",
        "format": "binary",
        "required": false
      }
    ]
  },
  {
    "id": "updateMedia",
    "method": "PUT",
    "path": "/pets/{id}/media/{mid}",
    "summary": "修改流浪宠物图片/视频信息",
    "tags": [
      "Pet"
    ],
    "pathParams": [
      "id",
      "mid"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "name": "",
      "description": ""
    },
    "formFields": []
  },
  {
    "id": "deleteMedia",
    "method": "DELETE",
    "path": "/pets/{id}/media/{mid}",
    "summary": "删除流浪宠物图片/视频",
    "tags": [
      "Pet"
    ],
    "pathParams": [
      "id",
      "mid"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addTags",
    "method": "POST",
    "path": "/pets/{id}/tags",
    "summary": "添加流浪宠物特征",
    "tags": [
      "Pet"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "tags": [
        ""
      ]
    },
    "formFields": []
  },
  {
    "id": "deleteTags",
    "method": "DELETE",
    "path": "/pets/{id}/tags",
    "summary": "删除流浪宠物特征",
    "tags": [
      "Pet"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "updateStatus",
    "method": "PUT",
    "path": "/pets/{id}/status",
    "summary": "updateStatus",
    "tags": [
      "Pet"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "petId": 0
    },
    "formFields": []
  },
  {
    "id": "getStatusRecords",
    "method": "GET",
    "path": "/pets/{id}/status",
    "summary": "获取宠物状态流转记录",
    "tags": [
      "Pet"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addArticle",
    "method": "POST",
    "path": "/publicity/articles",
    "summary": "创建文章",
    "tags": [
      "Publicity"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "type": "",
      "title": "",
      "content": "",
      "cover": "",
      "publish": false
    },
    "formFields": []
  },
  {
    "id": "getArticles",
    "method": "GET",
    "path": "/publicity/articles",
    "summary": "查询文章列表",
    "tags": [
      "Publicity"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "type",
        "required": false,
        "type": "string",
        "description": "文章查询参数"
      },
      {
        "name": "author",
        "required": false,
        "type": "integer",
        "description": "作者 id"
      },
      {
        "name": "status",
        "required": false,
        "type": "array",
        "description": "文章状态"
      },
      {
        "name": "title",
        "required": false,
        "type": "string",
        "description": "标题"
      },
      {
        "name": "tag",
        "required": false,
        "type": "string",
        "description": "标签"
      },
      {
        "name": "time0",
        "required": false,
        "type": "string",
        "description": "发布时间起点"
      },
      {
        "name": "time1",
        "required": false,
        "type": "string",
        "description": "发布时间终点"
      },
      {
        "name": "isDiscard",
        "required": false,
        "type": "boolean",
        "description": "已删除"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getArticle",
    "method": "GET",
    "path": "/publicity/articles/{id}",
    "summary": "获取文章详情",
    "tags": [
      "Publicity"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "updateArticle",
    "method": "PUT",
    "path": "/publicity/articles/{id}",
    "summary": "修改文章",
    "tags": [
      "Publicity"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "title": "",
      "summary": "",
      "content": "",
      "cover": "",
      "tags": ""
    },
    "formFields": []
  },
  {
    "id": "deleteArticle",
    "method": "DELETE",
    "path": "/publicity/articles/{id}",
    "summary": "删除文章",
    "tags": [
      "Publicity"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "updateArticleStatus",
    "method": "PATCH",
    "path": "/publicity/articles/{id}/{st}",
    "summary": "文章状态修改",
    "tags": [
      "Publicity"
    ],
    "pathParams": [
      "id",
      "st"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "likeArticle",
    "method": "POST",
    "path": "/publicity/articles/{id}/like",
    "summary": "点赞文章",
    "tags": [
      "Publicity"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "unlikeArticle",
    "method": "DELETE",
    "path": "/publicity/articles/{id}/like",
    "summary": "取消点赞",
    "tags": [
      "Publicity"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "favoriteArticle",
    "method": "POST",
    "path": "/publicity/articles/{id}/favorite",
    "summary": "收藏文章",
    "tags": [
      "Publicity"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "unfavoriteArticle",
    "method": "DELETE",
    "path": "/publicity/articles/{id}/favorite",
    "summary": "取消收藏",
    "tags": [
      "Publicity"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getFavorites",
    "method": "GET",
    "path": "/publicity/favorites",
    "summary": "我的收藏列表",
    "tags": [
      "Publicity"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "shareArticle",
    "method": "POST",
    "path": "/publicity/articles/{id}/share",
    "summary": "获取文章分享信息",
    "tags": [
      "Publicity"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getRescueTasks",
    "method": "GET",
    "path": "/tasks",
    "summary": "获取所有信息",
    "tags": [
      "RescueTask"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "beginRescueTask",
    "method": "PUT",
    "path": "/tasks",
    "summary": "准备上报新救助任务",
    "tags": [
      "RescueTask"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addRescueTask",
    "method": "POST",
    "path": "/tasks",
    "summary": "提交救助任务",
    "tags": [
      "RescueTask"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "id": "",
      "previousId": 0,
      "summary": "",
      "description": "",
      "type": ""
    },
    "formFields": []
  },
  {
    "id": "uploadRescueTaskMedia",
    "method": "POST",
    "path": "/tasks/{_id}/uploads",
    "summary": "信息上报时上传媒体",
    "tags": [
      "RescueTask"
    ],
    "pathParams": [
      "_id"
    ],
    "queryParams": [
      {
        "name": "file",
        "required": false,
        "type": "string",
        "description": ""
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "deleteRescueMediaWhenAdd",
    "method": "DELETE",
    "path": "/tasks/{_id}/uploads/{file}",
    "summary": "信息上报时删除媒体",
    "tags": [
      "RescueTask"
    ],
    "pathParams": [
      "_id",
      "file"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getRescueTask",
    "method": "GET",
    "path": "/tasks/{id}",
    "summary": "获取救助任务信息",
    "tags": [
      "RescueTask"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "updateRescueTask",
    "method": "PUT",
    "path": "/tasks/{id}",
    "summary": "更新救助任务信息",
    "tags": [
      "RescueTask"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "summary": "",
      "description": "",
      "type": ""
    },
    "formFields": []
  },
  {
    "id": "deleteRescueTask",
    "method": "DELETE",
    "path": "/tasks/{id}",
    "summary": "删除救助任务",
    "tags": [
      "RescueTask"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getStatusRecords_2",
    "method": "GET",
    "path": "/tasks/{id}/status",
    "summary": "获取任务状态记录",
    "tags": [
      "RescueTask"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "updateStatus_2",
    "method": "PATCH",
    "path": "/tasks/{id}/status",
    "summary": "修改任务状态",
    "tags": [
      "RescueTask"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "assignRescueTask",
    "method": "POST",
    "path": "/tasks/{id}/assign",
    "summary": "分配任务",
    "tags": [
      "RescueTask"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "deleteRescueMediaWhenUpdate",
    "method": "DELETE",
    "path": "/tasks/{id}/media/{mid}",
    "summary": "删除媒体文件",
    "tags": [
      "RescueTask"
    ],
    "pathParams": [
      "id",
      "mid"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getUser",
    "method": "GET",
    "path": "/users/{id}",
    "summary": "获取用户信息",
    "tags": [
      "User"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "updateUser",
    "method": "PUT",
    "path": "/users/{id}",
    "summary": "更新用户信息",
    "tags": [
      "User"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "username": "",
      "password": "",
      "email": "",
      "phone": "",
      "role": 0
    },
    "formFields": []
  },
  {
    "id": "removeUser",
    "method": "DELETE",
    "path": "/users/{id}",
    "summary": "删除用户",
    "tags": [
      "User"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "uploadAvatar",
    "method": "PATCH",
    "path": "/users/{id}/avatar",
    "summary": "上传头像",
    "tags": [
      "User"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [
      {
        "name": "avatar",
        "required": false,
        "type": "string",
        "description": ""
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "deleteAvatar",
    "method": "DELETE",
    "path": "/users/{id}/avatar",
    "summary": "删除头像",
    "tags": [
      "User"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getUserList",
    "method": "GET",
    "path": "/users",
    "summary": "获取用户列表",
    "tags": [
      "User"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addRecruitment",
    "method": "POST",
    "path": "/volunteers/recruitments",
    "summary": "创建招募计划",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "title": "",
      "description": "",
      "requirement": "",
      "headcount": 0,
      "startTime": "2026-04-24T00:00:00",
      "endTime": "2026-04-24T00:00:00"
    },
    "formFields": []
  },
  {
    "id": "getRecruitments",
    "method": "GET",
    "path": "/volunteers/recruitments",
    "summary": "查询招募计划",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "title",
        "required": false,
        "type": "string",
        "description": "志愿者招募计划查询参数"
      },
      {
        "name": "publisher",
        "required": false,
        "type": "integer",
        "description": "发布人 id"
      },
      {
        "name": "province",
        "required": false,
        "type": "string",
        "description": "省份"
      },
      {
        "name": "city",
        "required": false,
        "type": "string",
        "description": "城市"
      },
      {
        "name": "status",
        "required": false,
        "type": "array",
        "description": "招募状态集合"
      },
      {
        "name": "time0",
        "required": false,
        "type": "string",
        "description": "招募开始时间范围起点"
      },
      {
        "name": "time1",
        "required": false,
        "type": "string",
        "description": "招募开始时间范围终点"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getRecruitment",
    "method": "GET",
    "path": "/volunteers/recruitments/{id}",
    "summary": "获取招募计划",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "updateRecruitment",
    "method": "PUT",
    "path": "/volunteers/recruitments/{id}",
    "summary": "修改招募计划",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "title": "",
      "description": "",
      "requirement": "",
      "headcount": 0,
      "startTime": "2026-04-24T00:00:00",
      "endTime": "2026-04-24T00:00:00"
    },
    "formFields": []
  },
  {
    "id": "updateRecruitmentStatus",
    "method": "PATCH",
    "path": "/volunteers/recruitments/{id}/{st}",
    "summary": "修改招募计划状态",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [
      "id",
      "st"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addApplication",
    "method": "POST",
    "path": "/volunteers/applications",
    "summary": "提交志愿者申请",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "recruitmentId": 0,
      "realName": "",
      "phone": "",
      "sex": "",
      "age": 0,
      "profession": "",
      "experience": "",
      "skills": "",
      "availableTimeDesc": "",
      "motivation": ""
    },
    "formFields": []
  },
  {
    "id": "getApplications",
    "method": "GET",
    "path": "/volunteers/applications",
    "summary": "查询申请列表",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "recruitment",
        "required": false,
        "type": "integer",
        "description": "志愿者申请查询参数"
      },
      {
        "name": "user",
        "required": false,
        "type": "integer",
        "description": "申请人 id"
      },
      {
        "name": "reviewer",
        "required": false,
        "type": "integer",
        "description": "审核人 id"
      },
      {
        "name": "province",
        "required": false,
        "type": "string",
        "description": "省份"
      },
      {
        "name": "city",
        "required": false,
        "type": "string",
        "description": "城市"
      },
      {
        "name": "status",
        "required": false,
        "type": "array",
        "description": "申请状态集合"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getApplication",
    "method": "GET",
    "path": "/volunteers/applications/{id}",
    "summary": "获取申请详情",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "setApplicationStatus",
    "method": "PATCH",
    "path": "/volunteers/applications/{id}",
    "summary": "更新申请状态",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getProfiles",
    "method": "GET",
    "path": "/volunteers/profiles",
    "summary": "查询志愿者档案",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "user",
        "required": false,
        "type": "integer",
        "description": "志愿者档案查询参数"
      },
      {
        "name": "keyword",
        "required": false,
        "type": "string",
        "description": "关键字"
      },
      {
        "name": "status",
        "required": false,
        "type": "array",
        "description": "档案状态集合"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getProfile",
    "method": "GET",
    "path": "/volunteers/profiles/{id}",
    "summary": "获取志愿者档案",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "updateProfile",
    "method": "PUT",
    "path": "/volunteers/profiles/{id}",
    "summary": "修改志愿者档案",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "realName": "",
      "sex": "",
      "phone": "",
      "skills": "",
      "serviceIntention": "",
      "availableTimeDesc": "",
      "remark": ""
    },
    "formFields": []
  },
  {
    "id": "updateProfileStatus",
    "method": "POST",
    "path": "/volunteers/profiles/{id}/{st}",
    "summary": "修改志愿者档案状态",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [
      "id",
      "st"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addShift",
    "method": "POST",
    "path": "/volunteers/shifts",
    "summary": "创建排班",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "volunteerId": 0,
      "taskType": "",
      "taskId": 0,
      "title": "",
      "content": "",
      "startTime": "2026-04-24T00:00:00",
      "endTime": "2026-04-24T00:00:00",
      "taskStartTime": "2026-04-24T00:00:00",
      "taskEndTime": "2026-04-24T00:00:00",
      "remark": ""
    },
    "formFields": []
  },
  {
    "id": "getShifts",
    "method": "GET",
    "path": "/volunteers/shifts",
    "summary": "查询排班",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "volunteer",
        "required": false,
        "type": "integer",
        "description": "志愿者排班查询参数"
      },
      {
        "name": "assigner",
        "required": false,
        "type": "integer",
        "description": "排班人 id"
      },
      {
        "name": "keyword",
        "required": false,
        "type": "string",
        "description": "关键字"
      },
      {
        "name": "taskType",
        "required": false,
        "type": "array",
        "description": "任务类型集合"
      },
      {
        "name": "status",
        "required": false,
        "type": "array",
        "description": "状态集合"
      },
      {
        "name": "time0",
        "required": false,
        "type": "string",
        "description": "开始时间范围起点"
      },
      {
        "name": "time1",
        "required": false,
        "type": "string",
        "description": "开始时间范围终点"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getShift",
    "method": "GET",
    "path": "/volunteers/shifts/{id}",
    "summary": "获取排班详情",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "updateShift",
    "method": "PUT",
    "path": "/volunteers/shifts/{id}",
    "summary": "修改排班",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "volunteerId": 0,
      "startTime": "2026-04-24T00:00:00",
      "endTime": "2026-04-24T00:00:00"
    },
    "formFields": []
  },
  {
    "id": "updateShiftStatus",
    "method": "PATCH",
    "path": "/volunteers/shifts/{id}",
    "summary": "修改排班状态",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addServiceRecord",
    "method": "POST",
    "path": "/volunteers/shifts/{id}/records",
    "summary": "提交服务记录",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "startTime": "2026-04-24T00:00:00",
      "endTime": "2026-04-24T00:00:00",
      "actualHours": 0,
      "summary": "",
      "content": "",
      "problem": "",
      "suggestion": ""
    },
    "formFields": []
  },
  {
    "id": "getServiceRecords",
    "method": "GET",
    "path": "/volunteers/records",
    "summary": "查询服务记录",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "volunteer",
        "required": false,
        "type": "integer",
        "description": "志愿者服务记录查询参数"
      },
      {
        "name": "shift",
        "required": false,
        "type": "integer",
        "description": "排班 id"
      },
      {
        "name": "reviewer",
        "required": false,
        "type": "integer",
        "description": "审核人 id"
      },
      {
        "name": "status",
        "required": false,
        "type": "array",
        "description": "状态集合"
      },
      {
        "name": "time0",
        "required": false,
        "type": "string",
        "description": "服务开始时间范围起点"
      },
      {
        "name": "time1",
        "required": false,
        "type": "string",
        "description": "服务开始时间范围终点"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "updateServiceRecordStatus",
    "method": "PATCH",
    "path": "/volunteers/records/{id}",
    "summary": "审核服务记录",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getServiceRecord",
    "method": "GET",
    "path": "/volunteers/records/{id}",
    "summary": "获取服务记录详情",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "addReward",
    "method": "POST",
    "path": "/volunteers/rewards",
    "summary": "创建激励记录",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [],
    "queryParams": [],
    "contentType": "application/json",
    "bodyExample": {
      "volunteerId": 0,
      "periodStart": "2026-04-24T00:00:00",
      "periodEnd": "2026-04-24T00:00:00",
      "serviceCount": 0,
      "totalHours": 0,
      "rewardType": "",
      "rewardValue": "",
      "rewardReason": "",
      "remark": ""
    },
    "formFields": []
  },
  {
    "id": "getRewards",
    "method": "GET",
    "path": "/volunteers/rewards",
    "summary": "查询激励记录",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [],
    "queryParams": [
      {
        "name": "volunteer",
        "required": false,
        "type": "integer",
        "description": "志愿者激励查询参数"
      },
      {
        "name": "issuer",
        "required": false,
        "type": "integer",
        "description": "发放人 id"
      },
      {
        "name": "status",
        "required": false,
        "type": "array",
        "description": "激励状态集合"
      },
      {
        "name": "type",
        "required": false,
        "type": "array",
        "description": "激励类型集合"
      },
      {
        "name": "time0",
        "required": false,
        "type": "string",
        "description": "统计开始时间范围起点"
      },
      {
        "name": "time1",
        "required": false,
        "type": "string",
        "description": "统计开始时间范围终点"
      },
      {
        "name": "sort",
        "required": false,
        "type": "string",
        "description": "sort"
      },
      {
        "name": "order",
        "required": false,
        "type": "string",
        "description": "order"
      }
    ],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "getReward",
    "method": "GET",
    "path": "/volunteers/rewards/{id}",
    "summary": "获取激励详情",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  },
  {
    "id": "issueReward",
    "method": "POST",
    "path": "/volunteers/rewards/{id}/issue",
    "summary": "发放激励",
    "tags": [
      "Volunteer"
    ],
    "pathParams": [
      "id"
    ],
    "queryParams": [],
    "contentType": "",
    "bodyExample": null,
    "formFields": []
  }
]

export const OPENAPI_OPERATION_COUNT = OPENAPI_OPERATIONS.length
