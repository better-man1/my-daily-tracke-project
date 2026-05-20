# Node.js 技术知识点总结

## 1. Node.js 是什么

Node.js 是一个基于 Chrome V8 JavaScript 引擎的 JavaScript 运行时环境。它让 JavaScript 不再只能运行在浏览器中，也可以运行在服务器、命令行工具、构建工具、桌面应用和自动化脚本等场景中。

Node.js 的核心特点：

- 使用 JavaScript 作为开发语言。
- 基于 V8 引擎执行 JavaScript。
- 采用事件驱动和非阻塞 I/O 模型。
- 适合处理高并发 I/O 密集型任务。
- 拥有庞大的 npm 生态。

Node.js 常见应用场景：

- Web 服务端开发。
- RESTful API 和 GraphQL API。
- 中间层 BFF 服务。
- 实时通信服务，如聊天室、协同编辑、消息推送。
- 前端工程化工具，如 Vite、Webpack、ESLint、Babel。
- 命令行工具 CLI。
- 爬虫、脚本和自动化任务。
- 桌面应用，如 Electron。

## 2. Node.js 运行机制

### 2.1 V8 引擎

V8 是 Google 开发的高性能 JavaScript 引擎，负责解析、编译和执行 JavaScript 代码。

在 Node.js 中，V8 主要负责：

- 编译 JavaScript 源码。
- 执行 JavaScript 代码。
- 管理内存。
- 执行垃圾回收。

需要注意的是，V8 只负责 JavaScript 语言本身的执行，文件系统、网络、定时器等能力并不是 V8 提供的，而是 Node.js 运行时通过底层库提供的。

### 2.2 libuv

libuv 是 Node.js 底层非常重要的跨平台异步 I/O 库。

它主要负责：

- 事件循环。
- 异步文件 I/O。
- TCP、UDP 网络通信。
- 定时器。
- 子进程。
- 线程池。

Node.js 能够在单个主线程中处理大量并发连接，关键就在于 JavaScript 主线程不直接阻塞等待 I/O 完成，而是把耗时任务交给底层系统或线程池处理，完成后再通过事件循环通知 JavaScript 回调执行。

### 2.3 单线程与非阻塞 I/O

Node.js 常被说成是单线程的，这里的单线程主要指 JavaScript 执行线程是单线程的。

但 Node.js 整体并不是只有一个线程。它内部还包括：

- libuv 线程池。
- 操作系统内核 I/O 能力。
- Worker Threads。
- 子进程。

Node.js 的优势不是用一个线程完成所有事情，而是用单线程执行 JavaScript 逻辑，同时通过事件循环和非阻塞 I/O 避免线程被长时间阻塞。

适合 Node.js 的任务：

- 数据库查询。
- 文件读写。
- 网络请求。
- API 网关。
- 实时消息服务。

不太适合直接放在主线程中的任务：

- 大量 CPU 密集计算。
- 大图片处理。
- 大规模加密解密。
- 长时间同步循环。

如果必须处理 CPU 密集型任务，可以使用 Worker Threads、子进程、任务队列或独立计算服务。

## 3. 事件循环 Event Loop

### 3.1 事件循环的作用

事件循环是 Node.js 异步机制的核心。它负责调度异步任务的回调，使 Node.js 能够在主线程不阻塞的情况下处理多个任务。

示例：

```js
const fs = require('node:fs')

fs.readFile('./data.txt', 'utf8', (err, data) => {
  if (err) throw err
  console.log(data)
})

console.log('start')
```

执行结果通常是：

```text
start
文件内容
```

原因是 `fs.readFile` 是异步 I/O，读取文件的任务交给底层处理，JavaScript 主线程继续执行后面的代码。文件读取完成后，回调再进入事件循环等待执行。

### 3.2 事件循环阶段

Node.js 事件循环大致包含以下阶段：

- timers：执行 `setTimeout` 和 `setInterval` 到期回调。
- pending callbacks：执行某些系统操作的延迟回调。
- idle、prepare：Node.js 内部使用。
- poll：处理 I/O 回调，是事件循环中非常重要的阶段。
- check：执行 `setImmediate` 回调。
- close callbacks：执行关闭事件回调，如 socket 的 `close`。

每个阶段都有自己的回调队列，事件循环会按顺序进入不同阶段并执行对应回调。

### 3.3 宏任务与微任务

