# 前端浏览器原理、网络协议与 Web 安全知识点总结

## 1. 总览

前端工程不仅是写页面和组件，还需要理解浏览器如何加载、解析、渲染和执行页面，理解网络请求如何从浏览器发出并返回，理解 Web 安全机制如何保护用户和系统。

这部分知识是高级前端工程师必须掌握的基础能力，常用于：

- 分析白屏和首屏慢。
- 排查接口请求失败。
- 解决跨域问题。
- 优化缓存和静态资源加载。
- 分析页面卡顿。
- 处理登录态和 Cookie。
- 防御 XSS、CSRF、点击劫持等安全问题。
- 设计前端监控和性能指标。
- 和后端、网关、运维协作定位线上问题。

整体知识体系可以分为：

- 浏览器架构。
- 页面加载流程。
- 渲染原理。
- JavaScript 运行机制。
- 浏览器存储。
- HTTP 协议。
- HTTPS 和 TLS。
- DNS、TCP、UDP、HTTP/2、HTTP/3。
- 缓存机制。
- 同源策略与 CORS。
- Cookie、Session、Token。
- XSS、CSRF、点击劫持、CSP 等 Web 安全。
- 前端安全工程实践。

## 2. 浏览器整体架构

### 2.1 浏览器是什么

浏览器是运行 Web 应用的平台。它负责把 HTML、CSS、JavaScript、图片、字体、视频等资源转换成用户可以看到并交互的页面。

浏览器的核心职责：

- 发起网络请求。
- 解析 HTML。
- 解析 CSS。
- 执行 JavaScript。
- 构建 DOM。
- 构建 CSSOM。
- 布局和绘制页面。
- 管理事件。
- 管理存储。
- 执行安全策略。
- 提供 Web API。

### 2.2 多进程架构

现代浏览器通常采用多进程架构。

常见进程：

- Browser Process：浏览器主进程。
- Renderer Process：渲染进程。
- GPU Process：图形处理进程。
- Network Service Process：网络进程。
- Extension Process：扩展进程。
- Utility Process：工具进程。

浏览器主进程负责：

- 地址栏。
- 书签。
- 前进后退。
- 标签页管理。
- 进程协调。

渲染进程负责：

- 解析 HTML/CSS。
- 执行 JavaScript。
- 构建 DOM。
- 布局和绘制。
- 处理页面交互。

多进程的价值：

- 提升稳定性，一个页面崩溃不一定影响整个浏览器。
- 提升安全性，不同站点可以隔离。
- 提升并行能力。
- 方便沙箱机制。

### 2.3 渲染进程中的线程

渲染进程内部通常包含多个线程：

- 主线程。
- 合成线程。
- 光栅线程。
- Worker 线程。
- I/O 线程。

主线程负责：

- 执行 JavaScript。
- 解析 HTML。
- 样式计算。
- 布局。
- 部分绘制任务。
- 事件回调。

前端性能优化中常说“不要阻塞主线程”，就是因为主线程一旦被长时间 JavaScript 占用，页面就无法及时响应用户输入，也无法及时更新渲染。

## 3. 从输入 URL 到页面展示

### 3.1 整体流程

用户在地址栏输入 URL 后，浏览器大致会经历：

1. 解析 URL。
2. 检查缓存。
3. DNS 解析。
4. 建立 TCP 连接。
5. TLS 握手。
6. 发送 HTTP 请求。
7. 服务器处理请求。
8. 浏览器接收响应。
9. 解析 HTML。
10. 下载 CSS、JS、图片、字体等子资源。
11. 构建 DOM。
12. 构建 CSSOM。
13. 构建 Render Tree。
14. Layout。
15. Paint。
16. Composite。
17. 页面展示并响应交互。

这个流程是分析页面性能问题的基础。

### 3.2 URL 解析

URL 通常包含：

```text
scheme://host:port/path?query#hash
```

示例：

```text
https://example.com:443/users?page=1#list
```

组成部分：

- `https`：协议。
- `example.com`：主机名。
- `443`：端口。
- `/users`：路径。
- `page=1`：查询参数。
- `#list`：片段标识。

浏览器会根据 URL 判断协议和资源位置。

### 3.3 DNS 解析

DNS 用于把域名解析成 IP 地址。

大致流程：

1. 浏览器缓存。
2. 操作系统缓存。
3. hosts 文件。
4. 本地 DNS 服务器。
5. 根域名服务器。
6. 顶级域名服务器。
7. 权威 DNS 服务器。

优化方式：

- 使用 CDN。
- DNS 预解析。
- 减少不同域名数量。
- 使用稳定 DNS 服务。

DNS 预解析：

```html
<link rel="dns-prefetch" href="//cdn.example.com">
```

### 3.4 建立连接

HTTP/1.1 和 HTTP/2 通常基于 TCP。HTTPS 还需要 TLS 握手。

