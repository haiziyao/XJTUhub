# Search 模块接口文档

本文档描述当前已经实现的 `search` 模块接口。

基础路径：

```text
/api/v1/search
```

全局响应包络、统一错误结构继承自：

- `doc/backend/prd/global-api-contracts.md`
- `doc/backend/search.md`

## 1. 搜索占位接口

```text
GET /api/v1/search
```

用途：

- 为前端和后续 Elasticsearch 接入提供稳定搜索接口占位。

查询参数：

- `q`：搜索关键词，可为空。
- `page`：页码，可为空，默认 `1`。
- `pageSize`：分页大小，可为空，默认 `10`，最大 `50`。

成功响应 `data`：

```json
{
  "query": "xjtu",
  "items": [],
  "page": 1,
  "pageSize": 10,
  "total": 0,
  "hasNext": false,
  "indexStatus": "placeholder"
}
```

当前行为：

- 当前阶段不连接真实 Elasticsearch。
- 默认返回空结果，供前端联调和接口契约测试使用。
- `indexStatus` 固定返回 `placeholder`，用于明确表示当前是占位实现。

## 2. 索引任务占位接口

```text
GET /api/v1/search/index-tasks
```

用途：

- 为后续索引任务监控页和后台调试页提供稳定接口占位。

成功响应 `data`：

```json
{
  "items": [
    {
      "taskType": "content_index_sync",
      "targetType": "content",
      "status": "placeholder",
      "detail": "Elasticsearch is not connected yet. Placeholder task state is returned for integration testing."
    }
  ]
}
```

当前行为：

- 当前返回固定占位任务状态。
- 后续接入真实 ES 后，这里会切换为 `search_index_tasks` 任务读取和诊断视图。

## 3. 当前实现说明

- 当前 `search` 模块只提供稳定 API 骨架。
- 真实搜索、分词、排序、高亮、索引重建尚未接入。
- 当前返回值主要用于：
  - 前端联调
  - 契约测试
  - 后续 ES 接入前的模块边界稳定化
