# 前端工程化技术知识点总结

## 1. 什么是前端工程化

前端工程化是指使用规范化、自动化、模块化、工具化的方式来组织和管理前端项目，使项目在开发、构建、测试、部署、协作和维护过程中更加高效、稳定、可持续。

早期前端开发主要围绕 HTML、CSS、JavaScript 文件直接编写和引入，项目规模较小时问题不明显。但随着业务复杂度提升，前端项目开始面临模块依赖、代码复用、多人协作、浏览器兼容、性能优化、自动部署、质量保障等问题。前端工程化正是为了解决这些问题而形成的一整套技术体系。

前端工程化的核心目标包括：

- 提升开发效率：通过脚手架、热更新、自动构建等工具减少重复劳动。
- 提升代码质量：通过规范、Lint、类型检查、测试等手段降低缺陷率。
- 提升可维护性：通过模块化、组件化、目录规范和架构分层降低维护成本。
- 提升交付效率：通过自动化构建、CI/CD、环境配置实现稳定发布。
- 提升运行性能：通过代码分割、压缩、缓存、资源优化提高页面加载速度。

## 2. 前端工程化包含的核心知识点

### 2.1 模块化

模块化是前端工程化的基础。它将复杂代码拆分为职责明确、可复用、可维护的小模块。

常见模块化规范包括：

- CommonJS：主要用于 Node.js 环境，使用 `require` 和 `module.exports`。
- AMD/CMD：早期浏览器端模块化方案，现在较少使用。
- ES Module：现代 JavaScript 官方模块规范，使用 `import` 和 `export`。

ES Module 是当前前端项目的主流模块化方式。Vite 正是基于浏览器原生 ES Module 能力来实现快速开发服务器的。

模块化带来的好处：

- 避免全局变量污染。
- 明确模块之间的依赖关系。
- 提高代码复用能力。
- 方便构建工具进行依赖分析、Tree Shaking 和代码分割。

### 2.2 组件化

组件化是现代前端框架的核心思想。它将页面拆分为多个独立、可复用的 UI 单元。

常见组件化框架包括：

- Vue
- React
- Angular
- Svelte

组件通常包含：

- 结构：HTML 或 JSX/模板。
- 样式：CSS、Less、Sass、CSS Modules 等。
- 行为：JavaScript 或 TypeScript 逻辑。
- 状态：组件内部状态或外部状态管理。

组件化的价值：

- 提高 UI 复用性。
- 降低复杂页面的维护难度。
- 方便多人协作开发。
- 有利于形成统一的设计系统和组件库。

### 2.3 包管理

包管理工具用于安装、升级、锁定和管理项目依赖。

常见工具包括：

- npm：Node.js 默认包管理工具。
- Yarn：强调稳定性和安装速度。
- pnpm：通过硬链接和内容寻址存储优化磁盘占用和安装速度。

重要文件：

- `package.json`：描述项目依赖、脚本、版本、入口等信息。
- `package-lock.json` / `yarn.lock` / `pnpm-lock.yaml`：锁定依赖版本，保证不同环境安装结果一致。

常见脚本：

```json
{
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview",
    "lint": "eslint src"
  }
}
```

包管理的工程化意义：

- 管理第三方依赖。
- 固定依赖版本，减少环境差异。
- 通过脚本统一开发、构建、测试命令。
- 支持 Monorepo、多包管理等复杂项目结构。

### 2.4 构建工具

构建工具负责将开发阶段的源码转换为生产环境可运行的静态资源。

构建过程通常包括：

- 模块解析。
- 代码转译。
- 语法降级。
- 样式处理。
- 资源处理。
- 代码压缩。
- Tree Shaking。
- 代码分割。
- 生成构建产物。

常见构建工具：

- Webpack
- Vite
- Rollup
- esbuild
- Parcel
- Rspack

其中 Webpack 和 Vite 是目前最常见、最值得重点掌握的两类工具。

### 2.5 编译与转译

现代前端代码通常使用新语法和扩展语言，例如 TypeScript、JSX、Vue SFC、Sass 等，这些代码需要经过编译或转译才能在浏览器中稳定运行。

常见工具：

