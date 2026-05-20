/**
 * ============================================================================
 * 文件：vite.config.ts — Vite 构建工具的配置文件
 * ============================================================================
 *
 * 【文件用途】
 * 配置 Vite 开发服务器、构建选项、插件、路径别名、CSS 预处理器等。
 * Vite 是下一代前端构建工具，由 Vue.js 作者尤雨溪开发。
 *
 * 【在项目中的位置】
 * frontend/vite.config.ts
 * 位于前端项目根目录，运行 `npm run dev` 或 `npm run build` 时 Vite 会自动读取此文件。
 *
 * 【相关技术知识 — Vite 构建原理】
 *
 * 【为什么 Vite 比 Webpack 快？】
 *
 * 传统工具（如 Webpack）的开发模式：
 *   1. 启动时扫描所有文件，构建完整的依赖图
 *   2. 将所有模块打包成一个或多个 bundle
 *   3. 启动开发服务器
 *   → 项目越大，启动越慢（可能需要几十秒甚至几分钟）
 *
 * Vite 的开发模式：
 *   1. 启动时几乎不做任何打包（"无打包"模式）
 *   2. 利用浏览器原生 ES Module 支持，按需编译
 *   3. 当浏览器请求某个模块时，Vite 才编译并返回该模块
 *   → 无论项目多大，启动都很快（通常不到 1 秒）
 *
 * Vite 的生产构建：
 *   1. 使用 Rollup 进行打包（Rollup 的 tree-shaking 效果更好）
 *   2. 自动代码分割、CSS 提取、资源压缩
 *   3. 支持多种优化策略（如手动分包、压缩等）
 *
 * 【Vite 核心特性】
 * - 极快的冷启动（利用浏览器原生 ESM）
 * - 即时的模块热替换（HMR，修改代码后页面局部更新，不刷新整个页面）
 * - 开箱即用的 TypeScript 支持（编译但不进行类型检查）
 * - 丰富的插件生态（兼容 Rollup 插件）
 * ============================================================================
 */

// ============================================================================
// 导入依赖
// ============================================================================

/**
 * defineConfig — Vite 提供的配置辅助函数。
 *
 * 【作用】
 * 提供类型提示和智能补全，让开发者在编写配置时获得完整的 TypeScript 类型支持。
 * 如果不使用 defineConfig 包裹，配置对象也能正常工作，但没有类型提示。
 *
 * 【两种写法】
 * // 对象式配置
 * export default defineConfig({ ... })
 *
 * // 函数式配置（可以根据命令行参数动态配置）
 * export default defineConfig(({ command, mode }) => {
 *   if (command === 'build') { ... }
 *   return { ... }
 * })
 */
import { defineConfig } from 'vite'

/**
 * @vitejs/plugin-vue — Vite 的 Vue 3 官方插件。
 *
 * 【作用】
 * 提供对 .vue 单文件组件（SFC）的编译支持。
 * 没有这个插件，Vite 无法识别和编译 .vue 文件。
 *
 * 【它做了什么？】
 * 当 Vite 遇到 .vue 文件时，这个插件会：
 *   1. 解析 <template> → 编译为 JavaScript render 函数
 *   2. 解析 <script> → 保留或编译为 ES Module
 *   3. 解析 <style> → 编译为 CSS 并注入到页面
 *   4. 组装为一个标准的 ES Module 导出
 */
import vue from '@vitejs/plugin-vue'

/**
 * resolve — Node.js 的 path 模块方法，用于拼接文件路径。
 *
 * 【为什么需要 resolve？】
 * 因为不同操作系统的路径分隔符不同（Windows 用 \，Linux/Mac 用 /），
 * 使用 resolve 可以生成跨平台的正确路径。
 *
 * __dirname 是 Node.js 的全局变量，表示当前文件所在的目录的绝对路径。
 */
import { resolve } from 'path'

/**
 * unplugin-auto-import — 自动导入 API 的 Vite 插件。
 *
 * 【解决的问题】
 * 在 Vue 3 的 Composition API 中，经常需要手动导入 ref、reactive、computed 等：
 *   import { ref, reactive, computed, watch } from 'vue'
 * 每个文件都要写一遍，非常繁琐。
 *
 * 这个插件会自动在编译时添加这些 import 语句，让你直接使用而无需手动导入：
 *   const count = ref(0)  // 不需要 import { ref } from 'vue'
 *
 * 支持自动导入的库：
 *   - vue：ref、reactive、computed、watch、onMounted 等
 *   - vue-router：useRouter、useRoute、onBeforeRouteLeave 等
 *   - pinia：defineStore、storeToRefs 等
 */
