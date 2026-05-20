# React 前端实现对比文档

本文档对比了 Vue 版本和 React 版本的前端实现，帮助理解两种框架的差异和各自的实现方式。

## 技术栈对比

| Vue 技术栈 | React 对应技术栈 | 说明 |
|---------|---------------|------|
| Vue 3 (Composition API) | React 18 + Hooks | 使用Hooks实现组件逻辑 |
| TypeScript | TypeScript | 保持类型安全 |
| Vite | Vite | 使用相同构建工具 |
| Element Plus | Ant Design | React生态最成熟的UI库 |
| Pinia | Zustand | 轻量级状态管理，比Redux简洁 |
| Vue Router | React Router v6 | React标准路由方案 |
| Axios | Axios | 保持相同的HTTP库 |
| dayjs | dayjs | 保持相同的日期库 |
| echarts | echarts-react | React封装的ECharts |
| sortablejs | dnd-kit | React拖拽库 |

## 核心概念对比

### 1. 组件定义

#### Vue 方式
```vue
<script setup>
import { ref, computed } from 'vue'

const count = ref(0)
const doubled = computed(() => count.value * 2)

function increment() {
  count.value++
}
</script>

<template>
  <div>{{ count }}</div>
  <button @click="increment">增加</button>
</template>
```

#### React 方式
```tsx
import { useState, useMemo } from 'react'

function Component() {
  const [count, setCount] = useState(0)
  const doubled = useMemo(() => count * 2, [count])

  function increment() {
    setCount(c => c + 1)
  }

  return (
    <div>
      <div>{count}</div>
      <button onClick={increment}>增加</button>
    </div>
  )
}
```

### 2. 状态管理

#### Pinia (Vue)
```typescript
export const useUserStore = defineStore('user', () => {
  const token = ref('')
  function setToken(val: string) { token.value = val }
  return { token, setToken }
})
```

#### Zustand (React)
```typescript
const useUserStore = create((set) => ({
  token: '',
  setToken: (val: string) => set({ token: val })
}))
```

### 3. 路由配置

#### Vue Router
```typescript
const routes = [
  { path: '/dashboard', component: DashboardView }
]
```

#### React Router
```tsx
const routes = [
  { path: '/dashboard', element: <DashboardPage /> }
]
```

## 实现差异

### 1. 响应式数据

**Vue:**
- 使用 `ref()` 和 `reactive()` 创建响应式数据
- 自动追踪依赖，无需手动声明
- 访问 ref 需要使用 `.value`

**React:**
- 使用 `useState()` 创建状态
- 需要在依赖数组中声明依赖
- 直接访问状态值

### 2. 计算属性

**Vue:**
```typescript
const doubled = computed(() => count.value * 2)
```

**React:**
```typescript
const doubled = useMemo(() => count * 2, [count])
```

### 3. 生命周期

**Vue:**
```typescript
onMounted(() => {
  // 组件挂载后
})

onUnmounted(() => {
  // 组件卸载前
})
```

**React:**
```typescript
useEffect(() => {
  // 组件挂载后
  return () => {
    // 组件卸载前（清理函数）
  }
}, [])
```

### 4. 表单处理

**Vue:**
```vue
<script setup>
const form = ref({ username: '', password: '' })
</script>

<template>
  <input v-model="form.username" />
</template>
```

**React:**
```tsx
const [form, setForm] = useState({ username: '', password: '' })

return (
  <input
    value={form.username}
    onChange={(e) => setForm({ ...form, username: e.target.value })}
  />
)
```

### 5. 条件渲染

**Vue:**
```vue
<div v-if="condition">内容</div>
<div v-else>其他内容</div>
```

**React:**
```tsx
{condition ? (
  <div>内容</div>
) : (
  <div>其他内容</div>
)}
```

### 6. 列表渲染

**Vue:**
```vue
<li v-for="item in list" :key="item.id">{{ item.name }}</li>
```

**React:**
```tsx
{list.map(item => (
  <li key={item.id}>{item.name}</li>
))}
```

### 7. 事件处理

**Vue:**
```vue
<button @click="handleClick">点击</button>
<button @click.stop="handleClick">阻止冒泡</button>
```