TCP 三次握手：

1. 客户端发送 SYN。
2. 服务端返回 SYN + ACK。
3. 客户端返回 ACK。

三次握手的目的：

- 确认双方发送和接收能力正常。
- 建立可靠连接。
- 协商初始序列号。

HTTPS 还需要进行 TLS 握手，用于协商加密参数、验证证书、生成会话密钥。

### 3.5 发送请求和接收响应

浏览器建立连接后发送 HTTP 请求。

请求包含：

- 请求行。
- 请求头。
- 请求体。

响应包含：

- 状态行。
- 响应头。
- 响应体。

响应体可能是：

- HTML。
- CSS。
- JavaScript。
- JSON。
- 图片。
- 字体。
- 视频。

## 4. 关键渲染路径

### 4.1 什么是关键渲染路径

关键渲染路径是浏览器把 HTML、CSS 和 JavaScript 转换成屏幕像素所经历的关键步骤。

主要包括：

- DOM。
- CSSOM。
- Render Tree。
- Layout。
- Paint。
- Composite。

理解关键渲染路径有助于优化首屏加载、减少卡顿和避免布局抖动。

### 4.2 DOM 构建

浏览器解析 HTML，生成 DOM 树。

示例 HTML：

```html
<body>
  <h1>Hello</h1>
  <p>World</p>
</body>
```

对应 DOM 树：

```text
document
└─ html
   └─ body
      ├─ h1
      └─ p
```

注意：

- HTML 解析是增量进行的。
- 遇到外部脚本时可能暂停解析。
- JavaScript 可以修改 DOM。

### 4.3 CSSOM 构建

浏览器解析 CSS，生成 CSSOM 树。

CSSOM 表示样式规则和元素之间的关系。

CSS 通常会阻塞渲染，因为浏览器需要知道元素最终样式才能绘制页面。

优化方式：

- 减少首屏 CSS。
- 拆分关键 CSS。
- 压缩 CSS。
- 避免过多无用样式。

### 4.4 Render Tree

Render Tree 由 DOM 和 CSSOM 合成。

它只包含需要渲染的节点。

例如：

- `display: none` 的元素不会进入 Render Tree。
- `visibility: hidden` 的元素仍占位，通常仍会参与布局。

### 4.5 Layout

Layout 也叫 Reflow，布局阶段会计算元素的位置和尺寸。

影响 Layout 的因素：

- 盒模型。
- display。
- position。
- flex/grid。
- 字体。
- 图片尺寸。
- 视口大小。

频繁触发布局会导致性能问题。

### 4.6 Paint

Paint 是绘制阶段，把文字、颜色、边框、阴影、图片等绘制成图层。

会触发 Paint 的变化：

- color。
- background。
- box-shadow。
- border-color。

### 4.7 Composite

Composite 是合成阶段，将多个图层合成为最终画面。

常见能触发合成层的属性：

- transform。
- opacity。
- will-change。
- video。
- canvas。

动画中推荐使用：

- transform。
- opacity。

因为它们通常不需要重新布局，性能更好。

## 5. JavaScript 执行机制

### 5.1 单线程模型

浏览器中的 JavaScript 通常运行在主线程上。

单线程意味着：

- 同一时间只能执行一段 JS。
- 长时间任务会阻塞页面交互。
- JS 执行可能阻塞渲染。

前端需要避免：

- 大量同步计算。
- 巨大 JSON 同步解析。
- 循环中频繁操作 DOM。
- 大量组件同步渲染。

### 5.2 事件循环

事件循环用于协调同步代码、异步任务、用户事件和渲染。

核心概念：

- 调用栈。
- 宏任务。
- 微任务。
- 渲染。

常见宏任务：

- `setTimeout`
- `setInterval`
- DOM 事件。
- 网络回调。

常见微任务：

- `Promise.then`
- `Promise.catch`
- `queueMicrotask`

示例：

```js
console.log('sync')

setTimeout(() => {
  console.log('timeout')
}, 0)

Promise.resolve().then(() => {
  console.log('promise')
})
```

输出顺序：

```text
sync
promise
timeout
```

### 5.3 requestAnimationFrame

`requestAnimationFrame` 会在浏览器下一帧绘制前执行。

适合：

- 动画。
- 视觉更新。
- 滚动同步。

示例：

```js
function animate() {
  updatePosition()
  requestAnimationFrame(animate)
}

requestAnimationFrame(animate)
```

### 5.4 requestIdleCallback

`requestIdleCallback` 会在浏览器空闲时执行低优先级任务。

适合：

- 日志上报。
- 非关键计算。
- 预加载低优先级数据。

注意：

- 不适合关键业务逻辑。
- 兼容性需要评估。

### 5.5 Web Worker

Web Worker 可以让 JavaScript 在独立线程运行。