- Babel：将新版 JavaScript 转换为兼容性更好的旧版 JavaScript。
- TypeScript Compiler：将 TypeScript 转换为 JavaScript，并进行类型检查。
- SWC：使用 Rust 编写的高性能 JavaScript/TypeScript 编译器。
- esbuild：使用 Go 编写的高性能构建和转译工具。

常见处理对象：

- TypeScript -> JavaScript
- JSX -> JavaScript
- Vue SFC -> JavaScript/CSS/模板渲染函数
- Sass/Less -> CSS
- 新版 JavaScript -> 兼容旧浏览器的 JavaScript

### 2.6 代码规范与质量控制

多人协作项目必须建立统一的代码规范。

常见工具：

- ESLint：检查 JavaScript/TypeScript 代码质量和风格问题。
- Prettier：自动格式化代码。
- Stylelint：检查 CSS、Less、Sass 等样式代码。
- EditorConfig：统一不同编辑器的基础格式。
- Husky：管理 Git Hooks。
- lint-staged：只对暂存区文件执行检查。
- Commitlint：规范 Git 提交信息。

典型流程：

1. 开发者提交代码。
2. Husky 触发 pre-commit 钩子。
3. lint-staged 对改动文件执行 ESLint 和 Prettier。
4. commit-msg 钩子通过 Commitlint 检查提交信息。
5. CI 阶段再次执行完整检查和测试。

### 2.7 CSS 工程化

CSS 工程化解决样式组织、复用、隔离和维护问题。

常见方案：

- CSS 预处理器：Sass、Less、Stylus。
- PostCSS：通过插件转换 CSS，例如自动添加浏览器前缀。
- CSS Modules：为类名生成局部作用域，避免样式冲突。
- CSS-in-JS：在 JavaScript 中组织样式，如 styled-components、emotion。
- Atomic CSS：原子化 CSS，如 Tailwind CSS、UnoCSS。

CSS 工程化关注点：

- 样式复用。
- 命名规范。
- 作用域隔离。
- 主题变量。
- 响应式布局。
- 暗色模式。
- 生产环境压缩和兼容性处理。

### 2.8 静态资源处理

前端项目中的图片、字体、音视频、JSON、SVG 等资源也需要工程化管理。

构建工具通常会处理：

- 小图片转 Base64。
- 图片文件 Hash 命名。
- 字体文件拷贝和路径重写。
- SVG 作为组件使用。
- 静态资源 CDN 前缀配置。
- 图片压缩和格式优化。

资源 Hash 的作用是配合浏览器缓存。当文件内容变化时，文件名 Hash 改变，浏览器会重新请求最新资源；如果文件未变化，则可以继续使用缓存。

### 2.9 环境变量与配置管理

前端项目通常需要区分不同环境：

- development：本地开发环境。
- test：测试环境。
- staging：预发布环境。
- production：生产环境。

常见配置内容：

- API 地址。
- CDN 地址。
- 是否开启 Mock。
- 是否开启调试工具。
- 构建产物路径。
- 代理配置。

Vite 中常见环境变量文件：

- `.env`
- `.env.development`
- `.env.production`
- `.env.local`

Vite 默认要求暴露给客户端的环境变量以 `VITE_` 开头，例如：

```env
VITE_API_BASE_URL=https://api.example.com
```

代码中使用：

```ts
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL
```

### 2.10 开发服务器与代理

开发服务器用于提升本地开发体验。

核心能力包括：

- 本地 HTTP 服务。
- 热更新。
- 错误提示。
- Source Map。
- 接口代理。
- 静态资源服务。

接口代理通常用于解决本地开发时的跨域问题。例如前端本地运行在 `http://localhost:5173`，后端运行在 `http://localhost:8080`，可以让开发服务器把 `/api` 请求代理到后端服务。

### 2.11 测试体系

测试是前端工程化中保障质量的重要部分。

常见测试类型：

- 单元测试：测试函数、组件、工具方法等最小单元。
- 组件测试：测试组件渲染和交互。
- 集成测试：测试多个模块协作。
- 端到端测试：模拟真实用户在浏览器中的操作流程。
- 视觉回归测试：比较页面截图，发现 UI 差异。

常见工具：

- Vitest
- Jest
- Testing Library
- Cypress
- Playwright
- Storybook

### 2.12 性能优化

前端工程化中的性能优化贯穿开发和构建全过程。

常见优化手段：

