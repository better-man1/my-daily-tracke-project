# Vue3 技术知识点总结

## 1. Vue3 是什么

Vue3 是一个用于构建用户界面的渐进式 JavaScript 框架。它可以用于简单页面增强，也可以通过 Vue Router、Pinia、Vite、TypeScript 等生态工具构建复杂的单页应用和大型前端系统。

Vue3 的核心特点：

- 渐进式：可以按需引入，从局部页面增强到完整应用都适用。
- 声明式渲染：开发者描述状态和界面的关系，Vue 负责更新 DOM。
- 组件化：把页面拆成可复用、可组合、可维护的组件。
- 响应式系统：数据变化自动驱动视图更新。
- Composition API：更适合复杂逻辑组织和复用。
- 更好的 TypeScript 支持。
- 更高性能的响应式和编译优化。

Vue3 常见应用场景：

- 后台管理系统。
- 企业中台。
- 移动端 H5。
- 运营活动页。
- 低代码平台。
- 组件库。
- 可视化大屏。
- SSR 应用，如 Nuxt。

## 2. Vue3 与 Vue2 的主要区别

Vue3 在设计和实现上相比 Vue2 有较大升级。

主要变化：

- 使用 Proxy 重写响应式系统。
- 引入 Composition API。
- 支持多个根节点 Fragment。
- 更好的 TypeScript 支持。
- 更小的运行时体积。
- 更强的 Tree Shaking 能力。
- 新增 Teleport、Suspense 等内置组件。
- 生命周期命名发生变化。
- 全局 API 改为应用实例 API。
- 推荐使用 Pinia 替代 Vuex。

Vue2 示例：

```js
new Vue({
  el: '#app',
  render: h => h(App)
})
```

Vue3 示例：

```ts
import { createApp } from 'vue'
import App from './App.vue'

createApp(App).mount('#app')
```

Vue3 的变化不是简单语法替换，而是更适合大型项目、逻辑复用和类型推导的整体升级。

## 3. Vue3 项目基础结构

现代 Vue3 项目通常使用 Vite 创建。

典型目录结构：

```text
src
├─ assets
├─ components
├─ composables
├─ layouts
├─ pages
├─ router
├─ services
├─ stores
├─ styles
├─ types
├─ utils
├─ App.vue
└─ main.ts
```

目录说明：

- `assets`：图片、字体等静态资源。
- `components`：通用组件。
- `composables`：组合式函数，类似 React 自定义 Hook。
- `layouts`：布局组件。
- `pages`：页面组件。
- `router`：路由配置。
- `services`：接口请求。
- `stores`：Pinia 状态管理。
- `styles`：全局样式、变量、主题。
- `types`：TypeScript 类型声明。
- `utils`：通用工具函数。
- `App.vue`：根组件。
- `main.ts`：应用入口。

应用入口示例：

```ts
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.mount('#app')
```

## 4. 单文件组件 SFC

### 4.1 SFC 是什么

Vue 单文件组件，简称 SFC，是 `.vue` 文件。它把模板、逻辑和样式组织在一个文件中。

基本结构：

```vue
<template>
  <section class="user-card">
    <h2>{{ user.name }}</h2>
    <p>{{ user.email }}</p>
  </section>
</template>

<script setup lang="ts">
type User = {
  name: string
  email: string
}

const user: User = {
  name: 'Alice',
  email: 'alice@example.com'
}
</script>

<style scoped>
.user-card {
  padding: 16px;
}
</style>
```

SFC 的优势：

- 组件结构清晰。
- 模板、逻辑、样式聚合。
- 支持编译优化。
- 支持局部样式。
- 支持 TypeScript。
- 适合工程化管理。

### 4.2 template

`template` 用于描述组件的视图结构。

Vue 模板语法接近 HTML，但支持指令、插值、表达式、事件绑定等能力。

```vue
<template>
  <button @click="count++">
    点击次数：{{ count }}
  </button>
</template>
```

模板中可以使用：

- 文本插值。
- 指令。
- 事件绑定。
- 属性绑定。
- 条件渲染。
- 列表渲染。
- 插槽。
- 组件。

### 4.3 script setup

`<script setup>` 是 Vue3 推荐写法。它是 Composition API 的编译时语法糖。

特点：

- 写法更简洁。
- 顶层变量可直接在模板使用。
- 组件导入后可直接在模板使用。
- 更好的 TypeScript 推导。
- defineProps、defineEmits 等宏无需导入。

示例：

```vue
<script setup lang="ts">
import BaseButton from '@/components/BaseButton.vue'

const message = 'hello vue3'
</script>

<template>
  <BaseButton>{{ message }}</BaseButton>
</template>
```

### 4.4 style scoped

`scoped` 用于让样式只作用于当前组件。

```vue
<style scoped>
.title {
  color: red;
}
</style>
```

Vue 会在编译时为当前组件 DOM 添加特殊属性选择器，从而实现样式隔离。

注意：