适合：

- 大数据计算。
- 文件解析。
- 图片处理。
- 加密解密。
- 长时间 CPU 任务。

限制：

- 不能直接访问 DOM。
- 和主线程通过消息通信。
- 传输大对象有序列化成本。

## 6. 浏览器存储机制

### 6.1 Cookie

Cookie 是浏览器和服务器之间传递的小型数据。

常用于：

- 登录态。
- 会话 ID。
- 用户偏好。
- 追踪标识。

重要属性：

- `Expires` / `Max-Age`：过期时间。
- `Domain`：作用域域名。
- `Path`：作用路径。
- `Secure`：仅 HTTPS 发送。
- `HttpOnly`：禁止 JavaScript 读取。
- `SameSite`：控制跨站请求是否携带。

示例：

```http
Set-Cookie: sessionId=abc; HttpOnly; Secure; SameSite=Lax
```

安全建议：

- 登录态 Cookie 设置 `HttpOnly`。
- HTTPS 下设置 `Secure`。
- 根据场景设置 `SameSite`。
- 不在 Cookie 中存储明文敏感信息。

### 6.2 localStorage

localStorage 是持久化本地存储。

特点：

- 容量比 Cookie 大。
- 不会自动随请求发送。
- 同源共享。
- 同步 API。

示例：

```js
localStorage.setItem('theme', 'dark')
const theme = localStorage.getItem('theme')
```

注意：

- 不要存放敏感 Token。
- 同步读写可能阻塞主线程。
- XSS 攻击可以读取 localStorage。

### 6.3 sessionStorage

sessionStorage 生命周期是当前标签页会话。

适合：

- 临时表单状态。
- 当前页面临时数据。
- 不需要跨标签共享的数据。

### 6.4 IndexedDB

IndexedDB 是浏览器本地数据库。

适合：

- 大量结构化数据。
- 离线应用。
- PWA。
- 本地缓存。
- 文件元数据。

特点：

- 异步 API。
- 容量较大。
- 支持索引。
- 支持事务。

### 6.5 Cache Storage

Cache Storage 常和 Service Worker 配合使用。

适合：

- 静态资源缓存。
- 离线访问。
- PWA。

注意：

- 缓存策略需要谨慎。
- 版本更新机制要设计好。
- 错误缓存可能导致用户长期看到旧页面。

## 7. HTTP 协议

### 7.1 HTTP 是什么

HTTP 是 Hypertext Transfer Protocol，超文本传输协议，是 Web 通信的核心应用层协议。

HTTP 特点：

- 请求-响应模型。
- 无状态。
- 可扩展。
- 基于报文。
- 支持缓存。
- 支持代理。
- 支持认证。

无状态意味着每个请求本身不天然记住用户身份，需要 Cookie、Session、Token 等机制维护状态。

### 7.2 HTTP 请求方法

常见方法：

- GET：获取资源。
- POST：创建资源或提交数据。
- PUT：整体更新资源。
- PATCH：部分更新资源。
- DELETE：删除资源。
- OPTIONS：查询服务器支持的方法，CORS 预检常用。
- HEAD：类似 GET，但只返回响应头。

幂等性：

- GET、PUT、DELETE 通常应设计为幂等。
- POST 通常不是幂等。

安全性：

- GET 应只读取数据，不应产生状态变更。
- 状态变更操作不应使用 GET。

### 7.3 HTTP 状态码

状态码分类：

- 1xx：信息响应。
- 2xx：成功。
- 3xx：重定向。
- 4xx：客户端错误。
- 5xx：服务端错误。

常见状态码：

- 200：成功。
- 201：创建成功。
- 204：成功但无响应体。
- 301：永久重定向。
- 302：临时重定向。
- 304：协商缓存命中。
- 400：请求参数错误。
- 401：未认证。
- 403：无权限。
- 404：资源不存在。
- 409：资源冲突。
- 422：语义校验失败。
- 429：请求过多。
- 500：服务器内部错误。
- 502：网关错误。
- 503：服务不可用。
- 504：网关超时。

前端排查时要结合：

- 状态码。
- 响应体。
- 请求头。
- 响应头。
- 网关日志。
- 后端日志。

### 7.4 HTTP Header

常见请求头：

- `Accept`
- `Accept-Encoding`
- `Authorization`
- `Content-Type`
- `Cookie`
- `Origin`
- `Referer`
- `User-Agent`

常见响应头：

- `Content-Type`
- `Cache-Control`
- `ETag`
- `Set-Cookie`
- `Location`
- `Access-Control-Allow-Origin`
- `Content-Security-Policy`
- `Strict-Transport-Security`

Header 是浏览器、服务器、缓存、代理、安全策略协作的重要载体。

### 7.5 Content-Type

常见类型：

