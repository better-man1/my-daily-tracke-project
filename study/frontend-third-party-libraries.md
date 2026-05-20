# 前端常用三方库与工具库知识点总结

## 1. 总览

前端三方库和工具库是现代前端工程的重要组成部分。它们可以帮助开发者更高效地完成请求、状态管理、表单校验、路由、日期处理、图表、动画、测试、构建、代码规范、监控等工作。

但三方库不是越多越好。高级前端工程师需要掌握的不只是“会用库”，还包括：

- 这个库解决什么问题。
- 它适合什么场景。
- 它和同类库有什么区别。
- 它会带来什么成本。
- 它是否影响包体积和性能。
- 它是否容易维护和替换。
- 它是否有安全风险。
- 它是否符合团队技术栈。

常见前端三方库可以分为：

- 工具函数库。
- 日期时间库。
- 请求库。
- 路由库。
- 状态管理库。
- 服务端状态管理库。
- 表单和校验库。
- UI 组件库。
- 样式和 CSS 工具。
- 动画库。
- 图表和可视化库。
- 富文本和编辑器库。
- 拖拽和交互库。
- 国际化库。
- 测试库。
- Mock 和接口调试工具。
- 构建和工程化工具。
- 代码质量工具。
- 前端监控和埋点工具。
- 安全和依赖治理工具。

## 2. 三方库选型原则

### 2.1 明确问题再引入库

引入库之前先回答：

- 当前问题是否真的需要三方库？
- 原生 API 是否已经足够？
- 自己实现的维护成本是否更高？
- 库的体积是否可接受？
- 库是否活跃维护？
- 团队是否熟悉？
- 是否有 TypeScript 类型支持？
- 是否容易测试？
- 是否会绑定项目架构？

例如：

- 简单防抖函数可以自己写。
- 复杂日期国际化更适合使用成熟库。
- 大型表单校验适合使用表单库和 Schema 校验库。
- 复杂图表不应该从零实现。

### 2.2 关注维护状态

选择库时要关注：

- 最近更新时间。
- GitHub issue 和 PR 活跃度。
- 文档质量。
- 社区使用量。
- TypeScript 支持。
- 兼容性。
- 安全漏洞记录。
- 主版本升级频率。

一个库如果长期无人维护，即使现在能用，也可能在未来带来安全、兼容和迁移成本。

### 2.3 关注包体积

前端库最终会影响浏览器加载和执行。

需要关注：

- gzip 后体积。
- 是否支持 ESM。
- 是否支持 Tree Shaking。
- 是否可以按需引入。
- 是否包含大量 polyfill。
- 是否依赖其他大型库。

常见工具：

- Bundle Analyzer。
- Bundlephobia。
- rollup-plugin-visualizer。
- webpack-bundle-analyzer。

### 2.4 关注替换成本

库一旦深入业务，替换成本会变高。

高替换成本库：

- UI 组件库。
- 状态管理库。
- 路由库。
- 表单库。
- 数据请求缓存库。
- 富文本编辑器。
- 图表库。

低替换成本库：

- 单个工具函数。
- 日期格式化函数。
- 小型辅助库。

建议：

- 对高替换成本库做充分评估。
- 对外封装适配层，避免业务代码直接依赖过深。
- 对核心库写清楚选型文档。

## 3. 工具函数库

### 3.1 Lodash

Lodash 是经典 JavaScript 工具函数库，提供数组、对象、字符串、函数、防抖、节流、深拷贝等工具。

常见能力：

- `debounce`：防抖。
- `throttle`：节流。
- `cloneDeep`：深拷贝。
- `merge`：深合并。
- `get`：安全读取对象路径。
- `isEqual`：深比较。
- `uniq`：数组去重。
- `groupBy`：分组。
- `sortBy`：排序。

示例：

```ts
import debounce from 'lodash-es/debounce'

const handleSearch = debounce((keyword: string) => {
  console.log(keyword)
}, 300)
```

使用建议：

- 优先使用 `lodash-es`，更利于 Tree Shaking。
- 不要直接引入整个 lodash。
- 简单工具函数可以用原生 API 替代。
- 深拷贝应谨慎使用，可能带来性能问题。

适合场景：

- 后台系统。
- 数据处理较多的业务。
- 需要稳定工具函数的团队项目。

### 3.2 Radash

Radash 是较新的 TypeScript 友好工具库，API 简洁，适合现代项目。

特点：

- TypeScript 支持较好。
- 函数命名现代。
- 提供常用数组、对象、异步工具。

适合：

- TypeScript 项目。
- 希望使用较轻量工具库的项目。

注意：

- 生态和历史沉淀不如 Lodash。
- 引入前应评估团队熟悉程度和维护活跃度。