Node.js 中常见异步任务可以粗略分为宏任务和微任务。

常见宏任务：

- `setTimeout`
- `setInterval`
- `setImmediate`
- I/O 回调

常见微任务：

- `Promise.then`
- `Promise.catch`
- `Promise.finally`
- `queueMicrotask`
- `process.nextTick`

需要特别注意：`process.nextTick` 在 Node.js 中优先级很高，通常会比 Promise 微任务更早执行。过度使用 `process.nextTick` 可能导致事件循环无法进入后续阶段，造成 I/O 饥饿。

示例：

```js
setTimeout(() => console.log('timeout'), 0)

Promise.resolve().then(() => console.log('promise'))

process.nextTick(() => console.log('nextTick'))

console.log('sync')
```

通常输出：

```text
sync
nextTick
promise
timeout
```

## 4. 模块系统

### 4.1 CommonJS

CommonJS 是 Node.js 早期和长期使用的模块规范。

导出：

```js
function add(a, b) {
  return a + b
}

module.exports = {
  add
}
```

导入：

```js
const { add } = require('./math')

console.log(add(1, 2))
```

CommonJS 特点：

- 使用 `require` 导入。
- 使用 `module.exports` 或 `exports` 导出。
- 模块在运行时加载。
- 导入的是导出值的拷贝或引用表现，具体取决于导出内容类型。
- 适合 Node.js 传统生态。

### 4.2 ES Module

ES Module 是 JavaScript 官方模块规范，现代 Node.js 已经支持 ESM。

导出：

```js
export function add(a, b) {
  return a + b
}
```

导入：

```js
import { add } from './math.js'

console.log(add(1, 2))
```

启用 ESM 的常见方式：

- 文件扩展名使用 `.mjs`。
- 在 `package.json` 中配置 `"type": "module"`。

```json
{
  "type": "module"
}
```

ES Module 特点：

- 使用 `import` 和 `export`。
- 静态结构更明确，利于 Tree Shaking。
- 支持顶层 `await`。
- 与浏览器模块规范统一。

### 4.3 CommonJS 与 ES Module 区别

| 对比项 | CommonJS | ES Module |
| --- | --- | --- |
| 导入语法 | `require` | `import` |
| 导出语法 | `module.exports` | `export` |
| 加载时机 | 运行时加载 | 编译期静态分析为主 |
| 顶层 await | 不支持 | 支持 |
| Tree Shaking | 不友好 | 更友好 |
| 常见文件类型 | `.cjs`、`.js` | `.mjs`、`.js` |

实际项目中需要注意模块格式混用问题。例如 ESM 中没有 CommonJS 的 `__dirname` 和 `__filename`，需要通过 `import.meta.url` 转换获得。

```js
import { fileURLToPath } from 'node:url'
import path from 'node:path'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
```

## 5. npm 与包管理

### 5.1 npm 是什么

npm 是 Node.js 默认包管理工具，也是 JavaScript 生态最重要的基础设施之一。

npm 的作用：

- 安装第三方依赖。
- 发布 npm 包。
- 管理项目脚本。
- 管理依赖版本。
- 维护锁文件，保证安装一致性。

### 5.2 package.json

`package.json` 是 Node.js 项目的核心描述文件。

常见字段：

```json
{
  "name": "node-demo",
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "node src/index.js",
    "test": "vitest",
    "start": "node dist/index.js"
  },
  "dependencies": {
    "express": "^4.18.0"
  },
  "devDependencies": {
    "eslint": "^9.0.0"
  }
}
```

重要字段说明：

- `name`：包名。
- `version`：版本号。
- `type`：指定 `.js` 文件按 CommonJS 还是 ESM 解析。
- `main`：CommonJS 入口。
- `module`：ESM 入口，常用于库。
- `exports`：现代包导出入口配置。
- `scripts`：项目命令脚本。
- `dependencies`：生产依赖。
- `devDependencies`：开发依赖。
- `peerDependencies`：同伴依赖，常用于插件和组件库。

### 5.3 语义化版本

npm 依赖通常遵循语义化版本 SemVer：

```text
主版本号.次版本号.修订号
major.minor.patch
```

例如 `2.5.1`：

- `2`：主版本，通常表示不兼容变更。
- `5`：次版本，通常表示向下兼容的新功能。
- `1`：修订版本，通常表示 bug 修复。

常见版本范围：

