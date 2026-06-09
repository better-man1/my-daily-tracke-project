# 前端 DevOps 与部署知识点总结

## 1. 总览

前端 DevOps 与部署是指把前端项目从本地开发，经过代码检查、测试、构建、制品管理、发布、监控、回滚等流程，稳定交付到用户可访问环境的一整套工程体系。

它关注的不只是“把 dist 文件上传到服务器”，而是：

- 如何保证构建可重复。
- 如何保证不同环境配置正确。
- 如何避免发布后白屏。
- 如何让静态资源缓存稳定。
- 如何让发布可灰度、可回滚。
- 如何在 CI 中自动检查质量。
- 如何保护密钥和环境变量。
- 如何监控线上错误和性能。
- 如何让团队交付流程标准化。

前端 DevOps 覆盖的核心知识：

- Git 工作流。
- 环境管理。
- 包管理和依赖安装。
- 构建流程。
- CI/CD。
- 静态资源部署。
- Nginx 配置。
- CDN 与缓存。
- Docker 容器化。
- SSR/BFF 部署。
- 灰度发布和回滚。
- Source Map 管理。
- 监控和告警。
- 安全与密钥管理。
- 多环境和多租户发布。
- Monorepo 发布。

## 2. DevOps 是什么

DevOps 是 Development 和 Operations 的组合，强调开发、测试、运维、发布之间的自动化协作。

前端 DevOps 的目标：

- 缩短从代码提交到上线的时间。
- 降低人工发布错误。
- 提高构建和部署一致性。
- 提高质量检查自动化程度。
- 提升线上问题发现和回滚速度。
- 让团队形成可持续交付能力。

传统前端发布常见问题：

- 手动打包，容易漏环境。
- 手动上传，容易覆盖错目录。
- 没有构建记录，无法追溯。
- 静态资源缓存导致用户加载旧文件。
- 没有回滚机制。
- 没有自动测试。
- 线上报错无法定位源码。

DevOps 要解决这些问题，让发布流程变成稳定、可审计、可复现的流水线。

## 3. 前端部署对象

### 3.1 静态单页应用

典型项目：

- React SPA。
- Vue SPA。
- Vite 项目。
- 后台管理系统。

构建产物通常是：

```text
dist
├─ index.html
├─ assets
│  ├─ app.[hash].js
│  ├─ app.[hash].css
│  └─ logo.[hash].png
└─ favicon.ico
```

部署方式：

- Nginx。
- CDN + 对象存储。
- Vercel。
- Netlify。
- 云厂商静态网站托管。

核心关注：

- History 路由 fallback。
- 静态资源缓存。
- HTML 缓存策略。
- base path。
- API 代理。

### 3.2 多页应用

多页应用可能有多个 HTML 入口。

示例：

```text
dist
├─ index.html
├─ admin.html
├─ mobile.html
└─ assets
```

适合：

- 官网。
- 活动页。
- 多入口业务系统。
- 老项目。

部署关注：

- 多入口路由映射。
- 每个 HTML 的缓存策略。
- 公共资源拆分。

### 3.3 SSR 应用

SSR 应用需要 Node.js 或边缘运行时执行服务端渲染。

常见框架：

- Next.js。
- Nuxt。
- Remix。

部署对象：

- Node.js 服务。
- Serverless Function。
- Edge Function。
- 容器镜像。

关注点：

- 服务启动。
- Node.js 运行时版本。
- 环境变量。
- 缓存。
- 日志。
- 健康检查。
- 扩缩容。

### 3.4 BFF 服务

BFF 是 Backend For Frontend，面向前端的服务层。

部署方式：

- Node.js 服务。
- Docker 容器。
- Kubernetes。
- Serverless。

关注点：

- 接口聚合。
- 鉴权。
- 超时。
- 降级。
- 监控。
- 日志。
- 配置和密钥。

### 3.5 组件库和工具库

组件库和工具库不是部署到服务器，而是发布到包仓库或内部制品库。

常见产物：

