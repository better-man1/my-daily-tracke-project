/**
 * ============================================================================
 * 【React 应用程序入口文件 (main.tsx)】
 * ============================================================================
 *
 * 【知识点解析：0-1 学习 React】
 *
 * 1. 为什么是 .tsx 扩展名？
 *    - 在 React 中，凡是包含 JSX（即类似于 HTML 的标签语法）且使用 TypeScript 的文件，
 *      都必须命名为 `.tsx` 结尾；纯逻辑或不包含标签的文件命名为 `.ts` 即可。
 *
 * 2. React 的虚拟 DOM 挂载机制：
 *    - 传统网页通过操作真实 DOM（文档对象模型）来更新页面，性能开销较大。
 *    - React 引入了【虚拟 DOM (Virtual DOM)】：用 JavaScript 对象模拟真实 DOM 结构。
 *    - 本文件中的 `createRoot` 就是将这套虚拟 DOM 树挂载到宿主 HTML 页面中的真实 DOM 节点上。
 */

// 从 React 核心库导入 StrictMode（严格模式）组件
import { StrictMode } from 'react'
// 从 react-dom/client 导入 createRoot 函数。react-dom 是 React 与浏览器 DOM 交互的桥梁
import { createRoot } from 'react-dom/client'
// 导入应用程序的根组件 App
import App from './App.tsx'

console.log('Starting React App...')

/**
 * 3. 页面挂载步骤详解：
 *    - `document.getElementById('root')`：获取 `index.html` 中预留的 `<div id="root"></div>` 节点。
 *    - `!`（非空断言）：这是 TypeScript 语法，告诉编译器该 DOM 节点一定存在，不用担心它为 null。
 *    - `createRoot(...)`：创建一个 React 的 Root 根对象，它接管了容器节点内部的所有内容。
 *    - `.render(...)`：将 `<StrictMode>` 包裹的 `<App />` 渲染到这个 Root 中。
 *
 * 4. 【React 核心概念：StrictMode 严格模式】
 *    - `<StrictMode>` 是一个辅助组件，它**不会**在浏览器中渲染任何真实的 HTML 元素。
 *    - 它的主要作用是在开发环境下（Development Mode）激活额外的检查和警告：
 *      a. 检测不安全的生命周期、过时的 API 弃用提示。
 *      b. 故意**双击运行渲染和副作用**（即某些生命周期/Effect 会被执行两次），以此来暴露组件中
 *         非纯函数（带副作用）的代码。如果在渲染期间修改了外部变量，在双重执行下很容易暴露问题。
 *      c. 注意：在生产环境（Production Build）中，严格模式的行为会自动关闭，不会执行两次。
 */
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)

