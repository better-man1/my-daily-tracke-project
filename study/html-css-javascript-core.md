# 前端工程核心三件套 HTML、CSS、JavaScript 知识点总结

## 1. 总览

HTML、CSS、JavaScript 是前端工程的核心三件套。

- HTML 负责页面结构和语义。
- CSS 负责页面样式、布局、视觉表现和响应式适配。
- JavaScript 负责页面交互、数据处理、业务逻辑和运行时行为。

现代前端框架如 React、Vue、Angular，本质上仍然建立在这三者之上。高级前端能力不是绕过 HTML、CSS、JavaScript，而是更深入地理解它们如何被浏览器解析、渲染、执行和优化。

三件套之间的关系：

```text
HTML 描述内容结构
CSS 描述视觉表现
JavaScript 描述交互行为
浏览器把三者组合成可见、可交互的页面
```

学习三件套要建立三个层次：

1. 会用：能写页面、样式和交互。
2. 理解：知道浏览器如何解析和执行。
3. 工程化：能写出可维护、可扩展、性能好的代码。

## 2. HTML 知识体系

### 2.1 HTML 是什么

HTML 是 HyperText Markup Language，超文本标记语言。它不是编程语言，而是一种标记语言，用来描述网页的结构和内容。

HTML 的核心作用：

- 描述页面结构。
- 表达内容语义。
- 组织文本、图片、表单、视频、链接等元素。
- 为 CSS 提供样式作用目标。
- 为 JavaScript 提供 DOM 操作对象。
- 为搜索引擎和辅助设备提供页面结构信息。

示例：

```html
<article>
  <h1>文章标题</h1>
  <p>这是一段正文内容。</p>
</article>
```

这段代码不仅展示内容，还表达了“这是一篇文章，包含标题和段落”的语义。

### 2.2 HTML 文档结构

标准 HTML 文档结构：

```html
<!doctype html>
<html lang="zh-CN">
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>页面标题</title>
  </head>
  <body>
    <h1>Hello HTML</h1>
  </body>
</html>
```

关键说明：

- `<!doctype html>`：声明文档类型，告诉浏览器使用标准模式解析页面。
- `html`：页面根元素。
- `lang`：声明页面语言，有利于 SEO 和可访问性。
- `head`：存放元信息，不直接展示在页面主体中。
- `meta charset`：声明字符编码，通常使用 UTF-8。
- `viewport`：移动端适配关键配置。
- `title`：页面标题，显示在浏览器标签页，也影响 SEO。
- `body`：页面可见内容。

高级要求：

- 始终声明 doctype，避免浏览器进入怪异模式。
- 正确设置语言、编码和 viewport。
- 页面标题应准确描述当前页面。

### 2.3 语义化 HTML

语义化 HTML 是指使用符合内容含义的标签，而不是全部使用 `div` 和 `span`。

常见语义标签：

- `header`：页头或区块头部。
- `nav`：导航区域。
- `main`：页面主要内容。
- `section`：页面中的主题区块。
- `article`：独立文章或内容块。
- `aside`：侧边栏、补充信息。
- `footer`：页脚或区块底部。
- `h1` 到 `h6`：标题层级。
- `p`：段落。
- `ul`、`ol`、`li`：列表。
- `figure`、`figcaption`：图片和说明。

示例：

```html
<main>
  <article>
    <header>
      <h1>前端工程化入门</h1>
      <p>发布时间：2026-05-20</p>
    </header>

    <section>
      <h2>什么是工程化</h2>
      <p>工程化是让项目更可维护的一套实践。</p>
    </section>
  </article>
</main>
```

语义化的价值：

- 提升代码可读性。
- 有利于 SEO。
- 有利于屏幕阅读器理解页面。
- 有利于团队维护。
- 减少无意义嵌套。

高级要求：

- 一个页面通常只有一个主要 `h1`。
- 标题层级应按结构递进，不要只因为字体大小乱用标题标签。
- 导航、正文、侧栏、页脚应使用合适语义标签。

### 2.4 常用文本标签

常见文本标签：

- `h1` 到 `h6`：标题。
- `p`：段落。
- `span`：行内容器，无特殊语义。
- `strong`：强调重要内容。
- `em`：强调语气。
- `br`：换行。
- `hr`：主题分割。
- `blockquote`：引用块。
- `code`：行内代码。
- `pre`：保留格式文本。

