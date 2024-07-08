import {createRouter, createWebHistory} from 'vue-router'
import Login from "@/components/login/index.vue";
import Home from "@/views/home/index.vue";

export default createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            redirect: "Home"
        },
        {
            path: '/login',
            component: Login,
            name: 'Login',
            meta: {
                title: '登录页'
            }
        },
        {
            path: '/home',
            component: Home,
            name: 'Home',
            meta: {
                title: '首页'
            }
        }
    ]
})

