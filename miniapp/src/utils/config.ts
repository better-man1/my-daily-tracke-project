/**
 * 环境配置
 *
 * 开发时修改 DEV_BASE_URL 为你的后端地址：
 *  - 微信开发者工具模拟器：http://127.0.0.1:8080
 *  - 真机调试（需同一局域网）：http://192.168.x.x:8080
 *  - 线上环境：https://your-domain.com
 *
 * 注意：微信小程序正式发布时必须使用 HTTPS 且域名需在微信后台白名单
 */

// #ifdef MP-WEIXIN
// 微信小程序环境：必须用完整 URL，不支持相对路径
export const BASE_URL = 'http://127.0.0.1:8080/api/v1'
// #endif

// #ifdef H5
// H5 环境：可以用相对路径走 Vite 代理
export const BASE_URL = '/api/v1'
// #endif

// #ifndef MP-WEIXIN
// #ifndef H5
// 其他平台兜底
export const BASE_URL = 'http://127.0.0.1:8080/api/v1'
// #endif
// #endif

/** 请求超时时间 (ms) */
export const REQUEST_TIMEOUT = 15000