示例：

```html
<p>
  <strong>注意：</strong>
  删除操作不可恢复。
</p>

<pre><code>const message = 'hello'</code></pre>
```

注意：

- 不要用 `br` 大量制造间距，间距应该交给 CSS。
- 不要用标题标签只是为了变大字体。
- `strong` 表示重要性，不只是加粗。
- `em` 表示语义强调，不只是斜体。

### 2.5 链接和资源标签

链接：

```html
<a href="https://example.com">访问网站</a>
```

新窗口打开：

```html
<a href="https://example.com" target="_blank" rel="noopener noreferrer">
  新窗口打开
</a>
```

为什么需要 `rel="noopener noreferrer"`：

- 避免新页面通过 `window.opener` 操作原页面。
- 提升安全性。

图片：

```html
<img src="avatar.webp" alt="用户头像" width="120" height="120">
```

图片注意点：

- `alt` 用于图片无法加载或辅助阅读器描述。
- 设置宽高可以减少布局偏移。
- 非首屏图片可以使用 `loading="lazy"`。

```html
<img src="photo.webp" alt="照片" loading="lazy">
```

### 2.6 表单

表单是前端业务的核心。

常见表单标签：

- `form`
- `input`
- `textarea`
- `select`
- `option`
- `button`
- `label`
- `fieldset`
- `legend`

示例：

```html
<form>
  <label for="username">用户名</label>
  <input id="username" name="username" type="text" required>

  <label for="password">密码</label>
  <input id="password" name="password" type="password" required>

  <button type="submit">登录</button>
</form>
```

关键知识点：

- `label` 的 `for` 应对应表单控件的 `id`。
- `name` 是表单提交时的字段名。
- `required` 表示必填。
- `type="submit"` 会触发表单提交。
- 表单内的 `button` 默认可能是提交按钮，建议明确写 `type`。

常见 input 类型：

- `text`
- `password`
- `email`
- `number`
- `checkbox`
- `radio`
- `file`
- `date`
- `search`
- `tel`
- `url`

高级要求：

- 表单控件应有明确 label。
- 错误提示应和字段关联。
- 不要只依赖前端校验，后端必须再次校验。
- 复杂表单要考虑校验、联动、草稿、提交中、防重复提交等状态。

### 2.7 表格

表格用于展示结构化二维数据。

常用标签：

- `table`
- `thead`
- `tbody`
- `tfoot`
- `tr`
- `th`
- `td`
- `caption`

示例：

```html
<table>
  <caption>用户列表</caption>
  <thead>
    <tr>
      <th>姓名</th>
      <th>角色</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Alice</td>
      <td>管理员</td>
    </tr>
  </tbody>
</table>
```

注意：

- 表格适合展示表格数据，不适合做页面布局。
- `th` 表示表头单元格。
- `caption` 可以提供表格说明。
- 大数据表格需要考虑分页、虚拟滚动和列固定。

### 2.8 多媒体

图片、音频、视频是网页常见资源。

音频：

```html
<audio controls src="music.mp3"></audio>
```

视频：

```html
<video controls width="640" poster="cover.webp">
  <source src="video.mp4" type="video/mp4">
  你的浏览器不支持 video 标签。
</video>
```

注意：

- 视频应提供 `poster` 封面。
- 大视频应使用流媒体或 CDN。
- 自动播放通常会受到浏览器策略限制。
- 移动端视频播放行为存在平台差异。

### 2.9 HTML 可访问性

可访问性是让不同能力、不同设备的用户都能使用页面。

基础要求：

- 使用语义化标签。
- 表单控件绑定 label。
- 图片提供 alt。
- 按钮使用 button。
- 可交互元素可通过键盘访问。
- 焦点状态清晰。
- 弹窗打开后管理焦点。

ARIA 示例：

```html
<button aria-expanded="false" aria-controls="menu">
  菜单
</button>
```

注意：

- 优先使用原生语义标签。
- ARIA 是补充，不应滥用。
- 如果一个 `button` 可以解决，不要用 `div role="button"`。

### 2.10 HTML SEO

SEO 关注搜索引擎如何理解页面。

重点：

- 合理 title。
- meta description。
- 语义化结构。
- 正确 heading 层级。
- 图片 alt。
- 结构化数据。
- canonical。
- robots.txt。
- sitemap。

示例：