- `scoped` 不是 Shadow DOM。
- 父组件样式仍可能影响子组件根节点。
- 深度选择器需要使用 `:deep()`。

```css
:deep(.child-title) {
  color: blue;
}
```

## 5. 模板语法

### 5.1 文本插值

文本插值使用双大括号。

```vue
<template>
  <p>{{ message }}</p>
</template>
```

插值中可以写 JavaScript 表达式：

```vue
<p>{{ count + 1 }}</p>
<p>{{ isActive ? '启用' : '禁用' }}</p>
```

不建议在模板中写过于复杂的表达式，复杂逻辑应放到计算属性或方法中。

### 5.2 v-bind

`v-bind` 用于动态绑定属性，简写为 `:`。

```vue
<img :src="avatarUrl" :alt="username" />
```

绑定 class：

```vue
<div :class="{ active: isActive, disabled: isDisabled }"></div>
```

绑定 style：

```vue
<div :style="{ color: textColor, fontSize: size + 'px' }"></div>
```

### 5.3 v-on

`v-on` 用于绑定事件，简写为 `@`。

```vue
<button @click="submit">提交</button>
```

传递参数：

```vue
<button @click="removeItem(item.id)">删除</button>
```

事件对象：

```vue
<button @click="handleClick($event)">点击</button>
```

常用事件修饰符：

- `.stop`：阻止事件冒泡。
- `.prevent`：阻止默认行为。
- `.once`：只触发一次。
- `.capture`：使用捕获模式。
- `.self`：只有事件目标是当前元素时触发。

```vue
<form @submit.prevent="submitForm"></form>
```

### 5.4 v-if 与 v-show

`v-if` 是条件渲染，会真正创建或销毁 DOM。

```vue
<p v-if="isLogin">已登录</p>
<p v-else>未登录</p>
```

`v-show` 是显示隐藏，通过 CSS `display` 控制。

```vue
<p v-show="visible">内容</p>
```

选择建议：

- 条件很少变化，用 `v-if`。
- 频繁切换显示隐藏，用 `v-show`。

### 5.5 v-for

`v-for` 用于列表渲染。

```vue
<ul>
  <li v-for="user in users" :key="user.id">
    {{ user.name }}
  </li>
</ul>
```

`key` 很重要，它帮助 Vue 识别节点身份。

不推荐用数组下标作为 key，尤其是列表会新增、删除或排序时。应优先使用稳定唯一 ID。

### 5.6 v-model

`v-model` 用于双向绑定。

```vue
<input v-model="keyword" />
```

等价于：

```vue
<input
  :value="keyword"
  @input="keyword = $event.target.value"
/>
```

组件中使用 `v-model`：

```vue
<!-- Parent.vue -->
<UserEditor v-model="username" />
```

子组件：

```vue
<script setup lang="ts">
const model = defineModel<string>()
</script>

<template>
  <input v-model="model" />
</template>
```

Vue3.4 之后推荐使用 `defineModel` 简化组件双向绑定。

## 6. 响应式系统

### 6.1 响应式是什么

响应式是 Vue 的核心能力。它让数据和视图建立自动关系，当数据变化时，依赖这些数据的视图和计算结果会自动更新。

示例：

```vue
<script setup lang="ts">
import { ref } from 'vue'

const count = ref(0)
</script>

<template>
  <button @click="count++">{{ count }}</button>
</template>
```

当 `count` 改变时，模板会自动重新渲染。

### 6.2 ref

`ref` 用于创建响应式数据，可以包装基本类型和对象。

```ts
import { ref } from 'vue'

const count = ref(0)
const name = ref('Alice')
```

在 JavaScript 中访问或修改时，需要使用 `.value`：

```ts
count.value++
```

在模板中会自动解包：

```vue
<p>{{ count }}</p>
```

适合使用 `ref` 的场景：

- 基本类型。
- 单个值。
- DOM 引用。
- 希望保持引用稳定的数据。

### 6.3 reactive

`reactive` 用于创建响应式对象。

```ts
import { reactive } from 'vue'

const user = reactive({
  name: 'Alice',
  age: 18
})

user.age++
```

注意：

- `reactive` 只能用于对象、数组、Map、Set 等复杂类型。
- 解构 reactive 对象会丢失响应式。

错误示例：

```ts
const { name } = user
```

如果需要解构并保持响应式，可以使用 `toRefs`：

```ts
import { toRefs } from 'vue'

const { name, age } = toRefs(user)
```

### 6.4 computed

`computed` 用于声明计算属性。它会基于依赖缓存结果，只有依赖变化时才重新计算。

```ts
import { computed, ref } from 'vue'

const firstName = ref('Alice')
const lastName = ref('Smith')

const fullName = computed(() => {
  return `${firstName.value} ${lastName.value}`
})
```

适合：

- 从已有状态派生数据。
- 需要缓存的计算结果。
- 模板中复杂表达式抽离。

可写计算属性：