**React:**
```tsx
<button onClick={handleClick}>点击</button>
<button onClick={(e) => { e.stopPropagation(); handleClick(); }}>阻止冒泡</button>
```

## API 调用对比

### Vue Composition API
```typescript
const { data, loading, error } = useAsyncData(() => planApi.list())
```

### React Hooks
```typescript
const [data, setData] = useState([])
const [loading, setLoading] = useState(false)

useEffect(() => {
  async function fetchData() {
    setLoading(true)
    try {
      const result = await planApi.list()
      setData(result)
    } finally {
      setLoading(false)
    }
  }
  fetchData()
}, [])
```

## 组件通信

### Props

**Vue:**
```vue
<script setup>
const props = defineProps<{ title: string }>()
</script>

<template>
  <div>{{ props.title }}</div>
</template>
```

**React:**
```tsx
interface Props {
  title: string
}

function Component({ title }: Props) {
  return <div>{title}</div>
}
```

### 事件

**Vue:**
```vue
<script setup>
const emit = defineEmits<{ update: [value: string] }>()

function handleChange(value: string) {
  emit('update', value)
}
</script>

<template>
  <input @change="handleChange" />
</template>
```

**React:**
```tsx
interface Props {
  onUpdate: (value: string) => void
}

function Component({ onUpdate }: Props) {
  return (
    <input onChange={(e) => onUpdate(e.target.value)} />
  )
}
```

## 拖拽实现

### Vue (sortablejs)
```vue
<script setup>
import { ref } from 'vue'
import Sortable from 'sortablejs'

const list = ref([])
const listRef = ref<HTMLElement>()

onMounted(() => {
  new Sortable(listRef.value!, {
    onEnd: (evt) => {
      // 处理拖拽结束
    }
  })
})
</script>

<template>
  <div ref="listRef">
    <div v-for="item in list" :key="item.id">{{ item.title }}</div>
  </div>
</template>
```

### React (dnd-kit)
```tsx
import { DndContext, closestCenter } from '@dnd-kit/core'
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable'

function Component() {
  const [items, setItems] = useState([])

  function handleDragEnd(event) {
    // 处理拖拽结束
  }

  return (
    <DndContext onDragEnd={handleDragEnd}>
      <SortableContext items={items} strategy={verticalListSortingStrategy}>
        {items.map(item => <SortableItem key={item.id} item={item} />)}
      </SortableContext>
    </DndContext>
  )
}
```

## 学习建议

### 对于 Vue 开发者学习 React

1. **理解 Hooks 的概念**
   - Hooks 是 React 的核心，相当于 Vue 的 Composition API
   - 重点理解 `useState`, `useEffect`, `useMemo`, `useCallback`

2. **注意依赖数组**
   - React 需要显式声明依赖，Vue 自动追踪
   - 避免遗漏依赖导致的问题

3. **理解不可变数据**
   - React 状态更新是替换而非修改
   - 使用展开运算符或 `immer` 等工具

4. **事件处理差异**
   - React 事件是小写的，HTML 属性格式
   - 需要手动调用 `e.preventDefault()`

### 对于 React 开发者学习 Vue

1. **理解响应式系统**
   - Vue 的自动响应式更直观
   - 不需要手动声明依赖

2. **模板语法**
   - Vue 使用模板语法，类似 HTML
   - 指令如 `v-if`, `v-for`, `v-model` 等

3. **双向绑定**
   - Vue 的 `v-model` 简化了表单处理
   - React 需要手动处理 value 和 onChange

4. **组件定义**
   - Vue 单文件组件包含 template, script, style
   - 更加集中和直观

## 性能优化对比

### Vue

- 自动依赖追踪，精确更新
- 计算属性自动缓存
- 指令系统优化渲染

### React

- 需要手动优化（useMemo, useCallback）
- Virtual DOM diff 算法
- 需要避免不必要的重渲染

## 总结

**Vue 的优势：**
- 学习曲线平缓，语法直观
- 自动响应式，减少样板代码
- 完整的生态系统

**React 的优势：**
- 更灵活的编程模型
- 更大的社区和就业市场
- 更多的学习资源

两种框架都能实现相同的功能，选择主要取决于团队偏好和项目需求。通过对比学习，可以更好地理解各自的优缺点，在适当的时候选择合适的工具。