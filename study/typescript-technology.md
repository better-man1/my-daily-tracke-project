# TypeScript 技术知识点总结

## 1. TypeScript 是什么

TypeScript 是 JavaScript 的超集，由 Microsoft 开源维护。它在 JavaScript 的基础上增加了静态类型系统，使开发者可以在编译阶段发现类型错误，并获得更好的编辑器提示、重构能力和代码可维护性。

TypeScript 的核心价值：

- 在开发阶段发现错误。
- 提升大型项目可维护性。
- 提供更好的 IDE 智能提示。
- 让接口、组件、函数和数据模型更清晰。
- 支持现代 JavaScript 语法。
- 编译后仍然是普通 JavaScript。

TypeScript 不会改变 JavaScript 的运行时行为。它主要在编译阶段工作，最终会被转换为 JavaScript 后运行在浏览器、Node.js 或其他 JavaScript 运行环境中。

适合使用 TypeScript 的场景：

- 中大型前端项目。
- Node.js 后端服务。
- 组件库。
- 工具库。
- CLI 工具。
- 多人协作项目。
- 长期维护项目。

## 2. TypeScript 与 JavaScript 的关系

TypeScript 是 JavaScript 的超集，这意味着合法的 JavaScript 代码通常也是合法的 TypeScript 代码。

例如：

```ts
const message = 'hello'
console.log(message)
```

TypeScript 在此基础上增加类型标注：

```ts
const message: string = 'hello'
```

TypeScript 的工作流程：

1. 编写 `.ts` 或 `.tsx` 源码。
2. TypeScript 编译器进行类型检查。
3. 编译器输出 JavaScript。
4. JavaScript 在运行时环境中执行。

需要理解：

- TypeScript 类型只存在于编译阶段。
- TypeScript 类型不会直接出现在最终 JavaScript 产物中。
- TypeScript 不能阻止所有运行时错误。
- TypeScript 的目标是提升开发体验和代码可靠性，而不是替代测试。

## 3. 基础类型

### 3.1 boolean

布尔类型表示 true 或 false。

```ts
const isLogin: boolean = true
```

常用于：

- 开关状态。
- 表单勾选。
- 权限判断。
- 条件渲染。

### 3.2 number

数字类型表示整数和浮点数。

```ts
const age: number = 18
const price: number = 99.9
```

JavaScript 中没有单独的整数类型，TypeScript 的 `number` 与 JavaScript 的 Number 一致。

### 3.3 string

字符串类型。

```ts
const username: string = 'Alice'
const greeting: string = `Hello ${username}`
```

常用于：

- 文本。
- ID。
- URL。
- 枚举值。
- 表单输入。

如果字符串只能是固定几个值，更推荐使用字面量联合类型。

```ts
type Theme = 'light' | 'dark'
```

### 3.4 null 和 undefined

`null` 表示主动为空，`undefined` 表示未定义。

```ts
let user: User | null = null
let value: string | undefined = undefined
```

建议开启 `strictNullChecks`，让 TypeScript 严格检查空值。

```ts
function getName(user: User | null) {
  return user?.name ?? 'anonymous'
}
```

### 3.5 symbol

`symbol` 表示唯一值。

```ts
const key: symbol = Symbol('key')
```

常用于：

- 对象唯一属性键。
- 框架内部标识。
- 避免属性名冲突。

### 3.6 bigint

`bigint` 用于表示超过 `number` 安全范围的大整数。

```ts
const id: bigint = 9007199254740993n
```

适合：

- 大整数计算。
- 特殊 ID。
- 金融或区块链相关场景。

注意浏览器和编译目标兼容性。

## 4. 数组、元组和对象

### 4.1 数组

数组类型有两种常见写法。

```ts
const numbers: number[] = [1, 2, 3]
const names: Array<string> = ['Alice', 'Bob']
```

对象数组：

```ts
type User = {
  id: number
  name: string
}

const users: User[] = [
  { id: 1, name: 'Alice' }
]
```

建议：

- 简单数组常用 `T[]`。
- 泛型场景可以使用 `Array<T>`。
- 不确定元素类型时，优先建模清楚，而不是使用 `any[]`。

### 4.2 只读数组

只读数组不能被修改。

```ts
const list: readonly number[] = [1, 2, 3]
```

或：

```ts
const list: ReadonlyArray<number> = [1, 2, 3]
```

适合：

- 函数入参保护。
- 配置项。
- 不希望被修改的数据。

### 4.3 元组

元组用于表示固定长度、固定位置类型的数组。

```ts
const point: [number, number] = [10, 20]
```

命名元组：

```ts
type Point = [x: number, y: number]
```

常见场景：

- 坐标。
- React Hook 返回值。
- 固定结构的数据。

示例：