### 3.3 clsx 和 classnames

`clsx` 和 `classnames` 用于根据条件拼接 className。

React 示例：

```tsx
import clsx from 'clsx'

function Button({ active }: { active: boolean }) {
  return (
    <button className={clsx('button', active && 'button-active')}>
      提交
    </button>
  )
}
```

适合：

- React 组件。
- 条件样式。
- CSS Modules。
- Tailwind CSS 组合。

选择建议：

- `clsx` 更小巧。
- `classnames` 使用历史更久。

### 3.4 nanoid

`nanoid` 用于生成短 ID。

```ts
import { nanoid } from 'nanoid'

const id = nanoid()
```

适合：

- 临时前端 ID。
- 表单动态项 key。
- 客户端生成唯一标识。

注意：

- 不应直接把前端生成 ID 当成关键业务主键。
- 核心业务 ID 应由后端统一生成。

## 4. 日期时间库

### 4.1 Day.js

Day.js 是轻量日期处理库，API 风格接近 Moment.js。

常见用法：

```ts
import dayjs from 'dayjs'

const text = dayjs().format('YYYY-MM-DD HH:mm:ss')
```

常见能力：

- 日期格式化。
- 日期计算。
- 日期比较。
- 插件扩展。
- 国际化。

优点：

- 体积较小。
- API 简洁。
- 迁移 Moment 成本较低。

注意：

- 一些能力需要插件。
- 复杂时区处理需要额外插件。

### 4.2 date-fns

date-fns 是函数式日期工具库。

示例：

```ts
import { format, addDays } from 'date-fns'

const next = addDays(new Date(), 7)
const text = format(next, 'yyyy-MM-dd')
```

特点：

- 函数式 API。
- 按函数引入。
- Tree Shaking 友好。

适合：

- 喜欢函数式风格的项目。
- 对包体积敏感的项目。

### 4.3 Luxon

Luxon 更关注国际化和时区处理。

适合：

- 多时区系统。
- 国际化较强的业务。
- 日历、会议、排期类系统。

选型建议：

- 普通业务日期格式化：Day.js 或 date-fns。
- 多时区复杂业务：Luxon 或 Temporal 相关方案。
- 简单格式化：优先考虑原生 `Intl.DateTimeFormat`。

## 5. 请求库

### 5.1 Fetch API

Fetch 是浏览器原生请求 API。

示例：

```ts
async function getUsers() {
  const response = await fetch('/api/users')

  if (!response.ok) {
    throw new Error('request failed')
  }

  return response.json()
}
```

优点：

- 原生支持。
- 不需要额外依赖。
- 支持 AbortController 取消请求。

不足：

- 默认不会因为 HTTP 4xx/5xx reject。
- 请求和响应拦截需要自己封装。
- 超时需要自己实现。

### 5.2 Axios

Axios 是常用 HTTP 请求库。

基础封装：

```ts
import axios from 'axios'

export const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

request.interceptors.response.use(
  response => response.data,
  error => Promise.reject(error)
)
```

常见能力：

- 请求拦截器。
- 响应拦截器。
- 超时配置。
- 请求取消。
- 自动 JSON 处理。
- 浏览器和 Node.js 通用。

适合：

- 中大型业务系统。
- 需要统一请求封装。
- 需要拦截器和错误处理。

注意：

- 不要在组件中到处创建 Axios 实例。
- 统一封装请求层。
- 统一处理 token、错误码、登录失效、超时。

### 5.3 ky

ky 是基于 Fetch 的轻量请求库。

特点：

- 小巧。
- Promise API 简洁。
- 支持 hooks。
- 支持重试。

适合：

- 偏轻量项目。
- 希望基于 Fetch 增强而不是使用 Axios 的项目。

## 6. 服务端状态管理库

### 6.1 TanStack Query

TanStack Query 用于管理服务端状态，也就是接口数据。

它解决的问题：

- loading 状态。
- error 状态。
- 缓存。
- 请求去重。
- 自动重新请求。
- 分页。
- 无限滚动。
- 乐观更新。
- 后台刷新。
- 数据过期。

React 示例：

```tsx
import { useQuery } from '@tanstack/react-query'

function UserProfile({ userId }: { userId: string }) {
  const { data, isLoading, error } = useQuery({
    queryKey: ['user', userId],
    queryFn: () => fetchUser(userId)
  })

  if (isLoading) return <div>加载中</div>
  if (error) return <div>加载失败</div>

  return <div>{data.name}</div>
}
```

适合：

- 接口数据较多的中大型项目。
- 需要缓存和自动刷新。
- 需要减少手写 loading/error/data 逻辑。

注意：