- `text/html`
- `text/css`
- `application/javascript`
- `application/json`
- `multipart/form-data`
- `application/x-www-form-urlencoded`

接口请求中常见：

```http
Content-Type: application/json
```

文件上传常见：

```http
Content-Type: multipart/form-data
```

注意：

- 不正确的 Content-Type 可能导致解析失败。
- 某些 Content-Type 会影响 CORS 是否触发预检。

## 8. HTTP 缓存

### 8.1 缓存的价值

HTTP 缓存可以减少网络请求，提升页面加载速度，降低服务器压力。

缓存对象：

- HTML。
- CSS。
- JavaScript。
- 图片。
- 字体。
- API 响应。

### 8.2 强缓存

强缓存命中时，浏览器不向服务器发请求。

常用响应头：

```http
Cache-Control: max-age=31536000
```

常见指令：

- `max-age`：缓存有效秒数。
- `public`：可被共享缓存。
- `private`：只能被浏览器私有缓存。
- `no-store`：不存储缓存。
- `no-cache`：可以存储，但使用前必须向服务器验证。
- `immutable`：资源在有效期内不会变化。

注意：

- `no-cache` 不是“不缓存”，而是“使用前重新验证”。
- 真正不存储缓存应使用 `no-store`。

### 8.3 协商缓存

协商缓存会向服务器验证资源是否变化。

常见机制：

- `ETag` / `If-None-Match`
- `Last-Modified` / `If-Modified-Since`

如果资源未变化，服务器返回：

```http
304 Not Modified
```

浏览器继续使用本地缓存。

### 8.4 前端静态资源缓存策略

推荐：

- HTML：不强缓存或短缓存。
- JS/CSS：文件名带 Hash，长缓存。
- 图片/字体：文件名带 Hash，长缓存。
- API：根据业务实时性设置缓存。

示例：

```text
index.html -> Cache-Control: no-cache
app.8f3a1c.js -> Cache-Control: max-age=31536000, immutable
```

原因：

- HTML 需要及时更新引用资源。
- Hash 静态资源内容变化时文件名变化，可以长期缓存。

## 9. HTTPS 与 TLS

### 9.1 HTTPS 是什么

HTTPS 是在 HTTP 和 TCP 之间加入 TLS 加密层。

它解决：

- 窃听。
- 篡改。
- 冒充。

HTTPS 提供：

- 加密传输。
- 身份认证。
- 完整性保护。

### 9.2 对称加密和非对称加密

对称加密：

- 加密和解密使用同一把密钥。
- 速度快。
- 密钥分发困难。

非对称加密：

- 公钥加密，私钥解密。
- 或私钥签名，公钥验证。
- 速度慢。
- 适合密钥交换和身份验证。

TLS 通常使用非对称加密协商密钥，再使用对称加密传输数据。

### 9.3 证书和 CA

证书用于证明服务器身份。

CA 是证书颁发机构。

浏览器会验证：

- 证书是否可信。
- 域名是否匹配。
- 证书是否过期。
- 证书是否被吊销。

常见 HTTPS 问题：

- 证书过期。
- 证书域名不匹配。
- 混合内容。
- 中间证书缺失。
- 本地环境证书不可信。

### 9.4 HSTS

HSTS 通过响应头告诉浏览器后续只能使用 HTTPS 访问。

```http
Strict-Transport-Security: max-age=31536000; includeSubDomains
```

价值：

- 防止降级攻击。
- 强制 HTTPS。

注意：

- 开启前要确保 HTTPS 配置完整。
- `includeSubDomains` 会影响所有子域名。

## 10. HTTP/1.1、HTTP/2、HTTP/3

### 10.1 HTTP/1.1

特点：

- 文本协议。
- 支持长连接。
- 支持管线化但实践中受限。
- 队头阻塞问题明显。

优化手段：

- 合并资源。
- 域名分片。
- Keep-Alive。
- 缓存。

### 10.2 HTTP/2

HTTP/2 特点：

- 二进制分帧。
- 多路复用。
- Header 压缩。
- 流优先级。
- 单连接并发多个请求。

优势：

- 减少连接数量。
- 改善多个资源并发加载。
- 降低请求头重复传输成本。

注意：

- HTTP/2 不意味着可以无限拆包。
- JS 解析执行成本仍然存在。

### 10.3 HTTP/3

HTTP/3 基于 QUIC，QUIC 基于 UDP。

优势：

- 减少连接建立延迟。
- 改善弱网表现。
- 避免 TCP 层队头阻塞。
- 支持连接迁移。

适合：

- 移动网络。
- 跨区域访问。
- 对延迟敏感的应用。

## 11. 同源策略与跨域

### 11.1 同源策略

同源策略是浏览器重要安全机制。

两个 URL 同源需要：

- 协议相同。
- 域名相同。
- 端口相同。

示例：

```text
https://example.com:443
```