```ts
function useCounter(): [number, () => void] {
  let count = 0

  return [
    count,
    () => {
      count += 1
    }
  ]
}
```

### 4.4 对象类型

对象类型描述对象的属性结构。

```ts
const user: {
  id: number
  name: string
} = {
  id: 1,
  name: 'Alice'
}
```

实际项目中更推荐使用 `type` 或 `interface` 抽离对象类型。

```ts
type User = {
  id: number
  name: string
  email?: string
}
```

可选属性：

```ts
email?: string
```

只读属性：

```ts
readonly id: number
```

## 5. any、unknown、never、void

### 5.1 any

`any` 表示任意类型。使用 `any` 后，TypeScript 基本放弃类型检查。

```ts
let value: any = 1
value = 'hello'
value.foo.bar()
```

`any` 的问题：

- 失去类型安全。
- 失去编辑器提示。
- 错误延迟到运行时。
- 会污染后续类型推导。

建议：

- 尽量少用 `any`。
- 临时迁移老项目时可以使用。
- 不确定类型时优先使用 `unknown`。

### 5.2 unknown

`unknown` 表示未知类型，比 `any` 更安全。

```ts
let value: unknown = 'hello'
```

使用前必须先缩小类型。

```ts
if (typeof value === 'string') {
  console.log(value.toUpperCase())
}
```

适合：

- 外部输入。
- JSON 解析结果。
- 第三方接口返回。
- catch 错误对象。

### 5.3 void

`void` 通常表示函数没有返回值。

```ts
function log(message: string): void {
  console.log(message)
}
```

常用于：

- 事件处理函数。
- 日志函数。
- 只执行副作用的函数。

### 5.4 never

`never` 表示永远不会出现的值。

常见场景：

- 函数永远抛出错误。
- 函数永远不会正常结束。
- 联合类型穷尽检查。

```ts
function fail(message: string): never {
  throw new Error(message)
}
```

穷尽检查：

```ts
type Status = 'success' | 'error'

function handleStatus(status: Status) {
  switch (status) {
    case 'success':
      return '成功'
    case 'error':
      return '失败'
    default: {
      const exhaustiveCheck: never = status
      return exhaustiveCheck
    }
  }
}
```

如果未来新增状态但没有处理，TypeScript 会提示错误。

## 6. 类型推断

TypeScript 会根据代码自动推断类型。

```ts
const name = 'Alice'
const age = 18
```

推断结果：

- `name` 是 string。
- `age` 是 number。

函数返回值也可以推断：

```ts
function add(a: number, b: number) {
  return a + b
}
```

建议：

- 能清晰推断时，不必重复标注类型。
- 函数参数通常需要标注。
- 复杂函数返回值可以显式标注。
- 公共 API、导出函数、库代码建议显式声明类型。

示例：

```ts
export function getUserName(user: User): string {
  return user.name
}
```

## 7. 联合类型和交叉类型

### 7.1 联合类型

联合类型表示一个值可以是多种类型之一。

```ts
let id: string | number

id = 1
id = 'u_001'
```

常见场景：

- ID 可能是字符串或数字。
- 状态只能是几个固定值。
- 接口返回可能成功或失败。

字面量联合类型：

```ts
type ButtonType = 'primary' | 'default' | 'danger'
```

这比普通字符串更安全。

```ts
function createButton(type: ButtonType) {}

createButton('primary')
```

### 7.2 类型缩小

使用联合类型时，需要通过判断缩小类型。

```ts
function printId(id: string | number) {
  if (typeof id === 'string') {
    console.log(id.toUpperCase())
  } else {
    console.log(id.toFixed(0))
  }
}
```

常见缩小方式：

- `typeof`
- `instanceof`
- `in`
- 字面量判断。
- 自定义类型守卫。

### 7.3 交叉类型

交叉类型表示多个类型合并。

```ts
type User = {
  id: number
  name: string
}

type WithTime = {
  createdAt: string
  updatedAt: string
}

type UserRecord = User & WithTime
```

`UserRecord` 同时拥有 `User` 和 `WithTime` 的属性。

适合：

- 组合多个对象能力。
- 给实体增加公共字段。
- 混入类型。

## 8. type 与 interface

### 8.1 type

`type` 用于定义类型别名。

```ts
type User = {
  id: number
  name: string
}
```

`type` 可以定义：

- 对象类型。
- 联合类型。
- 交叉类型。
- 元组。
- 函数类型。
- 字面量类型。
- 条件类型。
- 映射类型。

```ts
type ID = string | number
type Point = [number, number]
type Handler = (value: string) => void
```

### 8.2 interface

`interface` 主要用于定义对象结构。

```ts
interface User {
  id: number
  name: string
}
```

接口可以继承：

```ts
interface Admin extends User {
  permissions: string[]
}
```

接口可以声明合并：

