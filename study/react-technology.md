# React 技术知识点总结

## 1. React 是什么

React 是一个用于构建用户界面的 JavaScript 库，由 Meta 开源维护。它主要用于构建组件化、声明式、可复用的前端界面。

React 的核心思想：

- 声明式 UI：开发者描述“界面应该是什么样”，React 负责把状态变化同步到页面。
- 组件化：把页面拆成独立、可复用、可组合的组件。
- 单向数据流：数据从父组件流向子组件，状态变化驱动界面更新。
- 虚拟 DOM：通过内存中的 UI 描述对象计算最小更新。
- Hooks：在函数组件中使用状态、副作用、上下文、引用等能力。

React 适合的场景：

- 中大型 Web 应用。
- 后台管理系统。
- H5 活动页。
- 跨端应用，如 React Native。
- 组件库和设计系统。
- 服务端渲染应用，如 Next.js。

## 2. React 项目基础结构

现代 React 项目通常使用 Vite、Next.js、Create React App 或其他脚手架创建。现在新项目更推荐使用 Vite 或 Next.js。

典型 Vite + React 项目结构：

```text
src
├─ assets
├─ components
├─ hooks
├─ layouts
├─ pages
├─ router
├─ services
├─ stores
├─ styles
├─ types
├─ utils
├─ App.tsx
└─ main.tsx
```

常见文件职责：

- `main.tsx`：应用入口，负责挂载 React 应用。
- `App.tsx`：根组件，通常包含路由、布局或全局 Provider。
- `components`：通用组件。
- `pages`：页面组件。
- `hooks`：自定义 Hooks。
- `services`：接口请求。
- `stores`：状态管理。
- `styles`：全局样式和主题变量。
- `types`：TypeScript 类型定义。
- `utils`：工具函数。

React 应用入口示例：

```tsx
import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
)
```

## 3. JSX

### 3.1 JSX 是什么

JSX 是 JavaScript 的语法扩展，用于在 JavaScript 中描述 UI 结构。

示例：

```tsx
function App() {
  const title = 'React'

  return <h1>Hello {title}</h1>
}
```

JSX 看起来像 HTML，但本质上会被编译成 JavaScript 函数调用。它允许在标签中使用 JavaScript 表达式。

### 3.2 JSX 基本规则

JSX 常见规则：

- 组件必须返回一个根节点。
- 标签必须闭合。
- JavaScript 表达式写在 `{}` 中。
- `class` 要写成 `className`。
- `for` 要写成 `htmlFor`。
- 样式对象使用驼峰命名。

示例：

```tsx
function UserCard() {
  const user = {
    name: 'Alice',
    active: true
  }

  return (
    <section className="user-card">
      <h2>{user.name}</h2>
      <p>{user.active ? '在线' : '离线'}</p>
    </section>
  )
}
```

### 3.3 条件渲染

常见写法：

```tsx
function Status({ isLogin }: { isLogin: boolean }) {
  return <div>{isLogin ? '已登录' : '未登录'}</div>
}
```

使用 `&&`：

```tsx
function Notice({ count }: { count: number }) {
  return <div>{count > 0 && <span>你有 {count} 条消息</span>}</div>
}
```

注意：如果左侧可能是 `0`，页面可能会渲染出 `0`。更稳妥的写法是使用明确布尔表达式：

```tsx
{count > 0 && <span>你有 {count} 条消息</span>}
```

### 3.4 列表渲染

列表渲染通常使用 `map`。

```tsx
function UserList({ users }: { users: Array<{ id: number; name: string }> }) {
  return (
    <ul>
      {users.map(user => (
        <li key={user.id}>{user.name}</li>
      ))}
    </ul>
  )
}
```

`key` 的作用是帮助 React 识别列表项身份，提高更新效率并避免状态错乱。

不建议使用数组下标作为 `key`，尤其是在列表会新增、删除、排序时。更推荐使用稳定唯一 ID。

## 4. 组件

### 4.1 函数组件

现代 React 主要使用函数组件。

```tsx
type ButtonProps = {
  text: string
  onClick: () => void
}

function Button({ text, onClick }: ButtonProps) {
  return <button onClick={onClick}>{text}</button>
}
```

函数组件的特点：

- 写法简洁。
- 配合 Hooks 使用能力完整。
- 更容易组合和复用逻辑。
- 是现代 React 推荐方式。

### 4.2 类组件

类组件是 React 早期主要写法，现在新项目中较少使用，但维护历史项目仍需要理解。

