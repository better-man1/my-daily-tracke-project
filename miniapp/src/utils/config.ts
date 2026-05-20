/**
 * ============================================================================
 * config.ts — 环境配置文件
 * ============================================================================
 *
 * 【文件说明】
 * 定义不同平台（微信小程序、H5 等）的 API 基础地址和全局配置。
 * 使用 Uni-app 的条件编译语法实现多平台差异化配置。
 *
 * 【Uni-app 条件编译 — 核心语法】
 * 条件编译是 Uni-app 最重要的特性之一，允许同一份代码针对不同平台生成不同内容。
 *
 * 语法格式（注释型条件编译）：
 *   // #ifdef 平台标识
 *   ... 仅在该平台生效的代码 ...
 *   // #endif
 *
 *   // #ifndef 平台标识
 *   ... 在除该平台外的所有平台生效的代码 ...
 *   // #endif
 *
 * 常用平台标识：
 *   MP-WEIXIN  — 微信小程序
 *   MP-ALIPAY  — 支付宝小程序
 *   H5         — 网页（浏览器）
 *   APP        — 原生 App
 *   MP         — 所有小程序（泛指）
 *
 * 【与 H5 前端的差异】
 * - H5 开发: 通常使用 import.meta.env.MODE 或 process.env.NODE_ENV 区分环境
 * - Uni-app: 使用条件编译 + 环境变量两种方式
 *   条件编译是编译时的，最终产物中只包含目标平台的代码（零运行时开销）
 *
 * 【微信小程序网络请求限制】
 * 1. 正式版必须使用 HTTPS（开发时可关闭校验）
 * 2. 域名必须在小程序后台"服务器域名"中配置白名单
 * 3. 不支持 localhost/127.0.0.1（真机预览时）
 * 4. 每个请求默认超时 60 秒
 *
 * 【参考文档】
 * - Uni-app 条件编译: https://uniapp.dcloud.net.cn/tutorial/platform.html
 * - 微信小程序网络限制: https://developers.weixin.qq.com/miniprogram/dev/framework/ability/network.html
 * ============================================================================
 */

// #ifdef MP-WEIXIN
// 【微信小程序环境】必须使用完整的 HTTPS URL
// 小程序不支持相对路径（没有"域名"的概念，每个请求都是独立的网络请求）
// 开发时可以在微信开发者工具中关闭域名校验：详情 → 本地设置 → 不校验合法域名
export const BASE_URL = 'http://127.0.0.1:8080/api/v1'
// #endif

// #ifdef H5
// 【H5 网页环境】可以使用相对路径，通过 Vite 的 proxy 配置代理到后端
// 开发时在 vite.config.ts 中配置 server.proxy，生产环境用 Nginx 反向代理
export const BASE_URL = '/api/v1'
// #endif

// #ifndef MP-WEIXIN
// #ifndef H5
// 【其他平台兜底】如 App 端、支付宝小程序等
export const BASE_URL = 'http://127.0.0.1:8080/api/v1'
// #endif
// #endif

/** 请求超时时间（毫秒） — 小程序网络请求默认超时为 60s，这里设为 15s */
export const REQUEST_TIMEOUT = 15000