以下不完全同源：

```text
http://example.com
https://example.com
https://api.example.com
https://example.com:8443
```

同源策略限制：

- 跨源读取响应内容。
- 跨源访问 DOM。
- 跨源访问存储。

价值：

- 防止恶意站点读取用户在其他站点的敏感数据。

### 11.2 跨域不是请求发不出去

很多人误解跨域。

实际情况：

- 请求可能已经发送到服务器。
- 服务器也可能已经处理。
- 浏览器阻止 JavaScript 读取响应。

所以：

- CORS 是浏览器安全策略。
- 不是后端没有收到请求。
- 不是前端本地代理的根本安全方案。

### 11.3 CORS

CORS 是 Cross-Origin Resource Sharing，跨源资源共享。

它通过 HTTP Header 告诉浏览器是否允许某个源读取响应。

常见响应头：

```http
Access-Control-Allow-Origin: https://app.example.com
Access-Control-Allow-Methods: GET, POST, OPTIONS
Access-Control-Allow-Headers: Content-Type, Authorization
Access-Control-Allow-Credentials: true
```

注意：

- 携带 Cookie 时不能使用 `Access-Control-Allow-Origin: *`。
- 如果动态返回 Origin，应设置 `Vary: Origin`。
- 预检请求使用 OPTIONS。

### 11.4 简单请求和预检请求

简单请求通常满足：

- 方法是 GET、HEAD、POST。
- Header 限制在安全列表。
- Content-Type 是特定值，如 `application/x-www-form-urlencoded`、`multipart/form-data`、`text/plain`。

非简单请求会触发预检 OPTIONS。

预检用于询问服务器：

- 是否允许该来源。
- 是否允许该方法。
- 是否允许该 Header。

### 11.5 常见 CORS 问题

常见错误：

- 缺少 `Access-Control-Allow-Origin`。
- 携带凭证时使用 `*`。
- 预检请求未正确响应。
- OPTIONS 被网关拦截。
- 自定义 Header 未加入允许列表。
- 重定向导致预检失败。
- 前端设置 `mode: no-cors` 误以为解决跨域。

`no-cors` 通常会得到 opaque response，前端无法读取真正内容，不是解决 API 跨域的方案。

## 12. Cookie、Session 与 Token

### 12.1 Session

Session 通常存储在服务端，浏览器通过 Cookie 保存 session id。

流程：

1. 用户登录。
2. 服务端创建 Session。
3. 服务端通过 Set-Cookie 返回 session id。
4. 浏览器后续请求自动携带 Cookie。
5. 服务端根据 session id 找到用户状态。

优点：

- 服务端可控。
- 容易失效和踢下线。
- 敏感数据不放浏览器。

缺点：

- 服务端需要存储。
- 分布式场景需要共享 Session 或集中存储。

### 12.2 JWT

JWT 是 JSON Web Token。

结构：

```text
header.payload.signature
```

优点：

- 服务端可以无状态验证。
- 适合前后端分离。
- 适合多服务认证。

注意：

- JWT 一旦签发，在过期前通常难以主动失效。
- 不应在 payload 中放敏感明文。
- 需要合理设计过期时间和刷新机制。
- 前端存储位置影响安全风险。

### 12.3 Token 存储

常见存储：

- HttpOnly Cookie。
- localStorage。
- sessionStorage。
- 内存变量。

安全比较：

- HttpOnly Cookie 防止 JS 读取，降低 XSS 窃取风险。
- localStorage 容易被 XSS 读取。
- 内存变量刷新后丢失，但暴露面较小。

没有绝对完美方案，要结合业务、CSRF 防护、XSS 防护和用户体验设计。

## 13. Web 安全基础

### 13.1 安全基本原则

重要原则：

- 不信任任何用户输入。
- 前端校验不能替代后端校验。
- 权限必须在服务端校验。
- 敏感信息不要暴露到前端。
- 最小权限原则。
- 默认拒绝，显式允许。
- 安全策略要分层防御。

前端安全不是只靠前端完成，而是浏览器、前端、后端、网关、运维共同完成。

## 14. XSS

### 14.1 XSS 是什么

XSS 是 Cross-Site Scripting，跨站脚本攻击。

攻击者把恶意脚本注入页面，使脚本在其他用户浏览器中执行。

可能后果：

- 窃取 Token。
- 读取用户信息。
- 伪造操作。
- 修改页面内容。
- 劫持会话。
- 传播恶意链接。

### 14.2 XSS 类型

常见类型：

- 存储型 XSS。
- 反射型 XSS。
- DOM 型 XSS。

存储型 XSS：

- 恶意内容存入数据库。
- 其他用户访问页面时执行。
- 风险较高。

反射型 XSS：

- 恶意内容来自 URL 参数。
- 服务端直接反射到页面。

DOM 型 XSS：