```ts
const fullName = computed({
  get() {
    return `${firstName.value} ${lastName.value}`
  },
  set(value: string) {
    const [first, last] = value.split(' ')
    firstName.value = first
    lastName.value = last
  }
})
```

### 6.5 watch

`watch` 用于监听响应式数据变化并执行副作用。

```ts
import { ref, watch } from 'vue'

const keyword = ref('')

watch(keyword, newValue => {
  console.log('keyword changed:', newValue)
})
```

监听多个数据：

```ts
watch([startDate, endDate], ([newStart, newEnd]) => {
  console.log(newStart, newEnd)
})
```

深度监听：

```ts
watch(
  user,
  newValue => {
    console.log(newValue)
  },
  { deep: true }
)
```

注意：深度监听成本较高，不应滥用。

### 6.6 watchEffect

`watchEffect` 会自动收集副作用中使用到的响应式依赖。

```ts
watchEffect(() => {
  console.log(count.value)
})
```

区别：

- `watch` 明确指定监听源。
- `watchEffect` 自动追踪依赖。

`watchEffect` 适合依赖关系简单、需要立即执行的副作用。复杂场景中 `watch` 更可控。

### 6.7 响应式原理

Vue3 使用 Proxy 实现响应式。

核心过程：

1. 通过 Proxy 拦截对象的读取和写入。
2. 读取属性时收集依赖。
3. 写入属性时触发依赖更新。
4. 组件重新渲染或计算属性重新计算。

简化理解：

```text
读取数据 -> track 收集依赖
修改数据 -> trigger 触发更新
```

Vue3 相比 Vue2 的优势：

- 可以监听新增属性。
- 可以监听删除属性。
- 对数组支持更自然。
- 对 Map、Set 支持更好。
- 性能和类型支持更好。

## 7. Composition API

### 7.1 Composition API 是什么

Composition API 是 Vue3 推荐的逻辑组织方式。它允许开发者按功能组织代码，而不是按选项类型分散代码。

Options API 示例：

```js
export default {
  data() {},
  computed: {},
  methods: {},
  mounted() {}
}
```

Composition API 示例：

```ts
const count = ref(0)
const double = computed(() => count.value * 2)

function increment() {
  count.value++
}
```

Composition API 的优势：

- 更适合复杂组件。
- 更方便逻辑复用。
- 更好的 TypeScript 支持。
- 更容易按业务功能组织代码。
- 避免 mixin 命名冲突和来源不清。

### 7.2 Composable

Composable 是封装可复用逻辑的函数，通常以 `use` 开头。

示例：

```ts
import { onMounted, onUnmounted, ref } from 'vue'

export function useWindowSize() {
  const width = ref(window.innerWidth)
  const height = ref(window.innerHeight)

  const update = () => {
    width.value = window.innerWidth
    height.value = window.innerHeight
  }

  onMounted(() => {
    window.addEventListener('resize', update)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', update)
  })

  return {
    width,
    height
  }
}
```

组件中使用：

```ts
const { width, height } = useWindowSize()
```

Composable 适合封装：

- 请求逻辑。
- 表单逻辑。
- 权限逻辑。
- 浏览器事件。
- 本地存储。
- WebSocket。
- 复杂业务状态。

## 8. 生命周期

Vue3 Composition API 生命周期：

- `onBeforeMount`
- `onMounted`
- `onBeforeUpdate`
- `onUpdated`
- `onBeforeUnmount`
- `onUnmounted`
- `onActivated`
- `onDeactivated`
- `onErrorCaptured`

常用示例：

```ts
import { onMounted, onUnmounted } from 'vue'

onMounted(() => {
  console.log('组件挂载完成')
})

onUnmounted(() => {
  console.log('组件卸载完成')
})
```

常见用途：

- `onMounted`：请求初始数据、访问 DOM、注册事件。
- `onUnmounted`：清除定时器、取消订阅、移除事件监听。
- `onUpdated`：DOM 更新后执行操作。
- `onActivated`：KeepAlive 缓存组件被激活。
- `onDeactivated`：KeepAlive 缓存组件被停用。

生命周期使用建议：

- 数据请求不一定都要放在 `onMounted`，也可以在路由守卫、Pinia action、异步组件或 SSR 数据钩子中处理。
- 注册的副作用必须在卸载时清理。
- 不要在 `onUpdated` 中无条件修改状态，容易造成循环更新。

## 9. 组件基础

### 9.1 组件注册

在 `<script setup>` 中导入组件即可直接使用。

```vue
<script setup lang="ts">
import UserCard from '@/components/UserCard.vue'
</script>

<template>
  <UserCard />
</template>
```

全局组件注册：

```ts
app.component('BaseButton', BaseButton)
```

建议：

- 通用基础组件可以全局注册。
- 业务组件优先局部导入。
- 大项目可以使用自动导入插件提升效率。

### 9.2 Props

Props 用于父组件向子组件传递数据。

```vue
<script setup lang="ts">
defineProps<{
  title: string
  count?: number
}>()
</script>
```

带默认值：

