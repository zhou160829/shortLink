import {createMemoryHistory, createRouter} from 'vue-router'
import Login from "../views/login/Login.vue";

const routes = [
    {path: '/', component: Login},
]

const router = createRouter({
    history: createMemoryHistory(),
    routes,
})

export default router