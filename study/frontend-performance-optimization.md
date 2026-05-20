# 前端工程性能优化知识点总结

## 1. 前端性能优化是什么

前端性能优化是指通过工程化、代码设计、资源管理、网络优化、渲染优化、缓存策略和监控体系等手段，让页面加载更快、交互更流畅、资源消耗更低、用户体验更稳定。

前端性能优化不仅是“让页面快一点”，它关注的是完整用户体验：

- 页面能否快速打开。
- 首屏内容能否尽快展示。
- 用户点击后是否快速响应。
- 页面滚动和动画是否流畅。
- 弱网和低端设备下是否可用。
- 发布后性能是否持续稳定。

性能优化的核心目标：

- 降低白屏时间。
- 降低首屏时间。
- 降低资源体积。
- 降低主线程阻塞。
- 提升交互响应速度。
- 提升缓存命中率。
- 提升页面稳定性。
- 建立可持续监控和治理机制。

## 2. 性能优化的基本原则

### 2.1 先测量，再优化

性能优化不能靠感觉。正确流程应该是：

1. 明确性能指标。
2. 使用工具采集数据。
3. 找出瓶颈。
4. 制定优化方案。
5. 小范围验证。
6. 上线观察。
7. 沉淀规范。

常见错误做法：

- 没有数据就开始优化。
- 只看本地开发环境，不看真实用户环境。
- 只关注加载速度，不关注交互性能。
- 优化后不验证结果。
- 只做一次优化，没有长期监控。

### 2.2 优先优化关键路径

关键路径是影响用户看到页面和完成操作的核心链路。

例如：

- 首屏所需 HTML、CSS、JavaScript、图片。
- 登录、下单、支付、提交表单等关键流程。
- 核心页面的主接口。
- 首屏组件和首屏数据。

优化时应优先处理：

- 阻塞首屏渲染的资源。
- 体积最大的 JS/CSS。
- 最慢的接口。
- 最耗时的主线程任务。
- 用户最常访问的页面。

### 2.3 分层优化

前端性能问题通常不是单点问题，需要分层分析：

- 网络层：DNS、TCP、TLS、HTTP、CDN、缓存。
- 资源层：JS、CSS、图片、字体、视频。
- 构建层：打包、压缩、拆包、Tree Shaking。
- 运行时层：JavaScript 执行、渲染、事件、内存。
- 框架层：组件渲染、状态管理、数据请求。
- 业务层：接口设计、页面流程、数据规模。
- 监控层：指标采集、错误追踪、性能告警。

## 3. 核心性能指标

### 3.1 FCP

FCP 是 First Contentful Paint，首次内容绘制。

它表示浏览器第一次绘制文本、图片、SVG、Canvas 等内容的时间。

意义：

- 衡量用户什么时候看到页面开始有内容。
- FCP 越早，用户越容易感知页面正在加载。

优化方向：

- 减少 HTML 阻塞。
- 减少首屏 CSS 体积。
- 优化服务器响应时间。
- 避免过大的首屏 JS 阻塞渲染。

### 3.2 LCP

LCP 是 Largest Contentful Paint，最大内容绘制。

它表示视口中最大的内容元素完成渲染的时间，通常是：

- 首屏大图。
- 主标题。
- Banner。
- 大块文本。
- 核心内容区。

意义：

- 衡量首屏主要内容什么时候真正可见。
- 是 Core Web Vitals 的重要指标。

优化方向：

- 优化首屏大图。
- 预加载 LCP 资源。
- 减少阻塞渲染的 JS 和 CSS。
- 优化服务端响应。
- 使用 CDN。
- 避免首屏内容被客户端长时间计算阻塞。

### 3.3 CLS

CLS 是 Cumulative Layout Shift，累计布局偏移。

它衡量页面加载过程中元素意外移动的程度。

常见原因：

- 图片没有设置宽高。
- 广告位后加载撑开布局。
- 字体加载导致文字尺寸变化。
- 异步内容插入到已有内容上方。
- 骨架屏和真实内容尺寸不一致。

优化方向：

- 给图片、视频、广告容器预留尺寸。
- 使用稳定布局。
- 字体加载使用合理策略。
- 避免在用户阅读过程中突然插入内容。

### 3.4 INP

INP 是 Interaction to Next Paint，交互到下一次绘制的延迟。

