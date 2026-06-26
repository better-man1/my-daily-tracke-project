# Daily Tracker Backend README

Daily Tracker 后台是一个基于 Spring Boot 3 的单体 REST API 服务，为 Vue H5、React H5 和 Uni-app 小程序提供统一的数据与认证能力。后台围绕个人效率管理场景展开，核心业务包括用户认证、每日计划、目标 OKR、记账、摘录、每日总结、仪表盘统计、文件存储、计划提醒和标签管理。

## 技术栈

| 类别 | 技术 | 用途 |
| --- | --- | --- |
| 运行时 | Java 17 | 后台服务运行环境 |
| Web 框架 | Spring Boot 3.2.5 | REST API、自动配置、内嵌 Tomcat |
| 安全 | Spring Security + JWT | 登录认证、接口保护、无状态鉴权 |
| ORM | MyBatis-Plus 3.5.7 | Mapper、Lambda 查询、逻辑删除、分页插件 |
| 数据库 | MySQL 8 | 业务数据持久化 |
| 缓存 | Redis | Refresh Token、缓存和临时数据能力 |
| 对象存储 | MinIO | 头像、附件、导出文件等对象存储 |
| API 文档 | Knife4j OpenAPI 3 | 接口文档与在线调试 |
| 工具 | Lombok、Hutool、MapStruct | 样板代码简化、工具方法、对象映射 |

## 总体架构

后台采用典型的分层单体架构，所有模块位于 `com.dailytracker` 包下：

```text
HTTP Request
  -> Spring Security Filter Chain
  -> JwtAuthenticationFilter
  -> Controller
  -> Service / ServiceImpl
  -> Mapper (MyBatis-Plus)
  -> MySQL / Redis / MinIO
```

主要分层职责如下：

- `controller`：REST API 入口，接收请求参数，返回统一 `Result<T>` 响应。
- `service`：业务接口定义，表达领域能力。
- `service.impl`：业务实现，负责事务、权限归属校验、状态流转、统计计算等逻辑。
- `mapper`：MyBatis-Plus 数据访问层，每个 Mapper 通常对应一张业务表。
- `entity`：数据库实体，字段与表结构对应。
- `dto.request`：请求 DTO，用于创建、更新、查询等入参。
- `dto.response`：响应 DTO，用于隔离实体和接口返回结构。
- `config`：Spring、Security、Redis、MinIO、Knife4j、CORS 等基础设施配置。
- `security`：JWT 生成、解析、过滤器和当前登录用户模型。
- `common`：统一响应、错误码、业务异常、基础实体和常量。
- `util`：日期、文件、安全上下文等通用工具。

## 目录结构

```text
backend/
├── pom.xml
├── src/main/java/com/dailytracker/
│   ├── DailyTrackerApplication.java     # Spring Boot 启动类，扫描 Mapper 并打印启动信息
│   ├── common/                          # 统一结果、异常、常量、基础类
│   ├── config/                          # 安全、跨域、Redis、MinIO、Knife4j、MyBatis-Plus 配置
│   ├── controller/                      # REST Controller
│   ├── dto/request/                     # 请求 DTO
│   ├── dto/response/                    # 响应 DTO
│   ├── entity/                          # 数据库实体
│   ├── mapper/                          # MyBatis-Plus Mapper
│   ├── security/                        # JWT、认证过滤器、安全用户对象
│   ├── service/                         # 业务服务接口
│   ├── service/impl/                    # 业务服务实现
│   └── util/                            # 工具类
└── src/main/resources/
    ├── application.yml                  # 通用配置
    ├── application-dev.yml              # 开发环境配置
    └── db/migration/                    # 数据库初始化和演进 SQL
```

## 核心业务模块

| 模块 | Controller | 主要能力 |
| --- | --- | --- |
| 用户认证 | `AuthController` | 注册、登录、刷新 Token、微信小程序登录 |
| 用户资料 | `UserController` | 用户资料、设置、密码等账号信息维护 |
| 每日计划 | `DailyPlanController` | 日计划 CRUD、状态更新、批量操作、重复计划、子任务、时间块统计 |
| 计划提醒 | `PlanReminderController` | 计划提醒设置和查询 |
| 计划标签 | `PlanTagController` | 标签创建、维护、关联计划 |
| 目标管理 | `GoalController` | 五年、年度、月度、周计划目标，OKR 关键结果，树形结构，进度统计 |
| 记账管理 | `AccountingController` | 收入支出记录、分类、预算、月度/年度统计 |
| 摘录管理 | `ExcerptController` | 摘录、感悟、收藏、标签、随机摘录 |
| 每日总结 | `SummaryController` | 日复盘、心情、成就、不足、趋势统计 |
| 仪表盘 | `DashboardController` | 今日、周、月、年维度聚合统计 |
| 文件 | `FileController` | 文件上传、对象存储访问 |

