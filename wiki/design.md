# 设计文档：API 通知系统

## 目标
- 对内提供统一、标准化的 HTTP API。
- 负责将通知请求可靠投递到外部系统。
- 业务系统不关心外部返回值，仅关注投递尽可能可靠。

## 系统边界
系统内解决：
- 统一接入与基础校验。
- 投递任务持久化与状态记录。
- 失败重试与投递状态可追踪。

系统内不解决（第一版）：
- 供应商协议适配/编排与复杂模板。
- 精确一次投递与分布式事务。
- 运营后台与 SLA 管理。

## 总体架构
```
业务系统 --> 接入 API --> 任务存储(数据库) --> 调度/扫描 --> 投递执行器 --> 外部供应商
```

说明：
- 接入 API 负责接收通知请求并写入任务表，快速返回已接收。
- 调度/扫描负责找出待投递或到期重试的任务。
- 投递执行器调用外部 API，失败则记录原因并更新重试信息。

## 核心组件
1. API 接入层
   - 统一接口：POST /notifications
   - 参数：target_url、method、headers、body、timeout_ms、idempotency_key
2. 持久化存储
   - 记录通知任务与状态
3. 调度/扫描器
   - 周期性扫描到期任务
4. 投递执行器
   - 调用外部 API 并处理失败重试

## 关键数据模型（示意）
Notification
- id
- target_url
- method
- headers
- body
- status (pending/sending/succeeded/failed)
- attempts
- next_retry_at
- last_error
- created_at
- updated_at

## 数据库设计（示意）
表：notifications
- id (PK, string/uuid)
- target_url (varchar)
- method (varchar)
- headers (json/text)
- body (text)
- status (varchar)
- attempts (int)
- next_retry_at (timestamp)
- last_error (text)
- created_at (timestamp)
- updated_at (timestamp)

索引建议：
- idx_status_next_retry_at (status, next_retry_at)
- idx_created_at (created_at)

状态约定：
- pending：待投递
- sending：投递中（可选）
- succeeded：投递成功
- failed：投递失败但仍可重试
- dead：超过重试阈值，进入死信

## 接口设计（对内）
### 创建通知
POST /notifications
请求体：
```
{
  "target_url": "https://example.com/webhook",
  "method": "POST",
  "headers": {"Content-Type": "application/json"},
  "body": "{\"foo\":\"bar\"}",
  "timeout_ms": 3000,
  "idempotency_key": "order-123"
}
```
响应体：
```
{
  "id": "uuid",
  "status": "accepted",
  "accepted_at": "2024-01-01T00:00:00Z"
}
```

### 查询通知状态（可选）
GET /notifications/{id}
响应体：
```
{
  "id": "uuid",
  "status": "pending|sending|succeeded|failed|dead",
  "attempts": 2,
  "next_retry_at": "2024-01-01T00:05:00Z",
  "last_error": "timeout"
}
```

## 可靠性策略
- 投递语义：至少一次。
- 失败重试：指数退避 + 抖动；达到最大次数或时间窗口后进入死信。
- 幂等性：支持 idempotency_key，幂等需由业务方或供应商保证。

## 失败与异常处理
- 外部系统超时/不可用：记录失败并延迟重试。
- 外部系统长期不可用：进入死信并告警，支持手动重放。

## 取舍与演进
### 取舍
- MVP 使用数据库 + 扫描方式，降低系统复杂度。
- 不引入 Kafka，避免在延迟调度/编排上增加复杂度。

### 演进
- 流量增长：引入 Redis 作为延迟队列，提升吞吐与调度精度。
- 供应商多样化：支持配置化模板与签名策略。
- 可观测性：完善监控、告警与追踪。