- ESM。
- CommonJS。
- UMD。
- TypeScript 类型声明。
- CSS。
- 文档站。

发布目标：

- npm。
- 私有 npm registry。
- GitHub Packages。
- 内部制品平台。

关注点：

- 版本号。
- changelog。
- peerDependencies。
- 按需引入。
- 类型声明。
- 发布权限。

## 4. Git 工作流

### 4.1 Git 在 DevOps 中的作用

Git 是 CI/CD 的起点。一次提交、一个分支、一个 tag 通常会触发不同的流水线。

常见触发方式：

- push 到分支。
- 创建 Pull Request。
- 合并到主分支。
- 创建 tag。
- 手动触发。
- 定时触发。

### 4.2 常见分支模型

#### Git Flow

包含：

- main。
- develop。
- feature。
- release。
- hotfix。

适合：

- 发布周期较长。
- 多版本并行维护。
- 传统企业项目。

缺点：

- 流程较重。
- 分支多，合并成本高。

#### GitHub Flow

流程：

1. 从 main 拉 feature 分支。
2. 提交代码。
3. 创建 Pull Request。
4. Code Review。
5. CI 通过。
6. 合并 main。
7. 自动部署。

适合：

- 持续交付。
- Web 应用。
- 团队协作清晰的项目。

#### Trunk Based Development

主干开发强调频繁小批量合入主干。

特点：

- 分支生命周期短。
- 强依赖自动化测试。
- 适合持续部署。

高级要求：

- 需要 feature flag。
- 需要完善 CI。
- 需要团队纪律。

### 4.3 提交规范

常见提交格式：

```text
feat: add user profile page
fix: resolve login redirect issue
docs: update deployment guide
refactor: split request module
test: add user form tests
chore: update dependencies
```

价值：

- 提交历史清晰。
- 方便生成 changelog。
- 方便定位问题。
- 方便自动化版本发布。

工具：

- Commitlint。
- Husky。
- lint-staged。

## 5. 环境管理

### 5.1 常见环境

前端项目常见环境：

- local：本地开发。
- development：开发环境。
- test：测试环境。
- staging：预发布环境。
- production：生产环境。

不同环境可能不同：

- API 地址。
- CDN 地址。
- 登录地址。
- Sentry DSN。
- 是否开启 Mock。
- 是否开启调试工具。
- 是否启用埋点。
- 是否使用真实支付。

### 5.2 环境变量

Vite 示例：

```env
VITE_API_BASE_URL=https://api.example.com
VITE_APP_ENV=production
```