```html
<title>前端性能优化指南</title>
<meta name="description" content="系统总结前端性能优化指标、工具和实践。">
```

高级要求：

- 内容型页面应避免完全依赖客户端渲染。
- SSR/SSG 对 SEO 友好。
- 标题和描述应和页面内容一致。

## 3. CSS 知识体系

### 3.1 CSS 是什么

CSS 是 Cascading Style Sheets，层叠样式表。它负责控制 HTML 的视觉表现。

CSS 可以控制：

- 颜色。
- 字体。
- 尺寸。
- 间距。
- 布局。
- 动画。
- 响应式。
- 主题。
- 暗色模式。

示例：

```css
.button {
  padding: 8px 16px;
  color: #fff;
  background: #1677ff;
  border-radius: 4px;
}
```

### 3.2 CSS 引入方式

行内样式：

```html
<div style="color: red;">文本</div>
```

内部样式：

```html
<style>
  p {
    color: red;
  }
</style>
```

外部样式：

```html
<link rel="stylesheet" href="./style.css">
```

工程建议：

- 普通项目优先使用外部样式或组件样式。
- 不推荐大量行内样式，维护困难。
- 组件化项目可使用 CSS Modules、Scoped CSS、CSS-in-JS、Tailwind 等方案。

### 3.3 选择器

常见选择器：

- 元素选择器：`div`
- 类选择器：`.card`
- ID 选择器：`#app`
- 属性选择器：`[type="text"]`
- 后代选择器：`.card p`
- 子代选择器：`.card > p`
- 相邻兄弟选择器：`h1 + p`
- 通用兄弟选择器：`h1 ~ p`
- 伪类：`:hover`、`:focus`、`:nth-child`
- 伪元素：`::before`、`::after`

示例：

```css
.list > li:hover {
  background: #f5f5f5;
}
```

选择器建议：

- 优先使用 class。
- 避免选择器过深。
- 避免滥用 ID 选择器。
- 保持命名清晰。

### 3.4 层叠、继承和优先级

CSS 的 C 是 Cascading，表示层叠。

样式最终生效由这些因素决定：

- 来源。
- 重要性。
- 选择器优先级。
- 代码顺序。
- 继承。

优先级大致顺序：

```text
!important > 行内样式 > ID > class/属性/伪类 > 元素/伪元素
```

示例：

```css
p {
  color: black;
}

.text {
  color: blue;
}
```

如果元素是：

```html
<p class="text">内容</p>
```

最终颜色是蓝色，因为类选择器优先级更高。

工程建议：

- 少用 `!important`。
- 避免过高优先级导致后续难以覆盖。
- 使用稳定命名和模块化样式降低冲突。

### 3.5 盒模型

盒模型由四部分组成：

- content：内容。
- padding：内边距。
- border：边框。
- margin：外边距。

默认盒模型：

```css
box-sizing: content-box;
```

宽度只包含 content。

更常用的工程设置：

```css
*,
*::before,
*::after {
  box-sizing: border-box;
}
```

`border-box` 下，宽度包含 content、padding 和 border，更容易布局。

### 3.6 display

常见 display 值：

- `block`：块级元素。
- `inline`：行内元素。
- `inline-block`：行内块。
- `flex`：弹性布局。
- `grid`：网格布局。
- `none`：不显示且不占位。

区别：

- block 独占一行，可设置宽高。
- inline 不独占一行，宽高通常无效。
- inline-block 不独占一行，但可设置宽高。

### 3.7 position 定位

常见定位：

- `static`：默认定位。
- `relative`：相对自身原位置偏移。
- `absolute`：相对最近非 static 定位祖先定位。
- `fixed`：相对视口定位。
- `sticky`：粘性定位。

示例：

```css
.parent {
  position: relative;
}

.child {
  position: absolute;
  right: 0;
  top: 0;
}
```

高级要求：

- 理解 containing block。
- 理解 z-index 只在定位上下文或层叠上下文中起作用。
- sticky 需要滚动容器和偏移条件配合。

### 3.8 Flex 布局

Flex 是一维布局模型，适合横向或纵向排列。

容器属性：

- `display: flex`
- `flex-direction`
- `justify-content`
- `align-items`
- `flex-wrap`
- `gap`

子项属性：

- `flex`
- `flex-grow`
- `flex-shrink`
- `flex-basis`
- `align-self`
- `order`

示例：

```css
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
```

常见场景：

