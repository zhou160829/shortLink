import {createRouter, createWebHistory} from 'vue-router'
import Login from "../views/login/Login.vue";
import Home from "../views/Home.vue";
import cookie from "js-cookie";

const routes = [
    {
        path: '/',
        redirect: "Home"
    },
    {
        path: '/login',
        component: Login,
        name: 'Login'
    },
    {
        path: '/home',
        component: Home,
        name: 'Home',
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes: routes,
})

router.beforeEach((to, from, next) => {
    const token = cookie.get("token")
    if (token === undefined && to.name !== 'Login') {
        next({name: 'Login'})
    } else next()
})

export default router