代码中读取：

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL
```

注意：

- 前端环境变量会被打包进浏览器代码。
- 不要把真正密钥放入前端环境变量。
- `VITE_`、`NEXT_PUBLIC_` 等公开变量不是秘密。

### 5.3 编译时配置和运行时配置

#### 编译时配置

构建时注入，打包后固定。

优点：

- 简单。
- 构建工具原生支持。

缺点：

- 同一份构建产物不能直接部署到多个环境。
- 改配置需要重新构建。

#### 运行时配置

应用启动或页面加载时读取配置。

常见方式：

- `config.js`。
- 服务端注入 HTML。
- 接口返回配置。
- 容器启动时替换配置文件。

示例：

```html
<script src="/config.js"></script>
```

```js
window.__APP_CONFIG__ = {
  API_BASE_URL: 'https://api.example.com'
}
```

优点：

- 一份产物可部署多个环境。
- 改配置不一定重新构建。

缺点：

- 需要额外设计配置加载。
- 类型约束和缓存策略要处理。

### 5.4 环境隔离

必须避免：

- 测试环境请求生产 API。
- 生产包开启 Mock。
- 生产包暴露调试入口。
- 测试支付误连真实支付。
- 预发配置污染生产。

建议：

- 环境变量命名清晰。
- 构建日志输出当前环境。
- CI 中增加环境校验。
- 高风险配置在发布前二次确认。

## 6. 包管理与依赖安装

### 6.1 包管理器

常见：

- npm。
- yarn。
- pnpm。

团队应该统一包管理器。

不要同时提交：

- `package-lock.json`
- `yarn.lock`
- `pnpm-lock.yaml`

除非项目明确需要多包管理器支持。

### 6.2 lockfile

lockfile 用于锁定依赖版本，保证不同机器安装结果一致。

价值：

- 构建可重复。
- CI 和本地一致。
- 减少依赖漂移。
- 方便审计和回滚。

### 6.3 npm ci

CI 中 npm 项目推荐：

```bash
npm ci
```

特点：

- 根据 lockfile 精确安装。
- 比 npm install 更适合 CI。
- lockfile 和 package.json 不一致时会失败。

pnpm 项目：

```bash
pnpm install --frozen-lockfile
```

### 6.4 CI 缓存

依赖安装是 CI 中常见耗时点。

可缓存：

- npm cache。
- pnpm store。
- yarn cache。
- node_modules，视平台和策略而定。
- 构建缓存。

注意：

- 缓存 key 应包含 lockfile hash。
- 缓存失效策略要合理。
- 不要因为缓存导致依赖错误难以排查。

## 7. 构建流程

### 7.1 前端构建做了什么

构建过程通常包括：

- 解析模块依赖。
- TypeScript 转译。
- JSX/Vue SFC 编译。
- CSS 处理。
- 静态资源处理。
- 代码分割。
- Tree Shaking。
- 压缩混淆。
- 文件 Hash。
- 生成 HTML。
- 生成 Source Map。

### 7.2 常见构建命令

Vite：

```bash
npm run build
```

常见脚本：

```json
{
  "scripts": {
    "dev": "vite",
    "typecheck": "tsc --noEmit",
    "lint": "eslint .",
    "test": "vitest",
    "build": "vite build",
    "preview": "vite preview"
  }
}
```

### 7.3 构建产物检查

构建完成后应检查：

- 是否生成 dist。
- HTML 是否引用正确资源。
- JS/CSS 是否带 Hash。
- 资源路径是否正确。
- Source Map 是否按策略生成。
- 包体积是否超标。
- 是否包含测试环境地址。
- 是否包含敏感信息。

### 7.4 Source Map 策略

Source Map 用于把压缩后的线上代码映射回源码，方便定位错误。

常见策略：

- 生产不生成 Source Map。
- 生产生成但不公开访问。
- 上传到监控平台后删除。
- 只保留 hidden source map。

风险：

- 公开 Source Map 可能暴露源码。
- 可能暴露内部接口和业务逻辑。

推荐：

- 生产可生成 Source Map。
- 上传 Sentry 等监控平台。
- 不把 Source Map 直接暴露给公网。

## 8. CI/CD

### 8.1 CI 是什么

CI 是 Continuous Integration，持续集成。

前端 CI 通常做：

- 拉取代码。
- 安装依赖。
- 代码格式检查。
- ESLint。
- TypeScript 类型检查。
- 单元测试。
- 构建。
- 产物体积检查。
- 安全扫描。

CI 的目标：

- 在合并前发现问题。
- 保证主分支始终可构建。
- 降低人工 Review 低级问题。

### 8.2 CD 是什么

CD 可以指：

- Continuous Delivery：持续交付。
- Continuous Deployment：持续部署。

前端 CD 通常做：

- 获取构建产物。
- 上传静态资源。
- 部署 HTML。
- 刷新 CDN。
- 上传 Source Map。
- 通知发布结果。
- 失败回滚。

### 8.3 典型流水线

典型流程：

```text
push / pull request
-> install dependencies
-> lint
-> typecheck
-> test
-> build
-> artifact upload
-> deploy to test/staging/production
-> smoke test
-> notify
```

生产发布建议：

- 需要人工确认或审批。
- 记录发布人、版本、commit。
- 支持回滚。
- 自动通知团队。

### 8.4 GitHub Actions 示例

```yaml
name: frontend-ci

on:
  pull_request:
  push:
    branches:
      - main

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-node@v4
        with:
          node-version: 20
          cache: npm

      - run: npm ci
      - run: npm run lint
      - run: npm run typecheck
      - run: npm run test
      - run: npm run build