它衡量用户点击、输入、键盘操作后，页面多久能响应并绘制结果。

常见原因：

- 主线程长任务。
- 事件处理函数太重。
- 大量组件同步更新。
- 大量 DOM 操作。
- 复杂表格、图表、富文本阻塞。

优化方向：

- 拆分长任务。
- 减少事件处理逻辑。
- 降低组件重渲染范围。
- 使用 Web Worker。
- 使用虚拟列表。
- 使用防抖、节流和调度。

### 3.5 TTFB

TTFB 是 Time To First Byte，首字节时间。

它表示浏览器发起请求到收到第一个字节的时间。

影响因素：

- DNS 解析。
- TCP/TLS 建连。
- 服务器处理时间。
- 后端接口耗时。
- CDN 命中情况。
- 网络距离。

优化方向：

- 使用 CDN。
- 优化服务端渲染或接口。
- 启用缓存。
- 减少重定向。
- 使用更近的边缘节点。

### 3.6 TTI

TTI 是 Time To Interactive，可交互时间。

它表示页面达到稳定可交互状态的时间。

虽然现在 Core Web Vitals 更关注 INP，但 TTI 仍然有参考价值。

优化方向：

- 减少首屏 JavaScript。
- 延迟非关键脚本。
- 拆分代码。
- 避免主线程长时间阻塞。

## 4. 性能分析工具

### 4.1 Chrome DevTools Performance

Performance 面板用于分析页面运行时性能。

可以观察：

- 主线程任务。
- 长任务。
- JavaScript 执行耗时。
- Layout。
- Paint。
- Composite。
- FPS。
- 网络请求。
- 用户交互。

常见分析方法：

1. 录制用户操作。
2. 查看是否存在长任务。
3. 找出耗时最多的脚本。
4. 查看是否频繁 Layout。
5. 分析函数调用栈。
6. 针对瓶颈优化。

### 4.2 Chrome DevTools Network

Network 面板用于分析资源加载。

重点关注：

- 请求数量。
- 请求体积。
- 请求耗时。
- 是否命中缓存。
- 是否阻塞。
- 是否有重定向。
- 是否有失败请求。
- 资源加载顺序。

常见问题：

- JS 包过大。
- 图片过大。
- 字体阻塞。
- 接口慢。
- 资源未缓存。
- CDN 未命中。

### 4.3 Lighthouse

Lighthouse 可以生成页面性能报告。

它会分析：

- Performance。
- Accessibility。
- Best Practices。
- SEO。
- PWA。

适合：

- 快速检查页面质量。
- 发现常见性能问题。
- 和优化前后结果对比。

注意：Lighthouse 是实验室数据，不完全等于真实用户数据。

### 4.4 WebPageTest

WebPageTest 可以模拟不同地区、网络、设备下的页面加载表现。

适合：

- 分析首屏加载。
- 分析瀑布图。
- 对比 CDN 效果。
- 模拟弱网。
- 分析重复访问缓存效果。

### 4.5 React Profiler 和 Vue Devtools

框架性能问题需要使用框架工具分析。

React Profiler 可用于：

- 查看组件渲染耗时。
- 找到频繁重渲染组件。
- 分析 props/state 变化影响。

Vue Devtools 可用于：

- 查看组件树。
- 查看响应式状态。
- 分析组件更新。
- 调试 Pinia。

## 5. 加载性能优化

### 5.1 减少关键资源体积

首屏加载速度很大程度取决于关键资源大小。

关键资源包括：

- HTML。
- 首屏 CSS。
- 首屏 JavaScript。
- 首屏图片。
- 字体文件。

优化方式：

- 压缩 JavaScript。
- 压缩 CSS。
- 移除无用代码。
- 按需加载依赖。
- 图片压缩。
- 字体裁剪。
- 使用现代图片格式。
- 避免首屏引入大型库。

### 5.2 代码分割

代码分割是把一个大包拆成多个小包，按需加载。

常见拆分方式：

- 路由级拆分。
- 组件级拆分。
- 第三方依赖拆分。
- 业务模块拆分。

React 示例：

```tsx
import { lazy, Suspense } from 'react'

const Settings = lazy(() => import('./pages/Settings'))

function App() {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <Settings />
    </Suspense>
  )
}
```

Vue Router 示例：