```tsx
import React from 'react'

class Counter extends React.Component<object, { count: number }> {
  state = {
    count: 0
  }

  render() {
    return (
      <button onClick={() => this.setState({ count: this.state.count + 1 })}>
        {this.state.count}
      </button>
    )
  }
}
```

类组件依赖生命周期方法，如 `componentDidMount`、`componentDidUpdate`、`componentWillUnmount`。

### 4.3 Props

Props 是父组件传递给子组件的数据。

```tsx
function Welcome({ name }: { name: string }) {
  return <h1>Hello {name}</h1>
}

function App() {
  return <Welcome name="React" />
}
```

Props 特点：

- 只读，不应在子组件中修改。
- 用于父子组件通信。
- 可以传递字符串、数字、对象、数组、函数、React 节点等。

### 4.4 State

State 是组件内部状态。状态变化会触发组件重新渲染。

```tsx
import { useState } from 'react'

function Counter() {
  const [count, setCount] = useState(0)

  return (
    <button onClick={() => setCount(count + 1)}>
      {count}
    </button>
  )
}
```

State 使用原则：

- 不要直接修改状态。
- 状态更新可能是异步批处理的。
- 新状态依赖旧状态时，使用函数式更新。

```tsx
setCount(prev => prev + 1)
```

### 4.5 组件组合

React 推荐通过组合而不是继承来复用 UI。

```tsx
type CardProps = {
  title: string
  children: React.ReactNode
}

function Card({ title, children }: CardProps) {
  return (
    <section>
      <h2>{title}</h2>
      <div>{children}</div>
    </section>
  )
}
```

使用：

```tsx
<Card title="用户信息">
  <p>Alice</p>
</Card>
```

`children` 是 React 组件组合中非常重要的能力。

## 5. Hooks

Hooks 是 React 16.8 引入的能力，使函数组件可以使用状态、副作用、引用、上下文等特性。

Hooks 使用规则：

- 只能在 React 函数组件或自定义 Hook 中调用。
- 只能在顶层调用，不能放在条件、循环、嵌套函数中。
- Hook 名称通常以 `use` 开头。

### 5.1 useState

`useState` 用于声明组件状态。

```tsx
const [value, setValue] = useState('')
```

示例：

```tsx
function SearchBox() {
  const [keyword, setKeyword] = useState('')

  return (
    <input
      value={keyword}
      onChange={event => setKeyword(event.target.value)}
    />
  )
}
```

对象状态更新时要保留原有字段：

```tsx
setUser(prev => ({
  ...prev,
  name: 'Bob'
}))
```

### 5.2 useEffect

`useEffect` 用于处理副作用。

常见副作用：

- 请求接口。
- 订阅事件。
- 操作 DOM。
- 设置定时器。
- 同步外部状态。

基本示例：

```tsx
import { useEffect, useState } from 'react'

function UserProfile({ userId }: { userId: string }) {
  const [user, setUser] = useState(null)

  useEffect(() => {
    fetch(`/api/users/${userId}`)
      .then(res => res.json())
      .then(setUser)
  }, [userId])

  return <pre>{JSON.stringify(user, null, 2)}</pre>
}
```

依赖数组含义：

- 不传依赖数组：每次渲染后都执行。
- 空数组 `[]`：组件挂载后执行一次。
- `[userId]`：依赖变化后执行。

清理副作用：

```tsx
useEffect(() => {
  const timer = window.setInterval(() => {
    console.log('tick')
  }, 1000)

  return () => {
    window.clearInterval(timer)
  }
}, [])
```

### 5.3 useRef

`useRef` 用于保存可变引用，修改它不会触发重新渲染。

常见用途：

- 获取 DOM 节点。
- 保存定时器 ID。
- 保存上一次值。
- 保存不需要触发渲染的变量。

```tsx
import { useRef } from 'react'

function InputFocus() {
  const inputRef = useRef<HTMLInputElement>(null)

  return (
    <>
      <input ref={inputRef} />
      <button onClick={() => inputRef.current?.focus()}>聚焦</button>
    </>
  )
}
```

### 5.4 useMemo

`useMemo` 用于缓存计算结果，避免每次渲染都重复执行昂贵计算。

```tsx
const total = useMemo(() => {
  return list.reduce((sum, item) => sum + item.price, 0)
}, [list])
```

使用建议：

- 只在计算确实昂贵或引用稳定性重要时使用。
- 不要为了“看起来优化”到处使用。
- 过度使用会增加代码复杂度。

### 5.5 useCallback