- `1.2.3`：固定版本。
- `^1.2.3`：允许升级次版本和修订版本，不升级主版本。
- `~1.2.3`：允许升级修订版本，通常不升级次版本。
- `latest`：安装最新版本，不建议在生产项目中随意使用。

### 5.4 npm、yarn、pnpm

常见包管理工具：

- npm：Node.js 默认工具，生态兼容性好。
- Yarn：安装体验稳定，早期比 npm 更快。
- pnpm：通过内容寻址和硬链接节省磁盘空间，安装速度快，依赖结构更严格。

锁文件：

- npm 使用 `package-lock.json`。
- Yarn 使用 `yarn.lock`。
- pnpm 使用 `pnpm-lock.yaml`。

团队项目中应该统一包管理器，不建议多人混用不同锁文件。

## 6. Node.js 核心模块

Node.js 内置了大量核心模块，不需要额外安装即可使用。

### 6.1 fs 文件系统模块

`fs` 用于文件和目录操作。

常见能力：

- 读取文件。
- 写入文件。
- 删除文件。
- 创建目录。
- 遍历目录。
- 获取文件状态。

推荐使用 Promise API：

```js
import { readFile, writeFile } from 'node:fs/promises'

const content = await readFile('./input.txt', 'utf8')
await writeFile('./output.txt', content.toUpperCase())
```

同步 API 也可以使用，但在服务端请求处理中应谨慎使用，因为同步文件操作会阻塞主线程。

### 6.2 path 路径模块

`path` 用于处理文件路径。

```js
import path from 'node:path'

const filePath = path.join(process.cwd(), 'src', 'index.js')
const ext = path.extname(filePath)
```

常用方法：

- `path.join`：拼接路径。
- `path.resolve`：生成绝对路径。
- `path.dirname`：获取目录名。
- `path.basename`：获取文件名。
- `path.extname`：获取扩展名。

使用 `path` 的原因是不同操作系统路径分隔符不同。Windows 使用 `\`，Linux/macOS 使用 `/`，直接拼字符串容易出错。

### 6.3 http 模块

`http` 模块可以创建 HTTP 服务。

```js
import http from 'node:http'

const server = http.createServer((req, res) => {
  res.setHeader('Content-Type', 'application/json')
  res.end(JSON.stringify({ message: 'hello node' }))
})

server.listen(3000, () => {
  console.log('server running at http://localhost:3000')
})
```

实际项目中通常会使用 Express、Koa、Fastify、NestJS 等框架来提高开发效率。

### 6.4 url 和 querystring

`url` 用于解析和处理 URL。

```js
const url = new URL('https://example.com/users?page=1&pageSize=10')

console.log(url.pathname)
console.log(url.searchParams.get('page'))
```

现代 Node.js 推荐优先使用 WHATWG 标准的 `URL` 和 `URLSearchParams`，而不是旧的 `querystring` 模块。

### 6.5 stream 流

Stream 是 Node.js 非常重要的抽象，用于处理大文件、网络数据、压缩数据等连续数据。

流的优势：

- 不需要一次性把全部数据读入内存。
- 适合处理大文件。
- 可以边读边写，提高效率。
- 可以通过管道组合多个处理步骤。

常见流类型：

- Readable：可读流。
- Writable：可写流。
- Duplex：双工流，可读可写。
- Transform：转换流，输入经过处理后输出。

示例：

```js
import { createReadStream, createWriteStream } from 'node:fs'

createReadStream('./large.log')
  .pipe(createWriteStream('./large-copy.log'))
```

### 6.6 buffer 缓冲区

JavaScript 字符串适合处理文本，但网络传输、文件读写、图片处理等场景经常需要处理二进制数据。Node.js 使用 `Buffer` 表示二进制数据。

```js
const buf = Buffer.from('hello')

console.log(buf)
console.log(buf.toString('utf8'))
```

Buffer 常见应用：

- 文件读写。
- 网络协议。
- 图片和音视频处理。
- 加密摘要。
- Base64 编码和解码。

### 6.7 events 事件模块

`events` 模块提供事件发布订阅能力。

```js
import { EventEmitter } from 'node:events'

const emitter = new EventEmitter()

emitter.on('login', user => {
  console.log(`${user.name} logged in`)
})