## API 设计约定

后台接口统一使用 `/api/v1` 前缀，例如：

```text
POST /api/v1/auth/login
GET  /api/v1/goals
GET  /api/v1/goals/tree
GET  /api/v1/daily-plans
```

Controller 返回统一响应体 `Result<T>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1700000000000
}
```

错误响应同样使用该结构，业务错误码集中在 `ResultCode` 中维护。全局异常由 `GlobalExceptionHandler` 统一转换，避免在 Controller 中散落 `try-catch`。

## 认证与安全实现

后台使用无状态 JWT 认证，不依赖服务端 Session。

登录流程：

1. 用户调用登录接口提交账号密码。
2. `AuthServiceImpl` 通过 Spring Security `AuthenticationManager` 校验凭据。
3. 校验成功后由 `JwtTokenProvider` 签发 Access Token 和 Refresh Token。
4. 前端后续请求携带请求头：`Authorization: Bearer <token>`。
5. `JwtAuthenticationFilter` 在请求进入 Controller 前解析 Token，并把用户信息写入 `SecurityContextHolder`。
6. 业务层通过 `SecurityUtils.getCurrentUserId()` 获取当前用户 ID，进行数据归属过滤。

安全配置位于 `SecurityConfig`：

- 关闭 CSRF，匹配前后端分离 JWT 模式。
- 使用 `SessionCreationPolicy.STATELESS`，服务端不创建 Session。
- 白名单放行注册、登录、刷新 Token 和 Knife4j 文档资源。
- 其他接口默认要求认证。
- 认证失败和权限不足统一返回 JSON 格式错误。
- 密码使用 BCrypt 存储和校验。

## 数据持久化

后台使用 MyBatis-Plus 操作 MySQL。主配置在 `application.yml`：

- 开启下划线到驼峰字段映射。
- 配置逻辑删除字段 `isDeleted`。
- 主键策略为数据库自增。
- 开发环境开启 SQL 控制台输出。

数据库脚本位于 `src/main/resources/db/migration/`，当前包含：

```text
V1__init_schema.sql        # 初始化用户、目标、计划、摘录、记账、总结等核心表
V2__init_data.sql          # 初始化基础数据
V3__add_repeat_support.sql # 重复计划支持
V4__add_subtask_support.sql# 子任务支持
V5__add_timeblock_support.sql
V6__add_reminder_support.sql
V7__add_tag_support.sql
```

虽然目录名是 `db/migration`，当前项目没有引入 Flyway 依赖；首次部署时需要手动创建 `daily_tracker` 数据库，并按版本号顺序执行 SQL。

## 关键领域实现

### 每日计划

每日计划以 `t_daily_plan` 为核心表，支持计划日期、优先级、分类、预估/实际耗时、状态、排序、模板、重复计划、子任务、提醒和标签。业务实现集中在 `DailyPlanServiceImpl`，常见逻辑包括：

- 按日期查询计划列表。
- 更新计划状态和完成时间。
- 批量更新计划。
- 生成重复计划实例。
- 使用 `parentId` 表达子任务关系。
- 聚合完成趋势、分类分布、优先级分布和时间分布。

### 目标管理

目标模块采用 OKR 风格设计，核心表为 `t_goal_plan` 和 `t_goal_kr`。

目标层级为：

```text
五年规划 -> 年度目标 -> 月度目标 -> 周计划
```

`GoalServiceImpl` 的关键逻辑：

- 创建目标时校验父目标存在且属于当前用户。
- `getTree` 根据 `parentId` 在内存中组装树形结构。
- 关键结果保存时计算 KR 进度。
- 目标进度达到 100% 时自动标记为 `COMPLETED`。
- 统计总数、不同状态数量、类型分布、分类分布和平均进度。

### 记账管理

记账模块包含分类、账目和预算：

- `t_accounting_category` 支持系统预设分类和用户自定义分类。
- `t_accounting` 记录收入、支出、账户类型、凭证图片和记账日期。
- `t_budget` 记录月度总预算或分类预算。
- `AccountingServiceImpl` 提供月度、年度、分类统计能力。

### 摘录和总结

摘录模块用 `t_excerpt` 记录内容、来源、链接、感悟、图片和收藏状态，使用 `t_tag` 与 `t_excerpt_tag_rel` 支持标签能力。