```ts
const routes = [
  {
    path: '/settings',
    component: () => import('@/pages/Settings.vue')
  }
]
```

注意：

- 拆包不是越碎越好。
- 过多小包会增加请求和调度成本。
- 需要结合页面访问路径和业务模块设计拆分策略。

### 5.3 路由懒加载

路由懒加载是最常见、收益明显的代码分割方式。

适合：

- 后台系统。
- 多页面业务系统。
- 大型单页应用。
- 非首屏页面较多的应用。

优势：

- 减少首屏 JS。
- 用户访问到某个页面时再加载对应代码。
- 降低初始解析和执行成本。

### 5.4 组件懒加载

某些大型组件不需要首屏立即加载，可以延迟加载。

适合：

- 富文本编辑器。
- 代码编辑器。
- 图表库。
- 地图组件。
- 大型弹窗。
- 低频功能模块。

优化思路：

- 首屏只加载轻量占位。
- 用户点击或滚动到可视区域时再加载。
- 对大型库做动态导入。

### 5.5 第三方依赖优化

第三方依赖是包体积膨胀的常见原因。

常见问题：

- 引入整个工具库。
- 使用体积巨大的 UI 库。
- 图表库、编辑器、地图 SDK 首屏加载。
- 重复依赖多个版本。
- 未开启 Tree Shaking。

优化方式：

- 按需引入。
- 使用更轻量替代库。
- 动态加载低频库。
- 分析 Bundle。
- 去除重复依赖。
- 使用 ESM 版本依赖。

示例：

```ts
import debounce from 'lodash-es/debounce'
```

比直接引入整个 lodash 更容易被优化。

### 5.6 Tree Shaking

Tree Shaking 用于移除未使用代码。

生效条件：

- 使用 ES Module。
- 构建工具支持。
- 依赖包没有不可分析的副作用。
- package.json 正确配置 `sideEffects`。

注意：

- CommonJS 不利于 Tree Shaking。
- 动态访问对象属性可能影响优化。
- 有副作用的模块不能随意移除。

### 5.7 预加载和预取

常见资源提示：

- `preload`：提前加载当前页面很快需要的关键资源。
- `prefetch`：浏览器空闲时预取未来可能用到的资源。
- `preconnect`：提前建立连接。
- `dns-prefetch`：提前 DNS 解析。

示例：

```html
<link rel="preload" href="/fonts/main.woff2" as="font" type="font/woff2" crossorigin>
<link rel="preconnect" href="https://cdn.example.com">
```

使用建议：

- LCP 图片可以考虑 preload。
- 字体可以考虑 preload。
- 未来页面资源可以 prefetch。
- 不要滥用 preload，否则会抢占关键资源带宽。

## 6. 网络优化

### 6.1 HTTP 缓存

HTTP 缓存是前端性能优化中收益非常高的手段。

缓存分为：

- 强缓存。
- 协商缓存。

强缓存常用 Header：

```http
Cache-Control: max-age=31536000, immutable
```

协商缓存常用 Header：

- `ETag`
- `If-None-Match`
- `Last-Modified`
- `If-Modified-Since`

推荐策略：

- HTML：不强缓存或短缓存。
- JS/CSS：文件名带 Hash，长缓存。
- 图片字体：文件名带 Hash，长缓存。
- 接口：按业务决定缓存策略。

### 6.2 CDN

CDN 可以把静态资源分发到离用户更近的节点。

适合：

- JS。
- CSS。
- 图片。
- 字体。
- 视频。
- 下载资源。

CDN 优势：

- 降低网络延迟。
- 提升下载速度。
- 减轻源站压力。
- 提高可用性。

注意：

- 配置正确缓存策略。
- 资源文件名使用 Hash。
- 发布后需要考虑 CDN 刷新。
- 回滚时要保证旧资源仍可访问。

### 6.3 减少重定向

重定向会增加一次或多次网络往返。

常见重定向：

- HTTP 跳 HTTPS。
- 无 www 跳 www。
- 旧路径跳新路径。
- 登录跳转。

优化建议：

- 链接直接使用最终地址。
- 避免多级重定向。
- CDN 和网关层统一规则。

### 6.4 HTTP/2 和 HTTP/3

HTTP/2 优势：

- 多路复用。
- Header 压缩。
- 二进制帧。
- 连接复用。