```ts
interface Window {
  appVersion: string
}
```

### 8.3 如何选择

一般建议：

- 定义对象模型时，`type` 和 `interface` 都可以。
- 需要声明合并时用 `interface`。
- 需要联合类型、元组、条件类型、工具类型组合时用 `type`。
- 团队内统一风格更重要。

常见实践：

- 业务对象模型：`type` 或 `interface` 均可。
- 组件 Props：React 中常用 `type`，Vue 中常用泛型对象。
- 第三方库扩展声明：常用 `interface`。
- 复杂类型运算：使用 `type`。

## 9. 函数类型

### 9.1 函数参数和返回值

```ts
function add(a: number, b: number): number {
  return a + b
}
```

箭头函数：

```ts
const add = (a: number, b: number): number => {
  return a + b
}
```

函数类型别名：

```ts
type Add = (a: number, b: number) => number

const add: Add = (a, b) => a + b
```

### 9.2 可选参数

```ts
function greet(name?: string) {
  return `Hello ${name ?? 'Guest'}`
}
```

可选参数必须放在必选参数之后。

### 9.3 默认参数

```ts
function createUser(name: string, role = 'user') {
  return {
    name,
    role
  }
}
```

TypeScript 会根据默认值推断 `role` 为 string。

### 9.4 剩余参数

```ts
function sum(...numbers: number[]) {
  return numbers.reduce((total, item) => total + item, 0)
}
```

### 9.5 函数重载

函数重载用于一个函数根据不同参数返回不同类型。

```ts
function format(value: string): string
function format(value: number): string
function format(value: string | number): string {
  return String(value)
}
```

更典型示例：

```ts
function getValue(type: 'string'): string
function getValue(type: 'number'): number
function getValue(type: 'string' | 'number') {
  return type === 'string' ? 'hello' : 1
}
```

注意：

- 重载签名写在上方。
- 实现签名写在最后。
- 外部只能看到重载签名。
- 能用泛型或联合类型清晰表达时，不一定需要重载。

## 10. 类

### 10.1 基础类

```ts
class User {
  name: string

  constructor(name: string) {
    this.name = name
  }

  sayHello() {
    return `Hello ${this.name}`
  }
}
```

### 10.2 访问修饰符

TypeScript 支持：

- `public`：公开，默认值。
- `private`：只能在当前类内部访问。
- `protected`：当前类和子类可访问。
- `readonly`：只读。

```ts
class User {
  public name: string
  private password: string
  protected role: string
  readonly id: number

  constructor(id: number, name: string, password: string) {
    this.id = id
    this.name = name
    this.password = password
    this.role = 'user'
  }
}
```

### 10.3 参数属性

构造函数中可以直接声明属性。

```ts
class User {
  constructor(
    public readonly id: number,
    public name: string
  ) {}
}
```

### 10.4 抽象类

抽象类不能直接实例化，通常用于定义公共能力和约束子类实现。

```ts
abstract class Animal {
  abstract speak(): string

  move() {
    return 'moving'
  }
}

class Dog extends Animal {
  speak() {
    return 'wang'
  }
}
```

适合：

- 面向对象建模。
- 框架基类。
- 插件系统。
- 领域模型。

### 10.5 implements

类可以实现接口。

```ts
interface Repository<T> {
  findById(id: number): Promise<T | null>
}

class UserRepository implements Repository<User> {
  async findById(id: number): Promise<User | null> {
    return null
  }
}
```

## 11. 枚举 enum

### 11.1 数字枚举

```ts
enum Direction {
  Up,
  Down,
  Left,
  Right
}
```

默认从 0 开始。

### 11.2 字符串枚举

```ts
enum UserRole {
  Admin = 'admin',
  User = 'user'
}
```

字符串枚举更直观，适合接口字段、权限角色、状态值。

### 11.3 enum 的注意点

`enum` 会生成运行时代码。对于只需要类型约束的场景，可以使用字面量联合类型替代。

```ts
type UserRole = 'admin' | 'user'
```

常见建议：

- 业务状态值推荐使用字面量联合类型。
- 需要运行时对象和值映射时可以使用对象常量。

```ts
const UserRole = {
  Admin: 'admin',
  User: 'user'
} as const

type UserRole = typeof UserRole[keyof typeof UserRole]
```

这种方式兼顾运行时值和类型推导。

## 12. 泛型

### 12.1 泛型是什么

泛型用于在定义函数、类型、接口、类时保留类型参数，让代码既可复用又保持类型安全。

示例：

```ts
function identity<T>(value: T): T {
  return value
}

const a = identity<string>('hello')
const b = identity(123)
```

`T` 是类型参数，调用时可以显式传入，也可以由 TypeScript 自动推断。

### 12.2 泛型函数