- Tree Shaking：移除未使用代码。
- Code Splitting：按需加载代码。
- Lazy Loading：懒加载路由、组件和图片。
- Bundle Analysis：分析构建产物体积。
- Minify：压缩 JavaScript、CSS、HTML。
- Gzip/Brotli：启用传输压缩。
- CDN：使用内容分发网络加速静态资源。
- Cache-Control：合理设置缓存策略。
- 图片优化：使用 WebP/AVIF、压缩、响应式图片。
- 预加载：使用 preload、prefetch 优化关键资源加载。

### 2.13 CI/CD 自动化

CI/CD 是前端工程化落地到团队协作和发布流程的关键。

CI 持续集成通常包括：

- 安装依赖。
- 代码检查。
- 类型检查。
- 单元测试。
- 构建验证。

CD 持续交付/部署通常包括：

- 构建生产产物。
- 上传静态资源到服务器或对象存储。
- 刷新 CDN。
- 部署到测试、预发或生产环境。
- 发布通知和回滚机制。

常见平台：

- GitHub Actions
- GitLab CI
- Jenkins
- Azure DevOps
- Vercel
- Netlify

### 2.14 Monorepo

Monorepo 是指在一个代码仓库中管理多个项目或多个包。

常见场景：

- 一个仓库同时管理后台系统、移动端 H5、组件库、工具库。
- 多个业务项目共享公共组件和工具函数。
- 大型团队统一管理多个前端应用。

常见工具：

- pnpm workspace
- Yarn workspace
- npm workspace
- Nx
- Turborepo
- Lerna

Monorepo 的优点：

- 方便代码共享。
- 统一依赖和规范。
- 原子化提交，跨项目改动更容易追踪。
- 统一构建、测试和发布流程。

挑战：

- 仓库体积变大。
- 权限和边界管理更复杂。
- 构建缓存和增量构建能力更重要。

## 3. Webpack 重点详解

### 3.1 Webpack 是什么

Webpack 是一个高度可配置的 JavaScript 应用静态模块打包工具。它会从入口文件开始，递归分析项目中的模块依赖关系，然后将 JavaScript、CSS、图片、字体等资源转换并打包成浏览器可加载的静态文件。

Webpack 的核心思想是：

> 一切资源皆模块。

这意味着 JavaScript 可以引入 CSS，CSS 可以引用图片，图片和字体也可以作为模块被构建工具统一管理。

### 3.2 Webpack 核心概念

#### Entry

Entry 表示构建入口。Webpack 从入口文件开始分析依赖图。

```js
module.exports = {
  entry: './src/main.js'
}
```

多入口配置：

```js
module.exports = {
  entry: {
    main: './src/main.js',
    admin: './src/admin.js'
  }
}
```

#### Output

Output 表示构建产物输出配置。

```js
const path = require('path')

module.exports = {
  output: {
    path: path.resolve(__dirname, 'dist'),
    filename: '[name].[contenthash].js',
    clean: true
  }
}
```

常见占位符：

- `[name]`：入口名称。
- `[hash]`：本次构建 Hash。
- `[chunkhash]`：Chunk 级别 Hash。
- `[contenthash]`：文件内容 Hash，适合长期缓存。

#### Loader

Webpack 默认只能理解 JavaScript 和 JSON。Loader 用于处理其他类型文件，或者转换 JavaScript 语法。

常见 Loader：

- `babel-loader`：使用 Babel 转译 JavaScript。
- `ts-loader`：处理 TypeScript。
- `css-loader`：解析 CSS 中的 `@import` 和 `url()`。
- `style-loader`：将 CSS 注入页面。
- `sass-loader`：将 Sass 编译为 CSS。
- `vue-loader`：处理 Vue 单文件组件。

示例：

```js
module.exports = {
  module: {
    rules: [
      {
        test: /\.css$/,
        use: ['style-loader', 'css-loader']
      },
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: 'babel-loader'
      }
    ]
  }
}
```

Loader 的执行顺序是从右到左、从下到上。例如：

```js
use: ['style-loader', 'css-loader', 'sass-loader']
```

实际执行顺序为：

1. `sass-loader` 将 Sass 转成 CSS。
2. `css-loader` 解析 CSS 依赖。
3. `style-loader` 将 CSS 注入页面。

#### Plugin