HTTP/3 基于 QUIC，改善弱网和连接迁移场景。

优化意义：

- 降低多个小资源请求成本。
- 改善并发加载。
- 提升网络不稳定环境体验。

但即使使用 HTTP/2，也不意味着可以无限拆包。资源调度和浏览器解析执行仍然有成本。

## 7. 图片优化

### 7.1 选择合适格式

常见格式：

- JPEG：适合照片。
- PNG：适合透明图、图标、截图。
- WebP：体积更小，兼容性较好。
- AVIF：压缩率更高，但兼容性和编码成本需要评估。
- SVG：适合图标和矢量图。

优化建议：

- 照片优先考虑 WebP/AVIF。
- 图标优先使用 SVG。
- 透明图根据场景选择 PNG/WebP。
- 不要用超大原图直接展示。

### 7.2 响应式图片

不同设备不应加载同一张超大图片。

示例：

```html
<img
  src="image-800.webp"
  srcset="image-400.webp 400w, image-800.webp 800w, image-1200.webp 1200w"
  sizes="(max-width: 600px) 400px, 800px"
  alt="示例图片"
>
```

好处：

- 移动端加载小图。
- 高分屏加载合适清晰度图片。
- 减少带宽浪费。

### 7.3 图片懒加载

浏览器原生支持：

```html
<img src="photo.webp" loading="lazy" alt="photo">
```

适合：

- 列表图片。
- 商品图片。
- 内容流。
- 非首屏图片。

注意：

- 首屏 LCP 图片不应懒加载。
- 懒加载图片应预留尺寸，避免 CLS。

### 7.4 图片尺寸预留

图片没有宽高会导致布局偏移。

建议：

```html
<img src="banner.webp" width="1200" height="400" alt="banner">
```

或使用 CSS：

```css
.image-wrapper {
  aspect-ratio: 3 / 1;
}
```

### 7.5 图片压缩

常见工具：

- imagemin。
- sharp。
- squoosh。
- tinypng。
- vite-plugin-imagemin。

工程建议：

- 构建阶段压缩图片。
- 上传前压缩图片。
- CDN 提供动态裁剪和格式转换。
- 对用户上传图片做服务端处理。

## 8. 字体优化

### 8.1 字体加载问题

字体文件通常较大，可能影响首屏。

常见问题：

- 字体阻塞文本显示。
- 字体切换导致布局偏移。
- 加载了过多字重。
- 加载了完整中文字体。

### 8.2 font-display

使用 `font-display` 控制字体加载策略。

```css
@font-face {
  font-family: "Inter";
  src: url("/fonts/inter.woff2") format("woff2");
  font-display: swap;
}
```

常见值：

- `auto`
- `block`
- `swap`
- `fallback`
- `optional`

一般 Web 项目常用 `swap` 或 `optional`，避免长时间白字。

### 8.3 字体裁剪

中文字体体积极大，需要特别谨慎。

优化方式：

- 只加载必要字重。
- 使用系统字体。
- 对固定文案做字体子集化。
- 使用 CDN 字体服务。
- 避免首屏加载大体积字体。

## 9. CSS 性能优化

### 9.1 减少阻塞 CSS

CSS 会阻塞渲染，因为浏览器需要 CSSOM 才能构建渲染树。

优化方式：

- 压缩 CSS。
- 移除未使用 CSS。
- 拆分首屏关键 CSS。
- 非关键 CSS 延迟加载。
- 避免过大的全局样式。

### 9.2 Critical CSS

Critical CSS 是首屏关键样式。

优化思路：

- 将首屏必要 CSS 内联到 HTML。
- 非首屏样式异步加载。
- 减少首屏渲染阻塞。

适合：

- 官网。
- 营销页。
- 内容站。
- SSR/SSG 页面。

### 9.3 CSS 选择器优化

现代浏览器对选择器已经很快，但仍应避免过度复杂。

建议：

- 避免过深嵌套。
- 避免大量通配符选择器。
- 避免复杂后代选择器。
- 保持样式结构清晰。

更重要的是可维护性。糟糕 CSS 结构会导致后续性能和维护问题。

## 10. JavaScript 运行时优化

### 10.1 减少主线程阻塞

浏览器主线程负责：

- 执行 JavaScript。
- 处理用户事件。
- 样式计算。
- 布局。
- 绘制。