```ts
function first<T>(list: T[]): T | undefined {
  return list[0]
}

const value = first([1, 2, 3])
```

泛型的价值是保留输入和输出之间的类型关系。

不好的写法：

```ts
function first(list: any[]): any {
  return list[0]
}
```

这样会丢失类型信息。

### 12.3 泛型接口

```ts
interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

type UserResponse = ApiResponse<User>
```

非常适合封装接口响应结构。

### 12.4 泛型类型别名

```ts
type PageResult<T> = {
  list: T[]
  total: number
  page: number
  pageSize: number
}
```

使用：

```ts
type UserPage = PageResult<User>
```

### 12.5 泛型约束

使用 `extends` 约束泛型范围。

```ts
function getLength<T extends { length: number }>(value: T): number {
  return value.length
}
```

这样 `value` 必须拥有 `length` 属性。

### 12.6 keyof 泛型约束

```ts
function getProperty<T, K extends keyof T>(obj: T, key: K): T[K] {
  return obj[key]
}

const user = {
  id: 1,
  name: 'Alice'
}

const name = getProperty(user, 'name')
```

这里：

- `T` 表示对象类型。
- `K extends keyof T` 表示 key 必须是对象的键。
- `T[K]` 表示对应键的值类型。

这是 TypeScript 高级类型设计中非常常用的模式。

### 12.7 泛型默认值

```ts
type ApiResponse<T = unknown> = {
  code: number
  data: T
}
```

当使用者不传泛型时，默认使用 `unknown`。

## 13. 类型守卫

类型守卫用于在运行时判断类型，并让 TypeScript 在代码分支中缩小类型。

### 13.1 typeof

```ts
function format(value: string | number) {
  if (typeof value === 'string') {
    return value.toUpperCase()
  }

  return value.toFixed(2)
}
```

### 13.2 instanceof

```ts
function handleDate(value: Date | string) {
  if (value instanceof Date) {
    return value.getTime()
  }

  return Date.parse(value)
}
```

### 13.3 in

```ts
type Cat = {
  meow: () => void
}

type Dog = {
  bark: () => void
}

function speak(animal: Cat | Dog) {
  if ('meow' in animal) {
    animal.meow()
  } else {
    animal.bark()
  }
}
```

### 13.4 自定义类型守卫

```ts
function isUser(value: unknown): value is User {
  return (
    typeof value === 'object' &&
    value !== null &&
    'id' in value &&
    'name' in value
  )
}
```

使用：

```ts
if (isUser(data)) {
  console.log(data.name)
}
```

适合校验外部数据，但复杂场景建议使用 Zod、Valibot、Yup 等运行时校验库。

## 14. 字面量类型与 as const

### 14.1 字面量类型

字面量类型表示具体的值。

```ts
type Direction = 'up' | 'down' | 'left' | 'right'
```

常用于：

- 状态。
- 类型标识。
- 组件属性。
- API 参数。
- 权限值。

### 14.2 as const

`as const` 可以把对象或数组转换为只读字面量类型。

```ts
const routes = ['home', 'user', 'settings'] as const

type RouteName = typeof routes[number]
```

对象示例：

```ts
const Status = {
  Success: 'success',
  Error: 'error'
} as const

type Status = typeof Status[keyof typeof Status]
```

这种模式常用于替代 enum。

### 14.3 satisfies

`satisfies` 用于检查一个值是否满足某个类型，同时保留更精确的推断结果。

```ts
type RouteConfig = Record<string, {
  path: string
  title: string
}>

const routes = {
  home: {
    path: '/',
    title: '首页'
  }
} satisfies RouteConfig
```

它与类型断言不同：

- `as` 是告诉编译器“相信我”。
- `satisfies` 是让编译器检查“是否满足”。

推荐在配置对象中使用 `satisfies`。

## 15. 类型操作符

### 15.1 keyof

`keyof` 获取对象类型的键。

```ts
type User = {
  id: number
  name: string
}

type UserKey = keyof User
```

结果：

```ts
type UserKey = 'id' | 'name'
```

### 15.2 typeof

TypeScript 中的 `typeof` 可以从变量获取类型。

```ts
const user = {
  id: 1,
  name: 'Alice'
}

type User = typeof user
```

### 15.3 indexed access type

索引访问类型用于获取对象属性类型。

```ts
type User = {
  id: number
  name: string
}

type UserName = User['name']
```

结果是 `string`。

获取数组元素类型：

```ts
type UserList = User[]
type UserItem = UserList[number]
```

### 15.4 in

`in` 常用于映射类型。

```ts
type Flags<T> = {
  [K in keyof T]: boolean
}
```

### 15.5 infer

`infer` 用于在条件类型中推断类型。

```ts
type ReturnTypeOf<T> = T extends (...args: any[]) => infer R ? R : never
```

如果 `T` 是函数类型，则推断返回值类型为 `R`。

