module.exports = {
  // TypeScript 和 Vue 文件
  '*.{ts,tsx,vue}': [
    'eslint --fix',
    'prettier --write'
  ],
  // JavaScript 文件
  '*.{js,jsx}': [
    'eslint --fix',
    'prettier --write'
  ],
  // 样式文件
  '*.{scss,css}': [
    'prettier --write'
  ],
  // JSON 文件
  '*.{json,jsonc}': [
    'prettier --write'
  ],
  // Markdown 文件
  '*.md': [
    'prettier --write'
  ]
}