如果 JavaScript 长时间占用主线程，页面就会卡顿。

优化方式：

- 拆分长任务。
- 延迟非关键逻辑。
- 使用 Web Worker。
- 使用 requestIdleCallback。
- 减少同步计算。
- 避免大型 JSON 同步解析阻塞。

### 10.2 长任务优化

长任务通常指超过 50ms 的主线程任务。

拆分示例：

```ts
function processLargeList(list: Item[]) {
  let index = 0

  function runChunk() {
    const end = Math.min(index + 100, list.length)

    while (index < end) {
      processItem(list[index])
      index++
    }

    if (index < list.length) {
      setTimeout(runChunk, 0)
    }
  }

  runChunk()
}
```

这样可以让浏览器在任务之间处理用户输入和渲染。

### 10.3 防抖和节流

防抖 debounce：连续触发后，只在停止一段时间后执行。

适合：

- 搜索输入。
- 窗口 resize 后计算。
- 表单自动保存。

节流 throttle：一段时间内最多执行一次。

适合：

- 滚动监听。
- 鼠标移动。
- 拖拽。
- 高频按钮点击。

### 10.4 Web Worker

Web Worker 可以把计算放到独立线程，避免阻塞主线程。

适合：

- 大量数据计算。
- 文件解析。
- 图片处理。
- 加密解密。
- 大型 JSON 处理。

注意：

- Worker 和主线程通信需要序列化数据。
- 不适合频繁传输巨大对象。
- Worker 不能直接访问 DOM。

## 11. 渲染性能优化

### 11.1 减少重排和重绘

重排 Layout：元素几何信息变化，需要重新计算布局。

重绘 Paint：元素样式变化，但不影响布局。

常见触发重排：

- 修改宽高。
- 修改 margin/padding。
- 修改 position。
- 读取布局属性后立即写入样式。
- 添加或删除大量 DOM。

优化方式：

- 批量读写 DOM。
- 使用 class 切换样式。
- 避免循环中频繁触发布局。
- 使用 transform 替代 top/left 动画。
- 使用虚拟列表减少 DOM 数量。

### 11.2 动画优化

推荐动画属性：

- `transform`
- `opacity`

不推荐频繁动画属性：

- `width`
- `height`
- `top`
- `left`
- `margin`

示例：

```css
.box {
  transform: translateX(100px);
  opacity: 0.8;
  transition: transform 0.2s, opacity 0.2s;
}
```

### 11.3 requestAnimationFrame

`requestAnimationFrame` 会在浏览器下一帧绘制前执行回调。

适合：

- 动画。
- 滚动同步。
- 高频视觉更新。

```ts
function animate() {
  update()
  requestAnimationFrame(animate)
}

requestAnimationFrame(animate)
```

相比 `setTimeout`，它更符合浏览器渲染节奏。

### 11.4 虚拟列表

虚拟列表只渲染可视区域附近的元素。

适合：

- 大表格。
- 长列表。
- 日志列表。
- 聊天记录。
- 下拉大数据选项。

常见库：

- react-window。
- react-virtualized。
- TanStack Virtual。
- vue-virtual-scroller。

优化效果：

- 减少 DOM 数量。
- 降低渲染成本。
- 降低内存占用。

## 12. React 性能优化

### 12.1 减少不必要渲染

React 组件重新渲染常见原因：

- state 更新。
- props 变化。
- 父组件重新渲染。
- context value 变化。
- 外部 store 订阅变化。

优化方式：

- 合理拆分组件。
- 状态下沉或局部化。
- 使用 `React.memo`。
- 使用 `useMemo`。
- 使用 `useCallback`。
- 避免频繁创建新对象和新函数传给子组件。

### 12.2 React.memo

`React.memo` 用于在 props 不变时跳过组件重新渲染。

```tsx
const UserItem = React.memo(function UserItem({ name }: { name: string }) {
  return <li>{name}</li>
})
```

适合：

- 列表项。
- 渲染成本较高组件。
- props 稳定的组件。

不适合：

- 极简单组件。
- props 每次都变化。
- 为了优化而盲目包裹所有组件。

### 12.3 useMemo 和 useCallback

`useMemo` 缓存计算结果。

```tsx
const filteredList = useMemo(() => {
  return list.filter(item => item.name.includes(keyword))
}, [list, keyword])
```