emitter.emit('login', { name: 'Alice' })
```

Node.js 中很多对象都基于事件机制，例如 HTTP Server、Stream、Socket 等。

### 6.8 child_process 子进程

`child_process` 用于创建子进程执行系统命令或独立脚本。

常见 API：

- `exec`：执行命令，适合输出较小的命令。
- `spawn`：启动子进程，适合长时间运行或大量输出。
- `fork`：创建新的 Node.js 子进程，适合进程间通信。

示例：

```js
import { spawn } from 'node:child_process'

const child = spawn('node', ['script.js'])

child.stdout.on('data', data => {
  console.log(data.toString())
})
```

### 6.9 crypto 加密模块

`crypto` 提供哈希、加密、签名等能力。

哈希示例：

```js
import crypto from 'node:crypto'

const hash = crypto.createHash('sha256')
  .update('hello')
  .digest('hex')

console.log(hash)
```

常见用途：

- 密码哈希。
- 文件完整性校验。
- Token 签名。
- 数据加密。

注意：真实密码存储不要直接使用普通 SHA 系列哈希，应使用 bcrypt、argon2、scrypt 等带盐且抗暴力破解的方案。

## 7. 异步编程

### 7.1 回调函数

Node.js 早期大量 API 使用错误优先回调风格。

```js
fs.readFile('./data.txt', 'utf8', (err, data) => {
  if (err) {
    console.error(err)
    return
  }

  console.log(data)
})
```

特点：

- 第一个参数通常是错误对象。
- 后续参数是成功结果。
- 多层嵌套时容易形成回调地狱。

### 7.2 Promise

Promise 让异步代码更容易组合和链式处理。

```js
import { readFile } from 'node:fs/promises'

readFile('./data.txt', 'utf8')
  .then(data => {
    console.log(data)
  })
  .catch(err => {
    console.error(err)
  })
```

### 7.3 async/await

`async/await` 是当前最常用的异步编程方式，可以让异步代码写起来接近同步流程。

```js
import { readFile } from 'node:fs/promises'

async function main() {
  try {
    const data = await readFile('./data.txt', 'utf8')
    console.log(data)
  } catch (err) {
    console.error(err)
  }
}

main()
```

使用建议：

- 对相互依赖的异步任务使用连续 `await`。
- 对互不依赖的异步任务使用 `Promise.all` 并发执行。

```js
const [user, orders] = await Promise.all([
  getUser(userId),
  getOrders(userId)
])
```

## 8. Web 服务开发

### 8.1 原生 HTTP 服务

Node.js 原生 `http` 模块可以创建服务，但直接处理路由、参数、错误和中间件会比较繁琐。

```js
import http from 'node:http'

const server = http.createServer((req, res) => {
  if (req.url === '/health') {
    res.end('ok')
    return
  }

  res.statusCode = 404
  res.end('not found')
})

server.listen(3000)
```

### 8.2 Express

Express 是 Node.js 生态中最经典的 Web 框架之一。

```js
import express from 'express'

const app = express()

app.use(express.json())

app.get('/health', (req, res) => {
  res.json({ status: 'ok' })
})

app.post('/users', (req, res) => {
  res.json({ user: req.body })
})

app.listen(3000)
```

Express 的核心概念：

- 路由。
- 中间件。
- 请求对象 `req`。
- 响应对象 `res`。
- 错误处理中间件。

### 8.3 Koa

Koa 是 Express 原班团队设计的新一代框架，核心更轻量，基于 async/await 和洋葱模型中间件。

```js
import Koa from 'koa'

const app = new Koa()

app.use(async (ctx, next) => {
  console.log('before')
  await next()
  console.log('after')
})

app.use(ctx => {
  ctx.body = { message: 'hello koa' }
})

app.listen(3000)
```

Koa 的中间件执行模型像洋葱：

1. 请求进入时按顺序执行 `await next()` 之前的逻辑。
2. 到达最内层中间件。
3. 响应返回时倒序执行 `await next()` 之后的逻辑。

### 8.4 Fastify

Fastify 是一个高性能 Node.js Web 框架，关注速度、低开销和 Schema 驱动。

```js
import Fastify from 'fastify'

const fastify = Fastify()

fastify.get('/health', async () => {
  return { status: 'ok' }
})

