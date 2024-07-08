import {createApp} from 'vue'
import '@/style/reset.scss'
import App from '@/App.vue'
import router from "./router/index.ts";
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';
import axios from "axios";
import pinia from '@/store'

const app = createApp(App);
app.use(router)
app.use(Antd)
app.use(pinia);
app.config.globalProperties.$axios = axios;

//引入路由鉴权的文件
import './permisstion'
app.mount('#app')