`useCallback` 缓存函数引用。

```tsx
const handleSelect = useCallback((id: number) => {
  setSelectedId(id)
}, [])
```

使用建议：

- 先测量是否有性能问题。
- 对昂贵计算使用 useMemo。
- 对传给 memo 子组件的函数使用 useCallback。
- 不要过度使用，避免代码复杂。

### 12.4 Context 优化

Context value 变化会让消费它的组件重新渲染。

优化方式：

- 拆分 Context。
- 状态和 dispatch 分离。
- 使用 selector 型状态库。
- 避免把频繁变化的大对象放入单个 Context。

## 13. Vue3 性能优化

### 13.1 响应式优化

Vue3 响应式虽然高效，但仍需避免不必要的深层响应式。

优化方式：

- 大型不可变数据使用 `shallowRef`。
- 不需要响应式的数据不要放入 `reactive`。
- 避免滥用 deep watch。
- 合理拆分组件和状态。

```ts
const largeData = shallowRef<LargeData[]>([])
```

### 13.2 v-memo 和 v-once

`v-once` 用于只渲染一次静态内容。

```vue
<h1 v-once>{{ title }}</h1>
```

`v-memo` 用于根据依赖跳过部分更新。

```vue
<div v-memo="[item.id, item.selected]">
  {{ item.name }}
</div>
```

适合列表中部分稳定内容。

### 13.3 KeepAlive

`KeepAlive` 可以缓存组件实例。

适合：

- 页面切换保留状态。
- 复杂表单。
- 列表返回详情后保留滚动位置。

注意：

- 缓存过多会增加内存占用。
- 需要合理设置 include、exclude、max。

## 14. 数据请求优化

### 14.1 请求合并

多个接口可以合并时，减少请求数量。

适合：

- 首屏多个小接口。
- 多个模块都依赖相同基础信息。
- 页面初始化配置。

注意：

- 合并接口会增加后端复杂度。
- 不应把所有接口都粗暴合并。
- 要考虑缓存和复用。

### 14.2 并发请求

互不依赖的请求应并发执行。

```ts
const [user, orders, messages] = await Promise.all([
  fetchUser(),
  fetchOrders(),
  fetchMessages()
])
```

相互依赖的请求才串行执行。

### 14.3 请求取消

页面切换或搜索条件变化时，应取消过期请求。

```ts
const controller = new AbortController()

fetch('/api/search', {
  signal: controller.signal
})

controller.abort()
```

适合：

- 搜索建议。
- 路由切换。
- 表格筛选。
- 用户快速切换条件。

### 14.4 请求缓存

同一数据不应重复请求。

常见方案：

- 内存缓存。
- HTTP 缓存。
- Service Worker 缓存。
- TanStack Query/SWR/Vue Query。

服务端状态库通常提供：

- 缓存。
- 重新请求。
- 过期时间。
- 重试。
- 乐观更新。
- 请求去重。

### 14.5 乐观更新

乐观更新是在接口成功前先更新 UI，让用户感知更快。

适合：

- 点赞。
- 收藏。
- 开关状态。
- 简单编辑。

注意：

- 失败时需要回滚。
- 不能用于高风险操作，如支付、资金、库存关键链路。

## 15. 构建性能优化

### 15.1 开发构建速度

影响开发体验的因素：

- 冷启动速度。
- HMR 速度。
- TypeScript 检查速度。
- 依赖预构建速度。
- 插件耗时。

优化方式：

- 使用 Vite。
- 使用 esbuild/SWC。
- 减少启动期插件。
- 优化依赖预构建。
- 开启缓存。
- 拆分大型项目。
- 使用 Monorepo 增量构建。

### 15.2 生产构建速度

优化方式：

- 构建缓存。
- 并行压缩。
- 减少无用插件。
- 使用更快压缩器。
- 分离类型检查和打包。
- CI 缓存 node_modules 或 pnpm store。

### 15.3 Bundle 分析

常见工具：

- rollup-plugin-visualizer。
- webpack-bundle-analyzer。
- source-map-explorer。

重点观察：

- 哪些依赖最大。
- 是否存在重复依赖。
- 是否把低频模块打入首屏包。
- Tree Shaking 是否生效。
- polyfill 是否过多。

## 16. 内存优化