import AutoImport from 'unplugin-auto-import/vite'

/**
 * unplugin-vue-components — 自动注册组件的 Vite 插件。
 *
 * 【解决的问题】
 * 使用 Element Plus 时，通常需要手动导入并注册每个组件：
 *   import { ElButton, ElInput, ElTable } from 'element-plus'
 *
 * 这个插件会自动扫描模板中使用的组件，按需导入，无需手动注册。
 * 同时配合 ElementPlusResolver 解析器，自动解析 Element Plus 组件。
 *
 * 【好处】
 * - 不需要全量导入 Element Plus（减小打包体积）
 * - 不需要在每个组件中手动 import
 * - 编译时处理，不影响运行时性能
 */
import Components from 'unplugin-vue-components/vite'

/**
 * ElementPlusResolver — Element Plus 组件解析器。
 * 配合 AutoImport 和 Components 插件使用，按需自动导入 Element Plus 的组件和 API。
 */
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

/**
 * unplugin-icons — 图标按需自动导入插件。
 *
 * 【解决的问题】
 * 图标库通常包含数千个图标，全量导入会极大增加打包体积。
 * 这个插件只在模板中使用到某个图标时才导入对应的图标组件。
 *
 * 【使用方式】
 * 在模板中直接使用图标名作为组件名：
 *   <IconEpEdit />       ← 自动导入 @iconify-json/ep 中的 Edit 图标
 *   <IconEpDelete />     ← 自动导入 Delete 图标
 */
import Icons from 'unplugin-icons/vite'

/**
 * IconsResolver — 图标解析器。
 * 配合 AutoImport 和 Components 插件，自动解析图标组件名。
 */
import IconsResolver from 'unplugin-icons/resolver'

/**
 * vite-plugin-compression — 静态资源压缩插件。
 *
 * 【解决的问题】
 * 在生产构建时，将静态资源（JS、CSS、HTML）额外生成一份 gzip 格式的压缩文件。
 * 如果服务器启用了 gzip 传输，浏览器可以直接下载压缩版本，大幅减少传输体积。
 *
 * 【效果】
 * 通常 gzip 可以将文本资源压缩 60%-80%：
 *   原始：app.js 500KB → 压缩后：app.js.gz 150KB
 */
import viteCompression from 'vite-plugin-compression'

// ============================================================================
// 导出 Vite 配置
// ============================================================================

