import { useState, useEffect } from 'react'

/**
 * ============================================================================
 * 【自定义 Hook: useLocalStorage】
 * ============================================================================
 *
 * 【知识点解析：0-1 学习 React】
 *
 * 1. 什么是自定义 Hook (Custom Hook)？
 *    - React 允许我们将组件中可复用的【状态逻辑】（不仅是普通函数，而是包含 useState/useEffect 等 Hook 的逻辑）
 *      封装成独立的函数，这就是自定义 Hook。
 *
 * 2. 为什么 Hook 必须以 "use" 开头？
 *    - 这是 React 的【Hook 规则 (Rules of Hooks)】强制要求的约定。
 *    - 只有以 "use" 开头的函数，React 的 Linter 插件才会识别并检查它内部是否遵守了 Hook 的调用规则：
 *      a. 只能在 React 函数组件或其它自定义 Hook 的顶层调用。
 *      b. 不能在循环、条件判断（if-else）或嵌套函数中调用，以确保 React 每次渲染时 Hook 的执行顺序一致。
 *
 * 3. 【React 核心概念：useState 的惰性初始化 (Lazy Initialization)】
 *    - 正常情况下，`useState(initialValue)` 中的初始值在每次组件重新渲染时都会被计算一遍。
 *    - 如果初始值需要经过复杂的计算（例如读取和解析本地 localStorage，这是磁盘 I/O 操作，性能开销大），
 *      我们可以传入一个【初始化函数】：`useState(() => { ... })`。
 *    - 这个函数**只会在组件首次挂载 (Mount) 时执行一次**，后续重新渲染时会被忽略，从而极大地优化了性能。
 *
 * 4. React 状态更新的高级用法：函数式更新 (Functional Updates)
 *    - React 的状态设置函数（如 `setCount`）不仅能接收新值，还能接收一个【回调函数】：`setCount(prevCount => prevCount + 1)`。
 *    - 在下面的 `setValue` 中，我们通过 `value instanceof Function` 判断用户传入的是新值还是回调函数。
 *      如果是回调函数，则把当前最新的 `storedValue` 传进去计算出新状态。
 *
 * 5. TypeScript 的 `as const`（只读元组断言）：
 *    - React 的 `useState` 返回的是一个包含两项的数组：`[value, setValue]`。
 *    - 默认情况下，TypeScript 会把返回数组推断为普通数组 `(T | Function)[]`。
 *    - 加上 `as const` 后，TS 会将其锁定为精确的【只读元组类型】：`readonly [T, (value: T) => void]`，
 *      这样外部解构时，第一项就会被正确识别为状态数据，第二项被正确识别为更新函数。
 */
export function useLocalStorage<T>(key: string, initialValue: T) {
  // 使用惰性初始化：只在组件初次挂载时读取一次本地存储
  const [storedValue, setStoredValue] = useState<T>(() => {
    try {
      const item = window.localStorage.getItem(key)
      // 如果本地存在值，使用 JSON.parse 反序列化；否则使用传入的初始值
      return item ? JSON.parse(item) : initialValue
    } catch (error) {
      console.error('读取 localStorage 出错:', error)
      return initialValue
    }
  })

  // 封装更新状态并同步到 localStorage 的方法
  const setValue = (value: T | ((val: T) => T)) => {
    try {
      // 兼容函数式更新：若 value 是函数，执行它以获取最新的状态值
      const valueToStore = value instanceof Function ? value(storedValue) : value
      // 1. 更新 React 的响应式状态，触发视图重新渲染
      setStoredValue(valueToStore)
      // 2. 同步写入浏览器的本地物理存储进行持久化
      window.localStorage.setItem(key, JSON.stringify(valueToStore))
    } catch (error) {
      console.error('写入 localStorage 出错:', error)
    }
  }

  // 返回状态和设置函数，并断言为元组
  return [storedValue, setValue] as const
}