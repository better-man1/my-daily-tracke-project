# Daily Tracker - 个人每日记录与生产力管理系统

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Vue](https://img.shields.io/badge/Vue-3.5-brightgreen.svg)
![React](https://img.shields.io/badge/React-19-blue.svg)
![Uni-app](https://img.shields.io/badge/Uni--app-Vue3-blue.svg)

Daily Tracker 是一个个人效率与日常记录系统，覆盖后端服务、Vue H5 管理端、React H5 管理端和 Uni-app 小程序端。系统围绕每日计划、目标管理、记账、感悟摘录、每日总结和数据看板展开，适合用于个人事务追踪与多端学习实践。

## 核心功能

- **用户与认证**：注册、登录、JWT 鉴权、用户资料与设置维护。
- **仪表盘统计**：展示今日、周、月、年等维度的数据概览和趋势。
- **计划管理**：支持每日计划、子任务、重复计划、时间块、批量操作、计划提醒和计划标签。
- **目标管理**：维护目标和关键结果，追踪目标进度。
- **记账管理**：记录收入与支出，支持分类、预算和统计分析。
- **感悟摘录**：记录摘录内容，支持标签、收藏、随机摘录和导出。
- **每日总结**：记录每日复盘内容，支持连续记录和心情趋势。
- **文件能力**：后端集成 MinIO，用于头像、附件或导出文件等对象存储场景。

## 技术栈

### 后端 backend

- Spring Boot 3.2.5
- Java 17
- MyBatis-Plus 3.5.7
- MySQL 8
- Redis
- Spring Security + JWT
- Knife4j OpenAPI 3
- MinIO
- Hutool / MapStruct / Lombok

### Vue 前端 frontend

- Vue 3.5
- Vite 8
- TypeScript
- Element Plus
- Pinia
- Vue Router
- ECharts
- Axios
- Sass

### React 前端 react-frontend

- React 19
- Vite 8
- TypeScript
- Ant Design
- Zustand
- React Router
- ECharts
- Axios
- Sass

### 小程序 miniapp

- Uni-app Vue 3
- TypeScript
- Pinia
- Sass
- 微信小程序端构建

## 项目结构

```text
my-daily-project-claude/
├── backend/              # Spring Boot 后端服务
├── frontend/             # Vue 3 H5 前端
├── react-frontend/       # React H5 前端
├── miniapp/              # Uni-app 微信小程序
├── study/                # 学习总结与技术文档
├── .husky/               # Git hooks 配置
├── .claude/              # Claude/Codex 相关本地配置
├── commitlint.config.js  # Git 提交信息规范配置
├── .gitignore
└── README.md
```

## 本地环境要求

建议准备以下环境：

- JDK 17
- Maven 3.8+（当前项目也可先尝试使用已有 Maven）
- Node.js 18+（本机 Node 22 也可运行）
- npm 10+
- MySQL 8
- Redis
- MinIO（使用文件上传、头像、附件或导出存储时需要）

默认端口：

| 服务 | 端口 | 说明 |
| --- | --- | --- |
| 后端 API | 8080 | Spring Boot 服务 |
| Vue H5 | 5173 | Vite 开发服务器 |
| React H5 | 5179 | Vite 开发服务器 |
| MySQL | 3306 | 默认数据库服务 |
| Redis | 6379 | 默认缓存服务 |
| MinIO API | 9000 | 对象存储 API |
| MinIO Console | 9001 | 对象存储控制台 |

## 后端配置

后端默认启用 `dev` profile：

```yaml
spring:
  profiles:
    active: dev
```

开发环境配置位于：

```text
backend/src/main/resources/application-dev.yml
```

默认连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/daily_tracker?useSSL=false&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: root
  data:
    redis:
      host: localhost
      port: 6379

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: daily-tracker
```

如本机 MySQL、Redis 或 MinIO 配置不同，请先修改 `application-dev.yml`。

## 数据库初始化

数据库名默认为：

```text
daily_tracker
```

初始化脚本位于：

```text
backend/src/main/resources/db/migration/
```

当前脚本顺序：

```text
V1__init_schema.sql
V2__init_data.sql
V3__add_repeat_support.sql
V4__add_subtask_support.sql
V5__add_timeblock_support.sql
V6__add_reminder_support.sql
V7__add_tag_support.sql
```

如果是首次运行，可先创建数据库，再按文件名顺序执行 SQL：

```sql
CREATE DATABASE IF NOT EXISTS daily_tracker DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 启动后端

```bash
cd backend
mvn spring-boot:run
```

启动成功后可访问：

- API 根地址：`http://localhost:8080`
- Knife4j 文档：`http://localhost:8080/doc.html`

## 启动 Vue 前端

```bash
cd frontend
npm install
npm run dev
```

访问地址：

```text
http://localhost:5173
```

Vue 前端通过 Vite 代理将 `/api` 请求转发到：

```text
http://localhost:8080
```

## 启动 React 前端

```bash
cd react-frontend
npm install
npm run dev
```

访问地址：

```text
http://localhost:5179
```

React 前端同样通过 Vite 代理将 `/api` 请求转发到：

```text
http://localhost:8080
```

## 启动小程序端

```bash
cd miniapp
npm install
```

微信小程序开发构建：

```bash
npm run dev:mp-weixin
```

微信小程序生产构建：

```bash
npm run build:mp-weixin
```

H5 调试：

```bash
npm run dev:h5
```

编译后可使用微信开发者工具导入对应输出目录。

## 常用命令

### 后端

```bash
cd backend
mvn clean package
mvn spring-boot:run
```

### Vue 前端

```bash
cd frontend
npm run dev
npm run build
npm run preview
```

### React 前端

```bash
cd react-frontend
npm run dev
npm run build
npm run preview
```

### 小程序

```bash
cd miniapp
npm run dev:mp-weixin
npm run build:mp-weixin
npm run dev:h5
npm run build:h5
```

## 本地启动建议

推荐按以下顺序启动：

1. 启动 MySQL，并确认 `daily_tracker` 数据库和表结构已初始化。
2. 启动 Redis。
3. 如需要文件上传或对象存储功能，启动 MinIO 并创建 `daily-tracker` bucket。
4. 启动后端 `backend`。
5. 选择启动 `frontend` 或 `react-frontend`。
6. 如需调试移动端，再启动 `miniapp`。

## 一键本地启动

数据库和表结构已初始化后，可以使用项目根目录下的脚本同时启动后端、Vue 前端和 React 前端：

```bash
start-local.bat
```

也可以直接运行 PowerShell 脚本：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\start-local.ps1
```

脚本会启动：

- 后端：`http://localhost:8080`
- Vue 前端：`http://localhost:5173`
- React 前端：`http://localhost:5179`
- Knife4j 文档：`http://localhost:8080/doc.html`

如果前端 `node_modules` 不存在，脚本会自动执行 `npm install`。如需跳过依赖安装检查，可使用：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\start-local.ps1 -SkipInstall
```

## 注意事项

- `frontend` 和 `react-frontend` 是两套 H5 前端实现，默认都连接同一个后端 API。
- 后端接口统一使用 `/api/v1` 前缀，前端通过 Vite proxy 转发到 `localhost:8080`。
- 小程序端在真实微信环境中访问本机接口时，需要根据调试方式调整 `miniapp/src/utils/config.ts` 中的接口地址。
- `application-dev.yml` 中的数据库密码、JWT secret、MinIO 密钥和微信小程序配置仅适合本地开发，生产环境应改用环境变量或独立配置。
- `study/` 目录用于存放学习文档，不参与应用运行。