## 16. 映射类型

映射类型可以基于已有类型生成新类型。

### 16.1 基础映射

```ts
type Optional<T> = {
  [K in keyof T]?: T[K]
}
```

这类似内置工具类型 `Partial<T>`。

### 16.2 只读映射

```ts
type ReadonlyType<T> = {
  readonly [K in keyof T]: T[K]
}
```

### 16.3 去除修饰符

去除可选：

```ts
type RequiredType<T> = {
  [K in keyof T]-?: T[K]
}
```

去除只读：

```ts
type Mutable<T> = {
  -readonly [K in keyof T]: T[K]
}
```

### 16.4 key remapping

可以通过 `as` 重映射键名。

```ts
type Getter<T> = {
  [K in keyof T as `get${Capitalize<string & K>}`]: () => T[K]
}
```

示例：

```ts
type User = {
  name: string
  age: number
}

type UserGetter = Getter<User>
```

结果包含：

- `getName`
- `getAge`

## 17. 条件类型

条件类型根据类型关系选择不同结果。

```ts
type IsString<T> = T extends string ? true : false
```

示例：

```ts
type A = IsString<string>
type B = IsString<number>
```

结果：

- `A` 是 true。
- `B` 是 false。

### 17.1 分布式条件类型

当条件类型作用于联合类型时，会对每个成员分别计算。

```ts
type ToArray<T> = T extends any ? T[] : never

type Result = ToArray<string | number>
```

结果：

```ts
type Result = string[] | number[]
```

如果不想分布，可以包一层元组。

```ts
type ToArrayNonDist<T> = [T] extends [any] ? T[] : never
```

### 17.2 常见条件类型应用

提取 Promise 值：

```ts
type AwaitedValue<T> = T extends Promise<infer R> ? R : T
```

提取数组元素：

```ts
type ElementType<T> = T extends Array<infer U> ? U : never
```

提取函数参数：

```ts
type Params<T> = T extends (...args: infer P) => any ? P : never
```

条件类型是 TypeScript 高级类型编程的核心。

## 18. 模板字面量类型

模板字面量类型可以像字符串模板一样组合类型。

```ts
type Size = 'small' | 'large'
type Color = 'red' | 'blue'

type ClassName = `${Size}-${Color}`
```

结果：

```ts
type ClassName =
  | 'small-red'
  | 'small-blue'
  | 'large-red'
  | 'large-blue'
```

常见用途：

- 事件名。
- CSS class。
- API 路径。
- 国际化 key。
- 权限 key。

示例：

```ts
type EventName<T extends string> = `on${Capitalize<T>}`

type ClickEvent = EventName<'click'>
```

结果是 `onClick`。

## 19. 内置工具类型

TypeScript 提供了很多实用工具类型。

### 19.1 Partial

把所有属性变成可选。

```ts
type PartialUser = Partial<User>
```

常用于更新参数：

```ts
function updateUser(id: number, patch: Partial<User>) {}
```

### 19.2 Required

把所有属性变成必选。

```ts
type RequiredUser = Required<User>
```

### 19.3 Readonly

把所有属性变成只读。

```ts
type ReadonlyUser = Readonly<User>
```

### 19.4 Pick

从类型中选择部分属性。

```ts
type UserBase = Pick<User, 'id' | 'name'>
```

### 19.5 Omit

从类型中排除部分属性。

```ts
type CreateUserInput = Omit<User, 'id' | 'createdAt'>
```

### 19.6 Record

创建键值映射类型。

```ts
type RoleMap = Record<string, string[]>
```

固定键：

```ts
type PermissionMap = Record<'admin' | 'user', string[]>
```

### 19.7 Exclude

从联合类型中排除成员。

```ts
type Status = 'success' | 'error' | 'loading'
type FinalStatus = Exclude<Status, 'loading'>
```

### 19.8 Extract

从联合类型中提取成员。

```ts
type ErrorStatus = Extract<Status, 'error'>
```

### 19.9 NonNullable

去除 `null` 和 `undefined`。

```ts
type Value = string | null | undefined
type SafeValue = NonNullable<Value>
```

### 19.10 ReturnType

获取函数返回值类型。

```ts
function getUser() {
  return {
    id: 1,
    name: 'Alice'
  }
}

type User = ReturnType<typeof getUser>
```

### 19.11 Parameters

获取函数参数元组。

```ts
type Params = Parameters<typeof fetch>
```

### 19.12 Awaited

获取 Promise 解析后的类型。

```ts
type Data = Awaited<Promise<string>>
```

结果是 `string`。

## 20. 模块系统

### 20.1 export 和 import

导出：

```ts
export type User = {
  id: number
  name: string
}

export function getUser(id: number) {}
```

导入：

```ts
import { getUser } from './user'
import type { User } from './user'
```

