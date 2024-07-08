//引入路由器
import router from "@/router";
//引入进度条
import progress from 'nprogress';
//引入用户相关的仓库
import useUserStore from '@/store/modules/user.ts';
//引入大仓库
import pinia from '@/store'
//引入进度条的样式
import "nprogress/nprogress.css"

let userStore = useUserStore(pinia);
//进度条的加载小圆球不要
progress.configure({showSpinner: false});
//存储用户未登录可以访问路由得路径
let whiteList = ['/login'];
router.beforeEach((to, from, next) => {
    //访问路由组件的之前,进度条开始动
    progress.start();
    //动态设置网页左上角的标题
    document.title = `短链接-${to.meta.title}`;
    //判断用户是否登录-token
    let token = userStore.userInfo.token;
    console.log(token)
    //用户登陆了
    if (token) {
        next();
    } else {
        //用户未登录
        if (whiteList.includes(to.path)) {
            next();
        } else {
            next({path: '/login'})
        }
    }
});

//后置路由
router.afterEach(() => {
    //访问路由组件成功,进度条消息
    progress.done();
})