# 搜索

> **权威性说明：** 本文件是 `doc/backend/search.md` 的中文镜像，只供人工阅读。AI agent 和实现工作必须以英文 `doc/` 为准。

XJTUhub 从 Phase 1 开始使用 Elasticsearch。

## 事实数据源

- MySQL 是事实数据源。
- Elasticsearch 是搜索索引。
- 永远不要把 Elasticsearch 作为内容、权限、审核状态或用户身份的权威存储。

## 搜索模块

后端 `search` 模块拥有：

- 索引 mapping。
- 索引写入任务。
- 重建索引。
- 搜索 API。
- 搜索结果 DTO。
- 搜索诊断。

其他模块应发布搜索索引事件，或调用搜索应用服务。

## 索引

内容索引必须异步且可重试。

必需行为：

- 发布或更新内容会更新索引。
- 隐藏、删除、拒绝或权限变化的内容会更新或移除索引可见性。
- 失败的索引任务可以重试。
- 全量重建可以从 MySQL 重建。

## 索引版本

使用版本化索引名，例如：

```text
xjtuhub_contents_v1
```

需要重建索引的 mapping 变更应创建新的版本化索引。

## 初始内容索引字段

建议字段：

- content id。
- type。
- title。
- body excerpt。
- tags。
- board id。
- board name。
- author id。
- author display name。
- anonymous display state。
- visibility。
- status。
- review status。
- published time。
- updated time。
- popularity score。

## 前端访问

前端必须调用后端搜索 API，不能直接调用 Elasticsearch。