建议使用 `import type` 导入纯类型，帮助编译器和构建工具区分类型和运行时代码。

### 20.2 默认导出

```ts
export default function createApp() {}
```

导入：

```ts
import createApp from './createApp'
```

团队中应统一默认导出和命名导出的使用规范。

### 20.3 类型声明文件

`.d.ts` 文件用于声明类型。

常见用途：

- 为没有类型的 JS 库补充类型。
- 声明全局变量。
- 扩展第三方库类型。
- 声明静态资源模块。

示例：

```ts
declare module '*.vue' {
  import type { DefineComponent } from 'vue'

  const component: DefineComponent<{}, {}, any>
  export default component
}
```

声明静态资源：

```ts
declare module '*.png' {
  const src: string
  export default src
}
```

## 21. tsconfig.json

`tsconfig.json` 是 TypeScript 项目的配置文件。

基础示例：

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "module": "ESNext",
    "moduleResolution": "Bundler",
    "strict": true,
    "jsx": "react-jsx",
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    },
    "noEmit": true
  },
  "include": ["src"]
}
```

### 21.1 target

`target` 指定输出 JavaScript 的语法版本。

```json
{
  "target": "ES2022"
}
```

较新的 target 可以减少编译转换，产物更接近现代 JavaScript，但要考虑运行环境兼容性。

### 21.2 module

`module` 指定模块输出格式。

常见值：

- `ESNext`
- `CommonJS`
- `NodeNext`

前端项目通常使用 `ESNext`，Node.js ESM 项目常用 `NodeNext`。

### 21.3 moduleResolution

模块解析策略。

常见值：

- `Node`
- `NodeNext`
- `Bundler`

Vite 等现代前端项目常用 `Bundler`。

### 21.4 strict

`strict` 开启严格类型检查，是高级 TypeScript 项目非常重要的配置。

它包含多项严格规则，例如：

- `noImplicitAny`
- `strictNullChecks`
- `strictFunctionTypes`
- `strictBindCallApply`
- `strictPropertyInitialization`

建议新项目默认开启。

### 21.5 noEmit

`noEmit` 表示只做类型检查，不输出文件。

```json
{
  "noEmit": true
}
```

在 Vite、Webpack、esbuild、SWC 负责构建时，TypeScript 常只负责类型检查。

### 21.6 paths

配置路径别名。

```json
{
  "baseUrl": ".",
  "paths": {
    "@/*": ["src/*"]
  }
}
```

注意：`tsconfig` 的 paths 只影响 TypeScript 类型解析，构建工具也需要配置对应别名。

## 22. TypeScript 与前端框架

### 22.1 TypeScript 与 React

React 中 TypeScript 常用于：

- Props 类型。
- State 类型。
- 事件类型。
- ref 类型。
- 组件泛型。
- 自定义 Hook。
- API 数据类型。

Props 示例：

```tsx
type ButtonProps = {
  type?: 'primary' | 'default'
  disabled?: boolean
  onClick?: () => void
  children: React.ReactNode
}

function Button(props: ButtonProps) {
  return <button onClick={props.onClick}>{props.children}</button>
}
```

事件类型：

```tsx
function SearchInput() {
  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    console.log(event.target.value)
  }

  return <input onChange={handleChange} />
}
```

ref 类型：

```tsx
const inputRef = useRef<HTMLInputElement>(null)
```

建议：

- 组件 Props 明确建模。
- children 使用 `React.ReactNode`。
- 事件类型尽量具体。
- 自定义 Hook 返回值保持类型清晰。
- 避免使用 `React.FC` 作为唯一固定习惯，按团队规范选择。

### 22.2 TypeScript 与 Vue3

Vue3 对 TypeScript 支持很好。

Props：

```ts
defineProps<{
  id: number
  title: string
}>()
```

Emits：

```ts
const emit = defineEmits<{
  submit: [id: number]
  cancel: []
}>()
```

Ref：

```ts
const count = ref<number>(0)
const inputRef = ref<HTMLInputElement | null>(null)
```

建议：

- 使用 `<script setup lang="ts">`。
- 接口数据定义类型。
- Pinia Store 保持类型推导。
- 复杂组件 Props 使用字面量联合类型约束。

## 23. TypeScript 与 Node.js

Node.js 项目使用 TypeScript 可以提升服务端代码质量。

常见工具：

- `typescript`
- `tsx`
- `ts-node`
- `tsup`
- `esbuild`
- `@types/node`

常见配置：

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "module": "NodeNext",
    "moduleResolution": "NodeNext",
    "strict": true,
    "outDir": "dist"
  }
}
```

Node.js 中 TypeScript 常用于：

- API 请求参数。
- 数据库模型。
- Service 入参和返回值。
- 配置对象。
- 中间件上下文。
- 错误类型。
- 领域模型。