- Query Key 设计很重要。
- 本地 UI 状态不应放入 Query。
- 需要理解 staleTime、gcTime、invalidateQueries 等概念。

### 6.2 SWR

SWR 是 Vercel 推出的 React 数据请求缓存库。

核心理念：

```text
stale-while-revalidate
先返回缓存，再后台重新验证
```

示例：

```tsx
import useSWR from 'swr'

const fetcher = (url: string) => fetch(url).then(res => res.json())

function User() {
  const { data, error, isLoading } = useSWR('/api/user', fetcher)
}
```

适合：

- React 项目。
- 简洁数据请求缓存。
- Next.js 生态。

### 6.3 Vue Query

Vue Query 是 TanStack Query 的 Vue 适配。

适合：

- Vue3 项目。
- 服务端状态复杂。
- 希望拥有缓存、刷新、重试、分页等能力。

## 7. 状态管理库

### 7.1 Redux Toolkit

Redux 是经典状态管理库，Redux Toolkit 是官方推荐写法。

核心概念：

- Store。
- Slice。
- Action。
- Reducer。
- Dispatch。
- Selector。

示例：

```ts
import { createSlice } from '@reduxjs/toolkit'

const counterSlice = createSlice({
  name: 'counter',
  initialState: {
    value: 0
  },
  reducers: {
    increment(state) {
      state.value += 1
    }
  }
})

export const { increment } = counterSlice.actions
export default counterSlice.reducer
```

适合：

- 大型 React 应用。
- 状态变化复杂。
- 需要 DevTools。
- 团队需要统一严格数据流。

注意：

- 不建议把所有接口数据都放进 Redux。
- 服务端状态更适合 TanStack Query 或 RTK Query。

### 7.2 Zustand

Zustand 是轻量 React 状态管理库。

示例：

```ts
import { create } from 'zustand'

type CounterStore = {
  count: number
  increment: () => void
}

export const useCounterStore = create<CounterStore>(set => ({
  count: 0,
  increment: () => set(state => ({ count: state.count + 1 }))
}))
```

适合：

- 中小型 React 项目。
- 需要轻量全局状态。
- 不想引入 Redux 复杂度。

注意：

- Store 拆分要有边界。
- 不要把所有状态都堆到一个 Store。

### 7.3 Jotai

Jotai 是原子化状态管理库。

核心思想：

- 状态由一个个 atom 组成。
- 组件只订阅自己使用的 atom。

适合：

- 状态拆分细。
- 希望降低无关渲染。
- 喜欢原子状态模型的项目。

### 7.4 Pinia

Pinia 是 Vue 官方推荐状态管理库。

示例：

```ts
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    name: ''
  }),
  actions: {
    setToken(token: string) {
      this.token = token
    }
  }
})
```

适合：

- Vue3 项目。
- 后台管理系统。
- 需要全局用户、权限、配置状态。

注意：

- 局部状态不要全部放入 Pinia。
- 服务端接口数据可以考虑 Vue Query。

## 8. 路由库

### 8.1 React Router

React Router 是 React 常用路由库。

核心能力：

- Browser Router。
- Hash Router。
- 嵌套路由。
- 动态路由。
- 路由参数。
- 路由懒加载。
- loader/action。
- 路由守卫式逻辑。

示例：

```tsx
import { createBrowserRouter, RouterProvider } from 'react-router-dom'

const router = createBrowserRouter([
  {
    path: '/',
    element: <Home />
  },
  {
    path: '/users/:id',
    element: <UserDetail />
  }
])

export function App() {
  return <RouterProvider router={router} />
}
```

适合：

- React SPA。
- 后台管理系统。
- 多页面业务应用。

### 8.2 Vue Router

Vue Router 是 Vue 官方路由库。

示例：

```ts
import { createRouter, createWebHistory } from 'vue-router'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/pages/Home.vue')
    }
  ]
})
```

常见能力：

- 路由懒加载。
- 动态路由。
- 路由守卫。
- 路由 meta。
- 嵌套路由。

适合：

- Vue3 SPA。
- 后台管理系统。
- 权限菜单系统。

## 9. 表单库

### 9.1 React Hook Form

React Hook Form 是 React 常用表单库。

特点：

- 性能好。
- 非受控表单思路。
- API 简洁。
- 和 Zod/Yup 集成方便。

示例：

```tsx
import { useForm } from 'react-hook-form'

type FormValues = {
  username: string
}

function LoginForm() {
  const { register, handleSubmit } = useForm<FormValues>()

  return (
    <form onSubmit={handleSubmit(values => console.log(values))}>
      <input {...register('username')} />
      <button type="submit">提交</button>
    </form>
  )
}
```

适合：