`useCallback` 用于缓存函数引用。

```tsx
const handleSubmit = useCallback(() => {
  submit(form)
}, [form])
```

常见用途：

- 传递给经过 `React.memo` 优化的子组件。
- 作为其他 Hook 的依赖。
- 避免事件订阅重复绑定。

### 5.6 useContext

`useContext` 用于跨层级共享数据，避免层层传递 Props。

```tsx
import { createContext, useContext } from 'react'

const ThemeContext = createContext('light')

function Toolbar() {
  const theme = useContext(ThemeContext)
  return <div>当前主题：{theme}</div>
}

function App() {
  return (
    <ThemeContext.Provider value="dark">
      <Toolbar />
    </ThemeContext.Provider>
  )
}
```

适合放入 Context 的数据：

- 主题。
- 当前用户。
- 国际化语言。
- 权限信息。
- 全局配置。

不建议把频繁变化的大型状态全部放进单个 Context，否则可能导致较多组件重新渲染。

### 5.7 useReducer

`useReducer` 适合管理复杂状态逻辑。

```tsx
type State = {
  count: number
}

type Action =
  | { type: 'increment' }
  | { type: 'decrement' }

function reducer(state: State, action: Action): State {
  switch (action.type) {
    case 'increment':
      return { count: state.count + 1 }
    case 'decrement':
      return { count: state.count - 1 }
    default:
      return state
  }
}

function Counter() {
  const [state, dispatch] = useReducer(reducer, { count: 0 })

  return (
    <>
      <button onClick={() => dispatch({ type: 'decrement' })}>-</button>
      <span>{state.count}</span>
      <button onClick={() => dispatch({ type: 'increment' })}>+</button>
    </>
  )
}
```

适合使用 `useReducer` 的场景：

- 状态字段较多。
- 状态更新逻辑复杂。
- 多个操作会影响同一份状态。
- 希望把状态更新逻辑集中管理。

### 5.8 自定义 Hook

自定义 Hook 用于复用状态逻辑。

```tsx
import { useEffect, useState } from 'react'

function useWindowSize() {
  const [size, setSize] = useState({
    width: window.innerWidth,
    height: window.innerHeight
  })

  useEffect(() => {
    const handleResize = () => {
      setSize({
        width: window.innerWidth,
        height: window.innerHeight
      })
    }

    window.addEventListener('resize', handleResize)

    return () => {
      window.removeEventListener('resize', handleResize)
    }
  }, [])

  return size
}
```

自定义 Hook 的价值：

- 抽离可复用逻辑。
- 保持组件简洁。
- 统一处理副作用。
- 方便测试。

## 6. 事件处理

React 使用合成事件系统，屏蔽不同浏览器事件差异。

基本示例：

```tsx
function Button() {
  const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    console.log(event.currentTarget)
  }

  return <button onClick={handleClick}>点击</button>
}
```

传递参数：

```tsx
function UserItem({ id }: { id: number }) {
  return <button onClick={() => console.log(id)}>查看</button>
}
```

阻止默认行为：

```tsx
function Form() {
  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault()
  }

  return <form onSubmit={handleSubmit}>...</form>
}
```

注意：事件处理函数不要在渲染过程中直接执行。

错误写法：

```tsx
<button onClick={submit()}>提交</button>
```

正确写法：

```tsx
<button onClick={submit}>提交</button>
<button onClick={() => submit(id)}>提交</button>
```

## 7. 表单处理

### 7.1 受控组件

受控组件是指表单值由 React state 控制。

```tsx
function LoginForm() {
  const [username, setUsername] = useState('')

  return (
    <input
      value={username}
      onChange={event => setUsername(event.target.value)}
    />
  )
}
```

优点：

- 状态可控。
- 方便校验。
- 方便联动。
- 方便提交前统一处理。

### 7.2 非受控组件

非受控组件通过 DOM 自身保存值，React 通过 ref 获取。

```tsx
function UploadForm() {
  const inputRef = useRef<HTMLInputElement>(null)

  const handleSubmit = () => {
    const file = inputRef.current?.files?.[0]
    console.log(file)
  }

  return (
    <>
      <input ref={inputRef} type="file" />
      <button onClick={handleSubmit}>提交</button>
    </>
  )
}
```

文件上传场景通常使用非受控方式更自然。

### 7.3 表单库

复杂表单建议使用成熟表单库。

常见表单库：

- React Hook Form。
- Formik。
- Final Form。
- Ant Design Form。

常见校验库：

