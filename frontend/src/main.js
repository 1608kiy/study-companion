import { createApp } from 'vue'
import { createPinia } from 'pinia'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import './assets/main.css'

import {
  Aim, ArrowDown, Calendar, ChatDotRound, Check, CircleCheck, CircleClose, Close,
  DataAnalysis, Delete, Document, Expand, Finished, Fold, House,
  MagicStick, MoreFilled, Notebook, Plus, Promotion, Refresh, Setting,
  Share, SwitchButton, Timer, TrendCharts, Trophy, User,
  VideoPause, VideoPlay
} from '@element-plus/icons-vue'

const app = createApp(App)

const icons = {
  Aim, ArrowDown, Calendar, ChatDotRound, Check, CircleCheck, CircleClose, Close,
  DataAnalysis, Delete, Document, Expand, Finished, Fold, House,
  MagicStick, MoreFilled, Notebook, Plus, Promotion, Refresh, Setting,
  Share, SwitchButton, Timer, TrendCharts, Trophy, User,
  VideoPause, VideoPlay
}
for (const [key, component] of Object.entries(icons)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)

app.mount('#app')