await fastify.listen({ port: 3000 })
```

Fastify 适合对性能要求较高、接口结构清晰的服务。

### 8.5 NestJS

NestJS 是一个企业级 Node.js 框架，默认使用 TypeScript，架构风格接近 Angular。

核心概念：

- Module。
- Controller。
- Service。
- Provider。
- Dependency Injection。
- Guard。
- Pipe。
- Interceptor。
- Exception Filter。

NestJS 适合中大型后端项目，特别是需要清晰分层、依赖注入、模块边界和团队规范的业务系统。

## 9. 中间件机制

中间件是 Web 框架中非常重要的扩展机制。它可以在请求进入业务逻辑前后执行通用逻辑。

常见中间件职责：

- 日志记录。
- 请求体解析。
- Cookie 解析。
- CORS 处理。
- 鉴权认证。
- 参数校验。
- 错误处理。
- 响应压缩。
- 静态资源服务。

Express 中间件示例：

```js
app.use((req, res, next) => {
  console.log(req.method, req.url)
  next()
})
```

错误处理中间件：

```js
app.use((err, req, res, next) => {
  console.error(err)
  res.status(500).json({ message: 'internal server error' })
})
```

良好的中间件设计可以让业务代码保持简洁，让通用能力集中维护。

## 10. 数据库访问

Node.js 常用于连接各种数据库。

### 10.1 关系型数据库

常见数据库：

- MySQL。
- PostgreSQL。
- SQLite。
- SQL Server。

常见工具：

- mysql2。
- pg。
- Prisma。
- Sequelize。
- TypeORM。
- Knex。

Prisma 示例：

```ts
const user = await prisma.user.findUnique({
  where: {
    id: 1
  }
})
```

关系型数据库适合结构化数据、事务要求高、复杂查询多的场景。

### 10.2 NoSQL 数据库

常见数据库：

- MongoDB。
- Redis。
- Elasticsearch。

MongoDB 适合文档型数据，Redis 常用于缓存、分布式锁、排行榜、会话存储、消息队列等场景。

### 10.3 数据访问层设计

实际项目中建议把数据库操作封装在独立层中。

常见分层：

- Controller：处理 HTTP 请求和响应。
- Service：处理业务逻辑。
- Repository/DAO：处理数据访问。
- Model/Entity：描述数据结构。

这样可以避免 SQL 或 ORM 逻辑散落在 Controller 中，提高可维护性和测试性。

## 11. 鉴权与安全

### 11.1 常见鉴权方式

常见方式：

- Session + Cookie。
- JWT。
- OAuth 2.0。
- API Key。
- 单点登录 SSO。

Session + Cookie 适合传统 Web 应用。JWT 适合前后端分离、移动端和跨服务认证场景，但需要注意 Token 失效、刷新和泄露风险。

### 11.2 常见安全风险

Node.js Web 服务常见安全问题：

- XSS：跨站脚本攻击。
- CSRF：跨站请求伪造。
- SQL 注入。
- 命令注入。
- 路径遍历。
- 敏感信息泄露。
- 不安全的依赖包。
- 过宽的 CORS 配置。

基本防护建议：

- 永远不要直接拼接 SQL，使用参数化查询或 ORM。
- 不要信任客户端输入，对参数进行校验。
- 不要把密钥提交到代码仓库。
- 对密码使用安全哈希算法。
- 合理设置 Cookie 的 `HttpOnly`、`Secure`、`SameSite`。
- 限制 CORS 来源。
- 定期扫描依赖漏洞。
- 避免把错误堆栈直接返回给客户端。

### 11.3 环境变量

敏感配置应通过环境变量管理。

常见变量：

- 数据库连接地址。
- JWT 密钥。
- 第三方 API Key。
- 服务端口。
- 日志级别。

示例：

```env
PORT=3000
DATABASE_URL=mysql://user:password@localhost:3306/app
JWT_SECRET=replace_with_real_secret
```

代码读取：

```js
const port = process.env.PORT || 3000
```

生产环境应由部署平台、容器编排系统或密钥管理服务注入环境变量。

## 12. 错误处理与日志

### 12.1 错误分类

常见错误类型：

- 语法错误。
- 运行时错误。
- 参数校验错误。
- 数据库错误。
- 网络错误。
- 第三方服务错误。
- 业务错误。

良好的错误处理应该区分：

- 给用户看的错误信息。
- 给开发者排查问题的内部错误信息。
- 需要告警的严重错误。

### 12.2 统一错误处理

Web 服务中建议使用统一错误处理中间件。

```js
app.use((err, req, res, next) => {
  const status = err.status || 500

  res.status(status).json({
    message: status === 500 ? 'internal server error' : err.message
  })
})
```

这样可以避免每个接口重复写错误响应逻辑。

### 12.3 日志系统

日志是线上排查问题的重要依据。

常见日志内容：

- 请求日志。
- 错误日志。
- 慢查询日志。
- 业务关键事件。
- 外部服务调用结果。

常见日志库：

- pino。
- winston。
- morgan。

日志建议：

- 使用结构化 JSON 日志。
- 记录 requestId，方便链路追踪。
- 不记录密码、Token 等敏感信息。
- 区分 debug、info、warn、error 等级别。
- 生产环境日志应接入集中化平台。

## 13. 性能优化

### 13.1 避免阻塞主线程

Node.js 主线程负责执行 JavaScript。如果主线程被 CPU 密集型任务阻塞，所有请求都会受到影响。

应避免：

- 大量同步文件操作。
- 长时间循环计算。
- 同步压缩大文件。
- 同步加密大量数据。
- 在请求处理中执行复杂 CPU 任务。

优化方式：

- 使用异步 API。
- 使用 Worker Threads。
- 使用子进程。
- 使用任务队列。
- 将 CPU 密集任务拆到独立服务。

### 13.2 缓存

缓存可以显著减少数据库和外部服务压力。

常见缓存层：

- 内存缓存。
- Redis 缓存。
- HTTP 缓存。
- CDN 缓存。

常见缓存策略：

- Cache Aside：先读缓存，未命中再读数据库并写缓存。
- Write Through：写数据库时同步写缓存。
- TTL：设置过期时间。
- 主动失效：数据变更时删除或更新缓存。

缓存需要考虑：

- 缓存穿透。
- 缓存击穿。
- 缓存雪崩。
- 数据一致性。

### 13.3 连接池

访问数据库、Redis 或其他服务时，频繁创建连接成本很高，应使用连接池复用连接。

连接池配置需要关注：

- 最大连接数。
- 最小连接数。
- 空闲连接回收。
- 请求等待超时。
- 数据库最大连接限制。

连接池过小会导致请求排队，过大可能压垮数据库。

### 13.4 压缩与限流

常见服务端性能保护手段：

- gzip 或 brotli 响应压缩。
- 请求限流。
- 请求体大小限制。
- 静态资源缓存。
- 超时控制。
- 熔断和降级。

限流常见算法：

- 固定窗口。
- 滑动窗口。
- 令牌桶。
- 漏桶。

## 14. 进程管理与部署

### 14.1 开发与生产启动

开发环境常用：

```bash
npm run dev
```

生产环境常用：

```bash
npm run build
npm run start
```

生产环境不要依赖开发服务器，也不要使用会自动重启但缺乏生产管理能力的开发工具作为主要进程管理方式。

### 14.2 PM2

PM2 是常见 Node.js 进程管理工具。

常见能力：

- 后台运行服务。
- 进程自动重启。
- 日志管理。
- 集群模式。
- 开机自启动。
- 查看进程状态。

示例：

```bash
pm2 start dist/index.js --name api-server
pm2 logs api-server
pm2 restart api-server
```

### 14.3 Docker 部署

Node.js 服务常使用 Docker 打包和部署。

简单 Dockerfile 示例：

```dockerfile
FROM node:20-alpine