- Zod。
- Yup。
- Valibot。

表单库解决的问题：

- 字段注册。
- 校验规则。
- 错误提示。
- 表单状态。
- 动态字段。
- 性能优化。

## 8. 组件通信

### 8.1 父传子

通过 Props 传递。

```tsx
function Parent() {
  return <Child name="Alice" />
}
```

### 8.2 子传父

父组件传递回调函数给子组件。

```tsx
function Parent() {
  const handleChange = (value: string) => {
    console.log(value)
  }

  return <Child onChange={handleChange} />
}

function Child({ onChange }: { onChange: (value: string) => void }) {
  return <button onClick={() => onChange('hello')}>发送</button>
}
```

### 8.3 兄弟组件通信

常见方式：

- 状态提升到共同父组件。
- 使用 Context。
- 使用状态管理库。
- 使用事件总线，但 React 项目中通常不优先推荐。

### 8.4 跨层级通信

跨层级通信可以使用 Context。

适合共享较稳定的全局信息，不适合所有业务状态都无脑放入 Context。

## 9. 状态管理

### 9.1 本地状态

组件内部状态使用 `useState` 或 `useReducer`。

适合：

- 弹窗开关。
- 输入框内容。
- 当前选中项。
- 局部 UI 状态。

### 9.2 状态提升

当多个组件需要共享同一份状态时，可以把状态提升到它们最近的共同父组件。

```tsx
function Parent() {
  const [selectedId, setSelectedId] = useState<number | null>(null)

  return (
    <>
      <List selectedId={selectedId} onSelect={setSelectedId} />
      <Detail selectedId={selectedId} />
    </>
  )
}
```

状态提升是 React 中非常基础也非常重要的状态管理方式。

### 9.3 Context 状态

Context 适合中小规模的跨层级状态共享。

可以结合 `useReducer` 使用：

```tsx
const AuthContext = createContext<AuthState | null>(null)
const AuthDispatchContext = createContext<React.Dispatch<AuthAction> | null>(null)
```

为了减少无关组件重渲染，可以把状态和 dispatch 拆成两个 Context。

### 9.4 Redux

Redux 是经典全局状态管理方案，适合复杂应用。

核心概念：

- Store：全局状态容器。
- Action：描述发生了什么。
- Reducer：根据 Action 计算新状态。
- Dispatch：触发状态更新。
- Selector：从状态中读取数据。

现代 Redux 推荐使用 Redux Toolkit。

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

Redux 适合：

- 多页面共享复杂状态。
- 状态变化需要可追踪。
- 大型团队协作。
- 有复杂异步流程。
- 需要时间旅行调试或 DevTools。

### 9.5 Zustand

Zustand 是轻量状态管理库，API 简洁。

```ts
import { create } from 'zustand'

type CounterStore = {
  count: number
  increment: () => void
}

const useCounterStore = create<CounterStore>(set => ({
  count: 0,
  increment: () => set(state => ({ count: state.count + 1 }))
}))
```

组件中使用：

```tsx
const count = useCounterStore(state => state.count)
const increment = useCounterStore(state => state.increment)
```

Zustand 适合：

- 中小型项目。
- 需要轻量全局状态。
- 不想引入 Redux 复杂度。

### 9.6 服务端状态

服务端状态指来自后端接口的数据，例如列表、详情、分页、搜索结果。

它与本地 UI 状态不同，通常需要处理：

- 请求中。
- 请求成功。
- 请求失败。
- 缓存。
- 重新请求。
- 分页。
- 乐观更新。
- 后台刷新。

常见库：

- TanStack Query。
- SWR。
- RTK Query。

示例：

```tsx
const { data, isLoading, error } = useQuery({
  queryKey: ['user', userId],
  queryFn: () => fetchUser(userId)
})
```

实际项目中，不建议把所有接口数据都塞进 Redux。服务端状态更适合交给 TanStack Query 这类专门工具管理。

## 10. 路由

React 本身不包含路由功能，常用 React Router。

### 10.1 基础路由

```tsx
import { createBrowserRouter, RouterProvider } from 'react-router-dom'

const router = createBrowserRouter([
  {
    path: '/',
    element: <Home />
  },
  {
    path: '/users',
    element: <UserList />
  }
])

function App() {
  return <RouterProvider router={router} />
}
```

### 10.2 嵌套路由

```tsx
const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      {
        index: true,
        element: <Home />
      },
      {
        path: 'users',
        element: <UserList />
      }
    ]
  }
])
```

布局组件中使用 `Outlet` 渲染子路由。