- React 复杂表单。
- 高性能表单。
- 与 Schema 校验结合。

### 9.2 Formik

Formik 是较早流行的 React 表单库。

特点：

- 概念清晰。
- 生态成熟。
- 适合中等复杂表单。

注意：

- 在性能和现代 API 方面，很多新项目会优先考虑 React Hook Form。

### 9.3 Element Plus Form 和 Ant Design Form

对于 Vue 或 React 后台系统，UI 组件库通常内置表单能力。

适合：

- 后台管理系统。
- 表单样式与组件库深度绑定。
- 需要统一交互和校验提示。

注意：

- 深度绑定 UI 库会增加迁移成本。
- 复杂业务表单仍要清晰设计数据模型和校验规则。

## 10. 校验库

### 10.1 Zod

Zod 是 TypeScript 友好的运行时 Schema 校验库。

示例：

```ts
import { z } from 'zod'

const UserSchema = z.object({
  id: z.number(),
  name: z.string()
})

type User = z.infer<typeof UserSchema>

const user = UserSchema.parse(data)
```

适合：

- 表单校验。
- 接口响应校验。
- 环境变量校验。
- TypeScript 类型推导。

价值：

- 同时拥有运行时校验和静态类型。
- 减少类型和校验规则重复维护。

### 10.2 Yup

Yup 是经典 Schema 校验库。

常用于：

- Formik。
- 表单校验。
- 老项目维护。

注意：

- TypeScript 类型推导体验通常不如 Zod。

### 10.3 Valibot

Valibot 是较新的轻量 Schema 校验库。

特点：

- 更轻量。
- 模块化。
- 类型推导友好。

适合：

- 对体积敏感的项目。
- 希望使用现代 Schema 校验方案的项目。

## 11. UI 组件库

### 11.1 Ant Design

Ant Design 是 React 生态常用企业级 UI 组件库。

适合：

- 后台管理系统。
- 企业中台。
- 表格表单密集系统。

常见组件：

- Button。
- Form。
- Table。
- Modal。
- Drawer。
- Select。
- DatePicker。
- Upload。
- Layout。

优点：

- 组件完整。
- 设计规范成熟。
- 企业级场景覆盖广。

注意：

- 体积和样式定制需要关注。
- 深度使用后迁移成本高。

### 11.2 Element Plus

Element Plus 是 Vue3 常用组件库。

适合：

- Vue3 后台管理系统。
- 中后台业务。
- 快速搭建企业应用。

优点：

- 上手快。
- 文档清晰。
- 组件覆盖常见后台场景。

### 11.3 Naive UI

Naive UI 是 Vue3 组件库。

特点：

- TypeScript 支持好。
- 主题能力强。
- 风格现代。

适合：

- Vue3 项目。
- 需要较强主题定制的项目。

### 11.4 MUI

MUI 是 React Material Design 风格组件库。

适合：

- 国际化产品。
- 偏 Material Design 风格项目。
- 需要成熟 React UI 生态的项目。

### 11.5 Headless UI 和 Radix UI

Headless UI 和 Radix UI 提供无样式或低样式的可访问组件基础。

适合：

- 自定义设计系统。
- 需要高可访问性。
- 不希望被 UI 组件库视觉绑定。

价值：

- 提供交互逻辑和可访问性。
- 样式由团队自己控制。

## 12. 样式和 CSS 工具库

### 12.1 Tailwind CSS

Tailwind CSS 是原子化 CSS 框架。

示例：

```tsx
function Button() {
  return (
    <button className="rounded bg-blue-600 px-4 py-2 text-white">
      提交
    </button>
  )
}
```

优点：

- 开发效率高。
- 设计约束清晰。
- 不容易产生无边界 CSS。
- 构建后可清理未使用样式。

注意：

- className 较长。
- 团队需要统一规范。
- 复杂主题和组件封装需要设计。

### 12.2 Sass 和 Less

CSS 预处理器提供：

- 变量。
- 嵌套。
- mixin。
- 函数。
- 模块化。

示例：

```scss
$primary: #1677ff;

.button {
  background: $primary;

  &:hover {
    background: darken($primary, 8%);
  }
}
```

适合：

- 传统样式项目。
- 组件库样式。
- 需要变量和 mixin 的项目。

### 12.3 PostCSS

PostCSS 是 CSS 转换工具。

常见插件：

- autoprefixer。
- postcss-preset-env。
- cssnano。

用途：

- 自动添加浏览器前缀。
- 使用未来 CSS 语法。
- 压缩 CSS。

### 12.4 styled-components 和 emotion

CSS-in-JS 方案。

适合：

- React 组件样式。
- 动态样式。
- 主题系统。

注意：

- 运行时成本。
- SSR 配置。
- 团队风格统一。

