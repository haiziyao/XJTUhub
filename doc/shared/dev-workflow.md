# 开发流程

XJTUhub 采用文档优先的开发方式。

## 总体流程

1. 在需要时定义或更新产品约束。
2. 为某项能力编写后端 PRD。
3. 编写或更新 API 与数据契约。
4. 推导前端页面规格。
5. 在需要时生成视觉提示词和参考图。
6. 按已批准文档实现后端与前端。
7. 每实现一个后端功能，都必须同步编写或更新对应模块的接口文档，位置为 `doc/backend/modules/<module>/`。
8. 补充测试。
9. 更新 `doc/shared/task-board.md`、`doc/shared/task-completion-log.md` 与决策文档。

## 前后端分离

- 后端文档位于 `doc/backend`。
- 模块接口文档位于 `doc/backend/modules/<module>`。
- 前端文档位于 `doc/frontend`。
- 共享术语与流程文档位于 `doc/shared`。
- 跨 agent 协作任务台账位于 `doc/shared/task-board.md` 与 `doc/shared/task-completion-log.md`。
- 允许交叉引用。
- 混合业务规则与视觉实现的文档必须拆分。
- 接口文档必须按模块拆分，不能持续堆到一个全局实现说明里。
- 模块接口文档默认使用中文。

## 规则变更

当某项约束发生变化时：

1. 更新所属文档。
2. 如果变更影响架构、流程、安全或产品范围，则补充决策记录。
3. 更新受影响文档。
4. 在实现说明或 PR 描述中说明变化。

## 多 Agent 协作

- 跨 agent 协作时，以 `doc/shared/task-board.md` 作为唯一总任务表。
- 完成任务后，必须在 `doc/shared/task-completion-log.md` 追加完成记录。
- `TODO.md` 可以继续保留作顶层简略提示，但不能替代任务表和完成记录。

## 分支与提交建议

尽量保持提交聚焦。

建议的 commit 前缀：

- `docs:`
- `feat:`
- `fix:`
- `test:`
- `refactor:`
- `chore:`

## 评审检查点

评审时应重点检查：

- 变更是否遵循相关 PRD / 规格？
- 前后端职责是否清晰分离？
- 权限是否由后端强制执行？
- 第三方 provider SDK 是否隔离在 adapter 后面？
- 高风险行为是否附带测试？