- 漏洞发生在前端 DOM 操作中。
- 例如把 URL 参数直接写入 `innerHTML`。

### 14.3 XSS 防御

核心防御：

- 输出编码。
- 输入校验。
- HTML Sanitization。
- 避免危险 API。
- CSP。
- HttpOnly Cookie。
- 框架默认转义。

危险写法：

```js
element.innerHTML = userInput
```

安全建议：

```js
element.textContent = userInput
```

如果确实需要渲染 HTML，应使用成熟消毒库，例如 DOMPurify，并限制允许标签和属性。

### 14.4 React 和 Vue 的 XSS 注意点

React 默认会转义文本。

危险点：

```tsx
<div dangerouslySetInnerHTML={{ __html: html }} />
```

Vue 默认也会转义插值。

危险点：

```vue
<div v-html="html"></div>
```

使用这些能力时必须确保 HTML 已经过安全过滤。

## 15. CSRF

### 15.1 CSRF 是什么

CSRF 是 Cross-Site Request Forgery，跨站请求伪造。

攻击者诱导已登录用户访问恶意页面，恶意页面向目标网站发起请求。由于浏览器会自动携带目标网站 Cookie，目标网站可能误认为请求是用户本人发起。

### 15.2 CSRF 发生条件

通常需要：

- 用户已登录目标网站。
- 目标网站使用 Cookie 维持登录态。
- 攻击者能诱导用户发起请求。
- 目标接口缺少 CSRF 防护。

### 15.3 CSRF 防御

常见防御：

- SameSite Cookie。
- CSRF Token。
- 双重提交 Cookie。
- 校验 Origin/Referer。
- 自定义请求头。
- 重要操作二次确认。
- GET 不做状态变更。

Cookie 示例：

```http
Set-Cookie: sessionId=abc; HttpOnly; Secure; SameSite=Lax
```

CSRF Token 思路：

1. 服务端生成 token。
2. 页面或接口返回 token。
3. 前端在请求中携带 token。
4. 服务端校验 token。

注意：

- CORS 不是 CSRF 的完整防御。
- SameSite 是重要防线，但不应是唯一防线。
- 高风险操作需要更强校验。

## 16. 点击劫持

### 16.1 点击劫持是什么

点击劫持是攻击者把目标页面放在透明 iframe 中，诱导用户点击看似无害的页面，实际点击了目标页面按钮。

可能后果：

- 用户误操作。
- 修改设置。
- 授权操作。
- 发起敏感请求。

### 16.2 防御方式

使用响应头：

```http
X-Frame-Options: DENY
```

或 CSP：

```http
Content-Security-Policy: frame-ancestors 'none'
```

常见策略：

- `DENY`：禁止被任何页面嵌入。
- `SAMEORIGIN`：只允许同源嵌入。
- `frame-ancestors`：更灵活控制允许嵌入来源。

## 17. CSP

### 17.1 CSP 是什么

CSP 是 Content Security Policy，内容安全策略。

它通过 HTTP Header 限制页面可以加载和执行哪些资源。

示例：

```http
Content-Security-Policy: default-src 'self'; script-src 'self'; object-src 'none'
```

CSP 可以限制：

- 脚本来源。
- 样式来源。
- 图片来源。
- 字体来源。
- iframe 来源。
- 连接目标。
- 表单提交目标。

### 17.2 CSP 的价值

CSP 可以降低 XSS 影响：

- 禁止执行非授权脚本。
- 禁止内联脚本。
- 限制外部资源来源。
- 上报违规行为。

注意：

- CSP 不是 XSS 的唯一防御。
- 不能用 CSP 替代输出编码和输入处理。
- 上线 CSP 前建议先使用 Report-Only 模式观察。

```http
Content-Security-Policy-Report-Only: default-src 'self'
```

## 18. SRI

### 18.1 SRI 是什么

SRI 是 Subresource Integrity，子资源完整性。

它允许浏览器验证从 CDN 加载的脚本或样式是否被篡改。

示例：

```html
<script
  src="https://cdn.example.com/lib.js"
  integrity="sha384-..."
  crossorigin="anonymous">
</script>
```

适合：

- 加载第三方 CDN 脚本。
- 静态外部资源。

注意：

- 文件内容变化后 integrity 也要更新。
- 动态变化资源不适合 SRI。

## 19. CORS 安全误区

### 19.1 误区一：CORS 是服务端安全认证

CORS 只是浏览器控制前端脚本是否能读取跨源响应。

它不能替代：

- 认证。
- 授权。
- CSRF 防护。
- 数据权限。

### 19.2 误区二：设置 `*` 最省事

如果 API 是公开资源，且不携带凭证，可以考虑 `*`。

但私有 API 不应随意：

```http
Access-Control-Allow-Origin: *
```

尤其是需要 Cookie 或 Authorization 的接口，应使用明确 Origin 白名单。