### 16.1 常见内存泄漏

前端常见内存泄漏来源：

- 定时器未清理。
- 事件监听未移除。
- WebSocket 未关闭。
- 大对象被闭包引用。
- 组件卸载后异步回调仍更新状态。
- 缓存无限增长。
- 图表、地图实例未销毁。

### 16.2 清理副作用

React 示例：

```tsx
useEffect(() => {
  const timer = window.setInterval(() => {}, 1000)

  return () => {
    window.clearInterval(timer)
  }
}, [])
```

Vue 示例：

```ts
onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
```

### 16.3 内存分析工具

Chrome DevTools Memory 可以分析：

- Heap Snapshot。
- Allocation instrumentation。
- Detached DOM。
- 对象保留路径。

适合排查：

- 页面越用越卡。
- 切换页面后内存不降。
- 大量 DOM 没释放。
- 图表或编辑器销毁不完整。

## 17. Service Worker 与 PWA

### 17.1 Service Worker

Service Worker 是运行在浏览器后台的脚本，可以拦截请求、管理缓存、实现离线能力。

适合：

- 静态资源缓存。
- 离线访问。
- 弱网优化。
- PWA。
- 后台同步。

### 17.2 缓存策略

常见策略：

- Cache First：优先缓存。
- Network First：优先网络。
- Stale While Revalidate：先用缓存，同时后台更新。
- Network Only：只走网络。
- Cache Only：只走缓存。

选择建议：

- 静态资源适合 Cache First。
- 接口数据根据实时性选择 Network First 或 Stale While Revalidate。
- HTML 需要谨慎缓存，避免版本错乱。

### 17.3 PWA 注意事项

PWA 可以提升体验，但也会增加复杂度。

注意：

- Service Worker 更新机制复杂。
- 缓存策略错误会导致用户一直看到旧版本。
- 需要设计版本更新提示。
- 需要监控缓存命中和异常。

## 18. SSR 和 SSG 性能优化

### 18.1 SSR 性能价值

SSR 可以让服务端先生成 HTML，提升首屏内容可见速度和 SEO。

适合：

- 官网。
- 内容站。
- 电商详情页。
- 需要 SEO 的页面。

优化点：

- 服务端渲染缓存。
- 数据请求并发。
- 流式渲染。
- 边缘渲染。
- 减少 hydration 成本。

### 18.2 Hydration 优化

Hydration 是客户端接管服务端 HTML 的过程。

常见问题：

- 客户端 JS 过大。
- 页面虽然可见但不可交互。
- hydration 阻塞主线程。

优化方向：

- 减少客户端组件。
- 组件懒 hydration。
- 使用 Server Components。
- 拆分交互区域。
- 避免把纯展示内容都变成客户端组件。

### 18.3 SSG 优化

SSG 构建阶段生成静态 HTML。

优势：

- 访问速度快。
- CDN 友好。
- 服务端压力小。

注意：

- 数据更新频率。
- 增量构建。
- 缓存失效。
- 构建时间。

## 19. 前端监控与持续治理

### 19.1 实验室数据与真实用户数据

实验室数据：

- Lighthouse。
- WebPageTest。
- 本地 Performance。

真实用户数据：

- RUM。
- Web Vitals 上报。
- 真实设备、真实网络、真实地区数据。

二者区别：

- 实验室数据适合发现问题。
- 真实用户数据适合评估线上体验。

成熟团队需要两者结合。

### 19.2 Web Vitals 上报

需要上报：

- FCP。
- LCP。
- CLS。
- INP。
- TTFB。

同时记录维度：

- 页面路由。
- 用户设备。
- 网络类型。
- 浏览器。
- 地区。
- 版本号。
- 是否命中缓存。

### 19.3 错误和性能关联

性能问题常常和错误有关。

例如：

- 资源 404 导致页面白屏。
- 接口 500 导致首屏空白。
- JS 错误导致 hydration 失败。
- CDN 缓存错误导致旧资源加载。

监控系统应能关联：

- 错误日志。
- 性能指标。
- 用户行为。
- 版本发布。
- Source Map。

### 19.4 性能预算

性能预算是对性能指标设置上限。

示例：

- 首屏 JS 不超过 200KB gzip。
- LCP 小于 2.5s。
- INP 小于 200ms。
- 单个路由 chunk 不超过 300KB。
- 图片不超过指定尺寸。

