import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

dayjs.locale('zh-cn')

// 全局样式 - 必须在 Element Plus 之前引入，确保我们的样式优先级更高
import '@/styles/global.scss'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
