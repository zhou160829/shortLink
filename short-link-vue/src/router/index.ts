import {createRouter, createWebHistory} from 'vue-router'
import Login from "@/components/login/login.vue";
import Home from "@/views/home.vue";

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