WORKDIR /app

COPY package*.json ./
RUN npm ci --omit=dev

COPY . .

EXPOSE 3000
CMD ["node", "src/index.js"]
```

生产镜像建议：

- 使用较小基础镜像。
- 使用锁文件安装依赖。
- 不把 `.env` 和敏感文件打进镜像。
- 使用多阶段构建。
- 以非 root 用户运行应用。

### 14.4 Nginx 反向代理

生产环境中 Node.js 服务前面通常会有 Nginx 或云负载均衡。

Nginx 常见职责：

- 反向代理。
- HTTPS 终止。
- 静态资源服务。
- 负载均衡。
- gzip 压缩。
- 请求大小限制。
- 访问日志。

Node.js 应用通常专注业务逻辑，TLS、静态资源和负载均衡交给更合适的基础设施处理。

## 15. 测试

Node.js 项目测试非常重要，尤其是服务端逻辑通常直接影响数据和业务流程。

常见测试类型：

- 单元测试：测试函数、类、服务方法。
- 集成测试：测试数据库、缓存、外部服务协作。
- 接口测试：测试 HTTP API 行为。
- 端到端测试：模拟完整业务流程。

常见工具：

- Node.js 内置 test runner。
- Jest。
- Vitest。
- Mocha。
- Supertest。
- Playwright。

接口测试示例：

```js
import request from 'supertest'
import app from '../src/app.js'