- 导航栏。
- 按钮组。
- 表单行。
- 卡片列表。
- 垂直居中。

### 3.9 Grid 布局

Grid 是二维布局模型，适合同时控制行和列。

示例：

```css
.dashboard {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
```

常用属性：

- `grid-template-columns`
- `grid-template-rows`
- `gap`
- `grid-column`
- `grid-row`
- `minmax`
- `auto-fit`
- `auto-fill`

响应式示例：

```css
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
}
```

适合：

- 仪表盘。
- 图片墙。
- 商品列表。
- 复杂页面骨架。

### 3.10 BFC

BFC 是 Block Formatting Context，块级格式化上下文。

BFC 的作用：

- 内部浮动不会影响外部。
- 可以清除浮动。
- 可以阻止 margin 折叠。
- 可以避免元素被浮动元素覆盖。

常见触发方式：

- `overflow: hidden`
- `display: flow-root`
- `position: absolute`
- `position: fixed`
- `display: inline-block`
- `display: flex`
- `display: grid`

推荐清除浮动方式：

```css
.container {
  display: flow-root;
}
```

### 3.11 响应式设计

响应式设计让页面适配不同屏幕。

常用技术：

- 媒体查询。
- 弹性布局。
- Grid。
- 百分比。
- `rem`。
- `vw`、`vh`。
- `clamp`。
- 容器查询。

媒体查询：

```css
@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
}
```

动态字体：

```css
.title {
  font-size: clamp(24px, 4vw, 48px);
}
```

高级要求：

- 不是简单缩小页面，而是重新组织布局。
- 移动端交互区域要足够大。
- 复杂表格在小屏下需要降级方案。

### 3.12 CSS 动画和过渡

过渡 transition：

```css
.button {
  transition: background 0.2s ease;
}

.button:hover {
  background: #0958d9;
}
```

动画 animation：

```css
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal {
  animation: fadeIn 0.2s ease;
}
```

性能建议：

- 优先动画 `transform` 和 `opacity`。
- 避免频繁动画 `width`、`height`、`top`、`left`。
- 动画不要过度影响可读性和可访问性。

### 3.13 CSS 变量

CSS 变量适合主题系统。

```css
:root {
  --primary-color: #1677ff;
  --text-color: #1f2937;
}

.button {
  color: #fff;
  background: var(--primary-color);
}
```

暗色主题：

```css
[data-theme='dark'] {
  --primary-color: #4dabf7;
  --text-color: #f9fafb;
}
```

价值：

- 支持主题切换。
- 运行时可动态修改。
- 减少硬编码颜色。
- 有利于设计系统落地。

### 3.14 CSS 工程实践

常见方案：

- BEM：统一 class 命名。
- CSS Modules：局部作用域。
- Sass/Less：变量、嵌套、mixin。
- PostCSS：自动转换和兼容处理。
- Tailwind CSS：原子化 CSS。
- CSS-in-JS：样式和组件逻辑结合。

工程建议：

- 小项目保持简单。
- 中大型项目需要样式隔离。
- 组件库需要主题和设计 Token。
- 不要让全局样式无边界扩散。

## 4. JavaScript 知识体系

### 4.1 JavaScript 是什么

JavaScript 是 Web 的编程语言，负责页面交互和业务逻辑。

JavaScript 可以做：

- DOM 操作。
- 事件处理。
- 表单交互。
- 网络请求。
- 数据处理。
- 动画。
- 本地存储。
- 前端路由。
- WebSocket。
- Canvas/WebGL。
- Node.js 服务端开发。

现代前端中的 React、Vue、Vite、Webpack、Node.js 都离不开 JavaScript。

### 4.2 基础数据类型

JavaScript 原始类型：

- string。
- number。
- boolean。
- null。
- undefined。
- symbol。
- bigint。

引用类型：

- object。
- array。
- function。
- date。
- regexp。
- map。
- set。

示例：

```js
const name = 'Alice'
const age = 18
const user = {
  name,
  age
}
```

注意：

- `typeof null` 结果是 `"object"`，这是历史遗留问题。
- 数组也是对象。
- 函数是一种可调用对象。

### 4.3 变量声明

JavaScript 有三种声明方式：

- `var`
- `let`
- `const`

现代代码建议：

- 默认使用 `const`。
- 需要重新赋值时使用 `let`。
- 避免使用 `var`。

原因：