性能预算可以接入 CI，在构建产物超标时提醒或阻断发布。

## 20. 常见业务场景优化

### 20.1 后台管理系统

常见问题：

- 首屏加载大量路由。
- UI 组件库体积大。
- 表格数据量大。
- 表单复杂。
- 权限菜单计算复杂。

优化建议：

- 路由懒加载。
- 权限路由按需生成。
- 表格虚拟滚动。
- 大表单拆分。
- UI 库按需引入。
- 首屏只加载必要模块。

### 20.2 电商页面

常见问题：

- 图片多。
- 首屏 Banner 大。
- 商品列表长。
- 接口多。
- 转化路径对性能敏感。

优化建议：

- 图片 CDN 裁剪。
- WebP/AVIF。
- 首屏图片 preload。
- 商品图懒加载。
- 列表分页或虚拟列表。
- 接口缓存。
- SSR/SSG。

### 20.3 数据可视化大屏

常见问题：

- 图表多。
- 数据刷新频繁。
- Canvas/WebGL 消耗高。
- 动画多。
- 长时间运行内存增长。

优化建议：

- 控制刷新频率。
- 图表实例复用。
- 销毁不用图表。
- 数据增量更新。
- 使用 requestAnimationFrame。
- 避免同时渲染过多高成本图表。

### 20.4 移动端 H5

常见问题：

- 弱网。
- 低端设备。
- WebView 差异。
- 首屏白屏敏感。
- 图片和 JS 体积影响明显。

优化建议：

- 减少首屏 JS。
- 图片压缩和懒加载。
- 骨架屏。
- 离线包。
- CDN。
- 关键接口优化。
- 避免复杂动画和长任务。

## 21. 性能优化检查清单

### 21.1 加载阶段

- HTML 是否过大。
- 首屏 JS 是否过大。
- CSS 是否阻塞。
- 图片是否压缩。
- 字体是否影响渲染。
- CDN 是否命中。
- HTTP 缓存是否正确。
- 是否存在多余重定向。
- 是否有资源 404。

### 21.2 构建阶段

- 是否开启压缩。
- 是否开启 Tree Shaking。
- 是否做路由懒加载。
- 是否分析 Bundle。
- 是否存在重复依赖。
- 是否有低频模块进入首屏包。
- 是否正确拆分 vendor。
- 是否移除无用 polyfill。

### 21.3 运行时阶段

- 是否存在长任务。
- 是否频繁重排。
- 是否大量 DOM。
- 是否大列表未虚拟化。
- 是否事件处理过重。
- 是否状态更新范围过大。
- 是否有内存泄漏。
- 是否动画使用高成本属性。

### 21.4 监控阶段

- 是否采集 Web Vitals。
- 是否采集 JS 错误。
- 是否采集资源错误。
- 是否上传 Source Map。
- 是否关联版本号。
- 是否有性能告警。
- 是否有性能预算。
- 是否能按页面和设备分析。

## 22. 学习路线建议

推荐学习顺序：

1. 理解浏览器渲染流程。
2. 掌握 Core Web Vitals 指标。
3. 熟练使用 DevTools Performance 和 Network。
4. 学习 HTTP 缓存和 CDN。
5. 学习图片、字体、CSS 优化。
6. 学习代码分割、Tree Shaking、Bundle 分析。
7. 学习 JavaScript 主线程和长任务优化。
8. 学习 React/Vue 框架层性能优化。
9. 学习虚拟列表、Web Worker、requestAnimationFrame。
10. 学习前端监控和 Web Vitals 上报。
11. 学习性能预算和 CI 性能治理。
12. 在真实项目中建立持续优化机制。

## 23. 总结

前端性能优化是一套系统工程，而不是几个零散技巧。它既包括资源加载、网络缓存、图片字体、构建产物，也包括 JavaScript 执行、浏览器渲染、框架更新、数据请求、内存管理和线上监控。

高级前端工程师需要具备完整的性能分析能力：先通过指标和工具定位瓶颈，再针对关键路径优化，并通过真实用户监控验证效果。真正成熟的性能优化，不是一次性把页面调快，而是建立性能预算、监控告警、工程规范和持续治理机制，让项目在长期迭代中仍然保持稳定、快速、可用。