Plugin 用于扩展 Webpack 的构建能力。相比 Loader 只关注具体文件转换，Plugin 可以介入 Webpack 构建生命周期，完成更广泛的任务。

常见 Plugin：

- `HtmlWebpackPlugin`：自动生成 HTML 并注入构建产物。
- `MiniCssExtractPlugin`：将 CSS 抽离为独立文件。
- `DefinePlugin`：定义编译时常量。
- `CopyWebpackPlugin`：复制静态资源。
- `BundleAnalyzerPlugin`：分析产物体积。

示例：

```js
const HtmlWebpackPlugin = require('html-webpack-plugin')

module.exports = {
  plugins: [
    new HtmlWebpackPlugin({
      template: './public/index.html'
    })
  ]
}
```

#### Mode

Webpack 提供三种模式：

- `development`：开发模式，构建速度优先，默认开启有利于调试的配置。
- `production`：生产模式，默认开启压缩、Tree Shaking 等优化。
- `none`：不使用默认优化。

```js
module.exports = {
  mode: 'production'
}
```

#### DevServer

Webpack Dev Server 提供本地开发服务、自动刷新和热模块替换。

```js
module.exports = {
  devServer: {
    port: 3000,
    open: true,
    hot: true,
    proxy: [
      {
        context: ['/api'],
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    ]
  }
}
```

### 3.3 Webpack 构建流程

Webpack 的大致构建流程：

1. 读取配置文件。
2. 根据 Entry 确定入口模块。
3. 从入口开始递归解析依赖。
4. 使用 Loader 转换不同类型的模块。
5. 生成模块依赖图。
6. 根据依赖图生成 Chunk。
7. 使用 Plugin 在不同生命周期中扩展处理。
8. 输出最终构建产物。

理解这个流程有助于排查构建问题。例如 CSS 没有生效，可能是 Loader 配置问题；HTML 没有注入 JS，可能是 HtmlWebpackPlugin 配置问题；产物过大，可能需要分析 Chunk 和依赖。

### 3.4 Webpack 优化手段

#### Tree Shaking

Tree Shaking 用于删除未使用的代码。它依赖 ES Module 的静态结构。

需要注意：

- 尽量使用 ES Module。
- 避免模块存在不可预测的副作用。
- 正确配置 `package.json` 中的 `sideEffects`。

```json
{
  "sideEffects": false
}
```

如果 CSS 文件被误删，可以这样配置：

```json
{
  "sideEffects": ["*.css"]
}
```

#### Code Splitting

Code Splitting 用于拆分代码，避免所有代码打到一个大文件里。

常见方式：

- 多入口拆分。
- 动态 `import()` 拆分。
- `splitChunks` 抽离公共依赖。

动态导入示例：

```js
button.addEventListener('click', async () => {
  const module = await import('./dialog')
  module.openDialog()
})
```

`splitChunks` 示例：

```js
module.exports = {
  optimization: {
    splitChunks: {
      chunks: 'all'
    }
  }
}
```

#### 缓存优化

生产环境通常使用内容 Hash 文件名：

```js
module.exports = {
  output: {
    filename: 'js/[name].[contenthash].js'
  }
}
```

这样可以配合浏览器长期缓存：

- 文件内容未变，文件名不变，浏览器继续使用缓存。
- 文件内容变化，Hash 改变，浏览器请求新文件。

#### 构建速度优化

常见方式：

- 使用持久化缓存。
- 缩小 Loader 处理范围。
- 使用 SWC、esbuild 等高性能工具替代 Babel 的部分工作。
- 合理配置 Source Map。
- 减少不必要的 Plugin。
- 使用多进程或并行压缩。

示例：

```js
module.exports = {
  cache: {
    type: 'filesystem'
  }
}
```

### 3.5 Webpack 的优点和不足

优点：

- 生态成熟，插件和 Loader 丰富。
- 配置能力极强，适合复杂项目。
- 对各种资源和历史项目兼容性好。
- 在大型企业项目中使用广泛。

不足：

- 配置复杂，学习成本较高。
- 大型项目冷启动和重新构建可能较慢。
- 对现代浏览器原生 ESM 的利用不如 Vite 直接。
- 简单项目中可能显得笨重。

Webpack 适合：

- 对构建流程有复杂定制需求的项目。
- 历史包袱较重的项目。
- 大型企业级前端应用。
- 需要兼容复杂资源处理和老旧生态的项目。