- `var` 是函数作用域，容易产生意外提升。
- `let` 和 `const` 是块级作用域。
- `const` 可以减少无意重新赋值。

示例：

```js
const user = { name: 'Alice' }
user.name = 'Bob'
```

注意：`const` 表示变量绑定不能变，不表示对象内容不可变。

### 4.4 作用域和闭包

作用域决定变量在哪里可访问。

常见作用域：

- 全局作用域。
- 函数作用域。
- 块级作用域。
- 模块作用域。

闭包是函数记住并访问其词法作用域的能力。

示例：

```js
function createCounter() {
  let count = 0

  return function increment() {
    count += 1
    return count
  }
}

const counter = createCounter()
counter()
counter()
```

闭包用途：

- 封装私有变量。
- 保存状态。
- 函数工厂。
- 防抖节流。
- 模块化。

注意：

- 闭包会保持变量引用。
- 不合理使用可能导致内存无法释放。

### 4.5 this

`this` 的值取决于函数调用方式，而不是定义位置。

常见情况：

- 普通函数调用：非严格模式下指向全局对象，严格模式下是 undefined。
- 对象方法调用：指向调用对象。
- 构造函数调用：指向新创建对象。
- call/apply/bind：显式指定。
- 箭头函数：没有自己的 this，继承外层 this。

示例：

```js
const user = {
  name: 'Alice',
  sayName() {
    console.log(this.name)
  }
}

user.sayName()
```

箭头函数：

```js
const obj = {
  name: 'Alice',
  say: () => {
    console.log(this.name)
  }
}
```

这里箭头函数不适合作为需要自身 this 的对象方法。

### 4.6 原型和原型链

JavaScript 使用原型机制实现对象继承。

每个对象都有内部原型，查找属性时：

1. 先查找对象自身属性。
2. 如果没有，沿原型链向上查找。
3. 直到找到或到达 null。

示例：

```js
function User(name) {
  this.name = name
}

User.prototype.sayHello = function () {
  return `Hello ${this.name}`
}

const user = new User('Alice')
user.sayHello()
```

现代 class 语法本质上仍建立在原型之上。

### 4.7 class

class 是更清晰的面向对象语法。

```js
class User {
  constructor(name) {
    this.name = name
  }

  sayHello() {
    return `Hello ${this.name}`
  }
}

const user = new User('Alice')
```

继承：

```js
class Admin extends User {
  constructor(name, role) {
    super(name)
    this.role = role
  }
}
```

注意：

- class 不会让 JavaScript 变成传统类语言。
- 方法仍在原型上。
- 前端业务中应避免过度继承，组合通常更灵活。

### 4.8 函数

函数是 JavaScript 的核心。

函数声明：

```js
function add(a, b) {
  return a + b
}
```

函数表达式：

```js
const add = function (a, b) {
  return a + b
}
```

箭头函数：

```js
const add = (a, b) => a + b
```

高阶函数：

```js
function withLog(fn) {
  return function (...args) {
    console.log('start')
    return fn(...args)
  }
}
```

常见数组高阶方法：

- `map`
- `filter`
- `reduce`
- `forEach`
- `some`
- `every`
- `find`

### 4.9 异步编程

JavaScript 是单线程执行，但通过事件循环处理异步任务。

常见异步方式：

- 回调函数。
- Promise。
- async/await。
- 事件监听。
- 定时器。
- 网络请求。

Promise 示例：

```js
fetch('/api/users')
  .then(response => response.json())
  .then(data => {
    console.log(data)
  })
  .catch(error => {
    console.error(error)
  })
```

async/await 示例：

```js
async function getUsers() {
  try {
    const response = await fetch('/api/users')
    const data = await response.json()
    return data
  } catch (error) {
    console.error(error)
  }
}
```

高级要求：

- 正确处理错误。
- 并发请求使用 `Promise.all`。
- 需要全部完成但允许部分失败可用 `Promise.allSettled`。
- 搜索和路由切换场景要考虑请求取消。

### 4.10 事件循环

事件循环决定异步任务执行顺序。

核心概念：

- 调用栈。
- 宏任务。
- 微任务。
- 渲染时机。

常见宏任务：

- `setTimeout`
- `setInterval`
- DOM 事件。
- 网络回调。

常见微任务：

- `Promise.then`
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

输出：

```text
sync
promise
timeout
```

原因：