示例：

```ts
type CreateUserInput = {
  username: string
  email: string
  password: string
}

async function createUser(input: CreateUserInput): Promise<User> {
  return userRepository.create(input)
}
```

建议：

- 公共服务函数显式声明返回值。
- 外部输入使用运行时校验。
- 不要把 TypeScript 类型当成接口安全校验。
- 生产构建输出 JavaScript 后运行。

## 24. 类型设计实践

### 24.1 接口响应类型

通用响应：

```ts
type ApiResponse<T> = {
  code: number
  message: string
  data: T
}
```

分页响应：

```ts
type PageResult<T> = {
  list: T[]
  total: number
  page: number
  pageSize: number
}
```

使用：

```ts
type UserPageResponse = ApiResponse<PageResult<User>>
```

### 24.2 表单类型

创建表单和实体类型通常不完全一致。

```ts
type User = {
  id: number
  username: string
  email: string
  createdAt: string
}

type CreateUserForm = Pick<User, 'username' | 'email'> & {
  password: string
}
```

建议：

- 不要直接复用数据库实体作为前端表单类型。
- 根据场景设计输入类型、输出类型、展示类型。

### 24.3 状态类型

```ts
type RequestState<T> =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'success'; data: T }
  | { status: 'error'; error: Error }
```

使用：

```ts
function renderUser(state: RequestState<User>) {
  switch (state.status) {
    case 'idle':
      return '未开始'
    case 'loading':
      return '加载中'
    case 'success':
      return state.data.name
    case 'error':
      return state.error.message
  }
}
```

这种可辨识联合类型非常适合描述状态机。

### 24.4 权限类型

```ts
type Resource = 'user' | 'order' | 'product'
type Action = 'create' | 'read' | 'update' | 'delete'

type Permission = `${Resource}:${Action}`
```

结果类似：

- `user:create`
- `order:read`
- `product:update`

这种类型能减少权限字符串写错。

## 25. 运行时校验

TypeScript 只在编译阶段做类型检查，不能保证运行时外部数据一定符合类型。

例如：

```ts
const user = await response.json() as User
```

这里的 `as User` 只是类型断言，不会真正校验数据。

推荐使用运行时校验库：

- Zod。
- Valibot。
- Yup。
- io-ts。

Zod 示例：

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

- 接口响应校验。
- 表单校验。
- 环境变量校验。
- 配置文件校验。
- 第三方 Webhook 校验。

高级实践是让运行时 Schema 和 TypeScript 类型互相推导，避免维护两套规则。

## 26. 类型断言与非空断言

### 26.1 类型断言

类型断言告诉 TypeScript 把某个值当作指定类型。

```ts
const input = document.querySelector('input') as HTMLInputElement
```

注意：类型断言不会做运行时检查。

不建议滥用：

```ts
const user = data as User
```

如果 data 来自外部，应先校验。

### 26.2 非空断言

`!` 表示告诉 TypeScript 某个值一定不是 null 或 undefined。

```ts
const root = document.getElementById('root')!
```

风险：

- 如果判断错误，运行时仍会报错。
- 过度使用会掩盖空值问题。

建议：

- 能用显式判断就用判断。
- 框架入口、确定存在的 DOM 可以少量使用。

### 26.3 const assertion

`as const` 是一种特殊断言，可生成更窄的字面量类型和只读属性。

```ts
const config = {
  mode: 'dark'
} as const
```

此时 `mode` 类型是 `'dark'`，不是 string。

## 27. 装饰器

装饰器用于给类、方法、属性、参数增加元信息或额外行为。

常见于：

- NestJS。
- Angular。
- TypeORM。
- class-validator。

示例：

```ts
class UserController {
  @Get('/users')
  findAll() {}
}
```

装饰器适合框架和元编程场景，但普通业务代码中不应滥用。

需要注意：

- TypeScript 装饰器经历过不同阶段的语法变化。
- 项目配置和框架版本会影响写法。
- 使用装饰器会增加一定隐式行为，团队需要统一规范。

## 28. 命名空间与全局声明

现代 TypeScript 项目主要使用 ES Module，不推荐大量使用 namespace。

### 28.1 namespace

```ts
namespace Utils {
  export function formatDate() {}
}
```

现在更推荐：

```ts
export function formatDate() {}
```

### 28.2 全局声明

扩展全局类型：

```ts
declare global {
  interface Window {
    appConfig: {
      version: string
    }
  }
}

export {}
```

注意：全局声明应谨慎，避免污染全局命名空间。

## 29. 工程化实践

### 29.1 类型检查脚本

在 `package.json` 中配置：

```json
{
  "scripts": {
    "typecheck": "tsc --noEmit"
  }
}
```

CI 中应执行类型检查，避免类型错误进入主分支。

