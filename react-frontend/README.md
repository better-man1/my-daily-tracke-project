# React 前端项目

这是 DailyTracker 项目的 React 版本实现，作为学习 React 技术的参考实现。

## 技术栈

- **React 18** - 使用 Hooks 实现组件逻辑
- **TypeScript** - 类型安全
- **Vite** - 构建工具
- **Ant Design** - UI 组件库
- **Zustand** - 状态管理
- **React Router v6** - 路由管理
- **Axios** - HTTP 客户端
- **dayjs** - 日期处理
- **echarts-for-react** - 图表库
- **@dnd-kit** - 拖拽库

## 快速开始

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

访问 http://localhost:5173

### 生产构建

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 主要功能

- 用户认证（登录/注册）
- 数据看板（统计图表）
- 每日计划（任务管理、拖拽排序）
- 每日记账（收支记录）
- 每日摘录（知识收集）
- 每日总结（心情追踪）
- 目标管理（OKR 风格）
- 个人中心（资料管理）

## React vs Vue 对比

详细的对比文档请查看 [COMPARISON.md](./COMPARISON.md)

### 主要差异

1. **组件定义**
   - Vue: 单文件组件，模板 + 脚本 + 样式
   - React: JSX，JavaScript 语法扩展

2. **状态管理**
   - Vue: Pinia，自动响应式
   - React: Zustand，需要手动更新

3. **路由**
   - Vue: Vue Router，配置式
   - React: React Router，组件式

## 项目结构

```
src/
├── api/                    # API接口模块
├── pages/                   # 页面组件
├── components/              # 公共组件
├── stores/                  # 状态管理
├── hooks/                   # 自定义Hooks
├── router/                  # 路由配置
├── utils/                   # 工具函数
├── types/                   # 类型定义
├── styles/                  # 样式文件
├── App.tsx                 # 根组件
└── main.tsx                # 入口文件
```

## 相关资源

- [React 官方文档](https://react.dev/)
- [Ant Design 文档](https://ant.design/)
- [Zustand 文档](https://zustand-demo.pmnd.rs/)
- [React Router 文档](https://reactrouter.com/)