- 同步代码先执行。
- 微任务在当前宏任务结束后执行。
- 定时器回调作为后续宏任务执行。

### 4.11 DOM 操作

DOM 是 Document Object Model，文档对象模型。

常见 API：

```js
const button = document.querySelector('.button')
button.textContent = '提交'
button.classList.add('active')
```

创建元素：

```js
const li = document.createElement('li')
li.textContent = 'Item'
document.querySelector('ul').appendChild(li)
```

注意：

- 频繁 DOM 操作成本较高。
- 大量插入 DOM 应批量处理。
- 现代框架通过虚拟 DOM 或响应式机制减少手动 DOM 操作。

### 4.12 事件处理

绑定事件：

```js
button.addEventListener('click', event => {
  console.log('clicked')
})
```

事件传播阶段：

- 捕获阶段。
- 目标阶段。
- 冒泡阶段。

阻止默认行为：

```js
event.preventDefault()
```

阻止冒泡：

```js
event.stopPropagation()
```

事件委托：

```js
document.querySelector('ul').addEventListener('click', event => {
  if (event.target.matches('li')) {
    console.log(event.target.textContent)
  }
})
```

事件委托适合动态列表和大量子元素，可以减少事件监听数量。

### 4.13 模块化

ES Module 是现代 JavaScript 官方模块系统。

导出：

```js
export function add(a, b) {
  return a + b
}
```

导入：

```js
import { add } from './math.js'
```

默认导出：

```js
export default function createApp() {}
```

动态导入：

```js
const module = await import('./dialog.js')
```

价值：

- 明确依赖关系。
- 避免全局污染。
- 支持代码分割。
- 支持 Tree Shaking。

### 4.14 错误处理

同步错误：

```js
try {
  JSON.parse('{')
} catch (error) {
  console.error(error)
}
```

异步错误：

```js
async function run() {
  try {
    await fetchData()
  } catch (error) {
    console.error(error)
  }
}
```

全局错误：

```js
window.addEventListener('error', event => {
  console.log(event.error)
})

window.addEventListener('unhandledrejection', event => {
  console.log(event.reason)
})
```

高级要求：

- 不吞掉错误。
- 给用户友好提示。
- 给开发者保留上下文日志。
- 线上接入错误监控。

### 4.15 Web API

浏览器提供大量 Web API。

常见 API：

- DOM API。
- Fetch API。
- Storage API。
- History API。
- URL API。
- WebSocket。
- Canvas。
- Web Worker。
- IntersectionObserver。
- MutationObserver。
- ResizeObserver。

示例：IntersectionObserver 实现图片懒加载。

```js
const observer = new IntersectionObserver(entries => {
  for (const entry of entries) {
    if (entry.isIntersecting) {
      const img = entry.target
      img.src = img.dataset.src
      observer.unobserve(img)
    }
  }
})
```

高级要求：

- 熟悉常见 Web API 的适用场景。
- 了解兼容性和降级方案。
- 避免滥用轮询，优先使用浏览器观察器 API。

## 5. 三件套如何协同工作

### 5.1 浏览器渲染流程

浏览器大致流程：

1. 解析 HTML，生成 DOM。
2. 解析 CSS，生成 CSSOM。
3. DOM 和 CSSOM 合成渲染树。
4. 计算布局 Layout。
5. 绘制 Paint。
6. 合成 Composite。
7. 执行 JavaScript，可能修改 DOM 和 CSSOM。

关键理解：

- HTML 结构影响 DOM。
- CSS 影响样式计算和布局。
- JavaScript 可以修改 DOM 和样式。
- JS 执行可能阻塞 HTML 解析。
- CSS 可能阻塞渲染。

### 5.2 关注点分离

传统理念：

- HTML 负责结构。
- CSS 负责样式。
- JS 负责行为。

现代组件化中，三者可能放在同一个组件文件里，但职责仍然应清晰。

例如 Vue SFC：

```vue
<template>
  <button class="button" @click="submit">提交</button>
</template>

<script setup>
function submit() {}
</script>

<style scoped>
.button {
  padding: 8px 16px;
}
</style>
```

虽然写在一个文件中，但结构、行为、样式仍有清楚边界。

### 5.3 性能协同

HTML 性能关注：

- 结构简洁。
- 减少无意义嵌套。
- 关键内容尽早出现。
- 图片设置尺寸和 alt。

CSS 性能关注：