## 4. Vite 重点详解

### 4.1 Vite 是什么

Vite 是新一代前端构建工具，由 Vue 作者尤雨溪发起。它的目标是提供更快的开发服务器启动速度、更快的热更新体验，以及相对简单的配置方式。

Vite 的名字来自法语，含义是“快速”。

Vite 主要由两部分组成：

- 开发阶段：基于浏览器原生 ES Module 提供开发服务器。
- 生产构建：基于 Rollup 打包生产环境代码。

### 4.2 Vite 为什么快

Webpack 在开发阶段通常需要先分析整个项目依赖图，并进行打包后再启动开发服务器。项目越大，启动和重新构建成本越高。

Vite 的思路不同：

1. 开发服务器启动时不预先打包整个应用源码。
2. 浏览器请求哪个模块，Vite 就按需转换哪个模块。
3. 第三方依赖会使用 esbuild 进行预构建。
4. 源码模块通过原生 ES Module 直接交给浏览器加载。

因此 Vite 在开发阶段通常具有非常快的冷启动速度。

### 4.3 Vite 核心能力

#### 原生 ES Module 开发服务

现代浏览器已经支持 ES Module：

```html
<script type="module" src="/src/main.ts"></script>
```

浏览器可以直接解析 `import` 语句，并按需请求依赖模块。

Vite 利用这一点，在开发阶段不把所有源码提前打成一个 Bundle，而是在请求时进行转换并返回给浏览器。

#### 依赖预构建

Vite 会在开发启动时使用 esbuild 对第三方依赖进行预构建。

依赖预构建的原因：

- 将 CommonJS 或 UMD 依赖转换为 ESM。
- 合并依赖内部的大量小模块，减少浏览器请求数量。
- 使用 esbuild 提升依赖处理速度。

预构建产物通常缓存在 `node_modules/.vite` 中。

#### 热模块替换 HMR

HMR 即 Hot Module Replacement，热模块替换。

Vite 的 HMR 基于 ESM 模块边界实现。当某个模块变化时，Vite 可以精确地让浏览器只更新相关模块，而不必刷新整个页面。

这带来的好处：

- 状态保留更好。
- 更新速度更快。
- 开发体验更流畅。

#### 生产构建

Vite 生产环境构建使用 Rollup。

Rollup 擅长处理 ES Module，产物干净，Tree Shaking 效果好，非常适合生产打包。

Vite 通过 Rollup 插件体系支持：

- 代码分割。
- CSS 抽离。
- 静态资源 Hash。
- Tree Shaking。
- 构建压缩。
- Library 模式。

### 4.4 Vite 基础配置

典型 `vite.config.ts`：

```ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/api/, '')
      }
    }
  },
  build: {
    outDir: 'dist',
    sourcemap: false
  }
})
```

React 项目示例：

```ts
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()]
})
```

### 4.5 Vite 常见配置项

#### plugins

用于配置插件，例如 Vue、React、Legacy、Mock、自动导入等。

```ts
plugins: [vue()]
```

#### server

开发服务器配置。

```ts
server: {
  host: '0.0.0.0',
  port: 5173,
  open: true,
  proxy: {
    '/api': 'http://localhost:8080'
  }
}
```

#### resolve.alias

配置路径别名。

```ts
import path from 'node:path'

resolve: {
  alias: {
    '@': path.resolve(__dirname, 'src')
  }
}
```

使用：

```ts
import Button from '@/components/Button.vue'
```

#### css

配置 CSS 预处理器、CSS Modules、PostCSS 等。

```ts
css: {
  preprocessorOptions: {
    scss: {
      additionalData: '@use "@/styles/variables.scss" as *;'
    }
  }
}
```

#### build

生产构建配置。

```ts
build: {
  outDir: 'dist',
  assetsDir: 'assets',
  sourcemap: false,
  minify: 'esbuild',
  rollupOptions: {
    output: {
      manualChunks: {
        vendor: ['vue']
      }
    }
  }
}
```

#### define

定义全局常量。

```ts
define: {
  __APP_VERSION__: JSON.stringify('1.0.0')
}
```

### 4.6 Vite 环境变量

Vite 使用 `.env` 文件管理环境变量。

示例：