## 13. 动画库

### 13.1 Framer Motion

Framer Motion 是 React 常用动画库。

适合：

- 页面过渡。
- 组件动效。
- 拖拽。
- 手势。
- 复杂交互动效。

示例：

```tsx
import { motion } from 'framer-motion'

function Card() {
  return (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
      内容
    </motion.div>
  )
}
```

### 13.2 GSAP

GSAP 是强大的动画库。

适合：

- 复杂时间线动画。
- 营销页。
- 互动页面。
- 可视化动效。

注意：

- 功能强大但需要控制复杂度。
- 不适合普通后台系统滥用。

### 13.3 Anime.js

Anime.js 是轻量动画库。

适合：

- 简单元素动画。
- SVG 动画。
- 小型交互效果。

## 14. 图表和可视化库

### 14.1 ECharts

ECharts 是常用图表库。

适合：

- 折线图。
- 柱状图。
- 饼图。
- 地图。
- 仪表盘。
- 大屏。

优点：

- 配置丰富。
- 中文生态好。
- 常见图表覆盖广。

注意：

- 图表实例要在组件卸载时销毁。
- 大数据量需要开启采样、渐进渲染或降采样。
- 不要频繁全量 setOption。

### 14.2 AntV

AntV 是数据可视化解决方案集合。

常见产品：

- G2。
- G6。
- X6。
- L7。

适合：

- 关系图。
- 流程图。
- 地理可视化。
- 高级数据可视化。

### 14.3 D3

D3 是底层可视化工具库。

特点：

- 灵活。
- 能力强。
- 学习成本高。

适合：

- 高度定制可视化。
- 数据驱动图形。
- 复杂交互图表。

### 14.4 Chart.js

Chart.js 简单易用。

适合：

- 简单图表。
- 小型项目。
- 快速展示数据。

## 15. 富文本和编辑器库

### 15.1 Quill

Quill 是经典富文本编辑器。

适合：

- 文章编辑。
- 评论编辑。
- 简单内容管理。

### 15.2 TinyMCE

TinyMCE 是成熟富文本编辑器。

适合：

- CMS。
- 企业内容系统。
- 需要完整编辑能力的项目。

### 15.3 TipTap

TipTap 基于 ProseMirror，适合现代可扩展编辑器。

特点：

- 扩展能力强。
- 支持结构化内容。
- 适合复杂编辑场景。

注意：

- 学习成本比简单编辑器更高。
- 复杂编辑器需要重点关注数据结构、粘贴处理、协作和安全过滤。

### 15.4 Monaco Editor

Monaco Editor 是 VS Code 使用的编辑器核心。

适合：

- 代码编辑器。
- 在线 IDE。
- SQL 编辑器。
- JSON 配置编辑器。

注意：

- 体积较大。
- 应动态加载。
- 需要配置 Worker。

## 16. 拖拽和交互库

### 16.1 dnd-kit

dnd-kit 是 React 拖拽库。

适合：

- 排序。
- 拖拽面板。
- 看板。
- 低代码编辑器。

特点：

- 现代。
- 可访问性较好。
- 扩展能力强。

### 16.2 SortableJS

SortableJS 是列表拖拽排序库。

适合：

- 简单列表排序。
- Vue/React 包装使用。

### 16.3 interact.js

interact.js 支持拖拽、缩放、手势等交互。

适合：

- 可视化编辑器。
- 画布编辑。
- 复杂交互面板。

## 17. 国际化库

### 17.1 i18next

i18next 是成熟国际化库，常用于 React 和通用 JavaScript 项目。

能力：

- 多语言。
- 插值。
- 复数。
- 命名空间。
- 懒加载语言包。

适合：

- React 应用。
- 多语言业务系统。
- 需要国际化扩展能力的项目。

### 17.2 vue-i18n

vue-i18n 是 Vue 生态常用国际化库。

适合：

- Vue3 多语言项目。
- 后台系统国际化。
- Nuxt 项目。

### 17.3 Intl API

浏览器原生 Intl API 可以处理：

- 日期格式。
- 数字格式。
- 货币格式。
- 相对时间。

示例：

```ts
const formatter = new Intl.NumberFormat('zh-CN', {
  style: 'currency',
  currency: 'CNY'
})

formatter.format(99.9)
```

建议：

- 文案翻译用 i18n 库。
- 日期、数字、货币优先考虑 Intl API。

## 18. 测试库

### 18.1 Vitest

Vitest 是现代测试框架，和 Vite 生态结合紧密。

适合：

- Vite 项目。
- 单元测试。
- 组件测试。
- TypeScript 项目。

示例：

