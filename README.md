# Daily Tracker - 个人每日记录与生产力管理系统

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)
![Vue](https://img.shields.io/badge/Vue-3.5-brightgreen.svg)
![Uni-app](https://img.shields.io/badge/Uni--app-Vue3-blue.svg)

Daily Tracker 是一个全栈个人效率管理平台，旨在帮助用户记录每日收支、个人计划、感悟摘录并进行多维度的数据分析。系统包含后端服务、H5 管理端以及微信小程序端。

## 🌟 核心功能

- **📊 仪表盘统计**：直观展示每日/周/月的数据趋势，包括计划完成率、收支对比等。
- **💰 记账管理**：便捷记录每日收支，支持按类型过滤和分类统计。
- **📝 计划管理**：管理每日待办事项，追踪任务进度。
- **📔 感悟摘录**：记录每日心得体会，支持趋势分析。
- **🤖 AI 助手**：基于 AI 的实时建议引擎，提供个性化生产力提升方案。
- **📱 多端覆盖**：
  - **H5 端**：基于 Vue 3 + Element Plus，适合桌面和移动端浏览器。
  - **小程序端**：基于 Uni-app，完美适配微信生态，支持移动端便捷操作。

## 🛠️ 技术栈

### 后端 (Backend)
- **核心框架**：Spring Boot 3.2.5, Java 17
- **持久层**：MyBatis-Plus, MySQL
- **安全校验**：Spring Security, JWT (jjwt)
- **缓存/中间件**：Redis
- **工具库**：Hutool, MapStruct, Lombok
- **API 文档**：Knife4j (OpenAPI 3)

### 前端 (Frontend)
- **框架**：Vue 3.5 (Composition API)
- **构建工具**：Vite 6.0
- **UI 组件库**：Element Plus
- **状态管理**：Pinia
- **图表库**：ECharts
- **开发语言**：TypeScript

### 小程序 (Mini-program)
- **框架**：Uni-app (Vue 3)
- **开发语言**：TypeScript
- **UI 组件**：Uni-ui
- **样式**：Sass/Scss

## 📂 项目结构

```text
my-daily-project-claude/
├── backend/            # Spring Boot 后端源码
├── frontend/           # Vue 3 H5 前端源码
├── miniapp/            # Uni-app 微信小程序源码
├── design/             # 项目设计文档及资源
├── deploy/             # 部署相关脚本/配置
└── .gitignore          # Git 忽略配置
```

## 🚀 快速开始

### 1. 后端启动
1. 确保已安装 JDK 17 和 Maven。
2. 配置 `backend/src/main/resources/application.yml` 中的 MySQL 和 Redis 连接。
3. 执行 Maven 命令：
   ```bash
   cd backend
   mvn clean install
   mvn spring-boot:run
   ```

### 2. 前端 (H5) 启动
1. 确保已安装 Node.js (建议 v18+)。
2. 安装依赖并启动：
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

### 3. 小程序启动
1. 下载并安装 [HBuilderX](https://www.dcloud.io/hbuilderx.html) 或使用 VS Code。
2. 安装依赖：
   ```bash
   cd miniapp
   npm install
   ```
3. 使用微信开发者工具导入 `miniapp/unpackage/dist/dev/mp-weixin` (编译后生成)。

## 📝 开源协议
本项目遵循 [MIT License](LICENSE) 开源协议。