```tsx
import { Outlet } from 'react-router-dom'

function Layout() {
  return (
    <>
      <Header />
      <Outlet />
    </>
  )
}
```

### 10.3 路由参数

```tsx
import { useParams } from 'react-router-dom'

function UserDetail() {
  const { id } = useParams()

  return <div>用户 ID：{id}</div>
}
```

### 10.4 编程式导航

```tsx
import { useNavigate } from 'react-router-dom'

function LoginButton() {
  const navigate = useNavigate()

  return <button onClick={() => navigate('/dashboard')}>进入后台</button>
}
```

### 10.5 路由懒加载

```tsx
import { lazy, Suspense } from 'react'

const UserList = lazy(() => import('./pages/UserList'))

function App() {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <UserList />
    </Suspense>
  )
}
```

路由懒加载可以减少首屏包体积，提高初始加载速度。

## 11. 生命周期

函数组件中生命周期通常通过 `useEffect` 表达。

类组件生命周期和 Hook 对应关系：

| 类组件生命周期 | 函数组件写法 |
| --- | --- |
| `componentDidMount` | `useEffect(() => {}, [])` |
| `componentDidUpdate` | `useEffect(() => {}, [deps])` |
| `componentWillUnmount` | `useEffect` 返回清理函数 |

示例：

```tsx
useEffect(() => {
  console.log('mounted')

  return () => {
    console.log('unmounted')
  }
}, [])
```

需要注意：`useEffect` 不是生命周期的简单替代品，它表达的是“渲染后同步副作用”。理解依赖数组比机械对应生命周期更重要。

## 12. 虚拟 DOM 与 Diff

### 12.1 虚拟 DOM

虚拟 DOM 是用 JavaScript 对象描述 UI 结构。

示例：

```tsx
const element = <h1 className="title">Hello</h1>
```

它会被转换成类似对象结构：

```js
{
  type: 'h1',
  props: {
    className: 'title',
    children: 'Hello'
  }
}
```

React 状态变化后，会生成新的虚拟 DOM，然后与旧虚拟 DOM 进行比较，计算需要更新的部分，最后更新真实 DOM。

### 12.2 Diff 算法

React Diff 基于一些假设优化性能：

- 不同类型的元素会生成不同树。
- 同层级列表通过 `key` 判断元素身份。
- 开发者可以通过稳定结构和 key 帮助 React 更准确更新。

例如列表中如果使用稳定 ID 作为 key，React 可以知道哪个元素被移动或更新。如果使用数组下标作为 key，在插入、删除和排序时可能导致组件状态错乱。

### 12.3 Fiber 架构

Fiber 是 React 的新协调架构。它把渲染工作拆成可中断、可恢复的小单元，使 React 能够更好地处理优先级和并发渲染。

Fiber 带来的能力：

- 可中断渲染。
- 任务优先级。
- 并发特性。
- 更灵活的调度。
- 更好的用户交互响应。

## 13. React 渲染机制

### 13.1 渲染触发条件

组件重新渲染通常由以下情况触发：

- state 更新。
- props 变化。
- 父组件重新渲染。
- context value 变化。
- 外部状态订阅变化。

重新渲染不等于真实 DOM 一定更新。React 会先重新执行组件函数，得到新的虚拟 DOM，再通过比较决定是否更新真实 DOM。

### 13.2 批处理更新

React 会把多个状态更新合并成一次渲染，提高性能。

```tsx
setCount(prev => prev + 1)
setName('Alice')
```

在现代 React 中，异步回调、Promise、定时器中的状态更新也可以自动批处理。

### 13.3 StrictMode

`React.StrictMode` 是开发环境下的检查工具。

它可以帮助发现：

- 不安全的生命周期。
- 副作用清理问题。
- 意外的可变数据修改。
- 未来并发特性下可能出问题的代码。

开发环境下 StrictMode 可能会让某些函数执行两次，这是为了暴露潜在副作用问题。生产环境不会这样执行。

## 14. 性能优化

### 14.1 React.memo

`React.memo` 用于缓存组件渲染结果。当 props 没有变化时，可以跳过子组件重新渲染。

```tsx
const UserItem = React.memo(function UserItem({ name }: { name: string }) {
  return <li>{name}</li>
})
```

适合：

- 组件渲染成本较高。
- props 比较稳定。
- 列表项较多。

不适合：

- 组件很简单。
- props 每次都变。
- 过度优化导致代码复杂。

### 14.2 useMemo 与 useCallback

