import {createApp} from 'vue'
import './style.css'
import App from './App.vue'
import router from "./router/index.js";
import {DatePicker} from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';
import axios from "axios";
import {createPinia} from 'pinia'

const pinia = createPinia();

const app = createApp(App);
app.use(router)
app.use(DatePicker)
app.use(pinia)
app.config.globalProperties.$axios = axios;


app.mount('#app')
