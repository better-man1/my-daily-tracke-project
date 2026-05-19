/**
 * 生成 TabBar PNG 图标
 * 使用 Node.js 内置模块（无需额外依赖）生成简洁的圆形图标
 */
const fs = require('fs')
const path = require('path')
const zlib = require('zlib')

// ── CRC32 实现 ──────────────────────────────────────────
const crcTable = new Int32Array(256)
for (let i = 0; i < 256; i++) {
  let c = i
  for (let j = 0; j < 8; j++) {
    c = (c & 1) ? (0xedb88320 ^ (c >>> 1)) : (c >>> 1)
  }
  crcTable[i] = c
}
function crc32(buf) {
  let crc = -1
  for (let i = 0; i < buf.length; i++) {
    crc = crcTable[(crc ^ buf[i]) & 0xff] ^ (crc >>> 8)
  }
  return (crc ^ -1) >>> 0
}
function makeChunk(type, data) {
  const tb = Buffer.from(type, 'ascii')
  const lenBuf = Buffer.alloc(4)
  lenBuf.writeUInt32BE(data.length, 0)
  const crcInput = Buffer.concat([tb, data])
  const crcBuf = Buffer.alloc(4)
  crcBuf.writeUInt32BE(crc32(crcInput), 0)
  return Buffer.concat([lenBuf, tb, data, crcBuf])
}

// ── PNG 生成（RGBA，含透明通道）──────────────────────────
function buildPng(size, drawFn) {
  // 每行: 1字节filter + size*4字节RGBA
  const raw = Buffer.alloc(size * (1 + size * 4), 0)
  for (let y = 0; y < size; y++) {
    const rowStart = y * (1 + size * 4)
    raw[rowStart] = 0 // filter = None
    for (let x = 0; x < size; x++) {
      const pixel = drawFn(x, y, size)
      const off = rowStart + 1 + x * 4
      raw[off]     = pixel[0] // R
      raw[off + 1] = pixel[1] // G
      raw[off + 2] = pixel[2] // B
      raw[off + 3] = pixel[3] // A
    }
  }
  const compressed = zlib.deflateSync(raw, { level: 9 })
  const sig = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10])
  const ihdr = Buffer.alloc(13)
  ihdr.writeUInt32BE(size, 0)
  ihdr.writeUInt32BE(size, 4)
  ihdr[8] = 8  // bit depth
  ihdr[9] = 6  // color type: RGBA
  return Buffer.concat([
    sig,
    makeChunk('IHDR', ihdr),
    makeChunk('IDAT', compressed),
    makeChunk('IEND', Buffer.alloc(0))
  ])
}

// ── 绘制函数工厂 ──────────────────────────────────────────
function parseHex(hex) {
  const h = hex.replace('#', '')
  return [
    parseInt(h.slice(0, 2), 16),
    parseInt(h.slice(2, 4), 16),
    parseInt(h.slice(4, 6), 16)
  ]
}

/**
 * 绘制一个带圆形背景 + 几何图标的 PNG
 * @param {string} shape  - 'home'|'plan'|'wallet'|'book'|'user'
 * @param {string} fgHex  - 前景色（图标线条）
 * @param {boolean} active - 是否激活态（加深背景）
 */