```

### 8.5 GitLab CI 示例

```yaml
stages:
  - install
  - check
  - build

cache:
  paths:
    - node_modules/

install:
  stage: install
  script:
    - npm ci

check:
  stage: check
  script:
    - npm run lint
    - npm run typecheck
    - npm run test

build:
  stage: build
  script:
    - npm run build
  artifacts:
    paths:
      - dist/
```

## 9. 质量门禁

### 9.1 为什么需要质量门禁

质量门禁是发布前必须通过的检查。

常见门禁：

- lint 通过。
- 类型检查通过。
- 单元测试通过。
- E2E 测试通过。
- 构建通过。
- 包体积未超标。
- 安全扫描无高危漏洞。
- 核心页面冒烟通过。

价值：

- 降低生产事故。
- 保证代码质量底线。
- 把团队规范变成自动化流程。

### 9.2 类型检查

TypeScript 项目应在 CI 中运行：

```bash
npm run typecheck
```

Vite 构建默认可能只转译，不做完整类型检查，所以类型检查应单独执行。

### 9.3 单元测试

适合检查：

- 工具函数。
- 状态管理。
- 复杂业务规则。
- 表单校验。
- 组件行为。

工具：

- Vitest。
- Jest。
- Testing Library。

### 9.4 E2E 测试

适合检查：

- 登录。
- 权限。
- 下单。
- 支付。
- 审批。
- 核心业务流程。

工具：

- Playwright。
- Cypress。

建议：

- E2E 不宜过多。
- 重点覆盖核心链路。
- 测试数据要可重复。
- CI 中可对主分支和发布分支执行。

### 9.5 包体积检查

包体积膨胀会影响用户加载。

可设置预算：

- 首屏 JS gzip 不超过指定大小。
- 单个 chunk 不超过指定大小。
- 图片不超过指定大小。
- 新增依赖需要说明。

工具：

- bundlesize。
- size-limit。
- rollup-plugin-visualizer。
- webpack-bundle-analyzer。

## 10. 静态资源部署

### 10.1 部署到 Nginx

最常见方式是把 dist 放到 Nginx 静态目录。

Nginx 示例：

```nginx
server {
  listen 80;
  server_name example.com;

  root /usr/share/nginx/html;
  index index.html;

  location / {
    try_files $uri $uri/ /index.html;
  }
}
```

`try_files` 用于支持 SPA History 路由。

如果没有 fallback，用户直接访问 `/users/1` 可能出现 404。

### 10.2 部署到对象存储

对象存储适合托管静态资源。

常见平台：

- AWS S3。
- 阿里云 OSS。
- 腾讯云 COS。
- 七牛云。

通常搭配 CDN 使用。

部署流程：

1. 构建 dist。
2. 上传 assets 到对象存储。
3. 上传或更新 HTML。
4. 刷新或预热 CDN。

### 10.3 部署到静态托管平台

常见平台：

- Vercel。
- Netlify。
- Cloudflare Pages。
- GitHub Pages。

优点：

- 配置简单。
- 自动构建。
- 预览环境方便。
- CDN 集成。

注意：

- 企业私有网络和合规要求。
- 环境变量配置。
- 构建限制。
- SSR 支持方式。

## 11. Nginx 前端配置

### 11.1 SPA fallback

React Router、Vue Router 使用 history 模式时需要：

```nginx
location / {
  try_files $uri $uri/ /index.html;
}
```

否则刷新深层路由会 404。

### 11.2 静态资源缓存

Hash 资源长缓存：

```nginx
location /assets/ {
  add_header Cache-Control "public, max-age=31536000, immutable";
}
```

HTML 不强缓存：

```nginx
location = /index.html {
  add_header Cache-Control "no-cache";
}
```

### 11.3 Gzip

```nginx
gzip on;
gzip_types text/plain text/css application/javascript application/json image/svg+xml;
gzip_min_length 1024;
```

### 11.4 Brotli

Brotli 通常比 gzip 压缩率更高，但需要 Nginx 模块支持。

适合：

- JS。
- CSS。
- HTML。
- JSON。
- SVG。

### 11.5 API 反向代理

```nginx
location /api/ {
  proxy_pass http://backend:8080/;
  proxy_set_header Host $host;
  proxy_set_header X-Real-IP $remote_addr;
}
```

注意：

- 生产环境代理应由网关或 Nginx 管理。
- 不要把开发代理配置误认为生产安全方案。

## 12. CDN 与缓存策略

### 12.1 CDN 的作用

CDN 把静态资源分发到离用户更近的节点。

优势：

- 降低延迟。
- 提升下载速度。
- 减轻源站压力。
- 提高可用性。

### 12.2 前端推荐缓存策略

推荐：

```text
index.html：no-cache
assets/*.js：max-age=31536000, immutable
assets/*.css：max-age=31536000, immutable
assets/*.{png,jpg,webp,svg,woff2}：max-age=31536000, immutable
```

原因：

- HTML 需要及时更新。
- Hash 资源内容变化后文件名变化，可以长期缓存。

### 12.3 发布顺序

推荐发布顺序：

1. 先上传带 Hash 的静态资源。
2. 再更新 HTML。
3. 最后刷新必要 CDN。

原因：

- 如果先更新 HTML，而资源还没上传，用户可能加载到引用新资源的 HTML，但 JS/CSS 404，导致白屏。

### 12.4 回滚策略

回滚时要注意：

- 旧 HTML 要能恢复。
- 旧静态资源不能被删除。
- CDN 缓存要正确刷新。
- Source Map 要对应版本。

建议：

- 每次发布保留版本目录。
- 静态资源按版本存储。
- 不立即删除旧资源。
- 回滚只切换 HTML 或版本指针。

## 13. Docker 容器化

### 13.1 静态前端 Dockerfile

多阶段构建示例：

```dockerfile
FROM node:20-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
```

优点：

- 构建环境和运行环境隔离。
- 运行镜像更小。
- 部署一致。

### 13.2 Node SSR Dockerfile

SSR 应用通常需要 Node.js 运行。

示例：

```dockerfile
FROM node:20-alpine

WORKDIR /app
COPY package*.json ./
RUN npm ci --omit=dev

COPY . .
RUN npm run build

EXPOSE 3000
CMD ["npm", "run", "start"]
```

生产建议：

- 使用非 root 用户。
- 不把 `.env` 打进镜像。
- 使用健康检查。
- 控制镜像体积。
- 使用锁文件安装依赖。

### 13.3 镜像标签

镜像 tag 不应只用 latest。

推荐：

- commit hash。
- 版本号。
- 构建号。
- 环境标识。

示例：

```text
frontend-app:1.4.2
frontend-app:commit-a1b2c3d
```

这样方便追踪和回滚。

## 14. Kubernetes 部署

### 14.1 前端在 Kubernetes 中的形态

可能形态：

- Nginx 静态资源容器。
- Node.js SSR 容器。
- BFF 容器。

常见资源：

- Deployment。
- Service。
- Ingress。
- ConfigMap。
- Secret。
- HPA。

### 14.2 Deployment

示例：

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: frontend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: frontend
  template:
    metadata:
      labels:
        app: frontend
    spec:
      containers:
        - name: frontend
          image: registry.example.com/frontend:1.0.0
          ports:
            - containerPort: 80
```

### 14.3 健康检查

对 Node.js SSR/BFF 服务应配置：

- readinessProbe。
- livenessProbe。

价值：

- 服务未准备好时不接流量。
- 服务异常时自动重启。

静态 Nginx 容器也可以提供健康检查路径。

### 14.4 滚动发布

Kubernetes Deployment 默认支持滚动更新。

优点：

- 不需要停机。
- 新旧 Pod 平滑替换。

注意：

- 应确保新版本健康后再下线旧版本。
- SSR/BFF 要处理优雅关闭。
- 前端静态资源要保证新旧版本兼容。

## 15. 灰度发布与回滚

### 15.1 为什么需要灰度

灰度发布是让新版本先对少量用户生效，观察无异常后再全量。

价值：

- 降低发布风险。
- 及时发现问题。
- 避免一次性影响全部用户。

### 15.2 常见灰度策略

策略：

- 按用户 ID。
- 按租户。
- 按地区。
- 按 Cookie。
- 按请求头。
- 按比例。
- 按内部账号。

### 15.3 前端灰度方式

静态前端灰度：

- 不同用户返回不同 HTML。
- CDN 或网关按规则分流。
- 版本目录隔离。

SSR/BFF 灰度：

- 服务层按流量比例分流。
- Kubernetes 多版本 Deployment。
- Ingress/Service Mesh 灰度。

### 15.4 Feature Flag

Feature Flag 用于控制功能开关。

适合：

- 功能未完全开放。
- 分批上线。
- A/B 实验。
- 快速关闭异常功能。

注意：

- Flag 要有生命周期。
- 长期不用的 Flag 要清理。
- 高风险逻辑要服务端控制。

### 15.5 回滚

回滚要求：

- 知道当前版本。
- 知道上一个稳定版本。
- 旧资源仍可访问。
- 数据库或接口变更向后兼容。
- 监控能验证回滚结果。

前端回滚重点：

- HTML 回滚。
- CDN 刷新。
- 静态资源保留。
- Source Map 对应版本。

## 16. 多环境部署

### 16.1 环境差异

不同环境可能有：

- 不同 API。
- 不同登录系统。
- 不同 CDN。
- 不同监控项目。
- 不同 feature flag。
- 不同权限数据。

### 16.2 环境提升流程

常见流程：

```text
local -> dev -> test -> staging -> production
```

建议：

- 同一个 commit 逐级提升。
- 避免每个环境重新打不同代码。
- 配置和代码分离。
- 流水线记录每个环境部署版本。

### 16.3 预览环境

Pull Request 预览环境可以让产品、测试、设计提前验证。

适合：

- 前端页面评审。
- UI 验收。
- 多人协作。
- 组件库预览。

常见平台：

- Vercel Preview。
- Netlify Deploy Preview。
- 自建 PR 环境。

## 17. SSR 与 BFF 部署

### 17.1 SSR 部署关注点

SSR 不是纯静态部署，需要服务端运行。

关注：

- Node.js 版本。
- 进程管理。
- 服务端日志。
- 内存使用。
- CPU 使用。
- 接口超时。
- HTML 缓存。
- Hydration 错误。
- 服务健康检查。

### 17.2 SSR 缓存

可缓存：

- 页面 HTML。
- 数据接口。
- 静态资源。
- API 聚合结果。

缓存位置：

- Node 内存。
- Redis。
- CDN。
- 边缘节点。

注意：

- 用户个性化页面不能随意共享缓存。
- 登录态页面要谨慎缓存。
- 缓存 key 要包含必要维度。

### 17.3 BFF 部署关注点

BFF 需要后端服务能力。

关注：

- 接口聚合性能。
- 下游服务超时。
- 错误降级。
- 请求日志。
- Trace ID。
- 鉴权。
- 限流。
- 熔断。
- 灰度。

### 17.4 进程管理

Node.js 服务常见方式：

- PM2。
- Docker。
- Kubernetes。
- systemd。
- Serverless。

生产建议：

- 不使用开发服务器。
- 配置健康检查。
- 捕获未处理异常。
- 设置日志和监控。
- 支持优雅关闭。

## 18. Source Map 与错误定位

### 18.1 Source Map 的作用

线上代码通常被压缩混淆。Source Map 可以把错误堆栈映射回源码位置。

没有 Source Map 时：

```text
app.8f3a.js:1:38492
```

有 Source Map 后可以定位到：

```text
src/pages/UserList.tsx:42:10
```

### 18.2 Source Map 安全策略

推荐：

- 生成 Source Map。
- 上传监控平台。
- 删除公开目录中的 `.map` 文件。
- 按版本保存。

不推荐：

- 把 `.map` 文件直接公开在生产 CDN。

### 18.3 发布版本关联

监控平台需要知道：

- release 版本。
- commit hash。
- Source Map。
- 部署环境。

这样错误才能准确映射。

## 19. 前端监控与告警

### 19.1 错误监控

需要监控：

- JS 运行时错误。
- Promise 未捕获错误。
- 资源加载失败。
- 接口错误。
- 白屏。
- Hydration 错误。

上报信息：

- 错误堆栈。
- 页面 URL。
- 用户环境。
- 浏览器。
- 版本号。
- Source Map。
- 用户行为路径。

### 19.2 性能监控

指标：

- FCP。
- LCP。
- CLS。
- INP。
- TTFB。
- 接口耗时。
- 资源耗时。

价值：

- 发现性能退化。
- 对比不同版本。
- 按地区、设备、网络分析。

### 19.3 发布监控

发布后应重点观察：

- 错误率是否上升。
- 白屏率是否上升。
- 接口错误是否上升。
- LCP/INP 是否变差。
- 用户转化是否下降。

可以设置发布后自动观察窗口，例如 10 到 30 分钟。

### 19.4 告警

告警应避免太多噪音。

可告警：

- 错误率超过阈值。
- 白屏率超过阈值。
- 核心接口失败率升高。
- LCP 明显劣化。
- 资源 404 增加。
- 新版本错误集中爆发。

## 20. 安全与密钥管理

### 20.1 前端不能保存真正密钥

前端代码会运行在用户浏览器中，任何打包进去的内容都可能被看到。

不要放入前端：

- 数据库密码。
- 服务端私钥。
- 第三方 Secret。
- 云服务 Access Secret。
- 内部系统高权限 Token。

### 20.2 CI Secret

CI 中可能需要：

- npm token。
- 云存储上传密钥。
- CDN 刷新密钥。
- Sentry token。
- Docker registry token。

应使用平台 Secret 管理，不要写入代码仓库。

### 20.3 最小权限

部署密钥应最小权限：

- 只能上传指定 bucket。
- 只能刷新指定 CDN。
- 只能发布指定包。
- 不能拥有管理员权限。

### 20.4 安全扫描

CI 可加入：

- npm audit。
- pnpm audit。
- 依赖漏洞扫描。
- license 检查。
- Secret 扫描。
- Docker 镜像扫描。

## 21. Monorepo DevOps

### 21.1 Monorepo 的挑战

Monorepo 中可能有：

- 多个前端应用。
- 多个组件包。
- 多个工具包。
- 多个文档站。

挑战：

- 构建时间长。
- 依赖关系复杂。
- 发布顺序复杂。
- CI 成本增加。

### 21.2 增量构建

工具：

- Turborepo。
- Nx。
- pnpm workspace。
- Rush。

目标：

- 只构建受影响项目。
- 缓存构建结果。
- 并行执行任务。

### 21.3 变更影响分析

发布前需要知道：

- 哪些包变了。
- 哪些应用受影响。
- 是否需要重新构建。
- 是否需要发布新版本。

### 21.4 Changesets

Changesets 常用于 Monorepo 包版本管理。

能力：

- 记录变更。
- 自动生成 changelog。
- 自动更新版本号。
- 自动发布包。

适合：

- 组件库。
- 工具库。
- 多包仓库。

## 22. 组件库发布

### 22.1 组件库产物

组件库通常输出：

- ESM。
- CommonJS。
- TypeScript 类型。
- CSS。
- 主题变量。
- 文档站。

### 22.2 peerDependencies

组件库应把 React、Vue 等框架依赖放入 peerDependencies。

示例：

```json
{
  "peerDependencies": {
    "react": ">=18"
  }
}
```

原因：

- 避免打包多个 React。
- 使用方控制框架版本。
- 降低包体积。

### 22.3 版本发布

语义化版本：

- major：不兼容变更。
- minor：向下兼容新功能。
- patch：bug 修复。

组件库发布应提供：

- changelog。
- 迁移说明。
- Breaking Change 标记。
- 示例更新。

## 23. 常见部署事故

### 23.1 发布后白屏

可能原因：

- JS 资源 404。
- HTML 引用新资源但资源未上传。
- CDN 缓存旧 HTML。
- base path 错误。
- 环境变量错误。
- 生产接口跨域失败。
- JS 运行时错误。

排查：

- 看 Network。
- 看 Console。
- 看资源路径。
- 看 CDN。
- 看发布版本。
- 看监控错误。

### 23.2 刷新页面 404

原因：

- SPA history 路由没有 fallback。

解决：

```nginx
try_files $uri $uri/ /index.html;
```

### 23.3 用户加载到新 HTML 旧 JS

原因：

- HTML 缓存策略不正确。
- CDN 没刷新。
- Service Worker 缓存策略错误。

解决：

- HTML 使用 no-cache。
- Hash 静态资源长缓存。
- 正确刷新 CDN。
- 设计 Service Worker 更新机制。

### 23.4 环境配置错

表现：

- 生产请求测试 API。
- 测试环境使用生产登录。
- 埋点上报到错误项目。

预防：

- 构建日志打印环境。
- 发布前校验配置。
- 使用运行时配置中心。
- CI 中做环境断言。

## 24. 前端 DevOps 检查清单

### 24.1 代码阶段

- 是否有统一分支策略。
- 是否有提交规范。
- 是否有 Code Review。
- 是否有 ESLint。
- 是否有 Prettier。
- 是否有 TypeScript 类型检查。
- 是否有单元测试。
- 是否有核心 E2E 测试。

### 24.2 构建阶段

- 是否使用 lockfile。
- CI 是否使用稳定安装命令。
- 构建是否可重复。
- 是否检查包体积。
- 是否生成 Hash 文件名。
- Source Map 策略是否正确。
- 是否检查敏感信息。

### 24.3 部署阶段

- 是否先上传静态资源再更新 HTML。
- 是否配置 SPA fallback。
- 是否配置 HTML 缓存策略。
- 是否配置静态资源长缓存。
- 是否支持 CDN 刷新。
- 是否支持回滚。
- 是否记录发布版本。

### 24.4 运行阶段

- 是否有错误监控。
- 是否有性能监控。
- 是否有资源错误监控。
- 是否上传 Source Map。
- 是否有告警。
- 是否能按版本定位问题。
- 是否有发布后观察机制。

### 24.5 安全阶段

- 是否保护 CI Secret。
- 是否最小权限。
- 是否做依赖审计。
- 是否检查许可证。
- 是否避免前端暴露密钥。
- 是否处理 Source Map 暴露风险。

## 25. 学习路线建议

推荐学习顺序：

1. 掌握 npm、pnpm、package.json、lockfile。
2. 掌握 Vite/Webpack 构建产物结构。
3. 学习环境变量和多环境配置。
4. 学习 Nginx 静态资源部署。
5. 学习 HTTP 缓存和 CDN。
6. 学习 CI/CD 基础流程。
7. 学习 GitHub Actions 或 GitLab CI。
8. 学习 Source Map 和前端监控。
9. 学习 Docker 和容器化部署。
10. 学习灰度发布、回滚和 Feature Flag。
11. 学习 SSR/BFF 部署。
12. 学习 Monorepo 增量构建和包发布。
13. 建立完整前端发布规范和检查清单。

## 26. 总结

前端 DevOps 与部署是一套保障前端项目稳定交付的工程体系。它从 Git 分支和代码提交开始，经过依赖安装、代码检查、测试、构建、制品管理、部署、CDN 缓存、灰度、回滚、监控和告警，最终形成可持续、可追溯、可恢复的发布能力。

高级前端工程师不能只会本地运行项目，还要理解产物如何生成、如何部署、如何缓存、如何回滚、如何监控。前端架构师则需要进一步把这些能力标准化、平台化，让团队在频繁迭代中仍能保持高质量和低风险交付。