`useMemo` 缓存计算结果，`useCallback` 缓存函数引用。

典型组合：

```tsx
const filteredList = useMemo(() => {
  return list.filter(item => item.name.includes(keyword))
}, [list, keyword])

const handleSelect = useCallback((id: number) => {
  setSelectedId(id)
}, [])
```

优化前先确认性能瓶颈，不要机械使用。

### 14.3 代码分割

使用动态导入和 `React.lazy` 分割代码。

```tsx
const Settings = lazy(() => import('./pages/Settings'))
```

常见分割点：

- 路由页面。
- 大型图表库。
- 富文本编辑器。
- 不常用业务模块。

### 14.4 虚拟列表

当页面需要渲染大量列表数据时，可以使用虚拟列表，只渲染可视区域内的元素。

常见库：

- react-window。
- react-virtualized。
- TanStack Virtual。

适合：

- 上千条列表。
- 大型表格。
- 聊天记录。
- 日志查看器。

### 14.5 避免无意义状态

能由现有数据计算出来的值，不一定要放进 state。

不推荐：

```tsx
const [fullName, setFullName] = useState('')
```

如果 `fullName` 可以由 `firstName` 和 `lastName` 得到，更推荐：

```tsx
const fullName = `${firstName} ${lastName}`
```

减少冗余状态可以避免数据不同步。

### 14.6 性能分析工具

常见工具：

- React DevTools Profiler。
- Chrome Performance。
- Lighthouse。
- Web Vitals。
- Bundle Analyzer。

性能优化应遵循：

1. 先测量。
2. 找瓶颈。
3. 做局部优化。
4. 再验证。

## 15. 样式方案

React 本身不限制样式方案。

常见方式：

- 普通 CSS。
- CSS Modules。
- Sass/Less。
- CSS-in-JS。
- Tailwind CSS。
- 组件库主题系统。

### 15.1 CSS Modules

CSS Modules 可以避免全局类名冲突。

```css
.title {
  color: red;
}
```

```tsx
import styles from './Card.module.css'

function Card() {
  return <h2 className={styles.title}>标题</h2>
}
```

### 15.2 CSS-in-JS

常见库：

- styled-components。
- emotion。

优点：

- 样式和组件逻辑放在一起。
- 支持动态样式。
- 支持主题。

缺点：

- 运行时成本需要关注。
- 服务端渲染配置可能更复杂。

### 15.3 Tailwind CSS

Tailwind CSS 是原子化 CSS 框架。

```tsx
function Button() {
  return (
    <button className="rounded bg-blue-600 px-4 py-2 text-white">
      提交
    </button>
  )
}
```

适合：

- 快速构建界面。
- 统一设计约束。
- 减少手写 CSS 文件。

需要注意类名可读性和团队规范。

## 16. TypeScript 与 React

TypeScript 可以让 React 项目更安全、更容易维护。

### 16.1 Props 类型

```tsx
type UserCardProps = {
  id: number
  name: string
  onSelect?: (id: number) => void
}

function UserCard({ id, name, onSelect }: UserCardProps) {
  return <button onClick={() => onSelect?.(id)}>{name}</button>
}
```

### 16.2 useState 类型

```tsx
const [user, setUser] = useState<User | null>(null)
```

如果初始值能推断出类型，可以省略泛型：

```tsx
const [count, setCount] = useState(0)
```

### 16.3 事件类型

```tsx
const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
  setValue(event.target.value)
}
```

常见事件类型：

- `React.MouseEvent`
- `React.ChangeEvent`
- `React.FormEvent`
- `React.KeyboardEvent`

### 16.4 children 类型

```tsx
type LayoutProps = {
  children: React.ReactNode
}
```

`React.ReactNode` 可以表示字符串、数字、元素、数组、null 等可渲染内容。

### 16.5 ref 类型

```tsx
const inputRef = useRef<HTMLInputElement>(null)
```

组件转发 ref：

```tsx
import { forwardRef } from 'react'

const Input = forwardRef<HTMLInputElement, React.ComponentProps<'input'>>(
  function Input(props, ref) {
    return <input ref={ref} {...props} />
  }
)
```

## 17. 请求与数据处理

### 17.1 fetch

浏览器原生提供 `fetch`。

```tsx
async function getUsers() {
  const res = await fetch('/api/users')

  if (!res.ok) {
    throw new Error('request failed')
  }

  return res.json()
}
```

### 17.2 Axios

Axios 是常用请求库。