```ts
import { expect, test } from 'vitest'

test('add', () => {
  expect(1 + 1).toBe(2)
})
```

### 18.2 Jest

Jest 是经典 JavaScript 测试框架。

适合：

- 老项目。
- React 项目。
- Node.js 工具库。

注意：

- 在 Vite 新项目中，Vitest 通常体验更好。

### 18.3 Testing Library

Testing Library 强调从用户视角测试。

核心理念：

- 测试用户能看到什么。
- 测试用户能如何操作。
- 少测试实现细节。

React 示例：

```tsx
import { render, screen } from '@testing-library/react'

test('render button', () => {
  render(<button>提交</button>)
  expect(screen.getByRole('button', { name: '提交' })).toBeInTheDocument()
})
```

### 18.4 Playwright

Playwright 是端到端测试工具。

适合：

- 登录流程。
- 下单流程。
- 权限流程。
- 多浏览器测试。
- 自动化回归。

示例：

```ts
import { expect, test } from '@playwright/test'

test('home page', async ({ page }) => {
  await page.goto('http://localhost:3000')
  await expect(page).toHaveTitle(/Home/)
})
```

### 18.5 Cypress

Cypress 是流行 E2E 测试工具。

特点：

- 调试体验好。
- 生态成熟。
- 适合前端团队。

Playwright 与 Cypress 都可用，选型看团队习惯、浏览器覆盖、CI 需求和调试体验。

## 19. Mock 和接口调试

### 19.1 MSW

MSW 是 Mock Service Worker，通过 Service Worker 拦截请求。

适合：

- 前端本地开发 Mock。
- 组件测试 Mock。
- E2E 测试 Mock。

价值：

- 更接近真实网络请求。
- 不需要改业务请求代码。
- 可以复用 Mock 规则。

### 19.2 Mock.js

Mock.js 常用于生成随机数据。

适合：

- 快速生成假数据。
- 原型开发。
- 老项目 Mock。

注意：

- 不建议长期让 Mock 数据结构和真实接口脱节。
- 最好基于接口契约生成 Mock。

### 19.3 OpenAPI 相关工具

OpenAPI 可以描述接口契约。

常见用途：

- 生成 TypeScript 类型。
- 生成接口请求代码。
- 生成 Mock 服务。
- 生成接口文档。

适合：

- 前后端协作规范化。
- 大型团队。
- 接口多的业务系统。

## 20. 构建和工程化工具

### 20.1 Vite

Vite 是现代前端构建工具。

核心能力：

- 开发服务器。
- HMR。
- 依赖预构建。
- TypeScript 支持。
- CSS 处理。
- 静态资源处理。
- Rollup 生产构建。
- 插件系统。

适合：

- Vue3。
- React。
- Svelte。
- 现代前端项目。
- 组件库。

### 20.2 Webpack

Webpack 是成熟模块打包工具。

核心概念：

- Entry。
- Output。
- Loader。
- Plugin。
- DevServer。
- SplitChunks。
- Tree Shaking。

适合：

- 历史项目。
- 构建深度定制。
- 复杂企业工程。
- Module Federation 微前端。

### 20.3 Rollup

Rollup 更适合库构建。

适合：

- 工具库。
- 组件库。
- SDK。
- ESM/CJS 多格式产物。

### 20.4 esbuild 和 SWC

esbuild 使用 Go 编写，SWC 使用 Rust 编写，都追求高性能编译。

常见用途：

- TypeScript 转译。
- 代码压缩。
- 构建加速。
- Vite 依赖预构建。
- Next.js 编译。

注意：

- 它们主要负责快速转译，不完全等价于 TypeScript 类型检查。

### 20.5 Babel

Babel 用于 JavaScript 语法转换。

常见用途：

- 转换新版语法。
- JSX 转换。
- 插件扩展。
- polyfill 配合。

现代项目中，Babel 仍常用于需要插件生态和语法转换的场景。

## 21. 代码质量工具

### 21.1 ESLint

ESLint 用于检查 JavaScript/TypeScript 代码质量。

可以检查：

- 未使用变量。
- 潜在错误。
- 不规范写法。
- React Hooks 规则。
- Vue 组件规则。
- TypeScript 规则。

建议：

- CI 中执行 lint。
- pre-commit 中检查改动文件。
- 规则不要过度复杂。

### 21.2 Prettier

Prettier 用于统一代码格式。

关注：

- 缩进。
- 换行。
- 分号。
- 引号。
- 尾逗号。

价值：

- 避免格式争论。
- 降低 Code Review 噪音。
- 统一团队代码风格。

### 21.3 Stylelint

Stylelint 用于检查 CSS、Sass、Less 等样式代码。

适合：

- 样式规范要求高的项目。
- 组件库。
- 设计系统。