```env
VITE_API_BASE_URL=https://api.example.com
VITE_APP_TITLE=Daily Project
```

代码中读取：

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL
```

常见内置变量：

- `import.meta.env.MODE`：当前模式。
- `import.meta.env.BASE_URL`：部署基础路径。
- `import.meta.env.PROD`：是否生产环境。
- `import.meta.env.DEV`：是否开发环境。
- `import.meta.env.SSR`：是否服务端渲染。

注意：只有以 `VITE_` 开头的变量会暴露给客户端代码。

### 4.7 Vite 插件机制

Vite 插件基于 Rollup 插件接口扩展，并额外提供了一些 Vite 专属钩子。

常见插件：

- `@vitejs/plugin-vue`
- `@vitejs/plugin-react`
- `vite-plugin-mock`
- `unplugin-auto-import`
- `unplugin-vue-components`
- `vite-plugin-compression`
- `@vitejs/plugin-legacy`

插件可以完成：

- 框架文件转换。
- 自动导入 API。
- 自动注册组件。
- Mock 接口。
- 压缩构建产物。
- 兼容旧浏览器。
- 自定义虚拟模块。

### 4.8 Vite 性能优化

开发阶段优化：

- 合理使用 `optimizeDeps.include` 预构建依赖。
- 使用 `optimizeDeps.exclude` 排除不需要预构建的依赖。
- 避免在启动阶段执行过重插件。
- 减少大型模块的全量导入。

示例：

```ts
export default defineConfig({
  optimizeDeps: {
    include: ['lodash-es'],
    exclude: ['some-esm-only-lib']
  }
})
```

生产阶段优化：

- 使用动态导入进行路由懒加载。
- 使用 `manualChunks` 拆分大型依赖。
- 开启 Gzip/Brotli 压缩。
- 分析打包产物。
- 控制第三方库体积。

示例：

```ts
build: {
  rollupOptions: {
    output: {
      manualChunks(id) {
        if (id.includes('node_modules')) {
          return 'vendor'
        }
      }
    }
  }
}
```

### 4.9 Vite 的优点和不足

优点：

- 开发服务器启动快。
- HMR 更新快。
- 配置相对简单。
- 默认支持 TypeScript、CSS Modules、PostCSS、静态资源处理。
- 与 Vue、React、Svelte 等框架集成方便。
- 生产构建基于 Rollup，产物质量较好。

不足：

- 对非常复杂的历史项目迁移可能有成本。
- 某些依赖如果不符合现代 ESM 规范，可能需要额外处理。
- 生产构建使用 Rollup，开发与生产机制不完全一致，极少数情况下需要分别排查。
- 在高度定制的企业构建场景中，可控性通常不如 Webpack 生态成熟。

Vite 适合：

- 新项目。
- Vue、React、Svelte 等现代框架项目。
- 追求开发体验和启动速度的项目。
- 中小型到大型现代前端应用。
- 组件库和工具库开发。

## 5. Vite 与 Webpack 对比

| 维度 | Vite | Webpack |
| --- | --- | --- |
| 开发模式 | 基于原生 ESM，按需转换源码 | 通常先构建依赖图并打包 |
| 冷启动速度 | 通常很快 | 项目越大启动越慢 |
| HMR | 基于 ESM 边界，更新快 | 依赖打包机制，复杂项目可能较慢 |
| 生产构建 | 默认使用 Rollup | 使用 Webpack 自身构建 |
| 配置复杂度 | 较低，默认能力较完整 | 较高，但灵活度强 |
| 插件生态 | 增长快，现代项目友好 | 非常成熟，覆盖面广 |
| 历史项目兼容 | 需要评估依赖和插件 | 兼容性通常更强 |
| 适用场景 | 新项目、现代框架、快速开发 | 复杂工程、深度定制、历史项目 |

简单理解：

- Vite 更像是面向现代浏览器和现代框架的新一代开发构建工具，强调快和简洁。
- Webpack 更像是高度成熟、能力全面、适合复杂场景的模块打包平台。

## 6. 如何选择 Vite 或 Webpack

优先选择 Vite 的情况：

- 新启动的 Vue、React、Svelte 项目。
- 团队希望降低配置成本。
- 项目希望获得更快的本地开发体验。
- 项目依赖相对现代，兼容 ESM 较好。
- 没有特别复杂的构建定制需求。

优先选择 Webpack 的情况：

- 项目已有成熟 Webpack 配置，迁移成本较高。
- 需要大量历史 Loader 或 Plugin。
- 构建流程高度定制。
- 需要兼容复杂老旧依赖。
- 企业内部已有基于 Webpack 的构建平台。

实际工作中不要只根据工具热度选型，而要结合：

- 项目历史包袱。
- 团队熟悉程度。
- 依赖生态。
- 构建定制需求。
- 浏览器兼容要求。
- CI/CD 和部署体系。

## 7. 前端工程化实践建议

### 7.1 新项目推荐基础配置

一个现代前端项目通常建议包含：

- Vite 作为构建工具。
- TypeScript 作为开发语言。
- ESLint + Prettier 作为代码规范工具。
- Vitest 作为单元测试工具。
- Playwright 作为端到端测试工具。
- pnpm 作为包管理工具。
- Husky + lint-staged + Commitlint 作为提交规范工具。
- GitHub Actions 或 GitLab CI 作为自动化流水线。

### 7.2 推荐目录结构

```text
src
├─ assets
├─ components
├─ composables
├─ hooks
├─ layouts
├─ pages
├─ router
├─ services
├─ stores
├─ styles
├─ types
├─ utils
└─ main.ts
```

目录职责说明：

- `assets`：图片、字体等静态资源。
- `components`：通用组件。
- `pages`：页面级组件。
- `router`：路由配置。
- `services`：接口请求。
- `stores`：状态管理。
- `styles`：全局样式、变量、主题。
- `types`：TypeScript 类型定义。
- `utils`：通用工具函数。

### 7.3 常见工程化检查清单

项目初始化阶段：

- 是否统一 Node.js 版本。
- 是否选择合适包管理器。
- 是否配置 TypeScript。
- 是否配置路径别名。
- 是否配置环境变量。
- 是否配置代码规范工具。

开发阶段：

- 是否支持热更新。
- 是否配置接口代理。
- 是否支持 Mock 数据。
- 是否有统一请求封装。
- 是否有错误处理机制。
- 是否有组件和状态管理规范。

构建阶段：

- 是否开启代码压缩。
- 是否开启 Tree Shaking。
- 是否合理拆包。
- 是否生成 Source Map。
- 是否分析产物体积。
- 是否处理静态资源 Hash。

发布阶段：

- 是否有 CI/CD 流水线。
- 是否区分测试、预发、生产环境。
- 是否支持回滚。
- 是否配置 CDN 和缓存策略。
- 是否有构建产物校验。

质量保障：

- 是否有 ESLint。
- 是否有 Prettier。
- 是否有单元测试。
- 是否有端到端测试。
- 是否有提交规范。
- 是否有 Code Review 流程。

## 8. 学习路线建议

推荐学习顺序：

1. 掌握 HTML、CSS、JavaScript 基础。
2. 理解 ES Module、CommonJS 等模块化规范。
3. 学习 npm、pnpm、package.json 和依赖管理。
4. 学习 Vite 的基本使用和配置。
5. 学习 Webpack 的 Entry、Output、Loader、Plugin、DevServer。
6. 学习 Babel、TypeScript、PostCSS、Sass 等编译工具。
7. 学习 ESLint、Prettier、Husky、Commitlint 等规范工具。
8. 学习 Tree Shaking、代码分割、缓存、CDN 等性能优化知识。
9. 学习 Vitest、Jest、Playwright 等测试工具。
10. 学习 CI/CD、Docker、Nginx、自动化部署等发布相关知识。
11. 学习 Monorepo、组件库、微前端等大型项目工程化方案。

## 9. 总结

前端工程化不是某一个工具，而是一整套围绕项目开发、构建、测试、发布和维护的技术体系。它的价值在于让前端项目从“能运行”走向“可维护、可协作、可扩展、可稳定交付”。

Webpack 代表了成熟、强大、灵活的传统打包体系，适合复杂项目和深度定制场景。Vite 代表了现代、快速、简洁的新一代构建体验，适合大多数现代前端项目。

实际开发中，理解工具背后的原理比记住配置更重要。掌握模块化、依赖图、构建流程、浏览器加载机制、缓存策略和工程规范，才能在不同项目中做出合理的技术选择。