### 29.2 ESLint

TypeScript 项目建议配合 ESLint。

常用包：

- `typescript-eslint`
- `eslint`
- `prettier`

ESLint 负责代码质量和部分类型相关规则，TypeScript 编译器负责类型检查。

### 29.3 路径别名

TypeScript 配置：

```json
{
  "compilerOptions": {
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    }
  }
}
```

Vite 配置：

```ts
import path from 'node:path'

export default defineConfig({
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  }
})
```

### 29.4 Monorepo 类型共享

Monorepo 中常见类型共享方式：

- 独立 `types` 包。
- 前后端共享 DTO。
- 使用 OpenAPI 生成类型。
- 使用 tRPC 共享端到端类型。
- 使用 GraphQL Code Generator。

注意：

- 类型共享不能替代运行时校验。
- 前后端共享类型要控制边界，避免业务耦合过深。
- 生成类型应进入自动化流程。

## 30. 常见错误与反模式

### 30.1 any 泛滥

问题：

- 类型系统失效。
- 后续维护困难。
- 错误无法提前发现。

改进：

- 使用 unknown。
- 定义明确类型。
- 用泛型表达关系。
- 使用运行时校验。

### 30.2 过度类型体操

问题：

- 类型难以阅读。
- 编译变慢。
- 团队难以维护。

改进：

- 类型设计服务业务。
- 复杂类型加注释。
- 能简单表达就简单表达。
- 不为了炫技写复杂类型。

### 30.3 滥用类型断言

问题：

- 绕过类型检查。
- 掩盖真实错误。

改进：

- 使用类型守卫。
- 使用运行时校验。
- 减少 `as unknown as T`。

### 30.4 类型和业务模型脱节

问题：

- 类型只是表面标注。
- 无法表达业务约束。
- 维护时容易误用。

改进：

- 按业务场景设计类型。
- 区分 Entity、DTO、Form、ViewModel。
- 用字面量联合类型表达状态。
- 用可辨识联合类型表达流程。

## 31. 最佳实践

建议：

- 新项目开启 `strict`。
- 外部数据使用 `unknown` 接收。
- 公共函数声明参数和返回值。
- 不滥用 `any` 和类型断言。
- 用字面量联合类型替代随意字符串。
- 用泛型表达输入输出关系。
- 用工具类型减少重复。
- 用 `satisfies` 检查配置对象。
- 用运行时校验保护外部输入。
- 类型设计贴近业务模型。
- 类型复杂度要服务可维护性。

项目分层类型建议：

- `types`：公共类型。
- `models`：领域模型。
- `services`：接口请求和响应类型。
- `components`：组件 Props 类型。
- `stores`：状态管理类型。
- `schemas`：运行时校验 Schema。

## 32. 学习路线建议

推荐学习顺序：

1. 掌握 JavaScript 基础。
2. 学习 TypeScript 基础类型。
3. 学习数组、对象、函数和类类型。
4. 学习联合类型、交叉类型、类型缩小。
5. 学习 type、interface、泛型。
6. 学习 keyof、typeof、索引访问类型。
7. 学习内置工具类型。
8. 学习条件类型、映射类型、模板字面量类型。
9. 学习 tsconfig 配置。
10. 在 React、Vue3、Node.js 项目中实践。
11. 学习运行时校验和类型生成。
12. 学习大型项目类型设计。
13. 阅读优秀开源库的类型声明。

## 33. 能力自检清单

你应该能够回答：

- TypeScript 和 JavaScript 的关系是什么？
- `any` 和 `unknown` 有什么区别？
- `type` 和 `interface` 如何选择？
- 联合类型如何做类型缩小？
- `never` 常用于哪些场景？
- 泛型解决了什么问题？
- `keyof T` 和 `T[K]` 分别表示什么？
- `Partial`、`Pick`、`Omit`、`Record` 如何使用？
- 条件类型中的 `infer` 有什么作用？
- `as const` 和 `satisfies` 有什么区别？
- TypeScript 类型为什么不能替代运行时校验？
- `tsconfig.json` 中 `strict` 为什么重要？
- React/Vue3 中如何设计组件 Props 类型？
- Node.js 中如何设计接口入参和返回值类型？
- 如何避免大型项目中类型越来越混乱？

## 34. 总结

TypeScript 的核心不是“给变量加类型”，而是用类型系统表达程序结构、业务约束和模块边界。它能让代码在开发阶段暴露更多问题，让团队协作更稳定，让重构更安全。

学习 TypeScript 应从基础类型开始，逐步理解联合类型、泛型、类型缩小、工具类型、条件类型和映射类型。真正掌握 TypeScript 后，你应该能够在 React、Vue3、Node.js、组件库、工具库和大型工程中设计清晰、可靠、可维护的类型体系。