```ts
import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(config => {
  config.headers.Authorization = `Bearer ${localStorage.getItem('token')}`
  return config
})
```

常见封装内容：

- baseURL。
- 超时时间。
- 请求头。
- Token 注入。
- 错误统一处理。
- 响应数据解包。
- 取消请求。

### 17.3 请求状态

接口请求通常需要处理：

- loading。
- error。
- data。
- empty。
- retry。

简单示例：

```tsx
function UserList() {
  const [data, setData] = useState<User[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<Error | null>(null)

  useEffect(() => {
    setLoading(true)

    getUsers()
      .then(setData)
      .catch(setError)
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div>加载中</div>
  if (error) return <div>加载失败</div>

  return <UserTable data={data} />
}
```

复杂项目建议使用 TanStack Query 管理服务端状态。

## 18. 错误处理

### 18.1 Error Boundary

Error Boundary 用于捕获子组件渲染过程中的错误，避免整个应用崩溃。

Error Boundary 目前通常使用类组件实现：

```tsx
import React from 'react'

class ErrorBoundary extends React.Component<
  { children: React.ReactNode },
  { hasError: boolean }
> {
  state = {
    hasError: false
  }

  static getDerivedStateFromError() {
    return { hasError: true }
  }

  componentDidCatch(error: Error) {
    console.error(error)
  }

  render() {
    if (this.state.hasError) {
      return <div>页面出错了</div>
    }

    return this.props.children
  }
}
```

Error Boundary 不能捕获：

- 事件处理函数中的错误。
- 异步代码中的错误。
- 服务端渲染错误。
- Error Boundary 自身的错误。

### 18.2 接口错误处理

接口错误建议统一处理，同时允许业务层自定义。

常见策略：

- 401 跳转登录。
- 403 显示无权限。
- 404 显示资源不存在。
- 500 显示服务异常。
- 网络超时允许重试。

### 18.3 空状态和异常状态

成熟界面不应只考虑成功状态，还要考虑：

- 加载中。
- 空数据。
- 请求失败。
- 权限不足。
- 网络断开。
- 表单校验失败。

这些状态是用户体验的重要部分。

## 19. 测试

React 测试常见类型：

- 单元测试。
- 组件测试。
- Hook 测试。
- 集成测试。
- 端到端测试。

常见工具：

- Vitest。
- Jest。
- React Testing Library。
- Cypress。
- Playwright。
- MSW。

### 19.1 组件测试

React Testing Library 推荐从用户视角测试组件行为。

```tsx
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'

test('click button should increase count', async () => {
  render(<Counter />)

  await userEvent.click(screen.getByRole('button', { name: '+' }))

  expect(screen.getByText('1')).toBeInTheDocument()
})
```

测试建议：

- 少测试实现细节。
- 多测试用户能看到和操作的结果。
- 使用语义化查询，如 `getByRole`、`getByLabelText`。
- 接口请求使用 MSW Mock。

## 20. React 工程化

一个成熟 React 项目通常包含：

- Vite 或 Next.js。
- TypeScript。
- ESLint。
- Prettier。
- Stylelint。
- 路由管理。
- 状态管理。
- 请求封装。
- 组件库。
- 单元测试和端到端测试。
- Git Hooks。
- CI/CD。
- 构建产物分析。
- 环境变量管理。

### 20.1 代码规范

常见工具：

- ESLint：检查代码质量。
- Prettier：统一格式。
- eslint-plugin-react：React 规则。
- eslint-plugin-react-hooks：Hooks 规则。
- lint-staged：只检查暂存文件。
- Husky：Git Hooks。

Hooks 规则非常重要，建议开启 `eslint-plugin-react-hooks`。

### 20.2 环境变量

Vite React 项目中，客户端环境变量需要以 `VITE_` 开头。

```env
VITE_API_BASE_URL=https://api.example.com
```