### 19.3 误区三：前端代理解决了安全问题

开发环境代理只是为了本地开发方便。

生产安全必须由：

- 服务端鉴权。
- 网关策略。
- CORS 配置。
- Cookie 策略。
- CSRF 防护。
- 权限校验。

共同完成。

## 20. 前端常见安全风险

### 20.1 敏感信息泄露

不要在前端暴露：

- 私钥。
- 数据库密码。
- 后端服务密钥。
- 第三方 Secret。
- 内部接口地址。
- 未脱敏用户数据。

注意：

- 前端环境变量会被打包进浏览器代码。
- `VITE_`、`NEXT_PUBLIC_` 等公开变量不是秘密。

### 20.2 localStorage 存 Token 风险

localStorage 可以被 JavaScript 读取。

一旦发生 XSS，攻击者可能窃取 Token。

建议：

- 高安全场景优先考虑 HttpOnly Cookie。
- 加强 XSS 防护。
- 缩短 Token 有效期。
- 使用刷新机制和风控。

### 20.3 第三方脚本风险

第三方脚本拥有很高权限，可能读取页面数据和操作 DOM。

常见来源：

- 统计脚本。
- 广告脚本。
- 客服脚本。
- 地图 SDK。
- 支付 SDK。

防护：

- 只引入可信来源。
- 使用 CSP 限制来源。
- 尽量使用 SRI。
- 定期审查第三方脚本。

### 20.4 依赖供应链风险

前端依赖很多 npm 包，存在供应链风险。

措施：

- 使用 lockfile。
- 定期审计依赖。
- 不安装可疑包。
- 检查包名拼写。
- 使用 Dependabot/Renovate。
- CI 中加入安全扫描。

## 21. 前端鉴权与权限

### 21.1 认证与授权

认证 Authentication：

- 判断你是谁。

授权 Authorization：

- 判断你能做什么。

前端常见：

- 登录态判断。
- 路由权限。
- 菜单权限。
- 按钮权限。
- 页面权限。

注意：

- 前端权限只用于体验控制。
- 真正安全边界必须在后端。

### 21.2 RBAC

RBAC 是基于角色的访问控制。

基本模型：

```text
用户 -> 角色 -> 权限
```

示例：

- 用户 Alice 拥有管理员角色。
- 管理员角色拥有用户管理权限。
- 页面根据权限显示菜单和按钮。

### 21.3 前端权限实践

常见实现：

- 登录后获取用户信息和权限。
- 根据权限生成菜单。
- 根据权限过滤动态路由。
- 按钮级权限使用指令或组件。
- 请求 401 跳登录。
- 请求 403 显示无权限。

注意：

- 不要只靠隐藏按钮防止越权。
- 后端接口必须校验权限。
- 路由刷新后要能恢复权限状态。

## 22. 文件上传安全

### 22.1 常见风险

文件上传可能带来：

- 上传恶意脚本。
- 伪造文件类型。
- 超大文件攻击。
- 图片木马。
- 存储型 XSS。
- 覆盖已有文件。
- 泄露临时访问地址。

### 22.2 前端需要做什么

前端可以做：

- 限制文件大小。
- 限制文件扩展名。
- 限制 MIME 类型。
- 上传前预览。
- 用户提示。
- 分片上传。
- 上传进度。

但前端校验不能替代后端校验。

后端必须：

- 校验文件内容。
- 重命名文件。
- 隔离存储。
- 设置下载响应头。
- 病毒扫描。
- 权限控制。

## 23. Web 安全响应头

### 23.1 常见安全响应头

建议了解：

- `Content-Security-Policy`
- `Strict-Transport-Security`
- `X-Frame-Options`
- `X-Content-Type-Options`
- `Referrer-Policy`
- `Permissions-Policy`
- `Cross-Origin-Opener-Policy`
- `Cross-Origin-Resource-Policy`
- `Cross-Origin-Embedder-Policy`

### 23.2 X-Content-Type-Options

```http
X-Content-Type-Options: nosniff
```

作用：

- 阻止浏览器 MIME 嗅探。
- 降低资源被错误解释的风险。

### 23.3 Referrer-Policy

控制 Referer 信息发送策略。

示例：

```http
Referrer-Policy: strict-origin-when-cross-origin
```

价值：

- 减少敏感 URL 泄露。
- 控制跨站请求携带的来源信息。

### 23.4 Permissions-Policy

限制浏览器能力使用。

示例：

```http
Permissions-Policy: camera=(), microphone=(), geolocation=()
```

可限制：

- 摄像头。
- 麦克风。
- 定位。
- 全屏。
- 传感器。

## 24. 网络问题排查

### 24.1 常见问题

前端常见网络问题：