```ts
const props = withDefaults(
  defineProps<{
    size?: 'small' | 'medium' | 'large'
  }>(),
  {
    size: 'medium'
  }
)
```

Props 原则：

- Props 是只读的。
- 子组件不应直接修改 Props。
- 修改应通过 emit 通知父组件。

### 9.3 Emits

Emits 用于子组件向父组件发送事件。

```vue
<script setup lang="ts">
const emit = defineEmits<{
  change: [value: string]
  submit: []
}>()

function handleChange(value: string) {
  emit('change', value)
}
</script>
```

父组件监听：

```vue
<UserInput @change="handleChange" />
```

### 9.4 Slots

插槽用于组件内容分发，让组件更灵活。

默认插槽：

```vue
<!-- BaseCard.vue -->
<template>
  <section class="card">
    <slot />
  </section>
</template>
```

使用：

```vue
<BaseCard>
  <p>卡片内容</p>
</BaseCard>
```

具名插槽：

```vue
<template>
  <header>
    <slot name="header" />
  </header>
  <main>
    <slot />
  </main>
</template>
```

作用域插槽：

```vue
<slot name="item" :item="item" />
```

作用域插槽适合让父组件决定某部分如何渲染。

### 9.5 expose

`defineExpose` 用于显式暴露组件内部方法或状态给父组件。

```vue
<script setup lang="ts">
function focus() {
  console.log('focus')
}

defineExpose({
  focus
})
</script>
```

父组件通过 ref 调用：

```vue
<Child ref="childRef" />
```

建议谨慎使用，优先通过 Props 和 Emits 通信。`defineExpose` 更适合表单组件、弹窗组件等需要暴露命令式方法的场景。

## 10. 组件通信

### 10.1 父传子

通过 Props。

```vue
<UserCard :user="user" />
```

### 10.2 子传父

通过 Emits。

```vue
<UserEditor @submit="saveUser" />
```

### 10.3 双向绑定

通过 `v-model`。

```vue
<SearchBox v-model="keyword" />
```

适合表单、弹窗开关、筛选条件等场景。

### 10.4 跨层级通信

使用 provide/inject。

```ts
import { provide } from 'vue'

provide('theme', 'dark')
```

子孙组件：

```ts
import { inject } from 'vue'

const theme = inject('theme')
```

建议使用 Symbol 作为 key，提高类型安全和避免冲突。

```ts
export const ThemeKey = Symbol('theme')
```

### 10.5 全局状态通信

复杂应用使用 Pinia 管理全局状态。

适合：

- 用户信息。
- 权限信息。
- 全局配置。
- 跨页面共享状态。
- 多组件共同修改的数据。

不建议滥用全局状态。局部状态应留在组件内部，跨组件共享再提升或放入 Store。

## 11. Pinia 状态管理

### 11.1 Pinia 是什么

Pinia 是 Vue 官方推荐的状态管理库，用于替代 Vuex。它 API 简洁、类型推导友好，适合 Vue3 项目。

核心概念：

- Store：状态容器。
- State：状态数据。
- Getter：派生状态。
- Action：修改状态和处理异步逻辑。

### 11.2 定义 Store

```ts
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    profile: null as null | { id: number; name: string }
  }),
  getters: {
    isLogin: state => Boolean(state.token)
  },
  actions: {
    setToken(token: string) {
      this.token = token
    },
    async fetchProfile() {
      this.profile = await getUserProfile()
    }
  }
})
```

组件中使用：

```ts
const userStore = useUserStore()

userStore.setToken('xxx')
```

### 11.3 Setup Store

Pinia 也支持类似 Composition API 的写法。

```ts
export const useCounterStore = defineStore('counter', () => {
  const count = ref(0)
  const double = computed(() => count.value * 2)

  function increment() {
    count.value++
  }

  return {
    count,
    double,
    increment
  }
})
```

Setup Store 更灵活，适合复杂逻辑和组合式函数复用。

### 11.4 Store 设计建议

建议：

- 按业务领域拆分 Store。
- 不要把所有状态放进一个巨大 Store。
- Action 中处理异步请求和状态变更。
- Getter 只做派生计算，不做副作用。
- 敏感数据不要盲目持久化到 localStorage。
- 服务端数据可结合请求缓存库管理。

常见 Store 拆分：

- `userStore`：用户信息、登录状态。
- `permissionStore`：权限、菜单、路由。
- `settingsStore`：主题、语言、布局配置。
- `cartStore`：购物车。
- `notificationStore`：消息提醒。

## 12. Vue Router

### 12.1 Vue Router 是什么

Vue Router 是 Vue 官方路由库，用于构建单页应用。

基础配置：

```ts
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/pages/Home.vue')
    },
    {
      path: '/users/:id',
      component: () => import('@/pages/UserDetail.vue')
    }
  ]
})

export default router
```

### 12.2 路由模式

常见模式：