使用：

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL
```

不要把真正的密钥放在前端环境变量中，因为前端代码最终会暴露给用户浏览器。

### 20.3 构建优化

常见优化：

- 路由懒加载。
- 拆分大型依赖。
- 移除未使用代码。
- 图片压缩。
- CDN 加速。
- Gzip/Brotli。
- 分析 Bundle 体积。
- 合理设置缓存策略。

## 21. 服务端渲染 SSR 与 Next.js

React 默认是客户端渲染，但也可以通过框架实现服务端渲染。

### 21.1 CSR

CSR 是 Client Side Rendering，客户端渲染。

特点：

- 首次返回基本 HTML 和 JS。
- 浏览器下载 JS 后渲染页面。
- 页面交互体验好。
- 首屏和 SEO 需要额外优化。

### 21.2 SSR

SSR 是 Server Side Rendering，服务端渲染。

特点：

- 服务端先生成 HTML。
- 浏览器更快看到首屏内容。
- SEO 更友好。
- 服务端压力更大。
- 工程复杂度更高。

### 21.3 SSG

SSG 是 Static Site Generation，静态站点生成。

特点：

- 构建阶段生成 HTML。
- 访问速度快。
- CDN 友好。
- 适合内容相对稳定的页面。

### 21.4 Next.js

Next.js 是 React 生态中非常重要的全栈框架，提供：

- 文件路由。
- SSR。
- SSG。
- API Routes。
- 图片优化。
- 中间件。
- 服务端组件。
- 增量静态再生成。
- 部署优化。

适合：

- 官网。
- 内容站。
- 电商页面。
- 需要 SEO 的应用。
- 全栈 React 应用。

## 22. React Server Components

React Server Components，简称 RSC，是 React 新一代架构能力之一，常见于 Next.js App Router。

它允许部分组件只在服务端渲染，不把对应 JavaScript 发送到浏览器。

Server Component 适合：

- 读取数据库。
- 读取文件系统。
- 请求后端服务。
- 渲染静态或低交互内容。

Client Component 适合：

- 使用 `useState`。
- 使用 `useEffect`。
- 浏览器事件交互。
- 访问 DOM。
- 使用浏览器 API。

在 Next.js 中通常通过文件顶部声明：

```tsx
'use client'
```

有这个声明的组件是 Client Component，没有声明的默认可以作为 Server Component。

RSC 的价值：

- 减少客户端 JavaScript 体积。
- 更靠近数据源。
- 改善首屏性能。
- 让服务端和客户端职责更清晰。

## 23. 常见最佳实践

### 23.1 组件设计

建议：

- 组件职责单一。
- Props 命名清晰。
- 避免组件过大。
- 通用组件不耦合业务。
- 业务组件可以组合通用组件。
- 复杂逻辑抽到 Hook 或 Service。

### 23.2 状态设计

建议：

- 优先使用局部状态。
- 多组件共享时再提升状态。
- 跨层级稳定数据用 Context。
- 复杂全局状态用 Zustand 或 Redux Toolkit。
- 接口数据用 TanStack Query 等服务端状态库。
- 避免重复和派生状态。

### 23.3 Hooks 使用

建议：

- 遵守 Hooks 调用规则。
- 认真维护依赖数组。
- 副作用要清理。
- 不要把所有逻辑都塞进一个 `useEffect`。
- 自定义 Hook 只封装真正可复用的逻辑。

### 23.4 性能实践

建议：

- 首先保证代码清晰。
- 使用 Profiler 找瓶颈。
- 路由级别代码分割。
- 大列表使用虚拟滚动。
- 避免在渲染中执行重计算。
- 不要过度使用 memo。
- 保持 key 稳定。

### 23.5 可维护性实践

建议：

- 使用 TypeScript。
- 明确目录结构。
- 统一请求层。
- 统一错误处理。
- 统一表单校验方案。
- 统一代码规范。
- 保持组件边界清晰。
- 编写关键路径测试。

## 24. 学习路线建议

推荐学习顺序：

1. 掌握 JavaScript、TypeScript、HTML、CSS 基础。
2. 学习 JSX、组件、Props、State。
3. 熟练使用 `useState`、`useEffect`、`useRef`。
4. 学习组件通信和状态提升。
5. 学习自定义 Hook。
6. 学习 React Router。
7. 学习表单处理和请求封装。
8. 学习 Context、Redux Toolkit、Zustand、TanStack Query。
9. 学习性能优化、代码分割、虚拟列表。
10. 学习 React Testing Library、Vitest、Playwright。
11. 学习 Vite、ESLint、Prettier、CI/CD 等工程化工具。
12. 学习 Next.js、SSR、SSG、React Server Components。
13. 阅读优秀组件库和业务项目源码。

## 25. 总结

React 的核心不是某个 API，而是围绕“状态驱动 UI”的开发模型。掌握 React，需要理解组件、Props、State、Hooks、单向数据流、渲染机制、状态管理和性能优化。

在真实项目中，React 还需要和路由、请求、表单、状态管理、测试、构建工具、样式方案、服务端渲染等技术结合使用。学习 React 的最好方式，是先理解它的核心模型，再通过项目逐步补齐工程化、性能和架构能力。