总结模块用 `t_daily_summary` 记录每天的复盘内容，包括心情、评分、成就、不足、明日计划、感恩记录、健康记录和自由书写。`SummaryServiceImpl` 负责唯一日期记录、列表查询和统计趋势。

### 仪表盘统计

`DashboardServiceImpl` 聚合计划、目标、记账、摘录和总结模块数据，为前端看板提供今日、周、月、年等维度的汇总结果。它不拥有独立业务表，主要承担跨模块查询和结果组装。

## 文件存储

文件能力通过 MinIO 实现，配置位于 `application-dev.yml`：

```yaml
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: daily-tracker
```

相关类：

- `MinioProperties`：绑定 MinIO 配置。
- `MinioConfig`：创建 MinIO 客户端。
- `FileStorageService`：文件存储抽象。
- `MinioFileStorageServiceImpl`：MinIO 实现。
- `FileController`：文件上传和访问入口。

## 配置说明

通用配置：`src/main/resources/application.yml`

- `server.port`: 默认 `8080`。
- `spring.profiles.active`: 默认 `dev`。
- `spring.jackson`: 日期格式、时区和 null 字段序列化策略。
- `mybatis-plus`: 字段映射、SQL 日志、逻辑删除、主键策略。
- `jwt`: Token 密钥和过期时间。
- `knife4j`: API 文档开关。
- `logging`: 项目和 Spring Security 日志等级。

开发环境配置：`src/main/resources/application-dev.yml`

- MySQL: `localhost:3306/daily_tracker`。
- Redis: `localhost:6379`。
- MinIO: `localhost:9000`。
- 微信小程序: `wx.app-id` 和 `wx.app-secret` 占位配置。

生产环境不要直接复用开发配置中的密码、JWT secret、MinIO 密钥和微信密钥，应改用环境变量、独立 profile 或配置中心。

## 本地运行

准备环境：

- JDK 17
- Maven 3.8+
- MySQL 8
- Redis
- MinIO，只有使用文件能力时必须启动

初始化数据库：

```sql
CREATE DATABASE IF NOT EXISTS daily_tracker
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

然后按 `src/main/resources/db/migration/` 中的版本号顺序执行 SQL。

启动后台：

```bash
cd backend
mvn spring-boot:run
```

打包：

```bash
cd backend
mvn clean package
```

如果希望使用项目内 Maven 本地仓库，可执行：

```bash
mvn -Dmaven.repo.local=.m2/repository clean package
```

启动成功后访问：

```text
API Base URL : http://localhost:8080
Knife4j Docs : http://localhost:8080/doc.html
```

## 与前端的关系

前端请求路径通常以 `/api` 开头，通过 Vite proxy 转发到后台 `http://localhost:8080`。后台 Controller 自身使用 `/api/v1` 作为接口前缀，因此真实接口类似：

```text
http://localhost:8080/api/v1/auth/login
http://localhost:8080/api/v1/goals/tree
```

Vue H5、React H5 和 Uni-app 小程序共用同一套后台接口。小程序在真机调试时需要根据网络环境调整 `miniapp/src/utils/config.ts` 中的 API 地址。

## 开发约定

新增后台功能时，建议按现有分层补齐文件：

1. 在 `entity` 中定义或扩展数据库实体。
2. 在 `dto.request` 和 `dto.response` 中定义接口入参和出参。
3. 在 `mapper` 中新增 MyBatis-Plus Mapper。
4. 在 `service` 中定义业务接口。
5. 在 `service.impl` 中实现业务逻辑，并通过 `SecurityUtils` 做当前用户归属约束。
6. 在 `controller` 中暴露 REST API，返回 `Result<T>`。
7. 如涉及表结构，新增递增版本号 SQL 文件。
8. 如接口需要公开访问，谨慎更新 `SecurityConfig` 白名单。

原则上不要让 Controller 直接访问 Mapper；业务校验、事务和跨表操作应放在 Service 层。

## 常见排查点

- 接口返回 401：检查 `Authorization: Bearer <token>` 是否存在、Token 是否过期、接口是否应加入白名单。
- 数据为空：检查当前登录用户 ID，后台大多数查询都会按 `userId` 过滤。
- SQL 未生效：确认数据库是否为 `daily_tracker`，迁移 SQL 是否按顺序执行。
- 文件上传失败：确认 MinIO 是否启动、bucket 是否存在、`application-dev.yml` 密钥是否正确。
- Knife4j 无法访问：确认 `/doc.html`、`/webjars/**`、`/v3/api-docs/**` 是否仍在白名单中。
- 日期偏差：确认 Jackson 和数据库连接都使用 `Asia/Shanghai` 时区。