### 21.4 Husky 和 lint-staged

Husky 管理 Git Hooks。

lint-staged 对暂存文件执行检查。

常见流程：

```text
git commit
-> pre-commit
-> lint-staged
-> ESLint/Prettier
-> commit
```

价值：

- 在提交前发现问题。
- 避免把格式和低级错误带入仓库。

### 21.5 Commitlint

Commitlint 用于规范提交信息。

常见格式：

```text
feat: add user profile
fix: resolve login error
docs: update README
```

价值：

- 提交历史清晰。
- 方便生成 changelog。
- 方便团队协作。

## 22. 文档和组件开发工具

### 22.1 Storybook

Storybook 用于独立开发、展示和测试组件。

适合：

- 组件库。
- 设计系统。
- 复杂业务组件。

能力：

- 组件示例。
- 交互文档。
- 视觉回归。
- Props 控制。
- 设计协作。

### 22.2 VitePress

VitePress 是基于 Vite 的静态文档站工具。

适合：

- 技术文档。
- 组件库文档。
- 项目规范文档。
- 博客。

### 22.3 Docusaurus

Docusaurus 是成熟文档站框架。

适合：

- 大型文档站。
- 开源项目文档。
- 多版本文档。
- 国际化文档。

## 23. 前端监控和埋点工具

### 23.1 Sentry

Sentry 是常见错误监控平台。

可监控：

- JavaScript 错误。
- Promise 未捕获错误。
- Source Map。
- 用户行为面包屑。
- 性能追踪。

适合：

- 线上错误追踪。
- 快速定位版本问题。
- 多端错误监控。

### 23.2 OpenTelemetry

OpenTelemetry 是可观测性标准。

适合：

- 前后端链路追踪。
- 指标、日志、Trace 统一。
- 企业级可观测体系。

### 23.3 Web Vitals

Web Vitals 用于采集页面性能指标。

关键指标：

- LCP。
- CLS。
- INP。
- FCP。
- TTFB。

适合：

- 真实用户性能监控。
- 性能预算。
- 页面体验治理。

### 23.4 埋点 SDK

埋点用于采集用户行为和业务指标。

常见采集：

- 页面访问。
- 按钮点击。
- 表单提交。
- 转化漏斗。
- 停留时间。
- 错误事件。

注意：

- 埋点要有规范。
- 避免重复上报。
- 注意隐私合规。
- 不采集敏感信息。

## 24. 安全和依赖治理工具

### 24.1 npm audit

npm audit 用于检查依赖漏洞。

```bash
npm audit
```

注意：

- 不是所有漏洞都直接影响项目。
- 需要结合运行环境和依赖使用方式判断风险。

### 24.2 pnpm audit

pnpm 也提供依赖审计。

```bash
pnpm audit
```

适合 pnpm 项目。

### 24.3 Dependabot 和 Renovate

用于自动检测和提交依赖升级 PR。

价值：

- 及时升级安全补丁。
- 减少依赖长期落后。
- 自动化维护依赖。

注意：

- 依赖升级需要测试保障。
- 主版本升级要人工评估破坏性变更。

### 24.4 license 检查

企业项目需要关注开源协议。

常见协议：

- MIT。
- Apache-2.0。
- BSD。
- GPL。
- LGPL。

注意：

- GPL 类协议可能对商业项目有影响。
- 企业项目应建立依赖许可证扫描流程。

## 25. 常见选型场景建议

### 25.1 React 中后台项目

常见组合：

- React。
- TypeScript。
- Vite。
- React Router。
- Ant Design。
- Zustand 或 Redux Toolkit。
- TanStack Query。
- Axios。
- React Hook Form。
- Zod。
- Vitest。
- Playwright。

适合：

- 企业后台。
- 中台系统。
- 管理系统。

### 25.2 Vue3 中后台项目

常见组合：

- Vue3。
- TypeScript。
- Vite。
- Vue Router。
- Pinia。
- Element Plus 或 Naive UI。
- Axios。
- Zod 或表单内置校验。
- Vitest。
- Playwright。

适合：

- 国内企业后台。
- 管理平台。
- 数据运营平台。

### 25.3 组件库项目

常见组合：

- TypeScript。
- Vite 或 Rollup。
- Storybook 或 VitePress。
- Vitest。
- Playwright。
- ESLint。
- Prettier。
- Changesets。

重点：

- API 设计。
- 类型声明。
- 按需引入。
- 样式隔离。
- 文档。
- 版本管理。

### 25.4 内容站和官网

常见组合：

- Next.js 或 Nuxt。
- MDX 或 Markdown。
- 图片优化。
- SEO 工具。
- Web Vitals。
- CDN。

重点：

