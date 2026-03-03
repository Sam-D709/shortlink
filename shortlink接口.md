---
title: 默认模块
language_tabs:
  - shell: Shell
  - http: HTTP
  - javascript: JavaScript
  - ruby: Ruby
  - python: Python
  - php: PHP
  - java: Java
  - go: Go
toc_footers: []
includes: []
search: true
code_clipboard: true
highlight_theme: darkula
headingLevel: 2
generator: "@tarslib/widdershins v4.0.30"

---

# 默认模块

Base URLs:

# Authentication

# 用户模块

## GET 用户名获取信息

GET /api/shortlink/admin/user/username

根据用户名获取用户信息

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|username|header|string| 否 |none|
|token|header|string| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## PUT 更新用户信息

PUT /api/shortlink/admin/user/update

> Body 请求参数

```json
{
  "password": "string",
  "realname": "string",
  "phone": "string",
  "email": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|username|header|string| 否 |none|
|token|header|string| 否 |none|
|body|body|object| 是 |none|
|» password|body|string| 是 |密码|
|» realname|body|string| 是 |真实姓名|
|» phone|body|string| 是 |手机号|
|» email|body|string| 是 |邮箱|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## POST 用户注册

POST /api/shortlink/admin/user/register

用户注册接口,需要提供一个body,参数有username(必须),password(必须),realname(可选),phone(可选),email(可选)

> Body 请求参数

```json
{
  "username": "123456",
  "password": "123456789",
  "realname": "admin",
  "phone": "12345641545",
  "email": "123456789@qq.com"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|username|header|string| 否 |none|
|token|header|string| 否 |none|
|body|body|object| 是 |none|
|» username|body|string| 是 |用户名|
|» password|body|string| 是 |密码|
|» realname|body|string| 是 |真实姓名|
|» phone|body|string| 是 |手机号|
|» email|body|string| 是 |邮箱|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## POST 用户登录

POST /api/shortlink/admin/user/login

> Body 请求参数

```json
{
  "username": "123",
  "password": "45618"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|username|header|string| 否 |none|
|token|header|string| 否 |none|
|body|body|object| 是 |none|
|» username|body|string| 是 |用户名|
|» password|body|string| 是 |密码|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## POST 用户登出

POST /api/shortlink/admin/user/logout

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|username|header|string| 否 |none|
|token|header|string| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## GET 判断用户名是否存在

GET /api/shortlink/admin/user/hasUsername

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|username|query|string| 是 |用户名|
|token|header|string| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

# 短链接分组模块

## GET 获取短链接分组内容

GET /api/shortlink/admin/group/list

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|username|header|string| 否 |none|
|token|header|string| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## POST 创建短链接分组

POST /api/shortlink/admin/group/create

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|groupName|query|string| 是 |分组自定义的名称|
|username|header|string| 否 |none|
|token|header|string| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## PUT 更改分组名

PUT /api/shortlink/admin/group/updatename

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|gid|query|string| 是 |分组标识id|
|groupName|query|string| 是 |更改的分组名|
|username|header|string| 否 |none|
|token|header|string| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## DELETE 删除分组

DELETE /api/shortlink/admin/group/delete

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|gid|query|string| 否 |分组标识id|
|username|header|string| 否 |none|
|token|header|string| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## POST 更新分组排序

POST /api/shortlink/admin/group/order

> Body 请求参数

```json
[
  {
    "gid": "J1HivX",
    "sortOrder": "10"
  },
  {
    "gid": "RYrgzA",
    "sortOrder": "20"
  }
]
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|username|header|string| 否 |none|
|token|header|string| 否 |none|
|body|body|array[object]| 是 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

# 短链接管理模块

## POST 短链接创建

POST /api/shortlink/project/link/create

> Body 请求参数

```json
{
  "domain": "string",
  "originurl": "string",
  "gid": "string",
  "createdtype": "string",
  "validdatetype": "string",
  "validdate": "string",
  "description": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|username|header|string| 否 |none|
|token|header|string| 否 |none|
|body|body|object| 是 |none|
|» domain|body|string| 是 |域名(项目部署的域名)|
|» originurl|body|string| 是 |原始链接|
|» gid|body|string| 是 |短链接分组序号|
|» createdtype|body|string| 是 |创建方式,0：接口创建 1：控制台创建|
|» validdatetype|body|string| 是 |有效期类型,0：永久有效 1：自定义|
|» validdate|body|string| 是 |有效期(采用datetime格式)|
|» description|body|string| 是 |描述|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## GET 短链接分页查询

GET /api/shortlink/project/getpagelink

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|gid|query|string| 是 |分组标识|
|current|query|string| 是 |当前页|
|size|query|string| 是 |页面数据大小|
|username|header|string| 否 |none|
|token|header|string| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## GET 根据短链接分组标识查询相应短链接总数

GET /api/shortlink/project/getlinkcount

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|gid|query|array[string]| 是 |数组元素,可以存在多个gid|
|username|header|string| 否 |none|
|token|header|string| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## PUT 短链接更新基本参数(不包含分组更新)

PUT /api/shortlink/project/link/updatebase

> Body 请求参数

```json
{
  "id": "string",
  "gid": "string",
  "originurl": "string",
  "createdtype": "string",
  "validdatetype": "string",
  "validdate": "string",
  "description": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|username|header|string| 否 |none|
|token|header|string| 否 |none|
|body|body|object| 是 |none|
|» id|body|string| 是 |短链接id标识|
|» gid|body|string| 是 |分组标识|
|» originurl|body|string| 是 |新原始链接|
|» createdtype|body|string| 是 |创建标识:0：接口创建 1：控制台创建|
|» validdatetype|body|string| 是 |有效期:0：永久有效 1：自定义|
|» validdate|body|string| 是 |有效期时长,采用datetime格式|
|» description|body|string| 是 |短链接描述|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## PUT 短链接更新分组

PUT /api/shortlink/project/link/updategid

> Body 请求参数

```json
{
  "id": "3",
  "oldGid": "s1n5HM",
  "newGid": "tfLydi"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|username|header|string| 否 |none|
|token|header|string| 否 |none|
|body|body|object| 是 |none|
|» id|body|string| 是 |短链接id|
|» oldGid|body|string| 是 |旧分组标识id|
|» newGid|body|string| 是 |新分组标识id|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## GET 短链接跳转

GET /{shortlink}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|shortlink|path|string| 是 |短链接部分,并非完整短链接,而是6位字母数字组合|
|username|header|string| 否 |none|
|token|header|string| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

# 回收站模块

## GET 分页获取该用户回收站的所有短链接

GET /api/shortlink/project/recyclelink/getpage

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|current|query|string| 是 |当前页数|
|size|query|string| 是 |每页大小|
|username|header|string| 否 |none|
|token|header|string| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## PUT 将短链接移动到回收站

PUT /api/shortlink/project/recyclelink/create

> Body 请求参数

```json
{
  "gid": "zFmtb0",
  "id": [
    "457"
  ]
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|username|header|string| 否 |none|
|token|header|string| 否 |none|
|body|body|object| 是 |none|
|» gid|body|string| 是 |分组标识id|
|» id|body|[string]| 是 |数组元素,可以有多个id|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## PUT 从回收站恢复短链接

PUT /api/shortlink/project/recyclelink/recover

> Body 请求参数

```json
{
  "gid": "zFmtb0",
  "id": [
    "457"
  ]
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|username|header|string| 否 |none|
|token|header|string| 否 |none|
|body|body|object| 是 |none|
|» gid|body|string| 是 |分组标识id|
|» id|body|[string]| 是 |数组元素,可以有多个id|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## DELETE 彻底删除短链接

DELETE /api/shortlink/project/recyclelink/delete

> Body 请求参数

```json
{
  "gid": "tfLydi",
  "id": [
    "10",
    "11"
  ]
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|username|header|string| 否 |none|
|token|header|string| 否 |none|
|body|body|object| 是 |none|
|» gid|body|string| 是 |分组标识id|
|» id|body|[string]| 是 |数组元素,可以有多个id|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

# 短链接监控模块

## GET 获取默认短链接监控信息

GET /api/shortlink/project/linkstate/defaultstate

获取默认短链接监控信息,包含过去24h的pv,uv数字,今日的访问请求中os类型,device类型和browser类型

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|fullshorturl|query|string| 否 |完整短链接|
|username|header|string| 否 |none|
|token|header|string| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## GET 获取指定时间段内的短链接监控数据

GET /api/shortlink/project/linkstate/daystate

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|fullshorturl|query|string| 是 |完整短链接|
|startDate|query|string| 是 |起始日期,datetime形式|
|endDate|query|string| 是 |结束日期,datetime形式|
|username|header|string| 否 |none|
|token|header|string| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## GET 获取每月的短链接监控数据

GET /api/shortlink/project/linkstate/monthstate

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|fullshorturl|query|string| 是 |完整短链接|
|startMonth|query|string| 是 |起始日期,年份-月份|
|endMonth|query|string| 是 |结束日期,年份-月份|
|username|header|string| 否 |none|
|token|header|string| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

# 数据模型