test('GET /health should return ok', async () => {
  await request(app)
    .get('/health')
    .expect(200)
    .expect({ status: 'ok' })
})
```

测试实践建议：

- 业务逻辑尽量从 Controller 中拆出来，方便单元测试。
- 测试数据应可重复创建和清理。
- 外部服务使用 Mock 或测试环境。
- CI 中自动执行测试。

## 16. TypeScript 与 Node.js

TypeScript 可以显著提升 Node.js 项目的可维护性，尤其适合中大型服务端项目。

TypeScript 带来的价值：

- 静态类型检查。
- 更好的编辑器提示。
- 更清晰的接口约束。
- 更安全的重构。
- 更适合多人协作。

常见工具：

- `typescript`：TypeScript 编译器。
- `ts-node` / `tsx`：开发阶段直接运行 TypeScript。
- `tsup` / `esbuild`：构建 Node.js 库或服务。
- `@types/node`：Node.js 类型定义。

简单配置：

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

建议开启 `strict` 模式，让类型检查发挥更大价值。

## 17. CLI 工具开发

Node.js 很适合开发命令行工具。

常见场景：

- 项目脚手架。
- 代码生成器。
- 构建工具。
- 发布工具。
- 自动化脚本。

CLI 入口示例：

```js
#!/usr/bin/env node

console.log('hello cli')
```

`package.json` 配置：

```json
{
  "bin": {
    "hello-cli": "./bin/index.js"
  }
}
```

常见 CLI 工具库：

- commander：命令和参数解析。
- cac：轻量命令解析。
- yargs：复杂命令行参数处理。
- prompts：交互式命令行输入。
- chalk：终端颜色。
- ora：加载动画。
- execa：执行子命令。

## 18. 工程化实践

一个成熟 Node.js 项目通常会包含：

- 统一 Node.js 版本。
- 统一包管理器。
- TypeScript。
- ESLint 和 Prettier。
- 环境变量管理。
- 日志系统。
- 统一错误处理。
- 参数校验。
- 测试体系。
- CI/CD。
- Docker 镜像构建。
- 安全扫描。
- 监控告警。

推荐目录结构：

```text
src
├─ config
├─ controllers
├─ middlewares
├─ models
├─ repositories
├─ routes
├─ services
├─ utils
├─ validators
├─ app.ts
└─ server.ts
```

目录说明：

- `config`：配置读取和校验。
- `controllers`：处理 HTTP 请求和响应。
- `middlewares`：中间件。
- `models`：数据模型。
- `repositories`：数据访问。
- `routes`：路由定义。
- `services`：业务逻辑。
- `utils`：通用工具。
- `validators`：参数校验。
- `app.ts`：应用实例。
- `server.ts`：服务启动入口。

## 19. 学习路线建议

推荐学习顺序：

1. 掌握 JavaScript 基础，尤其是作用域、闭包、原型、异步编程。
2. 理解 Node.js 运行时、V8、libuv、事件循环。
3. 学习 CommonJS 和 ES Module。
4. 掌握 npm、package.json、语义化版本和锁文件。
5. 学习 fs、path、http、stream、buffer、events 等核心模块。
6. 熟练使用 Promise、async/await 和错误处理。
7. 学习 Express、Koa、Fastify 或 NestJS。
8. 学习数据库访问、缓存、鉴权、安全。
9. 学习日志、测试、性能优化和部署。
10. 使用 TypeScript 构建更规范的 Node.js 项目。
11. 学习 Docker、Nginx、CI/CD 和监控告警。
12. 尝试开发 CLI、脚手架或前端工程化工具。

## 20. 总结

Node.js 的核心价值在于把 JavaScript 扩展到了服务端和工具链领域。它依靠 V8、libuv、事件循环和非阻塞 I/O，在 I/O 密集型场景中具备很强的并发处理能力。

学习 Node.js 不应只停留在会写接口，而要理解它背后的运行机制、异步模型、模块系统、核心模块、工程化实践和部署方式。真正掌握 Node.js 后，可以同时胜任服务端开发、前端工程化工具开发、自动化脚本、CLI 工具和中间层架构设计等多种工作。