- SEO。
- 首屏性能。
- 内容构建。
- 静态生成。
- 缓存。

## 26. 三方库使用最佳实践

### 26.1 建立依赖准入机制

团队应明确：

- 哪些库可以直接使用。
- 哪些库需要评审。
- 引入大型库需要说明理由。
- 高风险库需要安全检查。
- 低维护库需要替代方案。

### 26.2 做适度封装

需要封装的库：

- 请求库。
- 监控 SDK。
- 埋点 SDK。
- 组件库主题。
- 权限工具。
- 存储工具。

不建议过度封装：

- 简单工具函数。
- 标准 API。
- 很稳定且直接的库。

封装目标：

- 统一使用方式。
- 降低替换成本。
- 隐藏项目通用逻辑。
- 避免业务代码重复。

### 26.3 控制依赖数量

依赖越多，风险越多：

- 包体积增加。
- 安全风险增加。
- 升级成本增加。
- 构建时间增加。
- 兼容性风险增加。

建议定期清理：

- 未使用依赖。
- 重复功能依赖。
- 长期无人维护依赖。
- 被原生 API 替代的依赖。

### 26.4 锁定依赖版本

使用 lockfile 保证安装一致：

- npm：`package-lock.json`。
- pnpm：`pnpm-lock.yaml`。
- yarn：`yarn.lock`。

团队应统一包管理器，不要混用多个 lockfile。

### 26.5 关注安全和供应链风险

建议：

- 使用 lockfile。
- 定期 audit。
- 不随意安装陌生包。
- 检查包名是否拼写正确。
- 谨慎执行安装脚本。
- CI 中增加依赖扫描。
- 对关键依赖保持关注。

## 27. 学习路线建议

推荐学习顺序：

1. 先掌握原生 JavaScript、DOM、Fetch、Intl、Web API。
2. 学习工具函数库，如 Lodash、clsx。
3. 学习请求库和请求封装，如 Axios、ky。
4. 学习状态管理，如 Redux Toolkit、Zustand、Pinia。
5. 学习服务端状态管理，如 TanStack Query、SWR。
6. 学习表单和校验，如 React Hook Form、Zod。
7. 学习 UI 组件库，如 Ant Design、Element Plus。
8. 学习样式工具，如 Tailwind、Sass、PostCSS。
9. 学习测试工具，如 Vitest、Testing Library、Playwright。
10. 学习构建工具，如 Vite、Webpack、Rollup。
11. 学习监控、埋点和性能指标采集。
12. 学习依赖治理、安全扫描和选型文档写作。

## 28. 能力自检清单

你应该能够回答：

- 什么情况下应该引入三方库？
- 如何评估一个库是否适合项目？
- Lodash 和原生 API 如何取舍？
- Axios 和 Fetch 有什么区别？
- TanStack Query 解决了什么问题？
- Redux Toolkit 和 Zustand 如何选择？
- Pinia 应该如何拆分 Store？
- 表单库和校验库分别解决什么问题？
- Zod 的价值是什么？
- UI 组件库为什么迁移成本高？
- Tailwind CSS 的优缺点是什么？
- ECharts 大数据量图表如何优化？
- Vitest、Jest、Playwright 分别适合什么？
- 如何分析三方库对包体积的影响？
- 如何治理依赖安全风险？

## 29. 官方资料入口

- Lodash：https://lodash.com/docs/
- Axios：https://axios-http.com/docs/intro
- TanStack Query：https://tanstack.com/query/latest
- Redux Toolkit：https://redux-toolkit.js.org/
- Zustand：https://zustand.docs.pmnd.rs/
- Pinia：https://pinia.vuejs.org/
- React Router：https://reactrouter.com/
- Vue Router：https://router.vuejs.org/
- React Hook Form：https://react-hook-form.com/
- Zod：https://zod.dev/
- Ant Design：https://ant.design/
- Element Plus：https://element-plus.org/
- Tailwind CSS：https://tailwindcss.com/
- ECharts：https://echarts.apache.org/
- Vitest：https://vitest.dev/
- Playwright：https://playwright.dev/
- ESLint：https://eslint.org/
- Prettier：https://prettier.io/
- Vite：https://vite.dev/
- Webpack：https://webpack.js.org/

## 30. 总结

前端三方库和工具库的价值在于提升开发效率、降低重复劳动、复用成熟方案。但真正高级的能力不是记住库名，而是知道每个库解决什么问题、适合什么场景、有什么代价、如何封装、如何治理以及如何替换。

在真实项目中，三方库选型应该服务业务和团队，而不是追逐热度。优秀的前端工程体系，既能充分利用生态，又能控制依赖风险、包体积、安全问题和长期维护成本。