- `createWebHistory`：HTML5 history 模式，URL 更美观，需要服务端配置 fallback。
- `createWebHashHistory`：hash 模式，兼容性好，不需要服务端额外配置。
- `createMemoryHistory`：内存模式，常用于 SSR 或测试。

### 12.3 动态路由参数

路由：

```ts
{
  path: '/users/:id',
  component: UserDetail
}
```

组件中读取：

```ts
import { useRoute } from 'vue-router'

const route = useRoute()
const id = route.params.id
```

### 12.4 编程式导航

```ts
import { useRouter } from 'vue-router'

const router = useRouter()

router.push('/dashboard')
router.replace('/login')
```

带参数：

```ts
router.push({
  name: 'UserDetail',
  params: {
    id: 1
  }
})
```

### 12.5 导航守卫

全局前置守卫：

```ts
router.beforeEach((to, from) => {
  const isLogin = Boolean(localStorage.getItem('token'))

  if (to.meta.requiresAuth && !isLogin) {
    return '/login'
  }
})
```

常见用途：

- 登录校验。
- 权限校验。
- 页面标题。
- 埋点统计。
- 动态路由加载。

### 12.6 路由懒加载

```ts
{
  path: '/settings',
  component: () => import('@/pages/Settings.vue')
}
```

路由懒加载可以把页面代码拆分成独立 chunk，减少首屏加载体积。

## 13. 表单处理

### 13.1 基础表单绑定

```vue
<script setup lang="ts">
const form = reactive({
  username: '',
  password: ''
})
</script>

<template>
  <input v-model="form.username" />
  <input v-model="form.password" type="password" />
</template>
```

### 13.2 常见表单控件

输入框：

```vue
<input v-model="text" />
```

复选框：

```vue
<input v-model="checked" type="checkbox" />
```

单选框：

```vue
<input v-model="gender" type="radio" value="male" />
```

下拉框：

```vue
<select v-model="city">
  <option value="shanghai">上海</option>
  <option value="beijing">北京</option>
</select>
```

### 13.3 表单校验

复杂表单建议使用成熟方案：

- Element Plus Form。
- Ant Design Vue Form。
- VeeValidate。
- FormKit。
- Zod。
- Yup。

表单校验重点：

- 必填校验。
- 格式校验。
- 长度校验。
- 异步校验。
- 联动校验。
- 提交前整体校验。
- 错误信息展示。

### 13.4 表单设计建议

建议：

- 表单状态集中管理。
- 校验规则靠近字段定义。
- 提交时统一处理 loading。
- 防止重复提交。
- 服务端错误要能映射到字段。
- 复杂表单拆成多个子组件。
- 表单数据和展示状态适当分离。

## 14. 请求与接口封装

### 14.1 Axios 封装

Vue 项目中常用 Axios 请求接口。

```ts
import axios from 'axios'

export const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
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
  error => {
    return Promise.reject(error)
  }
)
```

### 14.2 接口分层

建议按业务模块组织 API。

```text
services
├─ user.ts
├─ order.ts
└─ product.ts
```

示例：

```ts
export function getUserList(params: UserQuery) {
  return request.get<User[]>('/users', { params })
}
```

好处：

- 接口集中维护。
- 类型更清晰。
- 方便替换请求库。
- 组件不直接关心 URL 细节。

### 14.3 请求状态管理

接口请求通常涉及：

- loading。
- data。
- error。
- empty。
- retry。

可以封装组合式函数：

```ts
export function useRequest<T>(requestFn: () => Promise<T>) {
  const data = ref<T | null>(null)
  const loading = ref(false)
  const error = ref<unknown>(null)

  async function run() {
    loading.value = true
    error.value = null

    try {
      data.value = await requestFn()
    } catch (err) {
      error.value = err
    } finally {
      loading.value = false
    }
  }

  return {
    data,
    loading,
    error,
    run
  }
}
```

大型项目也可以使用 Vue Query 管理服务端状态。

## 15. TypeScript 与 Vue3

### 15.1 为什么 Vue3 更适合 TypeScript

Vue3 从设计上增强了 TypeScript 支持，尤其是 Composition API 和 `<script setup>` 能提供更自然的类型推导。

TypeScript 在 Vue3 中的价值：

- 明确 Props 类型。
- 明确 Emits 类型。
- 明确接口数据类型。
- 明确 Store 类型。
- 提升组件使用体验。
- 降低重构风险。

### 15.2 Props 类型

```ts
defineProps<{
  id: number
  title: string
  visible?: boolean
}>()
```

带默认值：

```ts
withDefaults(
  defineProps<{
    size?: 'small' | 'medium' | 'large'
  }>(),
  {
    size: 'medium'
  }
)
```

### 15.3 Emits 类型

```ts
const emit = defineEmits<{
  save: [id: number]
  cancel: []
}>()
```

使用：

```ts
emit('save', 1)
```

### 15.4 Ref 类型

```ts
const count = ref<number>(0)
const inputRef = ref<HTMLInputElement | null>(null)
```

模板 ref 使用：

```vue
<input ref="inputRef" />
```