export default defineConfig({

  // ==========================================================================
  // plugins — Vite 插件数组
  // ==========================================================================
  // 插件是 Vite 的核心扩展机制，类似于 Webpack 的 loader 和 plugin 的结合。
  // 插件可以在构建的不同阶段（启动、编译、打包、输出）介入并修改行为。
  // ==========================================================================

  plugins: [
    /**
     * Vue 3 插件 — 必须！提供 .vue 文件的编译支持。
     * 几乎所有 Vue 3 + Vite 项目都需要这个插件。
     */
    vue(),

    /**
     * AutoImport — 自动导入 API 插件配置。
     *
     * 配置项说明：
     * - imports: 指定需要自动导入 API 的库列表
     *   - 'vue' → 自动导入 ref、reactive、computed、watch 等
     *   - 'vue-router' → 自动导入 useRouter、useRoute 等
     *   - 'pinia' → 自动导入 defineStore、storeToRefs 等
     *
     * - resolvers: 解析器列表
     *   - ElementPlusResolver() → 自动导入 Element Plus 的 API（如 ElMessage）
     *   - IconsResolver({ prefix: 'Icon' }) → 自动导入图标
     *
     * - dts: 生成 TypeScript 类型声明文件的路径
     *   插件会自动生成 src/auto-imports.d.ts 文件，
     *   让 TypeScript 知道自动导入的 API 的类型信息，避免 IDE 报错。
     */
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia'],
      resolvers: [
        ElementPlusResolver(),
        IconsResolver({ prefix: 'Icon' }),
      ],
      dts: 'src/auto-imports.d.ts',
    }),

    /**
     * Components — 自动注册组件插件配置。
     *
     * 配置项说明：
     * - resolvers: 解析器列表
     *   - ElementPlusResolver() → 自动注册 Element Plus 组件（如 <el-button>）
     *   - IconsResolver({ enabledCollections: ['ep'] }) → 自动注册 Element Plus 图标
     *     'ep' 表示 @iconify-json/ep 包（Element Plus 图标集）
     *
     * - dts: 生成组件类型声明文件的路径
     *   插件会自动生成 src/components.d.ts 文件，
     *   让 TypeScript 知道自动注册的组件的类型信息。
     *
     * 【使用效果】
     * 在 .vue 文件中可以直接使用：
     *   <el-button type="primary">按钮</el-button>  ← 无需 import
     *   <IconEpEdit />                                ← 无需 import
     */
    Components({
      resolvers: [
        ElementPlusResolver(),
        IconsResolver({ enabledCollections: ['ep'] }),
      ],
      dts: 'src/components.d.ts',
    }),

    /**
     * Icons — 图标插件配置。
     *
     * - autoInstall: true → 当使用到某个图标集的图标时，
     *   自动安装对应的图标集 npm 包（如 @iconify-json/ep）。
     *   这样开发者不需要手动安装每个图标集。
     *
     * 【图标使用示例】
     * <template>
     *   <IconEpEdit />       ← Element Plus 的编辑图标
     *   <IconEpDelete />     ← Element Plus 的删除图标
     *   <IconEpSearch />     ← Element Plus 的搜索图标
     * </template>
     */
    Icons({
      autoInstall: true,
    }),

    /**
     * viteCompression — Gzip 压缩插件配置。
     *
     * - verbose: true → 在控制台输出压缩日志，方便查看压缩效果
     * - disable: false → 启用压缩（设为 true 可在开发时临时禁用）
     * - threshold: 10240 → 只压缩大于 10KB (10240 bytes) 的文件
     *   （小文件压缩收益不大，反而增加请求复杂度）
     * - algorithm: 'gzip' → 使用 gzip 压缩算法（最通用的压缩格式）
     * - ext: '.gz' → 压缩后的文件扩展名
     *
     * 【构建后的文件对比】
     * dist/assets/
     *   vue-vendor-abc123.js      ← 原始文件
     *   vue-vendor-abc123.js.gz   ← 压缩文件（小 60-80%）
     *
     * 【服务器配置】
     * 需要配置 Nginx/Apache 启用 gzip_static：
     *   Nginx: gzip_static on;
     *   这样 Nginx 会优先发送 .gz 文件，浏览器自动解压。
     */
    viteCompression({
      verbose: true,
      disable: false,
      threshold: 10240,
      algorithm: 'gzip',
      ext: '.gz',
    }),
  ],

  // ==========================================================================
  // resolve — 模块解析配置
  // ==========================================================================

  /**
   * alias — 路径别名配置。
   *
   * 【解决的问题】
   * 在导入模块时，避免使用冗长的相对路径：
   *   // 不使用别名（繁琐、容易出错）
   *   import { useUserStore } from '../../../stores/user'
   *
   *   // 使用别名（简洁、直观）
   *   import { useUserStore } from '@/stores/user'
   *
   * 【配置说明】
   * '@' 被映射到项目的 src 目录。
   * resolve(__dirname, './src') 会生成 src 目录的绝对路径。
   *
   * 【注意】
   * 这里配置的别名需要与 tsconfig.json 中的 paths 配置保持一致，
   * 否则 TypeScript 编译器会报"找不到模块"的错误。
   */
  resolve: {
    alias: {
      '@': resolve(__dirname, './src')
    }
  },

  // ==========================================================================
  // server — 开发服务器配置
  // ==========================================================================

  /**
   * 配置 Vite 的开发服务器（Dev Server）。
   * 运行 `npm run dev` 时会启动此服务器。
   */
  server: {
    /**
     * port: 5173 → 开发服务器监听的端口号。
     * 访问 http://localhost:5173 即可打开开发中的应用。
     *
     * 【为什么是 5173？】
     * 5173 在 T9 键盘上对应 "VITE"，是 Vite 的默认端口。
     * 如果该端口被占用，Vite 会自动尝试下一个可用端口（5174、5175...）。
     */
    port: 5173,

    /**
     * proxy — 开发服务器代理配置。
     *
     * 【解决的问题 — 跨域问题】
     * 前端运行在 localhost:5173（Vite），后端运行在 localhost:8080（Spring Boot）。
     * 浏览器的同源策略（Same-Origin Policy）会阻止前端直接请求后端接口（跨域）。
     *
     * 代理的工作原理：
     *   浏览器 → Vite Dev Server (localhost:5173) → Spring Boot (localhost:8080)
     *                         ↑
     *                    服务器端请求不受同源策略限制
     *
     * 【配置说明】
     * - '/api'：匹配所有以 /api 开头的请求
     * - target: 'http://localhost:8080' → 将请求转发到后端服务器
     * - changeOrigin: true → 修改请求头中的 Host 为目标地址，
     *   避免后端服务器因为 Host 不匹配而拒绝请求
     *
     * 【请求示例】
     * 前端请求：GET http://localhost:5173/api/users
     * Vite 代理：GET http://localhost:8080/api/users
     * 后端响应 → Vite 转发 → 前端收到响应
     *
     * 【注意】
     * 代理只在开发模式下生效。生产环境中需要通过 Nginx 等反向代理解决跨域。
     */
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },

  // ==========================================================================
  // css — CSS 相关配置
  // ==========================================================================

  /**
   * preprocessorOptions — CSS 预处理器选项。
   *
   * 【SCSS 全局变量注入】
   * additionalData 配置会在每个 SCSS 文件编译前自动注入一段代码。
   * 这样在每个 .vue 文件的 <style lang="scss"> 中都可以直接使用
   * variables.scss 中定义的变量、mixin 等，无需手动导入。
   *
   * 【示例】
   * // variables.scss 中定义了：
   * $primary-color: #409eff;
   *
   * // 在任何组件的 <style lang="scss"> 中可以直接使用：
   * .btn { color: $primary-color; }  // 无需 @import '@/styles/variables.scss'
     *
   * 【注意】
   * additionalData 中的代码会被注入到每个 SCSS 文件的开头，
   * 所以不要包含实际生成 CSS 的代码（如选择器），只放变量和 mixin 定义。
   */
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@use "@/styles/variables.scss" as *;`
      }
    }
  },

  // ==========================================================================
  // build — 生产构建配置
  // ==========================================================================

  /**
   * 配置生产环境的打包行为。
   * 运行 `npm run build` 时使用这些配置。
   */
  build: {
    /**
     * rollupOptions — Rollup 打包选项。
     *
     * Vite 的生产构建使用 Rollup 作为打包器。
     * Rollup 以其出色的 tree-shaking（摇树优化）能力著称，
     * 可以自动移除代码中未使用的导出，减小打包体积。
     */
    rollupOptions: {
      /**
       * output — 输出配置。
       */
      output: {
        /**
         * manualChunks — 手动代码分割策略。
         *
         * 【为什么要代码分割？】
         * 如果所有依赖都打包成一个 JS 文件，体积可能达到数 MB，
         * 浏览器需要完整下载后才能执行，首屏加载非常慢。
         *
         * 代码分割将不同的依赖拆分到不同的文件（chunk）中，
         * 浏览器可以并行下载，且当某个依赖不变时可以利用浏览器缓存。
         *
         * 【本项目的分割策略】
         * ┌────────────────────┬───────────────────────────────────┐
         * │ chunk 名称         │ 包含的内容                        │
         * ├────────────────────┼───────────────────────────────────┤
         * │ vue-vendor         │ Vue、Pinia、Vue Router            │
         * │ element-plus       │ Element Plus UI 组件库            │
         * │ echarts            │ ECharts 图表库                    │
         * │ vendor             │ 其他所有第三方依赖                │
         * └────────────────────┴───────────────────────────────────┘
         *
         * 【分割思路】
         * - Vue 核心（变化少）单独一个 chunk，利用浏览器缓存
         * - Element Plus（体积大）单独一个 chunk，按需加载
         * - ECharts（体积大）单独一个 chunk，只在看板页需要
         * - 其他第三方库一个 chunk
         * - 业务代码由 Vite 自动按路由分割（懒加载）
         *
         * 【参数 id 说明】
         * id 是模块的绝对路径，通过 id.includes() 判断模块属于哪个库。
         * 例如 id 为 'node_modules/vue/dist/vue.runtime.esm-bundler.js'
         * 则 id.includes('vue') 为 true，归入 'vue-vendor' chunk。
         */
        manualChunks(id) {
          // 只对 node_modules 中的第三方依赖进行分包
          if (id.includes('node_modules')) {
            // Vue 核心生态：框架本身 + 状态管理 + 路由
            if (id.includes('vue') || id.includes('pinia') || id.includes('vue-router')) {
              return 'vue-vendor'
            }
            // Element Plus UI 组件库（通常体积较大，单独分包）
            if (id.includes('element-plus')) {
              return 'element-plus'
            }
            // ECharts 图表库（只在看板页使用，单独分包）
            if (id.includes('echarts')) {
              return 'echarts'
            }
            // 其他第三方依赖（如 dayjs、axios、sortablejs 等）
            return 'vendor'
          }
        }
      }
    }
  }
})