- 避免过大 CSS。
- 避免复杂布局抖动。
- 使用合适动画属性。
- 提取关键 CSS。

JavaScript 性能关注：

- 减少首屏 JS。
- 避免长任务。
- 减少频繁 DOM 操作。
- 合理拆包和懒加载。

## 6. 工程化视角下的三件套

### 6.1 HTML 工程化

现代项目中 HTML 常由框架和构建工具生成或增强。

相关能力：

- 模板引擎。
- SSR。
- SSG。
- SEO meta 管理。
- HTML 压缩。
- 资源注入。
- preload/prefetch。

高级要求：

- 能控制页面 head 信息。
- 能设计 SEO 友好页面。
- 能处理 SSR 生成 HTML。
- 能理解 HTML 与首屏性能的关系。

### 6.2 CSS 工程化

CSS 工程化能力：

- Sass/Less。
- PostCSS。
- CSS Modules。
- Scoped CSS。
- CSS-in-JS。
- Tailwind CSS。
- 设计 Token。
- 主题系统。
- 样式压缩。
- 无用 CSS 清理。

高级要求：

- 能制定团队样式规范。
- 能设计主题和暗色模式。
- 能避免全局样式污染。
- 能维护组件库样式体系。

### 6.3 JavaScript 工程化

JavaScript 工程化能力：

- 模块化。
- TypeScript。
- Babel。
- ESLint。
- Prettier。
- Vite。
- Webpack。
- Rollup。
- 测试。
- 代码分割。
- Tree Shaking。
- 性能监控。

高级要求：

- 能搭建项目工程。
- 能分析构建产物。
- 能制定代码规范。
- 能设计模块边界。
- 能处理浏览器兼容和 polyfill。

## 7. 常见面试和自检问题

### 7.1 HTML

你应该能回答：

- 什么是语义化 HTML？
- `doctype` 有什么作用？
- `meta viewport` 为什么重要？
- `script` 的 defer 和 async 有什么区别？
- `label` 为什么要和表单控件关联？
- `alt` 的作用是什么？
- HTML 如何影响 SEO？
- 如何提升页面可访问性？

### 7.2 CSS

你应该能回答：

- 标准盒模型和 IE 盒模型有什么区别？
- `box-sizing: border-box` 为什么常用？
- Flex 和 Grid 分别适合什么场景？
- BFC 是什么，有什么作用？
- CSS 优先级如何计算？
- `position: absolute` 相对谁定位？
- 如何实现水平垂直居中？
- 如何做响应式布局？
- 为什么推荐使用 transform 做动画？

### 7.3 JavaScript

你应该能回答：

- JavaScript 有哪些数据类型？
- `let`、`const`、`var` 有什么区别？
- 什么是闭包？
- this 指向如何判断？
- 原型链是什么？
- Promise 的状态有哪些？
- async/await 如何处理错误？
- 宏任务和微任务有什么区别？
- 事件冒泡和事件委托是什么？
- ES Module 和 CommonJS 有什么区别？

## 8. 学习路线建议

推荐学习顺序：

1. 学习 HTML 文档结构和常用标签。
2. 学习语义化、表单、表格、媒体和 SEO。
3. 学习 CSS 选择器、盒模型、层叠和继承。
4. 学习 Flex、Grid、定位和响应式。
5. 学习 CSS 动画、变量和工程化方案。
6. 学习 JavaScript 基础类型、函数、对象和数组。
7. 学习作用域、闭包、this、原型链。
8. 学习 Promise、async/await、事件循环。
9. 学习 DOM、事件、Fetch、Storage 等 Web API。
10. 学习 ES Module、构建工具和 TypeScript。
11. 学习浏览器渲染原理和性能优化。
12. 用 React 或 Vue 项目综合实践三件套能力。

## 9. 总结

HTML、CSS、JavaScript 是前端工程的根基。HTML 决定页面结构和语义，CSS 决定布局和视觉表现，JavaScript 决定交互和业务逻辑。

很多开发者学习框架后容易忽略三件套，但真正的高级前端能力往往来自对基础的深入理解。语义化 HTML 影响 SEO 和可访问性，CSS 布局能力决定页面质量，JavaScript 运行机制决定交互稳定性和性能。

掌握三件套的目标不是背标签、背属性、背 API，而是能够在真实项目中写出结构清晰、样式稳定、交互可靠、性能良好、易于维护的前端代码。