### 15.5 组件实例类型

```ts
import type UserDialog from './UserDialog.vue'

const dialogRef = ref<InstanceType<typeof UserDialog> | null>(null)
```

用于调用子组件通过 `defineExpose` 暴露的方法。

### 15.6 类型设计建议

建议：

- 接口响应数据定义类型。
- 表单模型定义类型。
- 路由 meta 扩展类型。
- Pinia Store 保持类型推导。
- 避免滥用 `any`。
- 对复杂类型保持可读性，不要过度炫技。

## 16. 样式方案

### 16.1 Scoped CSS

Vue SFC 原生支持 `scoped`。

```vue
<style scoped>
.button {
  color: white;
}
</style>
```

适合组件局部样式。

### 16.2 CSS Modules

```vue
<style module>
.title {
  color: red;
}
</style>

<template>
  <h1 :class="$style.title">标题</h1>
</template>
```

CSS Modules 适合需要强隔离和类名模块化的项目。

### 16.3 Sass/Less

```vue
<style scoped lang="scss">
.card {
  &__title {
    font-weight: 600;
  }
}
</style>
```

适合：

- 变量。
- 嵌套。
- mixin。
- 函数。
- 主题样式。

### 16.4 CSS 变量与主题

CSS 变量适合实现主题切换。

```css
:root {
  --primary-color: #1677ff;
}

[data-theme='dark'] {
  --primary-color: #4dabf7;
}
```

组件中使用：

```css
.button {
  background: var(--primary-color);
}
```

### 16.5 UI 组件库

常见 Vue3 UI 库：

- Element Plus。
- Ant Design Vue。
- Naive UI。
- Vant。
- Arco Design Vue。

选型考虑：

- 组件完整度。
- TypeScript 支持。
- 主题定制能力。
- 文档质量。
- 社区活跃度。
- 项目视觉规范。

## 17. 内置组件

### 17.1 KeepAlive

`KeepAlive` 用于缓存组件实例。

```vue
<KeepAlive>
  <component :is="currentComponent" />
</KeepAlive>
```

常见用途：

- 缓存页面状态。
- 保留表单输入。
- 保留列表滚动位置。

配合生命周期：

- `onActivated`
- `onDeactivated`

### 17.2 Teleport

`Teleport` 可以把组件内容渲染到 DOM 的其他位置。

```vue
<Teleport to="body">
  <div class="modal">弹窗内容</div>
</Teleport>
```

适合：

- 弹窗。
- 抽屉。
- Toast。
- Tooltip。
- 全局浮层。

### 17.3 Suspense

`Suspense` 用于协调异步组件加载状态。

```vue
<Suspense>
  <template #default>
    <AsyncComponent />
  </template>

  <template #fallback>
    <div>加载中...</div>
  </template>
</Suspense>
```

适合异步组件和异步 setup 场景。

### 17.4 Transition

`Transition` 用于单元素或组件过渡动画。

```vue
<Transition name="fade">
  <p v-if="visible">内容</p>
</Transition>
```

CSS：

```css
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
```

### 17.5 TransitionGroup

`TransitionGroup` 用于列表过渡。

```vue
<TransitionGroup name="list" tag="ul">
  <li v-for="item in list" :key="item.id">
    {{ item.name }}
  </li>
</TransitionGroup>
```

## 18. 自定义指令

自定义指令用于封装直接操作 DOM 的逻辑。

局部指令：

```ts
const vFocus = {
  mounted(el: HTMLElement) {
    el.focus()
  }
}
```

模板中使用：

```vue
<input v-focus />
```

常见场景：

- 自动聚焦。
- 权限控制。
- 点击外部关闭。
- 图片懒加载。
- 埋点。
- 拖拽。

建议：

- 优先用组件和组合式函数。
- 只有确实需要直接操作 DOM 时再使用指令。
- 指令中注册的事件要在卸载时清理。

## 19. 插件机制

Vue 插件用于为应用添加全局能力。

插件示例：

```ts
import type { App } from 'vue'

export default {
  install(app: App) {
    app.config.globalProperties.$formatDate = formatDate
    app.provide('formatDate', formatDate)
  }
}
```

使用：

```ts
app.use(MyPlugin)
```

适合封装：

- UI 组件库。
- 国际化。
- 权限系统。
- 全局工具。
- 埋点。
- 日志。
- 请求配置。

## 20. 权限设计

Vue 后台系统常见权限：

- 登录权限。
- 路由权限。
- 菜单权限。
- 按钮权限。
- 接口权限。
- 数据权限。

前端权限常见实现：

- 路由 meta 标记权限。
- 导航守卫校验登录状态。
- 根据后端返回菜单生成动态路由。
- 按钮通过指令或组件控制显示。
- 请求拦截器处理 401/403。

路由 meta 示例：

```ts
{
  path: '/admin',
  component: AdminPage,
  meta: {
    requiresAuth: true,
    permissions: ['admin:view']
  }
}
```

注意：