- DNS 解析失败。
- 连接超时。
- TLS 证书错误。
- 404 资源不存在。
- 502 网关错误。
- 504 网关超时。
- CORS 错误。
- Cookie 未携带。
- 缓存未更新。
- 预检请求失败。

### 24.2 DevTools Network 排查

重点查看：

- Request URL。
- Request Method。
- Status Code。
- Remote Address。
- Request Headers。
- Response Headers。
- Preview/Response。
- Timing。
- Initiator。
- Size。
- Cache 状态。

### 24.3 常见排查思路

接口失败：

1. 看状态码。
2. 看请求是否发出。
3. 看请求 URL 是否正确。
4. 看请求头是否带 token/cookie。
5. 看响应头是否有 CORS。
6. 看响应体错误信息。
7. 看后端日志。

静态资源失败：

1. 看资源 URL。
2. 看是否 404。
3. 看 CDN 是否同步。
4. 看 base path 是否正确。
5. 看缓存是否旧版本。

Cookie 问题：

1. 看 Domain。
2. 看 Path。
3. 看 SameSite。
4. 看 Secure。
5. 看是否跨站。
6. 看前端请求是否设置 credentials。

## 25. 浏览器原理与安全自检清单

### 25.1 浏览器原理

你应该能回答：

- 从输入 URL 到页面展示发生了什么？
- DOM、CSSOM、Render Tree 的关系是什么？
- 什么是重排和重绘？
- 为什么 CSS 会阻塞渲染？
- 为什么 JavaScript 可能阻塞 HTML 解析？
- requestAnimationFrame 和 setTimeout 有什么区别？
- Web Worker 适合什么场景？
- localStorage、sessionStorage、Cookie、IndexedDB 有什么区别？

### 25.2 网络协议

你应该能回答：

- HTTP 是什么？
- GET 和 POST 有什么区别？
- 常见 HTTP 状态码分别表示什么？
- 强缓存和协商缓存有什么区别？
- Cache-Control: no-cache 是什么意思？
- HTTP/1.1、HTTP/2、HTTP/3 有什么区别？
- HTTPS 解决了什么问题？
- TLS 握手大致做了什么？

### 25.3 Web 安全

你应该能回答：

- 同源策略是什么？
- CORS 是什么？
- 为什么携带 Cookie 时不能使用 `Access-Control-Allow-Origin: *`？
- XSS 有哪些类型？
- 如何防御 XSS？
- CSRF 是什么？
- 如何防御 CSRF？
- CSP 的作用是什么？
- HttpOnly、Secure、SameSite 分别有什么用？
- 前端权限为什么不是安全边界？

## 26. 学习路线建议

推荐学习顺序：

1. 学习浏览器整体架构。
2. 学习从 URL 到页面展示流程。
3. 学习关键渲染路径。
4. 学习事件循环和 JS 执行机制。
5. 学习浏览器存储。
6. 学习 HTTP 请求响应模型。
7. 学习状态码、Header、缓存。
8. 学习 HTTPS、TLS、HTTP/2、HTTP/3。
9. 学习同源策略和 CORS。
10. 学习 Cookie、Session、JWT。
11. 学习 XSS、CSRF、点击劫持。
12. 学习 CSP、SRI、安全响应头。
13. 使用 DevTools 排查真实问题。
14. 在项目中建立安全和性能检查清单。

## 27. 官方资料入口

- MDN：Critical rendering path  
  https://developer.mozilla.org/en-US/docs/Web/Performance/Critical_rendering_path

- MDN：How browsers work  
  https://developer.mozilla.org/docs/Web/Performance/Guides/How_browsers_work

- MDN：HTTP Overview  
  https://developer.mozilla.org/en-US/docs/Web/HTTP/Overview

- MDN：HTTP Caching  
  https://developer.mozilla.org/en-US/docs/Web/HTTP/Caching

- MDN：Same-origin policy  
  https://developer.mozilla.org/en-US/docs/Web/Security/Same-origin_policy

- MDN：CORS  
  https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/CORS

- MDN：Content Security Policy  
  https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy

- OWASP：XSS Prevention Cheat Sheet  
  https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html

- OWASP：CSRF Prevention Cheat Sheet  
  https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html

## 28. 总结

浏览器原理、网络协议和 Web 安全是前端高级能力的底层支撑。

浏览器原理帮助你理解页面为什么会白屏、为什么会卡顿、为什么资源加载顺序会影响首屏。网络协议帮助你理解请求如何发出、缓存如何命中、HTTPS 如何保护传输、CORS 为什么会失败。Web 安全帮助你识别 XSS、CSRF、点击劫持、敏感信息泄露、依赖供应链等风险。

真正成熟的前端工程能力，是能把这些知识用于真实项目：优化加载和渲染，设计合理缓存和鉴权，排查复杂网络问题，建立安全编码规范，并和后端、网关、运维共同保障系统稳定与安全。