function makeDrawFn(shape, fgHex, active) {
  const fg = parseHex(fgHex)
  const bgAlpha = active ? 30 : 0  // 激活态有淡色背景圆

  return function drawPixel(x, y, size) {
    const cx = size / 2, cy = size / 2
    const r = size / 2
    const dx = x - cx, dy = y - cy
    const dist = Math.sqrt(dx * dx + dy * dy)

    // 背景圆（激活态）
    if (active && dist < r * 0.88) {
      // 超淡的圆形背景
    }

    // 抗锯齿辅助: 线段距离
    function lineAlpha(x0, y0, x1, y1, lw) {
      // 点到线段距离
      const ex = x1 - x0, ey = y1 - y0
      const len2 = ex * ex + ey * ey
      if (len2 === 0) return 0
      let t = ((x - x0) * ex + (y - y0) * ey) / len2
      t = Math.max(0, Math.min(1, t))
      const px = x0 + t * ex - x, py = y0 + t * ey - y
      const d = Math.sqrt(px * px + py * py)
      const half = lw / 2
      if (d < half) return 1
      if (d < half + 1.2) return (half + 1.2 - d) / 1.2
      return 0
    }
    function circleAlpha(ccx, ccy, cr, lw) {
      const d = Math.sqrt((x - ccx) ** 2 + (y - ccy) ** 2)
      const inner = cr - lw / 2, outer = cr + lw / 2
      if (d >= inner && d <= outer) return 1
      if (d >= inner - 1 && d < inner) return (d - (inner - 1))
      if (d > outer && d <= outer + 1) return (outer + 1 - d)
      return 0
    }
    function fillCircleAlpha(ccx, ccy, cr) {
      const d = Math.sqrt((x - ccx) ** 2 + (y - ccy) ** 2)
      if (d <= cr) return 1
      if (d <= cr + 1.2) return (cr + 1.2 - d) / 1.2
      return 0
    }
    function rectAlpha(rx, ry, rw, rh) {
      const inX = x >= rx && x <= rx + rw
      const inY = y >= ry && y <= ry + rh
      if (inX && inY) return 1
      // 边缘抗锯齿
      const edgeX = Math.min(Math.abs(x - rx), Math.abs(x - rx - rw))
      const edgeY = Math.min(Math.abs(y - ry), Math.abs(y - ry - rh))
      const nearX = (x >= rx - 0.8 && x <= rx + rw + 0.8)
      const nearY = (y >= ry - 0.8 && y <= ry + rh + 0.8)
      if (nearX && nearY && (edgeX < 0.8 || edgeY < 0.8)) return 0.5
      return 0
    }

    const s = size
    let alpha = 0
    const lw = s * 0.10  // 线宽

    if (shape === 'home') {
      // 屋顶三角形
      const roofY = s * 0.20
      const midX = s * 0.50
      const roofL = s * 0.18, roofR = s * 0.82, roofMid = s * 0.28
      alpha = Math.max(alpha,
        lineAlpha(roofL, roofMid, midX, roofY, lw),
        lineAlpha(midX, roofY, roofR, roofMid, lw),
        // 主体矩形
        rectAlpha(s*0.27, s*0.44, s*0.46, s*0.36) > 0 ? lw/s : 0,
      )
      // 轮廓门
      alpha = Math.max(alpha,
        lineAlpha(roofL, roofMid, s*0.27, roofMid, lw*0.5),
        lineAlpha(s*0.73, roofMid, roofR, roofMid, lw*0.5),
        lineAlpha(s*0.27, roofMid, s*0.27, s*0.80, lw),
        lineAlpha(s*0.73, roofMid, s*0.73, s*0.80, lw),
        lineAlpha(s*0.27, s*0.80, s*0.73, s*0.80, lw),
        // 门
        rectAlpha(s*0.41, s*0.58, s*0.18, s*0.22)
      )
    } else if (shape === 'plan') {
      // 剪贴板：矩形 + 横线
      alpha = Math.max(alpha,
        lineAlpha(s*0.28, s*0.24, s*0.72, s*0.24, lw),
        lineAlpha(s*0.72, s*0.24, s*0.72, s*0.78, lw),
        lineAlpha(s*0.28, s*0.78, s*0.72, s*0.78, lw),
        lineAlpha(s*0.28, s*0.24, s*0.28, s*0.78, lw),
        // 横线
        lineAlpha(s*0.38, s*0.40, s*0.62, s*0.40, lw*0.7),
        lineAlpha(s*0.38, s*0.52, s*0.62, s*0.52, lw*0.7),
        lineAlpha(s*0.38, s*0.64, s*0.55, s*0.64, lw*0.7),
        // 顶部夹子
        lineAlpha(s*0.38, s*0.18, s*0.62, s*0.18, lw*0.8),
        lineAlpha(s*0.38, s*0.18, s*0.38, s*0.28, lw*0.6),
        lineAlpha(s*0.62, s*0.18, s*0.62, s*0.28, lw*0.6),
      )
    } else if (shape === 'wallet') {
      // 钱包：矩形 + 圆形硬币
      alpha = Math.max(alpha,
        lineAlpha(s*0.18, s*0.34, s*0.82, s*0.34, lw),
        lineAlpha(s*0.82, s*0.34, s*0.82, s*0.72, lw),
        lineAlpha(s*0.18, s*0.72, s*0.82, s*0.72, lw),
        lineAlpha(s*0.18, s*0.34, s*0.18, s*0.72, lw),
        // 硬币
        circleAlpha(s*0.63, s*0.53, s*0.13, lw*0.8),
        fillCircleAlpha(s*0.63, s*0.53, s*0.05),
        // ¥ 符号位置的点
        lineAlpha(s*0.32, s*0.44, s*0.46, s*0.44, lw*0.6),
        lineAlpha(s*0.32, s*0.53, s*0.46, s*0.53, lw*0.6),
        // 封口横
        lineAlpha(s*0.22, s*0.28, s*0.58, s*0.28, lw*0.7),
      )
    } else if (shape === 'book') {
      // 书本：两页 + 书脊
      alpha = Math.max(alpha,
        // 左页
        lineAlpha(s*0.18, s*0.22, s*0.18, s*0.78, lw),
        lineAlpha(s*0.18, s*0.22, s*0.48, s*0.22, lw),
        lineAlpha(s*0.48, s*0.22, s*0.48, s*0.78, lw),
        lineAlpha(s*0.18, s*0.78, s*0.48, s*0.78, lw),
        // 右页
        lineAlpha(s*0.52, s*0.22, s*0.82, s*0.22, lw),
        lineAlpha(s*0.82, s*0.22, s*0.82, s*0.78, lw),
        lineAlpha(s*0.52, s*0.78, s*0.82, s*0.78, lw),
        lineAlpha(s*0.52, s*0.22, s*0.52, s*0.78, lw),
        // 书页线条
        lineAlpha(s*0.25, s*0.36, s*0.42, s*0.36, lw*0.55),
        lineAlpha(s*0.25, s*0.46, s*0.42, s*0.46, lw*0.55),
        lineAlpha(s*0.58, s*0.36, s*0.75, s*0.36, lw*0.55),
        lineAlpha(s*0.58, s*0.46, s*0.75, s*0.46, lw*0.55),
      )
    } else if (shape === 'user') {
      // 人物：头圆 + 身体弧
      alpha = Math.max(alpha,
        circleAlpha(s*0.50, s*0.30, s*0.16, lw*0.9),
        // 身体半圆
        lineAlpha(s*0.24, s*0.78, s*0.76, s*0.78, lw),
        lineAlpha(s*0.24, s*0.58, s*0.24, s*0.78, lw*0.8),
        lineAlpha(s*0.76, s*0.58, s*0.76, s*0.78, lw*0.8),
      )
      // 肩膀曲线
      const shX = s * 0.50, shY = s * 0.55, shR = s * 0.26
      alpha = Math.max(alpha, circleAlpha(shX, shY, shR, lw * 0.9) * (y < shY ? 1 : 0))
    }

    if (alpha <= 0) return [0, 0, 0, 0] // 透明
    const a = Math.min(255, Math.round(alpha * 255))
    return [fg[0], fg[1], fg[2], a]
  }
}

// ── 主程序 ─────────────────────────────────────────────
const outDir = path.resolve(__dirname, '..', 'src/static/tabbar')
if (!fs.existsSync(outDir)) fs.mkdirSync(outDir, { recursive: true })

const SIZE = 81
const GRAY   = '#8E8E93'
const INDIGO = '#6366F1'

const icons = ['home', 'plan', 'wallet', 'book', 'user']

icons.forEach(name => {
  // 普通态（灰色）
  const normalBuf = buildPng(SIZE, makeDrawFn(name, GRAY, false))
  fs.writeFileSync(path.join(outDir, `${name}.png`), normalBuf)

  // 激活态（靛蓝）
  const activeBuf = buildPng(SIZE, makeDrawFn(name, INDIGO, true))
  fs.writeFileSync(path.join(outDir, `${name}-active.png`), activeBuf)

  console.log(`✅ ${name}.png / ${name}-active.png`)
})

console.log('\n🎉 所有 TabBar PNG 图标生成完毕！')
console.log('   路径: src/static/tabbar/')