- 前端权限只能提升用户体验，不能作为真正安全边界。
- 后端必须做接口权限校验。
- 动态路由要处理刷新、退出登录、权限变化等情况。

## 21. 国际化

Vue 常用 `vue-i18n` 实现国际化。

核心能力：

- 多语言文本。
- 日期格式。
- 数字格式。
- 货币格式。
- 语言切换。
- 懒加载语言包。

示例：

```ts
import { createI18n } from 'vue-i18n'

const i18n = createI18n({
  locale: 'zh-CN',
  messages: {
    'zh-CN': {
      hello: '你好'
    },
    en: {
      hello: 'Hello'
    }
  }
})
```

模板中使用：

```vue
<p>{{ $t('hello') }}</p>
```

国际化设计建议：

- 不要把文案硬编码在组件中。
- 文案 key 要有命名规范。
- 大型项目按模块拆分语言包。
- 注意日期、货币、复数、方向性布局等问题。

## 22. Vue3 性能优化

### 22.1 编译优化

Vue3 编译器会对模板进行优化，例如：

- 静态节点提升。
- Patch Flag。
- 缓存事件处理函数。
- Block Tree。

这些优化让 Vue 能更精确地更新动态部分，而不是每次都全量比较。

### 22.2 组件层面优化

常见手段：

- 合理拆分组件。
- 避免组件过大。
- 使用 `v-memo` 跳过部分更新。
- 使用 `v-once` 渲染一次静态内容。
- 使用 KeepAlive 缓存页面状态。
- 大列表使用虚拟滚动。
- 异步组件和路由懒加载。

`v-once` 示例：

```vue
<h1 v-once>{{ title }}</h1>
```

`v-memo` 示例：

```vue
<div v-memo="[item.id, item.selected]">
  {{ item.name }}
</div>
```

### 22.3 响应式优化

建议：

- 不要把巨大对象全部做深层响应式。
- 大型不可变数据可使用 `shallowRef`。
- 不需要响应式的数据不要放进 `ref` 或 `reactive`。
- 避免不必要的深度 watch。
- 合理拆分状态，减少无关更新。

`shallowRef` 示例：

```ts
const chartData = shallowRef<LargeDataSet | null>(null)
```

### 22.4 加载性能优化

常见手段：

- 路由懒加载。
- 组件懒加载。
- 图片懒加载。
- CDN。
- Gzip/Brotli。
- Tree Shaking。
- 分包。
- 预加载关键资源。
- 使用现代图片格式。

### 22.5 性能分析工具

常用工具：

- Vue Devtools。
- Chrome Performance。
- Lighthouse。
- Web Vitals。
- Bundle Analyzer。

性能优化流程：

1. 测量指标。
2. 找到瓶颈。
3. 针对性优化。
4. 验证结果。
5. 沉淀规范。

## 23. 测试

### 23.1 测试类型

Vue 项目常见测试：

- 单元测试。
- 组件测试。
- Composable 测试。
- 集成测试。
- 端到端测试。

常见工具：

- Vitest。
- Vue Test Utils。
- Testing Library Vue。
- Cypress。
- Playwright。
- MSW。

### 23.2 组件测试

示例：

```ts
import { mount } from '@vue/test-utils'
import Counter from './Counter.vue'

test('click should increase count', async () => {
  const wrapper = mount(Counter)

  await wrapper.get('button').trigger('click')

  expect(wrapper.text()).toContain('1')
})
```

测试建议：

- 优先测试用户行为。
- 少测试内部实现细节。
- 对接口请求使用 Mock。
- 关键业务逻辑要有单元测试。
- 复杂表单和权限流程适合 E2E 测试。

## 24. Vue3 工程化

### 24.1 Vite

Vue3 新项目通常推荐 Vite。

基础配置：

```ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

Vite 优势：

- 启动快。
- HMR 快。
- 配置简洁。
- 原生支持 Vue3。
- 生产构建基于 Rollup。

### 24.2 代码规范

常用工具：

- ESLint。
- Prettier。
- Stylelint。
- eslint-plugin-vue。
- Husky。
- lint-staged。
- Commitlint。

建议：

- 统一格式化规则。
- 提交前自动检查。
- CI 中执行完整检查。
- 组件命名、目录结构、接口封装建立团队规范。

### 24.3 自动导入

常用插件：

- `unplugin-auto-import`
- `unplugin-vue-components`

用途：

- 自动导入 Vue API。
- 自动导入组件。
- 自动生成类型声明。
- 减少重复 import。

注意：自动导入虽然方便，但团队需要统一规范，避免来源不清。

### 24.4 环境变量

Vite 中客户端环境变量以 `VITE_` 开头。

```env
VITE_API_BASE_URL=https://api.example.com
```

使用：

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL
```

注意：前端环境变量会暴露到浏览器，不要存放真正密钥。

### 24.5 构建和部署

构建：

```bash
npm run build
```

预览：

```bash
npm run preview
```

部署注意事项：

- 配置正确的 `base`。
- History 路由需要服务端 fallback。
- 静态资源配置缓存。
- HTML 不宜强缓存。
- JS/CSS 使用 Hash 文件名长期缓存。
- 接口地址通过环境变量区分。

Nginx fallback 示例：

```nginx
location / {
  try_files $uri $uri/ /index.html;
}
```

## 25. SSR 与 Nuxt

### 25.1 CSR

CSR 是客户端渲染。浏览器先加载 HTML 和 JavaScript，再由 JS 渲染页面。

优点：

- 交互体验好。
- 部署简单。
- 前后端分离清晰。

缺点：

- 首屏可能较慢。
- SEO 需要额外处理。

### 25.2 SSR

SSR 是服务端渲染。服务端先生成 HTML，浏览器更快看到内容。

优点：

- 首屏体验更好。
- SEO 更友好。

缺点：

- 服务端复杂度更高。
- 部署和缓存策略更复杂。
- 需要处理同构代码问题。

### 25.3 SSG

SSG 是静态站点生成，在构建阶段生成 HTML。

适合：

- 官网。
- 文档站。
- 博客。
- 营销页。
- 内容较稳定的页面。

### 25.4 Nuxt

Nuxt 是 Vue 生态中的全栈框架，支持 SSR、SSG、文件路由、服务端 API、自动导入等能力。

Nuxt 适合：

- 需要 SEO 的 Vue 应用。
- 内容站。
- 官网。
- 中大型全栈应用。
- 需要服务端渲染和更完整约定的项目。

## 26. 常见最佳实践

### 26.1 组件设计

建议：

- 组件职责单一。
- 通用组件不耦合业务。
- 业务组件组合通用组件。
- Props 和 Emits 命名清晰。
- 插槽用于提升组件扩展性。
- 弹窗、表单、表格等复杂组件要设计清楚状态边界。

### 26.2 状态设计

建议：

- 局部状态放组件内。
- 可复用逻辑放 Composable。
- 跨页面状态放 Pinia。
- 服务端数据不要全部塞进全局 Store。
- 避免重复状态和派生状态。
- 复杂状态变化要集中管理。

### 26.3 请求设计

建议：

- Axios 实例统一封装。
- 接口按业务模块组织。
- 请求和响应类型明确。
- 统一处理错误码。
- 统一处理登录失效。
- 组件不要直接散落 URL 字符串。

### 26.4 路由设计

建议：

- 页面组件和路由结构保持一致。
- 使用 meta 描述权限、标题、缓存策略。
- 大页面使用懒加载。
- 动态路由要考虑刷新恢复。
- 404、403、登录页等基础页面要完整。

### 26.5 可维护性

建议：

- 使用 TypeScript。
- 保持目录结构清晰。
- 建立命名规范。
- 关键业务写测试。
- 使用 ESLint 和 Prettier。
- 定期清理无用依赖。
- 避免组件过大。
- 对复杂业务写设计文档。

## 27. 学习路线建议

推荐学习顺序：

1. 掌握 HTML、CSS、JavaScript、TypeScript 基础。
2. 学习 Vue3 模板语法和单文件组件。
3. 熟练使用 `ref`、`reactive`、`computed`、`watch`。
4. 学习 Composition API 和 Composable。
5. 学习组件 Props、Emits、Slots、v-model。
6. 学习 Vue Router。
7. 学习 Pinia。
8. 学习 Axios 请求封装和接口分层。
9. 学习表单、权限、国际化、主题切换。
10. 学习 Vite、ESLint、Prettier、环境变量和构建部署。
11. 学习 Vue3 响应式原理和渲染优化。
12. 学习组件测试和 E2E 测试。
13. 学习 Nuxt、SSR、SSG。
14. 阅读成熟 Vue3 项目和组件库源码。

## 28. 能力自检清单

你应该能够回答：

- Vue3 为什么使用 Proxy 实现响应式？
- `ref` 和 `reactive` 有什么区别？
- `computed` 和 `watch` 应该如何选择？
- `watch` 和 `watchEffect` 有什么区别？
- `<script setup>` 的优势是什么？
- Props 为什么不能直接修改？
- 插槽和作用域插槽适合解决什么问题？
- Vue Router 导航守卫如何做权限校验？
- Pinia 如何拆分 Store？
- 如何设计后台管理系统的动态路由？
- 如何封装 Axios 请求层？
- 如何实现按钮级权限？
- 如何优化 Vue3 大列表性能？
- History 路由部署为什么需要 fallback？
- Vue3 项目如何做工程化规范？

## 29. 总结

Vue3 的核心能力可以概括为：响应式系统、组件化、模板编译、Composition API 和生态工程化。

学习 Vue3 不应只停留在会写页面，而要深入理解数据如何驱动视图、组件如何设计边界、状态如何管理、路由和权限如何组织、请求层如何封装、构建和部署如何落地。真正掌握 Vue3 后，可以独立构建从简单 H5 到大型后台管理系统、组件库、SSR 应用等多种前端项目。
